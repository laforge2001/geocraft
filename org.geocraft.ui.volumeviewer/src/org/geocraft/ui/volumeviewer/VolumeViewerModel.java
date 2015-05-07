/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer;


import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.ColorProperty;
import org.geocraft.core.model.property.DoubleProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.validation.IValidation;


/**
 * This class defines the model of display properties for the 3D viewer.
 */
public class VolumeViewerModel extends Model {

  /** The key constant for the background color. */
  private static final String BACKGROUND_COLOR = "Background Color";

  /** The key constant for the domain property. */
  private static final String DOMAIN = "Domain";

  /** The key constant for the z-scaling property. */
  private static final String Z_SCALING = "ZScaling";

  /** The key constant for the projection property. */
  private static final String PROJECTION = "Projection";

  /** The key constant for the pick location visible property. */
  private static final String SHOW_PICK_LOCATION = "Show Pick Location";

  /** The background color property. */
  private final ColorProperty _backgroundColor;

  /** The domain property. */
  private final EnumProperty<VolumeViewZDomain> _domain;

  /** The z scaling property. */
  public final DoubleProperty _zScaling;

  /** The projection property. */
  public final EnumProperty<ProjectionMode> _projection;

  /** The pick location visible property. */
  private final BooleanProperty _showPickLoc;

  /**
   * Constructs the model of display properties for the 3D viewer.
   */
  public VolumeViewerModel() {
    _backgroundColor = addColorProperty(BACKGROUND_COLOR, new RGB(0, 0, 0));
    _domain = addEnumProperty(DOMAIN, VolumeViewZDomain.class, VolumeViewZDomain.TIME);
    _zScaling = addDoubleProperty(Z_SCALING, 1);
    _projection = addEnumProperty(PROJECTION, ProjectionMode.class, ProjectionMode.PERSPECTIVE);
    _showPickLoc = addBooleanProperty(SHOW_PICK_LOCATION, false);
  }

  /**
   * Gets the z-domain (time or depth).
   * 
   * @return the z-domain.
   */
  public VolumeViewZDomain getZDomain() {
    return _domain.get();
  }

  /**
   * Sets the z-domain (time or depth).
   * 
   * @param domain the z-domain.
   */
  public void setZDomain(final VolumeViewZDomain domain) {
    _domain.set(domain);
  }

  /**
   * Gets the z-scaling factor.
   * 
   * @return the z-scaling factor.
   */
  public double getZScaling() {
    return _zScaling.get();
  }

  /**
   * Sets the z-scaling factor.
   * 
   * @param zScaling the z-scaling factor.
   */
  public void setZScaling(final double zScaling) {
    _zScaling.set(zScaling);
  }

  /**
   * Gets the background color.
   * 
   * @return the background color.
   */
  public RGB getBackgroundColor() {
    return _backgroundColor.get();
  }

  /**
   * Sets the background color.
   * 
   * @param color the background color.
   */
  public void setBackgroundColor(final RGB color) {
    _backgroundColor.set(color);
  }

  /**
   * Gets the projection method.
   * 
   * @return the projection method.
   */
  public ProjectionMode getProjection() {
    return _projection.get();
  }

  /**
   * Sets the projection method.
   * 
   * @param projection the projection method.
   */
  public void setProjection(final ProjectionMode projection) {
    _projection.set(projection);
  }

  /**
   * Gets the pick location visible flag.
   * 
   * @return <i>true</i> to show pick locations; otherwise <i>false</i>.
   */
  public boolean getShowPickLocation() {
    return _showPickLoc.get();
  }

  /**
   * Sets the pick location visible flag.
   * 
   * @return showPickLoc <i>true</i> to show pick locations; otherwise <i>false</i>.
   */
  public void setShowPickLocation(final boolean showPickLoc) {
    _showPickLoc.set(showPickLoc);
  }

  public void validate(final IValidation results) {
    // Validate the background color is specified.
    if (_backgroundColor.isNull()) {
      results.error(BACKGROUND_COLOR, "No background color specified.");
    }

    // Validate the domain is either time or distance (i.e. depth).
    if (_domain.isNull()) {
      results.error(DOMAIN, "No domain specified.");
    }

    // Validate the z-scaling factor is positive.
    if (_zScaling.get() <= 0) {
      results.error(Z_SCALING, "Z-Scaling factor cannot be negative.");
    }

    // Validate the projection method is specified.
    if (_projection.isNull()) {
      results.error(PROJECTION, "No projection method specified.");
    }
  }

}
