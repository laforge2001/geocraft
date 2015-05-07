/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.navigation;


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.geocraft.ui.common.GridLayoutHelper;


public abstract class AbstractNavigationDialog extends FormDialog {

  protected static final int NUMERIC_ENTER = 16777296;

  protected static final int MAX_VERTICAL_SCALE = 1000000;

  protected IManagedForm _managedForm;

  protected String _title;

  /**
   * The constructor.
   * @param shell the parent shell
   * @param title the dialog title
   */
  public AbstractNavigationDialog(final Shell shell, final String title) {
    super(shell);
    _title = title;
    //shell.setText(title);
    //super(shell, title, null, "", MessageDialog.INFORMATION, new String[] { "OK", "Apply", "Close" }, 0);
    setShellStyle(SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
  }

  @Override
  public void createButtonsForButtonBar(final Composite parent) {
    createButton(parent, IDialogConstants.CANCEL_ID, "Close", false);
  }

  @Override
  protected void createFormContent(final IManagedForm managedForm) {
    getShell().setText(_title);
    _managedForm = managedForm;
    managedForm.getForm().setText(_title);
    managedForm.getToolkit().decorateFormHeading(managedForm.getForm().getForm());

    Composite body = managedForm.getForm().getBody();
    FillLayout fillLayout = new FillLayout();
    fillLayout.type = SWT.HORIZONTAL | SWT.VERTICAL;
    body.setLayout(fillLayout);

    Composite mainPanel = new Composite(body, SWT.NONE);
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
        if (id == IDialogConstants.CANCEL_ID) {
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

  protected Group createGroup(final Composite parent, final String title) {
    return createGroup(parent, title, 5);
  }

  protected Group createGroup(final Composite parent, final String title, final int numColumns) {
    Group group = new Group(parent, SWT.NONE);
    group.setText(title);
    group.setLayout(GridLayoutHelper.createLayout(numColumns, false));
    group.setLayoutData(GridLayoutHelper.createLayoutData(true, true, SWT.FILL, SWT.FILL, 2, 1));
    return group;
  }

  protected Text addLabeledText(final Group group, final String text, final float value) {
    Label label = new Label(group, SWT.NONE);
    label.setText(text);
    label.setLayoutData(GridLayoutHelper.createLayoutData(false, false, SWT.FILL, SWT.FILL, 1, 1));
    Text textField = new Text(group, SWT.BORDER_SOLID);
    textField.setText("" + value);
    textField.setLayoutData(GridLayoutHelper.createLayoutData(true, false, SWT.FILL, SWT.FILL, 1, 1));
    return textField;
  }

  protected Slider createSlider(final Group group, final int min, final int max) {
    Slider slider = new Slider(group, SWT.HORIZONTAL);
    int thumb = 1;
    slider.setMinimum(min);
    slider.setMaximum(max + thumb);
    slider.setThumb(thumb);
    slider.setLayoutData(GridLayoutHelper.createLayoutData(true, false, SWT.FILL, SWT.FILL, 2, 1));
    return slider;
  }

  protected Button createToggleButton(final Group group, final boolean selected, final int hSpan) {
    final Button button = new Button(group, SWT.CHECK);
    button.setSelection(selected);
    button.setLayoutData(GridLayoutHelper.createLayoutData(false, false, SWT.FILL, SWT.FILL, hSpan, 1));
    return button;
  }

  protected Text createCurrentText(final Group group, final float value) {
    Label label = new Label(group, SWT.NONE);
    label.setText("Current");
    label.setLayoutData(GridLayoutHelper.createLayoutData(false, false, SWT.LEFT, SWT.FILL, 1, 1));
    Text text = new Text(group, SWT.NONE);
    text.setText("" + value);
    text.setLayoutData(GridLayoutHelper.createLayoutData(true, false, SWT.FILL, SWT.FILL, 1, 1));
    return text;
  }

  protected Spinner createStepSpinner(final Group group, final int selection, final int maximum) {
    Label label = new Label(group, SWT.NONE);
    label.setText("Step");
    label.setLayoutData(GridLayoutHelper.createLayoutData(true, false, SWT.RIGHT, SWT.FILL, 1, 1));
    final Spinner spinner = new Spinner(group, SWT.BORDER);
    spinner.setValues(selection, 1, maximum, 0, 1, 10);
    spinner.setLayoutData(GridLayoutHelper.createLayoutData(false, false, SWT.FILL, SWT.FILL, 1, 1));
    return spinner;
  }

  protected Label createStartLabel(final Group group, final float value) {
    Label label = new Label(group, SWT.NONE);
    label.setText("" + value);
    label.setLayoutData(GridLayoutHelper.createLayoutData(false, false, SWT.RIGHT, SWT.FILL, 2, 1));
    return label;
  }

  protected Label createEndLabel(final Group group, final float value) {
    Label label = new Label(group, SWT.NONE);
    label.setText("" + value);
    label.setLayoutData(GridLayoutHelper.createLayoutData(false, false, SWT.LEFT, SWT.FILL, 1, 1));
    return label;
  }

  protected Label createLabel(final Group group, final String text) {
    Label label = new Label(group, SWT.NONE);
    label.setText(text);
    Font currentFont = label.getFont();
    label.setFont(new Font(group.getDisplay(), currentFont.getFontData()[0].getName(), 10, SWT.ITALIC));
    label.setLayoutData(GridLayoutHelper.createLayoutData(true, false, SWT.FILL, SWT.FILL, 5, 1));
    return label;
  }
}
