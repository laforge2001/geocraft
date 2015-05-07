/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.geocraft.core.model.IModel;
import org.geocraft.core.model.property.Property;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.model.validation.Validation;
import org.geocraft.core.repository.specification.ISpecification;
import org.geocraft.ui.common.TableWrapLayoutHelper;
import org.geocraft.ui.form2.field.AbstractField;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.EntityComboField;
import org.geocraft.ui.form2.field.ListSelectionField;
import org.geocraft.ui.form2.field.OrderedListField;


public class ModelForm implements IModelForm {

  /** The collection of parameters fields, mapped by parameter keys. */
  private final Map<String, AbstractField> _fieldMap;

  /** The model currently associated with the form. This can be changed. */
  private IModel _model;

  /** The toolkit associated with the parent form. */
  private final FormToolkit _formToolkit;

  /** The main container in which widgets are placed. */
  private Composite _mainComposite;

  /** The managed form associated with the header. */
  private IManagedForm _headerForm;

  /** The collection of model form listeners. */
  private List<IModelFormListener> _listeners;

  /** The title of the form. */
  private String _title;

  /** The collection of sections contained in the form. */
  private List<Section> _sections = new ArrayList<Section>();

  /** The key of the field that triggered the property change. */
  private String _triggerKey = "";

  /**
   * Constructs an empty form for adding parameter editors.
   * @param parent the parent composite.
   * @param managedForm the managed form to use for decoration.
   * @param headerSection <i>true</i> to add a header section; otherwise <i>false</i>.
   */
  public ModelForm(final Composite parent, final IManagedForm managedForm, int maxNumColumns) {//, final boolean headerSection) {

    // Store the reference to the managed form.
    _headerForm = managedForm;

    // Get and store the form toolkit.
    _formToolkit = managedForm.getToolkit();

    // Initialize the collection of form listeners.
    _listeners = Collections.synchronizedList(new ArrayList<IModelFormListener>());

    _formToolkit.adapt(parent, true, true);

    _mainComposite = parent;

    ColumnLayout colLayout = new ColumnLayout();
    colLayout.minNumColumns = 1;
    colLayout.maxNumColumns = maxNumColumns;
    _mainComposite.setLayout(colLayout);

    // Initialize the collection of fields.
    _fieldMap = Collections.synchronizedMap(new HashMap<String, AbstractField>());
  }

  public Composite getComposite() {
    return _mainComposite;
  }

  public IManagedForm getManagedForm() {
    return _headerForm;
  }

  public String getTitle() {
    return _title;
  }

  public void setTitle(String title) {
    _title = title;
  }

  public IModel getModel() {
    return _model;
  }

  public void setModel(final IModel model) {
    // Remove the form as a listener from the old model so it no longer listens for updates.
    if (_model != null) {
      _model.removeListener(this);
    }

    // Set the new model.
    _model = model;

    // Add the form as a listener to the new model.
    if (_model != null) {
      _model.addListener(this);
      for (AbstractField field : _fieldMap.values()) {
        propertyChanged(field.getKey());
      }
    }
  }

  public FormSection addSection(final String label) {
    return addSection(label, false);
  }

  public FormSection addSection(final String label, final boolean expandable) {
    if (expandable) {
      return addSection(label, ExpandableComposite.TITLE_BAR | ExpandableComposite.TREE_NODE);
    }
    return addSection(label, ExpandableComposite.TITLE_BAR);
  }

  /**
   * The internal base method for adding a section to the form.
   * 
   * @param label the text label of the section.
   * @param style the style of the section.
   * @return the created section.
   */
  private FormSection addSection(final String label, final int style) {
    Section section = _formToolkit.createSection(_mainComposite, style);
    FormSection formSection = new FormSection(section, this, _formToolkit);
    section.setText(label);

    Composite client = _formToolkit.createComposite(section, SWT.NONE);
    TableWrapLayout layout = new TableWrapLayout();
    layout.numColumns = 4;
    client.setLayout(layout);
    section.setClient(client);

    section.setExpanded(true);
    _sections.add(section);

    return formSection;
  }

  public Section[] getSections() {
    return _sections.toArray(new Section[0]);
  }

  public void propertyChanged(String key) {
    // Run validation of the model.
    Validation validation = new Validation();
    _model.validate(validation);

    // If the property changed key equals the field trigger key, then there is no need to
    // notify listeners, as the fieldChanged() method will have handled this.
    boolean notifyListeners = !key.equals(_triggerKey);

    // Check if the form contains a field associated with the property that changed.
    if (_fieldMap.containsKey(key)) {
      // If so, and the property change was not triggered from the UI, then update
      // the value in the field.
      AbstractField field = _fieldMap.get(key);
      if (!key.equals(_triggerKey)) {
        try {
          Property property = _model.getProperty(key);
          field.updateField(property.getValueObject());
        } catch (Exception e) {
          // If the model does not contain the property, then simply ignore.
        }
      }

      // Then update the field status, but only if the internal field status is OK.
      IStatus status = validation.getStatus(key);
      if (status != null && field.getInternalStatus().isOK()) {
        field.setStatus(status);
      }
    }

    // Loop thru all the fields in the form.
    for (AbstractField field : _fieldMap.values()) {

      // Get the validation status for the property associated with the field.
      IStatus status = validation.getStatus(field.getKey());

      // Set the field status from the validation results, but only if the internal field status is OK.
      if (field.getInternalStatus().isOK()) {
        field.setStatus(status);
      }
    }

    _triggerKey = "";
    if (notifyListeners) {
      modelFormUpdated(key);
    }
  }

  /**
   * Notifies listeners that the model form has been updated.
   * This indicates either a change in a model property, or a change in a field.
   * 
   * @param key the key of the triggering property or field.
   */
  private void modelFormUpdated(String key) {
    for (IModelFormListener listener : _listeners.toArray(new IModelFormListener[0])) {
      listener.modelFormUpdated(key);
    }
  }

  private void validateObject(String key, Object valueObject) {
    if (_model != null) {
      try {
        _triggerKey = key;
        if (_fieldMap.containsKey(key)) {
          _fieldMap.get(key).setInternalStatus(ValidationStatus.ok());
        }
        // Attempt to set the value into the model.
        // If successful, a property changed event should be triggered, which
        // will ripple back to the propertyChanged() method in this class.
        _model.setValueObject(key, valueObject);
      } catch (Exception ex) {
        // If the attempt to update the model failed, then set the internal
        // status of the field to an error with the appropriate message.
        // Note: this can not be done via the propertyChanged() method, because
        // no property changed event is triggered when a failure occurs.
        _triggerKey = "";
        if (_fieldMap.containsKey(key)) {
          _fieldMap.get(key).setInternalStatus(ValidationStatus.error(ex.getMessage()));
        }
      }
      // Notify listeners that the model form has been updated.
      modelFormUpdated(key);
    }
  }

  /**
   * Invoked when a field in the form has been updated.
   * 
   * @param key the key of the triggering field.
   * @param valueObject the value object of the field.
   */
  public void fieldChanged(String key, Object valueObject) {
    validateObject(key, valueObject);
  }

  /**
   * Invoked when a field in the form has been enabled/disabled.
   * 
   * @param key the key of the triggering field.
   * @param enabled <i>true</i> if enabled; <i>false</i> is disabled.
   */
  public void fieldEnabled(String key, boolean enabled) {
    if (_model != null) {
      Validation validation = new Validation();
      _model.validate(validation);
      if (_fieldMap.containsKey(key)) {
        AbstractField field = _fieldMap.get(key);
        if (!key.equals(_triggerKey)) {
          Property property = _model.getProperty(key);
          field.updateField(property.getValueObject());
        }
        IStatus status = validation.getStatus(key);
        if (status != null && field.getInternalStatus().isOK()) {
          field.setStatus(status);
        }
      }
      _triggerKey = "";
      modelFormUpdated(key);
    }
  }

  public void validate(IValidation validation) {
    for (AbstractField field : _fieldMap.values()) {
      IStatus status = field.getInternalStatus();
      if (!status.isOK()) {
        validation.setStatus(field.getKey(), status);
      }
    }
    _model.validate(validation);
  }

  public Composite createComposite(final String label) {
    return createComposite(label, ExpandableComposite.TREE_NODE | ExpandableComposite.TITLE_BAR);
  }

  public Composite createComposite(final String label, boolean expandable) {
    if (expandable) {
      return createComposite(label, ExpandableComposite.TREE_NODE | ExpandableComposite.TITLE_BAR);
    }
    return createComposite(label, ExpandableComposite.TITLE_BAR);
  }

  public Composite createComposite(final String label, final int style) {
    Section section = _formToolkit.createSection(_mainComposite, style);
    section.setText(label);

    Composite client = _formToolkit.createComposite(section, SWT.NONE);
    client.setLayout(TableWrapLayoutHelper.createLayout(1, false));
    section.setClient(client);
    section.setExpanded(true);
    _sections.add(section);

    return client;
  }

  public void addListener(IModelFormListener listener) {
    if (!_listeners.contains(listener)) {
      _listeners.add(listener);
    }
  }

  public void removeListener(IModelFormListener listener) {
    _listeners.remove(listener);
  }

  protected void mapField(String key, AbstractField field) {
    _fieldMap.put(key, field);
  }

  protected void unmapField(String key) {
    _fieldMap.remove(key);
  }

  public void setFieldVisible(String key, boolean visible, boolean redoLayout) {
    if (_fieldMap.containsKey(key)) {
      _fieldMap.get(key).setVisible(visible);
    }
    if (redoLayout) {
      _mainComposite.layout(true);
    }
  }

  public void redoLayout(String key) {
    if (_fieldMap.containsKey(key)) {
      _fieldMap.get(key).redoLayout();
    }
  }

  public void setFieldEnabled(String key, boolean enabled) {
    if (_fieldMap.containsKey(key)) {
      _fieldMap.get(key).setEnabled(enabled);
    }
  }

  public void setAllFieldsEnabled(boolean enabled) {
    for (Entry<String, AbstractField> e : _fieldMap.entrySet()) {
      e.getValue().setEnabled(enabled);
    }
  }

  public void setFieldLabel(String key, String label) {
    if (_fieldMap.containsKey(key)) {
      _fieldMap.get(key).setLabel(label);
    }
  }

  public void setFieldFilter(String key, ISpecification filter) {
    if (_fieldMap.containsKey(key)) {
      AbstractField field = _fieldMap.get(key);
      if (field instanceof EntityComboField) {
        EntityComboField comboField = (EntityComboField) field;
        comboField.setFilter(filter);
      }
    }
  }

  public void setFieldOptions(String key, Object[] options) {
    if (_fieldMap.containsKey(key)) {
      AbstractField field = _fieldMap.get(key);
      if (field instanceof ComboField) {
        ComboField comboField = (ComboField) field;
        comboField.setOptions(options);
      } else if (field instanceof OrderedListField) {
        OrderedListField listField = (OrderedListField) field;
        listField.setOptions(options);
      } else if (field instanceof ListSelectionField) {
        ListSelectionField listField = (ListSelectionField) field;
        listField.setOptions(options);
      }
    }
  }

  public void dispose() {
    // Remove the form as a model listener.
    if (_model != null) {
      _model.removeListener(this);
    }
    // Clear the internal collections.
    _listeners.clear();
    _fieldMap.clear();
    _mainComposite.dispose();
  }

  public void collapseSections() {
    // Loop thru each section, collapsing each one.
    for (Section section : getSections()) {
      section.setExpanded(false);
    }
  }

  public void expandSections() {
    // Loop thru each section, expanding each one.
    for (Section section : getSections()) {
      section.setExpanded(true);
    }
  }

  public FormToolkit getToolkit() {
    return _formToolkit;
  }

  /**
   * Validation error in the form. Caught when an action is taken, not when a
   * @param key The key of the property that failed validation.
   * @param status The validation error message.
   */
  public void modelFormValidationError(String key, IStatus status) {
    if (_fieldMap.containsKey(key)) {
      _fieldMap.get(key).setInternalStatus(status);
      modelFormUpdated(key);
    }
  }

  private void removeSection(Section section) {
    for (Control control : section.getChildren()) {
      control.dispose();
    }
    section.getClient().dispose();
    if (!section.isDisposed()) {
      section.dispose();
    }
  }

  public void removeAllFields() {
    Section[] sections = getSections();
    int numSections = sections.length;
    for (int i = numSections - 1; i >= 0; i--) {
      Section section = sections[i];
      _sections.remove(section);
      removeSection(section);
    }
    _fieldMap.clear();
    _mainComposite.layout(true);
  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.form2.IModelForm#containsSection(java.lang.String)
   */
  @Override
  public boolean containsSection(String key) {
    for (Section section : _sections) {
      if (key.equals(section.getText())) {
        return true;
      }
    }
    return false;
  }

}
