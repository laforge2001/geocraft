/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.ascii.aoi;


import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.common.xml.XmlIO;
import org.geocraft.core.common.xml.XmlUtils;
import org.geocraft.core.model.aoi.SeismicSurvey2dAOI;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class provides methods for reading/writing a
 * <code>SeismicSurvey2dAOI</code> in a custom GeoCraft XML format.
 */
public class SeismicSurvey2dAOIReaderWriter implements AsciiAOIConstants {

  public void readFromStore(final SeismicSurvey2dAOI aoi, final String filePath) throws IOException {
    final Unit xyUnit = UnitPreferences.getInstance().getHorizontalDistanceUnit();
    XmlIO xmlio = new XmlIO() {

      public void setXML(final Document doc, final Node parent) throws Exception {

        Element aoiNode = null;

        // Look for the AOI node.
        NodeList nodeList = doc.getElementsByTagName(AOI_NODE);
        int nodeCount = nodeList.getLength();
        if (nodeCount > 0) {
          for (int i = 0; i < nodeCount; i++) {
            Element node = (Element) nodeList.item(i);
            if (node.getAttribute(TYPE_ATTR).equals(LINE_SHOTPOINT_AOI)) {
              aoiNode = node;
              break;
            }
          }
        }
        if (aoiNode == null) {
          throw new IOException("Error: No valid line/CDP ranges defined in the file.");
        }

        // Look for an optional z range.
        MapPolygonAOIReaderWriter.readOptionalZRangeNode(aoi, aoiNode);

        // Look for the survey element of the AOI.
        NodeList surveyNodeList = aoiNode.getElementsByTagName(SURVEY_NODE);
        if (surveyNodeList.getLength() == 0) {
          throw new IOException("Error: The AOI does not define a survey.");
        }
        // Take the 1st survey found, although there may be more than one.
        Element surveyNode = (Element) surveyNodeList.item(0);

        // Look for the inline element of the survey.
        NodeList lineNodeList = surveyNode.getElementsByTagName(LINE_NODE);
        if (lineNodeList.getLength() == 0) {
          throw new IOException("Error: The AOI does not define any lines.");
        }

        Map<String, FloatRange> cdpRanges = new HashMap<String, FloatRange>();
        for (int i = 0; i < lineNodeList.getLength(); i++) {
          Element lineNode = (Element) lineNodeList.item(i);
          String lineName = lineNode.getAttribute(NAME_ATTR);
          NodeList cdpRangeNodeList = lineNode.getElementsByTagName(CDP_NODE);
          if (cdpRangeNodeList.getLength() == 0) {
            throw new IOException("Error: The AOI does not define a CDP range for line " + lineName);
          }
          Element cdpRangeNode = (Element) cdpRangeNodeList.item(0);
          float cdpStart = Float.parseFloat(cdpRangeNode.getAttribute(START_ATTR));
          float cdpEnd = Float.parseFloat(cdpRangeNode.getAttribute(END_ATTR));
          float cdpDelta = Float.parseFloat(cdpRangeNode.getAttribute(DELTA_ATTR));
          FloatRange cdpRange = new FloatRange(cdpStart, cdpEnd, cdpDelta);
          cdpRanges.put(lineName, cdpRange);
        }
        aoi.setCdpRanges(cdpRanges);
        aoi.setDirty(false);
      }

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

  public void updateInStore(final SeismicSurvey2dAOI aoi, final String filePath) throws IOException {
    final Unit xyUnit = UnitPreferences.getInstance().getHorizontalDistanceUnit();

    XmlIO xmlio = new XmlIO() {

      public void getXML(final Document doc, final Node parent) throws Exception {

        // Add the AOI node.
        Element aoiNode = XmlUtils.addElement(doc, parent, AOI_NODE);
        XmlUtils.addAttribute(doc, aoiNode, TYPE_ATTR, LINE_SHOTPOINT_AOI);

        // Add the optional z-range. 
        MapPolygonAOIReaderWriter.writeOptionalZRangeNode(aoi, doc, aoiNode);

        // Add the survey node and its x,y unit of measurement.
        Element surveyNode = XmlUtils.addElement(doc, aoiNode, SURVEY_NODE);

        Map<String, FloatRange> cdpRanges = aoi.getCdpRanges();

        for (String lineName : cdpRanges.keySet()) {
          FloatRange cdpRange = cdpRanges.get(lineName);

          // Add the inline,xline range of the survey.
          Element lineNode = XmlUtils.addElement(doc, surveyNode, LINE_NODE);
          XmlUtils.addAttribute(doc, lineNode, NAME_ATTR, lineName);
          Element cdpRangeNode = XmlUtils.addElement(doc, lineNode, CDP_NODE);
          XmlUtils.addAttribute(doc, cdpRangeNode, START_ATTR, Float.toString(cdpRange.getStart()));
          XmlUtils.addAttribute(doc, cdpRangeNode, END_ATTR, Float.toString(cdpRange.getEnd()));
          XmlUtils.addAttribute(doc, cdpRangeNode, DELTA_ATTR, Float.toString(cdpRange.getDelta()));
        }
      }

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
