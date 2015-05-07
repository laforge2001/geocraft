/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.action;


import org.eclipse.jface.action.Action;
import org.geocraft.abavo.ABavoImages;
import org.geocraft.abavo.Activator;
import org.geocraft.abavo.crossplot.IABavoCrossplot;
import org.geocraft.math.regression.RegressionMethodDescription;


public class SetRegressionMethod extends Action {

  private final IABavoCrossplot _crossplot;

  private final RegressionMethodDescription _method;

  public SetRegressionMethod(final RegressionMethodDescription method, final IABavoCrossplot crossplot) {
    _method = method;
    _crossplot = crossplot;
    if (method.getAcronym().equals("PPD")) {
      setImageDescriptor(Activator.getDefault().createImageDescriptor(ABavoImages.REGRESSION_PPD));
    } else if (method.getAcronym().equals("LSQ")) {
      setImageDescriptor(Activator.getDefault().createImageDescriptor(ABavoImages.REGRESSION_LSQ));
    } else if (method.getAcronym().equals("RMA")) {
      setImageDescriptor(Activator.getDefault().createImageDescriptor(ABavoImages.REGRESSION_RMA));
    }
    setText("Ellipse Major Axis -" + method.toString() + "  ");
    setToolTipText("Select the " + method.toString() + " regression method");
  }

  @Override
  public void run() {
    _crossplot.getModel().setRegressionMethod(_method);
  }
}
