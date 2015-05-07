/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.session;


import java.util.HashMap;
import java.util.Map;

import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;


public class AbstractStore {

  /**
   * Recursively search the preferences tree for a node
   * with the given id. 
   * 
   * @param parent node to start the search from
   * @param id the name of the node
   * @return the preferences node or null if none found
   */
  public static Preferences lookupNode(final Preferences parent, final String id) {

    //    dumpPreferences();

    if (parent.name().equals(id)) {
      return parent;
    }

    try {
      String[] children = parent.childrenNames();
      for (String child : children) {
        Preferences res = lookupNode(parent.node(child), id);
        if (res != null) {
          return res;
        }
      }
    } catch (BackingStoreException e) {
      e.printStackTrace();
    }

    return null;
  }

  /** 
   * Creates a map of name, value pairs from the preferences tree structure
   * for a specific child node. 
   * 
   * @param prefs the child node. 
   * @return
   */
  public static Map<String, String> getParameters(final Preferences prefs) {

    Map<String, String> parms = new HashMap<String, String>();

    try {
      for (String key : prefs.keys()) {
        parms.put(key, prefs.get(key, ""));
      }
    } catch (BackingStoreException e) {
      e.printStackTrace();
    }

    return parms;

  }

  public static StringBuffer dumpPreferences(final Preferences parent, final String indent, StringBuffer res) {

    res.append(indent + parent.toString() + "\n");

    try {

      // recurse the preferences tree
      String[] children = parent.childrenNames();
      for (String child : children) {
        Preferences node = parent.node(child);
        res = dumpPreferences(node, indent + "  ", res);
      }
      // dump the attributes at this node 
      for (String key : parent.keys()) {
        res.append(indent + "  " + key + " " + parent.get(key, "none") + "\n");
      }
      // TODO:
    } catch (BackingStoreException e) {
      e.printStackTrace();
    }

    return res;
  }
}
