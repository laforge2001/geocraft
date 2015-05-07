/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.core.model.event;


/**
 * Base class for geocraft events
 */
public abstract class Event {

  String _senderId;

  /**
   * The unique id of the sending component.
   * @param sendId
   */
  public Event(final String sendId) {
    _senderId = sendId;
  }

  /**
   * @param myId The unique id of the receiving component.
   * @return Return true if this event was sent from "this" component.
   */
  public boolean isSender(final String myId) {
    boolean isSender;
    if (myId.equals(_senderId)) {
      isSender = true;
    } else {
      isSender = false;
    }
    return isSender;
  }

}
