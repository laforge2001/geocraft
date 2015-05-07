/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer;


import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.common.progress.BackgroundTask;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.datatypes.Coordinate;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.TraceAxisKey;
import org.geocraft.core.model.datatypes.TraceSectionSelection;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.event.CursorLocation;
import org.geocraft.core.model.event.CursorLocation.TimeOrDepth;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.core.model.preferences.ApplicationPreferences;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.model.seismic.SeismicLine2d;
import org.geocraft.core.model.seismic.SeismicSurvey2d;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.model.seismic.TraceSection;
import org.geocraft.core.model.seismic.TraceSection.SectionType;
import org.geocraft.core.model.seismic.TraceSectionEvent;
import org.geocraft.core.model.specification.EntityUniqueIdSpecification;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.core.service.message.Topic;
import org.geocraft.ui.plot.IAxisRangeCanvas;
import org.geocraft.ui.plot.ICanvas;
import org.geocraft.ui.plot.ICornerCanvas;
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
import org.geocraft.ui.plot.defs.AxisPlacement;
import org.geocraft.ui.plot.defs.AxisScale;
import org.geocraft.ui.plot.defs.CanvasType;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.Orientation;
import org.geocraft.ui.plot.label.ILabel;
import org.geocraft.ui.plot.label.Label;
import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.plot.listener.ICursorListener;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.model.ModelSpace;
import org.geocraft.ui.plot.model.ModelSpaceBounds;
import org.geocraft.ui.sectionviewer.action.DecrementSection;
import org.geocraft.ui.sectionviewer.action.IncrementSection;
import org.geocraft.ui.sectionviewer.action.NavigationAction;
import org.geocraft.ui.sectionviewer.action.RedrawSection;
import org.geocraft.ui.sectionviewer.action.ToggleGroupEnableAction;
import org.geocraft.ui.sectionviewer.component.TraceAxisCornerRenderer;
import org.geocraft.ui.sectionviewer.component.TraceAxisRangeRenderer;
import org.geocraft.ui.sectionviewer.factory.PostStack3dSectionFactory;
import org.geocraft.ui.sectionviewer.factory.PreStack3dSectionFactory;
import org.geocraft.ui.sectionviewer.navigation.PostStack2dNavigationAction;
import org.geocraft.ui.sectionviewer.navigation.PostStack3dNavigationAction;
import org.geocraft.ui.sectionviewer.navigation.PreStack3dNavigationAction;
import org.geocraft.ui.sectionviewer.navigation.ZRangeSelectionAction;
import org.geocraft.ui.viewer.AbstractDataViewer;
import org.geocraft.ui.viewer.IRenderer;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.viewer.RendererSpecification;
import org.geocraft.ui.viewer.action.HelpAction;
import org.geocraft.ui.viewer.layer.IViewLayer;
import org.geocraft.ui.viewer.toolbar.SharedToolBar;
import org.geocraft.ui.viewer.toolbar.SimpleToolBar;


public class SectionViewer extends AbstractDataViewer implements ISectionViewer, ICursorListener {

  /** The error logger. */
  private static ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(SectionViewer.class);

  /** The section viewer's model of display properties */
  private final SectionViewerModel _model;

  /** The plotting structure. */
  private IPlot _plot;

  /** The tool bar button for selecting the z-range to display. */
  private ToolItem _zRangeSelectionButton;

  /** The toolbar control for selecting the background color. */
  private ColorSelector _colorSelector;

  /** The toolbar button to increment the inline/xline section. */
  private ToolItem _incrementSection;

  /** The toolbar button to decrement the inline/xline section. */
  private ToolItem _decrementSection;

  /** The toolbar button to run the navigation action. */
  private ToolItem _navigationButton;

  /** The action to run when the toolbar navigator button is pressed. */
  private Action _navigationAction;

  private Combo _toggleGroupCombo;

  private boolean _toggleGroupsEnabled = false;

  /**
   * Constructs a section viewer.
   * 
   * @param parent the parent composite.
   * @param title the plot title.
   */
  public SectionViewer(final Composite parent, final String title) {
    super(parent, true, true, true);
    _model = new SectionViewerModel(_plot.getActiveModelSpace());
    _plot.setTitle(title);
  }

  @Override
  protected void checkAspectRatio() {
    // Does not apply for section viewer.
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
    return new String[] { Topic.CURSOR_LOCATION, Topic.DATA_SELECTION, Topic.REPOSITORY_OBJECT_UPDATED,
        Topic.TRACE_SECTION_SELECTED, "EntityChangeEvent" };
  }

  @Override
  protected ReadoutInfo getViewReadoutInfo(final double x, final double y) {
    int traceNum = Math.round((float) x);
    float z = (float) y;
    int traceIndex = traceNum - 1;
    String[] keys = new String[] { "Trace", "X", "Y", "Z" };
    if (_model.getTraceSection() != null && traceIndex >= 0 && traceIndex < _model.getTraceSection().getNumTraces()) {
      Point3d point = _model.getTraceSection().getPointsXY()[traceIndex];
      double rwx = point.getX();
      double rwy = point.getY();
      String[] vals = new String[] { "" + (traceIndex + 1), "" + rwx, "" + rwy, "" + z };
      return new ReadoutInfo("Mouse Location", keys, vals);
    }
    return new ReadoutInfo("Mouse Location", keys, new String[] { "-", "-", "-", "-" });
  }

  @Override
  protected void hookContextMenu() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void initializeCanvas(final Composite canvasComposite) {
    RGB colorForeground = new RGB(0, 0, 0);
    RGB colorBackground = new RGB(255, 255, 255);

    // Create the plot structure.
    _plot = new Plot(canvasComposite, initializeModelSpace(), PlotScrolling.BOTH);

    IModelSpaceCanvas canvas = _plot.getModelSpaceCanvas();

    // Set the background color and grid line properties.
    canvas.setBackgroundColor(colorBackground);
    canvas.setVerticalAxisGridLineProperties(LineStyle.SOLID, colorForeground, 1);
    canvas.setHorizontalAxisGridLineProperties(LineStyle.NONE, colorForeground, 0);
    canvas.setVerticalAxisGridLineDensity(20);

    _plot.setVerticalAxisGridLineDensity(20);
    _plot.setVerticalAxisAnnotationDensity(10);

    // Set the formatter for x-coordinates.
    NumberFormat cursorFormatter = NumberFormat.getIntegerInstance();
    _plot.setCursorFormatterX(cursorFormatter);

    Map<CanvasType, ICanvas> canvasMap = _plot.getPlotComposite().getCanvasMap();

    // Replace the default renderer in the top axis range canvas with a custom one.
    IAxisRangeCanvas topRangeCanvas = (IAxisRangeCanvas) canvasMap.get(CanvasType.TOP_AXIS_RANGE);
    topRangeCanvas.setRenderer(new TraceAxisRangeRenderer(this, topRangeCanvas, _plot.getModelSpaces()[0].getAxisX(),
        AxisPlacement.TOP));

    // Replace the default renderers in the top corners with custom ones.
    ICornerCanvas topLeftCanvas = (ICornerCanvas) canvasMap.get(CanvasType.TOP_LEFT_CORNER);
    topLeftCanvas.setRenderer(new TraceAxisCornerRenderer(this, AxisPlacement.LEFT));
    ICornerCanvas topRightCanvas = (ICornerCanvas) canvasMap.get(CanvasType.TOP_RIGHT_CORNER);
    topRightCanvas.setRenderer(new TraceAxisCornerRenderer(this, AxisPlacement.RIGHT));
  }

  /**
   * Creates the default model space for the section viewer.
   * The model space consists of "Trace" and "Z" axes.
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

  @Override
  protected void initializeToolBars() {
    // Add the help action to the shared toolbar.
    SharedToolBar sharedToolbar = getSharedToolBar();
    sharedToolbar.addPushButton(new HelpAction("org.geocraft.ui.sectionviewer.sectionplot"));

    // Add a custom toolbar.
    SimpleToolBar sectionToolBar = addCustomToolBar();

    // Add a background color selector to the custom toolbar.
    _colorSelector = sectionToolBar.addColorSelector(new RGB(255, 255, 255));
    _colorSelector.addListener(new IPropertyChangeListener() {

      public void propertyChange(final PropertyChangeEvent event) {
        RGB newColor = _colorSelector.getColorValue();
        setBackgroundViewColor(newColor);
      }
    });
    _colorSelector.getButton().setToolTipText("Select background color");

    // Add a custom toolbar button for redrawing the section.
    sectionToolBar.addPushButton(new RedrawSection(this));

    // Add custom toolbar prev/next and navigation buttons.
    _decrementSection = sectionToolBar.addPushButton(new DecrementSection(this));
    _incrementSection = sectionToolBar.addPushButton(new IncrementSection(this));
    _decrementSection.setEnabled(false);
    _incrementSection.setEnabled(false);
    _navigationButton = sectionToolBar.addPushButton(new NavigationAction(this));
    _navigationButton.setEnabled(false);

    // Add a custom toolbar button for selecting the z-range.
    _zRangeSelectionButton = sectionToolBar.addPushButton(new ZRangeSelectionAction(this));
    _zRangeSelectionButton.setEnabled(false);

    // Add a custom toolbar button for selecting the active toggle frames.
    sectionToolBar.addToggleButton(new ToggleGroupEnableAction(this, true), new ToggleGroupEnableAction(this, false));

    final String[] toggleGroupIds = { "1", "2", "3", "4", "5", "6", "7", "8", "9" };
    _toggleGroupCombo = sectionToolBar.addCombo("Group", toggleGroupIds);
    _toggleGroupCombo.select(0);
    _toggleGroupCombo.setVisible(false);
    _toggleGroupCombo.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        int id = 1 + _toggleGroupCombo.getSelectionIndex();
        setToggleGroup(id);
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

  @Override
  protected void setRendererData(final IRenderer renderer, final Shell shell, final Object[] objects,
      final boolean autoUpdate) {
    ((SectionViewRenderer) renderer).setData(shell, SectionViewer.this, objects, autoUpdate);
  }

  @Override
  protected void updateAll() {
    _plot.updateAll();
  }

  public void addRenderer(final String klass, final Map<String, String> props, final String uniqueId) {
    SectionViewRenderer renderer = null;
    try {
      renderer = RendererRegistry.findRenderer(klass);
    } catch (Exception ex) {
      LOGGER.error("Cannot find renderer " + klass);
      LOGGER.error("  message: " + ex.getMessage());
    }
    if (renderer == null) {
      return;
    }

    //set the renderer's model properties
    Model model = renderer.getSettingsModel();
    //set the data entity to be rendered and add renderer to viewer
    Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    Map<String, Object> results = ServiceProvider.getRepository().get(new EntityUniqueIdSpecification(uniqueId));
    Object[] objects = results.values().toArray();
    renderer.setData(shell, SectionViewer.this, objects, false);
    model.unpickle(props);
    renderer.refresh();
  }

  public SectionViewRenderer[] getRenderers() {
    List<SectionViewRenderer> renderers = new ArrayList<SectionViewRenderer>();
    for (IViewLayer viewLayer : getLayerModel().getLayers()) {
      if (viewLayer instanceof RendererViewLayer) {
        IPlotLayer plotLayer = ((RendererViewLayer) viewLayer).getPlotLayer();
        if (plotLayer instanceof SectionViewRenderer) {
          renderers.add((SectionViewRenderer) plotLayer);
        }
      }
    }
    return renderers.toArray(new SectionViewRenderer[0]);
  }

  public SectionViewerModel getViewerModel() {
    return _model;
  }

  @Override
  public void print() {
    // TODO Auto-generated method stub

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
    //_plot.getModelSpaceCanvas().checkAspectRatio();
    //_plot.getModelSpaceCanvas().update(UpdateLevel.RESIZE);
    //redraw();

    /* TOO LATE to update model space. Restoring renderers will update the viewable 
     * bounds and default viewable bounds. Must update model space when unpickle.
        AxisRange xAxisRange = _model.getXAxisRange();
        AxisRange yAxisRange = _model.getYAxisRange();
        getModelSpace().setViewableBounds(xAxisRange.getStart(), xAxisRange.getEnd(), yAxisRange.getStart(),
            yAxisRange.getEnd());
        AxisRange xAxisDefaultRange = _model.getXAxisDefaultRange();
        AxisRange yAxisDefaultRange = _model.getYAxisDefaultRange();
        getModelSpace().setDefaultBounds(xAxisDefaultRange.getStart(), xAxisDefaultRange.getEnd(),
            yAxisDefaultRange.getStart(), yAxisDefaultRange.getEnd());
    */
    setTraceSection(_model.getTraceSection(), true);
    //getModelSpace().updated();

    //restore the viewer's buttons
    SeismicDataset dataset = _model.getReferenceDataset();
    if (dataset instanceof PostStack3d) {
      PostStack3d poststack = (PostStack3d) dataset;
      setNavigationAction(new PostStack3dNavigationAction(poststack, SectionViewer.this));
    } else if (dataset instanceof PreStack3d) {
      final PreStack3d prestack = (PreStack3d) dataset;
      setNavigationAction(new PreStack3dNavigationAction(prestack, SectionViewer.this));
    } else if (dataset instanceof PostStack2dLine) {
      PostStack2dLine poststack = (PostStack2dLine) dataset;
      setNavigationAction(new PostStack2dNavigationAction(poststack.getPostStack(), SectionViewer.this));
    } // Enable/disable the increment/decrement and z range selection buttons.
    boolean enableIncrementDecrement = false;
    if (_model.getTraceSection() != null) {
      SectionType sectionType = _model.getTraceSection().getSectionType();
      if (sectionType.equals(SectionType.INLINE_SECTION) || sectionType.equals(SectionType.XLINE_SECTION)) {
        enableIncrementDecrement = true;
      }
    }
    _decrementSection.setEnabled(enableIncrementDecrement);
    _incrementSection.setEnabled(enableIncrementDecrement);
    _zRangeSelectionButton.setEnabled(true);
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
    if (_plot != null && _plot.getCursorReception()) {
      if (topic.equals(Topic.CURSOR_LOCATION) && _model.getTraceSection() != null) {
        CursorLocation cursorLoc = (CursorLocation) message;
        final double x = cursorLoc.getLocation().getX();
        final double y = cursorLoc.getLocation().getY();
        if (!cursorLoc.isSender(getViewerID())) {
          final double z = cursorLoc.getLocation().getZ();
          float offset = cursorLoc.getOffset();
          Point3d[] points = _model.getTraceSection().getPointsXY();
          float[] offsets = new float[_model.getTraceSection().getNumTraces()];
          if (_model.getTraceSection().containsTraceAxisKey(TraceAxisKey.OFFSET)) {
            offsets = _model.getTraceSection().getTraceAxisKeyValues(TraceAxisKey.OFFSET);
          }
          int traceIndexNearest = -1;
          double minDistance = Double.MAX_VALUE;
          for (int traceIndex = 0; traceIndex < points.length; traceIndex++) {
            final Point3d point = points[traceIndex];
            if (point == null) {
              return;
            }
            if (MathUtil.isEqual(point.getX(), x, 0.001) && MathUtil.isEqual(point.getY(), y, 0.001)
                && MathUtil.isEqual(offsets[traceIndex], offset)) {
              double dx = point.getX() - x;
              double dy = point.getY() - y;
              double distance = dx * dx + dy * dy;
              if (distance < minDistance) {
                minDistance = distance;
                traceIndexNearest = traceIndex;
              }

            }
          }
          if (traceIndexNearest != -1) {
            final double trace = traceIndexNearest + 1;
            Display.getDefault().asyncExec(new Runnable() {

              public void run() {
                _plot.cursorTracked(trace, z);
                cursorUpdated(trace, z, false);
              }
            });
          }
        }
      } else if (topic.equals(Topic.DATA_SELECTION)) {
        List<DataSelection> selectionList = (List<DataSelection>) message;
        for (DataSelection selection : selectionList) {
          Object[] selectedObjects = selection.getSelectedObjects();
          float[] selectedPosition = selection.getSelectedPosition();
          for (Object selectedObject : selectedObjects) {
            SectionViewRenderer renderer = findRendererForSelectedObject(selectedObject);
            if (renderer != null) {
              renderer.setCurrentPosition(selectedPosition);
            }
          }
        }
      } else if (topic.equals(Topic.TRACE_SECTION_SELECTED)) {
        TraceSectionSelection selection = (TraceSectionSelection) message;
        applyTraceSectionSelection(selection);
      }
    } else if (topic.equals(Topic.REPOSITORY_OBJECT_UPDATED) && message != null) {
      Display.getDefault().asyncExec(new Runnable() {

        public void run() {
          boolean redraw = false;
          for (SectionViewRenderer renderer : getRenderers()) {
            for (Object renderedObject : renderer.getRenderedObjects()) {
              if (renderedObject != null && renderedObject.equals(message)) {
                renderer.redraw();
                redraw = true;
                break;
              }
            }
          }
          if (redraw) {
            _plot.updateAll();
          }
        }
      });
    }
  }

  /**
   * Returns the renderer for a specified object.
   * If the object is not associated with a renderer (i.e. it is not currently
   * displayed in the section viewer), then <i>null</i> will be returned.
   * 
   * @param object the entity on which to search.
   * @return the associated renderer.
   */
  private SectionViewRenderer findRendererForSelectedObject(final Object object) {
    SectionViewRenderer objectRenderer = null;
    for (SectionViewRenderer renderer : getRenderers()) {
      if (objectRenderer == null && renderer.getRenderedObjects()[0].equals(object)) {
        objectRenderer = renderer;
      }
    }
    return objectRenderer;
  }

  private void applyTraceSectionSelection(final TraceSectionSelection selection) {
    SeismicSurvey2d referenceSurvey2d = getSeismicSurvey2d();
    if (selection.is2D() && referenceSurvey2d != null && selection.getSurvey2d().equals(referenceSurvey2d)) {
      int lineNumber = selection.getLineNumber2d();
      // Handle 2D prestack and poststack datasets.
      int numOffsets = 1;
      float offsetStart = 0;
      float offsetDelta = 0;
      Domain domain = Domain.TIME;
      FloatRange zRange = new FloatRange(0, 0, 1);
      SeismicLine2d seismicLine = referenceSurvey2d.getLineByNumber(lineNumber);
      int numBins = seismicLine.getNumBins();
      Point3d[] controlPoints = new Point3d[numBins];
      for (int bin = 0; bin < numBins; bin++) {
        double[] xy = seismicLine.transformBinToXY(bin);
        controlPoints[bin] = new Point3d(xy[0], xy[1], 0);
      }
      if (_model.getReferenceDataset() instanceof PostStack2dLine) {
        PostStack2dLine poststack = (PostStack2dLine) _model.getReferenceDataset();
        numOffsets = 1;
        domain = poststack.getZDomain();
        zRange = poststack.getZRange();
      } else {
        return;
      }
      int numPanels = 1;
      int numTraces = 0;
      Point3d[][] panelPoints = new Point3d[numPanels][];
      for (int i = 0; i < numPanels; i++) {
        panelPoints[i] = controlPoints;
        numTraces += panelPoints[i].length * numOffsets;
      }
      Point3d[] tracePoints = new Point3d[numTraces];
      int[] panelIndices = new int[numTraces];
      TraceAxisKey[] traceAxisKeys = { TraceAxisKey.INLINE, TraceAxisKey.CDP, TraceAxisKey.OFFSET };
      float[][] traceAxisKeyValues = new float[numTraces][3];
      int index = 0;
      int panelIndex = 0;
      int numPointsPerPanel = panelPoints[panelIndex].length;
      for (int pointIndex = 0; pointIndex < numPointsPerPanel; pointIndex++) {
        for (int offsetIndex = 0; offsetIndex < numOffsets; offsetIndex++) {
          double x = panelPoints[panelIndex][pointIndex].getX();
          double y = panelPoints[panelIndex][pointIndex].getY();
          tracePoints[index] = new Point3d(x, y, 0);
          panelIndices[index] = panelIndex;
          int bin = pointIndex;
          float cdp = seismicLine.transformBinToCdp(bin);
          traceAxisKeyValues[index][0] = lineNumber;
          traceAxisKeyValues[index][1] = cdp;
          traceAxisKeyValues[index][2] = offsetStart + offsetIndex * offsetDelta;
          index++;
        }
      }
      TraceSection section = new TraceSection(controlPoints, tracePoints, panelIndices, traceAxisKeys,
          traceAxisKeyValues, domain, zRange.getStart(), zRange.getEnd());
      setTraceSection(section);
    } else {
      SeismicSurvey3d referenceSurvey3d = getReferenceSurvey3d();
      if (selection.is3D() && referenceSurvey3d != null && selection.getSurvey3d().equals(referenceSurvey3d)) {
        Point3d[] controlPoints = selection.getPoints3d();
        // Handle 3D prestack and poststack datasets.
        int numOffsets = 1;
        float offsetStart = 0;
        float offsetDelta = 0;
        Domain domain = Domain.TIME;
        FloatRange zRange = new FloatRange(0, 0, 1);
        if (_model.getReferenceDataset() instanceof PreStack3d) {
          PreStack3d prestack = (PreStack3d) _model.getReferenceDataset();
          numOffsets = prestack.getNumOffsets();
          offsetStart = prestack.getOffsetStart();
          offsetDelta = prestack.getOffsetDelta();
          domain = prestack.getZDomain();
          zRange = prestack.getZRange();
        } else {
          PostStack3d poststack = (PostStack3d) _model.getReferenceDataset();
          numOffsets = 1;
          domain = poststack.getZDomain();
          zRange = poststack.getZRange();
        }
        int numPanels = controlPoints.length - 1;
        int numTraces = 0;
        Point3d[][] panelPoints = new Point3d[numPanels][];
        for (int i = 0; i < numPanels; i++) {
          panelPoints[i] = getPoints(referenceSurvey3d, controlPoints[i], controlPoints[i + 1]);
          numTraces += panelPoints[i].length * numOffsets;
        }
        Point3d[] tracePoints = new Point3d[numTraces];
        int[] panelIndices = new int[numTraces];
        TraceAxisKey[] traceAxisKeys = { TraceAxisKey.INLINE, TraceAxisKey.XLINE, TraceAxisKey.OFFSET };
        float[][] traceAxisKeyValues = new float[numTraces][3];
        int index = 0;
        for (int panelIndex = 0; panelIndex < numPanels; panelIndex++) {
          int numPointsPerPanel = panelPoints[panelIndex].length;
          for (int pointIndex = 0; pointIndex < numPointsPerPanel; pointIndex++) {
            for (int offsetIndex = 0; offsetIndex < numOffsets; offsetIndex++) {
              double x = panelPoints[panelIndex][pointIndex].getX();
              double y = panelPoints[panelIndex][pointIndex].getY();
              tracePoints[index] = new Point3d(x, y, 0);
              panelIndices[index] = panelIndex;
              float[] result = referenceSurvey3d.transformXYToInlineXline(x, y, true);
              traceAxisKeyValues[index][0] = result[0];
              traceAxisKeyValues[index][1] = result[1];
              traceAxisKeyValues[index][2] = offsetStart + offsetIndex * offsetDelta;
              index++;
            }
          }
        }
        TraceSection section = new TraceSection(controlPoints, tracePoints, panelIndices, traceAxisKeys,
            traceAxisKeyValues, domain, zRange.getStart(), zRange.getEnd());
        setTraceSection(section);
      }
    }
  }

  /**
   * Sets the trace section to display in the viewer, using the current horizontal and vertical display scales.
   * <p>
   * This method also sets the default and viewable bounds to those of the section.
   * 
   * @param traceSection the trace section to display.
   */
  public void setTraceSection(final TraceSection traceSection) {
    setTraceSectionWithScales(traceSection, _model.getHorizontalScale(), _model.getVerticalScale(), true);
  }

  /**
   * Sets the trace section to display in the viewer, using the current horizontal and vertical display scales.
   * <p>
   * This method optionally sets the default and viewable bounds to those of the section.
   * 
   * @param traceSection the trace section to display.
   * @param updateBounds <i>true</i> to auto-update the default and viewable bounds; otherwise <i>false</i>
   */
  public void setTraceSection(final TraceSection traceSection, final boolean updateBounds) {
    setTraceSectionWithScales(traceSection, _model.getHorizontalScale(), _model.getVerticalScale(), updateBounds);
  }

  /**
   * Decrements the current inline/xline trace section displayed by a single logical step.
   * <p>
   * If the current trace section is not an inline or xline section, then this method does nothing.
   */
  public void decrementSection() {
    incrementSection(-1);
  }

  /**
   * Increments the current inline/xline trace section displayed by a single logical step.
   * <p>
   * If the current trace section is not an inline or xline section, then this method does nothing.
   */
  public void incrementSection() {
    incrementSection(1);
  }

  /**
   * Increments the current inline/xline trace section displayed by the given logical step.
   * <p>
   * If the current trace section is not an inline or xline section, then this method does nothing.
   * 
   * TODO: implement the other PreStack3d sections.
   * 
   * @param delta the number of inline/xline indices to increment.
   */
  protected void incrementSection(final int delta) {
    // If not trace section is currently defined, then simply return.
    if (_model.getTraceSection() == null) {
      return;
    }

    SectionType sectionType = _model.getTraceSection().getSectionType();
    switch (sectionType) {
      case INLINE_OFFSET_GATHER:
        break;
      case INLINE_SECTION:
        float inline = _model.getTraceSection().getTraceAxisKeyValue(0, TraceAxisKey.INLINE);
        // Create an initial trace section.
        if (_model.getReferenceDataset() instanceof PostStack3d) {
          PostStack3d poststack3d = (PostStack3d) _model.getReferenceDataset();
          SeismicSurvey3d referenceSurvey = poststack3d.getSurvey();
          int numInlines = referenceSurvey.getNumInlines();
          float inlineStart = referenceSurvey.getInlineStart();
          float inlineDelta = referenceSurvey.getInlineDelta();
          int inlineIndex = Math.round((inline - inlineStart) / inlineDelta);
          inlineIndex += delta;
          inlineIndex = Math.max(inlineIndex, 0);
          inlineIndex = Math.min(inlineIndex, numInlines - 1);
          inline = inlineStart + inlineIndex * inlineDelta;
          TraceSection traceSection = PostStack3dSectionFactory.createInlineSection(poststack3d, inline, 1);
          setTraceSection(traceSection, false);
        } else if (_model.getReferenceDataset() instanceof PreStack3d) {
          PreStack3d prestack3d = (PreStack3d) _model.getReferenceDataset();
          SeismicSurvey3d referenceSurvey = prestack3d.getSurvey();
          int numInlines = referenceSurvey.getNumInlines();
          float inlineStart = referenceSurvey.getInlineStart();
          float inlineDelta = referenceSurvey.getInlineDelta();
          int inlineIndex = Math.round((inline - inlineStart) / inlineDelta);
          inlineIndex += delta;
          inlineIndex = Math.max(inlineIndex, 0);
          inlineIndex = Math.min(inlineIndex, numInlines - 1);
          inline = inlineStart + inlineIndex * inlineDelta;
          TraceSection traceSection = PreStack3dSectionFactory.createInlineSection(prestack3d,
              PreStack3d.StorageOrder.INLINE_XLINE_OFFSET_Z, inline, 10, 1);
          setTraceSection(traceSection, false);
        }
        break;
      case INLINE_XLINE_GATHER:
        break;
      case INLINE_XLINE_OFFSET_TRACE:
        break;
      case IRREGULAR:
        break;
      case OFFSET_SECTION:
        break;
      case XLINE_OFFSET_GATHER:
        break;
      case XLINE_SECTION:
        float xline = _model.getTraceSection().getTraceAxisKeyValue(0, TraceAxisKey.XLINE);
        // Create an initial trace section.
        if (_model.getReferenceDataset() instanceof PostStack3d) {
          PostStack3d poststack3d = (PostStack3d) _model.getReferenceDataset();
          SeismicSurvey3d referenceSurvey = poststack3d.getSurvey();
          int numXlines = referenceSurvey.getNumXlines();
          float xlineStart = referenceSurvey.getXlineStart();
          float xlineDelta = referenceSurvey.getXlineDelta();
          int xlineIndex = Math.round((xline - xlineStart) / xlineDelta);
          xlineIndex += delta;
          xlineIndex = Math.max(xlineIndex, 0);
          xlineIndex = Math.min(xlineIndex, numXlines - 1);
          xline = xlineStart + xlineIndex * xlineDelta;
          TraceSection traceSection = PostStack3dSectionFactory.createXlineSection(poststack3d, xline, 1);
          setTraceSection(traceSection, false);
        } else if (_model.getReferenceDataset() instanceof PreStack3d) {
          PreStack3d prestack3d = (PreStack3d) _model.getReferenceDataset();
          SeismicSurvey3d referenceSurvey = prestack3d.getSurvey();
          int numXlines = referenceSurvey.getNumXlines();
          float xlineStart = referenceSurvey.getXlineStart();
          float xlineDelta = referenceSurvey.getXlineDelta();
          int xlineIndex = Math.round((xline - xlineStart) / xlineDelta);
          xlineIndex += delta;
          xlineIndex = Math.max(xlineIndex, 0);
          xlineIndex = Math.min(xlineIndex, numXlines - 1);
          xline = xlineStart + xlineIndex * xlineDelta;
          TraceSection traceSection = PreStack3dSectionFactory.createXlineSection(prestack3d,
              PreStack3d.StorageOrder.XLINE_INLINE_OFFSET_Z, xline, 10, 1);
          setTraceSection(traceSection, false);
        }
        break;

    }

  }

  /**
   * Sets the trace section to display in the viewer, along with the horizontal and vertical display scales to use.
   * 
   * @param traceSection the trace section to display.
   * @param tracesPerInch the horizon display scale (in traces-per-inch).
   * @param inchesPerSecOrKilofoot the vertical display scale (in inches-per-second).
   */
  public void setTraceSectionWithScales(final TraceSection traceSection, final float tracesPerInch,
      final float inchesPerSecOrKilofoot, final boolean updateBounds) {
    TraceSection traceSectionOld = _model.getTraceSection();
    //_traceSection = traceSection;
    _model.setTraceSection(traceSection);
    float zStart = _model.getSelectedZRange().getStart();
    float zEnd = _model.getSelectedZRange().getEnd();

    if (updateBounds) {
      if (!Float.isNaN(tracesPerInch) && !Float.isNaN(inchesPerSecOrKilofoot)) {
        setDisplayScales(tracesPerInch, inchesPerSecOrKilofoot, zStart, zEnd);
      }
      getModelSpace().setMaximumBounds(new ModelSpaceBounds(1, traceSection.getNumTraces(), zStart, zEnd));

    } else {
      ModelSpaceBounds viewableBounds = getModelSpace().getViewableBounds();
      getModelSpace().setViewableBounds(viewableBounds.getStartX(), viewableBounds.getEndX(),
          viewableBounds.getStartY(), viewableBounds.getEndY());
    }

    // Enable/disable the increment/decrement and z range selection buttons.
    boolean enableIncrementDecrement = false;
    if (_model.getTraceSection() != null) {
      SectionType sectionType = _model.getTraceSection().getSectionType();
      if (sectionType.equals(SectionType.INLINE_SECTION) || sectionType.equals(SectionType.XLINE_SECTION)) {
        enableIncrementDecrement = true;
      }
    }
    _decrementSection.setEnabled(enableIncrementDecrement);
    _incrementSection.setEnabled(enableIncrementDecrement);
    _zRangeSelectionButton.setEnabled(true);

    // Publish to the message service that the trace section has changed.
    ServiceProvider.getMessageService().publish(Topic.TRACE_SECTION_DISPLAYED,
        new TraceSectionEvent(traceSection, traceSectionOld));
  }

  private IModelSpace getModelSpace() {
    return _plot.getModelSpaces()[0];
  }

  /**
   * Returns the reference seismic dataset.
   * 
   * @return the reference seismic dataset; or <i>null</i> if none yet set.
   */
  public SeismicDataset getReferenceDataset() {
    return _model.getReferenceDataset();
  }

  /**
   * Returns the seismic survey of the reference 2D dataset.
   * 
   * @return the reference 2D seismic survey; or <i>null</i> if the reference dataset is not 2D.
   */
  public SeismicSurvey2d getSeismicSurvey2d() {
    //return _referenceSurvey2d;
    return _model.getReferenceSurvey2d();
  }

  /**
   * Returns the seismic survey of the reference 3D seismic dataset.
   * 
   * @return the reference 3D seismic survey; or <i>null</i> if the reference dataset is not 3D.
   */
  public SeismicSurvey3d getReferenceSurvey3d() {
    //return _referenceSurvey3d;
    return _model.getReferenceSurvey3d();
  }

  /**
   * Returns the line number of the reference 2D dataset.
   * 
   * @return the line number; or 0 if the reference dataset is not defined or is not 2D.
   */
  public int getSeismicLineNumber2d() {
    if (_model.getReferenceDataset() != null && _model.getReferenceDataset() instanceof PostStack2dLine) {
      return ((PostStack2dLine) _model.getReferenceDataset()).getLineNumber();
    }
    return 0;
  }

  /**
   * Returns the offset range of the reference 3D dataset.
   * 
   * @return the offset range; or [0,0,1] if the reference dataset is not defined or is not 3D.
   */
  public FloatRange getOffsetRange() {
    if (_model.getReferenceDataset() != null && _model.getReferenceDataset() instanceof PreStack3d) {
      return ((PreStack3d) _model.getReferenceDataset()).getOffsetRange();
    }
    return new FloatRange(0, 0, 1);
  }

  /**
   * Updates the selected z range to display.
   * <p>
   * Note: This only sets the range, it does not trigger a redraw.
   * 
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   */
  public void setZStartAndEnd(final float zStart, final float zEnd) {
    FloatRange zrange = new FloatRange(zStart, zEnd, _model.getReferenceDataset().getZRange().getDelta());
    _model.setSelectedZRange(zrange);
    if (_model.getTraceSection() != null) {
      _model.getTraceSection().setZStartAndEnd(zStart, zEnd);
    }
  }

  /**
   * Returns the selected z range to display.
   * 
   * @return the selected z range.
   */
  public FloatRange getZRangeSelected() {
    if (_model.getReferenceDataset() != null) {
      return _model.getSelectedZRange();
    }
    return new FloatRange(0, 1, 1);
  }

  /**
   * Sets the current toggle group ID.
   * 
   * @param toggleGroupId the toggle group ID.
   */
  public void setToggleGroup(final int toggleGroupId) {
    if (_toggleGroupsEnabled) {
      if (toggleGroupId > 0) {
        _toggleGroupCombo.select(toggleGroupId - 1);
      }
      CheckboxTreeViewer treeViewer = _layerViewer;
      for (IViewLayer viewLayer : getLayerModel().getLayers()) {
        viewLayer.setToggleGroup(toggleGroupId);
        treeViewer.setChecked(viewLayer, viewLayer.isVisible());
      }
    }
  }

  /**
   * Enables/disables the toggle group functionality.
   * 
   * @param flag <i>true</i> to enable toggle groups; <i>false</i> to disable.
   */
  public void enableToggleGroups(final boolean flag) {
    _toggleGroupCombo.setVisible(flag);
    if (flag) {
      _toggleGroupsEnabled = flag;
      int id = 1 + _toggleGroupCombo.getSelectionIndex();
      setToggleGroup(id);
    } else {
      setToggleGroup(0);
      _toggleGroupsEnabled = flag;
    }
  }

  /**
   * Returns an array of points that represent the bin location in the a 3D seismic survey between two given points.
   * 
   * @param survey the 3D seismic survey.
   * @param point1 the 1st point.
   * @param point2 the 2nd point.
   * @return the array of points on the survey between the given points.
   */
  private Point3d[] getPoints(final SeismicSurvey3d survey, final Point3d point1, final Point3d point2) {
    List<Point3d> points = new ArrayList<Point3d>();

    // Compute the row,column of the 1st point.
    double x0 = point1.getX();
    double y0 = point1.getY();
    double[] rowcol0 = survey.transformXYToRowCol(x0, y0, true);
    int row0 = Math.round((float) rowcol0[0]);
    int col0 = Math.round((float) rowcol0[1]);
    points.add(point1);

    // Compute the row,column of the 2nd point.
    double x1 = point2.getX();
    double y1 = point2.getY();
    double[] rowcol1 = survey.transformXYToRowCol(x1, y1, true);
    int row1 = Math.round((float) rowcol1[0]);
    int col1 = Math.round((float) rowcol1[1]);

    // Compute the row,column of all the point in between.
    int rowDiff = row1 - row0;
    int colDiff = col1 - col0;
    int rowDiffAbs = Math.abs(rowDiff);
    int colDiffAbs = Math.abs(colDiff);
    float maxDiffAbs = Math.max(rowDiffAbs, colDiffAbs);
    float rowDelta = rowDiff / maxDiffAbs;
    float colDelta = colDiff / maxDiffAbs;
    for (int j = 0; j < maxDiffAbs; j++) {
      int row = Math.round(row0 + (j + 1) * rowDelta);
      int col = Math.round(col0 + (j + 1) * colDelta);
      double[] xy = survey.transformRowColToXY(row, col);
      points.add(new Point3d(xy[0], xy[1], 0));
    }

    // Return the array of points.
    return points.toArray(new Point3d[0]);
  }

  /**
   * Returns the horizontal display scale (in traces-per-inch) of the current section.
   * 
   * @return the horizontal display scale.
   */
  public float getHorizontalDisplayScale() {
    return _model.getHorizontalScale();
  }

  /**
   * Returns the vertical display scale (in inches-per-second) of the current section.
   * 
   * @return the vertical display scale.
   */
  public float getVerticalDisplayScale() {
    return _model.getVerticalScale();
  }

  public void addObjects(final Object[] objects, final boolean block) {
    addObjects(block, objects);
  }

  public Action getNavigationAction() {
    return _navigationAction;
  }

  public TraceSection getTraceSection() {
    return _model.getTraceSection();
  }

  public void redrawAllRenderers() {
    for (IRenderer renderer : getRenderers()) {
      ((SectionViewRenderer) renderer).redraw();
    }
  }

  public IPlot getPlot() {
    return _plot;
  }

  public IModelSpaceCanvas getModelSpaceCanvas() {
    return _plot.getModelSpaceCanvas();
  }

  public void cursorSelectionUpdated(final double x, final double y) {
    List<DataSelection> selectionList = new ArrayList<DataSelection>();
    IPlotLayer[] layers = _plot.getActiveModelSpace().getLayers();
    for (IPlotLayer layer : layers) {
      if (layer instanceof SectionViewRenderer) {
        SectionViewRenderer renderer = (SectionViewRenderer) layer;
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

  /**
   * Returns the trace section currently displayed with the given z domain.
   * 
   * @param zDomain the z domain of the section to get.
   * @return the current trace section; or <i>null</i> if none or z domain does not match.
   */
  public TraceSection getTraceSection(final Domain zDomain) {
    if (_model.getTraceSection() != null && _model.getTraceSection().getDomain().equals(zDomain)) {
      return _model.getTraceSection();
    }
    return null;
  }

  /**
   * Sets the horizontal and vertical display scales of the current section.
   * 
   * @param tracesPerInch the horizontal scale (in traces-per-inch).
   * @param inchesPerSecOrKilofoot the vertical scale (in inches-per-second).
   */
  public void setScales(final float tracesPerInch, final float inchesPerSecOrKilofoot) {
    // If there is no current section, then simply return.
    if (_model.getTraceSection() == null) {
      return;
    }

    AxisRange zRange = getModelSpace().getViewableBounds().getRangeY();
    float zStart = (float) zRange.getStart();
    float zEnd = (float) zRange.getEnd();
    setDisplayScales(tracesPerInch, inchesPerSecOrKilofoot, zStart, zEnd);
  }

  /**
   * Sets the horizontal and vertical display scales of the current section.
   * 
   * @param tracesPerInch the horizontal scale (in traces-per-inch).
   * @param inchesPerSec the vertical scale (in inches-per-second).
   * @param the starting z value.
   * @param the ending z value.
   */
  public void setDisplayScales(final float tracesPerInch, final float inchesPerSecOrKilofoot, final float zStart,
      final float zEnd) {
    //_tracesPerInch = tracesPerInch;
    _model.setHorizontalScale(tracesPerInch);
    //_inchesPerSec = inchesPerSecOrKilofoot;
    _model.setVerticalScale(inchesPerSecOrKilofoot);

    // If there is no current section, then simply return.
    if (_model.getTraceSection() == null) {
      return;
    }

    Display.getDefault().syncExec(new Runnable() {

      public void run() {

        // Default the maximum size of the drawing area to 400 sq. inches.
        float areaInSqInchesMax = IPlot.MAX_IMAGE_SIZE;
        float heightInInches = 1;
        float widthInInches = 1;
        int numTraces = _model.getTraceSection().getNumTraces();

        // Compute the required width of the drawing area (in inches).
        widthInInches = numTraces / tracesPerInch;

        // Compute the maximum height of the drawing area (in inches).
        float heightInInchesMax = areaInSqInchesMax / widthInInches;

        // For the given z domain, compute the required height of the drawing area (in inches).
        float zDelta = zEnd - zStart;
        final Domain zDomain = _model.getTraceSection().getDomain();
        if (zDomain.equals(Domain.TIME)) {
          Unit timeUnit = UnitPreferences.getInstance().getTimeUnit();
          float seconds = Unit.convert(zDelta, timeUnit, Unit.SECOND);
          heightInInches = seconds * inchesPerSecOrKilofoot;
        } else if (zDomain.equals(Domain.DISTANCE)) {
          Unit depthUnit = UnitPreferences.getInstance().getVerticalDistanceUnit();
          if (depthUnit.equals(Unit.FOOT)) {
            zDelta = Unit.convert(zDelta, depthUnit, Unit.KILOFEET);
          } else {
            zDelta = Unit.convert(zDelta, depthUnit, Unit.KILOMETER);
          }
          heightInInches = zDelta * inchesPerSecOrKilofoot;
        }

        // If necessary, restrict the height of the drawing area to its maximum.
        float zPercent = 1;
        if (heightInInches > heightInInchesMax) {
          zPercent = heightInInchesMax / heightInInches;
          heightInInches = heightInInchesMax;
        }
        float zDeltaAdj = (zEnd - zStart) * zPercent;

        // Compute the width and height of the drawing area (in pixels).
        int widthInPixels = Math.round(widthInInches * _model.getScreenResolution());
        int heightInPixels = Math.round(heightInInches * _model.getScreenResolution());

        // Set the size of the model space canvas to the computed width and height.
        getModelSpaceCanvas().setSize(widthInPixels, heightInPixels);

        // Update the default and viewable bounds of the viewer.
        Unit yUnit = Unit.UNDEFINED;
        String yLabel = "";
        if (zDomain.equals(Domain.TIME)) {
          yUnit = UnitPreferences.getInstance().getTimeUnit();
          yLabel = "Time";
        } else if (zDomain.equals(Domain.DISTANCE)) {
          yUnit = UnitPreferences.getInstance().getVerticalDistanceUnit();
          yLabel = "Depth";
        } else {
          throw new IllegalArgumentException("Invalid domain: " + zDomain);
        }
        AxisRange xRange = getModelSpace().getViewableBounds().getRangeX();
        double traceStart = xRange.getStart();
        double traceEnd = traceStart + numTraces - 1;
        getModelSpace().setDefaultAndViewableBounds(traceStart, traceEnd, zStart, zStart + zDeltaAdj, yUnit, yLabel);

        // Update the title of the viewer.
        _plot.setTitle(_model.getTraceSection().getLabelText());
      }
    });
  }

  /**
   * Adds objects for rendering in the section viewer.
   * The viewer will find and create a renderer capable of displaying each object.
   * If no renderer can be found, then the object will simply be skipped.
   * 
   * @param objects the array of objects to add/render.
   * @param block <i>true</i> to block the UI while the object is being add; otherwise <i>false</i>.
   */
  @Override
  public void addObjects(final boolean block, final Object... objects) {
    loadEntities(objects);
    final Shell shell = getShell();
    final BackgroundTask task = new BackgroundTask() {

      @Override
      public Void compute(final ILogger logger, final IProgressMonitor monitor) {

        Display.getDefault().asyncExec(new Runnable() {

          public void run() {
            boolean setInitialTraceSection = false;

            monitor.beginTask("Add entities to the section viewer", objects.length);
            try {
              for (Object object : objects) {
                boolean reject = false;
                if (object instanceof SeismicDataset) {
                  SeismicDataset dataset = (SeismicDataset) object;
                  Domain zDomain = dataset.getZDomain();
                  if (_model.getReferenceDataset() == null) {
                    //_referenceDataset = dataset;
                    _model.setReferenceDataset(dataset);
                    //_referenceZDomain = zDomain;
                    //Note: get reference dataset's Z-Domain from viewer model
                    setInitialTraceSection = true;
                    if (dataset instanceof PostStack3d) {
                      PostStack3d poststack = (PostStack3d) dataset;
                      //_referenceSurvey3d = poststack.getSurvey();
                      setNavigationAction(new PostStack3dNavigationAction(poststack, SectionViewer.this));
                    } else if (dataset instanceof PreStack3d) {
                      final PreStack3d prestack = (PreStack3d) dataset;
                      //_referenceSurvey3d = prestack.getSurvey();
                      setNavigationAction(new PreStack3dNavigationAction(prestack, SectionViewer.this));
                    } else if (dataset instanceof PostStack2dLine) {
                      PostStack2dLine poststack = (PostStack2dLine) dataset;
                      //_referenceSurvey2d = poststack.getSurvey();
                      setNavigationAction(new PostStack2dNavigationAction(poststack.getPostStack(), SectionViewer.this));
                    } else {
                      reject = true;
                    }

                    // Default the selected z range to that of the reference dataset.
                    //_zRangeSelected = dataset.getZRange();
                    _model.setSelectedZRange(dataset.getZRange());
                  } else {
                    SeismicSurvey2d survey2d = null;
                    SeismicSurvey3d survey3d = null;
                    if (dataset instanceof PostStack3d) {
                      survey3d = ((PostStack3d) dataset).getSurvey();
                    } else if (dataset instanceof PreStack3d) {
                      survey3d = ((PreStack3d) dataset).getSurvey();
                    } else if (dataset instanceof PostStack2dLine) {
                      survey2d = ((PostStack2dLine) dataset).getSurvey();
                    }
                    if (!zDomain.equals(_model.getReferenceZDomain())) {
                      String message = "Could not add dataset " + dataset.getDisplayName()
                          + " to section viewer.\nDomain mismatch.";
                      MessageDialog.openError(getShell(), "Section View Error", message);
                      reject = true;
                    } else if (survey2d == null && survey3d == null) {
                      String message = "Could not add dataset " + dataset.getDisplayName()
                          + " to section viewer.\nUnrecognized volume type.";
                      MessageDialog.openError(getShell(), "Section View Error", message);
                      reject = true;
                      //} else if (_referenceSurvey3d != null && survey3d != null
                      //  && !survey3d.matchesGeometry(_referenceSurvey3d)) {
                      //String message = "Could not add dataset " + dataset.getDisplayName()
                      //    + " to section viewer.\nGeometry mismatch: " + survey3d.toString() + " "
                      //    + _referenceSurvey3d.toString();
                      //MessageDialog.openError(getShell(), "Section View Error", message);
                      //reject = true;
                    } else {
                      SeismicSurvey2d referenceSurvey2d = getSeismicSurvey2d();
                      if (referenceSurvey2d != null && survey2d != null && !survey2d.matchesGeometry(referenceSurvey2d)) {
                        String message = "Could not add dataset " + dataset.getDisplayName()
                            + " to section viewer.\nGeometry mismatch: " + survey2d.toString() + " "
                            + referenceSurvey2d.toString();
                        MessageDialog.openError(getShell(), "Section View Error", message);
                        reject = true;
                      }
                    }
                  }
                }
                if (!isObjectRendered(object) && !reject) {
                  final List<IConfigurationElement> configsSingle = RendererRegistry.findRenderer(shell, object);
                  for (IConfigurationElement config : configsSingle) {
                    //AbstractRenderer rendererSingle = RendererRegistry.selectRenderer("Renderer: " + object.toString(),
                    //    configsSingle, !multiRendererSelected)
                    try {
                      boolean rendererExists = false;
                      SectionViewRenderer newRenderer = RendererRegistry.createRenderer(config);
                      for (SectionViewRenderer renderer : getRenderers()) {
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
                        newRenderer.setData(shell, SectionViewer.this, new Object[] { object });
                      }
                    } catch (Exception ex) {
                      ServiceProvider.getLoggingService().getLogger(getClass())
                          .error("Error creating renderer for " + object.toString(), ex);
                    }
                  }
                }
                if (setInitialTraceSection) {
                  //NOTE: Only set true when setting the reference dataset which also sets the navigation action.
                  //      So doing likewise here is redundant.

                  // Enable the navigation button.
                  //_navigationButton.setEnabled(true);

                  // Create an initial trace section.
                  if (_model.getReferenceDataset() instanceof PostStack3d) {
                    PostStack3d poststack3d = (PostStack3d) _model.getReferenceDataset();

                    // Create the navigation action for stacked 3D volumes.
                    //_navigationAction = new PostStack3dNavigationAction(poststack3d, SectionViewer.this);

                    float inline = poststack3d.getInlineStart();
                    TraceSection traceSection = PostStack3dSectionFactory.createInlineSection(poststack3d, inline, 1);
                    SectionViewer.this.setTraceSection(traceSection);

                  } else if (_model.getReferenceDataset() instanceof PreStack3d) {
                    PreStack3d prestack3d = (PreStack3d) _model.getReferenceDataset();

                    //_navigationAction = new PreStack3dNavigationAction(prestack3d, SectionViewer.this);

                    float inline = prestack3d.getInlineStart();
                    TraceSection traceSection = PreStack3dSectionFactory.createInlineSection(prestack3d,
                        PreStack3d.StorageOrder.INLINE_XLINE_OFFSET_Z, inline, 10, 1);
                    setTraceSection(traceSection);
                  }
                }
                monitor.worked(1);
                if (monitor.isCanceled()) {
                  break;
                }
              }
            } finally {
              monitor.done();
            }
            // Lastly, trigger a redraw of all the renderers.
            Display.getDefault().syncExec(new Runnable() {

              public void run() {
                for (SectionViewRenderer renderer : getRenderers()) {
                  renderer.redraw();
                }
              }
            });
            monitor.worked(1);
          }

        });
        return null;
      }
    };

    // If blocking run the task in the current thread.
    if (block) {
      TaskRunner.runTask(task, "Add entities to the section viewer");
    } else {
      // Otherwise, run it in a new thread.
      new Thread(new Runnable() {

        public void run() {
          TaskRunner.runTask(task, "Add entities to the section viewer");
        }
      }).start();
    }
  }

  /**
   * Sets the action to associated with the toolbar navigator button.
   * 
   * @param action the navigation action to set.
   */
  protected void setNavigationAction(final Action action) {
    Display.getDefault().asyncExec(new Runnable() {

      public void run() {
        _navigationAction = action;
        _navigationButton.setEnabled(_navigationAction != null);
      }
    });
  }

  @Override
  public void cursorUpdated(final double x, final double y, final boolean broadcast) {

    // The x value is the trace number and the y value is the z value.
    int traceNum = Math.round((float) x);
    float z = (float) y;

    int traceIndex = traceNum - 1;

    super.cursorUpdated(traceNum, z, false);

    if (_plot.getCursorBroadcast() && broadcast && _model.getTraceSection() != null) {
      Domain zDomain = _model.getTraceSection().getDomain();
      TraceSection traceSection = getTraceSection(zDomain);
      if (traceSection != null && traceIndex >= 0 && traceIndex < traceSection.getNumTraces()) {
        Point3d point = traceSection.getPointsXY()[traceIndex];
        float offset = 0;
        if (traceSection.containsTraceAxisKey(TraceAxisKey.OFFSET)) {
          offset = traceSection.getTraceAxisKeyValue(traceIndex, TraceAxisKey.OFFSET);
        }
        TimeOrDepth timeOrDepth = TimeOrDepth.TIME;
        CoordinateSystem coordinateSystem = ApplicationPreferences.getInstance().getTimeCoordinateSystem();
        if (zDomain.equals(Domain.DISTANCE)) {
          coordinateSystem = ApplicationPreferences.getInstance().getDepthCoordinateSystem();
          timeOrDepth = TimeOrDepth.DEPTH;
        }
        Coordinate coordinate = new Coordinate(new Point3d(point.getX(), point.getY(), z), coordinateSystem);
        // System.out.println("  publish: " + point.getX() + " " + point.getY());
        ServiceProvider.getMessageService().publish(Topic.CURSOR_LOCATION,
            new CursorLocation(coordinate, timeOrDepth, offset, getViewerID()));
      }
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
  public void dispose() {
    // Broadcast final message to undisplay the trace section.
    ServiceProvider.getMessageService().publish(Topic.TRACE_SECTION_DISPLAYED,
        new TraceSectionEvent(null, _model.getTraceSection()));

    // Remove the listeners.
    getModelSpaceCanvas().removeCursorListener(this);

    super.dispose();
  }
}
