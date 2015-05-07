package org.geocraft.ui.traceviewer;


import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.geocraft.core.common.progress.BackgroundTask;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.datatypes.Coordinate;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.event.CursorLocation;
import org.geocraft.core.model.event.CursorLocation.TimeOrDepth;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.core.model.preferences.ApplicationPreferences;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.specification.EntityUniqueIdSpecification;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.core.service.message.Topic;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.Plot;
import org.geocraft.ui.plot.PlotScrolling;
import org.geocraft.ui.plot.RendererViewLayer;
import org.geocraft.ui.plot.action.IPlotMouseAction;
import org.geocraft.ui.plot.action.PlotMouseActionList;
import org.geocraft.ui.plot.axis.Axis;
import org.geocraft.ui.plot.axis.AxisRange;
import org.geocraft.ui.plot.axis.IAxis;
import org.geocraft.ui.plot.defs.Alignment;
import org.geocraft.ui.plot.defs.AxisDirection;
import org.geocraft.ui.plot.defs.AxisScale;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.Orientation;
import org.geocraft.ui.plot.defs.UpdateLevel;
import org.geocraft.ui.plot.label.ILabel;
import org.geocraft.ui.plot.label.Label;
import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.plot.layout.CanvasLayoutModel;
import org.geocraft.ui.plot.listener.ICursorListener;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.model.ModelSpace;
import org.geocraft.ui.plot.model.ModelSpaceBounds;
import org.geocraft.ui.traceviewer.renderer.trace.PicksData;
import org.geocraft.ui.traceviewer.renderer.trace.PicksRenderer;
import org.geocraft.ui.traceviewer.renderer.trace.TraceDataRenderer;
import org.geocraft.ui.viewer.AbstractDataViewer;
import org.geocraft.ui.viewer.IRenderer;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.viewer.RendererSpecification;
import org.geocraft.ui.viewer.action.HelpAction;
import org.geocraft.ui.viewer.layer.IViewLayer;
import org.geocraft.ui.viewer.toolbar.SharedToolBar;
import org.geocraft.ui.viewer.toolbar.SimpleToolBar;


public class TraceViewer extends AbstractDataViewer implements ITraceViewer, ICursorListener {

  /** The error logger. */
  private static ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(TraceViewer.class);

  /** The trace viewer's model of display properties */
  private final TraceViewerModel _model;

  /** The plotting structure. */
  private IPlot _plot;

  /**
   * Constructs a trace viewer.
   * 
   * @param parent the parent composite.
   * @param title the plot title.
   */
  public TraceViewer(final Composite parent, final String title) {
    super(parent, true, true, true);
    _model = new TraceViewerModel(_plot.getActiveModelSpace());
    _plot.setTitle(title);
  }

  @Override
  protected IRenderer createRenderer(final IConfigurationElement configElement) throws Exception {
    return RendererRegistry.createRenderer(configElement);
  }

  @Override
  protected List<IConfigurationElement> findRenderer(final Shell shell, final Object object) {
    return RendererRegistry.findRenderer(shell, object);
  }

  @Override
  protected String[] getMessageSubscriptionTopics() {
    return new String[] { Topic.CURSOR_LOCATION, Topic.REPOSITORY_OBJECT_UPDATED, Topic.REPOSITORY_OBJECTS_REMOVED,
        Topic.TRACE_SECTION_DISPLAYED };
  }

  @Override
  protected ReadoutInfo getViewReadoutInfo(final double x, final double y) {
    int traceNum = Math.round((float) x);
    float z = (float) y;
    return new ReadoutInfo("Mouse location", new String[] { "Trace", "Z" }, new String[] { traceNum + "", z + "" });
  }

  @Override
  protected void hookContextMenu() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void initializeCanvas(final Composite canvasComposite) {
    // Create the plot structure.
    _plot = new Plot(canvasComposite, initializeModelSpace(), PlotScrolling.NONE);

    // Get the various plot components that will be used.
    // TODO: improve the plot API so this is simpler.
    IModelSpaceCanvas canvas = _plot.getModelSpaceCanvas();

    RGB colorForeground = new RGB(0, 0, 0);
    RGB colorBackground = new RGB(255, 255, 255);

    // Set the background color and grid line properties.
    canvas.setBackgroundColor(colorBackground);
    canvas.setVerticalAxisGridLineProperties(LineStyle.SOLID, colorForeground, 1);
    canvas.setHorizontalAxisGridLineProperties(LineStyle.NONE, colorForeground, 0);
    _plot.setVerticalAxisGridLineDensity(20);
    _plot.setVerticalAxisAnnotationDensity(10);

    // Set the formatter for x-coordinates.
    NumberFormat cursorFormatter = NumberFormat.getIntegerInstance();
    _plot.setCursorFormatterX(cursorFormatter);

    // Initialize the canvas layout model (label margins, etc).
    CanvasLayoutModel layoutModel = _plot.getCanvasLayoutModel();
    // Hide the top and right axes to save screen space.
    layoutModel.setTopAxisHeight(50);
    layoutModel.setTopLabelHeight(0);
    layoutModel.setBottomLabelHeight(0);
    layoutModel.setBottomAxisHeight(0);
    layoutModel.setRightAxisWidth(0);
    layoutModel.setRightLabelWidth(0);
    _plot.updateCanvasLayout(layoutModel);
  }

  @Override
  protected void initializeToolBars() {
    // Add the help action to the shared toolbar.
    SharedToolBar sharedToolbar = getSharedToolBar();
    sharedToolbar.addPushButton(new HelpAction("org.geocraft.ui.traceviewer.MapPlot"));

    // Create a custom toolbar just for the trace viewer.
    SimpleToolBar toolbar = addCustomToolBar();

    // Add a color selector for choosing the background color.
    final ColorSelector colorSelector = toolbar.addColorSelector(getBackgroundViewColor());
    colorSelector.getButton().setToolTipText("Select background color");
    colorSelector.addListener(new IPropertyChangeListener() {

      /**
       * Invoked when a color is chosen in the color selector.
       * 
       * @param event
       *          the property change event.
       */
      public void propertyChange(final PropertyChangeEvent event) {
        RGB newColor = colorSelector.getColorValue();
        setBackgroundViewColor(newColor);
        _plot.getModelSpaceCanvas().setGridLineProperties(LineStyle.NONE, newColor, 0);
      }
    });
  }

  @Override
  protected void initializeViewerSpecificFeatures() {

    IModelSpaceCanvas canvas = _plot.getModelSpaceCanvas();
    canvas.removeCursorListener(_plot);
    canvas.addCursorListener(this);

    canvas.getComposite().addKeyListener(new KeyListener() {

      public void keyPressed(final KeyEvent e) {
        IModelSpace activeModelSpace = _plot.getActiveModelSpace();
        if (e.keyCode == SWT.ARROW_UP) {
          if (activeModelSpace.getAxisY().getScale() == AxisScale.LOG) {
            MessageDialog.openError(getShell(), "Shift Error",
                "Shifting of a logarithmic axis not currently supported.");
            return;
          }
          ModelSpaceBounds bounds = activeModelSpace.getViewableBounds();
          double yStart = bounds.getStartY();
          double yEnd = bounds.getEndY();
          double dist = yEnd - yStart;
          double shift = dist / 4;
          double sign = -1;
          AxisDirection direction = activeModelSpace.getAxisY().getDirection();
          if (direction.equals(AxisDirection.BOTTOM_TO_TOP)) {
            sign = 1;
          }
          yStart += shift * sign;
          yEnd += shift * sign;
          activeModelSpace.setViewableBounds(bounds.getStartX(), bounds.getEndX(), yStart, yEnd);
        } else if (e.keyCode == SWT.ARROW_DOWN) {
          if (activeModelSpace.getAxisY().getScale() == AxisScale.LOG) {
            MessageDialog.openError(getShell(), "Shift Error",
                "Shifting of a logarithmic axis not currently supported.");
            return;
          }
          ModelSpaceBounds bounds = activeModelSpace.getViewableBounds();
          double yStart = bounds.getStartY();
          double yEnd = bounds.getEndY();
          double dist = yEnd - yStart;
          double shift = dist / 4;
          double sign = 1;
          AxisDirection direction = activeModelSpace.getAxisY().getDirection();
          if (direction.equals(AxisDirection.BOTTOM_TO_TOP)) {
            sign = -1;
          }
          yStart += shift * sign;
          yEnd += shift * sign;
          activeModelSpace.setViewableBounds(bounds.getStartX(), bounds.getEndX(), yStart, yEnd);
        } else if (e.keyCode == SWT.ARROW_LEFT) {
          if (activeModelSpace.getAxisX().getScale() == AxisScale.LOG) {
            MessageDialog.openError(getShell(), "Shift Error",
                "Shifting of a logarithmic axis not currently supported.");
            return;
          }
          ModelSpaceBounds bounds = activeModelSpace.getViewableBounds();
          double xStart = bounds.getStartX();
          double xEnd = bounds.getEndX();
          double dist = xEnd - xStart;
          double shift = dist / 4;
          double sign = 1;
          AxisDirection direction = activeModelSpace.getAxisX().getDirection();
          if (direction.equals(AxisDirection.LEFT_TO_RIGHT)) {
            sign = -1;
          }
          xStart += shift * sign;
          xEnd += shift * sign;
          activeModelSpace.setViewableBounds(xStart, xEnd, bounds.getStartY(), bounds.getEndY());
        } else if (e.keyCode == SWT.ARROW_RIGHT) {
          if (activeModelSpace.getAxisX().getScale() == AxisScale.LOG) {
            MessageDialog.openError(getShell(), "Shift Error",
                "Shifting of a logarithmic axis not currently supported.");
            return;
          }
          ModelSpaceBounds bounds = activeModelSpace.getViewableBounds();
          double xStart = bounds.getStartX();
          double xEnd = bounds.getEndX();
          double dist = xEnd - xStart;
          double shift = dist / 4;
          double sign = -1;
          AxisDirection direction = activeModelSpace.getAxisX().getDirection();
          if (direction.equals(AxisDirection.LEFT_TO_RIGHT)) {
            sign = 1;
          }
          xStart += shift * sign;
          xEnd += shift * sign;
          activeModelSpace.setViewableBounds(xStart, xEnd, bounds.getStartY(), bounds.getEndY());
        } else if (e.character == ' ') {
          TreeSelection selection = (TreeSelection) _layerViewer.getSelection();
          Iterator iterator = selection.iterator();
          while (iterator.hasNext()) {
            Object object = iterator.next();
            if (object instanceof IViewLayer) {
              IViewLayer layer = (IViewLayer) object;
              boolean visible = !layer.isVisible();
              layer.setVisible(visible);
              _layerViewer.setChecked(layer, visible);
              _plot.updateAll();
            }
          }
        }

      }

      public void keyReleased(final KeyEvent e) {
        // TODO Auto-generated method stub

      }

    });
  }

  public void addRenderer(final String klass, final Map<String, String> props, final String uniqueId) {
    TraceViewRenderer renderer = null;
    try {
      renderer = RendererRegistry.findRenderer(klass);
    } catch (Exception ex) {
      LOGGER.error("Cannot find renderer " + klass);
      LOGGER.error("  message: " + ex.getMessage());
    }
    if (renderer == null) {
      return;
    }

    // Set the renderer's model properties.
    Model model = renderer.getSettingsModel();
    // Note: if model is null, renderer is TraceSectionRenderer (at least).
    if (model != null) {
      // Set the data entity to be rendered and add renderer to viewer.
      Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
      Map<String, Object> results = ServiceProvider.getRepository().get(new EntityUniqueIdSpecification(uniqueId));
      Object[] objects = results.values().toArray();
      renderer.setData(shell, TraceViewer.this, objects, false);
      model.unpickle(props);
      renderer.refresh();
    }
  }

  /**
   * Returns an array of the current trace view renderers.
   * 
   * @return an array of the current renderers.
   */
  public TraceViewRenderer[] getRenderers() {
    List<TraceViewRenderer> renderers = new ArrayList<TraceViewRenderer>();
    for (IViewLayer viewLayer : getLayerModel().getLayers()) {
      if (viewLayer instanceof RendererViewLayer) {
        IPlotLayer plotLayer = ((RendererViewLayer) viewLayer).getPlotLayer();
        if (plotLayer instanceof TraceViewRenderer) {
          renderers.add((TraceViewRenderer) plotLayer);
        }
      }
    }
    return renderers.toArray(new TraceViewRenderer[0]);
  }

  public TraceViewerModel getViewerModel() {
    return _model;
  }

  @Override
  public void print() {
    // TODO: Implement printing functionality.
  }

  public RGB getBackgroundViewColor() {
    return _plot.getBackgroundPlotColor();
  }

  public void setBackgroundViewColor(final RGB color) {
    _plot.setBackgroundPlotColor(color);
  }

  public void setCursorStyle(final int cursorStyle) {
    _plot.getModelSpaceCanvas().setCursorStyle(cursorStyle);
  }

  public void updateFromModel() {
    _plot.getModelSpaceCanvas().checkAspectRatio();
    _plot.getModelSpaceCanvas().update(UpdateLevel.RESIZE);
    redraw();
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
        // trace viewer is toggled on to receive cursor events.
        if (topic.equals(Topic.CURSOR_LOCATION) && _plot != null && _plot.getCursorReception()) {
          CursorLocation cursorLoc = (CursorLocation) data;
          // Check that the cursor location event did not come
          // from this viewer.
          if (!cursorLoc.isSender("" + hashCode())) {
            // Unpack the x,y coordinates and pass them along to
            // the cursor updated method.
            final Point3d p = cursorLoc.getLocation().getPoint();
            double x = p.getX();
            double y = p.getY();
            _plot.cursorTracked(x, y);
            cursorUpdated(x, y, false);
          }
        }
      }
    });
  }

  @Override
  public void dispose() {
    _plot.getModelSpaceCanvas().removeCursorListener(this);
    _plot.dispose();
    super.dispose();
  }

  @Override
  protected void checkAspectRatio() {
    _plot.getModelSpaceCanvas().checkAspectRatio();
  }

  /**
   * Creates the default model space for the trace viewer. The model space
   * consists of "X" and "Y" axes with units that match the application units
   * for horizontal distance.
   * 
   * @return the created model space.
   */
  private static IModelSpace initializeModelSpace() {
    String modelSpaceName = "Default Trace-Time Space";
    String yAxisName = "Time";
    Unit yUnit = UnitPreferences.getInstance().getTimeUnit();
    IAxis xAxis = new TraceAxis(new AxisRange(1, 10));
    ILabel yAxisLabel = new Label(yAxisName, Orientation.VERTICAL, Alignment.CENTER, true);
    IAxis yAxis = new Axis(yAxisLabel, yUnit, new AxisRange(0, 1000), Orientation.VERTICAL, AxisDirection.TOP_TO_BOTTOM);
    return new ModelSpace(modelSpaceName, xAxis, yAxis);
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
              .getTimeCoordinateSystem()), TimeOrDepth.NONE, "" + hashCode()));
    }
  }

  public void cursorSelectionUpdated(final double x, final double y) {
    List<DataSelection> selectionList = new ArrayList<DataSelection>();
    IPlotLayer[] layers = _plot.getActiveModelSpace().getLayers();
    for (IPlotLayer layer : layers) {
      if (layer instanceof TraceViewRenderer) {
        TraceViewRenderer renderer = (TraceViewRenderer) layer;
        if (renderer.isVisible()) {
          DataSelection selection = renderer.getDataSelection(x, y);
          if (selection != null) {
            selectionList.add(selection);
            renderer.editSettings();
            IViewLayer viewLayer = getLayerModel().getLayer(new RendererSpecification(renderer));
            getLayerViewer().setSelection(new StructuredSelection(viewLayer));
          }
        }
      }
    }
    if (selectionList.size() > 0) {
      ServiceProvider.getMessageService().publish(Topic.DATA_SELECTION, selectionList);
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

  @Override
  protected void setRendererData(final IRenderer renderer, final Shell shell, final Object[] objects,
      final boolean autoUpdate) {
    ((TraceViewRenderer) renderer).setData(shell, this, objects, autoUpdate);
  }

  /**
   * Adds objects to the viewer.
   * 
   * @param objects the array of objects to add.
   * @param block
   */
  @Override
  public void addObjects(final boolean block, final Object... objects) {
    boolean hasRenderers = false;
    IRenderer[] renderers = getRenderers();
    if (renderers != null && renderers.length > 0) {
      hasRenderers = true;
    }
    final boolean autoUpdate = !hasRenderers;
    final Shell shell = getShell();
    final BackgroundTask task = new BackgroundTask() {

      /**
       * The task for adding entities to the viewer.
       * @param logger the logger.
       * @param monitor the progress monitor. 
       */
      @Override
      public Void compute(final ILogger logger, final IProgressMonitor monitor) {
        // Begin the task.
        monitor.beginTask("Adding objects to the viewer", 1 + objects.length);
        try {
          Display.getDefault().syncExec(new Runnable() {

            public void run() {

              // Lookup renderers for the individual objects.
              for (final Object obj : objects) {
                final Object object = obj;
                if (object instanceof TraceData) {
                  boolean rendererExists = false;
                  final IRenderer newRenderer = new TraceDataRenderer();
                  for (final IRenderer renderer : getRenderers()) {
                    if (renderer.getClass().equals(newRenderer.getClass())) {
                      if (renderer.getRenderedObjects()[0].equals(object)) {
                        rendererExists = true;
                        break;
                      }
                    }
                  }
                  if (!rendererExists) {
                    if (Entity.class.isAssignableFrom(object.getClass())) {
                      Entity entity = (Entity) object;
                      try {
                        entity.load();
                      } catch (final Exception ex) {
                        ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.getMessage(), ex);
                      }
                    }
                    setRendererData(newRenderer, shell, new Object[] { object }, autoUpdate);
                    //newRenderer.setData(shell, AbstractDataViewer.this, new Object[] { object });
                  }
                } else if (object instanceof PicksData) {
                  boolean rendererExists = false;
                  final IRenderer newRenderer = new PicksRenderer();
                  for (final IRenderer renderer : getRenderers()) {
                    if (renderer.getClass().equals(newRenderer.getClass())) {
                      if (renderer.getRenderedObjects()[0].equals(object)) {
                        rendererExists = true;
                        break;
                      }
                    }
                  }
                  if (!rendererExists) {
                    if (Entity.class.isAssignableFrom(object.getClass())) {
                      Entity entity = (Entity) object;
                      try {
                        entity.load();
                      } catch (final Exception ex) {
                        ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.getMessage(), ex);
                      }
                    }
                    setRendererData(newRenderer, shell, new Object[] { object }, autoUpdate);
                    //newRenderer.setData(shell, AbstractDataViewer.this, new Object[] { object });
                  }
                }

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
        // Lastly, trigger a redraw of all the renderers.
        Display.getDefault().syncExec(new Runnable() {

          public void run() {
            checkAspectRatio();
            for (final IRenderer renderer : getRenderers()) {
              renderer.redraw();
            }
          }
        });
        monitor.worked(1);
        return null;
      }
    };

    if (block) {
      // If blocking the UI, run the task with the default JOIN flag.
      TaskRunner.runTask(task, "Add entities to the viewer");
    } else {
      // Otherwise, run it in another thread.
      new Thread(new Runnable() {

        public void run() {
          TaskRunner.runTask(task, "Add entities to the viewer");
        }
      }).start();
    }
  }

  public IModelSpaceCanvas getModelSpaceCanvas() {
    return _plot.getModelSpaceCanvas();
  }

  public void addTraces(final Trace[] traces) {
    addTraces(new TraceData(traces));
  }

  public void addTraces(final TraceData traceData) {
    addObjects(new Object[] { traceData });
  }

  public void addPicks(final float[] picks, final String name, final RGB color) {
    addObjects(new Object[] { new PicksData(picks, name, color) });
  }

  public void addPicks(final PicksData picksObject) {
    addObjects(new Object[] { picksObject });
  }
}
