package org.geocraft.ui.mapviewer.renderer.seismic;


import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.core.model.seismic.SeismicLine2d;
import org.geocraft.core.model.seismic.SeismicSurvey2d;
import org.geocraft.ui.mapviewer.MapViewRenderer;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.plot.attribute.LineProperties;
import org.geocraft.ui.plot.attribute.PointProperties;
import org.geocraft.ui.plot.attribute.TextProperties;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.defs.TextAnchor;
import org.geocraft.ui.plot.object.IPlotLine;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPolyline;
import org.geocraft.ui.plot.object.PlotLine;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPolyline;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;


public class SeismicSurvey2dRenderer extends MapViewRenderer {

  private SeismicSurvey2d _survey;

  private Map<SeismicLine2d, IPlotPolyline> _lines;

  private Map<SeismicLine2d, IPlotLine> _plotLines1;

  private Map<SeismicLine2d, IPlotLine> _plotLines2;

  public SeismicSurvey2dRenderer() {
    super("Seismic Survey 2d Renderer");
    showReadoutInfo(true);
  }

  @Override
  public void redraw() {
    for (IPlotPolyline plotLine : _lines.values()) {
      addShape(plotLine);
    }
  }

  @Override
  public ReadoutInfo getReadoutInfo(final double x, final double y) {
    return new ReadoutInfo("" + getClass());
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _survey };
  }

  public PointProperties getPointProperties() {
    if (_lines.size() > 0) {
      return _lines.values().toArray(new IPlotPolyline[0])[0].getPoint(0).getPointProperties();
    }
    return null;
  }

  public void setPointProperties(final PointProperties properties) {
    block();
    for (IPlotPolyline line : _lines.values()) {
      line.setPointStyle(properties.getStyle());
      line.setPointColor(properties.getColor());
      line.setPointSize(properties.getSize());
    }
    unblock();
    updated();
  }

  public TextProperties getTextProperties() {
    if (_plotLines1.size() > 0) {
      return _plotLines1.values().toArray(new IPlotLine[0])[0].getTextProperties();
    }
    return null;
  }

  public void setTextProperties(final TextProperties properties) {
    block();
    if (_plotLines1.size() > 0) {
      for (IPlotLine line : _plotLines1.values()) {
        line.setTextFont(properties.getFont());
        line.setTextColor(properties.getColor());
      }
      for (IPlotLine line : _plotLines2.values()) {
        line.setTextFont(properties.getFont());
        line.setTextColor(properties.getColor());
      }
    }
    unblock();
    updated();
  }

  public LineProperties getLineProperties() {
    if (_lines.size() > 0) {
      return _lines.values().toArray(new IPlotPolyline[0])[0].getLineProperties();
    }
    return null;
  }

  public void setLineProperties(final LineProperties properties) {
    block();
    for (IPlotPolyline line : _lines.values()) {
      line.setLineStyle(properties.getStyle());
      line.setLineColor(properties.getColor());
      line.setLineWidth(properties.getWidth());
    }
    unblock();
    updated();
  }

  @Override
  protected void addPopupMenuActions() {
    Dialog dialog = new SeismicSurvey2dRendererDialog(getShell(), _survey.getDisplayName(), this);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);

    // Add action for selecting a line trace section in the map viewer.
    Action constructLineSection = new ConstructLine2dTraceSection(getViewer(), _survey);
    addPopupMenuAction(constructLineSection);
  }

  @Override
  protected void addPlotShapes() {
    SeismicLine2d[] lineGeometries = _survey.getLines();

    _lines = Collections.synchronizedMap(new HashMap<SeismicLine2d, IPlotPolyline>());
    _plotLines1 = Collections.synchronizedMap(new HashMap<SeismicLine2d, IPlotLine>());
    _plotLines2 = Collections.synchronizedMap(new HashMap<SeismicLine2d, IPlotLine>());
    for (SeismicLine2d lineGeometry : lineGeometries) {
      RGB color = new RGB(255, 255, 255);
      IPlotPolyline plotLine = new PlotPolyline();
      plotLine.setTextColor(color);
      plotLine.setPointColor(color);
      plotLine.setPointSize(1);
      plotLine.setPointStyle(PointStyle.NONE);
      plotLine.setLineColor(color);
      plotLine.setLineWidth(1);
      plotLine.setLineStyle(LineStyle.SOLID);
      CoordinateSeries coords = lineGeometry.getPoints();

      // For large 2D surveys, decimate the number of points used to
      // represent the lines. Unless the line geometry varies greatly
      // from point to point, this should give a good approximation.
      int numPoints = coords.getNumPoints();
      int skipFactor = 1;
      if (numPoints >= 10) {
        skipFactor = 1 + numPoints / 10;
      }
      int j;
      for (j = 0; j < coords.getNumPoints(); j += skipFactor) {
        IPlotPoint plotPoint = new PlotPoint(coords.getX(j), coords.getY(j), 0);
        plotPoint.setPropertyInheritance(true);
        plotLine.addPoint(plotPoint);
      }

      // If the end point of the line geometry was skipped, go back
      // and add it to make sure the end of the line does not appear
      // clipped off.
      if (j >= coords.getNumPoints()) {
        j = coords.getNumPoints() - 1;
        IPlotPoint plotPoint = new PlotPoint(coords.getX(j), coords.getY(j), 0);
        plotPoint.setPropertyInheritance(true);
        plotLine.addPoint(plotPoint);
      }

      _lines.put(lineGeometry, plotLine);
      addShape(plotLine);

      NumberFormat formatter = NumberFormat.getIntegerInstance();
      RGB labelColor = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW).getRGB();
      Font labelFont = new Font(null, "Courier", 10, SWT.NORMAL);
      TextProperties textProps = plotLine.getTextProperties();
      textProps.setFont(labelFont);
      PointProperties pointProps = plotLine.getPointProperties();
      LineProperties lineProps = plotLine.getLineProperties();

      float cdpStart = lineGeometry.getCDPStart();
      float cdpEnd = lineGeometry.getCDPEnd();
      //      float cdpDelta = lineGeometry.getCDPDelta();
      //      for (float value = cdpStart; value < cdpEnd; value += cdpDelta) {
      Point3d[] points = lineGeometry.transformCDPsToXYs(new float[] { cdpStart, cdpEnd }).getPointsDirect();
      Point3d p1 = points[0];
      Point3d p2 = points[1];
      double dx = p2.getX() - p1.getX();
      double dy = p2.getY() - p1.getY();
      dx *= 0.025;
      dy *= 0.025;
      IPlotPoint plotPoint1a = new PlotPoint(p1.getX(), p1.getY(), p1.getZ());
      IPlotPoint plotPoint1b = new PlotPoint(p1.getX() - dx, p1.getY() - dy, p1.getZ());
      plotPoint1b.setName(lineGeometry.getDisplayName());
      PlotLine plotLine1 = new PlotLine("", textProps, pointProps, lineProps);
      plotLine1.setTextColor(labelColor);
      plotLine1.setTextFont(labelFont);
      plotLine1.setTextAnchor(TextAnchor.getBest(-dx, -dy));
      plotLine1.setPoints(plotPoint1a, plotPoint1b);
      _plotLines1.put(lineGeometry, plotLine1);
      IPlotPoint plotPoint2a = new PlotPoint(p2.getX(), p2.getY(), p2.getZ());
      IPlotPoint plotPoint2b = new PlotPoint(p2.getX() + dx, p2.getY() + dy, p2.getZ());

      plotPoint2b.setName(lineGeometry.getDisplayName());
      PlotLine plotLine2 = new PlotLine("", textProps, pointProps, lineProps);
      plotLine2.setTextColor(labelColor);
      plotLine2.setTextFont(labelFont);
      plotLine2.setTextAnchor(TextAnchor.getBest(dx, dy));
      plotLine2.setPoints(plotPoint2a, plotPoint2b);
      _plotLines2.put(lineGeometry, plotLine2);
      addShape(plotLine1);
      addShape(plotLine2);
      //      }

      labelFont.dispose();
    }
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(IViewer.SEISMIC_FOLDER, autoUpdate);
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    _survey = (SeismicSurvey2d) objects[0];
  }

  @Override
  protected void setNameAndImage() {
    setName(_survey.getDisplayName());
    setImage(ModelUI.getSharedImages().getImage(_survey));
  }

  @Override
  public DataSelection getDataSelection(final double x, final double y) {
    return null;
  }

  public SeismicSurvey2d getSurvey() {
    return _survey;
  }

  public void showSeismicLines(final Map<String, Boolean> lines) {
    block();
    for (String lineName : lines.keySet()) {
      SeismicLine2d seismicLine = _survey.getLineByName(lineName);
      boolean visible = lines.get(lineName);
      _lines.get(seismicLine).setVisible(visible);
      _plotLines1.get(seismicLine).setVisible(visible);
      _plotLines2.get(seismicLine).setVisible(visible);
    }
    unblock();
    updated();
  }

  public boolean isShown(final SeismicLine2d geometry) {
    return _lines.get(geometry).isVisible();
  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.viewer.IRenderer#getSettingsModel()
   */
  @Override
  public SeismicSurvey2dRendererModel getSettingsModel() {
    // TODO Auto-generated method stub
    return null;
  }

}
