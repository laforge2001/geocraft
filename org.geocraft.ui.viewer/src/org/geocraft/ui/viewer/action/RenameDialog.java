/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.action;


import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class RenameDialog extends MessageDialog implements KeyListener {

  private String _name = "";

  public RenameDialog(final Shell shell, final String title, final String msg, final String name) {
    super(shell, title, null, msg, QUESTION, new String[] { "Ok", "Cancel" }, 0);
    _name = name;
  }

  @Override
  protected Control createCustomArea(final Composite parent) {
    GridLayout layout = new GridLayout();
    layout.makeColumnsEqualWidth = false;
    layout.numColumns = 2;
    parent.setLayout(layout);

    Label label = new Label(parent, SWT.NONE);
    label.setText("Name: ");
    GridData gridData = new GridData();
    gridData.grabExcessHorizontalSpace = false;
    gridData.horizontalAlignment = SWT.LEFT;
    gridData.verticalAlignment = SWT.FILL;
    label.setLayoutData(gridData);

    Text text = new Text(parent, SWT.BORDER);
    gridData = new GridData();
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalAlignment = SWT.FILL;
    gridData.verticalAlignment = SWT.FILL;
    text.setText(_name);
    text.selectAll();
    text.setLayoutData(gridData);
    text.addKeyListener(this);

    return text;
  }

  public String getName() {
    return _name;
  }

  public void keyPressed(final KeyEvent event) {
    keyReleased(event);
  }

  public void keyReleased(final KeyEvent event) {
    Text text = (Text) event.widget;
    _name = text.getText();
  }
}
