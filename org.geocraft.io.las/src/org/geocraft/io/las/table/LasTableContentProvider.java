/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.las.table;


import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.geocraft.io.las.ILasModelListViewer;
import org.geocraft.io.las.LasColumnModelArrayProperty;
import org.geocraft.io.las.LasMnemonicDescriptionModel;
import org.geocraft.ui.form2.ITableContentProvider;


public class LasTableContentProvider implements ITableContentProvider, ILasModelListViewer {

  private LasColumnModelArrayProperty _modelList;

  private TableViewer _viewer;

  public LasTableContentProvider(TableViewer viewer, LasColumnModelArrayProperty prop) {
    _viewer = viewer;
    _modelList = prop;
    _modelList.addChangeListener(this);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  /**
   * @param inputElement  
   */
  @Override
  public Object[] getElements(Object inputElement) {
    if (_modelList != null) {
      for (LasMnemonicDescriptionModel m : _modelList.get()) {
        m.addListener(_modelList);
      }
      return _modelList.get().toArray();
    }
    return new Object[0];
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  @Override
  public void dispose() {
    _modelList.removeChangeListener(this);

  }

  /**
   * @param viewer 
   * @param oldInput  
   */
  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    // TODO:
  }

  @Override
  public void addModel(LasMnemonicDescriptionModel model) {
    _viewer.add(model);

  }

  /* (non-Javadoc)
   * @see org.geocraft.io.las.ILasModelListViewer#removeModel(org.geocraft.io.las.LasMnemonicDescriptionModel)
   */
  @Override
  public void removeModel(LasMnemonicDescriptionModel model) {
    _viewer.remove(model);
  }

  /* (non-Javadoc)
   * @see org.geocraft.io.las.ILasModelListViewer#updateModel(org.geocraft.io.las.LasMnemonicDescriptionModel)
   */
  @Override
  public void updateModel(LasMnemonicDescriptionModel model) {
    _viewer.update(model, null);
    _viewer.refresh();
  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.form2.ITableContentProvider#setInput(java.lang.Object)
   */
  @Override
  public void setInput(Object o) {
    _modelList.setValueObject(o);
  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.form2.ITableContentProvider#getInput()
   */
  @Override
  public Object getInput() {
    return _modelList;
  }

}
