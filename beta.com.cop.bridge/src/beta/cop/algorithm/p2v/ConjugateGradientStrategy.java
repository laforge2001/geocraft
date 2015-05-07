/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package beta.cop.algorithm.p2v;

import beta.cop.model.grid.GridSampleIterator;
import beta.cop.model.grid.IRegularGrid;
import beta.cop.model.math.ConjugateSolver;
import beta.cop.model.points.IPointSet3D;

public class ConjugateGradientStrategy extends PointsToVolumeStrategy {

	private static double THRESHOLD = 0.1;

	private int iEqn;

	private int iEnt;

	private int nEqn;

	private int nEnt;

	/**
	 * @param points
	 * @param rgrid
	 */
	public ConjugateGradientStrategy(IPointSet3D points, IRegularGrid rgrid) {
		super(points, rgrid);
	}

	private void setPercentComplete(float percent) {
		_progressPercentage = percent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.cop.prowess.tool.pointstovolume.dev.PointsToVolumeStrategy#interpolate
	 * (java.lang.String)
	 */
	@Override
	public void interpolate(String attributeOfInterest) {

		int nVol;

		int[] localLength = _rgrid.getLocalLengths();

		int n1 = localLength[RGRID_XDIM];
		int n2 = localLength[RGRID_YDIM];
		int n3 = localLength[RGRID_ZDIM];

		nVol = n1 * n2 * n3;

		float[] data = new float[nVol];
		float[] mask = new float[nVol];
		float[] dataout = new float[nVol];

		populateInputData(data, mask, attributeOfInterest, n1, n2);

		int nVolnew;

		float[] datanew;

		float[] dataold = null;

		float[] masknew;

		int n1new = 0, n2new = 0, n3new = 0;

		int n1old = 0, n2old = 0, n3old = 0;

		do {
			if (dataold == null) {
				/* start with simple 4x4x4 grid */
				n1new = Math.min(4, n1);
				n2new = Math.min(4, n2);
				n3new = Math.min(4, n3);
			} else {
				/* sample doubling */
				n1new = Math.min(n1new * 2, n1);
				n2new = Math.min(n2new * 2, n2);
				n3new = Math.min(n3new * 2, n3);
			}

			nVolnew = n1new * n2new * n3new;

			if (nVolnew == nVol) {
				datanew = data;
				masknew = mask;
			} else {
				datanew = new float[nVolnew];
				masknew = new float[nVolnew];
				post2newgrid(data, mask, n1, n2, n3, datanew, masknew, n1new,
						n2new, n3new);
			}

			// creat parameter indices, and count number of inversion parameters
			int[] parIndex = new int[nVolnew];
			int nPar = 0;
			for (int i1 = 0; i1 < nVolnew; ++i1) {
				if (masknew[i1] < THRESHOLD) {
					parIndex[i1] = nPar;
					nPar++;
				} else {
					parIndex[i1] = -1;
				}
			}

			System.out.printf("%d %d %d %d\n", n1new, n2new, n3new, nPar);

			if (nPar != 0) {

				try {
					// max number of possible equations
					nEqn = nVolnew;

					// max number of possible entries
					long maxNumberTest = 7 * nPar;

					// if we've gone past Java's integer limit, we cannot
					// allocate the entries using arrays so break out
					if (maxNumberTest > Integer.MAX_VALUE) {
						break;
					}

					nEnt = 7 * nPar;

					float[] a = new float[nEnt];
					int[] iRow = new int[nEnt];
					int[] iCol = new int[nEnt];
					float[] x = new float[nPar];
					float[] b = new float[nEqn];

					float penalty1 = 1;

					float penalty2 = 1;

					float penalty3 = 1;

					/*
					 * set all equations
					 */
					setAllEquations(a, iRow, iCol, b, datanew, parIndex, n1new,
							n2new, n3new, penalty1, penalty2, penalty3);

					/*
					 * set initial value inverted from last iteration
					 */
					setInitX(x, parIndex, n1new, n2new, n3new, dataold, n1old,
							n2old, n3old);

					/*
					 * Conjugate Gradient solver to interpolate
					 */
					ConjugateSolver.CGsolver(a, iRow, iCol, nEnt, b, nEqn, x,
							nPar, 20, (float) 1.0e-4);

					/*
					 * put values that need to be iterpolated
					 */
					fillParmHoles(datanew, parIndex, x, nVolnew);
				} catch (OutOfMemoryError e) {
					System.out
							.println("Hit memory limit... outputting what we have and aborting");
					e.printStackTrace();
					break;
				} catch (NegativeArraySizeException e) {
					System.out
							.println("We've overflowed!... outputting what we have and aborting");
					e.printStackTrace();
					break;
				}

			}

			parIndex = null;
			masknew = null;

			// free old coarser grid data if not first iteration
			if (dataold != null) {
				dataold = null;
			}

			// save current grid data for next iteration
			interpTriLinear(dataout, n1, n2, n3, datanew, n1new, n2new, n3new);

			dataold = datanew;
			n1old = n1new;
			n2old = n2new;
			n3old = n3new;

			setPercentComplete(nVolnew / nVol * 100f);

		} while (nVolnew != nVol);

		populateOutputGrid(dataout, n1, n2);

		setPercentComplete(100f);
		data = null;
	}

	/**
	 * @param dataout
	 * @param n1
	 * @param n2
	 */
	private void populateOutputGrid(float[] dataout, int n1, int n2) {
		GridSampleIterator giterOut = new GridSampleIterator(_rgrid);
		int[] pos;
		while (giterOut.hasNext()) {
			pos = giterOut.next();
			if (_rgrid.isPositionLocal(pos)) {
				giterOut.putSampleFloat(dataout[(pos[RGRID_ZDIM] * n2 + pos[RGRID_YDIM])
						* n1 + pos[RGRID_XDIM]]);
			}
		}
	}

	/**
	 * @param data
	 * @param mask
	 */
	private void populateInputData(float[] data, float[] mask,
			String attributeOfInterest, int n1, int n2) {
		// create a temporary copy of input grid for setting up input values
		IRegularGrid tempGrid = _rgrid.createCopy();

		// populate regular grid with points in their exact xyz position
		new DirectInsertStrategy(_points, tempGrid)
				.interpolate(attributeOfInterest);

		int[] pos;
		GridSampleIterator giter = new GridSampleIterator(tempGrid);
		while (giter.hasNext()) {
			pos = giter.next();
			if (_rgrid.isPositionLocal(pos)) {
				pos = _rgrid.localPosition(pos);
				float value = giter.getSampleFloat();
				data[(pos[RGRID_ZDIM] * n2 + pos[RGRID_YDIM]) * n1
						+ pos[RGRID_XDIM]] = value;
				if (Float.compare(0.0f, value) != 0) {
					mask[(pos[RGRID_ZDIM] * n2 + pos[RGRID_YDIM]) * n1
							+ pos[RGRID_XDIM]] = 1f;
				}
			}
		}
		System.out.println("Finished loading samples!!");
		tempGrid = null;
	}

	private void post2newgrid(float[] data, float[] mask, int n1, int n2,
			int n3, float[] datanew, float[] masknew, int n1new, int n2new,
			int n3new) {
		float r1, r2, r3;
		int i1, j, k;
		int ii, jj, kk;
		int index, indexnew;

		r1 = (float) n1new / n1;
		r2 = (float) n2new / n2;
		r3 = (float) n3new / n3;

		for (k = 0; k < n3; k++) {
			kk = (int) (k * r3);
			for (j = 0; j < n2; j++) {
				jj = (int) (j * r2);
				for (i1 = 0; i1 < n1; i1++) {
					ii = (int) (i1 * r1);
					index = (k * n2 + j) * n1 + i1;
					indexnew = (kk * n2new + jj) * n1new + ii;
					datanew[indexnew] += data[index] * mask[index];
					masknew[indexnew] += mask[index];
				}
			}
		}

		for (i1 = 0; i1 < n1new * n2new * n3new; i1++) {
			if (masknew[i1] < THRESHOLD) {
				datanew[i1] = 0.0f;
				masknew[i1] = 0.0f;
			} else {
				float value = datanew[i1] / masknew[i1];
				datanew[i1] = value;
				masknew[i1] = 1.0f;
			}
		}
	}

	void fillParmHoles(float[] data, int[] parIndex, float[] x, int n) {
		for (int i1 = 0; i1 < n; i1++) {
			if (parIndex[i1] >= 0) {
				data[i1] = x[parIndex[i1]];
			}
		}
	}

	void setInitX(float[] x, int[] parIndex, int n1new, int n2new, int n3new,
			float[] dataold, int n1old, int n2old, int n3old) {

		int ii, jj, kk;
		int indexnew;
		float r1, r2, r3;

		r1 = (float) n1old / n1new;
		r2 = (float) n2old / n2new;
		r3 = (float) n3old / n3new;

		for (int k = 0; k < n3new; k++) {
			kk = (int) (k * r3);
			for (int j = 0; j < n2new; j++) {
				jj = (int) (j * r2);
				for (int i1 = 0; i1 < n1new; i1++) {
					indexnew = (k * n2new + j) * n1new + i1;
					if (parIndex[indexnew] >= 0) {
						if (dataold == null) {
							x[parIndex[indexnew]] = 0.0f;
						} else {
							// simple nearest neighbour interpolation, and let
							// CG fine-tune it
							ii = (int) (i1 * r1);
							x[parIndex[indexnew]] = dataold[(kk * n2old + jj)
									* n1old + ii];
						}
					}
				}
			}
		}
	}

	void interpTriLinear(float[] dataout, int n1out, int n2out, int n3out,
			float[] datain, int n1in, int n2in, int n3in) {
		int i1, j, k;
		int ii, jj, kk;
		float r1, r2, r3;
		int iip, jjp, kkp;
		float fi, fj, fk;
		int indexout;

		r1 = (float) n1in / n1out;
		r2 = (float) n2in / n2out;
		r3 = (float) n3in / n3out;

		for (k = 0; k < n3out; k++) {
			fk = k * r3;
			kk = (int) fk;
			fk -= kk;
			kkp = Math.min(kk + 1, n3in - 1);
			for (j = 0; j < n2out; j++) {
				fj = j * r2;
				jj = (int) fj;
				fj -= jj;
				jjp = Math.min(jj + 1, n2in - 1);
				for (i1 = 0; i1 < n1out; i1++) {
					indexout = (k * n2out + j) * n1out + i1;
					fi = i1 * r1;
					ii = (int) fi;
					fi -= ii;
					iip = Math.min(ii + 1, n1in - 1);

					dataout[indexout] = (float) (datain[(kk * n2in + jj) * n1in
							+ ii]
							* (1.0 - fi)
							* (1.0 - fj)
							* (1.0 - fk)
							+ datain[(kk * n2in + jj) * n1in + iip]
							* fi
							* (1.0 - fj)
							* (1.0 - fk)
							+ datain[(kk * n2in + jjp) * n1in + ii]
							* (1.0 - fi)
							* fj
							* (1.0 - fk)
							+ datain[(kk * n2in + jjp) * n1in + iip]
							* fi
							* fj
							* (1.0 - fk)
							+ datain[(kkp * n2in + jj) * n1in + ii]
							* (1.0 - fi)
							* (1.0 - fj)
							* fk
							+ datain[(kkp * n2in + jj) * n1in + iip]
							* fi
							* (1.0 - fj)
							* fk
							+ datain[(kkp * n2in + jjp) * n1in + ii]
							* (1.0 - fi) * fj * fk + datain[(kkp * n2in + jjp)
							* n1in + iip]
							* fi * fj * fk);
				}
			}
		}
	}

	void setAllEquations(float[] a, int[] iRow, int[] iCol, float[] b,
			float[] data, int[] parIndex, int n1, int n2, int n3,
			float penalty1, float penalty2, float penalty3) {

		int i1, j, k;
		int ip1, jp1, kp1;
		int im1, jm1, km1;
		int index;
		int indexp1;
		int indexm1;
		int iCenter;
		int count;

		iEnt = iEqn = 0;

		for (k = 0; k < n3; k++) {
			kp1 = Math.min(k + 1, n3 - 1);
			km1 = Math.max(k - 1, 0);
			for (j = 0; j < n2; j++) {
				jp1 = Math.min(j + 1, n2 - 1);
				jm1 = Math.max(j - 1, 0);
				for (i1 = 0; i1 < n1; i1++) {
					ip1 = Math.min(i1 + 1, n1 - 1);
					im1 = Math.max(i1 - 1, 0);

					index = (k * n2 + j) * n1 + i1;

					// remember the index of center point
					iCenter = iEnt;

					// if center point has no value, increment iEnt
					if (parIndex[index] != -1) {
						iEnt++;
					}

					// set equation building count;
					count = 0;

					// equation in the ist dimension
					indexp1 = (k * n2 + j) * n1 + ip1;

					indexm1 = (k * n2 + j) * n1 + im1;
					count += setEquation(a, iRow, iCol, b, data, parIndex,
							indexm1, index, indexp1, penalty1, iCenter, iEqn);

					// equation in the 2nd dimension

					indexp1 = (k * n2 + jp1) * n1 + i1;
					indexm1 = (k * n2 + jm1) * n1 + i1;
					count += setEquation(a, iRow, iCol, b, data, parIndex,
							indexm1, index, indexp1, penalty2, iCenter, iEqn);

					// equation in the 3rd dimension
					indexp1 = (kp1 * n2 + j) * n1 + i1;
					indexm1 = (km1 * n2 + j) * n1 + i1;
					count += setEquation(a, iRow, iCol, b, data, parIndex,
							indexm1, index, indexp1, penalty3, iCenter, iEqn);

					// if can not add any equation, set entry index back
					if (count == 0) {
						iEnt = iCenter;
					} else {
						iEqn++;
					}
				}
			}
		}

		nEqn = iEqn;
		nEnt = iEnt;
	}

	int setEquation(float[] a, int[] iRow, int[] iCol, float[] b, float[] data,
			int[] parIndex, int indexm1, int index, int indexp1, float penalty,
			int iCenter, int localIEqn) {
		// if no inversion point, do nothing
		if (parIndex[indexm1] == -1 && parIndex[index] == -1
				&& parIndex[indexp1] == -1) {
			return 0;
		}

		// if same point, do nothing
		if (parIndex[index] == parIndex[indexm1]
				&& parIndex[index] == parIndex[indexp1]) {
			return 0;
		}

		if (parIndex[indexm1] == -1) {
			b[localIEqn] -= data[indexm1] * penalty;
		} else {
			a[iEnt] = penalty;
			iRow[iEnt] = localIEqn;
			iCol[iEnt] = parIndex[indexm1];
			iEnt++;
		}

		// center of differtial equation should be accumulated to reduce size of
		// A or b
		if (parIndex[index] == -1) {
			b[localIEqn] += 2.0 * data[index] * penalty;
		} else {
			a[iCenter] -= 2.0 * penalty;
			iRow[iCenter] = localIEqn;
			iCol[iCenter] = parIndex[index];
		}

		if (parIndex[indexp1] == -1) {
			b[localIEqn] -= data[indexp1] * penalty;
		} else {
			a[iEnt] = penalty;
			iRow[iEnt] = localIEqn;
			iCol[iEnt] = parIndex[indexp1];
			iEnt++;
		}

		return 1;
	}
}
