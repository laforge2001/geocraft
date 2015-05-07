/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.layer;


import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;


public class LayerPropertySource implements IPropertySource {

  public static final String NAME = "name";

  public static final String DESCRIPTION = "description";

  private final String _name;

  private final String _description;

  public LayerPropertySource(final String name, final String description) {
    _name = name;
    _description = description;
  }

  public Object getEditableValue() {
    return null;
  }

  public IPropertyDescriptor[] getPropertyDescriptors() {
    IPropertyDescriptor[] descriptors = new IPropertyDescriptor[2];
    descriptors[0] = new PropertyDescriptor(NAME, "Name");
    descriptors[1] = new PropertyDescriptor(DESCRIPTION, "Description");
    return descriptors;
  }

  public Object getPropertyValue(final Object id) {
    if (id.equals(NAME)) {
      return _name;
    } else if (id.equals(DESCRIPTION)) {
      return _description;
    }
    return "";
  }

  public boolean isPropertySet(final Object id) {
    return true;
  }

  public void resetPropertyValue(final Object id) {
    // No action.
  }

  public void setPropertyValue(final Object id, final Object value) {
    // No action.
  }
}
