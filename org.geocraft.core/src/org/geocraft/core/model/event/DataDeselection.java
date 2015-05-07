/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.core.model.event;


import org.geocraft.core.model.Entity;


/**
 * An event that indicates which entities have been deselected by the user. The recieving component
 * may hide or deselect these entities in some fashion.
 */
public class DataDeselection extends Event {

  Entity[] _entities;

  public DataDeselection(final String sendId) {
    super(sendId);
  }

  public DataDeselection(final String sendId, final Entity[] entities) {
    super(sendId);
    _entities = entities;
  }

  public void setSelectedEntities(final Entity[] entities) {
    _entities = entities;
  }

}
