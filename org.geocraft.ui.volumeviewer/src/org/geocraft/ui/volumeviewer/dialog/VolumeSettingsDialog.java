/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.ui.volumeviewer.dialog;


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.ui.viewer.dialog.AbstractSettingsDialog;
import org.geocraft.ui.volumeviewer.VolumeViewer;


/**
 * Volume settings dialog with OK, Apply and Cancel buttons.
 */
public abstract class VolumeSettingsDialog extends AbstractSettingsDialog {

  /** The viewer viewer. */
  protected final VolumeViewer _viewer;

  /**
   * The constructor
   * @param shell the dialog shell
   * @param viewer the viewer viewer
   * @param the dialog title
   */
  public VolumeSettingsDialog(final Shell shell, final VolumeViewer viewer, final String title) {
    super(shell, title);
    _viewer = viewer;
  }

  @Override
  protected Button createButton(final Composite parent, final int id, final String label, final boolean defaultButton) {
    final Button button = super.createButton(parent, id, label, defaultButton);
    final Listener[] listeners = button.getListeners(SWT.Selection);
    for (final Listener listener : listeners) {
      button.removeListener(SWT.Selection, listener);
    }

    button.addSelectionListener(new SelectionAdapter() {

      @Override
      @SuppressWarnings("unused")
      public void widgetSelected(final SelectionEvent e) {
        if (id == IDialogConstants.OK_ID || id == IDialogConstants.YES_ID) {
          applySettings();
          _viewer.makeDirty();
        }
        if (id == IDialogConstants.OK_ID || id == IDialogConstants.CANCEL_ID) {
          close();
        }
      }
    });
    return button;
  }

}
