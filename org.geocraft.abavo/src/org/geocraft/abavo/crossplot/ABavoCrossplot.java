/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot;


import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.geocraft.abavo.ABavoImages;
import org.geocraft.abavo.Activator;
import org.geocraft.abavo.ClassBackgroundColorMap;
import org.geocraft.abavo.aplusb.GenerateAplusBVolume2d;
import org.geocraft.abavo.aplusb.GenerateAplusBVolume3d;
import org.geocraft.abavo.classbkg.GenerateClassVolume2d;
import org.geocraft.abavo.classbkg.GenerateClassVolume3d;
import org.geocraft.abavo.crossplot.CrossplotBoundsModel.BoundsType;
import org.geocraft.abavo.crossplot.action.ConstructEllipse;
import org.geocraft.abavo.crossplot.action.ConstructSelectionPolygon;
import org.geocraft.abavo.crossplot.action.DisableCommunication;
import org.geocraft.abavo.crossplot.action.DisableCrossplotAnchor;
import org.geocraft.abavo.crossplot.action.EditBounds;
import org.geocraft.abavo.crossplot.action.EditColorBar;
import org.geocraft.abavo.crossplot.action.EditEllipseRegionsModel;
import org.geocraft.abavo.crossplot.action.EditPolygonRegionsModel;
import org.geocraft.abavo.crossplot.action.EnableCommunication;
import org.geocraft.abavo.crossplot.action.EnableCrossplotAnchor;
import org.geocraft.abavo.crossplot.action.EndPolygonDefinition;
import org.geocraft.abavo.crossplot.action.MoveSeriesToFront;
import org.geocraft.abavo.crossplot.action.OpenAlgorithm;
import org.geocraft.abavo.crossplot.action.SetRegressionMethod;
import org.geocraft.abavo.crossplot.layer.CrossplotSeriesLayer;
import org.geocraft.abavo.crossplot.layer.EllipseLayer;
import org.geocraft.abavo.crossplot.layer.EllipseRegionsImageLayer;
import org.geocraft.abavo.crossplot.layer.EllipseRegionsModelLayer;
import org.geocraft.abavo.crossplot.layer.PolygonLayer;
import org.geocraft.abavo.crossplot.layer.PolygonRegionsModelLayer;
import org.geocraft.abavo.crossplot.layer.RegionBoundsLayer;
import org.geocraft.abavo.crossplot.layer.RegressionLayer;
import org.geocraft.abavo.defs.CommunicationStatus;
import org.geocraft.abavo.ellipse.EllipseRegionsModel;
import org.geocraft.abavo.ellipse.EllipseRegionsModelEvent;
import org.geocraft.abavo.ellipse.RegionsBoundary;
import org.geocraft.abavo.ellipse.RegionsBoundaryModel;
import org.geocraft.abavo.ellipse.EllipseRegionsModel.EllipseType;
import org.geocraft.abavo.polygon.PolygonModel;
import org.geocraft.abavo.polygon.PolygonRegionsModel;
import org.geocraft.abavo.polygon.PolygonRegionsModelEvent;
import org.geocraft.abavo.polygon.PolygonRegionsModel.PolygonType;
import org.geocraft.abavo.preferences.CrossplotPreferencePage;
import org.geocraft.abavo.preferences.CrossplotPreferencePage2;
import org.geocraft.abavo.preferences.CrossplotPreferencePage3;
import org.geocraft.core.color.ColorBar;
import org.geocraft.core.color.map.SpectrumColorMap;
import org.geocraft.core.common.preferences.PropertyStoreFactory;
import org.geocraft.core.model.IModelListener;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.message.Topic;
import org.geocraft.internal.abavo.ABavoCursor;
import org.geocraft.internal.abavo.PreferencesConstants;
import org.geocraft.internal.abavo.ServiceComponent;
import org.geocraft.internal.abavo.polygon.PolygonController;
import org.geocraft.math.regression.IRegressionMethodService;
import org.geocraft.math.regression.RegressionDataStatistics;
import org.geocraft.math.regression.RegressionMethodDescription;
import org.geocraft.math.regression.RegressionStatistics;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.plot.PlotScrolling;
import org.geocraft.ui.plot.PlotView;
import org.geocraft.ui.plot.RendererViewLayer;
import org.geocraft.ui.plot.action.IPlotMouseAction;
import org.geocraft.ui.plot.action.PlotActionMask;
import org.geocraft.ui.plot.action.point.EndPointMotionAction;
import org.geocraft.ui.plot.action.point.PointMotionAction;
import org.geocraft.ui.plot.action.point.StartPointMotionAction;
import org.geocraft.ui.plot.action.shape.DeselectShapeAction;
import org.geocraft.ui.plot.action.shape.EndShapeMotionAction;
import org.geocraft.ui.plot.action.shape.SelectShapeAction;
import org.geocraft.ui.plot.action.shape.ShapeMotionAction;
import org.geocraft.ui.plot.action.shape.StartShapeMotionAction;
import org.geocraft.ui.plot.attribute.LineProperties;
import org.geocraft.ui.plot.attribute.PointProperties;
import org.geocraft.ui.plot.axis.IAxis;
import org.geocraft.ui.plot.defs.ActionMaskType;
import org.geocraft.ui.plot.defs.FillStyle;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.object.IPlotLine;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPolygon;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.util.PolygonRegionsUtil;
import org.geocraft.ui.viewer.IRenderer;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.viewer.action.HelpAction;
import org.geocraft.ui.viewer.layer.ILayeredModel;
import org.geocraft.ui.viewer.layer.IViewLayer;
import org.geocraft.ui.viewer.layer.LayeredModel;
import org.geocraft.ui.viewer.layer.LayeredModelContentProvider;
import org.geocraft.ui.viewer.layer.LayeredModelLabelProvider;
import org.geocraft.ui.viewer.layer.ViewGroupLayer;
import org.geocraft.ui.viewer.toolbar.SimpleToolBar;


/**
 * An implementation of an ABAVO crossplot.
 * This implementation is built on top of the org.geocraft.ui.plot code
 * in GeoCraft.
 */
public class ABavoCrossplot extends PlotView implements IABavoCrossplot, IModelListener {

  /** The logger. */
  static {
    IRegressionMethodService service = null;
  }

  /** The ellipse regions model. */
  private EllipseRegionsModel _ellipseRegionsModel;

  /** The polygon regions model. */
  private PolygonRegionsModel _polygonRegionsModel;

  /** The array of data series. */
  private ABDataSeries[] _series;

  /** The array of data series point properties. */
  private PointProperties[] _seriesPointProps;

  /** The array of data series line properties. */
  private LineProperties[] _seriesLineProps;

  /** The color bar used for the rendering of point z-values. */
  private ColorBar _depthColorBar;

  /** The color bar used for the rendering of class regions. */
  private ColorBar _classColorBar;

  /** The plot layer for the background (inner) ellipse. */
  private EllipseLayer _backgroundEllipseLayer;

  /** The plot layer for the maximum (outer) ellipse. */
  private EllipseLayer _maximumEllipseLayer;

  /** The plot layer for the selection ellipse. */
  private EllipseLayer _selectionEllipseLayer;

  /** The plot layers for the region polygons. */
  private PolygonLayer[] _polygonLayers;

  /** The plot layer for the selection polygon. */
  private PolygonLayer _selectionPolygonLayer;

  /** The plot layer for the ellipse region boundaries layer. */
  private RegionBoundsLayer _regionBoundsLayer;

  /** The plot layer for the ellipse regions image layer. */
  private EllipseRegionsImageLayer _regionsImageLayer;

  /** The combo for selecting the active series. */
  private Combo _activeSeriesCombo;

  private List<IPlotMouseAction> _defaultMouseActions;

  private CrossplotBoundsModel _crossplotBoundsModel;

  private final ABavoCrossplotModel _model;

  private final List<ICrossplotSelectionListener> _selectionListeners;

  private final IPreferenceStore _preferenceStore;

  private final IPreferenceStore _preferenceStore2;

  private final IPreferenceStore _preferenceStore3;

  private final IPropertyChangeListener _preferenceStoreListener;

  private final IPropertyChangeListener _preferenceStoreListener2;

  private final IPropertyChangeListener _preferenceStoreListener3;

  public ABavoCrossplot(final Composite parent, final IModelSpace modelSpace, final IWorkbenchPartSite site) {
    super(parent, "Seismic Volume Crossplot", modelSpace, PlotScrolling.NONE, true, false);
    // Initialize the crossplot model.
    _model = new ABavoCrossplotModel();
    _model.addListener(this);

    _selectionListeners = Collections.synchronizedList(new ArrayList<ICrossplotSelectionListener>());

    // Initializes the data series display attributes.
    initializeDataSeriesAttributes();

    getSharedToolBar().addPushButton(new HelpAction("org.geocraft.abavo.crossplotavsb"));

    // Initialize the custom tool bar for the ABAVO crossplot.
    initializeCustomToolBar(site);

    // Initialize and set the default mouse actions for the ABAVO crossplot.
    initializeDefaultMouseActions();

    // Set the initial regression method to the 1st one returned from the service.
    RegressionMethodDescription[] methods = ServiceComponent.getRegressionMethodService().getRegressionMethods();
    _model.setRegressionMethod(methods[0]);

    // Add the canvas as a listener to the crossplot preference pages.
    _preferenceStore = PropertyStoreFactory.getStore(CrossplotPreferencePage.ID);
    CrossplotPreferencePage.setDefaults();
    _preferenceStoreListener = new IPropertyChangeListener() {

      @Override
      public void propertyChange(final org.eclipse.jface.util.PropertyChangeEvent event) {
        updateColorsAndFonts();
      }
    };
    _preferenceStore.addPropertyChangeListener(_preferenceStoreListener);

    // Add the preferences for the data series colors, point sizes and symbols.
    _preferenceStore2 = PropertyStoreFactory.getStore(CrossplotPreferencePage2.ID);
    CrossplotPreferencePage2.setDefaults();
    _preferenceStoreListener2 = new IPropertyChangeListener() {

      @Override
      public void propertyChange(final org.eclipse.jface.util.PropertyChangeEvent event) {
        updateDataSeriesAttributes();
      }
    };
    _preferenceStore2.addPropertyChangeListener(_preferenceStoreListener2);

    // Add the preferences for the ellipse colors and line widths.
    _preferenceStore3 = PropertyStoreFactory.getStore(CrossplotPreferencePage3.ID);
    CrossplotPreferencePage3.setDefaults();
    _preferenceStoreListener3 = new IPropertyChangeListener() {

      @Override
      public void propertyChange(final org.eclipse.jface.util.PropertyChangeEvent event) {
        updateEllipseAttributes();
      }
    };
    _preferenceStore3.addPropertyChangeListener(_preferenceStoreListener3);

    //Initialize default colors
    updateColorsAndFonts();
    updateDataSeriesAttributes();
    updateEllipseAttributes();
  }

  protected void updateDataSeriesAttributes() {
    for (int i = 0; i < IABavoCrossplot.MAX_SERIES; i++) {
      int id = i + 1;
      RGB rgb = PreferenceConverter.getColor(_preferenceStore2, PreferencesConstants.DATA_SERIES_COLOR + id);
      _seriesPointProps[i].setColor(rgb);
      int size = _preferenceStore2.getInt(PreferencesConstants.DATA_SERIES_SIZE + id);
      _seriesPointProps[i].setSize(size);
      String symbol = _preferenceStore2.getString(PreferencesConstants.DATA_SERIES_SYMBOL + id);
      PointStyle style = PointStyle.lookup(symbol);
      if (style != null) {
        _seriesPointProps[i].setStyle(style);
      }
      RendererViewLayer viewLayer = (RendererViewLayer) getLayerModel().findLayerByName("Series #" + id + " Points");
      if (viewLayer != null) {
        CrossplotSeriesLayer plotLayer = (CrossplotSeriesLayer) viewLayer.getPlotLayer();
        plotLayer.setPointProperties(_seriesPointProps[i]);
      }
    }

    getPlotComposite().redraw();
    getLayerViewer().refresh();
  }

  protected void updateEllipseAttributes() {
    RGB rgb = PreferenceConverter.getColor(_preferenceStore3, PreferencesConstants.MINIMUM_ELLIPSE_COLOR);
    int lineWidth = _preferenceStore3.getInt(PreferencesConstants.MINIMUM_ELLIPSE_LINE_WIDTH);

    RendererViewLayer viewLayer = (RendererViewLayer) getLayerModel().findLayerByName(
        EllipseType.Background.toString() + " Ellipse");
    if (viewLayer != null) {
      EllipseLayer plotLayer = (EllipseLayer) viewLayer.getPlotLayer();
      plotLayer.setLineProperties(rgb, lineWidth);
    }

    rgb = PreferenceConverter.getColor(_preferenceStore3, PreferencesConstants.MAXIMUM_ELLIPSE_COLOR);
    lineWidth = _preferenceStore3.getInt(PreferencesConstants.MAXIMUM_ELLIPSE_LINE_WIDTH);
    viewLayer = (RendererViewLayer) getLayerModel().findLayerByName(EllipseType.Maximum.toString() + " Ellipse");
    if (viewLayer != null) {
      EllipseLayer plotLayer = (EllipseLayer) viewLayer.getPlotLayer();
      plotLayer.setLineProperties(rgb, lineWidth);
    }

    rgb = PreferenceConverter.getColor(_preferenceStore3, PreferencesConstants.SELECTION_ELLIPSE_COLOR);
    lineWidth = _preferenceStore3.getInt(PreferencesConstants.SELECTION_ELLIPSE_LINE_WIDTH);
    viewLayer = (RendererViewLayer) getLayerModel().findLayerByName(EllipseType.Selection.toString() + " Ellipse");
    if (viewLayer != null) {
      EllipseLayer plotLayer = (EllipseLayer) viewLayer.getPlotLayer();
      plotLayer.setLineProperties(rgb, lineWidth);
    }

    getPlotComposite().redraw();
    getLayerViewer().refresh();
  }

  /**
   * 
   */
  protected void updateColorsAndFonts() {
    int width = _preferenceStore.getInt(PreferencesConstants.AXIS_LINE_WIDTH);
    //if (width != _preferenceStore.getDefaultInt(PreferencesConstants.AXIS_LINE_WIDTH)) {
    if (true) {
      LineStyle style = LineStyle.lookup(_preferenceStore.getString(PreferencesConstants.AXIS_LINE_STYLE));
      RGB rgb = PreferenceConverter.getColor(_preferenceStore, PreferencesConstants.AXIS_LINE_COLOR);
      getPlotComposite().setGridLineProperties(style, rgb, width);
    }

    FontData fontData = PreferenceConverter.getFontData(_preferenceStore, PreferencesConstants.PLOT_TITLE_TEXT_FONT);
    //if (!fontData.equals(PreferenceConverter.FONTDATA_DEFAULT_DEFAULT)) {
    if (true) {
      RGB fontRGB = PreferenceConverter.getColor(_preferenceStore, PreferencesConstants.PLOT_TITLE_TEXT_COLOR);
      Font textFont = new Font(null, fontData);
      getPlotComposite().setTitleTextProperties(textFont, fontRGB);
      textFont.dispose();
    }

    fontData = PreferenceConverter.getFontData(_preferenceStore, PreferencesConstants.AXIS_LABEL_TEXT_FONT);
    //if (!fontData.equals(PreferenceConverter.FONTDATA_DEFAULT_DEFAULT)) {
    if (true) {
      RGB fontRGB = PreferenceConverter.getColor(_preferenceStore, PreferencesConstants.AXIS_LABEL_TEXT_COLOR);
      Font textFont = new Font(null, fontData);
      getPlotComposite().setAxisLabelTextProperties(textFont, fontRGB);
      textFont.dispose();
    }

    fontData = PreferenceConverter.getFontData(_preferenceStore, PreferencesConstants.AXIS_RANGE_TEXT_FONT);
    //if (!fontData.equals(PreferenceConverter.FONTDATA_DEFAULT_DEFAULT)) {
    if (true) {
      RGB fontRGB = PreferenceConverter.getColor(_preferenceStore, PreferencesConstants.AXIS_RANGE_TEXT_COLOR);
      Font textFont = new Font(null, fontData);
      getPlotComposite().setAxisRangeTextProperties(textFont, fontRGB);
      textFont.dispose();
    }

    RGB bkgRGB = PreferenceConverter.getColor(_preferenceStore, PreferencesConstants.PLOT_BACKGROUND_COLOR);
    //if (!bkgRGB.equals(PreferenceConverter.COLOR_DEFAULT_DEFAULT)) {
    if (true) {
      Color bkgColor = new Color(null, bkgRGB);
      getPlotComposite().setCanvasBackgroundColor(bkgColor);
      bkgColor.dispose();
    }

    getPlotComposite().redraw();
  }

  private int addSeriesData(final ABDataSeries series) {

    double xmin = 0;
    double xmax = 0;
    double ymin = 0;
    double ymax = 0;
    double zmin = 0;
    double zmax = 0;
    double xymax;
    int status = 0;
    int limit = 200000;

    // Create a number formatter.
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(0);
    nf.setGroupingUsed(false);

    // For usability purposes, a limit of 200,000 points has been
    // established. Prompt the user to continue if that limit is
    // being exceeded.
    int numPoints = series.getNumPoints();
    if (numPoints > limit) {
      String message = "Point count (" + nf.format(numPoints) + ") exceeds " + nf.format(limit) + "!\nContinue?";
      boolean confirm = MessageDialog.openConfirm(new Shell(Display.getCurrent()), "Add Data Series", message);
      if (!confirm) {
        series.clear();
        return 0;
      }
    }

    // Set the wait cursor.
    setCursorStyle(SWT.CURSOR_WAIT);

    // Get the active AB data series index.
    int seriesId = series.getId();
    int seriesIndex = seriesId - 1;
    if (seriesId <= 0 || seriesId > MAX_SERIES) {
      seriesIndex = getActiveSeriesIndex();
    }
    seriesId = seriesIndex + 1;
    series.setId(seriesId);

    // Check if a layer already exists for the data series to
    // be added. If so, prompt the user to replace.
    for (IViewLayer layer : getLayers()) {

      if (layer.getName().equals("Series #" + (seriesIndex + 1))) {
        String message = "Replace the current data for series #" + (seriesIndex + 1) + "?";
        boolean confirm = MessageDialog.openConfirm(new Shell(Display.getCurrent()), "Add Data Series", message);
        if (!confirm) {
          setCursorStyle(SWT.CURSOR_ARROW);
          return 0;
        }
        removeLayer(layer);
        break;
      }
    }
    _series[seriesIndex] = series;

    String msg = series.getName() + ": " + numPoints + " points plotted.\n";
    ServiceProvider.getLoggingService().getLogger(getClass()).info(msg);

    // Update the plot bounds if set to automatic.
    IModelSpace modelSpace = getActiveModelSpace();
    IAxis xAxis = modelSpace.getAxisX();
    IAxis yAxis = modelSpace.getAxisY();

    ABDataSeries s = _series[0];
    boolean firstSeries = true;

    for (int ndx = 0; ndx < MAX_SERIES; ndx++) {

      s = _series[ndx];

      if (s != null) {
        double xCenter = 0;
        double yCenter = 0;
        if (!_model.getAnchoredToOrigin()) {
          RegressionDataStatistics dataStats = s.getRegressionDataStatistics();
          xCenter = dataStats.getXBar();
          yCenter = dataStats.getYBar();
        }

        double xminSeries = s.getMinimumA() - xCenter;
        double xmaxSeries = s.getMaximumA() - xCenter;
        double yminSeries = s.getMinimumB() - yCenter;
        double ymaxSeries = s.getMaximumB() - yCenter;
        double zminSeries = s.getMinimumZ();
        double zmaxSeries = s.getMaximumZ();

        xmaxSeries = Math.max(Math.abs(xmaxSeries), Math.abs(xminSeries));
        ymaxSeries = Math.max(Math.abs(ymaxSeries), Math.abs(yminSeries));
        double xymaxSeries = Math.max(xmaxSeries, ymaxSeries);

        double xylog = 0;
        double natlog10 = Math.log(10);

        xylog = 1 + (int) (Math.log(xymaxSeries) / natlog10);
        xymaxSeries = Math.exp(xylog * natlog10);
        xminSeries = xCenter - xymaxSeries;
        xmaxSeries = xCenter + xymaxSeries;
        yminSeries = yCenter - xymaxSeries;
        ymaxSeries = yCenter + xymaxSeries;
        if (firstSeries) {
          xmin = xminSeries;
          xmax = xmaxSeries;
          ymin = yminSeries;
          ymax = ymaxSeries;
          zmin = zminSeries;
          zmax = zmaxSeries;
          firstSeries = false;
        } else {
          xmin = Math.min(xmin, xminSeries);
          xmax = Math.max(xmax, xmaxSeries);
          ymin = Math.min(ymin, yminSeries);
          ymax = Math.max(ymax, ymaxSeries);
          zmin = Math.min(zmin, zminSeries);
          zmax = Math.max(zmax, zmaxSeries);
        }
      }
    }

    int numSeries = 0;
    for (ABDataSeries temp : _series) {
      if (temp != null) {
        numSeries++;
      }
    }
    if (numSeries == 1 && _crossplotBoundsModel.getBoundsType().equals(BoundsType.AUTOMATIC)) {
      if (_model.getAnchoredToOrigin()) {
        xmax = Math.max(Math.abs(xmax), Math.abs(xmin));
        ymax = Math.max(Math.abs(ymax), Math.abs(ymin));
        xymax = Math.max(xmax, ymax);
        //
        //    double xylog = 0;
        //    double natlog10 = Math.log(10);
        //
        //    xylog = 1 + (int) (Math.log(xymax) / natlog10);
        //    xymax = Math.exp(xylog * natlog10);
        //if (xymax != xAxis.getDefaultRange().getEnd()) {
        modelSpace.setDefaultBounds(-xymax, xymax, -xymax, xymax);
        modelSpace.setViewableBounds(-xymax, xymax, -xymax, xymax);
      } else {
        modelSpace.setDefaultBounds(xmin, xmax, ymin, ymax);
        modelSpace.setViewableBounds(xmin, xmax, ymin, ymax);
      }
    }
    //if (xymax != 0) {
    // TODO: updateRegressions();
    //}
    //}

    ILayeredModel layerModel = getLayerModel();

    // Add a group layer for the series.
    IViewLayer seriesGroupLayer = new ViewGroupLayer(series.getName(), series.getName(), false, true);
    seriesGroupLayer.setImage(Activator.getDefault().createImage(ABavoImages.DATA_SERIES));
    layerModel.addLayer(seriesGroupLayer);

    // Add a layer for the series point data.
    PointProperties pointProps = _seriesPointProps[seriesIndex];
    CrossplotSeriesLayer seriesLayer = new CrossplotSeriesLayer(series, pointProps, _depthColorBar);
    addLayer(seriesLayer, false, false);
    IViewLayer seriesPointsLayer = new RendererViewLayer(seriesLayer, series.getName() + " Points", true, false, false);
    layerModel.addLayer(seriesPointsLayer, seriesGroupLayer);

    // Add a group layer for the regression layers.
    IViewLayer regressionsLayer = new ViewGroupLayer(series.getName() + " Regressions", series.getName()
        + " Regressions", false, false);
    regressionsLayer.setImage(Activator.getDefault().createImage(ABavoImages.REGRESSIONS));
    layerModel.addLayer(regressionsLayer, seriesGroupLayer);

    // Add a layer for each of the regressions (Minimum Distance, Least-Squares, Reduced Mean).
    RegressionMethodDescription[] methods = ServiceComponent.getRegressionMethodService().getRegressionMethods();
    List<RegressionLayer> regressionLayers = new ArrayList<RegressionLayer>();
    for (int i = 0; i < methods.length; i++) {
      RegressionStatistics regressionStats = series.getRegression(methods[i]);
      RegressionLayer regressionPlotLayer = new RegressionLayer(methods[i].getName(), methods[i].getAcronym(),
          regressionStats, _seriesLineProps[i]);
      addLayer(regressionPlotLayer, false, false);
      IViewLayer regressionLayer = new RendererViewLayer(regressionPlotLayer, series.getName() + " "
          + methods[i].getName(), false, false, false);
      layerModel.addLayer(regressionLayer, regressionsLayer);
      msg = "Regression " + methods[i].getName() + ": Slope = " + regressionStats.getSlope() + ", Intercept = "
          + regressionStats.getIntercept();
      ServiceProvider.getLoggingService().getLogger(getClass()).info(msg);
    }
    seriesGroupLayer.addAction(new MoveSeriesToFront(seriesLayer, regressionLayers.toArray(new RegressionLayer[0])));

    // TODO: _actionsModel.setRegressionComputed(seriesIndex, true);

    // TODO: updateSeriesProperties(seriesIndex);
    // TODO: updateLegendProperties();

    // Set the default cursor.
    setCursorStyle(SWT.CURSOR_ARROW);

    return status;
  }

  public void editClassColorBar() {
    final Action action = new EditColorBar(_classColorBar);
    getDisplay().asyncExec(new Runnable() {

      public void run() {
        action.run();
      }
    });
  }

  public void editDepthColorBar() {
    final Action action = new EditColorBar(_depthColorBar);
    getDisplay().asyncExec(new Runnable() {

      public void run() {
        action.run();
      }
    });
  }

  public int getActiveSeriesIndex() {
    return _activeSeriesCombo.getSelectionIndex();
  }

  public EllipseRegionsModel getEllipseRegionsModel() {
    return _ellipseRegionsModel;
  }

  public PolygonRegionsModel getPolygonRegionsModel() {
    return _polygonRegionsModel;
  }

  public void ellipseDefined(final EllipseType ellipseType) {
    if (ellipseType.equals(EllipseType.Background) || ellipseType.equals(EllipseType.Maximum)) {
      EllipseLayer layer = getEllipseLayer(ellipseType);
      _ellipseRegionsModel.updateEllipseModel(ellipseType, layer.getActiveEllipseSlope(), layer
          .getActiveEllipseLength(), layer.getActiveEllipseWidth(), layer.getActiveEllipseCenterX(), layer
          .getActiveEllipseCenterY());
    } else if (ellipseType.equals(EllipseType.Selection)) {
      /**
       * TODO: implement the point selection logic.
       */
      CommunicationStatus status = _model.getCommunicationStatus();
      if (!status.equals(CommunicationStatus.COMMUNICATION_ENABLED)) {
        Point3d[] points = new Point3d[0];
        for (ICrossplotSelectionListener listener : _selectionListeners.toArray(new ICrossplotSelectionListener[0])) {
          listener.pointsSelected(points);
        }
        return;
      }
    }
  }

  /**
   * TODO: implement the point selection logic.
   */
  public void selectionPolygonDefined(final IPlotPolygon polygon) {
    CommunicationStatus status = _model.getCommunicationStatus();
    if (!status.equals(CommunicationStatus.COMMUNICATION_ENABLED)) {
      Point3d[] points = new Point3d[0];
      for (ICrossplotSelectionListener listener : _selectionListeners.toArray(new ICrossplotSelectionListener[0])) {
        listener.pointsSelected(points);
      }
      return;
    }

    // Get the active AB data series.
    ABDataSeries series = _series[_activeSeriesCombo.getSelectionIndex()];

    if (series != null) {
      // Initialize an empty list of points to broadcast.
      List<Point3d> pointList = new ArrayList<Point3d>();
      int counter = 0;

      // TODO: For now, only points inside the polygon are broadcast.
      boolean selectPointsInside = true;

      // Loop thru each point to determine its inclusion.
      for (int i = 0; i < series.getNumPoints(); i++) {

        double a = series.getA()[i];
        double b = series.getB()[i];
        Point3d point = series.getPoints()[i];

        // If the point is inside, add it to the list.
        if (PolygonRegionsUtil.isPointInside(polygon, a, b) == selectPointsInside) {
          pointList.add(new Point3d(point));
          counter++;
        }
      }

      if (counter > 0) {
        Point3d[] outputPoints3d = new Point3d[counter];

        for (int i = 0; i < counter; i++) {
          outputPoints3d[i] = pointList.get(i);
        }

        // Send data out on the EventBusManager
        Domain domainType = series.getDomainType();
        CoordinateSystem coordSys = new CoordinateSystem(domainType.toString(), domainType);
        CoordinateSeries outputData3d = CoordinateSeries.createDirect(outputPoints3d, coordSys);
        ServiceProvider.getMessageService().publish(Topic.POINTS3D_SELECTION, outputData3d);
      }

      // Clear the list of points.
      pointList.clear();
    }
  }

  public void setDefaultActions() {
    setMouseActions(_defaultMouseActions.toArray(new IPlotMouseAction[0]), SWT.CURSOR_ARROW);
  }

  @Override
  protected ILayeredModel initializeLayeredModel(final CheckboxTreeViewer treeViewer) {
    // Create a layered model with content and label providers.
    ILayeredModel model = new LayeredModel("AB Model");
    LayeredModelContentProvider contentProvider = new LayeredModelContentProvider(model);
    LayeredModelLabelProvider labelProvider = new LayeredModelLabelProvider();

    // Add the default ABAVO layers to the model.
    addDefaultLayers(model);

    treeViewer.setContentProvider(contentProvider);
    treeViewer.setLabelProvider(labelProvider);
    treeViewer.setInput(model);
    treeViewer.expandAll();
    treeViewer.setAllChecked(true);
    return model;
  }

  /**
   * Add the default ABAVO view layers to the layered model.
   * @param model the layered model.
   */
  private void addDefaultLayers(final ILayeredModel model) {
    // Create the depth color bar.
    RGB[] depthColors = new SpectrumColorMap().getRGBs(64);
    _depthColorBar = new ColorBar(depthColors, Double.NaN, Double.NaN, 10);

    // Create the class color bar.
    RGB[] classColors = new ClassBackgroundColorMap().getRGBs(64);
    _classColorBar = new ColorBar(classColors, -128, 128, 32);
    _classColorBar.setReversedRange(true);

    // Create the ellipse regions model.
    _ellipseRegionsModel = new EllipseRegionsModel();
    _ellipseRegionsModel.addEllipseModelListener(this);

    // Create the polygon regions model.
    _polygonRegionsModel = new PolygonRegionsModel();
    _polygonRegionsModel.setPolygonColors(_classColorBar.getColors());
    _polygonRegionsModel.addPolygonModelListener(this);
    _classColorBar.addColorMapListener(_polygonRegionsModel);

    // Create the default view layer for the ellipse regions model.
    IViewLayer ellipseRegionsLayer = new RendererViewLayer(new EllipseRegionsModelLayer(), "Ellipse Regions Model",
        true, false, false);
    model.addLayer(ellipseRegionsLayer);

    // Create the default view layer for the background (inner) ellipse.
    _backgroundEllipseLayer = new EllipseLayer(EllipseRegionsModel.EllipseType.Background);
    _backgroundEllipseLayer.addPopupMenuAction(new ConstructEllipse(this, EllipseType.Background, true));
    _ellipseRegionsModel.addEllipseModelListener(_backgroundEllipseLayer);
    addLayer(_backgroundEllipseLayer);
    IViewLayer minimumEllipseLayer = new RendererViewLayer(_backgroundEllipseLayer, "Background Ellipse", false, false,
        false);
    model.addLayer(minimumEllipseLayer, ellipseRegionsLayer);

    // Create the default view layer for the maximum (outer) ellipse.
    _maximumEllipseLayer = new EllipseLayer(EllipseRegionsModel.EllipseType.Maximum);
    _maximumEllipseLayer.addPopupMenuAction(new ConstructEllipse(this, EllipseType.Maximum, true));
    _ellipseRegionsModel.addEllipseModelListener(_maximumEllipseLayer);
    addLayer(_maximumEllipseLayer);
    IViewLayer maximumEllipseLayer = new RendererViewLayer(_maximumEllipseLayer, "Maximum Ellipse", false, false, false);
    model.addLayer(maximumEllipseLayer, ellipseRegionsLayer);

    // Create the default view layer for the selection ellipse.
    _selectionEllipseLayer = new EllipseLayer(EllipseRegionsModel.EllipseType.Selection);
    _selectionEllipseLayer.addPopupMenuAction(new ConstructEllipse(this, EllipseType.Selection, true));
    addLayer(_selectionEllipseLayer);
    IViewLayer selectionEllipseLayer = new RendererViewLayer(_selectionEllipseLayer, "Selection Ellipse", false, false,
        false);
    model.addLayer(selectionEllipseLayer, ellipseRegionsLayer);

    // Create the default view layer for the ellipse regions boundaries.
    _regionBoundsLayer = new RegionBoundsLayer(this);
    addLayer(_regionBoundsLayer);
    IViewLayer regionBoundsLayer = new RendererViewLayer(_regionBoundsLayer, "Ellipse Region Bounds", false, false,
        false);
    model.addLayer(regionBoundsLayer, ellipseRegionsLayer);

    // Create the default view layer for the ellipse regions image.
    _regionsImageLayer = new EllipseRegionsImageLayer(this);
    _regionsImageLayer.showReadoutInfo(true);
    _classColorBar.addColorMapListener(_regionsImageLayer);
    addLayer(_regionsImageLayer);
    _ellipseRegionsModel.addEllipseModelListener(_regionsImageLayer);
    IViewLayer regionImageLayer = new RendererViewLayer(_regionsImageLayer, "Ellipse Regions Image", false, false,
        false);
    //rootLayer.addListener(imageLayer);
    model.addLayer(regionImageLayer, ellipseRegionsLayer);

    // Create the default view layer for the polygon regions model.
    PolygonRegionsModelLayer polygonRegionsModelLayer = new PolygonRegionsModelLayer();
    polygonRegionsModelLayer.showReadoutInfo(true);
    addLayer(polygonRegionsModelLayer);
    IViewLayer polygonRegionsLayer = new RendererViewLayer(polygonRegionsModelLayer, "Polygon Regions Model", true,
        false, false);
    model.addLayer(polygonRegionsLayer);

    // Create the default view layers for the region polygons.
    _polygonLayers = new PolygonLayer[PolygonRegionsModel.NUMBER_OF_POLYGONS];
    for (int i = 0; i < PolygonRegionsModel.NUMBER_OF_POLYGONS; i++) {
      _polygonLayers[i] = new PolygonLayer(_polygonRegionsModel.getPolygonModel(i));
      _polygonLayers[i].showReadoutInfo(false);
      addLayer(_polygonLayers[i]);
      _polygonLayers[i].getPolygon().addShapeListener(new PolygonController(this, _polygonRegionsModel, i));
    }
    polygonRegionsModelLayer.setPolygonLayers(_polygonLayers);

    // Create the default view layer for the selection polygon.
    RGB color = Display.getCurrent().getSystemColor(SWT.COLOR_BLUE).getRGB();
    PolygonModel selectionPolygonModel = new PolygonModel(PolygonType.Selection, 0, 0, color);
    _selectionPolygonLayer = new PolygonLayer(selectionPolygonModel);
    _selectionPolygonLayer.addPopupMenuAction(new ConstructSelectionPolygon(this, _selectionPolygonLayer));
    addLayer(_selectionPolygonLayer);
    IViewLayer polygonSelectionViewLayer = new RendererViewLayer(_selectionPolygonLayer, "Selection Polygon", false,
        false, false);
    polygonSelectionViewLayer.setImage(Activator.getDefault().createImage(ABavoImages.SELECTION_POLYGON));
    model.addLayer(polygonSelectionViewLayer, polygonRegionsLayer);
  }

  /**
   * Returns the requested ellipse layer.
   * @param id the id of the ellipse layer to return.
   * @return the requested ellipse layer.
   */
  public EllipseLayer getEllipseLayer(final EllipseType id) {
    if (id.equals(EllipseType.Background)) {
      return _backgroundEllipseLayer;
    } else if (id.equals(EllipseType.Maximum)) {
      return _maximumEllipseLayer;
    } else if (id.equals(EllipseType.Selection)) {
      return _selectionEllipseLayer;
    }
    return null;
  }

  public void ellipseModelUpdated(final EllipseRegionsModelEvent event) {
    if (event.getType().equals(EllipseRegionsModelEvent.Type.EllipsesUpdated)) {
      event.getEllipseRegionsModel().computeEllipseRegions(this);
      event.getEllipseRegionsModel().updated(EllipseRegionsModelEvent.Type.RegionBoundariesUpdated);
      return;
    } else if (event.getType().equals(EllipseRegionsModelEvent.Type.RegionBoundariesUpdated)) {

      RegionsBoundary[] regionBoundaries = EllipseRegionsModel.getRegionBoundaries();
      double[] oxs = new double[EllipseRegionsModel.NUMBER_OF_REGION_BOUNDARIES];
      double[] oys = new double[EllipseRegionsModel.NUMBER_OF_REGION_BOUNDARIES];
      double[] ixs = new double[EllipseRegionsModel.NUMBER_OF_REGION_BOUNDARIES];
      double[] iys = new double[EllipseRegionsModel.NUMBER_OF_REGION_BOUNDARIES];

      double xStart = Double.MAX_VALUE;
      double xEnd = -Double.MAX_VALUE;
      double yStart = Double.MAX_VALUE;
      double yEnd = -Double.MAX_VALUE;
      for (int i = 0; i < regionBoundaries.length; i++) {

        RegionsBoundaryModel boundsModel = event.getEllipseRegionsModel().getRegionsBoundaryModel(regionBoundaries[i]);
        double outerX = boundsModel.getOuterX();
        double outerY = boundsModel.getOuterY();
        double innerX = boundsModel.getInnerX();
        double innerY = boundsModel.getInnerY();
        if (Double.isNaN(outerX) && Double.isNaN(outerY) && Double.isNaN(innerX) && Double.isNaN(innerY)) {
          return;
        }
        oxs[i] = outerX;
        oys[i] = outerY;
        ixs[i] = innerX;
        iys[i] = innerY;
        xStart = Math.min(xStart, outerX);
        xEnd = Math.max(xEnd, outerX);
        yStart = Math.min(yStart, outerY);
        yEnd = Math.max(yEnd, outerY);
      }
      for (int i = 0; i < regionBoundaries.length; i++) {

        IPlotLine regionBound = _regionBoundsLayer.getRegionBound(i);

        if (regionBound != null) {

          regionBound.blockUpdate();

          IPlotPoint outerPoint = new PlotPoint(oxs[i], oys[i], 0);

          outerPoint.setPropertyInheritance(true);

          IPlotPoint innerPoint = new PlotPoint(ixs[i], iys[i], 0);

          innerPoint.setPropertyInheritance(true);

          regionBound.setPoints(outerPoint, innerPoint);

          regionBound.redraw();
          regionBound.unblockUpdate();
        }
      }
      IModelSpace modelSpace = getActiveModelSpace();
      //modelSpace.setViewableBounds(xStart, xEnd, yStart, yEnd);
      //modelSpace.setDefaultBounds(xStart, xEnd, yStart, yEnd);
      //_regionsImageLayer.redraw();
    }
  }

  public void polygonModelUpdated(final PolygonRegionsModelEvent event) {
    PolygonRegionsModelEvent.Type type = event.getType();
    PolygonRegionsModel model = event.getPolygonRegionsModel();
    if (type.equals(PolygonRegionsModelEvent.Type.PolygonsUpdated)) {
      int[] indices = event.getPolygonIndices();
      for (int index : indices) {
        PolygonModel polygonModel = model.getPolygonModel(index);
        PolygonLayer layer = getPolygonLayer(index);
        IPlotPolygon polygon = (IPlotPolygon) layer.getShapes()[0];
        polygon.blockUpdate();
        layer.setColor(polygonModel.getColor());
        int numPoints = polygonModel.getNumPoints();
        polygon.clear();
        for (int i = 0; i < numPoints; i++) {
          Point3d pt = polygonModel.getPoint(i);
          IPlotPoint point = new PlotPoint(pt.getX(), pt.getY(), pt.getZ());
          point.setPropertyInheritance(true);
          polygon.addPoint(point);
        }
        layer.setVisible(polygonModel.getVisible());
        FillStyle fillStyle = FillStyle.NONE;
        if (model.getPolygonsFilled()) {
          fillStyle = FillStyle.SOLID;
        }
        polygon.setFillStyle(fillStyle);
        polygon.setFillColor(layer.getColor());
        polygon.unblockUpdate();
        layer.updated();
      }
    } else if (type.equals(PolygonRegionsModelEvent.Type.RegionSymmetryUpdated)) {
      // TODO:
    } else if (type.equals(PolygonRegionsModelEvent.Type.PolygonCreated)) {
      int[] indices = event.getPolygonIndices();
      if (indices != null && indices.length > 0) {
        for (int index : indices) {
          IPlotPolygon polygon = getPolygonLayer(index).getPolygon();
          polygon.select();
          getModelSpaceCanvas().setActiveShape(polygon);
          Cursor cursor = ABavoCursor.getPencil();
          setMouseActions(PolygonRegionsModel.getPolygonMouseActions(this, index, _polygonRegionsModel
              .getSymmetricRegions()), cursor);
        }
      }
    } else if (type.equals(PolygonRegionsModelEvent.Type.PolygonDeleted)) {
      int[] indices = event.getPolygonIndices();
      if (indices != null && indices.length > 0) {
        for (int index : indices) {
          IPlotPolygon polygon = getPolygonLayer(index).getPolygon();
          polygon.clear();
          polygon.deselect();
          setDefaultActions();
        }
      }
    }
  }

  /**
   * Returns the requested data series.
   * @param seriesIndex the index of the data series to return.
   * @return the requested data series.
   */
  public ABDataSeries getDataSeries(final int seriesIndex) {
    if (seriesIndex < 0 || seriesIndex >= MAX_SERIES) {
      throw new IllegalArgumentException("Series index must be in the range 0-9.");
    }
    return _series[seriesIndex];
  }

  /**
   * Initializes the data series display attributes.
   */
  private void initializeDataSeriesAttributes() {
    PointStyle[] pointStyles = { PointStyle.CIRCLE, PointStyle.SQUARE, PointStyle.TRIANGLE, PointStyle.DIAMOND,
        PointStyle.CROSS, PointStyle.FILLED_CIRCLE, PointStyle.FILLED_SQUARE, PointStyle.FILLED_TRIANGLE,
        PointStyle.FILLED_DIAMOND, PointStyle.X };
    int[] pointRGBs = { SWT.COLOR_BLUE, SWT.COLOR_RED, SWT.COLOR_GREEN, SWT.COLOR_CYAN, SWT.COLOR_MAGENTA };

    _series = new ABDataSeries[MAX_SERIES];
    _seriesPointProps = new PointProperties[MAX_SERIES];
    for (int i = 0; i < MAX_SERIES; i++) {
      Color pointColor = getDisplay().getSystemColor(pointRGBs[i % 5]);
      _seriesPointProps[i] = new PointProperties(pointStyles[i % 10], pointColor.getRGB(), 3);
    }
    Color red = getDisplay().getSystemColor(SWT.COLOR_RED);
    Color green = getDisplay().getSystemColor(SWT.COLOR_GREEN);
    Color blue = getDisplay().getSystemColor(SWT.COLOR_BLUE);
    Color[] lineColors = { red, green, blue };
    _seriesLineProps = new LineProperties[3];
    for (int i = 0; i < 3; i++) {
      _seriesLineProps[i] = new LineProperties(LineStyle.SOLID, lineColors[i].getRGB(), 1);
    }
  }

  /**
   * Initializes the custom tool bar for the ABAVO crossplot.
   */
  private void initializeCustomToolBar(final IWorkbenchPartSite site) {
    // Add a custom tool bar.
    SimpleToolBar toolBar = addCustomToolBar();

    // Add the button for editing the crossplot bounds.
    _crossplotBoundsModel = new CrossplotBoundsModel(BoundsType.AUTOMATIC, 100, -100, 100, -100, 100);
    toolBar.addPushButton(new EditBounds(this, _crossplotBoundsModel));

    // Add the button for toggling the communication on/off.
    toolBar.addToggleButton(new EnableCommunication(this), new DisableCommunication(this));

    // Add the button for bringing up an editor for the ellipse regions model.
    toolBar.addPushButton(new EditEllipseRegionsModel(site.getShell(), _ellipseRegionsModel));

    // Add the button for bringing up an editor for the polygon regions model.
    toolBar.addPushButton(new EditPolygonRegionsModel(site.getShell(), _polygonRegionsModel));

    toolBar.addSeparator();

    // Add the button for bringing up an editor for the depth color bar.
    Action editDepthColorBar = new EditColorBar(_depthColorBar);
    editDepthColorBar.setToolTipText("Edit the depth color bar");
    toolBar.addPushButton(editDepthColorBar);

    // Add the button for bringing up an editor for the class color bar.
    Action editClassColorBar = new EditColorBar(_classColorBar);
    editClassColorBar.setImageDescriptor(Activator.getDefault().createImageDescriptor("icons/ClassColorBar16.png"));
    editClassColorBar.setToolTipText("Edit the class color bar");
    toolBar.addPushButton(editClassColorBar);

    toolBar.addToggleButton(new EnableCrossplotAnchor(this), new DisableCrossplotAnchor(this), true);

    toolBar = addCustomToolBar();
    //toolBar.addSeparator();

    // Add the button for initializing the construction of the background ellipse.
    Action constructBackgroundEllipse = new ConstructEllipse(this, EllipseType.Background, false);
    toolBar.addPushButton(constructBackgroundEllipse);

    // Add the button for initializing the construction of the maximum ellipse.
    Action constructMaximumEllipse = new ConstructEllipse(this, EllipseType.Maximum, false);
    toolBar.addPushButton(constructMaximumEllipse);

    // Add the button for initializing the construction of the selection ellipse.
    Action constructSelectionEllipse = new ConstructEllipse(this, EllipseType.Selection, false);
    toolBar.addPushButton(constructSelectionEllipse);

    toolBar.addSeparator();

    // Add the combo for selecting the active series.
    String[] seriesLabels = new String[MAX_SERIES];
    for (int i = 0; i < MAX_SERIES; i++) {
      seriesLabels[i] = "Series #" + (i + 1) + "  ";
    }
    _activeSeriesCombo = toolBar.addCombo("", seriesLabels);
    _activeSeriesCombo.select(0);
    _activeSeriesCombo.setToolTipText("Select the active series");
    _activeSeriesCombo.setFont(new Font(null, "Courier", 10, SWT.NORMAL));

    // Add the combo for selecting the regression method.
    RegressionMethodDescription[] methods = ServiceComponent.getRegressionMethodService().getRegressionMethods();
    Action[] regressionActions = new Action[methods.length];
    int index = 0;
    for (int i = 0; i < methods.length; i++) {
      regressionActions[i] = new SetRegressionMethod(methods[i], this);
      if (methods[i].equals(_model.getRegressionMethod())) {
        index = i;
      }
    }

    toolBar.addSeparator();

    Combo regressionMethodCombo = toolBar.addCombo(regressionActions);
    regressionMethodCombo.select(index);
    regressionMethodCombo.setToolTipText("Select the regression method for ellipses");
    regressionMethodCombo.setFont(new Font(null, "Courier", 10, SWT.NORMAL));

    toolBar = addCustomToolBar();

    Class[] crossplotAlgorithms = new Class[2];
    crossplotAlgorithms[0] = CrossplotAvsB2d.class;
    crossplotAlgorithms[1] = CrossplotAvsB3d.class;

    Class[] classVolumeAlgorithms = new Class[2];
    classVolumeAlgorithms[0] = GenerateClassVolume2d.class;
    classVolumeAlgorithms[1] = GenerateClassVolume3d.class;

    Class[] aplusbVolumeAlgorithms = new Class[2];
    aplusbVolumeAlgorithms[0] = GenerateAplusBVolume2d.class;
    aplusbVolumeAlgorithms[1] = GenerateAplusBVolume3d.class;

    Action[] crossplotActions = new Action[crossplotAlgorithms.length];
    for (int i = 0; i < crossplotAlgorithms.length; i++) {
      Action crossplotAction = new OpenAlgorithm(crossplotAlgorithms[i], site);
      crossplotAction.setText("Plot A vs B (" + (i + 2) + "D)");
      //crossplotAction.setToolTipText("Open the \'Crossplot A vs B\' algorithm");
      crossplotAction.setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(
          ISharedImages.IMG_FORM_AUTOPILOTS));
      crossplotActions[i] = crossplotAction;
    }
    //toolBar.addPushButton(crossplotAction);
    toolBar.addMenu("Crossplot A vs B", crossplotActions);

    toolBar.addSeparator();

    Action[] classVolumeActions = new Action[classVolumeAlgorithms.length];
    for (int i = 0; i < classVolumeAlgorithms.length; i++) {
      Action classVolumeAction = new OpenAlgorithm(classVolumeAlgorithms[i], site);
      classVolumeAction.setText("Class Volume (" + (i + 2) + "D)");
      //classVolumeAction.setToolTipText("Open the \'Class Volume\' algorithm");
      classVolumeAction.setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(
          ISharedImages.IMG_FORM_AUTOPILOTS));
      classVolumeActions[i] = classVolumeAction;
    }
    //toolBar.addPushButton(classVolumeAction);
    toolBar.addMenu("Class Volume", classVolumeActions);

    toolBar.addSeparator();

    Action[] aplusbVolumeActions = new Action[aplusbVolumeAlgorithms.length];
    for (int i = 0; i < aplusbVolumeAlgorithms.length; i++) {
      Action aplusbVolumeAction = new OpenAlgorithm(aplusbVolumeAlgorithms[i], site);
      aplusbVolumeAction.setText("A + B (" + (i + 2) + "D)");
      //aplusbVolumeAction.setToolTipText("Open the \'A+B Volume\' algorithm");
      aplusbVolumeAction.setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(
          ISharedImages.IMG_FORM_AUTOPILOTS));
      aplusbVolumeActions[i] = aplusbVolumeAction;
    }
    //toolBar.addPushButton(aplusbVolumeAction);
    toolBar.addMenu("A+B Volume", aplusbVolumeActions);
  }

  /**
   * Initializes the default mouse actions for the crossplot.
   */
  private void initializeDefaultMouseActions() {
    // Create an empty list of plot mouse actions. */
    _defaultMouseActions = new ArrayList<IPlotMouseAction>();

    PlotActionMask mask;

    // PointGroup deselect: button#2, single-click
    mask = new PlotActionMask(ActionMaskType.MOUSE_DOWN, 2, 1, 0);
    _defaultMouseActions.add(new DeselectShapeAction(mask));

    // PointGroup select: button#2, single-click
    mask = new PlotActionMask(ActionMaskType.MOUSE_DOWN, 1, 1, SWT.SHIFT);
    _defaultMouseActions.add(new SelectShapeAction(mask, true));

    // PointGroup start move: button#1, single-click, shift
    mask = new PlotActionMask(ActionMaskType.MOUSE_DOWN, 1, 1, SWT.SHIFT);
    _defaultMouseActions.add(new StartShapeMotionAction(mask));

    // PointGroup move: button#1, drag, shift
    mask = new PlotActionMask(ActionMaskType.MOUSE_MOVE, 0, 0, SWT.BUTTON1 | SWT.SHIFT);
    _defaultMouseActions.add(new ShapeMotionAction(mask));

    // PointGroup end move: button#1, shift
    mask = new PlotActionMask(ActionMaskType.MOUSE_UP, 1, 1, SWT.BUTTON1 | SWT.SHIFT);
    _defaultMouseActions.add(new EndShapeMotionAction(mask));

    // Point start move: button#1, single-click, shift
    mask = new PlotActionMask(ActionMaskType.MOUSE_DOWN, 1, 1, SWT.CONTROL);
    _defaultMouseActions.add(new StartPointMotionAction(mask));

    // Point move: button#1, drag, shift
    mask = new PlotActionMask(ActionMaskType.MOUSE_MOVE, 0, 0, SWT.BUTTON1 | SWT.CONTROL);
    _defaultMouseActions.add(new PointMotionAction(mask));

    // Point end move: button#1, shift
    mask = new PlotActionMask(ActionMaskType.MOUSE_UP, 1, 1, SWT.BUTTON1 | SWT.CONTROL);
    _defaultMouseActions.add(new EndPointMotionAction(mask));

    // Set the default actions.
    setDefaultActions();
  }

  @Override
  public void dispose() {
    // Dispose of the ellipse regions model.
    _ellipseRegionsModel.dispose();
    // Dispose of the polygon regions model.
    _polygonRegionsModel.dispose();
    // Dispose of the depth color bar.
    _depthColorBar.dispose();
    // Dispose of the class color bar.
    _classColorBar.dispose();
    // Dispose of the selection listener list.
    _selectionListeners.clear();
    // Dispose of the preferences listener.
    _preferenceStore.removePropertyChangeListener(_preferenceStoreListener);
    _preferenceStore2.removePropertyChangeListener(_preferenceStoreListener2);
    super.dispose();
  }

  public ABavoCrossplotModel getModel() {
    return _model;
  }

  public void propertyChanged(String key) {
    if (key.equals(ABavoCrossplotModel.REGRESSION_METHOD)) {
      RegressionMethodDescription method = _model.getRegressionMethod();
      int seriesIndex = getActiveSeriesIndex();
      if (_series[seriesIndex] != null) {
        RegressionStatistics regressionStats = _series[seriesIndex].getRegression(method);
        _ellipseRegionsModel.updateEllipses(regressionStats);
      }
    }
  }

  public void addSelectionListener(final ICrossplotSelectionListener listener) {
    if (!_selectionListeners.contains(listener)) {
      _selectionListeners.add(listener);
    }
  }

  public void removeSelectionListener(final ICrossplotSelectionListener listener) {
    _selectionListeners.remove(listener);
  }

  public PolygonLayer getPolygonLayer(final int index) {
    if (index >= 0 && index < PolygonRegionsModel.NUMBER_OF_POLYGONS) {
      return _polygonLayers[index];
    }
    return _selectionPolygonLayer;
  }

  public Point3d[] computeSymmetricPoints(Point3d[] points) {
    Point3d[] symmetryPoints = new Point3d[points.length];
    int seriesIndex = getActiveSeriesIndex();
    ABDataSeries series = getDataSeries(seriesIndex);
    double xCenter = 0;
    double yCenter = 0;
    if (series != null) {
      RegressionDataStatistics stats = series.getRegressionDataStatistics();
      xCenter = stats.getXBar();
      yCenter = stats.getYBar();
    }
    for (int i = 0; i < symmetryPoints.length; i++) {
      Point3d point = points[i];
      double x = point.getX();
      double y = point.getY();
      double dx = x - xCenter;
      double dy = y - yCenter;
      symmetryPoints[i] = new Point3d(xCenter - dx, yCenter - dy, point.getZ());
    }
    return symmetryPoints;
  }

  public void regionPolygonDefined(final int polygonIndex, boolean symmetry) {
    // TODO: implement this.
    System.out.println("Region polygon for index=" + polygonIndex + " defined...");
    if (symmetry) {
      PolygonModel model = _polygonRegionsModel.getPolygonModel(polygonIndex);
      int symmetryIndex = PolygonRegionsModel.NUMBER_OF_POLYGONS - 1 - polygonIndex;
      PolygonModel symmetryModel = _polygonRegionsModel.getPolygonModel(symmetryIndex);
      Point3d[] sourcePoints = model.getPoints();
      Point3d[] symmetryPoints = computeSymmetricPoints(sourcePoints);
      symmetryModel.setPoints(symmetryPoints);
      symmetryModel.setExists(true);
      symmetryModel.setVisible(true);
      EndPolygonDefinition action = new EndPolygonDefinition(this, PolygonType.Region, symmetryIndex, false);
      action.cleanup();
    }
  }

  @Override
  public synchronized void addObjects(final Object[] objects) {
    if (objects == null) {
      return;
    }
    for (Object object : objects) {
      if (object.getClass().equals(ABDataSeries.class)) {
        addSeriesData((ABDataSeries) object);
      } else {
        ServiceProvider.getLoggingService().getLogger(getClass()).error(
            "AB crossplot cannot display object of class " + object.getClass().getSimpleName());
      }
    }
  }

  @Override
  public void cursorUpdated(final double x, final double y, final boolean broadcast) {

    // if the panel is not visible we don't need to update it. 
    if (!_readoutPanel.isVisible()) {
      return;
    }

    // Create the main readout section info 
    ReadoutInfo mainInfo = new ReadoutInfo("Mouse location", new String[] { "x", "y" }, new String[] { x + "", y + "" });
    _readoutPanel.update(mainInfo);

    IPlotLayer[] layers = getPlot().getActiveModelSpace().getLayers();
    for (IPlotLayer layer : layers) {
      if (layer.showReadoutInfo()) {
        ReadoutInfo info = layer.getReadoutInfo(x, y);
        _readoutPanel.update(info);
      }
    }

    _readoutPanel.updateForm();

    super.cursorUpdated(x, y, false);
  }

  public void applyBounds(final CrossplotBoundsModel model) {
    _crossplotBoundsModel.updateFrom(model);
    BoundsType boundsType = model.getBoundsType();
    if (boundsType.equals(BoundsType.USER_DEFINED)) {
      double xStart = model.getStartA();
      double xEnd = model.getEndA();
      double yStart = model.getStartB();
      double yEnd = model.getEndB();
      getActiveModelSpace().setDefaultBounds(xStart, xEnd, yStart, yEnd);
      getActiveModelSpace().setViewableBounds(xStart, xEnd, yStart, yEnd);
    } else if (boundsType.equals(BoundsType.COMMON_MIN_MAX)) {
      double common = model.getCommonMinMax();
      getActiveModelSpace().setDefaultBounds(-common, common, -common, common);
      getActiveModelSpace().setViewableBounds(-common, common, -common, common);
    } else {
      // No action.
    }
  }

  public IRenderer[] getRenderers() {
    // TODO Revisit this!
    return new IRenderer[0];
  }

  @Override
  public Model getViewerModel() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateFromModel() {
    // TODO Auto-generated method stub

  }

  public void addRenderer(String klass, Map<String, String> props, String uniqueId) {
    // TODO Auto-generated method stub

  }

  public final ILayeredModel getLayeredModel() {
    return _layerModel;
  }
}
