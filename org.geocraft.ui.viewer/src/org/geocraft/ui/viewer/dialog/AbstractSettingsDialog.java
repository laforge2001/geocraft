/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.ui.viewer.dialog;


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;


/**
 * Abstract settings dialog with OK, Apply and Cancel buttons.
 */
public abstract class AbstractSettingsDialog extends FormDialog {

  protected IManagedForm _managedForm;

  protected String _title;

  /**
   * The constructor.
   * @param shell the parent shell
   * @param title the dialog title
   */
  public AbstractSettingsDialog(final Shell shell, final String title) {
    super(shell);
    _title = title;
    //shell.setText(title);
    //super(shell, title, null, "", MessageDialog.INFORMATION, new String[] { "OK", "Apply", "Close" }, 0);
    setShellStyle(SWT.DIALOG_TRIM | SWT.MODELESS);
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
    managedForm.getForm().setText(_title);
    managedForm.getToolkit().decorateFormHeading(managedForm.getForm().getForm());

    FormToolkit toolkit = managedForm.getToolkit();

    Composite body = managedForm.getForm().getBody();
    toolkit.adapt(body);
    FillLayout fillLayout = new FillLayout();
    fillLayout.type = SWT.HORIZONTAL | SWT.VERTICAL;
    body.setLayout(fillLayout);

    Composite mainPanel = new Composite(body, SWT.NONE);
    toolkit.adapt(mainPanel);
    mainPanel.setLayout(new FormLayout());
    createPanel(mainPanel);
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

  /**
   * Create the settings panel.
   * @param parent the parent
   */
  protected abstract void createPanel(Composite parent);

  /**
   * Apply the current settings.
   */
  protected abstract void applySettings();

  /**
   * Undo the current settings.
   */
  protected abstract void undoSettings();
}
