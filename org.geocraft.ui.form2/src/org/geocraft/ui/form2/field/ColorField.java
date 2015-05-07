/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.geocraft.ui.common.TableWrapLayoutHelper;


public class ColorField extends AbstractField {

  /** The color field control. */
  private ColorSelector _selector;

  /**
   * Constructs a parameter color field.
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
  public ColorField(final Composite parent, IFieldListener listener, final String key, final String label, final boolean showToggle) {
    super(parent, listener, key, label, showToggle);
  }

  @Override
  public Control[] createControls(final Composite parent) {
    _selector = new ColorSelector(parent);
    _selector.setColorValue(new RGB(0, 0, 0));
    _selector.getButton().setLayoutData(
        TableWrapLayoutHelper.createLayoutData(true, false, TableWrapData.FILL, TableWrapData.FILL));
    _selector.addListener(new IPropertyChangeListener() {

      @Override
      public void propertyChange(final org.eclipse.jface.util.PropertyChangeEvent event) {
        RGB valueObject = _selector.getColorValue();
        _listener.fieldChanged(_key, valueObject);
      }

    });
    return new Control[] { _selector.getButton() };
  }

  @Override
  public void updateField(Object valueObject) {
    if (valueObject != null && valueObject instanceof RGB) {
      _selector.setColorValue((RGB) valueObject);
      setInternalStatus(ValidationStatus.ok());
    } else {
      setInternalStatus(ValidationStatus.error("Not an RGB: " + valueObject));
    }
  }

}
