package org.geocraft.ui.form2;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;


/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */

public abstract class PreferencesModelDialog extends ModelDialog {

  /**
   * The constructor.
   * @param shell the parent shell
   * @param title the dialog title
   */
  public PreferencesModelDialog(final Shell shell, final String title) {
    super(shell, title);
  }

  @Override
  public void createButtonsForButtonBar(final Composite parent) {
    createButton(parent, IDialogConstants.NEXT_ID, "Update Preferences", false);
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    createButton(parent, IDialogConstants.YES_ID, "Apply", false);
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
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
        } else if (id == IDialogConstants.NEXT_ID) {
          updatePreferences();
        }
      }
    });
    return button;
  }

  protected abstract void updatePreferences();
}
