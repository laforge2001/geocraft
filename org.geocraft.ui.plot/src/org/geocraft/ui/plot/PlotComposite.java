package org.geocraft.ui.plot;


import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.geocraft.ui.plot.action.EditCanvasLayout;
import org.geocraft.ui.plot.axis.AxisComposite;
import org.geocraft.ui.plot.axis.IAxis;
import org.geocraft.ui.plot.defs.Alignment;
import org.geocraft.ui.plot.defs.AxisPlacement;
import org.geocraft.ui.plot.defs.CanvasType;
import org.geocraft.ui.plot.defs.CornerPlacement;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.Orientation;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.defs.RenderLevel;
import org.geocraft.ui.plot.defs.UpdateLevel;
import org.geocraft.ui.plot.internal.AxisLabelCanvas;
import org.geocraft.ui.plot.internal.AxisRangeCanvas;
import org.geocraft.ui.plot.internal.CanvasLayoutManager;
import org.geocraft.ui.plot.internal.CornerCanvas;
import org.geocraft.ui.plot.internal.ModelSpaceCanvas;
import org.geocraft.ui.plot.internal.PlotCanvas;
import org.geocraft.ui.plot.internal.TitleCanvas;
import org.geocraft.ui.plot.label.Label;
import org.geocraft.ui.plot.layer.PlotLayer;
import org.geocraft.ui.plot.layout.CanvasLayoutModel;
import org.geocraft.ui.plot.listener.ICursorListener;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.object.PlotLine;
import org.geocraft.ui.plot.util.PlotUtil;


/**
 * Defines the composite plot panel. The plot panel is a container for the 10
 * different canvas components (and 2 scroll bars) that are used in the plot
 * display. The layout of these components is shown below:
 * 
 * /----------------------------------------------------------------------------------------\
 * |                                    TitleCanvas                                     |   |
 * |------------------------------------------------------------------------------------| V |
 * |                      |                                      |                      | S |
 * |    TopLeftCanvas     |            TopAxisCanvas             |    TopRightCanvas    | c |
 * |                      |                                      |                      | r |
 * |------------------------------------------------------------------------------------| r |
 * |                      |                                      |                      | o |
 * |    LeftAxisCanvas    |             ModelCanvas              |    RightAxisCanvas   | l |
 * |                      |                                      |                      | l |
 * |------------------------------------------------------------------------------------| B |
 * |                      |                                      |                      | a |
 * |   BottomLeftCanvas   |           BottomAxisCanvas           |   BottomRightCanvas  | r |
 * |                      |                                      |                      |   |
 * |----------------------------------------------------------------------------------------|
 * |                                HorizontalScrollBar                                 |   |
 * \----------------------------------------------------------------------------------------/
 * 
 * The plot composite is not meant to be accessed directly by developers. However,
 * it does contain lots of useful and necessary information, which can be
 * obtained from the plot composite thru methods in the plot interface.
 */
public class PlotComposite extends Composite implements ICursorListener {

  /** The associated plot. */
  private final IPlot _plot;

  /** The layout out the plot canvases. */
  private final CanvasLayoutModel _canvasLayout;

  /** The tool bar container. */
  private final Composite _toolBarComposite;

  /** The model canvas. */
  private IModelSpaceCanvas _modelSpaceCanvas;

  /** The model scrolled composite. */
  private ScrolledComposite _modelScroll;

  /** The composite containing the label and range canvases for the top axis. */
  private final AxisComposite _topAxisComposite;

  /** The composite containing the label and range canvases for the left axis. */
  private final AxisComposite _leftAxisComposite;

  /** The composite containing the label and range canvases for the right axis. */
  private final AxisComposite _rightAxisComposite;

  /** The composite containing the label and range canvases for the bottom axis. */
  private final AxisComposite _bottomAxisComposite;

  /** The collection of canvases, mapped by canvas type. */
  private final Map<CanvasType, ICanvas> _canvasMap;

  /** The action for editing the canvas layout. */
  private final EditCanvasLayout _editCanvasLayout;

  /** The formatter for displaying x-axis coordinated in the cursor text field. */
  private NumberFormat _cursorFormatterX;

  /** The formatter for displaying y-axis coordinates in the cursor text field. */
  private NumberFormat _cursorFormatterY;

  private Listener _mouseWheelFilter;

  private boolean _showCursorCross;

  private PlotLine _cursorCrossH;

  private PlotLine _cursorCrossV;

  /**
   * Constructs a composite that contains a plot.
   * @param parent the parent composite.
   * @param plot the plot.
   */
  public PlotComposite(final Composite parent, final IPlot plot) {
    super(parent, SWT.NONE);
    parent.setLayout(new FillLayout());
    _plot = plot;
    _canvasLayout = new CanvasLayoutModel();
    _editCanvasLayout = new EditCanvasLayout(_plot);
    GridLayout layout = new GridLayout();
    layout.makeColumnsEqualWidth = false;
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    layout.numColumns = 5;
    layout.horizontalSpacing = 0;
    layout.verticalSpacing = 0;
    setLayout(layout);

    // Add a tool bar container.
    _toolBarComposite = new Composite(this, SWT.NONE);
    GridData gridData = new GridData();
    gridData.horizontalAlignment = SWT.FILL;
    gridData.horizontalSpan = 5;
    gridData.grabExcessVerticalSpace = false;
    gridData.verticalAlignment = SWT.FILL;
    gridData.verticalSpan = 1;
    gridData.minimumHeight = 1;
    _toolBarComposite.setLayoutData(gridData);
    GridLayout gridLayout = new GridLayout();
    gridLayout.makeColumnsEqualWidth = true;
    gridLayout.numColumns = 1;
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    gridLayout.horizontalSpacing = 0;
    gridLayout.verticalSpacing = 0;
    _toolBarComposite.setLayout(gridLayout);

    // Add a separator.
    org.eclipse.swt.widgets.Label sep = new org.eclipse.swt.widgets.Label(_toolBarComposite, SWT.HORIZONTAL
        | SWT.SEPARATOR);
    gridData = new GridData();
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalAlignment = SWT.FILL;
    gridData.horizontalSpan = 1;
    gridData.grabExcessVerticalSpace = false;
    gridData.verticalAlignment = SWT.FILL;
    gridData.verticalSpan = 1;
    gridData.minimumHeight = 1;
    sep.setLayoutData(gridData);

    // Add the plot canvases.
    _canvasMap = Collections.synchronizedMap(new HashMap<CanvasType, ICanvas>());

    IAxis xAxis = _plot.getModelSpaces()[0].getAxisX();
    IAxis yAxis = _plot.getModelSpaces()[0].getAxisY();

    TitleCanvas titleCanvas = new TitleCanvas(this, _plot, new Label("Plot", Orientation.HORIZONTAL, Alignment.CENTER,
        true), _canvasLayout);

    CornerCanvas topLeftCanvas = new CornerCanvas(this, _plot, CornerPlacement.TOP_LEFT, _canvasLayout);
    AxisLabelCanvas topLabelCanvas = new AxisLabelCanvas(this, _plot, xAxis, AxisPlacement.TOP, _canvasLayout);
    CornerCanvas topRightCanvas = new CornerCanvas(this, _plot, CornerPlacement.TOP_RIGHT, _canvasLayout);
    AxisRangeCanvas topRangeCanvas = new AxisRangeCanvas(this, _plot, xAxis, AxisPlacement.TOP, _canvasLayout);

    AxisLabelCanvas leftLabelCanvas = new AxisLabelCanvas(this, _plot, yAxis, AxisPlacement.LEFT, _canvasLayout);
    AxisRangeCanvas leftRangeCanvas = new AxisRangeCanvas(this, _plot, yAxis, AxisPlacement.LEFT, _canvasLayout);
    addModelSpaceCanvas();
    AxisRangeCanvas rightRangeCanvas = new AxisRangeCanvas(this, _plot, yAxis, AxisPlacement.RIGHT, _canvasLayout);
    AxisLabelCanvas rightLabelCanvas = new AxisLabelCanvas(this, _plot, yAxis, AxisPlacement.RIGHT, _canvasLayout);

    CornerCanvas bottomLeftCanvas = new CornerCanvas(this, _plot, CornerPlacement.BOTTOM_LEFT, _canvasLayout);
    AxisRangeCanvas bottomRangeCanvas = new AxisRangeCanvas(this, _plot, xAxis, AxisPlacement.BOTTOM, _canvasLayout);
    CornerCanvas bottomRightCanvas = new CornerCanvas(this, _plot, CornerPlacement.BOTTOM_RIGHT, _canvasLayout);
    AxisLabelCanvas bottomLabelCanvas = new AxisLabelCanvas(this, _plot, xAxis, AxisPlacement.BOTTOM, _canvasLayout);

    // Add the title canvas to the canvas map.
    _canvasMap.put(CanvasType.TITLE, titleCanvas);

    // Add the corner canvases to the canvas map.
    _canvasMap.put(CanvasType.TOP_LEFT_CORNER, topLeftCanvas);
    _canvasMap.put(CanvasType.TOP_RIGHT_CORNER, topRightCanvas);
    _canvasMap.put(CanvasType.BOTTOM_LEFT_CORNER, bottomLeftCanvas);
    _canvasMap.put(CanvasType.BOTTOM_RIGHT_CORNER, bottomRightCanvas);

    // Add the axis label canvases to the canvas map.
    _canvasMap.put(CanvasType.TOP_AXIS_LABEL, topLabelCanvas);
    _canvasMap.put(CanvasType.LEFT_AXIS_LABEL, leftLabelCanvas);
    _canvasMap.put(CanvasType.RIGHT_AXIS_LABEL, rightLabelCanvas);
    _canvasMap.put(CanvasType.BOTTOM_AXIS_LABEL, bottomLabelCanvas);

    // Add the range canvases to the canvas map.
    _canvasMap.put(CanvasType.TOP_AXIS_RANGE, topRangeCanvas);
    _canvasMap.put(CanvasType.LEFT_AXIS_RANGE, leftRangeCanvas);
    _canvasMap.put(CanvasType.RIGHT_AXIS_RANGE, rightRangeCanvas);
    _canvasMap.put(CanvasType.BOTTOM_AXIS_RANGE, bottomRangeCanvas);

    // Create the axis composites.
    _topAxisComposite = new AxisComposite(_plot, topLabelCanvas, topRangeCanvas);
    _leftAxisComposite = new AxisComposite(_plot, leftLabelCanvas, leftRangeCanvas);
    _rightAxisComposite = new AxisComposite(_plot, rightLabelCanvas, rightRangeCanvas);
    _bottomAxisComposite = new AxisComposite(_plot, bottomLabelCanvas, bottomRangeCanvas);

    _modelSpaceCanvas.setAxisCanvases(topLabelCanvas, leftLabelCanvas, rightLabelCanvas, bottomLabelCanvas);
    _modelSpaceCanvas.addCursorListener(plot);

    // Create a formatter for the cursor x-coordinates.
    _cursorFormatterX = NumberFormat.getNumberInstance();
    _cursorFormatterX.setGroupingUsed(false);
    _cursorFormatterX.setMaximumIntegerDigits(10);
    _cursorFormatterX.setMinimumFractionDigits(1);
    _cursorFormatterX.setMaximumFractionDigits(1);

    // Create a formatter for the cursor y-coordinates.
    _cursorFormatterY = NumberFormat.getNumberInstance();
    _cursorFormatterY.setGroupingUsed(false);
    _cursorFormatterY.setMaximumIntegerDigits(10);
    _cursorFormatterY.setMinimumFractionDigits(1);
    _cursorFormatterY.setMaximumFractionDigits(1);

    // Create the canvas layout manager.
    IAxisLabelCanvas[] axisLabelCanvases = { topLabelCanvas, leftLabelCanvas, rightLabelCanvas, bottomLabelCanvas };
    IAxisRangeCanvas[] axisRangeCanvases = { topRangeCanvas, leftRangeCanvas, rightRangeCanvas, bottomRangeCanvas };
    ICornerCanvas[] cornerCanvases = { topLeftCanvas, topRightCanvas, bottomLeftCanvas, bottomRightCanvas };
    CanvasLayoutManager layoutManager = new CanvasLayoutManager(_canvasLayout, this, titleCanvas, axisLabelCanvases,
        axisRangeCanvases, cornerCanvases);
    _canvasLayout.addListener(layoutManager);

    _showCursorCross = true;
    _cursorCrossH = createCursorCrossPlotLine();
    _cursorCrossV = createCursorCrossPlotLine();
    PlotLayer cursorCrossLayer = new PlotLayer("cursor");
    cursorCrossLayer.addShape(_cursorCrossH);
    cursorCrossLayer.addShape(_cursorCrossV);
    _plot.getModelSpaces()[0].addLayer(cursorCrossLayer, false);
    _cursorCrossH.select();
    _cursorCrossV.select();
  }

  /**
   * Creates a plot line to be used in displaying the cursor crosshair location.
   */
  private PlotLine createCursorCrossPlotLine() {
    PlotLine cursorCross = new PlotLine();
    cursorCross.setPointStyle(PointStyle.NONE);
    cursorCross.setPointSize(0);
    cursorCross.setLineStyle(LineStyle.SOLID);
    cursorCross.setLineWidth(1);
    cursorCross.setLineColor(new RGB(255, 0, 0));
    cursorCross.setRenderLevel(RenderLevel.SELECTED);
    cursorCross.getPoint(0).setPropertyInheritance(true);
    cursorCross.getPoint(1).setPropertyInheritance(true);
    return cursorCross;
  }

  /**
   * Returns the model scroll window width.
   */
  public int getModelScrollWidth() {
    Point d = getModelScrollSize();
    PlotScrolling scrolling = _plot.getScrolling();
    boolean isScrolled = scrolling != PlotScrolling.NONE;
    if (isScrolled && d.x <= 0) {
      d.x = 1;
    }
    return d.x;
  }

  /**
   * Returns the model scroll window height.
   */
  public int getModelScrollHeight() {
    Point d = getModelScrollSize();
    PlotScrolling scrolling = _plot.getScrolling();
    boolean isScrolled = scrolling != PlotScrolling.NONE;
    if (isScrolled && d.y <= 0) {
      d.y = 1;
    }
    return d.y;
  }

  /**
   * Returns the model scroll window size.
   */
  public Point getModelScrollSize() {
    Point d = new Point(0, 0);
    if (!_plot.getScrolling().equals(PlotScrolling.NONE)) {
      d = _modelScroll.getSize();
    }
    return d;
  }

  /**
   * Returns the composite that contains the shared and any custom toolbars.
   */
  public Composite getToolBarContainer() {
    return _toolBarComposite;
  }

  /**
   * Adds the model space canvas to the plot composite.
   */
  private void addModelSpaceCanvas() {
    GridData constraints = new GridData();
    constraints.grabExcessHorizontalSpace = true;
    constraints.grabExcessVerticalSpace = true;
    constraints.horizontalSpan = 1;
    constraints.verticalSpan = 1;
    constraints.horizontalAlignment = SWT.FILL;
    constraints.verticalAlignment = SWT.FILL;

    PlotScrolling scrolling = _plot.getScrolling();
    final boolean horizontalScrolling = scrolling == PlotScrolling.HORIZONTAL_ONLY || scrolling == PlotScrolling.BOTH;
    final boolean verticalScrolling = scrolling == PlotScrolling.VERTICAL_ONLY || scrolling == PlotScrolling.BOTH;

    SelectionListener horizontalScrollListener = null;
    SelectionListener verticalScrollListener = null;

    // Add scrolling listeners, if necessary.
    if (horizontalScrolling) {
      horizontalScrollListener = new SelectionListener() {

        public void widgetDefaultSelected(final SelectionEvent e) {
          ScrollBar scrollBar = (ScrollBar) e.widget;
          int verticalScrollBarWidth = 0;
          if (verticalScrolling) {
            verticalScrollBarWidth = _modelScroll.getVerticalBar().getSize().x;
          }
          AxisRangeCanvas topRangeCanvas = (AxisRangeCanvas) getCanvasMap().get(CanvasType.TOP_AXIS_RANGE);
          topRangeCanvas.scrolled(scrollBar, verticalScrollBarWidth);
          AxisRangeCanvas bottomRangeCanvas = (AxisRangeCanvas) getCanvasMap().get(CanvasType.BOTTOM_AXIS_RANGE);
          bottomRangeCanvas.scrolled(scrollBar, verticalScrollBarWidth);
        }

        public void widgetSelected(final SelectionEvent event) {
          widgetDefaultSelected(event);
        }

      };
    }
    if (verticalScrolling) {
      verticalScrollListener = new SelectionListener() {

        public void widgetDefaultSelected(final SelectionEvent e) {
          ScrollBar scrollBar = (ScrollBar) e.widget;
          int horizontalScrollBarHeight = 0;
          if (horizontalScrolling) {
            horizontalScrollBarHeight = _modelScroll.getHorizontalBar().getSize().y;
          }
          AxisRangeCanvas leftRangeCanvas = (AxisRangeCanvas) getCanvasMap().get(CanvasType.LEFT_AXIS_RANGE);
          leftRangeCanvas.scrolled(scrollBar, horizontalScrollBarHeight);
          AxisRangeCanvas rightRangeCanvas = (AxisRangeCanvas) getCanvasMap().get(CanvasType.RIGHT_AXIS_RANGE);
          rightRangeCanvas.scrolled(scrollBar, horizontalScrollBarHeight);
        }

        public void widgetSelected(final SelectionEvent event) {
          widgetDefaultSelected(event);
        }

      };
    }
    if (horizontalScrolling || verticalScrolling) {
      ControlListener scrollListener = new ControlAdapter() {

        @Override
        public void controlResized(final ControlEvent event) {
          resized();
        }

      };
      int style = SWT.BORDER;
      if (horizontalScrolling) {
        style = style | SWT.H_SCROLL;
      }
      if (verticalScrolling) {
        style = style | SWT.V_SCROLL;
      }

      _modelScroll = new ScrolledComposite(this, style);

      _mouseWheelFilter = new Listener() {

        public void handleEvent(Event event) {
          if (event.widget != null && event.widget.equals(_modelScroll)) {
            event.doit = false;
          }
        }
      };
      Display.getDefault().addFilter(SWT.MouseWheel, _mouseWheelFilter);

      _modelScroll.setAlwaysShowScrollBars(true);
      this.addControlListener(scrollListener);
      if (horizontalScrolling) {
        _modelScroll.getHorizontalBar().addSelectionListener(horizontalScrollListener);
      }
      if (verticalScrolling) {
        _modelScroll.getVerticalBar().addSelectionListener(verticalScrollListener);
      }
      _modelScroll.setLayoutData(constraints);
      _modelSpaceCanvas = new ModelSpaceCanvas(_modelScroll, _plot);
      _modelScroll.setContent(_modelSpaceCanvas.getComposite());
      Color bkgColor = new Color(getDisplay(), PlotUtil.RGB_LIGHT_GRAY);
      _modelScroll.setBackground(bkgColor);
      bkgColor.dispose();
    } else {
      _modelSpaceCanvas = new ModelSpaceCanvas(this, _plot);
      _modelSpaceCanvas.getComposite().setLayoutData(constraints);
    }
  }

  /**
   * @param horizontalScrolling
   * @param verticalScrolling
   */
  private void resized() {
    PlotScrolling scrolling = _plot.getScrolling();
    final boolean horizontalScrolling = scrolling == PlotScrolling.HORIZONTAL_ONLY || scrolling == PlotScrolling.BOTH;
    final boolean verticalScrolling = scrolling == PlotScrolling.VERTICAL_ONLY || scrolling == PlotScrolling.BOTH;

    if (!horizontalScrolling && !verticalScrolling) {
      return;
    }
    Point canvasSize = _modelSpaceCanvas.getSize();
    Point scrollSize = _modelScroll.getSize();

    int horizontalScrollBarHeight = 0;
    if (horizontalScrolling) {
      horizontalScrollBarHeight = _modelScroll.getHorizontalBar().getSize().y;
    }

    int verticalScrollBarWidth = 0;
    if (verticalScrolling) {
      verticalScrollBarWidth = _modelScroll.getVerticalBar().getSize().x;
    }

    if (horizontalScrolling && !verticalScrolling) {
      _modelSpaceCanvas.setSize(canvasSize.x, scrollSize.y - horizontalScrollBarHeight);
    } else if (verticalScrolling && !horizontalScrolling) {
      _modelSpaceCanvas.setSize(scrollSize.x - verticalScrollBarWidth, canvasSize.y);
    }

    if (horizontalScrolling) {
      ScrollBar scrollBar = _modelScroll.getHorizontalBar();
      AxisRangeCanvas topRangeCanvas = (AxisRangeCanvas) getCanvasMap().get(CanvasType.TOP_AXIS_RANGE);
      topRangeCanvas.scrolled(scrollBar, verticalScrollBarWidth);
      AxisRangeCanvas bottomRangeCanvas = (AxisRangeCanvas) getCanvasMap().get(CanvasType.BOTTOM_AXIS_RANGE);
      bottomRangeCanvas.scrolled(scrollBar, verticalScrollBarWidth);
    } else {
      AxisRangeCanvas topRangeCanvas = (AxisRangeCanvas) getCanvasMap().get(CanvasType.TOP_AXIS_RANGE);
      topRangeCanvas.setSize(scrollSize.x - verticalScrollBarWidth, topRangeCanvas.getSize().y);
      AxisRangeCanvas bottomRangeCanvas = (AxisRangeCanvas) getCanvasMap().get(CanvasType.BOTTOM_AXIS_RANGE);
      bottomRangeCanvas.setSize(scrollSize.x - verticalScrollBarWidth, bottomRangeCanvas.getSize().y);
    }

    if (verticalScrolling) {
      ScrollBar scrollBar = _modelScroll.getVerticalBar();
      AxisRangeCanvas leftRangeCanvas = (AxisRangeCanvas) getCanvasMap().get(CanvasType.LEFT_AXIS_RANGE);
      leftRangeCanvas.scrolled(scrollBar, horizontalScrollBarHeight);
      AxisRangeCanvas rightRangeCanvas = (AxisRangeCanvas) getCanvasMap().get(CanvasType.RIGHT_AXIS_RANGE);
      rightRangeCanvas.scrolled(scrollBar, horizontalScrollBarHeight);
    } else {
      AxisRangeCanvas leftRangeCanvas = (AxisRangeCanvas) getCanvasMap().get(CanvasType.TOP_AXIS_RANGE);
      leftRangeCanvas.setSize(leftRangeCanvas.getSize().x, scrollSize.y - horizontalScrollBarHeight);
      AxisRangeCanvas rightRangeCanvas = (AxisRangeCanvas) getCanvasMap().get(CanvasType.BOTTOM_AXIS_RANGE);
      rightRangeCanvas.setSize(rightRangeCanvas.getSize().x, scrollSize.y - horizontalScrollBarHeight);
    }
  }

  /**
   * Returns the size of the plot.
   */
  @Override
  public Point getSize() {
    Point d = super.getSize();
    Point dview = _modelSpaceCanvas.getComposite().getSize();
    // plotUpdated();
    return d;
  }

  @Override
  public void dispose() {
    for (PlotCanvas canvas : getCanvases()) {
      canvas.dispose();
    }
    if (_mouseWheelFilter != null) {
      Display.getDefault().removeFilter(SWT.MouseWheel, _mouseWheelFilter);
    }
    _modelSpaceCanvas.dispose();
    _canvasMap.clear();
    super.dispose();
  }

  /**
   * Returns the collection of plot canvases.
   */
  public Map<CanvasType, ICanvas> getCanvasMap() {
    return _canvasMap;
  }

  /**
   * Returns the title canvas of the plot composite.
   */
  public ITitleCanvas getTitleCanvas() {
    return (ITitleCanvas) _canvasMap.get(CanvasType.TITLE);
  }

  /**
   * Returns the model space canvas.
   * @return
   */
  public IModelSpaceCanvas getModelSpaceCanvas() {
    return _modelSpaceCanvas;
  }

  /**
   * Updates the canvas layout model.
   * 
   * @param model the layout model to use for updating.
   */
  public void updateCanvasLayout(final CanvasLayoutModel model) {
    _canvasLayout.updateFrom(model);
  }

  /**
   * Returns a copy of the current canvas layout model.
   * The layout model contains the preferred sizes of the various canvases.
   */
  public CanvasLayoutModel getCanvasLayoutModel() {
    CanvasLayoutModel model = new CanvasLayoutModel();
    model.updateFrom(_canvasLayout);
    return model;
  }

  /**
   * Sets the current canvas layout model.
   * The layout model contains the preferred sizes of the various canvases.
   */
  public void setCanvasLayoutModel(final CanvasLayoutModel model) {
    _canvasLayout.updateFrom(model);
    updateAll();
  }

  /**
   * Triggers the action for editing the canvas layout model.
   */
  public void editCanvasLayout() {
    _editCanvasLayout.run();
  }

  /**
   * Triggers all the canvases to redraw.
   */
  public void updateAll() {
    for (PlotCanvas canvas : getCanvases()) {
      canvas.redraw();
      canvas.update();
    }
    _modelSpaceCanvas.update(UpdateLevel.RESIZE);
    resized();
  }

  /**
   * Returns an array of all the plot canvases.
   */
  private PlotCanvas[] getCanvases() {
    return _canvasMap.values().toArray(new PlotCanvas[0]);
  }

  /**
   * Sets the formatter for the cursor x-coordinate.
   */
  public void setCursorFormatterX(final NumberFormat cursorFormatter) {
    _cursorFormatterX = cursorFormatter;
  }

  /**
   * Sets the formatter for the cursor y-coordinate.
   */
  public void setCursorFormatterY(final NumberFormat cursorFormatter) {
    _cursorFormatterY = cursorFormatter;
  }

  /**
   * Triggered when the cursor location is updated.
   */
  public void cursorUpdated(final double x, final double y, final boolean broadcast) {
    // Get the active model space and its axes.
    IModelSpace modelSpace = _plot.getActiveModelSpace();
    IAxis xAxis = modelSpace.getAxisX();
    IAxis yAxis = modelSpace.getAxisY();
    String xStr = "---";
    String yStr = "---";
    boolean outOfBounds = Double.isNaN(x) || Double.isNaN(y);
    if (!Double.isNaN(x)) {
      xStr = _cursorFormatterX.format(x);
    }
    if (!Double.isNaN(y)) {
      yStr = _cursorFormatterY.format(y);
    }

    boolean first = true;
    StringBuilder builder = new StringBuilder();
    if (xAxis.getLabel().getText().length() > 0) {
      builder.append(xAxis.getLabel().getText() + ": " + xStr);
      first = false;
    }
    if (yAxis.getLabel().getText().length() > 0) {
      if (!first) {
        builder.append("   ");
      }
      builder.append(yAxis.getLabel().getText() + ": " + yStr);
      first = false;
    }

    boolean cursorCrossVisible = !outOfBounds;
    if (_cursorCrossH.isVisible() != cursorCrossVisible) {
      _cursorCrossH.blockUpdate();
      _cursorCrossH.setVisible(cursorCrossVisible);
      _cursorCrossV.setVisible(cursorCrossVisible);
      _cursorCrossH.unblockUpdate();
      _modelSpaceCanvas.update(UpdateLevel.REFRESH);
    }
    if (_showCursorCross && cursorCrossVisible) {
      double x0 = modelSpace.getAxisX().getViewableStart();
      double y0 = modelSpace.getAxisY().getViewableStart();
      double x1 = modelSpace.getAxisX().getViewableEnd();
      double y1 = modelSpace.getAxisY().getViewableEnd();
      _cursorCrossH.blockUpdate();
      _cursorCrossH.getPoint(0).moveTo(x0, y);
      _cursorCrossH.getPoint(1).moveTo(x1, y);
      _cursorCrossH.unblockUpdate();

      _cursorCrossV.blockUpdate();
      _cursorCrossV.getPoint(0).moveTo(x, y0);
      _cursorCrossV.getPoint(1).moveTo(x, y1);
      _cursorCrossV.unblockUpdate();
      _modelSpaceCanvas.update(UpdateLevel.REFRESH);
    }
  }

  public void setCursorCrossVisible(boolean showCursorCross) {
    _showCursorCross = showCursorCross;
  }

  @SuppressWarnings("unused")
  public void cursorSelectionUpdated(final double x, final double y) {
    // does nothing for now
  }

  /**
   * Sets the background color of the plot canvases (excluding the model space canvas).
   */
  public void setCanvasBackgroundColor(final Color color) {
    _canvasMap.get(CanvasType.TOP_AXIS_LABEL).getComposite().setBackground(color);
    _canvasMap.get(CanvasType.LEFT_AXIS_LABEL).getComposite().setBackground(color);
    _canvasMap.get(CanvasType.RIGHT_AXIS_LABEL).getComposite().setBackground(color);
    _canvasMap.get(CanvasType.BOTTOM_AXIS_LABEL).getComposite().setBackground(color);
    _canvasMap.get(CanvasType.TOP_AXIS_LABEL).redraw();
    _canvasMap.get(CanvasType.LEFT_AXIS_LABEL).redraw();
    _canvasMap.get(CanvasType.RIGHT_AXIS_LABEL).redraw();
    _canvasMap.get(CanvasType.BOTTOM_AXIS_LABEL).redraw();
    _canvasMap.get(CanvasType.TOP_AXIS_RANGE).getComposite().setBackground(color);
    _canvasMap.get(CanvasType.LEFT_AXIS_RANGE).getComposite().setBackground(color);
    _canvasMap.get(CanvasType.RIGHT_AXIS_RANGE).getComposite().setBackground(color);
    _canvasMap.get(CanvasType.BOTTOM_AXIS_RANGE).getComposite().setBackground(color);
    _canvasMap.get(CanvasType.TOP_AXIS_RANGE).redraw();
    _canvasMap.get(CanvasType.LEFT_AXIS_RANGE).redraw();
    _canvasMap.get(CanvasType.RIGHT_AXIS_RANGE).redraw();
    _canvasMap.get(CanvasType.BOTTOM_AXIS_RANGE).redraw();
    _canvasMap.get(CanvasType.TOP_LEFT_CORNER).getComposite().setBackground(color);
    _canvasMap.get(CanvasType.TOP_RIGHT_CORNER).getComposite().setBackground(color);
    _canvasMap.get(CanvasType.BOTTOM_LEFT_CORNER).getComposite().setBackground(color);
    _canvasMap.get(CanvasType.BOTTOM_RIGHT_CORNER).getComposite().setBackground(color);
    _canvasMap.get(CanvasType.TOP_LEFT_CORNER).redraw();
    _canvasMap.get(CanvasType.TOP_RIGHT_CORNER).redraw();
    _canvasMap.get(CanvasType.BOTTOM_LEFT_CORNER).redraw();
    _canvasMap.get(CanvasType.BOTTOM_RIGHT_CORNER).redraw();
    _canvasMap.get(CanvasType.TITLE).getComposite().setBackground(color);
    _canvasMap.get(CanvasType.TITLE).redraw();
  }

  /**
   * Sets the text properties for the plot title.
   * 
   * @param font the text font.
   * @param color the text color.
   */
  public void setTitleTextProperties(final Font font, final RGB color) {
    TitleCanvas canvas = (TitleCanvas) _canvasMap.get(CanvasType.TITLE);
    canvas.setTextProperties(font, color);
    canvas.redraw();
  }

  /**
   * Sets the text properties for the axis labels.
   * 
   * @param font the text font.
   * @param color the text color.
   */
  public void setAxisLabelTextProperties(final Font font, final RGB color) {
    CanvasType[] canvasTypes = { CanvasType.TOP_AXIS_LABEL, CanvasType.LEFT_AXIS_LABEL, CanvasType.RIGHT_AXIS_LABEL,
        CanvasType.BOTTOM_AXIS_LABEL };
    for (CanvasType canvasType : canvasTypes) {
      AxisLabelCanvas canvas = (AxisLabelCanvas) _canvasMap.get(canvasType);
      canvas.setTextProperties(font, color);
      canvas.redraw();
    }
  }

  /**
   * Sets the text properties for the axis ranges.
   * 
   * @param font the text font.
   * @param color the text color.
   */
  public void setAxisRangeTextProperties(final Font font, final RGB color) {
    CanvasType[] canvasTypes = { CanvasType.TOP_AXIS_RANGE, CanvasType.LEFT_AXIS_RANGE, CanvasType.RIGHT_AXIS_RANGE,
        CanvasType.BOTTOM_AXIS_RANGE };
    for (CanvasType canvasType : canvasTypes) {
      AxisRangeCanvas canvas = (AxisRangeCanvas) _canvasMap.get(canvasType);
      canvas.setTextProperties(font, color);
      canvas.redraw();
    }
  }

  /**
   * Sets the line properties for the axis grid lines.
   * 
   * @param style the line style.
   * @param color the line color.
   * @param width the line width.
   */
  public void setGridLineProperties(final LineStyle style, final RGB color, final int width) {
    _modelSpaceCanvas.setGridLineProperties(style, color, width);
    redraw();
  }

  /**
   * Updates the axes canvases based on the specified model space.
   */
  public void setAxes(final IModelSpace modelSpace) {
    IModelSpace[] modelSpaces = _plot.getModelSpaces();
    int index = -1;
    for (int i = 0; i < modelSpaces.length; i++) {
      if (modelSpaces[i].equals(modelSpace)) {
        index = i;
        break;
      }
    }
    if (index != -1) {
      _topAxisComposite.setAxis(index);
      _leftAxisComposite.setAxis(index);
      _rightAxisComposite.setAxis(index);
      _bottomAxisComposite.setAxis(index);
    }
  }
}
