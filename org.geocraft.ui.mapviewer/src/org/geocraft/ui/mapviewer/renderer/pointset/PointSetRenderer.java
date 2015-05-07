package org.geocraft.ui.mapviewer.renderer.pointset;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.color.ColorBar;
import org.geocraft.core.model.PointSet;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.ui.mapviewer.MapViewRenderer;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.plot.attribute.TextProperties;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPointGroup;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPointGroup;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;


/**
 * Renders a point set in a map viewer.
 */
public class PointSetRenderer extends MapViewRenderer implements IPointSetRendererConstants {

  /** The point set to render. */
  private PointSet _pointSet;

  /** The model of display settings. */
  private final PointSetRendererModel _model;

  /** The plot shape representation of the pointset. */
  private IPlotPointGroup _plotPoints;

  public PointSetRenderer() {
    super("PointSet Renderer");
    _model = new PointSetRendererModel();
  }

  @Override
  public DataSelection getDataSelection(final double x, final double y) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ReadoutInfo getReadoutInfo(final double x, final double y) {
    return new ReadoutInfo("" + getClass());
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _pointSet };
  }

  @Override
  public void redraw() {
    _plotPoints.blockUpdate();
    _plotPoints.updated();
  }

  public PointSetRendererModel getSettingsModel() {
    return _model;
  }

  /**
   * Updates the display settings (size, style, color) of the points.
   * This will trigger the renderer to redraw.
   */
  public void updateSettings(final PointSetRendererModel model, final ColorBar colorBar) {
    _model.updateFrom(model);

    _plotPoints.blockUpdate();
    _plotPoints.setPointStyle(_model.getPointStyle());
    _plotPoints.setPointColor(_model.getPointColor());
    _plotPoints.setPointSize(_model.getPointSize());

    boolean colorByAttribute = _model.getColorByAttribute();
    String colorAttribute = _model.getColorAttribute();
    boolean sizeByAttribute = _model.getSizeByAttribute();
    String sizeAttribute = _model.getSizeAttribute();
    float sizeAttributeMin = _model.getSizeAttributeMin();
    float sizeAttributeMax = _model.getSizeAttributeMax();
    int pointSizeMin = _model.getPointSizeMin();
    int pointSizeMax = _model.getPointSizeMax();
    for (int i = 0; i < _plotPoints.getPointCount(); i++) {
      IPlotPoint point = _plotPoints.getPoint(i);
      point.setPointStyle(_model.getPointStyle());
      point.setPropertyInheritance(!colorByAttribute && !sizeByAttribute);
      if (_model.getColorByAttribute()) {
        float value = 0;
        if (colorAttribute.equals(Z_ATTRIBUTE)) {
          value = (float) point.getZ();
        } else {
          value = _pointSet.getAttribute(colorAttribute).getFloat(i);
        }
        RGB rgb = colorBar.getColor(value, false);
        point.setPointColor(rgb);
      }
      int pointSize = _model.getPointSize();
      if (sizeByAttribute) {
        float sizeAttrValue = 0;
        if (sizeAttribute.equals(Z_ATTRIBUTE)) {
          sizeAttrValue = (float) point.getZ();
        } else {
          sizeAttrValue = _pointSet.getAttribute(sizeAttribute).getFloat(i);
        }
        float percent = (sizeAttrValue - sizeAttributeMin) / (sizeAttributeMax - sizeAttributeMin);
        percent = Math.max(percent, 0);
        percent = Math.min(percent, 1);
        pointSize = Math.round(pointSizeMin + percent * (pointSizeMax - pointSizeMin));
        pointSize = Math.max(pointSize, 1);
        System.out.println("I: " + i + " SIZE_ATTR: " + sizeAttrValue + "=" + sizeAttrValue + " " + sizeAttributeMin
            + " " + sizeAttributeMax + " " + pointSizeMin + " " + pointSizeMax + " PCNT: " + percent + " SIZE: "
            + pointSize);
      }
      point.setPointSize(pointSize);
    }
    _plotPoints.unblockUpdate();
    _plotPoints.updated();
  }

  @Override
  protected void addPlotShapes() {
    // Create the plot shape representation of the point set.
    _plotPoints = new PlotPointGroup("", new TextProperties(), _model.getPointProperties());
    _plotPoints.setName(_pointSet.getDisplayName());
    ColorBar colorBar = _model.getColorBar();
    PointStyle pointStyle = _model.getPointStyle();
    int pointSize = _model.getPointSize();
    boolean sizeByAttribute = _model.getSizeByAttribute();
    String sizeAttribute = _model.getSizeAttribute();
    float sizeAttributeMin = _model.getSizeAttributeMin();
    float sizeAttributeMax = _model.getSizeAttributeMax();
    float pointSizeMin = _model.getPointSizeMin();
    float pointSizeMax = _model.getPointSizeMax();
    RGB pointColor = _model.getPointColor();
    boolean colorByAttribute = _model.getColorByAttribute();
    String colorAttribute = _model.getColorAttribute();

    // Loop thru the points in the point set, adding them to the plot shape.
    for (int i = 0; i < _pointSet.getNumPoints(); i++) {
      double x = _pointSet.getX(i);
      double y = _pointSet.getY(i);
      double z = _pointSet.getZ(i);
      IPlotPoint point = new PlotPoint("", x, y, z);
      point.setPropertyInheritance(!colorByAttribute && !sizeByAttribute);
      if (sizeByAttribute) {
        float sizeAttrValue = 0;
        if (sizeAttribute.equals(Z_ATTRIBUTE)) {
          sizeAttrValue = (float) point.getZ();
        } else {
          sizeAttrValue = _pointSet.getAttribute(sizeAttribute).getFloat(i);
        }
        float percent = (sizeAttrValue - sizeAttributeMin) / (sizeAttributeMax - sizeAttributeMin);
        percent = Math.max(percent, 0);
        percent = Math.min(percent, 1);
        pointSize = Math.round(pointSizeMin + percent * (pointSizeMax - pointSizeMin));
        pointSize = Math.max(pointSize, 1);
        System.out.println("I: " + i + " SIZE_ATTR: " + sizeAttrValue + "=" + sizeAttrValue + " " + sizeAttributeMin
            + " " + sizeAttributeMax + " " + pointSizeMin + " " + pointSizeMax + " PCNT: " + percent + " SIZE: "
            + pointSize);
      }
      point.setPointSize(pointSize);
      point.setPointColor(pointColor);
      point.setPointStyle(pointStyle);
      if (colorByAttribute) {
        float colorAttrValue = 0;
        if (colorAttribute.equals(Z_ATTRIBUTE)) {
          colorAttrValue = (float) point.getZ();
        } else {
          colorAttrValue = _pointSet.getAttribute(colorAttribute).getFloat(i);
        }
        RGB rgb = colorBar.getColor(colorAttrValue, false);
        point.setPointColor(rgb);
      }
      _plotPoints.addPoint(point);
    }
    // Add the plot shape to the renderer.
    addShape(_plotPoints);
  }

  @Override
  protected void addPopupMenuActions() {
    Dialog dialog = new PointSetRendererDialog(getShell(), _pointSet.getDisplayName(), this);
    addSettingsPopupMenuAction(dialog, 600, SWT.DEFAULT);
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    // Add the renderer to the point set folder in the map view tree.
    addToLayerTree(IViewer.POINTSET_FOLDER, autoUpdate);
  }

  @Override
  protected void setNameAndImage() {
    // Set the renderer name and image to that of the point set.
    setName(_pointSet);
    setImage(ModelUI.getSharedImages().getImage(_pointSet));
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    // Store the reference to the point set entity.
    _pointSet = (PointSet) objects[0];
    if (_model.getColorByAttribute()) {
      PointSetRendererModel.updateColorBarRangeBasedOnAttribute(_pointSet, _model.getColorBar(),
          _model.getColorAttribute());
    }
  }

}
