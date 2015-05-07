/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.waveletviewer;


import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.geocraft.core.common.preferences.PropertyStoreFactory;
import org.geocraft.core.common.progress.BackgroundTask;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.specification.EntityUniqueIdSpecification;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.core.service.message.Topic;
import org.geocraft.internal.ui.waveletviewer.RendererRegistry;
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
import org.geocraft.ui.viewer.AbstractDataViewer;
import org.geocraft.ui.viewer.IRenderer;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.viewer.action.HelpAction;
import org.geocraft.ui.viewer.layer.FolderLayer;
import org.geocraft.ui.viewer.layer.ILayeredModel;
import org.geocraft.ui.viewer.layer.IViewLayer;
import org.geocraft.ui.viewer.toolbar.SharedToolBar;
import org.geocraft.ui.viewer.toolbar.SimpleToolBar;
import org.geocraft.ui.waveletviewer.renderer.PhaseSpectrumRenderer;
import org.geocraft.ui.waveletviewer.renderer.WaveletViewRenderer;


public class PhaseSpectrumViewer extends AbstractDataViewer implements IWaveletViewer, ICursorListener {

  /** The logger. */
  private static ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(PhaseSpectrumViewer.class);

  /** The phase spectrum  viewer's model of display properties */
  private PhaseSpectrumViewerModel _model;

  /** The plotting structure. */
  private IPlot _plot;

  private static IPreferenceStore _preferenceStore = PropertyStoreFactory.getStore(PreferencePage.ID);

  public PhaseSpectrumViewer(final Composite parent, final String title) {
    super(parent, true, true, false, false);
    _model = new PhaseSpectrumViewerModel(_plot.getActiveModelSpace());
    _plot.setTitle(title);
    setBounds();
  }

  public void cursorSelectionUpdated(double x, double y) {
    // Nothing to do.
  }

  @Override
  protected ReadoutInfo getViewReadoutInfo(double x, double y) {
    IModelSpace modelSpace = _plot.getActiveModelSpace();
    String xLabel = modelSpace.getAxisX().getLabel().getText();
    String yLabel = modelSpace.getAxisY().getLabel().getText();
    String[] keys = { xLabel, yLabel };
    String[] values = { "" + x, "" + y };
    return new ReadoutInfo("Mouse Location", keys, values);
  }

  @Override
  protected void initializeCanvas(final Composite canvasComposite) {
    // Create the plot structure.
    _plot = new Plot(canvasComposite, initializeModelSpace(), PlotScrolling.NONE);

    // Initialize the canvas layout model (label margins, etc).
    CanvasLayoutModel layoutModel = _plot.getCanvasLayoutModel();
    layoutModel.setTopLabelHeight(0);
    layoutModel.setTopAxisHeight(0);
    layoutModel.setRightLabelWidth(0);
    layoutModel.setRightAxisWidth(0);
    _plot.updateCanvasLayout(layoutModel);

    NumberFormat cursorFormatter = NumberFormat.getNumberInstance();
    cursorFormatter.setGroupingUsed(false);
    _plot.setCursorFormatterX(cursorFormatter);
    _plot.setCursorFormatterY(cursorFormatter);
    _plot.setHorizontalAxisGridLineDensity(10);
    _plot.setVerticalAxisGridLineDensity(10);
    _plot.setHorizontalAxisAnnotationDensity(10);
    _plot.setVerticalAxisAnnotationDensity(10);
  }

  @Override
  protected void initializeLayeredModel(final ILayeredModel model, final Map<String, IViewLayer> folderLayers) {
    folderLayers.put(WAVELET_FOLDER, new FolderLayer(WAVELET_FOLDER, "Folder containing wavelets and spectra"));
    for (final IViewLayer layer : folderLayers.values()) {
      model.addLayer(layer);
    }
  }

  @Override
  protected void initializeToolBars() {
    // Add the help action to the shared toolbar.
    SharedToolBar sharedToolbar = getSharedToolBar();
    sharedToolbar.addPushButton(new HelpAction("org.geocraft.ui.waveletviewer.WaveletPlot"));
    SimpleToolBar emptyToolbar = addCustomToolBar();
    emptyToolbar.addLabel("");
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
  public IRenderer[] getRenderers() {
    return getWaveletViewRenderers();
  }

  private WaveletViewRenderer[] getWaveletViewRenderers() {
    List<WaveletViewRenderer> renderers = new ArrayList<WaveletViewRenderer>();
    for (IViewLayer viewLayer : getLayerModel().getLayers()) {
      if (viewLayer instanceof RendererViewLayer) {
        IPlotLayer plotLayer = ((RendererViewLayer) viewLayer).getPlotLayer();
        if (plotLayer instanceof WaveletViewRenderer) {
          renderers.add((WaveletViewRenderer) plotLayer);
        }
      }
    }
    return renderers.toArray(new WaveletViewRenderer[0]);
  }

  /**
   * Adds objects to the amplitude spectrum viewer.
   * Only objects that are supported by a registered renderer will be added.
   * The others will be ignored.
   * @param objects the array of objects to add.
   */
  @Override
  public void addObjects(final Object[] objects) {
    addObjects(objects, false);
  }

  /**
   * Adds objects to the amplitude spectrum viewer.
   * Only objects that are supported by a registered renderer will be added.
   * The others will be ignored.
   * @param objects the array of objects to add.
   * @param block <i>true</i> to block the UI thread; otherwise <i>false</i>.
   */
  public void addObjects(final Object[] objects, final boolean block) {
    loadEntities(objects);
    final Shell shell = getShell();
    final BackgroundTask task = new BackgroundTask() {

      /**
       * The task for adding entities to the amplitude spectrum viewer.
       * @param logger the logger.
       * @param monitor the progress monitor. 
       */
      @Override
      public Void compute(final ILogger logger, final IProgressMonitor monitor) {
        // Begin the task.
        monitor.beginTask("Adding objects to the amplitude spectrum viewer", 1 + objects.length);
        try {
          // First, lookup renderers for the combination of objects.
          Display.getDefault().syncExec(new Runnable() {

            public void run() {

              final List<IConfigurationElement> configsMulti = RendererRegistry.findRenderer(shell, objects,
                  PHASE_SPECTRUM_SUBPLOT);
              for (IConfigurationElement config : configsMulti) {
                //AbstractRenderer rendererMulti = RendererRegistry.selectRenderer("Multi-Object Renderer", configsMulti,
                //    false);
                try {
                  WaveletViewRenderer rendererMulti = RendererRegistry.createRenderer(config);
                  rendererMulti.setData(shell, PhaseSpectrumViewer.this, objects);
                } catch (Exception ex) {
                  LOGGER.error("Error creating renderer for objects", ex);
                }
              }

              // Next, lookup renderers for the individual objects.
              for (Object obj : objects) {
                final Object object = obj;
                final List<IConfigurationElement> configsSingle = RendererRegistry.findRenderer(shell, object,
                    PHASE_SPECTRUM_SUBPLOT);
                for (IConfigurationElement config : configsSingle) {
                  //AbstractRenderer rendererSingle = RendererRegistry.selectRenderer("Renderer: " + object.toString(),
                  //    configsSingle, !multiRendererSelected)
                  try {
                    boolean rendererExists = false;
                    WaveletViewRenderer newRenderer = RendererRegistry.createRenderer(config);
                    for (WaveletViewRenderer renderer : getWaveletViewRenderers()) {
                      if (renderer.getClass().equals(newRenderer.getClass())) {
                        if (renderer.getRenderedObjects()[0].equals(object)) {
                          rendererExists = true;
                          break;
                        }
                      }
                    }
                    if (!rendererExists) {
                      newRenderer.setData(shell, PhaseSpectrumViewer.this, new Object[] { object });
                    }
                  } catch (Exception ex) {
                    LOGGER.error("Error creating renderer for " + object.toString(), ex);
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
            for (WaveletViewRenderer renderer : getWaveletViewRenderers()) {
              renderer.redraw();
            }
            setBounds();
          }
        });
        monitor.worked(1);
        return null;
      }
    };

    if (block) {
      // If blocking the UI, run the task with the default JOIN flag.
      TaskRunner.runTask(task, "Add entities to the amplitude spectrum viewer");
    } else {
      // Otherwise, run it in another thread.
      new Thread(new Runnable() {

        public void run() {
          TaskRunner.runTask(task, "Add entities to the amplitude spectrum viewer");
        }
      }).start();
    }
  }

  private void setBounds() {
    int maxFrequency = _preferenceStore.getInt(PreferencePage.MAXIMUM_FREQUENCY);
    int minPhase = _preferenceStore.getInt(PreferencePage.MINIMUM_PHASE);
    int maxPhase = _preferenceStore.getInt(PreferencePage.MAXIMUM_PHASE);
    IModelSpace modelSpace = _plot.getActiveModelSpace();
    modelSpace.setViewableBounds(0, maxFrequency, minPhase, maxPhase);
    modelSpace.setDefaultBounds(0, maxFrequency, minPhase, maxPhase);
  }

  private static IModelSpace initializeModelSpace() {
    String modelSpaceName = "Phase vs Frequency";
    Unit xUnit = Unit.HERTZ;
    String xLabelStr = "Frequency (" + xUnit.getSymbol() + ")";
    ILabel xAxisLabel = new Label(xLabelStr, Orientation.HORIZONTAL, Alignment.CENTER, true);
    IAxis xAxis = new Axis(xAxisLabel, xUnit, new AxisRange(0, 250), Orientation.HORIZONTAL);
    ILabel yAxisLabel = new Label("Phase", Orientation.VERTICAL, Alignment.CENTER, true);
    IAxis yAxis = new Axis(yAxisLabel, Unit.DEGREE_OF_AN_ANGLE, new AxisRange(-180, 180), Orientation.VERTICAL);
    return new ModelSpace(modelSpaceName, xAxis, yAxis);
  }

  public Model getViewerModel() {
    return _model;
  }

  public void updateFromModel() {
    _plot.getModelSpaceCanvas().checkAspectRatio();
    _plot.getModelSpaceCanvas().update(UpdateLevel.RESIZE);
    this.redraw();
  }

  public void addRenderer(String klass, Map<String, String> props, String uniqueId) {
    WaveletViewRenderer renderer = null;
    try {
      renderer = RendererRegistry.findRenderer(klass);
    } catch (Exception ex) {
      LOGGER.error("Cannot find renderer " + klass);
      LOGGER.error("  message: " + ex.getMessage());
    }
    if (renderer == null) {
      return;
    }

    //set the data entity to be rendered and add renderer to viewer
    Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    Map<String, Object> results = ServiceProvider.getRepository().get(new EntityUniqueIdSpecification(uniqueId));
    Object[] objects = results.values().toArray();
    renderer.setData(shell, PhaseSpectrumViewer.this, objects, false);
    //set the renderer's model properties
    Model model = renderer.getSettingsModel();
    //Note: if model is null, renderer is TraceSectionRenderer (at least)
    if (model != null) {
      model.unpickle(props);
    }
  }

  public void zoomIn() {
    _plot.zoom(_plot.getZoomFactor());
  }

  public void zoomOut() {
    _plot.zoom(1 / _plot.getZoomFactor());
  }

  public void zoomWindow(boolean enabled) {
    if (enabled) {
      _plot.setMouseActions(PlotMouseActionList.getDefaultZoomWindowActions().getActions(), SWT.CURSOR_CROSS);
    } else {
      _plot.setMouseActions(new IPlotMouseAction[0], SWT.CURSOR_ARROW);
    }
  }

  public void pan(boolean enabled) {
    if (enabled) {
      _plot.setMouseActions(PlotMouseActionList.getDefaultPanActions().getActions(), SWT.CURSOR_HAND);
    } else {
      _plot.setMouseActions(new IPlotMouseAction[0], SWT.CURSOR_ARROW);
    }
  }

  public void home() {
    _plot.unzoom();
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

  public IPlot getPlot() {
    return _plot;
  }

  @Override
  protected String[] getMessageSubscriptionTopics() {
    return new String[] { Topic.REPOSITORY_OBJECT_UPDATED, Topic.REPOSITORY_OBJECTS_REMOVED };
  }

  @Override
  protected void hookContextMenu() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void initializeViewerSpecificFeatures() {
    IModelSpaceCanvas canvas = _plot.getModelSpaceCanvas();
    canvas.removeCursorListener(_plot);
    canvas.addCursorListener(this);

    canvas.getComposite().addKeyListener(new KeyAdapter() {

      @Override
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

    });
  }

  @Override
  protected IRenderer createRenderer(IConfigurationElement configElement) throws Exception {
    return RendererRegistry.createRenderer(configElement);
  }

  @Override
  protected List<IConfigurationElement> findRenderer(Shell shell, Object object) {
    return RendererRegistry.findRenderer(shell, object, PHASE_SPECTRUM_SUBPLOT);
  }

  @Override
  protected void setRendererData(IRenderer renderer, Shell shell, Object[] objects, boolean autoUpdate) {
    ((PhaseSpectrumRenderer) renderer).setData(shell, this, objects, autoUpdate);
  }

  @Override
  protected void updateAll() {
    _plot.updateAll();
  }

  @Override
  protected void checkAspectRatio() {
    _plot.getModelSpaceCanvas().checkAspectRatio();
  }
}
