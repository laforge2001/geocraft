/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.layer;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.geocraft.ui.plot.defs.PlotEventType;
import org.geocraft.ui.plot.event.ModelSpaceEvent;
import org.geocraft.ui.plot.event.PlotLayerEvent;
import org.geocraft.ui.plot.event.ShapeEvent;
import org.geocraft.ui.plot.listener.IPlotLayerListener;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.ModelSpaceBounds;
import org.geocraft.ui.plot.object.IPlotShape;
import org.geocraft.ui.viewer.ReadoutInfo;


/**
 * The basic implementation of a plot layer.
 */
public class PlotLayer implements IPlotLayer {

  /** The plot layer name. */
  protected String _name;

  /** The model space containing the plot layer. */
  protected IModelSpace _modelSpace;

  /** The plot shapes contained in the plot layer. */
  protected List<IPlotShape> _shapes;

  /** The plot layer listeners. */
  protected List<IPlotLayerListener> _listeners;

  /** The visibility of the plot layer. */
  protected boolean _isVisible;

  /** The list of pop-up actions registered with the plot layer. */
  protected List<Action> _actions;

  /** The flag for appending cursor info. */
  protected boolean _appendCursorInfo;

  /** The tree image for the layer. */
  protected Image _image;

  protected boolean _imageIsDisposable = false;

  protected boolean _blockUpdate = false;

  protected boolean _includeBounds;

  /**
   * Constructs a plot layer with the specified name.
   * @param name the plot layer name.
   */
  public PlotLayer(final String name) {
    _shapes = Collections.synchronizedList(new ArrayList<IPlotShape>());
    _listeners = Collections.synchronizedList(new ArrayList<IPlotLayerListener>());
    _image = createImage();
    _name = name;
    setVisible(true);
    _appendCursorInfo = false;
    _actions = Collections.synchronizedList(new ArrayList<Action>());
    for (Action action : createActions()) {
      _actions.add(action);
    }
    _includeBounds = true;
    // TODO: addTreePopupAction(new RenamePlotLayerAction(this));
  }

  /**
   * Constructs a plot layer with the specified name.
   * @param name the plot layer name.
   */
  public PlotLayer(final String name, boolean includeBounds) {
    this(name);
    _includeBounds = includeBounds;
  }

  @Override
  public String toString() {
    return getName() + " : " + getShapes().length;
  }

  public String getName() {
    return _name;
  }

  public void setName(final String name) {
    boolean changed = _name.equals(name);
    _name = name;
    if (changed) {
      fireLayerEvent(new PlotLayerEvent(this, PlotEventType.LAYER_UPDATED));
    }
  }

  public Image getImage() {
    return _image;
  }

  public void setImage(final Image image) {
    setImage(image, false);
  }

  public void setImage(final Image image, final boolean imageIsDisposable) {
    Image imageOld = _image;
    boolean imageIsDisposableOld = _imageIsDisposable;
    _image = image;
    _imageIsDisposable = imageIsDisposable;
    if (imageIsDisposableOld && imageOld != null && !imageOld.isDisposed()) {
      imageOld.dispose();
    }
  }

  public IModelSpace getModelSpace() {
    return _modelSpace;
  }

  public void setModelSpace(final IModelSpace modelSpace) {
    _modelSpace = modelSpace;
    updated(null, PlotEventType.LAYER_UPDATED);
  }

  public boolean isVisible() {
    return _isVisible;
  }

  public void setVisible(final boolean visible) {
    boolean changed = _isVisible != visible;
    _isVisible = visible;
    if (changed) {
      updated(null, PlotEventType.LAYER_UPDATED);
    }
  }

  public IPlotShape[] getShapes() {
    IPlotShape[] shapes = new IPlotShape[_shapes.size()];
    for (int i = 0; i < shapes.length; i++) {
      shapes[i] = _shapes.get(i);
    }
    return shapes;
  }

  public void addShape(final IPlotShape shape) {
    addShape(shape, false);
  }

  public void addShape(final IPlotShape shape, final boolean selected) {
    if (_shapes.contains(shape)) {
      return;
    }
    shape.setLayer(this);
    if (_modelSpace != null) {
      shape.setModelSpace(_modelSpace);
    }
    _shapes.add(shape);
    shape.addShapeListener(this);
    shape.added();
    shape.setSelected(selected);
  }

  public void addShapes(final IPlotShape[] shapes) {
    for (IPlotShape shape : shapes) {
      if (_shapes.contains(shape)) {
        continue;
      }
      shape.setLayer(this);
      if (_modelSpace != null) {
        shape.setModelSpace(_modelSpace);
      }
      _shapes.add(shape);
      shape.addShapeListener(this);
    }
    fireLayerEvent(new PlotLayerEvent(this, PlotEventType.SHAPE_ADDED));
  }

  public void removeShape(final IPlotShape shape) {
    boolean redraw = false;
    boolean removed = _shapes.remove(shape);
    if (removed) {
      redraw = true;
    }
    shape.removed();
    shape.removeShapeListener(this);
    if (redraw && _modelSpace != null) {
      _modelSpace.redraw();
    }
  }

  public void removeAllShapes() {
    removeAllShapes(true);
  }

  public void removeAllShapes(boolean notifyListeners) {
    boolean redraw = false;
    IPlotShape[] shapes = _shapes.toArray(new IPlotShape[0]);
    for (IPlotShape shape : shapes) {
      if (!notifyListeners) {
        shape.blockUpdate();
      }
      boolean removed = _shapes.remove(shape);
      if (removed) {
        redraw = true;
      }
      shape.removed();
      shape.removeShapeListener(this);
    }
    if (redraw && _modelSpace != null && !_blockUpdate) {
      _modelSpace.redraw();
    }
  }

  public void clear() {
    removeAllShapes();
  }

  public void shapeUpdated(final ShapeEvent event) {
    PlotEventType type = event.getEventType();
    IPlotShape shape = event.getShape();
    //    if (type.equals(PlotEventType.SHAPE_SELECTED) || type.equals(PlotEventType.SHAPE_DESELECTED)) {
    //      redraw();
    //    }
    //else if(type.equals(EventType.SHAPE_UPDATED) && !shape.isSelected()) {
    //  redraw();
    //}
    updated(event.getShape(), type);
  }

  public void addLayerListener(final IPlotLayerListener listener) {
    _listeners.add(listener);
  }

  public void removeLayerListener(final IPlotLayerListener listener) {
    _listeners.remove(listener);
  }

  public void addPopupMenuAction(final Action action) {
    _actions.add(action);
  }

  public void updated() {
    updated(null, PlotEventType.LAYER_UPDATED);
  }

  /**
   * Invoked when the plot layer is updated.
   * @param shape the shape causing the layer update.
   * @param type the type of plot layer event (LayerAdded, LayerRemoved, etc).
   */
  protected void updated(final IPlotShape shape, final PlotEventType type) {
    PlotLayerEvent event = new PlotLayerEvent(this, shape, type);
    fireLayerEvent(event);
  }

  /**
   * Fires a plot layer event to the listeners.
   * @param event the event to fire.
   */
  protected void fireLayerEvent(final PlotLayerEvent event) {
    if (_blockUpdate) {
      return;
    }
    IPlotLayerListener[] listeners = _listeners.toArray(new IPlotLayerListener[0]);
    for (IPlotLayerListener listener : listeners) {
      listener.layerUpdated(event);
    }
  }

  public Action[] getActions() {
    return _actions.toArray(new Action[0]);
  }

  public void clearActions() {
    _actions.clear();
  }

  protected Action[] createActions() {
    // Sub-classes should override this method to add layer actions.
    return new Action[0];
  }

  protected Image createImage() {
    Image image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
    return new Image(image.getDevice(), image, SWT.IMAGE_COPY);
  }

  public void refresh() {
    boolean isVisible = isVisible();
    if (_modelSpace != null) {
      setVisible(false);
      setVisible(isVisible);
    }
  }

  public void dispose() {
    if (_imageIsDisposable && _image != null && !_image.isDisposed()) {
      _image.dispose();
    }
    for (IPlotShape shape : _shapes) {
      shape.dispose();
    }
    _shapes.clear();
    _actions.clear();
    _listeners.clear();
  }

  @Override
  public ModelSpaceBounds getBounds() {
    double xminAll = Double.NaN;
    double xmaxAll = Double.NaN;
    double yminAll = Double.NaN;
    double ymaxAll = Double.NaN;
    double zminAll = Double.NaN;
    double zmaxAll = Double.NaN;
    if (!_includeBounds) {
      return new ModelSpaceBounds(xminAll, xmaxAll, yminAll, ymaxAll, zminAll, zmaxAll);
    }
    for (IPlotShape shape : getShapes()) {
      double xmin = shape.getMinimumX();
      double xmax = shape.getMaximumX();
      double ymin = shape.getMinimumY();
      double ymax = shape.getMaximumY();
      double zmin = shape.getMinimumZ();
      double zmax = shape.getMaximumZ();
      if (!Double.isNaN(xmin)) {
        if (Double.isNaN(xminAll)) {
          xminAll = xmin;
        } else {
          xminAll = Math.min(xmin, xminAll);
        }
      }
      if (!Double.isNaN(xmax)) {
        if (Double.isNaN(xmaxAll)) {
          xmaxAll = xmax;
        } else {
          xmaxAll = Math.max(xmax, xmaxAll);
        }
      }
      if (!Double.isNaN(ymin)) {
        if (Double.isNaN(yminAll)) {
          yminAll = ymin;
        } else {
          yminAll = Math.min(ymin, yminAll);
        }
      }
      if (!Double.isNaN(ymax)) {
        if (Double.isNaN(ymaxAll)) {
          ymaxAll = ymax;
        } else {
          ymaxAll = Math.max(ymax, ymaxAll);
        }
      }
      if (!Double.isNaN(zmin)) {
        if (Double.isNaN(zminAll)) {
          zminAll = zmin;
        } else {
          zminAll = Math.min(zmin, zminAll);
        }
      }
      if (!Double.isNaN(zmax)) {
        if (Double.isNaN(zmaxAll)) {
          zmaxAll = zmax;
        } else {
          zmaxAll = Math.max(zmax, zmaxAll);
        }
      }
    }
    return new ModelSpaceBounds(xminAll, xmaxAll, yminAll, ymaxAll, zminAll, zmaxAll);
  }

  public boolean showReadoutInfo() {
    return _appendCursorInfo;
  }

  public void showReadoutInfo(final boolean appendCursorInfo) {
    _appendCursorInfo = appendCursorInfo;
  }

  public String getToolTipText() {
    return "";
  }

  public void block() {
    _blockUpdate = true;
  }

  public void unblock() {
    _blockUpdate = false;
  }

  public void modelSpaceUpdated(ModelSpaceEvent event) {
    return;
  }

  public ReadoutInfo getReadoutInfo(double x, double y) {
    return new ReadoutInfo(getName());
  }
}
