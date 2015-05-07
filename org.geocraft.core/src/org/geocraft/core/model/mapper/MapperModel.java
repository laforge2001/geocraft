/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.mapper;


import java.util.Map;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.geocraft.core.model.Model;


/**
 * The abstract base class for models containing mapper properties.
 * Sub-classes need to implement the methods related to generation
 * and updating of the uniqueID, as well as the validate() method
 * related to I/O.
 */
public abstract class MapperModel extends Model {

  /** The string constant representing the key of the unique ID property. */
  public static final String UNIQUE_ID = "Unique ID";

  private IOMode _ioMode = IOMode.INPUT;

  /**
   * The default zero-argument constructor for OSGI.
   */
  public MapperModel() {
    super();
  }

  /**
   * Returns the unique ID of the mapped entity.
   * The unique ID uniquely identifies an object from a datastore
   * and allows the system to prevent the loading of duplicates.
   * This must be unique across all datastores (i.e. two
   * entities cannot have the same unique ID, even if they
   * originate from different datastores. Thus, it is
   * suggested that the unique ID contain the datastore
   * name to help ensure uniqueness.
   */
  public abstract String getUniqueId();

  /**
   * override this if there is a need to save a non-default unique id
   * (ie. for 2d entities)
   * 
   * @return true by default unless overridden
   */
  public boolean useDefaultId() {
    return true;
  }

  public String getCustomId() {
    return "";
  }

  /**
   * Updates the unique identifier in the model.
   * 
   * @param name the name used in updating the unique identifier.
   */
  public abstract void updateUniqueId(final String name);

  @Override
  public Map<String, String> pickle() {
    Map<String, String> map = super.pickle();
    map.put(UNIQUE_ID, getUniqueId());
    return map;
  }

  /**
   * Returns a flag indicating if the current settings of
   * the model represent an existing entry in the underlying
   * datastore.
   * @return <i>true</i> if entry exists; <i>false</i> if not.
   */
  public abstract boolean existsInStore();

  /**
   * Returns a flag indicating if the current settings of
   * the model, along with the specified name, represent an
   * existing entry in the underlying datastore.
   * @return <i>true</i> if entry exists; <i>false</i> if not.
   */
  public abstract boolean existsInStore(String name);

  public IOMode getIOMode() {
    return _ioMode;
  }

  public void setIOMode(final IOMode ioMode) {
    _ioMode = ioMode;
  }

  /**
   * Validate the proposed name for a new entry in the underlying datastore.
   * Datastores often have naming restrictions (length, special characters, etc)
   * and this method allows for implementation of the naming logic.
   * 
   * @param proposedName the name of the proposed entry.
   * @return the validation status of the proposed name.
   */
  public IStatus validateName(final String proposedName) {
    if (proposedName == null || proposedName.isEmpty()) {
      return ValidationStatus.error("Name must contain as least 1 character.");
    }
    String[] invalidChars = { "-", "+", "/", "\\", ",", "(", ")" };
    for (String currentChar : invalidChars) {
      if (proposedName.contains(currentChar)) {
        return ValidationStatus.error("The following character in the name is invalid:" + currentChar);
      }
    }
    return ValidationStatus.ok();
  }
}
