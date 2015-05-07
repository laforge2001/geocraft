/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.aoi;


import org.geocraft.core.model.Entity;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.IMapper;


/**
 * This class defines a region-of-interest (ROI), which can be used to restrict processing
 * of some data objects to a desired subset in a 3D sense (x,y,z).
 */
public abstract class RegionOfInterest extends Entity {

  /**
   * The base constructor for a region-of-interest (ROI).
   * 
   * @param name the name of the ROI.
   * @param mapper the mapper to an underlying datastore.
   */
  public RegionOfInterest(final String name, final IMapper mapper) {
    super(name, mapper);
  }

  /**
   * Returns the starting and ending z values for the given x,y coordinates.
   * 
   * @param x the x coordinate.
   * @param y the y coordinate.
   * @return an array containing the starting and ending z values, respectively; or a zero-length array if out-of-bounds.
   */
  public abstract float[] getZStartAndEnd(final double x, final double y);

  /**
   * Returns a flag indicating of the given x,y,z coordinate is contained within the ROI.
   * 
   * @param x the world x coordinate.
   * @param y the world y coordinate.
   * @param z the world z coordinate.
   * @param zUnit the unit of measurement for the z coordinate.
   * @return <i>true</i> if the x,y,z is within the ROI; <i>false</i> if not.
   */
  public abstract boolean contains(final double x, final double y, final double z, final Unit zUnit);
}
