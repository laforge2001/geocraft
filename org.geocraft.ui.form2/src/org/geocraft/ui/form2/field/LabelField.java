/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.geocraft.ui.common.TableWrapLayoutHelper;


public class LabelField extends AbstractField {

  /** The label field control. */
  private Label _label;

  /**
   * Constructs a parameter label field.
   * 
   * @param parent
   *            the parent composite.
   * @param parameter
   *            the parameter key.
   * @param label
   *            the parameter label.
   * @param showToggle
   *            <i>true</i> to show a parameter toggle button; otherwise
   *            <i>false</i>.
   */
  public LabelField(final Composite parent, IFieldListener listener, final String key, final String label, final boolean showToggle) {
    super(parent, listener, key, label, showToggle);
  }

  @Override
  public Control[] createControls(final Composite parent) {
    _label = new Label(parent, SWT.NONE);
    _label.setText("");
    _label.setLayoutData(TableWrapLayoutHelper.createLayoutData(true, false, TableWrapData.FILL, TableWrapData.FILL));
    return new Control[] { _label };
  }

  @Override
  public void updateField(Object valueObject) {
    if (valueObject != null) {
      _label.setText(valueObject.toString());
    } else {
      _label.setText("");
    }
    setInternalStatus(ValidationStatus.ok());
  }

}
