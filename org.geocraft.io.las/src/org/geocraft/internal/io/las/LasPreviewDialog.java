/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.io.las;


import java.io.File;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.geocraft.core.common.io.TextFile;
import org.geocraft.io.las.WellMapperModel;


public class LasPreviewDialog extends FormDialog {

  private Text _fileContents;

  /**
   * @param shell
   */
  public LasPreviewDialog(Shell shell) {
    super(shell);
    setShellStyle(SWT.SHELL_TRIM);
  }

  /**
   * @param model
   */
  public void setFile(final WellMapperModel model) {
    Display.getCurrent().asyncExec(new Runnable() {

      @Override
      public void run() {
        TextFile tf = new TextFile(model.getDirectory() + File.separatorChar + model.getFileName());
        String[] contents = tf.getRecords();
        for (String s : contents) {
          _fileContents.append(s);
          _fileContents.append(System.getProperty("line.separator"));
        }
        _fileContents.setSelection(1);
      }
    });

  }

  @Override
  public void createFormContent(IManagedForm mform) {
    mform.getForm().setText("LAS Preview");
    Composite parent = mform.getForm().getBody();
    parent.setLayout(new FillLayout());

    _fileContents = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
    FontData defaultFont = new FontData("Courier", 10, SWT.BOLD);
    _fileContents.setFont(new Font(parent.getDisplay(), defaultFont));
  }

  public ScrollBar getScrollBar() {
    return _fileContents.getVerticalBar();
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    // create OK button
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
  }

}
