/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.core.common.xml;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Defines the XmlUtils class.
 */
public class XmlUtils {

  /**
   * Adds an element to the specified document.
   * @param doc the document.
   * @param parent the parent node.
   * @param name the element name.
   * @return the element.
   */
  public static Element addElement(final Document doc, final Node parent, final String name) {
    Element element = doc.createElement(name);
    if (parent != null) {
      parent.appendChild(element);
    } else {
      doc.appendChild(element);
    }
    return element;
  }

  /**
   * Adds an attribute to the specified document element.
   * @param doc the document.
   * @param parent the parent element.
   * @param name the attribute name.
   * @param value the attribute value.
   * @return the attribute.
   */
  public static Attr addAttribute(final Document doc, final Element parent, final String name, final String value) {
    Attr attr = doc.createAttribute(name);
    attr.setNodeValue(value);
    parent.setAttributeNode(attr);
    return attr;
  }

  /**
   * Reads XML file information.
   * @param file the input file for reading.
   * @param xmlio the XML I/O interface.
   * @throws Exception thrown on XML parsing error.
   */
  public static void readXML(final File file, final XmlIO xmlio) throws Exception {

    // Check file pointer.
    if (file == null) {
      throw new Exception("Exception:\nreadSession(file)\nNull File Pointer");
    }

    // Create file input stream.
    FileInputStream istream = new FileInputStream(file);

    // Read properties.
    readXML(istream, xmlio);

    // Close file input stream.
    istream.close();
  }

  /**
   * Reads XML information.
   * @param istream The input stream for reading.
   * @param xmlio the XML I/O interface.
   * @throws Exception Thrown on XML parsing error.
   */
  public static void readXML(final InputStream istream, final XmlIO xmlio) throws Exception {

    // Create a DOM document.
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    docFactory.setValidating(false);
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    Document doc = docBuilder.parse(istream);

    if (doc == null) {
      throw new Exception("Error: Unable to generate internal XML document.");
    }

    // Append internal session XML to the DOM document.
    xmlio.setXML(doc, null);

    doc = null;
    System.gc();
  }

  /**
   * Writes XML information.
   * @param file The file for writing.
   * @param xmlio the XML I/O interface.
   * @throws Exception Thrown on XML parsing error.
   */
  public static void writeXML(final File file, final XmlIO xmlio) throws Exception {
    OutputStream ostream = new FileOutputStream(file);
    XmlUtils.writeXML(ostream, xmlio);
    ostream.close();
  }

  /**
   * Writes XML information.
   * @param ostream The output stream for writing.
   * @param xmlio the XML I/O interface.
   * @throws Exception Thrown on XML parsing error.
   */
  public static void writeXML(final OutputStream ostream, final XmlIO xmlio) throws Exception {
    writeXML(ostream, xmlio, null);
  }

  /**
   * Writes XML information.
   * @param ostream The output stream for writing.
   * @param xmlio the XML I/O interface.
   * @param comments the comments, or null if none.
   * @throws Exception Thrown on XML parsing error.
   */
  public static void writeXML(final OutputStream ostream, final XmlIO xmlio, final String comments) throws Exception {
    // Create a DOM document.
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    Document doc = docBuilder.newDocument();

    if (doc == null) {
      throw new Exception("Error: Unable to generate internal XML document.");
    }

    // Insert the author comment.
    Properties props = System.getProperties();
    String authorString = "AUTHOR: " + props.getProperty("user.name");
    Comment authorComment = doc.createComment(authorString);
    doc.appendChild(authorComment);

    Calendar c = Calendar.getInstance();

    // Insert the time comment.
    int second = c.get(Calendar.SECOND);
    int minute = c.get(Calendar.MINUTE);
    int hour = c.get(Calendar.HOUR_OF_DAY);
    String secondStr = Integer.toString(second);
    if (second < 10) {
      secondStr = "0" + second;
    }
    String minuteStr = Integer.toString(minute);
    if (minute < 10) {
      minuteStr = "0" + minute;
    }
    String hourStr = Integer.toString(hour);
    if (hour < 10) {
      hourStr = "0" + hour;
    }
    String timeString = "TIME: " + hourStr + ":" + minuteStr + ":" + secondStr;
    Comment timeComment = doc.createComment(timeString);
    doc.appendChild(timeComment);

    // Insert the date comment.
    int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
    int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
    int monthOfYear = c.get(Calendar.MONTH);
    int year = c.get(Calendar.YEAR);
    String[] dayOfWeekStrings = { "", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
    String[] monthOfYearStrings = { "January", "February", "March", "April", "May", "June", "July", "August",
        "September", "October", "November", "December" };
    String[] dayOfMonthStrings = { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
    String dateString = "DATE: " + dayOfWeekStrings[dayOfWeek] + ", " + monthOfYearStrings[monthOfYear] + " "
        + dayOfMonth + dayOfMonthStrings[dayOfMonth % 10] + ", " + year;
    Comment dateComment = doc.createComment(dateString);
    doc.appendChild(dateComment);

    // Insert the comments.
    if (comments != null) {
      Comment nodeComment = doc.createComment(comments);
      doc.appendChild(nodeComment);
    }

    // Append internal session XML to the DOM document.
    xmlio.getXML(doc, null);

    // Write the DOM document to the output stream.
    TransformerFactory factory = TransformerFactory.newInstance();
    factory.setAttribute("indent-number", new Integer(2));
    Transformer transformer = factory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    transformer.transform(new DOMSource(doc), new StreamResult(ostream));

    doc = null;
    System.gc();
  }
}
