/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.geocraft.core.model.IModel;
import org.geocraft.core.model.IModelListener;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.specification.ISpecification;
import org.geocraft.ui.form2.field.IFieldListener;


/**
 * The interface for building a parameterized form.
 * It provides convenience method for adding commonly-used UI components
 * to a form that is associated with a parameter model. These components
 * include such things as text fields, combo boxes, radio buttons, etc.
 */
public interface IModelForm extends IFieldListener, IModelListener {

  /**
   * Adds a listener to the model form.
   * @param listener the listener to add.
   */
  void addListener(IModelFormListener listener);

  /**
   * Removes a listener from the model form.
   * @param listener the listener to remove.
   */
  void removeListener(IModelFormListener listener);

  /**
   * Returns the form composite.
   */
  Composite getComposite();

  /**
   * Returns the managed form associated with the model form.
   */
  IManagedForm getManagedForm();

  /**
   * Runs the validation on the fields and the model associated with the form.
   * @param validation the container for the validation results.
   */
  void validate(IValidation validation);

  /**
   * Adds a section to the form.
   * @param label the text label of the section.
   * @return the created section.
   */
  FormSection addSection(final String label);

  /**
   * Adds a section to the form.
   * @param label the text label of the section.
   * @param expandable <i>true</i> to make an expandable tree section; otherwise </i>false</i>.
   * @return the created section.
   */
  FormSection addSection(final String label, boolean expandable);

  /**
   * An "escape hatch" method that returns a composite in which
   * users can add custom UI components using SWT directly.
   * Note: These custom components will not have any automatic
   * interaction with the underlying model.
   * 
   * @param label the label of the composite section.
   * @return the composite widget.
   */
  Composite createComposite(final String label);

  /**
   * An "escape hatch" method that returns a composite in which
   * users can add custom UI components using SWT directly.
   * Note: These custom components will not have any automatic
   * interaction with the underlying model.
   * 
   * @param label the label of the composite section.
   * @return the composite widget.
   */
  Composite createComposite(final String label, int style);

  /**
   * An "escape hatch" method that returns a composite in which
   * users can add custom UI components using SWT directly.
   * Note: These custom components will not have any automatic
   * interaction with the underlying model.
   * 
   * @param label the label of the composite section.
   * @param expandable <i>true</i> to make the section expandable; otherwise <i>false</i>.
   * @return the composite widget.
   */
  Composite createComposite(final String label, boolean expandable);

  /**
   * Gets the title of the model form.
   *
   * @return the title.
   */
  String getTitle();

  /**
   * Sets the title of the model form.
   *
   * @param title the title to set.
   */
  void setTitle(String title);

  /**
   * Sets the model to associate with the model form.
   * 
   * @param model the model to set.
   */
  void setModel(IModel model);

  /**
   * Returns the model associated with the model form.
   */
  IModel getModel();

  /**
   * Returns an array of the sections contained in the form.
   */
  Section[] getSections();

  /**
   * Hide/Show a field.
   * 
   * @param key The key of the field to hide/show.
   * @param visible <i>true</i> to show the field; <i>false</i> to hide the field.
   * @param redoLayout <i>true</i> to force a redo of the layout; otherwise <i>false</i>.
   */
  void setFieldVisible(String key, boolean visible, boolean redoLayout);

  /**
   * Redo the layout of the Composite housing the field.
   * @param key The key of the field
   */
  public void redoLayout(String key);

  /**
   * Enable/disable a field in the form.
   * 
   * @param key The key of the field to enable/disable.
   * @param enabled <i>true</i> to enable the field; <i>false</i> to disable the field.
   */
  void setFieldEnabled(String key, boolean enabled);

  /**
   * Enable/disable all the fields in the form.
   * 
   * @param enabled <i>true</i> to enable the field; <i>false</i> to disable the field.
   */
  void setAllFieldsEnabled(boolean enabled);

  /**
   * Sets the label on a field.
   * 
   * @param key the key of the field to update.
   * @param label the label to set.
   */
  void setFieldLabel(String key, String label);

  /**
   * Sets the options available on an entity combo field.
   * 
   * @param key the key of the field to update.
   * @param filter the filter to set.
   */
  void setFieldFilter(String key, ISpecification filter);

  /**
   * Sets the options available on a combo field.
   * 
   * @param key the key of the field to update.
   * @param options the array of options to set.
   */
  void setFieldOptions(String key, Object[] options);

  /**
   * Collapses all sections of the model form.
   */
  void collapseSections();

  /**
   * Expands all sections of the model form.
   */
  void expandSections();

  /**
   * Returns the form toolkit.
   * @return
   */
  FormToolkit getToolkit();

  void removeAllFields();

  boolean containsSection(String key);

}