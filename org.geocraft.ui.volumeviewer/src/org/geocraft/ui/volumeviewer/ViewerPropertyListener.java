/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.geocraft.ui.volumeviewer.preference.ViewerPreferencePage;


/**
 * A listener for the changes in the preferences store for the 3d viewer settings.
 */
public class ViewerPropertyListener implements IPropertyChangeListener {

  /** The volume viewer. */
  private final IVolumeViewer _viewer;

  /** The preferences store. */
  private final IPreferenceStore _store;

  ViewerPropertyListener(final IVolumeViewer viewer, final IPreferenceStore store) {
    _viewer = viewer;
    _store = store;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final String propertyName = event.getProperty();
    if (ViewerPreferencePage.CURRENT_CENTER_KEY.equals(propertyName)
        || ViewerPreferencePage.PROJECTION_MODE_KEY.equals(propertyName)
        || ViewerPreferencePage.SELECTION_COLOR_KEY.equals(propertyName) || "useProjectSettings".equals(propertyName)) {
      _viewer.setPreferences(_store.getString(ViewerPreferencePage.CURRENT_CENTER_KEY), _store
          .getString(ViewerPreferencePage.PROJECTION_MODE_KEY), PreferenceConverter.getColor(_store,
          ViewerPreferencePage.SELECTION_COLOR_KEY));
    }

    if (ViewerPreferencePage.SHOW_LABELS_KEY.equals(propertyName)
        || ViewerPreferencePage.LABELS_TEXT_BASE_SIZE.equals(propertyName) || "useProjectSettings".equals(propertyName)) {
      //      final VolumeCanvasRegistry registry = _viewer.getRegistry();
      //      final Spatial[] nodes = registry.getNodes();
      //      SceneText.setBaseFontScale(_store.getInt(ViewerPreferencePage.LABELS_TEXT_BASE_SIZE) / 100f);
      //      for (final Spatial node : nodes) {
      //        final AbstractRenderer renderer = registry.getRendererForNode(node);
      //        renderer.setShowLabels(_store.getBoolean(ViewerPreferencePage.SHOW_LABELS_KEY));
      //
      //        // refresh for the nodes that should change the labels display
      //        renderer.refresh();
      //      }
    }

  }
}
