/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.geocraft.ui.common.TableWrapLayoutHelper;


public class ScaleField extends AbstractField {

  private Scale _scale;

  public ScaleField(final Composite parent, IFieldListener listener, final String key, final String label, final boolean showToggle) {
    super(parent, listener, key, label, showToggle);
  }

  @Override
  public Control[] createControls(final Composite parent) {
    _scale = new Scale(parent, SWT.HORIZONTAL);
    _scale.setLayoutData(TableWrapLayoutHelper.createLayoutData(true, false, TableWrapData.FILL, TableWrapData.FILL));
    _scale.addListener(SWT.Selection, new Listener() {

      @Override
      public void handleEvent(final Event event) {
        String valueObject = "" + _scale.getSelection();
        _listener.fieldChanged(_key, valueObject);
      }

    });
    return new Control[] { _scale };
  }

  @Override
  public void updateField(Object valueObject) {
    if (valueObject != null) {
      int min = _scale.getMinimum();
      int max = _scale.getMaximum();
      try {
        int value = Integer.parseInt(valueObject.toString());
        if (value >= min && value <= max) {
          _scale.setSelection(value);
        } else {
          // TODO:
        }
      } catch (NumberFormatException e) {
        // TODO:
      }
    }
  }

  public void setRange(final int minimum, final int maximum) {
    _scale.setMinimum(minimum);
    _scale.setMaximum(maximum);
  }

  public void setIncrement(final int increment) {
    _scale.setPageIncrement(increment);
  }

  public void setPageIncrement(final int pageIncrement) {
    _scale.setPageIncrement(pageIncrement);
  }
}
