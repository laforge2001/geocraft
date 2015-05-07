/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package beta.cop.algorithm.p2v;

import java.util.Arrays;

import beta.cop.model.grid.GridSampleIterator;
import beta.cop.model.grid.IRegularGrid;
import beta.cop.model.points.IPointSet3D;

/**
 * Instead of traversing the grid and repeatedly searching points for each
 * sample, this approach will instead traverse the list of points, putting the
 * values in the approximate locations in the volume and populating a given
 * distance around that point using a gaussian kernel.
 * 
 * The weights and values for overlapping regions are summed and the average is
 * returned if the weighting is greater than a threshold
 * 
 * @author georde
 * 
 */
public class WeightedInterpolationStrategy extends PointsToVolumeStrategy {

	private float[][][] _kernel;

	private final float[][][] _w; // sum of the weights

	private final float[][][] _wx; // sum of the weighted values

	int[] _localLengths;

	private final int _distance;

	private static final double WEIGHT_THRESHOLD = 0.001;

	/**
	 * @param points
	 * @param rgrid
	 */
	public WeightedInterpolationStrategy(IPointSet3D points,
			IRegularGrid rgrid, int distance) {
		super(points, rgrid);

		_distance = distance;
		int[] localLength = _rgrid.getLengths();

		_localLengths = new int[] { localLength[RGRID_XDIM],
				localLength[RGRID_YDIM], localLength[RGRID_ZDIM] };
		_w = new float[_localLengths[0]][_localLengths[1]][_localLengths[2]];
		_wx = new float[_localLengths[0]][_localLengths[1]][_localLengths[2]];
		// _kernel = GaussianKernelMaker.gaussian3D(1f, _distance);
		createKernel();
	}

	/**
	 * 
	 * @param i
	 *            x index
	 * @param j
	 *            y index
	 * @param k
	 *            z index
	 * @return weighted value for the new volume
	 */
	private float getValue(int i, int j, int k) {
		try {
			if (_w[i][j][k] > WEIGHT_THRESHOLD) {
				return _wx[i][j][k] / _w[i][j][k];
			}
			return 0.0f;
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("_w dimensions: " + _w.length + " "
					+ _w[0].length + " " + _w[0][0].length);
			System.out.println("i j k: " + i + " " + j + " " + k);
			throw e;
		}
	}

	/**
	 * creates the volume using the calculated weights
	 */
	private void createVol() {
		int[] pos;

		GridSampleIterator giter = new GridSampleIterator(_rgrid);
		while (giter.hasNext()) {
			pos = giter.next();
			giter.putSampleFloat(getValue(pos[RGRID_XDIM], pos[RGRID_YDIM],
					pos[RGRID_ZDIM]));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cop.velocity.points.PointsToVolumeStrategy#interpolate()
	 */
	@Override
	public void interpolate(String attributeOfInterest) {
		System.out.println("LOCAL LENGTHS: "
				+ Arrays.toString(_rgrid.getLocalLengths()));
		System.out.println("GLOBAL LENGTHS: "
				+ Arrays.toString(_rgrid.getLengths()));
		for (int i = 0; i < _points.size(); ++i) {
			computeWeights(i, attributeOfInterest);
			_progressPercentage = i / (float) _points.size() * 100.0f;
		}
		createVol();
	}

	public void computeWeights(int index, String attributeOfInterest) {

		float attr = _points.getAttributeValue(index, attributeOfInterest);

		long[] loc = _points.getIndexLocation(index);
		int[] ix = new int[3];
		ix[RGRID_XDIM] = (int) loc[0];
		ix[RGRID_YDIM] = (int) loc[1];
		ix[RGRID_ZDIM] = (int) loc[2];

		// verify the position is a local position
		if (_rgrid.isPositionLocal(ix)) {
			// use local position on current node - if operating distributed
			ix = _rgrid.localPosition(ix);
			final int i = ix[RGRID_XDIM];
			final int j = ix[RGRID_YDIM];
			final int k = ix[RGRID_ZDIM];

			int is = Math.max(0, i - _distance);
			int ie = Math.min(i + _distance, _localLengths[0]);

			final int js = Math.max(0, j - _distance);
			final int je = Math.min(j + _distance, _localLengths[1]);

			final int ks = Math.max(0, k - _distance);
			final int ke = Math.min(k + _distance, _localLengths[2]);

			for (int ii = is; ii < ie; ii++) {
				for (int jj = js; jj < je; jj++) {
					for (int kk = ks; kk < ke; kk++) {
						float wt = _kernel[Math.abs(ii - i)][Math.abs(jj - j)][Math
								.abs(kk - k)];
						_wx[ii][jj][kk] = _wx[ii][jj][kk] + wt * attr;
						_w[ii][jj][kk] = _w[ii][jj][kk] + wt;
					}
				}
			}
		}
	}

	private double getDist(int index, float delta) {
		return index * delta * index * delta;
	}

	public void createKernel() {
		_kernel = new float[_distance + 1][_distance + 1][_distance + 1];

		float iDelta = (float) _rgrid.getDeltas()[RGRID_XDIM];// refVol.getInlineDelta();
		float xDelta = (float) _rgrid.getDeltas()[RGRID_YDIM];// refVol.getXlineDelta();
		float zDelta = (float) _rgrid.getDeltas()[RGRID_ZDIM];// verticalStep;

		for (int i = 0; i <= _distance; i++) {
			for (int j = 0; j <= _distance; j++) {
				for (int k = 0; k <= _distance; k++) {
					double dist = Math.sqrt(i * i + j * j + k * k);
					if (dist <= _distance) {
						_kernel[i][j][k] = (float) Math.pow(Math.E,
								-(dist * dist) / _distance);
					}
				}
			}
		}

	}
}
