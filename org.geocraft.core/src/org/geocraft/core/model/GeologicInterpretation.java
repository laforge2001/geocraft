/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model;


import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.LoadStatus;
import org.geocraft.core.model.geologicfeature.GeologicFeature;
import org.geocraft.core.model.mapper.IMapper;


/**
 * A geologic interpretation contains the shape of a geologic feature, as well
 * as a quantity attribute evaluated on the geometry of the shape.
 * <p>
 * Within a given earth model a geologic feature may have multiple geologic interpretations
 * but none should represent a different geologic opinion. A geologic interpretation expressing
 * an alternate opinion should be in a different earth model or a different feature.
 * <p>
 * Multiple geologic interpretations of the same feature may be in a single earth model if the
 * different geologic interpretations are parts of the same geologic feature. This may arise if
 * the feature was interpreted over different seismic geometries.  Reverse faulted horizons are
 * another case where multiple geologic interpretations can exist.
 * <p>
 * Also, the same geologic feature in an earth model can have multiple geologic interpretations
 * that reflect different shape types (e.g. both a 2D grid and trimesh interpretations of the
 * same geologic opinion of a horizon feature).
 */
public abstract class GeologicInterpretation extends Entity {

  /**
   * The name of the company, vendor, software application, or other provider
   * supplying the information.
   */
  private String _dataSource;

  /**
   * The name or identifier (for example, user-id) of the person that created
   * the interpretation.
   */
  private String _interpreter;

  /**
   * The earth model in which the geologic interpretation is contained.
   */
  private EarthModel _earthModel;

  /**
   * The geologic feature to which the geologic interpretation is associated.
   */
  private GeologicFeature _geologicFeature;

  /**
   * The elevation of the vertical reference datum. This is the height offset to the MapSystem
   * and provides the reference point for what time = 0 means.
   * For entities with a seismic origin (e.g. seismic grids, faults) this is commonly referred
   * to as the seismic elevation datum. For entities without a seismic origin (e.g. non-seismic grids,
   * point sets generated from well data) the datum is typically zero.
   * This datum is used when converting the depth-based primary Z values to a depth-based MapSystem
   * commonly used when posting seismic-based data with well data.
   * The datum is commonly zero for data acquired offshore (e.g. sea level).
   */
  private float _datumElevation;

  /**
   * The domain (time or depth) of the structural interpretation.
   */
  private Domain _zDomain;

  /**
   * Parameterized constructor.
   *
   * @param name the name of the interpretation.
   * @param mapper the entity mapper
   */
  protected GeologicInterpretation(final String name, final IMapper mapper) {
    super(name, mapper);
    _status = LoadStatus.GHOST;
  }

  /**
   * Returns the data source of the interpretation.
   * @return the data source.
   */
  public String getDataSource() {
    load();
    return _dataSource;
  }

  /**
   * Sets data source of the interpretation.
   * @param dataSource the data source to set.
   */
  public void setDataSource(final String dataSource) {
    _dataSource = dataSource;
  }

  /**
   * Returns the interpreter of the interpretation..
   * @return the interpreter
   */
  public String getInterpreter() {
    load();
    return _interpreter;
  }

  /**
   * Sets the interpreter of the interpretation.
   * @param interpreter the interpreter to set.
   */
  public void setInterpreter(final String interpreter) {
    _interpreter = interpreter;
  }

  /**
   * Returns the earth model in which the geologic interpretation is contained.
   */
  public EarthModel getEarthModel() {
    load();
    return _earthModel;
  }

  /**
   * Returns the geologic feature to which the interpretation is associated.
   */
  public GeologicFeature getGeologicFeature() {
    load();
    return _geologicFeature;
  }

  /**
   * Sets the geologic feature to which the interpretation is associated.
   */
  public void setGeologicFeature(final GeologicFeature geologicFeature) {
    _geologicFeature = geologicFeature;
  }

  /**
   * Returns the elevation of the vertical reference datum.
   * The unit of measurement is the vertical distance unit from the application preferences.
   */
  public float getDatumElevation() {
    load();
    return _datumElevation;
  }

  /**
   * Sets the elevation of the vertical reference datum.
   * The unit of measurement is the vertical distance unit from the application preferences.
   */
  public void setDatumElevation(final float datumElevation) {
    _datumElevation = datumElevation;
  }

  /**
   * Returns the z domain of the geologic interpretation.
   */
  public Domain getZDomain() {
    load();
    return _zDomain;
  }

  /**
   * Sets the z domain of the geologic interpretation.
   */
  public void setZDomain(final Domain domain) {
    _zDomain = domain;
  }
}
