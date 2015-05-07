/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.session;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.geocraft.core.common.xml.XmlIO;
import org.geocraft.core.common.xml.XmlUtils;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.algorithm.IAlgorithmsService;
import org.osgi.service.prefs.BackingStoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class Session implements XmlIO {

  private static final String GEOCRAFT_TAG = "geocraft";

  private static final String SESSION_ATTRIBUTE = "session";

  private static final String REPOSITORY_TAG = "repository";

  private static final String ENTITY_TAG = "entity";

  private static final String MAPPER_MODEL_TAG = "mapperModel";

  private static final String UNIQUE_ID_ATTRIBUTE = "uniqueID";

  private static final String VAR_NAME_ATTRIBUTE = "varName";

  private static final String KEY_ATTRIBUTE = "key";

  private static final String VALUE_ATTRIBUTE = "value";

  private static final String NAME_ATTRIBUTE = "name";

  private static final String CLASS_NAME_ATTRIBUTE = "className";

  private static final String PROPERTY_TAG = "property";

  private static final String WORKBENCH_TAG = "workbench";

  private static final String WORKBENCH_WINDOW_TAG = "window";

  private static final String ALGORITHMS_TAG = "algorithms";

  private static final String ALGORITHM_TAG = "algorithm";

  private static final String VIEWER_PARTS_TAG = "viewerParts";

  private static final String VIEWER_PART_TAG = "viewerPart";

  private static final String VIEWER_TAG = "viewer";

  private static final String PART_ID_ATTRIBUTE = "PartID";

  private static final String RENDERER_TAG = "renderer";

  private static final String ACTIVE_PERSPECTIVE_ATTRIBUTE = "activePerspective";

  public final String _sessionName;

  public Session(String sessionName) {
    _sessionName = sessionName;
  }

  /**
   * Saves the current state of GeoCraft.
   * <p>
   * This includes the workbench layout, repository, algorithms and viewers.
   */
  public void getXML(final Document doc, final Node parent) throws Exception {
    Element geocraftNode = null;
    if (parent == null) {
      geocraftNode = doc.createElement(GEOCRAFT_TAG);
      doc.appendChild(geocraftNode);
    } else {
      geocraftNode = doc.createElement(GEOCRAFT_TAG);
      parent.appendChild(geocraftNode);
    }
    geocraftNode.setAttribute(SESSION_ATTRIBUTE, _sessionName);

    // Save the state of the repository.
    Element repositoryNode = doc.createElement(REPOSITORY_TAG);
    geocraftNode.appendChild(repositoryNode);
    saveRepositoryState(doc, repositoryNode);

    // Save the workbench layout.
    Element workbenchNode = doc.createElement(WORKBENCH_TAG);
    geocraftNode.appendChild(workbenchNode);
    saveWorkbenchState(doc, workbenchNode);

    // Save the state of the algorithms.
    Element algorithmsNode = doc.createElement(ALGORITHMS_TAG);
    geocraftNode.appendChild(algorithmsNode);
    saveAlgorithmsState(doc, algorithmsNode);

    // Save the state of the viewers.
    Element viewersNode = doc.createElement(VIEWER_PARTS_TAG);
    geocraftNode.appendChild(viewersNode);
    saveViewersState(doc, viewersNode);
  }

  /**
   * Saves the state of the repository.
   * 
   * @param doc the document.
   * @param repositoryNode the repository node within the document.
   */
  private static void saveRepositoryState(final Document doc, final Element repositoryNode) throws Exception {
    Map<String, Object> objectMap = ServiceProvider.getRepository().getAll();
    Set<String> keyset = objectMap.keySet();
    for (String key : keyset) {
      Object object = objectMap.get(key);
      if (Entity.class.isAssignableFrom(object.getClass())) {
        Entity entity = (Entity) object;
        IMapper mapper = entity.getMapper();
        if (mapper != null) {
          MapperModel model = mapper.getModel();
          Element entityNode = XmlUtils.addElement(doc, repositoryNode, ENTITY_TAG);
          entityNode.setAttribute(UNIQUE_ID_ATTRIBUTE, entity.getUniqueID());
          entityNode.setAttribute(VAR_NAME_ATTRIBUTE, key);
          propertiesToXML(doc, entityNode, model.pickle());
        }
      }
    }
  }

  /**
   * Saves the state of the algorithms.
   * 
   * @param doc the document.
   * @param algorithmsNode the algorithms node within the document.
   */
  private static void saveAlgorithmsState(final Document doc, final Element algorithmsNode) {
    IAlgorithmsService service = ServiceProvider.getAlgorithmsService();
    Map<Integer, Object> algorithmMap = service.getRegisteredAlgorithms();
    for (Integer akey : algorithmMap.keySet()) {
      Element algorithmNode = doc.createElement(ALGORITHM_TAG);
      algorithmsNode.appendChild(algorithmNode);

      String name = service.getAlgorithmName(akey);
      String className = service.getAlgorithmClassName(akey);
      algorithmNode.setAttribute(NAME_ATTRIBUTE, name);
      algorithmNode.setAttribute(CLASS_NAME_ATTRIBUTE, className);

      Map<String, String> parameterMap = service.getAlgorithmParms(akey);
      propertiesToXML(doc, algorithmNode, parameterMap);
    }
  }

  /**
   * Saves the state of the viewers.
   * 
   * @param doc the document.
   * @param viewersNode the viewers node within the document.
   */
  private static void saveViewersState(final Document doc, final Element viewersNode) {
    // TODO: Implement this.
  }

  /**
   * Saves the state of the repository.
   * 
   * @param doc the document.
   * @param repositoryNode the repository node within the document.
   */
  private static void saveWorkbenchState(final Document doc, final Element workbenchNode) throws Exception {
    for (IWorkbenchWindow iwindow : PlatformUI.getWorkbench().getWorkbenchWindows()) {
      WorkbenchWindow window = (WorkbenchWindow) iwindow;
      Element windowNode = doc.createElement(WORKBENCH_WINDOW_TAG);
      workbenchNode.appendChild(windowNode);
      XMLMemento memento = new XMLMemento(doc, windowNode);
      window.saveState(memento);
    }
  }

  /**
   * Restores the current state of GeoCraft.
   * <p>
   * This includes the workbench layout, repository, algorithms and viewers.
   */
  public void setXML(final Document doc, final Node parent) throws Exception {
    Element geocraftNode = null;
    if (parent == null) {
      geocraftNode = doc.createElement(GEOCRAFT_TAG);
      doc.appendChild(geocraftNode);
    } else {
      geocraftNode = doc.createElement(GEOCRAFT_TAG);
      parent.appendChild(geocraftNode);
    }

    // Restore the state of the repository.
    NodeList repositoryNodes = doc.getElementsByTagName(REPOSITORY_TAG);
    if (repositoryNodes.getLength() > 0) {
      Element repositoryNode = (Element) repositoryNodes.item(0);
      restoreRepositoryState(doc, repositoryNode);
    }

    // Restore the workbench layout.
    NodeList workbenchNodes = doc.getElementsByTagName(WORKBENCH_TAG);
    if (workbenchNodes.getLength() > 0) {
      Element workbenchNode = (Element) workbenchNodes.item(0);
      restoreWorkbenchState(doc, workbenchNode);
    }

    // Restore the state of the algorithms.
    NodeList algorithmsNodes = doc.getElementsByTagName(ALGORITHMS_TAG);
    if (algorithmsNodes.getLength() > 0) {
      Element algorithmsNode = (Element) algorithmsNodes.item(0);
      restoreAlgorithmsState(doc, algorithmsNode);
    }

    // Restore the state of the viewers.
    NodeList viewersNodes = doc.getElementsByTagName(VIEWER_PARTS_TAG);
    if (viewersNodes.getLength() > 0) {
      Element viewersNode = (Element) viewersNodes.item(0);
      restoreViewersState(doc, viewersNode);
    }

  }

  /**
   * Restores the state of the repository.
   * 
   * @param doc the document.
   * @param repositoryNode the repository node within the document.
   */
  public static void restoreRepositoryState(final Document doc, final Element repositoryNode) {
    IRepository repository = ServiceProvider.getRepository();
    if (repository == null) {
      throw new RuntimeException("Could not find repository!");
    }
    System.out.println("CLEARING REPOSITORY");
    repository.clear();

    NodeList entityNodes = repositoryNode.getElementsByTagName(ENTITY_TAG);
    HashMap<String, String> varNamesMap = new HashMap<String, String>();
    for (int i = 0; i < entityNodes.getLength(); i++) {
      Element entityNode = (Element) entityNodes.item(i);
      String uniqueID = entityNode.getAttribute(UNIQUE_ID_ATTRIBUTE);
      String varName = entityNode.getAttribute(VAR_NAME_ATTRIBUTE);
      varNamesMap.put(uniqueID, varName);
    }
    repository.setVarnameMap(varNamesMap);

    for (int i = 0; i < entityNodes.getLength(); i++) {
      Element entityNode = (Element) entityNodes.item(i);
      String uniqueID = entityNode.getAttribute(UNIQUE_ID_ATTRIBUTE);
      Map<String, String> properties = propertiesFromXML(doc, entityNode);
      try {
        System.out.println("RESTORING: " + uniqueID);
        RepositoryIdStore.restoreEntity(properties, uniqueID);
      } catch (BackingStoreException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  /**
   * Restores the state of the workbench windows.
   * 
   * @param doc the document.
   * @param workbenchNode the workbench node within the document.
   */
  public static void restoreWorkbenchState(final Document doc, final Element workbenchNode) throws Exception {
    Workbench workbench = (Workbench) PlatformUI.getWorkbench();
    IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
    int numWindows = workbench.getWorkbenchWindowCount();
    for (int i = numWindows - 1; i > 0; i--) {
      windows[i].close();
    }
    NodeList windowNodes = workbenchNode.getElementsByTagName(WORKBENCH_WINDOW_TAG);
    numWindows = windowNodes.getLength();
    for (int i = 0; i < numWindows; i++) {
      Element windowNode = (Element) windowNodes.item(i);
      XMLMemento memento = new XMLMemento(doc, windowNode);
      String activePerspectiveId = "";
      NodeList perspectivesList = windowNode.getElementsByTagName("perspectives");
      IPerspectiveDescriptor activePerspective = null;
      if (perspectivesList.getLength() > 0) {
        Element perspectivesNode = (Element) perspectivesList.item(0);
        activePerspectiveId = perspectivesNode.getAttribute(ACTIVE_PERSPECTIVE_ATTRIBUTE);
        if (activePerspectiveId.isEmpty()) {
          activePerspective = PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(
              activePerspectiveId);
        }
      }

      WorkbenchWindow window = (WorkbenchWindow) PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      if (i > 0) {
        IAdaptable input = workbench.getDefaultPageInput();
        window = (WorkbenchWindow) PlatformUI.getWorkbench().openWorkbenchWindow(activePerspectiveId, input);
      }
      for (IWorkbenchPage page : window.getPages()) {
        page.closeAllPerspectives(true, true);
      }
      window.restoreState(memento, activePerspective);
    }
  }

  /**
   * Restores the state of the algorithms.
   * 
   * @param doc the document.
   * @param algorithmsNode the algorithms node within the document.
   */
  public static void restoreAlgorithmsState(final Document doc, final Element algorithmsNode) {
    IAlgorithmsService service = ServiceProvider.getAlgorithmsService();
    Set<Integer> keyset = service.getRegisteredAlgorithms().keySet();
    for (Integer akey : keyset) {
      service.deactivate(akey);
    }
    NodeList algorithmNodes = algorithmsNode.getElementsByTagName(ALGORITHM_TAG);
    int numAlgorithms = algorithmNodes.getLength();
    for (int i = 0; i < numAlgorithms; i++) {
      Element algorithmNode = (Element) algorithmNodes.item(i);
      String className = algorithmNode.getAttribute(CLASS_NAME_ATTRIBUTE);
      Map<String, String> properties = propertiesFromXML(doc, algorithmNode);
      service.activate(className, properties);
    }
  }

  /**
   * Restores the state of the viewers.
   * 
   * @param doc the document.
   * @param viewersNode the viewers node within the document.
   */
  public static void restoreViewersState(final Document doc, final Element viewersNode) {
    // TODO: Implement this.
  }

  /**
   * Sets a map of key-value pairs into an XML structure under the given parent node.
   * 
   * @param doc the XML document.
   * @param parentNode the parent node.
   * @param properties the map of properties.
   */
  private static void propertiesToXML(final Document doc, final Element parentNode, final Map<String, String> properties) {
    for (String key : properties.keySet()) {
      Element propertyNode = doc.createElement(PROPERTY_TAG);
      parentNode.appendChild(propertyNode);
      propertyNode.setAttribute(KEY_ATTRIBUTE, key);
      String value = properties.get(key);
      if (value == null) {
        value = "";
      }
      propertyNode.setAttribute(VALUE_ATTRIBUTE, value);
    }
  }

  /**
   * Gets a map of key-value pairs from an XML structure under the given parent node.
   * 
   * @param doc the XML document.
   * @param parentNode the parent node.
   * @return the map of properties.
   */
  private static Map<String, String> propertiesFromXML(final Document doc, final Element parentNode) {
    Map<String, String> properties = new HashMap<String, String>();
    NodeList propertyNodes = parentNode.getElementsByTagName(PROPERTY_TAG);
    int numProperties = propertyNodes.getLength();
    for (int j = 0; j < numProperties; j++) {
      Element propertyNode = (Element) propertyNodes.item(j);
      String key = propertyNode.getAttribute(KEY_ATTRIBUTE);
      String value = propertyNode.getAttribute(VALUE_ATTRIBUTE);
      if (value == null) {
        value = "";
      }
      properties.put(key, value);
    }
    return properties;
  }
}
