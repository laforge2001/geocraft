/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.las;


import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.geocraft.core.model.IModelListener;
import org.geocraft.core.model.property.ObjectListProperty;
import org.geocraft.core.model.validation.IValidation;


public class LasColumnModelArrayProperty extends ObjectListProperty<LasMnemonicDescriptionModel> implements
    IModelListener {

  private IStatus[] _errorState;

  /**
   * @param key
   * @param klass
   */
  public LasColumnModelArrayProperty(String key, Class<LasMnemonicDescriptionModel> klass) {
    super(key, klass);
  }

  private Set<ILasModelListViewer> _listeners = new HashSet<ILasModelListViewer>();

  public void addModel(LasMnemonicDescriptionModel model) {
    add(model);
    for (ILasModelListViewer v : _listeners) {
      v.addModel(model);
    }
  }

  public void removeModel(LasMnemonicDescriptionModel model) {
    remove(model);
    for (ILasModelListViewer v : _listeners) {
      v.removeModel(model);
    }
  }

  public void updateModel(LasMnemonicDescriptionModel model) {
    for (ILasModelListViewer v : _listeners) {
      v.updateModel(model);
    }
  }

  public IStatus[] getErrorState() {
    return _errorState == null ? new IStatus[0] : _errorState;
  }

  public void addChangeListener(ILasModelListViewer viewer) {
    _listeners.add(viewer);
  }

  public void removeChangeListener(ILasModelListViewer viewer) {
    _listeners.remove(viewer);
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.property.Property#pickle()
   */
  @Override
  public String pickle() {
    // TODO Auto-generated method stub
    return "pickle";
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.property.Property#unpickle(java.lang.String)
   */
  @Override
  public void unpickle(String value) {
    // TODO Auto-generated method stub

  }

  public void validate(IValidation results) {
    for (LasMnemonicDescriptionModel m : get()) {
      m.validate(results);
    }
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.IModelListener#propertyChanged(java.lang.String)
   */
  @Override
  public void propertyChanged(String key) {
    firePropertyChange(null, get());
  }
}
