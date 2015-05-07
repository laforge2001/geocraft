/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.ui.volumeviewer.widget;


import java.nio.FloatBuffer;

import org.geocraft.internal.ui.volumeviewer.canvas.ViewCanvasImplementor;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.Transform.ValueType;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.shape.AxisRods;
import com.ardor3d.util.geom.BufferUtils;


/**
 * Extension of ardor3d's AxisRods that have additional functionality for resizing rods, etc.
 * 
 * @author Joshua Slack
 *
 */
public class FocusRods extends AxisRods {

  public enum ShowMode {
    /** Show at all times. */
    ALWAYS("alwaysOn", "Always on"),
    /** Show when you are rotating or panning the camera. */
    ON_INTERACT("onInteract", "On mouse interaction"),
    /** Never show. */
    NEVER("alwaysOff", "Always off");

    private String _id;

    private String _name;

    private ShowMode(final String id, final String name) {
      _id = id;
      _name = name;
    }

    public String getId() {
      return _id;
    }

    public String getName() {
      return _name;
    }

    public static ShowMode getModeForId(final String id) {
      ShowMode mode = null;
      ShowMode[] modes = values();
      for (int i = 0; i < modes.length && mode == null; i++) {
        if (modes[i].getId().equals(id)) {
          mode = modes[i];
        }
      }
      if (mode == null) {
        mode = ON_INTERACT;
      }
      return mode;
    }
  }

  // scale of the rods versus screen size and camera distance
  private double _scaleFactor = 0.05;

  // width of our axis lines
  private float _lineWidth = 2.5f;

  private ShowMode _mode = ShowMode.ON_INTERACT;

  // Our main axis lines - these respect z depth
  private Line _northMain, _eastMain, _upMain;

  private Line _southMain, _westMain, _downMain;

  // Our "ghost" axis lines - these do not respect z depth, but are blended with a low alpha
  private Line _northGhost, _eastGhost, _upGhost;

  private Line _southGhost, _westGhost, _downGhost;

  private final ViewCanvasImplementor _impl;

  private final boolean _cursor;

  private boolean _interacting;

  public FocusRods(final ViewCanvasImplementor impl, final boolean cursor) {
    super("focus", false, 1);
    _impl = impl;
    setCullHint(CullHint.Always);
    _cursor = cursor;
    if (cursor) {
      ColorRGBA color = ColorRGBA.YELLOW;
      addAxisLines(1000, color, color, color, (short) 0xFFFF, (short) 0xFFFF);
      ColorRGBA color2 = ColorRGBA.WHITE.asMutable();
      color2.setAlpha(0);
      xAxis.setSolidColor(color2);
      yAxis.setSolidColor(color2);
      zAxis.setSolidColor(color2);
    } else {
      addAxisLines(1000000.0, xAxisColor, yAxisColor, zAxisColor, (short) 0xFFFF, (short) 0xFF00);
    }
  }

  /**
   * 
   * @param camera
   * @param tpf
   * @param inFocus
   */
  @SuppressWarnings("fallthrough")
  public void update(final Camera camera, final double tpf, final boolean inFocus) {
    switch (_mode) {
      case NEVER:
        if (getLocalCullHint() != CullHint.Always) {
          setCullHint(CullHint.Always);
          _impl.makeDirty();
        }
        break;
      case ON_INTERACT:
        if (!inFocus || !_interacting) { // rotate, pan
          if (getLocalCullHint() != CullHint.Always) {
            setCullHint(CullHint.Always);
            _impl.makeDirty();
          }
          break;
        }
        // FALLS THROUGH ON PURPOSE
      case ALWAYS:
        if (getLocalCullHint() != CullHint.Never) {
          setCullHint(CullHint.Never);
          _impl.makeDirty();
        }
        break;
    }
    if (getCullHint() != CullHint.Always) {
      // Update size of focus rods
      double scale = camera.getLocation(Vector3.getTempInstance()).distance(
          _localTransform.getValue(ValueType.TranslationX), _localTransform.getValue(ValueType.TranslationY),
          _localTransform.getValue(ValueType.TranslationZ))
          * _scaleFactor;
      if (scale < _scaleFactor) {
        scale = _scaleFactor;
      }

      if (_cursor) {
        Vector3 p1 = new Vector3(0, 0, 0);
        Vector3 p2 = new Vector3(scale, 0, 0);
        // XXX: YIKES!  Do not create a new FloatBuffer every update!!!
        FloatBuffer points = BufferUtils.createFloatBuffer(p1, p2);
        _northMain.getMeshData().setVertexBuffer(points);
        _northGhost.getMeshData().setVertexBuffer(points);
        p2.negateLocal();
        points = BufferUtils.createFloatBuffer(p1, p2);
        _southMain.getMeshData().setVertexBuffer(points);
        _southGhost.getMeshData().setVertexBuffer(points);
        p1.set(0, 0, 0);
        p2.set(0, scale, 0);
        points = BufferUtils.createFloatBuffer(p1, p2);
        _eastMain.getMeshData().setVertexBuffer(points);
        _eastGhost.getMeshData().setVertexBuffer(points);
        p2.negateLocal();
        points = BufferUtils.createFloatBuffer(p1, p2);
        _westMain.getMeshData().setVertexBuffer(points);
        _westGhost.getMeshData().setVertexBuffer(points);
        p1.set(0, 0, 0);
        p2.set(0, 0, scale);
        points = BufferUtils.createFloatBuffer(p1, p2);
        _downMain.getMeshData().setVertexBuffer(points);
        _downGhost.getMeshData().setVertexBuffer(points);
        p2.negateLocal();
        points = BufferUtils.createFloatBuffer(p1, p2);
        _upMain.getMeshData().setVertexBuffer(points);
        _upGhost.getMeshData().setVertexBuffer(points);
      } else {
        xAxis.setScale(scale);
        yAxis.setScale(scale * 2);
        zAxis.setScale(scale, scale, -scale);
        double translation = length * .5 * scale;
        xAxis.setTranslation(translation, 0, 0);
        yAxis.setTranslation(0, translation * 2, 0);
        zAxis.setTranslation(0, 0, -translation);
      }
    }

    updateGeometricState(tpf, true);
  }

  public double getScaleFactor() {
    return _scaleFactor;
  }

  public void setScaleFactor(final double factor) {
    _scaleFactor = factor;
  }

  public ShowMode getShowMode() {
    return _mode;
  }

  public void setShowMode(final ShowMode mode) {
    _mode = mode;
  }

  public boolean isInteracting() {
    return _interacting;
  }

  public void setInteracting(final boolean interacting) {
    _interacting = interacting;
  }

  public double getLineWidth() {
    return _lineWidth;
  }

  public void setAxisLineWidth(final float lineWidth) {
    _lineWidth = lineWidth;
    _northMain.setLineWidth(_lineWidth);
    _northGhost.setLineWidth(_lineWidth);
    _eastMain.setLineWidth(_lineWidth);
    _eastGhost.setLineWidth(_lineWidth);
    _upMain.setLineWidth(_lineWidth);
    _upGhost.setLineWidth(_lineWidth);
    _southMain.setLineWidth(_lineWidth);
    _southGhost.setLineWidth(_lineWidth);
    _westMain.setLineWidth(_lineWidth);
    _westGhost.setLineWidth(_lineWidth);
    _downMain.setLineWidth(_lineWidth);
    _downGhost.setLineWidth(_lineWidth);
  }

  private void addAxisLines(final double lineLength, final ColorRGBA xColor, final ColorRGBA yColor,
      final ColorRGBA zColor, final short mainPattern, final short secondPattern) {
    BlendState alphaBlend = new BlendState();
    alphaBlend.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
    alphaBlend.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
    alphaBlend.setBlendEnabled(true);

    ZBufferState noZ = new ZBufferState();
    noZ.setEnabled(false);
    ZBufferState useZ = new ZBufferState();
    useZ.setEnabled(true);

    Vector3 lineStart = new Vector3();
    Vector3 lineEnd = new Vector3();

    //X Lines
    lineStart.set(0, 0, 0);
    lineEnd.set(lineLength, 0, 0);
    _northMain = addAxisLine(lineStart, lineEnd, mainPattern, xColor, 0.75f, useZ, alphaBlend);
    _northGhost = addAxisLine(lineStart, lineEnd, mainPattern, xColor, 0.08f, noZ, alphaBlend);
    lineEnd.negateLocal();
    _southMain = addAxisLine(lineStart, lineEnd, secondPattern, xColor, 0.75f, useZ, alphaBlend);
    _southGhost = addAxisLine(lineStart, lineEnd, secondPattern, xColor, 0.08f, noZ, alphaBlend);

    //Y Lines
    lineStart.set(0, 0, 0);
    lineEnd.set(0, lineLength, 0);
    _eastMain = addAxisLine(lineStart, lineEnd, mainPattern, yColor, 0.75f, useZ, alphaBlend);
    _eastGhost = addAxisLine(lineStart, lineEnd, mainPattern, yColor, 0.08f, noZ, alphaBlend);
    lineEnd.negateLocal();
    _westMain = addAxisLine(lineStart, lineEnd, secondPattern, yColor, 0.75f, useZ, alphaBlend);
    _westGhost = addAxisLine(lineStart, lineEnd, secondPattern, yColor, 0.08f, noZ, alphaBlend);

    //Z Lines
    lineStart.set(0, 0, 0);
    lineEnd.set(0, 0, -lineLength);
    _upMain = addAxisLine(lineStart, lineEnd, mainPattern, zColor, 0.75f, useZ, alphaBlend);
    _upGhost = addAxisLine(lineStart, lineEnd, mainPattern, zColor, 0.08f, noZ, alphaBlend);
    lineEnd.negateLocal();
    _downMain = addAxisLine(lineStart, lineEnd, secondPattern, zColor, 0.75f, useZ, alphaBlend);
    _downGhost = addAxisLine(lineStart, lineEnd, secondPattern, zColor, 0.08f, noZ, alphaBlend);

    updateRenderState();
  }

  private Line addAxisLine(final Vector3 startPoint, final Vector3 endPoint, final short stipple,
      final ColorRGBA color, final float alpha, final ZBufferState zs, final BlendState bs) {
    FloatBuffer lineVerts = BufferUtils.createFloatBuffer(startPoint, endPoint);
    ColorRGBA lineColor = color.asMutable();
    lineColor.setAlpha(alpha);

    // Make our line
    Line line = new Line("guidLine", lineVerts, null, null, null);
    line.setDefaultColor(lineColor);

    // Set some line properties
    line.setLineWidth(_lineWidth);
    line.setAntialiased(true);
    line.setRenderState(zs);
    line.setRenderState(bs);
    line.setStipplePattern(stipple);

    // attach
    attachChild(line);

    // return
    return line;
  }

  public void setAxisLinesVisible(final boolean visible) {
    CullHint hint;
    if (visible) {
      hint = CullHint.Never;
    } else {
      hint = CullHint.Always;
    }

    _eastMain.setCullHint(hint);
    _eastGhost.setCullHint(hint);
    _westMain.setCullHint(hint);
    _westGhost.setCullHint(hint);
    _upMain.setCullHint(hint);
    _upGhost.setCullHint(hint);
    _downMain.setCullHint(hint);
    _downGhost.setCullHint(hint);
    _northMain.setCullHint(hint);
    _northGhost.setCullHint(hint);
    _southMain.setCullHint(hint);
    _southGhost.setCullHint(hint);
  }
}
