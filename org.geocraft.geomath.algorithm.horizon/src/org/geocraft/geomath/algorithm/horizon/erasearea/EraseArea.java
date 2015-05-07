package org.geocraft.geomath.algorithm.horizon.erasearea;


import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.factory.model.Grid3dFactory;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;


public class EraseArea extends StandaloneAlgorithm {

  /** The input grid property. */
  public EntityProperty<Grid3d> _inputGrid;

  /** The area-of-interest property. */
  public EntityProperty<AreaOfInterest> _areaOfInterest;

  /** The output grid name property. */
  public StringProperty _outputGridName;

  /** The output comments property. */
  public StringProperty _outputComments;

  public EraseArea() {
    _inputGrid = addEntityProperty("Input Grid", Grid3d.class);
    _areaOfInterest = addEntityProperty("Area-of-Interest", AreaOfInterest.class);
    _outputGridName = addStringProperty("Output Grid Name", "");
    _outputComments = addStringProperty("Output Comments", "");
  }

  @Override
  public void buildView(IModelForm form) {
    // Build the input parameters section.
    FormSection inputSection = form.addSection("Input", false);
    inputSection.addEntityComboField(_inputGrid, Grid3d.class);
    inputSection.addEntityComboField(_areaOfInterest, AreaOfInterest.class);

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
            .createOutputDisplayName(_inputGrid.get().getDisplayName(), "_erase");
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

    if (_areaOfInterest.isNull()) {
      results.error(_areaOfInterest, "No input area of interest specified.");
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
    String outputGridName = _outputGridName.get();
    String outputComments = _outputComments.get();

    // Start the progress monitor.
    monitor.beginTask("Erasing Area of Grid...", inputGrid.getGeometry().getNumRows());

    // Erase area of the grid data.
    float[][] outputData = eraseGridData(inputGrid, aoi, monitor);

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

  public float[][] eraseGridData(final Grid3d inputGrid, final AreaOfInterest aoi, final IProgressMonitor monitor) {

    GridGeometry3d geometry = inputGrid.getGeometry();
    int numRows = geometry.getNumRows();
    int numCols = geometry.getNumColumns();

    float[][] inputData = inputGrid.getValues();
    float[][] outputData = new float[numRows][numCols];
    float nullvalue = inputGrid.getNullValue();

    for (int row = 0; row < numRows && !monitor.isCanceled(); row++) {
      for (int col = 0; col < numCols; col++) {
        double[] xy = geometry.transformRowColToXY(row, col);
        if (aoi.contains(xy[0], xy[1])) {
          outputData[row][col] = nullvalue;
        } else {
          outputData[row][col] = inputData[row][col];
        }
      }
      monitor.worked(1);
    }

    return outputData;
  }

}
