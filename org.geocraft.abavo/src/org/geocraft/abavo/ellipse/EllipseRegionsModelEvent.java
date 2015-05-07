/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.ellipse;


public class EllipseRegionsModelEvent {

  /** Enumeration for the ellipse regions model event type. */
  public static enum Type {
    EllipsesUpdated, RegionBoundariesUpdated, RegionSymmetryUpdated
  }

  /** The ellipse regions model. */
  private final EllipseRegionsModel _model;

  /** The event type. */
  private final Type _type;

  /**
   * The default constructor.
   * @param model the ellipse regions model.
   * @param type the type of event.
   */
  public EllipseRegionsModelEvent(final Type type, final EllipseRegionsModel model) {
    _type = type;
    _model = model;
  }

  /**
   * Gets the ellipse regions model.
   * @return the ellipse regions model.
   */
  public EllipseRegionsModel getEllipseRegionsModel() {
    return _model;
  }

  /**
   * Gets the event type (e.g. EllipsesUpdated, RegionBoundsUpdated, etc).
   * @return the event type.
   */
  public Type getType() {
    return _type;
  }
}
