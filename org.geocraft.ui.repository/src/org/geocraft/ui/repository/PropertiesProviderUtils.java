/*
 * Copyright (C) ConocoPhillips 2009 All Rights Reserved. 
 */
package org.geocraft.ui.repository;


import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.geocraft.core.model.base.AbstractPropertiesProvider;
import org.geocraft.core.model.base.IPropertiesProvider;
import org.geocraft.core.model.base.PropertiesDescriptor;
import org.geocraft.core.model.base.PropertyDescriptor;
import org.geocraft.core.model.base.PropertiesDescriptor.EntityProperty;
import org.geocraft.ui.model.IModelSharedImages;
import org.geocraft.ui.model.ModelUI;


public class PropertiesProviderUtils {

  private static PropertiesProviderUtils instance = null;

  // inhibit instantiation
  protected PropertiesProviderUtils() {
    // The empty constructor.
  }

  public static PropertiesProviderUtils getInstance() {
    if (instance == null) {
      instance = new PropertiesProviderUtils();
    }

    return instance;
  }

  /**
   * Create a list of property descriptors for all the displayable properties of this
   * property provider.
   * @param propProvider Provider of the properties
   * @return List of descriptors for all the displayable properties
   */
  public PropertyDescriptor[] getPropertyDescriptors(IPropertiesProvider propProvider) {
    PropertiesDescriptor propertiesDescriptor = ((AbstractPropertiesProvider) propProvider).getPropertiesDescriptor();
    PropertyDescriptor[] descriptors = null;
    Object[][] properties = propProvider.getDisplayableProperties();
    int len = properties.length;
    if (propertiesDescriptor == null) { // create a linear list of properties in alphabetical order
      descriptors = new PropertyDescriptor[len];
      for (int i = 0; i < len; i++) {
        descriptors[i] = getPropertyDescriptor(properties[i][1], properties[i][0], properties[i][0].toString(), false);
      }
    } else { // create a property tree
      // Check if there are more properties than specified in the descriptor
      if (len > propertiesDescriptor.getNumProperties() + propertiesDescriptor.getNumExcludedProperties()) {
        System.out.println("Warning: " + propertiesDescriptor.getEntityName()
            + " has properties that will not be displayed");
      }
      HashMap<String, Integer> propIdxs = new HashMap<String, Integer>();
      // get the indices for all of the properties
      for (int i = 0; i < len; i++) {
        String name = properties[i][0].toString();
        propIdxs.put(name, new Integer(i));
      }

      ArrayList<String> categories = propertiesDescriptor.getCategories();
      int idx = 0;
      len = propertiesDescriptor.getNumProperties();
      descriptors = new PropertyDescriptor[len];
      for (String category : categories) {
        ArrayList<EntityProperty> props = propertiesDescriptor.getCategoryProperties(category);
        String propName = "";
        try {
          for (EntityProperty property : props) {
            propName = property.getId();
            int k = propIdxs.get(propName).intValue();
            descriptors[idx] = getPropertyDescriptor(properties[k][1], properties[k][0], property.getDisplayName(),
                false);
            descriptors[idx].setCategory(category);
            idx++;
          }
        } catch (NullPointerException npe) {
          System.out.println("Error: Property " + propName + " does not exist in entity "
              + propertiesDescriptor.getEntityName());
        }
      }
    }

    return descriptors;
  }

  /**
   * Create property's descriptor
   * @param value Value of the property
   * @param id ID of the property
   * @param propertyName Display name of the property
   * @param editable true if property editable; otherwise, false
   * @return Property descriptor
   */
  PropertyDescriptor getPropertyDescriptor(final Object value, final Object id, final String propertyName,
      final boolean editable) {
    /*
    if (value instanceof String && editable) {
      return new StringEntityPropertyDescriptor(id, propertyName, value);
    }
    */
    return new EntityPropertyDescriptor(id, propertyName, value);
  }

  class EntityPropertyDescriptor extends PropertyDescriptor {

    EntityPropertyDescriptor(final Object id, final String propertyName, final Object value) {
      super(id, propertyName, value);
    }

    @Override
    public ILabelProvider getLabelProvider() {
      return new FieldLabelProvider();
    }
  }

  /*
    class StringEntityPropertyDescriptor extends TextPropertyDescriptor {

      StringEntityPropertyDescriptor(final Object id, final String propertyName) {
        super(id, propertyName);
      }

      @Override
      public ILabelProvider getLabelProvider() {
        return new FieldLabelProvider();
      }
    }
  */
  public class FieldLabelProvider implements ILabelProvider {

    private final IModelSharedImages _sharedImages = ModelUI.getSharedImages();

    public Image getImage(final Object element) {
      if (element != null && element instanceof AbstractPropertiesProvider) {
        return _sharedImages.getImage((IPropertiesProvider) element);
      }
      return null;
    }

    public String getText(final Object element) {
      if (element != null) {
        return element.toString();
      }
      return "";
    }

    public void addListener(final ILabelProviderListener listener) {
      // TODO Auto-generated method stub

    }

    public void dispose() {
      // TODO Auto-generated method stub

    }

    public boolean isLabelProperty(final Object element, final String property) {
      // TODO Auto-generated method stub
      return false;
    }

    public void removeListener(final ILabelProviderListener listener) {
      // TODO Auto-generated method stub

    }
  }
}
