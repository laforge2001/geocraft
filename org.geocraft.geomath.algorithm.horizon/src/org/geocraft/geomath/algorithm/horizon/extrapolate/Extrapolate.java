/*
 * Copyright (C) ConocoPhillips 2009 All Rights Reserved.
 */
package org.geocraft.geomath.algorithm.horizon.extrapolate;


import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.factory.model.Grid3dFactory;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.aoi.AreaOfInterestHelper;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.ComboField;


public class Extrapolate extends StandaloneAlgorithm {

  // UI TYPES
  // Input section
  /** Input horizon */
  private EntityProperty<Grid3d> _inputHorizon;

  /** The use-area-of-interest property. */
  protected final BooleanProperty _useAOI;

  /** The area-of-interest property. */
  protected final EntityProperty<AreaOfInterest> _areaOfInterest;

  /** The number of rows to extrapolate (going up) */
  private IntegerProperty _topDistance;

  /** The number of rows to extrapolate (going down) */
  private IntegerProperty _bottomDistance;

  /** The number of columns to extrapolate (to the left */
  private IntegerProperty _leftDistance;

  /** The number of columns to extrapolate (to the right) */
  private IntegerProperty _rightDistance;

  /** The output horizon name */
  private StringProperty _outputHorizonName;

  /** The output grid comments property. */
  public StringProperty _outputComments;

  public Extrapolate() {
    super();
    _inputHorizon = addEntityProperty("Horizon", Grid3d.class);
    _useAOI = addBooleanProperty("Use Area of Interest", false);
    _areaOfInterest = addEntityProperty("Area of Interest", AreaOfInterest.class);
    _topDistance = addIntegerProperty("Top Distance", 5);
    _bottomDistance = addIntegerProperty("Bottom Distance", 5);
    _leftDistance = addIntegerProperty("Left Distance", 5);
    _rightDistance = addIntegerProperty("Right Distance", 5);
    _outputHorizonName = addStringProperty("Name", "e1");
    _outputComments = addStringProperty("Output Comments", "");
  }

  @Override
  public void propertyChanged(String key) {
    if (key.equals(_inputHorizon.getKey()) && _inputHorizon.get() != null) {
      String outputName = _inputHorizon.get().getMapper()
          .createOutputDisplayName(_inputHorizon.get().getDisplayName(), "_e1");
      _outputHorizonName.set(outputName);
    }
  }

  @Override
  public void validate(IValidation results) {

    // Validate the input grid is non-null and of the correct type.
    if (_inputHorizon.isNull()) {
      results.error(_inputHorizon, "No input grid specified.");
    }

    // Validate the output name is non-zero length.
    if (_outputHorizonName.isEmpty()) {
      results.error(_outputHorizonName, "No output grid name specified.");
    }

    // Check if an entry already exists in the datastore.
    if (!_inputHorizon.isNull() && !_outputHorizonName.isEmpty()) {
      if (Grid3dFactory.existsInStore(_inputHorizon.get(), _outputHorizonName.get())) {
        results.warning(_outputHorizonName, "Exists in datastore and will be overwritten.");
      }
    }
  }

  /* (non-Javadoc)
   * Construct the algorithm's UI consisting of form fields partitioned into sections: Input,
   * Output, and algorithm Parameters.
   * @see org.geocraft.algorithm.StandaloneAlgorithm#buildView(org.geocraft.algorithm.IModelForm)
   */
  @Override
  public void buildView(IModelForm modelForm) {
    // Build the input parameters section.
    FormSection inputSection = modelForm.addSection("Input", false);
    inputSection.addEntityComboField(_inputHorizon, Grid3d.class);
    ComboField areaOfInterest = inputSection.addEntityComboField(_areaOfInterest, AreaOfInterest.class);
    areaOfInterest.setTooltip("Select an area-of-interest (optional).");
    areaOfInterest.showActiveFieldToggle(_useAOI);

    FormSection parametersSection = modelForm.addSection("Parameters", false);
    parametersSection.addTextField(_topDistance);
    parametersSection.addTextField(_bottomDistance);
    parametersSection.addTextField(_leftDistance);
    parametersSection.addTextField(_rightDistance);

    FormSection outputSection = modelForm.addSection("Output", false);
    outputSection.addTextField(_outputHorizonName);
    outputSection.addTextBox(_outputComments);
  }

  /**
   * Runs the domain logic of the algorithm.
   * @param monitor the progress monitor.
   * @param logger the logger to log messages.
   * @param repository the repository in which to add output entities.
   */

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) {

    // Unpack the model parameters.
    Grid3d property = _inputHorizon.get();
    AreaOfInterest areaOfInterest = null;
    if (_useAOI.get()) {
      areaOfInterest = _areaOfInterest.get();
    }
    int topDistance = _topDistance.get();
    int bottomDistance = _bottomDistance.get();
    int leftDistance = _leftDistance.get();
    int rightDistance = _rightDistance.get();

    String propertyName = _outputHorizonName.get();
    String outputComments = _outputComments.get();

    // determine the geometry of the input property
    GridGeometry3d geometry = property.getGeometry();
    int nWorkItems = geometry.getNumRows() * 4;

    // Start the progress monitor.
    monitor.beginTask("Extrapolate...", nWorkItems);

    // clip the horizon
    float[][] result = extrapolateHor(geometry, areaOfInterest, property, topDistance, bottomDistance, leftDistance,
        rightDistance, monitor);
    // Create the new property
    Grid3d newProperty = Grid3dFactory.create(repository, property, result, propertyName);

    // add comments to the output grid
    if (!monitor.isCanceled()) {
      newProperty.setComment(property.getComment() + "\n" + outputComments);
    }
    try {
      newProperty.update();
    } catch (IOException ex) {
      throw new RuntimeException(ex.getMessage());
    }

    // Task is done.
    monitor.done();
  }

  /**
   * extrapolate the horizon
   */
  public static float[][] extrapolateHor(final GridGeometry3d geometry, final AreaOfInterest inputAOI,
      final Grid3d prop1, final int topDist, final int bottomDist, final int rightDist, final int leftDist,
      IProgressMonitor monitor) {

    // Get size of hor1
    int nRows = geometry.getNumRows();
    int nCols = geometry.getNumColumns();

    // convert series data to a double presision multi-dimension array
    float[][] gvals1 = prop1.getValues();

    // Initialize to extrapolate
    float[][] gvals2 = new float[nRows][nCols];
    float[][] gvalsw = new float[nRows][nCols];
    int[][] svals = new int[nRows][nCols];
    int[][] nullflgs = new int[nRows][nCols];
    double nullvalue = prop1.getNullValue();

    // Initialize
    for (int i1 = 0; i1 < nRows; i1++) {
      for (int i2 = 0; i2 < nCols; i2++) {
        svals[i1][i2] = 0;
        nullflgs[i1][i2] = 0;
      }
    }

    // extrapolate the output horizon (to the left)
    for (int i1 = 0; i1 < nRows && !monitor.isCanceled(); i1++) {
      for (int i2 = 0; i2 < nCols; i2++) {

        // Determine if the current point is null
        if (prop1.isNull(i1, i2)) {
          // set null flag
          nullflgs[i1][i2] = 1;

          // Flag values going to the left
          // determine the beginning column
          int i3 = i1;
          int ibx = i2;
          // determine the ending column
          int iex = i2 + leftDist;

          if (iex >= nCols) {
            iex = nCols - 1;
          }

          for (int i4 = ibx; i4 <= iex; i4 = i4 + 1) {
            svals[i3][i4] = 1;
          }
        }
      }
      // Update the progress monitor.
      monitor.worked(1);
    }

    // create work area & the output horizon with the input hor and status hor
    for (int i1 = 0; i1 < nRows; i1++) {
      for (int i2 = 0; i2 < nCols; i2++) {
        gvalsw[i1][i2] = gvals1[i1][i2];
        if (AreaOfInterestHelper.isInAreaOfInterest(inputAOI, geometry, i1, i2) && svals[i1][i2] == 1) {
          gvals2[i1][i2] = (float) nullvalue;
        } else {
          gvals2[i1][i2] = gvals1[i1][i2];
        }
      }
    }

    // extrapolate along +x
    boolean endLoop1 = false;
    boolean endLoop2 = false;
    boolean endLoop3 = false;
    for (int i1 = 0; i1 < nRows; i1++) {
      endLoop1 = false;
      int i2 = 0;
      while (!endLoop1) {
        if (svals[i1][i2] == 1) {
          int ibx = i2 + leftDist;
          int iex = nCols - 1;
          if (ibx > iex) {
            ibx = i2;
          }
          int i3 = i1;
          int i4 = ibx;
          endLoop2 = false;
          endLoop3 = false;
          double xextrap = gvalsw[i1][i2];
          while (!endLoop2) {
            if (nullflgs[i3][i4] == 1) {
              i4 = i4 + 1;
              if (i4 > iex) {
                endLoop1 = true;
                endLoop2 = true;
                endLoop3 = true;
              }
            } else {
              xextrap = gvalsw[i3][i4];
              endLoop2 = true;
            }
          }

          ibx = i2;
          iex = nCols - 1;
          i3 = i1;
          i4 = ibx;

          while (!endLoop3) {

            if (svals[i3][i4] == 0) {
              endLoop3 = true;
            } else {

              if (AreaOfInterestHelper.isInAreaOfInterest(inputAOI, geometry, i3, i4) && nullflgs[i3][i4] == 1) {
                gvals2[i3][i4] = (float) xextrap;
                gvalsw[i3][i4] = (float) xextrap;
                nullflgs[i3][i4] = 0;
              } else {
                gvals2[i3][i4] = gvalsw[i3][i4];
              }
              svals[i3][i4] = 0;
            }
            i4 = i4 + 1;
            if (i4 > iex) {
              endLoop3 = true;
            }
          }
        }
        i2 = i2 + 1;
        if (i2 >= nCols) {
          endLoop1 = true;
        }
      }
    }

    // extrapolate the output horizon (to the right)
    for (int i1 = 0; i1 < nRows && !monitor.isCanceled(); i1++) {
      for (int i2 = 0; i2 < nCols; i2++) {
        // Determine if the current point is null
        if (nullflgs[i1][i2] == 1) {

          // Flag values going to the right
          // determine the beginning column
          int ibx = i2;
          // determine the ending column
          int iex = i2 - rightDist;
          if (iex < 0) {
            iex = 0;
          }

          int i3 = i1;

          for (int i4 = ibx; i4 >= iex; i4 = i4 - 1) {
            svals[i3][i4] = 1;
          }
        }
      }
      // Update the progress monitor.
      monitor.worked(1);
    }

    // extrapolate along -x
    for (int i1 = 0; i1 < nRows; i1++) {
      endLoop1 = false;
      int i2 = nCols - 1;
      while (!endLoop1) {
        if (svals[i1][i2] == 1) {
          int ibx = i2 - rightDist;
          int iex = 0;
          if (ibx < iex) {
            ibx = i2;
          }
          int i3 = i1;
          int i4 = ibx;
          endLoop2 = false;
          endLoop3 = false;
          double xextrap = gvalsw[i1][i2];
          while (!endLoop2) {
            if (nullflgs[i3][i4] == 1) {
              i4 = i4 - 1;
              if (i4 < iex) {
                endLoop1 = true;
                endLoop2 = true;
                endLoop3 = true;
              }
            } else {
              xextrap = gvalsw[i3][i4];
              endLoop2 = true;
            }
          }

          ibx = i2;
          iex = nCols - 1;
          i3 = i1;
          i4 = ibx;

          while (!endLoop3) {

            if (svals[i3][i4] == 0) {
              endLoop3 = true;
            } else {
              if (AreaOfInterestHelper.isInAreaOfInterest(inputAOI, geometry, i3, i4) && nullflgs[i3][i4] == 1) {
                gvals2[i3][i4] = (float) xextrap;
                gvalsw[i3][i4] = (float) xextrap;
                nullflgs[i3][i4] = 0;
              } else {
                gvals2[i3][i4] = gvalsw[i3][i4];
              }
              svals[i3][i4] = 0;
            }
            i4 = i4 - 1;
            if (i4 < iex) {
              endLoop3 = true;
            }
          }
        }
        i2 = i2 - 1;
        if (i2 < 0) {
          endLoop1 = true;
        }
      }
    }

    // extrapolate the output horizon (going up)
    for (int i2 = 0; i2 < nCols && !monitor.isCanceled(); i2++) {
      for (int i1 = 0; i1 < nRows; i1++) {

        // Determine if the current point is null
        if (nullflgs[i1][i2] == 1) {

          // Flag values going up
          // determine the beginning row
          int iby = i1;
          int i4 = i2;
          // determine the ending row
          int iey = i1 + topDist;

          if (iey >= nRows) {
            iey = nRows - 1;
          }

          for (int i3 = iby; i3 <= iey; i3 = i3 + 1) {
            svals[i3][i4] = 1;
          }
        }
      }
      // Update the progress monitor.
      monitor.worked(1);
    }

    // extrapolate along +y
    for (int i2 = 0; i2 < nCols; i2++) {
      endLoop1 = false;
      int i1 = 0;
      while (!endLoop1) {
        if (svals[i1][i2] == 1) {
          int iby = i1 + topDist;
          int iey = nRows - 1;
          if (iby > iey) {
            iby = i1;
          }
          int i3 = iby;
          int i4 = i2;
          endLoop2 = false;
          endLoop3 = false;
          double yextrap = gvalsw[i1][i2];
          while (!endLoop2) {
            if (nullflgs[i3][i4] == 1) {
              i3 = i3 + 1;
              if (i3 > iey) {
                endLoop1 = true;
                endLoop2 = true;
                endLoop3 = true;
              }
            } else {
              yextrap = gvalsw[i3][i4];
              endLoop2 = true;
            }
          }

          iby = i1;
          iey = nRows - 1;
          i3 = iby;
          i4 = i2;

          while (!endLoop3) {

            if (svals[i3][i4] == 0) {
              endLoop3 = true;
            } else {
              if (AreaOfInterestHelper.isInAreaOfInterest(inputAOI, geometry, i3, i4) && nullflgs[i3][i4] == 1) {
                gvals2[i3][i4] = (float) yextrap;
                gvalsw[i3][i4] = (float) yextrap;
                nullflgs[i3][i4] = 0;
              } else {
                gvals2[i3][i4] = gvalsw[i3][i4];
              }
              svals[i3][i4] = 0;
            }
            i3 = i3 + 1;
            if (i3 > iey) {
              endLoop3 = true;
            }
          }
        }
        i1 = i1 + 1;
        if (i1 >= nRows) {
          endLoop1 = true;
        }
      }
    }

    // extrapolate the output horizon (going down)
    for (int i2 = 0; i2 < nCols && !monitor.isCanceled(); i2++) {
      for (int i1 = 0; i1 < nRows; i1++) {

        // Determine if the current point is null
        if (nullflgs[i1][i2] == 1) {

          // Flag values going to the right
          // determine the beginning column
          int iby = i1;
          // determine the ending column
          int iey = i1 - bottomDist;
          if (iey < 0) {
            iey = 0;
          }

          int i4 = i2;

          for (int i3 = iby; i3 >= iey; i3 = i3 - 1) {
            svals[i3][i4] = 1;
          }
        }
      }

      // Update the progress monitor.
      monitor.worked(1);
    }

    // extrapolate along -y
    for (int i2 = 0; i2 < nCols; i2++) {
      endLoop1 = false;
      int i1 = nRows - 1;
      while (!endLoop1) {
        if (svals[i1][i2] == 1) {
          int iby = i1 - bottomDist;
          int iey = 0;
          if (iby < iey) {
            iby = i1;
          }
          int i4 = i2;
          int i3 = iby;
          endLoop2 = false;
          endLoop3 = false;
          double yextrap = gvalsw[i1][i2];
          while (!endLoop2) {
            if (nullflgs[i3][i4] == 1) {
              i3 = i3 - 1;
              if (i3 < iey) {
                endLoop1 = true;
                endLoop2 = true;
                endLoop3 = true;
              }
            } else {
              yextrap = gvalsw[i3][i4];
              endLoop2 = true;
            }
          }

          iby = i1;
          iey = 0;
          i3 = iby;
          i4 = i2;

          while (!endLoop3) {

            if (svals[i3][i4] == 0) {
              endLoop3 = true;
            } else {
              if (AreaOfInterestHelper.isInAreaOfInterest(inputAOI, geometry, i3, i4) && nullflgs[i3][i4] == 1) {
                gvals2[i3][i4] = (float) yextrap;
                gvalsw[i3][i4] = (float) yextrap;
                nullflgs[i3][i4] = 0;
              } else {
                gvals2[i3][i4] = gvalsw[i3][i4];
              }
              svals[i3][i4] = 0;
            }
            i3 = i3 - 1;
            if (i3 < iey) {
              endLoop3 = true;
            }
          }
        }
        i1 = i1 - 1;
        if (i1 < 0) {
          endLoop1 = true;
        }
      }
    }

    return gvals2;
  }
}
