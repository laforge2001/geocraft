/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.action;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.abavo.ABavoImages;
import org.geocraft.abavo.Activator;
import org.geocraft.abavo.crossplot.ABDataSeries;
import org.geocraft.abavo.crossplot.IABavoCrossplot;
import org.geocraft.abavo.ellipse.EllipseRegionsModel.EllipseType;
import org.geocraft.core.common.util.UserAssistMessageBuilder;
import org.geocraft.math.regression.RegressionDataStatistics;
import org.geocraft.math.regression.RegressionMethodDescription;
import org.geocraft.math.regression.RegressionStatistics;
import org.geocraft.ui.plot.action.IPlotMouseAction;


/**
 * This action initiates the construction of an ellipse in the crossplot. The type
 * of ellipse being created (Background, Maximum or Selection) is passed into the constructor.
 * The major axis along which to generate the ellipse is based upon the active series and the
 * active regression line, as currently selected in the crossplot view. If the active series
 * does not contain any data, an error dialog will appear explaining the problem and offering
 * possible solutions. In this case, the solution include adding data to the active series and
 * trying again, or else switching the active series to one already containing data.
 */
public class ConstructEllipse extends Action {

  /** The crossplot in which to construct the ellipse. */
  private final IABavoCrossplot _crossplot;

  /** The type of ellipse to construct. */
  private final EllipseType _ellipseType;

  public ConstructEllipse(final IABavoCrossplot crossplot, final EllipseType ellipseType, final boolean setText) {
    // Set the action image based on the type of ellipse.
    if (ellipseType.equals(EllipseType.Background)) {
      setImageDescriptor(Activator.getDefault().createImageDescriptor(ABavoImages.BACKGROUND_ELLIPSE));
    } else if (ellipseType.equals(EllipseType.Maximum)) {
      setImageDescriptor(Activator.getDefault().createImageDescriptor(ABavoImages.MAXIMUM_ELLIPSE));
    } else if (ellipseType.equals(EllipseType.Selection)) {
      setImageDescriptor(Activator.getDefault().createImageDescriptor(ABavoImages.SELECTION_ELLIPSE));
    } else {
      setImageDescriptor(Activator.getDefault().createImageDescriptor(ABavoImages.ELLIPSE));
    }
    String text = "Construct the " + ellipseType.toString().toLowerCase() + " ellipse";
    if (setText) {
      setText(text);
    }
    setToolTipText(text);
    _crossplot = crossplot;
    _ellipseType = ellipseType;
  }

  @Override
  public void run() {
    // Check that the active series is valid.
    int seriesIndex = _crossplot.getActiveSeriesIndex();
    String seriesId = "Series #" + (seriesIndex + 1);
    ABDataSeries series = _crossplot.getDataSeries(seriesIndex);
    if (series == null) {
      // If series is not valid, inform the user with an error dialog.
      Shell shell = new Shell(Display.getCurrent());

      UserAssistMessageBuilder message = new UserAssistMessageBuilder();
      message.setDescription("Cannot construct the " + _ellipseType.toString().toLowerCase() + " ellipse.");
      message.addReason("The active data series (" + seriesId + ") is empty.");
      message.addSolution("Crossplot data into " + seriesId + ".");
      message.addSolution("Switch to a non-empty active series.");

      MessageDialog.openError(shell, "Ellipse Error", message.toString());
      shell.dispose();

      // Do not continue.
      return;
    }

    // Get the selected regression method.
    RegressionMethodDescription regressionMethod = _crossplot.getModel().getRegressionMethod();

    // Get the regression stats from the active series.
    RegressionStatistics regression = series.getRegression(regressionMethod);
    RegressionDataStatistics dataStats = series.getRegressionDataStatistics();
    double xCenter = 0;
    double yCenter = 0;
    if (!_crossplot.getModel().getAnchoredToOrigin()) {
      xCenter = dataStats.getXBar();
      yCenter = dataStats.getYBar();
    }

    // Allocate 2 new mouse actions for ellipse definition.
    IPlotMouseAction[] actions = new IPlotMouseAction[2];

    // Create an action for updating the ellipse definition based on the current cursor position.
    actions[0] = new EllipseDefinition(_crossplot, _ellipseType, regression.getSlope(), regression.getIntercept(),
        xCenter, yCenter);

    // Create a mouse action for ending the ellipse definition.
    actions[1] = new EndEllipseDefinition(_crossplot, _ellipseType);

    // Set the new mouse actions for the crossplot and change the cursor.
    _crossplot.setDefaultActions();
    _crossplot.setMouseActions(actions, SWT.CURSOR_ARROW);
  }
}
