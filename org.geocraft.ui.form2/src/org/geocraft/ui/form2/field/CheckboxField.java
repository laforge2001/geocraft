/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.geocraft.ui.common.TableWrapLayoutHelper;


public class CheckboxField extends AbstractField {

  private Button _button;

  public CheckboxField(final Composite parent, IFieldListener listener, final String key, final String label, final boolean showToggle) {
    super(parent, listener, key, label, showToggle);
  }

  @Override
  public void setLabel(String label) {
    //super.setLabel(label);
    _button.setText(label);
  }

  @Override
  public String getLabel() {
    return _button.getText();
  }

  @Override
  public boolean getVisible() {
    return _button.getVisible();
  }

  @Override
  public Control[] createControls(final Composite parent) {
    _button = new Button(parent, SWT.CHECK);
    String label = _labelWidget.getText();
    int index = label.lastIndexOf(":");
    if (index >= 0) {
      label = label.substring(0, index);
    }
    _button.setText(label);
    _labelWidget.setText("");
    _labelWidget.setVisible(false);

    _button.setLayoutData(TableWrapLayoutHelper.createLayoutData(true, false, TableWrapData.FILL, TableWrapData.FILL));
    _button.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        Boolean valueObject = new Boolean(_button.getSelection());
        _listener.fieldChanged(_key, valueObject);
        return;
      }
    });
    return new Control[] { _button };
  }

  @Override
  public void setTooltip(final String tooltip) {
    super.setTooltip(tooltip);
    _button.setToolTipText(tooltip);
  }

  @Override
  public void adapt(final FormToolkit toolkit) {
    super.adapt(toolkit);
    _button.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
    _button.setBackground(toolkit.getColors().getBackground());
  }

  @Override
  public void updateField(Object valueObject) {
    if (valueObject != null) {
      try {
        boolean value = Boolean.parseBoolean(valueObject.toString());
        _button.setSelection(value);
      } catch (Exception e) {
        setInternalStatus(ValidationStatus.error("Not a boolean: " + valueObject));
      }
    } else {
      setInternalStatus(ValidationStatus.error("Not a boolean: " + valueObject));
    }
  }
}
