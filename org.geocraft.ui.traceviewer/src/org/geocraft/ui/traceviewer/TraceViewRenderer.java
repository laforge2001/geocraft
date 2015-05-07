package org.geocraft.ui.traceviewer;


import org.eclipse.swt.widgets.Display;
import org.geocraft.ui.plot.AbstractRenderer;
import org.geocraft.ui.plot.RendererViewLayer;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.viewer.layer.IViewLayer;


public abstract class TraceViewRenderer extends AbstractRenderer<ITraceViewer> {

  public TraceViewRenderer(final String name) {
    super(name);
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof TraceViewRenderer && ((TraceViewRenderer) object).getRenderedObjects().length > 0) {
      return ((TraceViewRenderer) object).getRenderedObjects().equals(getRenderedObjects());
    }
    return false;
  }

  @Override
  public ReadoutInfo getReadoutInfo(final double x, final double y) {
    int traceNum = Math.round((float) x);
    float z = (float) y;
    return getReadoutInfo(traceNum, z);
  }

  /**
  * Returns the cursor readout information for the renderer.
  * @param trace the cursor trace (x) coordinate.
  * @param z the the cursor z (y) coordinate.
  * @return the cursor readout information; or <i>null</i> if no readout info available.
  */
  public abstract ReadoutInfo getReadoutInfo(final int traceNum, final float z);

  @Override
  protected void addToLayerTree(final Object parentObject, final String parentObjectName, final String folderName,
      final boolean autoUpdate) {
    Display.getDefault().syncExec(new Runnable() {

      public void run() {
        ITraceViewer viewer = getViewer();

        // First add the parent object.
        if (parentObject != null) {
          viewer.addObjects(true, new Object[] { parentObject });
        }

        // Create a view layer for this renderer.
        RendererViewLayer viewLayer = new RendererViewLayer(TraceViewRenderer.this, false, false, true);

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
