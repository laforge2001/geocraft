package org.geocraft.geomath.algorithm.horizon.interpolate;


import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.factory.model.Grid3dFactory;
import org.geocraft.core.model.aoi.AreaOfInterest;
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
import org.geocraft.ui.form2.field.EntityComboField;


public class LinearInterpolation extends StandaloneAlgorithm {

  /** The input grid property. */
  public EntityProperty<Grid3d> _inputGrid;

  /** The area-of-interest property. */
  public EntityProperty<AreaOfInterest> _areaOfInterest;

  /** The area-of-interest usage property. */
  public BooleanProperty _useAreaOfInterest;

  /** The max. # of rows to fill property. */
  public IntegerProperty _maxRowsToFill;

  /** The max. # of columns to fill property. */
  public IntegerProperty _maxColsToFill;

  /** The output grid name property. */
  public StringProperty _outputGridName;

  /** The output comments property. */
  public StringProperty _outputComments;

  public LinearInterpolation() {
    _inputGrid = addEntityProperty("Input Grid", Grid3d.class);
    _areaOfInterest = addEntityProperty("Area-of-Interest", AreaOfInterest.class);
    _useAreaOfInterest = addBooleanProperty("Use Area-of-Interest", false);
    _maxRowsToFill = addIntegerProperty("Maximum # of Rows to Fill", 0);
    _maxColsToFill = addIntegerProperty("Maximum # of Columns to Fill", 0);
    _outputGridName = addStringProperty("Output Grid Name", "");
    _outputComments = addStringProperty("Output Comments", "");
  }

  @Override
  public void buildView(IModelForm form) {
    // Build the input parameters section.
    FormSection inputSection = form.addSection("Input", false);
    inputSection.addEntityComboField(_inputGrid, Grid3d.class);
    EntityComboField aoi = inputSection.addEntityComboField(_areaOfInterest, AreaOfInterest.class);
    aoi.showActiveFieldToggle(_useAreaOfInterest);

    FormSection interpolationSection = form.addSection("Interpolation", false);
    interpolationSection.addTextField(_maxRowsToFill).setTooltip(
        "Enter the maximum # of rows to fill (or 0 for max possible)");
    interpolationSection.addTextField(_maxColsToFill).setTooltip(
        "Enter the maximum # of columns to fill (or 0 for max possible)");

    // Build the output parameters section.
    FormSection outputSection = form.addSection("Output", false);
    outputSection.addTextField(_outputGridName);
    outputSection.addTextBox(_outputComments);
  }

  @Override
  public void propertyChanged(String key) {
    // Auto-generate an output name from the input grid.
    if (key.equals(_inputGrid.getKey())) {
      if (!_inputGrid.isNull()) {
        String outputName = _inputGrid.get().getMapper()
            .createOutputDisplayName(_inputGrid.get().getDisplayName(), "_linterp");
        _outputGridName.set(outputName);
      }
    }
  }

  @Override
  public void validate(IValidation results) {
    // Validate the input grid is non-null and of the correct type.
    if (_inputGrid.isNull()) {
      results.error(_inputGrid, "No input grid specified.");
    }

    if (_useAreaOfInterest.get() && _areaOfInterest.isNull()) {
      results.error(_areaOfInterest, "No input grid specified.");
    }

    // Validate the # of rows and columns to fill.
    if (_maxRowsToFill.get() < 0) {
      results.error(_maxRowsToFill, "The max. # of rows to fill must be >= 0.");
    }
    if (_maxColsToFill.get() < 0) {
      results.error(_maxColsToFill, "The max. # of columns to fill must be >= 0.");
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

  /**
   * @throws CoreException  
   */
  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) throws CoreException {

    // Unpack the model parameters.
    Grid3d inputGrid = _inputGrid.get();
    AreaOfInterest aoi = _areaOfInterest.get();
    if (!_useAreaOfInterest.get()) {
      aoi = null;
    }
    int maxRowsToFill = _maxRowsToFill.get();
    int maxColsToFill = _maxColsToFill.get();
    String outputGridName = _outputGridName.get();
    String outputComments = _outputComments.get();

    // Start the progress monitor.
    monitor.beginTask("Linar Interpolation of Grid...", inputGrid.getGeometry().getNumRows());

    // Interpolate (linear) the grid data.
    float[][] outputData = interpolateGridData(inputGrid, aoi, maxRowsToFill, maxColsToFill, monitor);

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

  private float[][] interpolateGridData(final Grid3d inputGrid, final AreaOfInterest aoi, final int maxRowsToFill,
      final int maxColsToFill, final IProgressMonitor monitor) {

    // Get size of horizon1
    int numRows = inputGrid.getNumRows();
    int numCols = inputGrid.getNumColumns();

    // convert series data to a double precision multi-dimension array
    float[][] inputData = inputGrid.getValues();
    float[][] outputData = new float[numRows][numCols];
    double nullvalue = inputGrid.getNullValue();

    // Make sure the number of rows to fill and columns to fill are greater than 0
    int numRowsToFill = maxRowsToFill;
    int numColsToFill = maxColsToFill;

    if (numRowsToFill <= 0) {
      numRowsToFill = numRows;
    }
    if (numColsToFill <= 0) {
      numColsToFill = numCols;
    }

    // Interpolate null values.
    for (int row = 0; row < numRows && !monitor.isCanceled(); row++) {

      for (int col = 0; col < numCols; col++) {

        // Determine x,y at the current location.
        double[] xy = inputGrid.getGeometry().transformRowColToXY(row, col);
        double x = xy[0];
        double y = xy[1];

        // Default output value.
        outputData[row][col] = inputData[row][col];

        boolean processLocation = false;

        // Determine if the current location is in the area of interest.
        if (aoi == null) {
          processLocation = true;
        } else if (aoi.contains(x, y)) {
          processLocation = true;
        } else {
          processLocation = false;
        }

        // Process the current location only if it is null.
        if (processLocation) {
          if (inputGrid.isNull(row, col)) {
            processLocation = true;
          } else {
            processLocation = false;
          }
        }

        // Process the current location.
        if (processLocation) {

          // Now look back to find the nearest row.
          int iby = row - 1;
          if (iby < 0) {
            iby = 0;
          }
          int iey = row - numRowsToFill / 2;
          if (iey < 0) {
            iey = 0;
          }
          int i3 = iby;
          int i4 = col;

          double yval1 = nullvalue;
          int idisty1 = 0;

          if (iey <= iby) {

            boolean endFlag = false;

            while (!endFlag) {

              if (inputGrid.isNull(i3, i4)) {
                i3 = i3 - 1;
                if (i3 < iey) {
                  endFlag = true;
                }
              } else {
                yval1 = inputData[i3][i4];
                idisty1 = row - i3;
                endFlag = true;
              }
            }
          }

          double yval2 = nullvalue;
          int idisty2 = 0;

          // Now look forward to find the nearest row.
          // (Look forward only if nearest row looking back was found).
          if (idisty1 > 0) {
            iby = row + 1;
            if (iby >= numRows) {
              iby = numRows - 1;
            }
            iey = row + numRowsToFill / 2;
            if (iey >= numRows) {
              iey = numRows - 1;
            }
            i3 = iby;
            i4 = col;

            if (iey >= iby) {

              boolean endFlag = false;

              while (!endFlag) {

                if (inputGrid.isNull(i3, i4)) {
                  i3 = i3 + 1;
                  if (i3 > iey) {
                    endFlag = true;
                  }
                } else {
                  yval2 = inputData[i3][i4];
                  idisty2 = i3 - row;
                  endFlag = true;
                }
              }
            }
          }

          // Now look back to find the nearest column.
          int ibx = col - 1;
          if (ibx < 0) {
            ibx = 0;
          }
          int iex = col - numColsToFill / 2;
          if (iex < 0) {
            iex = 0;
          }

          i3 = row;
          i4 = ibx;

          double xval1 = nullvalue;
          int idistx1 = 0;

          if (iex <= ibx) {

            boolean endFlag = false;

            while (!endFlag) {

              if (inputGrid.isNull(i3, i4)) {
                i4 = i4 - 1;
                if (i4 < iex) {
                  endFlag = true;
                }
              } else {
                xval1 = inputData[i3][i4];
                idistx1 = col - i4;
                endFlag = true;
              }
            }
          }

          double xval2 = nullvalue;
          int idistx2 = 0;

          // Now look forward to find the nearest row
          // (Look forward only if nearest row looking back was found).
          if (idistx1 > 0) {
            ibx = col + 1;
            if (ibx >= numCols) {
              ibx = numCols - 1;
            }
            iex = col + numColsToFill / 2;
            if (iex >= numCols) {
              iex = numCols - 1;
            }
            i3 = row;
            i4 = ibx;
            if (iex >= ibx) {

              boolean endFlag = false;

              while (!endFlag) {

                if (inputGrid.isNull(i3, i4)) {
                  i4 = i4 + 1;
                  if (i4 > iex) {
                    endFlag = true;
                  }
                } else {
                  xval2 = inputData[i3][i4];
                  idistx2 = i4 - col;
                  endFlag = true;
                }
              }
            }
          }

          // Determine the weight in the y direction.
          int iwgty = 0;
          double yval = nullvalue;

          if (idisty1 > 0 && idisty2 > 0) {
            iwgty = idisty1 + idisty2;
            yval = (yval1 * idisty2 + yval2 * idisty1) / iwgty;
          }

          // Determine the weight in the x direction.
          int iwgtx = 0;
          double xval = nullvalue;

          if (idistx1 > 0 && idistx2 > 0) {
            iwgtx = idistx1 + idistx2;
            xval = (xval1 * idistx2 + xval2 * idistx1) / iwgtx;
          }

          // Determine the new interpolated value.
          if (iwgtx > 0) {
            if (iwgty > 0) {
              outputData[row][col] = (float) (xval * iwgty + yval * iwgtx) / (iwgtx + iwgty);
            } else {
              outputData[row][col] = (float) xval;
            }
          } else {
            outputData[row][col] = (float) yval;
          }
        }
      }
      monitor.worked(1);
    }

    return outputData;
  }
}
