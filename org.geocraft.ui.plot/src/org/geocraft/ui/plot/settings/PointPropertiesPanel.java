/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.settings;


import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.geocraft.ui.plot.attribute.PointProperties;
import org.geocraft.ui.plot.defs.PointStyle;


public class PointPropertiesPanel extends Composite {

  private final PointProperties _properties;

  private ComboViewer _style;

  private Spinner _size;

  private ColorSelector _color;

  private final boolean _showStyleSettings;

  /**
   * @param parent
   * @param style
   * @param prop
   */
  public PointPropertiesPanel(final Composite parent, final int style, final PointProperties prop, final boolean showStyleSettings) {
    super(parent, style);
    _properties = prop;
    setLayout(new GridLayout(2, false));
    _showStyleSettings = showStyleSettings;
    createPanel();
  }

  public void createPanel() {

    if (_showStyleSettings) {
      Label styleL = new Label(this, SWT.NULL);
      styleL.setText("Point Style");
      _style = new ComboViewer(this);
      _style.add(PointStyle.values());
      _style.setSelection(new StructuredSelection(_properties.getStyle()));
    }

    Label sizeL = new Label(this, SWT.NULL);
    sizeL.setText("Point Size");
    _size = new Spinner(this, SWT.BORDER);
    _size.setValues(_properties.getSize(), 1, 100, 0, 1, 10);

    Label colorL = new Label(this, SWT.NULL);
    colorL.setText("Point Color");
    _color = new ColorSelector(this);
    _color.setColorValue(_properties.getColor());
  }

  public PointProperties getProperties() {
    if (_showStyleSettings) {
      return new PointProperties((PointStyle) ((StructuredSelection) _style.getSelection()).getFirstElement(), _color
          .getColorValue(), _size.getSelection());
    }
    return new PointProperties(PointStyle.NONE, _color.getColorValue(), _size.getSelection());

  }

}
