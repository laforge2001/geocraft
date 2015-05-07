/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */
package org.geocraft.core.model;


import java.util.ArrayList;
import java.util.List;

import org.geocraft.core.model.PointSetAttribute.Type;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.MultiPoint;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.InMemoryMapper;


/**
 * A set of random x,y,z points. A PointSet may optionally have associated 
 * attribute properties sampled at these points.
 */
public class PointSet extends Entity {

  /** The collection of points in the point set. */
  private final ArrayList<Point3d> _points = new ArrayList<Point3d>();

  /** The collection of attributes associated with the point set.
  private final List<PointSetAttribute> _attributes = new ArrayList<PointSetAttribute>();

  /** The unit of measurement for the values array. */
  private Unit _zUnit;

  /** The z domain (time or depth) of the structural interpretation. */
  private Domain _zDomain;

  private final List<PointSetAttribute> _attributes = new ArrayList<PointSetAttribute>();

  private PointSetAction _pointClickAction = null;

  /**
   * Constructs an in-memory point set with the given name.
   * 
   * @param name the name of the point set.
   */
  public PointSet(final String name) {
    this(name, new InMemoryMapper(PointSet.class));
  }

  /**
   * Constructs a point set with the given datastore mapper.
   * 
   * @param name of the point set.
   * @param mapper of the point set.
   */
  public PointSet(final String name, final IMapper mapper) {
    super(name, mapper);
  }

  /**
   * Returns the number of points contained in the point set.
   * 
   * @return the number of points.
   */
  public synchronized int getNumPoints() {
    load();
    return _points.size();
  }

  /**
   * Returns the specified point from the point set.
   * <p>
   * WARNING: No defensive copy of the points array is made, so any changes
   * made to the point <i>will</i> reflected in the point set.
   * 
   * @param pointIndex the index of the point to get.
   * @return the point in the point set.
   */
  public synchronized Point3d getPointReference(final int pointIndex) {
    load();
    return _points.get(pointIndex);
  }

  /**
   * Returns the specified point from the point set.
   * <p>
   * Note: This returns a copy of the point, so any changes made to
   * the point <i>will not</i> be reflected in the point set.
   * 
   * @param pointIndex the index of the point to get.
   * @return the requested point.
   */
  public synchronized Point3d getPoint(final int pointIndex) {
    load();
    return new Point3d(_points.get(pointIndex));
  }

  /**
   * Adds a point to the point set.
   * 
   * @param point the point to add.
   */
  public synchronized void addPoint(final Point3d point) {
    _points.add(point);
    for (PointSetAttribute attr : _attributes) {
      attr.addPoint();
    }
  }

  /**
   * Gets the unit of measurement for the grid data values.
   * 
   * @return the data unit of measurement.
   */
  public synchronized Unit getZUnit() {
    load();
    return _zUnit;
  }

  /**
   * Returns the z domain of the geologic interpretation.
   * 
   * @return the z domain.
   */
  public synchronized Domain getZDomain() {
    load();
    return _zDomain;
  }

  /**
   * Removes a point from the point set.
   * 
   * @param the index of the point to remove.
   * @return the point that was removed.
   */
  public synchronized Point3d removePoint(final int pointIndex) {
    Point3d pt = _points.remove(pointIndex);
    for (PointSetAttribute attr : _attributes) {
      attr.removePoint(pointIndex);
    }
    return pt;
  }

  /**
   * Checks if the point set contains an attribute with the given name.
   * 
   * @param attributeName the name of the attribute to find.
   * @return <i>true</i> if found; </i>false</i> if not.
   */
  public synchronized boolean containsAttribute(final String attributeName) {
    for (PointSetAttribute attr : _attributes) {
      if (attr.getName().equals(attributeName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Sets the unit of measurement for the z values.
   * <p>
   * This will optionally set the domain as well, if the unit
   * of measurement represents time or depth (length).
   * 
   * @param dataUnit the data unit of measurement.
   */
  public synchronized void setZUnit(final Unit zUnit) {
    _zUnit = zUnit;
    setZDomain(zUnit.getDomain());
  }

  /**
   * Sets the z domain of the geologic interpretation.
   * 
   * @param zDomain the z domain.
   */
  public synchronized void setZDomain(final Domain zDomain) {
    _zDomain = zDomain;
  }

  /**
   * Returns the x coordinate of the point at the given index within the point set.
   * 
   * @param index the index of the point.
   * @return the x coordinate of the point.
   */
  public synchronized double getX(final int index) {
    load();
    return _points.get(index).getX();
  }

  /**
   * Returns the y coordinate of the point at the given index within the point set.
   * 
   * @param index the index of the point.
   * @return the y coordinate of the point.
   */
  public synchronized double getY(final int index) {
    load();
    return _points.get(index).getY();
  }

  /**
   * Returns the z coordinate of the point at the given index within the point set.
   * 
   * @param index the index of the point.
   * @return the z coordinate of the point.
   */
  public synchronized double getZ(final int index) {
    load();
    return _points.get(index).getZ();
  }

  /**
   * Returns the name of an attribute.
   * 
   * @param attributeIndex the index of the attribute to get.
   * @return the name of the attribute.
   */
  public synchronized String getAttributeName(final int attributeIndex) {
    return _attributes.get(attributeIndex).getName();
  }

  /**
   * Returns the type of an attribute.
   * 
   * @param attributeIndex the index of the attribute to get.
   * @return the type of the attribute.
   */
  public synchronized Type getAttributeType(final int attributeIndex) {
    return _attributes.get(attributeIndex).getType();
  }

  public synchronized PointSetAttribute getAttribute(final String attributeName) {
    for (PointSetAttribute attribute : _attributes) {
      if (attribute.getName().equals(attributeName)) {
        return attribute;
      }
    }
    return null;
  }

  public PointSetAttribute getAttribute(final int attributeIndex) {
    return _attributes.get(attributeIndex);
  }

  /**
   * Returns an array of the attribute names for the point set. 
   * 
   * @return an array of the attribute names. 
   */
  public synchronized String[] getAttributeNames() {
    load();
    List<String> attributeNames = new ArrayList<String>();
    for (PointSetAttribute attribute : _attributes) {
      attributeNames.add(attribute.getName());
    }
    return attributeNames.toArray(new String[0]);
  }

  public synchronized PointSetAttribute addAttribute(final Type attributeType, final String attributeName) {
    if (containsAttribute(attributeName)) {
      throw new IllegalArgumentException("Attribute already exists: " + attributeName);
    }
    PointSetAttribute attribute = new PointSetAttribute(attributeType, attributeName, this);
    for (int i = 0; i < _points.size(); i++) {
      attribute.addPoint();
    }
    _attributes.add(attribute);
    return attribute;
  }

  public MultiPoint getMultiPoint(final int index) {
    Point3d pt = getPoint(index);
    String[] values = getAttributeValues(index);
    MultiPoint mpt = new MultiPoint(new double[] { pt.getX(), pt.getY(), pt.getZ() }, values);
    return mpt;
  }

  public String[] getAttributeValues(final int pointIndex) {
    String[] attributeNames = getAttributeNames();
    int numAttributes = attributeNames.length;
    String[] values = new String[numAttributes];
    for (int i = 0; i < numAttributes; i++) {
      values[i] = getAttribute(attributeNames[i]).getString(pointIndex);
    }
    return values;
  }

  public void setPointClickAction(final PointSetAction pointClickAction) {
    _pointClickAction = pointClickAction;
  }

  public void triggerPointClickAction(final int pointIndex) {
    if (_pointClickAction != null) {
      _pointClickAction.initialize(this, pointIndex);
      Thread thread = new Thread(_pointClickAction);
      thread.start();
    }
  }
}
