/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.geocraft.ui.common.TableWrapLayoutHelper;


public class ListSelectionField extends AbstractField {

  private Text _text;

  private Button _button;

  private List<Object> _basicList;

  private boolean _editable = false;

  public ListSelectionField(final Composite parent, IFieldListener listener, final String key, final String label, final boolean showToggle) {
    super(parent, listener, key, label, showToggle);
    _basicList = new ArrayList<Object>();
  }

  @Override
  public Control[] createControls(final Composite parent) {

    Composite container = new Composite(parent, SWT.NONE);
    container
        .setLayoutData(TableWrapLayoutHelper.createLayoutData(true, false, TableWrapData.FILL, TableWrapData.FILL));

    TableWrapLayout layout = new TableWrapLayout();
    layout.numColumns = 2;
    layout.makeColumnsEqualWidth = false;
    layout.topMargin = 0;
    layout.leftMargin = 0;
    layout.rightMargin = 0;
    layout.bottomMargin = 0;
    container.setLayout(layout);

    _text = new Text(container, SWT.BORDER | SWT.READ_ONLY);
    _text.setLayoutData(TableWrapLayoutHelper.createLayoutData(true, false, TableWrapData.FILL, TableWrapData.FILL));

    _text.addKeyListener(new KeyListener() {

      @Override
      public void keyPressed(KeyEvent e) {
        e.doit = _editable;
      }

      @Override
      public void keyReleased(KeyEvent e) {
        e.doit = _editable;
      }

    });

    final Listener selectionListener = new Listener() {

      public void handleEvent(Event event) {
        Object valueObject = _text.getData();
        _listener.fieldChanged(_key, valueObject);
      }
    };
    _text.addListener(SWT.Selection, selectionListener);

    _button = new Button(container, SWT.PUSH);
    _button.setText("List...");
    _button.setLayoutData(TableWrapLayoutHelper.createLayoutData(false, false, TableWrapData.FILL, TableWrapData.FILL));
    _button.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event e) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        if (shell == null) {
          shell = Display.getDefault().getActiveShell();
        }
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(e.display.getActiveShell(),
            new LabelProvider());
        dialog.setMultipleSelection(false);
        dialog.setElements(_basicList.toArray());
        String label = _labelWidget.getText();
        dialog.setTitle(label.substring(0, label.length() - 1) + " Options");
        dialog.open();
        if (dialog.getResult() != null) {
          Object[] items = dialog.getResult();
          Object valueObject = null;
          if (items != null && items.length == 1) {
            valueObject = items[0];
          }
          updateCombo(valueObject);
          selectionListener.handleEvent(null);
        }
      }

    });
    return new Control[] { container };
  }

  @Override
  public void adapt(final FormToolkit toolkit) {
    super.adapt(toolkit);
    //toolkit.adapt(_text);
  }

  @Override
  public void dispose() {
    _basicList.clear();
    super.dispose();
  }

  public void setOptions(final Object[] basicList) {
    _basicList.clear();
    _basicList = new ArrayList<Object>();
    for (Object object : basicList) {
      _basicList.add(object);
    }
  }

  @Override
  public void updateField(Object valueObject) {
    _text.setData(valueObject);
    if (valueObject != null) {
      _text.setText(valueObject.toString());
      setInternalStatus(ValidationStatus.ok());
    } else {
      _text.setText("");
      setInternalStatus(ValidationStatus.ok());
    }
  }

  private void updateCombo(final Object valueObject) {
    Display.getDefault().syncExec(new Runnable() {

      public void run() {
        _text.setText("");
        if (valueObject != null) {
          _text.setText(valueObject.toString());
        }
        _text.setData(valueObject);
      }
    });
  }
}
