package org.geocraft.geomath.algorithm.horizon.tessellate;


import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.factory.model.Grid3dFactory;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.ComboField;


public class InterpolateTessellate extends StandaloneAlgorithm {

  /** The input grid property. */
  public EntityProperty<Grid3d> _inputGrid;

  /** The area-of-interest property. */
  public EntityProperty<AreaOfInterest> _areaOfInterest;

  /** The use-area-of-interest property. */
  protected final BooleanProperty _useAOI;

  /** The output grid name property. */
  public StringProperty _outputGridName;

  /** The output comments property. */
  public StringProperty _outputComments;

  public InterpolateTessellate() {
    _inputGrid = addEntityProperty("Input Grid", Grid3d.class);
    _areaOfInterest = addEntityProperty("Area-of-Interest", AreaOfInterest.class);
    _useAOI = addBooleanProperty("Use Area of Interest", false);
    _outputGridName = addStringProperty("Output Grid Name", "it1");
    _outputComments = addStringProperty("Output Comments", "");
  }

  @Override
  public void buildView(IModelForm form) {
    // Build the input parameters section.
    FormSection inputSection = form.addSection("Input", false);
    inputSection.addEntityComboField(_inputGrid, Grid3d.class);
    ComboField aoiField = inputSection.addEntityComboField(_areaOfInterest, AreaOfInterest.class);
    aoiField.showActiveFieldToggle(_useAOI);

    // Build the output parameters section.
    FormSection outputSection = form.addSection("Output", false);
    outputSection.addTextField(_outputGridName);
    outputSection.addTextBox(_outputComments);
  }

  @Override
  public void propertyChanged(String key) {
    if (key.equals(_inputGrid.getKey()) && _inputGrid.get() != null) {
      String outputName = _inputGrid.get().getMapper()
          .createOutputDisplayName(_inputGrid.get().getDisplayName(), "_it1");
      _outputGridName.set(outputName);
    }
  }

  @Override
  public void validate(IValidation results) {
    // Validate the input grid is non-null and of the correct type.
    if (_inputGrid.isNull()) {
      results.error(_inputGrid, "No input grid specified.");
    }

    if (_useAOI.get() && _areaOfInterest.isNull()) {
      results.error(_areaOfInterest, "No area of Interest specified.");
    }

    // Validate the output name is non-zero length.
    if (_outputGridName.isEmpty()) {
      results.error(_outputGridName, "No output grid name specified.");
    }

    // Check if an entry already exists in the datastore.
    if (!_inputGrid.isNull() && !_outputGridName.isEmpty()) {
      if (Grid3dFactory.existsInStore(_inputGrid.get(), _outputGridName.get())) {
        results.warning(_outputGridName, "Exists in datastore and will be overwritten.");
      }
    }
  }

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) {

    // Unpack the model parameters.
    Grid3d inputGrid = _inputGrid.get();
    AreaOfInterest aoi = null;
    if (_useAOI.get()) {
      aoi = _areaOfInterest.get();
    }
    String outputGridName = _outputGridName.get();
    String outputComments = _outputComments.get();

    // Start the progress monitor.
    monitor.beginTask("Extracting Area of Grid...", inputGrid.getGeometry().getNumRows());

    // Interpolate (tessellate) the grid data.
    float[][] outputData = interpolateGridData(inputGrid, aoi, monitor);

    // Only create the output grid if the job completed normally.
    if (!monitor.isCanceled()) {
      // Find (or create) the output grid and update it in the datastore.
      try {
        Grid3d outputGrid = Grid3dFactory.create(repository, inputGrid, outputData, outputGridName);
        outputGrid.setComment(inputGrid.getComment() + "\n" + outputComments);
        outputGrid.update();
      } catch (IOException ex) {
        throw new RuntimeException(ex.getMessage());
      }
    }

    // Task is done.
    monitor.done();
  }

  /**
   * Interpolate (tessellate) the grid data.
   */
  public float[][] interpolateGridData(final Grid3d inputGrid, AreaOfInterest aoi, IProgressMonitor monitor) {

    int numRows = inputGrid.getNumRows();
    int numCols = inputGrid.getNumColumns();
    float[][] inputData = inputGrid.getValues();
    float[][] outputData = new float[numRows][numCols];

    int numPoints = numRows * numCols;
    int numPoints3 = numPoints + 3;
    int numPoints4 = numPoints3 * 2 + 1;

    double[] dataX = new double[numPoints];
    double[] dataY = new double[numPoints];
    double[] dataG = new double[numPoints];
    double[] pnts1 = new double[numPoints3];
    double[] pnts2 = new double[numPoints3];
    double[] tetr1 = new double[numPoints4];
    double[] tetr2 = new double[numPoints4];
    double[] tetr3 = new double[numPoints4];
    double[] xPnts1 = { -1.0, 5.0, -1.0 };
    double[] xPnts2 = { -1.0, -1.0, 5.0 };
    double[] xPnts3 = { 2.0, 2.0, 18.0 };
    double[] det = new double[6];
    double nullvalue = inputGrid.getNullValue();

    // Determine min & max X & Y based on the corner points
    Point3d[] cornerPoints = inputGrid.getCornerPoints().getPointsDirect();
    double minX = cornerPoints[0].getX();
    double maxX = cornerPoints[1].getX();
    double minY = cornerPoints[0].getY();
    double maxY = cornerPoints[3].getY();

    // make sure the min & max values are correct
    for (Point3d cornerPoint : cornerPoints) {
      if (cornerPoint.getX() < minX) {
        minX = cornerPoint.getX();
      }
      if (cornerPoint.getX() > maxX) {
        maxX = cornerPoint.getX();
      }
      if (cornerPoint.getY() < minY) {
        minY = cornerPoint.getY();
      }
      if (cornerPoint.getY() > maxY) {
        maxY = cornerPoint.getY();
      }
    }

    double deltaX = (maxX - minX) / (numRows - 1);
    double deltaY = (maxY - minY) / (numCols - 1);
    double tolr = 0.00001;
    double zero = 0.0;
    int[] iStack = new int[numPoints4];
    int[] iTetr1 = new int[numPoints4];
    int[] iTetr2 = new int[numPoints4];
    int[] iTetr3 = new int[numPoints4];
    int[] iTetrFlg = new int[numPoints4];
    int[] iTemp = { 1, 2, 1, 3, 2, 3 };
    int[] kTetr1 = new int[numPoints];
    int[] kTetr2 = new int[numPoints];

    // initialize
    int nVals = 0;

    for (int row = 0; row < numRows && !monitor.isCanceled(); row++) {

      double yPnt = minY + row * deltaY;

      for (int col = 0; col < numCols; col++) {

        double xPnt = minX + col * deltaX;

        // determine Xval and Yval at the current point
        double[] xy = inputGrid.getGeometry().transformRowColToXY(row, col);
        double xLoc = xy[0];
        double yLoc = xy[1];

        // default output values
        outputData[row][col] = inputData[row][col];

        boolean processLocation = false;
        // Determine if the current location is in the area of interest
        if (inputGrid.isNull(row, col)) {
          processLocation = false;
        } else if (aoi == null) {
          processLocation = true;
        } else if (aoi.contains(xLoc, yLoc)) {
          processLocation = true;
        } else {
          processLocation = false;
        }

        // Process the current location
        if (processLocation) {
          dataY[nVals] = yPnt;
          dataX[nVals] = xPnt;
          dataG[nVals] = inputData[row][col];
          nVals++;
        }
      }
    }

    // initialize
    for (int i1 = 0; i1 < numPoints4 && !monitor.isCanceled(); i1++) {
      iTetrFlg[i1] = 0;
      iTetr1[i1] = -1;
      iTetr2[i1] = -1;
      iTetr3[i1] = -1;
      tetr1[i1] = 0.0;
      tetr2[i1] = 0.0;
      tetr3[i1] = 0.0;
      iStack[i1] = i1;
    }
    for (int i1 = 0; i1 < numPoints && !monitor.isCanceled(); i1++) {
      kTetr1[i1] = -1;
      kTetr2[i1] = -1;
    }

    // initialize arrays
    int nVals3 = 0;

    iTetr1[0] = nVals3;
    tetr1[0] = xPnts3[nVals3];

    pnts1[nVals3] = xPnts1[nVals3];
    pnts2[nVals3] = xPnts2[nVals3];
    nVals3++;

    iTetr2[0] = nVals3;
    tetr2[0] = xPnts3[nVals3];

    pnts1[nVals3] = xPnts1[nVals3];
    pnts2[nVals3] = xPnts2[nVals3];
    nVals3++;

    iTetr3[0] = nVals3;
    tetr3[0] = xPnts3[nVals3];
    iTetrFlg[0] = 1;

    pnts1[nVals3] = xPnts1[nVals3];
    pnts2[nVals3] = xPnts2[nVals3];
    nVals3++;

    // normalize the data
    double scaleX = maxX - minX;
    double scaleY = maxY - minY;

    for (int i1 = 0; i1 < nVals && !monitor.isCanceled(); i1++) {
      pnts1[nVals3] = (dataX[i1] - minX) / scaleX;
      pnts2[nVals3] = (dataY[i1] - minY) / scaleY;
      nVals3++;
    }

    // begin
    monitor.beginTask("Interpolating (Tessellating) Grid...", nVals);
    int iSp = 0;
    int id = 1;

    for (int i1 = 0; i1 < nVals && !monitor.isCanceled(); i1++) {

      int km = -1;
      int nuc = i1 + 3;

      // Loop thru the established triangles
      int ie = iSp + 1;

      for (int jt = 0; jt < ie; jt++) {

        boolean endLoop1 = false;

        // Test if new data point is within the circumcle of triangle "JT"
        double value = pnts1[nuc] - tetr1[jt];
        double dsq = tetr3[jt] - value * value;

        if (dsq < 0.0) {
          endLoop1 = true;
        }

        if (!endLoop1) {
          value = pnts2[nuc] - tetr2[jt];
          dsq = dsq - value * value;
          if (dsq < 0.0) {
            endLoop1 = true;
          }
        }

        // This data point is within the circumcle of the triangle "JT"
        // Delete this triangle but save its edges
        if (!endLoop1) {

          id = id - 1;
          if (id < 0) {
            id = 0;
          }
          iStack[id] = jt;

          // Add edges to ktetr but delete if already listed
          for (int i3 = 0; i3 < 3; i3++) {

            int it1 = i3 * 2;
            int l1 = iTemp[it1];
            int it2 = it1 + 1;
            int l2 = iTemp[it2];
            boolean addEdge = false;

            // add this edge to ktetr
            if (km < 0) {
              addEdge = true;
            } else {

              int kmt = km + 1;

              // The following loop checks to see if the edge is already listed.
              // If it is, then all references to the edge is deleted in KTETR
              // and KM is decremented
              boolean endLoop2 = false;
              int j1 = 0;

              while (!endLoop2) {

                boolean deleteEdge = true;
                int iTval1 = iTetr1[jt];

                if (l1 == 1) {
                  iTval1 = iTetr1[jt];
                } else if (l1 == 2) {
                  iTval1 = iTetr2[jt];
                } else if (l1 == 3) {
                  iTval1 = iTetr3[jt];
                }
                if (iTval1 != kTetr1[j1]) {
                  deleteEdge = false;
                } else {

                  int iTval2 = iTetr1[j1];

                  if (l2 == 1) {
                    iTval2 = iTetr1[jt];
                  } else if (l2 == 2) {
                    iTval2 = iTetr2[jt];
                  } else if (l2 == 3) {
                    iTval2 = iTetr3[jt];
                  }
                  if (iTval2 != kTetr2[j1]) {
                    deleteEdge = false;
                  }
                }

                // Edge is deleted in KTETR and KM is decremented
                if (deleteEdge) {

                  km = km - 1;
                  if (j1 > km) {
                    endLoop2 = true;
                    addEdge = false;
                  } else {

                    int kmt2 = km + 1;

                    for (int k1 = j1; k1 < kmt2; k1++) {
                      int k2 = k1 + 1;

                      kTetr1[k1] = kTetr1[k2];
                      kTetr2[k1] = kTetr2[k2];
                    }
                    endLoop2 = true;
                    addEdge = false;
                  }
                }

                // determine if we are at the end of the loop
                if (!endLoop2) {
                  j1++;
                  if (j1 >= kmt) {
                    endLoop2 = true;
                    addEdge = true;
                  }
                }
              }
            }

            // add this edge to ktetr
            if (addEdge) {

              km = km + 1;

              int iTval1 = iTetr1[jt];

              if (l1 == 1) {
                iTval1 = iTetr1[jt];
              } else if (l1 == 2) {
                iTval1 = iTetr2[jt];
              } else if (l1 == 3) {
                iTval1 = iTetr3[jt];
              }
              kTetr1[km] = iTval1;

              int iTval2 = iTetr1[jt];

              if (l2 == 1) {
                iTval2 = iTetr1[jt];
              } else if (l2 == 2) {
                iTval2 = iTetr2[jt];
              } else if (l2 == 3) {
                iTval2 = iTetr3[jt];
              }
              kTetr2[km] = iTval2;
            }
          }
        }
      }

      // Form new triangles
      int kmt = km + 1;

      if (kmt < 1) {
        kmt = 1;
      }

      for (int i3 = 0; i3 < kmt; i3++) {

        int kt = iStack[id];

        id = id + 1;

        // Calculate the circumcircle center and radius
        // squared of points ktetr and place in tetr
        int i4 = kTetr1[i3];

        det[0] = pnts1[i4] - pnts1[nuc];
        det[1] = pnts2[i4] - pnts2[nuc];
        det[2] = det[0] * (pnts1[i4] + pnts1[nuc]) / 2.0 + det[1] * (pnts2[i4] + pnts2[nuc]) / 2.0;
        i4 = kTetr2[i3];
        det[3] = pnts1[i4] - pnts1[nuc];
        det[4] = pnts2[i4] - pnts2[nuc];
        det[5] = det[3] * (pnts1[i4] + pnts1[nuc]) / 2.0 + det[4] * (pnts2[i4] + pnts2[nuc]) / 2.0;

        double dd = det[0] * det[4] - det[1] * det[3];

        tetr1[kt] = (det[2] * det[4] - det[5] * det[1]) / dd;
        tetr2[kt] = (det[0] * det[5] - det[3] * det[2]) / dd;
        double value1 = pnts1[nuc] - tetr1[kt];
        double value2 = pnts2[nuc] - tetr2[kt];
        tetr3[kt] = value1 * value1 + value2 * value2;
        iTetrFlg[kt] = 1;

        // add this triangle to the list of triangles in ITETR
        iTetr1[kt] = kTetr1[i3];
        iTetr2[kt] = kTetr2[i3];
        iTetr3[kt] = nuc;
      }

      // We now have 2 new triangles
      iSp = iSp + 2;

      // Make sure values have been initialized
      while (iTetrFlg[iSp] != 1) {
        iSp = iSp - 1;
      }
      monitor.worked(1);
    }

    // The tessellation was performed using one triangle
    // with location (-1,-1) (5,-1) (-1,5).
    // We don't want any references to these points in the information
    // returned to the calling program, so form new triangle vertices
    // in "IPOL" which do not reference these 3 points
    int[] iPol1 = new int[numPoints4];
    int[] iPol2 = new int[numPoints4];
    int[] iPol3 = new int[numPoints4];
    int nPol = 0;
    int ie = iSp + 1;
    boolean endLoop = false;

    for (int i4 = 0; i4 < ie && !monitor.isCanceled(); i4++) {

      int i1 = iTetr1[i4];
      int i2 = iTetr2[i4];
      int i3 = iTetr3[i4];

      endLoop = false;
      if (i1 < 3 | i2 < 3 | i3 < 3) {
        endLoop = true;
      }
      if (!endLoop) {
        iPol1[nPol] = iTetr1[i4] - 3;
        iPol2[nPol] = iTetr2[i4] - 3;
        iPol3[nPol] = iTetr3[i4] - 3;
        nPol = nPol + 1;
      }
    }

    for (int ip = 0; ip < nPol && !monitor.isCanceled(); ip++) {

      double x1 = dataX[iPol1[ip]];
      double y1 = dataY[iPol1[ip]];
      double z1 = dataG[iPol1[ip]];
      double x2 = dataX[iPol2[ip]];
      double y2 = dataY[iPol2[ip]];
      double z2 = dataG[iPol2[ip]];
      double x3 = dataX[iPol3[ip]];
      double y3 = dataY[iPol3[ip]];
      double z3 = dataG[iPol3[ip]];
      double xMinT = Math.min(x1, x2);
      double xMin3 = Math.min(xMinT, x3);
      double xMaxT = Math.max(x1, x2);
      double xMax3 = Math.max(xMaxT, x3);
      double yMinT = Math.min(y1, y2);
      double yMin3 = Math.min(yMinT, y3);
      double yMaxT = Math.max(y1, y2);
      double yMax3 = Math.max(yMaxT, y3);
      int ibx = (int) Math.rint((xMin3 - minX) / deltaX) - 1;

      if (ibx < 0) {
        ibx = 0;
      }

      int iby = (int) Math.rint((yMin3 - minY) / deltaY) - 1;

      if (iby < 0) {
        iby = 0;
      }

      int iex = (int) Math.rint((xMax3 - minX) / deltaX) + 1;

      if (iex > numCols) {
        iex = numCols;
      }

      int iey = (int) Math.rint((yMax3 - minY) / deltaY) + 1;

      if (iey > numRows) {
        iey = numRows;
      }

      // perform the interpolation
      for (int i1 = iby; i1 < iey && !monitor.isCanceled(); i1++) {

        double y4 = minY + i1 * deltaY;

        for (int i2 = ibx; i2 < iex; i2++) {

          double x4 = minX + i2 * deltaX;
          double z4 = nullvalue;

          double a1 = y2 - y1;
          double a2 = y3 - y2;
          double a3 = y1 - y3;
          double b1 = x1 - x2;
          double b2 = x2 - x3;
          double b3 = x3 - x1;
          double c1 = x2 * y1 - x1 * y2;
          double c2 = x3 * y2 - x2 * y3;
          double c3 = x1 * y3 - x3 * y1;

          int iFlag = 0;

          double val1 = x4 * a1 + y4 * b1 + c1;
          double val2 = x3 * a1 + y3 * b1 + c1;

          if (val1 >= zero && val2 >= zero) {
            iFlag = iFlag + 1;
          } else if (val1 <= zero && val2 <= zero) {
            iFlag = iFlag + 1;
          }

          val1 = x4 * a2 + y4 * b2 + c2;
          val2 = x1 * a2 + y1 * b2 + c2;
          if (val1 >= zero && val2 >= zero) {
            iFlag = iFlag + 1;
          } else if (val1 <= zero && val2 <= zero) {
            iFlag = iFlag + 1;
          }

          val1 = x4 * a3 + y4 * b3 + c3;
          val2 = x2 * a3 + y2 * b3 + c3;
          if (val1 >= zero && val2 >= zero) {
            iFlag = iFlag + 1;
          } else if (val1 <= zero && val2 <= zero) {
            iFlag = iFlag + 1;
          }

          // Now interpolate to find z4
          boolean isCenter = false;

          if (iFlag == 3) {

            double aa1 = x1 - x2;
            double aa2 = y1 - y2;
            double aa3 = z1 - z2;
            double bb1 = x1 - x3;
            double bb2 = y1 - y3;
            double bb3 = z1 - z3;
            double cc1 = aa2 * bb3 - aa3 * bb2;
            double cc2 = aa3 * bb1 - aa1 * bb3;
            double cc3 = aa1 * bb2 - aa2 * bb1;

            if (cc3 >= zero - tolr && cc3 <= zero + tolr) {
              isCenter = false;
            } else {
              isCenter = true;

              double cc4 = -(cc1 * x1 + cc2 * y1 + cc3 * z1);

              z4 = -(cc1 * x4 + cc2 * y4 + cc4) / cc3;
            }
          }

          // Save the z4 value
          if (inputGrid.isNull(i1, i2) && isCenter) {
            outputData[i1][i2] = (float) z4;
          }
        }
      }
    }
    monitor.done();

    return outputData;
  }
}
