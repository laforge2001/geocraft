/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.model.well;


import org.geocraft.core.model.fault.FaultType;
import org.geocraft.core.model.mapper.IMapper;


/**
 * The FaultPick class defines a fault pick found in a well bore.
 */
public class FaultPick extends WellPick {

  /** 
   * The horizontal component of separation or displacement on a fault.
   * The unit of measurement is the application horizontal distance unit.
   */
  private float _faultHeave;

  /**
   * The length of the vertical displacement of a fault.
   * The unit of measurement is the application horizontal distance unit.
   */
  private float _faultThrow;

  /** The type of fault (e.g. Normal, Reverse, etc). */
  private FaultType _faultType;

  /**
   * Constructs a well pick associated with a given well bore.
   * 
   * @param name name of the fault pick.
   * @param wellBore WellBore object that contains the pick.
   * @param pickValueMD The measured depth value of the pick.
   */
  public FaultPick(final String name, final IMapper mapper, final Well well) {
    super(name, mapper, well);
  }

  /**
   * Returns the horizontal component of separation or displacement on a fault.
   */
  public final float getFaultHeave() {
    load();
    return _faultHeave;
  }

  /**
   * Returns the length of the vertical displacement of a fault.
   */
  public final float getFaultThrow() {
    load();
    return _faultThrow;
  }

  /**
   * Returns the type of fault (e.g. Normal, Reverse, etc).
   */
  public final FaultType getFaultType() {
    load();
    return _faultType;
  }

  /**
   * Sets the horizontal component of separation or displacement on a fault.
   */
  public final void setFaultHeave(final float faultHeave) {
    _faultHeave = faultHeave;
    setDirty(true);
  }

  /**
   * Sets the length of the vertical displacement of a fault.
   */
  public final void setFaultThrow(final float faultThrow) {
    _faultThrow = faultThrow;
    setDirty(true);
  }

  /**
   * Sets the type of fault (e.g. Normal, Reverse, etc).
   */
  public final void setFaultType(final FaultType faultType) {
    _faultType = faultType;
    setDirty(true);
  }

}
