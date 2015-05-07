/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.model.culture;


import java.util.HashSet;
import java.util.Set;

import org.geocraft.core.model.Entity;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.DataType;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.InMemoryMapper;


/**
 * A named collection of GIS features that are all of the same type.
 * <p>
 * For example a layer might contain a set of points representing platform locations, 
 * or a set of polylines representing rivers, or a set polygonal features representing lease blocks) 
 * <p>
 * The features all have the same set of possible attribute names/types. // TODO 360
 * <p>
 * The feature name attribute is determined as follows. First, look for a layer attribute
 * whose name is "NAME" (ignoring case). If that fails, look for the first layer attribute
 * whose name contains "NAME" (ignoring case). If that, too, fails, use the first layer
 * attribute.
 */

public class Layer extends Entity {

  /** The coordinate system that the points in this layer are defined in. */
  // TODO doubt we need this
  private CoordinateSystem _coordinateSystem;

  /** The name of the coordinate system that the points in this layer are defined in. */
  // TODO 360 needed?
  private String _coordinateSystemName;

  /** A textual description of this layer. */
  private String _description;

  /** The layer attribute used to populate the "Name" attribute of the "EpCulture_Feature" class. */
  // TODO 360 not needed?
  private String _featureNameAttribute;

  /** True if the layer features have z-values. */
  private boolean _is3D;

  /** True if the layer features have measure values. */
  private boolean _isMeasured;

  /** The geometry type of layer - this will be "Point", "Polyline" or "Polygon". */
  // TODO 360 use the object type? or examine contents to find it. 
  private final LayerType _layerType;

  /** The name of the owner of the database tables for this layer. */
  private String _owner;

  /** The features contained in this layer. */
  private final Set<Feature> _features = new HashSet<Feature>();

  /** The names of the layer specific extended attributes that features in this layer have */
  private final String[] _attributeNames;

  /** The types of the layer specific extended attributes that features in this layer have */
  private final DataType[] _attributeTypes;

  public Layer(final String name, final LayerType layerType, final String[] attributeNames, final DataType[] attributeTypes) {
    this(name, new InMemoryMapper(Layer.class), layerType, attributeNames, attributeTypes);
  }

  /**
   * Parameterized constructor
   *
   * @param name of the entity.
   * @param mapper the mapper.
   * @param layerType Type of the layer. Will be "Point", "Polyline", or "Polygon".
   * @param attributeNames of the extended feature attributes that all features in this layer have.
   * @param attributeTypes of the extended feature attributes that all features in this layer have.
   */
  public Layer(final String name, final IMapper mapper, final LayerType layerType, final String[] attributeNames, final DataType[] attributeTypes) {

    super(name, mapper);
    _layerType = layerType;
    _attributeNames = attributeNames;
    _attributeTypes = attributeTypes;
  }

  /**
   * Reference to the coordinate system that the points in this layer are defined in.
   *
   * @return coordinateSystem
   * 
   * TODO 360 do we need this?
   */
  public CoordinateSystem getCoordinateSystem() {
    return _coordinateSystem;
  }

  /**
   * The name of the coordinate system that the points in this layer are defined in.
   *
   * @return coordinateSystemName
   */
  public String getCoordinateSystemName() {
    return _coordinateSystemName;
  }

  /**
   * A textual description of this layer.
   *
   * @return description
   */
  public String getDescription() {
    return _description;
  }

  /**
   * The layer attribute used to populate the "Name" attribute of the "EpCulture_Feature" class.
   *
   * @return featureNameAttribute
   */
  public String getFeatureNameAttribute() {
    return _featureNameAttribute;
  }

  /**
   * True if the layer features have z-values.
   * @return is3D
   */
  public boolean is3D() {
    return _is3D;
  }

  /**
   * True if the layer features have measure values.
   * @return isMeasured
   */
  public boolean isMeasured() {
    return _isMeasured;
  }

  /**
   * The geometry type of layer - this will be "Point", "Polyline" or "Polygon".
   * @return layerType
   */
  public LayerType getLayerType() {
    return _layerType;
  }

  /**
   * The name of the owner of the database tables for this layer.
   * @return owner
   */
  public String getOwner() {
    return _owner;
  }

  /**
   * The features of this layer.
   *
   * @return features
   */
  public Feature[] getFeatures() {

    Feature[] featuresArray = new Feature[_features.size()];

    _features.toArray(featuresArray);
    return featuresArray;
  }

  /**
   * Reference to the coordinate system that the points in this layer are defined in.
   *
   * @param coordinateSystem
   */
  // TODO 360 
  public void setCoordinateSystem(final CoordinateSystem coordinateSystem) {
    _coordinateSystem = coordinateSystem;
    setDirty(true);
  }

  /**
   * The name of the coordinate system that the points in this layer are defined in.
   *
   * @param coordinateSystemName
   */
  public void setCoordinateSystemName(final String coordinateSystemName) {
    _coordinateSystemName = coordinateSystemName;
    setDirty(true);
  }

  /**
   * A textual description of this layer.
   *
   * @param description
   */
  public void setDescription(final String description) {
    _description = description;
    setDirty(true);
  }

  /**
   * The layer attribute used to populate the "Name" attribute of the "EpCulture_Feature" class.
   *
   * @param featureNameAttribute
   */
  public void setFeatureNameAttribute(final String featureNameAttribute) {
    _featureNameAttribute = featureNameAttribute;
    setDirty(true);
  }

  /**
   * True if the layer features have z-values.
   * TODO 360 property only needs to be assigned in constructor?
   * 
   * TODO getDimension would allow for 4d etc. later. 
   *
   * @param is3D
   */
  public void set3D(final boolean is3D) {
    _is3D = is3D;
    setDirty(true);
  }

  /**
   * True if the layer features have measure values.
   * TODO 360 property only needs to be assigned in constructor?
   *
   * @param isMeasured
   */
  public void setMeasured(final boolean isMeasured) {
    _isMeasured = isMeasured;
    setDirty(true);
  }

  /**
   * The name of the owner of the database tables for this layer.
   * @param owner
   */
  public void setOwner(final String owner) {
    _owner = owner;
    setDirty(true);
  }

  /**
   * Add an array of Features, ignoring duplicates, for this layer.
   * @param features An array of features of this layer.
   */
  public void addFeatures(final Feature[] features) {

    for (Feature feature : features) {
      addFeature(feature);
    }
  }

  /**
   * Add a Feature, ignoring duplicates.
   *
   * @param feature object of this layer.
   */
  public void addFeature(final Feature feature) {

    if (_layerType == LayerType.POINT && !PointFeature.class.isAssignableFrom(feature.getClass())) {
      throw new IllegalArgumentException("Invalid feature type " + feature);
    }

    if (_layerType == LayerType.POLYLINE && !PolylineFeature.class.isAssignableFrom(feature.getClass())) {
      throw new IllegalArgumentException("Invalid feature type " + feature);
    }

    if (_layerType == LayerType.POLYGON && !PolygonFeature.class.isAssignableFrom(feature.getClass())) {
      throw new IllegalArgumentException("Invalid feature type " + feature);
    }

    // TODO 360 this use to enforce uniqueness using the key
    _features.add(feature);
  }

  /**
   * Remove elements specified in the parameter.
   *
   * @param features An array of features of this layer.
   */
  public void removeFromFeatures(final Feature[] features) {

    for (Feature feature : features) {
      _features.remove(feature);
    }
  }

  /**
   * Removes all features of this layer.
   */
  public void removeAllFeatures() {
    _features.clear();
  }

  /**
   * @return the LayerAttributeNames
   */
  public String[] getAttributeNames() {
    return _attributeNames;
  }

  /**
   * @return the LayerAttributeTypes
   */
  public DataType[] getAttributeTypes() {
    return _attributeTypes;
  }

}
