/*
 * Copyright (C) ConocoPhillips 2010 All Rights Reserved. 
 */
package org.geocraft.ui.viewer;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.IWorkbenchWindow;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.core.session.SessionManager;


/**
 * Registry of active viewers (actually, viewer parts) and the IDs of the
 * workbench window containing the viewers.
 * @author hansegj
 *
 */
public class ActiveViewerRegistry {

  private static ActiveViewerRegistry singleton = null;

  private ActiveViewerRegistry() {
    // The empty constructor.
  }

  /**
   * Get the singleton instance of this class. If the registry class doesn't
   * exist, create it.
   */
  public static ActiveViewerRegistry getInstance() {
    if (singleton == null) {
      singleton = new ActiveViewerRegistry();
      singleton._registry = new HashMap<Integer, Object>();
      singleton._windowIDs = new HashMap<Integer, String>();
    }

    return singleton;
  }

  /** The logger. */
  private static final ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(ActiveViewerRegistry.class);

  /** List of registered viewer parts. key is the part's unique ID */
  HashMap<Integer, Object> _registry;

  /** Map between a registered viewer part and its containing workbench window ID.
   *  key=viewer part's unique ID, value = workbench window ID
   */
  HashMap<Integer, String> _windowIDs;

  public Map<Integer, Object> getRegisteredViewerParts() {
    return _registry;
  }

  /**
   * Register an active viewer part
   * @param viewerPart Viewer's part
   */
  public void registerViewerPart(Object viewerPart) {
    //Note: The hash code should be distinct integers for distinct objects
    int key = viewerPart.hashCode();
    _registry.put(key, viewerPart);
    IWorkbenchWindow win = viewerPart instanceof AbstractViewerPart ? ((AbstractViewerPart) viewerPart).getSite()
        .getWorkbenchWindow() : SessionManager.getInstance().findViewerWindow(viewerPart);
    _windowIDs.put(key, win != null ? SessionManager.getInstance().getWorkbenchWindowID(win) : "");
  }

  /**
   * Unregister a registered viewer part
   * @param hashcode Unique ID of the viewer part instance
   */
  public void unregisterViewerPart(int key) {
    _registry.remove(key);
    _windowIDs.remove(key);
  }

  /**
   * Get a registered viewer part
   * @param key Unique ID of the viewer part instance
   * @return The registered viewer part
   */
  public IViewerPart getRegisteredViewerPart(int key) {
    return (IViewerPart) _registry.get(key);
  }

  /**
   * Get the ID of the workbench window containing a registered viewer part.
   * @param key Unique ID of the viewer part instance
   * @return If a workbench window is found containing the registered viewer part, its ID; otherwise, the empty string
   */
  public String getWindowID(int key) {
    String wid = _windowIDs.get(key);
    //Note: If the window ID is the empty string, it could not be determined when
    //the viewer part was registered because its containing window had not been
    //created yet.
    if (wid.equals("")) {
      Object viewerPart = _registry.get(key);
      //get the workbench window containing the viewer part
      IWorkbenchWindow win = SessionManager.getInstance().findViewerWindow(viewerPart);
      wid = win != null ? SessionManager.getInstance().getWorkbenchWindowID(win) : "";
      _windowIDs.put(key, wid);
    }
    return wid;
  }

  /**
   * Clear the registry, i.e., remove all registered viewers
   */
  public void clearRegistry() {
    _registry.clear();
    _windowIDs.clear();
  }

  public void dumpRegistry() {
    StringBuffer sbuf = new StringBuffer("Active viewer's registry:\n");
    Set<Integer> keys = _registry.keySet();
    Iterator<Integer> iter = keys.iterator();
    while (iter.hasNext()) {
      Integer key = iter.next();
      String name = _registry.get(key).getClass().getName();
      String windowID = getWindowID(key);
      sbuf.append("\nViewer: " + name + ", windowID: " + windowID + ", Instance: " + key + "\n");
    }
    LOGGER.debug(sbuf.toString());
  }
}
