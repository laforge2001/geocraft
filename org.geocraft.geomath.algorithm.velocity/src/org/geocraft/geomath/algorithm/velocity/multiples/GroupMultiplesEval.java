/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.velocity.multiples;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.geocraft.core.factory.model.Grid3dFactory;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.geomath.algorithm.velocity.horizonstretch.HorizonStretchAlgorithm;
import org.geocraft.internal.geomath.algorithm.velocity.VelocityArrayTimeDepthConverter.Method;


public class GroupMultiplesEval {

  /** Input horizon layers */
  private Grid3d[] _layers;

  /** The Input velocity volume */
  private PostStack3d _velVol;

  /** The conversion Method (Cell-Based) or (Knee-Based) */
  private Method _conversionMethod;

  /** The output prefix name. */
  private String _outputPrefixName;

  /** The set # of multiples. */
  private int _setNo;

  /** Output horizon Properties */
  private Grid3d[] _outputProperties;

  /** Output types */
  private int[][] _outTypes;

  /** The progress object. */
  private IProgressMonitor _monitor = new NullProgressMonitor();

  /** The output horizon property. */
  private Grid3d _outputHorizon = null;

  /**
   * Determine mulitples
   *
   * @param layers horizon layers.
   * @param outputPrefixName name for the output horizons.
   */
  private Grid3d multiples(final Grid3d[] layers, final PostStack3d velVol, final Method conversionMethod,
      final int setNo, final String outputPrefixName, final ILogger logger) {
    logger.debug("running GroupMultiplesImpl.java");
    _layers = layers;
    _velVol = velVol;
    _conversionMethod = conversionMethod;
    _setNo = setNo;
    _outputPrefixName = outputPrefixName;

    _outputHorizon = runGroupMultiples(logger);

    return _outputHorizon;
  }

  /**
   * Loops through volume and selects each trace associated with a point in the horizon. The trace
   * is flooded and saved in new volume.
   */
  private Grid3d runGroupMultiples(final ILogger logger) {

    Grid3d outputHorizon = null;

    try {

      int numInputHors = _layers.length;
      PostStack3d velVolume = _velVol;
      Method convMethod = _conversionMethod;
      AreaOfInterest inAreaOfInterest = null;
      Grid3d[] layers = _layers;

      // Save the initial input property
      Grid3d inputProp1 = layers[0];

      // Determine the initial value of the domain for property1
      inputProp1.getDataUnit();
      Domain prop1Domain = inputProp1.getDataDomain();

      int setNo = _setNo;

      // Make sure the number of input Horizon is correct for Group set Selected
      if (setNo == 1) {
        if (numInputHors < 1 || numInputHors > 5) {
          logger
              .error("You cannot have no more than 5 horizons for Group set #1 when executing the multiples algorithms");
          return null;
        }
      } else {
        if (numInputHors < 1 || numInputHors > 3) {
          logger
              .error("You cannot have no more than 3 horizons for Group set #2 when executing the multiples algorithms");
          return null;
        }
      }

      // Initialize properties
      Grid3d prop1 = layers[0];
      Grid3d prop2 = null;
      Grid3d prop3 = null;
      Grid3d prop4 = null;
      Grid3d prop5 = null;

      // Determine the Output Horizons to create and set the input properties
      int numOutputHors = 0;
      int numOutputTypes = 0;
      if (setNo == 1) {
        if (numInputHors == 1) {
          numOutputHors = 3;
          prop1 = layers[0];
        } else if (numInputHors == 2) {
          numOutputHors = 10;
          prop1 = layers[0];
          prop2 = layers[1];
        } else if (numInputHors == 3) {
          numOutputHors = 18;
          //          numOutputHors = 5;
          prop1 = layers[0];
          prop2 = layers[1];
          prop3 = layers[2];
        } else if (numInputHors == 4) {
          numOutputHors = 25;
          prop1 = layers[0];
          prop2 = layers[1];
          prop3 = layers[2];
          prop4 = layers[3];
        } else if (numInputHors == 5) {
          numOutputHors = 33;
          prop1 = layers[0];
          prop2 = layers[1];
          prop3 = layers[2];
          prop4 = layers[3];
          prop5 = layers[4];
        }
        numOutputTypes = 7;
        _outTypes = new int[][] {
            { 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5 },
            { 1, 1, 1, 2, 2, 1, 1, 2, 2, 2, 3, 1, 1, 2, 2, 3, 3, 2, 4, 4, 1, 1, 2, 2, 2, 1, 1, 2, 2, 3, 3, 2, 5 },
            { 0, 1, 1, 0, 2, 0, 1, 0, 2, 1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 4, 0, 1, 0, 2, 1, 0, 1, 0, 1, 0, 1, 0, 0 },
            { 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 2, 2, 1, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 2, 2, 1, 4 },
            { 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } };
      } else if (setNo == 2) {
        if (numInputHors == 1) {
          numOutputHors = 2;
          numOutputTypes = 5;
          _outTypes = new int[][] { { 1, 1 }, { 1, 1 }, { 0, 1 }, { 0, 0 }, { 0, 0 } };
          // set the property
          prop1 = layers[0];
        } else if (numInputHors == 2) {
          numOutputHors = 11;
          numOutputTypes = 5;
          _outTypes = new int[][] { { 1, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2 }, { 1, 1, 2, 2, 1, 1, 2, 2, 2, 2, 2 },
              { 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2 }, { 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1 },
              { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 } };
          // set the properties
          prop1 = layers[0];
          prop2 = layers[1];
        } else if (numInputHors == 3) {
          numOutputHors = 37;
          numOutputTypes = 5;
          _outTypes = new int[][] {
              { 1, 2, 2, 2, 3, 3, 3, 3, 3, 3, 1, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
                  3, 3, 3 },
              { 1, 1, 2, 2, 1, 2, 2, 3, 3, 3, 1, 1, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
                  3, 3, 3 },
              { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 1, 1, 1, 2, 2, 2, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3,
                  3, 3, 3 },
              { 0, 0, 0, 1, 0, 0, 1, 0, 1, 2, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 1, 2, 0, 0, 1, 2, 2, 0, 0, 0,
                  1, 1, 2 },
              { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 1, 0, 1, 2,
                  1, 2, 2 } };
          // set the properties
          prop1 = layers[0];
          prop2 = layers[1];
          prop3 = layers[2];
        }
        // Error if number of input horizons is invalid
      } else {
        numOutputHors = 0;
        logger.error("The set number for the multiples group is invalid!");
        _monitor.done();
        return null;
      }

      boolean lastHorizon = false;

      GridGeometry3d hor1 = prop1.getGeometry();
      // Get size of grid
      int nRows1 = prop1.getNumRows();
      int nCols1 = prop1.getNumColumns();
      // convert series data to a float multi-dimension array
      float[][] gvals1 = prop1.getValues();
      // Initialize units
      Unit horStretchUnits = prop1.getDataUnit();
      if (horStretchUnits.getDomain() == Domain.DISTANCE) {
        horStretchUnits = UnitPreferences.getInstance().getTimeUnit();
      }

      int nRows2 = 0;
      int nCols2 = 0;
      float[][] gvals2 = null;
      if (prop2 != null) {
        // Get size of grid
        nRows2 = prop2.getNumRows();
        nCols2 = prop2.getNumColumns();
        // convert series data to a float multi-dimension array
        gvals2 = prop2.getValues();
      }

      int nRows3 = 0;
      int nCols3 = 0;
      float[][] gvals3 = null;
      if (prop3 != null) {
        // Get size of grid
        nRows3 = prop3.getNumRows();
        nCols3 = prop3.getNumColumns();
        // convert series data to a float multi-dimension array
        gvals3 = prop3.getValues();
      }

      int nRows4 = 0;
      int nCols4 = 0;
      float[][] gvals4 = null;
      if (prop4 != null) {
        // Get size of grid
        nRows4 = prop4.getNumRows();
        nCols4 = prop4.getNumColumns();
        // convert series data to a float multi-dimension array
        gvals4 = prop4.getValues();
      }

      int nRows5 = 0;
      int nCols5 = 0;
      float[][] gvals5 = null;
      if (prop5 != null) {
        // Get size of grid
        nRows5 = prop5.getNumRows();
        nCols5 = prop5.getNumColumns();
        // convert series data to a float multi-dimension array
        gvals5 = prop5.getValues();
      }

      int nRows = nRows1;
      int nCols = nCols1;

      // set the null value
      double nullvalue = prop1.getNullValue();

      IRepository repository = ServiceProvider.getRepository();

      // Begin the progress bar
      _monitor.beginTask("Group Multiples", (numInputHors + numOutputHors) * nRows);

      // Convert all depth properties to time
      for (int i = 0; i < numInputHors; ++i) {
        if (layers[i].getDataDomain() == Domain.DISTANCE) {

          // Set up to run horizon stretch
          HorizonStretchAlgorithm horizonStretchTask = new HorizonStretchAlgorithm();

          // run Horizon stretch (depth to time)
          Grid3d d2tResult = horizonStretchTask.runHorizonStretch(layers[i], velVolume, convMethod, inAreaOfInterest,
              layers[i].getDisplayName() + "_d2t", logger, null);
          layers[i] = d2tResult; // Set Input horizon data

          GridGeometry3d d2tGeometry = d2tResult.getGeometry();
          // save results from the horizon stretch
          if (i < 1) {
            prop1 = d2tResult;
            hor1 = d2tGeometry;
            gvals1 = d2tResult.getValues();
          } else if (i < 2) {
            prop2 = d2tResult;
            gvals2 = d2tResult.getValues();
          } else if (i < 3) {
            prop3 = d2tResult;
            gvals3 = d2tResult.getValues();
          } else if (i < 4) {
            prop4 = d2tResult;
            gvals4 = d2tResult.getValues();
          } else if (i < 5) {
            prop5 = d2tResult;
            gvals5 = d2tResult.getValues();
          }
        }
        _monitor.worked(nRows);
        if (_monitor.isCanceled()) {
          break;
        }
      }

      for (int horNumOut = 0; horNumOut < numOutputHors; ++horNumOut) {
        int type1 = 0;
        int type2 = 0;
        int type3 = 0;
        int type4 = 0;
        int type5 = 0;
        int type6 = 0;
        int type7 = 0;
        if (numOutputTypes == 5) {
          type1 = _outTypes[0][horNumOut];
          type2 = _outTypes[1][horNumOut];
          type3 = _outTypes[2][horNumOut];

          type5 = _outTypes[3][horNumOut];
          type6 = _outTypes[4][horNumOut];
        } else if (numOutputTypes == 7) {
          type1 = _outTypes[0][horNumOut];
          type2 = _outTypes[1][horNumOut];
          type3 = _outTypes[2][horNumOut];
          type4 = _outTypes[3][horNumOut];

          type5 = _outTypes[4][horNumOut];
          type6 = _outTypes[5][horNumOut];
          type7 = _outTypes[6][horNumOut];
        }
        String outputSuffixName = String.format("%d%d%d%d_%d%d%d", type1, type2, type3, type4, type5, type6, type7);
        if (type3 == 0) {
          outputSuffixName = String.format("%d%d_%d", type1, type2, type5);
        } else if (type4 == 0) {
          outputSuffixName = String.format("%d%d%d_%d%d", type1, type2, type3, type5, type6);
        }
        String outputName = _outputPrefixName + "_" + outputSuffixName;

        //create a new grid2d object
        GridGeometry3d horResult = new GridGeometry3d(outputName, hor1);

        float[][] result = new float[nRows][nCols];

        boolean isNull = true;

        // Perform the multiple
        for (int i1 = 0; i1 < nRows; i1++) {
          for (int i2 = 0; i2 < nCols; i2++) {

            isNull = false;
            // Initialize to the first Horizon
            if (type1 == 1) {
              if (prop1.isNull(i1, i2)) {
                result[i1][i2] = 0;
                isNull = true;
              } else {
                result[i1][i2] = gvals1[i1][i2];
              }
              // Initialize to the 2nd Horizon
            } else if (type1 == 2) {
              if (i1 >= nRows2 || i2 >= nCols2) {
                result[i1][i2] = 0;
                isNull = true;
              } else if (prop2.isNull(i1, i2)) {
                result[i1][i2] = 0;
                isNull = true;
              } else {
                result[i1][i2] = gvals2[i1][i2];
              }
              // Initialize to the 3rd Horizon
            } else if (type1 == 3) {
              if (i1 >= nRows3 || i2 >= nCols3) {
                result[i1][i2] = 0;
                isNull = true;
              } else if (prop3.isNull(i1, i2)) {
                result[i1][i2] = 0;
                isNull = true;
              } else {
                result[i1][i2] = gvals3[i1][i2];
              }
              // Initialize to the 4th Horizon
            } else if (type1 == 4) {
              if (i1 >= nRows4 || i2 >= nCols4) {
                result[i1][i2] = 0;
                isNull = true;
              } else if (prop4.isNull(i1, i2)) {
                result[i1][i2] = 0;
                isNull = true;
              } else {
                result[i1][i2] = gvals4[i1][i2];
              }
              // Initialize to the 5th Horizon
            } else if (type1 == 5) {
              if (i1 >= nRows5 || i2 >= nCols5) {
                result[i1][i2] = 0;
                isNull = true;
              } else if (prop5.isNull(i1, i2)) {
                result[i1][i2] = 0;
                isNull = true;
              } else {
                result[i1][i2] = gvals5[i1][i2];
              }
            } else {
              isNull = true;
            }

            int cType = 2;
            if (!isNull) {
              for (int i3 = 2; i3 < 5; i3++) {
                // determine the current type number
                if (i3 == 2) {
                  cType = type2;
                } else if (i3 == 3) {
                  cType = type3;
                } else if (i3 == 4) {
                  cType = type4;
                }
                // Add in the first Horizon
                if (cType == 1) {
                  if (prop1.isNull(i1, i2)) {
                    isNull = true;
                  } else {
                    result[i1][i2] += gvals1[i1][i2];
                  }
                  // Add in the 2nd Horizon
                } else if (cType == 2) {
                  if (i1 < nRows2 && i2 < nCols2) {
                    if (prop2.isNull(i1, i2)) {
                      isNull = true;
                    } else {
                      result[i1][i2] += gvals2[i1][i2];
                    }
                  }
                  // Add in the 3rd Horizon
                } else if (cType == 3) {
                  if (i1 < nRows3 && i2 < nCols3) {
                    if (prop3.isNull(i1, i2)) {
                      isNull = true;
                    } else {
                      result[i1][i2] += gvals3[i1][i2];
                    }
                  }
                  // Add in the 4th Horizon
                } else if (cType == 4) {
                  if (i1 < nRows4 && i2 < nCols4) {
                    if (prop4.isNull(i1, i2)) {
                      isNull = true;
                    } else {
                      result[i1][i2] += gvals4[i1][i2];
                    }
                  }
                  // Add in the 5th Horizon
                } else if (cType == 5) {
                  if (i1 < nRows5 && i2 < nCols5) {
                    if (prop5.isNull(i1, i2)) {
                      isNull = true;
                    } else {
                      result[i1][i2] += gvals5[i1][i2];
                    }
                  }
                }
              }
            }

            if (!isNull) {
              for (int i3 = 5; i3 < 8; i3++) {
                // determine the current type number
                if (i3 == 5) {
                  cType = type5;
                } else if (i3 == 6) {
                  cType = type6;
                } else if (i3 == 7) {
                  cType = type7;
                }
                // Subtract out the first Horizon
                if (cType == 1) {
                  if (prop1.isNull(i1, i2)) {
                    isNull = true;
                  } else {
                    result[i1][i2] -= gvals1[i1][i2];
                  }
                  // Subtract out the 2nd Horizon
                } else if (cType == 2) {
                  if (i1 < nRows2 && i2 < nCols2) {
                    if (prop2.isNull(i1, i2)) {
                      isNull = true;
                    } else {
                      result[i1][i2] -= gvals2[i1][i2];
                    }
                  }
                  // Subtract out the 3rd Horizon
                } else if (cType == 3) {
                  if (i1 < nRows3 && i2 < nCols3) {
                    if (prop3.isNull(i1, i2)) {
                      isNull = true;
                    } else {
                      result[i1][i2] -= gvals3[i1][i2];
                    }
                  }
                  // Subtract out the 4th Horizon
                } else if (cType == 4) {
                  if (i1 < nRows4 && i2 < nCols4) {
                    if (prop4.isNull(i1, i2)) {
                      isNull = true;
                    } else {
                      result[i1][i2] -= gvals4[i1][i2];
                    }
                  }
                }
              }
            }

            // set the output to null if any of the inputs are null
            if (isNull) {
              result[i1][i2] = (float) nullvalue;
            }
          }
          _monitor.worked(1);
          if (_monitor.isCanceled()) {
            break;
          }
        }

        Grid3d propResult = null;
        // Convert everything to a depth horizon if property #1 was in depth
        if (prop1Domain == Domain.DISTANCE) {

          // Create the new property
          Grid3d tempResult = Grid3dFactory.create(repository, inputProp1, result, outputName, horResult,
              horStretchUnits);
          tempResult.update();

          // Set up to run horizon stretch
          HorizonStretchAlgorithm horizonStretchTask = new HorizonStretchAlgorithm();

          // run Horizon stretch (time to depth)
          propResult = horizonStretchTask.runHorizonStretch(tempResult, velVolume, convMethod, inAreaOfInterest,
              outputName, logger, repository);
          // determine the horizon
          horResult = propResult.getGeometry();
        } else {
          // Create the new property
          propResult = Grid3dFactory.create(repository, prop1, result, outputName, horResult, prop1.getDataUnit());
          propResult.update();
        }

        // Unload the grid to save memory.
        propResult.unload();

        if (horNumOut == numOutputHors - 1) {
          lastHorizon = true;
        }

        // Just return the last horizon property
        if (lastHorizon) {
          outputHorizon = propResult;
        }
        // We now done with the created multiple
        result = null;
        propResult = null;
      }
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Error occurred when executing the multiples algorithm", e);
      _monitor.done();
      return null;
    }

    _monitor.done();
    return outputHorizon;
  }

  /**
   * Return a horizon property given its horizon type number
   */
  public Grid3d getProperty(final int typeWanted1, final int typeWanted2, final int typeWanted3, final int typeWanted4,
      final int typeWanted5) {

    // These horizons are equivalent to an horizon that was outputted
    int[][] dupTypes = new int[][] {
        { 1, 1, 2, 2, 1, 1, 1, 2, 1, 2, 2, 1, 1, 1, 1, 2, 2, 3, 1, 1, 2, 2, 2, 2, 2, 2, 2, 3, 2, 2, 1, 3, 1, 1, 2, 3,
            2, 2, 3, 3, 3, 2, 3, 2, 2, 3, 3, 3 },
        { 2, 3, 3, 3, 1, 2, 2, 1, 2, 2, 2, 1, 3, 2, 3, 1, 3, 1, 2, 3, 3, 2, 3, 2, 2, 3, 3, 2, 2, 3, 3, 1, 3, 3, 3, 2,
            3, 3, 2, 2, 3, 3, 2, 3, 3, 3, 3, 3 },
        { 0, 0, 0, 0, 2, 1, 2, 2, 2, 1, 2, 3, 1, 3, 2, 3, 1, 2, 3, 2, 1, 3, 2, 3, 3, 2, 2, 2, 3, 2, 3, 3, 3, 3, 3, 3,
            3, 3, 3, 3, 2, 3, 3, 3, 3, 3, 3, 3 },
        { 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0,
            0, 1, 0, 1, 1, 1, 1, 0, 1, 1, 2, 2 },
        { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 2, 0, 0,
            1, 0, 1, 0, 0, 1, 1, 2, 2, 0, 0, 1 } };

    // These horizons are equivalent to an horizon that may have been outputted
    int[][] matchTypes = new int[][] {
        { 2, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
            3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3 },
        { 1, 1, 2, 2, 1, 1, 2, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3,
            3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3 },
        { 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 2, 2,
            2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3 },
        { 0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 2, 0, 0,
            0, 0, 0, 0, 0, 1, 1, 2, 2, 0, 0, 1 },
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0,
            1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 2, 2 } };

    Grid3d outputHorizon = null;

    // determine if the horizon type wanted had been outputted
    int horNumOut = 0;
    boolean foundHorizon = false;
    while (!foundHorizon && horNumOut < _outputProperties.length) {
      int type1 = _outTypes[0][horNumOut];
      int type2 = _outTypes[1][horNumOut];
      int type3 = _outTypes[2][horNumOut];
      if (type1 == typeWanted1 && type2 == typeWanted2 && type3 == typeWanted3) {
        int type4 = _outTypes[3][horNumOut];
        int type5 = _outTypes[4][horNumOut];
        if (type4 == typeWanted4 && type5 == typeWanted5) {
          outputHorizon = _outputProperties[horNumOut];
          foundHorizon = true;
        }
      }
      horNumOut++;
    }

    // determine if input horizon property type matches an horizon that was outputted
    if (!foundHorizon) {
      int matchType1 = 0;
      int matchType2 = 0;
      int matchType3 = 0;
      int matchType4 = 0;
      int matchType5 = 0;
      int dupNum = 0;

      // determine if type wanted is one of duplicate horizon types
      boolean duplicateFound = false;
      while (!duplicateFound && dupNum < dupTypes[0].length) {
        int dupType1 = dupTypes[0][dupNum];
        int dupType2 = dupTypes[1][dupNum];
        int dupType3 = dupTypes[2][dupNum];
        if (typeWanted1 == dupType1 && typeWanted2 == dupType2 && typeWanted3 == dupType3) {
          int dupType4 = dupTypes[3][dupNum];
          int dupType5 = dupTypes[4][dupNum];
          if (typeWanted4 == dupType4 && typeWanted5 == dupType5) {
            matchType1 = matchTypes[0][dupNum];
            matchType2 = matchTypes[1][dupNum];
            matchType3 = matchTypes[2][dupNum];
            matchType4 = matchTypes[3][dupNum];
            matchType5 = matchTypes[4][dupNum];
            duplicateFound = true;
          }
        }
        dupNum++;
      }

      // if duplicate Horizon type found then determine if the horizon was outputted
      if (duplicateFound) {
        horNumOut = 0;
        while (!foundHorizon && horNumOut < _outputProperties.length) {
          int type1 = _outTypes[0][horNumOut];
          int type2 = _outTypes[1][horNumOut];
          int type3 = _outTypes[2][horNumOut];
          if (type1 == matchType1 && type2 == matchType2 && type3 == matchType3) {
            int type4 = _outTypes[3][horNumOut];
            int type5 = _outTypes[4][horNumOut];
            if (type4 == matchType4 && type5 == matchType5) {
              outputHorizon = _outputProperties[horNumOut];
              foundHorizon = true;
            }
          }
          horNumOut++;
        }

        // if duplicate not found then just return Invalid property Type
      } else {
        outputHorizon = null;
        return outputHorizon;
      }
    }
    return outputHorizon;
  }

  /**
   * @throws CoreException  
   */
  public Grid3d compute(final IProgressMonitor monitor, final ILogger logger, final IRepository repository,
      final GroupMultiplesAlgorithm algorithm) throws CoreException {
    _monitor = monitor;
    // Get field values required by the algorithm
    Grid3d[] inputHorizons = algorithm.getInputHorizons();
    PostStack3d velocityVolume = algorithm.getVelocityVolume();

    Grid3d outputHorizon = multiples(inputHorizons, algorithm.getVelocityVolume(), algorithm.getConversionMethod(),
        algorithm.getGroupNum(), algorithm.getOutHorizonPrefix(), logger);

    return outputHorizon;

  }
}
