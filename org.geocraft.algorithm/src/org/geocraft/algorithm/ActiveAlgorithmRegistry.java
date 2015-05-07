/*
 * Copyright (C) ConocoPhillips 2010 All Rights Reserved.
 */
package org.geocraft.algorithm;


import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.core.session.SessionManager;
import org.geocraft.internal.algorithm.StandaloneAlgorithmEditor;


/**
 * Registry of active standalone algorithms. Contains the algorithm
 * editors and the ID of their containing workbench window.
 * @author hansegj
 *
 */
public class ActiveAlgorithmRegistry {

  private static ActiveAlgorithmRegistry singleton = null;

  private ActiveAlgorithmRegistry() {
    // The empty constructor.
  }

  /**
   * Get the singleton instance of this class. If the registry class doesn't
   * exist, create it.
   */
  public static ActiveAlgorithmRegistry getInstance() {
    if (singleton == null) {
      singleton = new ActiveAlgorithmRegistry();
      singleton._registry = new ConcurrentHashMap<Integer, Object>();
      singleton._windowIDs = new ConcurrentHashMap<Integer, String>();
    }

    return singleton;
  }

  /** The logger. */
  private static final ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(ActiveAlgorithmRegistry.class);

  /** List of algorithm editors. key=editor hash code, value=algorithm editor */
  ConcurrentHashMap<Integer, Object> _registry;

  /** Map between a registered algorithm and its containing workbench window ID.
   *  key=algorithm editor hash code, value = workbench window ID
   */
  ConcurrentHashMap<Integer, String> _windowIDs;

  public Map<Integer, Object> getRegisteredAlgorithms() {
    return _registry;
  }

  /**
   * Register an active standalone algorithm
   * @param input Algorithm's input frame
   * @return Registration key
   */
  public int registerAlgorithm(Object algorithmEditor) {
    //Note: algorithmEditor of type StandaloneAlgorithmEditor or IEditorPart
    //Note: The hash code should be distinct integers for distinct objects
    int key = algorithmEditor.hashCode();
    _registry.put(key, algorithmEditor);
    IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    _windowIDs.put(key, SessionManager.getInstance().getWorkbenchWindowID(win));
    return key;
  }

  /**
   * Unregister a registered standalone algorithm
   * @param key Unique ID of the standalone algorithm instance
   */
  public void unregisterAlgorithm(int key) {
    _registry.remove(key);
    _windowIDs.remove(key);
  }

  public Object getRegisteredAlgorithm(int key) {
    return _registry.get(key);
  }

  public String getWindowID(int key) {
    return _windowIDs.get(key);
  }

  /**
   * Clear the registry, i.e., remove all registered standalone algorithms
   */
  public void clearRegistry() {
    _registry.clear();
    _windowIDs.clear();
  }

  public void dumpRegistry() {
    StringBuffer sbuf = new StringBuffer("Active standalone algorithm's registry:\n");
    Set<Integer> keys = _registry.keySet();
    Iterator<Integer> iter = keys.iterator();
    while (iter.hasNext()) {
      Integer key = iter.next();
      StandaloneAlgorithmEditor algorithmEditor = (StandaloneAlgorithmEditor) _registry.get(key);
      StandaloneAlgorithm algorithm = algorithmEditor.getAlgorithm();
      String name = algorithm.getClass().getName();
      String windowID = getWindowID(key);
      sbuf.append("\nAlgorithm: " + name + ", window ID: " + windowID + ", Instance: " + key + "\n");
      //      StandaloneAlgorithmDescription desc = algorithm.getAlgorithmDescription();
      //      sbuf.append(desc.dumpDescription());
    }
    LOGGER.debug(sbuf.toString());
  }
}
