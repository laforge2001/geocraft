/*
 * Copyright (C) ConocoPhillips 2006 - 2007 All Rights Reserved.
 */
package org.geocraft.core.common.xml;


import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * Defines the XmlIO interface.
 */
public interface XmlIO {

  /**
   * Gets XML information.
   * @param doc The document source.
   * @param parent the parent node.
   * @return The node.
   * @throws Exception Thrown on XML parsing error.
   */
  void getXML(Document doc, Node parent) throws Exception;

  /**
   * Sets XML information.
   * @param doc The document source.
   * @param parent the parent node.
   * @throws Exception Thrown on XML parsing error.
   */
  void setXML(Document doc, Node parent) throws Exception;

}
