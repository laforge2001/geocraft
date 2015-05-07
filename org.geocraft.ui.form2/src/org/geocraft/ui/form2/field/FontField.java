/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.geocraft.ui.common.TableWrapLayoutHelper;


public class FontField extends AbstractField {

  private Button _button;

  private FontData[] _fontData;

  public FontField(final Composite parent, IFieldListener listener, final String key, final String label, final boolean showToggle) {
    super(parent, listener, key, label, showToggle);
  }

  @Override
  public Control[] createControls(final Composite parent) {
    _fontData = getDefaultFontData();

    _button = new Button(parent, SWT.PUSH);
    _button.setText("Font  " + StringConverter.asString(_fontData));
    _button.setLayoutData(TableWrapLayoutHelper.createLayoutData(true, false, TableWrapData.FILL, TableWrapData.FILL));
    _button.addListener(SWT.Selection, new Listener() {

      @Override
      public void handleEvent(final Event event) {
        FontDialog dialog = new FontDialog(parent.getShell());
        dialog.setFontList(_fontData);
        dialog.open();
        Font font = new Font(null, dialog.getFontList());
        _fontData = font.getFontData();
        _button.setText("Font  " + StringConverter.asString(_fontData));
        _listener.fieldChanged(_key, _fontData);
      }

    });
    return new Control[] { _button };
  }

  @Override
  public void updateField(Object valueObject) {
    if (valueObject != null) {
      if (valueObject instanceof Font) {
        _fontData = ((Font) valueObject).getFontData();
        _button.setText("Font  " + StringConverter.asString(_fontData));
      }
    } else {
      _fontData = getDefaultFontData();
      _button.setText("Choose...");
    }
  }

  @Override
  public void adapt(final FormToolkit toolkit) {
    super.adapt(toolkit);
    toolkit.adapt(_button, true, true);
  }

  private FontData[] getDefaultFontData() {
    Font font = new Font(Display.getDefault(), "Serif", 10, SWT.NORMAL);
    FontData[] data = font.getFontData();
    font.dispose();
    return data;
  }

}
