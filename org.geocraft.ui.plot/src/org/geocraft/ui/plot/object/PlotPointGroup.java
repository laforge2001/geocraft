/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.ui.plot.object;


import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.swt.graphics.Rectangle;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.plot.attribute.PointProperties;
import org.geocraft.ui.plot.attribute.TextProperties;
import org.geocraft.ui.plot.defs.PointInsertionMode;
import org.geocraft.ui.plot.defs.ShapeType;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;


/**
 * The basic implementation of a plot point group.
 */
public class PlotPointGroup extends PlotShape implements IPlotPointGroup {

  /** The logger. */

  private boolean _rubberband = false;

  protected int _pointInsertionIndex = -1;

  /**
   * Constructs a plot point group.
   */
  public PlotPointGroup() {
    this("");
  }

  /**
   * Constructs a plot point group.
   */
  public PlotPointGroup(final String name) {
    this(name, new TextProperties(), new PointProperties());
  }

  /**
   * Constructs a plot point group.
   * @param name the point group name.
   * @param textProps the text properties.
   * @param pointProps the point properties.
   */
  public PlotPointGroup(final String name, final TextProperties textProps, final PointProperties pointProps) {
    super(ShapeType.POINT_GROUP, name, textProps, pointProps);
    setName(name);
    _isMovable = true;
    _textProperties = new TextProperties(textProps.getFont(), textProps.getColor(), textProps.getAnchor());
    _pointProperties = new PointProperties(pointProps.getStyle(), pointProps.getColor(), pointProps.getSize());
    _points = Collections.synchronizedList(new ArrayList<IPlotPoint>());
    setPrevShape(null);
    setNextShape(null);
  }

  /**
   * Constructs a plot point group.
   * @param name the point group name.
   * @param points the initial points in the point group.
   * @param textProps the text properties.
   * @param pointProps the point properties.
   */
  public PlotPointGroup(final String name, final IPlotPoint[] points, final TextProperties textProps, final PointProperties pointProps) {
    this(name, textProps, pointProps);
    blockUpdate();
    for (IPlotPoint point : points) {
      addPoint(point);
    }
    unblockUpdate();
  }

  /**
   * Constructs a plot point group, copied from a reference shape.
   * @param shape the reference shape to copy.
   */
  public PlotPointGroup(final IPlotShape shape) {
    this(shape.getName(), shape.getTextProperties(), shape.getPointProperties());
    blockUpdate();
    for (IPlotPoint point : _points) {
      addPoint(point);
    }
    unblockUpdate();
    updated();
  }

  public int addPoint(final IPlotPoint point) {
    return addPoint(point, PointInsertionMode.LAST);
  }

  public int addPoint(final IPlotPoint point, final PointInsertionMode mode) {
    IPlotPoint point0 = null;
    IPlotPoint point1 = null;
    double px;
    double nx;
    double fx;
    double lx;
    double py;
    double ny;
    double fy;
    double ly;
    double pz;
    double nz;
    double fz;
    double lz;
    double x = point.getX();
    double y = point.getY();
    double z = point.getZ();
    int size = _points.size();
    int insertionIndex = -1;
    boolean isEmpty = _points.isEmpty();
    if (isEmpty) {
      insertionIndex = 0;
      _points.add(point);
      _xmin = x;
      _xmax = x;
      _ymin = y;
      _ymax = y;
      _zmin = z;
      _zmax = z;
    } else {
      _xmin = Math.min(x, _xmin);
      _xmax = Math.max(x, _xmax);
      _ymin = Math.min(y, _ymin);
      _ymax = Math.max(y, _ymax);
      _zmin = Math.min(z, _zmin);
      _zmax = Math.max(z, _zmax);
      if (mode.equals(PointInsertionMode.FIRST)) {
        insertionIndex = 0;
        try {
          _points.add(insertionIndex, point);
        } catch (ArrayIndexOutOfBoundsException e) {
          ServiceProvider.getLoggingService().getLogger(getClass()).warn(e.getMessage(), e);
        }
      } else if (mode.equals(PointInsertionMode.LAST)) {
        insertionIndex = _points.size();
        _points.add(point);
      } else if (mode.equals(PointInsertionMode.BY_X_VALUE)) {
        size = _points.size();
        for (insertionIndex = 1; insertionIndex < size; insertionIndex++) {
          point0 = _points.get(insertionIndex - 1);
          point1 = _points.get(insertionIndex);
          x = point.getX();
          px = point0.getX();
          nx = point1.getX();
          if (x >= px && x <= nx || x <= px && x >= nx) {
            break;
          }
        }
        if (insertionIndex < size) {
          try {
            _points.add(insertionIndex, point);
          } catch (ArrayIndexOutOfBoundsException e) {
            ServiceProvider.getLoggingService().getLogger(getClass()).warn(e.getMessage(), e);
          }
        } else {
          point0 = _points.get(0);
          point1 = _points.get(_points.size() - 1);
          fx = point0.getX();
          lx = point1.getX();
          if (lx >= fx && x >= lx || lx < fx && x <= lx) {
            insertionIndex = _points.size();
            _points.add(point);
          } else {
            insertionIndex = 0;
            try {
              _points.add(insertionIndex, point);
            } catch (ArrayIndexOutOfBoundsException e) {
              ServiceProvider.getLoggingService().getLogger(getClass()).warn(e.getMessage(), e);
            }
          }
        }
      } else if (mode.equals(PointInsertionMode.BY_Y_VALUE)) {
        size = _points.size();
        for (insertionIndex = 1; insertionIndex < size; insertionIndex++) {
          point0 = _points.get(insertionIndex - 1);
          point1 = _points.get(insertionIndex);
          y = point.getY();
          py = point0.getY();
          ny = point1.getY();
          if (y >= py && y <= ny || y <= py && y >= ny) {
            break;
          }
        }
        if (insertionIndex < size) {
          try {
            _points.add(insertionIndex, point);
          } catch (ArrayIndexOutOfBoundsException e) {
            ServiceProvider.getLoggingService().getLogger(getClass()).warn(e.getMessage(), e);
          }
        } else {
          point0 = _points.get(0);
          point1 = _points.get(_points.size() - 1);
          fy = point0.getY();
          ly = point1.getY();
          if (ly >= fy && y >= ly || ly < fy && y <= ly) {
            insertionIndex = _points.size();
            _points.add(point);
          } else {
            try {
              insertionIndex = 0;
              _points.add(insertionIndex, point);
            } catch (ArrayIndexOutOfBoundsException e) {
              ServiceProvider.getLoggingService().getLogger(getClass()).warn(e.getMessage(), e);
            }
          }
        }
      } else if (mode.equals(PointInsertionMode.BY_Z_VALUE)) {
        size = _points.size();
        for (insertionIndex = 1; insertionIndex < size; insertionIndex++) {
          point0 = _points.get(insertionIndex - 1);
          point1 = _points.get(insertionIndex);
          z = point.getY();
          pz = point0.getY();
          nz = point1.getY();
          if (z >= pz && z <= nz || z <= pz && z >= nz) {
            break;
          }
        }
        if (insertionIndex < size) {
          try {
            _points.add(insertionIndex, point);
          } catch (ArrayIndexOutOfBoundsException e) {
            ServiceProvider.getLoggingService().getLogger(getClass()).warn(e.getMessage(), e);
          }
        } else {
          point0 = _points.get(0);
          point1 = _points.get(_points.size() - 1);
          fz = point0.getZ();
          lz = point1.getZ();
          if (lz >= fz && z >= lz || lz < fz && z <= lz) {
            insertionIndex = _points.size();
            _points.add(point);
          } else {
            try {
              insertionIndex = 0;
              _points.add(insertionIndex, point);
            } catch (ArrayIndexOutOfBoundsException e) {
              ServiceProvider.getLoggingService().getLogger(getClass()).warn(e.getMessage(), e);
            }
          }
        }
      }
    }
    point.setLayer(_group);
    point.setModelSpace(_model);
    point.addPlotPointListener(this);
    point.setShape(this);
    point.setSelected(_isSelected);
    updated();
    return insertionIndex;
  }

  public void removePoint(final IPlotPoint point) {
    if (point == _pointActive) {
      _pointActive = null;
    }
    _points.remove(point);
    point.setShape(null);
    point.removePlotPointListener(this);
    updated();
  }

  public void movePointBy(final IPlotPoint point, final double dx, final double dy) {
    movePointBy(point, dx, dy, 0);
    updated();
  }

  public void movePointBy(final IPlotPoint point, final double dx, final double dy, final double dz) {
    if (point == null) {
      return;
    }
    point.blockUpdate();
    point.moveBy(dx, dy, dz);
    point.unblockUpdate();
    updated();
  }

  public void movePointTo(final IPlotPoint point, final double x, final double y) {
    movePointTo(point, x, y, 0);
  }

  public void movePointTo(final IPlotPoint point, final double x, final double y, final double z) {
    if (point == null) {
      return;
    }
    point.blockUpdate();
    point.moveTo(x, y, z);
    point.unblockUpdate();
    updated();
  }

  @Override
  public void moveBy(final double dx, final double dy) {
    moveBy(dx, dy, 0);
  }

  @Override
  public void moveBy(final double dx, final double dy, final double dz) {
    for (IPlotPoint point : _points) {
      point.blockUpdate();
      movePointBy(point, dx, dy, dz);
      point.unblockUpdate();
    }
    updated();
  }

  @Override
  public void select() {
    super.select();
    int size = _points.size();
    for (int i = 0; i < size; i++) {
      IPlotPoint point = _points.get(i);
      point.blockUpdate();
      point.select();
      point.unblockUpdate();
    }
    updated();
  }

  @Override
  public void deselect() {
    super.deselect();
    int size = _points.size();
    for (int i = 0; i < size; i++) {
      IPlotPoint point = _points.get(i);
      point.blockUpdate();
      point.deselect();
      point.unblockUpdate();
    }
    updated();
  }

  public Rectangle getRectangle(final IModelSpaceCanvas renderer) {
    Rectangle rect = null;
    int ps;
    int pxmin = 0;
    int pxmax = 0;
    int pymin = 0;
    int pymax = 0;
    boolean first = true;
    for (int i = 0; i < getPointCount(); i++) {
      IPlotPoint point = getPoint(i);
      if (point != null) {
        IModelSpace model = point.getLayer().getModelSpace();
        if (model != null) {
          Point2D.Double p = new Point2D.Double(0, 0);
          renderer.transformModelToPixel(getModelSpace(), point.getX(), point.getY(), p);
          ps = point.getPointProperties().getSize();
          if (ps < 0) {
            ps = getPointSize();
          }
          if (ps < 5) {
            ps = 5;
          }
          ps *= 2;
          if (first) {
            first = false;
            pxmin = (int) (p.x - ps);
            pxmax = (int) (p.x + ps);
            pymin = (int) (p.y - ps);
            pymax = (int) (p.y + ps);
          } else {
            pxmin = Math.min(pxmin, (int) (p.x - ps));
            pxmax = Math.max(pxmax, (int) (p.x + ps));
            pymin = Math.min(pymin, (int) (p.y - ps));
            pymax = Math.max(pymax, (int) (p.y + ps));
          }
        }
      }
    }

    if (!first) {
      int width = pxmax - pxmin + 1;
      int height = pymax - pymin + 1;
      rect = new Rectangle(pxmin, pymin, width, height);
      rect.x += 0;
      rect.y += 0;
    }
    return rect;
  }

  public void propertyChange(final PropertyChangeEvent evt) {
    updated();
  }

  public void rubberband(final PointInsertionMode pointInsertionMode, final double x, final double y) {
    if (!_rubberband) {
      return;
    }
    if (_pointInsertionIndex >= 0) {
      if (_pointInsertionIndex == getPointCount() - 1) {
        getPoint(_pointInsertionIndex).moveTo(x, y);
      } else {
        _pointInsertionIndex = Math.min(_pointInsertionIndex, getPointCount() - 1);
        removePoint(getPoint(_pointInsertionIndex));
        _pointInsertionIndex = addPoint(new PlotPoint(x, y, 0), pointInsertionMode);
      }
      return;
    }
    _pointInsertionIndex = addPoint(new PlotPoint(x, y, 0), pointInsertionMode);
  }

  public void rubberbandOn() {
    _rubberband = true;
  }

  public void rubberbandOff() {
    if (_rubberband && _pointInsertionIndex >= 0) {
      _pointInsertionIndex = Math.min(_pointInsertionIndex, getPointCount() - 1);
      removePoint(getPoint(_pointInsertionIndex));
    }
    _pointInsertionIndex = -1;
    _rubberband = false;
  }

}
