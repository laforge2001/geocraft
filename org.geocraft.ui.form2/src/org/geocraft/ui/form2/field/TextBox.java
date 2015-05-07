/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.TableWrapData;


public class TextBox extends AbstractField {

  /** The text field control. */
  private Text _text;

  private TableWrapData _fieldLayoutData;

  /**
   * Constructs a parameter text field.
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
  public TextBox(final Composite parent, IFieldListener listener, final String key, final String label, final boolean showToggle) {
    super(parent, listener, key, label, showToggle);
  }

  /**
   * Construct a text box with a preferred height.
   * @param parent The parent composite housing the text box
   * @param listener
   * @param key The key of the associated property
   * @param label The parameter label
   * @param showToggle
   *            <i>true</i> to show a parameter toggle button; otherwise
   *            <i>false</i>.
   * @param height
   */
  public TextBox(final Composite parent, IFieldListener listener, final String key, final String label, final boolean showToggle, final int height) {
    super(parent, listener, key, label, showToggle);
    if (height > 0) {
      //Note: the field's layout is created by createControls which is called 
      //      by the base class (AbstractField)
      _fieldLayoutData.heightHint = height;
    }
  }

  @Override
  public Control[] createControls(final Composite parent) {
    int style = SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.WRAP | SWT.H_SCROLL;
    _text = new Text(parent, style);
    _text.setText("");

    _fieldLayoutData = new TableWrapData();
    _fieldLayoutData.grabHorizontal = true;
    _fieldLayoutData.grabVertical = true;
    _fieldLayoutData.align = TableWrapData.FILL;
    _fieldLayoutData.valign = TableWrapData.FILL;
    _fieldLayoutData.rowspan = 5;
    _text.setLayoutData(_fieldLayoutData);

    TableWrapData layoutData = new TableWrapData();
    layoutData.grabHorizontal = false;
    layoutData.grabVertical = false;
    layoutData.align = TableWrapData.RIGHT;
    layoutData.valign = TableWrapData.TOP;
    layoutData.rowspan = 1;
    _labelWidget.setLayoutData(layoutData);

    layoutData = new TableWrapData();
    layoutData.grabHorizontal = false;
    layoutData.grabVertical = false;
    layoutData.align = TableWrapData.CENTER;
    layoutData.valign = TableWrapData.TOP;
    layoutData.rowspan = 1;
    _statusWidget.setLayoutData(layoutData);

    layoutData = new TableWrapData();
    layoutData.grabHorizontal = false;
    layoutData.grabVertical = false;
    layoutData.align = TableWrapData.FILL;
    layoutData.valign = TableWrapData.TOP;
    layoutData.rowspan = 1;
    _toggleWidget.setLayoutData(layoutData);

    // This is a dummy component.
    // Its only purpose is to fill up space so that the layout
    // location of the label, status symbol, etc are aligned
    // along the top of the text field.
    Composite blank = new Composite(parent, SWT.NONE);
    layoutData = new TableWrapData();
    layoutData.grabHorizontal = false;
    layoutData.grabVertical = false;
    layoutData.align = TableWrapData.FILL;
    layoutData.valign = TableWrapData.FILL;
    layoutData.colspan = 3;
    layoutData.rowspan = 4;
    blank.setLayoutData(layoutData);

    _text.addListener(SWT.KeyUp, new Listener() {

      public void handleEvent(final Event event) {
        String valueObject = _text.getText();
        _listener.fieldChanged(_key, valueObject);
      }
    });
    return new Control[] { _text };
  }

  @Override
  public void updateField(Object valueObject) {
    if (valueObject != null) {
      _text.setText(valueObject.toString());
    } else {
      _text.setText("");
    }
    setInternalStatus(ValidationStatus.ok());
  }

  /**
   * Set the editable state
   * @param editable true if editable, false if not editable
   */
  public void setEditable(boolean editable) {
    _text.setEditable(editable);
  }

  /**
   * Set the text box's text
   * @param text The text box's text
   */
  public void setText(String text) {
    _text.setText(text);
  }

  /**
   * Append text to the text box.
   * @param text Text to append to the end
   */
  public void appendText(String text) {
    _text.append(text);
  }

  /**
   * Get the text box's text
   * @return The entered text
   */
  public String getText() {
    return _text.getText();
  }

  /**
   * Set the scrollbar to the top.
   */
  public void setScrollbarTop() {
    _text.setSelection(0);
  }

  /**
   * Set the textbox's font
   * @param font Font to set
   */
  public void setFont(Font font) {
    _text.setFont(font);
  }
}
