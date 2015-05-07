/*
 * Copyright (C) ConocoPhillips 2010 All Rights Reserved. 
 */

package org.geocraft.core.session;


import java.util.ArrayList;
import java.util.HashMap;

import org.geocraft.core.session.SavesetDescriptor.Viewer.Renderer;
import org.geocraft.core.session.SavesetDescriptor.Viewer.ViewerLayer;


/**
 * Descriptor for a saveset, i.e, container for all saveset data
 * @author hansegj
 */
public class SavesetDescriptor {

  /** Pathname of the saveset */
  String _savesetPath = "";

  /** List of unique workbench window IDs */
  ArrayList<String> _windowIds;

  /** Ordered list of entities (var names) */
  ArrayList<DataEntity> _entities;

  /** Ordered list of algorithm */
  ArrayList<Algorithm> _algorithms;

  /** Ordered list of preference pages */
  ArrayList<PreferencePage> _preferencePages;

  //** Ordered list of perspectives */
  ArrayList<Perspective> _perspectives;

  /** Map of viewer parts. key is viewer part's unique ID.  */
  HashMap<String, ViewerPart> _viewerParts;

  /** ID of the detached workbench window containing plots */
  String _plotWindowID;

  // GeoCraft window attributes
  String _width, _height, _x, _y;

  // Version of GeoCraft
  String _version;

  public SavesetDescriptor() {
    _entities = new ArrayList<DataEntity>();

    _algorithms = new ArrayList<Algorithm>();

    _preferencePages = new ArrayList<PreferencePage>();

    _perspectives = new ArrayList<Perspective>();

    _viewerParts = new HashMap<String, ViewerPart>();

    _windowIds = new ArrayList<String>();

    _plotWindowID = "";
  }

  // GETTERS

  /** Get the path of the saveset */
  public String getSavesetPath() {
    return _savesetPath;
  }

  /** Get the ordered list of entities (var names) */
  public ArrayList<DataEntity> getEntities() {
    return _entities;
  }

  /** Get the ordered list of algorithms */
  public ArrayList<Algorithm> getAlgorithms() {
    return _algorithms;
  }

  /** Get the ordered list of preference pages */
  public ArrayList<PreferencePage> getPreferencePages() {
    return _preferencePages;
  }

  /**
   * Get the algorithms associated with a window ID
   * @param winID Workbench window ID
   * @return List of associated algorithms
   */
  public ArrayList<Algorithm> getAlgorithms(String winID) {
    ArrayList<Algorithm> algorithms = new ArrayList<Algorithm>();
    for (Algorithm algorithm : _algorithms) {
      if (algorithm.getAlgorithmWindowID().equals(winID)) {
        algorithms.add(algorithm);
      }
    }
    return algorithms;
  }

  /** Get the ordered list of perspectives */
  public ArrayList<Perspective> getPerspectives() {
    return _perspectives;
  }

  /**
   * Get the perspectives associated with a window ID
   * @param winID Workbench window ID
   * @return List of associated perspectives
   */
  public ArrayList<Perspective> getPerspectives(String winID) {
    ArrayList<Perspective> perspectives = new ArrayList<Perspective>();
    for (Perspective perspective : _perspectives) {
      if (perspective.getPerspectiveWindowID().equals(winID)) {
        perspectives.add(perspective);
      }
    }
    return perspectives;
  }

  /** Get the ordered list of the viewer parts for all perspectives */
  public HashMap<String, ViewerPart> getViewerParts() {
    return _viewerParts;
  }

  /**
   * Get the viewer parts associated with a window ID
   * @param winID Workbench window ID
   * @return List of associated viewer parts
   */
  public ArrayList<ViewerPart> getViewerParts(String winID) {
    ArrayList<ViewerPart> parts = new ArrayList<ViewerPart>();
    for (ViewerPart part : _viewerParts.values()) {
      if (part.getViewerPartWindowID().equals(winID)) {
        parts.add(part);
      }
    }
    return parts;
  }

  /** Get the list of workbench window IDs */
  public ArrayList<String> getWorkbenchWindows() {
    return _windowIds;
  }

  public String getPlotWindowID() {
    return _plotWindowID;
  }

  // SETTERS
  public void setWindowAttributes(final String version, final String width, final String height, final String x,
      final String y) {
    _version = version;
    _width = width;
    _height = height;
    _x = x;
    _y = y;
  }

  public void addEntity(final DataEntity entity) {
    _entities.add(entity);
  }

  public void addAlgorithm(final Algorithm algorithm) {
    _algorithms.add(algorithm);
  }

  public void addPerspective(final Perspective perspective) {
    _perspectives.add(perspective);
  }

  public void addPreferencePage(final PreferencePage prefPage) {
    _preferencePages.add(prefPage);
  }

  public void addViewerPart(final String uniqueId, final ViewerPart viewerPart) {
    _viewerParts.put(uniqueId, viewerPart);
  }

  public void addWindowID(final String winID) {
    boolean inList = false;
    for (String id : _windowIds) {
      if (winID.equals(id)) {
        inList = true;
        break;
      }

    }
    if (!inList) {
      _windowIds.add(winID);
    }
  }

  public void setPlotWindowID(String winID) {
    _plotWindowID = winID;
  }

  /** Output saveset descriptor */
  public void dumpDescriptor() {
    System.out.println("Saveset descriptor for saveset: " + _savesetPath);

    String indent = "  ";
    System.out.println(indent + "# of  entities = " + _entities.size());
    for (DataEntity entity : _entities) {
      System.out.println(indent + "Entity: " + entity.getVarName() + ", uniqueID = " + entity.getEntityUniqueId());
      System.out.println(indent + "Total # of properties = " + entity.getNumEntityProperties());
      ArrayList<EntityModelProperty> properties = entity.getEntityProperties();
      for (EntityModelProperty property : properties) {
        System.out.println(indent + indent + "Property: key = " + property.getKey() + ", value = "
            + property.getValue());
      }
    }

    indent = "  ";
    System.out.println(indent + "# of algorithms = " + _algorithms.size());
    for (Algorithm algorithm : _algorithms) {
      System.out.println(indent + "Algorithm: " + algorithm.getAlgorithmName() + ", windowID="
          + algorithm.getAlgorithmWindowID() + ", action=" + algorithm.getAlgorithmAction());
      System.out.println(indent + "Total # of parameters = " + algorithm.getNumAlgorithmProperties());

      ArrayList<AlgorithmParameter> parameters = algorithm.getAlgorithmProperties();
      for (AlgorithmParameter param : parameters) {
        System.out.println(indent + indent + "Parameter: key = " + param.getKey() + ", value = " + param.getValue());
      }
    }

    indent = "  ";
    System.out.println(indent + "# of  preference pages = " + _preferencePages.size());
    for (PreferencePage prefPage : _preferencePages) {
      System.out.println(indent + "Preference Page: " + prefPage.getPreferencePageName());
      System.out.println(indent + "Total # of preferences = " + prefPage.getNumPreferences());

      ArrayList<Preference> preferences = prefPage.getPreferences();
      for (Preference pref : preferences) {
        System.out.println(indent + indent + "Preference: key = " + pref.getKey() + ", value = " + pref.getValue());
      }
    }

    indent = "  ";
    System.out.println(indent + "# of  perspectives = " + _perspectives.size());
    for (Perspective perspective : _perspectives) {
      System.out.println(indent + "Perspective: " + perspective.getPerspectiveName() + ", id="
          + perspective.getPerspectiveID() + ", class=" + perspective.getPerspectiveClass() + "windowID="
          + perspective.getPerspectiveWindowID());
    }

    indent = "  ";
    System.out.println(indent + "plot window ID =" + _plotWindowID);

    indent = "  ";
    System.out.println(indent + "# of  viewer parts = " + _viewerParts.size());
    for (String uniqueID : _viewerParts.keySet()) {
      ViewerPart viewerPart = _viewerParts.get(uniqueID);
      System.out.println(indent + "Viewer part: " + viewerPart.getViewerPartId() + ", class ="
          + viewerPart.getViewerPartClassName() + ", windowID=" + viewerPart.getViewerPartWindowID()
          + ", perspectiveID=" + viewerPart.getViewerPerspectiveID() + ", uniqueID ="
          + viewerPart.getViewerPartUniqueID());
      for (Viewer viewer : viewerPart.getViewers()) {
        System.out.println(indent + indent + "Viewer: " + viewer.getViewerTitle() + ", class ="
            + viewer.getViewerClassName());
        ArrayList<ViewerLayer> viewerLayers = viewer.getViewerLayers();
        for (ViewerLayer viewerLayer : viewerLayers) {
          System.out.println(indent + indent + indent + "Viewer layer: name = " + viewerLayer.getLayerName()
              + ", checked = " + viewerLayer.isChecked());
        }
        ArrayList<ViewerProperty> properties = viewer.getViewerProperties();
        for (ViewerProperty prop : properties) {
          System.out.println(indent + indent + indent + "Property: key = " + prop.getKey() + ", value = "
              + prop.getValue());
        }
        for (String uniqueId : viewer.getViewerRenderers().keySet()) {
          Renderer renderer = viewer.getViewerRenderers().get(uniqueId);
          System.out.println(indent + indent + indent + "Renderer: " + renderer.getRendererClassName() + ", uniqueID ="
              + renderer.getRendererUniqueId());
          System.out.println(indent + indent + indent + indent + "Rendered Entity: " + renderer.getRenderedEntity());
          ArrayList<RendererProperty> rprops = renderer.getRendererProperties();
          for (RendererProperty prop : rprops) {
            System.out.println(indent + indent + indent + indent + "Property: key = " + prop.getKey() + ", value = "
                + prop.getValue());
          }
        }
      }
    }
  }

  /**
   * Individual data entity
   */
  public class DataEntity {

    /** Data entity's var name */
    String _varName = "";

    /** Unique entity ID */
    String _entityUniqueId = "";

    /** Entity's ordered list of properties */
    ArrayList<EntityModelProperty> _entityProperties;

    public DataEntity() {
      _entityProperties = new ArrayList<EntityModelProperty>();
    }

    //GETTERS
    public String getVarName() {
      return _varName;
    }

    public String getEntityUniqueId() {
      return _entityUniqueId;
    }

    /** Get the entity's ordered list of properties */
    public ArrayList<EntityModelProperty> getEntityProperties() {
      return _entityProperties;
    }

    /** Get the number of entity properties */
    public int getNumEntityProperties() {
      return _entityProperties.size();
    }

    //SETTERS

    public void setVarName(String varName) {
      _varName = varName;
    }

    public void setEntityUniqueId(String uniqueId) {
      _entityUniqueId = uniqueId;
    }

    public void addEntityProperty(final String key, final String value) {
      _entityProperties.add(new EntityModelProperty(key, value, _entityUniqueId));
    }
  }

  /**
   * Individual algorithm
   */
  public class Algorithm {

    /** Name of the algorithm */
    String _algorithmName = "";

    /** Unique algorithm ID */
    String _algorithmUniqueId = "";

    /** Algorithm class name */
    String _algorithmClassName = "";

    /** ID of workbench window containing algorithm */
    String _windowID = "";

    /** Algorithm action */
    String _algorithmAction = "";

    /** Algorithm's ordered list properties */
    ArrayList<AlgorithmParameter> _algorithmProperties;

    public Algorithm() {
      _algorithmProperties = new ArrayList<AlgorithmParameter>();
    }

    //GETTERS
    /** Get the algorithm's name */
    public String getAlgorithmName() {
      return _algorithmName;
    }

    /** Get the algorithms's unique IDs */
    public String getAlgorithmUniqueID() {
      return _algorithmUniqueId;
    }

    /** Get the algorithm's class name */
    public String getAlgorithmClassName() {
      return _algorithmClassName;
    }

    public String getAlgorithmWindowID() {
      return _windowID;
    }

    /** Get the algorithm's action */
    public String getAlgorithmAction() {
      return _algorithmAction;
    }

    /** Get the algorithm's ordered list of properties */
    public ArrayList<AlgorithmParameter> getAlgorithmProperties() {
      return _algorithmProperties;
    }

    /** Get the number of algorithm properties */
    public int getNumAlgorithmProperties() {
      return _algorithmProperties.size();
    }

    //SETTERS
    public void setAlgorithmName(final String name) {
      _algorithmName = name;
    }

    public void setAlgorithmUniqueId(final String uniqueId) {
      _algorithmUniqueId = uniqueId;
    }

    public void setAlgorithmClassName(final String className) {
      _algorithmClassName = className;
    }

    public void setAlgorithmWindowID(final String winID) {
      _windowID = winID;
    }

    public void setAlgorithmAction(final String action) {
      _algorithmAction = action;
    }

    public void addAlgorithmProperty(final String key, final String value) {
      _algorithmProperties.add(new AlgorithmParameter(key, value, _algorithmUniqueId));
    }
  }

  /**
   * Individual preference page
   */
  public class PreferencePage {

    /** Name of the preference page */
    String _preferencePageName = "";

    /** Preference page class name */
    String _preferencePageClassName = "";

    /** Preference pages's ordered list preferences */
    ArrayList<Preference> _preferences;

    public PreferencePage() {
      _preferences = new ArrayList<Preference>();
    }

    //GETTERS
    /** Get the preference page's name */
    public String getPreferencePageName() {
      return _preferencePageName;
    }

    /** Get the preference page's class name */
    public String getPreferencePageClassName() {
      return _preferencePageClassName;
    }

    /** Get the preference page's ordered list of preferences */
    public ArrayList<Preference> getPreferences() {
      return _preferences;
    }

    /** Get the number of preferences */
    public int getNumPreferences() {
      return _preferences.size();
    }

    //SETTERS
    public void setPreferencePageName(final String name) {
      _preferencePageName = name;
    }

    public void setPreferencePageClassName(final String className) {
      _preferencePageClassName = className;
    }

    public void addPreference(final String key, final String value) {
      _preferences.add(new Preference(key, value));
    }
  }

  /**
   * Individual viewer part; contains list of viewers
   * @author hansegj
   *
   */
  public class ViewerPart {

    /** Viewer part's class name */
    String _viewerPartClassName = "";

    /** Viewer part's part ID */
    String _viewerPartId = "";

    String _windowID = "";

    String _perspectiveID = "";

    /** Viewer part' unique ID */
    String _viewerPartUniqueId = "";

    /** Viewer part's ordered list viewers */
    ArrayList<Viewer> _viewers;

    public ViewerPart() {
      _viewers = new ArrayList<Viewer>();
    }

    //GETTERS
    /** Get the viewer part's part ID */
    public String getViewerPartId() {
      return _viewerPartId;
    }

    /** Get the viewer part's unique IDs */
    public String getViewerPartUniqueID() {
      return _viewerPartUniqueId;
    }

    /** Get the viewer part's class name */
    public String getViewerPartClassName() {
      return _viewerPartClassName;
    }

    public String getViewerPartWindowID() {
      return _windowID;
    }

    public String getViewerPerspectiveID() {
      return _perspectiveID;
    }

    /** Get the viewer part's ordered list of viewers */
    public ArrayList<Viewer> getViewers() {
      return _viewers;
    }

    /** Get the number of viewer part's viewers */
    public int getNumViewers() {
      return _viewers.size();
    }

    //SETTERS
    public void setViewerPartId(final String partId) {
      _viewerPartId = partId;
    }

    /**
     * Add viewer part's unique ID
     * @param uniqueId Viewer part's unique ID
     */
    public void setViewerPartUniqueId(final String uniqueId) {
      _viewerPartUniqueId = uniqueId;
    }

    /**
     * Add viewer part's fully qualified class name
     * @param className Viewer part's class name
     */
    public void setViewerPartClassName(final String className) {
      _viewerPartClassName = className;
    }

    public void setViewerPartWindowID(final String winID) {
      _windowID = winID;
    }

    public void setViewerPerspectiveID(final String perspectiveID) {
      _perspectiveID = perspectiveID;
    }

    /**
     * Add a viewer associated with the viewer part
     * @param viewer Viewer
     */
    public void addViewer(Viewer viewer) {
      _viewers.add(viewer);
    }
  }

  /**
   * Individual viewer
   */
  public class Viewer {

    /** Viewer title */
    String _viewerTitle = "";

    /** Viewer class name */
    String _viewerClassName = "";

    /** Viewer's ordered list of properties */
    ArrayList<ViewerProperty> _viewerProperties;

    /** Viewer's list of layers (root and entity) */
    ArrayList<ViewerLayer> _viewerLayers;

    /** Map of renderers for a viewer. key is renderer's unique ID.  */
    HashMap<String, Renderer> _renderers;

    public Viewer() {
      _viewerProperties = new ArrayList<ViewerProperty>();
      _viewerLayers = new ArrayList<ViewerLayer>();
      _renderers = new HashMap<String, Renderer>();
    }

    //GETTERS

    /** Get the viewer's title */
    public String getViewerTitle() {
      return _viewerTitle;
    }

    /** Get the viewer's class name */
    public String getViewerClassName() {
      return _viewerClassName;
    }

    /** Get the viewer's ordered list of properties */
    public ArrayList<ViewerProperty> getViewerProperties() {
      return _viewerProperties;
    }

    /** Get the number of viewer properties */
    public int getNumViewerProperties() {
      return _viewerProperties.size();
    }

    /** Get the viewer's list of renderers */
    public HashMap<String, Renderer> getViewerRenderers() {
      return _renderers;
    }

    /** Get the number of viewer renderers */
    public int getNumViewerRenderers() {
      return _renderers.size();
    }

    /** Get the viewer's list of layers */
    public ArrayList<ViewerLayer> getViewerLayers() {
      return _viewerLayers;
    }

    //SETTERS
    public void addRenderer(final String uniqueId, final Renderer renderer) {
      _renderers.put(uniqueId, renderer);
    }

    public void setViewerTitle(final String title) {
      _viewerTitle = title;
    }

    public void setViewerClassName(final String className) {
      _viewerClassName = className;
    }

    public void addViewerProperty(final String key, final String value) {
      _viewerProperties.add(new ViewerProperty(key, value));
    }

    public void addViewerLayer(ViewerLayer layer) {
      _viewerLayers.add(layer);
    }

    /**
     * Individual viewer layer, root or entity (in the Layers tab)
     */
    public class ViewerLayer {

      String _layerName;

      String _layerChecked;

      public ViewerLayer() {
        _layerName = "";
        _layerChecked = "";
      }

      public ViewerLayer(String layerName, String checked) {
        _layerName = layerName;
        _layerChecked = checked;
      }

      //GETTERS
      public String getLayerName() {
        return _layerName;
      }

      public boolean isChecked() {
        return _layerChecked.equals("true") ? true : false;
      }

      //SETTERS
      public void setLayerName(String layerName) {
        _layerName = layerName;
      }

      public void setChecked(String checked) {
        _layerChecked = checked;
      }
    }

    /**
     * Individual viewer renderer
     */
    public class Renderer {

      String _rendererClassName = "";

      String _rendererUniqueId = "";

      String _renderedEntity = "";

      /** Renderer's ordered list of properties */
      ArrayList<RendererProperty> _rendererProperties;

      public Renderer() {
        _rendererProperties = new ArrayList<RendererProperty>();
      }

      //GETTERS
      /** Get the renderer's unique ID */
      public String getRendererUniqueId() {
        return _rendererUniqueId;
      }

      /** Get the renderer's class name */
      public String getRendererClassName() {
        return _rendererClassName;
      }

      /** Get the renderer's ordered list of properties */
      public ArrayList<RendererProperty> getRendererProperties() {
        return _rendererProperties;
      }

      /** Get the number of renderer properties */
      public int getNumRendererProperties() {
        return _rendererProperties.size();
      }

      public String getRenderedEntity() {
        return _renderedEntity;
      }

      //SETTERS
      /**
       * Add renderer's unique ID
       * @param uniqueId Renderer's unique ID
       */
      public void setRendererUniqueId(final String uniqueId) {
        _rendererUniqueId = uniqueId;
      }

      public void setRendererClassName(final String className) {
        _rendererClassName = className;
      }

      public void addRendererProperty(final String key, final String value) {
        _rendererProperties.add(new RendererProperty(key, value, _rendererUniqueId));
      }

      public void setRenderedEntity(final String entityId) {
        _renderedEntity = entityId;
      }
    }
  }

  /**
   * Individual perspective
   */
  class Perspective {

    String _perspectiveID = "";

    String _perspectiveName = "";

    String _perspectiveClass = "";

    String _windowID = "";

    boolean _isEditorAreaVisible = false;

    //GETTERS
    public String getPerspectiveID() {
      return _perspectiveID;
    }

    public String getPerspectiveName() {
      return _perspectiveName;
    }

    public String getPerspectiveClass() {
      return _perspectiveClass;
    }

    public String getPerspectiveWindowID() {
      return _windowID;
    }

    public boolean isEditorAreaVisible() {
      return _isEditorAreaVisible;
    }

    //SETTERS

    public void setPerspectiveID(String id) {
      _perspectiveID = id;
    }

    public void setPerspectiveName(String name) {
      _perspectiveName = name;
    }

    public void setPerspectiveClass(String klass) {
      _perspectiveClass = klass;
    }

    public void setPerspectiveWindowID(String winID) {
      _windowID = winID;
    }

    public void setEditorAreaVisible(boolean isVisible) {
      _isEditorAreaVisible = isVisible;
    }
  }

  /**
   * Individual model property of an entity
   */
  public class EntityModelProperty {

    // Atributes of an entity property
    String key, value, varName;

    /**
     * Model property constructor
     * @param key Model property name.
     * @param value The value of the model property.
     * @param varName The entity's var name
     */
    public EntityModelProperty(final String key, final String value, final String varName) {
      this.key = key;
      this.value = value;
      this.varName = varName;
    }

    public String getKey() {
      return key;
    }

    public String getValue() {
      return value;
    }

    public String getVarName() {
      return varName;
    }
  }

  /**
   * Individual parameter of an algorithm
   */
  public class AlgorithmParameter {

    // Attributes of an algorithm parameter
    String key, value, uniqueID;

    /**
     * Algorithm parameter constructor
     * @param key Parameter name.
     * @param value The value of the parameter.
     * @param uniqueID The algorithm's unique ID
     */
    public AlgorithmParameter(final String key, final String value, final String uniqueID) {
      this.key = key;
      this.value = value;
      this.uniqueID = uniqueID;
    }

    public String getKey() {
      return key;
    }

    public String getValue() {
      return value;
    }

    public String getUniqueID() {
      return uniqueID;
    }
  }

  /**
   * Individual preference of a preference page
   */
  public class Preference {

    // Attributes of a preference
    String key, value;

    /**
     * Preference constructor
     * @param key Preference name.
     * @param value The value of the preference.
     */
    public Preference(final String key, final String value) {
      this.key = key;
      this.value = value;
    }

    public String getKey() {
      return key;
    }

    public String getValue() {
      return value;
    }
  }

  /**
   * Individual property of a viewer
   */
  public class ViewerProperty {

    // Attributes of a viewer property
    String key, value, uniqueID;

    /**
     * Viewer property constructor
     * @param key property name.
     * @param value The value of the property.
     */
    public ViewerProperty(final String key, final String value) {
      this.key = key;
      this.value = value;
    }

    public String getKey() {
      return key;
    }

    public String getValue() {
      return value;
    }
  }

  /**
   * Individual property of a renderer
   */
  public class RendererProperty {

    // Attributes of a renderer property
    String key, value, uniqueID;

    /**
     * Renderer property constructor
     * @param key property name.
     * @param value The value of the property.
     * @param uniqueID The renderer's unique ID
     */
    public RendererProperty(final String key, final String value, final String uniqueID) {
      this.key = key;
      this.value = value;
      this.uniqueID = uniqueID;
    }

    public String getKey() {
      return key;
    }

    public String getValue() {
      return value;
    }

    public String getUniqueID() {
      return uniqueID;
    }
  }
}
