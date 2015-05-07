/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.property;


import java.io.File;


/**
 * A property for containing a file value.
 * The file value is an <code>File</code> object.
 */
public class FileProperty extends Property {

  /** The file value. */
  private File _value;

  /**
   * Constructs a file property.
   * 
   * @param key the property key.
   */
  public FileProperty(final String key) {
    super(key);
    _value = null;
  }

  /**
   * Gets the file value.
   * 
   * @return the file value.
   */
  public File get() {
    return _value;
  }

  /**
   * Sets the file value.
   * 
   * @param value the file value to set.
   */
  public void set(final File value) {
    firePropertyChange(_value, _value = value);
  }

  @Override
  public Object getValueObject() {
    return _value;
  }

  @Override
  public void setValueObject(final Object valueObject) {
    //Note: If a file is never specified for the property, the value will be null.
    try {
      if (valueObject != null && valueObject instanceof File) {
        set((File) valueObject);
      } else {
        File file = new File(valueObject == null ? "" : valueObject.toString());
        set(file);
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Not a file: " + valueObject);
    }
  }

  public boolean isNull() {
    return _value == null;
  }

  public boolean canRead() {
    return _value.canRead();
  }

  public boolean canWrite() {
    return _value.canWrite();
  }

  public boolean canExecute() {
    return _value.canExecute();
  }

  public boolean exists() {
    return _value.exists();
  }

  @Override
  public String toString() {
    return "" + get();
  }

  @Override
  public String pickle() {
    return getValueObject().toString();
  }

  @Override
  public void unpickle(final String value) {
    setValueObject(value);
  }
}
