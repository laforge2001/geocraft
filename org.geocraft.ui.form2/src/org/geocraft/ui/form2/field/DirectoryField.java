/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.geocraft.ui.common.TableWrapLayoutHelper;


public class DirectoryField extends AbstractField {

  /** The text field control. */
  private Text _text;

  private Button _button;

  private String _directory = null;

  public DirectoryField(final Composite parent, IFieldListener listener, final String key, final String label, final String directory, final boolean showToggle) {
    super(parent, listener, key, label, showToggle);
    _directory = directory;
  }

  @Override
  public Control[] createControls(final Composite parent) {

    Composite container = new Composite(parent, SWT.NONE);
    container
        .setLayoutData(TableWrapLayoutHelper.createLayoutData(true, false, TableWrapData.FILL, TableWrapData.FILL));
    container.setLayout(TableWrapLayoutHelper.createLayout(2, false));

    int style = SWT.BORDER;
    _text = new Text(container, style);
    _text.setText("");
    _text.setEditable(false);
    _text.setLayoutData(TableWrapLayoutHelper.createLayoutData(true, false, TableWrapData.FILL, TableWrapData.FILL));

    _button = new Button(container, SWT.PUSH);
    _button.setText("List...");
    _button.setLayoutData(TableWrapLayoutHelper.createLayoutData(false, false, TableWrapData.FILL, TableWrapData.FILL));
    _button.addSelectionListener(new SelectionListener() {

      public void widgetDefaultSelected(final SelectionEvent e) {
        widgetSelected(e);
      }

      public void widgetSelected(final SelectionEvent e) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        if (shell == null) {
          shell = Display.getDefault().getActiveShell();
        }
        DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
        dialog.setText("File Selection...");
        dialog.setFilterPath(_directory);
        String selected = dialog.open();
        if (selected != null) {
          _text.setText(selected);
          //getParameter().setValueObject(new File(selected));
          _listener.fieldChanged(_key, selected);
        } else {
          _text.setText("");
          //getParameter().setValueObject(null);
          _listener.fieldChanged(_key, null);
        }
      }

    });
    return new Control[] { container };
  }

  @Override
  public void updateField(Object valueObject) {
    if (valueObject != null) {
      _text.setText(valueObject.toString());
    } else {
      _text.setText("");
    }
    setInternalStatus(ValidationStatus.ok());
  }

  @Override
  public void setEnabled(boolean flag) {
    super.setEnabled(flag);
    _button.setEnabled(flag);
    _text.setEnabled(flag);
  }

  @Override
  public boolean getVisible() {
    return _text.isVisible();
  }

}
