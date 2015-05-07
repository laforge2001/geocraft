/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.abavo.ellipse;


import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.geocraft.abavo.ellipse.EllipseModel;
import org.geocraft.abavo.ellipse.EllipseRegionsModel;
import org.geocraft.abavo.ellipse.EllipseRegionsModelEvent;
import org.geocraft.abavo.ellipse.EllipseRegionsModelListener;
import org.geocraft.abavo.ellipse.EllipseRegionsModel.EllipseType;


public class EllipseTableContentProvider implements IStructuredContentProvider, EllipseRegionsModelListener {

  private EllipseRegionsModel _localModel;

  private TableViewer _viewer;

  public EllipseTableContentProvider() {
    // No action required.
  }

  public Object[] getElements(final Object inputElement) {
    EllipseRegionsModel model = (EllipseRegionsModel) inputElement;
    EllipseModel[] ellipses = new EllipseModel[2];
    ellipses[0] = model.getEllipseModel(EllipseType.Background);
    ellipses[1] = model.getEllipseModel(EllipseType.Maximum);
    return ellipses;
  }

  public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
    _viewer = (TableViewer) viewer;
    if (oldInput != null) {
      ((EllipseRegionsModel) oldInput).removeEllipseModelListener(this);
    }
    if (newInput != null) {
      ((EllipseRegionsModel) newInput).addEllipseModelListener(this);
      _localModel = (EllipseRegionsModel) newInput;
    }
  }

  public void dispose() {
    if (_localModel != null) {
      _localModel.removeEllipseModelListener(this);
    }
  }

  public void ellipseModelUpdated(final EllipseRegionsModelEvent event) {
    if (_viewer != null) {
      _viewer.refresh();
    }
  }
}
