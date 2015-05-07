/*
 * Copyright (C) ConocoPhillips 2010 All Rights Reserved. 
 */
package org.geocraft.core.service.viewer;


import java.util.ArrayList;
import java.util.Map;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;


/** Access methods to the registry of active viewers */
public interface IViewersService {

  /**
   * Get list of all registered viewer parts. key is the unique ID of the viewer part
   * instance (there may be many active instances of the same viewer part); value =
   * viewer part.
   * 
   * @return Map of all active viewer parts.
   */
  Map<Integer, Object> getRegisteredViewerParts();

  /**
   * Get the viewers's display properties.
   * 
   * @param viewer Viewer
   * @return Display properties of the specified viewer
   */
  Map<String, String> getViewerParms(Object viewer);

  /**
   * Get the viewers's name.
   * 
   * @param viewer Viewer
   * @return Name of the specified viewer
   */
  String getViewerName(Object viewer);

  /**
   * Get the viewer's class name.
   * 
   * @param viewer Viewer
   * @return Class name of the specified viewer
   */
  String getViewerClassName(Object viewer);

  /**
   * Get the viewer part's class name
   * 
   * @param key Map key of the viewer part instance
   * @return Class name of the viewer part
   */
  String getViewerPartClassName(int key);

  /**
   * Get the registered viewer part's ID
   * 
   * @param key Map key of the viewer part instance
   * @return The ID of the registered viewer part
   */
  String getViewerPartId(int key);

  /**
   * Get a viewer part's ID
   * @param viewerPart The viewer part (to be registered)
   * @return The ID of the viewer part
   */
  String getViewerPartID(Object viewerPart);

  /**
   * Get the viewer part's ID
   * @param viewPart Viewer part
   * @return Viewer part's ID
   */
  public String getViewerPartId(IViewPart viewPart);

  /**
   * Get the viewer part's unique ID.
   * 
   * @param key Map key of the viewer part instance
   * @return Viewer's unique ID
   */
  int getViewerPartUniqueId(int key);

  /**
   * Get the viewer part's name
   * @param key Map key of the viewer part instance
   * @return Viewer part's name
   */
  public String getViewerPartName(int key);

  /**
   * Get the viewer part's viewer
   * @param viewPart The viewer part
   * @return The part's viewer (IViewer)
   */
  public Object getViewer(IViewPart viewPart);

  /**
   * Get the viewer part's viewers
   * @param viewPart The viewer part
   * @return List of part's viewers (IViewer)
   */
  public Object[] getViewers(IViewPart viewPart);

  /**
   * Get the description of a registered viewer.
   * 
   * @param key
   *          Unique ID of the viewer instance
   * @return Descriptor of the specified viewer
   */
  Object getDescription(int key);

  /**
   * Get the ID of the workbench window containing the viewer part
   * @param key Unique ID of the algorithm instance
   * @return The ID of the workbench window containing the viewer part
   */
  String getViewerPartWindowID(int key);

  /**
   * Add (register) an active viewer part to the registry which contains one or
   * more associated viewers.
   * 
   * @param object Viewer part
   */
  void add(Object viewerPart);

  /**
   * Remove (unregister) a viewer part from the (active viewers) registry.
   * 
   * @param key Unique ID of the viewer part instance
   */
  void remove(int key);

  /**
   * Deactivate a registered viewer part, i.e., close it.
   * 
   * @param key Unique ID of the viewer part instance
   */
  void deactivate(int key);

  /**
   * Activate a single viewer part, update its model and register it
   * 
   * @param klass Full class name of the viewer part
   * @param parms (key,value) pairs of viewer model parameters
   * @param label New part name
   * @return The viewer activated (IViewer object)
   */
  Object activateViewer(String klass, Map<String, String> parms, String label);

  /**
   * Active all the viewers associate with a viewer part and register it
   * @param klass Full class name of the viewer part
   * @param vprops List of (key,value) pairs of each viewer's parameters
   * @return List of viewers activated (IViewer objects)
   */
  Object[] activateViewers(String klass, ArrayList<Map<String, String>> vprops);

  /**
   * Add a renderer to a created viewer
   * @param viewer Created IViewer
   * @param klass Qualified class of the renderer to be added
   * @param props Properties of the renderer to be added
   * @param uniqueId Unique ID of the rendered entity
   */
  void addRenderer(Object viewer, String klass, Map<String, String> props, String uniqueId);

  /**
   * Get the state of an active viewer and its associated renderers
   * 
   * @param key Unique ID of the viewer part instance
   * @return XML for the viewer's session state
   */
  ArrayList<String> getSessionState(int key);

  /**
   * Initialize the list of viewers being restored
   */
  void initViewers();

  /**
   * Add viewer to list of viewers being restored
   * @param viewer The IViewer to add to the list
   */
  void addViewer(Object viewer);

  /**
   * Initialize the list of layers (in a viewer's tree viewer) being updated
   */
  void initLayers();

  /**
   * Update a viewer's model properties
   * @param viewer The IViewer to be updated
   * @param vprops The viewer's model properties
   */
  void updateViewerModel(Object viewer, Map<String, String> vprops);

  /**
   * Update all restored viewer's folder layers (i.e., checkbox tree in the Layers tab)
   * @param desc Saveset descriptor containing state to be restored
   */
  void updateAllViewerLayers(Object desc, Object windowIds);

  /**
   * Update a viewer from its model which will cause a redraw
   * @param viewer The viewer to be redrawn
   */
  void updateFromModel(Object viewer);

  /**
   * Check if a workbench window is the detached plot window
   * @param win The workbench window to check
   * @return true if window is the plot window; otherwise, false
   */
  boolean isPlotWindow(IWorkbenchWindow win);

  /**
   * Set the plot workbench window
   * @param win The plot workbench window
   */
  void setPlotWindow(IWorkbenchWindow win);

  void dumpRegistry();

  void removeAll();
}
