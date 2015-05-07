/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */
package org.geocraft.core.model.well;


import java.sql.Timestamp;

import org.geocraft.core.common.util.Utilities;
import org.geocraft.core.model.base.ValueObject;
import org.geocraft.core.model.datatypes.Coordinate;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.FloatMeasurementSeries;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.preferences.ApplicationPreferences;


/**
 * The WellBore table provides descriptive information about a well bore and the geometry information of the well bore path. WellBore
 * contains logs, picks, and well velocities.
 */
public class WellBore extends ValueObject {

  /** The well that contains this well bore. */
  private final Well _well;

  /**
   * The azimuth north reference type of the survey. When inserting/updating AzimuthNorthType, PathAzimuth should be
   * inserted/updated also.
   */
  private String _azimuthNorthType;

  /**
   * Collection of various identifiers by which this well bore is known. The identifiers are represented by an array of name and
   * value pairs where the name is well identifier type and the value is the identifier value. Examples of identifier types are API
   * and Common Name.
   */
  private String[] _boreAliases = new String[0];

  /** Collection of various types of identifiers by which this well bore is known. */
  private String[] _boreAliasTypes = new String[0];

  /** The operational state of the well bore. */
  private String _boreStatus;

  /** The bottom location of the well in the specified coordinate system. */
  private Coordinate _bottomLocation;

  /** Method used to compute the wellpath geometry. */
  private String _calcMethod;

  /** Comment */
  private String _comment;

  /** The date the activities that created the well completed. */
  private Timestamp _completionDate;

  /** Text describing where/by who the well bore originated. */
  private String _dataSource;

  /** The well velocity used by default to compute the bore's path. */
  private WellCheckShot _defaultCheckShot;

  /** Distance from the regional vertical datum (positive upward). */
  private float _elevation;

  /** The datum from which depth measurements are taken. */
  // TODO enum? is this kb etc.?
  private String _elevationDatum;

  /** Indicates the flowing mode of the well bore. */
  private String _flowDirection;

  /** Type of fluid flowing into or out of the well bore. */
  private String _fluidType;

  /** The name of the formation encountered at total depth. */
  private String _formationAtTotalDepth;

  /** Primary well bore identifier assigned to a well by the API, ERCB, or any government agency. */
  private String _identifier;

  /** The type of identifier provided for the well bore. */
  // TODO enum?
  private String _identifierType;

  /**
   * The well path azimuth values. The azimuth values are always returned in the project's map display coordinate system. When
   * inserting/updating a well path, the PathXOffset, PathYOffset, PathMD, PathDip, PathAzimuth, and PathTVD or PathTWT columns
   * should be supplied. When inserting/updating PathAzimuth, AzimuthNorthType should be inserted/updated also.
   */
  private FloatMeasurementSeries _pathAzimuth;

  /**
   * The well path dip values. When inserting/updating a well path, the PathXOffset, PathYOffset, PathMD, PathDip, PathAzimuth, and
   * PathTVD or PathTWT columns should be supplied.
   */
  private FloatMeasurementSeries _pathDip;

  /**
   * The measured depth values of the well path positive downward from the well datum.
   * The unit of measurement is the application vertical distance unit.
   */
  private float[] _zMDs;

  /**
   * The true-vertical-depth values of the well path positive downward from the well datum.
   * The unit of measurement is the application vertical distance unit.
   */
  private float[] _zTVDs;

  /**
   * The two-way-time values of the well path.
   * The unit of measurement is the application time unit.
   */
  private float[] _zTWTs;

  /**
   * The x-offset values of the well path.
   * This is the x coordinate offset from the well surface location.
   * The unit of measurement is the application horizontal distance unit.
   */
  private double[] _xOffsets;

  /**
   * The y-offset values of the well path.
   * This is the y coordinate offset from the well surface location.
   * The unit of measurement is the application horizontal distance unit.
   */
  private double[] _yOffsets;

  /**
   * The distance measured along the well path from the elevationDatum to the deepest reachable point of the well bore. If the well
   * has been plugged back, it is the point to which the well is plugged back.
   */
  private float _plugBackTotalDepth;

  /** Type of hydrocarbon shown from the well bore. */
  private String _showType;

  /** Date when drilling bit penetrates surface utilizing a drilling rig capable of drilling the well to the authorized depth. */
  private Timestamp _spudDate;

  /**
   * The domain is either Domain.TIME or Domain.DISTANCE.
   * If setDepthsAndTimes was passed with a zero-length TVD, then the domain will be TIME
   * If setDepthsAndTimes was passed with a zero-length TWT, then the domain will be DEPTH
   * 
   * The purpose of this flags is to distinguish values computed from this functions from
   * original values.
   */
  private WellDomain _originalWellDomain;

  //  public WellBore(final String name) {
  //    this(name, new InMemoryMapper(WellBore.class));
  //  }

  /**
   * Constructs a WellBore entity.
   * 
   * @param name of the well bore
   * @param mapper the well bore mapper to the underlying datastore.
   */
  public WellBore(final String name, final Well well) {
    super(name);
    //_mapper = mapper;
    _well = well;
    _zMDs = new float[0];
    _zTVDs = new float[0];
    _zTWTs = new float[0];
    _xOffsets = new double[0];
    _yOffsets = new double[0];
    _originalWellDomain = WellDomain.TRUE_VERTICAL_DEPTH;
  }

  /**
   * @return either WellDomain.TRUE_VERTICAL_DEPTH or WellDomain.TWO_WAY_TIME, depending
   * on whether TVD or TWT was specified when setDepthsAndTimes was called.  This allows
   * recovery of the originally specified values.
   */
  public WellDomain getOriginalWellDomain() {
    return _originalWellDomain;
  }

  /**
   * Gets the locations along the well bore at the given measured depths.
   * The z values of the locations are in the requested domain.
   * 
   * @param measuredDepths the array of measured depths.
   * @param domain the desired domain (TVD, MD, etc) of the locations.
   * @return the location along the well bore.
   */
  public Point3d[] getLocationsFromMeasuredDepths(final float[] measuredDepths, final WellDomain domain) throws Exception {
    _well.load();
    if (measuredDepths == null || measuredDepths.length <= 0 || _zMDs == null) {
      return new Point3d[0];
    }
    Point3d[] locations = new Point3d[measuredDepths.length];
    for (int i = 0; i < measuredDepths.length; i++) {
      locations[i] = getLocationFromMeasuredDepth(measuredDepths[i], domain);
    }
    return locations;
  }

  /**
   * The azimuth north reference type of the survey. When inserting/updating AzimuthNorthType, PathAzimuth should be
   * inserted/updated also.
   * 
   * @return azimuthNorthType
   */
  public String getAzimuthNorthType() {
    _well.load();
    return _azimuthNorthType;
  }

  /**
   * Collection of various identifiers by which this well bore is known. The identifiers are represented by an array of name and
   * value pairs where the name is well identifier type and the value is the identifier value. Examples of identifier types are API
   * and Common Name.
   * 
   * @return boreAliases
   */
  public String[] getBoreAliases() {
    _well.load();
    String[] aliases = new String[_boreAliases.length];
    System.arraycopy(_boreAliases, 0, aliases, 0, _boreAliases.length);
    return aliases;
  }

  /**
   * Collection of various types of identifiers by which this well bore is known.
   * 
   * @return boreAliasTypes
   */
  public String[] getBoreAliasTypes() {
    _well.load();
    String[] aliasTypes = new String[_boreAliasTypes.length];
    System.arraycopy(_boreAliasTypes, 0, aliasTypes, 0, _boreAliasTypes.length);
    return aliasTypes;
  }

  /**
   * The operational state of the well bore.
   * 
   * @return boreStatus
   */
  public String getBoreStatus() {
    _well.load();
    return _boreStatus;
  }

  /**
   * The bottom location of the well in the specified coordinate system.
   * 
   * @return bottomLocation
   */
  public Coordinate getBottomLocation() {
    _well.load();
    return _bottomLocation;
  }

  /**
   * Method used to compute the wellpath geometry.
   * 
   * @return calcMethod
   */
  public String getCalcMethod() {
    _well.load();
    return _calcMethod;
  }

  /**
   * Comment regarding the wellbore geometry
   * 
   * @return calcMethod
   */
  public String getComment() {
    _well.load();
    return _comment;
  }

  /**
   * The date the activities that created the well completed.
   * 
   * @return completionDate
   */
  public Timestamp getCompletionDate() {
    _well.load();
    return _completionDate;
  }

  /**
   * Text describing where/by who the well bore originated.
   * 
   * @return dataSource
   */
  public String getDataSource() {
    _well.load();
    return _dataSource;
  }

  /**
   * Returns the check shot used by default to compute the bore's path.
   */
  public WellCheckShot getDefaultCheckShot() {
    _well.load();
    return _defaultCheckShot;
  }

  /**
   * Returns the distance from the regional vertical datum (positive upward).
   * The unit of measurement is the application vertical distance unit.
   * 
   * @return the elevation.
   */
  public float getElevation() {
    _well.load();
    return _elevation;
  }

  /**
   * The datum from which depth measurements are taken.
   * 
   * @return elevationDatum
   */
  public String getElevationDatum() {
    _well.load();
    return _elevationDatum;
  }

  /**
   * Indicates the flowing mode of the well bore.
   * 
   * @return flowDirection
   */
  public String getFlowDirection() {
    _well.load();
    return _flowDirection;
  }

  /**
   * Type of fluid flowing into or out of the well bore.
   * 
   * @return fluidType
   */
  public String getFluidType() {
    _well.load();
    return _fluidType;
  }

  /**
   * The name of the formation encountered at total depth.
   * 
   * @return formationAtTD
   */
  public String getFormationAtTD() {
    _well.load();
    return _formationAtTotalDepth;
  }

  /**
   * Primary well bore identifier assigned to a well by the API, ERCB, or any government agency.
   * 
   * @return identifier
   */
  public String getIdentifier() {
    _well.load();
    return _identifier;
  }

  /**
   * The type of identifier provided for the well bore.
   * 
   * @return identifierType
   */
  public String getIdentifierType() {
    _well.load();
    return _identifierType;
  }

  /**
   * Gets the z values for a given domain.
   * The necessary conversion will be done automatically.
   * 
   * @param domain the domain of the z values (MD, TVD, TWT, etc).
   * @return the z values.
   */
  public float[] getZValues(final WellDomain domain) {
    switch (domain) {
      case ONE_WAY_TIME:
        return getOneWayTimes();
      case TWO_WAY_TIME:
        return getTwoWayTimes();
      case MEASURED_DEPTH:
        return getMeasuredDepths();
      case TRUE_VERTICAL_DEPTH:
        return getTrueVerticalDepths();
      case TRUE_VERTICAL_DEPTH_SUBSEA:
        return getTrueVerticalDepthsSubsea();
      default:
        throw new IllegalArgumentException("Invalid domain: " + domain);
    }
  }

  /**
   * Returns the array of true-vertical-depths-sub-sea for the well bore.
   * These are computed from the true-vertical-depths.
   * 
   * @return the true-vertical-depths-sub-sea.
   */
  public float[] getTrueVerticalDepthsSubsea() {
    _well.load();
    float[] zTVDSSs = getTrueVerticalDepths();
    for (int k = 0; k < zTVDSSs.length; k++) {
      zTVDSSs[k] -= _elevation;
    }
    return zTVDSSs;
  }

  /**
   * Returns the array of true-vertical-depths for the well bore.
   * 
   * @return the true-vertical-depths.
   */
  public float[] getTrueVerticalDepths() {
    _well.load();
    // Check if the TVDs are set.
    if (_zTVDs.length == 0) {
      // If not, then calculate them from the check shot.
      return calculateTVDsFromCheckShot();
    }
    return Utilities.copyFloatArray(_zTVDs);
  }

  /**
   * Returns the array of measured-depths for the well bore.
   * 
   * @return the measured-depths.
   */
  public float[] getMeasuredDepths() {
    _well.load();
    return Utilities.copyFloatArray(_zMDs);
  }

  /**
   * Returns the array of two-way-times for the well bore.
   * 
   * @return the two-way-times.
   */
  public float[] getTwoWayTimes() {
    _well.load();
    // Check if the TWTs are set.
    if (_zTWTs.length == 0) {
      // If not, then calculate them from the check shot.
      return calculateTWTsFromCheckShot();
    }
    return Utilities.copyFloatArray(_zTWTs);
  }

  /**
   * Returns the array of one-way-times for the well bore.
   * These are computed from the two-way-times.
   * 
   * @return the one-way-times.
   */
  public float[] getOneWayTimes() {
    _well.load();
    float[] zOWTs = getTwoWayTimes();
    for (int k = 0; k < zOWTs.length; k++) {
      zOWTs[k] /= 2;
    }
    return zOWTs;
  }

  /**
   * Returns the path of the well bore in the TVDSS domain.
   */
  public CoordinateSeries getPathTVDSS() {
    _well.load();

    float[] zTVDs = _zTVDs;

    // If the true-vertical-depths are not currently specified, then try to calculate
    // then from the default check shot.
    if (zTVDs == null || zTVDs.length == 0) {
      zTVDs = calculateTVDsFromCheckShot();
    }

    // If there are still no true-vertical-depths, then simply return an empty coordinate series.
    if (zTVDs == null || zTVDs.length == 0) {
      return CoordinateSeries.createDirect(new Point3d[0], ApplicationPreferences.getInstance()
          .getTimeCoordinateSystem());
    }

    // Create the array of points for the coordinate series.
    Coordinate coordinate = _well.getLocation();
    double x = coordinate.getX();
    double y = coordinate.getY();
    Point3d[] points = new Point3d[zTVDs.length];
    for (int i = 0; i < points.length; i++) {
      // Compute the TVDSS by subtracting the elevation.
      float zTVDSS = zTVDs[i] - _elevation;
      points[i] = new Point3d(x + _xOffsets[i], y + _yOffsets[i], zTVDSS);
    }
    return CoordinateSeries.createDirect(points, ApplicationPreferences.getInstance().getTimeCoordinateSystem());
  }

  /**
   * Returns the path of the well bore in the TWT domain.
   */
  public CoordinateSeries getPathTWT() {
    _well.load();

    float[] zTWTs = _zTWTs;

    // If the two-way-times are not currently specified, then try to calculate
    // then from the default check shot.
    if (zTWTs == null || zTWTs.length == 0) {
      zTWTs = calculateTWTsFromCheckShot();
    }

    // If there are still no two-way-times, then simply return an empty coordinate series.
    if (zTWTs == null || zTWTs.length == 0) {
      return CoordinateSeries.createDirect(new Point3d[0], ApplicationPreferences.getInstance()
          .getTimeCoordinateSystem());
    }

    // Create the array of points for the coordinate series.
    Coordinate coordinate = _well.getLocation();
    double x = coordinate.getX();
    double y = coordinate.getY();
    Point3d[] points = new Point3d[zTWTs.length];
    for (int i = 0; i < points.length; i++) {
      points[i] = new Point3d(x + _xOffsets[i], y + _yOffsets[i], zTWTs[i]);
    }
    return CoordinateSeries.createDirect(points, ApplicationPreferences.getInstance().getTimeCoordinateSystem());
  }

  /**
   * The well path azimuth values. The azimuth values are always returned in the project's map display coordinate system. When
   * inserting/updating a well path, the PathXOffset, PathYOffset, PathMD, PathDip, PathAzimuth, and PathTVD or PathTWT columns
   * should be supplied. When inserting/updating PathAzimuth, AzimuthNorthType should be inserted/updated also.
   * 
   * @return pathAzimuth
   */
  public FloatMeasurementSeries getPathAzimuth() {
    _well.load();
    return _pathAzimuth;
  }

  /**
   * The well path dip values. When inserting/updating a well path, the PathXOffset, PathYOffset, PathMD, PathDip, PathAzimuth, and
   * PathTVD or PathTWT columns should be supplied.
   * 
   * @return pathDip
   */
  public FloatMeasurementSeries getPathDip() {
    _well.load();
    return _pathDip;
  }

  /**
   * The distance measured along the well path from the elevationDatum to the deepest reachable point of the well bore. If the well
   * has been plugged back, it is the point to which the well is plugged back.
   * 
   * @return plugBackTotalDepth
   */
  public float getPlugBackTotalDepth() {
    _well.load();
    return _plugBackTotalDepth;
  }

  /**
   * Type of hydrocarbon show from the well bore.
   * 
   * @return showType
   */
  public String getShowType() {
    _well.load();
    return _showType;
  }

  /**
   * Date when drilling bit penetrates surface utilizing a drilling rig capable of drilling the well to the authorized depth.
   * 
   * @return spudDate
   */
  public Timestamp getSpudDate() {
    _well.load();
    return _spudDate;
  }

  /**
   * Data key string representing the well that contains this well bore.
   * 
   * @return well
   */
  public Well getWell() {
    _well.load();
    return _well;
  }

  /**
   * The azimuth north reference type of the survey. When inserting/updating AzimuthNorthType, PathAzimuth should be
   * inserted/updated also.
   * 
   * @param azimuthNorthType
   */
  public void setAzimuthNorthType(final String azimuthNorthType) {
    _azimuthNorthType = azimuthNorthType;
    // setDirty(true);
  }

  /**
   * Sets the collection of various identifiers by which this well bore is known.
   * The identifiers are represented by an array of name and value pairs where the
   * name is well identifier type and the value is the identifier value. Examples of
   * identifier types are 'API' and 'Common Name'.
   * 
   * @param boreAliases the array of bore aliases.
   * @param boreAliasTypes the array of bore alias types.
   * @throws IllegalArgumentException if the arrays are not of the same length.
   */
  public void setBoreAliases(final String[] boreAliases, final String[] boreAliasTypes) {
    if (boreAliases.length != boreAliasTypes.length) {
      throw new IllegalArgumentException("The length of the bore alias and alias type arrays do not match.");
    }
    _boreAliases = new String[boreAliases.length];
    System.arraycopy(boreAliases, 0, _boreAliases, 0, boreAliases.length);
    _boreAliasTypes = new String[boreAliasTypes.length];
    System.arraycopy(boreAliasTypes, 0, _boreAliasTypes, 0, boreAliasTypes.length);
    // setDirty(true);
  }

  /**
   * The operational state of the well bore.
   * 
   * @param boreStatus
   */
  public void setBoreStatus(final String boreStatus) {
    _boreStatus = boreStatus;
    // setDirty(true);
  }

  /**
   * The bottom location of the well in the specified coordinate system.
   * 
   * @param bottomLocation
   */
  public void setBottomLocation(final Coordinate bottomLocation) {
    _bottomLocation = bottomLocation;
    // setDirty(true);
  }

  /**
   * Method used to compute the wellpath geometry.
   * 
   * @param calcMethod
   */
  public void setCalcMethod(final String calcMethod) {
    _calcMethod = calcMethod;
    // setDirty(true);
  }

  /**
   * Comment regarding the wellbore geometry.
   * 
   * @param calcMethod
   */
  public void setComment(final String comment) {
    _comment = comment;
    // setDirty(true);
  }

  /**
   * The date the activities that created the well completed.
   * 
   * @param completionDate
   */
  public void setCompletionDate(final Timestamp completionDate) {
    _completionDate = completionDate;
    // setDirty(true);
  }

  /**
   * Text describing where/by who the well bore originated.
   * 
   * @param dataSource
   */
  public void setDataSource(final String dataSource) {
    _dataSource = dataSource;
    // setDirty(true);
  }

  /**
   * Sets the check shot used by default to compute the bore's path.
   */
  public void setDefaultCheckShot(final WellCheckShot defaultCheckShot) {
    _defaultCheckShot = defaultCheckShot;
    //_well.load();
    //    if (defaultCheckShot != null) {
    //
    //      for (int i = 0; i < defaultCheckShot.getNumSamples(); i++) {
    //        float datum = defaultCheckShot.getDatum();
    //        float TVD = defaultCheckShot.getTrueVerticalDepths()[i];
    //        float TWT = defaultCheckShot.getTwoWayTimes()[i];
    //        float MD = getMeasuredDepth(TVD, WellDomain.TRUE_VERTICAL_DEPTH_SUBSEA);
    //        float VEL = Float.NaN;
    //        if (i > 0) {
    //          float TVD_prev = defaultCheckShot.getTrueVerticalDepths()[i - 1];
    //          float TWT_prev = defaultCheckShot.getTwoWayTimes()[i - 1];
    //          float DZ = TVD - TVD_prev;
    //          float DT = (TWT - TWT_prev) / 2;
    //          VEL = DZ / DT;
    //        }
    //        //System.out.println("TVD: " + TVD + "   TWT: " + TWT + "   MD: " + MD + "   VEL: " + VEL);
    //      }
    //    }
    //setDirty(true);
  }

  /**
   * Sets the distance from the regional vertical datum (positive upward).
   * The unit of measurement is the application vertical distance unit.
   * 
   * @param elevation the elevation.
   */
  public void setElevation(final float elevation) {
    _elevation = elevation;
    // setDirty(true);
  }

  /**
   * The datum from which depth measurements are taken.
   * 
   * @param elevationDatum
   */
  public void setElevationDatum(final String elevationDatum) {
    _elevationDatum = elevationDatum;
    // setDirty(true);
  }

  /**
   * Indicates the flowing mode of the well bore.
   * 
   * @param flowDirection
   */
  public void setFlowDirection(final String flowDirection) {
    _flowDirection = flowDirection;
    // setDirty(true);
  }

  /**
   * Type of fluid flowing into or out of the well bore.
   * 
   * @param fluidType
   */
  public void setFluidType(final String fluidType) {
    _fluidType = fluidType;
    // setDirty(true);
  }

  /**
   * The name of the formation encountered at total depth.
   * 
   * @param formationAtTD
   */
  public void setFormationAtTD(final String formationAtTD) {
    _formationAtTotalDepth = formationAtTD;
    // setDirty(true);
  }

  /**
   * Primary well bore identifier assigned to a well by the API, ERCB, or any government agency.
   * 
   * @param identifier
   */
  // TODO: can't change identifier once bore is created
  public void setIdentifier(final String identifier) {
    _identifier = identifier;
    // setDirty(true);
  }

  /**
   * The type of identifier provided for the well bore.
   * 
   * @param identifierType
   */
  // TODO: can't change identifier type once bore is created
  public void setIdentifierType(final String identifierType) {
    _identifierType = identifierType;
    // setDirty(true);
  }

  /**
   * The well path azimuth values. The azimuth values are always returned in the project's map display coordinate system. When
   * inserting/updating a well path, the PathXOffset, PathYOffset, PathMD, PathDip, PathAzimuth, and PathTVD or PathTWT columns
   * should be supplied. When inserting/updating PathAzimuth, AzimuthNorthType should be inserted/updated also.
   * 
   * @param pathAzimuth
   */
  // TODO 360: all the setPathxx methods need to be combined into 1 single set methods
  public void setPathAzimuth(final FloatMeasurementSeries pathAzimuth) {
    _pathAzimuth = pathAzimuth;
    // setDirty(true);
  }

  /**
   * The well path dip values. When inserting/updating a well path, the PathXOffset, PathYOffset, PathMD, PathDip, PathAzimuth, and
   * PathTVD or PathTWT columns should be supplied.
   * 
   * @param pathDip
   */
  // TODO 360: all the setPathxx methods need to be combined into 1 single set methods
  public void setPathDip(final FloatMeasurementSeries pathDip) {
    _pathDip = pathDip;
    // setDirty(true);
  }

  /**
   * Sets the measured depths and associated true-vertical-depths (or two-way-times).
   * The measured depth array must be specified, along with one of the other 2 arrays,
   * either true-vertical-depths or two-way-times. If specifying MDs and TVDs, then
   * a zero-length array should be passed in for the TWTs. If specifying MDs and TWTs,
   * then a zero-length array should be passed in for the TVDs.
   * 
   * @param zMDs the array of measured depths.
   * @param zTVDs the array of true-vertical-depths (optional).
   * @param zTWTs the array of two-way-times (optional).
   */
  public void setDepthsAndTimes(final float[] zMDs, final float[] zTVDs, final float[] zTWTs) {
    _zMDs = Utilities.copyFloatArray(zMDs);
    _zTVDs = Utilities.copyFloatArray(zTVDs);
    _zTWTs = Utilities.copyFloatArray(zTWTs);
    if (zTVDs.length > 0) {
      _originalWellDomain = WellDomain.TRUE_VERTICAL_DEPTH;
    } else {
      _originalWellDomain = WellDomain.TWO_WAY_TIME;
    }
  }

  /**
   * Sets the x and y offsets of the well bore path.
   * 
   * @param xOffsets the array of x-offset values.
   * @param yOffsets the array of y-offset values.
   * @throws IllegalArgumentException if the arrays are not of the same length.
   */
  public void setXYOffsets(final double[] xOffsets, final double[] yOffsets) {
    if (xOffsets.length != yOffsets.length) {
      throw new IllegalArgumentException("The length of the x-offsey and y-offset arrays do not match.");
    }
    _xOffsets = Utilities.copyDoubleArray(xOffsets);
    _yOffsets = Utilities.copyDoubleArray(yOffsets);
    // setDirty(true);
  }

  /**
   * returns the xOffset array
   * @return the xOffset array
   */
  public double[] getXOffsets() {
    return _xOffsets;
  }

  /**
   * Retruns the yOffset array
   * @return the yOffset array
   */
  public double[] getYOffsets() {
    return _yOffsets;
  }

  /**
   * Sets the distance measured along the well path from the elevationDatum to the deepest
   * reachable point of the well bore. If the well has been plugged back, it is the point
   * to which the well is plugged back.
   * 
   * @param plugBackTotalDepth
   */
  public void setPlugBackTotalDepth(final float plugBackTotalDepth) {
    _plugBackTotalDepth = plugBackTotalDepth;
    // setDirty(true);
  }

  /**
   * Type of hydrocarbon show from the well bore.
   * 
   * @param showType
   */
  public void setShowType(final String showType) {
    _showType = showType;
    // setDirty(true);
  }

  /**
   * Date when drilling bit penetrates surface utilizing a drilling rig capable of drilling the well to the authorized depth.
   * 
   * @param spudDate
   */
  public void setSpudDate(final Timestamp spudDate) {
    _spudDate = spudDate;
    // setDirty(true);
  }

  //  /**
  //   * Data key string representing the well that contains this well bore.
  //   * 
  //   * @param well
  //   */
  //  // TODO: can't change the parent
  //  public void setWell(final Well well) {
  //    _well = well;
  //    // setDirty(true);
  //  }

  /**
   * Calculates the two-way-time values for the well path from the default check shot.
   */
  private float[] calculateTWTsFromCheckShot() {
    _well.load();
    // If no check shot exists, then simply return a zero-length array.
    if (_defaultCheckShot == null) {
      return new float[0];
    }

    // Otherwise, use the check shot to calculate TWTs from TVDSSs.
    return _defaultCheckShot.calculateTWTsFromTVDs(getTrueVerticalDepthsSubsea(), 0);
  }

  /**
   * Calculates the true-vertical-depth values for the well path from the default check shot.
   */
  private float[] calculateTVDsFromCheckShot() {
    _well.load();
    // If no check shot exists, then simply return a zero-length array.
    if (_defaultCheckShot == null) {
      return new float[0];
    }

    // Otherwise, use the check shot to calculate TVDs from TWTs.
    float elevation = getElevation();
    float[] zTVDSSs = _defaultCheckShot.calculateTVDsFromTWTs(_zTWTs, 0);
    float[] zTVDs = new float[zTVDSSs.length];
    for (int i = 0; i < zTVDSSs.length; i++) {
      zTVDs[i] = zTVDSSs[i] + elevation;
    }
    return zTVDs;
  }

  /**
   * Gets the two-way-time(TWT) corresponding to a given measured depth (MD).
   * If necessary, a time/depth conversion will be performed using the default check shot.
   * 
   * @param measuredDepth the measured depth.
   * @return the corresponding two-way-time; or NaN if outside the measured depth range of the well bore.
   */
  public float getTwoWayTimeFromMeasuredDepth(final float measuredDepth) {
    _well.load();

    // Set the default value.
    float zTWT = Float.NaN;

    // Check that either the TVDs or TWTs are specified.
    if (_zMDs.length == 0 || _zTVDs.length == 0 && _zTWTs.length == 0) {
      throw new RuntimeException("The well bore does not have valid MDS or TVDs (or TWTs) specified: " + toString());
    }

    // If the well bore is not specified in TWT, then use the default check shot to convert.
    if (_zTWTs.length == 0) {

      // If there is no check shot available, then return a NaN value.
      if (_defaultCheckShot == null) {
        return zTWT;
      }

      // Otherwise, use the check shot to do the depth-time conversion.
      float zTVD = getTrueVerticalDepthFromMeasuredDepth(measuredDepth);
      if (Float.isNaN(zTVD)) {
        return zTWT;
      }
      float zTVDSS = zTVD - getElevation();
      return _defaultCheckShot.caluclateTWTFromTVD(zTVDSS, 0);
    }

    return WellUtil.lookupValue(measuredDepth, _zMDs, "MD", _zTWTs, "TWT");
  }

  /**
   * Gets the true-vertical-depth (TVD) corresponding to a given measured depth (MD).
   * If necessary, a time/depth conversion will be performed using the default check shot.
   * 
   * @param measuredDepth the measured depth.
   * @return the corresponding true-vertical-depth; or NaN if outside the measured depth range of the well bore.
   */
  public float getTrueVerticalDepthFromMeasuredDepth(final float measuredDepth) {
    _well.load();

    // Set the default value.
    float zTVD = Float.NaN;

    // Check that either the TVDs or TWTs are specified.
    if (_zMDs.length == 0 || _zTVDs.length == 0 && _zTWTs.length == 0) {
      throw new RuntimeException("The well bore does not have valid MDS or TVDs (or TWTs) specified: " + toString());
    }

    // If the well bore is not specified in TVD, then use the default check shot to convert.
    if (_zTVDs.length == 0) {

      // If there is no check shot available, then return a NaN value.
      if (_defaultCheckShot == null) {
        return zTVD;
      }

      // Otherwise, use the check shot to do the depth-time conversion.
      float zTWT = getTwoWayTimeFromMeasuredDepth(measuredDepth);
      if (Float.isNaN(zTWT)) {
        return zTVD;
      }
      float zTVDSS = _defaultCheckShot.caluclateTVDFromTWT(zTWT, 0);
      zTVD = zTVDSS + getElevation();
    }

    return WellUtil.lookupValue(measuredDepth, _zMDs, "MD", _zTVDs, "TVD");
  }

  /**
   * Gets the location along the well bore at the given measured depth.
   * The z value of the location is in the requested domain.
   * 
   * @param measuredDepth the measured depth.
   * @param domain the desired domain (TVD, MD, etc) of the location.
   * @return the location along the well bore.
   */
  public Point3d getLocationFromMeasuredDepth(final float measuredDepth, final WellDomain domain) {
    _well.load();

    int numPoints = _xOffsets.length;

    // If the well bore does not contain at least 2 points, throw an exception.
    if (numPoints < 2) {
      throw new RuntimeException("The well bore contains less than 2 points: " + getDisplayName());
    }

    // If the well bore does not have any measured depths, throw an exception.
    if (_zMDs.length == 0) {
      throw new RuntimeException("No measured depths exist for the well bore: " + getDisplayName());
    }

    int zIndex;
    for (zIndex = 0; zIndex < _zMDs.length; zIndex++) {
      if (_zMDs[zIndex] >= measuredDepth) {
        break;
      }
    }

    // If the measured depth is outside the bounds of the well bore, return a null point.
    if (zIndex < 0 || zIndex >= _zMDs.length) {
      return null;
    }

    // Get the z values of the requested domain.
    float[] zValues = getZValues(domain);
    if (zValues == null || zValues.length == 0) {
      // domain values not available
      return null;
    }

    // Compute the x,y,z values of the point.
    double x = _xOffsets[0];
    double y = _yOffsets[0];
    double z = zValues[0];
    if (zIndex > 0) {
      double previousX = _xOffsets[zIndex - 1];
      double previousY = _yOffsets[zIndex - 1];
      double currentX = _xOffsets[zIndex];
      double currentY = _yOffsets[zIndex];
      float previousZ = zValues[zIndex - 1];
      float currentZ = zValues[zIndex];
      float currentMD = _zMDs[zIndex];
      float previousMD = _zMDs[zIndex - 1];
      x = (measuredDepth - previousMD) / (currentMD - previousMD) * (currentX - previousX) + previousX;
      y = (measuredDepth - previousMD) / (currentMD - previousMD) * (currentY - previousY) + previousY;
      z = (measuredDepth - previousMD) / (currentMD - previousMD) * (currentZ - previousZ) + previousZ;
    }
    Coordinate surfaceLocation = _well.getLocation();
    x += surfaceLocation.getX();
    y += surfaceLocation.getY();
    return new Point3d(x, y, z);
  }

  /**
   * Returns the measured depth for the given depth and domain.
   * 
   * @param depth the depth to convert to measured depth.
   * @param domain the domain of the depth to convert.
   * @return the measured depth.
   */
  public float getMeasuredDepth(final float depth, final WellDomain domain) {
    float[] depths = getZValues(domain);
    float[] zMDs = getZValues(WellDomain.MEASURED_DEPTH);
    for (int i = 1; i < depths.length; i++) {
      if (depth >= depths[i - 1] && depth <= depths[i]) {
        float percent1 = (depth - depths[i - 1]) / (depths[i] - depths[i - 1]);
        float percent0 = 1 - percent1;
        return zMDs[i - 1] * percent0 + zMDs[i] * percent1;
      }
    }
    return Float.NaN;
  }
}
