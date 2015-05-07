/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.common;


import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;


public class MultipleSelectionListDialog extends FormDialog {

  private ISelectionListComposite _listComposite;

  private final String _title;

  public MultipleSelectionListDialog(final Shell shell, final String title) {
    super(shell);
    _title = title;
  }

  @Override
  protected void createFormContent(final IManagedForm mform) {
    mform.getForm().setText(_title);
    mform.getToolkit().decorateFormHeading(mform.getForm().getForm());

    Composite composite = mform.getForm().getBody();
    composite.setLayout(new FillLayout());
    _listComposite = new MultipleSelectionListComposite(composite);
  }

  public ISelectionListComposite getListComposite() {
    return _listComposite;
  }
}
