/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.polygon;


import java.beans.PropertyChangeEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.RGB;
import org.geocraft.abavo.polygon.PolygonRegionsModel.PolygonType;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.ColorProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;


public class PolygonModel extends Model {

  public static final String TYPE = "type";

  public static final String EXISTS = "exists";

  public static final String VISIBLE = "visible";

  public static final String ID = "id";

  public static final String TEXT = "name";

  public static final String VALUE = "value";

  public static final String COLOR = "color";

  public static final String X1 = "x1";

  public static final String X2 = "x2";

  public static final String X3 = "x3";

  public static final String Y1 = "y1";

  public static final String Y2 = "y2";

  public static final String Y3 = "y3";

  private EnumProperty<PolygonType> _type;

  private BooleanProperty _exists;

  private BooleanProperty _visible;

  private StringProperty _text;

  private IntegerProperty _id;

  private FloatProperty _value;

  private ColorProperty _color;

  private final List<Point3d> _points;

  private final NumberFormat _formatter;

  public PolygonModel(final PolygonType type, final int id, final float value, final RGB color) {
    _type = addEnumProperty(TYPE, PolygonType.class, type);
    _text = addStringProperty(TEXT, getDefaultName(id));
    _exists = addBooleanProperty(EXISTS, false);
    _visible = addBooleanProperty(VISIBLE, true);
    _id = addIntegerProperty(ID, id);
    _value = addFloatProperty(VALUE, value);
    _color = addColorProperty(COLOR, color);

    _points = Collections.synchronizedList(new ArrayList<Point3d>());

    // Create a number formatter.
    _formatter = NumberFormat.getInstance();
    _formatter.setMaximumFractionDigits(3);
    _formatter.setGroupingUsed(false);
  }

  public PolygonType getType() {
    return _type.get();
  }

  public void setType(final PolygonType type) {
    _type.set(type);
  }

  public boolean getExists() {
    return _exists.get();
  }

  public void setExists(final boolean exists) {
    if (exists != _exists.get()) {
      _points.clear();
    }
    _exists.set(exists);
  }

  public boolean getVisible() {
    return _visible.get();
  }

  public void setVisible(final boolean visible) {
    _visible.set(visible);
  }

  public int getId() {
    return _id.get();
  }

  public void setId(final int id) {
    _id.set(id);
  }

  public String getText() {
    return _text.get();
  }

  public void setText(final String text) {
    _text.set(text);
  }

  public float getValue() {
    return _value.get();
  }

  public void setValue(final float value) {
    _value.set(value);
  }

  public RGB getColor() {
    return _color.get();
  }

  public void setColor(final RGB color) {
    _color.set(color);
  }

  public int getNumPoints() {
    return _points.size();
  }

  public Point3d getPoint(final int index) {
    return _points.get(index);
  }

  public void clearPoints() {
    _points.clear();
    PropertyChangeEvent event = new PropertyChangeEvent(this, "POINTS", null, null);
    propertyChange(event);
  }

  public void addPoint(final int index, final Point3d point) {
    _points.add(index, point);
    PropertyChangeEvent event = new PropertyChangeEvent(this, "POINTS", null, null);
    propertyChange(event);
  }

  public void removePoint(final int index) {
    _points.remove(index);
    PropertyChangeEvent event = new PropertyChangeEvent(this, "POINTS", null, null);
    propertyChange(event);
  }

  public void setPoints(final Point3d[] points) {
    setPoints(points, true);
  }

  public void setPoints(final Point3d[] points, final boolean fireEvent) {
    _points.clear();
    for (Point3d point : points) {
      _points.add(point);
    }
    if (fireEvent) {
      PropertyChangeEvent event = new PropertyChangeEvent(this, "POINTS", null, null);
      propertyChange(event);
    }
  }

  private static String getDefaultName(final int id) {
    String[] names = { "Base I", "Base II", "Base III", "Base IV", "Top I", "Top II", "Top III", "Top IV" };
    int index = (id - 1) / 8;
    return names[index];
  }

  public void validate(IValidation results) {
    // TODO Auto-generated method stub

  }

  public Point3d[] getPoints() {
    Point3d[] points = new Point3d[_points.size()];
    for (int i = 0; i < points.length; i++) {
      points[i] = _points.get(i);
    }
    return points;
  }
}
