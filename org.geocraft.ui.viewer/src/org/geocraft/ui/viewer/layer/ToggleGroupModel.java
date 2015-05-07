/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.layer;


import org.geocraft.core.model.Model;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.validation.IValidation;


public class ToggleGroupModel extends Model {

  /** The maximum group #. */
  public static final int MAX_GROUPS = 9;

  /** The array of inclusion properties. */
  private final BooleanProperty[] _isIncluded;

  public ToggleGroupModel() {
    _isIncluded = new BooleanProperty[MAX_GROUPS + 1];
    for (int id = 0; id <= MAX_GROUPS; id++) {
      _isIncluded[id] = addBooleanProperty("Group #" + id, true);
    }
  }

  /**
   * Gets the group inclusion flag for the given group id.
   * 
   * @param id the group id (0-MAX_GROUPS).
   * @return <i>true</i> if included; <i>false</i> if excluded.
   */
  public boolean isIncluded(final int id) {
    if (id >= 0 && id <= MAX_GROUPS) {
      return _isIncluded[id].get();
    }
    return false;
  }

  /**
   * Sets the group inclusion flag for the given group id.
   * 
   * @param id the group id (0-MAX_GROUPS).
   * @param flag <i>true</i> to include; <i>false</i> to exclude.
   */
  public void setIncluded(final int id, final boolean flag) {
    if (id >= 0 && id <= MAX_GROUPS) {
      _isIncluded[id].set(flag);
      return;
    }
    throw new IllegalArgumentException("Invalid group (" + id + "). Must be in the range 1-" + MAX_GROUPS + ".");
  }

  public void validate(final IValidation results) {
    // No validation necessary.
  }
}
