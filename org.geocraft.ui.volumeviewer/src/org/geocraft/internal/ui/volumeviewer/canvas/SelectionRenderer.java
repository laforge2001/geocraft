/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.ui.volumeviewer.canvas;


import java.nio.FloatBuffer;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.renderer.state.BlendState.DestinationFunction;
import com.ardor3d.renderer.state.BlendState.SourceFunction;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.geom.BufferUtils;


/**
 * @author Joshua Slack
 */
public class SelectionRenderer {

  private static Line _outline = null;

  private final static float EDGE_LENGTH = .25f;

  private static final float PADDING = 50;

  public static void updateOutline(final Spatial selectedItem, final ColorRGBA color) {
    if (_outline == null) {
      makeOutline(color);
    }
    _outline.setDefaultColor(color);

    selectedItem.updateGeometricState(0, true);
    final BoundingVolume selBoundingVol = selectedItem.getWorldBound();
    if (selBoundingVol instanceof BoundingBox) {
      BoundingBox bb = (BoundingBox) selBoundingVol;
      _outline.setTranslation(bb.getCenter());
      _outline.setScale(bb.xExtent + PADDING, bb.yExtent + PADDING, bb.zExtent + PADDING);
    } else {
      BoundingBox tempBB = new BoundingBox();
      tempBB.xExtent = 0;
      tempBB.yExtent = 0;
      tempBB.zExtent = 0;
      tempBB.setCenter(selBoundingVol.getCenter());
      tempBB.mergeLocal(selBoundingVol);

      _outline.setTranslation(tempBB.getCenter());
      _outline.setScale(tempBB.xExtent + PADDING, tempBB.yExtent + PADDING, tempBB.zExtent + PADDING);
    }

    _outline.updateWorldTransform(false);
  }

  public static void drawOutline(final Renderer r) {
    if (_outline != null) {
      _outline.draw(r);
    }
  }

  /**
   * 
   */
  private static void makeOutline(final ColorRGBA color) {
    _outline = new Line("outline");
    _outline.setDefaultColor(color);
    _outline.setAntialiased(true);
    _outline.setLineWidth(2.5f);

    FloatBuffer outlineVerts = BufferUtils.createVector3Buffer(48);

    outlineVerts.put(1).put(1).put(1);
    outlineVerts.put(1 - EDGE_LENGTH).put(1).put(1);
    outlineVerts.put(1).put(1).put(1);
    outlineVerts.put(1).put(1 - EDGE_LENGTH).put(1);
    outlineVerts.put(1).put(1).put(1);
    outlineVerts.put(1).put(1).put(1 - EDGE_LENGTH);

    outlineVerts.put(1).put(1).put(-1);
    outlineVerts.put(1 - EDGE_LENGTH).put(1).put(-1);
    outlineVerts.put(1).put(1).put(-1);
    outlineVerts.put(1).put(1 - EDGE_LENGTH).put(-1);
    outlineVerts.put(1).put(1).put(-1);
    outlineVerts.put(1).put(1).put(EDGE_LENGTH - 1);

    outlineVerts.put(1).put(-1).put(1);
    outlineVerts.put(1 - EDGE_LENGTH).put(-1).put(1);
    outlineVerts.put(1).put(-1).put(1);
    outlineVerts.put(1).put(EDGE_LENGTH - 1).put(1);
    outlineVerts.put(1).put(-1).put(1);
    outlineVerts.put(1).put(-1).put(1 - EDGE_LENGTH);

    outlineVerts.put(1).put(-1).put(-1);
    outlineVerts.put(1 - EDGE_LENGTH).put(-1).put(-1);
    outlineVerts.put(1).put(-1).put(-1);
    outlineVerts.put(1).put(EDGE_LENGTH - 1).put(-1);
    outlineVerts.put(1).put(-1).put(-1);
    outlineVerts.put(1).put(-1).put(EDGE_LENGTH - 1);

    outlineVerts.put(-1).put(1).put(1);
    outlineVerts.put(EDGE_LENGTH - 1).put(1).put(1);
    outlineVerts.put(-1).put(1).put(1);
    outlineVerts.put(-1).put(1 - EDGE_LENGTH).put(1);
    outlineVerts.put(-1).put(1).put(1);
    outlineVerts.put(-1).put(1).put(1 - EDGE_LENGTH);

    outlineVerts.put(-1).put(1).put(-1);
    outlineVerts.put(EDGE_LENGTH - 1).put(1).put(-1);
    outlineVerts.put(-1).put(1).put(-1);
    outlineVerts.put(-1).put(1 - EDGE_LENGTH).put(-1);
    outlineVerts.put(-1).put(1).put(-1);
    outlineVerts.put(-1).put(1).put(EDGE_LENGTH - 1);

    outlineVerts.put(-1).put(-1).put(1);
    outlineVerts.put(EDGE_LENGTH - 1).put(-1).put(1);
    outlineVerts.put(-1).put(-1).put(1);
    outlineVerts.put(-1).put(EDGE_LENGTH - 1).put(1);
    outlineVerts.put(-1).put(-1).put(1);
    outlineVerts.put(-1).put(-1).put(1 - EDGE_LENGTH);

    outlineVerts.put(-1).put(-1).put(-1);
    outlineVerts.put(EDGE_LENGTH - 1).put(-1).put(-1);
    outlineVerts.put(-1).put(-1).put(-1);
    outlineVerts.put(-1).put(EDGE_LENGTH - 1).put(-1);
    outlineVerts.put(-1).put(-1).put(-1);
    outlineVerts.put(-1).put(-1).put(EDGE_LENGTH - 1);

    _outline.getMeshData().setVertexBuffer(outlineVerts);
    _outline.generateIndices();

    BlendState bState = new BlendState();
    bState.setEnabled(true);
    bState.setBlendEnabled(true);
    bState.setSourceFunction(SourceFunction.SourceAlpha);
    bState.setDestinationFunction(DestinationFunction.OneMinusSourceAlpha);
    _outline.setRenderState(bState);

    ZBufferState zState = new ZBufferState();
    zState.setWritable(false);
    zState.setEnabled(true);
    // XXX: change above to "false" and uncomment below if you prefer not to see selection box through entities. 
    //    zState.setFunction(TestFunction.LessThanOrEqualTo);
    _outline.setRenderState(zState);
    _outline.setRenderBucketType(RenderBucketType.Transparent);

    _outline.updateRenderState();
  }
}
