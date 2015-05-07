/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.ellipse;


import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.geocraft.abavo.crossplot.ABavoCrossplot;
import org.geocraft.abavo.crossplot.CrossplotBoundsModel;
import org.geocraft.abavo.crossplot.IABavoCrossplot;
import org.geocraft.abavo.crossplot.CrossplotBoundsModel.BoundsType;
import org.geocraft.abavo.crossplot.layer.EllipseLayer;
import org.geocraft.abavo.ellipse.EllipseRegionsModelEvent.Type;
import org.geocraft.core.common.xml.XmlIO;
import org.geocraft.core.common.xml.XmlUtils;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.internal.abavo.ABavoCrossplotRegistry;
import org.geocraft.math.regression.RegressionStatistics;
import org.geocraft.ui.plot.model.ModelSpaceBounds;
import org.geocraft.ui.plot.object.IPlotPolygon;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Defines the ellipse regions model for the AB crossplot.
 * The ellipse regions model include 3 ellipses (a background ellipse, a maximum ellipse and selection ellipse),
 * 8 regions and 10 region boundaries.
 */
public class EllipseRegionsModel extends Model implements XmlIO {

  private static final String GENERAL_TAG = "General";

  private static final String REGION_BOUNDARIES_TAG = "RegionBoundaries";

  private static final String ELLIPSES_TAG = "Ellipses";

  private static final String CROSSPLOT_ELLIPSE_MODEL_TAG = "CrossplotEllipseModel";

  /** Enumeration for the various ellipses: Minimum (or Background), Maximum and Selection. */
  public static enum EllipseType {
    Selection("Selection"),
    Background("Background"),
    Maximum("Maximum");

    private String _name;

    EllipseType(final String name) {
      _name = name;
    }

    public String getName() {
      return _name;
    }
  }

  /** Enumeration for the ellipse regions. */
  public static enum EllipseRegion {
    P1,
    P2,
    P3,
    P4,
    N1,
    N2,
    N3,
    N4
  }

  public static final String SYMMETRIC_REGIONS = "symmetricRegions";

  public static final String ELLIPSE_REGIONS_MODEL_PREFIX = "EllipseRegionsModel";

  public static final String SLOPE = "slope";

  public static final String LENGTH = "length";

  public static final String WIDTH = "width";

  public static final String CENTER_X = "centerX";

  public static final String CENTER_Y = "centerY";

  public static final String OUTER_X = "outerX";

  public static final String OUTER_Y = "outerY";

  public static final String INNER_X = "innerX";

  public static final String INNER_Y = "innerY";

  /** The constant for the number of ellipses. */
  public static final int NUMBER_OF_ELLIPSES = 2;

  /** The constant for the number of regions. */
  public static final int NUMBER_OF_REGIONS = 8;

  /** The constant for the number of region bounds. */
  public static final int NUMBER_OF_REGION_BOUNDARIES = 10;

  /** The constant for the number of ellipse points. */
  public static final int NUMBER_OF_ELLIPSE_POINTS = 60;

  public static final String FILE_EXTENSION = "ellipse_model";

  /**
   * Gets the array of region boundary definitions.
   * @return the array of region boundary definitions.
   */
  public static RegionsBoundary[] getRegionBoundaries() {
    RegionsBoundary[] regionBounds = { RegionsBoundary.P1toP2, RegionsBoundary.P2toP3, RegionsBoundary.P3toP4,
        RegionsBoundary.P4toNULL, RegionsBoundary.NULLtoN1, RegionsBoundary.N1toN2, RegionsBoundary.N2toN3,
        RegionsBoundary.N3toN4, RegionsBoundary.N4toNULL, RegionsBoundary.NULLtoP1 };
    return regionBounds;
  }

  private BooleanProperty _symmetricRegions;

  private final EllipseModel[] _ellipseModels;

  private final RegionsBoundaryModel[] _regionsBoundaryModels;

  private final List<EllipseRegionsModelListener> _listeners;

  private boolean _updateBlocked;

  private final Object _lock = new Object();

  public static final String[] ELLIPSE_LABELS = { "Background", "Maximum" };

  public static final String[] REGION_LINE_LABELS = { "Class I (Top) and Class II (Top)",
      "Class II (Top) and Class III (Top)", "Class III (Top) and Class IV (Top)", "Class IV (Top) and NULL",
      "NULL and Class I (Base)", "Class I (Base) and Class II (Base)", "Class II (Base) and Class III (Base)",
      "Class III (Base) and Class IV (Base)", "Class IV (Base) and NULL", "NULL and Class I (Top)" };

  /**
   * Constructs an ellipse regions model and associates it with the specified AB crossplot.
   * @param crossplot the AB crossplot to associates with the ellipse regions model.
   */
  public EllipseRegionsModel() {

    _updateBlocked = false;
    _listeners = Collections.synchronizedList(new ArrayList<EllipseRegionsModelListener>());
    _symmetricRegions = addBooleanProperty(SYMMETRIC_REGIONS, true);
    _ellipseModels = new EllipseModel[2];
    _ellipseModels[0] = new EllipseModel(EllipseType.Background, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
        Double.NaN);
    _ellipseModels[1] = new EllipseModel(EllipseType.Maximum, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
        Double.NaN);
    RegionsBoundary[] ids = RegionsBoundary.values();
    _regionsBoundaryModels = new RegionsBoundaryModel[NUMBER_OF_REGION_BOUNDARIES];
    for (int i = 0; i < NUMBER_OF_REGION_BOUNDARIES; i++) {
      _regionsBoundaryModels[i] = new RegionsBoundaryModel(ids[i], Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }
  }

  /**
   * The copy constructor.
   */
  public EllipseRegionsModel(final EllipseRegionsModel model) {
    this();
    updateModel(model);
  }

  /**
   * @param model
   */
  public void updateModel(final EllipseRegionsModel model) {
    setSymmetricRegions(model.getSymmetricRegions());
    for (EllipseModel ellipseModel : _ellipseModels) {
      EllipseType type = ellipseModel.getType();
      EllipseModel ellipseModelIn = model.getEllipseModel(type);
      ellipseModel.setSlope(ellipseModelIn.getSlope());
      ellipseModel.setLength(ellipseModelIn.getLength());
      ellipseModel.setWidth(ellipseModelIn.getWidth());
      ellipseModel.setCenterX(ellipseModelIn.getCenterX());
      ellipseModel.setCenterY(ellipseModelIn.getCenterY());
    }
    for (RegionsBoundaryModel boundaryModel : _regionsBoundaryModels) {
      RegionsBoundary id = boundaryModel.getId();
      RegionsBoundaryModel boundaryModelIn = model.getRegionsBoundaryModel(id);
      boundaryModel.setInnerX(boundaryModelIn.getInnerX());
      boundaryModel.setInnerY(boundaryModelIn.getInnerY());
      boundaryModel.setOuterX(boundaryModelIn.getOuterX());
      boundaryModel.setOuterY(boundaryModelIn.getOuterY());
    }
  }

  /**
   * Returns true if the model is blocked from updates; false if not.
   * @return true if the model is blocked from updates; false if not.
   */
  public boolean isUpdateBlocked() {
    return _updateBlocked;
  }

  /**
   * Blocks the model from updates.
   */
  public void blockUpdate() {
    _updateBlocked = true;
  }

  /**
   * Unblocks the model from updates.
   */
  public void unblockUpdate() {
    _updateBlocked = false;
  }

  /**
   * Gets the ellipse region symmetry lock flag.
   * @return true if ellipse region symmetry is to be enforced; otherwise false.
   */
  public boolean getSymmetricRegions() {
    return _symmetricRegions.get();
  }

  /**
   * Sets the ellipse region symmetry lock flag.
   * @param flag true if ellipse region symmetry is to be enforced; otherwise false.
   */
  public void setSymmetricRegions(final boolean symmetricRegions) {
    _symmetricRegions.set(symmetricRegions);
    /////firePropertyChange(SYMMETRIC_REGIONS, _symmetricRegions, _symmetricRegions = symmetricRegions);
    updatedRegionSymmetry();
  }

  /**
   * Gets the specified ellipse model.
   * @param ellipseType the id of the ellipse model to get.
   * @return the ellipse model specified by id.
   */
  public EllipseModel getEllipseModel(final EllipseType ellipseType) {
    if (ellipseType.equals(EllipseType.Background)) {
      return _ellipseModels[0];
    } else if (ellipseType.equals(EllipseType.Maximum)) {
      return _ellipseModels[1];
    }
    throw new IllegalArgumentException("Invalid ellipse: " + ellipseType);
  }

  /**
   * Updates the specified ellipse model.
   * @param ellipseType the id of the ellipse model to update.
   * @param m the slope of the ellipse.
   * @param a the length (major axis) of the ellipse.
   * @param b the width(minor axis) of the ellipse.
   */
  public void updateEllipseModel(final EllipseType ellipseType, final double m, final double a, final double b,
      final double x, final double y) {
    EllipseModel ellipseModel = null;
    if (ellipseType.equals(EllipseType.Background)) {
      ellipseModel = _ellipseModels[0];
    } else if (ellipseType.equals(EllipseType.Maximum)) {
      ellipseModel = _ellipseModels[1];
    } else {
      throw new IllegalArgumentException("Invalid ellipse type: " + ellipseType);
    }
    if (ellipseModel != null) {
      ellipseModel.setSlope(m);
      ellipseModel.setLength(a);
      ellipseModel.setWidth(b);
      ellipseModel.setCenterX(x);
      ellipseModel.setCenterY(y);
    }
    updatedEllipses();
  }

  /**
   * Gets the specified regions boundary model.
   * @param id the id of the regions boundary model to get.
   * @return the regions boundary model specified by id.
   */
  public RegionsBoundaryModel getRegionsBoundaryModel(final RegionsBoundary id) {
    int index = id.ordinal();
    return _regionsBoundaryModels[index];
  }

  /**
   * Updates the specified regions boundary model.
   * @param id the id of the regions boundary model to update.
   * @param outerX the outer x-coordinate of the region bound.
   * @param outerY the outer y-coordinate of the region bound.
   * @param innerX the inner x-coordinate of the region bound.
   * @param innerY the inner y-coordinate of the region bound.
   */
  public void updateRegionsBoundaryModel(final RegionsBoundary id, final double outerX, final double outerY,
      final double innerX, final double innerY) {
    int index = id.ordinal();
    _regionsBoundaryModels[index].setOuterX(outerX);
    _regionsBoundaryModels[index].setOuterY(outerY);
    _regionsBoundaryModels[index].setInnerX(innerX);
    _regionsBoundaryModels[index].setInnerY(innerY);
    updatedRegionBoundaries();
  }

  /**
   * Gets the outer x-coordinate of the specified region bound.
   * @param id the region bound.
   * @return the outer x-coordinate.
   */
  public double getRegionBoundariesOuterX(final RegionsBoundary id) {

    int index = id.ordinal();

    return _regionsBoundaryModels[index].getOuterX();
  }

  /**
   * Gets the outer y-coordinate of the specified region bound.
   * @param id the region bound.
   * @return the outer y-coordinate.
   */
  public double getRegionBoundariesOuterY(final RegionsBoundary id) {

    int index = id.ordinal();

    return _regionsBoundaryModels[index].getOuterY();
  }

  /**
   * Gets the inner x-coordinate of the specified region bound.
   * @param id the region bound.
   * @return the inner x-coordinate.
   */
  public double getRegionBoundariesInnerX(final RegionsBoundary id) {

    int index = id.ordinal();

    return _regionsBoundaryModels[index].getInnerX();
  }

  /**
   * Gets the inner y-coordinate of the specified region bound.
   * @param id the region bound.
   * @return the inner y-coordinate.
   */
  public double getRegionBoundariesInnerY(final RegionsBoundary id) {

    int index = id.ordinal();

    return _regionsBoundaryModels[index].getInnerY();
  }

  /**
   * Gets the outer x-coordinate of the specified region bounds.
   * @param ids the region boundss.
   * @return the outer x-coordinates.
   */
  public double[] getRegionBoundariesOuterXs(final RegionsBoundary[] ids) {
    double[] outerXs = new double[ids.length];
    for (int i = 0; i < ids.length; i++) {
      int index = ids[i].ordinal();
      outerXs[i] = _regionsBoundaryModels[index].getOuterX();
    }
    return outerXs;
  }

  /**
   * Gets the outer y-coordinate of the specified region bounds.
   * @param ids the region boundss.
   * @return the outer y-coordinates.
   */
  public double[] getRegionBoundariesOuterYs(final RegionsBoundary[] ids) {
    double[] outerYs = new double[ids.length];
    for (int i = 0; i < ids.length; i++) {
      int index = ids[i].ordinal();
      outerYs[i] = _regionsBoundaryModels[index].getOuterY();
    }
    return outerYs;
  }

  /**
   * Gets the inner x-coordinate of the specified region bounds.
   * @param ids the region boundss.
   * @return the inner x-coordinates.
   */
  public double[] getRegionBoundariesInnerXs(final RegionsBoundary[] ids) {
    double[] innerXs = new double[ids.length];
    for (int i = 0; i < ids.length; i++) {
      int index = ids[i].ordinal();
      innerXs[i] = _regionsBoundaryModels[index].getInnerX();
    }
    return innerXs;
  }

  /**
   * Gets the inner y-coordinate of the specified region bounds.
   * @param ids the region boundss.
   * @return the inner y-coordinates.
   */
  public double[] getRegionBoundariesInnerYs(final RegionsBoundary[] ids) {
    double[] innerYs = new double[ids.length];
    for (int i = 0; i < ids.length; i++) {
      int index = ids[i].ordinal();
      innerYs[i] = _regionsBoundaryModels[index].getInnerY();
    }
    return innerYs;
  }

  /**
   * Add an ellipse model listener.
   * @param listener the listener to add.
   */
  public void addEllipseModelListener(final EllipseRegionsModelListener listener) {
    if (!_listeners.contains(listener)) {
      _listeners.add(listener);
    }
  }

  /**
   * Removes an ellipse model listener.
   * @param listener the listener to remove.
   */
  public void removeEllipseModelListener(final EllipseRegionsModelListener listener) {
    _listeners.remove(listener);
  }

  /**
   * Fires an ellipse regions model event to the listeners.
   */
  private void fireEllipseModelEvent(final EllipseRegionsModelEvent event) {
    for (EllipseRegionsModelListener listener : _listeners.toArray(new EllipseRegionsModelListener[0])) {
      listener.ellipseModelUpdated(event);
    }
  }

  /**
   * Invoked when one of the ellipses is updated.
   */
  private void updatedEllipses() {
    updated(EllipseRegionsModelEvent.Type.EllipsesUpdated);
  }

  /**
   * Invoked when one of the regions boundaries is updated.
   */
  private void updatedRegionBoundaries() {
    updated(EllipseRegionsModelEvent.Type.RegionBoundariesUpdated);
  }

  /**
   * Invoked when the region symmetry is turned on/off.
   */
  private void updatedRegionSymmetry() {
    updated(EllipseRegionsModelEvent.Type.RegionSymmetryUpdated);
  }

  public void updated(final EllipseRegionsModelEvent.Type type) {
    if (!isUpdateBlocked()) {
      fireEllipseModelEvent(new EllipseRegionsModelEvent(type, this));
    }
  }

  /**
   * Computes the ellipse-based regions based on the background and maximum ellipses.
   */
  public void computeEllipseRegions(final IABavoCrossplot crossplot) {
    synchronized (_lock) {
      blockUpdate();

      ModelSpaceBounds bounds = crossplot.getActiveModelSpace().getDefaultBounds();
      double xmin = bounds.getStartX();
      double xmax = bounds.getEndX();
      double ymin = bounds.getStartY();
      double ymax = bounds.getEndY();
      double vpth = 0.1 * (xmax - xmin) / 2;
      double i3bx;
      double n4bx;
      double i3by;
      double n4by;

      EllipseModel bkgEllipseModel = getEllipseModel(EllipseType.Background);
      EllipseModel maxEllipseModel = getEllipseModel(EllipseType.Maximum);
      double minEllipseSlope = bkgEllipseModel.getSlope();
      double maxEllipseSlope = maxEllipseModel.getSlope();
      double minEllipseWidth = bkgEllipseModel.getWidth();
      double xCenter = bkgEllipseModel.getCenterX();
      double yCenter = bkgEllipseModel.getCenterY();

      IPlotPolygon tempEllipse = EllipseLayer.createEllipsePolygon(bkgEllipseModel);

      if (Double.isNaN(minEllipseSlope) || minEllipseSlope != maxEllipseSlope) {
        unblockUpdate();
        return;
      }

      double slope = minEllipseSlope;

      //Point3d point = EllipseUtil.intersection(false, dzero, vpth, tempEllipse, vpth, ymin);
      Point2D point = EllipseUtil.intersection(xCenter + vpth, ymin, 0, 1, bkgEllipseModel);
      double i0ix = point.getX();
      double i0iy = point.getY();

      //point = EllipseUtil.intersection(false, dzero, -vpth, tempEllipse, -vpth, ymin);
      point = EllipseUtil.intersection(xCenter - vpth, ymin, 0, 1, bkgEllipseModel);
      double i1ix = point.getX();
      double i1iy = point.getY();

      //point = EllipseUtil.intersection(true, dzero, dzero, tempEllipse, xmin, 0);
      point = EllipseUtil.intersection(xmin, yCenter, 1, 0, bkgEllipseModel);
      double i2ix = point.getX();
      double i2iy = point.getY();

      double ytmp = Math.sqrt(1 + slope * slope) * minEllipseWidth / 2;
      if (slope > 0) {
        if ((xmin - xCenter) * slope - ytmp < ymin - yCenter) {
          i3bx = xCenter + (ymin - yCenter + ytmp) / slope;
          i3by = ymin;
        } else {
          i3bx = xmin;
          i3by = yCenter + (xmin - xCenter) * slope - ytmp;
        }
        if (xmin * slope + ytmp < ymin) {
          n4bx = xCenter + (ymin - yCenter - ytmp) / slope;
          n4by = ymin;
        } else {
          n4bx = xmin;
          n4by = yCenter + (xmin - xCenter) * slope + ytmp;
        }
      } else {
        if ((xmin - xCenter) * slope - ytmp > ymax - yCenter) {
          i3bx = xCenter + (ymax - yCenter + ytmp) / slope;
          i3by = ymax;
        } else {
          i3bx = xmin;
          i3by = yCenter + (xmin - xCenter) * slope - ytmp;
        }
        if ((xmin - xCenter) * slope + ytmp > ymax - yCenter) {
          n4bx = xCenter + (ymax - yCenter - ytmp) / slope;
          n4by = ymax;
        } else {
          n4bx = xmin;
          n4by = yCenter + (xmin - xCenter) * slope + ytmp;
        }
      }
      //point = EllipseUtil.intersection(true, slope, -ytmp, tempEllipse, i3bx, i3by);
      point = EllipseUtil.intersection(i3bx, i3by, 1, slope, bkgEllipseModel);
      double i3ix = point.getX();
      double i3iy = point.getY();

      //point = EllipseUtil.intersection(true, slope, ytmp, tempEllipse, n4bx, n4by);
      point = EllipseUtil.intersection(n4bx, n4by, 1, slope, bkgEllipseModel);
      double n4ix = point.getX();
      double n4iy = point.getY();
      double i0bx = i0ix;
      double i0by = ymin;
      double i1bx = i1ix;
      double i1by = ymin;
      double i2bx = xmin;
      double i2by = yCenter;
      double i4bx = 2 * xCenter - i0bx;
      double i4by = 2 * yCenter - i0by;
      double i4ix = 2 * xCenter - i0ix;
      double i4iy = 2 * yCenter - i0iy;
      double i5bx = 2 * xCenter - i1bx;
      double i5by = 2 * yCenter - i1by;
      double i5ix = 2 * xCenter - i1ix;
      double i5iy = 2 * yCenter - i1iy;
      double i6bx = 2 * xCenter - i2bx;
      double i6by = 2 * yCenter - i2by;
      double i6ix = 2 * xCenter - i2ix;
      double i6iy = 2 * yCenter - i2iy;
      double i7bx = 2 * xCenter - i3bx;
      double i7by = 2 * yCenter - i3by;
      double i7ix = 2 * xCenter - i3ix;
      double i7iy = 2 * yCenter - i3iy;
      double n0bx = 2 * xCenter - n4bx;
      double n0by = 2 * yCenter - n4by;
      double n0ix = 2 * xCenter - n4ix;
      double n0iy = 2 * yCenter - n4iy;

      if (slope < 0) {
        updateRegionsBoundaryModel(RegionsBoundary.P1toP2, i0bx, i0by, i0ix, i0iy);
        updateRegionsBoundaryModel(RegionsBoundary.P2toP3, i1bx, i1by, i1ix, i1iy);
        updateRegionsBoundaryModel(RegionsBoundary.P3toP4, i2bx, i2by, i2ix, i2iy);
        updateRegionsBoundaryModel(RegionsBoundary.P4toNULL, i3bx, i3by, i3ix, i3iy);
        updateRegionsBoundaryModel(RegionsBoundary.NULLtoN1, n4bx, n4by, n4ix, n4iy);
        updateRegionsBoundaryModel(RegionsBoundary.N1toN2, i4bx, i4by, i4ix, i4iy);
        updateRegionsBoundaryModel(RegionsBoundary.N2toN3, i5bx, i5by, i5ix, i5iy);
        updateRegionsBoundaryModel(RegionsBoundary.N3toN4, i6bx, i6by, i6ix, i6iy);
        updateRegionsBoundaryModel(RegionsBoundary.N4toNULL, i7bx, i7by, i7ix, i7iy);
        updateRegionsBoundaryModel(RegionsBoundary.NULLtoP1, n0bx, n0by, n0ix, n0iy);
      } else {
        updateRegionsBoundaryModel(RegionsBoundary.P1toP2, i0bx, i0by, i0ix, i0iy);
        updateRegionsBoundaryModel(RegionsBoundary.P2toP3, i1bx, i1by, i1ix, i1iy);
        updateRegionsBoundaryModel(RegionsBoundary.NULLtoN1, i2bx, i2by, i2ix, i2iy);
        updateRegionsBoundaryModel(RegionsBoundary.P3toP4, i3bx, i3by, i3ix, i3iy);
        updateRegionsBoundaryModel(RegionsBoundary.P4toNULL, n4bx, n4by, n4ix, n4iy);
        updateRegionsBoundaryModel(RegionsBoundary.N1toN2, i4bx, i4by, i4ix, i4iy);
        updateRegionsBoundaryModel(RegionsBoundary.N2toN3, i5bx, i5by, i5ix, i5iy);
        updateRegionsBoundaryModel(RegionsBoundary.NULLtoP1, i6bx, i6by, i6ix, i6iy);
        updateRegionsBoundaryModel(RegionsBoundary.N3toN4, i7bx, i7by, i7ix, i7iy);
        updateRegionsBoundaryModel(RegionsBoundary.N4toNULL, n0bx, n0by, n0ix, n0iy);
      }
      unblockUpdate();
    }
    return;
  }

  public void readSession(final File file) throws Exception {
    synchronized (_lock) {
      blockUpdate();
      XmlUtils.readXML(file, this);
      Properties properties = new Properties();
      try {
        if (!file.exists()) {
          ServiceProvider.getLoggingService().getLogger(getClass()).error(
              "File \'" + file.getAbsolutePath() + "\' does not exist.");
        }
        FileInputStream istream = new FileInputStream(file);
        properties.load(istream);
      } catch (Exception ex) {
        ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.toString(), ex);
        return;
      }
      unblockUpdate();
    }

    // Update the bounds of the crossplot before triggering the ellipse model updates.
    // Otherwise, the crossplot will change the model based on its current bounds and
    // the defined ellipses.
    double minX = Double.MAX_VALUE;
    double maxX = -Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double maxY = -Double.MAX_VALUE;
    for (RegionsBoundary boundary : RegionsBoundary.values()) {
      double outerX = getRegionBoundariesOuterX(boundary);
      double outerY = getRegionBoundariesOuterY(boundary);
      minX = Math.min(minX, outerX);
      maxX = Math.max(maxX, outerX);
      minY = Math.min(minY, outerY);
      maxY = Math.max(maxY, outerY);
    }
    double xMaxAbs = Math.max(Math.abs(minX), Math.abs(maxX));
    double yMaxAbs = Math.max(Math.abs(minY), Math.abs(maxY));
    double commonMinMax = Math.max(xMaxAbs, yMaxAbs);
    CrossplotBoundsModel boundsModel = new CrossplotBoundsModel(BoundsType.USER_DEFINED, commonMinMax, minX, maxX,
        minY, maxY);
    ABavoCrossplot crossplot = (ABavoCrossplot) ABavoCrossplotRegistry.get().getCrossplots()[0];
    crossplot.applyBounds(boundsModel);

    updatedRegionBoundaries();
    updatedEllipses();
  }

  public void writeSession(final File file) throws Exception {
    XmlUtils.writeXML(file, this);
  }

  @Override
  public void dispose() {
    _listeners.clear();
  }

  /**
   * @param regressionStats
   */
  public void updateEllipses(final RegressionStatistics regressionStats) {
    synchronized (_lock) {
      blockUpdate();
      EllipseType[] ids = { EllipseType.Background, EllipseType.Maximum };
      for (EllipseType id : ids) {
        double length = getEllipseModel(id).getLength();
        double width = getEllipseModel(id).getWidth();
        double centerX = getEllipseModel(id).getCenterX();
        double centerY = getEllipseModel(id).getCenterY();
        updateEllipseModel(id, regressionStats.getSlope(), length, width, centerX, centerY);
      }
      unblockUpdate();
    }
    updated(Type.EllipsesUpdated);
  }

  public final String[] _ellipseTagsXML = { "Background", "Maximum" };

  public final String[] _regionBoundTagsXML = { "TopClassI_TopClassII", "TopClassII_TopClassIII",
      "TopClassIII_TopClassIV", "TopClassIV_Null", "Null_BaseClassI", "BaseClassI_BaseClassII",
      "BaseClassII_BaseClassIII", "BaseClassIII_BaseClassIV", "BaseClassIV_Null", "Null_TopClassI" };

  /**
   * Gets the ellipse regions model information as XML.
   * @param doc The document source.
   * @param parent the parent node.
   * @return The node.
   * @throws Exception Thrown on XML parsing error.
   */
  @Override
  public void getXML(final Document doc, final Node parent) throws Exception {

    if (doc == null) {
      throw new Exception("Error: No XML document specified.");
    }

    // Create the node for EllipseRegionsModel.
    Node nodeEllipseRegionsModel = XmlUtils.addElement(doc, parent, CROSSPLOT_ELLIPSE_MODEL_TAG);

    Properties properties = new Properties();

    // Get ellipse properties.
    Node nodeEllipses = XmlUtils.addElement(doc, nodeEllipseRegionsModel, ELLIPSES_TAG);
    EllipseType[] ellipses = { EllipseType.Background, EllipseType.Maximum };

    for (int i = 0; i < ellipses.length; i++) {

      properties.clear();
      getEllipseProperties(ellipses[i], properties);
      if (!properties.isEmpty()) {
        Element nodeEllipse = XmlUtils.addElement(doc, nodeEllipses, _ellipseTagsXML[i]);

        for (Object key : properties.keySet()) {
          Object obj = properties.get(key);
          Node nodeEllipseAttr = XmlUtils.addAttribute(doc, nodeEllipse, key.toString(), obj.toString());
        }
      }
    }

    // Get region bound properties.
    Node nodeRegionBounds = XmlUtils.addElement(doc, nodeEllipseRegionsModel, REGION_BOUNDARIES_TAG);
    RegionsBoundary[] regionBounds = getRegionBoundaries();

    for (int i = 0; i < NUMBER_OF_REGION_BOUNDARIES; i++) {

      properties.clear();
      getRegionsBoundaryProperties(regionBounds[i], properties);
      if (!properties.isEmpty()) {
        Element nodeRegionBound = XmlUtils.addElement(doc, nodeRegionBounds, _regionBoundTagsXML[i]);

        for (Object key : properties.keySet()) {
          Object obj = properties.get(key);
          Node nodeEllipseAttr = XmlUtils.addAttribute(doc, nodeRegionBound, key.toString(), obj.toString());
        }
      }
    }

    // Get miscellaneous ellipse/region properties.
    properties.clear();
    getGeneralProperties(properties);

    Element nodeGeneral = XmlUtils.addElement(doc, nodeEllipseRegionsModel, GENERAL_TAG);

    for (Object key : properties.keySet()) {
      Object obj = properties.get(key);
      Node node = XmlUtils.addAttribute(doc, nodeGeneral, key.toString(), obj.toString());
    }
  }

  /**
   * Sets the ellipse regions model information from XML.
   * @param doc The document source.
   * @param parent the parent node.
   * @throws Exception Thrown on XML parsing error.
   */
  @Override
  public void setXML(final Document doc, final Node parent) throws Exception {

    if (doc == null) {
      throw new Exception("Error: No XML document specified.");
    }

    Element nodeEllipseRegionsModel = null;

    NodeList nodeList = doc.getElementsByTagName(CROSSPLOT_ELLIPSE_MODEL_TAG);
    int nodeCount = nodeList.getLength();

    if (nodeCount == 1) {
      nodeEllipseRegionsModel = (Element) nodeList.item(0);
    } else if (nodeCount > 1) {
      throw new Exception("Error: Multiple " + CROSSPLOT_ELLIPSE_MODEL_TAG + " elements found.");
    }

    if (nodeEllipseRegionsModel == null) {
      throw new Exception("Error: No " + CROSSPLOT_ELLIPSE_MODEL_TAG + " element exists.");
    }

    Properties properties = new Properties();

    // Set ellipse properties.
    NodeList nodesEllipses = nodeEllipseRegionsModel.getElementsByTagName(ELLIPSES_TAG);

    if (nodesEllipses != null && nodesEllipses.getLength() == 1) {

      EllipseType[] ellipses = { EllipseType.Background, EllipseType.Maximum };

      for (int i = 0; i < ellipses.length; i++) {

        Element nodeEllipses = (Element) nodesEllipses.item(0);
        NodeList nodesEllipse = nodeEllipses.getElementsByTagName(_ellipseTagsXML[i]);

        if (nodesEllipse != null && nodesEllipse.getLength() > 0) {

          Element nodeEllipse = (Element) nodesEllipse.item(0);
          NamedNodeMap map = nodeEllipse.getAttributes();

          for (int j = 0; j < map.getLength(); j++) {
            Attr attr = (Attr) map.item(j);

            properties.put(attr.getName(), attr.getValue());
          }
        }
        setEllipseProperties(ellipses[i], properties);
        properties.clear();
      }
    }

    // Set region bound properties.
    NodeList nodesRegionBounds = nodeEllipseRegionsModel.getElementsByTagName(REGION_BOUNDARIES_TAG);

    if (nodesRegionBounds != null && nodesRegionBounds.getLength() == 1) {

      RegionsBoundary[] regionBounds = getRegionBoundaries();

      for (int i = 0; i < NUMBER_OF_REGION_BOUNDARIES; i++) {

        Element nodeRegionBounds = (Element) nodesRegionBounds.item(0);
        NodeList nodesRegionBound = nodeRegionBounds.getElementsByTagName(_regionBoundTagsXML[i]);

        if (nodesRegionBound != null && nodesRegionBound.getLength() > 0) {

          Element nodeRegionBound = (Element) nodesRegionBound.item(0);
          NamedNodeMap map = nodeRegionBound.getAttributes();

          for (int j = 0; j < map.getLength(); j++) {
            Attr attr = (Attr) map.item(j);

            properties.put(attr.getName(), attr.getValue());
          }
        }
        setRegionsBoundaryProperties(regionBounds[i], properties);
        properties.clear();
      }
    }

    // Set miscellaneous ellipse/region properties.
    Element nodeGeneral = null;
    NodeList nodesGeneral = nodeEllipseRegionsModel.getElementsByTagName(GENERAL_TAG);

    if (nodesGeneral != null && nodesGeneral.getLength() > 0) {

      nodeGeneral = (Element) nodesGeneral.item(0);

      NamedNodeMap map = nodeGeneral.getAttributes();

      for (int i = 0; i < map.getLength(); i++) {
        Attr attr = (Attr) map.item(i);

        properties.put(attr.getName(), attr.getValue());
      }
    }
    setGeneralProperties(properties);
    properties.clear();

    return;
  }

  /**
   * Gets the ellipse model miscellaneous properties.
   * @param properties the ellipse model miscellaneous properties.
   * @return the ellipse model miscellaneous properties.
   * @exception Exception thrown on properties error.
   */
  private Properties getGeneralProperties(final Properties properties) throws Exception {
    String keyword = "RegionSymmetry";
    properties.setProperty(keyword, Boolean.toString(getSymmetricRegions()));
    return properties;
  }

  /**
   * Sets the ellipse model miscellaneous properties.
   * @param properties the ellipse model miscellaneous properties.
   * @exception Exception thrown on properties error.
   */
  private void setGeneralProperties(final Properties properties) throws Exception {
    String value = properties.getProperty("RegionSymmetry");
    if (value != null) {
      setSymmetricRegions(Boolean.parseBoolean(value));
    }
  }

  /**
   * Gets the properties of the specified ellipse.
   * @param type the type of the ellipse to get properties.
   * @param properties the properties of the specified ellipse.
   * @return the properties of the specified ellipse.
   * @exception Exception thrown on properties error.
   */
  private Properties getEllipseProperties(final EllipseType type, final Properties properties) throws Exception {
    EllipseModel model = getEllipseModel(type);
    properties.setProperty(SLOPE, Double.toString(model.getSlope()));
    properties.setProperty(LENGTH, Double.toString(model.getLength()));
    properties.setProperty(WIDTH, Double.toString(model.getWidth()));
    properties.setProperty(CENTER_X, Double.toString(model.getCenterX()));
    properties.setProperty(CENTER_Y, Double.toString(model.getCenterY()));
    return properties;
  }

  /**
   * Sets the properties of the specified ellipse.
   * @param type the type of the ellipse to set properties.
   * @param properties the properties of the specified ellipse.
   * @exception Exception thrown on properties error.
   */
  private void setEllipseProperties(final EllipseType type, final Properties properties) throws Exception {
    String[] keys = { SLOPE, LENGTH, WIDTH, CENTER_X, CENTER_Y };
    double[] values = { Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN };
    for (int i = 0; i < keys.length; i++) {
      String value;
      if ((value = properties.getProperty(keys[i])) != null) {
        values[i] = Double.parseDouble(value);
      }
    }
    updateEllipseModel(type, values[0], values[1], values[2], values[3], values[4]);
  }

  /**
   * Gets the properties of the specified region bound.
   * @param id the id of the region bound to get properties.
   * @param properties the properties of the specified region bound.
   * @return the properties of the specified region bound.
   * @exception Exception thrown on properties error.
   */
  public Properties getRegionsBoundaryProperties(final RegionsBoundary id, final Properties properties) throws Exception {
    RegionsBoundaryModel model = getRegionsBoundaryModel(id);
    properties.setProperty(OUTER_X, Double.toString(model.getOuterX()));
    properties.setProperty(OUTER_Y, Double.toString(model.getOuterY()));
    properties.setProperty(INNER_X, Double.toString(model.getInnerX()));
    properties.setProperty(INNER_Y, Double.toString(model.getInnerY()));
    return properties;
  }

  /**
   * Sets the properties of the specified region bound.
   * @param id the id of the region bound to get properties.
   * @param properties the properties of the specified region bound.
   * @exception Exception thrown on properties error.
   */
  public void setRegionsBoundaryProperties(final RegionsBoundary id, final Properties properties) throws Exception {
    String[] keys = { OUTER_X, OUTER_Y, INNER_X, INNER_Y };
    double[] values = { Double.NaN, Double.NaN, Double.NaN, Double.NaN };
    for (int i = 0; i < keys.length; i++) {
      String value;
      if ((value = properties.getProperty(keys[i])) != null) {
        values[i] = Double.parseDouble(value);
      }
    }
    blockUpdate();
    updateRegionsBoundaryModel(id, values[0], values[1], values[2], values[3]);
    unblockUpdate();
    updatedRegionBoundaries();
  }

  public void validate(IValidation results) {
    // TODO Auto-generated method stub

  }

  @Override
  public Map<String, String> pickle() {
    // A fully custom pickle method, since this is a non-standard model.
    Map<String, String> map = new HashMap<String, String>();
    map.put(ELLIPSE_REGIONS_MODEL_PREFIX + " " + SYMMETRIC_REGIONS, Boolean.toString(getSymmetricRegions()));
    try {
      EllipseType[] ellipseTypes = { EllipseType.Background, EllipseType.Maximum };
      for (EllipseType ellipseType : ellipseTypes) {
        Properties properties = new Properties();
        getEllipseProperties(ellipseType, properties);
        String[] keys = { SLOPE, LENGTH, WIDTH, CENTER_X, CENTER_Y };
        for (String key : keys) {
          map.put(ELLIPSE_REGIONS_MODEL_PREFIX + " " + ellipseType.toString() + " " + key, properties.getProperty(key));
        }
      }
      for (RegionsBoundary boundary : RegionsBoundary.values()) {
        Properties properties = new Properties();
        getRegionsBoundaryProperties(boundary, properties);
        String[] keys = { OUTER_X, OUTER_Y, INNER_X, INNER_Y };
        for (String key : keys) {
          map.put(ELLIPSE_REGIONS_MODEL_PREFIX + " " + boundary.toString() + " " + key, properties.getProperty(key));
        }
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return map;
  }

  @Override
  public void unpickle(Map<String, String> map) {
    // A fully custom unpickle method, since this is a non-standard model.
    String value = map.get(ELLIPSE_REGIONS_MODEL_PREFIX + " " + SYMMETRIC_REGIONS);
    setSymmetricRegions(Boolean.parseBoolean(value));
    try {
      EllipseType[] ellipseTypes = { EllipseType.Background, EllipseType.Maximum };
      for (EllipseType ellipseType : ellipseTypes) {
        System.out.println("unpicking ellipse: " + ellipseType.toString());
        Properties properties = new Properties();
        String[] keys = { SLOPE, LENGTH, WIDTH, CENTER_X, CENTER_Y };
        for (String key : keys) {
          String ellipseValue = map.get(ELLIPSE_REGIONS_MODEL_PREFIX + " " + ellipseType.toString() + " " + key);
          System.out.println("unpicking ellipse property: " + key + "=" + ellipseValue);
          properties.put(key, ellipseValue);
        }
        setEllipseProperties(ellipseType, properties);
      }
      for (RegionsBoundary boundary : RegionsBoundary.values()) {
        System.out.println("unpicking ellipse: " + boundary.toString());
        Properties properties = new Properties();
        String[] keys = { OUTER_X, OUTER_Y, INNER_X, INNER_Y };
        for (String key : keys) {
          String polygonValue = map.get(ELLIPSE_REGIONS_MODEL_PREFIX + " " + boundary.toString() + " " + key);
          System.out.println("unpicking boundary property: " + key + "=" + polygonValue);
          properties.put(key, polygonValue);
        }
        setRegionsBoundaryProperties(boundary, properties);
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
