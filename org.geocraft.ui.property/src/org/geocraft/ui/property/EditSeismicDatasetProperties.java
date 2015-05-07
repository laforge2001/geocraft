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
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.core.service.message.Topic;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.EnhancedComboField;


public class EditSeismicDatasetProperties extends StandaloneAlgorithm {

  /**
   * Enumeration for the items that can be editted
   */
  public enum SeismicDatasetItemToEdit {
    COMMENTS("Comments"),
    DATA_UNIT("Data Unit");

    private String _name;

    SeismicDatasetItemToEdit(final String name) {
      _name = name;
    }

    @Override
    public String toString() {
      return _name;
    }
  }

  // UI TYPE
  // Input section
  /** Input volume */
  private EntityProperty<SeismicDataset> _inputVolume;

  /** The comments for the current volume */
  public StringProperty _inputComments;

  /** The input unit of measurement of the data in the volume */
  private EnumProperty<Unit> _inputDataUnit;

  /** The comments for the current volume */
  public StringProperty _comments;

  /** The unit of measurement of the data in the volume */
  private EnumProperty<Unit> _dataUnit;

  // Current window shell
  Shell _currentWindowShell;

  /** Item to edit */
  private SeismicDatasetItemToEdit _itemToEdit;

  /** Repository item selected */
  private Object _currentSelectedItem = null;

  public EditSeismicDatasetProperties() {
    super();
    _inputVolume = addEntityProperty("Input SeismicDataset", SeismicDataset.class);
    _inputComments = addStringProperty("Input Comments", "");
    _inputDataUnit = addEnumProperty("Input Data Unit", Unit.class, Unit.UNDEFINED);
    _comments = addStringProperty("Comments", "");
    _dataUnit = addEnumProperty("Data Unit", Unit.class, Unit.UNDEFINED);
  }

  public void setItemToEdit(SeismicDatasetItemToEdit itemToEdit) {
    // save the item to edit
    _itemToEdit = itemToEdit;
  }

  public void setInputVolume(SeismicDataset inputVolume) {
    // save the item to edit
    _inputVolume.set(inputVolume);
    propertyChanged(_inputVolume.getKey());
  }

  public void setSelectedItem(Object currentSelectedItem) {
    // save the current selected item from the repository
    _currentSelectedItem = currentSelectedItem;

  }

  @Override
  public void propertyChanged(String key) {
    if (key.equals(_inputVolume.getKey()) && _inputVolume.get() != null) {

      // Change input parameters based on the input volume
      _inputComments.set(_inputVolume.get().getComment());
      _inputDataUnit.set(_inputVolume.get().getDataUnit());

      // Default the parameters to edit
      _comments.set(_inputVolume.get().getComment());
      _dataUnit.set(_inputVolume.get().getDataUnit());
    }
  }

  @Override
  public void validate(IValidation results) {

    // Validate the input volume is non-null and of the correct type.
    if (_inputVolume.isNull()) {
      results.error(_inputVolume, "No input SeismicDataset specified.");
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
    inputSection.addEntityComboField(_inputVolume, SeismicDataset.class);

    FormSection inputVolumeSection = modelForm.addSection("Input SeismicDataset Parameters", false);
    inputVolumeSection.addLabelField(_inputDataUnit);
    inputVolumeSection.addLabelField(_inputComments);

    FormSection editSection = modelForm.addSection("SeismicDataset Parameters to Edit", false);
    Unit[] someUnits = Unit.getCommonUnitsByDomain(new Domain[] { Domain.VELOCITY, Domain.DIMENSIONLESS,
        Domain.VELOCITY_GRADIENT, Domain.DISTANCE, Domain.TIME });
    Unit[] allUnits = Unit.getUnitsByDomain(null);
    EnhancedComboField dataUnitField = editSection.addEnhancedComboField(_dataUnit, someUnits, allUnits);
    dataUnitField.setTooltip("The unit of measurement for data values in the SeismicDataset");

    editSection.addTextBox(_comments);

    // Set an item to be visible according to the item to edit
    boolean dataUnitVisible = false;
    if (_itemToEdit != null) {
      if (_itemToEdit.equals(SeismicDatasetItemToEdit.DATA_UNIT)) {
        dataUnitVisible = true;
      }
    }
    setFieldVisible(_inputDataUnit, dataUnitVisible);
    setFieldVisible(_dataUnit, dataUnitVisible);

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

    // Set the input volume
    SeismicDataset inputVolume = _inputVolume.get();

    SeismicDatasetItemToEdit editItem = _itemToEdit;

    // Set an item to be visible according to the item to edit
    if (editItem != null) {
      boolean itemEditted = false;
      if (editItem.equals(SeismicDatasetItemToEdit.DATA_UNIT)) {
        // Set the data unit
        inputVolume.setDataUnit(_dataUnit.get());
        itemEditted = true;
      }

      // add comments to the volume
      if (_comments.get() != "") {
        inputVolume.setComment(_comments.get());
        itemEditted = true;
      }

      // Perform an update to remove the dirty flag
      if (itemEditted) {
        try {
          inputVolume.update();
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
