/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.core.service.message;


public interface Topic {

  /** Shell command has been executed. */
  public static final String SHELL_COMMAND = "ShellCommand";

  /** Selection of an array of 3d points. */
  public static final String POINTS3D_SELECTION = "Points3dSelection";

  /** Selection of an object in the repository. */
  public static final String REPOSITORY_OBJECT_SELECTED = "RepositoryObjectSelected";

  /**
   * Topic of messages published when one or more objects are added to the repository.
   * <p>
   * The message object associated with this topic is a <code>HashMap</code> of the objects, mapped by their variable names.
   */
  public static final String REPOSITORY_OBJECTS_ADDED = "RepositoryObjectsAdded";

  /**
   * Topic of messages published when one or more objects are removed from the repository.
   * <p>
   * The message object associated with this topic is a <code>HashMap</code> of the objects, mapped by their variable names.
   */
  public static final String REPOSITORY_OBJECTS_REMOVED = "RepositoryObjectsRemoved";

  /**
   * Topic of messages published when an object is updated to its underlying datastore.
   * <p>
   * The message object associated with this topic is the updated object itself.
   */
  public static final String REPOSITORY_OBJECT_UPDATED = "RepositoryObjectUpdated";

  /** Repository tree node was selected */
  public static final String REPOSITORY_NODE_SELECTED = "RepositoryNodeSelected";

  /**
   * Message sent when the repository has been cleared and data entities belonging to 
   * (possibly) another project are to be loaded. For example, when restoring state.
   */
  public static final String CLOSE_ALL_PROJECTS = "CloseAllProjects";

  /** Property tree node was selected */
  public static final String PROPERTY_NODE_SELECTED = "PropertyNodeSelected";

  /** User has selected something in a viewer. */
  public static final String DATA_SELECTION = "DataSelection";

  /** User has deselected something in a viewer. */
  public static final String DATA_DESELECTION = "DataDeselection";

  /** Viewable bounds in the section viewer changed */
  public static final String VIEWABLE_BOUNDS_UPDATED = "ViewableBoundsUpdated";

  /**
   * Location of a cursor.
   * <p>
   * The data for this topic must be a <code>CursorLocation</code> object.
   */
  public static final String CURSOR_LOCATION = "CursorLocation";

  /** Location of a cursor selection. */
  public static final String CURSOR_SELECTION_LOCATION = "CursorSelectionLocation";

  /** Trace section has been selected (likely from a map view). */
  public static final String TRACE_SECTION_SELECTED = "TraceSectionSelected";

  /** Trace section has been displayed (likely in a section view). */
  public static final String TRACE_SECTION_DISPLAYED = "TraceSectionDisplayed";

  /** Data has been received from a remote application. */
  public static final String REMOTE_DATA_RECEIVED = "Remote Data Received";

  /** Acknowledge data was received from remote application. */
  public static final String REMOTE_DATA_ACKNOWLEDGED = "Remote Data Acknowledged";
}