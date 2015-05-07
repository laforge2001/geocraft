/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.grid;


import org.geocraft.core.model.GeologicInterpretation;
import org.geocraft.core.model.mapper.IMapper;


public abstract class AbstractCubeGridProperty extends GeologicInterpretation {

  private final CubeGrid _grid;

  private int timeEventId = INVALID_EVENT_ID;

  public static final int INVALID_EVENT_ID = -999;

  /**
   * @param name
   * @param mapper
   */
  protected AbstractCubeGridProperty(final String name, final IMapper mapper, final CubeGrid grid) {
    super(name, mapper);
    _grid = grid;
  }

  public abstract void setNumElements(final int numCells);

  public abstract int getSize();

  public abstract String getPropertyName();

  /**
   * USE AT YOUR OWN RISK! 
   * @return a REFERENCE to the internal Cube Grid object 
   */
  public CubeGrid getGrid() {
    return _grid;
  }

  public void setTimeEventId(final int eventId) {
    timeEventId = eventId;
  }

  public int getEventId() {
    return timeEventId;
  }
}
