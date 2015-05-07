/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.algorithm;


import java.util.Map;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.algorithm.IAlgorithmsService;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.internal.algorithm.StandaloneAlgorithmEditor;


/** Access methods to the registry of active standalone algorithms */
public class AlgorithmsService implements IAlgorithmsService {

  /** The logger. */
  private static final ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(AlgorithmsService.class);

  public AlgorithmsService() {
    LOGGER.debug("Algorithms service created.");
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.service.algorithms.IAlgorithmsService#getRegisteredAlgorithms()
   */
  @Override
  public Map<Integer, Object> getRegisteredAlgorithms() {
    return ActiveAlgorithmRegistry.getInstance().getRegisteredAlgorithms();
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.service.algorithms.IAlgorithmsService#getAlgorithm(int)
   */
  @Override
  public Map<String, String> getAlgorithmParms(int key) {
    Object editor = ActiveAlgorithmRegistry.getInstance().getRegisteredAlgorithm(key);
    //return StandaloneAlgorithm
    return ((StandaloneAlgorithmEditor) editor).getAlgorithm().pickle();
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.service.algorithms.IAlgorithmsService#getAlgorithmName(int)
   */
  @Override
  public String getAlgorithmName(int key) {
    Object editor = ActiveAlgorithmRegistry.getInstance().getRegisteredAlgorithm(key);
    return ((StandaloneAlgorithmEditor) editor).getAlgorithmDescription().getName();
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.service.algorithms.IAlgorithmsService#getAlgorithmClassName(int)
   */
  @Override
  public String getAlgorithmClassName(int key) {
    Object editor = ActiveAlgorithmRegistry.getInstance().getRegisteredAlgorithm(key);
    return ((StandaloneAlgorithmEditor) editor).getAlgorithmDescription().getClassName();
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.service.algorithms.IAlgorithmsService#get(int)
   */
  @Override
  public Object getDescription(final int key) {
    Object editor = ActiveAlgorithmRegistry.getInstance().getRegisteredAlgorithm(key);
    //return StandaloneAlgorithmDescription
    return ((StandaloneAlgorithmEditor) editor).getAlgorithmDescription();
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.service.algorithm.IAlgorithmsService#getWindowID(int)
   */
  @Override
  public String getWindowID(int key) {
    return ActiveAlgorithmRegistry.getInstance().getWindowID(key);
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.service.algorithms.IAlgorithmsService#add(java.lang.Object)
   */
  @Override
  public int add(final Object algorithmEditor) {
    return ActiveAlgorithmRegistry.getInstance().registerAlgorithm(algorithmEditor);
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.service.algorithms.IAlgorithmsService#remove(java.lang.String)
   */
  @Override
  public void remove(final int key) {
    ActiveAlgorithmRegistry.getInstance().unregisterAlgorithm(key);
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.service.algorithms.IAlgorithmsService#deactivate(int)
   */
  @Override
  public void deactivate(final int key) {
    Object editor = ActiveAlgorithmRegistry.getInstance().getRegisteredAlgorithm(key);
    //remove(key);
    ((StandaloneAlgorithmEditor) editor).close(false);
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.service.algorithms.IAlgorithmsService#activate(java.lang.String)
   */
  @Override
  public int activate(String klass, Map<String, String> parms) {
    StandaloneAlgorithmEditorInput input = StandaloneAlgorithmRegistry.getInstance().getEditorInput(klass);
    if (input == null) {
      ServiceProvider.getLoggingService().getLogger(getClass()).warn("Could not find algorithm: " + klass);
      return -1;
    }
    try {
      IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input,
          "org.geocraft.algorithm.StandaloneAlgorithmEditor");

      // Wait until the editor has been opened to unpickle, so that property changes can update the UI,
      // depending on the algorithm.
      input.getAlgorithm().unpickle(parms);

      return add(editor);
    } catch (Exception pie) {
      ServiceProvider.getLoggingService().getLogger(getClass()).warn("Cannot display standalone algorithm editor", pie);
    }
    return -1;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.service.algorithms.IAlgorithmsService#dumpRegistry()
   */
  @Override
  public void dumpRegistry() {
    ActiveAlgorithmRegistry.getInstance().dumpRegistry();
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.service.algorithm.IAlgorithmsService#removeAll()
   */
  @Override
  public void removeAll() {
    ActiveAlgorithmRegistry.getInstance().clearRegistry();
  }
}
