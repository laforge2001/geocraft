package org.geocraft.geomath.algorithm.horizon.mediansmooth;


import java.io.IOException;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.factory.model.Grid3dFactory;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.EntityComboField;


/**
 * The implementation of the median smooth algorithm for 3D grids.
 */
public class MedianSmooth3d extends MedianSmooth {

  /** The input grid property. */
  public EntityProperty<Grid3d> _inputGrid;

  /** The masking grid property. */
  public EntityProperty<Grid3d> _maskGrid;

  /** The masking grid usage property. */
  public BooleanProperty _useMaskGrid;

  /** The filter type property. */
  public EnumProperty<FilterType> _filterType;

  /** The interpolation option property. */
  public EnumProperty<InterpolateOption> _interpolationOption;

  /** The row filter size property. */
  public IntegerProperty _rowFilterSize;

  /** The column filter size property. */
  public IntegerProperty _colFilterSize;

  /** The square filter size property. */
  public IntegerProperty _squareFilterSize;

  /** The output grid name property. */
  public StringProperty _outputGridName;

  /** The output comments property. */
  public StringProperty _outputComments;

  public MedianSmooth3d() {
    _inputGrid = addEntityProperty("Input Grid", Grid3d.class);
    _maskGrid = addEntityProperty("Mask Grid", Grid3d.class);
    _useMaskGrid = addBooleanProperty("Use Mask Grid", false);
    _filterType = addEnumProperty("Filter Type", FilterType.class, FilterType.COLS_ROWS);
    _interpolationOption = addEnumProperty("Interpolate Options", InterpolateOption.class,
        InterpolateOption.NON_NULLS_ONLY);
    _rowFilterSize = addIntegerProperty("Filter Size (# of Rows)", 1);
    _colFilterSize = addIntegerProperty("Filter Size (# of Columns)", 1);
    _squareFilterSize = addIntegerProperty("Filter Size", 0);
    _outputGridName = addStringProperty("Output Grid Name", "");
    _outputComments = addStringProperty("Output Comments", "");
  }

  @Override
  public void buildView(IModelForm form) {
    // Build the input parameters section.
    FormSection inputSection = form.addSection("Input", false);
    inputSection.addEntityComboField(_inputGrid, Grid3d.class);
    EntityComboField maskGrid = inputSection.addEntityComboField(_maskGrid, Grid3d.class);
    maskGrid.showActiveFieldToggle(_useMaskGrid);

    FormSection smoothingSection = form.addSection("Smoothing", false);
    smoothingSection.addRadioGroupField(_filterType, FilterType.values());
    smoothingSection.addTextField(_rowFilterSize).setTooltip("Enter the filter size in # of rows");
    smoothingSection.addTextField(_colFilterSize).setTooltip("Enter the filter size in # of columns");
    smoothingSection.addTextField(_squareFilterSize).setEnabled(false);
    smoothingSection.addRadioGroupField(_interpolationOption, InterpolateOption.values());

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
            .createOutputDisplayName(_inputGrid.get().getDisplayName(), "_msmooth");
        _outputGridName.set(outputName);
      }
    } else if (key.equals(_filterType.getKey())) {
      boolean showSquare = _filterType.get().equals(FilterType.SQUARE_FILTER);
      setFieldEnabled(_rowFilterSize, !showSquare);
      setFieldEnabled(_colFilterSize, !showSquare);
      setFieldEnabled(_squareFilterSize, showSquare);
    }
  }

  @Override
  public void validate(IValidation results) {
    // Validate the input grid is non-null and of the correct type.
    if (_inputGrid.isNull()) {
      results.error(_inputGrid, "No input grid specified.");
    }

    if (_useMaskGrid.get() && _maskGrid.isNull()) {
      results.error(_maskGrid, "No masking grid specified.");
    }

    // Validate the # of rows and columns to filter.
    if (_rowFilterSize.get() < 1) {
      results.error(_rowFilterSize, "The max. # of rows to fill must be >= 1.");
    }
    if (_colFilterSize.get() < 1) {
      results.error(_colFilterSize, "The max. # of columns to fill must be >= 1.");
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
    Grid3d maskGrid = _maskGrid.get();
    if (!_useMaskGrid.get()) {
      maskGrid = null;
    }
    FilterType filterType = _filterType.get();
    InterpolateOption interpolateOption = _interpolationOption.get();
    int rowFilterSize = _rowFilterSize.get();
    int colFilterSize = _colFilterSize.get();
    int squareFilterSize = _squareFilterSize.get();
    String outputGridName = _outputGridName.get();
    String outputComments = _outputComments.get();

    // Start the progress monitor.
    monitor.beginTask("Median Smoothing of Grid...", inputGrid.getNumRows());

    // Smooth the grid data.
    float[][] outputData = smoothGridData3d(inputGrid, maskGrid, filterType, rowFilterSize, colFilterSize,
        squareFilterSize, interpolateOption, false, monitor);

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

  private static float[][] smoothGridData3d(final Grid3d inputGrid, final Grid3d maskGrid, FilterType filterType,
      int rowFilterSize, final int colFilterSize, final int squareFilterSize,
      final InterpolateOption interpolateOption, final boolean useOutData, IProgressMonitor monitor) {

    int numRows = inputGrid.getNumRows();
    int numCols = inputGrid.getNumColumns();
    float[][] inputData = inputGrid.getValues();

    int maxNumValues = numRows * numCols;

    // Determine filter size.
    int mxSvals = 0;
    int fSize = squareFilterSize;

    if (filterType.equals(FilterType.COLS_ROWS)) {
      if (rowFilterSize > 0 && colFilterSize > 0) {
        mxSvals = rowFilterSize * colFilterSize;
        fSize = 0;
      } else if (fSize > 0) {
        mxSvals = fSize;
      }
    }

    // Default filter size to 24.
    if (mxSvals == 0) {
      fSize = 24;
      mxSvals = fSize;
    }

    // Make sure # of smooth values is not too large.
    mxSvals = Math.min(mxSvals, maxNumValues);

    // Get size of mask geometry.
    int numRowsMask = 0;
    int numColsMask = 0;

    boolean useMaskData = maskGrid != null;
    if (useMaskData) {
      numRowsMask = maskGrid.getNumRows();
      numColsMask = maskGrid.getNumColumns();
    }

    // Initialize to smooth.
    float[][] outputData = new float[numRows][numCols];
    float[] svals = new float[mxSvals];
    double nullvalue = inputGrid.getNullValue();

    // Smooth the output horizon.
    for (int row = 0; row < numRows && !monitor.isCanceled(); row++) {

      for (int col = 0; col < numCols; col++) {

        int nSpnts = 0;
        boolean isNull = false;

        // Determine if the current point should be used
        // (Based on the mask data).
        boolean useCPoint = false;

        if (useMaskData) {
          if (row < numRowsMask && col < numColsMask) {
            if (!maskGrid.isNull(row, col)) {
              useCPoint = true;
            }
          }
        } else if (!useMaskData) {
          useCPoint = true;
        }

        // continue to determine if the current point should be used
        // (Based on the null data (Option 0=use data that is not null,
        // 1=use null data, 2=use all data))
        if (useCPoint) {

          useCPoint = false;
          if (inputGrid.isNull(row, col)) {
            isNull = true;
            if (interpolateOption != InterpolateOption.NON_NULLS_ONLY) {
              useCPoint = true;
            }
          } else {
            if (interpolateOption != InterpolateOption.NULLS_ONLY) {
              useCPoint = true;
            }
          }
        }

        if (useCPoint) {

          int nYout = 0;
          int nXout = 0;
          int i3;
          int i4;

          // The first value is the current value if it is not null
          if (!isNull) {
            svals[nSpnts] = inputData[row][col];
            nSpnts = nSpnts + 1;
          }

          Boolean endFlag = false;

          while (!endFlag) {

            // Determine where to look for a points.
            boolean topFlag = true;
            Boolean bottomFlag = true;
            Boolean leftFlag = true;
            Boolean rightFlag = true;

            // Move out each direction if filter size not met.
            if (fSize > 0) {
              nYout = nYout + 1;
              nXout = nXout + 1;

              // Move out according to the # of traces, # of lines specified.
            } else {

              int nY = (nYout + 1) * 2 + 1;

              if (nY <= rowFilterSize) {
                nYout = nYout + 1;
              } else {
                topFlag = false;
                bottomFlag = false;
              }

              int nX = (nXout + 1) * 2 + 1;

              if (nX <= colFilterSize) {
                nXout = nXout + 1;
              } else {
                leftFlag = false;
                rightFlag = false;
              }
            }

            // Determine a row for searching the top & make sure it is valid.
            i3 = row - nYout;
            if (topFlag) {
              if (nSpnts >= mxSvals | i3 < 0 | i3 >= numRows) {
                topFlag = false;
              }
            }

            // Determine the beginning column.
            int ibx = col - nXout;

            if (ibx < 0) {
              ibx = 0;
            }

            // Determine the ending column.
            int iex = col + nXout + 1;

            if (iex > numCols) {
              iex = numCols;
            }

            // Search the top.
            if (topFlag) {

              for (i4 = ibx; i4 < iex; i4++) {

                // Determine if the smoothing point should be used.
                boolean useSPoint = false;

                if (inputGrid.isNull(i3, i4)) {
                  useSPoint = false;
                } else if (useOutData) {
                  useSPoint = true;
                } else if (useMaskData) {
                  if (i3 < numRowsMask && i4 < numColsMask) {
                    if (!maskGrid.isNull(i3, i4)) {
                      useSPoint = true;
                    }
                  }
                } else if (!useMaskData) {
                  useSPoint = true;
                }

                if (useSPoint) {
                  if (nSpnts < mxSvals) {
                    svals[nSpnts] = inputData[i3][i4];
                    nSpnts = nSpnts + 1;
                  }
                }
              }
            }

            // Determine a column for searching right & make sure it is valid.
            i4 = col + nXout;
            if (rightFlag) {
              if (nSpnts >= mxSvals | i4 < 0 | i4 >= numCols) {
                rightFlag = false;
              }
            }

            // Determine the beginning row.
            int iby = row - nYout + 1;

            if (iby < 0) {
              iby = 0;
            }

            // Determine the ending row.
            int iey = row + nYout + 1;

            if (iey > numRows) {
              iey = numRows;
            }

            // Search right.
            if (rightFlag) {

              for (i3 = iby; i3 < iey; i3++) {

                // Determine if the smoothing point should be used.
                boolean useSPoint = false;

                if (inputGrid.isNull(i3, i4)) {
                  useSPoint = false;
                } else if (useOutData) {
                  useSPoint = true;
                } else if (useMaskData) {
                  if (i3 < numRowsMask && i4 < numColsMask) {
                    if (!maskGrid.isNull(i3, i4)) {
                      useSPoint = true;
                    }
                  }
                } else if (!useMaskData) {
                  useSPoint = true;
                }

                if (useSPoint) {
                  if (nSpnts < mxSvals) {
                    svals[nSpnts] = inputData[i3][i4];
                    nSpnts = nSpnts + 1;
                  }
                }
              }
            }

            // Determine a row for searching the bottom & make sure it is valid.
            i3 = row + nYout;
            if (bottomFlag) {
              if (nSpnts >= mxSvals | i3 < 0 | i3 >= numRows) {
                bottomFlag = false;
              }
            }

            // Determine the beginning column.
            ibx = col - nXout;
            if (ibx < 0) {
              ibx = 0;
            }

            // Determine the ending column.
            iex = col + nXout;
            if (iex > numCols) {
              iex = numCols;
            }

            // Search the bottom.
            if (bottomFlag) {

              for (i4 = ibx; i4 < iex; i4++) {

                // Determine if the smoothing point should be used.
                boolean useSPoint = false;

                if (inputGrid.isNull(i3, i4)) {
                  useSPoint = false;
                } else if (useOutData) {
                  useSPoint = true;
                } else if (useMaskData) {
                  if (i3 < numRowsMask && i4 < numColsMask) {
                    if (!maskGrid.isNull(i3, i4)) {
                      useSPoint = true;
                    }
                  }
                } else if (!useMaskData) {
                  useSPoint = true;
                }

                if (useSPoint) {
                  if (nSpnts < mxSvals) {
                    svals[nSpnts] = inputData[i3][i4];
                    nSpnts = nSpnts + 1;
                  }
                }
              }
            }

            // Determine a column for searching left & make sure it is valid.
            i4 = col - nXout;
            if (leftFlag) {
              if (nSpnts >= mxSvals | i4 < 0 | i4 >= numCols) {
                leftFlag = false;
              }
            }

            // Determine the beginning row.
            iby = row - nYout + 1;
            if (iby < 0) {
              iby = 0;
            }

            // Determine the ending row.
            iey = row + nYout;
            if (iey > numRows) {
              iey = numRows;
            }

            // Search left.
            if (leftFlag) {

              for (i3 = iby; i3 < iey; i3++) {

                // Determine if the smoothing point should be used.
                boolean useSPoint = false;

                if (inputGrid.isNull(i3, i4)) {
                  useSPoint = false;
                } else if (useOutData) {
                  useSPoint = true;
                } else if (useMaskData) {
                  if (i3 < numRowsMask && i4 < numColsMask) {
                    if (!maskGrid.isNull(i3, i4)) {
                      useSPoint = true;
                    }
                  }
                } else if (!useMaskData) {
                  useSPoint = true;
                }

                if (useSPoint) {
                  if (nSpnts < mxSvals) {
                    svals[nSpnts] = inputData[i3][i4];
                    nSpnts = nSpnts + 1;
                  }
                }
              }
            }

            // Determine if all points found according to the filter size.
            if (fSize > 0) {
              if (nSpnts >= mxSvals) {
                endFlag = true;
              }

              // Determine if # of traces & # of lines have been met.
            } else {

              int nX = (nXout + 1) * 2 + 1;

              if (nX > colFilterSize) {
                int nY = (nYout + 1) * 2 + 1;

                if (nY > rowFilterSize) {
                  endFlag = true;
                }
              }
            }
          }

          // Smooth the current point with the median point.
          if (nSpnts > 1) {
            Arrays.sort(svals, 0, nSpnts);

            int ip = (nSpnts - 1) / 2 + 1;

            outputData[row][col] = svals[ip];

            // Sorting not necessary if just one point.
          } else if (nSpnts == 1) {
            int ip = 0;

            outputData[row][col] = svals[ip];

            // There are no smoothing points.
          } else {
            outputData[row][col] = (float) nullvalue;
          }

          // Don't smooth the current point.
        } else {
          outputData[row][col] = inputData[row][col];
        }
      }
      monitor.worked(1);
      monitor.subTask("Completed row " + row);
    }
    return outputData;
  }
}
