/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer;


import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.geocraft.core.model.Model;
import org.geocraft.ui.viewer.layer.ILayeredModel;
import org.geocraft.ui.viewer.toolbar.SharedToolBar;
import org.geocraft.ui.viewer.toolbar.SimpleToolBar;


/**
 * The interface that describes the features that each viewer must have.
 */
public interface IViewer {

  /** The string constant name for the viewer folder containing grids. */
  public static final String GRID_FOLDER = "Grids";

  /** The string constant name for the viewer folder containing wells, well bores, logs, etc. */
  public static final String WELL_FOLDER = "Wells";

  /** The string constant name for the viewer folder containing faults. */
  public static final String FAULT_FOLDER = "Faults";

  /** The string constant name for the viewer folder containing culture. */
  public static final String CULTURE_FOLDER = "Culture";

  /** The string constant name for the viewer folder containing seismic datasets. */
  public static final String SEISMIC_FOLDER = "Seismic";

  /** The string constant name for the viewer folder containing point sets. */
  public static final String POINTSET_FOLDER = "Point Sets";

  /** The string constant name for the viewer folder containing areas/regions of interest. */
  public static final String AOI_ROI_FOLDER = "Areas/Regions of Interest";

  /**
   * Returns the composite container of the viewer.
   */
  Composite getComposite();

  /**
   * Get the model display properties of the viewer
   * @return
   */
  Model getViewerModel();

  /**
   * Returns the shared toolbar.
   * The shared toolbar contains the buttons and actions common to
   * most viewer (e.g. zoom, pan, cursor broadcast, etc).
   * @return the shared toolbar.
   */
  SharedToolBar getSharedToolBar();

  /**
   * Adds a toolbar to the plot viewer.
   * Users can be used to add custom items.
   * @return the toolbar.
   */
  SimpleToolBar addCustomToolBar();

  /**
   * Returns the tree view of the layer model.
   */
  TreeViewer getLayerViewer();

  /**
   * Performs a zoom-in action on the viewer.
   */
  void zoomIn();

  /**
   * Performs a zoom-out action on the viewer.
   */
  void zoomOut();

  /**
   * Turns on/off the windowed zoom functionality.
   * When windowed zoom in on, the mouse can be used to draw a window of the desired zoom.
   * @param enabled <i>true</i> to turn on the windowed zoom; <i>false</i> to turn off.
   */
  void zoomWindow(final boolean enabled);

  /**
   * Turns on/off the pan functionality.
   * When panning is on, the mouse can be used to drag the viewer on the screen.
   * @param enabled <i>true</i> to turn on the pan; <i>false</i> to turn off.
   */
  void pan(final boolean enabled);

  /**
   * Resets the viewer to it default (home) view.
   */
  void home();

  /**
   * Prints (or being the print preview process) for the viewer.
   */
  void print();

  /**
   * Returns the background color of the viewer.
   */
  RGB getBackgroundViewColor();

  /**
   * Sets the background color of the viewer.
   */
  void setBackgroundViewColor(final RGB color);

  /**
   * Sets the visibility of the layer tree associated with the viewer.
   * This is the programmatic method of setting the visibility. It can also
   * be done using the toggle button in the viewer's shared toolbar.
   * @param visible <i>true</i> to show the layer tree; <i>false</i> to hide it.
   */
  void setLayerTreeVisible(final boolean visible);

  /**
   * Sets the status of cursor broadcast for the viewer.
   * This is the programmatic method of setting the cursor broadcast status. It can also
   * be done using the toggle button in the viewer's shared toolbar.
   * @param broadcast <i>true</i> to turn on cursor broadcasting; <i>false</i> to turn it off.
   */
  void setCursorBroadcast(final boolean broadcast);

  /**
   * Sets the visibility of the cursor reception for the viewer.
   * This is the programmatic method of setting the cursor reception status. It can also
   * be done using the toggle button in the viewer's shared toolbar.
   * @param reception <i>true</i> to turn on cursor receiving; <i>false</i> to turn it off.
   */
  void setCursorReception(final boolean reception);

  /**
   * Sets the style of the cursor to display in the viewer canvas.
   * @param cursorStyle the predefined SWT cursor style.
   */
  void setCursorStyle(final int cursorStyle);

  /**
   * Adds objects to be rendered in the viewer.
   * The viewer will check to see if renderers exist for each of the objects.
   * If a renderer is found for an object, it will be rendered. Otherwise it
   * will be ignored. Implementers should log warnings if an object cannot be
   * rendered by a viewer.
   * @param objects the array of objects to add to the viewer.
   */
  void addObjects(final Object[] objects);

  /**
   * Removes objects rendered in the viewer.
   * The viewer will check to see if each of the objects is currently being
   * rendered in the viewer. If an object is, it will be removed. If not, then
   * the removal is simply ignored.
   * @param objects the array of objects to removed from the viewer.
   */
  void removeObjects(final Object[] objects);

  /**
   * Removes all objects rendered in the viewer.
   */
  void removeAllObjects();

  /**
   * Disposes of the viewer resources.
   */
  void dispose();

  /**
   * Get the renderers of the objects rendered in the viewer
   * @return List of viewer's renderers.
   */
  IRenderer[] getRenderers();

  /**
   * Add a renderer to the viewer.
   * 
   * @param klass the qualified class of the renderer to be added.
   * @param props the properties of the renderer to be added.
   * @param uniqueId the unique ID of the rendered entity.
   */
  void addRenderer(String klass, Map<String, String> props, String uniqueId);

  /**
   * Restore display properties from the viewer's model.
   */
  void updateFromModel();

  /**
   * Returns the layered model for the viewer.
   * 
   * @return the layered model, or <i>null</i> if no model exists.
   */
  ILayeredModel getLayeredModel();
}
