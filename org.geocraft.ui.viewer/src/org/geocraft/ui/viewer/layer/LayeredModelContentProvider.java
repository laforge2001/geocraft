/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.layer;


import java.util.Map;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.TreeItem;
import org.geocraft.core.service.ServiceProvider;


public class LayeredModelContentProvider implements ICheckStateListener, IStructuredContentProvider,
    ITreeContentProvider, ILayeredModelListener, MouseTrackListener {

  /** The reference to the viewer for which content is provided. */
  private CheckboxTreeViewer _treeViewer;

  /** The layered model. */
  private final ILayeredModel _model;

  public LayeredModelContentProvider(final ILayeredModel model) {
    _model = model;
    _model.addListener(this);
  }

  public ILayeredModel getModel() {
    return _model;
  }

  public void dispose() {
    // Nothing to do.
  }

  public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
    if (_treeViewer != null) {
      _treeViewer.getTree().removeMouseTrackListener(this);
    }
    _treeViewer = (CheckboxTreeViewer) viewer;
    _treeViewer.getTree().addMouseTrackListener(this);
    if (viewer instanceof CheckboxTreeViewer) {
      ((CheckboxTreeViewer) viewer).addCheckStateListener(this);
    }
    if (oldInput != null) {
      removeListenerFrom((ILayeredModel) oldInput);
    }
    if (newInput != null) {
      addListenerTo((ILayeredModel) newInput);
    }
  }

  public Object[] getElements(final Object element) {
    return getChildren(element);
  }

  public Object[] getChildren(final Object element) {
    if (element instanceof IViewLayer) {
      IViewLayer layer = (IViewLayer) element;
      return _model.getChildren(layer);
    } else if (element instanceof ILayeredModel) {
      return _model.getRootLayers();
    }
    return new Object[0];
  }

  public Object getParent(final Object element) {
    if (element instanceof IViewLayer) {
      IViewLayer layer = (IViewLayer) element;
      return _model.getParent(layer);
    }
    return null;
  }

  public boolean hasChildren(final Object element) {
    return getChildren(element).length > 0;
  }

  protected void addListenerTo(final ILayeredModel model) {
    model.addListener(this);
  }

  protected void removeListenerFrom(final ILayeredModel model) {
    model.removeListener(this);
  }

  /**
   * Refreshes the viewer.
   * @param viewer the viewer to refresh.
   */
  private void refreshViewer(final ViewLayerEvent event) {
    if (_treeViewer == null) {
      return;
    }
    if (!_treeViewer.getTree().isDisposed()) {
      _treeViewer.getControl().getDisplay().asyncExec(new Runnable() {

        public void run() {
          if (_treeViewer != null && !_treeViewer.getTree().isDisposed()) {
            try {
              _treeViewer.refresh(false);
              if (_treeViewer != null && event != null) {
                IViewLayer viewLayer = event.getViewLayer();
                if (event.getEventType().equals(ViewLayerEvent.EventType.LAYER_ADDED)) {
                  _treeViewer.expandAll();

                  // Default to the checked state of the view layer.
                  Boolean checked = viewLayer.isChecked();

                  // Then look at the checked-state map to see if an entry exists.
                  String uniqueID = viewLayer.getUniqueID();
                  Map<String, Boolean> checkedMap = _model.getCheckedMap();
                  if (checkedMap.containsKey(uniqueID)) {
                    // If so, use the checked state from the map as the "official" state.
                    checked = checkedMap.get(uniqueID);
                    viewLayer.setChecked(checked);

                    // Go ahead and remove the entry from the checked-state map.
                    checkedMap.remove(uniqueID);
                  }

                  // Set the checked state in the tree viewer.
                  _treeViewer.setChecked(viewLayer, checked);
                }
                _treeViewer.update(viewLayer, null);
              }
            } catch (SWTException e) {
              ServiceProvider.getLoggingService().getLogger(getClass()).warn("Could not refresh viewer");
            }
          }
        }
      });
    }
  }

  public void checkStateChanged(final CheckStateChangedEvent event) {
    boolean checked = event.getChecked();
    IViewLayer viewLayer = (IViewLayer) event.getElement();
    viewLayer.setVisible(checked);
    viewLayer.setChecked(checked);
    for (Object child : _treeViewer.getCheckedElements()) {
      IViewLayer layer = (IViewLayer) child;
      determineLayerVisibility(layer);
    }
  }

  /**
   * Determines the visibility of a given layer.
   * Starting with the layer of interest, visibility is checked recursively
   * on parent layers until the top is reached. If any of the layers in the
   * chain is not turned on (i.e. "checked"), then the layer of interest is
   * not considered visible.
   * 
   * @param viewLayer the layer of interest.
   */
  private void determineLayerVisibility(final IViewLayer viewLayer) {
    boolean visible = _treeViewer.getChecked(viewLayer);
    // Check all layers above. If any is unchecked, then visibility is false.
    if (visible) {
      IViewLayer parentLayer = _model.getParent(viewLayer);
      while (parentLayer != null) {
        if (!_treeViewer.getChecked(parentLayer)) {
          visible = false;
          break;
        }
        parentLayer = _model.getParent(parentLayer);
      }
    }
    viewLayer.setVisible(visible);
  }

  public void modelUpdated() {
    refreshViewer(null);
  }

  public void layeredModelUpdated(final ViewLayerEvent event) {
    refreshViewer(event);
  }

  public void mouseEnter(final MouseEvent e) {
    // Nothing to do.
  }

  public void mouseExit(final MouseEvent e) {
    // Nothing to do.
  }

  public void mouseHover(final MouseEvent e) {
    if (_treeViewer != null) {
      TreeItem item = _treeViewer.getTree().getItem(new Point(e.x, e.y));
      if (item != null) {
        IViewLayer viewLayer = (IViewLayer) item.getData();
        _treeViewer.getTree().setToolTipText(viewLayer.getToolTipText());
      }
    }
  }
}
