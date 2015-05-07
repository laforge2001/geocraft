/*
 * Copyright (C) ConocoPhillips 2009 All Rights Reserved. 
 */
package org.geocraft.core.model.base;


import java.util.HashMap;


/** 
 * Container for property descriptors for data file entities 
 * @author hansegj
 */
public class PropertyDescriptors {

  /** Map between a data file entity (key) and its property descriptor (value). */
  HashMap<String, PropertiesDescriptor> _propertyDescriptors = new HashMap<String, PropertiesDescriptor>();

  private static PropertyDescriptors instance = null;

  // inhibit instantiation
  protected PropertyDescriptors() {
    // The empty constructor.
  }

  public static PropertyDescriptors getInstance() {
    if (instance == null) {
      instance = new PropertyDescriptors();
    }

    return instance;
  }

  /** 
   * Get the properties descriptor for a data file entity
   * @param entityType Type of entity
   * @return null if no properties descriptor; otherwise, the properties descriptor
   * for the specified entity.
   */
  public PropertiesDescriptor getPropertiesDescriptor(final String entityType) {
    return _propertyDescriptors.get(entityType);
  }

  /**
   * Add a new properties descriptor for a data file entity to the list.
   * @param entityType Type of entity.
   * @param propertiesDescriptor Properties descriptor for the entity
   */
  public void addPropertiesDescriptor(final String entityType, final PropertiesDescriptor propertiesDescriptor) {
    _propertyDescriptors.put(entityType, propertiesDescriptor);
  }
}
