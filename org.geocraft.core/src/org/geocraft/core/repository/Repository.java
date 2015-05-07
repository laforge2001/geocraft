/*
 * Copyright (C) ConocoPhillips 2006 - 2008 All Rights Reserved.
 */
package org.geocraft.core.repository;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.geocraft.core.model.Entity;
import org.geocraft.core.repository.specification.ISpecification;
import org.geocraft.core.repository.specification.ObjectSpecification;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.core.service.message.Topic;


/**
 * A HashMap-based implementation of a repository.
 */
public class Repository implements IRepository {

  /** The logger. */
  private static final ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(Repository.class);

  /** Counter (hack) used to compute unique names. */
  private int _id = 0;

  /** The underlying HashMap. */
  private final Map<String, Object> _map = Collections.synchronizedMap(new LinkedHashMap<String, Object>(100));

  /** Collection of variable names, mapped by unique ID. */
  Map<String, String> _varMap = null;

  public void setVarnameMap(final HashMap<String, String> varMap) {
    _varMap = varMap;
    if (varMap.isEmpty()) {
      return;
    }

    //find the maximum var id and reset the counter used to name them
    Collection<String> vars = varMap.values();
    Iterator<String> varIter = vars.iterator();
    while (varIter.hasNext()) {
      String var = varIter.next();
      String[] subs = var.split("^[a-z, A-Z]+");
      int id = 0;
      try {
        id = Integer.parseInt(subs[1]);
        if (id >= _id) {
          _id = id + 1;
        }
      } catch (NumberFormatException nfe) {
        //do nothing
      }
    }
  }

  /**
   * Constructs a HashMap-based implementation of a repository.
   */
  public Repository() {
    LOGGER.debug("Repository created.");
  }

  public String add(final Object object) {
    String name = "";
    if (_varMap != null && !_varMap.isEmpty()) {
      //Note: Use the mapper's unique ID for var names
      String uniqueId = ((Entity) object).getUniqueID();
      /*
        //Note: uniqueID is 'path' where path contains '/'
        //Note: For Landmark volumes, the uniqueId is 'project file'
        //Want the path with '/' -> '.'
        int idx = uniqueId.indexOf('/');
        if (idx != -1) {
          uniqueId = uniqueId.substring(idx);
          uniqueId = uniqueId.replace("/", ".");
        }
      */
      name = _varMap.get(uniqueId);
      if (name == null) {
        name = getNextVariableName();
      }
    } else {
      name = getNextVariableName();
    }
    // LOGGER.debug("Adding object: " + object.toString() + " with name=" + name);
    _map.put(name, object);

    Map<String, Object> objectsMap = new HashMap<String, Object>();
    objectsMap.put(name, object);
    ServiceProvider.getMessageService().publish(Topic.REPOSITORY_OBJECTS_ADDED, objectsMap);

    return name;
  }

  /**
   * Add an array of data objects to the repository.
   */
  public String[] add(final Object[] objects) {
    Map<String, Object> vars = new HashMap<String, Object>();
    Map<String, Object> objectsMap = new HashMap<String, Object>();
    String[] names = new String[objects.length];

    //Determine if var names already defined
    String name = "";
    if (_varMap != null && !_varMap.isEmpty()) {
      for (int i = 0; i < objects.length; i++) {
        //Note: Use the mapper's unique ID for var names
        String uniqueId = ((Entity) objects[i]).getUniqueID();
        name = _varMap.get(uniqueId);
        if (name == null) {
          name = getNextVariableName();
        }
        vars.put(name, objects[i]);
        names[i] = name;
        objectsMap.put(name, objects[i]);
      }
    } else {
      /* Note: varIdSize never used in this method
          // determine the size of the last variable id
          int lastId = _id + objects.length;
          String newVarId = "" + lastId;
          int varIdSize = newVarId.length();
      */

      for (int i = 0; i < objects.length; i++) {
        name = getNextVariableName();
        vars.put(name, objects[i]);
        names[i] = name;
        objectsMap.put(name, objects[i]);
      }
    }
    synchronized (_map) {
      _map.putAll(vars);
    }
    ServiceProvider.getMessageService().publish(Topic.REPOSITORY_OBJECTS_ADDED, objectsMap);
    return names;
  }

  public Object get(final String key) {
    return _map.get(key);
  }

  public synchronized String getNextVariableName() {
    // TODO XXX handle types eg v1, h2
    return "var" + _id++;
  }

  // Set variable name based on a size
  public synchronized String getNextVariableName(final int varIdSize) {
    _id++;
    String newVarId = "" + _id;
    String newVarName = "var";
    int varNameSize = varIdSize + 3;
    while (newVarName.length() + newVarId.length() < varNameSize) {
      newVarName = newVarName + "0";
    }
    newVarName = newVarName + newVarId;
    return newVarName;
  }

  /**
   * Reset the variables.
   */
  public void clear() {
    remove(_map.keySet().toArray(new String[_map.keySet().size()]));
  }

  /**
   * Does the object manager contain anything that matches the specification?
   * 
   * @return true if there is one or more matches.
   */
  public boolean contains(final ISpecification spec) {
    Map<String, Object> results = get(spec);
    return results.size() > 0;
  }

  public void add(final String key, final Object value) {
    // LOGGER.debug("Adding object with key:" + key);
    synchronized (_map) {
      _map.put(key, value);
    }

    StringBuffer repositoryKeys = new StringBuffer();
    for (Object element : _map.keySet()) {
      String type = (String) element;
      repositoryKeys.append(type + ",");
    }

    // LOGGER.debug("Repository keys:" + repositoryKeys.toString());

    Map<String, Object> objectMap = new HashMap<String, Object>();
    objectMap.put(key, value);
    ServiceProvider.getMessageService().publish(Topic.REPOSITORY_OBJECTS_ADDED, objectMap);
  }

  public Map<String, Object> get(final ISpecification filter) {
    Map<String, Object> result = new HashMap<String, Object>();
    List<Entry> entries = new ArrayList<Entry>();
    synchronized (_map) {
      entries.addAll(_map.entrySet());
    }
    for (Entry entry : entries) {
      if (filter.isSatisfiedBy(entry.getValue())) {
        result.put((String) entry.getKey(), entry.getValue());
      }
    }

    return result;
  }

  public void remove(final String key) {
    Object removed;
    synchronized (_map) {
      removed = _map.remove(key);
    }

    // LOGGER.debug("Remove object with key:" + key);

    HashMap<String, Object> removedObjects = new HashMap<String, Object>();
    removedObjects.put(key, removed);
    ServiceProvider.getMessageService().publish(Topic.REPOSITORY_OBJECTS_REMOVED, removedObjects);

  }

  public void remove(final String[] keys) {
    Map<String, Object> removedObjects = new HashMap<String, Object>();
    for (String key : keys) {
      Object removed;
      synchronized (_map) {
        removed = _map.remove(key);
      }
      removedObjects.put(key, removed);
    }

    ServiceProvider.getMessageService().publish(Topic.REPOSITORY_OBJECTS_REMOVED, removedObjects);
  }

  public void remove(final ISpecification spec) {
    HashMap<String, Object> removedObjects = new HashMap<String, Object>();
    List<Entry> entries = new ArrayList<Entry>();
    Object removed;
    synchronized (_map) {
      entries.addAll(_map.entrySet());
    }
    for (Entry entry : entries) {
      if (spec.isSatisfiedBy(entry.getValue())) {
        synchronized (_map) {
          removed = _map.remove(entry.getKey());
        }
        removedObjects.put(entry.getKey().toString(), removed);
      }
    }
    ServiceProvider.getMessageService().publish(Topic.REPOSITORY_OBJECTS_REMOVED, removedObjects);
  }

  public String lookupVariableName(final Object obj) {
    Map<String, Object> vars = get(new ObjectSpecification(obj));
    if (vars.size() < 1) {
      LOGGER.warn("Object does not exist in namespace: " + obj);
      return UNKNOWN_VARIABLE;
    } else if (vars.size() > 1) {
      // TODO is it OK to have more than one reference to the same java
      // object?
      assert false : "object loaded more than once" + vars.keySet().toString();
      // LOGGER.warn("Found more than one variable name referencing the object");
    }
    return vars.keySet().toArray(new String[vars.size()])[0];
  }

  public Map<String, Object> getAll() {
    return _map;
  }
}
