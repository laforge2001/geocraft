package org.geocraft.geomath.algorithm.horizon.velocitymodelchecker;


import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.factory.model.Grid3dFactory;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.TextField;


public class VelocityModelChecker extends StandaloneAlgorithm {

  public enum CheckerValueColor {
    RED(10, "10 - (Red)"),
    YELLOW(33, "33 - (Yellow)"),
    GREEN(60, "60 - (Green)"),
    CYAN(80, "80 - (Cyan)"),
    BLUE(100, "100 - (Blue)");

    private Integer _value;

    private String _color;

    CheckerValueColor(final int value, final String color) {
      _value = value;
      _color = color;
    }

    public Integer getValue() {
      return _value;
    }

    @Override
    public String toString() {
      return _color;
    }

  }

  private EntityProperty<Grid3d> _topGrid;

  private EntityProperty<Grid3d> _baseGrid;

  private StringProperty _outputGridName;

  private EnumProperty<CheckerValueColor> _holeInTopGrid;

  private EnumProperty<CheckerValueColor> _holeInBaseGrid;

  private EnumProperty<CheckerValueColor> _holesInBothGrids;

  private EnumProperty<CheckerValueColor> _baseGridOverTopGrid;

  private EnumProperty<CheckerValueColor> _noConflicts;

  public VelocityModelChecker() {
    _topGrid = addEntityProperty("Top Grid", Grid3d.class);
    _baseGrid = addEntityProperty("Base Grid", Grid3d.class);
    _outputGridName = addStringProperty("Output Grid Name", "");
    _holeInTopGrid = addEnumProperty("Holes in Top Grid", CheckerValueColor.class, CheckerValueColor.YELLOW);
    _holeInBaseGrid = addEnumProperty("Holes in Base Grid", CheckerValueColor.class, CheckerValueColor.RED);
    _holesInBothGrids = addEnumProperty("Holes in Both Grids", CheckerValueColor.class, CheckerValueColor.GREEN);
    _baseGridOverTopGrid = addEnumProperty("Base Greater than Top", CheckerValueColor.class, CheckerValueColor.BLUE);
    _noConflicts = addEnumProperty("No Conflicts", CheckerValueColor.class, CheckerValueColor.CYAN);
  }

  @Override
  public void buildView(IModelForm form) {
    FormSection inputSection = form.addSection("Input");

    ComboField topGrid = inputSection.addEntityComboField(_topGrid, Grid3d.class);
    topGrid.setTooltip("First input for the velocity model checker.");

    ComboField baseGrid = inputSection.addEntityComboField(_baseGrid, Grid3d.class);
    baseGrid.setTooltip("Second input for the velocity model checker.");

    FormSection checkerSection = form.addSection("Parameters");

    String comment = "Select the value codes for each condition. The associated color represents the "
        + "value code when using a Spectrum color bar for display (normalized to the range 0-100).";
    checkerSection.addCommentField(comment);

    ComboField holeInTop = checkerSection.addComboField(_holeInTopGrid, CheckerValueColor.values());
    holeInTop.setTooltip("This will set the color of holes in the top horizon");

    ComboField holeInBottom = checkerSection.addComboField(_holeInBaseGrid, CheckerValueColor.values());
    holeInBottom.setTooltip("This will set the color of holes in the base horizon");

    ComboField holesInBoth = checkerSection.addComboField(_holesInBothGrids, CheckerValueColor.values());
    holesInBoth
        .setTooltip("This will set the color of locations where there are holes in the same location for both horizons");

    ComboField baseOverTop = checkerSection.addComboField(_baseGridOverTopGrid, CheckerValueColor.values());
    baseOverTop.setTooltip("This will set the color of areas where the base horizon is above the top horizon");

    ComboField noConflicts = checkerSection.addComboField(_noConflicts, CheckerValueColor.values());
    noConflicts.setTooltip("This will set the color of areas where there are no overlap in horizons");

    FormSection outputSection = form.addSection("Output");

    TextField outputName = outputSection.addTextField(_outputGridName);
    outputName.setTooltip("Name for the output horizon");
  }

  public void propertyChanged(String key) {
    // No UI updates.
  }

  public void validate(IValidation results) {
    if (_topGrid.isNull()) {
      results.error(_topGrid, "Top grid not specified.");
    }
    if (_baseGrid.isNull()) {
      results.error(_baseGrid, "Base grid not specified.");
    }
    if (!_topGrid.isNull() && !_baseGrid.isNull()) {
      if (!_topGrid.get().getGeometry().matchesGeometry(_baseGrid.get().getGeometry())) {
        results.error(_baseGrid, "Mismatch in grid geometry.");
      }
    }
    if (_outputGridName.isEmpty()) {
      results.error(_outputGridName, "Output grid name not specified.");
    }
    if (_holeInTopGrid.isNull()) {
      results.error(_holeInTopGrid, "Color not set for holes in top grid.");
    }
    if (_holeInBaseGrid.isNull()) {
      results.error(_holeInBaseGrid, "Color not set for holes in base grid.");
    }
    if (_holesInBothGrids.isNull()) {
      results.error(_holesInBothGrids, "Color not set for holes in top and base grids.");
    }
    if (_baseGridOverTopGrid.isNull()) {
      results.error(_baseGridOverTopGrid, "Color not set for base grid over top grid.");
    }
    if (_noConflicts.isNull()) {
      results.error(_noConflicts, "Color not set for no conflicts.");
    }
  }

  /**
   * @throws CoreException  
   */
  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) throws CoreException {
    Grid3d topGrid = _topGrid.get();
    Grid3d baseGrid = _baseGrid.get();
    String outputGridName = _outputGridName.get();
    CheckerValueColor holeInTop = _holeInTopGrid.get();
    CheckerValueColor holeInBase = _holeInBaseGrid.get();
    CheckerValueColor holesInBoth = _holesInBothGrids.get();
    CheckerValueColor baseOverTop = _baseGridOverTopGrid.get();
    CheckerValueColor noConflicts = _noConflicts.get();

    // Start the progress monitor.
    monitor.beginTask("Checking Grids...", topGrid.getNumRows());

    // Clip the grid data.
    float[][] outputData = checkVelocityModelGrids(monitor, logger, topGrid, baseGrid, holeInTop, holeInBase,
        holesInBoth, baseOverTop, noConflicts);

    // Only create the output grid if the job completed normally.
    if (!monitor.isCanceled()) {
      // Find (or create) the output grid and update it in the datastore.
      try {
        Grid3d outputGrid = Grid3dFactory.create(repository, topGrid, outputData, outputGridName);
        outputGrid.update();
      } catch (IOException ex) {
        throw new RuntimeException(ex.getMessage());
      }
    }

    // Task is done.
    monitor.done();
  }

  private float[][] checkVelocityModelGrids(final IProgressMonitor monitor, final ILogger logger, Grid3d topGrid,
      Grid3d baseGrid, CheckerValueColor holeInTopGrid, CheckerValueColor holeInBaseGrid,
      CheckerValueColor holesInBothGrids, CheckerValueColor baseGridOverTopGrid, CheckerValueColor noConflicts) {

    // Determine the largest dimensions of both of the horizons
    // And set the result to that.
    int numRows = topGrid.getNumRows();
    int numCols = topGrid.getNumColumns();
    float[][] outputData = new float[numRows][numCols];

    boolean isTopHole;
    boolean isBaseHole;
    boolean isBaseOverTop;

    // Loop over the rows.
    for (int row = 0; row < numRows; ++row) {

      // Loop over the columns.
      for (int col = 0; col < numCols; ++col) {
        // Check for holes in the top and base grids.
        isTopHole = topGrid.isNull(row, col);
        isBaseHole = baseGrid.isNull(row, col);

        // Determine if the base grid is over the top grid.
        isBaseOverTop = isBaseGridOverTopGrid(topGrid, baseGrid, row, col);

        // Set the output value at the given location.
        if (isTopHole && isBaseHole) {
          outputData[row][col] = holesInBothGrids.getValue();
        } else if (isTopHole) {
          outputData[row][col] = holeInTopGrid.getValue();
        } else if (isBaseHole) {
          outputData[row][col] = holeInBaseGrid.getValue();
        } else if (isBaseOverTop) {
          outputData[row][col] = baseGridOverTopGrid.getValue();
        } else {
          outputData[row][col] = noConflicts.getValue();
        }
      }
      // TODO: This is a hack to ensure the default range is 0-100 when displayed in map view.
      outputData[0][0] = 0;
      outputData[0][1] = 100;

      // Update the progress monitor.
      monitor.worked(1);
      monitor.subTask("Completed row " + row);
    }

    return outputData;
  }

  /**
   * Checks if the base grid is "above" the top grid and the given row,col location.
   * If either the top grid or base grid is null, then <i>false</i> is returned.
   * 
   * @param topGrid the top grid.
   * @param baseGrid the base grid.
   * @param row the row to check.
   * @param col the column to check.
   * @return <i>true<i> if both grid are non-null and base is "above" top; otherwise <i>false</i>.
   */
  private boolean isBaseGridOverTopGrid(Grid3d topGrid, Grid3d baseGrid, final int row, final int col) {
    if (!topGrid.isNull(row, col) && !baseGrid.isNull(row, col)) {
      return topGrid.getValueAtRowCol(row, col) > baseGrid.getValueAtRowCol(row, col);
    }
    return false;
  }

}
