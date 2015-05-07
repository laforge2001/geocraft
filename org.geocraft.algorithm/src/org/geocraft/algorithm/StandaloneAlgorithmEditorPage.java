/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.algorithm;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.editor.SharedHeaderFormEditor;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.geocraft.core.model.validation.Validation;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.IModelFormListener;
import org.geocraft.ui.form2.ModelForm;


/**
 * Defines a page for displaying algorithm parameters and their editors.
 */
public class StandaloneAlgorithmEditorPage extends FormPage {

  /** The message manager used to display errors, warnings, etc in the form header. */
  private IMessageManager _messageManager;

  /** The algorithm description for the algorithm being edited. */
  private final IStandaloneAlgorithmDescription _algorithmDescription;

  /** The algorithm being edited. */
  private final StandaloneAlgorithm _algorithm;

  private ModelForm _modelForm;

  private IManagedForm _headerForm;

  private IModelFormListener _listener;

  public StandaloneAlgorithmEditorPage(final SharedHeaderFormEditor editor, final IStandaloneAlgorithmDescription algorithmDescription, final StandaloneAlgorithm algorithm) {
    super(editor, algorithmDescription.getClassName(), "Parameters");
    _algorithmDescription = algorithmDescription;
    _algorithm = algorithm;
    _headerForm = editor.getHeaderForm();
    String name = _algorithmDescription.getName();
    if (name != null) {
      setPartName(name);
    }
  }

  @Override
  public void createFormContent(final IManagedForm managedForm) {
    //_managedForm = managedForm;
    setPartName(_algorithmDescription.getName());
    Composite mainPanel = managedForm.getForm().getBody();//new Composite(managedForm.getForm().getBody(), SWT.NONE);
    managedForm.getToolkit().adapt(mainPanel);

    // Create a parameter form.
    _modelForm = new ModelForm(mainPanel, _headerForm, 3);//, false);

    // Build the algorithm-specific editors in the form.
    _algorithm.setModelForm(_modelForm);
    _algorithm.buildView(_modelForm);
    _modelForm.getComposite().pack();

    // Associated the algorithm with the parameter form.
    _modelForm.setTitle(_algorithmDescription.getName());
    _modelForm.setModel(_algorithm);
    _modelForm.addListener(_listener);
    _algorithm.addListener(_algorithm);

    // Redraw the the managed form.
    managedForm.reflow(true);
    managedForm.getForm().reflow(true);
    managedForm.getForm().redraw();
    managedForm.getForm().update();

    _messageManager = _headerForm.getMessageManager();
    _messageManager.setDecorationPosition(SWT.TOP | SWT.LEFT);

    IWorkbenchHelpSystem help = getSite().getWorkbenchWindow().getWorkbench().getHelpSystem();
    help.setHelp(getPartControl(), ((StandaloneAlgorithmEditorInput) getEditorInput()).getAlgorithmDescription()
        .getHelpId());
    help.setHelp(_headerForm.getForm().getBody(), ((StandaloneAlgorithmEditorInput) getEditorInput())
        .getAlgorithmDescription().getHelpId());

    // Automatically trigger all the property changes to get the initial UI updates.
    //for (String key : _algorithm.getPropertyKeys()) {
    //  _algorithm.propertyChanged(key);
    //}
  }

  public void setListener(IModelFormListener listener) {
    _listener = listener;
  }

  public int getMaxSeverity() {
    // Clear out all messages.
    //_messageManager.removeAllMessages();
    int maxSeverity = IStatus.OK;

    Validation validation = new Validation();
    _modelForm.validate(validation);

    // Get the maximum severity present in the parameter cache.
    for (IStatus status : validation.getStatus()) {
      maxSeverity = Math.max(maxSeverity, status.getSeverity());
    }
    // Look thru the individual parameters, adding error and warning messages as needed.
    //    for (String key : validation.getStatusKeys()) {
    //      IStatus status = validation.getStatus(key);
    //      int severity = status.getSeverity();
    //      if (severity == IStatus.ERROR) {
    //        _messageManager.addMessage(key, key + ": " + status.getMessage(), null, IMessageProvider.ERROR);
    //      } else if (severity == IStatus.WARNING) {
    //        _messageManager.addMessage(key, key + ": " + status.getMessage(), null, IMessageProvider.WARNING);
    //      } else if (severity == IStatus.INFO) {
    //        _messageManager.addMessage(key, key + ": " + status.getMessage(), null, IMessageProvider.INFORMATION);
    //      }
    //    }

    return maxSeverity;
  }

  /**
   * Returns the algorithm being edited.
   */
  public StandaloneAlgorithm getAlgorithm() {
    return _algorithm;
  }

  public IModelForm getModelForm() {
    return _modelForm;
  }

  @Override
  public void dispose() {
    if (_algorithm != null) {
      _algorithm.dispose();
    }
    super.dispose();
  }

  public void setAutoUpdate(final boolean enabled) {
    _messageManager.setAutoUpdate(enabled);
  }
}
