/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.datatypes;


/**
 * This class is an enumeration of the load states of a domain object.
 * The 2 states are defined as follows:
 * <i>GHOST</i>: the object is in a partial state. Its fields will only be
 * loaded when one of them is accessed.
 * <i>LOADED</i>: the object is in its final, fully loaded state.
 */
public enum LoadStatus {
  /** The entity has not been loaded. */
  GHOST,
  /** The entity is currently being loaded. */
  LOADING,
  /** The entity has been fully loaded. */
  LOADED
}
