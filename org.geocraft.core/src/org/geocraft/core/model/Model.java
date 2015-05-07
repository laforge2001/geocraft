/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model;


import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.color.ColorBar;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.ColorBarProperty;
import org.geocraft.core.model.property.ColorProperty;
import org.geocraft.core.model.property.DoubleProperty;
import org.geocraft.core.model.property.EntityArrayProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumArrayProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FileProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.FontProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.LongProperty;
import org.geocraft.core.model.property.ObjectProperty;
import org.geocraft.core.model.property.OutputEntityObject;
import org.geocraft.core.model.property.OutputEntityProperty;
import org.geocraft.core.model.property.Property;
import org.geocraft.core.model.property.StringArrayProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.service.ServiceProvider;
import org.osgi.service.prefs.Preferences;


/**
 * The abstract implementation of the <code>IModel</code> interface. This method
 * provides default implementations of the <code>pickle</code> and
 * <code>unpickle</code> methods.
 */
public abstract class Model implements IModel {

  /** The collection of model properties, mapped by their keys. */
  private final Map<String, Property> _properties;

  /** The collection of model listeners. */
  private final List<IModelListener> _listeners;

  public Model() {
    // Initialize the property and listener collections.
    _properties = Collections.synchronizedMap(new HashMap<String, Property>());
    _listeners = Collections.synchronizedList(new ArrayList<IModelListener>());
  }

  /**
   * Adds an string property to the model.
   * 
   * @param key
   *          the string property key.
   * @param value
   *          the string property initial value.
   * @return the string property.
   */
  protected StringProperty addStringProperty(final String key, final String value) {
    StringProperty property = new StringProperty(key, value);
    mapProperty(property);
    return property;
  }

  /**
   * Adds an boolean property to the model.
   * 
   * @param key
   *          the boolean property key.
   * @param value
   *          the boolean property initial value.
   * @return the boolean property.
   */
  protected BooleanProperty addBooleanProperty(final String key, final boolean value) {
    BooleanProperty property = new BooleanProperty(key, value);
    mapProperty(property);
    return property;
  }

  /**
   * Adds an double property to the model.
   * 
   * @param key
   *          the double property key.
   * @param value
   *          the double property initial value.
   * @return the double property.
   */
  protected DoubleProperty addDoubleProperty(final String key, final double value) {
    DoubleProperty property = new DoubleProperty(key, value);
    mapProperty(property);
    return property;
  }

  /**
   * Adds an float property to the model.
   * 
   * @param key
   *          the float property key.
   * @param value
   *          the float property initial value.
   * @return the float property.
   */
  protected FloatProperty addFloatProperty(final String key, final float value) {
    FloatProperty property = new FloatProperty(key, value);
    mapProperty(property);
    return property;
  }

  /**
   * Adds an integer property to the model.
   * 
   * @param key
   *          the integer property key.
   * @param value
   *          the integer property initial value.
   * @return the integer property.
   */
  protected IntegerProperty addIntegerProperty(final String key, final int value) {
    IntegerProperty property = new IntegerProperty(key, value);
    mapProperty(property);
    return property;
  }

  /**
   * Adds a long property to the model.
   * 
   * @param key
   *          the long property key.
   * @param value
   *          the long property initial value.
   * @return the long property.
   */
  protected LongProperty addLongProperty(final String key, final long value) {
    LongProperty property = new LongProperty(key, value);
    mapProperty(property);
    return property;
  }

  /**
   * Adds a color property to the model.
   * 
   * @param key
   *          the color property key.
   * @param value
   *          the color property initial value.
   * @return the color property.
   */
  protected ColorProperty addColorProperty(final String key, final RGB value) {
    ColorProperty property = new ColorProperty(key, value);
    mapProperty(property);
    return property;
  }

  /**
   * Adds a color bar property to the model.
   * 
   * @param key
   *          the color bar property key.
   * @param value
   *          the color bar property initial value.
   * @return the color bar property.
   */
  protected ColorBarProperty addColorBarProperty(final String key, final ColorBar value) {
    ColorBarProperty property = new ColorBarProperty(key, value);
    mapProperty(property);
    return property;
  }

  /**
   * Adds a font property to the model.
   * 
   * @param key
   *          the font property key.
   * @param value
   *          the font property initial value.
   * @return the font property.
   */
  protected FontProperty addFontProperty(final String key, final FontData[] value) {
    FontProperty property = new FontProperty(key, value);
    mapProperty(property);
    return property;
  }

  /**
   * Adds a file property to the model. The initial value of the object property
   * is <i>null</i>.
   * 
   * @param key
   *          the file property key.
   * @return the file property.
   */
  protected FileProperty addFileProperty(final String key) {
    FileProperty property = new FileProperty(key);
    mapProperty(property);
    return property;
  }

  /**
   * Adds an enum property to the model.
   * 
   * @param key
   *          the enum property key.
   * @param value
   *          the enum property initial value.
   * @return the enum property.
   */
  protected <T extends Enum> EnumProperty<T> addEnumProperty(final String key, final Class<T> klass, final T value) {
    EnumProperty<T> property = new EnumProperty<T>(key, klass, value);
    mapProperty(property);
    return property;
  }

  /**
   * Adds an object property to the model. The initial value of the object
   * property is <i>null</i>.
   * 
   * @param key
   *          the object property key.
   * @param klass
   *          the class of the object.
   * @return the object property.
   */
  protected <T extends Entity> EntityProperty<T> addEntityProperty(final String key, final Class<T> klass) {
    EntityProperty<T> property = new EntityProperty<T>(key, klass);
    mapProperty(property);
    return property;
  }

  /**
   * Adds an object property to the model. The initial value of the object
   * property is <i>null</i>.
   * 
   * @param key
   *          the object property key.
   * @param klass
   *          the class of the object.
   * @param autoDefault
   *          <i>true</i> to auto-set the property value if a single entry
   *          exists in the repository; <i>false</i> not to set.
   * @return the object property.
   */
  protected <T extends Entity> EntityProperty<T> addEntityProperty(final String key, final Class<T> klass,
      final boolean autoDefault) {
    EntityProperty<T> property = new EntityProperty<T>(key, klass);
    mapProperty(property);
    if (autoDefault) {
      property.autoDefault();
    }
    return property;
  }

  public <T extends Entity> OutputEntityProperty addOutputEntityProperty(final String key, final String name) {
    OutputEntityProperty property = new OutputEntityProperty(key);
    property.set(new OutputEntityObject(name, ""));
    mapProperty(property);
    return property;
  }

  /**
   * Adds an object property to the model. The initial value of the object
   * property is <i>null</i>.
   * 
   * @param key
   *          the object property key.
   * @param klass
   *          the class of the object.
   * @return the object property.
   * 
   * @deprecated this method will go away when ObjectProperty becomes an
   *             abstract class.
   */
  @Deprecated
  protected <T> ObjectProperty<T> addObjectProperty(final String key, final Class<T> klass) {
    ObjectProperty<T> property = new ObjectProperty<T>(key, klass);
    mapProperty(property);
    return property;
  }

  /**
   * Adds a string array property to the model. The initial value of the object
   * property is <i>null</i>.
   * 
   * @param key
   *          the string array property key.
   * @return the string array property.
   */
  protected StringArrayProperty addStringArrayProperty(final String key) {
    StringArrayProperty property = new StringArrayProperty(key, String.class);
    mapProperty(property);
    return property;
  }

  /**
   * Adds an entity array property to the model. The initial value of the object
   * property is <i>null</i>.
   * 
   * @param key
   *          the entity property key.
   * @param klass
   *          the class of the entity.
   * @return the entity array property.
   */
  protected <T extends Entity> EntityArrayProperty<T> addEntityArrayProperty(final String key, final Class<T> klass) {
    EntityArrayProperty<T> property = new EntityArrayProperty<T>(key, klass);
    mapProperty(property);
    return property;
  }

  /**
   * Adds an enum array property to the model. The initial value of the object
   * property is <i>null</i>.
   * 
   * @param key
   *          the enum property key.
   * @param klass
   *          the class of the enum.
   * @return the enum array property.
   */
  protected <T extends Enum> EnumArrayProperty<T> addEnumArrayProperty(final String key, final Class<T> klass) {
    EnumArrayProperty<T> property = new EnumArrayProperty<T>(key, klass);
    mapProperty(property);
    return property;
  }

  protected void mapProperty(final Property property) {
    String key = property.getKey();
    _properties.put(key, property);
    property.addPropertyChangeListener(this);
  }

  protected void mapProperties(final Property... properties) {
    for (Property property : properties) {
      mapProperty(property);
    }
  }

  public Property getProperty(final String key) {
    if (!_properties.containsKey(key)) {
      throw new IllegalArgumentException("Model does not contain property: " + key);
    }
    return _properties.get(key);
  }

  public final void addListener(final IModelListener listener) {
    if (!_listeners.contains(listener)) {
      _listeners.add(listener);
    }
  }

  public final void removeListener(final IModelListener listener) {
    _listeners.remove(listener);
  }

  public void propertyChange(final PropertyChangeEvent event) {
    for (IModelListener listener : _listeners.toArray(new IModelListener[0])) {
      listener.propertyChanged(event.getPropertyName());
    }
  }

  public final void removeAllListeners() {
    _listeners.clear();
  }

  public Map<String, String> pickle() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("class", getClass().toString());
    for (Property property : _properties.values()) {
      if (property.getValueObject() != null) {
        String value = property.pickle();
        map.put(property.getKey(), value);
      }
    }
    return map;
  }

  public void unpickle(final Map<String, String> parms) {
    // Initialize the ordered list of keys for unpickling.
    List<String> keyOrderList = new ArrayList<String>();

    List<String> customKeyOrderList = new ArrayList<String>();
    String[] customKeyOrder = getUnpickleKeyOrder();
    if (customKeyOrder != null && customKeyOrder.length > 0) {
      for (String customKey : customKeyOrder) {
        customKeyOrderList.add(customKey);
        keyOrderList.add(customKey);
      }
    }

    // Now loop thru the map to unpickle.
    for (String key : parms.keySet()) {
      // If the key is not already part of list, then add it.
      if (!keyOrderList.contains(key)) {
        keyOrderList.add(key);
      }
    }

    for (String key : keyOrderList) {

      if (key.equals("Unique ID") || key.equals("class")) {
        continue;
      }

      if (_properties.containsKey(key)) {
        String value = parms.get(key);
        if (value != null) {
          _properties.get(key).unpickle(value);
        } else {
          unpickleFailure(parms, key, value);
        }
      } else {
        unpickleFailure(parms, key, "missing");
      }
    }
  }

  /**
   * Display diagnostics if restoring from the properties file fails.
   * 
   * @param parms
   *          the entire map of properties we are restoring
   * @param problemKey
   *          the key we could not find
   * @param problemValue
   *          the value that was null or missing
   */
  private void unpickleFailure(final Map<String, String> parms, final String problemKey, final String problemValue) {

    StringBuilder info = new StringBuilder();

    info.append("Failed to fully restore the model because of a ");
    info.append("problem with key [" + problemKey + "] with a value [" + problemValue + "]\n");
    for (Map.Entry<String, String> parm : parms.entrySet()) {
      info.append("---" + parm.getKey() + " : " + parm.getValue() + "\n");
    }

    ServiceProvider.getLoggingService().getLogger(getClass()).warn(info.toString());
    //throw new IllegalStateException("Could not find key [" + problemKey + "] in properties while unpickling "
    //    + getClass());
  }

  /**
   * @param prefs
   *          the preferences to use for testing if the current model is
   *          restoreable
   * @return true by default, should be overridden if not desired behaviour
   */
  public boolean isRestoreable(final Preferences prefs) {
    return true;
  }

  public Object getValueObject(final String key) {
    if (_properties.containsKey(key)) {
      return _properties.get(key).getValueObject();
    }
    throw new IllegalArgumentException("Model does not contain property: " + key);
  }

  public void setValueObject(final String key, final Object value) {
    if (_properties.containsKey(key)) {
      _properties.get(key).setValueObject(value);
      return;
    }
    throw new IllegalArgumentException("Model does not contain property: " + key);
  }

  public void updateFrom(final IModel model) {
    for (String key : _properties.keySet()) {
      setValueObject(key, model.getValueObject(key));
    }
  }

  /**
   * Disposes of the model resources by clearing out the internal collections.
   */
  public void dispose() {
    _properties.clear();
    _listeners.clear();
  }

  public void dump() {
    for (Property property : _properties.values()) {
      System.out.println("Key=" + property.getKey() + ", Value=" + property.getValueObject());
    }
  }

  public String[] getPropertyKeys() {
    return _properties.keySet().toArray(new String[0]);
  }

  /**
   * Returns an array of keys in the desired order for properties that need to be "unpicked" in a certain order.
   * <p>
   * This method returns an empty array by default (i.e. no custom order), but can be overridden
   * as necessary.
   * 
   * @return an array of the keys in the order of desired "unpickling".
   */
  protected String[] getUnpickleKeyOrder() {
    return new String[0];
  }

  protected void removeAllProperties() {
    for (Property property : _properties.values()) {
      property.removePropertyChangeListener(this);
    }
    _properties.clear();
  }

  protected void removeProperty(final String key) {
    if (_properties.containsKey(key)) {
      _properties.get(key).removePropertyChangeListener(this);
      _properties.remove(key);
    }
  }
}
