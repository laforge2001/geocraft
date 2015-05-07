/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.ui.repository;


import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;


public class PropertiesProviderFieldPropertyDescriptorFactory {

  private static PropertiesProviderFieldPropertyDescriptorFactory _instance = new PropertiesProviderFieldPropertyDescriptorFactory();

  /**
   * 
   * @param element Value of the property
   * @param id ID of the property
   * @param propertyName Display name of the property
   * @param editable true if property editable; otherwise, false
   * @return Property descriptor used by PropertyPageSheet
   */
  IPropertyDescriptor getPropertyDescriptor(Object element, Object id, String propertyName, boolean editable) {
    if (element instanceof String && editable) {
      return new StringEntityFieldPropertyDescriptor(id, propertyName);
    }
    return new EntityFieldPropertyDescriptor(id, propertyName);
  }

  static PropertiesProviderFieldPropertyDescriptorFactory getInstance() {
    return _instance;
  }

  class EntityFieldPropertyDescriptor extends PropertyDescriptor {

    EntityFieldPropertyDescriptor(Object id, String propertyName) {
      super(id, propertyName);
    }

    @Override
    public ILabelProvider getLabelProvider() {
      return new PropertiesProviderFieldLabelProvider();
    }
  }

  class StringEntityFieldPropertyDescriptor extends TextPropertyDescriptor {

    StringEntityFieldPropertyDescriptor(Object id, String propertyName) {
      super(id, propertyName);
    }

    @Override
    public ILabelProvider getLabelProvider() {
      return new PropertiesProviderFieldLabelProvider();
    }
  }

}
