package org.geocraft.ui.chartviewer;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.common.progress.BackgroundTask;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.model.datatypes.Coordinate;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.event.CursorLocation;
import org.geocraft.core.model.event.CursorLocation.TimeOrDepth;
import org.geocraft.core.model.preferences.ApplicationPreferences;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.core.service.message.Topic;
import org.geocraft.ui.chartviewer.renderer.ChartViewRenderer;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.Plot;
import org.geocraft.ui.plot.PlotScrolling;
import org.geocraft.ui.plot.RendererViewLayer;
import org.geocraft.ui.plot.action.IPlotMouseAction;
import org.geocraft.ui.plot.action.PlotMouseActionList;
import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.plot.layer.PlotLayer;
import org.geocraft.ui.plot.layout.CanvasLayoutModel;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.ModelSpace;
import org.geocraft.ui.viewer.AbstractDataViewer;
import org.geocraft.ui.viewer.IRenderer;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.viewer.layer.FolderLayer;
import org.geocraft.ui.viewer.layer.ILayeredModel;
import org.geocraft.ui.viewer.layer.IViewLayer;


public abstract class AbstractChartViewer extends AbstractDataViewer {

  public static final String DEFAULT_FOLDER = "Chart Data";

  private IPlot _plot;

  public static IModelSpace createModelSpace(final String xAxis, final String yAxis) {
    return new ModelSpace("", xAxis, yAxis, true);
  }

  public static IModelSpace createModelSpace(final String xAxis, final String yAxis, final float aspectRatio) {
    return new ModelSpace("", xAxis, yAxis, aspectRatio);
  }

  public AbstractChartViewer(final Composite parent, final String title, final boolean broadcastReceive) {
    //super(parent, title, modelSpace, scrolled, readout, broadcastReceive);
    super(parent, true, true, broadcastReceive);
    _plot.getActiveModelSpace().addLayer(new PlotLayer("Default"));
    _plot.setTitle(title);
    _plot.getPlotComposite().setCursorCrossVisible(false);
    setLayerTreeVisible(false);
  }

  @Override
  public void addObjects(final Object[] objects) {
    addObjects(objects, false);
  }

  public void addObjects(final Object[] objects, final boolean block) {
    loadEntities(objects);
    final Shell shell = getShell();
    final BackgroundTask task = new BackgroundTask() {

      @Override
      public Void compute(final ILogger logger, final IProgressMonitor monitor) {
        monitor.beginTask("Add data to the chart viewer", objects.length);
        try {
          Display.getDefault().syncExec(new Runnable() {

            public void run() {
              String chartType = getChartType();

              // First, lookup renderers for the combination of objects.
              if (objects.length > 1) {
                String objectTypes = objects[0].getClass().getSimpleName() + "[]";
                ChartViewRenderer newRenderer = RendererRegistry.getInstance().getRendererForObjectType(objectTypes,
                    chartType);
                if (newRenderer != null) {
                  //                  for (ChartViewRenderer renderer : getChartViewRenderers()) {
                  //                    if (renderer.getClass().equals(newRenderer.getClass())) {
                  //                      if (renderer.getRenderedObjects()[0].equals(object)) {
                  //                        rendererExists = true;
                  //                        break;
                  //                      }
                  //                    }
                  //                  }
                  //if (!rendererExists) {
                  newRenderer.setData(shell, AbstractChartViewer.this, objects);
                  //}
                }
              }

              // Next, lookup renderers for the individual objects.
              for (Object object : objects) {
                try {
                  boolean rendererExists = false;
                  String objectType = object.getClass().getSimpleName();
                  ChartViewRenderer newRenderer = RendererRegistry.getInstance().getRendererForObjectType(objectType,
                      chartType);
                  if (newRenderer != null) {
                    for (ChartViewRenderer renderer : getChartViewRenderers()) {
                      if (renderer.getClass().equals(newRenderer.getClass())) {
                        if (renderer.getRenderedObjects()[0].equals(object)) {
                          rendererExists = true;
                          break;
                        }
                      }
                    }
                    if (!rendererExists) {
                      newRenderer.setData(shell, AbstractChartViewer.this, new Object[] { object });
                    }
                  }
                } catch (Exception ex) {
                  ServiceProvider.getLoggingService().getLogger(getClass())
                      .error("Error creating renderer for " + object.toString(), ex);
                }
                //}

                // Update the progress monitor.
                monitor.worked(1);
                if (monitor.isCanceled()) {
                  break;
                }
              }
            }
          });
        } finally {
          monitor.done();
        }
        Display.getDefault().syncExec(new Runnable() {

          public void run() {
            for (ChartViewRenderer renderer : getChartViewRenderers()) {
              // the model space is set later, so we need to call
              // this for the renderers that
              // need the model space in order to draw the shapes
              renderer.redraw();
              monitor.worked(1);
            }
          }
        });
        return null;
      }
    };
    if (block) {
      TaskRunner.runTask(task, "Add entities to the chart viewer");
    } else {
      new Thread(new Runnable() {

        public void run() {
          TaskRunner.runTask(task, "Add entities to the chart viewer");
        }
      }).start();
    }
  }

  /**
   * Returns an array of the current chart view renderers.
   */
  protected ChartViewRenderer[] getChartViewRenderers() {
    List<ChartViewRenderer> renderers = new ArrayList<ChartViewRenderer>();
    for (IViewLayer viewLayer : getLayerModel().getLayers()) {
      if (viewLayer instanceof RendererViewLayer) {
        IPlotLayer plotLayer = ((RendererViewLayer) viewLayer).getPlotLayer();
        if (plotLayer instanceof ChartViewRenderer) {
          renderers.add((ChartViewRenderer) plotLayer);
        }
      }
    }
    return renderers.toArray(new ChartViewRenderer[0]);
  }

  protected abstract String getChartType();

  @Override
  protected ReadoutInfo getViewReadoutInfo(double x, double y) {
    // Create the main readout section info 
    return new ReadoutInfo("Mouse location", new String[] { "x", "y" }, new String[] { x + "", y + "" });
  }

  @Override
  public IRenderer[] getRenderers() {
    return getChartViewRenderers();
  }

  protected abstract IModelSpace initializeModelSpace();

  protected abstract PlotScrolling getPlotScrolling();

  @Override
  protected void initializeCanvas(Composite canvasComposite) {
    _plot = new Plot(canvasComposite, initializeModelSpace(), getPlotScrolling());

    CanvasLayoutModel layoutModel = _plot.getCanvasLayoutModel();
    layoutModel.setTopAxisHeight(0);
    layoutModel.setRightAxisWidth(0);
    layoutModel.setTopLabelHeight(0);
    layoutModel.setRightLabelWidth(0);
  }

  @Override
  protected void initializeLayeredModel(final ILayeredModel model, Map<String, IViewLayer> folderLayers) {
    folderLayers.put(DEFAULT_FOLDER, new FolderLayer(DEFAULT_FOLDER, "Folder containing miscellaneous histogram data"));
    folderLayers.put(GRID_FOLDER, new FolderLayer(GRID_FOLDER, "Folder containing grids and grid geometries"));
    folderLayers.put(WELL_FOLDER, new FolderLayer(WELL_FOLDER, "Folder containing well, bores, logs, etc."));
    folderLayers.put(FAULT_FOLDER, new FolderLayer(FAULT_FOLDER, "Folder containing fault surfaces"));
    folderLayers.put(CULTURE_FOLDER, new FolderLayer(CULTURE_FOLDER,
        "Folder containing culture data (lease blocks, etc.)"));
    folderLayers.put(AOI_ROI_FOLDER, new FolderLayer(AOI_ROI_FOLDER, "Folder containing areas/regions of interest"));
    folderLayers.put(SEISMIC_FOLDER,
        new FolderLayer(SEISMIC_FOLDER, "Folder containing seismic geometries and volumes"));
    folderLayers.put(POINTSET_FOLDER, new FolderLayer(POINTSET_FOLDER, "Folder containing point sets"));
    for (IViewLayer layer : folderLayers.values()) {
      model.addLayer(layer);
    }
  }

  public void setMainLayerName(final String name) {
    getLayerModel().getLayers()[0].setName(name);
  }

  @Override
  protected void initializeToolBars() {
    // Nothing to do.
  }

  @Override
  public String[] getMessageSubscriptionTopics() {
    return new String[] { Topic.REPOSITORY_OBJECT_UPDATED };
  }

  public void zoomIn() {
    _plot.zoom(_plot.getZoomFactor());
  }

  public void zoomOut() {
    _plot.zoom(1 / _plot.getZoomFactor());
  }

  public void zoomWindow(final boolean enabled) {
    if (enabled) {
      _plot.setMouseActions(PlotMouseActionList.getDefaultZoomWindowActions().getActions(), SWT.CURSOR_CROSS);
    } else {
      _plot.setMouseActions(new IPlotMouseAction[0], SWT.CURSOR_ARROW);
    }
  }

  public void pan(final boolean enabled) {
    if (enabled) {
      _plot.setMouseActions(PlotMouseActionList.getDefaultPanActions().getActions(), SWT.CURSOR_HAND);
    } else {
      _plot.setMouseActions(new IPlotMouseAction[0], SWT.CURSOR_ARROW);
    }
  }

  public void home() {
    _plot.unzoom();
  }

  public void messageReceived(final String topic, final Object message) {
    final Object data = message;
    Display.getDefault().asyncExec(new Runnable() {

      public void run() {

        // Check that the topic is a cursor location, and that the
        // map viewer is toggled on to receive cursor events.
        if (topic.equals(Topic.REPOSITORY_OBJECT_UPDATED) && data != null) {

          boolean redraw = false;
          for (IRenderer renderer : getRenderers()) {
            for (Object renderedObject : renderer.getRenderedObjects()) {
              if (renderedObject != null && renderedObject.equals(data)) {
                renderer.redraw();
                redraw = true;
                break;
              }
            }
          }
          if (redraw) {
            getPlot().updateAll();
          }
        }
      }
    });
  }

  @Override
  public void dispose() {
    _plot.dispose();
    super.dispose();
  }

  @Override
  protected void checkAspectRatio() {
    _plot.getModelSpaceCanvas().checkAspectRatio();
  }

  public IPlot getPlot() {
    return _plot;
  }

  @Override
  public void cursorUpdated(final double x, final double y, final boolean broadcast) {
    super.cursorUpdated(x, y, false);

    // Broadcast the cursor location.
    if (_plot.getCursorBroadcast() && broadcast) {
      ServiceProvider.getMessageService().publish(
          Topic.CURSOR_LOCATION,
          new CursorLocation(new Coordinate(new Point3d(x, y, 0), ApplicationPreferences.getInstance()
              .getTimeCoordinateSystem()), TimeOrDepth.NONE, getViewerID()));
    }
  }

  @Override
  public void setCursorBroadcast(final boolean broadcast) {
    super.setCursorBroadcast(broadcast);
    _plot.setCursorBroadcast(broadcast);
  }

  @Override
  public void setCursorReception(final boolean reception) {
    super.setCursorReception(reception);
    _plot.setCursorReception(reception);
  }

  @Override
  protected void updateAll() {
    _plot.updateAll();
  }

  public RGB getBackgroundViewColor() {
    return _plot.getBackgroundPlotColor();
  }

  public void setBackgroundViewColor(RGB color) {
    _plot.setBackgroundPlotColor(color);
  }

  public void setCursorStyle(int cursorStyle) {
    _plot.getModelSpaceCanvas().setCursorStyle(cursorStyle);
  }

  @Override
  protected void hookContextMenu() {
    // TODO Auto-generated method stub

  }

  @Override
  protected IRenderer createRenderer(IConfigurationElement configElement) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected List<IConfigurationElement> findRenderer(Shell shell, Object object) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void initializeViewerSpecificFeatures() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void setRendererData(IRenderer renderer, Shell shell, Object[] objects, boolean autoUpdate) {
    // TODO Auto-generated method stub

  }
}
