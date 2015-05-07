/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.color;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.geocraft.core.color.ColorBar;
import org.geocraft.core.common.util.UserAssistMessageBuilder;


public class ColorBarEditorDialog extends FormDialog {

  private ColorBarEditor _editor;

  private final ColorBar _colorBar;

  private final List<ColorBarEditorListener> _listeners = new ArrayList<ColorBarEditorListener>();

  public ColorBarEditorDialog(final Shell shell, final ColorBar colorBar) {
    super(shell);
    _colorBar = colorBar;
  }

  @Override
  public void createFormContent(final IManagedForm form) {
    _editor = new ColorBarEditor(form.getForm().getBody(), _colorBar);
  }

  public ColorBarEditor getEditor() {
    return _editor;
  }

  @Override
  protected void createButtonsForButtonBar(final Composite parent) {
    // Create OK, Apply and Cancel buttons by default.
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    createButton(parent, IDialogConstants.CLIENT_ID, "Apply", false);
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
  }

  @Override
  protected void buttonPressed(final int buttonId) {
    super.buttonPressed(buttonId);
    if (IDialogConstants.CLIENT_ID == buttonId) {
      applyPressed();
    }
  }

  /**
    * Set the layout data of the button to a GridData with appropriate heights
    * and widths.
    * 
    * @param button
    */
  @Override
  protected void setButtonLayoutData(final Button button) {
    GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
    Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
    data.widthHint = minSize.x;
    button.setLayoutData(data);
  }

  public void applyPressed() {
    UserAssistMessageBuilder message = new UserAssistMessageBuilder();
    message.setDescription("Cannot set the start/end range for the color bar.");
    double start = _colorBar.getStartValue();
    double end = _colorBar.getEndValue();
    boolean showError = false;
    try {
      start = _editor.getStartValue();
    } catch (NumberFormatException ex) {
      message.addReason("The start value is invalid: " + ex.getMessage());
      showError = true;
    }
    try {
      end = _editor.getEndValue();
    } catch (NumberFormatException ex) {
      message.addReason("The end value is invalid: " + ex.getMessage());
      showError = true;
    }
    if (showError) {
      message.addSolution("Enter numeric values for start and/or end.");
      MessageDialog.openError(_editor.getShell(), "Color Bar Error", message.toString());
    } else {
      _colorBar.setStartValue(start);
      _colorBar.setEndValue(end);
    }
    for (ColorBarEditorListener listener : _listeners) {
      listener.colorBarChanged(_colorBar);
    }
  }

  public void addColorBarEditorListener(final ColorBarEditorListener listener) {
    _listeners.add(listener);
  }

  public void removeColorBarEditorListener(final ColorBarEditorListener listener) {
    _listeners.remove(listener);
  }

  @Override
  public void okPressed() {
    applyPressed();
    super.okPressed();
  }

  public static ColorBarEditorDialog createEditor(final ColorBar colorBar) {
    Shell shell = new Shell(SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
    ColorBarEditorDialog dialog = new ColorBarEditorDialog(shell, colorBar);
    dialog.create();

    // Set the dialog title.
    dialog.getShell().pack();
    dialog.getShell().setText("Color Bar Editor");
    Point size = dialog.getShell().computeSize(SWT.DEFAULT, 500);
    dialog.getShell().setSize(size);

    return dialog;
  }
}
