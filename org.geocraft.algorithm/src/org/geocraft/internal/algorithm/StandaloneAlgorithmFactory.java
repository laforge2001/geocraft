/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.algorithm;


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.geocraft.algorithm.StandaloneAlgorithmEditorInput;
import org.geocraft.algorithm.StandaloneAlgorithmRegistry;


public class StandaloneAlgorithmFactory implements IElementFactory {

  @Override
  public IAdaptable createElement(final IMemento memento) {
    String algorithmClassName = memento.getString("algorithmClassName");
    StandaloneAlgorithmEditorInput input = StandaloneAlgorithmRegistry.getInstance().getEditorInput(algorithmClassName);
    return input;
  }

}
