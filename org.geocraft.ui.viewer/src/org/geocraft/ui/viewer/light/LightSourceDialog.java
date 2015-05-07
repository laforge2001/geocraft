/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.light;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.geocraft.ui.common.GridLayoutHelper;


public class LightSourceDialog extends FormDialog {

  private LightSourceModel _model;

  public LightSourceDialog(final Shell shell, final LightSourceModel model) {
    super(shell);
    _model = model;
  }

  @Override
  protected void createFormContent(IManagedForm managedForm) {
    Composite parent = managedForm.getForm().getBody();
    parent.setLayout(GridLayoutHelper.createLayout(1, false));
    LightSourceCanvas canvas = new LightSourceCanvas(parent, _model);
    canvas.setLayoutData(GridLayoutHelper.createLayoutData(true, true, SWT.FILL, SWT.FILL));
  }

}
