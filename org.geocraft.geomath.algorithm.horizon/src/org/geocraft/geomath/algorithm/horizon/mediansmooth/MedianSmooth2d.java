/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.horizon.mediansmooth;


import java.io.IOException;
import java.util.Arrays;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.geocraft.core.factory.model.Grid2dFactory;
import org.geocraft.core.model.DataSource;
import org.geocraft.core.model.geometry.GridGeometry2d;
import org.geocraft.core.model.geometry.LineGeometry;
import org.geocraft.core.model.grid.Grid2d;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.StringArrayProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.EntityComboField;


/**
 * The implementation of the median smooth algorithm for 2D grids.
 */
public class MedianSmooth2d extends MedianSmooth {

  /** The input grid property. */
  public EntityProperty<Grid2d> _inputGrid;

  /** The input lines property. */
  public StringArrayProperty _inputLines;

  /** The masking grid property. */
  public EntityProperty<Grid2d> _maskGrid;

  /** The masking grid usage property. */
  public BooleanProperty _useMaskGrid;

  /** The interpolation option property. */
  public EnumProperty<InterpolateOption> _interpolationOption;

  /** The CDP filter size property. */
  public IntegerProperty _cdpFilterSize;

  /** The output grid name property. */
  public StringProperty _outputGridName;

  /** The output comments property. */
  public StringProperty _outputComments;

  public MedianSmooth2d() {
    _inputGrid = addEntityProperty("Input Grid", Grid2d.class);
    _inputLines = addStringArrayProperty("Lines");
    _maskGrid = addEntityProperty("Mask Grid", Grid2d.class);
    _useMaskGrid = addBooleanProperty("Use Mask Grid", false);
    _interpolationOption = addEnumProperty("Interpolate Options", InterpolateOption.class,
        InterpolateOption.NON_NULLS_ONLY);
    _cdpFilterSize = addIntegerProperty("Filter Size (# of CDPs)", 3);
    _outputGridName = addStringProperty("Output Grid Name", "");
    _outputComments = addStringProperty("Output Comments", "");
  }

  @Override
  public void buildView(IModelForm form) {
    // Build the input parameters section.
    FormSection inputSection = form.addSection("Input", false);
    inputSection.addEntityComboField(_inputGrid, Grid2d.class);
    inputSection.addOrderedListField(_inputLines, new String[0]);
    EntityComboField maskGrid = inputSection.addEntityComboField(_maskGrid, Grid2d.class);
    maskGrid.showActiveFieldToggle(_useMaskGrid);

    FormSection smoothingSection = form.addSection("Smoothing", false);
    smoothingSection.addTextField(_cdpFilterSize).setTooltip("Enter the filter size in # of CDPs");
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
        String[] lineNames = _inputGrid.get().getGridGeometry().getLineNames();
        setFieldOptions(_inputLines, lineNames);
        _inputLines.set(lineNames);
        String outputName = _inputGrid.get().createOutputDisplayName(_inputGrid.get().getDisplayName(), "_msmooth");
        //String outputName = _inputGrid.get().getDisplayName() + "_msmooth";
        _outputGridName.set(outputName);
      }
    }
  }

  @Override
  public void validate(IValidation results) {
    // Validate the input grid is non-null.
    if (_inputGrid.isNull()) {
      results.error(_inputGrid, "No input grid specified.");
    }

    // Validate at least 1 input line is selected.
    if (_inputLines.isEmpty()) {
      results.error(_inputLines, "No input lines specified.");
    }

    // Validate the mask grid is non-null, if used.
    if (_useMaskGrid.get() && _maskGrid.isNull()) {
      results.error(_maskGrid, "No masking grid specified.");
    }

    // Validate the geometry of the mask grid, if used, matches the input grid.
    if (_useMaskGrid.get() && !_maskGrid.isNull() && !_inputGrid.isNull()) {
      if (!_maskGrid.get().getGridGeometry().matchesGeometry(_inputGrid.get().getGridGeometry())) {
        results.error(_maskGrid, "The mask grid geometry does not match the input grid geometry.");
      }
    }

    // Validate the # of rows and columns to filter.
    if (_cdpFilterSize.get() < 1) {
      results.error(_cdpFilterSize, "The max. # of CDPs to fill must be >= 1.");
    }

    // Validate the output name is non-zero length.
    if (_outputGridName.isEmpty()) {
      results.error(_outputGridName, "No output grid name specified.");
    }

    // Check if an entry already exists in the datastore, and if the output name is valid.
    if (!_inputGrid.isNull() && !_outputGridName.isEmpty()) {
      Grid2d inputGrid = _inputGrid.get();
      String proposedName = _outputGridName.get();
      if (DataSource.existsInStore(inputGrid, proposedName)) {
        results.warning(_outputGridName, "Exists in datastore and will be overwritten.");
      }
      IStatus status = DataSource.validateName(inputGrid, proposedName);
      if (!status.isOK()) {
        results.error(_outputGridName, status.getMessage());
      }
    }
  }

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) throws CoreException {

    // Unpack the model parameters.
    Grid2d inputGrid = _inputGrid.get();
    String[] lineNames = _inputLines.get();
    Grid2d maskGrid = _maskGrid.get();
    if (!_useMaskGrid.get()) {
      maskGrid = null;
    }
    InterpolateOption interpolateOption = _interpolationOption.get();
    int cdpFilterSize = _cdpFilterSize.get();
    String outputGridName = _outputGridName.get();
    String outputComments = _outputComments.get();

    // Start the progress monitor.
    monitor.beginTask("Median Smoothing of Grid...", lineNames.length + inputGrid.getNumLines());

    // Only create the output grid if the job completed normally.
    if (!monitor.isCanceled()) {
      // Find (or create) the output grid and update it in the datastore.
      try {
        Grid2d outputGrid = Grid2dFactory.create(repository, inputGrid, outputGridName);
        // Smooth the grid data.
        smoothGridData2d(inputGrid, lineNames, maskGrid, cdpFilterSize, interpolateOption, outputGrid, false, monitor);
        outputGrid.setComment(inputGrid.getComment() + "\n" + outputComments);
        outputGrid.update();
      } catch (IOException ex) {
        throw new CoreException(ValidationStatus.error(ex.getMessage()));
      }
    }

    // Task is done.
    monitor.done();
  }

  private static void smoothGridData2d(final Grid2d inputGrid, final String[] lineNames, final Grid2d maskGrid,
      final int cdpFilterSize, final InterpolateOption interpolateOption, final Grid2d outputGrid,
      final boolean useOutData, final IProgressMonitor monitor) throws IOException {

    GridGeometry2d gridGeometry = inputGrid.getGridGeometry();
    for (String lineName : lineNames) {
      LineGeometry lineGeometry = gridGeometry.getLineByName(lineName);
      int lineNumber = lineGeometry.getNumber();
      int numRows = 1;
      int numBins = lineGeometry.getNumBins();
      float[] inputData = inputGrid.getValues(lineNumber);

      int maxNumValues = numRows * numBins;

      // Determine filter size.
      int mxSvals = cdpFilterSize;
      int fSize = cdpFilterSize;

      // Default filter size to 24.
      if (mxSvals == 0) {
        fSize = 24;
        mxSvals = fSize;
      }

      // Make sure # of smooth values is not too large.
      mxSvals = Math.min(mxSvals, maxNumValues);

      // Get size of mask geometry.
      int numRowsMask = 0;
      int numCellsMask = 0;

      boolean useMaskData = maskGrid != null;
      if (useMaskData) {
        numRowsMask = 1;
        numCellsMask = numBins;
      }

      // Initialize to smooth.
      float[] outputData = new float[numBins];
      float[] svals = new float[mxSvals];
      double nullvalue = inputGrid.getNullValue();

      int row = 0;
      for (int bin = 0; bin < numBins && !monitor.isCanceled(); bin++) {

        int nSpnts = 0;
        boolean isNull = false;

        // Determine if the current point should be used
        // (Based on the mask data).
        boolean useCPoint = false;

        if (useMaskData) {
          if (row < numRowsMask && bin < numCellsMask) {
            if (!maskGrid.isNull(bin)) {
              useCPoint = true;
            }
          }
        } else if (!useMaskData) {
          useCPoint = true;
        }

        // Continue to determine if the current point should be used
        // (Based on the null data (Option 0=use data that is not null,
        // 1=use null data, 2=use all data)).
        if (useCPoint) {

          useCPoint = false;
          if (inputGrid.isNull(bin)) {
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

          // The first value is the current value if it is not null.
          if (!isNull) {
            svals[nSpnts] = inputData[bin];
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

              if (nY <= 1/*one row for 2d data*/) {
                nYout = nYout + 1;
              } else {
                topFlag = false;
                bottomFlag = false;
              }

              int nX = (nXout + 1) * 2 + 1;

              if (nX <= cdpFilterSize) {
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
            int ibx = bin - nXout;

            if (ibx < 0) {
              ibx = 0;
            }

            // Determine the ending column.
            int iex = bin + nXout + 1;

            if (iex > numBins) {
              iex = numBins;
            }

            // Search the top.
            if (topFlag) {

              for (i4 = ibx; i4 < iex; i4++) {

                // Determine if the smoothing point should be used.
                boolean useSPoint = false;

                if (inputGrid.isNull(i4)) {
                  useSPoint = false;
                } else if (useOutData) {
                  useSPoint = true;
                } else if (useMaskData) {
                  if (i3 < numRowsMask && i4 < numCellsMask) {
                    if (!maskGrid.isNull(i4)) {
                      useSPoint = true;
                    }
                  }
                } else if (!useMaskData) {
                  useSPoint = true;
                }

                if (useSPoint) {
                  if (nSpnts < mxSvals) {
                    svals[nSpnts] = inputData[i4];
                    nSpnts = nSpnts + 1;
                  }
                }
              }
            }

            // Determine a column for searching right & make sure it is valid.
            i4 = bin + nXout;
            if (rightFlag) {
              if (nSpnts >= mxSvals | i4 < 0 | i4 >= numBins) {
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

                if (inputGrid.isNull(i4)) {
                  useSPoint = false;
                } else if (useOutData) {
                  useSPoint = true;
                } else if (useMaskData) {
                  if (i3 < numRowsMask && i4 < numCellsMask) {
                    if (!maskGrid.isNull(i4)) {
                      useSPoint = true;
                    }
                  }
                } else if (!useMaskData) {
                  useSPoint = true;
                }

                if (useSPoint) {
                  if (nSpnts < mxSvals) {
                    svals[nSpnts] = inputData[i4];
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
            ibx = bin - nXout;
            if (ibx < 0) {
              ibx = 0;
            }

            // Determine the ending column.
            iex = bin + nXout;
            if (iex > numBins) {
              iex = numBins;
            }

            // Search the bottom.
            if (bottomFlag) {

              for (i4 = ibx; i4 < iex; i4++) {

                // Determine if the smoothing point should be used.
                boolean useSPoint = false;

                if (inputGrid.isNull(i4)) {
                  useSPoint = false;
                } else if (useOutData) {
                  useSPoint = true;
                } else if (useMaskData) {
                  if (i3 < numRowsMask && i4 < numCellsMask) {
                    if (!maskGrid.isNull(i4)) {
                      useSPoint = true;
                    }
                  }
                } else if (!useMaskData) {
                  useSPoint = true;
                }

                if (useSPoint) {
                  if (nSpnts < mxSvals) {
                    svals[nSpnts] = inputData[i4];
                    nSpnts = nSpnts + 1;
                  }
                }
              }
            }

            // Determine a column for searching left & make sure it is valid.
            i4 = bin - nXout;
            if (leftFlag) {
              if (nSpnts >= mxSvals | i4 < 0 | i4 >= numBins) {
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

                if (inputGrid.isNull(i4)) {
                  useSPoint = false;
                } else if (useOutData) {
                  useSPoint = true;
                } else if (useMaskData) {
                  if (i3 < numRowsMask && i4 < numCellsMask) {
                    if (!maskGrid.isNull(i4)) {
                      useSPoint = true;
                    }
                  }
                } else if (!useMaskData) {
                  useSPoint = true;
                }

                if (useSPoint) {
                  if (nSpnts < mxSvals) {
                    svals[nSpnts] = inputData[i4];
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

              if (nX > cdpFilterSize) {
                int nY = (nYout + 1) * 2 + 1;

                if (nY > 1/* one row for 2d data */) {
                  endFlag = true;
                }
              }
            }
          }

          // Smooth the current point with the median point.
          if (nSpnts > 1) {
            Arrays.sort(svals, 0, nSpnts);

            int ip = (nSpnts - 1) / 2 + 1;

            outputData[bin] = svals[ip];

            // Sorting not necessary if just one point.
          } else if (nSpnts == 1) {
            int ip = 0;

            outputData[bin] = svals[ip];

            // There are no smoothing points.
          } else {
            outputData[bin] = (float) nullvalue;
          }

          // Don't smooth the current point.
        } else {
          outputData[bin] = inputData[bin];
        }
        monitor.subTask("Completed bin " + bin);
      }
      monitor.worked(1);
      outputGrid.putValues(lineNumber, outputData);
    }

    // Update the progress monitor.
    monitor.subTask("Updating minimum/maximum values");

    float minValue = Float.MAX_VALUE;
    float maxValue = -Float.MAX_VALUE;
    boolean hasNonNull = false;
    for (LineGeometry lineGeometry : gridGeometry.getLines()) {
      int lineNumber = lineGeometry.getNumber();
      float[] values = outputGrid.getValues(lineNumber);
      for (float value : values) {
        if (!outputGrid.isNull(value)) {
          minValue = Math.min(minValue, value);
          maxValue = Math.max(maxValue, value);
          hasNonNull = true;
        }
      }

      // Update the progress monitor.
      monitor.worked(1);
    }

    // Set the min/max values for the output grids.
    if (!hasNonNull) {
      minValue = outputGrid.getNullValue();
      maxValue = outputGrid.getNullValue();
    }
    outputGrid.setMinMaxValues(minValue, maxValue);

    // Update the output grids in the datastore.
    outputGrid.update();
  }
}
