/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.geocraft.core.model.IModel;
import org.geocraft.core.model.IModelListener;
import org.geocraft.core.model.validation.Validation;
import org.geocraft.ui.common.GridLayoutHelper;


/**
 * An abstract base class for constructing dialogs for editing models.
 * A model dialog consists of the controls used to edit the model properties.
 * The model displayed can be changed, and the view will automatically update
 * to reflect the properties of the new model.
 */
public abstract class ModelDialog extends FormDialog implements IModelListener, IModelFormListener {

  /** The managed form. */
  protected IManagedForm _managedForm;

  /** The dialog title. */
  protected String _title;

  /** The list of model forms. */
  protected List<ModelForm> _modelForms;

  /** The model of parameters to edit. */
  protected IModel _model;

  /** The model of parameters to use for the undo operation. */
  protected IModel _modelUndo;

  /**
   * The constructor.
   * @param shell the parent shell
   * @param title the dialog title
   */
  public ModelDialog(final Shell shell, final String title) {
    super(shell);
    _title = title;
    _modelForms = new ArrayList<ModelForm>();
    setShellStyle(SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
  }

  @Override
  protected Control createButtonBar(Composite parent) {
    Control control = super.createButtonBar(parent);
    propertyChanged("");
    return control;
  }

  @Override
  public void createButtonsForButtonBar(final Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    createButton(parent, IDialogConstants.YES_ID, "Apply", false);
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
  }

  @Override
  protected void createFormContent(final IManagedForm managedForm) {
    getShell().setText(_title);
    _managedForm = managedForm;
    _managedForm.getForm().setAlwaysShowScrollBars(false);
    managedForm.getForm().setText(_title);
    managedForm.getToolkit().decorateFormHeading(managedForm.getForm().getForm());

    FormToolkit toolkit = managedForm.getToolkit();

    Composite body = managedForm.getForm().getBody();
    toolkit.adapt(body);
    //FillLayout fillLayout = new FillLayout();
    //fillLayout.type = SWT.HORIZONTAL;
    //body.setLayout(fillLayout);

    //Composite mainPanel = new Composite(body, SWT.NONE);
    //toolkit.adapt(mainPanel);
    //GridLayout gridLayout = GridLayoutHelper.createLayout(1, false);
    //mainPanel.setLayout(gridLayout);
    createPanel(body);
  }

  @Override
  protected Button createButton(final Composite parent, final int id, final String label, final boolean defaultButton) {
    Button button = super.createButton(parent, id, label, defaultButton);
    Listener[] listeners = button.getListeners(SWT.Selection);
    for (Listener listener : listeners) {
      button.removeListener(SWT.Selection, listener);
    }

    button.addSelectionListener(new SelectionAdapter() {

      @Override
      @SuppressWarnings("unused")
      public void widgetSelected(final SelectionEvent e) {
        setReturnCode(id);
        if (id == IDialogConstants.OK_ID) {
          applySettings();
          close();
        } else if (id == IDialogConstants.YES_ID) {
          applySettings();
        } else if (id == IDialogConstants.CANCEL_ID) {
          undoSettings();
          close();
        }
      }
    });
    return button;
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
    for (ModelForm modelForm : _modelForms) {
      modelForm.validate(validation);
    }
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

  /**
   * Create the parameter panel.
   * @param parent the parent
   */
  protected void createPanel(final Composite parent) {
    // Create the working and undo models.
    _model = createModel();
    _modelUndo = createModel();
    _modelForms.clear();

    // Create a parameter form and add the simple controls.
    _managedForm.getForm().setText("Settings");
    int numForms = getNumForms();
    if (numForms < 1) {
      throw new IllegalArgumentException("Invalid # of forms. Must be as least 1.");
    }
    TabFolder tabFolder = null;
    List<TabItem> tabItems = new ArrayList<TabItem>();
    if (numForms == 1) {
      for (int i = 0; i < numForms; i++) {
        _modelForms.add(new ModelForm(parent, _managedForm, 1));//, true));
      }
    } else {
      tabFolder = new TabFolder(parent, SWT.TOP);
      GridLayout layout = new GridLayout();
      parent.setLayout(layout);
      GridData gridData = new GridData();
      gridData.grabExcessHorizontalSpace = true;
      gridData.grabExcessVerticalSpace = true;
      gridData.horizontalAlignment = SWT.FILL;
      gridData.verticalAlignment = SWT.FILL;
      tabFolder.setLayoutData(gridData);
      for (int i = 0; i < numForms; i++) {
        TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
        tabItems.add(tabItem);
        Composite composite = new Composite(tabFolder, SWT.NONE);
        ModelForm modelForm = new ModelForm(composite, _managedForm, 1);//, true);
        tabItem.setText("Page " + (i + 1));
        tabItem.setControl(modelForm.getComposite());
        _modelForms.add(modelForm);
      }
    }

    // Build the parameter form controls.
    buildModelForms(_modelForms.toArray(new IModelForm[0]));
    if (numForms > 1) {
      for (int i = 0; i < numForms; i++) {
        String title = _modelForms.get(i).getTitle();
        if (title != null && !title.isEmpty()) {
          tabItems.get(i).setText(title);
        }
      }
      int widthHint = 0;
      int heightHint = 0;
      for (IModelForm modelForm : _modelForms) {
        modelForm.getComposite().pack();
        Point size = modelForm.getComposite().getSize();
        widthHint = Math.max(widthHint, size.x);
        heightHint = Math.max(heightHint, size.y);
      }
      GridData gridData = GridLayoutHelper.createLayoutData(true, false, SWT.FILL, SWT.CENTER, 1, 1);
      gridData.heightHint = heightHint;
      tabFolder.setLayoutData(gridData);
      tabFolder.pack();
      _managedForm.getForm().setSize(widthHint, heightHint);
    }

    // Associate the working model with the parameter form.
    for (ModelForm modelForm : _modelForms) {
      modelForm.setTitle("");
      modelForm.setModel(_model);
      modelForm.addListener(this);
    }
    _managedForm.getForm().pack();

    _model.addListener(this);
  }

  /**
   * Returns the number of forms in the model dialog.
   */
  protected abstract int getNumForms();

  @Override
  public boolean close() {
    _model.removeListener(this);
    for (IModelForm form : _modelForms) {
      _model.removeListener(form);
    }
    return super.close();
  }

  /**
   * Creates an instance of the model to be edited.
   */
  protected abstract IModel createModel();

  /**
   * Creates the editing controls in the parameter form.
   * @param form the parameter form.
   */
  protected abstract void buildModelForms(IModelForm[] forms);

  /**
   * Applies the current parameter settings.
   */
  protected abstract void applySettings();

  protected void setFieldEnabled(final String key, final boolean enabled) {
    for (ModelForm modelForm : _modelForms) {
      modelForm.setFieldEnabled(key, enabled);
    }
  }

  /**
   * Hides/Shows a field.
   * 
   * @param property The property of the field to hide/show.
   * @param visible <i>true</i> to show the field; <i>false</i> to hide the field.
   */
  public void setFieldVisible(final String key, final boolean visible) {
    for (ModelForm modelForm : _modelForms) {
      modelForm.setFieldVisible(key, visible, true);
    }
  }

  /**
   * Restores and applies the previous parameter settings.
   */
  protected void undoSettings() {
    // Restore the undo settings and apply them to the renderer.
    _model.updateFrom(_modelUndo);
    for (ModelForm modelForm : _modelForms) {
      modelForm.setTitle("");
      modelForm.setModel(_model);
    }
    applySettings();
  }
}
