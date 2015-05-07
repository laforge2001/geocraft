/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IMessageManager;
import org.geocraft.core.model.IModel;
import org.geocraft.core.model.validation.Validation;


/**
 * An abstract base class for constructing view composites for editing models.
 * A view composite consists of the controls used to edit the model properties.
 * The model displayed can be changed, and the view will automatically update
 * to reflect the properties of the new model.
 */
public abstract class AbstractModelView implements IModelFormListener {

  /** The model form associated with the view. */
  private IModelForm _modelForm;

  public AbstractModelView() {
    // The zero argument constructor.
  }

  public void buildView(Composite parent, IManagedForm form) {//, boolean headerSection) {
    // Construct a new model form and add this view as a listener.
    _modelForm = new ModelForm(parent, form, 1);//, headerSection);
    _modelForm.addListener(this);

    // Call the method to build the custom controls.
    buildView(_modelForm);

    _modelForm.getComposite().pack();
  }

  /**
   * Add the custom sections and controls to the view.
   * These are added to the associated model form, which is passed in as the sole parameter.
   * @see <code>IModelForm</code> for API methods
   * 
   * @param modelForm the associated model form.
   */
  public abstract void buildView(IModelForm modelForm);

  /**
   * Sets the model whose properties are to be displayed in the view.
   * The controls will automatically be updated to reflect the new model.
   * 
   * @param model the model to set.
   */
  public void setModel(IModel model) {
    _modelForm.setModel(model);
  }

  /**
   * Invoked when either a model property or form field is changed.
   * This method should provide logic for updating the view based
   * on the property that changed.
   * 
   * @param key the key of the property that changed.
   */
  public void updateView(String key) {
    // Sub-classes should override this method.
  }

  /**
   * Returns the model form associated with the view.
   * 
   * @return the model form.
   */
  public IModelForm getModelForm() {
    return _modelForm;
  }

  /**
   * Returns the model whose properties are currently displayed in the view.
   * 
   * @return the model.
   */
  public IModel getModel() {
    return _modelForm.getModel();
  }

  /**
   * Returns the managed form associated with the view.
   * The managed form is used for displayed error messages.
   * 
   * @return the managed form.
   */
  public IManagedForm getManagedForm() {
    return _modelForm.getManagedForm();
  }

  public void modelFormUpdated(String triggerKey) {
    // Get the message manager.
    IMessageManager messageManager = getManagedForm().getMessageManager();

    // Turn off auto-updating of message manager.
    messageManager.setAutoUpdate(false);

    // Clear out all messages.
    messageManager.removeAllMessages();
    int maxSeverity = IStatus.OK;

    Validation validation = validate();
    // Get the maximum severity present in the validation.
    for (IStatus status : validation.getStatus()) {
      maxSeverity = Math.max(maxSeverity, status.getSeverity());
    }
    // Look thru the individual properties, adding error and warning messages as needed.
    for (String key : validation.getStatusKeys()) {
      IStatus status = validation.getStatus(key);
      int severity = status.getSeverity();
      if (severity == IStatus.ERROR) {
        messageManager.addMessage(key, key + ": " + status.getMessage(), null, IMessageProvider.ERROR);
      } else if (severity == IStatus.WARNING) {
        messageManager.addMessage(key, key + ": " + status.getMessage(), null, IMessageProvider.WARNING);
      } else if (severity == IStatus.INFO) {
        messageManager.addMessage(key, key + ": " + status.getMessage(), null, IMessageProvider.INFORMATION);
      }
    }

    // Turn on auto-updating of message manager.
    messageManager.setAutoUpdate(true);

    updateView(triggerKey);
  }

  /**
   * Validates the view.
   * This is done by validating the associated model form.
   * 
   * @return the validation results.
   */
  protected Validation validate() {
    Validation validation = new Validation();
    getModelForm().validate(validation);
    return validation;
  }

  /**
   * Collapses all the sections in the view.
   */
  public void collapseSections() {
    // Can collapse only if a model form exists.
    if (_modelForm != null) {
      _modelForm.collapseSections();
    }
  }

  /**
   * Expands all the sections in the view.
   */
  public void expandSections() {
    // Can expand only if a model form exists.
    if (_modelForm != null) {
      _modelForm.expandSections();
    }
  }

  /**
   * Enabled/disables a property field.
   * 
   * @param key the key of the field to enabled/disable.
   * @param enabled <i>true</i> to enabled the field; <i>false</i> to disable.
   */
  public void setFieldEnabled(String key, boolean enabled) {
    // Can enable/disable field only if a model form exists.
    if (_modelForm != null) {
      _modelForm.setFieldEnabled(key, enabled);
    }
  }

  /**
   * Sets the options available on a combo field.
   * 
   * @param key the key of the field to update.
   * @param options the array of options to set.
   */
  public void setFieldOptions(final String key, Object[] options) {
    if (_modelForm != null) {
      _modelForm.setFieldOptions(key, options);
    }
  }

}
