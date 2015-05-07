/*
 * Copyright (C) ConocoPhillips 2010 All Rights Reserved.
 */
package org.geocraft.core.session;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.geocraft.core.common.util.Utilities;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.preferences.IGeocraftPreferencePage;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.core.service.message.Topic;
import org.geocraft.core.session.SavesetDescriptor.Algorithm;
import org.geocraft.core.session.SavesetDescriptor.AlgorithmParameter;
import org.geocraft.core.session.SavesetDescriptor.DataEntity;
import org.geocraft.core.session.SavesetDescriptor.EntityModelProperty;
import org.geocraft.core.session.SavesetDescriptor.Perspective;
import org.geocraft.core.session.SavesetDescriptor.Preference;
import org.geocraft.core.session.SavesetDescriptor.PreferencePage;
import org.geocraft.core.session.SavesetDescriptor.RendererProperty;
import org.geocraft.core.session.SavesetDescriptor.Viewer;
import org.geocraft.core.session.SavesetDescriptor.ViewerPart;
import org.geocraft.core.session.SavesetDescriptor.ViewerProperty;
import org.geocraft.core.session.SavesetDescriptor.Viewer.Renderer;
import org.osgi.service.prefs.BackingStoreException;


public class SessionManager {

  /** File operations */
  enum FileOp {
    RESTORE_SESSION_FILE,
    SAVE_SESSION_FILE,
    EXIT_SAVE_SESSION_FILE,
    SAVE_BATCH_FILE
  }

  /** The logger. */
  private static ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(SessionManager.class);

  /** Property file containing user preferences, in particular, the path of the last active session. */
  static final String USER_PREFS = ".userPreferences.properties";

  static final String LAST_ACTIVE_SESSION_PROP = "lastActiveSession";

  static final String RESTORE_ON_LAUNCH_PROP = "restoreOnLaunch";

  private static SessionManager singleton = null;

  private SessionManager() {
    // The empty constructor.
  }

  /**
   * Get the singleton instance of this class. If the parser class doesn't
   * exist, create it.
   */
  public static SessionManager getInstance() {
    if (singleton == null) {
      singleton = new SessionManager();
    }

    return singleton;
  }

  final static String SAVESET_SUBDIR = "sessions";

  final static String BATCH_SUBDIR = "gcbatch";

  final static String SAVESET_SUFFIX = ".gcs";

  public final static String BATCH_SUFFIX = ".gcb";

  final static String EOL = System.getProperty("line.separator");

  static String _currentSaveset = "";

  static IWorkbenchWindow _activeWindow;

  static IRepository _repo = ServiceProvider.getRepository();

  static String _windowState, _geocraftState;

  static BufferedWriter _xmlWriter;

  static BufferedReader _xmlReader;

  /**
   * Open a buffered reader for a XML session state file
   * @param pathname Full pathname of the file to read
   */
  public void openXMLReader(String pathname) {
    try {
      _xmlReader = new BufferedReader(new FileReader(pathname));

    } catch (IOException ioe) {
      LOGGER.error("Cannot open session state reader: " + pathname);
    }
  }

  /**
   * Open a buffered writer for a XML session state file
   * @param pathname Full pathname of the file to write
   * @param append A boolean indicating whether or not to append written data
   */
  public void openXMLWriter(String pathname, boolean append) {
    try {
      _xmlWriter = new BufferedWriter(new FileWriter(pathname, append));

    } catch (IOException ioe) {
      LOGGER.error("Cannot open session state writer: " + pathname);
    }
  }

  /**
   * Read a line from a session state file.
   * @return Next line; null if EOF
   */
  public String readXML() {
    try {
      return _xmlReader.readLine();
    } catch (IOException ioe) {
      LOGGER.error("Cannot read from session state file: " + _currentSaveset);
      return null;
    }
  }

  /**
   * Write an XML string to a session state file. Text is
   * appended to the file.
   * @param xml XML text to be written
   */
  public void writeXML(String xml) {
    try {
      _xmlWriter.write(xml + EOL);
    } catch (IOException ioe) {
      LOGGER.error("Cannot write to session state file:" + _currentSaveset);
    }
  }

  /**
   * Close session state input file
   */
  public void closeXMLReader() {
    try {
      _xmlReader.close();
    } catch (IOException ioe) {
      LOGGER.error("Cannot close session state input file:" + _currentSaveset);
    }
  }

  /**
   * Close session state output file
   */
  public void closeXMLWriter() {
    try {
      _xmlWriter.close();
    } catch (IOException ioe) {
      LOGGER.error("Cannot close session state output file:" + _currentSaveset);
    }
  }

  private String getFilename(String pathname) {
    int idx = pathname.lastIndexOf(File.separatorChar);
    return pathname.substring(idx + 1);
  }

  /**
   * Save session state to an XML file.
   * <p>
   * Gather the state of each active component by calling their genState() which
   * returns a component's state encapsulated in XML. Wrap the state in <component>
   * tags. Save the state in the specified .gcs file. After processing all active
   * components, get and save the state of GeoCraft itself.
   * <p>
   * If the save fails, a dialog tells the user why the save failed.
   * @param pathname Full path of the State XML file to be created.
   * @param batch true if saving batch state, false if saving session state
   */
  @SuppressWarnings("restriction")
  public void saveSession(String pathname, boolean batch) {
    //remember latest saveset
    _currentSaveset = pathname;

    //open session file for writing
    openXMLWriter(pathname, false);

    ComponentState.writePrologue();

    //NOTE: The repository and algorithms are common to all perspectives whereas
    //      viewers are specific to a perspective

    //Build up State XML piecemeal: repository, algorithms, perspectives and their views
    //OPEN ROOT element: <geocraft>
    ComponentState.openRootElement();

    //START REPOSITORY
    ComponentState.openRepositoryElement();

    //get list of all loaded data entities
    if (_repo != null) {
      Map<String, Object> reposMap = _repo.getAll();
      //Get the data entities in the repository
      Object[] objects = reposMap.values().toArray();
      //Get the keys of the hash map containing all the entities in the repository.
      //Note: The key is the var name associated with the entity
      Set<String> varNames = reposMap.keySet();
      Iterator<String> varsIter = varNames.iterator();
      String[] vars = new String[varNames.size()];
      int k = 0;
      while (varsIter.hasNext()) {
        String varName = varsIter.next();
        //Object object = reposMap.get(varName);
        vars[k++] = varName;
      }

      //capture data entity and its model properties
      ComponentState.openEntitiesElement();

      for (int i = 0; i < objects.length; i++) {
        Object object = objects[i];
        if (object instanceof Entity) {
          Entity entity = (Entity) object;
          MapperModel model = entity.getMapper().getModel();
          if (model == null) { // || entity.isDirty()) {
            continue;
          }
          if (RepositoryIdStore.canSave(entity)) {
            //String uniqueId = RepositoryIdStore.getUniqueId(entity);
            ComponentState.openEntityElement(vars[i], entity.getUniqueID());
            //capture model properties
            Map<String, String> props = model.pickle();
            for (String key : props.keySet()) {
              String val = props.get(key);
              ComponentState.entityPropertyElement(key, val);
            }
            ComponentState.closeEntityElement();
          }
        }
      }

      ComponentState.closeEntitiesElement();
    }

    ComponentState.closeRepositoryElement();
    //END REPOSITORY

    //START ALGORITHMS

    ComponentState.openAlgorithmsElement();

    //get list of all registered standalone algorithms
    Map<Integer, Object> algorithms = ServiceProvider.getAlgorithmsService().getRegisteredAlgorithms();
    //    ServiceProvider.getAlgorithmsService().dumpRegistry();

    //capture active algorithms and their model parameters
    for (Integer akey : algorithms.keySet()) {
      String name = ServiceProvider.getAlgorithmsService().getAlgorithmName(akey);
      String className = ServiceProvider.getAlgorithmsService().getAlgorithmClassName(akey);
      String windowID = ServiceProvider.getAlgorithmsService().getWindowID(akey);
      //capture algorithm parameters
      Map<String, String> parms = ServiceProvider.getAlgorithmsService().getAlgorithmParms(akey);
      ComponentState.openAlgorithmElement(name, className, windowID, akey, batch);
      List<String> keys = Utilities.sortKeys(parms.keySet());
      for (String key : keys) {
        String val = parms.get(key);
        val = val.replace("\r\n", "&#xD;&#xA;"); //Windows (CR+LF)
        val = val.replace("\n", "&#xA;"); //Unix (CR)
        key = key.replace("\"", "&#034;");
        val = val.replace("\"", "&#034;");
        val = val.replace("<", "&#060;");
        val = val.replace(">", "&#062;");
        if (!key.equals("")) {
          ComponentState.algorithmParameterElement(key, val);
        }
      }
      ComponentState.closeAlgorithmElement();
    }

    ComponentState.closeAlgorithmsElement();
    //END ALGORITHMS

    //START PREFERENCES
    IExtensionRegistry extRegistry = Platform.getExtensionRegistry();
    IConfigurationElement[] configElements = extRegistry.getConfigurationElementsFor("org.eclipse.ui.preferencePages");
    String name = "", klass = "";
    for (IConfigurationElement configElement : configElements) {
      name = configElement.getAttribute("name");
      klass = configElement.getAttribute("class");
      try {
        Object prefPage = configElement.createExecutableExtension("class");
        if (prefPage instanceof IGeocraftPreferencePage) {
          ComponentState.openPreferencePageElement(name, klass);
          Map<String, String> prefs = ((IGeocraftPreferencePage) prefPage).getPreferenceState();
          for (String key : prefs.keySet()) {
            String val = prefs.get(key);
            ComponentState.preferenceElement(key, val);
          }
          ComponentState.closePreferencePageElement();
        }
      } catch (CoreException ce) {
        LOGGER.warn("Cannot create " + klass);
        ce.printStackTrace();
      }
    }
    //END PREFERENCES

    if (batch) {
      //CLOSE ROOT element </geocraft>
      ComponentState.closeRootElement();
      //close XML writer
      closeXMLWriter();
      return;
    }

    IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
    for (IWorkbenchWindow win : windows) {
      //START PERSPECTIVES FOR A WORKBENCH WINDOW
      String windowID = getWorkbenchWindowID(win);
      //Get a list of all open perspectives in the workbench window
      //Note: List is in the order in which they were opened
      //IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      //IWorkbenchPage page = window.getActivePage();
      IPerspectiveDescriptor[] descs = findOpenPerspectivesInWindow(win);
      for (IPerspectiveDescriptor desc : descs) {
        String id = desc.getId();
        configElements = extRegistry.getConfigurationElementsFor("org.eclipse.ui.perspectives");
        for (IConfigurationElement configElement : configElements) {
          if (!id.equals(configElement.getAttribute("id"))) {
            continue;
          }
          name = configElement.getAttribute("name");
          klass = configElement.getAttribute("class");
          break;
        }

        //Set focus on perspective so can capture its state.
        setPerspectiveFocus(id, win);
        //Note: a perspective's state contains the labels of the views which are
        //      needed when restoring the viewers
        IWorkbenchPage pPage = getPerspectivePage(id);
        pPage.savePerspective();
        ComponentState.openPerspectiveElement(id, name, klass, windowID, pPage.isEditorAreaVisible());
        ComponentState.closePerspectiveElement();
      }
      //END PERSPECTIVES FOR A WORKBENCH WINDOW
    }

    //PLOT WINDOW (IF ANY)
    for (IWorkbenchWindow win : windows) {
      if (ServiceProvider.getViewersService().isPlotWindow(win)) {
        ComponentState.plotWindowElement(getWorkbenchWindowID(win));
      }
    }

    //START REGISTERED VIEWERS
    //Capture the state of each registered viewer in each workbench window, its model parameters and its associated renderers
    //Note: The registered item is a viewer part that contains its associated viewers
    for (Integer vpkey : ServiceProvider.getViewersService().getRegisteredViewerParts().keySet()) {
      String className = ServiceProvider.getViewersService().getViewerPartClassName(vpkey);
      String partId = ServiceProvider.getViewersService().getViewerPartId(vpkey);
      String perspectiveId = findViewerPerspective(partId);
      String winID = ServiceProvider.getViewersService().getViewerPartWindowID(vpkey);
      int uniqueId = ServiceProvider.getViewersService().getViewerPartUniqueId(vpkey);
      //String partName = ServiceProvider.getViewersService().getViewerPartName(vpkey);
      ComponentState.openViewerPartElement(className, partId, winID, perspectiveId, uniqueId);
      ArrayList<String> state = ServiceProvider.getViewersService().getSessionState(vpkey);
      for (String line : state) {
        writeXML(line);
      }
      ComponentState.closeViewerPartElement();
    }
    //END VIEWERS

    //CLOSE ROOT element </geocraft>
    ComponentState.closeRootElement();
    closeXMLWriter();

    //append Eclipse state
    openXMLWriter(pathname, true);

    //APPEND ECLIPSE STATE FOR EACH OF ITS WINDOWS
    for (IWorkbenchWindow win : windows) {
      writeXML("<!-- Start of state for Eclipse window: " + getWorkbenchWindowID(win) + " -->");
      XMLMemento memento = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_WINDOW);
      ((WorkbenchWindow) win).saveState(memento);
      _windowState = layoutStateToString(memento);
      writeXML(_windowState);
    }

    //close XML writer
    closeXMLWriter();
  }

  /*
   * Save this session to a State XML file. If a saveset is not already specified,
   * have the user specify the name of the saveset file.
   */
  public void saveSession() {
    //check if a saveset already specified either by a previous save or a
    //restored saveset
    if (!_currentSaveset.equals("")) {
      saveSession(_currentSaveset, false);
      return;
    }

    saveSessionAs(false);
  }

  /*
   * Save session state to a specified XML file.
   * @param true if exiting Geocraft; otherwise, false
   */
  public void saveSessionAs(boolean exiting) {
    //check if savesets directory exists; if not, create it
    String workspaceDir = Platform.getLocation().toString();
    String sessionsDir = workspaceDir + File.separatorChar + SAVESET_SUBDIR;
    File handle = new File(sessionsDir);
    if (!handle.exists() || !handle.isDirectory()) {
      handle.mkdirs();
    }

    //open a dialog for specifying the name of the State XML file (.gcs)
    String savesetPathname = SavesetDialog(exiting ? FileOp.EXIT_SAVE_SESSION_FILE : FileOp.SAVE_SESSION_FILE);

    //check if selection canceled
    if (savesetPathname == null) {
      return;
    }

    //append suffix if one not specified
    int idx = savesetPathname.lastIndexOf('.');
    if (idx != -1) {
      String suffix = savesetPathname.substring(idx);
      if (!suffix.equals(SAVESET_SUFFIX)) {
        savesetPathname += SAVESET_SUFFIX;
      }
    } else {
      savesetPathname += SAVESET_SUFFIX;
    }

    //remember the last active session
    saveLastActiveSession(savesetPathname);

    saveSession(savesetPathname, false);
  }

  /*
   * Save session state to a specified XML file.
   * Batch state only consists of repository and algorithm state.
   */
  public void saveSessionAsBatch() {
    //check if batch directory exists; if not, create it
    String workspaceDir = Platform.getLocation().toString();
    String sessionsDir = workspaceDir + File.separatorChar + BATCH_SUBDIR;
    File handle = new File(sessionsDir);
    if (!handle.exists() || !handle.isDirectory()) {
      handle.mkdirs();
    }

    //open a dialog for specifying the name of the batch state XML file (.gcb)
    String savesetPathname = SavesetDialog(FileOp.SAVE_BATCH_FILE);

    //check if selection canceled
    if (savesetPathname == null) {
      return;
    }

    //append suffix if one not specified
    int idx = savesetPathname.lastIndexOf('.');
    if (idx != -1) {
      String suffix = savesetPathname.substring(idx);
      if (!suffix.equals(BATCH_SUFFIX)) {
        savesetPathname += BATCH_SUFFIX;
      }
    } else {
      savesetPathname += BATCH_SUFFIX;
    }

    saveSession(savesetPathname, true);
  }

  /**
   * Restore a session from a State XML file.
   * <p>
   * Extract the state of each component from the specified .gcs XML file, create
   * an instance of the component and call the component's restoreState() which
   * re-establishes the state. After processing all active components, get and
   * restore the state of GeoCraft itself.
   * <p>
   * If the restore fails, a dialog tells the user why the restore failed.
   * @param pathname Full path of the State XML file to be restored.
   * @param launching true if restoring session when launching Geocraft; otherwise, false
   */
  @SuppressWarnings("restriction")
  public void restoreSession(String pathname, boolean launching) {
    IProgressMonitor monitor;
    ProgressMonitorDialog progressDialog;

    //remember latest saveset
    _currentSaveset = pathname;

    //open session state file for reading
    openXMLReader(pathname);

    //  unload the repository
    unloadRepository();

    //Restore the saveset
    //  parse the Geocraft session state
    final SavesetDescriptor desc = new SavesetDescriptor();
    boolean parsed = SavesetParser.getInstance().parseSaveset(desc);
    closeXMLReader();
    //desc.dumpDescriptor();
    if (parsed) {
      LOGGER.info("Restoring session: " + pathname);
      progressDialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
      progressDialog.open();
      monitor = progressDialog.getProgressMonitor();
      String sessionTask = "Restoring session: " + getFilename(pathname);

      //RESTORE PREFERENCES
      //Note: MUST be done before restoring the repository so the Unit Preferences are in place
      ArrayList<PreferencePage> prefPages = desc.getPreferencePages();
      Map<String, String> prefs = new HashMap<String, String>();
      monitor.beginTask(sessionTask, prefPages.size());
      for (PreferencePage prefPage : prefPages) {
        String name = prefPage.getPreferencePageName();
        LOGGER.info("Restoring preferences: " + name);
        monitor.subTask("Preference (" + prefPages.size() + "): " + name);
        //create the preference pages's preference state map
        ArrayList<Preference> preferences = prefPage.getPreferences();
        for (int j = 0; j < preferences.size(); j++) {
          Preference pref = preferences.get(j);
          prefs.put(pref.getKey(), pref.getValue());
        }
        IConfigurationElement configElement = findPreferencePageConfig(prefPage.getPreferencePageClassName());
        if (configElement != null) {
          Object prefPage2;
          try {
            prefPage2 = configElement.createExecutableExtension("class");
            ((IGeocraftPreferencePage) prefPage2).setPreferenceState(prefs);
          } catch (CoreException ce) {
            LOGGER.warn("Cannot create an executable extension for " + prefPage.getPreferencePageClassName());
            ce.printStackTrace();
          }
        }
        //clear out page preferences for next preference page
        prefs.clear();
        monitor.worked(1);
      }
      monitor.done();

      //LOAD REPOSITORY with the data entities (if any)
      try {
        //create the map of var names (key = entity's unique ID)
        HashMap<String, String> varNames = new HashMap<String, String>();
        ArrayList<DataEntity> entities = desc.getEntities();
        for (DataEntity entity : entities) {
          varNames.put(entity.getEntityUniqueId(), entity.getVarName());
        }
        //Set the var names to be used when loading the entities
        if (_repo != null) {
          _repo.setVarnameMap(varNames);
        }

        monitor.beginTask(sessionTask, varNames.size());
        Map<String, String> props = new HashMap<String, String>();
        for (DataEntity entity : entities) {
          String uniqueId = entity.getEntityUniqueId();
          monitor.subTask("Data Entities (" + varNames.size() + "): " + uniqueId);
          String varName = entity.getVarName();
          LOGGER.info("Restoring entity (" + varName + ") with unique ID: " + uniqueId);
          //create the model's unpickle map
          ArrayList<EntityModelProperty> properties = entity.getEntityProperties();
          for (int j = 0; j < properties.size(); j++) {
            EntityModelProperty prop = properties.get(j);
            props.put(prop.getKey(), prop.getValue());
          }
          //Note: restoration runs in JOIN mode, so restoring entities is sequential
          RepositoryIdStore.restoreEntity(props, uniqueId);
          //clear out model properties for next entity
          props.clear();
          monitor.worked(1);
        }
        monitor.done();
      } catch (BackingStoreException bse) {
        bse.printStackTrace();
      }

      //RESTORE WORKBENCH WINDOWS' STATE
      //There is at least 1 window.
      //  close all the open workbench windows but the active window
      if (!launching) {
        closeWindows();
      }

      //  prepare the active workbench window for restoration
      Workbench workbench = (Workbench) PlatformUI.getWorkbench();
      IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
      //clear the active algorithms registry
      ServiceProvider.getAlgorithmsService().removeAll();

      ArrayList<String> windowIds = desc.getWorkbenchWindows();
      //list of new window IDs. key=old ID, value=new ID
      final HashMap<String, String> newWindowIds = new HashMap<String, String>();
      int winIdx = 0;
      ArrayList<Integer> restoredAlgorithms = new ArrayList<Integer>();
      IWorkbenchWindow workbenchWindow = null;
      IPerspectiveDescriptor[] openPerspectives;
      String perspectiveId = "";

      //START RESORING EACH WORKBENCH WINDOW
      final ArrayList<String> restoredWindowIDs = new ArrayList<String>();
      for (String windowId : windowIds) {
        LOGGER.info("Restoring Eclipse window state: " + windowId);
        String winTask = "Workbench window " + (winIdx + 1) + "/" + windowIds.size() + "\n";
        //  prepare a newly created workbench window for restoration
        //Note: Need 1 window to open another. Use the active window, then close it.
        //get the perspectives associated with the window to be restored
        ArrayList<Perspective> windowPerspectives = desc.getPerspectives(windowId);
        //there is at least 1 perspective
        perspectiveId = windowPerspectives.get(0).getPerspectiveID();
        IAdaptable input = workbench.getDefaultPageInput();
        try {
          workbenchWindow = PlatformUI.getWorkbench().openWorkbenchWindow(perspectiveId, input);
          //set the plot window (if any)
          if (windowId.equals(desc.getPlotWindowID())) {
            ServiceProvider.getViewersService().setPlotWindow(workbenchWindow);
          }
          String newWindowID = getWorkbenchWindowID(workbenchWindow);
          restoredWindowIDs.add(newWindowID);
          newWindowIds.put(windowId, newWindowID);
          //capture the perspectives open at the start of the restore
          openPerspectives = findOpenPerspectivesInWindow(workbenchWindow);
          //close all the open perspectives in the window
          closeOpenPerspectives(workbenchWindow, openPerspectives);
        } catch (WorkbenchException we) {
          LOGGER.error("Cannot create workbench window: " + windowId);
          we.printStackTrace();
        }

        //Restore the state of the workbench window.
        openXMLReader(_currentSaveset);
        IPerspectiveDescriptor perspectiveDesc = restoreWindowState(workbenchWindow, readWindowState(windowId),
            perspectiveId);
        closeXMLReader();
        //close the initial active workbench window
        //Note: there is no window to close if launching Geocraft
        if (winIdx == 0 && !launching) {
          activeWorkbenchWindow.close();
        }

        //Restore the algorithms (if any)
        //get the algorithms associated with the window
        ArrayList<Algorithm> algorithms = desc.getAlgorithms(windowId);
        Map<String, String> parms = new HashMap<String, String>();
        monitor.beginTask(sessionTask, algorithms.size());
        for (Algorithm algorithm : algorithms) {
          String name = algorithm.getAlgorithmName();
          String uniqueID = algorithm.getAlgorithmUniqueID();
          LOGGER.info("Restoring algorithm: " + name);
          monitor.subTask(winTask + "Algorithms (" + algorithms.size() + "): " + name);
          //create the algorithms's unpickle map
          ArrayList<AlgorithmParameter> parameters = algorithm.getAlgorithmProperties();
          for (int j = 0; j < parameters.size(); j++) {
            AlgorithmParameter parm = parameters.get(j);
            parms.put(parm.getKey(), parm.getValue());
          }
          int key = ServiceProvider.getAlgorithmsService().activate(algorithm.getAlgorithmClassName(), parms);
          //remember keys of algorithms restored (cummulative)
          restoredAlgorithms.add(key);
          //clear out model parameters for next algorithm
          parms.clear();
          monitor.worked(1);
        }
        monitor.done();

        //RESTORE CHECKBOX STATE OF ALL VIEWER'S LAYERS TREE
        //Note: MUST be done before restore renderers
        ServiceProvider.getViewersService().updateAllViewerLayers(desc, newWindowIds);

        //Restore the renderers for the viewers (if any) in the window
        //  determine how many viewers to be restored
        int numViewers = 0;
        //clear the list of viewers being restored
        ServiceProvider.getViewersService().initViewers();
        ArrayList<ViewerPart> viewerParts = desc.getViewerParts(windowId);
        if (viewerParts.size() != 0) {
          //determine the total number of viewers amongst the viewer parts.
          for (ViewerPart part : viewerParts) {
            numViewers += part.getNumViewers();
          }
        }
        monitor.beginTask(sessionTask, numViewers);
        for (ViewerPart viewerPart : viewerParts) {
          Map<String, String> props = new HashMap<String, String>();
          Map<String, String> rprops = new HashMap<String, String>();

          //set focus to perspective containing viewer so viewer restored in perspective
          String partID = viewerPart.getViewerPartId();
          String perspectiveID = viewerPart.getViewerPerspectiveID();
          setPerspectiveFocus(perspectiveID, workbenchWindow);
          if (viewerPart.getNumViewers() == 1) {
            Viewer viewer = viewerPart.getViewers().get(0);
            LOGGER.info("Restoring viewer: " + partID);
            monitor.subTask(winTask + "Viewers (" + numViewers + "): " + partID);

            //create the viewers's unpickle map
            ArrayList<ViewerProperty> vproperties = viewer.getViewerProperties();
            props.clear();
            for (int j = 0; j < vproperties.size(); j++) {
              ViewerProperty prop = vproperties.get(j);
              props.put(prop.getKey(), prop.getValue());
            }

            // update viewer's model properties
            // Note: viewer's model properties needed when creating the model space for the renderers
            Object viewer2 = findViewerInWindow(workbenchWindow, partID);
            if (viewer2 == null) {
              LOGGER.warn("Cannot find viewer in perspective: " + partID);
              continue;
            }
            ServiceProvider.getViewersService().updateViewerModel(viewer2, props);

            //create viewer and update its model
            //Object viewer2 = ServiceProvider.getViewersService().activateViewer(viewerPart.getViewerPartClassName(),
            //    props, label);

            //restore renderers
            HashMap<String, Renderer> renderers = viewer.getViewerRenderers();
            for (String rkey : renderers.keySet()) {
              Renderer renderer = renderers.get(rkey);
              //create the renderer's unpickle map
              ArrayList<RendererProperty> rproperties = renderer.getRendererProperties();
              rprops.clear();
              for (int j = 0; j < rproperties.size(); j++) {
                RendererProperty prop = rproperties.get(j);
                rprops.put(prop.getKey(), prop.getValue());
              }
              //restore renderer and its properties
              try {
                ServiceProvider.getViewersService().addRenderer(viewer2, renderer.getRendererClassName(), rprops,
                    renderer.getRenderedEntity());
              } catch (Exception ex) {
                // If an error occurs while restoring a renderer, log an error and continue.
                ServiceProvider.getLoggingService().getLogger(getClass()).error("Error restoring renderer.", ex);
              }
            }

            //update the viewer's model properties a second time which may have been clobbered adding the renderers
            //ServiceProvider.getViewersService().updateViewerModel(viewer2, props);
            //update the viewer from its model which will cause a redraw
            ServiceProvider.getViewersService().updateFromModel(viewer2);

            ServiceProvider.getViewersService().addViewer(viewer2);
            monitor.worked(1);
          } else { // multiple viewers in viewer part
            ArrayList<Viewer> viewers = viewerPart.getViewers();
            // update model properties
            // Note: viewer's model properties needed when creating the model space for the renderers
            Object[] viewers2 = findViewersInWindow(workbenchWindow, partID);
            if (viewers2 != null) {
              int idx = 0;
              for (Viewer viewer : viewers) {
                //create the viewers's unpickle map
                ArrayList<ViewerProperty> vproperties = viewer.getViewerProperties();
                props.clear();
                for (int j = 0; j < vproperties.size(); j++) {
                  ViewerProperty prop = vproperties.get(j);
                  props.put(prop.getKey(), prop.getValue());
                }
                ServiceProvider.getViewersService().updateViewerModel(viewers2[idx], props);

                //restore renderers
                String title = viewer.getViewerTitle();
                LOGGER.info("Restoring viewer: " + title);
                monitor.subTask(winTask + "Viewers (" + numViewers + "): " + title);
                HashMap<String, Renderer> renderers = viewer.getViewerRenderers();
                for (String rkey : renderers.keySet()) {
                  Renderer renderer = renderers.get(rkey);
                  //create the renderer's unpickle map
                  ArrayList<RendererProperty> rproperties = renderer.getRendererProperties();
                  rprops.clear();
                  for (int k = 0; k < rproperties.size(); k++) {
                    RendererProperty prop = rproperties.get(k);
                    rprops.put(prop.getKey(), prop.getValue());
                  }

                  //restore renderer and its properties
                  ServiceProvider.getViewersService().addRenderer(viewers2[idx], renderer.getRendererClassName(),
                      rprops, renderer.getRenderedEntity());
                }

                //update the viewer's model properties a second time which may have been clobbered adding the renderers
                //ServiceProvider.getViewersService().updateViewerModel(viewers2[idx], vprops.get(idx));
                //update the viewer from its model which will cause a redraw
                ServiceProvider.getViewersService().updateFromModel(viewers2[idx]);

                ServiceProvider.getViewersService().addViewer(viewers2[idx]);

                //restore state of viewer folder layers (i.e., checkbox tree in the Layers tab)
                //Note: MUST BE DONE LAST
                //ServiceProvider.getViewersService().updateViewerFolderLayers(viewers2[idx], viewer);
                idx++;
              }
            }
            monitor.worked(1);
          }
        }
        //if (winIdx == 0) {
        //close all the registered algorithms not restored
        closeAlgorithms(restoredAlgorithms);
        //}
        winIdx++;
      }
      //END RESTORING EACH WORKBENCH WINDOW

      //If restoring when launching, close any open windows that are not restored windows
      if (launching) {
        Display.getDefault().asyncExec(new Runnable() {

          public void run() {
            closeUnrestoredWindows(restoredWindowIDs);
          }
        });

      }

      progressDialog.close();
    }
  }

  /**
   * Close any open window that was not restored.
   * @param windowIDs IDs of windows restored
   */
  private void closeUnrestoredWindows(ArrayList<String> windowIDs) {
    IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
    for (IWorkbenchWindow window : windows) {
      String windowID = getWorkbenchWindowID(window);
      if (!windowIDs.contains(windowID)) {
        window.close();
      }
    }
  }

  /**
   * Find the configuration element for a preference page
   * @param prefClass Preference page's class
   * @return Configuration element for the preference page if found; otherwise, null
   */
  private IConfigurationElement findPreferencePageConfig(String prefClass) {
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IConfigurationElement[] configElements = registry.getConfigurationElementsFor("org.eclipse.ui.preferencePages");
    String klass = "";
    for (IConfigurationElement configElement : configElements) {
      klass = configElement.getAttribute("class");
      if (klass.equals(prefClass)) {
        return configElement;
      }
    }
    return null;
  }

  /*
   * Restore a session from a State XML file. Have the user select an existing saveset.
   */
  public void restoreSession() {
    //check if want to save existing session first
    int answer = QuestionDialog("Do you want to save the current session?");
    if (answer == 0) { //Yes
      //The current session may never have been saved
      saveSession();
    }

    //open a dialog for selecting an existing State XML file (.gcs)
    String savesetPathname = SavesetDialog(FileOp.RESTORE_SESSION_FILE);

    //check if restore canceled
    if (savesetPathname == null) {
      return;
    }

    //remember the last active session
    saveLastActiveSession(savesetPathname);

    restoreSession(savesetPathname, false);
  }

  /*
   * Open a File dialog to select a saveset to be saved to or restored.
   * @param fileOp File operation - restore a session file, save session to a state file (.gcs),
   * save session to a batch file (.gcb)
   */
  private String SavesetDialog(FileOp fileOp) {
    Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    if (shell == null) {
      shell = Display.getDefault().getActiveShell();
    }
    String filterName = "", filterExtension = "", title = "", dir = "", stateFile = "";
    String workspaceDir = Platform.getLocation().toString();
    switch (fileOp) {
      case SAVE_BATCH_FILE:
        filterName = "Session Batch (.gcb)";
        filterExtension = "*.gcb";
        title = "Batch Saveset Selection...";
        dir = workspaceDir + File.separatorChar + BATCH_SUBDIR;
        break;
      case SAVE_SESSION_FILE:
        filterName = "GeoCraft Session (.gcs)";
        filterExtension = "*.gcs";
        title = "Session Selection...";
        dir = workspaceDir + File.separatorChar + SAVESET_SUBDIR;
        break;
      case EXIT_SAVE_SESSION_FILE:
        filterName = "GeoCraft Session (.gcs)";
        filterExtension = "*.gcs";
        title = "Session Selection...";
        String sessionPath = getLastActiveSessionPref();
        if (!sessionPath.equals("")) {
          int idx = sessionPath.lastIndexOf(File.separatorChar);
          dir = sessionPath.substring(0, idx);
          stateFile = sessionPath.substring(idx + 1);
        } else {
          dir = workspaceDir + File.separatorChar + SAVESET_SUBDIR;
        }
        break;
      case RESTORE_SESSION_FILE:
        filterName = "GeoCraft Session (.gcs)";
        filterExtension = "*.gcs";
        title = "Session Selection...";
        dir = workspaceDir + File.separatorChar + SAVESET_SUBDIR;
        File gcsDir = new File(dir);
        if (!gcsDir.exists()) {
          gcsDir.mkdir();
        }
        break;
      default:
    }
    String[] _filterNames = new String[] { filterName };
    String[] _filterExtensions = new String[] { filterExtension };

    int style = SWT.OPEN;
    switch (fileOp) {
      case SAVE_BATCH_FILE:
      case SAVE_SESSION_FILE:
      case EXIT_SAVE_SESSION_FILE:
        style = SWT.SAVE;
        break;
      case RESTORE_SESSION_FILE:
        style = SWT.OPEN;
        break;
      default:
        style = SWT.OPEN;
    }

    FileDialog dialog = new FileDialog(shell, style);
    dialog.setText(title);
    dialog.setFilterPath(dir);
    dialog.setFilterNames(_filterNames);
    dialog.setFilterExtensions(_filterExtensions);
    dialog.setFileName(stateFile);
    String selected = dialog.open();
    return selected;
  }

  public int QuestionDialog(String question) {
    Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    if (shell == null) {
      shell = Display.getDefault().getActiveShell();
    }

    MessageDialog dialog = new MessageDialog(shell, "Current Session", null, question, MessageDialog.QUESTION,
        new String[] { "Yes", "No" }, 0);
    return dialog.open();
  }

  /**
   * Unload the data entities loaded into the repository.
   * <p>
   * Note: Clearing the repository will close any open Landmark
   * SeisWorks and OpenWorks projects. This is handled in the
   * SeisWorksProject and OpenWOrksProject message handlers.
   */
  private void unloadRepository() {
    if (_repo != null) {
      _repo.clear();
      ServiceProvider.getMessageService().publish(Topic.CLOSE_ALL_PROJECTS, "");
    }
  }

  /**
   * Close all open windows but the active window
   */
  private void closeWindows() {
    IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
    IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    for (IWorkbenchWindow window : windows) {
      if (!window.equals(activeWindow)) {
        window.close();
      }
    }
  }

  /**
   * Close all the registered algorithms
   */
  private void closeAlgorithms() {
    Map<Integer, Object> algorithms = ServiceProvider.getAlgorithmsService().getRegisteredAlgorithms();

    //close all the active algorithms
    Set<Integer> keys = algorithms.keySet();
    Iterator<Integer> iter = keys.iterator();
    while (iter.hasNext()) {
      Integer key = iter.next();
      ServiceProvider.getAlgorithmsService().deactivate(key);
    }
    //clear the active algorithms registry
    ServiceProvider.getAlgorithmsService().removeAll();
  }

  /**
   * Close all the registered algorithms not restored and remove them from the
   * registry.
   * @param restoredAlgorithms List of restored algorithm keys
   */
  private void closeAlgorithms(ArrayList<Integer> restoredAlgorithms) {
    Map<Integer, Object> algRegistry = ServiceProvider.getAlgorithmsService().getRegisteredAlgorithms();
    //make a copy of the registry
    Map<Integer, Object> reg = new HashMap<Integer, Object>();
    for (Integer key : algRegistry.keySet()) {
      reg.put(key, algRegistry.get(key));
    }
    /*
    System.out.println("Restored algorithm keys: ");
    for (Integer raKey : restoredAlgorithms) {
      System.out.println("  key=" + raKey);
    }
    System.out.println("Registered algorithm keys: ");
    for (Integer raKey : reg.keySet()) {
      System.out.println("  key=" + raKey);
    }
    */
    //remove restored algorithms from list; those left need to be closed
    for (Integer key : restoredAlgorithms) {
      if (reg.containsKey(key)) {
        reg.remove(key);
      }
    }
    //close registered algorithms not restored
    for (Integer key : reg.keySet()) {
      ServiceProvider.getAlgorithmsService().deactivate(key);
      ServiceProvider.getAlgorithmsService().remove(key);
    }
  }

  /**
   * Close all the open viewers
   */
  private void closeViewers() {
    //get all the open viewers
    Map<Integer, Object> viewers = ServiceProvider.getViewersService().getRegisteredViewerParts();

    //close all the active viewers
    for (Integer key : viewers.keySet()) {
      ServiceProvider.getViewersService().deactivate(key);
      break;
    }
    //clear the active viewers registry
    ServiceProvider.getViewersService().removeAll();
  }

  /**
   * Close all open perspectives in a workbench window.
   * @param window Workbench window containing the open perspectives
   * @param openPerspectives List of open perspectives
   */
  private void closeOpenPerspectives(IWorkbenchWindow window, IPerspectiveDescriptor[] openPerspectives) {
    //close all the open perspectives
    for (IPerspectiveDescriptor openPerspective : openPerspectives) {
      String id = openPerspective.getId();
      IWorkbenchPage page = getPerspectivePage(id);
      page.closePerspective(openPerspective, false, false);
    }
    //close all the pages in the window
    IWorkbenchPage[] pages = window.getPages();
    for (IWorkbenchPage page : pages) {
      page.close();
    }
  }

  /**
   * Get the viewers associated with a perspective in a workbench window
   * @param perspectiveId ID of the perspective
   * @param window Workbench window
   * @return List of view IDs
   */
  private ArrayList<String> getViewers(String perspectiveId, IWorkbenchWindow window) {
    ArrayList<String> partIDs = new ArrayList<String>();
    //set focus on perspective in workbench window
    try {
      PlatformUI.getWorkbench().showPerspective(perspectiveId, window);
    } catch (WorkbenchException we) {
      LOGGER.error("Unable to get viewers in perspective " + perspectiveId + ". " + we.getMessage());
      we.printStackTrace();
    }
    //    IWorkbenchPage[] pages = window.getPages();
    //    for (IWorkbenchPage page : pages) {
    //get the page containing the perspective
    IWorkbenchPage page = window.getActivePage();
    IViewReference[] views = page.getViewReferences();
    for (IViewReference view : views) {
      IViewPart viewPart = view.getView(false);
      String partID = ServiceProvider.getViewersService().getViewerPartId(viewPart);
      if (!partID.isEmpty()) {
        partIDs.add(partID);
      }
    }
    //    }
    return partIDs;
  }

  /**
   * Find the perspective containing a registered viewer
   * @param partID Unique part ID of the viewer
   * @return Perspective's ID if found; otherwise, null
   */
  private String findViewerPerspective(String partID) {
    IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
    for (IWorkbenchWindow window : windows) {
      IPerspectiveDescriptor[] perspectives = findOpenPerspectivesInWindow(window);
      for (IPerspectiveDescriptor desc : perspectives) {
        ArrayList<String> viewerPartIds = getViewers(desc.getId(), window);
        for (String partId : viewerPartIds) {
          if (partId.equals(partID)) {
            return desc.getId();
          }
        }
      }
    }
    return "";
  }

  /**
   * Set focus on a perspective in a workbench window
   * @param perspectiveId perspectiveId Perspective's ID
   * @param window Workbench window containing perspective
   * @return true if could set focus; otherwise, false.
   */
  private boolean setPerspectiveFocus(String perspectiveId, IWorkbenchWindow window) {
    try {
      PlatformUI.getWorkbench().showPerspective(perspectiveId, window);
      return true;
    } catch (WorkbenchException we) {
      LOGGER.error("Unable to set focus on perspective " + perspectiveId + ". " + we.getMessage());
      we.printStackTrace();
      return false;
    }
  }

  /**
   * Get the page associated with a perspective in the active workbench window
   * and set focus on perspective.
   * @param perspectiveId Perspective's ID
   * @return Workbench page associated with the perspective
   */
  private IWorkbenchPage getPerspectivePage(String perspectiveId) {
    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    //set focus on perspective
    try {
      PlatformUI.getWorkbench().showPerspective(perspectiveId, window);
    } catch (WorkbenchException we) {
      LOGGER.error("Unable to get the page associated with perspective " + perspectiveId + ". " + we.getMessage());
      we.printStackTrace();
    }
    return window.getActivePage();
  }

  private String layoutStateToString(XMLMemento memento) {
    Writer writer = new StringWriter();
    try {
      memento.save(writer);
      writer.close();
      return writer.toString();
    } catch (IOException ioe) {
      LOGGER.error("Exception: Cannot capture perspective state" + ioe.getMessage());
      ioe.printStackTrace();
    }
    return "";
  }

  /**
   * Restore the state of a window containing an open perspective.
   * <p>
   * Note: Algorithms restored are not built until one selects there tab.
   * Therefore, all editors are forced to be restored, i.e., built, so
   * all algorithms restored get registered.
   * @param workbenchWindow The window to be restored
   * @param windowState State of the window
   * @param perspectiveId ID of perspective in window
   */
  @SuppressWarnings("restriction")
  private IPerspectiveDescriptor restoreWindowState(IWorkbenchWindow workbenchWindow, String windowState,
      String perspectiveId) {
    IPerspectiveDescriptor desc = workbenchWindow.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(
        perspectiveId);
    BufferedReader reader = new BufferedReader(new StringReader(windowState));
    try {
      IMemento memento = XMLMemento.createReadRoot(reader);
      ((WorkbenchWindow) workbenchWindow).restoreState(memento, desc);
      IWorkbenchPage[] pages = ((WorkbenchWindow) workbenchWindow).getPages();
      //restore all the editors (algorithms)
      for (IWorkbenchPage page : pages) {
        IEditorReference[] editorRefs = page.getEditorReferences();
        for (IEditorReference editorRef : editorRefs) {
          editorRef.getEditor(true);
        }
      }
    } catch (WorkbenchException we) {
      LOGGER.error("Cannot restore window's state: " + we.getMessage());
      we.printStackTrace();
    }
    return desc;
  }

  /**
   * Find all the open perspectives in a window
   * @param window The window containing the perspectives
   * @return A list of all openperspectives in a window
   */
  private IPerspectiveDescriptor[] findOpenPerspectivesInWindow(IWorkbenchWindow window) {
    ArrayList<IPerspectiveDescriptor> perspectives = new ArrayList<IPerspectiveDescriptor>();
    IWorkbenchPage[] pages = window.getPages();
    for (IWorkbenchPage page : pages) {
      IPerspectiveDescriptor[] descs = page.getOpenPerspectives();
      for (IPerspectiveDescriptor desc : descs) {
        perspectives.add(desc);
      }
    }
    return perspectives.toArray(new IPerspectiveDescriptor[perspectives.size()]);
  }

  /**
   * Find the workbench window containing a viewer
   * @param viewerPart 
   * @return If found, the workbenchwindow containing the viewer; otherwise, null
   */
  public IWorkbenchWindow findViewerWindow(Object viewerPart) {
    String partID = ServiceProvider.getViewersService().getViewerPartID(viewerPart);
    IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
    for (IWorkbenchWindow window : windows) {
      IWorkbenchPage[] pages = window.getPages();
      for (IWorkbenchPage page : pages) {
        IViewReference[] views = page.getViewReferences();
        for (IViewReference view : views) {
          IViewPart viewPart = view.getView(false);
          if (viewPart != null && ServiceProvider.getViewersService().getViewerPartId(viewPart).equals(partID)) {
            return window;
          }
        }
      }
    }
    return null;
  }

  /**
   * Determine if a viewer part is in a restored window
   * @param window The restored window
   * @param partID The unique part ID of the viewer
   * @return The viewer object if found in the restored window; otherwise, null
   */
  public Object findViewerInWindow(IWorkbenchWindow window, String partID) {
    IWorkbenchPage[] pages = window.getPages();
    for (IWorkbenchPage page : pages) {
      IViewReference[] views = page.getViewReferences();
      for (IViewReference view : views) {
        IViewPart viewPart = view.getView(true);
        if (viewPart != null && ServiceProvider.getViewersService().getViewerPartId(viewPart).equals(partID)) {
          return ServiceProvider.getViewersService().getViewer(viewPart);
        }
      }
    }
    return null;
  }

  /**
   * Find the viewers restored in a window
   * @param window The restored window
   * @param partID The unique part ID of the multiple viewers restored in the window
   * @return List of viewer objects if found; otherwise, null
   */
  public Object[] findViewersInWindow(IWorkbenchWindow window, String partID) {
    IWorkbenchPage[] pages = window.getPages();
    for (IWorkbenchPage page : pages) {
      IViewReference[] views = page.getViewReferences();
      for (IViewReference view : views) {
        IViewPart viewPart = view.getView(true);
        if (viewPart != null && ServiceProvider.getViewersService().getViewerPartId(viewPart).equals(partID)) {
          return ServiceProvider.getViewersService().getViewers(viewPart);
        }
      }
    }
    return null;
  }

  /**
   * Read the state of a workbench window from the state session file
   * window which has been opened. The state is preceded by an XML
   * comment containing the window's ID and starts with "<?xml"
   * @param winID The ID of the window state to be read
   * @return The state of the workbench window
   */
  private String readWindowState(String winID) {
    StringBuffer buf = new StringBuffer();
    String line = readXML();
    if (line == null) {
      return "";
    }
    //skip to the XML comment which is the start of the window's state
    boolean foundState = false;
    while (line != null && !foundState) {
      if (line.startsWith("<!--") && extractWorkbenchWindowID(line).equals(winID)) {
        foundState = true;
      }
      line = readXML();
    }
    if (!foundState) {
      return "";
    }
    buf.append(line);
    //read to the EOF or the start of the state for the next window
    while ((line = readXML()) != null && !line.startsWith("<!--")) {
      buf.append(line);
    }
    return buf.toString();
  }

  public String getWorkbenchWindowID(IWorkbenchWindow window) {
    String id = window.toString();
    int idx = id.lastIndexOf('@');
    return idx != -1 ? id.substring(idx + 1) : id;
  }

  /**
   * Extract the window ID from the XML comment
   * @param windowInfo XMl comment containing a workbench window ID
   * @return Workbench window ID
   */
  private String extractWorkbenchWindowID(String windowInfo) {
    if (windowInfo.equals("")) {
      return "";
    }
    int idx1 = windowInfo.indexOf("window: ");
    int idx2 = windowInfo.indexOf(" -->");
    return windowInfo.substring(idx1 + 8, idx2);
  }

  /**
   * Save the last active session preference in workspace's user preference property file.
   * @param pathname Full path of the session state XML file (.gcs).
   */
  private void saveLastActiveSession(String pathname) {
    String workspaceDir = Platform.getLocation().toString();
    Properties userPrefs = new Properties();
    String prefsPath = workspaceDir + File.separatorChar + USER_PREFS;
    try {
      //Check if the workspace has a user preference property file.
      //If so, read it.
      File prefs = new File(prefsPath);
      if (prefs.exists() && prefs.isFile()) {
        FileInputStream fis = new FileInputStream(prefsPath);
        userPrefs.load(fis);
      }
      userPrefs.setProperty(LAST_ACTIVE_SESSION_PROP, pathname);
      FileOutputStream fos = new FileOutputStream(prefsPath);
      userPrefs.store(fos, null);
    } catch (FileNotFoundException e) {
      //no .userPreferences.properties file
    } catch (IOException e) {
      //cannot read/write .userPreferences.properties file
    }
  }

  /**
   * Get the last active session preference for the specified workspace
   * @return Path of last active session if one exists; otherwise, the empty string
   */
  private String getLastActiveSessionPref() {
    String workspaceDir = Platform.getLocation().toString();
    Properties userPrefs = new Properties();
    String sessionPath = "";
    try {
      FileInputStream fis = new FileInputStream(workspaceDir + File.separatorChar + USER_PREFS);
      userPrefs.load(fis);
      String prop = userPrefs.getProperty(LAST_ACTIVE_SESSION_PROP);
      if (prop != null) {
        sessionPath = prop;
      }
    } catch (FileNotFoundException e) {
      //no .userPreferences.properties file
    } catch (IOException e) {
      //cannot read .userPreferences.properties file
    }

    return sessionPath;
  }

  /**
   * Save session state to an XML file.
   * <p>
   * Gather the state of each active component by calling their genState() which
   * returns a component's state encapsulated in XML. Wrap the state in <component>
   * tags. Save the state in the specified .gcs file. After processing all active
   * components, get and save the state of GeoCraft itself.
   * <p>
   * If the save fails, a dialog tells the user why the save failed.
   * @param pathname Full path of the State XML file to be created.
   * @param batch true if saving batch state, false if saving session state
   */
  @SuppressWarnings("restriction")
  public void saveAlgorithmAsBatchSession(String pathname, Model[] algorithms) {
    //remember latest saveset
    _currentSaveset = pathname;

    //open session file for writing
    openXMLWriter(pathname, false);

    ComponentState.writePrologue();

    //NOTE: The repository and algorithms are common to all perspectives whereas
    //      viewers are specific to a perspective

    //Build up State XML piecemeal: repository, algorithms, perspectives and their views
    //OPEN ROOT element: <geocraft>
    ComponentState.openRootElement();

    //START REPOSITORY
    ComponentState.openRepositoryElement();

    //get list of all loaded data entities
    if (_repo != null) {
      Map<String, Object> reposMap = _repo.getAll();
      //Get the data entities in the repository
      Object[] objects = reposMap.values().toArray();
      //Get the keys of the hash map containing all the entities in the repository.
      //Note: The key is the var name associated with the entity
      Set<String> varNames = reposMap.keySet();
      Iterator<String> varsIter = varNames.iterator();
      String[] vars = new String[varNames.size()];
      int k = 0;
      while (varsIter.hasNext()) {
        String varName = varsIter.next();
        //Object object = reposMap.get(varName);
        vars[k++] = varName;
      }

      //capture data entity and its model properties
      ComponentState.openEntitiesElement();

      for (int i = 0; i < objects.length; i++) {
        Object object = objects[i];
        if (object instanceof Entity) {
          Entity entity = (Entity) object;
          MapperModel model = entity.getMapper().getModel();
          if (model == null) { // || entity.isDirty()) {
            continue;
          }
          if (RepositoryIdStore.canSave(entity)) {
            //String uniqueId = RepositoryIdStore.getUniqueId(entity);
            ComponentState.openEntityElement(vars[i], entity.getUniqueID());
            //capture model properties
            Map<String, String> props = model.pickle();
            for (String key : props.keySet()) {
              String val = props.get(key);
              ComponentState.entityPropertyElement(key, val);
            }
            ComponentState.closeEntityElement();
          }
        }
      }

      ComponentState.closeEntitiesElement();
    }

    ComponentState.closeRepositoryElement();
    //END REPOSITORY

    //START ALGORITHMS

    ComponentState.openAlgorithmsElement();

    //get list of all registered standalone algorithms

    //capture active algorithms and their model parameters
    for (int i = 0; i < algorithms.length; i++) {
      String name = algorithms[i].getClass().getSimpleName();
      String className = algorithms[i].getClass().getName();
      String windowID = "";
      //capture algorithm parameters
      Map<String, String> parms = algorithms[i].pickle();
      ComponentState.openAlgorithmElement(name, className, windowID, i, true);
      List<String> keys = Utilities.sortKeys(parms.keySet());
      for (String key : keys) {
        String val = parms.get(key);
        val = val.replace("\r\n", "&#xD;&#xA;"); //Windows (CR+LF)
        val = val.replace("\n", "&#xA;"); //Unix (CR)
        key = key.replace("\"", "&#034;");
        val = val.replace("\"", "&#034;");
        val = val.replace("<", "&#060;");
        val = val.replace(">", "&#062;");
        if (!key.equals("")) {
          ComponentState.algorithmParameterElement(key, val);
        }
      }
      ComponentState.closeAlgorithmElement();
    }

    ComponentState.closeAlgorithmsElement();
    //END ALGORITHMS

    //START PREFERENCES
    IExtensionRegistry extRegistry = Platform.getExtensionRegistry();
    IConfigurationElement[] configElements = extRegistry.getConfigurationElementsFor("org.eclipse.ui.preferencePages");
    String name = "", klass = "";
    for (IConfigurationElement configElement : configElements) {
      name = configElement.getAttribute("name");
      klass = configElement.getAttribute("class");
      try {
        Object prefPage = configElement.createExecutableExtension("class");
        if (prefPage instanceof IGeocraftPreferencePage) {
          ComponentState.openPreferencePageElement(name, klass);
          Map<String, String> prefs = ((IGeocraftPreferencePage) prefPage).getPreferenceState();
          for (String key : prefs.keySet()) {
            String val = prefs.get(key);
            ComponentState.preferenceElement(key, val);
          }
          ComponentState.closePreferencePageElement();
        }
      } catch (CoreException ce) {
        LOGGER.warn("Cannot create " + klass);
        ce.printStackTrace();
      }
    }
    //END PREFERENCES

    //CLOSE ROOT element </geocraft>
    ComponentState.closeRootElement();
    //close XML writer
    closeXMLWriter();
  }
}
