/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.ascii.aoi;


import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;

import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.common.xml.XmlIO;
import org.geocraft.core.common.xml.XmlUtils;
import org.geocraft.core.model.aoi.SeismicSurvey3dAOI;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.CornerPointsSeries;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.model.seismic.SurveyOrientation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class provides methods for reading/writing a
 * <code>SeismicSurvey3dAOI</code> in a custom GeoCraft XML format.
 */
public class SeismicSurvey3dAOIReaderWriter implements AsciiAOIConstants {

  public void readFromStore(final SeismicSurvey3dAOI aoi, final String filePath) throws IOException {
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
            if (node.getAttribute(TYPE_ATTR).equals(INLINE_XLINE_AOI)) {
              aoiNode = node;
              break;
            }
          }
        }
        if (aoiNode == null) {
          throw new IOException("Error: No valid inline/xline AOI's defined in the file.");
        }

        // Look for an optional z range.
        MapPolygonAOIReaderWriter.readOptionalZRangeNode(aoi, aoiNode);

        // Look for the survey element of the AOI.
        NodeList surveyNodeList = aoiNode.getElementsByTagName(SURVEY_NODE);
        if (surveyNodeList.getLength() == 0) {
          throw new IOException("Error: The inline/xline AOI does not define a survey.");
        }
        // Take the 1st survey found, although there may be more than one.
        Element surveyNode = (Element) surveyNodeList.item(0);
        String orientationStr = surveyNode.getAttribute(ORIENTATION);
        SurveyOrientation orientation = SurveyOrientation.lookupByName(orientationStr);
        String xyUnitStr = surveyNode.getAttribute(UNIT_ATTR);
        Unit xyUnitDatastore = Unit.lookupByName(xyUnitStr);
        if (xyUnitDatastore.equals(Unit.UNDEFINED)) {
          throw new IOException("Error: Invalid survey units in AOI survey: " + xyUnitStr);
        }

        // Look for the corner point elements of the survey.
        NodeList cornerPointList = surveyNode.getElementsByTagName(CORNERPOINT_NODE);
        int numCornerPoints = cornerPointList.getLength();
        if (numCornerPoints != 4) {
          throw new IOException("Error: AOI survey must have 4 corner points..." + numCornerPoints + " found.");
        }

        Point3d[] points = new Point3d[4];
        for (int i = 0; i < 4; i++) {
          Element cornerPointNode = (Element) cornerPointList.item(i);
          int index = Integer.parseInt(cornerPointNode.getAttribute(INDEX_ATTR));
          double x = Double.parseDouble(cornerPointNode.getAttribute(X_ATTR));
          double y = Double.parseDouble(cornerPointNode.getAttribute(Y_ATTR));
          x = Unit.convert(x, xyUnitDatastore, xyUnit);
          y = Unit.convert(y, xyUnitDatastore, xyUnit);
          points[index] = new Point3d(x, y, 0);
        }
        CornerPointsSeries cornerPoints = CornerPointsSeries
            .createDirect(points, new CoordinateSystem("", Domain.TIME));

        // Look for the inline element of the survey.
        NodeList inlineNodeList = surveyNode.getElementsByTagName(INLINE_NODE);
        if (inlineNodeList.getLength() == 0) {
          throw new IOException("Error: The AOI survey does not define an inline range.");
        }
        // Take the 1st inline found, although there may be more than one.
        Element inlineNode = (Element) inlineNodeList.item(0);
        float inlineStart = Float.parseFloat(inlineNode.getAttribute(START_ATTR));
        float inlineEnd = Float.parseFloat(inlineNode.getAttribute(END_ATTR));
        float inlineDelta = Float.parseFloat(inlineNode.getAttribute(DELTA_ATTR));
        FloatRange inlineRange = new FloatRange(inlineStart, inlineEnd, inlineDelta);

        // Look for the xline element of the survey.
        NodeList xlineNodeList = surveyNode.getElementsByTagName(XLINE_NODE);
        if (xlineNodeList.getLength() == 0) {
          throw new IOException("Error: The AOI survey does not define a crossline range.");
        }
        // Take the 1st xline found, although there may be more than one.
        Element xlineNode = (Element) xlineNodeList.item(0);
        float xlineStart = Float.parseFloat(xlineNode.getAttribute(START_ATTR));
        float xlineEnd = Float.parseFloat(xlineNode.getAttribute(END_ATTR));
        float xlineDelta = Float.parseFloat(xlineNode.getAttribute(DELTA_ATTR));
        FloatRange xlineRange = new FloatRange(xlineStart, xlineEnd, xlineDelta);

        SeismicSurvey3d survey = new SeismicSurvey3d("", inlineRange, xlineRange, cornerPoints, orientation);

        // Look for the range element of the AOI.
        NodeList rangeNodeList = aoiNode.getElementsByTagName(RANGE_NODE);
        if (rangeNodeList.getLength() == 0) {
          throw new IOException("Error: The AOI does not define Inline/Xline ranges.");
        }
        // Take the 1st range found, although there may be more than one.
        Element rangeNode = (Element) rangeNodeList.item(0);

        // Look for the inline element of the range.
        NodeList inlineNodeListAOI = rangeNode.getElementsByTagName(INLINE_NODE);
        if (inlineNodeListAOI.getLength() == 0) {
          throw new IOException("Error: The AOI does not define an Inline range.");
        }
        // Take the 1st inline found, although there may be more than one.
        Element inlineNodeAOI = (Element) inlineNodeListAOI.item(0);
        float inlineStartAOI = Float.parseFloat(inlineNodeAOI.getAttribute(START_ATTR));
        float inlineEndAOI = Float.parseFloat(inlineNodeAOI.getAttribute(END_ATTR));
        float inlineDeltaAOI = Float.parseFloat(inlineNodeAOI.getAttribute(DELTA_ATTR));
        inlineRange = new FloatRange(inlineStartAOI, inlineEndAOI, inlineDeltaAOI);

        // Look for the xline element of the range.
        NodeList xlineNodeListAOI = rangeNode.getElementsByTagName(XLINE_NODE);
        if (xlineNodeListAOI.getLength() == 0) {
          throw new IOException("Error: The AOI does not define an Xline range.");
        }
        // Take the 1st xline found, although there may be more than one.
        Element xlineNodeAOI = (Element) xlineNodeListAOI.item(0);
        float xlineStartAOI = Float.parseFloat(xlineNodeAOI.getAttribute(START_ATTR));
        float xlineEndAOI = Float.parseFloat(xlineNodeAOI.getAttribute(END_ATTR));
        float xlineDeltaAOI = Float.parseFloat(xlineNodeAOI.getAttribute(DELTA_ATTR));
        xlineRange = new FloatRange(xlineStartAOI, xlineEndAOI, xlineDeltaAOI);

        aoi.setRanges(survey, inlineRange, xlineRange);
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

  public void updateInStore(final SeismicSurvey3dAOI aoi, final String filePath) throws IOException {
    final Unit xyUnit = UnitPreferences.getInstance().getHorizontalDistanceUnit();
    final SeismicSurvey3d survey = aoi.getSurvey();
    final Point3d[] points = survey.getCornerPoints().getPointsDirect();

    XmlIO xmlio = new XmlIO() {

      @Override
      public void getXML(final Document doc, final Node parent) throws Exception {

        // Add the AOI node.
        Element aoiNode = XmlUtils.addElement(doc, parent, AOI_NODE);
        XmlUtils.addAttribute(doc, aoiNode, TYPE_ATTR, INLINE_XLINE_AOI);

        // Add the optional z-range. 
        MapPolygonAOIReaderWriter.writeOptionalZRangeNode(aoi, doc, aoiNode);

        // Add the survey node and its x,y unit of measurement.
        Element surveyNode = XmlUtils.addElement(doc, aoiNode, SURVEY_NODE);
        XmlUtils.addAttribute(doc, surveyNode, UNIT_ATTR, xyUnit.getName());
        XmlUtils.addAttribute(doc, surveyNode, ORIENTATION, survey.getOrientation().toString());

        // Add the corner points of the survey.
        for (int i = 0; i < points.length; i++) {
          Element cornerPointNode = XmlUtils.addElement(doc, surveyNode, CORNERPOINT_NODE);
          XmlUtils.addAttribute(doc, cornerPointNode, INDEX_ATTR, Integer.toString(i));
          XmlUtils.addAttribute(doc, cornerPointNode, X_ATTR, Double.toString(points[i].getX()));
          XmlUtils.addAttribute(doc, cornerPointNode, Y_ATTR, Double.toString(points[i].getY()));
        }

        // Add the inline,xline range of the survey.
        Element inlineRangeNode = XmlUtils.addElement(doc, surveyNode, INLINE_NODE);
        XmlUtils.addAttribute(doc, inlineRangeNode, START_ATTR, Float.toString(survey.getInlineStart()));
        XmlUtils.addAttribute(doc, inlineRangeNode, END_ATTR, Float.toString(survey.getInlineEnd()));
        XmlUtils.addAttribute(doc, inlineRangeNode, DELTA_ATTR, Float.toString(survey.getInlineDelta()));
        Element xlineRangeNode = XmlUtils.addElement(doc, surveyNode, XLINE_NODE);
        XmlUtils.addAttribute(doc, xlineRangeNode, START_ATTR, Float.toString(survey.getXlineStart()));
        XmlUtils.addAttribute(doc, xlineRangeNode, END_ATTR, Float.toString(survey.getXlineEnd()));
        XmlUtils.addAttribute(doc, xlineRangeNode, DELTA_ATTR, Float.toString(survey.getXlineDelta()));

        // Add the inline,xline range of the AOI.
        Element rangeNode = XmlUtils.addElement(doc, aoiNode, RANGE_NODE);
        Element inlineRangeNodeAOI = XmlUtils.addElement(doc, rangeNode, INLINE_NODE);
        XmlUtils.addAttribute(doc, inlineRangeNodeAOI, START_ATTR, Float.toString(aoi.getInlineStart()));
        XmlUtils.addAttribute(doc, inlineRangeNodeAOI, END_ATTR, Float.toString(aoi.getInlineEnd()));
        XmlUtils.addAttribute(doc, inlineRangeNodeAOI, DELTA_ATTR, Float.toString(aoi.getInlineDelta()));
        Element xlineRangeNodeAOI = XmlUtils.addElement(doc, rangeNode, XLINE_NODE);
        XmlUtils.addAttribute(doc, xlineRangeNodeAOI, START_ATTR, Float.toString(aoi.getXlineStart()));
        XmlUtils.addAttribute(doc, xlineRangeNodeAOI, END_ATTR, Float.toString(aoi.getXlineEnd()));
        XmlUtils.addAttribute(doc, xlineRangeNodeAOI, DELTA_ATTR, Float.toString(aoi.getXlineDelta()));
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
}
