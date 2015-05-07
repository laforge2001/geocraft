/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.waveletviewer.renderer;


import org.eclipse.swt.widgets.Display;
import org.geocraft.ui.plot.AbstractRenderer;
import org.geocraft.ui.plot.RendererViewLayer;
import org.geocraft.ui.viewer.layer.IViewLayer;
import org.geocraft.ui.waveletviewer.IWaveletViewer;


public abstract class WaveletViewRenderer extends AbstractRenderer<IWaveletViewer> {

  public WaveletViewRenderer(final String name) {
    super(name);
  }

  @Override
  protected void addPopupMenuActions() {
    // No action.
  }

  @Override
  public void redraw() {
    // No action.
  }

  @Override
  protected void addToLayerTree(final Object parentObject, final String parentObjectName, final String folderName,
      final boolean autoUpdate) {
    Display.getDefault().syncExec(new Runnable() {

      public void run() {
        IWaveletViewer viewer = getViewer();

        // First add the parent object.
        if (parentObject != null) {
          viewer.addObjects(new Object[] { parentObject }, true);
        }

        // Create a view layer for this renderer.
        RendererViewLayer viewLayer = new RendererViewLayer(WaveletViewRenderer.this, false, false, true);

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
