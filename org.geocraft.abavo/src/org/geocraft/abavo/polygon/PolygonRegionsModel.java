/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.polygon;


import java.beans.PropertyChangeEvent;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.abavo.crossplot.IABavoCrossplot;
import org.geocraft.abavo.crossplot.action.EndPolygonDefinition;
import org.geocraft.core.color.ColorMapEvent;
import org.geocraft.core.color.ColorMapListener;
import org.geocraft.core.common.xml.XmlIO;
import org.geocraft.core.common.xml.XmlUtils;
import org.geocraft.core.model.IModelListener;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.internal.abavo.ABavoCrossplotRegistry;
import org.geocraft.ui.plot.action.IPlotMouseAction;
import org.geocraft.ui.plot.action.PlotActionMask;
import org.geocraft.ui.plot.action.point.AddPointAction;
import org.geocraft.ui.plot.action.point.DeletePointAction;
import org.geocraft.ui.plot.defs.ActionMaskType;
import org.geocraft.ui.plot.defs.PlotEventType;
import org.geocraft.ui.plot.defs.PointInsertionMode;
import org.geocraft.ui.plot.event.ShapeEvent;
import org.geocraft.ui.plot.listener.IPlotShapeListener;
import org.geocraft.ui.plot.model.ModelSpaceBounds;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Defines the polygon regions model for an AB crossplot. The polygon regions model contains up to 64 polygons,
 * with each polygon having its own assigned value to use for data classification.
 */
public class PolygonRegionsModel extends Model implements ColorMapListener, IPlotShapeListener, XmlIO {

  private static final String GENERAL_TAG = "General";

  private static final String CROSSPLOT_POLYGON_MODEL_TAG = "CrossplotPolygonModel";

  /** Enumeration for the various polygons: 1-64 and Selection. */
  public static enum PolygonType {
    Selection,
    Region
  }

  /** The constant for the number of polygons. */
  public static final int NUMBER_OF_POLYGONS = 64;

  public static final String FILE_EXTENSION = "polygon_model";

  public static final String FILE_EXTENSION_OLD = "plygn";

  public static final String NORMALIZATION_FACTOR = "normalizationFactor";

  public static final String POLYGONS_FILLED = "polygonsFilled";

  public static final String SYMMETRIC_REGIONS = "symmetricRegions";

  public static final String POLYGONS_REGION_MODEL_PREFIX = "PolygonsRegionModel";

  public static final String POLYGON = "polygon";

  public static final String POINT = "point";

  public static final String VISIBLE = "visible";

  public static final String TEXT = "text";

  public static final String VALUE = "value";

  public static final String POINT_COUNT = "pointCount";

  public static final String X = "x";

  public static final String Y = "y";

  private FloatProperty _normalizationFactor;

  private BooleanProperty _symmetricRegions;

  private BooleanProperty _polygonsFilled;

  private final PolygonModel[] _polygonModels;

  private final Set<PolygonRegionsModelListener> _listeners;

  private boolean _updateBlocked;

  private boolean _rescalePolygons;

  private final Object _lock = new Object();

  /**
   * The default constructor.
   */
  public PolygonRegionsModel() {
    _normalizationFactor = addFloatProperty(NORMALIZATION_FACTOR, 128);
    _symmetricRegions = addBooleanProperty(SYMMETRIC_REGIONS, false);
    _polygonsFilled = addBooleanProperty(POLYGONS_FILLED, false);
    unblockUpdate();
    _listeners = Collections.synchronizedSet(new HashSet<PolygonRegionsModelListener>());
    _polygonModels = new PolygonModel[NUMBER_OF_POLYGONS];
    for (int i = 0; i < NUMBER_OF_POLYGONS; i++) {
      RGB color = new RGB(0, 0, 255);
      final int id = i + 1;
      int value = -126 + i * 4;
      _polygonModels[i] = new PolygonModel(PolygonType.Region, id, value, color);
      _polygonModels[i].addListener(new IModelListener() {

        @Override
        public void propertyChanged(String key) {
          int index = id - 1;
          if (key.equals(PolygonModel.EXISTS)) {
            if (_polygonModels[index].getExists()) {
              updated(PolygonRegionsModelEvent.Type.PolygonCreated, new int[] { index });
            } else {
              updated(PolygonRegionsModelEvent.Type.PolygonDeleted, new int[] { index });
            }
          } else {
            updatedPolygons(new int[] { index });
          }
        }
      });
    }
  }

  /**
   * Returns the update blocked status of the polygon model.
   * @return true if the model is blocked from updates; false if not.
   */
  public boolean isUpdateBlocked() {
    return _updateBlocked;
  }

  /**
   * Blocks the polygon model from updates.
   */
  public void blockUpdate() {
    _updateBlocked = true;
  }

  /**
   * Unblocks the polygon model from updates.
   */
  public void unblockUpdate() {
    _updateBlocked = false;
  }

  /**
   * Returns the symmetric status of the polygon model.
   * @return true if symmetry is active; false if not.
   */
  //public boolean getSymmetricRegions() {
  //  return _symmetricRegions;
  //}
  /**
   * Sets the symmetric status of the polygon model.
   * @param symmetric true for symmetric; otherwise false.
   */
  //public void setSymmetricRegions(final boolean symmetryLock) {
  //  firePropertyChange(SYMMETRIC_REGIONS, _symmetricRegions, _symmetricRegions = symmetryLock);
  //  updatedRegionSymmetry();
  //}
  public boolean getPolygonsFilled() {
    return _polygonsFilled.get();
  }

  public void setPolygonsFilled(final boolean filled) {
    _polygonsFilled.set(filled);
    updatedPolygons(getAllPolygonIndices());
  }

  public RGB getPolygonColor(final int index) {
    return _polygonModels[index].getColor();
  }

  public RGB[] getPolygonColors() {
    RGB[] colors = new RGB[NUMBER_OF_POLYGONS];
    for (int index = 0; index < NUMBER_OF_POLYGONS; index++) {
      colors[index] = _polygonModels[index].getColor();
    }
    return colors;
  }

  public void setPolygonColors(final RGB[] colors) {
    if (colors.length != 64) {
      throw new IllegalArgumentException("Invalid # of colors (must be == 64).");
    }
    synchronized (_lock) {
      blockUpdate();
      for (int index = 0; index < NUMBER_OF_POLYGONS; index++) {
        _polygonModels[index].setColor(colors[NUMBER_OF_POLYGONS - 1 - index]);
      }
      unblockUpdate();
    }
    updatedPolygons(getAllPolygonIndices());
  }

  public void setPolygonColor(final int index, final RGB color) {
    _polygonModels[index].setColor(color);
  }

  public float getNormalizationFactor() {
    return _normalizationFactor.get();
  }

  public void setNormalizationFactor(final float normalizationFactor) {
    _normalizationFactor.set(normalizationFactor);
    updatePolygonValues();
    updatedPolygons(getAllPolygonIndices());
  }

  public boolean getSymmetricRegions() {
    return _symmetricRegions.get();
  }

  public void setSymmetricRegions(boolean symmetricRegions) {
    _symmetricRegions.set(symmetricRegions);
  }

  public boolean isPolygonVisible(final int index) {
    return _polygonModels[index].getVisible();
  }

  public void setPolygonVisible(final int index, final boolean visible) {
    _polygonModels[index].setVisible(visible);
  }

  public void setPolygonsVisible(final boolean[] visibles) {
    synchronized (_lock) {
      blockUpdate();
      for (int index = 0; index < NUMBER_OF_POLYGONS; index++) {
        _polygonModels[index].setVisible(visibles[index]);
      }
      unblockUpdate();
    }
    updatedPolygons(getAllPolygonIndices());
  }

  public String getPolygonText(final int index) {
    return _polygonModels[index].getText();
  }

  public void setPolygonText(final int index, final String text) {
    _polygonModels[index].setText(text);
  }

  public float getPolygonValue(final int index) {
    return _polygonModels[index].getValue();
  }

  public float[] getPolygonValues() {
    float[] polygonValues = new float[_polygonModels.length];
    for (int i = 0; i < NUMBER_OF_POLYGONS; i++) {
      polygonValues[i] = _polygonModels[i].getValue();
    }
    return polygonValues;
  }

  public void setPolygonValue(final int index, final float value) {
    _polygonModels[index].setValue(value);
  }

  /**
   * Updates the polygon values, based on the polygon normalization factor.
   */
  public void updatePolygonValues() {
    for (int i = 0; i < NUMBER_OF_POLYGONS; i++) {
      float numPolygons2 = NUMBER_OF_POLYGONS / 2;
      float value = (i - numPolygons2 + 0.5f) * _normalizationFactor.get() / numPolygons2;
      setPolygonValue(i, value);
    }
  }

  /**
   * Returns an array of all the polygon indices.
   * @return an array of all the polygon indices.
   */
  private int[] getAllPolygonIndices() {
    int[] indices = new int[NUMBER_OF_POLYGONS];
    for (int i = 0; i < NUMBER_OF_POLYGONS; i++) {
      indices[i] = i;
    }
    return indices;
  }

  /**
   * Returns the array of polygon models.
   * @return the array of polygon models.
   */
  public PolygonModel[] getPolygonModels() {
    return _polygonModels;
  }

  /**
   * Returns the specified polygon model.
   * @param index the index of the polygon model.
   */
  public PolygonModel getPolygonModel(final int index) {
    return _polygonModels[index];
  }

  /**
   * Invoked when the model is updated.
   */
  public void updated(final PolygonRegionsModelEvent.Type type, final int[] polygonIndices) {
    if (!isUpdateBlocked()) {
      firePolygonModelEvent(new PolygonRegionsModelEvent(type, this, polygonIndices));
    }
  }

  /**
   * Invoked when one or more of the polygons is updated.
   */
  private void updatedPolygons(final int[] polygonIndices) {
    updated(PolygonRegionsModelEvent.Type.PolygonsUpdated, polygonIndices);
  }

  /**
   * Invoked when the region symmetry is turned on/off.
   */
  private void updatedRegionSymmetry() {
    updated(PolygonRegionsModelEvent.Type.RegionSymmetryUpdated, getAllPolygonIndices());
  }

  /**
   * Adds a polygon model listener.
   * @param listener the listener to add.
   */
  public void addPolygonModelListener(final PolygonRegionsModelListener listener) {
    _listeners.add(listener);
  }

  /**
   * Removes a polygon model listener.
   * @param listener the listener to remove.
   */
  public void removePolygonModelListener(final PolygonRegionsModelListener listener) {
    _listeners.remove(listener);
  }

  /**
   * Fires an AB polygon model event to the listeners.
   * @param event the event to fire.
   */
  private void firePolygonModelEvent(final PolygonRegionsModelEvent event) {
    for (PolygonRegionsModelListener listener : _listeners.toArray(new PolygonRegionsModelListener[0])) {
      listener.polygonModelUpdated(event);
    }
  }

  public void shapeUpdated(final ShapeEvent event) {
    if (!event.getEventType().equals(PlotEventType.SHAPE_DESELECTED)) {
      return;
    }
    //    for (int i = 0; i < NUMBER_OF_POLYGONS; i++) {
    //      if (event.getShape().equals(_polygons[i])) {
    //        if (getSymmetryLock()) {
    //          int symmetricIndex = NUMBER_OF_POLYGONS - 1 - i;
    //          _polygons[symmetricIndex].removeShapeListener(this);
    //          _polygons[symmetricIndex].clear();
    //          for (int j = 0; j < _polygons[i].getPointCount(); j++) {
    //            IPlotPoint point = _polygons[i].getPoint(j);
    //            _polygons[symmetricIndex].addPoint(new PlotPoint(-point.getX(), -point.getY(), point.getZ()));
    //          }
    //          _polygons[symmetricIndex].setVisible(_polygons[i].isVisible());
    //          _polygons[symmetricIndex].addShapeListener(this);
    //          updated();
    //        }
    //        break;
    //      }
    //    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    super.propertyChange(event);
    updatedPolygons(getAllPolygonIndices());
  }

  public static IPlotMouseAction[] getPolygonMouseActions(final IABavoCrossplot crossplot, final int polygonIndex,
      boolean symmetry) {

    // Create an empty list of plot mouse actions. */
    List<IPlotMouseAction> actions = new ArrayList<IPlotMouseAction>();

    PlotActionMask mask;

    // Point add: mouse button#1 down
    mask = new PlotActionMask(ActionMaskType.MOUSE_DOWN, 1, 1, 0);
    actions.add(new AddPointAction(mask, PointInsertionMode.LAST));
    // Point delete: mouse button#2 down with shift
    mask = new PlotActionMask(ActionMaskType.MOUSE_DOWN, 2, 1, SWT.SHIFT);
    actions.add(new DeletePointAction(mask));

    // PointGroup deselect: mouse button #2 down
    PolygonType polygonType = PolygonType.Region;
    if (polygonIndex < 0 || polygonIndex >= PolygonRegionsModel.NUMBER_OF_POLYGONS) {
      polygonType = PolygonType.Selection;
    }
    actions.add(new EndPolygonDefinition(crossplot, polygonType, polygonIndex, symmetry));
    return actions.toArray(new IPlotMouseAction[0]);
  }

  public void colorsChanged(final ColorMapEvent event) {
    RGB[] colors = event.getColorMapModel().getColors();
    if (colors == null || colors.length != NUMBER_OF_POLYGONS) {
      throw new IllegalArgumentException("Number of colors must equal 64.");
    }
    blockUpdate();
    for (int i = 0; i < NUMBER_OF_POLYGONS; i++) {
      _polygonModels[i].setColor(colors[NUMBER_OF_POLYGONS - 1 - i]);
    }
    unblockUpdate();
    updatedPolygons(getAllPolygonIndices());
  }

  public void updateModel(final PolygonRegionsModel model) {
    setNormalizationFactor(model.getNormalizationFactor());
    setPolygonsFilled(model.getPolygonsFilled());
    setSymmetricRegions(model.getSymmetricRegions());
    for (int i = 0; i < NUMBER_OF_POLYGONS; i++) {
      PolygonModel polygonModel = getPolygonModel(i);
      PolygonModel polygonModelIn = model.getPolygonModel(i);
      polygonModel.setVisible(polygonModelIn.getVisible());
      polygonModel.setText(polygonModelIn.getText());
      polygonModel.setValue(polygonModelIn.getValue());
      polygonModel.setExists(polygonModelIn.getExists());
      polygonModel.clearPoints();
      for (int j = 0; j < polygonModelIn.getNumPoints(); j++) {
        Point3d point = polygonModelIn.getPoint(j);
        polygonModel.addPoint(j, new Point3d(point));
      }
    }
  }

  public void readSession(final File file, final boolean rescalePolygons) throws Exception {
    synchronized (_lock) {
      blockUpdate();
      _rescalePolygons = rescalePolygons;
      XmlUtils.readXML(file, this);
      Properties properties = new Properties();
      try {
        if (!file.exists()) {
          ServiceProvider.getLoggingService().getLogger(getClass())
              .error("File \'" + file.getAbsolutePath() + "\' does not exist.");
        }
        FileInputStream istream = new FileInputStream(file);
        properties.load(istream);
      } catch (Exception ex) {
        ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.toString(), ex);
        return;
      }
      _rescalePolygons = false;
      unblockUpdate();
    }
    updatedRegionSymmetry();
    updatedPolygons(getAllPolygonIndices());
  }

  //  public static void main(String[] args) throws Exception {
  //    File file = new File("/home/walucas/olderf.plygn");
  //    readSessionOld(file, false);
  //  }

  public void readSessionOld(final File file, final boolean rescalePolygons) throws Exception {
    synchronized (_lock) {
      blockUpdate();
      _rescalePolygons = rescalePolygons;
      FileInputStream istream = new FileInputStream(file);
      DataInputStream dstream = new DataInputStream(istream);
      boolean swapIt = false;
      float[] y = new float[2];
      float[] x = { 1f, 1f };
      int id = dstream.readInt();
      if (id < 1 || id > 20) {
        id = byteSwapInteger(id);
        swapIt = true;
      }
      if (id < 1 || id > 20) {
        // Very old polygon file.
        dstream.close();
        dstream = new DataInputStream(istream);
      } else {
        y[0] = dstream.readFloat();
        y[1] = dstream.readFloat();
        if (swapIt) {
          byteSwapFloatArray(y);
        }
        if (rescalePolygons) {
          ModelSpaceBounds bounds = ABavoCrossplotRegistry.get().getCrossplots()[0].getActiveModelSpace()
              .getDefaultBounds();
          x[0] = (float) bounds.getEndX();
          x[1] = (float) bounds.getEndY();
          x[0] /= y[0];
          x[1] /= y[1];
        }
      }

      OldPolygonsStructure poly = new OldPolygonsStructure();
      poly.read(dstream);
      if (swapIt) {
        for (int i = 0; i < 64; i++) {
          poly.p[i].defined = byteSwapInteger(poly.p[i].defined);
          poly.p[i].value = byteSwapFloat(poly.p[i].value);
          poly.p[i].ptcnt = byteSwapInteger(poly.p[i].ptcnt);
        }
        poly.pordcnt = byteSwapInteger(poly.pordcnt);
        byteSwapIntegerArray(poly.pord);
        poly.fill = byteSwapInteger(poly.fill);
        poly.sym_lock = byteSwapInteger(poly.sym_lock);
        poly.pnf = byteSwapFloat(poly.pnf);
        byteSwapIntegerArray(poly.pad);
      }
      char[][] temp = new char[1][];
      cread(dstream, temp, 0, swapIt);
      poly.colorfile = temp[0];
      for (int i = 0; i < 64; i++) {
        if (poly.p[i].defined != 0) {
          poly.p[i].pts = new float[poly.p[i].ptcnt];
          for (int j = 0; j < poly.p[i].ptcnt / 2; j++) {
            poly.p[i].pts[j * 2 + 0] = dstream.readFloat();
            poly.p[i].pts[j * 2 + 1] = dstream.readFloat();
          }
          if (swapIt) {
            byteSwapFloatArray(poly.p[i].pts);
          }
          for (int j = 0; j < poly.p[i].ptcnt / 2; j++) {
            poly.p[i].pts[j * 2 + 0] *= x[0];
            poly.p[i].pts[j * 2 + 1] *= x[1];
          }
        }
        // TODO: why set values based on x,y bounds?? .p[i].value *= x[0];
      }
      // TODO: why set scalar based on x,y bounds?? poly.pnf *= x[0];
      dstream.close();
      _rescalePolygons = false;
      for (int i = 0; i < NUMBER_OF_POLYGONS; i++) {
        PolygonModel pModel = getPolygonModel(NUMBER_OF_POLYGONS - 1 - i);
        pModel.setExists(poly.p[i].defined != 0);
        pModel.setValue(poly.p[i].value);
        Point3d[] points = new Point3d[0];
        if (poly.p[i].defined != 0) {
          int npts = poly.p[i].ptcnt / 2;
          points = new Point3d[npts];
          for (int j = 0; j < npts; j++) {
            points[j] = new Point3d(poly.p[i].pts[j * 2 + 0], poly.p[i].pts[j * 2 + 1], 0);
          }
        }
        pModel.setPoints(points);
      }
      setPolygonsFilled(poly.fill != 0);
      setSymmetricRegions(poly.sym_lock != 0);
      //setNormalizationFactor(poly.pnf);
      unblockUpdate();
    }
    updatedRegionSymmetry();
    updatedPolygons(getAllPolygonIndices());
  }

  private static void cread(final DataInputStream dstream, char[][] temp, int err, final boolean swapIt) throws IOException {
    temp[0] = null;
    int len = dstream.readInt();
    if (swapIt) {
      len = byteSwapInteger(len);
    }
    if (len > 0) {
      temp[0] = new char[len];
      byte[] bytes = new byte[4];
      int e = dstream.read(bytes);
      if (e < len) {
        throw new IOException("Error: Reading string");
      }
      for (int i = 0; i < len; i++) {
        temp[0][i] = (char) bytes[i];
      }
    }
  }

  private static float byteSwapFloat(float value) {
    int intValue = Float.floatToIntBits(value);
    intValue = byteSwapInteger(intValue);
    return Float.intBitsToFloat(intValue);
  }

  private static int byteSwapInteger(int value) {
    int b1 = value >> 0 & 0xff;
    int b2 = value >> 8 & 0xff;
    int b3 = value >> 16 & 0xff;
    int b4 = value >> 23 & 0xff;
    return b1 << 24 | b2 << 16 | b3 << 8 | b4 << 0;
  }

  private static void byteSwapFloatArray(float[] values) {
    for (int i = 0; i < values.length; i++) {
      values[i] = byteSwapFloat(values[i]);
    }
  }

  private static void byteSwapIntegerArray(int[] values) {
    for (int i = 0; i < values.length; i++) {
      values[i] = byteSwapInteger(values[i]);
    }
  }

  public void writeSession(final File file) throws Exception {
    XmlUtils.writeXML(file, this);
  }

  @Override
  public void getXML(final Document doc, final Node parent) throws Exception {

    if (doc == null) {
      throw new Exception("Error: No XML document specified.");
    }

    // Create the node for PolygonModel.
    Node nodePolygonModel = XmlUtils.addElement(doc, parent, CROSSPLOT_POLYGON_MODEL_TAG);

    // Get general polygon properties.
    Properties properties = new Properties();
    getGeneralProperties(properties);
    Element nodeGeneral = XmlUtils.addElement(doc, nodePolygonModel, GENERAL_TAG);
    for (Object key : properties.keySet()) {
      Object obj = properties.get(key);
      XmlUtils.addAttribute(doc, nodeGeneral, key.toString(), obj.toString());
    }

    // Get polygon properties.
    for (int index = 0; index < NUMBER_OF_POLYGONS; index++) {
      int id = index + 1;
      properties.clear();
      getPolygonProperties(index, properties);
      if (!properties.isEmpty()) {
        Element nodePolygon = XmlUtils.addElement(doc, nodePolygonModel, "Polygon" + id);
        for (Object key : properties.keySet()) {
          Object obj = properties.get(key);
          XmlUtils.addAttribute(doc, nodePolygon, key.toString(), obj.toString());
        }
      }
    }
  }

  @Override
  public void setXML(final Document doc, final Node parent) throws Exception {

    if (doc == null) {
      throw new Exception("Error: No XML document specified.");
    }

    Element nodePolygonModel = null;

    NodeList nodeList = doc.getElementsByTagName(CROSSPLOT_POLYGON_MODEL_TAG);
    int nodeCount = nodeList.getLength();
    if (nodeCount == 1) {
      nodePolygonModel = (Element) nodeList.item(0);
    } else if (nodeCount > 1) {
      throw new Exception("Error: Multiple " + CROSSPLOT_POLYGON_MODEL_TAG + " elements found.");
    }

    // Add the PolygonModel node.
    if (nodePolygonModel == null) {
      throw new Exception("Error: No " + CROSSPLOT_POLYGON_MODEL_TAG + " element exists.");
    }

    // Set general polygon/region properties.
    Properties properties = new Properties();
    Element nodeGeneral = null;
    NodeList nodesGeneral = nodePolygonModel.getElementsByTagName(GENERAL_TAG);
    if (nodesGeneral != null && nodesGeneral.getLength() > 0) {
      nodeGeneral = (Element) nodesGeneral.item(0);
      NamedNodeMap map = nodeGeneral.getAttributes();
      for (int i = 0; i < map.getLength(); i++) {
        Attr attr = (Attr) map.item(i);
        properties.put(attr.getName(), attr.getValue());
      }
    }
    double[] xyBounds = setGeneralProperties(properties);
    properties.clear();

    // Set polygon properties.
    for (int index = 0; index < NUMBER_OF_POLYGONS; index++) {
      int id = index + 1;
      Element nodePolygon = null;
      NodeList nodesPolygon = nodePolygonModel.getElementsByTagName("Polygon" + id);
      if (nodesPolygon != null && nodesPolygon.getLength() > 0) {
        nodePolygon = (Element) nodesPolygon.item(0);
        NamedNodeMap map = nodePolygon.getAttributes();
        for (int i = 0; i < map.getLength(); i++) {
          Attr attr = (Attr) map.item(i);
          properties.put(attr.getName(), attr.getValue());
        }
      }
      setPolygonProperties(index, properties, xyBounds, _rescalePolygons);
      properties.clear();
    }

    return;
  }

  /**
   * Gets the polygon model general properties.
   * @param properties the polygon model general properties.
   * @return the polygon model general properties.
   * @exception Exception thrown on properties error.
   */
  private Properties getGeneralProperties(final Properties properties) throws Exception {
    ModelSpaceBounds bounds = ABavoCrossplotRegistry.get().getCrossplots()[0].getActiveModelSpace().getDefaultBounds();
    properties.setProperty("StartX", Double.toString(bounds.getStartX()));
    properties.setProperty("EndX", Double.toString(bounds.getEndX()));
    properties.setProperty("StartY", Double.toString(bounds.getStartY()));
    properties.setProperty("EndY", Double.toString(bounds.getEndY()));
    properties.setProperty("NormalizationFactor", Float.toString(getNormalizationFactor()));
    properties.setProperty("PolygonsFilled", Boolean.toString(getPolygonsFilled()));
    properties.setProperty("SymmetricRegions", Boolean.toString(Boolean.FALSE));
    properties.setProperty("NumberOfPolygons", Integer.toString(NUMBER_OF_POLYGONS));
    return properties;
  }

  /**
   * Sets the polygon model general properties.
   * @param properties the polygon model general properties.
   * @exception Exception thrown on properties error.
   */
  private double[] setGeneralProperties(final Properties properties) throws Exception {

    String value = properties.getProperty("NormalizationFactor");
    if (value != null) {
      setNormalizationFactor(Float.parseFloat(value));
    }
    value = properties.getProperty("PolygonsFilled");
    if (value != null) {
      setPolygonsFilled(Boolean.parseBoolean(value));
    }
    value = properties.getProperty("SymmetricRegions");
    if (value != null) {
      // TODO: setSymmetric(Boolean.parseBoolean(value));
    }
    value = properties.getProperty("NumberOfPolygons");
    if (value != null) {
      int numberOfPolygons = Integer.parseInt(value);
      if (numberOfPolygons != NUMBER_OF_POLYGONS) {
        throw new Exception("The number of polygons must equal " + NUMBER_OF_POLYGONS);
      }
    }
    updatePolygonValues();
    updatedRegionSymmetry();

    String startX = properties.getProperty("StartX");
    String endX = properties.getProperty("EndX");
    String startY = properties.getProperty("StartY");
    String endY = properties.getProperty("EndY");
    if (startX != null && endX != null && startY != null && endY != null) {
      double xStart = Double.parseDouble(startX);
      double xEnd = Double.parseDouble(endX);
      double yStart = Double.parseDouble(startY);
      double yEnd = Double.parseDouble(endY);
      return new double[] { xStart, xEnd, yStart, yEnd };
    }

    return new double[0];
  }

  /**
   * Gets the properties of the specified polygon.
   * @param index the index of the polygon to get properties.
   * @param properties the properties of the specified polygon.
   * @return the properties of the specified polygon.
   * @exception Exception thrown on properties error.
   */
  private Properties getPolygonProperties(final int index, final Properties properties) throws Exception {
    PolygonModel model = getPolygonModel(index);
    properties.setProperty("visible", Boolean.toString(model.getVisible()));
    properties.setProperty("text", model.getText());
    properties.setProperty("value", Double.toString(model.getValue()));
    properties.setProperty("pointCount", Integer.toString(model.getNumPoints()));
    for (int i = 0; i < model.getNumPoints(); i++) {
      Point3d point = model.getPoint(i);
      properties.setProperty("x" + (i + 1), Double.toString(point.getX()));
      properties.setProperty("y" + (i + 1), Double.toString(point.getY()));
    }
    return properties;
  }

  /**
   * Sets the properties of the specified polygon.
   * @param index the index of the polygon to set properties.
   * @param properties the properties of the specified polygon.
   * @param rescalePolygons 
   * @exception Exception thrown on properties error.
   */
  private void setPolygonProperties(final int index, final Properties properties, double[] xyBounds,
      final boolean rescalePolygons) throws Exception {
    int pointCount = 0;

    String value = properties.getProperty("visible");
    if (value != null) {
      setPolygonVisible(index, Boolean.parseBoolean(value));
    }
    value = properties.getProperty("text");
    if (value != null) {
      setPolygonText(index, value);
    }
    value = properties.getProperty("value");
    if (value != null) {
      setPolygonValue(index, Float.parseFloat(value));
    }
    value = properties.getProperty("pointCount");
    if (value != null) {
      pointCount = Integer.parseInt(value);
    }

    double[] x = { 0 };
    double[] y = { 0 };
    if (pointCount > 0) {
      x = new double[pointCount];
      y = new double[pointCount];
    }
    int actualCount = 0;
    for (int i = 0; i < pointCount; i++) {
      String valueX = properties.getProperty("x" + (i + 1));
      String valueY = properties.getProperty("y" + (i + 1));
      if (valueX != null && valueY != null) {
        x[actualCount] = Double.parseDouble(valueX);
        y[actualCount] = Double.parseDouble(valueY);

        // Scale the x,y values, but only if the bounds were stored and recovered.
        if (xyBounds.length == 4 && rescalePolygons) {
          x[actualCount] = scaleX(x[actualCount], xyBounds[0], xyBounds[1]);
          y[actualCount] = scaleY(y[actualCount], xyBounds[2], xyBounds[3]);
        }
        actualCount++;
      }
    }
    pointCount = actualCount;

    PolygonModel model = getPolygonModel(index);
    model.setExists(pointCount > 0);
    Point3d[] points = new Point3d[pointCount];
    for (int i = 0; i < pointCount; i++) {
      points[i] = new Point3d(x[i], y[i], 0);
    }
    model.setPoints(points);
  }

  /**
   * Scales the polygon x value based on the current default bounds of the crossplot.
   * @param x the stored polygon x value.
   * @param xStart the stored starting x value.
   * @param xEnd the stored ending x value.
   * @return the scaled x value.
   */
  private double scaleX(double x, double xStart, double xEnd) {
    ModelSpaceBounds bounds = ABavoCrossplotRegistry.get().getCrossplots()[0].getActiveModelSpace().getDefaultBounds();
    double percent = (x - xStart) / (xEnd - xStart);
    return bounds.getStartX() + percent * (bounds.getEndX() - bounds.getStartX());
  }

  /**
   * Scales the polygon y value based on the current default bounds of the crossplot.
   * @param y the stored polygon y value.
   * @param yStart the stored starting y value.
   * @param yEnd the stored ending y value.
   * @return the scaled y value.
   */
  private double scaleY(double y, double yStart, double yEnd) {
    ModelSpaceBounds bounds = ABavoCrossplotRegistry.get().getCrossplots()[0].getActiveModelSpace().getDefaultBounds();
    double percent = (y - yStart) / (yEnd - yStart);
    return bounds.getStartY() + percent * (bounds.getEndY() - bounds.getStartY());
  }

  public void validate(IValidation results) {
    // TODO Auto-generated method stub

  }

  @Override
  public Map<String, String> pickle() {
    // A fully custom pickle method, since this is a non-standard model.
    Map<String, String> map = new HashMap<String, String>();
    map.put(POLYGONS_REGION_MODEL_PREFIX + " " + SYMMETRIC_REGIONS, Boolean.toString(getSymmetricRegions()));
    map.put(POLYGONS_REGION_MODEL_PREFIX + " " + POLYGONS_FILLED, Boolean.toString(getPolygonsFilled()));
    map.put(POLYGONS_REGION_MODEL_PREFIX + " " + NORMALIZATION_FACTOR, Float.toString(getNormalizationFactor()));
    try {
      for (int index = 0; index < NUMBER_OF_POLYGONS; index++) {
        PolygonModel model = getPolygonModel(index);
        map.put(POLYGONS_REGION_MODEL_PREFIX + " " + POLYGON + index + " " + VISIBLE,
            Boolean.toString(model.getVisible()));
        map.put(POLYGONS_REGION_MODEL_PREFIX + " " + POLYGON + index + " " + TEXT, model.getText());
        map.put(POLYGONS_REGION_MODEL_PREFIX + " " + POLYGON + index + " " + VALUE, Double.toString(model.getValue()));
        map.put(POLYGONS_REGION_MODEL_PREFIX + " " + POLYGON + index + " " + POINT_COUNT,
            Integer.toString(model.getNumPoints()));
        for (int i = 0; i < model.getNumPoints(); i++) {
          Point3d point = model.getPoint(i);
          map.put(POLYGONS_REGION_MODEL_PREFIX + " " + POLYGON + index + " " + POINT + (i + 1) + " " + X,
              Double.toString(point.getX()));
          map.put(POLYGONS_REGION_MODEL_PREFIX + " " + POLYGON + index + " " + POINT + (i + 1) + " " + Y,
              Double.toString(point.getY()));
        }
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return map;
  }

  @Override
  public void unpickle(Map<String, String> map) {
    // A fully custom unpickle method, since this is a non-standard model.
    setSymmetricRegions(Boolean.parseBoolean(map.get(POLYGONS_REGION_MODEL_PREFIX + " " + SYMMETRIC_REGIONS)));
    setPolygonsFilled(Boolean.parseBoolean(map.get(POLYGONS_REGION_MODEL_PREFIX + " " + POLYGONS_FILLED)));
    setNormalizationFactor(Float.parseFloat(map.get(POLYGONS_REGION_MODEL_PREFIX + " " + NORMALIZATION_FACTOR)));
    try {
      for (int index = 0; index < NUMBER_OF_POLYGONS; index++) {
        PolygonModel model = getPolygonModel(index);
        model.setVisible(Boolean.parseBoolean(map.get(POLYGONS_REGION_MODEL_PREFIX + " " + POLYGON + index + " "
            + VISIBLE)));
        model.setText(map.get(POLYGONS_REGION_MODEL_PREFIX + " " + POLYGON + index + " " + TEXT));
        model.setValue(Float.parseFloat(map.get(POLYGONS_REGION_MODEL_PREFIX + " " + POLYGON + index + " " + VALUE)));
        int numPoints = Integer.parseInt(map.get(POLYGONS_REGION_MODEL_PREFIX + " " + POLYGON + index + " "
            + POINT_COUNT));
        Point3d[] points = new Point3d[numPoints];
        for (int i = 0; i < model.getNumPoints(); i++) {
          double x = Double.parseDouble(map.get(POLYGONS_REGION_MODEL_PREFIX + " " + POLYGON + index + " " + POINT
              + (i + 1) + " " + X));
          double y = Double.parseDouble(map.get(POLYGONS_REGION_MODEL_PREFIX + " " + POLYGON + index + " " + POINT
              + (i + 1) + " " + Y));
          points[i] = new Point3d(x, y, 0);
        }
        model.setPoints(points);
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
