/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.layer;


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.model.datatypes.SpatialExtent;
import org.geocraft.ui.viewer.ReadoutInfo;


/**
 * The common interface for a view layer.
 * <p>
 * A view layer represents an element of a layered model.
 */
public interface IViewLayer extends IAdaptable {

  /**
   * Gets the view layer name.
   * 
   * @return the view layer name.
   */
  String getName();

  /**
   * Sets the view layer name.
   * 
   * @param name the view layer name.
   */
  void setName(String name);

  /**
   * Gets the view layer image.
   * 
   * @return the view layer image.
   */
  Image getImage();

  /**
   * Sets the view layer image.
   * 
   * @param image the view layer image.
   */
  void setImage(Image image);

  /**
   * Returns the unique ID of the view layer.
   * <p>
   * This must be unique within a viewer's layered model.
   * 
   * @return the unqiue ID.
   */
  String getUniqueID();

  /**
   * Returns the spatial extent of the object(s) contained
   * in the view layer.
   * 
   * @return the spatial extent; or <i>null</i> if none.
   */
  SpatialExtent getExtent();

  /**
   * Redraws the view layer.
   */
  void redraw();

  /**
   * Refreshes the view layer.
   */
  void refresh();

  /**
   * Gets the visibility of the view layer.
   * 
   * @return <i>true</i> if visible; <i>false</i> if not.
   */
  boolean isVisible();

  /**
   * Sets the visibility of the view layer.
   * 
   * @param visible <i>true</i> for visible; otherwise <i>false</i>.
   */
  void setVisible(boolean visible);

  /**
   * Adds a view layer listener.
   * <p>
   * If the listener has already been added, then no
   * action is taken.
   * 
   * @param listener the view layer listener to add.
   */
  void addListener(ViewLayerListener listener);

  /**
   * Removes a view layer listener.
   * <p>
   * If the listener has not yet been added, then no
   * action is taken.
   * 
   * @param listener the view layer listener to remove.
   */
  void removeListener(ViewLayerListener listener);

  /**
   * Gets the "checked" status of the layer in tree view.
   * 
   * @return <i>true</i> if checked; <i>false</i> if unchecked.
   */
  boolean isChecked();

  /**
   * Sets the "checked" flag of the layer in tree view.
   * 
   * @param checked <i>true</i> to check; <i>false</i> to uncheck.
   */
  void setChecked(boolean checked);

  /**
   * Adds an action to the view layer.
   * 
   * @param action the action to add.
   */
  void addAction(final IAction action);

  /**
   * Returns an array of the actions registered with the view layer.
   * 
   * @return an array of the registered actions.
   */
  IAction[] getActions();

  /**
   * Sets the RGB of the background color.
   * 
   * @param rgb the RGB of the background color to set.
   */
  void setBackgroundColor(RGB rgb);

  /**
   * Gets the RGB of the background color.
   * 
   * @return the RGB of the background color.
   */
  Color getBackgroundColor();

  /**
   * Sets the layer transparency (alpha).
   * 
   * @param alpha the alpha value.
   */
  void setTransparency(float alpha);

  /**
   * Gets the flag for showing readout info.
   * <p>
   * If <i>true</i>, the view layer is expected to provide additional
   * cursor location information. For example, a surface might provide
   * its depth.
   *
   * @return <i>true</i> to show readout info; otherwise <i>false</i>.
   */
  boolean showReadoutInfo();

  /**
   * Sets the flag for showing readout information.
   * <p>
   * If <i>true</i>, the view layer is expected to provide additional
   * cursor location information. For example, a surface might provide
   * its depth.
   *
   * @param show <i>true</i> to show readout info; otherwise <i>false</i>.
   */
  void showReadoutInfo(boolean show);

  /**
   * Returns the layer readout information at the given x,y screen coordinates.
   * 
   * @param x the x-coordinate.
   * @param y the y-coordinate.
   * @return the readout information.
   */
  ReadoutInfo getReadoutInfo(double x, double y);

  /**
   * Returns the tool tip for the view layer.
   * <p>
   * If none, then an empty string or <i>null</i> is returned.
   * 
   * @return the tool tip.
   */
  String getToolTipText();

  /**
   * Removes the layer from its view.
   */
  void remove();

  /**
   * Disposes of any resources associated with the layer.
   */
  void dispose();

  /**
   * Returns the user-defined object contained in the layer.
   */
  Object getUserObject();

  /**
   * Returns the toggle group model for the layer.
   * 
   * @return the toggle group model.
   */
  ToggleGroupModel getToggleGroupModel();

  /**
   * Sets the ID of the toggle group.
   * 
   * @param toggleGroupId the toggle group ID.
   */
  void setToggleGroup(final int toggleGroupId);
}
