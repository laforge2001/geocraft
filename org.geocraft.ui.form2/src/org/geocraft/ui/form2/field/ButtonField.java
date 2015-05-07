/*
 * Copyright (C) ConocoPhillips 2009 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.geocraft.ui.common.TableWrapLayoutHelper;


/**
 * Push button. A push button can be placed anywhere in a Section.
 * Multiple push buttons can be placed next to each other. A push
 * button can contain text or an image.
 * <p>
 * The position of the button is defined by the right margin of the
 * row containing the button. The button's right edge butts up to
 * the right margin.
 * <p>
 * One or a cluster of push buttons can be created at the same time.
 * <p>
 * NOTE: The push button is based on AbstractField even thought it
 * has no parameter label, parameter toggle button or error button.
 * This is so it will work in the Form2 framework, e.g., be passed
 * as a parameter requiring an AbstractField.
 * @author hansegj
 *
 */
public class ButtonField extends AbstractField {

  private Button[] _buttons;

  private Button _button;

  /** Push buttons' text */
  private String[] _texts;

  /** Push button's image */
  private Image _image = null;

  /** Composite housing the button */
  private Composite _parent;

  /** Action handler for when a button is pushed. Implements widgetSelected() method. */
  private IFieldListener _listener;

  /** The associated property key. */
  private final String[] _keys;

  private String _key;

  /** The control widgets for the field. */
  //private Control[] _controlWidgets;

  /** Indicator state of push button changed. */
  private boolean states[];

  /** Invisible widget that defines the right margin of the row containing the button */
  private Text rightMargin;

  /**
   * Create a single push button.
   * @param parent Composite housing the push button.
   * @param listener Action handler invoked when button is pushed.
   * @param key Associated property key.
   * @param text Push button's text.
   * @param rightMargin Position of the right side of the button.
   */
  public ButtonField(final Composite parent, IFieldListener listener, final String key, final String text, final int rightMargin) {
    super(parent, key);
    _parent = parent;
    _listener = listener;
    //_listeners = new IFieldListener[1];
    //_listeners[0] = listener;
    _keys = new String[1];
    _keys[0] = key;
    _texts = new String[1];
    _texts[0] = text;

    // Create the controls.
    _controlWidgets = createControls(parent);
    // Set the controls in the base class where they are normally created and used.
    setControlWidgets(_controlWidgets);

    //The button is created in createControls() which is called by the super class, AbstractField
    _buttons[0].setText(_texts[0]);
    ((TableWrapData) this.rightMargin.getLayoutData()).maxWidth = rightMargin;
  }

  /**
   * Create a set of push buttons clustered together.
   * @param parent Composite housing the push buttons.
   * @param listener Action handler invoked when button is pushed.
   * @param key Property key for the set of push buttons
   * @param keys Associated property keys for each push button.
   * @param texts Text for each push button.
   * @param rightMargin Position of the right side of the buttons.
   */
  public ButtonField(final Composite parent, final IFieldListener listener, final String key, final String keys[], final String texts[], final int rightMargin) {
    super(parent, key);
    _parent = parent;
    _listener = listener;
    _keys = keys;
    _texts = texts;

    // Create the controls.
    _controlWidgets = createControls(parent);
    // Set the controls in the base class where they are normally created and used.
    setControlWidgets(_controlWidgets);

    //The buttons are created in createControls() which is called by the super class, AbstractField
    for (int i = 0; i < _buttons.length; i++) {
      _buttons[i].setText(_texts[i]);
    }
    ((TableWrapData) this.rightMargin.getLayoutData()).maxWidth = rightMargin;
  }

  //SETTERS

  /**
   * Set the text of the ith button. There is no label next to a button.
   * @param text Button's text
   * @param idx Button's index in the set of buttons starting at 0.
   */
  public void setText(String text, int idx) {
    if (idx < 0 || idx >= _buttons.length) {
      return;
    }
    _buttons[idx].setText(text);
  }

  /**
   * Set the image of the ith button..
   * @param img Button's image
   * @param idx Button's index in the set of buttons starting at 0.
   */
  public void setImage(Image img, int idx) {
    if (idx < 0 || idx >= _buttons.length) {
      return;
    }
    _buttons[idx].setImage(img);
  }

  /** 
   * Set the background color of all the buttons.
   * @param bgcolor New background color
   */
  public void setBackgroundColor(Color color) {
    for (Button button : _buttons) {
      button.setBackground(color);
    }
  }

  /** 
   * Set the foreground color for all the buttons.
   * @param fgcolor New foreground color
   */
  public void setForegroundColor(Color color) {
    for (Button button : _buttons) {
      button.setForeground(color);
    }
  }

  /** Set a button's tooltip */
  @Override
  public void setTooltip(final String tooltip) {
    if (_button != null) {
      _button.setToolTipText(tooltip);
    }
  }

  /** Set the tooltip for the ith button. */
  public void setTooltip(final String tooltip, int idx) {
    //super.setTooltip(tooltip);
    if (idx < 0 || idx >= _buttons.length) {
      return;
    }
    _button = _buttons[idx];
    setTooltip(tooltip);
  }

  //GETTERS
  //Get the key for the ith button
  public String getKey(int idx) {
    if (idx < 0 || idx >= _buttons.length) {
      return "";
    }
    //_key = _keys[idx];
    return _keys[idx];
  }

  /**
   * Sets the visibility of the field. If hiding the field, reduces its height to zero.
   * If showing, reinstates its height to the default in the layout data. Redoing the
   * layout of the field's parent container is done separately. This must be done so
   * the field is redrawn.
   * <p>
   * NOTE: For some controls, such as a Combo, their height cannot be reduced less
   * that its preferred height.
   * *
   * @param visible <i>true</i> to show the field; <i>false</i> to hide the field.
   */
  @Override
  public void setVisible(final boolean active) {
    TableWrapData layoutData;
    if (!active) { // hide: set height of field to zero
      for (Control control : _controlWidgets) {
        layoutData = (TableWrapData) control.getLayoutData();
        layoutData.maxHeight = 0;
        control.setVisible(active);
      }
    } else { // show: restore height of field
      for (Control control : _controlWidgets) {
        layoutData = (TableWrapData) control.getLayoutData();
        layoutData.maxHeight = layoutData.heightHint;
        control.setVisible(active);
      }
    }
  }

  /**
   * Redo the layout for the Composite housing the field
   */
  @Override
  public void redoLayout() {
    _parent.layout(true);
  }

  @Override
  public Control[] createControls(final Composite parent) {
    int numButtons = _keys.length > _texts.length ? _texts.length : _keys.length;
    TableWrapData layoutData;

    Composite container = new Composite(parent, SWT.NONE);
    container
        .setLayoutData(TableWrapLayoutHelper.createLayoutData(true, false, TableWrapData.FILL, TableWrapData.FILL));
    TableWrapLayout layout = TableWrapLayoutHelper.createLayout(2 + numButtons, false);
    container.setLayout(layout);

    Text blank = new Text(container, SWT.BORDER);
    blank.setText("");
    blank.setEditable(false);
    blank.setVisible(false);
    blank.setLayoutData(TableWrapLayoutHelper.createLayoutData(true, false, TableWrapData.FILL, TableWrapData.FILL));

    _buttons = new Button[numButtons];
    states = new boolean[numButtons];
    for (int i = 0; i < numButtons; i++) {
      _buttons[i] = new Button(container, SWT.PUSH);
      layoutData = TableWrapLayoutHelper.createLayoutData(false, false, TableWrapData.FILL, TableWrapData.FILL);
      layoutData.grabVertical = false;
      _buttons[i].setLayoutData(layoutData);
      states[i] = true;
    }

    rightMargin = new Text(container, SWT.BORDER);
    rightMargin.setText("");
    rightMargin.setEditable(false);
    rightMargin.setVisible(false);
    layoutData = TableWrapLayoutHelper.createLayoutData(false, false, TableWrapData.RIGHT, TableWrapData.FILL);
    rightMargin.setLayoutData(layoutData);

    for (int i = 0; i < numButtons; i++) {
      final String _key = _keys[i];
      _buttons[i].addListener(SWT.Selection, new Listener() {

        boolean state;

        public void handleEvent(final Event event) {
          //NOTE: The value sent back is irrelevant, but is a required parameter.
          //      This just indicates the button was pushed. The action handler
          //      is contained in the algorithm's propertyChanged() method.
          //NOTE: getSelection() for a push button always returns false
          // HOWEVER, the value must alternate between true and false so the model
          // will think the state changed and invoke the algorithms's propertyChanged()
          // method where the button action handlers are implemented.
          Boolean valueObject = new Boolean(!state); //must start out true; boolean default is false
          state = !state;
          _listener.fieldChanged(_key, valueObject);
          return;
        }
      });
    }

    return new Control[] { container };
  }

  // Methods provided in AbstractBase that we don't need, but which are necessary
  // if the ButtonField is to function in the Form2 framework. 

  @Override
  public void adapt(final FormToolkit toolkit) {
    for (Control control : _controlWidgets) {
      toolkit.adapt(control, true, true);
    }
    setForegroundColor(toolkit.getColors().getColor(IFormColors.TITLE));
  }

  @Override
  public void updateField(Object valueObject) {
    // do nothing
  }

  @Override
  public void setStatus(final IStatus status) {
    //Do nothing, there is no status icon associated with a push button
  }

  @Override
  public void setInternalStatus(IStatus status) {
    //Do nothing, there is no internal status of a push button
  }

  /**
   * Set the enable state of the ith button in the field. Do nothing
   * if there is no ith button. Buttons are numbers from left to right
   * starting with 1.
   * @param enabled true if button enabled; false if disabled
   * @param idx The ith button in the field.
   */
  public void setEnabled(boolean enabled, int idx) {
    if (idx > 0 && idx <= _buttons.length) {
      _buttons[idx - 1].setEnabled(enabled);
    }
  }

  private int findIndex(String key) {
    for (int i = 0; i < _keys.length; ++i) {
      if (_keys[i].equals(key)) {
        return i + 1; //weirdly, these buttons are indexed starting at one
      }
    }
    return -1;
  }

  public void setEnabled(boolean enabled, String key) {
    int idx = findIndex(key);
    setEnabled(enabled, idx);
  }

  /**
   * Set the enable state of all the buttons in the field.
   */
  @Override
  public void setEnabled(boolean enabled) {
    for (Button button : _buttons) {
      button.setEnabled(enabled);
    }
  }
}
