/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.abavo.ellipse;


import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.geocraft.abavo.ellipse.EllipseRegionsModel;
import org.geocraft.abavo.ellipse.EllipseRegionsModelEvent;
import org.geocraft.abavo.ellipse.EllipseRegionsModelListener;
import org.geocraft.abavo.ellipse.RegionsBoundary;
import org.geocraft.abavo.ellipse.RegionsBoundaryModel;


public class RegionsBoundaryTableContentProvider implements IStructuredContentProvider, EllipseRegionsModelListener {

  EllipseRegionsModel _localModel;

  TableViewer _viewer;

  public RegionsBoundaryTableContentProvider() {
    // No action required.
  }

  public Object[] getElements(final Object inputElement) {
    EllipseRegionsModel model = (EllipseRegionsModel) inputElement;
    RegionsBoundary[] ids = RegionsBoundary.values();
    RegionsBoundaryModel[] regionBoundaries = new RegionsBoundaryModel[ids.length];
    for (int i = 0; i < ids.length; i++) {
      RegionsBoundary id = ids[i];
      regionBoundaries[i] = model.getRegionsBoundaryModel(id);
    }
    return regionBoundaries;
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
