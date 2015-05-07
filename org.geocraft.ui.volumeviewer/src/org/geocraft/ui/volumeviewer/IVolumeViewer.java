/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer;


import java.util.concurrent.Callable;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbenchPartSite;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.ui.volumeviewer.renderer.util.SceneText;
import org.geocraft.ui.volumeviewer.renderer.util.SceneText.Alignment;

import com.ardor3d.image.Texture;
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Spatial;


public interface IVolumeViewer extends IVolumeViewerConstants {

  //VolumeCanvasRegistry getRegistry();

  void setMessageText(String string);

  void showSettingsDialog(Spatial spatial);

  //void doRendererAction(PickRecord pickRecord);

  void setPreferences(final String currentCenter, final String projectionMode, final RGB selColor);

  int getMaximumTextureSize();

  void setCurrentDomain(Domain currentDomain);

  void addToScene(Spatial renderedS);

  void makeDirty();

  void setSelectedRenderer(Object renderer);

  void centerOnSpatial(Spatial... targets);

  void enqueueGLTask(Callable<?> exe);

  void removePropertyChangeListener(IPropertyChangeListener listener);

  void addPropertyChangeListener(IPropertyChangeListener listener);

  void showWireover(Spatial spatial);

  void removeWireover(Spatial spatial);

  void cleanupTexture(Texture tex);

  SceneText createSceneText(String name, String text, Alignment alignment);

  Vector3 getPickLocation();

  void mapSpatial(Spatial spatial, Object renderer);

  Spatial getSelectedSpatial();

  void setSelectedSpatial(final Spatial selected, final Vector3 pickLoc);

  IWorkbenchPartSite getSite();

  Spatial[] getNodes();

}
