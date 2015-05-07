package org.geocraft.ui.sectionviewer.renderer.pointset;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.color.ColorBar;
import org.geocraft.core.model.PointSet;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.TraceAxisKey;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.core.model.seismic.SeismicSurvey2d;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.model.seismic.TraceSection;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.plot.defs.PlotEventType;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.event.ModelSpaceEvent;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPointGroup;
import org.geocraft.ui.plot.object.IPlotPolyline;
import org.geocraft.ui.plot.object.IPlotShape;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPointGroup;
import org.geocraft.ui.plot.object.PlotPolyline;
import org.geocraft.ui.sectionviewer.ISectionViewer;
import org.geocraft.ui.sectionviewer.SectionViewRenderer;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;


public class PointSetRenderer extends SectionViewRenderer {

  /** The point set to render. */
  private PointSet _pointSet;

  /** The current trace section on which to render. */
  private TraceSection _currentSection;

  /** The model of display settings. */
  private final PointSetRendererModel _model;

  /** The plot shape used to render the point set. */
  private final IPlotPointGroup _plotPoints;

  /** The plot shapes used to render the point connections. */
  private final Map<String, IPlotPolyline> _plotPolylines;

  /** An index of all the points keyed by inline, crossline, offset. */
  private Map<String, Object> _pointsetIndex;

  /** The list of currently displayed point set indices. */
  private final List<Integer> _pointIndices;

  /** The map of currently displayed point set indices mapped by trace number. */
  private final Map<Integer, List<Integer>> _map;

  /** Flag indicating if point set has been indexed (0=not indexed, 2=indexed for 2D, 3=indexed for 3D. */
  private int _isIndexed = 0;

  /** The map of point set indices, mapped by the 2D seismic geometry. */
  private int[][] _indices2d = null;

  /** The map of point set indices, mapped by the 3D seismic geometry. */
  private Map<String, List<Integer>> _indices3d = null;

  private boolean _forceRedraw = false;

  public PointSetRenderer() {
    super("PointSet Renderer");
    _pointIndices = new ArrayList<Integer>();
    _map = new HashMap<Integer, List<Integer>>();
    _model = new PointSetRendererModel();
    _plotPoints = createPointGroup(_model);
    _plotPolylines = new HashMap<String, IPlotPolyline>();
    _appendCursorInfo = true;
  }

  /**
   * Loop through all of the points and return an array of the indexes of all of
   * the points that are closer to the defined section points than the tolerance. 
   */
  public int[][] findPointIntersections(final int numTraces, final Point3d[] sectionPoints,
      final float[] sectionOffsets, final String offsetName, final double tolerance) {

    //    if (_pointsetIndex == null) {
    //      _pointsetIndex = createIndex(offsetName);
    //    }

    ISectionViewer viewer = getViewer();

    // Check if the point set has been indexed.
    // If not, index according to the type of geometry in the section viewer.
    if (_isIndexed == 0) {
      SeismicSurvey2d survey2d = viewer.getSeismicSurvey2d();
      int lineNumber = viewer.getSeismicLineNumber2d();
      SeismicSurvey3d geometry3d = viewer.getReferenceSurvey3d();
      FloatRange offsetRange = viewer.getOffsetRange();
      if (survey2d != null) {
        _indices2d = new int[survey2d.getNumBins(lineNumber)][0];
        _isIndexed = 2;
      } else if (geometry3d != null) {
        indexByGeometry3d(geometry3d, offsetRange);
        _isIndexed = 3;
      } else {
        // Cannot display because no section displayed.
        return new int[0][0];
      }
    }

    int[][] results = new int[numTraces][0];

    switch (_isIndexed) {
      case 2:
        break;
      case 3:
        SeismicSurvey3d geometry3d = viewer.getReferenceSurvey3d();
        FloatRange offsetRange = viewer.getOffsetRange();
        for (int i = 0; i < numTraces; i++) {
          Point3d sectionPoint = sectionPoints[i];
          double[] rowcol = geometry3d.transformXYToRowCol(sectionPoint.getX(), sectionPoint.getY(), true);
          int row = (int) Math.round(rowcol[0]);
          int col = (int) Math.round(rowcol[1]);
          int bin = Math.round((sectionOffsets[i] - offsetRange.getStart()) / offsetRange.getDelta());
          String key = row + "," + col + "," + bin;
          List<Integer> list = _indices3d.get(key);
          if (list != null) {
            results[i] = new int[list.size()];
            for (int j = 0; j < list.size(); j++) {
              results[i][j] = list.get(j);
            }
          } else {
            results[i] = new int[0];
          }
        }
        break;
    }

    return results;
  }

  /**
   * Indexes the point set by the grid of the given seismic geometry.
   * This is done for quick lookup.
   * 
   * @param geometry the seismic geometry on which to index.
   */
  private void indexByGeometry3d(final SeismicSurvey3d geometry, final FloatRange offsetRange) {
    _indices3d = new HashMap<String, List<Integer>>();
    for (int i = 0; i < _pointSet.getNumPoints(); i++) {
      double x = _pointSet.getPoint(i).getX();
      double y = _pointSet.getPoint(i).getY();
      double[] rowcol = geometry.transformXYToRowCol(x, y, true);
      int row = (int) Math.round(rowcol[0]);
      int col = (int) Math.round(rowcol[1]);
      if (row >= 0 && row < geometry.getNumRows() && col >= 0 && col < geometry.getNumColumns()) {
        float offset = 0;
        if (_pointSet.containsAttribute("offset")) {
          offset = _pointSet.getAttribute("offset").getFloat(i);
        }
        int bin = Math.round((offset - offsetRange.getStart()) / offsetRange.getDelta());
        String key = row + "," + col + "," + bin;
        List<Integer> list = _indices3d.get(key);
        if (list == null) {
          list = new ArrayList<Integer>();
          _indices3d.put(key, list);
        }
        list.add(i);
      }
    }
  }

  @Override
  public ReadoutInfo getReadoutInfo(final int traceNum, final float z) {

    String title = "PointSet: " + _pointSet.getDisplayName();
    List<String> keys = new ArrayList<String>();
    List<String> values = new ArrayList<String>();

    List<Integer> list = _map.get(traceNum);
    if (list != null) {

      // find the closest point to the cursor location
      int closestPointIndex = -1;
      double minDz = Double.MAX_VALUE;
      for (Integer i : list) {
        double dz = Math.abs(z - _pointSet.getPoint(i.intValue()).getZ());
        if (dz < minDz) {
          minDz = dz;
          closestPointIndex = i;
        }
      }

      if (closestPointIndex >= 0) {
        keys.add("Point Z");
        values.add("" + _pointSet.getPoint(closestPointIndex).getZ());
        for (String attr : _pointSet.getAttributeNames()) {
          keys.add(attr);
          values.add("" + _pointSet.getAttribute(attr).getString(closestPointIndex));
        }
      }
    }

    return new ReadoutInfo(title, keys.toArray(new String[0]), values.toArray(new String[0]));
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _pointSet };
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

    TraceSection section = getViewer().getTraceSection();
    if (section == null) {
      removeAllShapes();
      _map.clear();
      _plotPoints.clear();
      for (IPlotPolyline plotPolyline : _plotPolylines.values()) {
        plotPolyline.clear();
      }
      _plotPolylines.clear();
      _pointIndices.clear();
      _currentSection = section;
      super.updated();
      return;
    }

    boolean colorByAttribute = _model.getColorByAttribute();
    String colorAttribute = _model.getColorAttribute();
    boolean thresholdByAttribute = _model.getThresholdByAttribute();
    String thresholdAttribute = _model.getThresholdAttribute();
    float thresholdMinValue = _model.getThresholdMinValue();
    float thresholdMaxValue = _model.getThresholdMaxValue();
    if (!section.equals(_currentSection) || _forceRedraw) {
      removeAllShapes();
      _map.clear();
      _plotPoints.clear();
      for (IPlotPolyline plotPolyline : _plotPolylines.values()) {
        plotPolyline.clear();
      }
      _plotPolylines.clear();
      _pointIndices.clear();
      Point3d[] sectionPoints = section.getPointsXY();
      float[] sectionOffsets = new float[section.getNumTraces()];
      float[][] traceKeyValues = section.getTraceKeyValues3d();
      for (int j = 0; j < section.getNumTraces(); j++) {
        sectionOffsets[j] = 0;
        if (section.containsTraceAxisKey(TraceAxisKey.OFFSET)) {
          sectionOffsets[j] = traceKeyValues[j][section.getTraceAxisKeyIndex(TraceAxisKey.OFFSET)];
        }
      }

      int numTraces = section.getNumTraces();
      int[][] indices = findPointIntersections(numTraces, sectionPoints, sectionOffsets, "offset", 1);

      for (int i = 0; i < numTraces; i++) {
        int trace = i + 1;
        List<Integer> list = new ArrayList<Integer>();
        for (int j = 0; j < indices[i].length; j++) {
          int index = indices[i][j];
          IPlotPoint plotPoint = new PlotPoint(trace, _pointSet.getZ(index), 0);
          plotPoint.setPointStyle(_model.getPointStyle());
          plotPoint.setPointSize(_model.getPointSize());
          plotPoint.setPropertyInheritance(!colorByAttribute);
          if (colorByAttribute) {
            float value = _pointSet.getAttribute(colorAttribute).getFloat(index);
            RGB rgb = _model.getColorBar().getColor(value, false);
            plotPoint.setPointColor(rgb);
          }
          _plotPoints.addPoint(plotPoint);
          if (thresholdByAttribute) {
            float value = _pointSet.getAttribute(thresholdAttribute).getFloat(index);
            plotPoint.setVisible(value >= thresholdMinValue && value <= thresholdMaxValue);
          }
          _pointIndices.add(index);
          list.add(index);

          // TODO: this is a hack right now, since 2D volumes would not have an xline trace key.
          if (_model.getConnectionByAttribute()) {
            IPlotPoint linePoint = new PlotPoint(trace, _pointSet.getZ(index), 0);
            linePoint.setPointStyle(PointStyle.NONE);
            linePoint.setPointSize(0);
            linePoint.setPropertyInheritance(true);
            float connectionAttribute = _pointSet.getAttribute(_model.getConnectionAttribute()).getFloat(index);
            String gatherKey = section.getTraceAxisKeyValue(i, TraceAxisKey.INLINE) + ","
                + section.getTraceAxisKeyValue(i, TraceAxisKey.XLINE) + "," + connectionAttribute;
            IPlotPolyline plotPolyline = null;
            if (!_plotPolylines.containsKey(gatherKey)) {
              plotPolyline = new PlotPolyline();
              plotPolyline.setLineStyle(_model.getLineStyle());
              plotPolyline.setLineWidth(_model.getLineWidth());
              plotPolyline.setLineColor(_model.getLineColor());
              _plotPolylines.put(gatherKey, plotPolyline);
            }
            plotPolyline = _plotPolylines.get(gatherKey);
            plotPolyline.addPoint(linePoint);
          }
        }
        _map.put(trace, list);
      }
    }

    _plotPoints.setPointColor(_model.getPointColor());

    addShape(_plotPoints);
    addShapes(_plotPolylines.values().toArray(new IPlotShape[0]));

    _currentSection = section;

    _forceRedraw = false;

    super.updated();

  }

  public PointSetRendererModel getSettingsModel() {
    return _model;
  }

  /**
   * Updates the display settings (size, style, color) of the points.
   * This will trigger the renderer to redraw.
   */
  public void updateSettings(final PointSetRendererModel model, final ColorBar colorBar) {
    boolean connectionChanged = model.getConnectionByAttribute() != _model.getConnectionByAttribute();
    _model.updateFrom(model);
    if (connectionChanged) {
      _forceRedraw = true;
      redraw();
      return;
    }

    _plotPoints.blockUpdate();
    _plotPoints.setPointStyle(_model.getPointStyle());
    _plotPoints.setPointColor(_model.getPointColor());
    _plotPoints.setPointSize(_model.getPointSize());
    boolean colorByAttribute = _model.getColorByAttribute();
    String colorAttribute = _model.getColorAttribute();
    boolean thresholdByAttribute = _model.getThresholdByAttribute();
    String thresholdAttribute = _model.getThresholdAttribute();
    float thresholdMinValue = _model.getThresholdMinValue();
    float thresholdMaxValue = _model.getThresholdMaxValue();
    for (int i = 0; i < _plotPoints.getPointCount(); i++) {
      IPlotPoint point = _plotPoints.getPoint(i);
      point.setPointStyle(_model.getPointStyle());
      point.setPointSize(_model.getPointSize());
      point.setPropertyInheritance(!colorByAttribute);
      int index = _pointIndices.get(i);
      if (colorByAttribute) {
        float value = _pointSet.getAttribute(colorAttribute).getFloat(index);
        RGB rgb = colorBar.getColor(value, false);
        point.setPointColor(rgb);
      }
      if (thresholdByAttribute) {
        float value = _pointSet.getAttribute(thresholdAttribute).getFloat(index);
        point.setVisible(value >= thresholdMinValue && value <= thresholdMaxValue);
      }
    }
    _plotPoints.unblockUpdate();

    for (IPlotPolyline plotPolyline : _plotPolylines.values()) {
      plotPolyline.blockUpdate();
      plotPolyline.setLineStyle(_model.getLineStyle());
      plotPolyline.setLineWidth(_model.getLineWidth());
      plotPolyline.setLineColor(_model.getLineColor());
      plotPolyline.unblockUpdate();
    }
    getViewer().getPlot().updateAll();
  }

  @Override
  protected void addPlotShapes() {
    // No initial action.
  }

  @Override
  protected void addPopupMenuActions() {
    Dialog dialog = new PointSetRendererDialog(getShell(), _pointSet.getDisplayName(), this);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, 600);
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(IViewer.POINTSET_FOLDER, autoUpdate);
  }

  @Override
  protected void setNameAndImage() {
    setName(_pointSet);
    setImage(ModelUI.getSharedImages().getImage(_pointSet));
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    _pointSet = (PointSet) objects[0];
  }

  private IPlotPointGroup createPointGroup(final PointSetRendererModel model) {
    IPlotPointGroup group = new PlotPointGroup();
    group.setPointStyle(model.getPointStyle());
    group.setPointSize(model.getPointSize());
    group.setPointColor(model.getPointColor());
    return group;
  }

  /**
   * Group points together with the same x,y and offset but different 
   * depths. They are stored in a hashmap which will use less memory 
   * if the data are sparse. Not sure if I can access the seismic
   * geometry to create a 2d array index here? 
   * 
   * key = x y offset
   * value = list of point indices
   * 
   * @param offsetName
   * @return a collection of the points
   */
  private Map<String, Object> createIndex(final String offsetName) {

    long startTime = System.currentTimeMillis();
    // first pass is to build up a map using a List

    Map<String, List<Integer>> tempMap = new HashMap<String, List<Integer>>();

    for (int i = 0; i < _pointSet.getNumPoints(); i++) {
      float offset = 0;
      try {
        offset = _pointSet.getAttribute(offsetName).getFloat(i);
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      String traceKey = computeStringKey(_pointSet.getX(i), _pointSet.getY(i), offset);

      if (!tempMap.containsKey(traceKey)) {
        //System.out.println("creating new entry " + traceKey);
        tempMap.put(traceKey, new ArrayList<Integer>());
      }

      List<Integer> pointList = tempMap.get(traceKey);
      pointList.add(i);
    }

    // now convert it to a map containing arrays of int[]
    // need a new programming language that handles generics properly :-( 

    _pointsetIndex = new HashMap<String, Object>();

    for (Map.Entry<String, List<Integer>> entry : tempMap.entrySet()) {
      List<Integer> points = entry.getValue();
      int[] result = new int[points.size()];
      for (int i = 0; i < result.length; i++) {
        result[i] = points.get(i);
      }
      _pointsetIndex.put(entry.getKey(), result);
    }

    // TODO this could be deleted when everything appears to be working. 
    int sum = 0;
    for (Map.Entry<String, Object> entry : _pointsetIndex.entrySet()) {
      int[] ind = (int[]) entry.getValue();
      sum += ind.length;
    }

    //System.out.println("Total number of points indexed " + sum);
    long time = System.currentTimeMillis() - startTime;
    System.out.println("Indexed " + sum + "points of " + _pointSet.getNumPoints() + " in " + time + " ms");

    return _pointsetIndex;
  }

  /**
   * @param pt
   * @param offset
   * @return
   */
  private String computeStringKey(final double px, final double py, final float offset) {
    StringBuilder builder = new StringBuilder();

    // TODO currently round the numbers to the nearest 5 units of distance because we are seeing 
    // inconsistent header values in javaseis test data sets. 
    long x = (long) (px / 5);
    x = x * 5;
    builder.append(x);
    builder.append(" ");
    long y = (long) (py / 5);
    y = y * 5;
    builder.append(y);
    builder.append(" ");
    builder.append(offset);
    return builder.toString();
  }

  @Override
  public DataSelection getDataSelection(final double x, final double y) {
    // TODO Auto-generated method stub
    return null;
  }

}
