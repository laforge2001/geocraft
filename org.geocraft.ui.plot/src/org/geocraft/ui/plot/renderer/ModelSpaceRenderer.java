/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.renderer;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.geocraft.ui.plot.defs.RenderLevel;
import org.geocraft.ui.plot.defs.ShapeType;
import org.geocraft.ui.plot.defs.UpdateLevel;
import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.plot.model.ICoordinateTransform;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.object.IPlotShape;


/**
 * Renders the contents of model spaces.
 */
public class ModelSpaceRenderer {

  /** The internal collection of shape renderers, mapped by their supported shape type. */
  private final Map<ShapeType, IShapeRenderer> _rendererMap;

  /** The anti-aliasing flag. */
  protected boolean _antiAliasing = false;

  /**
   * The default constructor.
   * 
   * @param coordTransform the coordinate transform.
   */
  public ModelSpaceRenderer(final ICoordinateTransform coordTransform) {
    setAntiAliasing(false);
    _rendererMap = new HashMap<ShapeType, IShapeRenderer>();
    _rendererMap.put(ShapeType.LINE, new LineRenderer(coordTransform));
    _rendererMap.put(ShapeType.POINT_GROUP, new PointGroupRenderer(coordTransform));
    _rendererMap.put(ShapeType.POLYLINE, new PolylineRenderer(coordTransform));
    _rendererMap.put(ShapeType.POLYGON, new PolygonRenderer(coordTransform));
    _rendererMap.put(ShapeType.IMAGE, new ImageRenderer(coordTransform));
  }

  /**
   * Sets the anti-aliasing flag.
   * 
   * @param antiAliasing true for anti-aliasing; otherwise false;
   */
  public void setAntiAliasing(final boolean antiAliasing) {
    _antiAliasing = antiAliasing;
  }

  /**
   * Gets the anti-aliasing flag.
   * 
   * @return true for anti-aliasing; otherwise false;
   */
  public boolean getAntiAliasing() {
    return _antiAliasing;
  }

  /**
   * Sets the rendering hints for the specified graphics-2D.
   * 
   * @param gc the graphics object.
   */
  public void setRenderingHints(final GC gc) {
    //    if (_antiAliasing) {
    //      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    //    } else {
    //      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    //    }
  }

  /**
   * Renders the shapes contained in the specified model space.
   * 
   * @param gc the graphics object.
   * @param rect the rectangle to render in.
   * @param mask the mask to render in.
   * @param renderLevel the level at which to render.
   * @param modelSpace the model space containing the layers to render.
   */
  public void render(final GC gc, final Rectangle rect, final Rectangle mask, final RenderLevel renderLevel,
      final IModelSpace modelSpace) {
    gc.setAntialias(SWT.OFF);
    gc.setTextAntialias(SWT.OFF);
    for (IPlotLayer layer : modelSpace.getLayers()) {
      if (layer.isVisible()) {
        renderInternal(layer.getShapes(), gc, rect, mask, renderLevel);
      }
    }
  }

  /**
   * The internal method for rendering shapes.
   * 
   * @param shapes the array of shapes to render.
   * @param gc the graphics object.
   * @param rect the rectangle to render in.
   * @param mask the mask to render in.
   * @param renderLevel the level at which to render.
   */
  protected void renderInternal(final IPlotShape[] shapes, final GC gc, final Rectangle rect, final Rectangle mask,
      final RenderLevel renderLevel) {

    //setAntiAliasing(false);
    Rectangle clip;
    RenderLevel level = RenderLevel.STANDARD;
    if (mask != null) {
      clip = rect.union(mask);
    } else {
      clip = rect;
    }
    // TODO: Need to re-visit anti-aliasing.
    //if(renderLevel.equals(RenderLevel.Static)) {
    //setAntiAliasing(true);
    //} else if(renderLevel.equals(RenderLevel.Selected)) {
    //setAntiAliasing(false);
    //}
    gc.setClipping(0, 0, clip.width, clip.height);
    for (IPlotShape shape : shapes) {
      if (shape.isVisible()) {
        level = shape.getRenderLevel();
        // TODO: updateLevel = canvas.getUpdateLevel();
        UpdateLevel updateLevel = UpdateLevel.REDRAW;
        if (shape.isSelected()) {
          level = RenderLevel.SELECTED;
        }
        if (level.equals(renderLevel)) {
          IShapeRenderer renderer = _rendererMap.get(shape.getShapeType());
          if (renderer != null) {
            renderer.render(gc, rect, null, shape);
          }
        }
      }
    }
  }

}
