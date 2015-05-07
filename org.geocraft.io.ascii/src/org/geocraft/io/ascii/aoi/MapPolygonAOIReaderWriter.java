/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.ascii.aoi;


import java.awt.geom.PathIterator;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.common.xml.XmlIO;
import org.geocraft.core.common.xml.XmlUtils;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.aoi.MapPolygon;
import org.geocraft.core.model.aoi.MapPolygonAOI;
import org.geocraft.core.model.aoi.ZRange;
import org.geocraft.core.model.aoi.ZRangeConstant;
import org.geocraft.core.model.aoi.MapPolygon.Type;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.datatypes.ZDomain;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class MapPolygonAOIReaderWriter implements AsciiAOIConstants {

  public void readFromStore(final MapPolygonAOI aoi, final String filePath) throws IOException {
    final Unit xyUnit = UnitPreferences.getInstance().getHorizontalDistanceUnit();
    XmlIO xmlio = new XmlIO() {

      @Override
      public void setXML(final Document doc, final Node parent) throws Exception {

        Element aoiNode = null;

        // Look for the AOI node.
        NodeList nodeList = doc.getElementsByTagName(AOI_NODE);
        int nodeCount = nodeList.getLength();
        if (nodeCount > 0) {
          for (int i = 0; i < nodeCount; i++) {
            Element node = (Element) nodeList.item(i);
            if (node.getAttribute(TYPE_ATTR).equals(MAP_POLYGON_AOI)) {
              aoiNode = node;
              break;
            }
          }
        }
        if (aoiNode == null) {
          throw new IOException("Error: No valid polygon AOI's defined in the file.");
        }

        // Look for an optional z range.
        readOptionalZRangeNode(aoi, aoiNode);

        // Look for the polygon elements of the AOI.
        NodeList polygonsNodeList = aoiNode.getElementsByTagName(POLYGON_NODE);
        if (polygonsNodeList.getLength() == 0) {
          throw new IOException("Error: The AOI does not define any polygons.");
        }
        int numPolygons = polygonsNodeList.getLength();
        for (int i = 0; i < numPolygons; i++) {
          Element polygonNode = (Element) polygonsNodeList.item(i);

          String xyUnitStr = polygonNode.getAttribute(UNIT_ATTR);
          Unit xyUnitDatastore = Unit.lookupByName(xyUnitStr);
          if (xyUnitDatastore.equals(Unit.UNDEFINED)) {
            throw new IOException("Error: Invalid x,y units in AOI: " + xyUnitStr);
          }

          int inclusive = Integer.parseInt(polygonNode.getAttribute(INCLUSIVE_ATTR));

          NodeList pointList = polygonNode.getElementsByTagName(POINT_NODE);
          int numPoints = pointList.getLength();
          if (numPoints < 3) {
            throw new IOException("Error: Polygon AOI must have at least 3 points..." + numPoints + " found.");
          }
          double[] xs = new double[numPoints];
          double[] ys = new double[numPoints];
          for (int j = 0; j < numPoints; j++) {
            Element pointNode = (Element) pointList.item(j);
            int index = Integer.parseInt(pointNode.getAttribute(INDEX_ATTR));
            double x = Double.parseDouble(pointNode.getAttribute(X_ATTR));
            double y = Double.parseDouble(pointNode.getAttribute(Y_ATTR));
            xs[index] = Unit.convert(x, xyUnitDatastore, xyUnit);
            ys[index] = Unit.convert(y, xyUnitDatastore, xyUnit);
          }
          switch (inclusive) {
            case 1:
              aoi.addInclusionPolygon(xs, ys);
              break;
            case -1:
              aoi.addExclusionPolygon(xs, ys);
              break;
          }
        }
      }

      @Override
      public void getXML(final Document doc, final Node parent) throws Exception {
        // TODO Auto-generated method stub
      }

    };

    File file = new File(filePath);
    try {
      XmlUtils.readXML(file, xmlio);
      Timestamp lastModifiedDate = new Timestamp(file.lastModified());
      aoi.setLastModifiedDate(lastModifiedDate);
      aoi.setDisplayColor(new RGB(255, 0, 0));
      aoi.setDirty(false);
    } catch (Exception ex) {
      throw new IOException("Error reading AOI file: " + ex.getMessage(), ex);
    }
  }

  public void updateInStore(final MapPolygonAOI aoi, final String filePath) throws IOException {
    final Unit xyUnit = UnitPreferences.getInstance().getHorizontalDistanceUnit();

    XmlIO xmlio = new XmlIO() {

      @Override
      public void getXML(final Document doc, final Node parent) throws Exception {

        // Add the AOI node.
        Element aoiNode = XmlUtils.addElement(doc, parent, AOI_NODE);
        XmlUtils.addAttribute(doc, aoiNode, TYPE_ATTR, MAP_POLYGON_AOI);

        // Add the optional z-range.
        writeOptionalZRangeNode(aoi, doc, aoiNode);

        MapPolygon[] mapPolygons = aoi.getPolygons();
        for (MapPolygon mapPolygon : mapPolygons) {
          Type inclusiveType = mapPolygon.getType();
          int inclusiveFlag = 0;
          if (inclusiveType == Type.INCLUSIVE) {
            inclusiveFlag = 1;
          } else if (inclusiveType == Type.EXCLUSIVE) {
            inclusiveFlag = -1;
          }
          PathIterator iter = mapPolygon.getPath().getPathIterator(null);
          List<Double> xs = new ArrayList<Double>();
          List<Double> ys = new ArrayList<Double>();
          while (!iter.isDone()) {
            double[] coords = new double[6];
            int type = iter.currentSegment(coords);
            if (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
              xs.add(coords[0]);
              ys.add(coords[1]);
            }
            iter.next();
          }
          int numPoints = xs.size();

          // Add a polygon node and its x,y unit of measurement and inclusive flag.
          Element polygonNode = XmlUtils.addElement(doc, aoiNode, POLYGON_NODE);
          XmlUtils.addAttribute(doc, polygonNode, UNIT_ATTR, xyUnit.getName());
          XmlUtils.addAttribute(doc, polygonNode, INCLUSIVE_ATTR, "" + inclusiveFlag);

          // Add the points to the polygon.
          for (int i = 0; i < numPoints; i++) {
            Element pointNode = XmlUtils.addElement(doc, polygonNode, POINT_NODE);
            XmlUtils.addAttribute(doc, pointNode, INDEX_ATTR, Integer.toString(i));
            XmlUtils.addAttribute(doc, pointNode, X_ATTR, Double.toString(xs.get(i)));
            XmlUtils.addAttribute(doc, pointNode, Y_ATTR, Double.toString(ys.get(i)));
          }
        }
      }

      @Override
      public void setXML(final Document doc, final Node parent) throws Exception {
        // TODO Auto-generated method stub

      }

    };
    File file = new File(filePath);
    try {
      XmlUtils.writeXML(file, xmlio);
    } catch (Exception ex) {
      throw new IOException("Error writing AOI file: " + ex.getMessage(), ex);
    }

  }

  /**
   * Reads the z-range node, if it exists.
   * <p>
   * Note: In the event that more than 1 z-range node exists, only the first one
   * is read.
   * 
   * @param aoi
   *          the area-of-interest.
   * @param aoiNode
   *          the parent node.
   */
  public static void readOptionalZRangeNode(final AreaOfInterest aoi, final Element aoiNode) {
    NodeList zNodeList = aoiNode.getElementsByTagName(Z_RANGE_NODE);
    if (zNodeList.getLength() > 0) {
      Element zNode = (Element) zNodeList.item(0);
      String zStartStr = zNode.getAttribute(START_ATTR);
      float zStart = Float.parseFloat(zStartStr);
      String zEndStr = zNode.getAttribute(END_ATTR);
      float zEnd = Float.parseFloat(zEndStr);
      String zUnitStr = zNode.getAttribute(UNIT_ATTR);
      Unit zUnit = Unit.lookupByName(zUnitStr);
      aoi.setZRange(zStart, zEnd, zUnit);
    }
  }

  /**
   * Writes the z-range node, if the AOI contains a z range.
   * 
   * @param aoi
   *          the area-of-interest.
   * @param doc
   *          the document.
   * @param aoiNode
   *          the parent node.
   */
  public static void writeOptionalZRangeNode(final AreaOfInterest aoi, final Document doc, final Element aoiNode) {
    if (aoi.hasZRange()) {
      ZRangeConstant zRange = aoi.getZRange();
      if (zRange != null && zRange.getType() == ZRange.Type.CONSTANT) {
        float[] zs = zRange.getZStartAndEnd(0, 0);
        Unit zUnit = null;
        ZDomain zDomain = zRange.getDomain();
        switch (zDomain) {
          case TIME:
            zUnit = UnitPreferences.getInstance().getTimeUnit();
            break;
          case DEPTH:
            zUnit = UnitPreferences.getInstance().getVerticalDistanceUnit();
            break;
          default:
            throw new RuntimeException("Invalid z-domain: " + zDomain);
        }
        Element zNode = XmlUtils.addElement(doc, aoiNode, Z_RANGE_NODE);
        XmlUtils.addAttribute(doc, zNode, TYPE_ATTR, ZRange.Type.CONSTANT.toString());
        XmlUtils.addAttribute(doc, zNode, START_ATTR, Float.toString(zs[0]));
        XmlUtils.addAttribute(doc, zNode, END_ATTR, Float.toString(zs[1]));
        XmlUtils.addAttribute(doc, zNode, UNIT_ATTR, zUnit.getName());
      }
    }
  }
}
