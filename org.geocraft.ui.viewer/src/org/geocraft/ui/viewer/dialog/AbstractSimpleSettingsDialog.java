/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.ui.viewer.dialog;


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.geocraft.ui.common.GridLayoutHelper;


/**
 * Abstract settings dialog with OK, Apply and Cancel buttons.
 */
public abstract class AbstractSimpleSettingsDialog extends FormDialog {

  private final String _title;

  /**
   * The constructor
   * @param shell the parent shell
   * @param the dialog title
   */
  public AbstractSimpleSettingsDialog(final Shell shell, final String title) {
    super(shell);
    setShellStyle(SWT.DIALOG_TRIM | SWT.MODELESS);
    _title = title;
  }

  @Override
  protected void createFormContent(final IManagedForm form) {
    getShell().setText(_title);
    Composite parent = form.getForm().getBody();
    GridLayout layout = GridLayoutHelper.createLayout(1, false, 0, 0, 0, 0, 0, 0);
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    parent.setLayout(layout);
    Composite mainPanel = new Composite(parent, SWT.NONE);
    mainPanel.setLayout(GridLayoutHelper.createLayout(1, false));
    mainPanel.setLayoutData(GridLayoutHelper.createLayoutData(true, true, SWT.FILL, SWT.FILL, 1, 1));
    createPanel(mainPanel);
  }

  @Override
  protected void createButtonsForButtonBar(final Composite parent) {
    // create OK button by default
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
  }

  @Override
  @SuppressWarnings("unused")
  protected Button createButton(final Composite parent, final int id, final String label, final boolean defaultButton) {
    Button button = super.createButton(parent, id, label, false);
    Listener[] listeners = button.getListeners(SWT.Selection);
    for (Listener listener : listeners) {
      button.removeListener(SWT.Selection, listener);
    }

    button.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent e) {
        close();
      }
    });
    return button;
  }

  /**
   * Create the settings panel.
   * @param parent the parent
   */
  protected abstract void createPanel(Composite parent);

}
