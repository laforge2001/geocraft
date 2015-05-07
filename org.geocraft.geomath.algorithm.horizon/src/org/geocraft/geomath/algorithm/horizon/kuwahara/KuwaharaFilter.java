package org.geocraft.geomath.algorithm.horizon.kuwahara;


import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.factory.model.Grid3dFactory;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.TextBox;
import org.geocraft.ui.form2.field.TextField;


/**
 * The standalone algorithm used to apply a Kuwahara filter to an input grid,
 * creating a new output grid.
 */
public class KuwaharaFilter extends StandaloneAlgorithm {

  /** The input grid to filter. */
  public final EntityProperty<Grid3d> _inputGrid;

  /** The Kuwahara filter size. */
  public final IntegerProperty _filterSize;

  /** The name for the output grid. */
  public final StringProperty _outputGridName;

  /** The user comments for the output grid. */
  public final StringProperty _outputComments;

  public KuwaharaFilter() {
    _inputGrid = addEntityProperty("Input Grid", Grid3d.class);
    _filterSize = addIntegerProperty("Filter Size", 5);
    _outputGridName = addStringProperty("Output Grid Name", "");
    _outputComments = addStringProperty("Output Comments", "");
  }

  @Override
  public void buildView(final IModelForm form) {
    FormSection inputSection = form.addSection("Input");

    ComboField inputGrid = inputSection.addEntityComboField(_inputGrid, Grid3d.class);
    inputGrid.setTooltip("Select the grid to filter");

    FormSection filterSection = form.addSection("Parameters");

    TextField filterSize = filterSection.addTextField(_filterSize);
    filterSize.setTooltip("Enter the filter size (odd integer, >= 3)");

    FormSection outputSection = form.addSection("Output");

    TextField outputGridName = outputSection.addTextField(_outputGridName);
    outputGridName.setTooltip("Enter a name for the output grid");

    TextBox outputComments = outputSection.addTextBox(_outputComments);
    outputComments.setTooltip("Enter comments for the output grid.");
  }

  public void propertyChanged(String key) {
    // Auto-generate an output name from the input grid.
    if (key.equals(_inputGrid.getKey())) {
      if (!_inputGrid.isNull()) {
        String outputName = _inputGrid.get().getMapper()
            .createOutputDisplayName(_inputGrid.get().getDisplayName(), "_kuwa");
        //String outputName = _inputGrid.get().getDisplayName() + "_kuwa";
        _outputGridName.set(outputName);
      }
    }
  }

  public void validate(final IValidation results) {
    // Validate the input grid is specified.
    if (_inputGrid.isNull()) {
      results.error(_inputGrid, "No input grid specified");
    }

    // Validate the filter size is >= 3 and odd.
    if (_filterSize.get() < 3) {
      results.error(_filterSize, "The filter size must be >= 3");
    }
    if (_filterSize.get() % 2 == 0) {
      results.error(_filterSize, "The filter size must be an odd value");
    }

    // Validate the output grid name is specified.
    if (_outputGridName.isEmpty()) {
      results.error(_outputGridName, "No output grid name specified");
    }

    // Check if an entry already exists in the datastore.
    if (!_inputGrid.isNull() && !_outputGridName.isEmpty()) {
      if (Grid3dFactory.existsInStore(_inputGrid.get(), _outputGridName.get())) {
        results.warning(_outputGridName, "Exists in datastore and will be overwritten.");
      }
    }
  }

  @Override
  public void run(final IProgressMonitor monitor, final ILogger logger, final IRepository repository) {

    // Unpack the model parameters.
    Grid3d inputGrid = _inputGrid.get();
    int filterSize = _filterSize.get();
    String outputGridName = _outputGridName.get();
    String outputComments = _outputComments.get();

    // Apply the Kuwahara filter on the grid.
    float[][] outputData = filterGrid(inputGrid, filterSize, logger, monitor);

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
   * Applies the Kuwahara filter to the input grid, creating an output array of grid values.
   * 
   * @param grid the input grid to filter.
   * @param filterSize the Kuwahara filter size.
   * @param logger the logger.
   * @param monitor the progress monitor.
   * @return the output array of filtered values.
   */
  public float[][] filterGrid(final Grid3d grid, final int filterSize, final ILogger logger,
      final IProgressMonitor monitor) {
    // Get size of the grid.
    int nRows = grid.getNumRows();
    int nCols = grid.getNumColumns();

    monitor.beginTask("Applying Kuwahara Filter to " + grid.getDisplayName(), nRows);

    // Convert series data to a 2d array.
    float[][] inputValues = grid.getValues();
    float[][] outputValues = new float[nRows][nCols];
    int offset = (filterSize - 1) / 2;
    float nullValue = grid.getNullValue();

    // Apply the Kuwahara filter on the grid.
    for (int row = 0; row < nRows && !monitor.isCanceled(); row++) {
      for (int col = 0; col < nCols; col++) {
        outputValues[row][col] = nullValue;
        if (row >= offset && col >= offset && row < nRows - offset && col < nCols - offset) {
          boolean isNull = false;
          for (int i = row - offset; i <= row + offset && !isNull; i++) {
            for (int k = col - offset; k <= col + offset && !isNull; k++) {
              isNull = grid.isNull(i, k);
            }
          }
          if (isNull) {
            outputValues[row][col] = nullValue;
          } else {
            float[][] meanVariance = new float[4][2];
            meanVariance[0] = meanVariance(inputValues, row - offset, col - offset, row, col);
            meanVariance[1] = meanVariance(inputValues, row, col - offset, row + offset, col);
            meanVariance[2] = meanVariance(inputValues, row - offset, col, row, col + offset);
            meanVariance[3] = meanVariance(inputValues, row, col, row + offset, col + offset);
            float mean = meanVariance[0][0];
            float variance = meanVariance[0][1];
            for (int k = 1; k < 4; k++) {
              if (variance > meanVariance[k][1]) {
                mean = meanVariance[k][0];
                variance = meanVariance[k][1];
              }
            }
            outputValues[row][col] = mean;
          }
        }
      }
      // Update the progress monitor.
      monitor.worked(1);
      monitor.subTask("Completed row " + row);
      if (monitor.isCanceled()) {
        logger.warn("Kuwahara Filter canceled.");
        break;
      }
    }

    return outputValues;
  }

  /**
   * Computes the mean variance for the specified subset of grid values.
   * 
   * @param values the full array of grid values.
   * @param rowStart the starting row number of the subset.
   * @param colStart the staring column number of the subset.
   * @param rowEnd the ending row number of the subset.
   * @param colEnd the ending column number of the subset.
   * @return the mean variance of the subset.
   */
  private float[] meanVariance(final float[][] values, final int rowStart, final int colStart, final int rowEnd,
      final int colEnd) {
    float[] meanVariance = new float[2];
    float sum = 0;
    float min = values[rowStart][colStart];
    float max = min;
    for (int row = rowStart; row <= rowEnd; row++) {
      for (int col = colStart; col <= colEnd; col++) {
        sum += values[row][col];
        min = Math.min(min, values[row][col]);
        max = Math.max(max, values[row][col]);
      }
    }
    meanVariance[0] = sum / ((rowEnd - rowStart + 1) * (colEnd - colStart + 1));
    meanVariance[1] = max - min;
    return meanVariance;
  }

}
