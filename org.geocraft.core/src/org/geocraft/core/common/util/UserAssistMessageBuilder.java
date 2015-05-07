/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.util;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class UserAssistMessageBuilder {

  private String _description = "";

  private final List<String> _reasons;

  private final List<String> _solutions;

  public UserAssistMessageBuilder() {
    _reasons = Collections.synchronizedList(new ArrayList<String>());
    _solutions = Collections.synchronizedList(new ArrayList<String>());
    clear();
  }

  /**
   * Sets the description of the error/warning.
   * This will appear as the first section of the message.
   * @param description the description.
   */
  public void setDescription(final String description) {
    _description = description;
  }

  public void addReason(final String reason) {
    _reasons.add(reason);
  }

  public void addSolution(final String solution) {
    _solutions.add(solution);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(_description);
    builder.append("\n\n");
    int numReasons = _reasons.size();
    assert numReasons >= 0 : "Negative number of reasons.";
    switch (numReasons) {
      case 0:
        builder.append("Reason:\n");
        builder.append("Unknown\n");
        break;
      case 1:
        builder.append("Reason:\n");
        builder.append(_reasons.get(0) + "\n");
        break;
      default:
        builder.append("Reasons:\n");
        for (int i = 0; i < numReasons; i++) {
          builder.append("" + (i + 1) + ") " + _reasons.get(i) + "\n");
        }
    }
    builder.append("\n");
    int numSolutions = _solutions.size();
    assert numSolutions >= 0 : "Negative number of solutions.";
    switch (numSolutions) {
      case 0:
        builder.append("Solution:\n");
        builder.append("Unknown\n");
        break;
      case 1:
        builder.append("Solution:\n");
        builder.append(_solutions.get(0) + "\n");
        break;
      default:
        builder.append("Solutions:\n");
        for (int i = 0; i < numSolutions; i++) {
          builder.append("" + (i + 1) + ") " + _solutions.get(i) + "\n");
        }
    }
    return builder.toString();
  }

  public void clear() {
    _description = "";
    _reasons.clear();
    _solutions.clear();
  }
}
