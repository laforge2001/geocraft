/**
 * 
 */
package org.geocraft.core.model.datatypes;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 * A Point class that is conformable with the javaseis.org MultiArray class
 * This point is N-Dimensional, and can have multiple attributes associated with the point
 * location. Attributes can be any primitive class or an Object.
 * @author moshecc
 *
 */
public class MultiPoint {

  /** Location of this point */
  private double[] _location;

  private ArrayList<String> _attributes;

  /**
   * 
   * @param location the x,y,z coordinates (in that order)
   * @param attributes the attributes for this point (maps to the names given in the pointset)
   */
  public MultiPoint(final double[] location, final String[] attributes) {
    initLocation(location);
    setAttributes(attributes);
  }

  /**
   * 
   * @param location the x,y,z coordinates (in that order)
   * @param attributes the attributes for this point (maps to the names given in the pointset)
   */
  public MultiPoint(final double[] location, final Collection<String> attributes) {
    initLocation(location);
    setAttributes(attributes);
  }

  public MultiPoint(final MultiPoint point) {
    initLocation(point.getLocation());
    setAttributes(point.getAttributes());
  }

  private void initLocation(final double[] location) {
    _location = new double[location.length];
    System.arraycopy(location, 0, _location, 0, location.length);
  }

  /**
   * returns a REFERENCE to the location array (no copy)
   */
  public double[] getLocation() {
    //    double[] result = new double[_location.length];
    //    System.arraycopy(_location, 0, result, 0, _location.length);
    //    return result;
    return _location;
  }

  public double getX() {
    return _location[0];
  }

  public double getY() {
    return _location[1];
  }

  public double getZ() {
    return _location[2];
  }

  public void setLocation(final double[] location) {
    initLocation(location);
  }

  public String[] getAttributes() {
    String[] result = new String[_attributes.size()];
    for (int i = 0; i < result.length; ++i) {
      result[i] = _attributes.get(i);
    }
    return result;
  }

  public List<String> getAttributesList() {
    return new ArrayList<String>(_attributes);
  }

  public String getAttribute(final int i) {
    return _attributes.get(i);
  }

  public void setAttribute(final int i, final String value) {
    _attributes.set(i, value);
  }

  public void setAttributes(final String[] attributes) {
    _attributes = new ArrayList<String>();
    for (String attr : attributes) {
      _attributes.add(attr);
    }
  }

  /* (non-Javadoc)
   * @see beta.core.model.points.IMultiPoint#setAttributes(java.util.Collection)
   */
  public void setAttributes(final Collection<String> attributes) {
    _attributes = new ArrayList<String>(attributes);
  }

  @Override
  public String toString() {
    return "MultiPoint : " + Arrays.toString(_location) + " Attributes: " + Arrays.toString(_attributes.toArray());
  }
}
