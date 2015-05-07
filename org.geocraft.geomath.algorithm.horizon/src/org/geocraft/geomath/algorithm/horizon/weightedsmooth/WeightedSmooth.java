package org.geocraft.geomath.algorithm.horizon.weightedsmooth;


import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.factory.model.Grid3dFactory;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.EntityComboField;


public class WeightedSmooth extends StandaloneAlgorithm {

  /** The input grid property. */
  public EntityProperty<Grid3d> _inputGrid;

  /** The masking grid property. */
  public EntityProperty<Grid3d> _maskGrid;

  /** The masking grid usage property. */
  public BooleanProperty _useMaskGrid;

  /** The edge weight property. */
  public FloatProperty _edgeWeight;

  /** The blending grid usage property. */
  public BooleanProperty _applyBlendingGrid;

  /** The row filter width property. */
  public IntegerProperty _rowFilterSize;

  /** The column filter width property. */
  public IntegerProperty _colFilterSize;

  /** The output grid name property. */
  public StringProperty _outputGridName;

  /** The output comments property. */
  public StringProperty _outputComments;

  public WeightedSmooth() {
    _inputGrid = addEntityProperty("Input Grid", Grid3d.class);
    _maskGrid = addEntityProperty("Mask Grid", Grid3d.class);
    _useMaskGrid = addBooleanProperty("Use Mask Grid", false);
    _edgeWeight = addFloatProperty("Edge Weight", 0);
    _applyBlendingGrid = addBooleanProperty("Apply Blending Grid", false);
    _rowFilterSize = addIntegerProperty("Filter Size (# of Rows)", 1);
    _colFilterSize = addIntegerProperty("Filter Size (# of Columns)", 1);
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
    smoothingSection.addTextField(_edgeWeight);
    smoothingSection.addCheckboxField(_applyBlendingGrid);
    smoothingSection.addTextField(_rowFilterSize).setTooltip("Enter the filter size in # of rows");
    smoothingSection.addTextField(_colFilterSize).setTooltip("Enter the filter size in # of columns");

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
            .createOutputDisplayName(_inputGrid.get().getDisplayName(), "_wsmooth");
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
    float edgeWeight = _edgeWeight.get();
    boolean applyBlending = _applyBlendingGrid.get();
    int rowFilterSize = _rowFilterSize.get();
    int colFilterSize = _colFilterSize.get();
    String outputGridName = _outputGridName.get();
    String outputComments = _outputComments.get();
    int totalWork = inputGrid.getNumRows();
    if (applyBlending && maskGrid != null) {
      totalWork *= 3;
    }

    // Start the progress monitor.
    monitor.beginTask("Weighted Smoothing of Grid...", totalWork);

    // Smooth the grid data.
    float[][] outputData = smoothGridData(inputGrid, maskGrid, edgeWeight, applyBlending, rowFilterSize, colFilterSize,
        false, monitor);

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

  public static float[][] smoothGridData(Grid3d inputGrid, final Grid3d maskGrid, final float edgeWeight,
      final boolean applyBlending, final int rowFilterSize, final int colFilterSize, final boolean useOutData,
      IProgressMonitor monitor) {

    GridGeometry3d geometry = inputGrid.getGeometry();
    int numRows = geometry.getNumRows();
    int numCols = geometry.getNumColumns();
    float[][] inputData = inputGrid.getValues();
    float[][] outputGridData = new float[numRows][numCols];

    int numRowsMask = 0;
    int numColsMask = 0;

    boolean useMaskData = maskGrid != null;
    if (useMaskData) {
      numRowsMask = maskGrid.getNumRows();
      numColsMask = maskGrid.getNumColumns();
    }

    double centerWeight = 1.0;
    double weightDiff = centerWeight - edgeWeight;
    int noOfNulls = 0;
    int nHXvals = colFilterSize / 2;
    int nHYvals = rowFilterSize / 2;
    int mxRings = Math.max(nHXvals, nHYvals);

    for (int row = 0; row < numRows && !monitor.isCanceled(); row++) {

      for (int col = 0; col < numCols; col++) {

        int nSpnts = 0;
        double sum = 0.0;
        double dnom = 0.0;

        // Determine if the current point should be used.
        boolean useCPoint = false;

        if (inputGrid.isNull(row, col)) {
          noOfNulls = noOfNulls + 1;
        } else if (useMaskData) {
          if (row < numRowsMask && col < numColsMask) {
            if (!maskGrid.isNull(row, col)) {
              useCPoint = true;
            }
          }
        } else if (!useMaskData) {
          useCPoint = true;
        }

        if (useCPoint) {

          double weight;
          int mxK;
          int mxX;
          int mxY;

          // Determine the beginning row.
          int iby = row - nHYvals;

          if (iby < 0) {
            iby = 0;
          }

          // Determine the ending row.
          int iey = row + nHYvals + 1;

          if (iey > numRows) {
            iey = numRows;
          }

          // Determine the beginning column.
          int ibx = col - nHXvals;

          if (ibx < 0) {
            ibx = 0;
          }

          // Determine the ending column.
          int iex = col + nHXvals + 1;

          if (iex > numCols) {
            iex = numCols;
          }
          for (int i3 = iby; i3 < iey; i3++) {

            for (int i4 = ibx; i4 < iex; i4++) {

              // Determine if the smoothing point should be used.
              Boolean useSPoint = false;

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
                mxX = Math.abs(i3 - row);
                mxY = Math.abs(i4 - col);
                mxK = Math.max(mxX, mxY);
                weight = centerWeight - weightDiff * mxK / mxRings;
                dnom = dnom + weight;
                sum = sum + weight * inputData[i3][i4];
                nSpnts = nSpnts + 1;
              }
            }
          }
        }

        // Smooth the current point.
        if (nSpnts > 0) {
          outputGridData[row][col] = (float) (sum / dnom);
        } else {
          outputGridData[row][col] = inputData[row][col];
        }
      }
      monitor.worked(1);
      monitor.subTask("Completed row " + row);
    }

    // Create a blending grid.
    if (applyBlending && useMaskData) {

      float[][] blendGridData = new float[numRows][numCols];

      for (int row = 0; row < numRows; row++) {

        for (int col = 0; col < numCols; col++) {

          int nSpnts = 0;
          double sum = 0.0;
          double dnom = 0.0;
          boolean useCPoint = false;

          // Determine whether to use the current point.
          if (row < numRowsMask && col < numColsMask) {
            if (!maskGrid.isNull(row, col)) {
              useCPoint = true;
            }
          }
          if (useCPoint) {

            double weight;
            int mxK;
            int mxX;
            int mxY;

            // Determine the beginning row.
            int iby = row - nHYvals;

            if (iby < 0) {
              iby = 0;
            }

            // Determine the ending row.
            int iey = row + nHYvals + 1;

            if (iey > numRows) {
              iey = numRows;
            }

            // Determine the beginning column.
            int ibx = col - nHXvals;

            if (ibx < 0) {
              ibx = 0;
            }

            // Determine the ending column.
            int iex = col + nHXvals + 1;

            if (iex > numCols) {
              iex = numCols;
            }

            // Calculate the current blended grid values.
            for (int i3 = iby; i3 < iey; i3++) {

              for (int i4 = ibx; i4 < iex; i4++) {
                mxY = Math.abs(i3 - row);
                mxX = Math.abs(i4 - col);
                mxK = Math.max(mxX, mxY);
                weight = centerWeight - weightDiff * mxK / mxRings;
                dnom = dnom + weight;
                if (!maskGrid.isNull(i3, i4)) {
                  sum = sum + weight;
                }
                nSpnts = nSpnts + 1;
              }
            }
          }

          // Determine the current blended grid values.
          if (nSpnts > 0) {
            blendGridData[row][col] = (float) (sum / dnom);
          } else {
            blendGridData[row][col] = 0;
          }
        }
        monitor.worked(1);
        monitor.subTask("Blending Grid: Completed row " + row);
      }

      // Apply the blended grid.
      noOfNulls = 0;
      for (int row = 0; row < numRows; row++) {

        for (int col = 0; col < numCols; col++) {

          double weight = blendGridData[row][col] * blendGridData[row][col];
          Boolean useCPoint = false;

          // determine whether to use the current point
          if (inputGrid.isNull(row, col)) {
            noOfNulls = noOfNulls + 1;
          } else if (row < numRowsMask && col < numColsMask) {
            if (!maskGrid.isNull(row, col)) {
              useCPoint = true;
            }
          }
          if (useCPoint) {
            outputGridData[row][col] = (float) (weight * outputGridData[row][col] + (1.0 - weight)
                * inputData[row][col]);
          }
        }
        monitor.worked(1);
        monitor.subTask("Applying Blended Grid: Completed row " + row);
      }
    }
    return outputGridData;
  }
}
