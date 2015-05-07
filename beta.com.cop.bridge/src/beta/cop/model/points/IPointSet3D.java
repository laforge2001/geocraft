/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package beta.cop.model.points;

import java.util.ArrayList;
import java.util.List;

public interface IPointSet3D {

	/**
	 * Adds a point to the PointSet3D object. TODO needs to be updated to handle
	 * dimensions greater than 3 and more than one attribute per point
	 * 
	 * @param x
	 *            x-coordiante
	 * @param y
	 *            y-coordinate
	 * @param z
	 *            z-coordinate
	 * @param value
	 *            attribute value for this point
	 */
	public abstract void add(Number x, Number y, Number z, float[] value);

	/**
	 * retrieves the attribute's value in the point at the given index within
	 * the point set.
	 * 
	 * @param index
	 *            in the point set of the point
	 * @param name
	 *            of the attribute to find
	 * @return attribute value of the point at the given index. if attribute is
	 *         blank or invalid return the first attribute available. attributes
	 *         can be null/blank depending on the underlying datastore (ie.
	 *         geoprobe)
	 */
	public abstract float getAttributeValue(int index, String name);

	/**
	 * Adds an attribute name to the point set. It is the caller's
	 * responsibility that the index of the names match the index of the
	 * attributes on each point
	 * 
	 * @param name
	 *            name of the attribute
	 */
	public abstract void addAttribute(String name);

	public abstract String getAttribute(int index);

	public abstract List<String> getAttributes();

	/**
	 * get x axis index
	 * 
	 * @return index of x axis
	 */
	public abstract int getXdim();

	/**
	 * get y axis index
	 * 
	 * @return index of y axis
	 */
	public abstract int getYdim();

	/**
	 * get z axis index
	 * 
	 * @return index of z axis
	 */
	public abstract int getZdim();

	/**
	 * returns point with coordinates in the grid coordinate system
	 * 
	 * @param index
	 * @return
	 */
	public abstract IMultiPoint get(int index);

	/**
	 * returns the point's location in the coordinate system the point set was
	 * created with
	 * 
	 * @param index
	 * @return the xy location in the grid coordinate system type z is in
	 *         original coordinate system
	 */
	public abstract double[] getGridLocation(int index);

	/**
	 * returns the point's location in the world coordinate system
	 * 
	 * @param index
	 * @return the xy location in the world coordinate system type and z in the
	 *         point set file's format
	 */
	public abstract double[] getWorldLocation(int index);

	/**
	 * returns the point's location in the index coordinate system
	 * 
	 * @param index
	 * @return the xyz location in the index coordinate system type and z in the
	 *         point set file's format
	 */
	public abstract long[] getIndexLocation(int index);

	/**
	 * returns the point's location in the world coordinate system
	 * 
	 * @param index
	 * @return the xy location in the world coordinate system type and z in the
	 *         point set file's format
	 */
	public abstract long[] getLogicalLocation(int index);

	/**
	 * assumes point is already in the grid coordinate system
	 * 
	 * @param point
	 */
	public abstract void add(IMultiPoint point);

	/**
	 * USE AT YOUR OWN RISK!! Returns the reference to the actual arraylist
	 * 
	 * @return the internal list of Points (not a copy)
	 */
	public abstract ArrayList<IMultiPoint> getPoints();

	public abstract int size();

	public abstract IMultiPoint remove(int index);

}