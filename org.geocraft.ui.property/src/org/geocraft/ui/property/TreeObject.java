/*
 * Copyright (C) ConocoPhillips 2009 All Rights Reserved.
 */
package org.geocraft.ui.property;


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.Image;


/*
 * A node in the property tree.
 */
public class TreeObject implements IAdaptable {

  /** Category of the property */
  private final String _category;

  /** Entity property */
  private final String _property;

  /** Value of the property */
  private Object _value;

  /** Image of the property */
  private Image _propertyImage;

  /** Image of the property value */
  private Image _valueImage;

  private TreeParent _parent;

  public TreeObject(final String category, String property, Object value, Image propertyImage, Image valueImage) {
    _category = category;
    _property = property;
    _value = value;
    _propertyImage = propertyImage;
    _valueImage = valueImage;
  }

  public String getCategory() {
    return _category;
  }

  public String getProperty() {
    return _property;
  }

  public Object getValue() {
    return _value;
  }

  public Image getPropertyImage() {
    return _propertyImage;
  }

  public Image getValueImage() {
    return _valueImage;
  }

  public void setParent(final TreeParent parent) {
    _parent = parent;
  }

  public TreeParent getParent() {
    return _parent;
  }

  public String getName() {
    return _category;
  }

  @Override
  public String toString() {
    return getProperty().equals("") ? getCategory() : getProperty();
  }

  public String getValueString() {
    return getValue() != null ? getValue().toString() : "";
  }

  @SuppressWarnings("unused")
  public Object getAdapter(final Class key) {
    return null;
  }

  public boolean isLeaf() {
    return true;
  }
}
