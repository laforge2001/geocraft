/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.renderer.util;


import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.eclipse.jface.preference.IPreferenceStore;
import org.geocraft.core.common.preferences.PropertyStoreFactory;
import org.geocraft.ui.volumeviewer.VolumeViewerPreferencePage;

import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.Transform.ValueType;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.TexCoords;
import com.ardor3d.util.geom.BufferUtils;


/**
 * Simple extension of jME's text class making the text live in the scene.
 * @author Joshua Slack
 *
 */
public class SceneText extends Mesh {

  private static final float FONT_KERNING = 0.625f;

  private static final float INSET = FONT_KERNING / 10f;

  private static final float NEAR_CAP = 250;

  private static final float NEAR_FONT_SIZE = 36.0f;

  private static final float FAR_CAP = 10000;

  private static final float FAR_FONT_SIZE = 18.0f;

  /** following statics are for Alignment */
  private static final int HALF_WIDTH = 1;

  private static final int WIDTH = 2;

  private static final int HALF_HEIGHT = 1;

  private static final int HEIGHT = 2;

  private static final int LEFT = 1;

  private static final int RIGHT = -1;

  private static final int TOP = -1;

  private static final int BOTTOM = 1;

  private static final int NONE = 0;

  public enum Alignment {

    NORTH(-HALF_WIDTH, NONE, NONE, TOP),
    NORTHWEST(NONE, NONE, LEFT, TOP),
    NORTHEAST(-WIDTH, NONE, RIGHT, TOP),
    CENTER(-HALF_WIDTH, HALF_HEIGHT, NONE, NONE),
    WEST(NONE, HALF_HEIGHT, LEFT, NONE),
    EAST(-WIDTH, HALF_HEIGHT, RIGHT, NONE),
    SOUTH(-HALF_WIDTH, HEIGHT, NONE, BOTTOM),
    SOUTHWEST(NONE, HEIGHT, LEFT, BOTTOM),
    SOUTHEAST(-WIDTH, HEIGHT, RIGHT, BOTTOM);

    private final int _horizontal;

    private final int _vertical;

    private final int _gapX;

    private final int _gapY;

    private Alignment(final int hor, final int vert, final int gapX, final int gapY) {
      _horizontal = hor;
      _vertical = vert;
      _gapX = gapX;
      _gapY = gapY;
    }

    public int getHorizontal() {
      return _horizontal;
    }

    public int getVertical() {
      return _vertical;
    }

    public int getGapX() {
      return _gapX;
    }

    public int getGapY() {
      return _gapY;
    }
  }

  private Alignment _alignment;

  /**
   * The size (in characters) of any gaps between the side of the label and its anchor point.
   */
  private float _gapX = .25f, _gapY = .25f;

  private String _text;

  private int _fontSizeFactor = 0;

  private float _fontSize = 1.0f;

  private float _gapFactor;

  private static final IPreferenceStore _store = PropertyStoreFactory.getStore(VolumeViewerPreferencePage.ID);

  private static float _baseFontScale = calculateBaseFontScale();

  private static Vector3 look = new Vector3();

  private static Vector3 left = new Vector3();

  public SceneText(final String sName, final String text) {
    this(sName, text, Alignment.WEST);
  }

  public SceneText(final String sName, final String text, final Alignment alignment) {
    super(sName);
    getMeshData().setIndexMode(IndexMode.Quads);
    setText(text);
    setLightCombineMode(LightCombineMode.Off);
    setTextureCombineMode(TextureCombineMode.Replace);
    setCullHint(Spatial.CullHint.Never);
    // TODO: remove the model bound such as the selection renderer not to include it
    setModelBound(null);
    setRenderBucketType(RenderBucketType.Transparent);
    _alignment = alignment;
  }

  private static float calculateBaseFontScale() {
    _store.setDefault(VolumeViewerPreferencePage.TEXT_LABELS_BASE_SIZE, 100);
    return _store.getInt(VolumeViewerPreferencePage.TEXT_LABELS_BASE_SIZE) / 100f;
  }

  /* (non-Javadoc)
   * @see com.jme.scene.QuadMesh#draw(com.jme.renderer.Renderer)
   */
  @Override
  public void draw(final Renderer r) {
    final Camera cam = ContextManager.getCurrentContext().getCurrentCamera();
    correctScale(cam, r);
    correctTransform(cam);
    updateWorldBound(false);

    super.draw(r);
  }

  public void correctTransform(final Camera cam) {
    updateWorldTransform(false);

    // Billboard rotation
    cam.getDirection(look);
    cam.getLeft(left).negateLocal();
    final Matrix3 rot = Matrix3.getTempInstance();
    rot.fromAxes(left, look, cam.getUp(Vector3.getTempInstance()));
    _worldTransform.setRotation(rot);
    _worldTransform.setScale(_localTransform.getValue(ValueType.ScaleX), _localTransform.getValue(ValueType.ScaleY),
        _localTransform.getValue(ValueType.ScaleZ));

    // correct for alignment using new rotation
    if (_alignment != null) {
      // Update for alignment
      final float halfWidth = getWidth() / 2f;
      final float halfHeight = getHeight() / 2f;

      // co-opt look to act as our standin for translation offset
      look.setX(halfWidth * _alignment.getHorizontal() + _gapX * _alignment.getGapX() * _gapFactor);
      look.setZ(halfHeight * _alignment.getVertical() + _gapY * _alignment.getGapY() * _gapFactor);
      look.setY(0);
      rot.applyPost(look, look);
      _worldTransform.translate(look.getX(), look.getY(), look.getZ());
    }

  }

  /**
   * Update the text's scale to fit the 
   * @param cam
   */
  public void correctScale(final Camera cam, final Renderer r) {
    // get our depth distance
    cam.getLocation(look).negateLocal().addLocal(_worldTransform.getValue(ValueType.TranslationX),
        _worldTransform.getValue(ValueType.TranslationY), _worldTransform.getValue(ValueType.TranslationZ));
    final double zDepth = cam.getDirection(Vector3.getTempInstance()).dot(look);
    if (zDepth > cam.getFrustumFar() || zDepth < cam.getFrustumNear()) {
      // it is out of the picture.
      return;
    }

    // calculate the height in world units of the screen at that depth
    final float heightAtZ = (float) (cam.isParallelProjection() ? cam.getFrustumBottom() : zDepth * cam.getFrustumTop()
        / cam.getFrustumNear());

    // determine a unit/pixel ratio using height
    final float screenHeight = r.getHeight();
    final float pixelRatio = heightAtZ / screenHeight;

    // determine the font size we want:
    float fontSize = pixelRatio;

    // adjust for depth
    if (zDepth < NEAR_CAP) {
      fontSize *= NEAR_FONT_SIZE;
    } else if (zDepth > FAR_CAP) {
      fontSize *= FAR_FONT_SIZE;
    } else {
      final double zDepthRatio = (zDepth - NEAR_CAP) / (FAR_CAP - NEAR_CAP);
      fontSize *= MathUtils.lerp(zDepthRatio, NEAR_FONT_SIZE, FAR_FONT_SIZE);
    }
    _gapFactor = fontSize;
    final float sizeFactor = _baseFontScale + _fontSizeFactor / 10f;
    _fontSize = fontSize * sizeFactor;
    setScale(_fontSize, _fontSize, -_fontSize);
  }

  public float getWidth() {
    return (float) (FONT_KERNING * _text.length() * _worldTransform.getValue(ValueType.ScaleX));
  }

  public float getHeight() {
    return (float) _worldTransform.getValue(ValueType.ScaleY);
  }

  public void setText(final String text) {
    _text = text;

    // setup new geometry for text
    final FloatBuffer vertices = BufferUtils.createVector3Buffer(_text.length() * 4);
    final FloatBuffer uvs = BufferUtils.createVector2Buffer(_text.length() * 4);
    final IntBuffer indices = BufferUtils.createIntBuffer(_text.length() * 4);
    float dx;
    float dy;
    for (int i = 0; i < _text.length(); i++) {
      final int cVal = _text.charAt(i) - 32;
      dx = cVal % 16 / 16.0f;
      dy = cVal / 16 / 16.0f;

      // verts
      final float x = i * FONT_KERNING;
      vertices.put(x).put(0).put(1); // top left
      vertices.put(x).put(0).put(0); // bottom left
      vertices.put(x + 1).put(0).put(0); // bottom right
      vertices.put(x + 1).put(0).put(+1); // top right

      // uvs
      uvs.put(dx).put(1.0f - dy - INSET);
      uvs.put(dx).put(1.0f - dy);
      uvs.put(dx + INSET).put(1.0f - dy);
      uvs.put(dx + INSET).put(1.0f - dy - INSET);

      // indices
      indices.put(i * 4 + 0);
      indices.put(i * 4 + 1);
      indices.put(i * 4 + 2);
      indices.put(i * 4 + 3);
    }

    reconstruct(vertices, null, null, new TexCoords(uvs), indices);
    updateModelBound();
  }

  public String getText() {
    return _text;
  }

  public void setAlignment(final Alignment alignment) {
    _alignment = alignment;
  }

  public Alignment getAlignment() {
    return _alignment;
  }

  public void setGapX(final float gapX) {
    _gapX = gapX;
  }

  public float getGapX() {
    return _gapX;
  }

  public void setGapY(final float gapY) {
    _gapY = gapY;
  }

  public float getGapY() {
    return _gapY;
  }

  public static float getBaseFontScale() {
    return _baseFontScale;
  }

  /**
   * Sets the base scaling of scene text labels before any per SceneText 
   * "fontSizeFactor" is taken into consideration.  Basically a way to 
   * globally enlarge or shrink scene labels.
   * 
   * @param percent the new percent of normal size for the labels. 
   * 1.0 (or 100%) is the default.  0.1 (10%) is the minimum and 
   * will be enforced if percent is lower than that. 
   */
  public static void setBaseFontScale(final float percent) {
    if (percent < 0.1f) {
      _baseFontScale = 0.1f;
    } else {
      _baseFontScale = percent;
    }
  }

  public int getFontSizeFactor() {
    return _fontSizeFactor;
  }

  /**
   * Increases or decreases this font's size by 10% per each factor point, 
   * similar in concept to html's font size="+1", etc.  Basically offers a way to relatively 
   * scale a single label versus other labels.
   * 
   * @param factor minimum is -9, default is 0.
   */
  public void setFontSizeFactor(final int factor) {
    if (factor < -9) {
      _fontSizeFactor = -9;
    } else {
      _fontSizeFactor = factor;
    }
  }
}
