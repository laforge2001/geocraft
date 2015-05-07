/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.algorithm;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;


public class StandaloneAlgorithmEditorInput implements IEditorInput {

  private final IStandaloneAlgorithmDescription _algorithmDescription;

  private final StandaloneAlgorithm _algorithm;

  /**
   * No argument constructor that is required to instantiate an extension. 
   */
  public StandaloneAlgorithmEditorInput(final IStandaloneAlgorithmDescription algorithmDescription) {
    _algorithmDescription = algorithmDescription;
    _algorithm = _algorithmDescription.createAlgorithm();
  }

  @Override
  public boolean exists() {
    return true;
  }

  public IStandaloneAlgorithmDescription getAlgorithmDescription() {
    return _algorithmDescription;
  }

  public StandaloneAlgorithm getAlgorithm() {
    return _algorithm;
  }

  public String getName() {
    return _algorithmDescription.getName();
  }

  public String getToolTipText() {
    return _algorithmDescription.getName();
  }

  @Override
  public Object getAdapter(final Class adapter) {
    return null;
  }

  @Override
  public ImageDescriptor getImageDescriptor() {
    return null;
  }

  @Override
  public IPersistableElement getPersistable() {
    return new IPersistableElement() {

      @Override
      public String getFactoryId() {
        return "org.geocraft.algorithm.StandaloneAlgorithmFactory";
      }

      @Override
      public void saveState(final IMemento memento) {
        memento.putString("algorithmClassName", _algorithmDescription.getClassName());
      }
    };
  }

}
