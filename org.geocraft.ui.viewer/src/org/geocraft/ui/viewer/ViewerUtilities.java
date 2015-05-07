/*
 * Copyright (C) ConocoPhillips 2010 All Rights Reserved. 
 */
package org.geocraft.ui.viewer;


public class ViewerUtilities {

  private static final String PART_ID = "PartID";

  private static int _counter = 1;

  /**
   * Create a unique viewer part ID. If the viewer part
   * already has an ID, update the counter used to create
   * a unique one; otherwise, create a new unique ID.
   * @param partID Existing part ID, it any
   * @return Existing part ID or new part ID
   */
  static public String getViewerPartID(String partID) {
    String pid = partID;
    if (partID == null || partID.isEmpty()) {
      pid = "Viewer #" + _counter;
    } else {
      int id = Integer.parseInt(partID.substring(partID.indexOf('#') + 1));
      if (id > _counter) {
        _counter = id;
      }
    }
    _counter++;

    return pid;
  }

}
