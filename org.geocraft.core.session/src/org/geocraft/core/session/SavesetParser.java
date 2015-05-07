/*
 * Copyright (C) ConocoPhillips 2010 All Rights Reserved. 
 */
package org.geocraft.core.session;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.core.session.SavesetDescriptor.Algorithm;
import org.geocraft.core.session.SavesetDescriptor.DataEntity;
import org.geocraft.core.session.SavesetDescriptor.Perspective;
import org.geocraft.core.session.SavesetDescriptor.PreferencePage;
import org.geocraft.core.session.SavesetDescriptor.Viewer;
import org.geocraft.core.session.SavesetDescriptor.ViewerPart;
import org.geocraft.core.session.SavesetDescriptor.Viewer.Renderer;
import org.geocraft.core.session.SavesetDescriptor.Viewer.ViewerLayer;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/** Parse the XML State (.gcs) and Batch (.gcb) saveset files */
public class SavesetParser {

  /** The logger. */
  private static ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(SavesetParser.class);

  private static SavesetParser singleton = null;

  private SavesetParser() {
    // The empty constructor.
  }

  /**
   * Get the singleton instance of this class. If the parser class doesn't
   * exist, create it.
   */
  public static SavesetParser getInstance() {
    if (singleton == null) {
      singleton = new SavesetParser();
    }

    return singleton;
  }

  private static final String ROOT_NODE = "geocraft";

  private static final String ROOT_VERSION_ATTR = "version";

  private static final String ROOT_WIDTH_ATTR = "width";

  private static final String ROOT_HEIGHT_ATTR = "height";

  private static final String ROOT_X_ATTR = "x";

  private static final String ROOT_Y_ATTR = "y";

  private static final String REPOSITORY_NODE = "repository";

  private static final String ENTITIES_NODE = "entities";

  private static final String ENTITY_NODE = "entity";

  private static final String ENTITY_VARNAME_ATTR = "varName";

  private static final String ENTITY_UNIQUEID_ATTR = "uniqueId";

  private static final String PROPERTY_NODE = "property";

  private static final String PROPERTY_KEY_ATTR = "key";

  private static final String PROPERTY_VALUE_ATTR = "value";

  private static final String ALGORITHMS_NODE = "algorithms";

  private static final String ALGORITHM_NODE = "algorithm";

  private static final String ALGORITHM_NAME_ATTR = "name";

  private static final String ALGORITHM_CLASS_ATTR = "class";

  private static final String ALGORITHM_WINDOW_ID_ATTR = "windowID";

  private static final String ALGORITHM_UNIQUEID_ATTR = "uniqueID";

  private static final String ALGORITHM_ACTION_ATTR = "action";

  private static final String PREFERENCE_PAGE_NODE = "preferencePage";

  private static final String PREFERENCE_PAGE_NAME_ATTR = "name";

  private static final String PREFERENCE_PAGE_CLASS_ATTR = "class";

  private static final String PARAMETER_NODE = "parameter";

  private static final String PARAMETER_KEY_ATTR = "key";

  private static final String PARAMETER_VALUE_ATTR = "value";

  private static final String PREFERENCE_NODE = "preference";

  private static final String PREFERENCE_KEY_ATTR = "key";

  private static final String PREFERENCE_VALUE_ATTR = "value";

  private static final String PERSPECTIVE_NODE = "perspective";

  private static final String PERSPECTIVE_NAME_ATTR = "name";

  private static final String PERSPECTIVE_CLASS_ATTR = "class";

  private static final String PERSPECTIVE_WINDOW_ID_ATTR = "windowID";

  private static final String PERSPECTIVE_ID_ATTR = "id";

  private static final String PERSPECTIVE_EDITOR_VISIBLE_ATTR = "isEditorAreaVisible";

  private static final String PERSPECTIVE_VIEW_ID_ATTR = "id";

  private static final String VIEWER_PART_NODE = "viewerPart";

  private static final String VIEWER_PART_CLASS_ATTR = "class";

  private static final String VIEWER_PART_ID_ATTR = "partID";

  private static final String VIEWER_PART_WINDOW_ID_ATTR = "windowID";

  private static final String VIEWER_PART_PERSPECTIVE_ID_ATTR = "perspectiveID";

  private static final String VIEWER_PART_UNIQUEID_ATTR = "uniqueID";

  private static final String VIEWER_NODE = "viewer";

  private static final String VIEWER_TITLE_ATTR = "title";

  private static final String VIEWER_CLASS_ATTR = "class";

  private static final String VIEWER_PROPERTY_NODE = "viewerProperty";

  private static final String VIEWER_PROPERTY_KEY_ATTR = "key";

  private static final String VIEWER_PROPERTY_VALUE_ATTR = "value";

  private static final String VIEWER_LAYER_NODE = "viewerLayer";

  private static final String VIEWER_LAYER_NAME_ATTR = "layerName";

  private static final String VIEWER_LAYER_CHECKED_ATTR = "checked";

  private static final String RENDERER_NODE = "renderer";

  private static final String RENDERER_CLASS_ATTR = "class";

  private static final String RENDERER_UNIQUEID_ATTR = "uniqueID";

  private static final String RENDERER_PROPERTY_NODE = "modelProperty";

  private static final String RENDERER_PROPERTY_KEY_ATTR = "key";

  private static final String RENDERER_PROPERTY_VALUE_ATTR = "value";

  private static final String RENDERED_ENTITY_NODE = "entityRendered";

  private static final String RENDERED_ENTITY_UNIQUEID_ATTR = "uniqueId";

  private static final String PLOT_WINDOW_NODE = "plotWindow";

  private static final String PLOT_WINDOW_ID_ATTR = "windowID";

  /** Saveset descriptor; populated as session state file is parsed */
  SavesetDescriptor _savesetDescriptor;

  /** Current data entity ID */
  String _currentEntityID;

  /** Current data entity */
  DataEntity _currentEntity;

  /** Current algorithm ID */
  String _currentAlgorithmID;

  /** Current algorithm */
  Algorithm _currentAlgorithm;

  /** Current preference page */
  PreferencePage _currentPreferencePage;

  /** Current perspective */
  Perspective _currentPerspective;

  /** Current viewer part ID */
  String _currentViewerPartId;

  /** Current viewer part */
  ViewerPart _currentViewerPart;

  /** Current viewer */
  Viewer _currentViewer;

  /** Current renderer ID */
  String _currentRendererId;

  /** Current renderer */
  Renderer _currentRenderer;

  /**
   * Read the Geocraft state from the state session file
   * which has been opened. The state is at the beginning of
   * the file and starts with "<?xml". State for workbench
   * windows occurs after the Geocraft state and each starts 
   * with an XML comment: "<!--".
   */
  private String readState() {
    StringBuffer buf = new StringBuffer();
    //read <?xml>
    String line = SessionManager.getInstance().readXML();
    if (line == null) {
      return "";
    }
    buf.append(line);
    while ((line = SessionManager.getInstance().readXML()) != null && !line.startsWith("<!--")) {
      buf.append(line);
    }
    return buf.toString();
  }

  /**
   * Parse the XML saveset file (.gcs or .gcb).
   * @param pathname Pathname of the session state file
   * @param desc Container for all parsed saveset data
   * @return true if processed XML file with no errors; otherwise, false
   */
  public boolean parseSaveset(final String pathname, final SavesetDescriptor desc) {
    //open session file for reading
    SessionManager.getInstance().openXMLReader(pathname);
    return parseSaveset(desc);
  }

  /**
   * Parse the XML saveset file (.gcs or .gcb).
   * @param desc Container for all parsed saveset data
   * @return true if processed XML file with no errors; otherwise, false
   */
  public boolean parseSaveset(final SavesetDescriptor desc) {
    _savesetDescriptor = desc;

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    try {
      DocumentBuilder db = dbf.newDocumentBuilder();
      //Document doc = db.parse(path);
      ByteArrayInputStream is = null;
      try {
        String saveset = readState();
        if (saveset.equals("")) {
          return false;
        }
        is = new ByteArrayInputStream(saveset.getBytes("UTF-8"));
      } catch (UnsupportedEncodingException uee) {
        LOGGER.error("Cannot parse Geocraft session state");
        return false;
      }
      //parse the session state into a DOM document
      Document doc = db.parse(is);
      //Walk the DOM tree, extracting all saveset metadata into the specified
      //saveset descriptor
      walkTree(doc);
    } catch (ParserConfigurationException pce) {
      //System.out.println("SavesetParser: Parser error: " + pce.getMessage());
      return false;
    } catch (SAXException saxe) {
      //System.out.println("SavesetParser: Cannot find file: " + saxe.getMessage());
      return false;
    } catch (IOException ioe) {
      //System.out.println("SavesetParser: IO error: " + ioe.getMessage());
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
    String attrName, attrValue;
    String key = "", value = "";

    switch (node.getNodeType()) {
      case Node.DOCUMENT_NODE:
        // Document object
        walkTree(((Document) node).getDocumentElement());
        break;

      case Node.ELEMENT_NODE:
        // element + attributes
        String name = node.getNodeName();
        // System.out.println("Element node: "+name);
        if (name.equals(ENTITY_NODE)) {
          _currentEntity = _savesetDescriptor.new DataEntity();

          attributes = node.getAttributes();
          // gather <entity> attributes. NOTE: they can be in any order
          for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            attrName = attr.getNodeName();
            attrValue = attr.getNodeValue();
            if (attrName.equals(ENTITY_VARNAME_ATTR)) {
              _currentEntity.setVarName(attrValue);
            } else if (attrName.equals(ENTITY_UNIQUEID_ATTR)) {
              _currentEntityID = attrValue;
              _currentEntity.setEntityUniqueId(attrValue);
              _savesetDescriptor.addEntity(_currentEntity);
            }
            // System.out.println("    Attribute: "+attrName+", Value: "+attrValue);
          }

          children = node.getChildNodes();
          if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
              Node child = children.item(i);
              // System.out.println("    Process child "+child.getNodeName());
              walkTree(child);
            }
          }
        } else if (name.equals(PROPERTY_NODE)) {
          attributes = node.getAttributes();
          key = "";
          value = "";
          // gather <property> attributes. NOTE: they can be in any order
          for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            attrName = attr.getNodeName();
            attrValue = attr.getNodeValue();
            if (attrName.equals(PROPERTY_KEY_ATTR)) {
              key = attrValue;
            } else if (attrName.equals(PROPERTY_VALUE_ATTR)) {
              value = attrValue;
            }
            // System.out.println("    Attribute: "+attrName+", Value: "+attrValue);
          }
          _currentEntity.addEntityProperty(key, value);

          children = node.getChildNodes();
          if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
              Node child = children.item(i);
              // System.out.println("    Process child "+child.getNodeName());
              walkTree(child);
            }
          }
        } else if (name.equals(ALGORITHM_NODE)) {
          _currentAlgorithm = _savesetDescriptor.new Algorithm();

          attributes = node.getAttributes();
          // gather <algorithm> attributes. NOTE: they can be in any order
          for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            attrName = attr.getNodeName();
            attrValue = attr.getNodeValue();
            if (attrName.equals(ALGORITHM_NAME_ATTR)) {
              _currentAlgorithm.setAlgorithmName(attrValue);
            } else if (attrName.equals(ALGORITHM_CLASS_ATTR)) {
              _currentAlgorithm.setAlgorithmClassName(attrValue);
            } else if (attrName.equals(ALGORITHM_WINDOW_ID_ATTR)) {
              _currentAlgorithm.setAlgorithmWindowID(attrValue);
              _savesetDescriptor.addWindowID(attrValue);
            } else if (attrName.equals(ALGORITHM_UNIQUEID_ATTR)) {
              _currentAlgorithmID = attrValue;
              _currentAlgorithm.setAlgorithmUniqueId(attrValue);
              _savesetDescriptor.addAlgorithm(_currentAlgorithm);
            } else if (attrName.equals(ALGORITHM_ACTION_ATTR)) {
              _currentAlgorithm.setAlgorithmAction(attrValue);
            }
            // System.out.println("    Attribute: "+attrName+", Value: "+attrValue);
          }

          children = node.getChildNodes();
          if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
              Node child = children.item(i);
              // System.out.println("    Process child "+child.getNodeName());
              walkTree(child);
            }
          }
        } else if (name.equals(PARAMETER_NODE)) {
          attributes = node.getAttributes();
          key = "";
          value = "";
          // gather <parameter> attributes. NOTE: they can be in any order
          for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            attrName = attr.getNodeName();
            attrValue = attr.getNodeValue();
            if (attrName.equals(PARAMETER_KEY_ATTR)) {
              key = attrValue;
            } else if (attrName.equals(PARAMETER_VALUE_ATTR)) {
              value = attrValue;
            }
            // System.out.println("    Attribute: "+attrName+", Value: "+attrValue);
          }
          value = value.replace("&#xD;&#xA;", "\r\n"); //Windows (CR+LF)
          value = value.replace("&#xA;", "\n"); //Unix (CR)
          value = value.replace("&#034;", "\"");
          value = value.replace("&#060;", "<");
          value = value.replace("&#062;", ">");
          _currentAlgorithm.addAlgorithmProperty(key, value);

          children = node.getChildNodes();
          if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
              Node child = children.item(i);
              // System.out.println("    Process child "+child.getNodeName());
              walkTree(child);
            }
          }
        } else if (name.equals(PREFERENCE_PAGE_NODE)) {
          _currentPreferencePage = _savesetDescriptor.new PreferencePage();

          attributes = node.getAttributes();
          // gather <preferencePage> attributes. NOTE: they can be in any order
          for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            attrName = attr.getNodeName();
            attrValue = attr.getNodeValue();
            if (attrName.equals(PREFERENCE_PAGE_NAME_ATTR)) {
              _currentPreferencePage.setPreferencePageName(attrValue);
            } else if (attrName.equals(PREFERENCE_PAGE_CLASS_ATTR)) {
              _currentPreferencePage.setPreferencePageClassName(attrValue);
              _savesetDescriptor.addPreferencePage(_currentPreferencePage);
            }
            // System.out.println("    Attribute: "+attrName+", Value: "+attrValue);
          }

          children = node.getChildNodes();
          if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
              Node child = children.item(i);
              // System.out.println("    Process child "+child.getNodeName());
              walkTree(child);
            }
          }
        } else if (name.equals(PREFERENCE_NODE)) {
          attributes = node.getAttributes();
          key = "";
          value = "";
          // gather <preference> attributes. NOTE: they can be in any order
          for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            attrName = attr.getNodeName();
            attrValue = attr.getNodeValue();
            if (attrName.equals(PREFERENCE_KEY_ATTR)) {
              key = attrValue;
            } else if (attrName.equals(PREFERENCE_VALUE_ATTR)) {
              value = attrValue;
            }
            // System.out.println("    Attribute: "+attrName+", Value: "+attrValue);
          }
          _currentPreferencePage.addPreference(key, value);

          children = node.getChildNodes();
          if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
              Node child = children.item(i);
              // System.out.println("    Process child "+child.getNodeName());
              walkTree(child);
            }
          }
        } else if (name.equals(PERSPECTIVE_NODE)) {
          _currentPerspective = _savesetDescriptor.new Perspective();

          attributes = node.getAttributes();
          // gather <perspective> attributes. NOTE: they can be in any order
          for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            attrName = attr.getNodeName();
            attrValue = attr.getNodeValue();
            if (attrName.equals(PERSPECTIVE_NAME_ATTR)) {
              _currentPerspective.setPerspectiveName(attrValue);
            } else if (attrName.equals(PERSPECTIVE_CLASS_ATTR)) {
              _currentPerspective.setPerspectiveClass(attrValue);
            } else if (attrName.equals(PERSPECTIVE_ID_ATTR)) {
              _currentPerspective.setPerspectiveID(attrValue);
              _savesetDescriptor.addPerspective(_currentPerspective);
            } else if (attrName.equals(PERSPECTIVE_WINDOW_ID_ATTR)) {
              _currentPerspective.setPerspectiveWindowID(attrValue);
              _savesetDescriptor.addWindowID(attrValue);
            } else if (attrName.equals(PERSPECTIVE_EDITOR_VISIBLE_ATTR)) {
              _currentPerspective.setEditorAreaVisible(attrValue.equals("true") ? true : false);
            }
          }

          children = node.getChildNodes();
          if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
              Node child = children.item(i);
              // System.out.println("    Process child "+child.getNodeName());
              walkTree(child);
            }
          }
        } else if (name.equals(VIEWER_NODE)) {
          _currentViewer = _savesetDescriptor.new Viewer();
          _currentViewerPart.addViewer(_currentViewer);

          attributes = node.getAttributes();
          // gather <viewer> attributes. NOTE: they can be in any order
          for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            attrName = attr.getNodeName();
            attrValue = attr.getNodeValue();
            if (attrName.equals(VIEWER_TITLE_ATTR)) {
              _currentViewer.setViewerTitle(attrValue);
            } else if (attrName.equals(VIEWER_CLASS_ATTR)) {
              _currentViewer.setViewerClassName(attrValue);
            }
            // System.out.println("    Attribute: "+attrName+", Value: "+attrValue);
          }

          children = node.getChildNodes();
          if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
              Node child = children.item(i);
              // System.out.println("    Process child "+child.getNodeName());
              walkTree(child);
            }
          }
        } else if (name.equals(VIEWER_LAYER_NODE)) {
          ViewerLayer viewerLayer = _currentViewer.new ViewerLayer();

          attributes = node.getAttributes();
          // gather <folderLayer> attributes. NOTE: they can be in any order
          for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            attrName = attr.getNodeName();
            attrValue = attr.getNodeValue();
            if (attrName.equals(VIEWER_LAYER_NAME_ATTR)) {
              viewerLayer.setLayerName(attrValue);
            } else if (attrName.equals(VIEWER_LAYER_CHECKED_ATTR)) {
              viewerLayer.setChecked(attrValue);
            }
          }
          _currentViewer.addViewerLayer(viewerLayer);
        } else if (name.equals(VIEWER_PROPERTY_NODE)) {
          attributes = node.getAttributes();
          key = "";
          value = "";
          // gather <viewerProperty> attributes. NOTE: they can be in any order
          for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            attrName = attr.getNodeName();
            attrValue = attr.getNodeValue();
            if (attrName.equals(VIEWER_PROPERTY_KEY_ATTR)) {
              key = attrValue;
            } else if (attrName.equals(VIEWER_PROPERTY_VALUE_ATTR)) {
              value = attrValue;
            }
            // System.out.println("    Attribute: "+attrName+", Value: "+attrValue);
          }
          value = value.replace("&#xD;&#xA;", "\r\n"); //Windows (CR+LF)
          value = value.replace("&#xA;", "\n"); //Unix (CR)
          _currentViewer.addViewerProperty(key, value);
        } else if (name.equals(RENDERER_NODE)) {
          _currentRenderer = _currentViewer.new Renderer();

          attributes = node.getAttributes();
          // gather <renderer> attributes. NOTE: they can be in any order
          for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            attrName = attr.getNodeName();
            attrValue = attr.getNodeValue();
            if (attrName.equals(RENDERER_CLASS_ATTR)) {
              _currentRenderer.setRendererClassName(attrValue);
            } else if (attrName.equals(RENDERER_UNIQUEID_ATTR)) {
              _currentRendererId = attrValue;
              _currentRenderer.setRendererUniqueId(attrValue);
              _currentViewer.addRenderer(_currentRendererId, _currentRenderer);
            }
            // System.out.println("    Attribute: "+attrName+", Value: "+attrValue);
          }

          children = node.getChildNodes();
          if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
              Node child = children.item(i);
              // System.out.println("    Process child "+child.getNodeName());
              walkTree(child);
            }
          }
        } else if (name.equals(RENDERED_ENTITY_NODE)) {
          attributes = node.getAttributes();
          // gather <entityRendered> attributes
          for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            attrName = attr.getNodeName();
            attrValue = attr.getNodeValue();
            if (attrName.equals(RENDERED_ENTITY_UNIQUEID_ATTR)) {
              _currentRenderer.setRenderedEntity(attrValue);
            }
          }
        } else if (name.equals(RENDERER_PROPERTY_NODE)) {
          attributes = node.getAttributes();
          key = "";
          value = "";
          // gather <modelProperty> attributes. NOTE: they can be in any order
          for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            attrName = attr.getNodeName();
            attrValue = attr.getNodeValue();
            if (attrName.equals(RENDERER_PROPERTY_KEY_ATTR)) {
              key = attrValue;
            } else if (attrName.equals(RENDERER_PROPERTY_VALUE_ATTR)) {
              value = attrValue;
            }
            // System.out.println("    Attribute: "+attrName+", Value: "+attrValue);
          }
          value = value.replace("&#xD;&#xA;", "\r\n"); //Windows (CR+LF)
          value = value.replace("&#xA;", "\n"); //Unix (CR)
          _currentRenderer.addRendererProperty(key, value);
        } else if (name.equals(ENTITIES_NODE)) {
          children = node.getChildNodes();
          if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
              Node child = children.item(i);
              // System.out.println("    Process child "+child.getNodeName());
              walkTree(child);
            }
          }
        } else if (name.equals(VIEWER_PART_NODE)) {
          _currentViewerPart = _savesetDescriptor.new ViewerPart();

          attributes = node.getAttributes();
          // gather <viewer> attributes. NOTE: they can be in any order
          for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            attrName = attr.getNodeName();
            attrValue = attr.getNodeValue();
            if (attrName.equals(VIEWER_PART_CLASS_ATTR)) {
              _currentViewerPart.setViewerPartClassName(attrValue);
            } else if (attrName.equals(VIEWER_PART_UNIQUEID_ATTR)) {
              _currentViewerPartId = attrValue;
              _currentViewerPart.setViewerPartUniqueId(attrValue);
              _savesetDescriptor.addViewerPart(_currentViewerPartId, _currentViewerPart);
            } else if (attrName.equals(VIEWER_PART_ID_ATTR)) {
              _currentViewerPart.setViewerPartId(attrValue);
            } else if (attrName.equals(VIEWER_PART_WINDOW_ID_ATTR)) {
              _currentViewerPart.setViewerPartWindowID(attrValue);
              _savesetDescriptor.addWindowID(attrValue);
            } else if (attrName.equals(VIEWER_PART_PERSPECTIVE_ID_ATTR)) {
              _currentViewerPart.setViewerPerspectiveID(attrValue);
            }
            // System.out.println("    Attribute: "+attrName+", Value: "+attrValue);
          }

          children = node.getChildNodes();
          if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
              Node child = children.item(i);
              // System.out.println("    Process child "+child.getNodeName());
              walkTree(child);
            }
          }
        } else if (name.equals(PLOT_WINDOW_NODE)) {
          attributes = node.getAttributes();
          // gather <plotWindow> attributes. NOTE: they can be in any order
          for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            attrName = attr.getNodeName();
            attrValue = attr.getNodeValue();
            if (attrName.equals(PLOT_WINDOW_ID_ATTR)) {
              _savesetDescriptor.setPlotWindowID(attrValue);
            }
            //System.out.println("    Attribute: " + attrName + ", Value: " + attrValue);
          }
        } else if (name.equals(ALGORITHMS_NODE)) {
          children = node.getChildNodes();
          if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
              Node child = children.item(i);
              // System.out.println("    Process child "+child.getNodeName());
              walkTree(child);
            }
          }
        } else if (name.equals(REPOSITORY_NODE)) {
          children = node.getChildNodes();
          if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
              Node child = children.item(i);
              // System.out.println("    Process child "+child.getNodeName());
              walkTree(child);
            }
          }
        } else if (name.equals(ROOT_NODE)) {
          attributes = node.getAttributes();
          String version = "", width = "1000", height = "1000", x = "0", y = "0";
          // gather <geocraft> attributes. NOTE: they can be in any order
          for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            attrName = attr.getNodeName();
            attrValue = attr.getNodeValue();
            if (attrName.equals(ROOT_VERSION_ATTR)) {
              version = attrValue;
            } else if (attrName.equals(ROOT_WIDTH_ATTR)) {
              width = attrValue;
            } else if (attrName.equals(ROOT_HEIGHT_ATTR)) {
              height = attrValue;
            } else if (attrName.equals(ROOT_X_ATTR)) {
              x = attrValue;
            } else if (attrName.equals(ROOT_Y_ATTR)) {
              y = attrValue;
            }
            //  System.out.println("    Attribute: "+attrName+", Value: "+attrValue);
          }

          _savesetDescriptor.setWindowAttributes(version, width, height, x, y);

          children = node.getChildNodes();
          if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
              Node child = children.item(i);
              // System.out.println("    Process child "+child.getNodeName());
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
