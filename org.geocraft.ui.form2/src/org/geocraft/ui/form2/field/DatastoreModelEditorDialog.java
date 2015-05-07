/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.geocraft.core.io.IDatastoreAccessor;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.ui.form2.AbstractModelView;


public class DatastoreModelEditorDialog extends FormDialog {

  private IDatastoreAccessor _accessor;

  private AbstractModelView _view;

  private MapperModel _model;

  public DatastoreModelEditorDialog(final Shell shell, IDatastoreAccessor accessor, final AbstractModelView view, final MapperModel model) {
    super(shell);
    _accessor = accessor;
    _view = view;
    _model = model;
    setShellStyle(SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
  }

  @Override
  public void createButtonsForButtonBar(final Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
  }

  @Override
  protected void createFormContent(final IManagedForm managedForm) {
    String title = _accessor.getName();
    getShell().setText(title);
    managedForm.getForm().setAlwaysShowScrollBars(false);
    managedForm.getForm().setText(title);
    managedForm.getToolkit().decorateFormHeading(managedForm.getForm().getForm());

    FormToolkit toolkit = managedForm.getToolkit();

    Composite body = managedForm.getForm().getBody();
    toolkit.adapt(body);
    _view.buildView(body, managedForm);
    _view.setModel(_model);
    _view.collapseSections();
    _view.expandSections();
  }

  @Override
  public boolean close() {
    _view.setModel(null);
    return super.close();
  }

  //  @Override
  //  protected Button createButton(final Composite parent, final int id, final String label, final boolean defaultButton) {
  //    Button button = super.createButton(parent, id, label, defaultButton);
  //    Listener[] listeners = button.getListeners(SWT.Selection);
  //    for (Listener listener : listeners) {
  //      button.removeListener(SWT.Selection, listener);
  //    }
  //
  //    button.addSelectionListener(new SelectionAdapter() {
  //
  //      @Override
  //      @SuppressWarnings("unused")
  //      public void widgetSelected(final SelectionEvent e) {
  //        if (id == IDialogConstants.OK_ID) {
  //          close();
  //        } else if (id == IDialogConstants.YES_ID) {
  //          applySettings();
  //        } else if (id == IDialogConstants.CANCEL_ID) {
  //          undoSettings();
  //          close();
  //        }
  //      }
  //    });
  //    return button;
  //  }

}
