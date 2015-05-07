/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.traceviewer.renderer.trace;


import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Widget;
import org.geocraft.core.color.ColorBar;
import org.geocraft.core.color.ColorMapEvent;
import org.geocraft.core.color.ColorMapListener;
import org.geocraft.core.color.ColorMapModel;
import org.geocraft.core.common.math.AGC;
import org.geocraft.core.model.IModelListener;
import org.geocraft.core.model.datatypes.Header;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.TraceHeaderCatalog;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.ui.chartviewer.data.HistogramData;
import org.geocraft.ui.plot.axis.IAxis;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PlotEventType;
import org.geocraft.ui.plot.defs.RenderLevel;
import org.geocraft.ui.plot.event.ModelSpaceEvent;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.model.ModelSpaceBounds;
import org.geocraft.ui.plot.object.IPlotImage;
import org.geocraft.ui.plot.object.IPlotLine;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.PlotImage;
import org.geocraft.ui.plot.object.PlotLine;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.sectionviewer.IPlotTrace;
import org.geocraft.ui.sectionviewer.InterpolationMethod;
import org.geocraft.ui.sectionviewer.NormalizationMethod;
import org.geocraft.ui.sectionviewer.PlotTrace;
import org.geocraft.ui.sectionviewer.renderer.seismic.ITraceInterpolationRenderStrategy;
import org.geocraft.ui.sectionviewer.renderer.seismic.LinearInterpolationRenderStrategy;
import org.geocraft.ui.sectionviewer.renderer.seismic.StepwiseInterpolationRenderStrategy;
import org.geocraft.ui.traceviewer.TraceViewRenderer;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.viewer.layer.ViewLayerEvent;


/**
 * The abstract base class for renderers of traces in a trace viewer.
 * This contains most of the logic for rendering traces, and is used by
 * subclasses such as PostStack3d renderer and PreStack3d renderer.
 */
public class TraceDataRenderer extends TraceViewRenderer implements ControlListener, ColorMapListener, IModelListener {

  /** The plot image for the seismic dataset. */
  protected IPlotImage _plotImage;

  /** The width of the plot image. */
  protected int _imageWidth;

  /** The size of the plot image. */
  protected int _imageHeight;

  /** The byte array of image pixels. */
  protected byte[] _imageColors;

  /** The byte array of image alphas. */
  protected byte _imageAlpha = (byte) 0;

  /** The seismic dataset to render. */
  protected TraceData _traceData;

  /**
   * The model of seismic dataset display properties.
   * These include such things as wiggle traces, variable density,
   * as well as a colorbar.
   */
  protected TraceDataRendererModel _model;

  /** The model space canvas in which to render the image. */
  protected IModelSpaceCanvas _canvas;

  /**
   * The maximum number of colors (252).
   * For an 8-bit image, 256 colors are available, but 4 are needed
   * for other purposed (the wiggle color, the positive color fill,
   * the negative color fill, and a null), which leaves 252.
   */
  protected static final int MAX_COLORS_8BIT = 252;

  /** The list of traces in the trace. */
  protected List<IPlotTrace> _traces;

  /** Flag to indicate rebuild of pixel byte array is needed. */
  protected boolean _rebuildPixels = true;

  /** The trace data minimum. */
  protected float _dataMinimum;

  /** The trace data maximum. */
  protected float _dataMaximum;

  /** The trace data average. */
  protected float _dataAverage;

  protected float[] _traceValues = null;

  /** The corner point for anchoring the top-left of the rendered image. */
  protected Point2D.Double _cornerPoint1;

  /** The corner point for anchoring the bottom-right of the rendered image. */
  protected Point2D.Double _cornerPoint2;

  protected ITraceInterpolationRenderStrategy _interpolationRenderStrategy;

  protected ITraceInterpolationRenderStrategy _linearInterpolationStrategy;

  protected ITraceInterpolationRenderStrategy _stepwiseInterpoationStrategy;

  /**
   * Constructs a renderer for seismic datasets.
   */
  public TraceDataRenderer() {
    this("Seismic Dataset Renderer");

    // Allocate the internal traces array.
    _traces = Collections.synchronizedList(new ArrayList<IPlotTrace>());

    // Turn on the flag for rebuilding the internal pixels array.
    _rebuildPixels = true;

    // Turn on the flag for appending cursor information.
    _appendCursorInfo = true;
  }

  public TraceDataRenderer(final String rendererName) {
    super(rendererName);
    _model = new TraceDataRendererModel();
    _model.setVariableDensity(true);
    _model.setWiggleTrace(true);
    _model.addListener(this);
  }

  public Object[] getRenderedObjects() {
    return new Object[] { _traceData };
  }

  @Override
  public ReadoutInfo getReadoutInfo(final int traceNum, final float z) {

    List<String> keys = new ArrayList<String>();
    List<String> values = new ArrayList<String>();

    IPlotTrace[] plotTraces = getPlotTraces();
    if (traceNum >= 1 && traceNum <= plotTraces.length) {
      IPlotTrace plotTrace = plotTraces[traceNum - 1];
      Trace trace = plotTrace.getTrace();
      Header header = trace.getHeader();
      boolean hasInline = header.getHeaderDefinition().contains(TraceHeaderCatalog.INLINE_NO);
      boolean hasXline = header.getHeaderDefinition().contains(TraceHeaderCatalog.XLINE_NO);
      boolean hasOffset = header.getHeaderDefinition().contains(TraceHeaderCatalog.OFFSET);
      boolean hasCDP = header.getHeaderDefinition().contains(TraceHeaderCatalog.CDP_NO);
      int zIndex = (int) Math.floor((z - trace.getZStart()) / trace.getZDelta());
      if (zIndex >= 0 && zIndex < trace.getNumSamples()) {
        float value = trace.getDataReference()[zIndex];
        if (hasInline) {
          keys.add("Inline");
          values.add("" + header.getInteger(TraceHeaderCatalog.INLINE_NO));
        }
        if (hasXline) {
          keys.add("Xline");
          values.add("" + header.getInteger(TraceHeaderCatalog.XLINE_NO));
        }
        if (hasOffset) {
          keys.add("Offset");
          values.add("" + header.getFloat(TraceHeaderCatalog.OFFSET));
        }
        if (hasCDP) {
          keys.add("CDP");
          values.add("" + header.getFloat(TraceHeaderCatalog.CDP_NO));
        }
        keys.add("Amplitude");
        values.add("" + value);
      }
    }
    return new ReadoutInfo(_traceData.toString(), keys.toArray(new String[0]), values.toArray(new String[0]));
  }

  @Override
  public synchronized void redraw() {
    redraw(true);
  }

  public synchronized void redraw(final boolean reload) {
    _model.setRecomputePercentile();
    _rebuildPixels = true;
    final Trace[] traces = _traceData.getTraces();

    synchronized (_traces) {
      if (traces != null) {
        removeAllTraces();
        for (Trace trace : traces) {
          addTrace(trace);
        }
      }
    }

    _rebuildPixels = true;
    renderImage();
  }

  private void preProcessTraces() {
    if (_traces.size() > 0) {

      Trace refTrace = _traces.get(0).getOriginalTrace();
      int numSamples = refTrace.getNumSamples();
      float zStart = refTrace.getZStart();
      float zDelta = refTrace.getZDelta();
      float[] scalars = getGeometricGainScalars(numSamples, zStart, zDelta);
      AGC agc = new AGC(_model.getAgcType());
      int windowLength = Math.round(_model.getAgcWindowLength() / refTrace.getZDelta());

      // Make sure window length is an odd number.
      if (windowLength % 2 == 0) {
        windowLength++;
      }

      for (IPlotTrace plotTrace : _traces.toArray(new IPlotTrace[0])) {
        Trace trace = plotTrace.getTrace();

        // Restore the original trace data.
        float[] dataOriginal = plotTrace.getOriginalTrace().getDataReference();
        float[] data = trace.getDataReference();
        for (int k = 0; k < trace.getNumSamples(); k++) {
          data[k] = dataOriginal[k];
        }

        // Apply the AGC.
        if (_model.getAgcApply()) {
          agc.applyAGC(data, windowLength, numSamples);
        }

        // Apply the geometric gain.
        for (int k = 0; k < numSamples; k++) {
          data[k] *= scalars[k];
        }

        plotTrace.recomputeDataStats();
      }
    }
  }

  private float[] getGeometricGainScalars(final int numSamples, final float zStart, final float zDelta) {
    boolean apply = _model.getGeometricGainApply();
    float t0 = _model.getGeometricGainT0();
    float tmax = _model.getGeometricGainTMax();
    float n = _model.getGeometricGainN();
    float[] scalars = new float[numSamples];
    float scalar0 = (float) Math.pow(t0 / 1000, n);
    for (int k = 0; k < numSamples; k++) {
      if (apply) {
        float t = zStart + k * zDelta;
        if (t > t0) {
          t = Math.min(t, tmax);
          scalars[k] = (float) Math.pow(t / 1000, n);
        } else {
          scalars[k] = scalar0;
        }
      } else {
        scalars[k] = 1;
      }
    }
    return scalars;
  }

  public boolean hasDataBeenSet() {
    return _traceData != null;
  }

  @Override
  public void refresh() {
    _rebuildPixels = true;
    renderImage();
    //super.updated();
  }

  public void viewLayerUpdated(final ViewLayerEvent event) {
    // The view layer has been updated, so redraw the image.
    redraw(false);
  }

  public void controlMoved(final ControlEvent event) {
    // No action required.
  }

  public void controlResized(final ControlEvent event) {
    // The control has been resized, so redraw the image.
    redraw(false);
  }

  @Override
  public void dispose() {
    if (_plotImage != null) {
      Image image = _plotImage.getImage();
      if (image != null) {
        image.dispose();
      }
    }
    Widget widget = _canvas.getComposite();
    if (!widget.isDisposed()) {
      _canvas.removeControlListener(this);
    }
    super.dispose();
  }

  @Override
  public void modelSpaceUpdated(final ModelSpaceEvent event) {
    PlotEventType eventType = event.getEventType();
    if (eventType.equals(PlotEventType.AXIS_UPDATED) || eventType.equals(PlotEventType.VIEWABLE_BOUNDS_UPDATED)) {
      redraw(false);
    }
  }

  /**
   * An internal method to add a trace to the renderer.
   * 
   * @param trace the trace to add.
   */
  protected void addTrace(final Trace trace) {
    int traceNo = _traces.size() + 1;
    _traces.add(new PlotTrace(traceNo, trace));
    _traceValues = null;
  }

  /**
   * An internal method to remove all traces from the renderer.
   */
  protected void removeAllTraces() {
    _traces.clear();
    _traceValues = null;
  }

  /**
   * Colors the pixels in the image pixels array.
   * 
   * @param width the width of the image.
   * @param height the height of the image.
   */
  public void colorPixels(final int width, final int height) {
    // Create new actual image for drawing from the pixel byte array.
    RGB[] rgbs = buildColorModel(_model.getColorBar());
    PaletteData palette = new PaletteData(rgbs);
    int depth = 8;
    if (_model.getColorBar().getNumColors() >= 252) {
      depth = 16;
    }
    int pw = width;
    int ph = height;
    //System.out.println("Creating image w=" + width + " h=" + height + " d=" + depth);
    ImageData imageData = new ImageData(pw, ph, depth, palette, 1, _imageColors);
    imageData.alpha = _imageAlpha;
    Image image = new Image(_canvas.getComposite().getDisplay(), imageData);

    _rebuildPixels = false;

    if (_plotImage == null) {
      _plotImage = new PlotImage(image, "", _cornerPoint1, _cornerPoint2);
      _plotImage.setRenderLevel(RenderLevel.IMAGE_UNDER_GRID);
      addShape(_plotImage);
    } else {
      _plotImage.blockUpdate();
      Image oldImage = _plotImage.getImage();
      _plotImage.getPoint(0).moveTo(_cornerPoint1.getX(), _cornerPoint1.getY());
      _plotImage.getPoint(1).moveTo(_cornerPoint2.getX(), _cornerPoint2.getY());
      _plotImage.setImage(image);
      _plotImage.unblockUpdate();
      if (oldImage != null) {
        oldImage.dispose();
      }
      _plotImage.updated();
    }
  }

  /**
   * Computes the data statistics for the traces in the trace.
   * This includes the minimum value, maximum value and average value.
   */
  protected void computeDataStatistics() {
    //long time0 = System.currentTimeMillis();
    // Initialize the values.
    _dataMinimum = Float.NaN;
    _dataMaximum = Float.NaN;
    _dataAverage = Float.NaN;
    double dataSum = 0;
    int numSamples = 0;
    // Loops thru all the traces, adding their individual statistics
    // to the overall results.
    for (int i = 0; i < _traces.size(); i++) {
      IPlotTrace plotTrace = _traces.get(i);
      Trace trace = plotTrace.getTrace();
      float dataMinimum = plotTrace.getDataMinimum();
      float dataMaximum = plotTrace.getDataMaximum();
      if (Float.isNaN(_dataMinimum) || Float.isNaN(_dataMaximum)) {
        _dataMinimum = dataMinimum;
        _dataMaximum = dataMaximum;
      } else {
        _dataMinimum = Math.min(dataMinimum, _dataMinimum);
        _dataMaximum = Math.max(dataMaximum, _dataMaximum);
      }
      dataSum += plotTrace.getDataAverage() * trace.getNumSamples();
      numSamples += trace.getNumSamples();
    }
    _dataAverage = (float) (dataSum / numSamples);
    //long time1 = System.currentTimeMillis();
    //System.out.println("Trace statistics computed in " + (time1 - time0) + " ms");
  }

  /**
   * Returns the minimum data value for all the traces.
   */
  public float getDataMinimum() {
    computeDataStatistics();
    return _dataMinimum;
  }

  /**
   * Returns the maximum data value for all the traces.
   * @return
   */
  public float getDataMaximum() {
    computeDataStatistics();
    return _dataMaximum;
  }

  /**
   * Returns the average data value for all the traces.
   */
  public float getDataAverage() {
    computeDataStatistics();
    return _dataAverage;
  }

  /**
   * Returns an array of the internal plot traces.
   */
  protected IPlotTrace[] getPlotTraces() {
    IPlotTrace[] traces = new PlotTrace[_traces.size()];
    for (int i = 0; i < traces.length; i++) {
      traces[i] = _traces.get(i);
    }
    return traces;
  }

  public void colorsChanged(final ColorMapEvent event) {
    if (event.getColorMapModel().getNumColors() != _model.getColorBar().getNumColors()) {
      _rebuildPixels = true;
      ColorBar colorBarOld = _model.getColorBar();
      ColorBar colorBar = new ColorBar(event.getColorMapModel(), colorBarOld.getStartValue(),
          colorBarOld.getEndValue(), colorBarOld.getStepValue());
      colorBar.setReversedRange(colorBarOld.isReversedRange());
      _model.setColorBar(colorBar);
      colorBarOld.dispose();
      redraw(false);
      //      Rectangle rect = _plotImage.getImage().getBounds();
      //      colorPixels(rect.width, rect.height);
      //      super.updated();
      return;
    }
    _model.getColorBar().setColors(event.getColorMapModel().getColors());
    renderImage();
  }

  /**
     * Updates the renderer's model of display properties.
     * The internal model will be updated and a redraw will be triggered.
     * @param model the model containing the updated display properties.
     */
  public void updateRendererModel(final TraceDataRendererModel model) {
    _model.updateFrom(model);
    _rebuildPixels = true;
    redraw(false);
  }

  /**
   * Computes the global minimum and maximum of the trace data based
   * on the normalization method.
   */
  private double[] computeGlobalMinMax() {
    NormalizationMethod normalization = _model.getNormalizationMethod();
    double globalMinimum = 0;
    double globalMaximum = 0;
    if (normalization.equals(NormalizationMethod.BY_LIMITS)) {
      ColorBar colorBar = _model.getColorBar();
      globalMinimum = colorBar.getStartValue();
      globalMaximum = colorBar.getEndValue();
    } else if (normalization.equals(NormalizationMethod.BY_MAXIMUM)) {
      float absMaximum = Math.max(Math.abs(getDataMinimum()), Math.abs(getDataMaximum()));
      ColorBar colorBar = _model.getColorBar(normalization, getTraceValues(), Float.NaN, -absMaximum, absMaximum);
      globalMinimum = colorBar.getStartValue();
      globalMaximum = colorBar.getEndValue();
    } else if (normalization.equals(NormalizationMethod.BY_AVERAGE)) {
      ColorBar colorBar = _model.getColorBar(normalization, getTraceValues(), Float.NaN, -getDataAverage(),
          getDataAverage());
      globalMinimum = colorBar.getStartValue();
      globalMaximum = colorBar.getEndValue();
    }
    return new double[] { globalMinimum, globalMaximum };
  }

  private float[] getTraceValues() {
    int numTraces = _traces.size();
    int numSamples = 0;
    if (numTraces > 0) {
      numSamples = _traces.get(0).getTrace().getNumSamples();
    }
    if (_traceValues == null) {
      _traceValues = new float[numTraces * numSamples];
    }
    for (int i = 0; i < numTraces; i++) {
      Trace trace = _traces.get(i).getTrace();
      System.arraycopy(trace.getDataReference(), 0, _traceValues, i * numSamples, numSamples);
    }
    return _traceValues;
  }

  /**
   * Initializes the arrays used to create the plot image.
   * The arrays are initialized will the <i>null</i> color index and
   * a transparent alpha value.
   */
  private void initializeImageArrays() {
    int numColorBarColors = _model.getColorBar().getNumColors();
    for (int i = 0; i < _imageColors.length; i++) {
      _imageColors[i] = (byte) numColorBarColors; // The null color index.
    }
  }

  /**
   *  If the number of pixels per trace is too small, then decimate the wiggles 
   *  that are drawn.
   * 
   * @param numTraces the number of traces to display.
   * @return an array of boolean flags indicating the display status of each traces wiggles.
   */
  private boolean[] createWiggleDecimationFlags(final int numTraces) {

    int decimation = 1;
    float tracesPerPixel = (float) numTraces / _imageWidth;
    while (tracesPerPixel > 0.25) {
      decimation++;
      tracesPerPixel = (float) (numTraces / decimation) / _imageWidth;
    }
    boolean[] drawWiggle = new boolean[numTraces];
    for (int i = 0; i < numTraces; i++) {
      drawWiggle[i] = i % decimation == 0;
    }
    return drawWiggle;
  }

  /**
   * Renders the traces as a plot image.
   */
  protected synchronized void renderImage() {
    // If the model space has not yet been set, then simply return.
    IModelSpace modelSpace = getModelSpace();
    if (modelSpace == null) {
      return;
    }

    // If the trace has not yet been sec, then simply return.
    if (_traceData == null) {
      return;
    }

    // If the data has not yet been set, throw an exception.
    if (!hasDataBeenSet()) {
      throw new RuntimeException("The seismic dataset should not be null.");
    }

    preProcessTraces();

    // Compute the image bounds, based on the trace trace and viewable window.
    Rectangle imageBounds = computeImageBounds(modelSpace);
    // In the case where the trace is outside the current view bounds, the image bounds
    // returned have negative dimensions. If this happens, simply remove the plot image
    // and null it out. It will be reconstructed when the trace moves back into view.
    if (imageBounds.width < 0 || imageBounds.height < 0) {
      _rebuildPixels = false;
      if (_plotImage != null) {
        removeShape(_plotImage);
        _plotImage = null;
      }
      return;
    }

    // Determine the max and max x,y model coordinates of the image to render.
    Point2D.Double temp1 = new Point2D.Double();
    _canvas.transformPixelToModel(modelSpace, imageBounds.x, imageBounds.y, temp1);
    Point2D.Double temp2 = new Point2D.Double();
    _canvas.transformPixelToModel(modelSpace, imageBounds.x + imageBounds.width - 1, imageBounds.y + imageBounds.height
        - 1, temp2);

    _cornerPoint1 = new Point2D.Double(Math.min(temp1.x, temp2.x), Math.min(temp1.y, temp2.y));
    _cornerPoint2 = new Point2D.Double(Math.max(temp1.x, temp2.x), Math.max(temp1.y, temp2.y));

    // Create a new image if necessary (e.g. the plot was resized).
    boolean updatePixelArray = false;
    try {
      updatePixelArray = createImagePixelArray(imageBounds.width, imageBounds.height);
      _imageWidth = imageBounds.width;
      _imageHeight = imageBounds.height;
    } catch (Exception ex) {
      throw new RuntimeException("Problem drawing seismic dataset " + ex.toString());
    }

    setInterpolationRenderStrategy(modelSpace);

    // Rebuild the pixels array only if a new one was allocated.
    if (updatePixelArray) {
      buildPixels();
    }

    // Always color the pixels array.
    colorPixels(_imageWidth, _imageHeight);

    _rebuildPixels = false;
  }

  /**
   * Sets the strategy for interpolation rendering.
   * Depending on the interpolation method selected (Linear or Stepwise), the corresponding
   * render strategy will be set. Also the pixel rendering strategies (positive fill, negative fill,
   * variables density, etc) will be set for the interpolation renderer.
   * 
   * @param modelSpace the plot model space.
   */
  private void setInterpolationRenderStrategy(final IModelSpace modelSpace) {
    if (_interpolationRenderStrategy == null) {
      _linearInterpolationStrategy = new LinearInterpolationRenderStrategy(_canvas, modelSpace);
      _stepwiseInterpoationStrategy = new StepwiseInterpolationRenderStrategy(_canvas, modelSpace);
    }
    InterpolationMethod interpolation = _model.getInterpolationMethod();
    if (interpolation.equals(InterpolationMethod.LINEAR)) {
      _interpolationRenderStrategy = _linearInterpolationStrategy;
    } else if (interpolation.equals(InterpolationMethod.STEPWISE)) {
      _interpolationRenderStrategy = _stepwiseInterpoationStrategy;
    } else {
      throw new IllegalArgumentException("Invalid interpolation method: " + interpolation);
    }

    // Set the pixel rendering strategies.
    _interpolationRenderStrategy.setPixelRenderStrategies(_model.getWiggleTrace(), _model.getPositiveColorFill(),
        _model.getNegativeColorFill(), _model.getPositiveDensityFill(), _model.getNegativeDensityFill(),
        _model.getVariableDensity());
  }

  /**
   * Computes the bounds (in pixel coordinates) of the image that will be used to
   * render the traces.
   * 
   * @param modelSpace the current plot model space.
   * @return the rectangle containing the image bounds.
   */
  private Rectangle computeImageBounds(final IModelSpace modelSpace) {
    // Determine the min and max x,y pixel coordinates of the viewable window.
    ModelSpaceBounds bounds = modelSpace.getViewableBounds();
    double xStart = bounds.getStartX();
    double xEnd = bounds.getEndX();
    double yStart = bounds.getStartY();
    double yEnd = bounds.getEndY();
    Point2D.Double pxy0 = new Point2D.Double();
    _canvas.transformModelToPixel(modelSpace, xStart, yStart, pxy0);
    Point2D.Double pxy1 = new Point2D.Double();
    _canvas.transformModelToPixel(modelSpace, xEnd, yEnd, pxy1);
    double windowMinX = Math.min(pxy0.x, pxy1.x);
    double windowMaxX = Math.max(pxy0.x, pxy1.x);
    double windowMinY = Math.min(pxy0.y, pxy1.y);
    double windowMaxY = Math.max(pxy0.y, pxy1.y);

    // Determine the min and max x,y pixel coordinates of the trace trace.
    double xmin = 1;
    double xmax = _traceData.getNumTraces();
    double ymin = _traceData.getStartZ();
    double ymax = _traceData.getEndZ();
    Point2D.Double traceTopLeft = new Point2D.Double();
    _canvas.transformModelToPixel(modelSpace, xmin, ymin, traceTopLeft);
    Point2D.Double traceTopRight = new Point2D.Double();
    _canvas.transformModelToPixel(modelSpace, xmax, ymax, traceTopRight);
    int traceMinX = (int) Math.floor(Math.min(traceTopLeft.getX(), traceTopRight.getX()));
    int traceMaxX = (int) Math.ceil(Math.max(traceTopLeft.getX(), traceTopRight.getX()));
    int traceMinY = (int) Math.floor(Math.min(traceTopLeft.getY(), traceTopRight.getY()));
    int traceMaxY = (int) Math.ceil(Math.max(traceTopLeft.getY(), traceTopRight.getY()));

    // Determine the min and max x,y pixel coordinates of the image to render.
    int imageMinX = (int) Math.max(traceMinX, windowMinX);
    int imageMaxX = (int) Math.min(traceMaxX, windowMaxX);
    int imageMinY = (int) Math.max(traceMinY, windowMinY);
    int imageMaxY = (int) Math.min(traceMaxY, windowMaxY);
    int imageWidth = imageMaxX - imageMinX + 1;
    int imageHeight = imageMaxY - imageMinY + 1;

    return new Rectangle(imageMinX, imageMinY, imageWidth, imageHeight);
  }

  /**
   * Creates the image pixel array (if necessary) for the renderer.
   * 
   * @param width the image width.
   * @param height the image height.
   * @return true if a new image was created; false if not.
   */
  private boolean createImagePixelArray(final int width, final int height) throws Exception {
    // Compute the total # of pixels in the image.
    int numPixels = width * height;
    // Check if the image pixel arrays need to be reallocated.
    // 1) If rebuildPixels flag is set.
    // 2) If the imagePixels or imageAlphas arrays are null.
    // 3) If the image width or height have changed.
    if (_rebuildPixels || _imageColors == null || _imageWidth != width || _imageHeight != height
        || numPixels != _imageColors.length) {
      // Allocate new arrays for the image pixels and image alphas.
      _imageColors = new byte[numPixels];
      for (int index = 0; index < numPixels; index++) {
        // The default pixel is the null.
        _imageColors[index] = (byte) _model.getColorBar().getNumColors();
      }
      return true;
    }
    return false;
  }

  /**
   * Builds the image pixels array for the seismic dataset.
   */
  private void buildPixels() {

    // For debug timing purposes only.
    //long start = System.currentTimeMillis();

    // Initialize the pixel and alpha arrays.
    initializeImageArrays();

    // Determine the traces in the viewable range.
    IAxis xAxis = getModelSpace().getAxisX();
    IPlotTrace[] seisPlotTraces = getPlotTraces();
    int traceStart = (int) Math.max(xAxis.getViewableStart() - 0.5, seisPlotTraces[0].getTraceNo());
    int traceEnd = (int) Math.min(xAxis.getViewableEnd() + 0.5, seisPlotTraces[seisPlotTraces.length - 1].getTraceNo());
    int numTraces = traceEnd - traceStart + 1;

    // If there is not at least 1 trace to render, then simply return.
    if (numTraces < 1) {
      return;
    }

    // Render the traces.
    int tracesRendered = renderTraces(seisPlotTraces, traceStart, traceEnd);

    _rebuildPixels = false;

    // For debug purposes only.
    //System.out.println("Traces Rendered = " + tracesRendered + " in " + (System.currentTimeMillis() - start) + " ms");
  }

  /**
   * Renders the given traces in the plot.
   * 
   * @param plotTraces the array of traces (wrapped in PlotTrace objects).
   * @param traceStart the starting trace number.
   * @param traceEnd the ending trace number.
   * @return the number of traces rendered.
   */
  private int renderTraces(final IPlotTrace[] plotTraces, final int traceStart, final int traceEnd) {

    int xCenterPrev = -999;
    int tracesRendered = 0;

    int numColorBarColors = _model.getColorBar().getNumColors();
    boolean isReversedRange = _model.getColorBar().isReversedRange();

    int polarityScalar = 1;
    if (_model.getReversePolarity()) {
      polarityScalar = -1;
    }

    int transparency = (int) (_model.getTransparency() * 255 / 100f);
    int alpha = Math.round(255 - transparency);
    _imageAlpha = (byte) alpha;

    Point2D.Double pixelAnchor = new Point2D.Double();
    _canvas.transformModelToPixel(_modelSpace, _cornerPoint1.x, _cornerPoint1.y, pixelAnchor);

    int pixelAnchorX = (int) pixelAnchor.x;
    int pixelAnchorY = (int) pixelAnchor.y;

    // Initialize the normalization min and max to the global trace statistics.
    NormalizationMethod normalization = _model.getNormalizationMethod();
    double[] globalMinMax = computeGlobalMinMax();
    double normalizationMin = globalMinMax[0];
    double normalizationMax = globalMinMax[1];
    double normalizationOffset = 0.5 * _model.getTraceExaggeration();

    int numTraces = traceEnd - traceStart + 1;
    boolean[] drawWiggle = createWiggleDecimationFlags(numTraces);

    boolean byTraceNormalization = normalization.equals(NormalizationMethod.BY_TRACE_AVERAGE)
        || normalization.equals(NormalizationMethod.BY_TRACE_MAXIMUM);
    int traceClipping = _model.getTraceClipping();

    //    // TODO: Implement this multi-threaded rendering?
    //    long time1 = System.currentTimeMillis();
    //    int numProcessors = Math.max(1, Runtime.getRuntime().availableProcessors() - 2);
    //    int numTracesPerThread = 1 + numTraces / numProcessors;
    //    int numThreads = 0;
    //    int numTracesTemp = 0;
    //    List<Thread> threads = new ArrayList<Thread>();
    //    int trcStart = 1;
    //    int trcEnd = 1;
    //    while (numTracesTemp < numTraces) {
    //      trcEnd = trcStart + numTracesPerThread - 1;
    //      if (trcEnd > numTraces) {
    //        trcEnd = numTraces;
    //      }
    //      numTracesPerThread = trcEnd - trcStart + 1;
    //      TraceRenderRunnable runnable = new TraceRenderRunnable(_canvas, getModelSpace(), isReversedRange,
    //          numColorBarColors, alpha, plotTraces, trcStart, trcEnd, _interpolationRenderStrategy, true, false,
    //          normalization, byTraceNormalization, traceClipping, normalizationMin, normalizationMax, normalizationOffset,
    //          polarityScalar, drawWiggle, pixelAnchorX, pixelAnchorY, _imageWidth, _imageHeight, _imageColors);
    //      Thread t = new Thread(runnable);
    //      threads.add(t);
    //      trcStart = trcEnd + 1;
    //      numTracesTemp += numTracesPerThread;
    //      numThreads++;
    //    }
    //    for (Thread t : threads.toArray(new Thread[0])) {
    //      t.start();
    //    }
    //    for (Thread t : threads.toArray(new Thread[0])) {
    //      try {
    //        t.join();
    //      } catch (InterruptedException e) {
    //        // TODO Auto-generated catch block
    //        e.printStackTrace();
    //      }
    //    }
    //    long time2 = System.currentTimeMillis();
    //    System.out.println(numThreads + "Thread: " + (time2 - time1) + " msec");

    //long time3 = System.currentTimeMillis();
    for (int traceNo = traceStart; traceNo <= traceEnd; traceNo++) {
      int traceIndex = traceNo - traceStart;
      IPlotTrace plotTrace = plotTraces[traceNo - 1];
      Trace trace = plotTrace.getTrace();
      float[] data = trace.getDataReference();

      // Determine the normalization min and max, if normalization is trace-based.
      if (normalization.equals(NormalizationMethod.BY_TRACE_MAXIMUM)) {
        normalizationMin = plotTrace.getDataMinimum() * polarityScalar;
        normalizationMax = plotTrace.getDataMaximum() * polarityScalar;
        double minmax = Math.max(Math.abs(normalizationMin), Math.abs(normalizationMax));
        normalizationMin = -minmax;
        normalizationMax = minmax;
      } else if (normalization.equals(NormalizationMethod.BY_TRACE_AVERAGE)) {
        normalizationMax = plotTrace.getDataAverage() * polarityScalar;
        normalizationMin = -normalizationMax * polarityScalar;
      }
      double normalizationFactor = Math.max(Math.abs(normalizationMin), Math.abs(normalizationMax));

      // For dead or missing traces, the normalization factor equals zero, which is invalid,
      // so reset it to 1. Also set a normalization min and max so that the computed color
      // is in the center of the color bar.
      if (byTraceNormalization && (plotTrace.getTrace().isDead() || plotTrace.getTrace().isMissing())) {
        normalizationMin = -1;
        normalizationMax = 1;
        normalizationFactor = 1;
      }

      Point2D.Double pixelTopCenter = new Point2D.Double();
      _canvas.transformModelToPixel(getModelSpace(), traceNo, trace.getZStart(), pixelTopCenter);
      Point2D.Double pixelTopLeft = new Point2D.Double();
      _canvas.transformModelToPixel(getModelSpace(), traceNo - 0.5, trace.getZStart(), pixelTopLeft);
      Point2D.Double pixelBottomRight = new Point2D.Double();
      _canvas.transformModelToPixel(getModelSpace(), traceNo + 0.5, trace.getZEnd(), pixelBottomRight);

      int xCenter = Math.round((float) pixelTopCenter.x);
      int xLeft = Math.round((float) pixelTopLeft.x);
      int yTop = Math.round((float) pixelTopLeft.y);
      int xRight = Math.round((float) pixelBottomRight.x);
      int yBottom = Math.round((float) pixelBottomRight.y);
      xLeft -= pixelAnchorX;
      xRight -= pixelAnchorX;

      // Check the pixel x coordinate against the previously rendered trace.
      // If it is the same, then skip this trace.
      if (xCenterPrev != -999 && xCenter == xCenterPrev) {
        continue;
      }
      xCenterPrev = xCenter;
      tracesRendered++;

      // Build the wiggle arrays.
      int numSamples = trace.getNumSamples();
      int[] xLinear = new int[numSamples];
      int[] yLinear = new int[numSamples];
      int[] xStep = new int[1 + (numSamples - 1) * 2];
      int[] yStep = new int[1 + (numSamples - 1) * 2];
      double pixelsPerSample = (pixelBottomRight.y - pixelTopLeft.y) / (numSamples - 1);
      Point2D.Double pixelCoord = new Point2D.Double();
      for (int i = 0; i < numSamples; i++) {
        double dx = data[i] * polarityScalar * normalizationOffset / normalizationFactor;
        if (dx >= 0) {
          dx = Math.min(dx, traceClipping);
        } else {
          dx = Math.max(dx, -traceClipping);
        }
        double x = traceNo + dx;
        double y = trace.getZStart() + i * trace.getZDelta();
        _canvas.transformModelToPixel(getModelSpace(), x, y, pixelCoord);
        int px = (int) (pixelCoord.x + 0.5) - pixelAnchorX;
        int py = (int) (pixelCoord.y + 0.5) - pixelAnchorY;
        xLinear[i] = px;
        yLinear[i] = py;
        if (i > 0) {
          xStep[1 + (i - 1) * 2] = xLinear[i - 1];
          yStep[1 + (i - 1) * 2] = yLinear[i];
          xStep[2 + (i - 1) * 2] = xLinear[i];
          yStep[2 + (i - 1) * 2] = yLinear[i];
        } else {
          xStep[i] = px;
          yStep[i] = py;
        }
      }
      int numPixelsY = yLinear[numSamples - 1] - yLinear[0] + 1;

      int[] xWiggle = xLinear;
      int[] yWiggle = yLinear;
      if (_interpolationRenderStrategy.equals(_linearInterpolationStrategy)) {
        xWiggle = xLinear;
        yWiggle = yLinear;
      } else if (_interpolationRenderStrategy.equals(_stepwiseInterpoationStrategy)) {
        xWiggle = xStep;
        yWiggle = yStep;
      }
      _interpolationRenderStrategy.renderTrace(traceNo, traceIndex, numSamples, data, alpha, numColorBarColors,
          isReversedRange, polarityScalar, normalizationMin, normalizationMax, normalizationFactor,
          normalizationOffset, traceClipping, drawWiggle, pixelAnchorX, pixelAnchorY, pixelCoord, xLeft, xCenter,
          xRight, yTop, numPixelsY, xWiggle, yWiggle, pixelsPerSample, _imageWidth, _imageHeight, _imageColors);
    }
    long time4 = System.currentTimeMillis();
    //System.out.println("1 Thread: " + (time4 - time3) + " msec");

    return tracesRendered;
  }

  /**
   * Returns the index of a pixel coordinate in a 1D array, based on its indices in a 2D array.
   * 
   * @param xIndex the x index of the pixel in a 2D array.
   * @param yIndex the y index of the pixel in a 2D array.
   * @param numPixelsX the width (x dimension) of the 2D array.
   * @param numPixelsY the height (y dimension) of the 2D array.
   * @return the index in a 1D array.
   */
  private int getPixelIndex(final int xIndex, final int yIndex, final int numPixelsX, final int numPixelsY) {
    if (xIndex < 0 || xIndex > numPixelsX || yIndex < 0 || yIndex > numPixelsY) {
      return -1;
    }
    return yIndex * numPixelsX + xIndex;
  }

  /**
   * Builds the color model for the plot image, based on the specified colormap.
   * The color model is returned as an array of RGB values, whose size equals
   * the number of colors in the colormap + 4. The 4 extra colors represent the
   * following: a null color, the wiggle color, the positive fill color, and the
   * negative fill color. These 4 extra colors are the last 4 elements in the
   * array of RGB values returned.
   * 
   * @param colormap the colormap to use.
   * @return the color model as an array of RGB values.
   */
  private RGB[] buildColorModel(final ColorMapModel colormap) {
    int numColorBarColors = colormap.getNumColors();
    RGB[] rgbs = new RGB[numColorBarColors + 4];
    for (int i = 0; i < numColorBarColors; i++) {
      rgbs[i] = colormap.getColor(i);
    }
    rgbs[numColorBarColors + 0] = _model.getColorNull();
    rgbs[numColorBarColors + 1] = _model.getColorWiggle();
    rgbs[numColorBarColors + 2] = _model.getColorPositiveFill();
    rgbs[numColorBarColors + 3] = _model.getColorNegativeFill();
    return rgbs;
  }

  public TraceDataRendererModel getSettingsModel() {
    return _model;
  }

  @Override
  protected void addPlotShapes() {
    IPlotLine line = new PlotLine();
    IPlotPoint point1 = new PlotPoint(1, _traceData.getStartZ(), 0);
    IPlotPoint point2 = new PlotPoint(_traceData.getNumTraces(), _traceData.getEndZ(), 0);
    line.setPoints(point1, point2);
    line.setLineStyle(LineStyle.NONE);
    line.setLineWidth(0);
    addShape(line);
  }

  @Override
  protected void addPopupMenuActions() {
    Dialog dialog = new TraceDataRendererDialog(getShell(), _traceData.toString(), this);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
    addPopupMenuAction(new SeismicHistogramAction(this));
  }

  @Override
  protected void setNameAndImage() {
    setName("Traces");
    //setImage(ModelUI.getSharedImages().getImage(_traceData));
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(IViewer.SEISMIC_FOLDER, autoUpdate);
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    _traceData = (TraceData) objects[0];
    _canvas = getViewer().getModelSpaceCanvas();
    _model.setDataUnit(Unit.SEISMIC_AMPLITUDE);
    _model.setGeometricGainTMax(_traceData.getEndZ());
  }

  public void propertyChanged(final String key) {
    // Nothing to do.
  }

  @Override
  public DataSelection getDataSelection(final double x, final double y) {
    return null;
  }

  public HistogramData getHistogramData() {
    if (_traces.isEmpty()) {
      return null;
    }
    int numTraces = _traces.size();
    int numSamples = _traces.get(0).getTrace().getNumSamples();
    float[] values = new float[numTraces * numSamples];
    for (int i = 0; i < numTraces; i++) {
      IPlotTrace plotTrace = _traces.get(i);
      Trace trace = plotTrace.getTrace();
      int index = i * numSamples;
      System.arraycopy(trace.getDataReference(), 0, values, index, trace.getNumSamples());
    }
    float dataMin = getDataMinimum();
    float dataMax = getDataMaximum();
    return new HistogramData(getName(), values, Float.NaN, 100, dataMin, dataMax, new RGB(255, 0, 0));
  }

}
