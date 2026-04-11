/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.ui.volumeviewer;


import java.util.concurrent.Callable;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbenchPartSite;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.rendering.backend.TextureHandle;
import org.geocraft.core.rendering.scene.SceneNode;
import org.geocraft.ui.volumeviewer.renderer.util.SceneText;
import org.geocraft.ui.volumeviewer.renderer.util.SceneText.Alignment;
import org.joml.Vector3f;


public interface IVolumeViewer extends IVolumeViewerConstants {

  void setMessageText(String string);

  void showSettingsDialog(SceneNode spatial);

  void setPreferences(final String currentCenter, final String projectionMode, final RGB selColor);

  int getMaximumTextureSize();

  void setCurrentDomain(Domain currentDomain);

  void addToScene(SceneNode renderedS);

  void makeDirty();

  void setSelectedRenderer(Object renderer);

  void centerOnSpatial(SceneNode... targets);

  void enqueueGLTask(Callable<?> exe);

  void removePropertyChangeListener(IPropertyChangeListener listener);

  void addPropertyChangeListener(IPropertyChangeListener listener);

  void showWireover(SceneNode spatial);

  void removeWireover(SceneNode spatial);

  void cleanupTexture(TextureHandle tex);

  SceneText createSceneText(String name, String text, Alignment alignment);

  Vector3f getPickLocation();

  void mapSpatial(SceneNode spatial, Object renderer);

  SceneNode getSelectedSpatial();

  void setSelectedSpatial(final SceneNode selected, final Vector3f pickLoc);

  IWorkbenchPartSite getSite();

  SceneNode[] getNodes();

}
