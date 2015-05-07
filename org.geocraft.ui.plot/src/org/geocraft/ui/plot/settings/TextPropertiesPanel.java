/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.settings;


import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.geocraft.ui.plot.attribute.TextProperties;
import org.geocraft.ui.plot.defs.TextAnchor;


public class TextPropertiesPanel extends Composite {

  private final TextProperties _properties;

  private Button _chooseFont;

  private Font _currentFont;

  private ComboViewer _anchor;

  private ColorSelector _color;

  private final boolean _showAnchor;

  /**
   * @param parent
   * @param style
   * @param properties
   */
  public TextPropertiesPanel(final Composite parent, final int style, final TextProperties properties, final boolean anchor) {
    super(parent, style);
    _properties = properties;
    _currentFont = new Font(null, _properties.getFont().getFontData());
    _showAnchor = anchor;
    setLayout(new GridLayout(2, false));
    createPanel();
  }

  public void createPanel() {
    final Label fontLabel = new Label(this, SWT.NULL);
    fontLabel.setText("Font " + StringConverter.asString(_currentFont.getFontData()));

    _chooseFont = new Button(this, SWT.PUSH);
    _chooseFont.setText("Choose");
    _chooseFont.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent e) {
        FontDialog dialog = new FontDialog(getShell());
        dialog.setFontList(_currentFont.getFontData());
        dialog.open();
        Font newFont = new Font(null, dialog.getFontList());
        Font oldFont = _currentFont;
        _currentFont = newFont;
        oldFont.dispose();
        fontLabel.setText("Font  " + StringConverter.asString(_currentFont.getFontData()));
        pack();
      }
    });

    Label colorLabel = new Label(this, SWT.NULL);
    colorLabel.setText("Text Color");
    _color = new ColorSelector(this);
    _color.setColorValue(_properties.getColor());

    Label anchorLabel = new Label(this, SWT.NULL);
    anchorLabel.setText("Text Anchor");
    anchorLabel.setVisible(_showAnchor);
    _anchor = new ComboViewer(this);
    _anchor.add(TextAnchor.values());
    _anchor.setSelection(new StructuredSelection(_properties.getAnchor()));
    _anchor.getCombo().setVisible(_showAnchor);
  }

  public TextProperties getProperties() {
    return new TextProperties(new Font(null, _currentFont.getFontData()), _color.getColorValue(),
        (TextAnchor) ((StructuredSelection) _anchor.getSelection()).getFirstElement());
  }
}
