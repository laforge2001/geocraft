/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package beta.cop.model.points;


import java.util.Collection;
import java.util.List;


public interface IMultiPoint {

  /**
   * @return a copy of the _location
   */
  public abstract double[] getLocation();

  /**
   * @param location the location to set
   */
  public abstract void setLocation(double[] location);

  /**
   * @return the _attributes
   */
  public abstract float[] getAttributes();

  /**
   * @return the _attributes
   */
  public abstract List<Float> getAttributesList();

  public abstract float getAttribute(int i);

  public abstract void setAttribute(int i, float value);

  /**
   * @param attributes the attributes to set
   */
  public abstract void setAttributes(float[] attributes);

  /**
   * @param attributes the attributes to set
   */
  public abstract void setAttributes(Collection<Float> attributes);

}