/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.TableWrapData;


public class CheckGroupField extends AbstractField {

  private Group _group;

  private final List<Button> _buttons;

  public CheckGroupField(final Composite parent, IFieldListener listener, final String key, final String label, final boolean showToggle) {
    super(parent, listener, key, label, showToggle);
    _buttons = Collections.synchronizedList(new ArrayList<Button>());
  }

  @Override
  public Control[] createControls(final Composite parent) {
    _group = new Group(parent, SWT.SHADOW_ETCHED_IN);
    //    GridData layoutData = new GridData();
    //    layoutData.grabExcessHorizontalSpace = true;
    //    layoutData.grabExcessVerticalSpace = false;
    //    layoutData.horizontalAlignment = TableWrapData.FILL;
    //    layoutData.verticalAlignment = TableWrapData.FILL;
    //    layoutData.verticalSpan = 2;
    TableWrapData layoutData = new TableWrapData();
    layoutData.grabHorizontal = true;
    layoutData.grabVertical = false;
    layoutData.align = TableWrapData.FILL;
    layoutData.valign = TableWrapData.FILL;
    layoutData.rowspan = 2;
    _group.setLayoutData(layoutData);
    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    layout.makeColumnsEqualWidth = false;
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    _group.setLayout(layout);

    Composite blank = new Composite(parent, SWT.NONE);
    //    layoutData = new GridData();
    //    layoutData.grabExcessHorizontalSpace = false;
    //    layoutData.grabExcessVerticalSpace = false;
    //    layoutData.horizontalAlignment = TableWrapData.FILL;
    //    layoutData.verticalAlignment = TableWrapData.FILL;
    //    layoutData.horizontalSpan = 3;
    //    layoutData.verticalSpan = 1;
    layoutData = new TableWrapData();
    layoutData.grabHorizontal = false;
    layoutData.grabVertical = false;
    layoutData.align = TableWrapData.FILL;
    layoutData.valign = TableWrapData.FILL;
    layoutData.colspan = 3;
    layoutData.rowspan = 1;
    blank.setLayoutData(layoutData);

    return new Control[] { _group };
  }

  public Button[] getButtons() {
    return _buttons.toArray(new Button[0]);
  }

  public void addButtons(final Object[] valueObjects) {
    for (Object valueObject : valueObjects) {
      addButton(valueObject);
    }
  }

  private void addButton(final Object valueObject) {
    final Button button = new Button(_group, SWT.CHECK);
    button.setText(valueObject.toString());
    button.setData(valueObject);
    GridData layoutData = new GridData();
    layoutData.grabExcessHorizontalSpace = true;
    layoutData.grabExcessVerticalSpace = false;
    layoutData.horizontalAlignment = TableWrapData.LEFT;
    button.setLayoutData(layoutData);
    button.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        List<Object> valueObjs = new ArrayList<Object>();
        Control[] children = _group.getChildren();
        for (Control control : children) {
          if (control instanceof Button) {
            Button b = (Button) control;
            if (b.getSelection()) {
              valueObjs.add(b.getData());
            }
          }
        }
        _listener.fieldChanged(_key, valueObjs);
      }
    });
    _buttons.add(button);
  }

  @Override
  public void updateField(Object newValue) {
    List<Object> valueObjectList = new ArrayList<Object>();
    if (newValue != null) {
      List valueObjects = (List) newValue;
      for (Object valueObject : valueObjects) {
        valueObjectList.add(valueObject);
      }
    }
    for (Button button : _buttons) {
      Object data = button.getData();
      if (valueObjectList.contains(data)) {
        button.setSelection(true);
      } else {
        button.setSelection(false);
      }
    }
  }
}
