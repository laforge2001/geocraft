/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.internal;


import java.awt.geom.Point2D;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.geocraft.ui.plot.IAxisCanvas;
import org.geocraft.ui.plot.ICanvas;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.action.IPlotMouseAction;
import org.geocraft.ui.plot.action.PlotMouseActionList;
import org.geocraft.ui.plot.action.PlotMouseEvent;
import org.geocraft.ui.plot.attribute.LineProperties;
import org.geocraft.ui.plot.axis.IAxis;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PointInsertionMode;
import org.geocraft.ui.plot.defs.RenderLevel;
import org.geocraft.ui.plot.defs.ShapeType;
import org.geocraft.ui.plot.defs.UpdateLevel;
import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.plot.listener.ICanvasListener;
import org.geocraft.ui.plot.listener.ICursorListener;
import org.geocraft.ui.plot.listener.IPlotMouseListener;
import org.geocraft.ui.plot.model.CoordinateTransform;
import org.geocraft.ui.plot.model.ICoordinateTransform;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.object.IPlotMovableShape;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPointGroup;
import org.geocraft.ui.plot.object.IPlotPolygon;
import org.geocraft.ui.plot.object.IPlotShape;
import org.geocraft.ui.plot.renderer.BackgroundColorRenderer;
import org.geocraft.ui.plot.renderer.BackgroundImageRenderer;
import org.geocraft.ui.plot.renderer.HorizontalGridLinesRenderer;
import org.geocraft.ui.plot.renderer.IBackgroundRenderer;
import org.geocraft.ui.plot.renderer.IGridLinesRenderer;
import org.geocraft.ui.plot.renderer.ModelSpaceRenderer;
import org.geocraft.ui.plot.renderer.VerticalGridLinesRenderer;
import org.geocraft.ui.plot.util.PlotUtil;


/**
 * Simple implementation of the model space canvas interface.
 */
public class ModelSpaceCanvas extends Canvas implements IModelSpaceCanvas, ICanvasListener, IPlotMouseListener,
    PaintListener {

  /** The associated plot. */
  private final IPlot _plot;

  /** The image-graphics buffer for the static render level. */
  protected PlotImageGraphics _bufferStatic;

  /** The image buffer for the static render level. */
  protected Image _bufferStaticImage;

  /** The graphics buffer for the static render level. */
  protected GC _bufferStaticGraphics;

  /** The image-graphics buffer for the selected render level. */
  protected PlotImageGraphics _bufferSelected;

  /** The image buffer for the selected render level. */
  protected Image _bufferSelectedImage;

  /** The graphics buffer for the selected render level. */
  protected GC _bufferSelectedGraphics;

  /** The image-graphics for the background render level. */
  protected PlotImageGraphics _bufferBackground;

  /** The image buffer for the background render level. */
  protected Image _bufferBackgroundImage;

  /** The graphics buffer for the background render level. */
  protected GC _bufferBackgroundGraphics;

  /** The plot background renderer. */
  private IBackgroundRenderer _backgroundRenderer;

  /** The horizontal axis renderer. */
  private final IGridLinesRenderer _horizontalAxisRenderer;

  /** The vertical axis renderer. */
  private final IGridLinesRenderer _verticalAxisRenderer;

  /** The model space renderer. */
  private final ModelSpaceRenderer _modelSpaceRenderer;

  /** The plot coordinate transform. */
  private final ICoordinateTransform _coordTransform;

  /** The set of plot cursor listeners. */
  private final Set<ICursorListener> _cursorListeners;

  private final LineProperties _verticalGridLineProperties;

  private final LineProperties _horizontalGridLineProperties;

  private IAxisCanvas _topAxisCanvas;

  private IAxisCanvas _leftAxisCanvas;

  private IAxisCanvas _rightAxisCanvas;

  private IAxisCanvas _bottomAxisCanvas;

  private final PlotMouseAdapter _mouseAdapter;

  private UpdateLevel _updateLevel;

  /** The rectangle drawn for zooming. */
  private IPlotPolygon _zoomRectangle;

  /** The active shape being edited, or <i>null</i> if none.. */
  private IPlotShape _activeShape;

  /** The plot point in motion, or <i>null</i> if none. */
  private IPlotPoint _pointInMotion;

  /** The plot shape in motion, or <i>null</i> if none. */
  private IPlotMovableShape _shapeInMotion;

  /** The tolerance (in pixels) for point/shape selection. */
  protected int _selectionTolerance = 5;

  /** The mode for inserting points into a shape. */
  private PointInsertionMode _pointInsertionMode;

  /** Flag for <i>rubberbanding</i> when creating new shapes. */
  private boolean _rubberband = true;

  private Cursor _cursor;

  private int _horizontalGridLineDensity = 10;

  private int _verticalGridLineDensity = 10;

  /** The image & graphics associated with the canvas. */
  protected PlotImageGraphics _imageGraphics;

  /** The image & graphics buffer associated with the canvas. */
  protected PlotImageGraphics _imageGraphicsBuffer;

  /**
   * The default constructor.
   */
  public ModelSpaceCanvas(final Composite parent, final IPlot plot) {
    super(parent, SWT.DOUBLE_BUFFERED);
    _plot = plot;
    _imageGraphics = null;
    _imageGraphicsBuffer = null;
    //addMouseMoveListener(this);
    Point size = new Point(400, 400);
    setSize(size);
    setPreferredSize(size);

    // Create the coordinate transform.
    _coordTransform = new CoordinateTransform(this);
    _cursor = new Cursor(getDisplay(), SWT.CURSOR_ARROW);

    // Create the background renderer.
    RGB white = new RGB(255, 255, 255);
    try {
      Image backgroundImage = PlotUtil.createImage("image/Seismic24.png");
      _backgroundRenderer = new BackgroundImageRenderer(backgroundImage, false);
      if (backgroundImage != null) {
        backgroundImage.dispose();
      }
    } catch (IOException e) {
      _backgroundRenderer = new BackgroundColorRenderer(white);
    }
    _backgroundRenderer = new BackgroundColorRenderer(white);

    // Create the vertical grid lines properties.
    Color lineColor = getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
    _verticalGridLineProperties = new LineProperties(LineStyle.DASHED, lineColor.getRGB(), 1);

    // Create the horizontal grid lines properties.
    _horizontalGridLineProperties = new LineProperties(LineStyle.DASHED, lineColor.getRGB(), 1);

    // Create the horizontal axis grid lines renderer.
    _horizontalAxisRenderer = new HorizontalGridLinesRenderer(_coordTransform);

    // Create the vertical axis grid lines renderer.
    _verticalAxisRenderer = new VerticalGridLinesRenderer(_coordTransform);

    // Create the model space renderer.
    _modelSpaceRenderer = new ModelSpaceRenderer(_coordTransform);

    // Create the list of cursor listeners.
    _cursorListeners = Collections.synchronizedSet(new HashSet<ICursorListener>());
    _cursorListeners.add(_plot);

    // Create the plot mouse adapter.
    _mouseAdapter = new PlotMouseAdapter(_plot, this);

    // Add the plot mouse adapter as a listener to mouse actions.
    addMouseTrackListener(new MouseTrackAdapter() {

      @Override
      public void mouseEnter(MouseEvent e) {
        //setFocus();
      }

    });
    addMouseListener(_mouseAdapter);
    addMouseMoveListener(_mouseAdapter);
    addMouseTrackListener(_mouseAdapter);
    addMouseWheelListener(_mouseAdapter);

    _mouseAdapter.addListener(this);
    PlotMouseActionList actionList = PlotMouseActionList.getDefaultObjectActions();
    for (IPlotMouseAction action : actionList.getActions()) {
      _mouseAdapter.addListener(action);
    }

    // Add the canvas as listener to various things.
    addPaintListener(this);

    ControlListener listener = new ControlListener() {

      @SuppressWarnings("unused")
      public void controlResized(final ControlEvent event) {
        checkAspectRatio();
        update(UpdateLevel.RESIZE);
        update();
      }

      public void controlMoved(final ControlEvent event) {
        // No action.
      }
    };
    addControlListener(listener);

    // Set the initial update level.
    _updateLevel = UpdateLevel.RESIZE;

    _pointInsertionMode = PointInsertionMode.LAST;
    _rubberband = true;
  }

  public RGB getBackgroundColor() {
    return _backgroundRenderer.getColor();
  }

  public Composite getComposite() {
    return this;
  }

  public void setAxisCanvases(final IAxisCanvas topAxisCanvas, final IAxisCanvas leftAxisCanvas,
      final IAxisCanvas rightAxisCanvas, final IAxisCanvas bottomAxisCanvas) {
    _topAxisCanvas = topAxisCanvas;
    _leftAxisCanvas = leftAxisCanvas;
    _rightAxisCanvas = rightAxisCanvas;
    _bottomAxisCanvas = bottomAxisCanvas;
    _topAxisCanvas.addCanvasListener(this);
    _leftAxisCanvas.addCanvasListener(this);
    _rightAxisCanvas.addCanvasListener(this);
    _bottomAxisCanvas.addCanvasListener(this);
  }

  public void transformModelToPixel(final IModelSpace model, final double mx, final double my, final Point2D.Double p) {
    _coordTransform.transformModelToPixel(model, mx, my, p);
  }

  public void transformPixelToModel(final IModelSpace model, final int px, final int py, final Point2D.Double m) {
    _coordTransform.transformPixelToModel(model, px, py, m);
  }

  //  public Dimension getPreferredScrollableViewportSize() {
  //    return new Dimension(getWidth(), getHeight());
  //  }
  //
  //  public int getScrollableBlockIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
  //    return 10000;
  //  }
  //
  //  public int getScrollableUnitIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
  //    return 10000;
  //  }
  //
  //  public boolean getScrollableTracksViewportHeight() {
  //    return false;
  //  }
  //
  //  public boolean getScrollableTracksViewportWidth() {
  //    return false;
  //  }

  public void addCursorListener(final ICursorListener listener) {
    _cursorListeners.add(listener);
  }

  public void removeCursorListener(final ICursorListener listener) {
    _cursorListeners.add(listener);
  }

  /**
   * Notifies the cursor listeners when the cursor is updated.
   * @param cursorLocation the cursor location string.
   */
  private void notifyCursorListeners(final double x, final double y) {
    ICursorListener[] listeners = _cursorListeners.toArray(new ICursorListener[0]);
    for (ICursorListener listener : listeners) {
      listener.cursorUpdated(x, y, true);
    }
  }

  /**
   * Notifies the cursor listeners when the cursor is updated.
   * @param cursorLocation the cursor location string.
   */
  private void notifyCursorSelectionListeners(final double x, final double y) {
    ICursorListener[] listeners = _cursorListeners.toArray(new ICursorListener[0]);
    for (ICursorListener listener : listeners) {
      listener.cursorSelectionUpdated(x, y);
    }
  }

  public Point getPreferredSize() {
    return getSize();
  }

  public void setPreferredSize(final Point size) {
    setSize(size);
  }

  public void canvasUpdated(final ICanvas canvas) {
    redraw();
  }

  //  private PlotSpaceBounds checkAspectRatio(final PlotSpace plotSpace,
  //      final PlotSpaceBounds defaultBounds,
  //      final PlotSpaceBounds viewableBounds, final Dimension size) {
  //    if (!plotSpace.isAspectRatioFixed()) {
  //      return viewableBounds;
  //    }
  //    double aspectRatio = plotSpace.getFixedAspectRatio();
  //    double currentPixelAR = (double) size.height / size.width;
  //    double dxPref = defaultBounds.getXEnd() - defaultBounds.getXStart();
  //    double dyPref = defaultBounds.getYEnd() - defaultBounds.getYStart();
  //    double dxModel = viewableBounds.getXEnd() - viewableBounds.getXStart();
  //    double dyModel = viewableBounds.getYEnd() - viewableBounds.getYStart();
  //    double xCtr = (viewableBounds.getXStart() + viewableBounds.getXEnd()) / 2;
  //    double yCtr = (viewableBounds.getYStart() + viewableBounds.getYEnd()) / 2;
  //    double preferredModelAR = dyPref / dxPref;
  //    // double currentModelAR = dyModel / dxModel;
  //    // double currentAR = currentPixelAR / currentModelAR;
  //    double preferredAR = currentPixelAR / preferredModelAR;
  //    double updatedModelAR = currentPixelAR / aspectRatio;
  //    double dx = dxModel;
  //    double dy = dyModel;
  //    if (preferredAR < aspectRatio) {
  //      double dy1 = Math
  //          .abs(defaultBounds.getYEnd() - defaultBounds.getYStart());
  //      double dy2 = Math.abs(viewableBounds.getYEnd()
  //          - viewableBounds.getYStart());
  //      dy = dy2;// Math.min(dy1, dy2);
  //      dx = dy / updatedModelAR;
  //    } else if (preferredAR > aspectRatio) {
  //      double dx1 = Math
  //          .abs(defaultBounds.getXEnd() - defaultBounds.getXStart());
  //      double dx2 = Math.abs(viewableBounds.getXEnd()
  //          - viewableBounds.getXStart());
  //      dx = dx2;// Math.min(dx1, dx2);
  //      dy = dx * updatedModelAR;
  //    }
  //    int xSign = 1;
  //    if (viewableBounds.getXStart() > viewableBounds.getXEnd()) {
  //      xSign = -1;
  //    }
  //    double xStart = xCtr - xSign * dx / 2;
  //    double xEnd = xCtr + xSign * dx / 2;
  //    int ySign = 1;
  //    if (viewableBounds.getYStart() > viewableBounds.getYEnd()) {
  //      ySign = -1;
  //    }
  //    double yStart = yCtr - ySign * dy / 2;
  //    double yEnd = yCtr + ySign * dy / 2;
  //    return new PlotSpaceBounds(xStart, xEnd, yStart, yEnd);
  //  }
  /**
   * Checks the aspect ratio of the canvas and model.
   */
  public void checkAspectRatio() {
    for (IModelSpace modelSpace : _plot.getModelSpaces()) {
      if (modelSpace.isFixedAspectRatio()) {
        double aspectRatio = modelSpace.aspectRatio();
        Point size = getComposite().getSize();
        double width = size.x;
        double height = size.y;
        double currentPixelAR = height / width;
        IAxis xAxis = modelSpace.getAxisX();
        IAxis yAxis = modelSpace.getAxisY();
        double dxPref = xAxis.getDefaultEnd() - xAxis.getDefaultStart();
        double dyPref = yAxis.getDefaultEnd() - yAxis.getDefaultStart();
        double dxModel = xAxis.getViewableEnd() - xAxis.getViewableStart();
        double dyModel = yAxis.getViewableEnd() - yAxis.getViewableStart();
        double xCtr = (xAxis.getViewableStart() + xAxis.getViewableEnd()) / 2;
        double yCtr = (yAxis.getViewableStart() + yAxis.getViewableEnd()) / 2;
        double preferredModelAR = dyPref / dxPref;
        // double currentModelAR = dyModel / dxModel;
        // double currentAR = currentPixelAR / currentModelAR;
        double preferredAR = currentPixelAR / preferredModelAR;
        double updatedModelAR = currentPixelAR / aspectRatio;
        double dx = dxModel;
        double dy = dyModel;
        if (preferredAR < aspectRatio) {
          double dy1 = Math.abs(yAxis.getDefaultEnd() - yAxis.getDefaultStart());
          double dy2 = Math.abs(yAxis.getViewableEnd() - yAxis.getViewableStart());
          dy = dy2;//Math.min(dy1, dy2);
          dx = dy / updatedModelAR;
        } else if (preferredAR > aspectRatio) {
          double dx1 = Math.abs(xAxis.getDefaultEnd() - xAxis.getDefaultStart());
          double dx2 = Math.abs(xAxis.getViewableEnd() - xAxis.getViewableStart());
          dx = dx2;//Math.min(dx1, dx2);
          dy = dx * updatedModelAR;
        }
        int xSign = 1;
        if (xAxis.getViewableStart() > xAxis.getViewableEnd()) {
          xSign = -1;
        }
        double xStart = xCtr - xSign * dx / 2;
        double xEnd = xCtr + xSign * dx / 2;
        int ySign = 1;
        if (yAxis.getViewableStart() > yAxis.getViewableEnd()) {
          ySign = -1;
        }
        double yStart = yCtr - ySign * dy / 2;
        double yEnd = yCtr + ySign * dy / 2;
        modelSpace.setViewableBounds(xStart, xEnd, yStart, yEnd);
      }
    }
  }

  public void paintControl(final PaintEvent event) {
    if (_updateLevel.equals(UpdateLevel.RESIZE) || _bufferStaticGraphics == null || _bufferSelectedGraphics == null) {
      updateBuffers();
    }
    UpdateLevel updateLevel = _updateLevel;
    paintControlCustom(event, updateLevel);
  }

  /**
   * Does the custom painting of the model canvas.
   * @param g the graphics in which to paint.
   */
  protected void paintControlCustom(final PaintEvent event, final UpdateLevel updateLevel) {
    Point size = getSize();
    ////Image bufferImage = new Image(event.display, size.x, size.y);
    ///Image bufferImage = ResourceFactory.createImage(event.display, size.x, size.y);

    // Allocate the GC.
    ////GC gc = new GC(bufferImage);
    GC gc = event.gc;
    ///GC gc = ResourceFactory.createGC(bufferImage);

    // Turn on anti-aliasing.
    gc.setAntialias(SWT.OFF);
    gc.setTextAntialias(SWT.OFF);

    // Clear out the select graphics buffer.
    clearGraphics(_bufferSelectedGraphics);

    // Create a basic stroke.
    //BasicStroke basicStroke = new BasicStroke();
    //gc.setStroke(basicStroke);

    // If resizing or redrawing, need to redraw the static graphics buffer.
    if (updateLevel.equals(UpdateLevel.RESIZE) || updateLevel.equals(UpdateLevel.REDRAW)) {
      // Clear the static graphics buffer.
      clearGraphics(_bufferStaticGraphics);

      // Draw the backgrid image, if one exists, into the static graphics buffer.
      if (_bufferBackgroundImage != null) {
        //_bufferStaticGraphics.drawImage(_bufferBackgroundImage, 0, 0);
      }
      //gc.setStroke(basicStroke);

      // Draw the background objects into the static graphics buffer.
      renderModelSpace(_bufferStaticGraphics, RenderLevel.BACKGROUND);
      //gc.setStroke(basicStroke);

      // Draw the image-under-grid objects into the static graphics buffer.
      renderModelSpace(_bufferStaticGraphics, RenderLevel.IMAGE_UNDER_GRID);
      //gc.setStroke(basicStroke);

      // Draw the grid objects into the static graphics buffer.
      renderModelSpace(_bufferStaticGraphics, RenderLevel.GRID);
      //gc.setStroke(basicStroke);

      // Draw the image-over-grid objects into the static graphics buffer.
      renderModelSpace(_bufferStaticGraphics, RenderLevel.IMAGE_OVER_GRID);
      //gc.setStroke(basicStroke);

      // Draw the static group into the static graphics buffer.
      renderModelSpace(_bufferStaticGraphics, RenderLevel.STANDARD);
      //gc.setStroke(basicStroke);
    }
    // Draw the static buffer image into the selected graphics buffer.
    _bufferSelectedGraphics.drawImage(_bufferStaticImage, 0, 0);
    //gc.setStroke(basicStroke);

    // Draw the selected objects into the selected graphics buffer.
    renderModelSpace(_bufferSelectedGraphics, RenderLevel.SELECTED);
    //gc.setStroke(basicStroke);

    // Draw the selected buffer image into the current graphics.
    gc.drawImage(_bufferSelectedImage, 0, 0);
    //gc.setStroke(basicStroke);

    ///event.gc.drawImage(bufferImage, 0, 0);
    ///bufferImage.dispose();
    ///ResourceFactory.disposeImage(bufferImage);

    // Dispose of the GC.
    ///gc.dispose();
    ///ResourceFactory.disposeGC(gc);

    // Everything has been redraw, so the update level can be set to refresh.
    _updateLevel = UpdateLevel.REFRESH;
  }

  /**
   * Draws the specified render level.
   * @param gc the graphics to draw in.
   * @param renderLevel the render level to draw (Standard, Selected, Background, etc).
   */
  protected void renderModelSpace(final GC gc, final RenderLevel renderLevel) {
    // No drawing rectangle is defined, so use the default.
    // No drawing mask is defined, so use null.
    renderModelSpace(gc, null, renderLevel);
  }

  /**
   * Draws the specified render level for the specified mask rectangle.
   * @param gc the graphics to draw in.
   * @param mask the rectangle mask to draw in.
   * @param renderLevel the render level to draw (Standard, Selected, Background, etc).
   */
  protected void renderModelSpace(final GC gc, final Rectangle mask, final RenderLevel renderLevel) {
    // No drawing rectangle is defined, so use the default.
    Point size = getSize();
    int width = size.x;
    int height = size.y;
    Rectangle rect = new Rectangle(0, 0, width, height);
    renderModelSpace(gc, rect, mask, renderLevel);
  }

  /**
   * Draws the specified render level for the specified mask rectangle,
   * into the specified rectangle.
   * @param gc the graphics to draw in.
   * @param rect the rectangle to draw in.
   * @param mask the rectangle mask to draw in.
   * @param renderLevel the render level to draw (Standard, Selected, Background, etc).
   */
  protected void renderModelSpace(final GC gc, final Rectangle rect, final Rectangle mask, final RenderLevel renderLevel) {
    int x0 = rect.x;
    int y0 = rect.y;
    int x1 = x0 + rect.width - 1;
    int y1 = y0 + rect.height - 1;

    // Draw the grid group.
    if (renderLevel.equals(RenderLevel.BACKGROUND)) {
      Point size = getSize();
      _backgroundRenderer.render(gc, new Rectangle(0, 0, size.x, size.y));
    } else if (renderLevel.equals(RenderLevel.GRID)) {
      for (IModelSpace model : _plot.getModelSpaces()) {
        IAxis xAxis = model.getAxisX();
        if (_topAxisCanvas.getAxis().equals(xAxis) || _bottomAxisCanvas.getAxis().equals(xAxis)) {
          _horizontalAxisRenderer.render(gc, getBounds(), xAxis, _horizontalGridLineProperties,
              _horizontalGridLineDensity);
        }
        IAxis yAxis = model.getAxisY();
        if (_leftAxisCanvas.getAxis().equals(yAxis) || _rightAxisCanvas.getAxis().equals(yAxis)) {
          _verticalAxisRenderer.render(gc, getBounds(), yAxis, _verticalGridLineProperties, _verticalGridLineDensity);
        }
      }
    } else {
      for (IModelSpace modelSpace : _plot.getModelSpaces()) {
        if (!_plot.renderActiveModelOnly() || modelSpace.equals(_plot.getActiveModelSpace())) {
          _modelSpaceRenderer.render(gc, getBounds(), null, renderLevel, modelSpace);
        }
      }
    }
  }

  public void renderOld(final GC gc) {
    Point size = getSize();
    _backgroundRenderer.render(gc, new Rectangle(0, 0, size.x, size.y));

    for (IModelSpace model : _plot.getModelSpaces()) {
      IAxis xAxis = model.getAxisX();
      if (_topAxisCanvas.getAxis().equals(xAxis) || _bottomAxisCanvas.getAxis().equals(xAxis)) {
        _horizontalAxisRenderer.render(gc, getBounds(), xAxis, _horizontalGridLineProperties,
            _horizontalGridLineDensity);
      }
      IAxis yAxis = model.getAxisY();
      if (_leftAxisCanvas.getAxis().equals(yAxis) || _rightAxisCanvas.getAxis().equals(yAxis)) {
        _verticalAxisRenderer.render(gc, getBounds(), yAxis, _verticalGridLineProperties, _verticalGridLineDensity);
      }
    }
    for (IModelSpace model : _plot.getModelSpaces()) {
      _modelSpaceRenderer.render(gc, getBounds(), null, RenderLevel.STANDARD, model);
    }
  }

  /**
   * Draws the blank bounds of the plot.
   * @param gc the graphics to draw in.
   */
  protected void clearGraphics(final GC gc) {
    int width = getSize().x;
    int height = getSize().y;
    Color color = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
    //Color color = PlotUtil.createColor(null, PlotUtil.RGB_WHITE);
    gc.setBackground(color);
    gc.fillRectangle(0, 0, width, height);
  }

  /**
   * Updates the static/active image buffers.
   */
  protected void updateBuffers() {
    Point size = getSize();
    int width = size.x;
    int height = size.y;

    if (width < 2) {
      width = 2;
    }
    if (height < 2) {
      height = 2;
    }

    // Update the static image graphics.
    PlotImageGraphics oldImageGraphics = _bufferStatic;
    _bufferStatic = new PlotImageGraphics(this, width, height);
    _bufferStaticImage = _bufferStatic.getImage();
    _bufferStaticGraphics = _bufferStatic.getGraphics();
    if (oldImageGraphics != null) {
      oldImageGraphics.dispose();
    }

    // Update the selected image graphics.
    oldImageGraphics = _bufferSelected;
    _bufferSelected = new PlotImageGraphics(this, width, height);
    _bufferSelectedImage = _bufferSelected.getImage();
    _bufferSelectedGraphics = _bufferSelected.getGraphics();
    if (oldImageGraphics != null) {
      oldImageGraphics.dispose();
    }

    if (_bufferBackground != null) {
      _bufferBackgroundImage = _bufferBackground.getImage();
      _bufferBackgroundGraphics = _bufferBackground.getGraphics();
    }
  }

  public UpdateLevel getUpdateLevel() {
    return _updateLevel;
  }

  public void update(final UpdateLevel updateLevel) {
    Rectangle mask = null;
    update(updateLevel, mask);
  }

  public void update(final UpdateLevel updateLevel, final Rectangle mask) {
    _updateLevel = updateLevel;
    if (this.isDisposed()) {
      return;
    }
    redraw();
    update();
    // TODO: investigate this update();
  }

  @Override
  public void dispose() {
    _cursorListeners.clear();
    _mouseAdapter.dispose();
    _verticalGridLineProperties.dispose();
    _horizontalGridLineProperties.dispose();
    Image backgroundImage = _backgroundRenderer.getImage();
    if (backgroundImage != null) {
      backgroundImage.dispose();
    }
    super.dispose();
  }

  public IPlotShape getActiveShape() {
    // TODO: does this belong here in canvas?
    return _activeShape;
  }

  public void setActiveShape(final IPlotShape shape) {
    if (_activeShape != null && _activeShape instanceof IPlotPointGroup && _rubberband) {
      ((IPlotPointGroup) _activeShape).rubberbandOff();
    }
    // Set the active shape.
    _activeShape = shape;
  }

  public IPlotPoint getNearestSelectablePoint(final IPlotShape shape, final double px, final double py) {
    return getNearestPoint(shape, px, py, true);
  }

  public IPlotPoint getNearestSelectablePoint(final double px, final double py) {
    return getNearestPoint(px, py, true);
  }

  public IPlotPoint getNearestPoint(final double px, final double py) {
    return getNearestPoint(px, py, false);
  }

  public IPlotPoint getNearestPoint(final double px, final double py, final boolean mustBeSelectable) {
    IPlotPoint pointNearest = null;
    double dist;
    double minDist = 0;
    boolean first = true;
    for (IModelSpace modelSpace : _plot.getModelSpaces()) {
      for (IPlotLayer plotGroup : modelSpace.getLayers()) {
        for (IPlotShape shape : plotGroup.getShapes()) {
          ShapeType shapeType = shape.getShapeType();
          if (!mustBeSelectable || mustBeSelectable && shape.isSelectable()) {
            if (shapeType.equals(ShapeType.POINT_GROUP) || shapeType.equals(ShapeType.POLYLINE)
                || shapeType.equals(ShapeType.POLYGON) || shapeType.equals(ShapeType.LINE)
                || shapeType.equals(ShapeType.RECTANGLE)) {

              for (IPlotPoint point : shape.getPoints()) {
                IModelSpace ms = shape.getModelSpace();
                Point2D.Double pixelCoord = new Point2D.Double(0, 0);
                transformModelToPixel(shape.getModelSpace(), point.getX(), point.getY(), pixelCoord);
                double dpx = pixelCoord.getX() - px;
                double dpy = pixelCoord.getY() - py;

                dist = Math.sqrt(dpx * dpx + dpy * dpy);
                if (first) {
                  pointNearest = point;
                  minDist = dist;
                  first = false;
                } else {
                  if (dist < minDist) {
                    pointNearest = point;
                    minDist = dist;
                  }
                }
              }
            }
          }
        }
      }
    }

    return pointNearest;
  }

  public double computePixelDistance(final IModelSpace modelSpace, final double x0, final double y0, final double x1,
      final double y1) {
    Point2D.Double pixelCoord0 = new Point2D.Double(0, 0);
    Point2D.Double pixelCoord1 = new Point2D.Double(0, 0);
    transformModelToPixel(modelSpace, x0, y0, pixelCoord0);
    transformModelToPixel(modelSpace, x1, y1, pixelCoord1);
    double dpx = pixelCoord1.getX() - pixelCoord0.getX();
    double dpy = pixelCoord1.getY() - pixelCoord0.getY();
    return Math.sqrt(dpx * dpx + dpy * dpy);
  }

  public IPlotPoint getNearestPoint(final IPlotShape shape, final double px, final double py) {
    return getNearestPoint(shape, px, py, false);
  }

  public IPlotPoint getNearestPoint(final IPlotShape shape, final double px, final double py,
      final boolean mustBeSelectable) {
    IPlotPoint pointNearest = null;
    double dist;
    double minDist = 0;
    boolean first = true;
    ShapeType shapeType = shape.getShapeType();

    // If selection is requested on a non-selectable shape, then return a null.
    if (mustBeSelectable && !shape.isSelectable()) {
      return pointNearest;
    }

    // Check if the shape is a point shape.
    if (shapeType.equals(ShapeType.POINT_GROUP) || shapeType.equals(ShapeType.POLYLINE)
        || shapeType.equals(ShapeType.POLYGON) || shapeType.equals(ShapeType.LINE)
        || shapeType.equals(ShapeType.RECTANGLE)) {

      // Loop thru the points in the shape.
      for (IPlotPoint point : shape.getPoints()) {
        // Transform the point into pixel coordinates.
        Point2D.Double pixelCoord = new Point2D.Double(0, 0);
        transformModelToPixel(shape.getModelSpace(), point.getX(), point.getY(), pixelCoord);
        double dpx = pixelCoord.getX() - px;
        double dpy = pixelCoord.getY() - py;
        // Compute the distance to the point, and record if minimum.
        dist = Math.sqrt(dpx * dpx + dpy * dpy);
        if (first) {
          pointNearest = point;
          minDist = dist;
          first = false;
        } else {
          if (dist < minDist) {
            pointNearest = point;
            minDist = dist;
          }
        }
      }
    }
    // Return the nearest point.
    return pointNearest;
  }

  public IPlotPoint getPointInMotion() {
    return _pointInMotion;
  }

  public void setPointInMotion(final IPlotPoint point) {
    _pointInMotion = point;
  }

  public IPlotMovableShape getShapeInMotion() {
    return _shapeInMotion;
  }

  public void setShapeInMotion(final IPlotMovableShape shape) {
    _shapeInMotion = shape;
  }

  public PointInsertionMode getPointInsertionMode() {
    return _pointInsertionMode;
  }

  public void setPointInsertionMode(final PointInsertionMode mode) {
    _pointInsertionMode = mode;
  }

  public int getSelectionTolerance() {
    return _selectionTolerance;
  }

  public void setSelectionTolerance(final int tolerance) {
    _selectionTolerance = tolerance;
  }

  public boolean getRubberband() {
    return _rubberband;
  }

  public void setRubberband(final boolean rubberband) {
    _rubberband = rubberband;
  }

  public List<IPlotShape> getSelectedShapes() {
    List<IPlotShape> selectedShapes = Collections.synchronizedList(new ArrayList<IPlotShape>());

    for (IModelSpace modelSpace : _plot.getModelSpaces()) {
      for (IPlotLayer group : modelSpace.getLayers()) {
        for (IPlotShape shape : group.getShapes()) {
          if (shape.isSelected()) {
            selectedShapes.add(shape);
          }
        }
      }
    }

    return selectedShapes;
  }

  public void mouseDoubleClick(final PlotMouseEvent event) {
    // TODO: implement this

  }

  public void mouseDown(final PlotMouseEvent event) {
    if (event.getMouseEvent().button == 3) {
      IModelSpace modelSpace = _plot.getActiveModelSpace();
      if (modelSpace != null) {
        Point2D.Double coords = event.getModelCoord();
        if (coords != null) {
          notifyCursorSelectionListeners(coords.x, coords.y);
        }
      }
    }
  }

  public void mouseMove(final PlotMouseEvent event) {
    StringBuilder builder = new StringBuilder();
    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMaximumIntegerDigits(10);
    format.setMaximumFractionDigits(1);
    format.setMinimumFractionDigits(1);
    IModelSpace modelSpace = _plot.getActiveModelSpace();
    double x = Double.NaN;
    double y = Double.NaN;
    if (modelSpace != null) {
      Point2D.Double coords = event.getModelCoord();
      if (coords != null) {
        IAxis xAxis = modelSpace.getAxisX();
        IAxis yAxis = modelSpace.getAxisY();
        x = coords.x;
        y = coords.y;
        builder.append(xAxis.getLabel().getText() + ": " + format.format(x) + "   " + yAxis.getLabel().getText() + ": "
            + format.format(y));

        if (_activeShape != null && _activeShape instanceof IPlotPointGroup && _rubberband) {
          if (coords != null) {
            IPlotPointGroup pointGroup = (IPlotPointGroup) _activeShape;
            pointGroup.rubberband(_pointInsertionMode, coords.getX(), coords.getY());
          }
        }
        //        if (_activeShape != null && _rubberband) {
        //          if (coords != null) {
        //            IPlotPoint point = new PlotPoint(coords.getX(), coords.getY(), 0);
        //            IPlotPointGroup pointGroup = (IPlotPointGroup) _activeShape;
        //            if (_pointInsertionIndex >= 0) {
        //              _pointInsertionIndex = Math.min(_pointInsertionIndex, pointGroup.getPointCount() - 1);
        //              pointGroup.removePoint(pointGroup.getPoint(_pointInsertionIndex));
        //            }
        //            _pointInsertionIndex = pointGroup.addPoint(point, _pointInsertionMode);
        //          }
        //        }

        //        PlotMouseEvent e = transformMouseEvent(event);
        //        fireModelMouseMotionEvent(e);
        //        if (_activeShape != null && _rubberband) {
        //          Point2D.Double modelCoord = e.getModelCoord();
        //          if (modelCoord != null) {
        //            IPlotPoint point = PlotFactory.get().createPoint(modelCoord.getX(), modelCoord.getY(), 0);
        //            IPlotPointGroup pointGroup = (IPlotPointGroup) _activeShape;
        //            if (_pointInsertionIndex >= 0) {
        //              _pointInsertionIndex = Math.min(_pointInsertionIndex, pointGroup.getPointCount() - 1);
        //              pointGroup.deletePoint(pointGroup.getPoint(_pointInsertionIndex));
        //            }
        //            _pointInsertionIndex = pointGroup.addPoint(point, _pointInsertionMode);
        //            PlotCursorEvent cursorEvent = new PlotCursorEvent(new Point(e.getX(), e.getY()), modelCoord);
        //            fireCursorEvent(cursorEvent);
        //          }
        //        }
      }
    }
    notifyCursorListeners(x, y);

  }

  public void mouseUp(final PlotMouseEvent event) {
    // TODO: implement this

  }

  public void mouseEnter(final PlotMouseEvent event) {
    // TODO: implement this

  }

  public void mouseExit(final PlotMouseEvent event) {
    StringBuilder builder = new StringBuilder();
    IModelSpace modelSpace = _plot.getActiveModelSpace();
    if (modelSpace != null) {
      IAxis xAxis = modelSpace.getAxisX();
      IAxis yAxis = modelSpace.getAxisY();
      builder.append(xAxis.getLabel().getText() + ": ---   " + yAxis.getLabel().getText() + ": ---");
    }
    notifyCursorListeners(Double.NaN, Double.NaN);
  }

  public void mouseHover(final PlotMouseEvent event) {
    // TODO: implement this

  }

  public IPlotPolygon getZoomRectangle() {
    return _zoomRectangle;
  }

  public void setZoomRectangle(final IPlotPolygon zoomRectangle) {
    _zoomRectangle = zoomRectangle;
  }

  public void setBackgroundColor(final RGB color) {
    _backgroundRenderer.setColor(color);
    Color colorOld = super.getBackground();
    super.setBackground(new Color(null, color));
    if (colorOld != null && !colorOld.isDisposed()) {
      colorOld.dispose();
    }
  }

  /**
   * Sets the mouse actions for the model space canvas.
   * @param actions the array of mouse actions to set.
   */
  public void setMouseActions(final IPlotMouseAction[] actions) {
    _mouseAdapter.clear();
    _mouseAdapter.addListener(this);
    for (IPlotMouseAction action : actions) {
      _mouseAdapter.addListener(action);
    }
  }

  /**
   * Sets the cursor style for the model space canvas.
   * @param style one of the pre-defined SWT cursor styles.
   */
  public void setCursorStyle(final int style) {
    Cursor cursorOld = _cursor;
    _cursor = new Cursor(getDisplay(), style);
    setCursor(_cursor);
    if (cursorOld != null) {
      cursorOld.dispose();
    }
  }

  public void setGridLineProperties(final LineStyle style, final RGB color, final int width) {
    _verticalGridLineProperties.setStyle(style);
    _verticalGridLineProperties.setColor(color);
    _verticalGridLineProperties.setWidth(width);
    _horizontalGridLineProperties.setStyle(style);
    _horizontalGridLineProperties.setColor(color);
    _horizontalGridLineProperties.setWidth(width);
    update(UpdateLevel.REDRAW);
  }

  public void setHorizontalAxisGridLineProperties(final LineStyle style, final RGB color, final int width) {
    _horizontalGridLineProperties.setStyle(style);
    _horizontalGridLineProperties.setColor(color);
    _horizontalGridLineProperties.setWidth(width);
    update(UpdateLevel.REDRAW);
  }

  public void setVerticalAxisGridLineProperties(final LineStyle style, final RGB color, final int width) {
    _verticalGridLineProperties.setStyle(style);
    _verticalGridLineProperties.setColor(color);
    _verticalGridLineProperties.setWidth(width);
    update(UpdateLevel.REDRAW);
  }

  public void setHorizontalAxisGridLineDensity(final int density) {
    _horizontalGridLineDensity = density;
  }

  public void setVerticalAxisGridLineDensity(final int density) {
    _verticalGridLineDensity = density;
  }

  public PlotImageGraphics getImageGraphics() {
    return _imageGraphics;
  }

  @Override
  public void setSize(Point size) {
    super.setSize(size);
    for (IModelSpace modelSpace : _plot.getModelSpaces()) {
      modelSpace.updated();
    }
  }

  @Override
  public void setSize(int width, int height) {
    super.setSize(width, height);
    for (IModelSpace modelSpace : _plot.getModelSpaces()) {
      modelSpace.updated();
    }
  }

  public void setImageGraphics(final PlotImageGraphics imageGraphics) {
    PlotImageGraphics imageGraphicsOld = _imageGraphics;
    _imageGraphics = imageGraphics;
    if (imageGraphicsOld != null) {
      imageGraphicsOld.dispose();
    }
  }

}
