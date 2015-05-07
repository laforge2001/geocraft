/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.geocraft.core.common.math.MathUtil;
import org.geocraft.ui.common.TableWrapLayoutHelper;


public class SpinnerField extends AbstractField {

  private Spinner _spinner;

  public SpinnerField(final Composite parent, IFieldListener listener, final String key, final String label, final boolean showToggle) {
    super(parent, listener, key, label, showToggle);
  }

  @Override
  public Control[] createControls(final Composite parent) {
    _spinner = new Spinner(parent, SWT.BORDER);
    _spinner.setForeground(new Color(null, 255, 255, 0));
    _spinner.setLayoutData(TableWrapLayoutHelper.createLayoutData(true, false, TableWrapData.FILL, TableWrapData.FILL));
    _spinner.addListener(SWT.Selection, new Listener() {

      @Override
      public void handleEvent(final Event event) {
        int value = _spinner.getSelection();
        int digits = _spinner.getDigits();
        Object valueObject = null;
        if (digits == 0) {
          valueObject = Integer.toString(value);
        } else {
          float denom = 1;
          for (int i = 0; i < digits; i++) {
            denom *= 10;
          }
          valueObject = Float.toString(value / denom);
        }
        _listener.fieldChanged(_key, valueObject);
      }

    });
    return new Control[] { _spinner };
  }

  public Spinner getSpinner() {
    return _spinner;
  }

  @Override
  public void adapt(final FormToolkit toolkit) {
    super.adapt(toolkit);
    toolkit.adapt(_spinner);
  }

  public void setRange(final int minimum, final int maximum, final int digits, final int increment,
      final int pageIncrement) {
    Display.getDefault().syncExec(new Runnable() {

      public void run() {
        _spinner.setMaximum(maximum);
        _spinner.setMinimum(minimum);
        _spinner.setDigits(digits);
        _spinner.setIncrement(increment);
        _spinner.setPageIncrement(pageIncrement);
      }
    });
  }

  @Override
  public void updateField(Object valueObject) {
    if (valueObject != null) {
      int max = _spinner.getMaximum();
      int min = _spinner.getMinimum();
      int inc = _spinner.getIncrement();
      int digits = _spinner.getDigits();
      int numItems = 1 + (max - min) / inc;
      float denom = 1;
      for (int i = 0; i < digits; i++) {
        denom *= 10;
      }
      for (int i = 0; i < numItems; i++) {
        int tempi = min + i * inc;
        if (digits == 0) {
          if (tempi == Integer.parseInt(valueObject.toString())) {
            _spinner.setSelection(tempi);
            setInternalStatus(ValidationStatus.ok());
            return;
          }
        } else {
          float tempf = tempi / denom;
          if (MathUtil.isEqual(tempf, Float.parseFloat(valueObject.toString()))) {
            _spinner.setSelection(tempi);
            setInternalStatus(ValidationStatus.ok());
            return;
          }
        }
      }
    }
    setInternalStatus(ValidationStatus.error("Invalid spinner value: " + valueObject));
  }
}
