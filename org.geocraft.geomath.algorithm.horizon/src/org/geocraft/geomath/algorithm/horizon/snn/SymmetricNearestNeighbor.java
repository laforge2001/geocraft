package org.geocraft.geomath.algorithm.horizon.snn;


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


public class SymmetricNearestNeighbor extends StandaloneAlgorithm {

  private EntityProperty<Grid3d> _inputGrid;

  private IntegerProperty _filterSize;

  private StringProperty _outputGridName;

  private StringProperty _outputComments;

  public SymmetricNearestNeighbor() {
    _inputGrid = addEntityProperty("Input Grid", Grid3d.class);
    _filterSize = addIntegerProperty("Filter Size", 5);
    _outputGridName = addStringProperty("Outut Grid Name", "");
    _outputComments = addStringProperty("Output Comments", "");
  }

  @Override
  public void buildView(IModelForm form) {
    FormSection section = form.addSection("Input");
    section.addEntityComboField(_inputGrid, Grid3d.class);

    section = form.addSection("Parameters");
    section.addTextField(_filterSize);

    section = form.addSection("Output");
    section.addTextField(_outputGridName);
    section.addTextBox(_outputComments);
  }

  @Override
  public void propertyChanged(String key) {
    if (key.equals(_inputGrid.getKey()) && !_inputGrid.isNull()) {
      // Auto-generate an output name based on the name of the input grid.
      String outputName = _inputGrid.get().getMapper()
          .createOutputDisplayName(_inputGrid.get().getDisplayName(), "_snn");
      _outputGridName.set(outputName);
    }
  }

  @Override
  public void validate(IValidation results) {
    if (_inputGrid.isNull()) {
      results.error(_inputGrid, "No input grid specified.");
    }
    int filterSize = _filterSize.get();
    if (filterSize < 3 || filterSize % 2 == 0) {
      results.error(_filterSize, "The filter size must be an odd value greater than or equal to 3.");
    }
    if (_outputGridName.isEmpty()) {
      results.error(_outputGridName, "No output grid name specified.");
    } else {
      if (!_inputGrid.isNull()) {
        if (Grid3dFactory.existsInStore(_inputGrid.get(), _outputGridName.get())) {
          results.warning(_outputGridName, "The output grid exists and will be overwritten.");
        }
      }
    }
  }

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) {
    // Unpack the parameters.
    Grid3d inputGrid = _inputGrid.get();
    int filterSize = _filterSize.get();
    String outputGridName = _outputGridName.get();
    String outputComments = _outputComments.get();

    // Apply SNN on the grid.
    float[][] outputData = snn(inputGrid, filterSize, monitor);

    try {
      // Create the output grid.
      Grid3d outputGrid = Grid3dFactory.create(repository, inputGrid, outputData, outputGridName);
      outputGrid.setComment(outputComments);
      outputGrid.update();
    } catch (IOException ex) {
      throw new RuntimeException(ex.getMessage());
    }
  }

  public float[][] snn(final Grid3d property, final int size, final IProgressMonitor monitor) {
    // Get size of horizon
    int nRows = property.getNumRows();
    int nCols = property.getNumColumns();

    monitor.beginTask("Symmetric nearest neighbor", nRows);

    // convert series data to a 2d array
    float[][] inputValues = property.getValues();
    float[][] outputValues = new float[nRows][nCols];
    int offset = (size - 1) / 2;
    float nullValue = property.getNullValue();

    // Apply the SNN filter on the horizon
    for (int row = 0; row < nRows && !monitor.isCanceled(); row++) {
      for (int col = 0; col < nCols; col++) {
        outputValues[row][col] = inputValues[row][col];
        if (row >= offset && col >= offset && row < nRows - offset && col < nCols - offset) {
          boolean isNull = false;
          for (int i = row - offset; i <= row + offset && !isNull; i++) {
            for (int k = col - offset; k <= col + offset && !isNull; k++) {
              isNull = property.isNull(i, k);
            }
          }
          if (isNull) {
            outputValues[row][col] = nullValue;
          } else {
            float value = inputValues[row][col];
            float sum = 0;
            for (int i = -offset; i <= offset; i++) {
              for (int k = -offset; k <= offset; k++) {
                float value1 = inputValues[row - i][col - k];
                float value2 = inputValues[row + i][col + k];
                if (Math.abs(value - value1) < Math.abs(value - value2)) {
                  sum += value1;
                } else {
                  sum += value2;
                }
              }
            }
            outputValues[row][col] = sum / (size * size);
          }
        }
      }
      monitor.worked(1);
    }
    monitor.done();

    return outputValues;
  }
}
