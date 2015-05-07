package org.geocraft.abavo.crossplot;


import org.eclipse.swt.widgets.Composite;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.internal.abavo.ABavoCrossplotRegistry;
import org.geocraft.ui.plot.axis.Axis;
import org.geocraft.ui.plot.axis.AxisRange;
import org.geocraft.ui.plot.axis.IAxis;
import org.geocraft.ui.plot.defs.Alignment;
import org.geocraft.ui.plot.label.ILabel;
import org.geocraft.ui.plot.label.Label;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.ModelSpace;
import org.geocraft.ui.viewer.AbstractViewerPart;
import org.geocraft.ui.viewer.IViewer;


public class ABavoCrossplotViewPart extends AbstractViewerPart {

  public static final String ID = "org.geocraft.abavo.CrossplotView";

  @Override
  public IViewer createViewer(final Composite parent) {
    String modelSpaceName = "Default A vs B";
    ILabel xAxisLabel = new Label("A", org.geocraft.ui.plot.defs.Orientation.HORIZONTAL, Alignment.CENTER, true);
    IAxis xAxis = new Axis(xAxisLabel, Unit.UNDEFINED, new AxisRange(-100, 100),
        org.geocraft.ui.plot.defs.Orientation.HORIZONTAL);
    ILabel yAxisLabel = new Label("B", org.geocraft.ui.plot.defs.Orientation.VERTICAL, Alignment.CENTER, true);
    IAxis yAxis = new Axis(yAxisLabel, Unit.UNDEFINED, new AxisRange(-100, 100),
        org.geocraft.ui.plot.defs.Orientation.VERTICAL);
    IModelSpace modelSpace = new ModelSpace(modelSpaceName, xAxis, yAxis);
    ABavoCrossplot crossplot = new ABavoCrossplot(parent, modelSpace, getSite());
    crossplot.setTitle("Seismic Volume Crossplot");
    ABavoCrossplotRegistry.get().addCrossplot("Standard ABAVO Crossplot", crossplot);
    return crossplot;
  }

  @Override
  public void setFocus() {
    // No action.
  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.viewer.IViewerPart#getViewers()
   */
  @Override
  public IViewer[] getViewers() {
    IViewer viewer = getViewer();
    return new IViewer[] { viewer };
  }

}
