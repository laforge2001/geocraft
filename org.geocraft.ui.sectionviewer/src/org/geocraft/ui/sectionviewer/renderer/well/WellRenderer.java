package org.geocraft.ui.sectionviewer.renderer.well;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.core.model.seismic.TraceSection;
import org.geocraft.core.model.seismic.TraceSection.SectionType;
import org.geocraft.core.model.well.Well;
import org.geocraft.core.model.well.WellBore;
import org.geocraft.core.model.well.WellDomain;
import org.geocraft.core.model.well.WellPick;
import org.geocraft.internal.ui.sectionviewer.SectionViewRendererUtil;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PlotEventType;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.defs.TextAnchor;
import org.geocraft.ui.plot.event.ModelSpaceEvent;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.object.IPlotLine;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPointGroup;
import org.geocraft.ui.plot.object.IPlotPolyline;
import org.geocraft.ui.plot.object.PlotLine;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPointGroup;
import org.geocraft.ui.plot.object.PlotPolyline;
import org.geocraft.ui.sectionviewer.SectionViewRenderer;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;


/**
 * Renders a <code>Well</code> in the section viewer.
 */
public class WellRenderer extends SectionViewRenderer implements ControlListener {

  /** The well to render. */
  private Well _well;

  /** The current trace section on which to project the well path. */
  private TraceSection _currentSection;

  /** The model space canvas in which to render the image. */
  private IModelSpaceCanvas _canvas;

  /** Flag indicating if the well needing re-rendering. */
  private boolean _needsRedraw = true;

  /** The list of plot polylines representing the well path. */
  private final List<IPlotPolyline> _wellBoreShapes;

  /** The list of plot points representing the TD of the well. */
  private final List<IPlotPointGroup> _wellTDShapes;

  /** The list of plot lines representing the well picks. */
  private final List<IPlotLine> _wellPickShapes;

  /** The model of renderer settings. */
  private final WellRendererModel _model;

  public WellRenderer() {
    super("Well Bore Renderer");
    _model = new WellRendererModel();
    _wellBoreShapes = Collections.synchronizedList(new ArrayList<IPlotPolyline>());
    _wellTDShapes = Collections.synchronizedList(new ArrayList<IPlotPointGroup>());
    _wellPickShapes = Collections.synchronizedList(new ArrayList<IPlotLine>());
    showReadoutInfo(true);

    IPreferenceStore preferences = WellRendererPreferencePage.PREFERENCE_STORE;
    IPropertyChangeListener preferencesListener = new IPropertyChangeListener() {

      @Override
      public void propertyChange(final org.eclipse.jface.util.PropertyChangeEvent event) {
        _needsRedraw = true;
        redraw();
      }
    };
    preferences.addPropertyChangeListener(preferencesListener);
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _well };
  }

  @Override
  public void modelSpaceUpdated(final ModelSpaceEvent event) {
    PlotEventType eventType = event.getEventType();
    if (eventType.equals(PlotEventType.AXIS_UPDATED) || eventType.equals(PlotEventType.VIEWABLE_BOUNDS_UPDATED)) {
      redraw();
    }
  }

  @Override
  public void redraw() {
    redrawInternal();
  }

  /**
   * The internal method that performs the actual drawing of the well, etc.
   */
  private void redrawInternal() {

    // Get the current trace section from the viewer.
    TraceSection section = getViewer().getTraceSection();

    // If the section is null, then remove all the shapes
    // currently in the viewer.
    if (section == null) {
      _currentSection = section;
      removeAllShapes(false);
      _wellBoreShapes.clear();
      _wellTDShapes.clear();
      _wellPickShapes.clear();
      super.updated();
      return;
    }

    WellBore wellBore = _well.getWellBore();
    LineSegment segment = new LineSegment();
    WellPathProjection[] segmentFront = { new WellPathProjection() };
    WellPathProjection[] segmentBehind = { new WellPathProjection() };
    ProjectionPlane plane = new ProjectionPlane();
    boolean showWellsFront = true;
    boolean showWellsBehind = true;

    float displayTolerance = _model.getDisplayTolerance();
    boolean showBoreLabels = _model.getShowBoreLabels();
    int boreWidth = _model.getBoreWidth();
    RGB boreColor = _model.getBoreColor();
    boolean showPicks = _model.getShowPicks();
    boolean showPickLabels = _model.getShowPickLabels();
    RGB pickColor = _model.getPickColor();
    RGB pickLabelColor = _model.getPickLabelColor();
    boolean showAll = false;

    int numTraces = section.getNumTraces();
    if (!section.equals(_currentSection) || _needsRedraw) {
      removeAllShapes(false);
      _wellBoreShapes.clear();
      _wellTDShapes.clear();
      _wellPickShapes.clear();
      Domain domain = section.getDomain();
      CoordinateSeries wellPath = null;
      if (domain.equals(Domain.DISTANCE)) {
        wellPath = wellBore.getPathTVDSS();
      } else if (domain.equals(Domain.TIME)) {
        wellPath = wellBore.getPathTWT();
      } else {
        throw new IllegalArgumentException("Invalid domain: " + domain);
      }
      int numPanels = section.getNumPanels();
      int[] panelIndices = section.getPanelIndices();
      SectionType sectionType = section.getSectionType();
      boolean canRenderWell = false;
      if (sectionType.equals(SectionType.INLINE_SECTION) || sectionType.equals(SectionType.XLINE_SECTION)) {
        canRenderWell = true;
      } else if (sectionType.equals(SectionType.IRREGULAR)) {
        canRenderWell = true;
      }
      int[][] traceIndices = new int[numPanels][2];
      if (panelIndices.length == 0) {
        traceIndices[0][0] = 0;
        traceIndices[0][1] = section.getNumTraces() - 1;
      } else {
        traceIndices = new int[numPanels][2];
        traceIndices[0][0] = 0;
        int prevPanelIndex = 0;
        for (int traceIndex = 0; traceIndex < section.getNumTraces(); traceIndex++) {
          int currentPanelIndex = panelIndices[traceIndex];
          if (currentPanelIndex == prevPanelIndex) {
            traceIndices[currentPanelIndex][1] = traceIndex;
          } else {
            traceIndices[currentPanelIndex][0] = traceIndex;
          }
          prevPanelIndex = currentPanelIndex;
        }
        traceIndices[numPanels - 1][1] = section.getNumTraces() - 1;
      }
      float dsize = 1;
      if (canRenderWell) {
        Point3d[] xyPoints = section.getPointsXY();
        for (int i = 0; i < numPanels; i++) {

          int index0 = traceIndices[i][0];
          int index1 = traceIndices[i][1];
          dsize = index1 - index0;

          plane.x0 = xyPoints[index0].getX();
          plane.y0 = xyPoints[index0].getY();
          plane.z0 = section.getStartZ();
          plane.x1 = xyPoints[index1].getX();
          plane.y1 = xyPoints[index1].getY();
          plane.z1 = section.getEndZ();
          plane.dx = plane.x1 - plane.x0;
          plane.dy = plane.y1 - plane.y0;

          boolean wellInRange = false;

          // Check each portion of the well path, projecting it onto the section.
          for (int k = 1; k < wellPath.getNumPoints(); k++) {
            segment.x0 = wellPath.getX(k - 1);
            segment.y0 = wellPath.getY(k - 1);
            segment.z0 = wellPath.getZ(k - 1);
            segment.x1 = wellPath.getX(k);
            segment.y1 = wellPath.getY(k);
            segment.z1 = wellPath.getZ(k);
            segment.dx = segment.x1 - segment.x0;
            segment.dy = segment.y1 - segment.y0;
            segment.dz = segment.z1 - segment.z0;

            // Check if the portion of the well path is in the z range of the section.
            if (segment.z0 >= plane.z0 && segment.z0 <= plane.z1 || segment.z1 >= plane.z0 && segment.z1 <= plane.z1) {
              double[] actualDistance = { 0 };
              wellInRange = SectionViewRendererUtil.checkProjection(segment, plane, displayTolerance, actualDistance);
              if (wellInRange) {
                break;
              }
            }
          }
          if (wellInRange) {
            for (int k = 1; k < wellPath.getNumPoints(); k++) {
              segment.x0 = wellPath.getX(k - 1);
              segment.y0 = wellPath.getY(k - 1);
              segment.z0 = wellPath.getZ(k - 1);
              segment.x1 = wellPath.getX(k);
              segment.y1 = wellPath.getY(k);
              segment.z1 = wellPath.getZ(k);
              segment.dx = segment.x1 - segment.x0;
              segment.dy = segment.y1 - segment.y0;
              segment.dz = segment.z1 - segment.z0;

              segmentFront[0] = new WellPathProjection();
              segmentBehind[0] = new WellPathProjection();
              int error = SectionViewRendererUtil.projectWellPathOntoSection(segment, plane, segmentFront,
                  segmentBehind, displayTolerance, showAll);
              if (error == 0) {
                if (showWellsFront && segmentFront[0].exists) {
                  double xStart = 1 + index0 + segmentFront[0].s0 * dsize;
                  double xEnd = 1 + index0 + segmentFront[0].s1 * dsize;
                  double yStart = segmentFront[0].projection.z0;
                  double yEnd = segmentFront[0].projection.z1;
                  IPlotPolyline shape = new PlotPolyline();
                  shape.setLineStyle(LineStyle.SOLID);
                  shape.setLineWidth(boreWidth);
                  shape.setLineColor(boreColor);
                  IPlotPoint point1 = new PlotPoint(xStart, yStart, 0);
                  IPlotPoint point2 = new PlotPoint(xEnd, yEnd, 0);
                  point1.setPropertyInheritance(true);
                  point2.setPropertyInheritance(true);
                  shape.addPoint(point1);
                  shape.addPoint(point2);
                  _wellBoreShapes.add(shape);
                }
                if (showWellsBehind && segmentBehind[0].exists) {
                  double xStart = 1 + index0 + segmentBehind[0].s0 * dsize;
                  double xEnd = 1 + index0 + segmentBehind[0].s1 * dsize;
                  double yStart = segmentBehind[0].projection.z0;
                  double yEnd = segmentBehind[0].projection.z1;
                  IPlotPolyline shape = new PlotPolyline();
                  shape.setLineStyle(LineStyle.DASHED);
                  shape.setLineWidth(boreWidth);
                  shape.setLineColor(boreColor);
                  IPlotPoint point1 = new PlotPoint(xStart, yStart, 0);
                  IPlotPoint point2 = new PlotPoint(xEnd, yEnd, 0);
                  point1.setPropertyInheritance(true);
                  point2.setPropertyInheritance(true);
                  shape.addPoint(point1);
                  shape.addPoint(point2);
                  _wellBoreShapes.add(shape);
                }
              }

              // Show well symbol.
              double s = Double.NaN;
              double z = Double.NaN;
              if (k == wellPath.getNumPoints() - 1) {
                if (segmentFront[0].exists && !segmentBehind[0].exists) {
                  s = segmentFront[0].s1;
                  z = segmentFront[0].projection.z1;
                } else if (!segmentFront[0].exists && segmentBehind[0].exists) {
                  s = segmentBehind[0].s1;
                  z = segmentBehind[0].projection.z1;
                } else if (segmentFront[0].exists && segmentBehind[0].exists) {
                  if (segmentFront[0].projection.z1 > segmentBehind[0].projection.z1) {
                    s = segmentFront[0].s1;
                    z = segmentFront[0].projection.z1;
                  } else {
                    s = segmentBehind[0].s1;
                    z = segmentBehind[0].projection.z1;
                  }
                }
                if (s >= 0 && s <= 1) {
                  IPlotPointGroup shape = new PlotPointGroup();
                  shape.setPointStyle(PointStyle.CIRCLE);
                  shape.setPointSize(5);
                  shape.setPointColor(boreColor);
                  double x = 1 + index0 + s * dsize;
                  IPlotPoint point = new PlotPoint(x, z, 0);
                  if (showBoreLabels) {
                    point.setName(wellBore.getDisplayName());
                    shape.setTextAnchor(TextAnchor.SOUTH);
                    shape.setTextColor(pickLabelColor);
                  }
                  point.setPropertyInheritance(true);
                  shape.addPoint(point);
                  _wellTDShapes.add(shape);
                }
              }
            }

            // Render the well picks, if requested.
            if (showPicks) {
              float[] measuredDepths = wellBore.getMeasuredDepths();
              Well well = wellBore.getWell();
              WellPick[] wellPicks = well.getWellPicks();
              for (WellPick pick : wellPicks) {
                double measuredDepth = pick.getValue(WellDomain.MEASURED_DEPTH);
                double pickX = Double.NaN;
                double pickY = Double.NaN;
                double pickZ = Double.NaN;
                if (domain.equals(Domain.DISTANCE)) {
                  pickZ = pick.getValue(WellDomain.TRUE_VERTICAL_DEPTH_SUBSEA);
                } else if (domain.equals(Domain.TIME)) {
                  pickZ = pick.getValue(WellDomain.TWO_WAY_TIME);
                } else {
                  throw new IllegalArgumentException("Invalid domain: " + domain);
                }

                boolean pickInRange = false;
                if (measuredDepth >= measuredDepths[0] && measuredDepth <= measuredDepths[measuredDepths.length - 1]) {
                  for (int m = 1; m < measuredDepths.length; m++) {
                    if (measuredDepth >= measuredDepths[m - 1] && measuredDepth <= measuredDepths[m]) {
                      double dx = wellPath.getX(m) - wellPath.getX(m - 1);
                      double dy = wellPath.getY(m) - wellPath.getY(m - 1);
                      double percent = (measuredDepth - measuredDepths[m - 1])
                          / (measuredDepths[m] - measuredDepths[m - 1]);
                      pickX = wellPath.getX(m - 1) + percent * dx;
                      pickY = wellPath.getY(m - 1) + percent * dy;
                      pickInRange = true;
                      break;
                    }
                  }
                }

                if (pickInRange && !Double.isNaN(pickX) && !Double.isNaN(pickX) && !Double.isNaN(pickZ)) {
                  segment.x0 = pickX;
                  segment.y0 = pickY;
                  segment.z0 = pickZ;
                  segment.x1 = pickX;
                  segment.y1 = pickY;
                  segment.z1 = pickZ;
                  segment.dx = segment.x1 - segment.x0;
                  segment.dy = segment.y1 - segment.y0;
                  segment.dz = segment.z1 - segment.z0;

                  // Project the pick location onto the plane.
                  int error = SectionViewRendererUtil.projectWellPathOntoSection(segment, plane, segmentFront,
                      segmentBehind, displayTolerance, showAll);
                  if (error == 0) {
                    double x0 = 0;
                    double x1 = 0;
                    if (segmentFront[0].exists) {
                      x0 = (float) (1 + index0 + segmentFront[0].s0 * dsize - 2.0);
                      x1 = (float) (1 + index0 + segmentFront[0].s0 * dsize + 2.0);
                    } else if (segmentBehind[0].exists) {
                      x0 = (float) (1 + index0 + segmentBehind[0].s0 * dsize - 2.0);
                      x1 = (float) (1 + index0 + segmentBehind[0].s0 * dsize + 2.0);
                    }
                    if (segmentFront[0].exists || segmentBehind[0].exists) {
                      IPlotLine shape = new PlotLine();
                      shape.setTextColor(pickLabelColor);
                      shape.setTextAnchor(TextAnchor.EAST);
                      shape.setPointStyle(PointStyle.NONE);
                      shape.setPointSize(0);
                      shape.setLineColor(pickColor);
                      shape.setLineStyle(LineStyle.SOLID);
                      shape.setLineWidth(2);
                      IPlotPoint point1 = new PlotPoint(x0, pickZ, 0);
                      IPlotPoint point2 = new PlotPoint(x1, pickZ, 0);
                      if (showPickLabels) {
                        point2.setName(pick.getDisplayName());
                      }
                      point1.setPropertyInheritance(true);
                      point2.setPropertyInheritance(true);
                      shape.setPoints(point1, point2);
                      _wellPickShapes.add(shape);
                    }

                  }
                }
              }
            }
          }
        }
      }
    }
    _currentSection = section;
    for (IPlotPolyline shape : _wellBoreShapes) {
      addShape(shape);
    }
    for (IPlotPointGroup shape : _wellTDShapes) {
      addShape(shape);
    }
    for (IPlotLine shape : _wellPickShapes) {
      addShape(shape);
    }

    _needsRedraw = false;

    super.updated();
  }

  @Override
  public void controlMoved(final ControlEvent event) {
    // No action.
  }

  @Override
  public void controlResized(final ControlEvent event) {
    redrawInternal();
  }

  @Override
  protected void addPlotShapes() {
    for (IPlotPolyline shape : _wellBoreShapes) {
      addShape(shape);
    }
    for (IPlotPointGroup shape : _wellTDShapes) {
      addShape(shape);
    }
    for (IPlotLine shape : _wellPickShapes) {
      addShape(shape);
    }
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(null, _well.getDisplayName(), IViewer.WELL_FOLDER, autoUpdate);
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    // Store a reference to the grid.
    _well = (Well) objects[0];
    //_model.updateFromPreferences(_wellBore);
    _canvas = getViewer().getModelSpaceCanvas();
  }

  @Override
  protected void setNameAndImage() {
    setName(_well);
    setImage(ModelUI.getSharedImages().getImage(_well));
  }

  @Override
  public DataSelection getDataSelection(final double x, final double y) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ReadoutInfo getReadoutInfo(final int trace, final float z) {
    return new ReadoutInfo("" + getClass());
  }

  @Override
  protected void addPopupMenuActions() {
    final Shell shell = new Shell(getShell());
    final WellRendererDialog dialog = new WellRendererDialog(shell, _well.getDisplayName(), this);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
  }

  public WellRendererModel getSettingsModel() {
    return _model;
  }

  public void updateRendererModel(final WellRendererModel model) {
    redraw();
  }

}
