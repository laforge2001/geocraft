/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.renderer.seismic;


import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.validation.IValidation;


/**
 * Defines the model of properties used to render a <code>PostStack3d</code> entity
 * in the 3D viewer.
 */
public class PostStack3dRendererModel extends SeismicDatasetRendererModel {

  /** The key constant for the inline slice visibility property. */
  public static final String INLINE_SLICE_VISIBLE = "Inline Visible";

  /** The key constant for the xline slice visibility property. */
  public static final String XLINE_SLICE_VISIBLE = "Xline Visible";

  /** The key constant for the z slice visibility property. */
  public static final String Z_SLICE_VISIBLE = "Z Visible";

  /** The key constant for the inline slice  property. */
  public static final String INLINE_SLICE = "Inline Slice";

  /** The key constant for the xline slice  property. */
  public static final String XLINE_SLICE = "Xline Slice";

  /** The key constant for the z slice property. */
  public static final String Z_SLICE = "Z Slice";

  /** The inline slice visibility property. */
  private final BooleanProperty _inlineSliceVisible;

  /** The xline slice visibility property. */
  private final BooleanProperty _xlineSliceVisible;

  /** The z slice visibility property. */
  private final BooleanProperty _zSliceVisible;

  /** The inline slice property. */
  private final FloatProperty _inlineSlice;

  /** The xline slice property. */
  private final FloatProperty _xlineSlice;

  /** The z slice property. */
  private final FloatProperty _zSlice;

  /**
   * Constructs a renderer model with default settings.
   */
  public PostStack3dRendererModel() {
    _inlineSliceVisible = addBooleanProperty(INLINE_SLICE_VISIBLE, true);
    _xlineSliceVisible = addBooleanProperty(XLINE_SLICE_VISIBLE, true);
    _zSliceVisible = addBooleanProperty(Z_SLICE_VISIBLE, false);
    _inlineSlice = addFloatProperty(INLINE_SLICE, 0);
    _xlineSlice = addFloatProperty(XLINE_SLICE, 0);
    _zSlice = addFloatProperty(Z_SLICE, 0);
  }

  /**
   * Constructs the renderer model, copied from another.
   * 
   * @param model the model from which to copy properties.
   */
  public PostStack3dRendererModel(final PostStack3dRendererModel model) {
    this();
    updateFrom(model);
  }

  /**
   * Updates this model based on the properties of another.
   * 
   * @param model the model from which to copy properties.
   */
  public void updateFrom(final PostStack3dRendererModel model) {
    super.updateFrom(model);
  }

  /**
   * Gets the inline slice visibility.
   * 
   * @return <i>true</i> to show the inline slice; <i>false</i> to hide.
   */
  public final boolean getInlineSliceVisible() {
    return _inlineSliceVisible.get();
  }

  /**
   * Sets the inline slice visibility.
   * 
   * @param inlineSliceVisible <i>true</i> to show the inline slice; <i>false</i> to hide.
   */
  public final void setInlineSliceVisible(final boolean inlineSliceVisible) {
    _inlineSliceVisible.set(inlineSliceVisible);
  }

  /**
   * Gets the xline slice visibility.
   * 
   * @return <i>true</i> to show the xline slice; <i>false</i> to hide.
   */
  public final boolean getXlineSliceVisible() {
    return _xlineSliceVisible.get();
  }

  /**
   * Sets the xline slice visibility.
   * 
   * @param xlineSliceVisible <i>true</i> to show the xline slice; <i>false</i> to hide.
   */
  public final void setXlineSliceVisible(final boolean xlineSliceVisible) {
    _xlineSliceVisible.set(xlineSliceVisible);
  }

  /**
   * Gets the z slice visibility.
   * 
   * @return <i>true</i> to show the z slice; <i>false</i> to hide.
   */
  public final boolean getZSliceVisible() {
    return _zSliceVisible.get();
  }

  /**
   * Sets the z slice visibility.
   * 
   * @param zSliceVisible <i>true</i> to show the z slice; <i>false</i> to hide.
   */
  public final void setZSliceVisible(final boolean zSliceVisible) {
    _zSliceVisible.set(zSliceVisible);
  }

  /**
   * Gets the inline slice to render.
   * 
   * @return the inline slice.
   */
  public final float getInlineSlice() {
    return _inlineSlice.get();
  }

  /**
   * Sets the inline slice to render.
   * 
   * @param inlineSlice the inline slice.
   */
  public final void setInlineSlice(final float inlineSlice) {
    _inlineSlice.set(inlineSlice);
  }

  /**
   * Gets the xline slice to render.
   * 
   * @return the xline slice.
   */
  public final float getXlineSlice() {
    return _xlineSlice.get();
  }

  /**
   * Sets the xline slice to render.
   * 
   * @param xlineSlice the xline slice.
   */
  public final void setXlineSlice(final float xlineSlice) {
    _xlineSlice.set(xlineSlice);
  }

  /**
   * Gets the z slice to render.
   * 
   * @return the z slice.
   */
  public final float getZSlice() {
    return _zSlice.get();
  }

  /**
   * Sets the z slice to render.
   * 
   * @param zSlice the z slice.
   */
  public final void setZSlice(final float zSlice) {
    _zSlice.set(zSlice);
  }

  @Override
  public void validate(final IValidation results) {
    super.validate(results);
  }

}
