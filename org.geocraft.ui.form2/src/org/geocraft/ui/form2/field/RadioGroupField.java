/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.geocraft.ui.common.GridLayoutHelper;
import org.geocraft.ui.common.TableWrapLayoutHelper;


public class RadioGroupField extends AbstractField {

  private Group _group;

  private final List<Button> _buttons;

  public RadioGroupField(final Composite parent, IFieldListener listener, final String key, final String label, final boolean showToggle) {
    super(parent, listener, key, label, showToggle);
    _buttons = Collections.synchronizedList(new ArrayList<Button>());
  }

  @Override
  public Control[] createControls(final Composite parent) {
    _group = new Group(parent, SWT.SHADOW_ETCHED_IN);
    _group.setLayoutData(TableWrapLayoutHelper.createLayoutData(true, false, TableWrapData.FILL, TableWrapData.FILL));
    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    layout.makeColumnsEqualWidth = false;
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    _group.setLayout(layout);

    _labelWidget.setLayoutData(TableWrapLayoutHelper.createLayoutData(false, false, TableWrapData.RIGHT,
        TableWrapData.TOP));
    _toggleWidget.setLayoutData(TableWrapLayoutHelper.createLayoutData(false, false, TableWrapData.CENTER,
        TableWrapData.TOP));
    _statusWidget.setLayoutData(TableWrapLayoutHelper.createLayoutData(false, false, TableWrapData.RIGHT,
        TableWrapData.TOP));

    //    Composite blank = new Composite(parent, SWT.NONE);
    //    layoutData = new GridData();
    //    layoutData.grabExcessHorizontalSpace = false;
    //    layoutData.grabExcessVerticalSpace = false;
    //    layoutData.horizontalAlignment = TableWrapData.FILL;
    //    layoutData.verticalAlignment = SWT.TOP;
    //    layoutData.horizontalSpan = 3;
    //    layoutData.verticalSpan = 1;
    //    blank.setLayoutData(layoutData);

    //parent.pack();

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
    final Button button = new Button(_group, SWT.RADIO);
    button.setText(valueObject.toString());
    button.setData(valueObject);
    GridData layoutData = GridLayoutHelper.createLayoutData(true, false, SWT.LEFT, SWT.CENTER);
    button.setLayoutData(layoutData);
    button.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        _listener.fieldChanged(_key, button.getData());
        //        IStatus status = getParameter().validate(valueObj);
        //        setStatus(status);
        //        if (status.getSeverity() == IStatus.ERROR) {
        //          event.doit = false;
        //        } else {
        //          event.doit = true;
        //        }
        //        if (event.doit) {
        //          getParameter().setValueObject(valueObj);
        //        }
      }
    });
    _buttons.add(button);
  }

  @Override
  public void adapt(final FormToolkit toolkit) {
    super.adapt(toolkit);
    for (Button button : _buttons) {
      toolkit.adapt(button, true, true);
    }
  }

  @Override
  public void updateField(Object valueObject) {
    boolean foundOne = false;
    for (Button button : _buttons) {
      Object data = button.getData();
      if (valueObject != null && valueObject.equals(data)) {
        button.setSelection(true);
        foundOne = true;
      } else {
        button.setSelection(false);
      }
    }
    if (foundOne) {
      setInternalStatus(ValidationStatus.ok());
    } else {
      setInternalStatus(ValidationStatus.error("Invalid selected: " + valueObject));
    }
  }

  /**
   * Disables the radio group field.
   * This method is an override since it needs to disable the
   * individual buttons, and not just the group.
   */
  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    for (Button button : _buttons) {
      button.setEnabled(enabled);
    }
  }

  /**
   * Enable/disable a specific radio button in the group
   * @param enabled false => disable, true => enable
   * @param valueObject Button to enable/disable
   */
  public void enable(boolean enabled, Object valueObject) {
    for (Button button : _buttons) {
      Object data = button.getData();
      if (valueObject != null && valueObject.equals(data)) {
        button.setEnabled(enabled);
        break;
      }
    }
  }
}
