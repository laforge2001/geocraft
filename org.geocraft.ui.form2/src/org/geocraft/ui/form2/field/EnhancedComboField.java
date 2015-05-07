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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;


public class EnhancedComboField extends AbstractField {

  private Combo _combo;

  private Button _button;

  private List<Object> _basicList;

  private List<Object> _enhancedList;

  private boolean _editable = false;

  public EnhancedComboField(final Composite parent, IFieldListener listener, final String key, final String label, final boolean showToggle) {
    super(parent, listener, key, label, showToggle, true);
    _basicList = new ArrayList<Object>();
    _enhancedList = new ArrayList<Object>();
  }

  @Override
  public Control[] createControls(final Composite parent) {

    Composite container = new Composite(parent, SWT.NONE);
    //setControl(_container);
    //    GridData layoutData = new GridData();
    //    layoutData.grabExcessHorizontalSpace = true;
    //    layoutData.grabExcessVerticalSpace = false;
    //    layoutData.horizontalAlignment = TableWrapData.FILL;
    //    layoutData.verticalAlignment = TableWrapData.FILL;
    TableWrapData layoutData = new TableWrapData();
    layoutData.grabHorizontal = true;
    layoutData.grabVertical = false;
    layoutData.align = TableWrapData.FILL;
    layoutData.valign = TableWrapData.FILL;
    container.setLayoutData(layoutData);
    //_container.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
    //    GridLayout layout = new GridLayout();
    //    layout.numColumns = 2;
    //    layout.makeColumnsEqualWidth = false;
    //    layout.marginTop = 0;
    //    layout.marginLeft = 0;
    //    layout.marginRight = 0;
    //    layout.marginBottom = 0;
    //    layout.marginWidth = 0;
    //    layout.marginHeight = 0;
    TableWrapLayout layout = new TableWrapLayout();
    layout.numColumns = 2;
    layout.makeColumnsEqualWidth = false;
    layout.topMargin = 0;
    layout.leftMargin = 0;
    layout.rightMargin = 0;
    layout.bottomMargin = 0;
    container.setLayout(layout);

    if (_readOnly) {
      _combo = new Combo(container, SWT.BORDER | SWT.FLAT | SWT.DROP_DOWN | SWT.READ_ONLY);
    } else {
      _combo = new Combo(container, SWT.BORDER | SWT.FLAT | SWT.DROP_DOWN);
    }
    //    layoutData = new GridData();
    //    layoutData.grabExcessHorizontalSpace = true;
    //    layoutData.grabExcessVerticalSpace = false;
    //    layoutData.horizontalAlignment = TableWrapData.FILL;
    //    layoutData.verticalAlignment = TableWrapData.FILL;
    layoutData = new TableWrapData();
    layoutData.grabHorizontal = true;
    layoutData.grabVertical = false;
    layoutData.align = TableWrapData.FILL;
    layoutData.valign = TableWrapData.FILL;
    _combo.setLayoutData(layoutData);

    _combo.addKeyListener(new KeyListener() {

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
        Object[] data = (Object[]) _combo.getData();
        int index = _combo.getSelectionIndex();
        Object valueObject = null;
        if (index >= 0 && index < data.length) {
          valueObject = data[index];
        }
        _listener.fieldChanged(_key, valueObject);
      }
    };
    _combo.addListener(SWT.Selection, selectionListener);

    _button = new Button(container, SWT.PUSH);
    _button.setText("List...");

    layoutData = new TableWrapData();
    layoutData.grabHorizontal = false;
    layoutData.grabVertical = false;
    layoutData.align = TableWrapData.FILL;
    layoutData.valign = TableWrapData.FILL;
    _button.setLayoutData(layoutData);
    _button.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event e) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        if (shell == null) {
          shell = Display.getDefault().getActiveShell();
        }
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(e.display.getActiveShell(),
            new LabelProvider());
        dialog.setMultipleSelection(false);
        dialog.setElements(_enhancedList.toArray());
        dialog.setTitle("Extended Options");
        dialog.open();
        int selectionIndex = _basicList.size();
        if (dialog.getResult() != null) {
          Object[] items = dialog.getResult();

          for (Object item : items) {
            _enhancedList.remove(item);
            _basicList.add(item);
          }
          updateCombo(selectionIndex);
          selectionListener.handleEvent(null);
        }
      }

    });
    return new Control[] { container };
  }

  @Override
  public void adapt(final FormToolkit toolkit) {
    super.adapt(toolkit);
    toolkit.adapt(_combo);
  }

  @Override
  public void dispose() {
    _basicList.clear();
    _enhancedList.clear();
    super.dispose();
  }

  public void setOptions(final Object[] basicList, final Object[] enhancedList) {
    _basicList.clear();
    _basicList = new ArrayList<Object>();
    for (Object object : basicList) {
      _basicList.add(object);
    }
    _enhancedList.clear();
    _enhancedList = new ArrayList<Object>();
    for (Object object : enhancedList) {
      _enhancedList.add(object);
    }
    updateCombo(_combo.getSelectionIndex());
  }

  @Override
  public void updateField(Object valueObject) {
    if (valueObject != null) {
      _combo.deselectAll();
      String[] items = _combo.getItems();
      for (int i = 0; i < items.length; i++) {
        if (items[i].equals(valueObject.toString())) {
          _combo.select(i);
          setInternalStatus(ValidationStatus.ok());
          return;
        }
      }
      setInternalStatus(ValidationStatus.error("Invalid selection: " + valueObject));
    } else {
      _combo.deselect(_combo.getSelectionIndex());
      setInternalStatus(ValidationStatus.ok());
    }
  }

  private void updateCombo(final int selectionIndex) {
    Display.getDefault().syncExec(new Runnable() {

      public void run() {
        Object currentSelection = null;
        if (selectionIndex >= 0 && selectionIndex < _combo.getItemCount()) {
          Object[] data = (Object[]) _combo.getData();
          currentSelection = data[selectionIndex];
        }
        String[] items = new String[_basicList.size()];
        for (int i = 0; i < items.length; i++) {
          items[i] = _basicList.get(i).toString();
        }
        _combo.setItems(items);
        _combo.setData(_basicList.toArray());
        if (_basicList.size() > 0) {
          int index = -1;
          if (currentSelection != null) {
            for (int i = 0; i < _basicList.size(); i++) {
              if (_basicList.get(i).equals(currentSelection)) {
                index = i;
                break;
              }
            }
          }
          if (index >= 0) {
            _combo.select(index);
          } else {
            if (selectionIndex >= 0 && selectionIndex < _combo.getItemCount()) {
              _combo.select(selectionIndex);
            }
          }
        }
      }
    });
  }
}
