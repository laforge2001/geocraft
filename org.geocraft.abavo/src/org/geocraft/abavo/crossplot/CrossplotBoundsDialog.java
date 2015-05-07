/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot;


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;


public class CrossplotBoundsDialog extends FormDialog {

  protected final ABavoCrossplot _crossplot;

  /** The crossplot bounds editor. */
  protected final CrossplotBoundsView _editor;

  /** The crossplot bounds model. */
  protected final CrossplotBoundsModel _model;

  /**
   * The private constructor. This dialog must be
   * created using the static convenience method.
   * @param shell the parent shell.
   * @param model the polygon regions model to edit.
   */
  public CrossplotBoundsDialog(final Shell shell, final CrossplotBoundsModel model, final ABavoCrossplot crossplot) {
    super(shell);
    _crossplot = crossplot;
    _model = model;
    _editor = new CrossplotBoundsView();
  }

  @Override
  public void createFormContent(final IManagedForm mform) {
    Composite composite = mform.getForm().getBody();
    composite.setLayout(new FillLayout());

    // Create the editor composite.
    _editor.buildView(mform.getForm().getBody(), mform);//, true);
    _editor.setModel(_model);
  }

  @Override
  public boolean close() {
    _model.removeListener(_editor.getModelForm());
    return super.close();
  }

  @Override
  public void createButtonsForButtonBar(final Composite parent) {
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
        if (id == IDialogConstants.OK_ID) {
          _editor.applyChanges(_crossplot);
          close();
        } else if (id == IDialogConstants.YES_ID) {
          _editor.applyChanges(_crossplot);
        } else if (id == IDialogConstants.CANCEL_ID) {
          _editor.undoChanges(_crossplot);
          close();
        }
      }
    });
    return button;
  }

  /**
   * Returns the editor composite.
   * @return the editor composite.
   */
  public CrossplotBoundsView getEditor() {
    return _editor;
  }

  public static CrossplotBoundsDialog createEditorDialog(final ABavoCrossplot crossplot,
      final CrossplotBoundsModel model) {

    // Create a parent shell.
    Shell shell = new Shell(Display.getCurrent());

    // Create the dialog.
    CrossplotBoundsDialog dialog = new CrossplotBoundsDialog(shell, model, crossplot);
    dialog.setShellStyle(SWT.TITLE | SWT.MODELESS);
    dialog.setBlockOnOpen(true);
    dialog.create();

    // Set the dialog title.
    dialog.getShell().pack();
    dialog.getShell().setText("Crossplot Bounds Model");
    Point size = dialog.getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
    dialog.getShell().setSize(size);

    return dialog;
  }
}
