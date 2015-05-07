/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.views;


import org.eclipse.ui.part.ViewPart;


/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public abstract class SampleView extends ViewPart {

  //  public static final String ID = "org.geocraft.ui.plot.views.SampleView";
  //
  //  private PlotView _plotView;
  //
  //  /**
  //   * The constructor.
  //   */
  //  public SampleView() {
  //    // Nothing to do.
  //  }
  //
  //  /**
  //   * This is a callback that will allow us to create the viewer and initialize
  //   * it.
  //   */
  //  @Override
  //  public void createPartControl(final Composite parent) {
  //    PlotScrolling scrolled = PlotScrolling.BOTH;
  //    String plotModelName = "Intercept-Gradient Model";
  //    ILabel xAxisLabel = new Label("Intercept", Orientation.HORIZONTAL, Alignment.CENTER, true);
  //    IAxis xAxis = new Axis(xAxisLabel, Unit.SEISMIC_AMPLITUDE, new AxisRange(1, 10), Orientation.HORIZONTAL, false);
  //    //xAxis.setLineProperties(new PlotLineProperties(PlotLineStyle.DASHED, 1, BLACK));
  //    ILabel yAxisLabel = new Label("Gradient", Orientation.VERTICAL, Alignment.CENTER, true);
  //    IAxis yAxis = new Axis(yAxisLabel, Unit.SEISMIC_AMPLITUDE, new AxisRange(1, 10), Orientation.VERTICAL, false);
  //    //yAxis.setLineProperties(new PlotLineProperties(PlotLineStyle.DASHED, 1, BLACK));
  //    IModelSpace modelSpace = new ModelSpace(plotModelName, xAxis, yAxis, 2);
  //
  //    IPlotLayer group = new PlotLayer("Foo");
  //    for (int k = 0; k < 10; k++) {
  //      IPlotPolygon polygon = new PlotPolygon();
  //      polygon.setFillStyle(FillStyle.SOLID);
  //      polygon.setFillColor(createRandomRGB());
  //      for (int i = 0; i < 10; i++) {
  //        IPlotPoint point = new PlotPoint(10 * Math.random(), 10 * Math.random(), 10 * Math.random());
  //        point.setPointColor(createRandomRGB());
  //        PointStyle style = createRandomPointStyle();
  //        point.setPointStyle(style);
  //        point.setPointSize((int) (10 * Math.random()));
  //        point.setPropertyInheritance(false);
  //        polygon.addPoint(point);
  //      }
  //      group.addShape(polygon);
  //    }
  //    modelSpace.addLayer(group, true);
  //    _plotView = new PlotView(parent, "Sample Plot", modelSpace, scrolled, false, false);
  //
  //    int numColors = 64;
  //    RGB[] colors = new RGB[numColors];
  //    for (int i = 0; i < numColors; i++) {
  //      int rgb = (int) (255 * i / (float) (numColors - 1));
  //      colors[i] = new RGB(rgb, rgb, rgb);
  //    }
  //    ColorMapModel colorMapModel = new ColorMapModel(colors);
  //    ColorBar colorBar = new ColorBar(colorMapModel, -100.0, 100.0, 10);
  //    //ColorBarEditorDialog dialog = ColorBarEditorDialog.createEditor(colorBar);
  //    //dialog.open();
  //  }
  //
  //  @Override
  //  public void setFocus() {
  //    // No action.
  //  }
  //
  //  @Override
  //  public void dispose() {
  //    super.dispose();
  //    _plotView.dispose();
  //  }
  //
  //  private RGB createRandomRGB() {
  //    int r = (int) (Math.random() * 256);
  //    int g = (int) (Math.random() * 256);
  //    int b = (int) (Math.random() * 256);
  //    return new RGB(r, g, b);
  //  }
  //
  //  private PointStyle createRandomPointStyle() {
  //    PointStyle[] styles = PointStyle.values();
  //    int index = (int) (Math.random() * styles.length);
  //    return styles[index];
  //  }
}