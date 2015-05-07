/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.common.preferences.PropertyStore;
import org.geocraft.ui.volumeviewer.renderer.util.SceneText;

import com.ardor3d.scenegraph.Spatial;


/**
 * A listener for the changes in the preferences store for the 3d viewer settings.
 */
public class VolumeViewerPropertyListener implements IPropertyChangeListener {

  /** The volume viewer. */
  private final IVolumeViewer _viewer;

  /** The preferences store. */
  private final IPreferenceStore _store;

  public VolumeViewerPropertyListener(final IVolumeViewer viewer, final IPreferenceStore store) {
    _viewer = viewer;
    _store = store;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final String propertyName = event.getProperty();
    if (propertyName.equals(VolumeViewerPreferencePage.CURRENT_CENTER)
        || propertyName.equals(VolumeViewerPreferencePage.PROJECTION_MODE)
        || propertyName.equals(VolumeViewerPreferencePage.SELECTION_COLOR)
        || propertyName.equals(PropertyStore.USE_PROJECT_SETTINGS)) {
      final String currentCenter = _store.getString(VolumeViewerPreferencePage.CURRENT_CENTER);
      final String projectionMode = _store.getString(VolumeViewerPreferencePage.PROJECTION_MODE);
      final RGB selectionColor = PreferenceConverter.getColor(_store, VolumeViewerPreferencePage.SELECTION_COLOR);
      _viewer.setPreferences(currentCenter, projectionMode, selectionColor);
    }

    if (propertyName.equals(VolumeViewerPreferencePage.SHOW_LABELS)
        || propertyName.equals(VolumeViewerPreferencePage.TEXT_LABELS_BASE_SIZE)
        || propertyName.equals(PropertyStore.USE_PROJECT_SETTINGS)) {
      final Spatial[] nodes = _viewer.getNodes();
      SceneText.setBaseFontScale(_store.getInt(VolumeViewerPreferencePage.TEXT_LABELS_BASE_SIZE) / 100f);
      for (final Spatial node : nodes) {
        final VolumeViewer viewer = (VolumeViewer) _viewer;
        final VolumeViewRenderer renderer = viewer.getRendererForNode(node);
        renderer.setShowLabels(_store.getBoolean(VolumeViewerPreferencePage.SHOW_LABELS));

        // refresh for the nodes that should change the labels display
        renderer.refresh();
      }
    }

  }
}
