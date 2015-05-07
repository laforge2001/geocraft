/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


import java.io.File;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.geocraft.ui.common.TableWrapLayoutHelper;


public class FileField extends AbstractField {

  /** The text field control. */
  private Text _text;

  private Button _button;

  private String _directory = null;

  private String[] _filterNames = null;

  private String[] _filterExtensions = null;

  public FileField(final Composite parent, IFieldListener listener, final String key, final String label, final String directory, final String[][] filters, final boolean showToggle) {
    super(parent, listener, key, label, showToggle);
    _directory = directory;
    if (filters != null) {
      _filterNames = new String[filters.length];
      _filterExtensions = new String[filters.length];
      for (int i = 0; i < filters.length; i++) {
        _filterNames[i] = filters[i][0];
        _filterExtensions[i] = filters[i][1];
      }
    }
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

    // Send field changed notification whenever field edited.
    // Note: Listener only invoked if file field explicitly made editable.
    //       The default is it is not editable.
    // Note: Necessary so associated property is updated
    _text.addListener(SWT.KeyUp, new Listener() {

      public void handleEvent(final Event event) {
        String valueObject = _text.getText();
        _listener.fieldChanged(_key, valueObject);
      }
    });

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
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setText("File Selection...");
        dialog.setFilterPath(_directory);
        dialog.setFilterNames(_filterNames);
        dialog.setFilterExtensions(_filterExtensions);
        String selected = dialog.open();
        if (selected != null && !selected.isEmpty()) {
          File file = new File(selected);
          if (file.exists()) {
            _directory = file.getParent();
          }
        }
        if (selected != null) {
          _text.setText(selected);
          // getParameter().setValueObject(new File(selected));
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

  /**
   * Set the text of the button for browsing for the file. Used
   * when don't want the default "List...";
   */
  public void setText(String text) {
    _button.setText(text);
  }

  @Override
  public boolean getVisible() {
    return _button.isVisible();
  }

  /**
   * Sets the editable state.
   *
   * @param editable The new editable state.
   */
  public void setEditable(boolean editable) {
    _text.setEditable(editable);
  }
}
