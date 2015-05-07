/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer.renderer.seismic;


import org.geocraft.core.color.ColorBar;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.core.model.seismic.PostStack2d;
import org.geocraft.core.model.seismic.SeismicSurvey2d;
import org.geocraft.ui.mapviewer.MapViewRenderer;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;


public class PostStack2dRenderer extends MapViewRenderer {

  /** The 2D poststack dataset to render. */
  private PostStack2d _poststack;

  /** The model of display settings. */
  private final PostStack2dRendererModel _model;

  public PostStack2dRenderer() {
    super("PostStack 2d Renderer");
    _model = new PostStack2dRendererModel();
    showReadoutInfo(true);
  }

  @Override
  protected void addPlotShapes() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void addPopupMenuActions() {
    //Dialog dialog = new PostStack2dRendererDialog(_shell, _poststack.getDisplayName(), this);
    //addSettingsPopupMenuAction(dialog, 600, SWT.DEFAULT);
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    // Add this renderer to the viewer tree.
    SeismicSurvey2d survey = _poststack.getSurvey();
    addToLayerTree(survey, survey.getDisplayName(), IViewer.SEISMIC_FOLDER, autoUpdate);
  }

  @Override
  public DataSelection getDataSelection(final double x, final double y) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ReadoutInfo getReadoutInfo(final double x, final double y) {
    return new ReadoutInfo(getName());
  }

  @Override
  public void redraw() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void setNameAndImage() {
    // Update the name and icon of the renderer based on the dataset.
    setName(_poststack);
    setImage(ModelUI.getSharedImages().getImage(_poststack));
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _poststack };
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    _poststack = (PostStack2d) objects[0];
  }

  public PostStack2dRendererModel getSettingsModel() {
    return _model;
  }

  /**
   * Updates the display settings.
   * This will trigger the renderer to redraw.
   */
  public void updateSettings(final PostStack2dRendererModel model, final ColorBar colorBar) {
    _model.updateFrom(model);
    //float z = _poststack.getZStart() + _model.getZSlice() * _poststack.getZDelta();
    //System.out.println("Displaying slice: " + z);

    //    _plotPoints.blockUpdate();
    //    _plotPoints.setPointStyle(_model.getPointStyle());
    //    _plotPoints.setPointColor(_model.getPointColor());
    //    _plotPoints.setPointSize(_model.getPointSize());
    //    for (int i = 0; i < _plotPoints.getPointCount(); i++) {
    //      IPlotPoint point = _plotPoints.getPoint(i);
    //      point.setPointStyle(_model.getPointStyle());
    //      point.setPointSize(_model.getPointSize());
    //      point.setPropertyInheritance(!_model.getColorByAttribute());
    //      if (_model.getColorByAttribute()) {
    //        String attributeName = _model.getColorAttribute();
    //        float value = 0;
    //        if (attributeName.equals(PointSetRendererModel.Z_ATTRIBUTE)) {
    //          value = (float) point.getZ();
    //        } else {
    //          value = _pointSet.getAttribute(_model.getColorAttribute(), i);
    //        }
    //        RGB rgb = colorBar.getColor(value, false);
    //        point.setPointColor(rgb);
    //      }
    //    }
    //    _plotPoints.unblockUpdate();
    //    _plotPoints.updated();
  }

}
