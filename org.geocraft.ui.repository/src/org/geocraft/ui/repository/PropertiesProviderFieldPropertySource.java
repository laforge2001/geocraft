/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.ui.repository;


import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.geocraft.core.model.base.IPropertiesProvider;


public class PropertiesProviderFieldPropertySource implements IPropertySource {

  private final IPropertiesProvider _propProvider;

  public PropertiesProviderFieldPropertySource(final IPropertiesProvider propProvider) {
    _propProvider = propProvider;
  }

  public Object getEditableValue() {
    return null;
  }

  public IPropertyDescriptor[] getPropertyDescriptors() {
    return PropertiesProviderUtils.getInstance().getPropertyDescriptors(_propProvider);

    /*    
        PropertiesDescriptor propertiesDescriptor = ((AbstractPropertiesProvider) _propProvider).getPropertiesDescriptor();
        IPropertyDescriptor[] descriptors = null;

        Object[][] properties = _propProvider.getDisplayableProperties();
        int len = properties.length;
        if (propertiesDescriptor == null) { // create a linear list of properties in alphabetical order
          descriptors = new IPropertyDescriptor[len];
          for (int i = 0; i < len; i++) {
            descriptors[i] = PropertiesProviderFieldPropertyDescriptorFactory.getInstance().getPropertyDescriptor(
                properties[i][1], properties[i][0], properties[i][0].toString(), false);
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
          descriptors = new IPropertyDescriptor[len];
          for (String category : categories) {
            ArrayList<EntityProperty> props = propertiesDescriptor.getCategoryProperties(category);
            String propName = "";
            try {
              for (EntityProperty property : props) {
                propName = property.getId();
                int k = propIdxs.get(propName).intValue();
                descriptors[idx] = PropertiesProviderFieldPropertyDescriptorFactory.getInstance().getPropertyDescriptor(
                    properties[k][1], properties[k][0], property.getDisplayName(), false);
                ((PropertyDescriptor) descriptors[idx]).setCategory(category);
                idx++;
              }
            } catch (NullPointerException npe) {
              System.out.println("Error: Property " + propName + " does not exist in entity "
                  + propertiesDescriptor.getEntityName());
            }
          }
        }
        return descriptors;
        */
  }

  public Object getPropertyValue(final Object id) {
    Object[][] properties = _propProvider.getDisplayableProperties();
    for (Object[] prop : properties) {
      if (prop[0].toString().equalsIgnoreCase(id.toString())) {
        return prop[1];
      }
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
    // TODO:
  }
}
