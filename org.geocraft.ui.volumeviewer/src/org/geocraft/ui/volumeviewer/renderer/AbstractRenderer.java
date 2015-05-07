///*
// * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
// */
//package org.geocraft.ui.volumeviewer.renderer;
//
//
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.eclipse.jface.dialogs.Dialog;
//import org.eclipse.jface.preference.IPreferenceStore;
//import org.eclipse.swt.widgets.Shell;
//import org.geocraft.core.common.preferences.PropertyStoreFactory;
//import org.geocraft.core.model.Entity;
//import org.geocraft.core.model.datatypes.Domain;
//import org.geocraft.ui.viewer.ReadoutInfo;
//import org.geocraft.ui.volumeviewer.VolumeCanvasRegistry;
//import org.geocraft.ui.volumeviewer.preference.AbstractViewerPreferencePage;
//import org.geocraft.ui.volumeviewer.preference.ViewerPreferencePage;
//import org.geocraft.ui.volumeviewer.renderer.util.SceneText;
//
//import com.ardor3d.image.Texture.MagnificationFilter;
//import com.ardor3d.image.Texture.MinificationFilter;
//import com.ardor3d.math.ColorRGBA;
//import com.ardor3d.math.Vector3;
//import com.ardor3d.renderer.Renderer;
//import com.ardor3d.scenegraph.Spatial;
//
//
///**
// * The abstract class that each renderer needs to override.
// */
//public abstract class AbstractRenderer {
//
//  /** The entity for which the renderer is built. */
//  protected Entity _entity;
//
//  /** The rendered node. */
//  protected Spatial _node;
//
//  /** The parent shell for the settings dialog. */
//  protected Shell _shell;
//
//  /** The canvas registry. */
//  protected VolumeCanvasRegistry _registry;
//
//  /** The 3d framework renderer instance. */
//  protected Renderer _renderer;
//
//  /** The current domain. */
//  protected Domain _domain;
//
//  /** The preferences store. */
//  private final IPreferenceStore _store = PropertyStoreFactory.getStore(AbstractViewerPreferencePage.ID);
//
//  /** The labels display status, for the renderers supporting labels. */
//  private boolean _showLabels = _store.getBoolean(ViewerPreferencePage.SHOW_LABELS_KEY);
//
//  /** If enabled, an interpolation smoothing will be applied over the textures. */
//  private boolean _smoothing;
//
//  /** The magnification filter used for rendering textures. */
//  private MagnificationFilter _mag = MagnificationFilter.NearestNeighbor;
//
//  /** The minification filter used for rendering textures. */
//  private MinificationFilter _min = MinificationFilter.NearestNeighborLinearMipMap;
//
//  /** The aniso level used for rendering textures. */
//  private float _aniso = 0.0f;
//
//  /**
//   * Initialize the spatial representation of the entity.
//   * @return the spatial object
//   */
//  public abstract Spatial initSpatialRepresentation();
//
//  /**
//   * Return the entity to be rendered.
//   * @return the entity
//   */
//  public Entity getEntity() {
//    return _entity;
//  }
//
//  /**
//   * Return the rendered spatial.
//   * @return the spatial
//   */
//  public Spatial getNode() {
//    return _node;
//  }
//
//  /**
//   * Return the current domain.
//   * @return the domain
//   */
//  public Domain getDomain() {
//    return _domain;
//  }
//
//  /**
//   * Return the show labels status.
//   * @return the show labels status
//   */
//  public boolean getShowLabels() {
//    return _showLabels;
//  }
//
//  /**
//   * Set the show labels status.
//   * @param showLabels the show labels status
//   */
//  public void setShowLabels(final boolean showLabels) {
//    _showLabels = showLabels;
//  }
//
//  /**
//   * Return the smoothing status.
//   * @return the smoothing status
//   */
//  public boolean getSmoothing() {
//    return _smoothing;
//  }
//
//  /**
//   * Set the smoothing status.
//   * @param smoothing the smoothing status
//   */
//  public void setSmoothing(final boolean smoothing) {
//    _smoothing = smoothing;
//    _mag = MagnificationFilter.NearestNeighbor;
//    _min = MinificationFilter.NearestNeighborLinearMipMap;
//    _aniso = 0.0f;
//    if (_smoothing) {
//      _mag = MagnificationFilter.Bilinear;
//      _min = MinificationFilter.Trilinear;
//      _aniso = 1.0f;
//    }
//  }
//
//  /**
//   * Return the magnification filter used for rendering textures.
//   * @return the magnification filter used for rendering textures
//   */
//  public MagnificationFilter getMagnificationFilter() {
//    return _mag;
//  }
//
//  /**
//   * Return the minification filter used for rendering textures.
//   * @return the minification filter used for rendering textures
//   */
//  public MinificationFilter getMinificationFilter() {
//    return _min;
//  }
//
//  /**
//   * Return the aniso level used for rendering textures.
//   * @return the aniso level used for rendering textures
//   */
//  public float getAnisoLevel() {
//    return _aniso;
//  }
//
//  /**
//   * Set the current domain,
//   * @param domain the domain
//   */
//  public void setDomain(final Domain domain) {
//    if (domain == null) {
//      throw new IllegalArgumentException("Domain was null");
//    }
//    _domain = domain;
//  }
//
//  /**
//   * Return the volume registry.
//   * @return the volume registry
//   */
//  public VolumeCanvasRegistry getRegistry() {
//    return _registry;
//  }
//
//  /**
//   * Set the renderer needed objects.
//   * @param shell the parent shell for the settings dialog
//   * @param entity the entity to be rendered
//   * @param registry the volume registry
//   */
//  public void setData(final Shell shell, final Entity entity, final VolumeCanvasRegistry registry) {
//    _shell = shell;
//    _entity = entity;
//    _registry = registry;
//  }
//
//  /**
//   * Build and return a renderer specific dialog.
//   * @return the settings dialog
//   */
//  public abstract Dialog getSettingsDialog();
//
//  /**
//   * Refresh the spatial representation of the entity.
//   */
//  public void refresh() {
//    // does nothing for now in the abstract class
//  }
//
//  /**
//   * Set the values to be used for the current display.
//   * @param position the current display position
//   */
//  public void setCurrentPosition(final float[] position) {
//    // to be implemented by the interested renderers
//  }
//
//  /**
//   * Null renderers that provide no spatial representation are fake.
//   * @return the renderer status
//   */
//  public boolean isFake() {
//    return false;
//  }
//
//  /**
//   * Build and return renderer specific data, to be displayed in the readout panel.
//   * @return renderer specific data
//   */
//  public ReadoutInfo[] getReadoutData() {
//
//    final String title = _entity.getType() + " : " + _entity.getDisplayName();
//    final List<String> keys = new ArrayList<String>();
//    final List<String> vals = new ArrayList<String>();
//
//    final Vector3 pickLoc = _registry.getPickLocation();
//    if (pickLoc != null) {
//      keys.add("x");
//      keys.add("y");
//      keys.add("z");
//      vals.add(pickLoc.getX() + "");
//      vals.add(pickLoc.getY() + "");
//      vals.add(pickLoc.getZ() + "");
//    }
//    return new ReadoutInfo[] { new ReadoutInfo(title, keys, vals) };
//  }
//
//  /**
//   * Build and return renderer specific data, to be displayed in the readout panel.
//   * @return renderer specific data
//   */
//  public Map<String, Object> getReadoutDataXX() {
//    final Map<String, Object> data = new LinkedHashMap<String, Object>();
//    if (_entity != null) {
//      data.put("Entity", _entity.getType() + " : " + _entity.getDisplayName());
//    }
//    final Vector3 pickLoc = _registry.getPickLocation();
//    if (pickLoc != null) {
//      data.put("Pick location",
//          new String[][] { { "Axis", "Location" }, { "x", pickLoc.getX() + "" }, { "y", pickLoc.getY() + "" },
//              { "z", pickLoc.getZ() + "" } });
//    }
//    return data;
//  }
//
//  /**
//   * Return a renderer specific message, to be displayed on the bottom side of the viewer.
//   * @return a short message
//   */
//  public String getShortMessage() {
//    final Vector3 pickLoc = _registry.getPickLocation();
//    if (pickLoc != null) {
//      return "x=" + pickLoc.getX() + ", y=" + pickLoc.getY() + ", z=" + pickLoc.getZ();
//    }
//    return "";
//  }
//
//  /**
//   * Overridden by the entities that need to provide their own outline functionality.
//   * Initialize the renderer provided outline.
//   * @return the outline support status
//   */
//  public boolean renderOutline() {
//    return false;
//  }
//
//  /**
//   * Overridden by the entities that need to provide their own outline functionality.
//   * Removes the renderer provided outline.
//   */
//  public void clearOutline() {
//    // does nothing for now
//  }
//
//  /**
//   * Build and return a scene text for a label.
//   * @param loc the text location
//   * @param name the spatial name
//   * @param label the text to be rendered
//   * @param alignment the text alignment
//   * @return the scene text spatial
//   */
//  protected SceneText getTextLabel(final Vector3 loc, final String name, final double label,
//      final SceneText.Alignment alignment) {
//    String text = (float) label + "";
//    final int value = (int) label;
//    if (value == label) {
//      text = value + "";
//    }
//    final SceneText sceneText = _registry.createSceneText(name, text, alignment);
//    sceneText.setTranslation(loc);
//    sceneText.setDefaultColor(ColorRGBA.WHITE);
//    return sceneText;
//  }
//
//  /**
//   * Called by the viewer when a renderer is disposed.
//   * Should release the resources hold by the renderer.
//   */
//  public abstract void dispose();
//
//}
