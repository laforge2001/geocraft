/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.util;


import java.util.ArrayList;
import java.util.List;


/**
 * Experimental implementation of an OctTree. Not very happy 
 * with the implementation but the unit tests suggests it is 
 * giving the right answers. Use at your own risk and modify
 * it very carefully.  
 * 
 * 
 * The OctTree can be in one of four modes:
 * 
 * it is initially created as a parent with no children
 * or
 * it is initially and permanently created as a data point
 * 
 * it can become the parent of between 0 and 8 data points
 * after 8 children are added it becomes the parent of 8 child OctTrees
 */

public class OctTree {

  public static final int MAX_CHILDREN = 32;

  double _xmin;

  double _xmax;

  double _ymin;

  double _ymax;

  double _zmin;

  double _zmax;

  double _data;

  OctTree[] _childNodes;

  int _numDataPoints;

  /**
   * Constructor for an OctTree that is a parent node. 
   * 
   * @param xmin the bounding box that contains all the children of this OctTree. 
   * @param xmax
   * @param ymin
   * @param ymax
   * @param zmin
   * @param zmax
   */
  public OctTree(final double xmin, final double xmax, final double ymin, final double ymax, final double zmin, final double zmax) {
    _xmin = xmin;
    _xmax = xmax;
    _ymin = ymin;
    _ymax = ymax;
    _zmin = zmin;
    _zmax = zmax;
  }

  /**
   * Constructor for an OctTree that contains an actual data point (a Leaf).
   * 
   * @param x the coordinate of this data point. 
   * @param y
   * @param z
   * @param data
   */
  public OctTree(final double x, final double y, final double z, final double data) {
    _xmin = x;
    _ymin = y;
    _zmin = z;
    _data = data;
    _childNodes = new OctTree[0];
  }

  /**
   * If the OctTree contains just a data point then this is a leaf. 
   * 
   * @return true if the OctTree does not contain child OctTrees
   */
  public boolean isLeaf() {
    return _childNodes != null && _childNodes.length == 0;
  }

  /**
   * When searching for points we need to back up to the grandparent
   * to ensure we search all of the potential bins. 
   * @return
   */
  public boolean isGrandParent() {
    return _numDataPoints == -1;
  }

  /**
   * Add this new OctTree element into this OctTree.
   * 
   * You need to have already done a search to find which one to add to. 
   * 
   * @param oct
   */
  public void add(final OctTree oct) {

    if (isLeaf() || isGrandParent()) {
      throw new IllegalStateException("Cannot add a node " + oct + " to this leaf or grandparent. " + this);
    }

    // if this is a parent with no children yet then allocate memory
    if (_childNodes == null) {
      _childNodes = new OctTree[MAX_CHILDREN];
      _numDataPoints = 0;
    }

    if (_numDataPoints < MAX_CHILDREN) {
      _childNodes[_numDataPoints++] = oct;
    } else if (_numDataPoints == MAX_CHILDREN) {
      insertLayer();
      // search for the newly created parent of this oct
      findParent(oct).add(oct);
    }

  }

  /**
   * Recursively searches from this OctTree downwards for the parent
   * of the specified OctTree. 
   * 
   * @param oct that you want to find the parent of. 
   * @return
   */
  public OctTree findParent(final OctTree oct) {

    //System.out.println("Looking for parent of " + oct + " in " + this);

    // check if we are a container but not a Grand Parent
    if (_childNodes == null && contains(oct)) {
      //System.out.println(" Found empty parent " + this);
      return this;
    }

    // check if we are a container with data points in it
    if (_childNodes[0].isLeaf() && contains(oct)) {
      //System.out.println(" Found parent " + this);
      return this;
    }

    for (OctTree child : _childNodes) {
      //System.out.println("  Checking " + child);
      // first check if the target oct lies in this container
      // no need to recurse through a region that cannot contain the point. 
      if (child.contains(oct)) {
        // keep looking
        if (child.isGrandParent()) {
          OctTree candidate = child.findParent(oct);
          if (candidate != null) {
            return candidate;
          }
        }

        return child;
      }
    }

    return null;
  }

  /**
   * Recursively find all the data OctTrees contained beneath
   * the specified OctTree. 
   * 
   * @param oct
   * @return
   */
  public List<OctTree> findChildren() {
    List<OctTree> results = new ArrayList<OctTree>();

    if (_childNodes == null) {
      return results;
    }

    for (OctTree child : _childNodes) {

      if (child == null) {
        continue;
      } else if (child.isLeaf()) {
        results.add(child);
      } else {
        results.addAll(child.findChildren());
      }
    }

    return results;
  }

  boolean contains(final OctTree oct) {

    // if this is a leaf it cannot contain a child OctTree
    if (isLeaf()) {
      return false;
    }

    double x = oct._xmin;
    double y = oct._ymin;
    double z = oct._zmin;

    // if it is a leaf check it is inside or on the border 
    if (oct.isLeaf()) {
      if (!(x >= _xmin && x < _xmax && y >= _ymin && y < _ymax && z >= _zmin && z < _zmax)) {
        return false;
      }
      return true;
    }

    // check that the OctTree is completely inside the bounds

    if (!(x >= _xmin && x <= _xmax && y >= _ymin && y <= _ymax && z >= _zmin && z <= _zmax)) {
      return false;
    }

    x = oct._xmax;
    y = oct._ymax;
    z = oct._zmax;

    if (!(x > _xmin && x <= _xmax && y >= _ymin && y <= _ymax && z >= _zmin && z <= _zmax)) {
      return false;
    }

    // now check if they are exactly the same area 
    if (oct._xmin == _xmin && oct._xmax == _xmax && oct._ymin == _ymin && oct._ymax == _ymax && oct._zmin == _zmin
        && oct._zmax == _zmax) {
      return false;
    }

    return true;
  }

  public void insertLayer() {

    //System.out.println("Inserting a layer");
    // make a copy of the data points
    OctTree[] dataPoints = _childNodes;

    // create a new array for the child nodes
    _childNodes = new OctTree[8];

    // determine the bounding box of the new children OctTrees
    double x2 = (_xmin + _xmax) / 2;
    double y2 = (_ymin + _ymax) / 2;
    double z2 = (_zmin + _zmax) / 2;

    _childNodes[0] = new OctTree(_xmin, x2, y2, _ymax, _zmin, z2);
    _childNodes[1] = new OctTree(x2, _xmax, y2, _ymax, _zmin, z2);
    _childNodes[2] = new OctTree(_xmin, x2, _ymin, y2, _zmin, z2);
    _childNodes[3] = new OctTree(x2, _xmax, _ymin, y2, _zmin, z2);

    _childNodes[4] = new OctTree(_xmin, x2, y2, _ymax, z2, _zmax);
    _childNodes[5] = new OctTree(x2, _xmax, y2, _ymax, z2, _zmax);
    _childNodes[6] = new OctTree(_xmin, x2, _ymin, y2, z2, _zmax);
    _childNodes[7] = new OctTree(x2, _xmax, _ymin, y2, z2, _zmax);

    // the 8 randomly located points now need to be assigned to 
    // their new parent OctTrees. 
    for (OctTree oct : dataPoints) {
      findParent(oct).add(oct);
    }

    // so we can tell that this OctTree is now a grandparent
    _numDataPoints = -1;

    //System.out.println("Finished inserting a layer");
  }

  public int getNumChildren() {
    return _numDataPoints;
  }

  public double getData() {
    return _data;
  }

  public OctTree search(double x, double y, double z) {
    OctTree oct = new OctTree(x, y, z, 0);

    if (!contains(oct)) {
      throw new IllegalArgumentException("The search point must be in the bounds of the original OctTree");
    }

    OctTree parent = findParent(oct);

    //System.out.println("Found the parent " + parent);

    // now back up and look for it's parent
    OctTree grandParent = findParent(parent);

    //System.out.println("Search results... ");
    //System.out.println(oct);
    //System.out.println(parent);
    //System.out.println(grandParent);

    // now visit all the siblings because one contains the closest point

    double minDist = Double.MAX_VALUE;
    OctTree result = null;
    List<OctTree> children = grandParent.findChildren();

    for (int i = 0; i < children.size(); i++) {
      OctTree test = children.get(i);
      double dist = distance(oct, test);
      if (dist < minDist) {
        result = test;
        minDist = dist;
      }
    }

    return result;
  }

  public double distance(OctTree pt1, OctTree pt2) {
    double d2 = (pt1._xmin - pt2._xmin) * (pt1._xmin - pt2._xmin);
    d2 += (pt1._ymin - pt2._ymin) * (pt1._ymin - pt2._ymin);
    d2 += (pt1._zmin - pt2._zmin) * (pt1._zmin - pt2._zmin);
    return d2;
  }

  @Override
  public String toString() {
    if (isLeaf()) {
      return _xmin + " " + _ymin + " " + _zmin;
    }

    return _xmin + "-" + _xmax + ", " + _ymin + "-" + _ymax + ", " + _zmin + "-" + _zmax;
  }

  public static void main(final String[] args) {

    OctTree root = new OctTree(0, 1, 3, 4, 7, 8);

    for (int i = 0; i < 100; i++) {
      //System.out.println("===========================" + i);
      OctTree oct = new OctTree(Math.random(), 3 + Math.random(), 7 + Math.random(), i);
      root.findParent(oct).add(oct);
    }

    //System.out.println("=====================================================================");

    long time = System.currentTimeMillis();

    int failed = 0;

    for (int i = 0; i < 100000; i++) {

      double x = Math.random();
      double y = 3 + Math.random();
      double z = 7 + Math.random();

      OctTree oct = root.search(x, y, z);
      if (oct == null) {
        //System.out.println(failed++ + " " + x + " " + y + " " + z);
        oct = root.search(x, y, z);
      }
    }

    //System.out.println(System.currentTimeMillis() - time);
  }
}
