package org.geocraft.core.session;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.geocraft.core.common.progress.BackgroundTask;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.internal.session.Activator;
import org.geocraft.core.io.IDatastoreAccessorService;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.model.validation.Validation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;


/**
 * Persists and restores the repository contents. It does this by saving the ids of the
 * mapper models as project local preferences. 
 */
public class RepositoryIdStore extends AbstractStore {

  /** The logger. */
  private static ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(RepositoryIdStore.class);

  public static final String NODE_NAME = "repository.session";

  private static Preferences _sessionData = new InstanceScope().getNode(NODE_NAME);

  /** Classes to not persist. */
  private static final String[] EXCLUDED_ENTITY_CLASSES = { "SeismicSurvey3d", "SeismicSurvey2d" };

  /** Currently unsupported model types. */
  private static final String[] EXCLUDED_MODEL_CLASSES = { "InMemoryMapperModel" };

  final static String EOL = System.getProperty("line.separator");

  /**
   * Saves the id of the contents of the repository to the session preferences store. 
   */
  public static void save() {

    BackgroundTask task = new BackgroundTask() {

      /**
       * @param logger  
       * @param monitor 
       * @throws CoreException 
       */
      @Override
      public Object compute(final ILogger logger, final IProgressMonitor monitor) throws CoreException {
        return saveTask(monitor);
      }
    };

    TaskRunner.runTask(task, "Saving Repository...", TaskRunner.INTERACTIVE);
  }

  /**
   * This restores the repository's last saved session.
   */
  public static void restore() {

    BackgroundTask task = new BackgroundTask() {

      /**
       * @param logger  
       * @throws CoreException 
       */
      @Override
      public Object compute(final ILogger logger, final IProgressMonitor monitor) throws CoreException {
        return restoreTask(monitor);
      }

    };

    TaskRunner.runTask(task, "Restoring Repository...", TaskRunner.INTERACTIVE);

  }

  /**
   * Save the repository - entity models, repository IDs and var names associated
   * with entities.
   * <p>
   * Use an array of values from the repository in order to
   * prevent a concurrent modification exception when the
   * repository changes as part of a multi-entity load
   * @param monitor 
   */
  private static synchronized Object saveTask(final IProgressMonitor monitor) {

    IRepository repo = ServiceProvider.getRepository();
    if (repo != null) {
      Map<String, Object> reposMap = repo.getAll();
      //Get the entities in repository
      Object[] objects = reposMap.values().toArray();
      //Get the keys of the hash map containing all the entities in the repository.
      //Note: The key is the var name associated with the entity
      Set<String> varNames = reposMap.keySet();
      Iterator<String> varsIter = varNames.iterator();
      String[] vars = new String[varNames.size()];

      initializeRepoSession();

      List<MapperModel> saveableModels = new ArrayList<MapperModel>();
      int k = 0;
      while (varsIter.hasNext()) {
        String varName = varsIter.next();
        Object object = reposMap.get(varName);
        // collect the var names associated with each repository object
        vars[k++] = varName;

        if (object instanceof Entity) {
          Entity entity = (Entity) object;

          // no need to save if the mapper does not contain a 
          // mapper model (ie. FakePostStack3dmapper)
          if (entity.getMapper().getModel() == null || entity.isDirty()) {
            continue;
          }

          if (canSave(entity)) {
            saveableModels.add(entity.getMapper().getModel());
          }
        }
      }
      //Persist the mapper models
      MapperParameterStore.save(saveableModels.toArray(new MapperModel[0]));
      // MapperParameterStore.dumpParameters(saveableModels);

      //Persist the list of unique ids of entities to restore and
      //their associated var name
      saveRepositoryId(vars, objects);
      //dumpPreferences();
    }
    return new Status(IStatus.OK, Activator.PLUGIN_ID, "saved repository");
  }

  private static synchronized Object restoreTask(final IProgressMonitor monitor) {

    LOGGER.info("Restoring session");
    try {
      //start the event service if it isn't created already
      //MessageBus.getDefaultEventService();

      String[] childrenNames = _sessionData.childrenNames();

      //preferences contains entity var names; extract, save and remove them
      HashMap<String, String> varNames = new HashMap<String, String>();
      for (String uniqueId : childrenNames) {
        Preferences prefs = _sessionData.node(uniqueId);
        String[] keys = prefs.keys();
        for (String key : keys) {
          int idx = key.indexOf("@*%");
          if (idx != -1) {
            String varName = key.substring(0, idx);
            System.out.println("uniqueId=" + prefs.get(key, "") + ", varName=" + varName);
            varNames.put(prefs.get(key, ""), varName);
            prefs.remove(key);
          }
        }
      }
      IRepository repo = ServiceProvider.getRepository();
      //Set the var names to be used when loading the entities
      if (repo != null) {
        repo.setVarnameMap(varNames);
      }

      monitor.beginTask("Restoring Entities", childrenNames.length);
      for (String uniqueId : childrenNames) {
        LOGGER.debug("Restoring entity with unique ID: " + uniqueId);
        monitor.setTaskName("Restoring " + uniqueId);
        try {
          Preferences prefs = MapperParameterStore.lookup(uniqueId);
          //MapperParameterStore.restore(model);
          restoreEntity(prefs, uniqueId);
        } catch (Exception ex) {
          LOGGER.error("Error restoring entity with unique ID: " + uniqueId, ex);
        }
        monitor.worked(1);
      }
      monitor.done();
    } catch (BackingStoreException e1) {
      e1.printStackTrace();
    }
    return null;
  }

  private static String[] getRepoStartupState() {
    try {
      return _sessionData.childrenNames();
    } catch (BackingStoreException e) {
      e.printStackTrace();
      return new String[] {};
    }
  }

  private static String[] getRepoCurrentState() {
    if (ServiceProvider.getRepository() != null) {
      Object[] objects = ServiceProvider.getRepository().getAll().values().toArray();
      List<String> repoIds = new ArrayList<String>();
      for (Object object : objects) {
        if (object instanceof Entity) {
          Entity entity = (Entity) object;
          String uniqueId = getUniqueId(entity);
          if (!uniqueId.isEmpty()) {
            //Don't add duplicates
            //Note: duplicates arise from the fact both a well and its bore
            //are included and they share the same model
            if (!repoIds.contains(uniqueId)) {
              repoIds.add(uniqueId);
            }
          }
        }
      }
      return repoIds.toArray(new String[0]);
    }
    return new String[] {};
  }

  public static boolean repositoryModified() {
    String[] startupRepoState = getRepoStartupState();
    String[] currentRepoState = getRepoCurrentState();

    Arrays.sort(startupRepoState);
    Arrays.sort(currentRepoState);

    return !Arrays.deepEquals(startupRepoState, currentRepoState);
  }

  /**
   * 
   * @param model
   * @param prefs
   */
  public static synchronized void save(final Model model, final Preferences prefs) {
    if (canSave(model)) {
      Map<String, String> parms = model.pickle();
      for (String key : parms.keySet()) {
        prefs.put(key, parms.get(key));
      }
    }
  }

  /**
   * Restore model properties from preferences.
   * <p>
   * This may be used to restore the the properties of a mapper model. 
   * 
   * @param model the model to restore.
   * @param prefs the preferences from which to restore the properties.
   */
  public static synchronized void restore(final Model model, final Preferences prefs) {

    if (model.isRestoreable(lookupNode(prefs, model.getClass().getSimpleName()))) {
      try {
        model.unpickle(getParameters(prefs));
      } catch (Exception e) {
        LOGGER.error(e + " failed to restore " + model.toString());
        e.printStackTrace();
        try {
          //remove invalid node to prevent geocraft from restoring obsolete properties
          lookupNode(prefs, model.getClass().getSimpleName()).removeNode();
        } catch (BackingStoreException e1) {
          e1.printStackTrace();
        }
      }
    }
  }

  private static void saveRepositoryId(final String[] varNames, final Object[] entities) {
    for (int i = 0; i < entities.length; i++) {
      Object entity = entities[i];
      if (entity instanceof Entity) {
        //append suffix to make sure var name never the same as the entity's unique ID
        String varName = varNames[i] + "@*%";
        saveRepositoryId(varName, (Entity) entity);
      }
    }

    try {
      _sessionData.flush();
    } catch (BackingStoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static String getUniqueId(final Entity entity) {
    MapperModel m = entity.getMapper().getModel();
    if (m == null) {
      return "";
    }
    String uniqueId;

    // check to see if saving the model's own unique id is ok
    // - usually the case unless this is 2d data, in which case, 
    // we need to save the id of the line's collection
    if (m.useDefaultId()) {
      uniqueId = m.getUniqueId().replace("/", ".");
    } else {
      uniqueId = m.getCustomId();
    }
    return uniqueId;
  }

  /**
   * Saves the unique Id of the entity to the repository preferences node.
   * @param varName var name associated with entity
   * @param entity to save
   */
  private static void saveRepositoryId(final String varName, final Entity entity) {
    String uniqueId = getUniqueId(entity);
    Preferences idToSave = _sessionData.node(uniqueId);
    idToSave.put(uniqueId, uniqueId);
    //for var names, use the mapper's unique ID which contains the model's as a substring
    idToSave.put(varName, entity.getUniqueID());
    //idToSave.put(varName, uniqueId);
  }

  /**
   * Clear out all of the saved data in the repository session preferences node
   * so that entities removed from the repository will not be restored 
   */
  private static void initializeRepoSession() {
    try {
      for (String s : _sessionData.childrenNames()) {
        Preferences pref = _sessionData.node(s);
        pref.removeNode();
      }
      _sessionData.flush();
    } catch (BackingStoreException e) {
      e.printStackTrace();
    }

  }

  /**
   * Load an entity back in to the repository. 
   * 
   * @param parms Map of model parameters
   * @param variableName
   * @throws BackingStoreException
   */
  public static void restoreEntity(final Map<String, String> parms, final String variableName) throws BackingStoreException {
    if (parms != null && parms.get("class") != null) {
      String modelClassName = parms.get("class").substring("class ".length());

      IDatastoreAccessorService datastoreAccessorService = ServiceProvider.getDatastoreAccessorService();

      if (datastoreAccessorService == null) {
        LOGGER.error("Could not access the dataStoreAccessorService - skipping " + variableName);
        return;
      }

      MapperModel model = datastoreAccessorService.createMapperModelFromClassName(modelClassName);
      if (model != null) {
        try {
          model.unpickle(parms);

          //add validation to make sure model is valid
          IValidation val = new Validation();

          // Only attempt to restore an entity if it still exists in the datastore.
          if (model.existsInStore()) {
            model.validate(val);
            if (!val.containsError()) {
              datastoreAccessorService.restoreEntityFromMapperModel(model);
            } else {
              LOGGER.error("Could not restore entity: " + variableName);
              LOGGER.error("  Model validation failed: " + val.getStatusMessages(0));
              AbstractStore.lookupNode(_sessionData, variableName).removeNode();
              _sessionData.flush();
            }
          } else {
            // If it no longer exists in the datastore, report a warning.
            LOGGER.warn("Could not restore entity: " + variableName + ". No longer exists in datastore.");
            AbstractStore.lookupNode(_sessionData, variableName).removeNode();
            _sessionData.flush();
          }
        } catch (Exception ex) {
          LOGGER.error("Could not restore entity: " + ex.getMessage());
          logStackTrace(ex.getStackTrace());
        }
      }
    }
  }

  /**
   * Load an entity back in to the repository. 
   * 
   * @param prefs
   * @param variableName
   * @throws BackingStoreException
   */
  private static void restoreEntity(final Preferences prefs, final String variableName) throws BackingStoreException {
    if (prefs != null && prefs.get("class", null) != null) {
      String modelClassName = prefs.get("class", null).substring("class ".length());

      IDatastoreAccessorService datastoreAccessorService = ServiceProvider.getDatastoreAccessorService();

      if (datastoreAccessorService == null) {
        LOGGER.error("Could not access the dataStoreAccessorService - skipping " + variableName);
        return;
      }

      MapperModel model = datastoreAccessorService.createMapperModelFromClassName(modelClassName);
      if (model != null) {
        try {
          if (model.isRestoreable(prefs)) {
            Map<String, String> parms = getParameters(prefs);
            model.unpickle(parms);

            //add validation to make sure model is valid
            IValidation val = new Validation();

            // Only attempt to restore an entity if it still exists in the datastore.
            if (model.existsInStore()) {
              model.validate(val);
              if (!val.containsError()) {
                datastoreAccessorService.restoreEntityFromMapperModel(model);
              } else {
                LOGGER.error("Could not restore entity: " + variableName);
                LOGGER.error("  Model validation failed: " + val.getStatusMessages(0));
                AbstractStore.lookupNode(_sessionData, variableName).removeNode();
                _sessionData.flush();
              }
            } else {
              // If it no longer exists in the datastore, report a warning.
              LOGGER.warn("Could not restore entity: " + variableName + ". No longer exists in datastore.");
              // Remove the node from the session data to prevent its reloading.
              AbstractStore.lookupNode(_sessionData, variableName).removeNode();
              _sessionData.flush();
            }
          }
        } catch (Exception ex) {
          LOGGER.error("Could not restore entity: " + ex.getMessage());
          logStackTrace(ex.getStackTrace());
        }
      }
    }
  }

  /**
   * Check if this is a mapper model that we want to save. 
   * 
   * @param model
   * @return
   */
  private static boolean canSave(final Model model) {
    String name = model.getClass().getName();
    for (String s : EXCLUDED_MODEL_CLASSES) {
      if (name.equals(s)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if the entity is excluded from saving.
   * 
   * @param entity
   * @return
   */
  public static boolean canSave(final Entity entity) {
    for (String excludeString : EXCLUDED_ENTITY_CLASSES) {
      if (excludeString.equals(entity.getType())) {
        return false;
      }
    }
    return true;
  }

  static void logStackTrace(StackTraceElement[] ste) {
    StringBuffer buf = new StringBuffer("Stack trace:" + EOL);

    for (StackTraceElement element : ste) {
      buf.append(element.toString() + EOL);
    }

    LOGGER.error(buf.toString());
  }

  public static String dumpPreferences() {
    StringBuffer txt = new StringBuffer("Repository Session Contents\n");
    txt = dumpPreferences(_sessionData, "  ", txt);
    LOGGER.debug(txt.toString());
    return txt.toString();
  }

}
