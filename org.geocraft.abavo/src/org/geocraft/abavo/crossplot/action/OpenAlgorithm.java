/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.action;


import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPartSite;
import org.geocraft.algorithm.StandaloneAlgorithmEditorInput;
import org.geocraft.algorithm.StandaloneAlgorithmRegistry;
import org.geocraft.core.service.ServiceProvider;


public class OpenAlgorithm extends Action {

  private final Class _algorithmClass;

  private final IWorkbenchPartSite _site;

  public OpenAlgorithm(final Class algorithmClass, final IWorkbenchPartSite site) {
    _algorithmClass = algorithmClass;
    _site = site;
  }

  @Override
  public void run() {
    try {
      StandaloneAlgorithmEditorInput input = StandaloneAlgorithmRegistry.getInstance().getEditorInput(
          _algorithmClass.getName());
      _site.getPage().openEditor(input, "org.geocraft.algorithm.StandaloneAlgorithmEditor");
    } catch (Exception ex) {
      ServiceProvider.getLoggingService().getLogger(getClass()).warn("Cannot display algorithm editor", ex);
    }
  }
}
