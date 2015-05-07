/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.ui.plot.object;


import java.beans.PropertyChangeListener;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.ui.plot.attribute.PointProperties;
import org.geocraft.ui.plot.defs.ObjectType;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.defs.RenderLevel;
import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.plot.model.IModelSpace;


/**
 * The interface for plot objects.
 * Plot objects come in two types: Points and Shapes.
 * Shapes include such things as lines, polylines, polygons, etc.,
 * and contain one or more points.
 */
public interface IPlotObject extends PropertyChangeListener {

  /**
   * Gets the object type.
   * This will be either <i>Point</i> or <i>Shape</i>.
   * @return the object type.
   */
  ObjectType getObjectType();

  /**
   * Gets the object name.
   * @return the object name.
   */
  String getName();

  /**
   * Sets the object name.
   * @param name the object name.
   */
  void setName(String name);

  /**
   * Gets the object point properties associated with the object.
   * The point properties will be used in rendering any points
   * contained in the object.
   * @return the object point properties.
   */
  PointProperties getPointProperties();

  /**
   * Gets the point style associated with the object.
   * This is a convenience method for the associated point properties.
   * @return the point style associated with the object.
   */
  PointStyle getPointStyle();

  /**
   * Gets the point color associated with the object.
   * This is a convenience method for the associated point properties.
   * @return the point color associated with the object.
   */
  RGB getPointColor();

  /**
   * Gets the point size associated with the object.
   * This is a convenience method for the associated point properties.
   * @return the point size associated with the object.
   */
  int getPointSize();

  /**
   * Sets the point style to associate with the object.
   * This is a convenience method for the associated point properties.
   * @param style the point style to associate with the object.
   */
  void setPointStyle(final PointStyle style);

  /**
   * Sets the point color (as RGB) to associate with the object.
   * This is a convenience method for the associated point properties.
   * @param color the point color to associate with the object.
   */
  void setPointColor(final RGB rgb);

  /**
   * Sets the point size to associate with the object.
   * This is a convenience method for the associated point properties.
   * @param size the point size to associate with the object.
   */
  void setPointSize(final int size);

  /**
   * Gets the plot layer in which the object is contained.
   * @return the plot layer in which the object is contained.
   */
  IPlotLayer getLayer();

  /**
   * Sets the plot layer in which the object is contained.
   * @param layer the plot layer in which the object is contained.
   */
  void setLayer(IPlotLayer layer);

  /**
   * Gets the plot model space with which the object is associated.
   * @return the plot model space with which the object is associated.
   */
  IModelSpace getModelSpace();

  /**
   * Sets the plot model space with which the object is to be associated.
   * @param model the plot model space with which the object is to be associated.
   */
  void setModelSpace(IModelSpace modelSpace);

  /**
   * Gets the object render level.
   * The render level defines the coarse order of rendering for plot objects.
   * @return the object render level.
   */
  RenderLevel getRenderLevel();

  /**
   * Sets the object render level.
   * The render level defines the coarse order of rendering for plot objects.
   * @param renderLevel the object render level.
   */
  void setRenderLevel(RenderLevel renderLevel);

  /**
   * Gets the visible status of the object.
   * @return <i>true</i> if visible; <i>false</i> if not.
   */
  boolean isVisible();

  /**
   * Sets the visible status of the object.
   * @param visible <i>true</i> if visible; <i>false</i> if not.
   */
  void setVisible(boolean visible);

  /**
   * Gets the editable status of the object.
   * Editable shapes can be moved and re-shaped.
   * @return <i>true</i> if editable; <i>false</i> if not.
   */
  boolean isEditable();

  /**
   * Sets the editable status of the object.
   * Editable shapes can be moved and re-shaped.
   * @param editable <i>true</i> if editable; <i>false</i> if not.
   */
  void setEditable(boolean editable);

  /**
   * Gets the selectable status of the object.
   * Selectable shapes can he moved, but not re-shaped.
   * @return <i>true</i> if selectable; <i>false</i> if not.
   */
  boolean isSelectable();

  /**
   * Sets the selectable status of the object.
   * Selectable shapes can he moved, but not re-shaped.
   * @param selectable <i>true</i> if selectable; <i>false</i> if not.
   */
  void setSelectable(boolean selectable);

  /**
   * Gets the selected status of the object.
   * @return <i>true</i> if selected; <i>false</i> if not.
   */
  boolean isSelected();

  /**
   * Sets the selected status of the object to <i>true</i>.
   * A property change event is fired to indicate the object
   * has been selected.
   */
  void select();

  /**
   * Sets the selected status of the object to <i>false</i>.
   * A property change event is fired to indicate the object
   * has been deselected.
   */
  void deselect();

  /**
   * Sets the selected status of the object.
   * Selected shapes can he moved, but not re-shaped.
   * @param selected <i>true</i> to select; <i>false</i> to deselect.
   */
  void setSelected(boolean selected);

  /**
   * Gets the in-motion status of the object.
   * @return <i>true</i> if in-motion; <i>false</i> if not.
   */
  boolean isInMotion();

  /**
   * Sets the in-motion status of the object.
   * @param inMotion <i>true</i> if in-motion; <i>false</i> if not.
   */
  void setInMotion(boolean inMotion);

  /**
   * Blocks the object from firing property change events.
   * This can be used along with <i>unblockUpdate</i> to
   * change several properties, and then fire a single
   * property change event.
   */
  void blockUpdate();

  /**
   * Unblocks the object from firing property change events.
   * This can be used along with <i>blockUpdate</i> to
   * change several properties, and then fire a single
   * property change event.
   */
  void unblockUpdate();

  /**
   * Gets the block-updates status of the object.
   * @return <i>true</i> if blocked; <i>false</i> if not.
   */
  boolean isUpdateBlocked();

  /**
   * Moves the object by delta-x, delta-y.
   * A property change event is fired to indicate the object
   * has been moved.
   * @param dx the delta-x.
   * @param dy the delta-y.
   */
  void moveBy(double dx, double dy);

  /**
   * Moves the object by delta-x, delta-y, delta-z.
   * A property change event is fired to indicate the object
   * has been moved.
   * @param dx the delta-x.
   * @param dy the delta-y.
   * @param dz the delta-z.
   */
  void moveBy(double dx, double dy, double dz);

  /**
   * Redraws the object.
   */
  void redraw();

  /**
   * Logic to run when the object is updated.
   */
  void updated();

  /**
   * Logic to run when the object is added.
   */
  void added();

  /**
   * Logic to run when the object is removed.
   */
  void removed();

  /**
   * Logic to run when the object is selected.
   */
  void selected();

  /**
   * Logic to run when the object is deselected.
   */
  void deselected();

  /**
   * Logic to run when the object is in motion.
   */
  void motion();

  /**
   * Logic to run when the object starts motion.
   */
  void motionStart();

  /**
   * Logic to run when the object ends motion.
   */
  void motionEnd();

  /**
   * Adds an action to the plot object.
   * This action can be anything, such as a renaming action,
   * a deletion action, etc. This action will appear in the
   * array returned by <i>getPopupActions</i>.
   * @param action the action to add.
   */
  void addPopupAction(Action action);

  /**
   * Returns an array of pop-up actions associated with the object.
   * These action can be anything, and may include such things
   * as renaming, deleting, etc.
   */
  Action[] getPopupActions();

  /**
   * Disposes of the point resources (colors, fonts, etc).
   */
  void dispose();
}