/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.ui.volumeviewer.renderer.util;


import org.eclipse.jface.preference.IPreferenceStore;
import org.geocraft.core.common.preferences.PropertyStoreFactory;
import org.geocraft.core.rendering.scene.TextOverlay;
import org.geocraft.ui.volumeviewer.VolumeViewerPreferencePage;
import org.joml.Vector4f;


/**
 * Simple billboarded scene text.
 *
 * TODO: port from Ardor3D BMText. Currently stubbed as a TextOverlay
 * with the Ardor3D-style public API preserved so callers compile.
 */
public class SceneText extends TextOverlay {

  public enum Alignment {
    NORTH, NORTHWEST, NORTHEAST, CENTER, WEST, EAST, SOUTH, SOUTHWEST, SOUTHEAST
  }

  private Alignment _alignment;
  private float _gapX = .25f, _gapY = .25f;
  private int _fontSizeFactor = 0;
  private Vector4f _defaultColor = new Vector4f(1, 1, 1, 1);

  private static final IPreferenceStore _store = PropertyStoreFactory.getStore(VolumeViewerPreferencePage.ID);
  private static float _baseFontScale = calculateBaseFontScale();

  public SceneText(final String sName, final String text) {
    this(sName, text, Alignment.WEST);
  }

  public SceneText(final String sName, final String text, final Alignment alignment) {
    super(sName, text);
    _alignment = alignment;
  }

  private static float calculateBaseFontScale() {
    _store.setDefault(VolumeViewerPreferencePage.TEXT_LABELS_BASE_SIZE, 100);
    return _store.getInt(VolumeViewerPreferencePage.TEXT_LABELS_BASE_SIZE) / 100f;
  }

  public void setDefaultColor(Vector4f color) { _defaultColor = new Vector4f(color); }
  public Vector4f getDefaultColor() { return new Vector4f(_defaultColor); }

  public float getWidth() { return getText() == null ? 0f : getText().length() * getFontSize() * 0.6f; }
  public float getHeight() { return getFontSize(); }

  public void setAlignment(final Alignment alignment) { _alignment = alignment; }
  public Alignment getAlignment() { return _alignment; }

  public void setGapX(final float gapX) { _gapX = gapX; }
  public float getGapX() { return _gapX; }

  public void setGapY(final float gapY) { _gapY = gapY; }
  public float getGapY() { return _gapY; }

  public static float getBaseFontScale() { return _baseFontScale; }

  public static void setBaseFontScale(final float percent) {
    _baseFontScale = Math.max(0.1f, percent);
  }

  public int getFontSizeFactor() { return _fontSizeFactor; }
  public void setFontSizeFactor(final int factor) { _fontSizeFactor = Math.max(-9, factor); }
}
