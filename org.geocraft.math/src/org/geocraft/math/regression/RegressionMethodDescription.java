/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.math.regression;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;


public class RegressionMethodDescription {

  private final IConfigurationElement _description;

  public RegressionMethodDescription(final IConfigurationElement description) {
    _description = description;
  }

  public String getName() {
    return _description.getAttribute("name");
  }

  public String getAcronym() {
    return _description.getAttribute("acronym");
  }

  public RegressionStatistics compute(final RegressionType type, final RegressionData data) {
    try {
      AbstractRegressionComputer computer = getComputer();
      return computer.compute(type, data);
    } catch (CoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  private AbstractRegressionComputer getComputer() throws CoreException {
    AbstractRegressionComputer computer = (AbstractRegressionComputer) _description.createExecutableExtension("computer");
    computer.setMethod(this);
    return computer;
  }

  @Override
  public String toString() {
    return getName();
  }
}
