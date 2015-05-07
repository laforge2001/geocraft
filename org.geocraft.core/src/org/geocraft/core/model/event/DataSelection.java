/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.core.model.event;




/**
 * An event that indicates which entities have been selected by the user. The recieving component
 * may display or use these entities in some fashion.
 */
public class DataSelection extends Event {

  private Object[] _selectedObjects;

  private float[] _selectedPosition;

  public DataSelection(final String sendId) {
    super(sendId);
  }

  public DataSelection(final String sendId, final Object[] selectedObjects, final float[] selectedPosition) {
    super(sendId);
    _selectedObjects = selectedObjects;
    _selectedPosition = selectedPosition;
  }

  public void setSelectedObjects(final Object[] selectedObjects) {
    _selectedObjects = selectedObjects;
  }

  public Object[] getSelectedObjects() {
    return _selectedObjects;
  }

  public float[] getSelectedPosition() {
    return _selectedPosition;
  }

}
