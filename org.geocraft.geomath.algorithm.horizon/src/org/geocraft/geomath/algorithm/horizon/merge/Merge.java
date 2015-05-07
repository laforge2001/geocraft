package org.geocraft.geomath.algorithm.horizon.merge;


import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.factory.model.Grid3dFactory;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.property.EntityArrayProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;


/**
 * Merges a collection of 3D grids using MIN,MAX or OR logic.
 * The 3 operations are described below.
 * 1) MIN: the minimum non-null value of all the grids is output at each location.
 * 2) MAX: the maximum non-null value of all the grids is output at each location.
 * 3) OR: the first non-null value of all the grids is output at each location, which
 * depends on the order of the grids in the input list.
 *
 * Restrictions:
 * 1) All the grids must have the same geometry (numRows, numCols, etc).
 *
 */
public class Merge extends StandaloneAlgorithm {

  /** Enumeration of the merge operations. */
  public enum Operation {
    /** Use the minimum non-null value found. */
    MIN("Minimum"),
    /** Use the maximum non-null value found. */
    MAX("Maximum"),
    /** Use the first non-null value found. */
    OR("OR");

    /** The name of the merge operation. */
    private final String _name;

    Operation(final String displayName) {
      _name = displayName;
    }

    @Override
    public String toString() {
      return _name;
    }
  }

  /** The input grids array property. */
  protected EntityArrayProperty<Grid3d> _inputGrids;

  /** The merge operation property. */
  protected EnumProperty<Operation> _operation;

  /** The output grid name property. */
  protected StringProperty _outputGridName;

  public Merge() {
    _inputGrids = addEntityArrayProperty("Input Grids", Grid3d.class);
    _operation = addEnumProperty("Operation", Operation.class, Operation.OR);
    _outputGridName = addStringProperty("Output Grid Name", "");
  }

  @Override
  public void buildView(IModelForm form) {
    FormSection input = form.addSection("Input", false);
    input.addEntityListField(_inputGrids, Grid3d.class);

    FormSection merge = form.addSection("Merge");
    merge.addRadioGroupField(_operation, Operation.values());

    FormSection output = form.addSection("Output");
    output.addTextField(_outputGridName);
  }

  public void propertyChanged(String key) {
    // Auto-generate an output name from the input grid.
    if (key.equals(_inputGrids.getKey())) {
      if (!_inputGrids.isEmpty()) {
        String outputName = _inputGrids.get()[0].getMapper().createOutputDisplayName(
            _inputGrids.get()[0].getDisplayName(), "_merge");
        _outputGridName.set(outputName);
      }
    }
  }

  public void validate(IValidation results) {
    // Validate that at least 2 input grids are selected.
    if (_inputGrids.isEmpty()) {
      results.error(_inputGrids, "No input grids specified.");
    } else {
      Grid3d[] grids = _inputGrids.get();
      int numGrids = grids.length;
      if (numGrids == 1) {
        results.error(_inputGrids, "Minimum of 2 input grids required.");
      } else {
        GridGeometry3d geometry = grids[0].getGeometry();
        for (int i = 1; i < numGrids; i++) {
          if (!grids[i].getGeometry().matchesGeometry(geometry)) {
            results.error(_inputGrids, "Grid geometries do not match.");
          }
        }
      }
    }

    // Validate the merge operation.
    if (_operation.isNull()) {
      results.error(_operation, "No merge operation specified.");
    }

    // Validate the output grid name.
    if (_outputGridName.isEmpty()) {
      results.error(_outputGridName, "No output grid name specified.");
    }
  }

  /**
   * @throws CoreException  
   */
  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) throws CoreException {

    // Unpack the model parameters.
    Grid3d[] inputGrids = _inputGrids.get();
    Operation operation = _operation.get();
    String outputGridName = _outputGridName.get();
    int totalWork = (inputGrids.length - 1) * inputGrids[0].getNumRows();

    // Start the progress monitor.
    monitor.beginTask("Merging Grids...", totalWork);

    // Merge the grid data.
    float[][] outputData = mergeGridData(monitor, logger, inputGrids, operation);

    // Only create the output grid if the job completed normally.
    if (!monitor.isCanceled()) {
      // Find (or create) the output grid and update it in the datastore.
      try {
        Grid3d outputGrid = Grid3dFactory.create(repository, inputGrids[0], outputData, outputGridName);
        outputGrid.update();
      } catch (IOException ex) {
        throw new RuntimeException(ex.getMessage());
      }
    }

    // Task is done.
    monitor.done();
  }

  /**
   * Merges a collection of grids.
   * 
   * @param monitor the progress monitor.
   * @param logger the logger.
   * @param inputGrids the array of grids to merge.
   * @param operation the merge operation (MIN,MAX,OR).
   * @return the array of merged grid data.
   */
  public static float[][] mergeGridData(IProgressMonitor monitor, ILogger logger, Grid3d[] inputGrids,
      Operation operation) {
    int numGrids = inputGrids.length;

    int numRows = inputGrids[0].getNumRows();
    int numCols = inputGrids[0].getNumColumns();

    // Initialize the output data to that of the 1st grid.
    float[][] outputData = inputGrids[0].getValues();

    // Process the rest of the input grids.
    for (int i = 1; i < numGrids; ++i) {

      Grid3d grid = inputGrids[i];
      float[][] inputData = grid.getValues();

      // Merge in the grid.
      for (int row = 0; row < numRows && !monitor.isCanceled(); row++) {
        for (int col = 0; col < numCols; col++) {
          float inputValue = inputData[row][col];
          float outputValue = outputData[row][col];
          if (!grid.isNull(row, col)) {
            switch (operation) {
              case MAX:
                if (inputValue > outputValue || inputGrids[0].isNull(outputValue)) {
                  outputData[row][col] = inputValue;
                }
                break;
              case MIN:
                if (inputValue < outputValue || inputGrids[0].isNull(outputValue)) {
                  outputData[row][col] = inputValue;
                }
                break;
              case OR:
                if (inputGrids[0].isNull(outputValue)) {
                  outputData[row][col] = inputValue;
                }
                break;
            }
          }
        }

        // Update the progress monitor.
        monitor.worked(1);
        monitor.subTask("Completed row " + row + " for Grid " + grid.getDisplayName());
      }
    }

    return outputData;
  }
}
