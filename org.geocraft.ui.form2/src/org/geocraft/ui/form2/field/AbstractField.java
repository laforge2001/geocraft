/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.geocraft.core.model.property.BooleanProperty;


public abstract class AbstractField {

  /** The associated property key. */
  protected final String _key;

  /**
   * The property status flag.
   * This relates to the validity of the associated model property.
   */
  protected IStatus _status;

  /**
   * The internal status flag.
   * This relates to the validity of the field itself (whether a valid selection is made, etc),
   * and is unrelated to the validity of the associated model property.
   */
  protected IStatus _statusInternal;

  protected final IFieldListener _listener;

  /** The toggle button for activating/deactivating the parameter field. */
  protected final Button _toggleWidget;

  /** The label widget for the parameter field. */
  protected final Label _labelWidget;

  /** The status widget for the parameter field. */
  protected final Label _statusWidget;

  /** The control widgets for the parameter field. */
  protected Control[] _controlWidgets;

  protected Composite _parent;

  protected String _toggleKey = "";

  protected boolean _isRequired = true;

  protected boolean _readOnly = false;

  protected BooleanProperty _showActiveProperty = null;

  protected PropertyChangeListener _showActiveListener = null;

  //SETTERS
  protected void setControlWidgets(Control[] controlWidgets) {
    _controlWidgets = controlWidgets;
  }

  /**
   * Constructor for fields that have no parameter label, parameter toggle or error indicator,
   * such as a push button. The controls are not created here, but by the subclass. This is so
   * it can have greater control on the layout of the controls, like multiple push buttons.
   * <p>
   * The sole purpose of the constructor is to set up the 3 widgets that proceed very field,
   * namely, the toggle, status and label widgets.
   * @param parent Composite housing the field
   * @param key Property key.
   */

  public AbstractField(final Composite parent, final String key) { //, IFieldListener listener, final String key) {
    _parent = parent;
    _key = key;
    //NOTE: The listener is immaterial because a _toggleWidget listener is not being created.
    _listener = null;

    // Create the toggle.
    _toggleWidget = new Button(parent, SWT.CHECK);

    TableWrapData layoutData = new TableWrapData();
    layoutData.grabHorizontal = false;
    layoutData.grabVertical = false;//false;
    layoutData.align = TableWrapData.FILL;
    layoutData.valign = TableWrapData.MIDDLE;
    _toggleWidget.setLayoutData(layoutData);
    _toggleWidget.setSelection(false);
    _toggleWidget.setVisible(false);

    // Create the label.
    _labelWidget = new Label(parent, SWT.NONE);
    _labelWidget.setText("");

    layoutData = new TableWrapData();
    layoutData.grabHorizontal = false;
    layoutData.grabVertical = false;
    layoutData.align = TableWrapData.RIGHT;
    layoutData.valign = TableWrapData.MIDDLE;
    _labelWidget.setLayoutData(layoutData);

    // Create the error label.
    _statusWidget = new Label(parent, SWT.NONE);
    _statusWidget.setText("  ");

    layoutData = new TableWrapData();
    layoutData.grabHorizontal = false;
    layoutData.grabVertical = false;
    layoutData.align = TableWrapData.RIGHT;
    layoutData.valign = TableWrapData.MIDDLE;
    layoutData.indent = 5;
    _statusWidget.setLayoutData(layoutData);
    _status = ValidationStatus.ok();
    _statusInternal = ValidationStatus.ok();

    _showActiveListener = new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        Object newValue = evt.getNewValue();
        if (newValue != null) {
          boolean active = Boolean.parseBoolean(newValue.toString());
          _toggleWidget.setSelection(active);
          setActiveInternal();
        }
      }

    };
  }

  public AbstractField(final Composite parent, IFieldListener listener, final String key, final String label, final boolean showToggle) {
    this(parent, listener, key, label, showToggle, false);
  }

  public AbstractField(final Composite parent, IFieldListener listener, final String key, final String label, final boolean showToggle, boolean readOnly) {
    _parent = parent;
    _key = key;
    _listener = listener;
    _status = ValidationStatus.ok();
    _statusInternal = ValidationStatus.ok();
    _readOnly = readOnly;

    // Create the toggle.
    _toggleWidget = new Button(parent, SWT.CHECK);

    TableWrapData layoutData = new TableWrapData();
    layoutData.grabHorizontal = false;
    layoutData.grabVertical = true;//false;
    layoutData.align = TableWrapData.FILL;
    layoutData.valign = TableWrapData.MIDDLE;
    _toggleWidget.setLayoutData(layoutData);
    _toggleWidget.setSelection(true);
    _toggleWidget.setVisible(showToggle);

    // Create the label.
    _labelWidget = new Label(parent, SWT.NONE);
    if (label != null && label.length() > 0) {
      _labelWidget.setText(label + ":");
    }

    layoutData = new TableWrapData();
    layoutData.grabHorizontal = false;
    layoutData.grabVertical = false;
    layoutData.align = TableWrapData.RIGHT;
    layoutData.valign = TableWrapData.MIDDLE;
    _labelWidget.setLayoutData(layoutData);

    // Create the error label.
    _statusWidget = new Label(parent, SWT.NONE);
    _statusWidget.setText("  ");

    layoutData = new TableWrapData();
    layoutData.grabHorizontal = false;
    layoutData.grabVertical = true;
    layoutData.align = TableWrapData.RIGHT;
    layoutData.valign = TableWrapData.MIDDLE;
    layoutData.indent = 5;
    _statusWidget.setLayoutData(layoutData);

    // Create the controls.
    _controlWidgets = createControls(parent);

    _toggleWidget.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        boolean selection = _toggleWidget.getSelection();
        //        if (!_toggleWidget.getSelection()) {
        //          _listener.fieldChanged(_key, null);
        //        }
        setActiveInternal();
        if (!_toggleKey.isEmpty()) {
          _listener.fieldChanged(_toggleKey, selection);
        }
        _listener.fieldEnabled(_key, selection);
      }
    });

    _showActiveListener = new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        Object newValue = evt.getNewValue();
        if (newValue != null) {
          boolean active = Boolean.parseBoolean(newValue.toString());
          _toggleWidget.setSelection(active);
          setActiveInternal();
        }
      }

    };

    setStatus(ValidationStatus.ok());
  }

  /**
   * Adapts the field controls (adjusts colors, etc).
   * 
   * @param toolkit the toolkit.
   */
  public void adapt(final FormToolkit toolkit) {
    toolkit.adapt(_toggleWidget, true, true);
    toolkit.adapt(_labelWidget, true, true);
    _labelWidget.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
    toolkit.adapt(_statusWidget, true, true);
    for (Control control : _controlWidgets) {
      if (!(control instanceof Button)) {
        toolkit.adapt(control, true, true);
      }
    }
  }

  /**
   * Shows or hides the activation toggle button for the field.
   * 
   * @param property the boolean property to bind to the toggle selection.
   */
  public void showActiveFieldToggle(BooleanProperty property) {
    _toggleKey = property.getKey();
    _toggleWidget.setVisible(true);
    setEnabled(property.get());
    _isRequired = false;
    if (_showActiveProperty != null) {
      _showActiveProperty.removePropertyChangeListener(_showActiveListener);
    }
    _showActiveProperty = property;
    if (_showActiveProperty != null) {
      _showActiveProperty.addPropertyChangeListener(_showActiveListener);
    }
  }

  /**
   * Returns the selected (active) status of the field.
   * 
   * @return <i>true</i> if the field is active; <i>false</i> if not.
   */
  public boolean isActive() {
    return _toggleWidget.getSelection();
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
  public void setVisible(final boolean visible) {
    /*
        _labelWidget.setVisible(active);
        for (Control control : _controlWidgets) {
          control.setVisible(active);
        }
    */
    TableWrapData layoutData;
    if (!visible) { // hide: set height of field to zero
      layoutData = (TableWrapData) _toggleWidget.getLayoutData();
      layoutData.maxHeight = 0;
      _toggleWidget.setVisible(visible);
      layoutData = (TableWrapData) _labelWidget.getLayoutData();
      layoutData.maxHeight = 0;
      _labelWidget.setVisible(visible);
      layoutData = (TableWrapData) _statusWidget.getLayoutData();
      layoutData.maxHeight = 0;
      _statusWidget.setVisible(visible);
      for (Control control : _controlWidgets) {
        layoutData = (TableWrapData) control.getLayoutData();
        layoutData.maxHeight = 0;
        control.setVisible(visible);
      }
    } else { // show: restore height of field
      if (!_isRequired) {
        layoutData = (TableWrapData) _toggleWidget.getLayoutData();
        layoutData.maxHeight = layoutData.heightHint;
        _toggleWidget.setVisible(visible);
      }
      layoutData = (TableWrapData) _labelWidget.getLayoutData();
      layoutData.maxHeight = layoutData.heightHint;
      _labelWidget.setVisible(visible);
      layoutData = (TableWrapData) _statusWidget.getLayoutData();
      layoutData.maxHeight = layoutData.heightHint;
      _statusWidget.setVisible(visible);
      for (Control control : _controlWidgets) {
        layoutData = (TableWrapData) control.getLayoutData();
        layoutData.maxHeight = layoutData.heightHint;
        control.setVisible(visible);
      }
    }
  }

  public boolean getVisible() {
    return _toggleWidget.getVisible();
  }

  /**
   * Redo the layout for the Composite housing the field
   */
  public void redoLayout() {
    _parent.layout(true);
  }

  /**
   * Sets the selected (active) status of the field.
   * 
   * @param active <i>true</i> to activate the field; <i>false</i> to deactivate the field.
   */
  public void setEnabled(final boolean active) {
    _toggleWidget.setSelection(active);
    setActiveInternal();
  }

  private void setActiveInternal() {
    boolean enabled = _toggleWidget.getSelection();
    _labelWidget.setEnabled(enabled);
    _statusWidget.setEnabled(enabled);
    _statusWidget.setVisible(enabled);
    for (Control control : _controlWidgets) {
      control.setEnabled(enabled);
    }
  }

  /**
   * Sets the tooltip for the parameter field.
   * 
   * @param tooltip the tooltip to set.
   */
  public void setTooltip(final String tooltip) {
    _labelWidget.setToolTipText(tooltip);
  }

  /**
   * Returns the internal status flag of the field.
   */
  public IStatus getInternalStatus() {
    return _statusInternal;
  }

  /**
   * Sets the internal status flag of the field and updates the status icon.
   * If not OK, this will override the property status when it comes to displaying the status icon.
   * 
   * @param status the internal status to set.
   */
  public void setInternalStatus(IStatus status) {
    _statusInternal = status;
    updateStatusIcon();
  }

  /**
   * Sets the property status of the field and updates the status icon.
   * 
   * @param status the status to set.
   */
  public void setStatus(final IStatus status) {
    _status = status;
    updateStatusIcon();
  }

  /**
   * Updates the status icon of the field.
   * The internal status flag is check first.
   * If it is not OK, then that status is used to display the status icon..
   * If it is OK, then the property status flag is used to display the status icon.
   */
  protected void updateStatusIcon() {
    IStatus status = _statusInternal;
    if (status.isOK()) {
      status = _status;
    }
    //    if (status == null) {
    //      _status = ValidationStatus.ok();
    //      _statusWidget.setImage(ImageRegistryUtil.getSharedImages().getImage(
    //          org.geocraft.ui.common.image.ISharedImages.IMG_BLANK));
    //      _statusWidget.setToolTipText("");
    //      return;
    //    }
    //    _status = status;
    switch (status.getSeverity()) {
      case IStatus.ERROR:
        //_statusWidget.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
        Image image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
        //Rectangle bounds = image.getBounds();
        //Image scaledImage = new Image(Display.getCurrent(), image.getImageData().scaledTo((int) (bounds.width * .9), (int) (bounds.height * .9)));
        _statusWidget.setImage(image);
        _statusWidget.setToolTipText(status.getMessage());
        //scaledImage.dispose();
        break;
      case IStatus.WARNING:
        _statusWidget.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK));
        _statusWidget.setToolTipText(status.getMessage());
        break;
      case IStatus.INFO:
        _statusWidget.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK));
        _statusWidget.setToolTipText(status.getMessage());
        break;
      default:
        _statusWidget.setImage(null);//ImageRegistryUtil.getSharedImages().getImage(
        //org.geocraft.ui.common.image.ISharedImages.IMG_BLANK));
        _statusWidget.setToolTipText("");
    }
  }

  //public IStatus getStatus() {
  //  return _status;
  //}

  public void setLabel(final String label) {
    if (label != null && label.length() > 0) {
      _labelWidget.setText(label + ":");
    } else {
      _labelWidget.setText("");
    }
    _parent.pack();
  }

  public String getLabel() {
    String label = _labelWidget.getText();
    if ("".equals(label)) {
      return label;
    }

    String strippedLabel = label.substring(0, label.length() - 1); //strip off the ':' at the end
    return strippedLabel;
  }

  public abstract Control[] createControls(final Composite parent);

  public abstract void updateField(Object valueObject);

  /**
   * Disposes of the parameter field and its resources.
   */
  public void dispose() {
    // Remove the property listener, if a boolean property has been specified.
    if (_showActiveProperty != null) {
      _showActiveProperty.removePropertyChangeListener(_showActiveListener);
    }
  }

  public String getKey() {
    return _key;
  }
}
