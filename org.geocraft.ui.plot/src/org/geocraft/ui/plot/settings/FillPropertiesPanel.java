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
import org.geocraft.ui.plot.attribute.FillProperties;
import org.geocraft.ui.plot.defs.FillStyle;


public class FillPropertiesPanel extends Composite {

  private final FillProperties _properties;

  private ComboViewer _style;

  private ColorSelector _color;

  /**
   * @param parent
   * @param style
   * @param prop
   */
  public FillPropertiesPanel(final Composite parent, final int style, final FillProperties prop) {
    super(parent, style);
    _properties = prop;
    setLayout(new GridLayout(2, false));
    createPanel();
  }

  public void createPanel() {
    Label styleL = new Label(this, SWT.NULL);
    styleL.setText("Fill Style");

    _style = new ComboViewer(this);
    _style.add(FillStyle.values());
    _style.setSelection(new StructuredSelection(_properties.getStyle()));

    Label colorL = new Label(this, SWT.NULL);
    colorL.setText("Fill Color");
    _color = new ColorSelector(this);
    _color.setColorValue(_properties.getRGB());
  }

  public FillProperties getProperties() {
    return new FillProperties((FillStyle) ((StructuredSelection) _style.getSelection()).getFirstElement(), _color
        .getColorValue(), null);
  }

}
