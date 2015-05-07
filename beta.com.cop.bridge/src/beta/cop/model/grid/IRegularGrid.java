/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package beta.cop.model.grid;


public interface IRegularGrid {

  public abstract OrientationType getOrientation();

  /**
   * Return the number of dimensions that this grid has
   * @return number of dimensions
   */
  public abstract int getNumDimensions();

  /**
   * Return the length of each axis of this grid
   * @return length of each axis
   */
  public abstract int[] getLengths();

  /**
   * Return the local length of each axis of this grid
   * @return length of each axis
   */
  public abstract int[] getLocalLengths();

  /**
      zdim = 0;
      xdim = 1;
      ydim = 2;
   * Return the delta between values along each axis of this grid
   * @return array containing delta values for each axis
   */
  public abstract double[] getDeltas();

  /**
   * Check if the global DistributedArray position is local to this task.
   * @param position Global position
   * @return true if the global position is local to this task
   */
  public abstract boolean isPositionLocal(int[] position);

  /**
   * Get the sample value at the given positionworld
   * @param position
   * @return
   */
  public abstract float getSample(int[] position);

  /**
   * @param _position
   * @return
   */
  public abstract float getFloat(int[] _position);

  /**
   * @param _position
   * @return
   */
  public abstract int getInt(int[] _position);

  /**
   * @param _position
   * @return
   */
  public abstract double getDouble(int[] _position);

  /**
   * @param val
   * @param _position
   */
  public abstract void putSample(float val, int[] _position);

  /**
   * @param val
   * @param _position
   */
  public abstract void putSample(double val, int[] _position);

  /**
   * Return the global index along an axis of a DistributedArray given the local
   * index
   * 
   * @param dimension
   *          the dimension of the DistributedArray for which conversion is
   *          requested
   * @param index
   *          the local index for the MultiArray owned by this task
   * @return global index in the DistributedArray
   */
  public abstract int localToGlobal(int dimension, int index);

  public abstract int globalToLocal(int dimension, int index);

  /**
   * @param pos
   * @return
   */
  public abstract int[] localPosition(int[] pos);

  /**
   * Return the location in world coordinates for a given position in the grid.
   * @param position position where world coordinates are desired
   * @param wxyz coordinates for the requested position
   */
  public abstract void worldCoords(int[] pos, double[] wxyz);

  public abstract IRegularGrid createCopy();

}