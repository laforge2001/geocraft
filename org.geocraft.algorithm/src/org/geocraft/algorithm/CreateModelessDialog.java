/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.algorithm;


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IMessageManager;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.ui.form2.ModelForm;


public class CreateModelessDialog extends FormDialog {

  /** The message manager used to display errors, warnings, etc in the form header. */
  private IMessageManager _messageManager;

  private final StandaloneAlgorithm _algorithm;

  private ModelForm _modelForm;

  public CreateModelessDialog(final Shell shell, final StandaloneAlgorithm model) {
    super(shell);
    _algorithm = model;
    setShellStyle(SWT.DIALOG_TRIM | SWT.MODELESS);
  }

  @Override
  protected void createFormContent(final IManagedForm managedForm) {
    Composite mainPanel = managedForm.getForm().getBody();
    managedForm.getToolkit().adapt(mainPanel);

    managedForm.getToolkit().adapt(mainPanel);

    // Create a parameter form.
    _modelForm = new ModelForm(mainPanel, managedForm, 3);
    // Build the algorithm-specific editors in the form.
    _algorithm.addListener(_algorithm);
    _algorithm.setModelForm(_modelForm);
    _algorithm.buildView(_modelForm);
    _modelForm.getComposite().pack();

    // Associated the algorithm with the parameter form.
    _modelForm.getComposite().getShell().setText(_algorithm.getClass().getSimpleName());
    _modelForm.setModel(_algorithm);

    // Redraw the the managed form.
    managedForm.reflow(true);
    managedForm.getForm().reflow(true);
    managedForm.getForm().redraw();
    managedForm.getForm().update();

    _messageManager = managedForm.getMessageManager();
    _messageManager.setDecorationPosition(SWT.TOP | SWT.LEFT);
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
  }

  public void renameOKButton(String Name) {
    // Change the Name of OK Button
    Button okButton = this.getButton(IDialogConstants.OK_ID);
    okButton.setText(Name);
  }
}
