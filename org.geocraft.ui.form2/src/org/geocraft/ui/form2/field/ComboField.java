/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.geocraft.ui.common.TableWrapLayoutHelper;


public class ComboField extends AbstractField {

  protected Combo _combo;

  protected boolean _editable;

  public ComboField(final Composite parent, IFieldListener listener, final String key, final String label, final boolean showToggle) {
    super(parent, listener, key, label, showToggle);
  }

  public ComboField(final Composite parent, IFieldListener listener, final String key, final String label, final boolean showToggle, final boolean readOnly) {
    super(parent, listener, key, label, showToggle, readOnly);
  }

  @Override
  public Control[] createControls(final Composite parent) {
    if (_readOnly) {
      _combo = new Combo(parent, SWT.BORDER | SWT.FLAT | SWT.DROP_DOWN | SWT.READ_ONLY);
      _editable = false;
    } else {
      _combo = new Combo(parent, SWT.BORDER | SWT.FLAT | SWT.DROP_DOWN);
      _editable = true;
    }
    _combo.setLayoutData(TableWrapLayoutHelper.createLayoutData(true, false, TableWrapData.FILL, TableWrapData.FILL));
    _combo.addListener(SWT.KeyUp, new Listener() {

      @Override
      public void handleEvent(Event event) {
        event.doit = _editable;
        if (_editable) {
          String valueObject = _combo.getText();
          _listener.fieldChanged(_key, valueObject);
        }
      }

    });
    _combo.addListener(SWT.Selection, new Listener() {

      @Override
      public void handleEvent(final Event event) {
        Object[] data = (Object[]) _combo.getData();
        int index = _combo.getSelectionIndex();
        Object valueObject = null;
        if (index >= 0 && index < data.length) {
          valueObject = data[index];
        }
        _listener.fieldChanged(_key, valueObject);
      }

    });
    return new Control[] { _combo };
  }

  public Combo getCombo() {
    return _combo;
  }

  @Override
  public void adapt(final FormToolkit toolkit) {
    super.adapt(toolkit);
    toolkit.adapt(_combo);
  }

  protected Object[] getOptions() {
    Object data = _combo.getData();
    if (data == null) {
      return new Object[0];
    }
    return (Object[]) data;
  }

  @Override
  public boolean getVisible() {
    return _combo.isVisible();
  }

  /**
   * Sets the list of available options for the combo field.
   * 
   * @param options the array of options.
   */
  public void setOptions(final Object[] options) {
    final String[] items = new String[options.length];
    for (int i = 0; i < options.length; i++) {
      items[i] = options[i].toString();
    }
    Display.getDefault().syncExec(new Runnable() {

      public void run() {

        int index = _combo.getSelectionIndex();
        Object currentSelection = null;
        if (index >= 0) {
          Object[] data = (Object[]) _combo.getData();
          currentSelection = data[index];
        }
        _combo.deselectAll();
        _combo.setItems(items);
        _combo.setData(options);
        if (options.length > 0) {
          index = -1;
          if (currentSelection != null) {
            for (int i = 0; i < options.length; i++) {
              if (options[i].equals(currentSelection)) {
                index = i;
                break;
              }
            }
          }
          if (index >= 0) {
            _combo.select(index);
            _listener.fieldChanged(_key, options[index]);
          } else {
            _listener.fieldChanged(_key, null);
          }
        } else {
          _listener.fieldChanged(_key, null);
        }
      }
    });
  }

  /**
   * Sets the combo field to be editable or not editable.
   * 
   * @param editable <i>true</i> to set editable; otherwise <i>false</i>.
   */
  public void setEditable(final boolean editable) {
    _editable = editable;
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
      // Report a selection error if no match has been found, but only
      // if the field is active.
      if (isActive()) {
        setInternalStatus(ValidationStatus.error("Invalid selection: " + valueObject));
      } else {
        setInternalStatus(ValidationStatus.ok());
      }
    } else {
      _combo.deselect(_combo.getSelectionIndex());
      setInternalStatus(ValidationStatus.ok());
    }
  }

  /**
   * Get the current item selected; there can only be one.
   * NOTE: The caller must cast the selected item to the class of selectable items.
   * @return null if no item is selected; otherwise, the selected item
   */
  public Object getCurrentSelection() {
    int index = _combo.getSelectionIndex();
    Object currentSelection = null;
    if (index >= 0) {
      Object[] data = (Object[]) _combo.getData();
      currentSelection = data[index];
    }
    return currentSelection;
  }
}
