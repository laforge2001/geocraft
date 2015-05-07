package org.geocraft.geomath.algorithm.horizon.gridlines;


import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.factory.model.Grid3dFactory;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;


public class GridLines extends StandaloneAlgorithm {

  /** The input grid property. */
  public EntityProperty<Grid3d> _inputGrid;

  /** The column (inline) decimation property. */
  public IntegerProperty _colDecimation;

  /** The row (xline) decimation property. */
  public IntegerProperty _rowDecimation;

  /** The output grid name property. */
  public StringProperty _outputGridName;

  /** The output grid comments property. */
  public StringProperty _outputComments;

  public GridLines() {
    _inputGrid = addEntityProperty("Input Grid", Grid3d.class);
    _colDecimation = addIntegerProperty("Column Spacing", 1);
    _rowDecimation = addIntegerProperty("Row Spacing", 1);
    _outputGridName = addStringProperty("Output Grid Name", "");
    _outputComments = addStringProperty("Output Comments", "");
  }

  @Override
  public void buildView(IModelForm form) {
    // Build the input parameters section.
    FormSection inputSection = form.addSection("Input", false);
    inputSection.addEntityComboField(_inputGrid, Grid3d.class);

    // Build the clip parameters section.
    FormSection decimateSection = form.addSection("Parameters", false);
    decimateSection.addTextField(_rowDecimation);
    decimateSection.addTextField(_colDecimation);

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
            .createOutputDisplayName(_inputGrid.get().getDisplayName(), "_lines");
        _outputGridName.set(outputName);
      }
    }
  }

  @Override
  public void validate(IValidation results) {

    int rowDecimationMax = Integer.MAX_VALUE;
    int colDecimationMax = Integer.MAX_VALUE;

    // Validate the input grid is non-null and of the correct type.
    if (_inputGrid.isNull()) {
      results.error(_inputGrid, "No input grid specified.");
    } else {
      Grid3d grid = _inputGrid.get();
      rowDecimationMax = grid.getNumRows();
      colDecimationMax = grid.getNumColumns();
    }

    // Validate row decimation is not negative and less than # of rows in grid.
    if (_rowDecimation.get() < 1) {
      results.error(_rowDecimation, "Row (Xline) decimation cannot be less than 1.");
    }

    if (_rowDecimation.get() >= rowDecimationMax) {
      results.error(_rowDecimation, "Row (Xline) decimation cannot be greater than # of rows of input grid.");
    }

    // Validate column decimation is not negative and less than # of columns in grid.
    if (_colDecimation.get() < 1) {
      results.error(_colDecimation, "Column (Inline) decimation cannot be less than 1.");
    }

    if (_colDecimation.get() >= colDecimationMax) {
      results.error(_colDecimation, "Column (Inline) decimation cannot be greater than # of columns of input grid.");
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
    GridGeometry3d geometry = inputGrid.getGeometry();
    int rowDecimation = _rowDecimation.get();
    int colDecimation = _colDecimation.get();
    String outputGridName = _outputGridName.get();
    String outputComments = _outputComments.get();

    // Start the progress monitor.
    monitor.beginTask("Decimating Grid...", geometry.getNumRows());

    // Decimate the grid data.
    float[][] outputData = decimateGridData(inputGrid, rowDecimation, colDecimation);

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
   * GridLines the input grid data.
   */
  public float[][] decimateGridData(final Grid3d inputGrid, final int rowDecimation, final int colDecimation) {

    GridGeometry3d geometry = inputGrid.getGeometry();
    int numRows = geometry.getNumRows();
    int numCols = geometry.getNumColumns();

    float[][] inputData = inputGrid.getValues();
    float[][] outputData = new float[numRows][numCols];
    float nullvalue = inputGrid.getNullValue();

    // GridLines
    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numCols; col++) {
        if (row % rowDecimation == 0 || col % colDecimation == 0) {
          outputData[row][col] = inputData[row][col];
        } else {
          outputData[row][col] = nullvalue;
        }
      }
    }

    return outputData;
  }
}
