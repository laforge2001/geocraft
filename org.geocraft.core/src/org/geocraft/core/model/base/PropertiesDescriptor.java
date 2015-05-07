/*
 * Copyright (C) ConocoPhillips 2009 All Rights Reserved. 
 */

package org.geocraft.core.model.base;


import java.util.ArrayList;
import java.util.HashMap;


/**
 * Properties descriptor for a data file entity.
 */
public class PropertiesDescriptor {

  /** Name of the data file entity */
  String _entityName = "'";

  /** Order list of property categories for the entity */
  ArrayList<String> _categories;

  /** Ordered list of of properties for a category */
  ArrayList<EntityProperty> _properties;

  /** List of properties associated with each category */
  HashMap<String, ArrayList<EntityProperty>> _categoryProperties;

  /** List of excluded displayable properties */
  ArrayList<EntityProperty> _excludedProperties;

  /** Number of displayable properties */
  int _numProperties = 0;

  public PropertiesDescriptor(final String entityName) {
    this._entityName = entityName;

    _categories = new ArrayList<String>();
    _categoryProperties = new HashMap<String, ArrayList<EntityProperty>>();
    _excludedProperties = new ArrayList<EntityProperty>();
  }

  /**
   * Read the entity property descriptor, an XML file, which defines
   * all the properties for the entity and the property categories.
   * An empty category is legal (its name is the empty string; all 
   * its properties are at the top of the tree. Displayable properties
   * can be excluded from the property tree.
   * @return true if XML file exists and successfully processed; otherwise, false.
   */
  public boolean readEntityPropertyDesc() {
    String fileName = "/descriptors/" + _entityName + "PropertyDesc.xml";

    /* FOR TESTING PURPOSES ONLY
    if (_entityName.equals("Grid3d")) {
      genMockGrid3dProperties();
      numProperties = 21;
      return true;
    }
    */
    // if XML file does not exists (yet), do nothing
    return PropertyDescriptorParser.getInstance().parseEntityProperties(fileName, this);
  }

  // GETTERS
  /** Get the ordered list of categories for the entity */
  public ArrayList<String> getCategories() {
    return _categories;
  }

  /** Get the ordered list of properties for a category */
  public ArrayList<EntityProperty> getCategoryProperties(final String category) {
    return _categoryProperties.get(category);
  }

  /** Get the number of displayable properties */
  public int getNumProperties() {
    return _numProperties;
  }

  /** Get the number of excluded properties */
  public int getNumExcludedProperties() {
    return _numProperties;
  }

  /** Get the name of the data file entity */
  public String getEntityName() {
    return _entityName;
  }

  // SETTERS
  public void setEntityName(final String name) {
    _entityName = name;
  }

  public void addCategory(final String category) {
    _categories.add(category);
    _properties = new ArrayList<EntityProperty>();
    _categoryProperties.put(category, _properties);
  }

  public void addProperty(final String category, final String property, final String displayName) {
    _properties.add(new EntityProperty(property, displayName, category));
    _numProperties++;
  }

  public void addExcludedProperty(final String property, final String displayName) {
    _excludedProperties.add(new EntityProperty(property, displayName, "Excluded"));
  }

  /** Output parsed properties descriptor */
  public void dumpDescriptor() {
    System.out.println("Properties descriptor for " + _entityName);
    String indent = "  ";
    System.out.println(indent + "# of categories = " + _categoryProperties.size());
    System.out.println(indent + "Total # of properties = " + _numProperties);
    for (String category : _categories) {
      System.out.println(indent + "Category: " + category);
      ArrayList<EntityProperty> properties = _categoryProperties.get(category);
      for (EntityProperty property : properties) {
        System.out.println(indent + indent + "Property: id = " + property.getId() + ", display = "
            + property.getDisplayName());
      }
    }

    System.out.println(indent + "Excluded properties");
    for (EntityProperty property : _excludedProperties) {
      System.out.println(indent + indent + "Property: id = " + property.getId() + ", display = "
          + property.getDisplayName());
    }
  }

  /**
   * Individual property of an entity
   */
  public class EntityProperty {

    // Atributes of an entity property
    String _id, _displayName, _category;

    /**
     * Property constructor
     * @param id Property ID. Unique across all entity properties. Used
     * to get the value of the property.
     * @param displayName The name to display in the Property view.
     * @param category The property's category
     */
    public EntityProperty(final String id, final String displayName, final String category) {
      _id = id;
      _displayName = displayName;
      _category = category;
    }

    public String getId() {
      return _id;
    }

    public String getDisplayName() {
      return _displayName;
    }

    public String getCategory() {
      return _category;
    }
  }
}
