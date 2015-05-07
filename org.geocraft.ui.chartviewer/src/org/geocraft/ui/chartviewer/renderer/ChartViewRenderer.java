package org.geocraft.ui.chartviewer.renderer;


import org.eclipse.swt.widgets.Display;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.ui.chartviewer.AbstractChartViewer;
import org.geocraft.ui.chartviewer.data.IChartData;
import org.geocraft.ui.plot.AbstractRenderer;
import org.geocraft.ui.plot.RendererViewLayer;
import org.geocraft.ui.viewer.layer.IViewLayer;


public abstract class ChartViewRenderer<V extends AbstractChartViewer> extends AbstractRenderer<V> {

  public ChartViewRenderer(final String name) {
    super(name);
    showReadoutInfo(true);
  }

  @Override
  public void redraw() {
    super.updated();
  }

  @Override
  protected void addPopupMenuActions() {
    // No actions to add.
  }

  public abstract IChartData getRenderedChartData();

  @Override
  public DataSelection getDataSelection(double x, double y) {
    return null;
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof ChartViewRenderer && ((ChartViewRenderer) object).getRenderedChartData() != null) {
      return ((ChartViewRenderer) object).getRenderedChartData().equals(getRenderedChartData());
    }
    return false;
  }

  @Override
  public int hashCode() {
    if (getRenderedChartData() != null) {
      return getRenderedChartData().hashCode();
    }
    return super.hashCode();
  }

  protected boolean autoAdjustBounds() {
    return true;
  }

  @Override
  protected void addToLayerTree(final Object parentObject, final String parentObjectName, final String folderName,
      final boolean autoUpdate) {
    Display.getDefault().syncExec(new Runnable() {

      public void run() {
        V viewer = getViewer();

        // First add the parent object.
        if (parentObject != null) {
          viewer.addObjects(new Object[] { parentObject }, true);
        }

        // Create a view layer for this renderer.
        RendererViewLayer viewLayer = new RendererViewLayer(ChartViewRenderer.this, false, false, true);

        // Add the layer to the viewer.
        boolean autoAdjust = autoAdjustBounds();
        if (autoAdjust) {
          autoAdjust = autoUpdate;
        }
        viewer.getPlot().addLayer(viewLayer.getPlotLayer(), autoAdjust, autoAdjust);

        // Attempt to find the parent layer in the layered model.
        IViewLayer[] layers = viewer.getLayerModel().getChildren(getViewer().findFolderLayer(folderName));
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
          if (parentLayer == null) {
            parentLayer = viewer.getLayerModel().getLayers()[0];
          }
        }

        // Add the layer to the layered model.
        viewer.getLayerModel().addLayer(viewLayer, parentLayer);
      }
    });
  }
}
