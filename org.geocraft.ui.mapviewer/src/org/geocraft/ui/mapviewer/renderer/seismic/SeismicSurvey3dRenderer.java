package org.geocraft.ui.mapviewer.renderer.seismic;


import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.geocraft.core.common.math.GeometryUtil;
import org.geocraft.core.common.util.Labels;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.ui.mapviewer.IMapViewer;
import org.geocraft.ui.mapviewer.MapViewRenderer;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.attribute.LineProperties;
import org.geocraft.ui.plot.attribute.PointProperties;
import org.geocraft.ui.plot.attribute.TextProperties;
import org.geocraft.ui.plot.defs.FillStyle;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.defs.TextAnchor;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.object.IPlotLine;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.PlotLine;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPolygon;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;


public class SeismicSurvey3dRenderer extends MapViewRenderer {

  /** The 3D seismic survey to render. */
  private SeismicSurvey3d _survey;

  private final SeismicSurvey3dRendererModel _model;

  /** The plot polygon representing the survey outline. */
  private PlotPolygon _plotOutline;

  /** The plot lines representing the survey inlines and xlines.*/
  private List<IPlotLine> _plotLines;

  public SeismicSurvey3dRenderer() {
    super("Seismic Survey 3d Renderer");
    _model = new SeismicSurvey3dRendererModel();
    showReadoutInfo(true);
  }

  @Override
  public void redraw() {
    // No action.
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _survey };
  }

  @Override
  public ReadoutInfo getReadoutInfo(final double x, final double y) {

    String[] keys = { "Inline", "Xline", "Row", "Col" };

    float[] rowColInlineXline = getRowColumnInlineXline(x, y);
    int nrows = _survey.getNumRows();
    int ncols = _survey.getNumColumns();
    if (rowColInlineXline[0] >= 0 && rowColInlineXline[0] < nrows && rowColInlineXline[1] >= 0
        && rowColInlineXline[1] < ncols) {
      String[] values = new String[] { "" + rowColInlineXline[2], "" + rowColInlineXline[3], "" + rowColInlineXline[0],
          "" + rowColInlineXline[1] };
      return new ReadoutInfo(_survey.toString(), keys, values);
    }

    return new ReadoutInfo(_survey.toString(), keys, new String[] { "", "", "", "" });
  }

  @Override
  protected void addPopupMenuActions() {
    IMapViewer viewer = getViewer();

    // Add the action for editing the renderer settings.
    Dialog dialog = new SeismicSurvey3dRendererDialog(getShell(), _survey.getDisplayName(), this);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);

    // Add action for selecting an inline trace section in the map viewer.
    Action constructInlineSection = new ConstructInline3dTraceSection(viewer, _survey);
    addPopupMenuAction(constructInlineSection);

    // Add action for selecting an xline trace section in the map viewer.
    Action constructXlineSection = new ConstructXline3dTraceSection(viewer, _survey);
    addPopupMenuAction(constructXlineSection);

    // Add action for selecting an arbitrary trace section in the map viewer.
    Action constructArbitrarySection = new ConstructArbitrary3dTraceSection(viewer, _survey);
    addPopupMenuAction(constructArbitrarySection);
  }

  @Override
  protected void addPlotShapes() {
    SeismicSurvey3d geometry = _survey;

    // Draw the outline shape.
    RGB outlineColor = new RGB(255, 255, 255);
    _plotOutline = new PlotPolygon();
    _plotOutline.setTextColor(outlineColor);
    _plotOutline.setPointColor(outlineColor);
    _plotOutline.setPointSize(1);
    _plotOutline.setPointStyle(PointStyle.NONE);
    _plotOutline.setLineColor(outlineColor);
    _plotOutline.setLineWidth(1);
    _plotOutline.setLineStyle(LineStyle.SOLID);
    _plotOutline.setFillStyle(FillStyle.NONE);
    Point3d[] points = _survey.getCornerPoints().getPointsDirect();
    for (Point3d point : points) {
      IPlotPoint plotPoint = new PlotPoint(point.getX(), point.getY(), point.getZ());
      plotPoint.setPropertyInheritance(true);
      _plotOutline.addPoint(plotPoint);
    }
    addShape(_plotOutline);

    double[] inlines = Labels.computeLabels(geometry.getInlineStart(), geometry.getInlineEnd(), 10);
    float inlineMin = (float) Math.min(inlines[0], inlines[1]);
    float inlineMax = (float) Math.max(inlines[0], inlines[1]);
    float inlineInc = (float) Math.max(1, Math.abs(inlines[2]));
    double[] xlines = Labels.computeLabels(geometry.getXlineStart(), geometry.getXlineEnd(), 10);
    float xlineMin = (float) Math.min(xlines[0], xlines[1]);
    float xlineMax = (float) Math.max(xlines[0], xlines[1]);
    float xlineInc = (float) Math.max(1, Math.abs(xlines[2]));

    NumberFormat formatter = NumberFormat.getIntegerInstance();
    formatter.setGroupingUsed(false);
    RGB labelColor = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW).getRGB();
    Font labelFont = new Font(null, "Courier", 10, SWT.NORMAL);
    TextProperties textProps = _plotOutline.getTextProperties();
    textProps.setFont(labelFont);
    PointProperties pointProps = _plotOutline.getPointProperties();
    LineProperties lineProps = _plotOutline.getLineProperties();
    _plotLines = new ArrayList<IPlotLine>();
    Point3d p1inline0 = null;
    Point3d p1inline1 = null;
    Point3d p2inline0 = null;
    Point3d p2inline1 = null;
    Point3d p1xline0 = null;
    Point3d p1xline1 = null;
    Point3d p2xline0 = null;
    Point3d p2xline1 = null;
    switch (geometry.getOrientation()) {
      case ROW_IS_INLINE:
        p1xline0 = points[0];
        p1xline1 = points[1];
        p2xline0 = points[3];
        p2xline1 = points[2];
        p1inline0 = points[0];
        p1inline1 = points[3];
        p2inline0 = points[1];
        p2inline1 = points[2];
        break;
      case ROW_IS_XLINE:
        p1inline0 = points[0];
        p1inline1 = points[1];
        p2inline0 = points[3];
        p2inline1 = points[2];
        p1xline0 = points[0];
        p1xline1 = points[3];
        p2xline0 = points[1];
        p2xline1 = points[2];
        break;
    }

    // Draw the inline shapes.
    for (float inline = inlineMin; inline <= inlineMax; inline += inlineInc) {
      double percent = (inline - geometry.getInlineStart()) / (geometry.getInlineEnd() - geometry.getInlineStart());
      Point3d p1 = Point3d.interpolate(p1inline0, p1inline1, percent);
      Point3d p2 = Point3d.interpolate(p2inline0, p2inline1, percent);
      double dx = p2.getX() - p1.getX();
      double dy = p2.getY() - p1.getY();
      dx *= 0.025;
      dy *= 0.025;
      IPlotPoint plotPoint1a = new PlotPoint(p1.getX(), p1.getY(), p1.getZ());
      IPlotPoint plotPoint1b = new PlotPoint(p1.getX() - dx, p1.getY() - dy, p1.getZ());
      plotPoint1b.setName(formatter.format(inline));
      PlotLine plotLine1 = new PlotLine("", textProps, pointProps, lineProps);
      plotLine1.setTextColor(labelColor);
      plotLine1.setTextFont(labelFont);
      plotLine1.setTextAnchor(TextAnchor.getBest(-dx, -dy));
      plotLine1.setPoints(plotPoint1a, plotPoint1b);
      _plotLines.add(plotLine1);
      IPlotPoint plotPoint2a = new PlotPoint(p2.getX(), p2.getY(), p2.getZ());
      IPlotPoint plotPoint2b = new PlotPoint(p2.getX() + dx, p2.getY() + dy, p2.getZ());
      plotPoint2b.setName(formatter.format(inline));
      PlotLine plotLine2 = new PlotLine("", textProps, pointProps, lineProps);
      plotLine2.setTextColor(labelColor);
      plotLine2.setTextFont(labelFont);
      plotLine2.setTextAnchor(TextAnchor.getBest(dx, dy));
      plotLine2.setPoints(plotPoint2a, plotPoint2b);
      _plotLines.add(plotLine2);
      addShape(plotLine1);
      addShape(plotLine2);
    }

    // Draw the xline shapes.
    for (float xline = xlineMin; xline <= xlineMax; xline += xlineInc) {
      double percent = (xline - geometry.getXlineStart()) / (geometry.getXlineEnd() - geometry.getXlineStart());
      Point3d p1 = Point3d.interpolate(p1xline0, p1xline1, percent);
      Point3d p2 = Point3d.interpolate(p2xline0, p2xline1, percent);
      double dx = p2.getX() - p1.getX();
      double dy = p2.getY() - p1.getY();
      dx *= 0.025;
      dy *= 0.025;
      IPlotPoint plotPoint1a = new PlotPoint(p1.getX(), p1.getY(), p1.getZ());
      IPlotPoint plotPoint1b = new PlotPoint(p1.getX() - dx, p1.getY() - dy, p1.getZ());

      plotPoint1b.setName(formatter.format(xline));
      PlotLine plotLine1 = new PlotLine("", textProps, pointProps, lineProps);
      plotLine1.setTextColor(labelColor);
      plotLine1.setTextFont(labelFont);
      plotLine1.setTextAnchor(TextAnchor.getBest(-dx, -dy));
      plotLine1.setPoints(plotPoint1a, plotPoint1b);
      _plotLines.add(plotLine1);
      IPlotPoint plotPoint2a = new PlotPoint(p2.getX(), p2.getY(), p2.getZ());
      IPlotPoint plotPoint2b = new PlotPoint(p2.getX() + dx, p2.getY() + dy, p2.getZ());
      plotPoint2b.setName(formatter.format(xline));
      PlotLine plotLine2 = new PlotLine("", textProps, pointProps, lineProps);
      plotLine2.setTextColor(labelColor);
      plotLine2.setTextFont(labelFont);
      plotLine2.setTextAnchor(TextAnchor.getBest(dx, dy));
      plotLine2.setPoints(plotPoint2a, plotPoint2b);
      _plotLines.add(plotLine2);
      addShape(plotLine1);
      addShape(plotLine2);
    }
    labelFont.dispose();
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    // Add this renderer to the viewer tree.
    addToLayerTree(IViewer.SEISMIC_FOLDER, autoUpdate);
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    _survey = (SeismicSurvey3d) objects[0];
  }

  @Override
  protected void setNameAndImage() {
    // Update the name and icon of the renderer based on the survey.
    setName(_survey.getDisplayName());
    setImage(ModelUI.getSharedImages().getImage(_survey));
  }

  @Override
  public DataSelection getDataSelection(final double x, final double y) {
    Point3d[] points = _survey.getCornerPoints().getPointsDirect();
    int[] startingIndices = { 0, 1, 2, 3 };
    int[] endingIndices = { 1, 2, 3, 0 };
    double minDistance = Double.MAX_VALUE;
    double[] closestPoint = { 0, 0 };
    boolean pointFound = false;
    for (int i = 0; i < 4; i++) {
      int index0 = startingIndices[i];
      int index1 = endingIndices[i];

      double[] projection = GeometryUtil.projectPointOntoLine(points[index0].getX(), points[index0].getY(),
          points[index1].getX(), points[index1].getY(), x, y);
      double dx = points[index1].getX() - points[index0].getX();
      double dy = points[index1].getY() - points[index0].getY();
      double t = 0;
      if (Math.abs(dx) >= Math.abs(dy)) {
        t = (projection[0] - points[index0].getX()) / dx;
      } else {
        t = (projection[1] - points[index0].getY()) / dy;
      }
      if (t >= 0 && t <= 1) {
        double distance = GeometryUtil.distancePointToLine(points[index0].getX(), points[index0].getY(),
            points[index1].getX(), points[index1].getY(), x, y);
        if (distance < minDistance) {
          minDistance = distance;
          closestPoint[0] = projection[0];
          closestPoint[1] = projection[1];
          pointFound = true;
        }
      }
    }
    if (pointFound) {
      Point2D.Double pixel1 = new Point2D.Double();
      Point2D.Double pixel2 = new Point2D.Double();
      IPlot plot = getViewer().getPlot();
      IModelSpaceCanvas canvas = plot.getModelSpaceCanvas();
      IModelSpace modelSpace = plot.getActiveModelSpace();
      canvas.transformModelToPixel(modelSpace, x, y, pixel1);
      canvas.transformModelToPixel(modelSpace, closestPoint[0], closestPoint[1], pixel2);
      if (Math.abs(pixel1.x - pixel2.x) <= 2 && Math.abs(pixel1.y - pixel2.y) <= 2) {
        DataSelection selection = new DataSelection(getClass().getSimpleName());
        selection.setSelectedObjects(new Object[] { _survey });
        return selection;
      }
    }
    return null;
  }

  public SeismicSurvey3dRendererModel getSettingsModel() {
    return _model;
  }

  /**
   * Updates the display settings (text properties, line properties, etc).
   * 
   * @param model the model of display properties.
   */
  public void updateSettings(final SeismicSurvey3dRendererModel model) {
    _model.updateFrom(model);
    setTextProperties(model.getTextProperties());
    setLineProperties(model.getLineProperties());
    setPointProperties(model.getPointProperties());
  }

  /**
   * Updates the plot text properties (font and color).
   * 
   * @param properties the text properties to set.
   */
  private void setTextProperties(final TextProperties properties) {
    if (_plotLines.size() > 0) {
      for (IPlotLine line : _plotLines) {
        line.setTextFont(properties.getFont());
        line.setTextColor(properties.getColor());
      }
    }
  }

  /**
   * Updates the plot point properties (style, color and size).
   * 
   * @param properties the point properties to set.
   */
  private void setPointProperties(final PointProperties properties) {
    _plotOutline.setPointStyle(properties.getStyle());
    _plotOutline.setPointColor(properties.getColor());
    _plotOutline.setPointSize(properties.getSize());
  }

  /**
   * Updates the plot line properties (style, color and width).
   * 
   * @param properties the line properties to set.
   */
  private void setLineProperties(final LineProperties properties) {
    _plotOutline.setLineStyle(properties.getStyle());
    _plotOutline.setLineColor(properties.getColor());
    _plotOutline.setLineWidth(properties.getWidth());
  }

  /**
   * Returns an array containing the row,column,inline and xline values for
   * the given x,y coordinate.
   * 
   * @param x the x coordinate.
   * @param y the y coordinate.
   * @return the array of [row,col,inline,xline].
   */
  private float[] getRowColumnInlineXline(final double x, final double y) {
    double[] rc = _survey.transformXYToRowCol(x, y, true);
    int row = (int) Math.round(rc[0]);
    int col = (int) Math.round(rc[1]);
    float[] ixln = _survey.transformRowColToInlineXline(row, col);
    float inline = ixln[0];
    float xline = ixln[1];
    return new float[] { row, col, inline, xline };
  }
}
