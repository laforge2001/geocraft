/*
 * Copyright (C) ConocoPhillips 2009 All Rights Reserved. 
 */

package org.geocraft.core.model.base;


import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class PropertyDescriptorParser {

  private static PropertyDescriptorParser singleton = null;

  private PropertyDescriptorParser() {
    // The empty constructor.
  }

  private static final String ROOT_NODE = "entity-properties";

  private static final String ROOT_NAME_ATTR = "name";

  private static final String CATEGORY_NODE = "category";

  private static final String CATEGORY_NAME_ATTR = "name";

  private static final String PROPERTY_NODE = "property";

  private static final String PROPERTY_ID_ATTR = "id";

  private static final String PROPERTY_DISPLAY_ATTR = "display";

  private static final String EXCLUDE_NODE = "exclude";

  /**
   * Get the singleton instance of this class. If the data explorer class doesn't
   * exist, create it.
   */
  public static PropertyDescriptorParser getInstance() {
    if (singleton == null) {
      singleton = new PropertyDescriptorParser();
    }

    return singleton;
  }

  /** Properties descriptor to populate as parse descriptor XML file */
  PropertiesDescriptor _propertiesDescriptor;

  /** <exclude> indicator: true if processing excluded properties; otherwise, false */
  boolean _excludeProperty;

  /** Current category */
  String _currentCategory;

  /**
   * Parse the entity property descriptor XML file.
   * @param path URI of the XML file to parse.
   * @param propertiesDescriptor Container for entity properties
   * @return true if processed XML file with no errors; otherwise, fasle
   */
  public boolean parseEntityProperties(final String path, final PropertiesDescriptor propertiesDescriptor) {
    _propertiesDescriptor = propertiesDescriptor;

    _excludeProperty = false;
    _currentCategory = "";

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    try {
      InputStream in = this.getClass().getResourceAsStream(path);

      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(new InputSource(in));

      walkTree(doc);
    } catch (ParserConfigurationException pce) {
      //System.out.println("PropertyDescriptorParser: Parser error: " + pce.getMessage());
      return false;
    } catch (SAXException saxe) {
      //System.out.println("PropertyDescriptorParser: Cannot find file: " + saxe.getMessage());
      return false;
    } catch (IOException ioe) {
      //System.out.println("PropertyDescriptorParser: IO error: " + ioe.getMessage());
      return false;
    }

    return true;
  }

  /**
   * Recursively walk the XML tree processing each node.
   * @param node Start node to begin walk.
   */
  private void walkTree(final Node node) {
    NodeList children;
    NamedNodeMap attributes;
    //<String> attrNames = new ArrayList<String>();
    //ArrayList<String> attrValues = new ArrayList<String>();
    String attrName, attrValue;

    switch (node.getNodeType()) {
      case Node.DOCUMENT_NODE:
        // Document object
        walkTree(((Document) node).getDocumentElement());
        break;

      case Node.ELEMENT_NODE:
        // element + attributes
        String name = node.getNodeName();
        //                System.out.println("Element node: "+name);

        if (name.equals(CATEGORY_NODE)) {
          attributes = node.getAttributes();
          // gather <category>'s sole attribute.
          Node attr = attributes.item(0);
          attrName = attr.getNodeName();
          attrValue = attr.getNodeValue();
          if (attrName.equals(CATEGORY_NAME_ATTR)) {
            _currentCategory = attrValue;
            _propertiesDescriptor.addCategory(attrValue);
          }

          children = node.getChildNodes();
          if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
              Node child = children.item(i);
              //                            System.out.println("    Process child "+child.getNodeName());
              walkTree(child);
            }
          }
        } else if (name.equals(PROPERTY_NODE)) {
          attributes = node.getAttributes();
          String id = "", display = "";
          // gather <property> attributes. NOTE: they can be in any order
          for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            attrName = attr.getNodeName();
            attrValue = attr.getNodeValue();
            if (attrName.equals(PROPERTY_ID_ATTR)) {
              id = attrValue;
            }
            if (attrName.equals(PROPERTY_DISPLAY_ATTR)) {
              display = attrValue;
            }
            //                        System.out.println("    Attribute: "+attrName+", Value: "+attrValue);
          }

          if (_excludeProperty) {
            _propertiesDescriptor.addExcludedProperty(id, display);
          } else {
            _propertiesDescriptor.addProperty(_currentCategory, id, display);
          }

          children = node.getChildNodes();
          if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
              Node child = children.item(i);
              //                            System.out.println("    Process child "+child.getNodeName());
              walkTree(child);
            }
          }
        } else if (name.equals(EXCLUDE_NODE)) {
          _excludeProperty = true;

          children = node.getChildNodes();
          if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
              Node child = children.item(i);
              //                            System.out.println("    Process child "+child.getNodeName());
              walkTree(child);
            }
          }
        } else if (name.equals(ROOT_NODE)) {
          attributes = node.getAttributes();
          Node attr = attributes.item(0);
          attrName = attr.getNodeName();
          attrValue = attr.getNodeValue();
          if (attrName.equals(ROOT_NAME_ATTR)) {
            _propertiesDescriptor.setEntityName(attrValue);
          }

          children = node.getChildNodes();
          if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
              Node child = children.item(i);
              //                            System.out.println("    Process child "+child.getNodeName());
              walkTree(child);
            }
          }
        }
        break;

      case Node.TEXT_NODE:
      case Node.CDATA_SECTION_NODE:
        // textual data
        String text = node.getNodeValue();
        break;

      case Node.PROCESSING_INSTRUCTION_NODE:
        // processing instruction
        break;

      case Node.ENTITY_REFERENCE_NODE:
        // entity reference
        break;

      case Node.DOCUMENT_TYPE_NODE:
        // DTD declaration
        break;
    }
  }
}
