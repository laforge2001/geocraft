/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.session;


import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;


/**
 * Stores the parameters associated with each mapper model to the preferences store. 
 */
public class MapperParameterStore extends AbstractStore {

  /** The logger. */
  private static ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(MapperParameterStore.class);

  public static final String NODE_NAME = "mapper.parameter";

  private static Preferences _sessionData = new InstanceScope().getNode(NODE_NAME);

  /**
   * Finds the node associated with a unique id. 
   * 
   * @param uniqueID
   * @return
   */
  public static Preferences lookup(final String uniqueID) {
    return lookupNode(_sessionData, uniqueID);
  }

  public static void save(final MapperModel[] models) {
    for (MapperModel model : models) {
      Preferences modelName = _sessionData.node(model.getUniqueId().replace("/", "."));
      RepositoryIdStore.save(model, modelName);
    }

    try {
      _sessionData.flush();
    } catch (BackingStoreException e) {
      e.printStackTrace();
    }
  }

  public static void restore(final MapperModel model) {
    try {
      String uniqueID = model.getUniqueId().replace("/", ".");
      if (_sessionData.nodeExists(uniqueID)) {
        Preferences prefs = _sessionData.node(uniqueID);
        try {
          model.unpickle(getParameters(prefs));
        } catch (Exception e) {
          LOGGER.error(e + " failed to restore " + uniqueID);
          e.printStackTrace();
        }
      }
    } catch (BackingStoreException e) {
      e.printStackTrace();
    }
  }

  public static String dumpParameters(List<MapperModel> models) {
    StringBuffer txt = new StringBuffer("Mapper Parameter Contents\n");
    Iterator<MapperModel> modelIter = models.iterator();
    while (modelIter.hasNext()) {
      MapperModel model = modelIter.next();
      String uniqueID = model.getUniqueId().replace("/", ".");
      txt = dumpPreferences(_sessionData.node(uniqueID), "  ", txt);
      LOGGER.debug(txt.toString());
    }
    return txt.toString();
  }
}
