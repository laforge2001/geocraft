/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.model.well;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.geocraft.core.model.Entity;
import org.geocraft.core.model.datatypes.Coordinate;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.InMemoryMapper;


// TODO Add InitialClass CurrentClass

/**
 * This class provides descriptive information about a well, such as names, aliases, and codes
 * used for identifying a well, supplemented with other associated properties. A well may be the
 * origin of multiple well bores, each of which may have further information, such as picks, logs,
 * and velocities.
 */
public class Well extends Entity {

  /** The well bore associated with the well. */
  private final WellBore _wellBore;

  /** The surface location of the well in the specified coordinate system. */
  private Coordinate _location;

  /** The ground elevation. The unit of measurement is the application vertical distance unit. */
  private float _groundElevation = Float.NaN;

  /** The water depth. The unit of measurement is the application vertical distance unit. */
  private float _waterDepth = Float.NaN;

  /** The name of the country in which the well was drilled. */
  private String _country = "";

  /** The name of the state or province in which the well is located. */
  private String _stateOrProvince = "";

  /** The name of the county in which the well was drilled.  */
  private String _county = "";

  /** The name of the field in which the well was drilled. */
  private String _field = "";

  /** The name of the basin in which the well was drilled. */
  private String _basin = "";

  /** The name of the lease. */
  private String _leaseName = "";

  /** The number of the lease in which the well was drilled. */
  private String _leaseNumber = "";

  /** The unique Outer Continental Shelf number of the well drilled, as assigned by the MMS. */
  private String _oCSNumber = "";

  /** The name of the Outer Continental Shelf area of the well drilled, as assigned by the MMS. */
  private String _offshoreArea = "";

  /** The number of the Outer Continental Shelf block of the well drilled, as assigned by the MMS. */
  private String _offshoreBlock = "";

  /** The number of the lease in which the well was drilled. */
  private String _permitNumber = "";

  /** The name of the company operating this well. */
  private String _currentOperator;

  /** The text describing where/by who the well originated. */
  private String _dataSource = "";

  /** 
   * The primary well identifier assigned to a well by the API, ERCB, or any government agency. 
   * In Canada (and the LAS specification) this is called the license number. 
   */
  private String _identifier = "";

  /** The type of identifier provided for the well. */
  private String _identifierType = "";

  /** The platform from which the well was drilled. */
  private String _platformIdentifier = "";

  /**
   * The date when drilling bit penetrates surface utilizing a drilling rig capable of drilling
   * the well to the authorized depth.
   */
  private Timestamp _spudDate;

  private String _initialClass = "";

  private String _currentClass = "";

  /**
   * The collection of various identifiers by which this well is known. The identifiers are represented
   * by an array of name and value pairs where the name is well identifier type and the value is the
   * identifier value. Examples of identifier types are API and Common Name.
   */
  private String[] _wellAliases = new String[0];

  /** The collection of various types of identifiers by which this well is known. */
  private String[] _wellAliasTypes = new String[0];

  /** The code representing the operational state of the well. */
  private String _wellStatus = "";

  /** The type of well (e.g. on shore, off shore). */
  private String _wellType = "";

  /**
   * The deepest depth drilled in this well bore from elevationDatum to bottom hole.
   * The unit of measurement is the application vertical distance unit.
   */
  private float _totalDepth = 0f;

  /** The type of total depth (e.g. drillers total depth, loggers total depth). */
  private String _totalDepthType = "";

  private final List<WellLogTrace> _wellLogTraces = new ArrayList<WellLogTrace>();

  private final List<WellCheckShot> _wellCheckShots = new ArrayList<WellCheckShot>();

  private final List<WellPick> _wellPicks = new ArrayList<WellPick>();

  /**
   * Constructs an in-memory well.
   * 
   * @param displayName the display name of the well.
   */
  public Well(final String displayName) {
    this(displayName, new InMemoryMapper(Well.class));
  }

  /**
   * Constructs a well, backed by an entry in an underlying datastore.
   * 
   * @param name the display name of the well.
   * @param mapper the mapper to the underlying datastore.
   */
  public Well(final String displayName, final IMapper mapper) {
    super(displayName, mapper);
    _wellBore = new WellBore(displayName + " Bore", this);
  }

  /**
   * The WellBore objects owned by this well.
   *
   * @return wellBores
   */
  public WellBore getWellBore() {
    //load();
    return _wellBore;
  }

  /**
   * Returns the name of the country in which the well was drilled.
   *
   * @return country
   */
  public String getCountry() {
    load();
    return _country;
  }

  /**
   * Returns the name of the county in which the well was drilled.
   *
   * @return county
   */
  public String getCounty() {
    load();
    return _county;
  }

  /**
   * Returns the name of the company operating this well.
   *
   * @return currentOperator
   */
  public String getCurrentOperator() {
    load();
    return _currentOperator;
  }

  /**
   * Returns the text describing where/by who the well originated.
   *
   * @return dataSource
   */
  public String getDataSource() {
    load();
    return _dataSource;
  }

  /**
   * Returns the name of the field in which the well was drilled.
   *
   * @return field
   */
  public String getField() {
    load();
    return _field;
  }

  /**
   * Returns the name of the basin in which the well was drilled.
   *
   * @return field
   */
  public String getBasin() {
    load();
    return _basin;
  }

  /**
   * Returns the ground elevation.
   *
   * @return groundElevation
   */
  public float getGroundElevation() {
    load();
    return _groundElevation;
  }

  /**
   * Returns the primary well identifier assigned to a well by the API, ERCB, or any government agency.
   *
   * @return identifier
   */
  public String getIdentifier() {
    load();
    return _identifier;
  }

  /**
   * Returns the type of identifier provided for the well.
   *
   * @return identifierType
   */
  public String getIdentifierType() {
    load();
    return _identifierType;
  }

  /**
   * Returns the name of the lease in which the well was drilled.
   *
   * @return leaseName
   */
  public String getLeaseName() {
    load();
    return _leaseName;
  }

  /**
   * Returns the number of the lease in which the well was drilled.
   *
   * @return leaseNumber
   */
  public String getLeaseNumber() {
    load();
    return _leaseNumber;
  }

  /**
   * Returns the surface location of the well in the specified coordinate system.
   *
   * @return location
   */
  public Coordinate getLocation() {
    load();
    return _location;
  }

  /**
   * Returns the number of the Outer Continental Shelf number of the well drilled as assigned by the MMS.
   *
   * @return oCSNumber
   */
  public String getOCSNumber() {
    load();
    return _oCSNumber;
  }

  /**
   * Returns the name of the Outer Continental Shelf area of the well drilled as assigned by the MMS.
   *
   * @return offshoreArea
   */
  public String getOffshoreArea() {
    load();
    return _offshoreArea;
  }

  /**
   * Returns the number of the Outer Continental Shelf block of the well drilled as assigned by the MMS.
   *
   * @return offshoreBlock
   */
  public String getOffshoreBlock() {
    load();
    return _offshoreBlock;
  }

  /**
   * Returns the number of the lease in which the well was drilled.
   *
   * @return permitNumber
   */
  public String getPermitNumber() {
    load();
    return _permitNumber;
  }

  /**
   * Returns the platform from which the well was drilled.
   *
   * @return platformIdentifier
   */
  public String getPlatformIdentifier() {
    load();
    return _platformIdentifier;
  }

  /**
   * Returns the date when drilling bit penetrates surface utilizing a drilling rig capable of drilling the well
   * to the authorized depth.
   *
   * @return spudDate
   */
  public Timestamp getSpudDate() {
    load();
    return _spudDate;
  }

  /**
   * Initial Well classification, (e.g., Pseudo Well, Injection Well)
   *
   * @return initialClass
   */
  public String getInitialClass() {
    load();
    return _initialClass;
  }

  /**
   * Current Well classification, (e.g., Pseudo Well, Injection Well)
   *
   * @return currentClass
   */
  public String getCurrentClass() {
    load();
    return _currentClass;
  }

  /**
   * Name of the state or province in which the well is located.
   *
   * @return stateOrProvince
   */
  public String getStateOrProvince() {
    load();
    return _stateOrProvince;
  }

  /**
   * Returns the depth of the water.
   *
   * @return waterDepth
   */
  public float getWaterDepth() {
    load();
    return _waterDepth;
  }

  /**
   * Returns the collection of various identifiers by which this well is known.
   */
  public String[] getAliases() {
    load();

    String[] aliases = new String[_wellAliases.length];

    System.arraycopy(_wellAliases, 0, aliases, 0, _wellAliases.length);
    return aliases;
  }

  /**
   * Returns the collection of various types of identifiers by which this well is known.
   */
  public String[] getAliasTypes() {
    load();

    String[] aliasTypes = new String[_wellAliasTypes.length];

    System.arraycopy(_wellAliasTypes, 0, aliasTypes, 0, _wellAliasTypes.length);
    return aliasTypes;
  }

  /**
   * Returns the code representing the operational state of the well.
   */
  public String getWellStatus() {
    load();
    return _wellStatus;
  }

  /**
   * Returns the type of well (eg. on shore, off shore).
   */
  public String getWellType() {
    load();
    return _wellType;
  }

  /**
   * The deepest depth drilled in this well bore from elevationDatum to bottom hole.
   * 
   * @return totalDepth
   */
  public float getTotalDepth() {
    load();
    return _totalDepth;
  }

  /**
   * TotalDepth type (e.g. drillers total depth, loggers total depth).
   * 
   * @return totalDepthType
   */
  public String getTotalDepthType() {
    load();
    return _totalDepthType;
  }

  //  /**
  //   * Add an array of well bores.
  //   * Any duplicates will be ignored.
  //   *
  //   * @param wellBores the array of well bores to add.
  //   */
  //  public void addWellBores(final WellBore[] wellBores) {
  //    for (WellBore wellBore : wellBores) {
  //      addWellBore(wellBore);
  //    }
  //  }
  //
  //  /**
  //   * Adds a single well bore to the well.
  //   * If duplicate, it will be ignored.
  //   *
  //   * @param wellBore the well bore to add.
  //   */
  //  public void addWellBore(final WellBore wellBore) {
  //    _wellBores.add(wellBore);
  //  }
  //
  //  /**
  //   * Remove elements specified in the parameter.
  //   *
  //   * @param wellBores A array of WellBore objects owned by this well.
  //   */
  //  public void removeFromWellBores(final WellBore[] wellBores) {
  //
  //    for (WellBore wellBore : wellBores) {
  //      _wellBores.remove(wellBore);
  //    }
  //  }
  //
  //  /**
  //   * Removes all wellBores owned by this well.
  //   */
  //  public void removeAllWellBores() {
  //    _wellBores.clear();
  //  }

  /**
   * Sets the name of the basin in which the well was drilled.
   *
   * @param country
   */
  public void setBasin(final String basin) {
    _basin = basin;
    setDirty(true);
  }

  /**
   * Sets the name of the country in which the well was drilled.
   *
   * @param country
   */
  public void setCountry(final String country) {
    _country = country;
    setDirty(true);
  }

  /**
   * Sets the name of the county in which the well was drilled.
   *
   * @param county
   */
  public void setCounty(final String county) {
    _county = county;
    setDirty(true);
  }

  /**
   * Sets the name of the company operating this well.
   *
   * @param currentOperator
   */
  public void setCurrentOperator(final String currentOperator) {
    _currentOperator = currentOperator;
    setDirty(true);
  }

  /**
   * Sets the text describing where/by who the well originated.
   *
   * @param dataSource
   */
  public void setDataSource(final String dataSource) {
    _dataSource = dataSource;
    setDirty(true);
  }

  /**
   * Sets the name of the field in which the well was drilled.
   *
   * @param field
   */
  public void setField(final String field) {
    _field = field;
    setDirty(true);
  }

  /**
   * Sets the ground elevation.
   *
   * @param groundElevation
   */
  public void setGroundElevation(final float groundElevation) {
    _groundElevation = groundElevation;
    setDirty(true);
  }

  /**
   * Sets the primary well identifier (and type) assigned to a well by the API, ERCB, or any government agency.
   *
   * @param identifier the well identifier.
   * @param identifierType the well identifier type.
   */
  public void setIdentifierAndType(final String identifier, final String identifierType) {
    _identifier = identifier;
    _identifierType = identifierType;
    setDirty(true);
  }

  public void setIdentifier(final String identifier) {
    _identifier = identifier;
    setDirty(true);
  }

  public void setIdentifierType(final String identifierType) {
    _identifierType = identifierType;
    setDirty(true);
  }

  /**
   * Sets the name of the lease in which the well was drilled.
   *
   * @param leaseName
   */
  public void setLeaseName(final String leaseName) {
    _leaseName = leaseName;
    setDirty(true);
  }

  /**
   * Sets the number of the lease in which the well was drilled.
   *
   * @param leaseNumber
   */
  public void setLeaseNumber(final String leaseNumber) {
    _leaseNumber = leaseNumber;
    setDirty(true);
  }

  /**
   * Sets the surface location of the well in the specified coordinate system.
   *
   * @param location
   */
  public void setLocation(final Coordinate location) {
    _location = location;
    setDirty(true);
  }

  /**
   * Sets the number of the Outer Continental Shelf number of the well drilled as assigned by the MMS.
   *
   * @param oCSNumber
   */
  public void setOCSNumber(final String oCSNumber) {
    _oCSNumber = oCSNumber;
    setDirty(true);
  }

  /**
   * Sets the name of the Outer Continental Shelf area of the well drilled as assigned by the MMS.
   *
   * @param offshoreArea
   */
  public void setOffshoreArea(final String offshoreArea) {
    _offshoreArea = offshoreArea;
    setDirty(true);
  }

  /**
   * Sets the number of the Outer Continental Shelf block of the well drilled as assigned by the MMS.
   *
   * @param offshoreBlock
   */
  public void setOffshoreBlock(final String offshoreBlock) {
    _offshoreBlock = offshoreBlock;
    setDirty(true);
  }

  /**
   * Sets the number of the lease in which the well was drilled.
   *
   * @param permitNumber
   */
  public void setPermitNumber(final String permitNumber) {
    _permitNumber = permitNumber;
    setDirty(true);
  }

  /**
   * Sets the platform from which the well was drilled.
   *
   * @param platformIdentifier
   */
  public void setPlatformIdentifier(final String platformIdentifier) {
    _platformIdentifier = platformIdentifier;
    setDirty(true);
  }

  /**
   * Sets the date when drilling bit penetrates surface utilizing a drilling rig capable of drilling the well
   * to the authorized depth.
   *
   * @param spudDate
   */
  public void setSpudDate(final Timestamp spudDate) {
    _spudDate = spudDate;
    setDirty(true);
  }

  /**
   * Sets the initial classification of the well.
   *
   * @param spudDate
   */
  public void setInitialClass(final String initialClass) {
    _initialClass = initialClass;
    setDirty(true);
  }

  /**
   * Sets the current classification of the well.
   *
   * @param spudDate
   */
  public void setCurrentClass(final String currentClass) {
    _currentClass = currentClass;
    setDirty(true);
  }

  /**
   * Sets the name of the state or province in which the well is located.
   *
   * @param stateOrProvince
   */
  public void setStateOrProvince(final String stateOrProvince) {
    _stateOrProvince = stateOrProvince;
    setDirty(true);
  }

  /**
   * Sets the depth of the water.
   *
   * @param waterDepth
   */
  public void setWaterDepth(final float waterDepth) {
    _waterDepth = waterDepth;
    setDirty(true);
  }

  /**
   * Sets the collection of various identifiers by which this well is known.
   *
   * @param aliases the array of well aliases.
   * @param aliasTypes the array of well alias types.
   */
  public void setAliasesAndTypes(final String[] aliases, final String[] aliasTypes) {
    if (aliases.length != aliasTypes.length) {
      throw new IllegalArgumentException("Mismatch between the number of aliases and alias types.");
    }
    int numAliases = aliases.length;
    _wellAliases = new String[numAliases];
    _wellAliasTypes = new String[numAliases];
    System.arraycopy(aliases, 0, _wellAliases, 0, numAliases);
    System.arraycopy(aliasTypes, 0, _wellAliasTypes, 0, numAliases);
    setDirty(true);
  }

  public void setAliases(final String[] aliases) {
    System.arraycopy(aliases, 0, _wellAliases, 0, aliases.length);
    setDirty(true);
  }

  public void setAliasTypes(final String[] aliasTypes) {
    System.arraycopy(aliasTypes, 0, _wellAliasTypes, 0, aliasTypes.length);
    setDirty(true);
  }

  /**
   * Sets the code representing the operational state of the well.
   *
   * @param wellStatus
   */
  public void setWellStatus(final String wellStatus) {
    _wellStatus = wellStatus;
    setDirty(true);
  }

  /**
   * Sets the type of well (eg. on shore, off shore).
   *
   * @param wellType
   */
  public void setWellType(final String wellType) {
    _wellType = wellType;
    setDirty(true);
  }

  /**
   * The deepest depth drilled in this well bore from elevationDatum to bottom hole.
   * 
   * @param totalDepth
   */
  public void setTotalDepth(final float totalDepth) {
    _totalDepth = totalDepth;
    setDirty(true);
  }

  /**
   * TotalDepth type (e.g. drillers total depth, loggers total depth).
   * 
   * @param totalDepthType
   */
  public void setTotalDepthType(final String totalDepthType) {
    _totalDepthType = totalDepthType;
    setDirty(true);
  }

  /**
   * Adds a log trace to the well bore.
   * If a trace of this name already exists in the well bore, this method will
   * overwrite with the passed in trace. Otherwise, it is simply added.
   * 
   * @param logTrace the log trace to add.
   */
  public void addWellLogTrace(final WellLogTrace logTrace) {
    // Only add the well log trace if it is not already contained.
    if (!_wellLogTraces.contains(logTrace)) {
      _wellLogTraces.add(logTrace);
    }
  }

  public void removeWellLogTrace(final WellLogTrace logTrace) {
    // Only add the well log trace if it is not already contained.
    if (_wellLogTraces.contains(logTrace)) {
      _wellLogTraces.remove(logTrace);
    }
  }

  /**
   * Returns an array of the log traces associated with the well bore.
   * 
   * @return the log traces.
   */
  public WellLogTrace[] getWellLogTraces() {
    load();
    return _wellLogTraces.toArray(new WellLogTrace[0]);
  }

  /**
   * Adds a check shot to the well bore.
   * If a check shot with this name already exists in the well bore, this method will
   * overwrite with the passed in check shot. Otherwise, it is simply added.
   * 
   * @param checkShot the check shot to add.
   */
  public void addWellCheckShot(final WellCheckShot checkShot) {
    _wellCheckShots.add(checkShot);
  }

  /**
   * The acoustic velocity functions for this bore.
   * 
   * @return wellVelocities
   */
  public WellCheckShot[] getWellCheckshots() {
    load();
    return _wellCheckShots.toArray(new WellCheckShot[0]);
  }

  public void addWellPick(final WellPick pick) {
    _wellPicks.add(pick);
  }

  /**
   * The picks interpreted from the bore's data.
   * 
   * @return picks
   */
  public WellPick[] getWellPicks() {
    load();
    return _wellPicks.toArray(new WellPick[0]);
  }
}
