/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot;


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertySource;
import org.geocraft.core.model.base.IPropertiesProvider;
import org.geocraft.core.model.base.IPropertiesProviderContainer;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.SpatialExtent;
import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.ModelSpaceBounds;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.viewer.layer.AbstractViewLayer;


/**
 * Defines a view layer that contains a plot renderer.
 */
public final class RendererViewLayer extends AbstractViewLayer implements IPropertiesProviderContainer, IAdaptable {

  /** The renderer. */
  private final IPlotLayer _plotLayer;

  public RendererViewLayer(final AbstractRenderer renderer, final boolean allowsChildren, final boolean allowsRename, final boolean allowsRemove) {
    super(renderer.getName(), renderer.getUniqueID(), allowsChildren, allowsRename, allowsRemove);
    _plotLayer = renderer;
    for (Action action : _plotLayer.getActions()) {
      addAction(action);
    }
  }

  public RendererViewLayer(final IPlotLayer plotLayer, final String uniqueID, final boolean allowsChildren, final boolean allowsRename, final boolean allowsRemove) {
    super(plotLayer.getName(), uniqueID, allowsChildren, allowsRename, allowsRemove);
    _plotLayer = plotLayer;
    for (Action action : _plotLayer.getActions()) {
      addAction(action);
    }
  }

  @Override
  public Image getImage() {
    return _plotLayer.getImage();
  }

  public IPlotLayer getPlotLayer() {
    return _plotLayer;
  }

  public SpatialExtent getExtent() {
    ModelSpaceBounds bounds = _plotLayer.getBounds();
    if (!bounds.isValid()) {
      return new SpatialExtent();
    }
    double[] xs = new double[2];
    double[] ys = new double[2];
    double[] zs = new double[2];
    xs[0] = bounds.getRangeX().getStart();
    xs[1] = bounds.getRangeX().getEnd();
    ys[0] = bounds.getRangeY().getStart();
    ys[1] = bounds.getRangeY().getEnd();
    zs[0] = bounds.getRangeZ().getStart();
    zs[1] = bounds.getRangeZ().getEnd();
    return new SpatialExtent(xs, ys, zs, Domain.TIME);
  }

  @Override
  public void setVisible(final boolean visible) {
    super.setVisible(visible);
    _plotLayer.setVisible(visible);
  }

  public void refresh() {
    _plotLayer.refresh();
  }

  @Override
  public void remove() {
    IModelSpace modelSpace = _plotLayer.getModelSpace();
    if (modelSpace != null) {
      modelSpace.removeLayer(_plotLayer);
    }
    _plotLayer.removeAllShapes();
    super.remove();
  }

  @Override
  public void dispose() {
    _plotLayer.dispose();
    super.dispose();
  }

  public boolean showReadoutInfo() {
    if (_plotLayer != null) {
      return _plotLayer.showReadoutInfo();
    }
    return false;
  }

  public void showReadoutInfo(final boolean show) {
    if (_plotLayer != null) {
      _plotLayer.showReadoutInfo(show);
    }
  }

  @Override
  public String getToolTipText() {
    if (_plotLayer != null) {
      return _plotLayer.getToolTipText();
    }
    return "";
  }

  public void redraw() {
    _plotLayer.updated();
  }

  @Override
  public Object getAdapter(final Class adapter) {
    if (adapter.equals(IPropertySource.class)) {
      if (_plotLayer != null && IAdaptable.class.isAssignableFrom(_plotLayer.getClass())) {
        IAdaptable adaptable = (IAdaptable) _plotLayer;
        return adaptable.getAdapter(adapter);
      }
    }
    return null;
  }

  @Override
  public IPropertiesProvider getPropertiesProvider() {
    if (IPropertiesProviderContainer.class.isAssignableFrom(_plotLayer.getClass())) {
      IPropertiesProviderContainer container = (IPropertiesProviderContainer) _plotLayer;
      return container.getPropertiesProvider();
    }
    return null;
  }

  public ReadoutInfo getReadoutInfo(double x, double y) {
    if (_plotLayer != null) {
      return _plotLayer.getReadoutInfo(x, y);
    }
    return new ReadoutInfo(getName());
  }

}
