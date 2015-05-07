/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer;


import org.eclipse.swt.widgets.Display;
import org.geocraft.ui.plot.AbstractRenderer;
import org.geocraft.ui.plot.RendererViewLayer;
import org.geocraft.ui.viewer.layer.IViewLayer;


/**
 * Defines the abstract base class for map view renderers.
 * The schema for the <code>org.geocraft.ui.mapviewer.renderer</code>
 * extension point requires that all map view renderers extend
 * this class.
 */
public abstract class MapViewRenderer extends AbstractRenderer<IMapViewer> {

  /**
   * Constructs a renderer with the specified name.
   * @param name the renderer name.
   */
  public MapViewRenderer(final String name) {
    super(name);
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof MapViewRenderer && ((MapViewRenderer) object).getRenderedObjects().length > 0) {
      return ((MapViewRenderer) object).getRenderedObjects().equals(getRenderedObjects());
    }
    return false;
  }

  @Override
  protected void addToLayerTree(final Object parentObject, final String parentObjectName, final String folderName,
      final boolean autoUpdate) {
    addToLayerTree(parentObject, parentObjectName, folderName, autoUpdate, true);
  }

  protected void addToLayerTree(final Object parentObject, final String parentObjectName, final String folderName,
      final boolean autoUpdate, final boolean allowsRemove) {
    Display.getDefault().syncExec(new Runnable() {

      public void run() {
        IMapViewer viewer = getViewer();

        // First add the parent object.
        if (parentObject != null) {
          viewer.addObjects(true, new Object[] { parentObject });
        }

        // Create a view layer for this renderer.
        RendererViewLayer viewLayer = new RendererViewLayer(MapViewRenderer.this, false, false, allowsRemove);

        // Add the layer to the viewer.
        viewer.getPlot().addLayer(viewLayer.getPlotLayer(), autoUpdate, autoUpdate);

        // Attempt to find the parent layer in the layered model.
        IViewLayer[] layers = viewer.getLayerModel().getChildren(viewer.findFolderLayer(folderName));
        IViewLayer parentLayer = null;
        if (parentObjectName != null && parentObjectName.length() > 0) {
          for (int k = 0; k < layers.length && parentLayer == null; k++) {
            if (layers[k].getName().equals(parentObjectName)) {
              parentLayer = layers[k];
              break;
            }
          }
        }

        // If no parent layer found, default to a top-level folder layer.
        if (parentLayer == null) {
          parentLayer = viewer.findFolderLayer(folderName);
        }

        // Add the layer to the layered model.
        viewer.getLayerModel().addLayer(viewLayer, parentLayer);
      }
    });
  }

}
