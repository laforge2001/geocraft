/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.ui.common.tree;


import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;


public class TreePropertySource implements IPropertySource {

  public static final String NAME = "name";

  public static final String DESCRIPTION = "description";

  private String _name;

  private String _description;

  public TreePropertySource(String name, String description) {
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

  public Object getPropertyValue(Object id) {
    if (id.equals(NAME)) {
      return _name;
    } else if (id.equals(DESCRIPTION)) {
      return _description;
    }
    return "";
  }

  public boolean isPropertySet(Object id) {
    return true;
  }

  public void resetPropertyValue(Object id) {
    // No action.
  }

  public void setPropertyValue(Object id, Object value) {
    // No action.
  }
}
