/*
 * Copyright (C) ConocoPhillips 2009 All Rights Reserved.
 */
package org.geocraft.ui.property;


import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.OnsetType;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.property.DoubleProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.core.service.message.Topic;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.EnhancedComboField;


public class EditGrid3dProperties extends StandaloneAlgorithm {

  /**
   * Enumeration for the items that can be editted
   */
  public enum Grid3dItemToEdit {
    INTERPRETER("Interpreter"),
    COMMENTS("Comments"),
    DATA_UNIT("Data Unit"),
    ONSET_TYPE("Onset Type"),
    NULL_VALUE("Null Value");

    private String _name;

    Grid3dItemToEdit(final String name) {
      _name = name;
    }

    @Override
    public String toString() {
      return _name;
    }
  }

  // UI TYPES
  // Input section
  /** Input grid */
  private EntityProperty<Grid3d> _inputGrid;

  /** The interpreter in the input grid file */
  private StringProperty _inputInterpreter;

  /** The unit of measurement of the data in the input grid file. */
  private EnumProperty<Unit> _inputDataUnit;

  /** The input onset type of the data in the input grid file. */
  private EnumProperty<OnsetType> _inputOnsetType;

  /** The null value */
  private DoubleProperty _inputNullValue;

  /** The comments for the current grid */
  public StringProperty _inputComments;

  /** The interpreter in the grid file */
  private StringProperty _interpreter;

  /** The unit of measurement of the data in the grid file. */
  private EnumProperty<Unit> _dataUnit;

  /** The onset type of the data in the grid file. */
  private EnumProperty<OnsetType> _onsetType;

  /** The null value */
  private DoubleProperty _nullValue;

  /** The comments for the current grid */
  public StringProperty _comments;

  // Current window shell
  Shell _currentWindowShell;

  /** Item to edit */
  private Grid3dItemToEdit _itemToEdit;

  /** Repository item selected */
  private Object _currentSelectedItem = null;

  public EditGrid3dProperties() {
    super();
    _inputGrid = addEntityProperty("Input Grid", Grid3d.class);
    _inputInterpreter = addStringProperty("Input Interpreter", "");
    _inputDataUnit = addEnumProperty("Input Data Unit", Unit.class, Unit.UNDEFINED);
    _inputOnsetType = addEnumProperty("Input Onset Type", OnsetType.class, OnsetType.MINIMUM);
    _inputNullValue = addDoubleProperty("Input Null Value", -999.25);
    _inputComments = addStringProperty("Input Comments", "");
    _interpreter = addStringProperty("Interpreter", "");
    _dataUnit = addEnumProperty("Data Unit", Unit.class, Unit.UNDEFINED);
    _onsetType = addEnumProperty("Onset Type", OnsetType.class, OnsetType.MINIMUM);
    _nullValue = addDoubleProperty("Null Value", -999.25);
    _comments = addStringProperty("Comments", "");
  }

  public void setItemToEdit(Grid3dItemToEdit itemToEdit) {
    // save the item to edit
    _itemToEdit = itemToEdit;
  }

  public void setInputGrid(Grid3d inputGrid) {
    // save the item to edit
    _inputGrid.set(inputGrid);
    propertyChanged(_inputGrid.getKey());
  }

  public void setSelectedItem(Object currentSelectedItem) {
    // save the current selected item from the repository
    _currentSelectedItem = currentSelectedItem;

  }

  @Override
  public void propertyChanged(String key) {
    if (key.equals(_inputGrid.getKey()) && _inputGrid.get() != null) {

      // Change input parameters based on the input grid
      _inputInterpreter.set(_inputGrid.get().getInterpreter());
      _inputDataUnit.set(_inputGrid.get().getDataUnit());
      _inputOnsetType.set(_inputGrid.get().getOnsetType());
      _inputNullValue.set(_inputGrid.get().getNullValue());
      _inputComments.set(_inputGrid.get().getComment());

      // Default the parameters to edit
      _interpreter.set(_inputGrid.get().getInterpreter());
      _dataUnit.set(_inputGrid.get().getDataUnit());
      _onsetType.set(_inputGrid.get().getOnsetType());
      _nullValue.set(_inputGrid.get().getNullValue());
      _comments.set(_inputGrid.get().getComment());
    }
  }

  @Override
  public void validate(IValidation results) {

    // Validate the input grid is non-null and of the correct type.
    if (_inputGrid.isNull()) {
      results.error(_inputGrid, "No input grid specified.");
    }
  }

  /* (non-Javadoc)
   * Construct the algorithm's UI consisting of form fields partitioned into sections: Input,
   * Output, and algorithm Parameters.
   * @see org.geocraft.algorithm.StandaloneAlgorithm#buildView(org.geocraft.algorithm.IModelForm)
   */
  @Override
  public void buildView(IModelForm modelForm) {
    // Build the input parameters section.
    FormSection inputSection = modelForm.addSection("Input", false);
    inputSection.addLabelField(_inputGrid);
    //    inputSection.addEntityComboField(_inputGrid, Grid3d.class);

    FormSection inputGridSection = modelForm.addSection("Input Grid Parameters", false);
    inputGridSection.addLabelField(_inputInterpreter);
    inputGridSection.addLabelField(_inputDataUnit);
    inputGridSection.addLabelField(_inputOnsetType);
    inputGridSection.addLabelField(_inputNullValue);
    inputGridSection.addLabelField(_inputComments);

    FormSection editSection = modelForm.addSection("Grid Parameters to Edit", false);
    editSection.addTextField(_interpreter);
    Unit[] someUnits = Unit.getCommonUnitsByDomain(new Domain[] { Domain.DISTANCE, Domain.TIME, Domain.VELOCITY,
        Domain.VELOCITY_GRADIENT });
    Unit[] allUnits = Unit.getUnitsByDomain(null);
    EnhancedComboField dataUnitField = editSection.addEnhancedComboField(_dataUnit, someUnits, allUnits);
    dataUnitField.setTooltip("The unit of measurement for data values in the grid");

    ComboField onsetTypeField = editSection.addComboField(_onsetType, OnsetType.values());
    onsetTypeField.setTooltip("The onset type of the grid (e.g. minimum, maximum, zero crossing, etc.)");
    editSection.addTextField(_nullValue);
    editSection.addTextBox(_comments);

    // Set an item to be visible according to the item to edit
    boolean interpreterVisible = false;
    boolean dataUnitVisible = false;
    boolean onsetTypeVisible = false;
    boolean nullValueVisible = false;
    if (_itemToEdit != null) {
      if (_itemToEdit.equals(Grid3dItemToEdit.INTERPRETER)) {
        interpreterVisible = true;
      } else if (_itemToEdit.equals(Grid3dItemToEdit.DATA_UNIT)) {
        dataUnitVisible = true;
      } else if (_itemToEdit.equals(Grid3dItemToEdit.ONSET_TYPE)) {
        onsetTypeVisible = true;
      } else if (_itemToEdit.equals(Grid3dItemToEdit.NULL_VALUE)) {
        nullValueVisible = true;
      }
    }
    setFieldVisible(_inputInterpreter, interpreterVisible);
    setFieldVisible(_inputDataUnit, dataUnitVisible);
    setFieldVisible(_dataUnit, dataUnitVisible);
    setFieldVisible(_inputOnsetType, onsetTypeVisible);
    setFieldVisible(_interpreter, interpreterVisible);
    setFieldVisible(_onsetType, onsetTypeVisible);
    setFieldVisible(_inputNullValue, nullValueVisible);
    setFieldVisible(_nullValue, nullValueVisible);

    // Get the current window shell
    _currentWindowShell = modelForm.getComposite().getShell();
  }

  /**
   * Runs the domain logic of the algorithm.
   * @param monitor the progress monitor.
   * @param logger the logger to log messages.
   * @param repository the repository in which to add output entities.
   */

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) {

    // Set the input grid
    Grid3d inputGrid = _inputGrid.get();

    Grid3dItemToEdit editItem = _itemToEdit;

    // Set an item to be visible according to the item to edit
    if (editItem != null) {
      boolean itemEditted = false;
      if (editItem.equals(Grid3dItemToEdit.INTERPRETER)) {
        inputGrid.setInterpreter(_interpreter.get());
        itemEditted = true;
      } else if (editItem.equals(Grid3dItemToEdit.DATA_UNIT)) {
        // Set the data unit
        inputGrid.setDataUnit(_dataUnit.get());
        itemEditted = true;
      } else if (editItem.equals(Grid3dItemToEdit.ONSET_TYPE)) {
        // Set the onset type
        inputGrid.setOnsetType(_onsetType.get());
        itemEditted = true;
      } else if (editItem.equals(Grid3dItemToEdit.NULL_VALUE)) {
        // Set the null value
        float nullValue = (float) _nullValue.get();
        inputGrid.setNullValue(nullValue);
        itemEditted = true;
      }

      // add comments to the grid
      if (_comments.get() != "") {
        inputGrid.setComment(_comments.get());
        itemEditted = true;
      }

      // Perform an update to remove the dirty flag
      if (itemEditted) {
        try {
          inputGrid.update();
        } catch (IOException ex) {
          throw new RuntimeException(ex.getMessage());
        }

        // set the current selected item from the repository
        final Object currentSelectedItem = _currentSelectedItem;

        // Notify the PropertiesView to redisplay the current selected item
        if (currentSelectedItem != null) {
          Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
              ServiceProvider.getMessageService().publish(Topic.REPOSITORY_NODE_SELECTED, currentSelectedItem);
            }
          });

        }
      }
    }

    // Close the window shell
    Display.getDefault().asyncExec(new Runnable() {

      @Override
      public void run() {
        _currentWindowShell.close();
      }
    });

    // Task is done.
    monitor.done();
  }

}
