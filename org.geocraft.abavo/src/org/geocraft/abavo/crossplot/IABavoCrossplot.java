/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot;


import org.eclipse.swt.widgets.Composite;
import org.geocraft.abavo.crossplot.layer.EllipseLayer;
import org.geocraft.abavo.crossplot.layer.PolygonLayer;
import org.geocraft.abavo.ellipse.EllipseRegionsModel;
import org.geocraft.abavo.ellipse.EllipseRegionsModelListener;
import org.geocraft.abavo.ellipse.EllipseRegionsModel.EllipseType;
import org.geocraft.abavo.polygon.PolygonRegionsModel;
import org.geocraft.abavo.polygon.PolygonRegionsModelListener;
import org.geocraft.ui.plot.IPlotViewer;
import org.geocraft.ui.plot.object.IPlotPolygon;


/**
 * Defines the interface for an A vs B crossplot.
 */
public interface IABavoCrossplot extends IPlotViewer, EllipseRegionsModelListener, PolygonRegionsModelListener {

  /** Enumeration for the point coloring mode: By Series or By Z-value. */
  public static enum ColorMode {
    BySeries,
    ByZ
  }

  /** Enumeration for the point coloring-by-z mode: Automatic or User-Defined. */
  public static enum ColorByZMode {
    Automatic,
    UserDefined
  }

  /** Enumeration for the plot mode. */
  public static enum PlotMode {
    General,
    Zoom,
    PolygonSelection,
    PolygonClass,
    EllipseSelection,
    EllipseMin,
    EllipseMax
  }

  /** Enumeration for ellipse type. */
  public static enum EllipseComputation {
    Mathematical,
    Visual
  }

  /** The string constant for the regression methods. */
  String[] REGRESSION_METHOD_STRINGS = { "Min. Distance (PPD)", "Least-Squares (LSQ)", "Reduced Mean (RMA)" };

  /** The constant for the number of histogram cells. */
  int NUMBER_OF_HISTOGRAM_CELLS = 20;

  /** The maximum number of data series in the crossplot. */
  int MAX_SERIES = 10;

  String getTitle();

  void setTitle(String title);

  /**
   * Gets the AB crossplot composite.
   * @return the AB crossplot composite.
   */
  Composite getComposite();

  /**
   * Sets the default plot actions (mouse actions, key actions, etc).
   */
  void setDefaultActions();

  /**
   * Gets the ellipse regions model.
   * @return the ellipse regions model, or null if none exists.
   */
  EllipseRegionsModel getEllipseRegionsModel();

  /**
   * Gets the polygon regions model.
   * @return the polygon regions model, or null if none exists.
   */
  PolygonRegionsModel getPolygonRegionsModel();

  /**
   * Popup the class background colorbar editor.
   */
  void editClassColorBar();

  /**
   * Popup the depth colorbar editor.
   */
  void editDepthColorBar();

  /**
   * Gets the active AB data series.
   * @return the active AB data series.
   */
  //TODO: ABDataSeries getActiveSeries();
  /**
   * Gets the index of the active AB data series.
   * @return the index of the active AB data series.
   */
  int getActiveSeriesIndex();

  /**
   * Gets the specified AB data series.
   * @param index the index of the data series to get.
   * @return the specified AB data series.
   */
  //TODO: ABDataSeries getSeries(int index);
  /**
   * Gets the specified ellipse layer.
   * @param ellipseType the id of the ellipse layer to get (Background, Maximum or Selection).
   * @return the specified ellipse layer.
   */
  //TODO: EllipseLayer getEllipseLayer(EllipseRegionsModel.EllipseId ellipseType);
  /**
   * Gets the polygon regions layer.
   * @return the polygon regions layer.
   */
  //TODO: PolygonRegionsLayer getPolygonRegionsLayer();
  /**
   * Gets the polygon selection layer.
   * @return the polygon selection layer.
   */
  //TODO: PolygonLayer getPolygonSelectionLayer();
  /**
   * Invoked when the background or maximum ellipse is defined.
   * @param ellipseType the ellipse id (Background or Maximum).
   */
  void ellipseDefined(EllipseType ellipseType);

  /**
   * Invoked when a region polygon or selection polygon is defined.
   * @param index the polygon index.
   */
  void regionPolygonDefined(int index, boolean symmetry);

  /**
   * Invoked when the selection polygon is defined.
   * The polygon currently defined in the polygon selection layer
   * will be used to broadcast selected points to subscribers on
   * the event bus manager.
   */
  void selectionPolygonDefined(IPlotPolygon polygon);

  /**
   * Returns the plot layer containing the specified ellipse.
   * @param id the id of the ellipse layer to get.
   * @return the plot layer containing the specified ellipse.
   */
  EllipseLayer getEllipseLayer(EllipseType id);

  /**
   * Returns the plot layer containing the specified polygon.
   * @param index the index of the polygon layer to get.
   * @return the plot layer containing the specified polygon.
   */
  PolygonLayer getPolygonLayer(int index);

  /**
   * Returns the requested data series.
   * @param seriesIndex the index of the data series.
   * @return the requested data series.
   */
  ABDataSeries getDataSeries(int seriesIndex);

  /**
   * Disposes of the crossplot resources.
   */
  void dispose();

  /**
   * Returns the crossplot model.
   * @return the crossplot model.
   */
  ABavoCrossplotModel getModel();

  /**
   * Adds a selection listener to the crossplot. The listener will be notified
   * of selection events if the crossplot is set to broadcast.
   * @param listener the listener to add.
   */
  void addSelectionListener(ICrossplotSelectionListener listener);

  /**
   * Removes a selection listener from the crossplot.
   * @param listener the listener to remove.
   */
  void removeSelectionListener(ICrossplotSelectionListener listener);

  void showCursorToolTip(boolean showToolTip);

}
