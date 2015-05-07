package org.geocraft.ui.mapviewer.renderer.aoi;


import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.geocraft.core.model.aoi.MapPolygon;
import org.geocraft.core.model.aoi.MapPolygonAOI;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.ui.mapviewer.MapViewRenderer;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.plot.attribute.FillProperties;
import org.geocraft.ui.plot.attribute.LineProperties;
import org.geocraft.ui.plot.attribute.PointProperties;
import org.geocraft.ui.plot.attribute.TextProperties;
import org.geocraft.ui.plot.defs.PlotEventType;
import org.geocraft.ui.plot.event.ShapeEvent;
import org.geocraft.ui.plot.listener.IPlotShapeListener;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPolygon;
import org.geocraft.ui.plot.object.IPlotShape;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPolygon;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;


public class MapPolygonAOIRenderer extends MapViewRenderer {

  /** The area-of-interest. */
  private MapPolygonAOI _aoi;

  private final MapPolygonAOIRendererModel _model;

  /** The plot shape representations for polygons. */
  private final List<IPlotPolygon> _plotPolygons;

  private final Map<IPlotPolygon, MapPolygon> _polygonMap;

  private boolean _isEditing = false;

  public MapPolygonAOIRenderer() {
    super("MapPolygonAOIRenderer");
    _model = new MapPolygonAOIRendererModel();
    _plotPolygons = Collections.synchronizedList(new ArrayList<IPlotPolygon>());
    _polygonMap = Collections.synchronizedMap(new HashMap<IPlotPolygon, MapPolygon>());
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _aoi };
  }

  @Override
  public void redraw() {
    //    for (IPlotPolygon aoiPolygon : _plotPolygons) {
    //      addShape(aoiPolygon);
    //    }
    updated();
  }

  private IPlotPolygon addPolygon(final MapPolygonAOI aoi, final MapPolygon polygon,
      final LineProperties lineProperties, final FillProperties fillProperties) {

    IPlotPolygon aoiPolygon = new PlotPolygon(polygon.getType().toString(), new TextProperties(),
        _model.getPointProperties(), lineProperties, fillProperties);
    aoiPolygon.setTransparency(_model.getTransparency());
    //aoiPolygon.setName(aoi.getDisplayName());
    aoiPolygon.setSelectable(false);
    PathIterator iter = polygon.getPath().getPathIterator(null);
    while (!iter.isDone()) {
      double[] coords = new double[6];
      int type = iter.currentSegment(coords);
      if (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
        double x = coords[0];
        double y = coords[1];
        IPlotPoint point = new PlotPoint("", x, y, 0);
        point.setPropertyInheritance(true);
        aoiPolygon.addPoint(point);
      }
      iter.next();
    }
    return aoiPolygon;
  }

  @Override
  public ReadoutInfo getReadoutInfo(final double x, final double y) {
    return new ReadoutInfo(_aoi.toString(), new String[] { "Inside?" }, new String[] { "" + _aoi.contains(x, y) });
  }

  @Override
  protected void addPopupMenuActions() {
    Dialog dialog = new MapPolygonAOIRendererDialog(getShell(), _aoi.getDisplayName(), this);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
    addPopupMenuAction(new EditMapPolygonAOI(this));
  }

  @Override
  protected void addPlotShapes() {

    // Create the display polygons.
    MapPolygon[] mapPolygons = _aoi.getPolygons();
    for (MapPolygon mapPolygon : mapPolygons) {
      LineProperties lineProperties = _model.getInclusiveLineProperties();
      FillProperties fillProperties = _model.getInclusiveFillProperties();
      if (mapPolygon.isExclusive()) {
        lineProperties = _model.getExclusiveLineProperties();
        fillProperties = _model.getExclusiveFillProperties();
      }
      IPlotPolygon plotPolygon = addPolygon(_aoi, mapPolygon, lineProperties, fillProperties);
      _plotPolygons.add(plotPolygon);
      _polygonMap.put(plotPolygon, mapPolygon);
      plotPolygon.addShapeListener(new IPlotShapeListener() {

        @Override
        public void shapeUpdated(final ShapeEvent event) {
          IPlotPolygon pPolygon = (IPlotPolygon) event.getShape();
          PlotEventType type = event.getEventType();
          switch (type) {
            case SHAPE_END_MOTION:
            case SHAPE_MOTION:
            case SHAPE_START_MOTION:
            case SHAPE_UPDATED:
              MapPolygon mPolygon = _polygonMap.get(pPolygon);
              synchronized (mPolygon) {
                int numPoints = pPolygon.getPointCount();
                double[] xs = new double[numPoints];
                double[] ys = new double[numPoints];
                for (int i = 0; i < numPoints; i++) {
                  IPlotPoint point = pPolygon.getPoint(i);
                  xs[i] = point.getX();
                  ys[i] = point.getY();
                }
                mPolygon.setPath(xs, ys);
                if (_isEditing) {
                  _aoi.setDirty(true);
                }
              }
              break;
            case SHAPE_DESELECTED:
              endEdit();
              break;
            case SHAPE_ADDED:
              // TODO:
              break;
            case SHAPE_REMOVED:
              // TODO:
              break;
            default:
              // Nothing to do.
          }
        }

      });
    }

    // Set the cursor info append flag.
    _appendCursorInfo = true;

    for (IPlotPolygon aoiPolygon : _plotPolygons) {
      addShape(aoiPolygon);
    }
  }

  @Override
  public void removeAllShapes() {
    _plotPolygons.clear();
    _polygonMap.clear();
    super.removeAllShapes();
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(IViewer.AOI_ROI_FOLDER, autoUpdate);
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    // Set the area-of-interest.
    _aoi = (MapPolygonAOI) objects[0];
  }

  @Override
  protected void setNameAndImage() {
    setName(_aoi);
    setImage(ModelUI.getSharedImages().getImage(_aoi));
  }

  @Override
  public DataSelection getDataSelection(final double x, final double y) {
    return null;
  }

  public MapPolygonAOIRendererModel getSettingsModel() {
    return _model;
  }

  public void updateSettings(final MapPolygonAOIRendererModel model) {
    _model.updateFrom(model);
    for (IPlotPolygon polygon : _plotPolygons) {
      polygon.setTransparency(_model.getTransparency());
    }
    setInclusiveFillProperties(model.getInclusiveFillProperties());
    setInclusiveLineProperties(model.getInclusiveLineProperties());
    setExclusiveFillProperties(model.getExclusiveFillProperties());
    setExclusiveLineProperties(model.getExclusiveLineProperties());
    setPointProperties(model.getPointProperties());
    redraw();
  }

  protected void setPointProperties(final PointProperties properties) {
    for (IPlotPolygon polygon : _plotPolygons) {
      polygon.setPointStyle(properties.getStyle());
      polygon.setPointColor(properties.getColor());
      polygon.setPointSize(properties.getSize());
    }
  }

  protected void setInclusiveLineProperties(final LineProperties properties) {
    for (IPlotPolygon polygon : _plotPolygons) {
      if (polygon.getName().equalsIgnoreCase(MapPolygon.Type.INCLUSIVE.toString())) {
        polygon.setLineStyle(properties.getStyle());
        polygon.setLineColor(properties.getColor());
        polygon.setLineWidth(properties.getWidth());
      }
    }
  }

  protected void setInclusiveFillProperties(final FillProperties properties) {
    for (IPlotPolygon polygon : _plotPolygons) {
      System.out.println("name: " + polygon.getName());
      if (polygon.getName().equalsIgnoreCase(MapPolygon.Type.INCLUSIVE.toString())) {
        polygon.setFillStyle(properties.getStyle());
        polygon.setFillColor(properties.getRGB());
      }
    }
  }

  protected void setExclusiveLineProperties(final LineProperties properties) {
    for (IPlotPolygon polygon : _plotPolygons) {
      if (polygon.getName().equalsIgnoreCase(MapPolygon.Type.EXCLUSIVE.toString())) {
        polygon.setLineStyle(properties.getStyle());
        polygon.setLineColor(properties.getColor());
        polygon.setLineWidth(properties.getWidth());
      }
    }
  }

  protected void setExclusiveFillProperties(final FillProperties properties) {
    for (IPlotPolygon polygon : _plotPolygons) {
      if (polygon.getName().equalsIgnoreCase(MapPolygon.Type.EXCLUSIVE.toString())) {
        polygon.setFillStyle(properties.getStyle());
        polygon.setFillColor(properties.getRGB());
      }
    }
  }

  public void startEdit() {
    for (IPlotShape shape : getShapes()) {
      shape.setEditable(true);
      shape.setSelectable(true);
      shape.select();
    }
    _isEditing = true;
  }

  public void endEdit() {
    _isEditing = false;
    for (IPlotShape shape : getShapes()) {
      shape.setEditable(false);
      shape.setSelectable(false);
      shape.deselect();
    }
  }
}
