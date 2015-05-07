/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.algorithm;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IMessageManager;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.model.IModelListener;
import org.geocraft.core.model.validation.Validation;
import org.geocraft.ui.form2.IModelFormListener;
import org.geocraft.ui.form2.ModelForm;


public class CreateFormDialog extends FormDialog implements IModelListener, IModelFormListener {

  /** The message manager used to display errors, warnings, etc in the form header. */
  private IMessageManager _messageManager;

  private final StandaloneAlgorithm _algorithm;

  private ModelForm _modelForm;

  /** The managed form. */
  protected IManagedForm _managedForm;

  public CreateFormDialog(final Shell shell, final StandaloneAlgorithm model) {
    super(shell);
    _algorithm = model;
  }

  @Override
  protected void createFormContent(final IManagedForm managedForm) {
    Composite mainPanel = managedForm.getForm().getBody();
    managedForm.getToolkit().adapt(mainPanel);

    managedForm.getToolkit().adapt(mainPanel);

    _managedForm = managedForm;

    // Create a parameter form.
    _modelForm = new ModelForm(mainPanel, managedForm, 3);
    // Build the algorithm-specific editors in the form.
    _algorithm.addListener(_algorithm);
    _algorithm.setModelForm(_modelForm);
    _algorithm.buildView(_modelForm);
    _modelForm.getComposite().pack();

    // Associated the algorithm with the parameter form.
    //    _modelForm.setTitle(_algorithmDescription.getName());
    _modelForm.setModel(_algorithm);
    _modelForm.addListener(this);
    _algorithm.addListener(this);

    // Redraw the the managed form.
    managedForm.reflow(true);
    managedForm.getForm().reflow(true);
    managedForm.getForm().redraw();
    managedForm.getForm().update();

    _messageManager = managedForm.getMessageManager();
    _messageManager.setDecorationPosition(SWT.TOP | SWT.LEFT);
  }

  @Override
  protected Control createButtonBar(Composite parent) {
    Control control = super.createButtonBar(parent);
    propertyChanged("");
    return control;
  }

  public void modelFormUpdated(String key) {
    propertyChanged(key);
  }

  public void propertyChanged(String triggerKey) {

    IMessageManager messageManager = _managedForm.getMessageManager();
    // Clear out all messages.
    messageManager.removeAllMessages();
    int maxSeverity = IStatus.OK;
    Validation validation = new Validation();
    _modelForm.validate(validation);

    //_model.validate(validation);
    // Get the maximum severity present in the parameter cache.
    for (IStatus status : validation.getStatus()) {
      maxSeverity = Math.max(maxSeverity, status.getSeverity());
    }
    // Look thru the individual parameters, adding error and warning messages as needed.
    for (String key : validation.getStatusKeys()) {
      IStatus status = validation.getStatus(key);
      int severity = status.getSeverity();
      if (severity == IStatus.ERROR) {
        messageManager.addMessage(key, status.getMessage(), null, IMessageProvider.ERROR);
      } else if (severity == IStatus.WARNING) {
        messageManager.addMessage(key, status.getMessage(), null, IMessageProvider.WARNING);
      } else if (severity == IStatus.INFO) {
        messageManager.addMessage(key, status.getMessage(), null, IMessageProvider.INFORMATION);
      }
    }
    Button okButton = getButton(IDialogConstants.OK_ID);
    if (okButton != null) {
      okButton.setEnabled(maxSeverity != IStatus.ERROR);
    }
    Button yesButton = getButton(IDialogConstants.YES_ID);
    if (yesButton != null) {
      yesButton.setEnabled(maxSeverity != IStatus.ERROR);
    }
  }

  @Override
  protected void okPressed() {
    // Run the task
    Thread t = new Thread(new Runnable() {

      public void run() {
        StandaloneAlgorithmTask task = new StandaloneAlgorithmTask(_algorithm, "Dialog");
        TaskRunner.runTask(task, task.toString(), TaskRunner.NO_JOIN);
      }
    });
    t.start();
    close();
  }
}
