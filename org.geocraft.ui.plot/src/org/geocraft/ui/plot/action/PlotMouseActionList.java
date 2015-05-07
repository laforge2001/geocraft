/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.geocraft.ui.plot.action.point.DeletePointAction;
import org.geocraft.ui.plot.action.point.EndPointMotionAction;
import org.geocraft.ui.plot.action.point.PointMotionAction;
import org.geocraft.ui.plot.action.point.StartPointMotionAction;
import org.geocraft.ui.plot.action.shape.DeleteShapeAction;
import org.geocraft.ui.plot.action.shape.DeselectShapeAction;
import org.geocraft.ui.plot.action.shape.EndShapeMotionAction;
import org.geocraft.ui.plot.action.shape.SelectShapeAction;
import org.geocraft.ui.plot.action.shape.ShapeMotionAction;
import org.geocraft.ui.plot.action.shape.StartShapeMotionAction;
import org.geocraft.ui.plot.action.zoom.EndZoomMotionAction;
import org.geocraft.ui.plot.action.zoom.PanAction;
import org.geocraft.ui.plot.action.zoom.StartZoomMotionAction;
import org.geocraft.ui.plot.action.zoom.ZoomMotionAction;
import org.geocraft.ui.plot.defs.ActionMaskType;


public class PlotMouseActionList {

  protected String _name;

  protected List<IPlotMouseAction> _actions;

  private static PlotMouseActionList _defaultObjectActions;

  private static PlotMouseActionList _defaultCursorActions;

  private static PlotMouseActionList _defaultPanActions;

  private static PlotMouseActionList _defaultZoomInOutActions;

  private static PlotMouseActionList _defaultZoomWindowActions;

  private static PlotMouseActionList _defaultPolygonCreateActions;

  /**
   * Constructs an instance of PlotouseActionMap.
   * @param name the action map name.
   */
  public PlotMouseActionList(final String name) {
    setName(name);
    _actions = Collections.synchronizedList(new ArrayList<IPlotMouseAction>());
  }

  /**
   * Gets the action map name.
   * @return the action map name.
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the action map name.
   * @param name the action map name to set.
   */
  public void setName(final String name) {
    _name = name;
  }

  /**
   * Adds an action to the action map.
   * @param action the action to add.
   */
  public void addAction(final IPlotMouseAction action) {
    _actions.add(action);
  }

  /**
   * Removes an action from the action map.
   * @param action the action to remove.
   */
  public void removeAction(final IPlotMouseAction action) {
    _actions.remove(action);
  }

  /**
   * Gets the specified action from the action map.
   * @param index the index of the action to get.
   * @return the index-th action from the action map.
   */
  public IPlotMouseAction getAction(final int index) {
    return _actions.get(index);
  }

  /**
   * Gets the list of actions from the action map.
   * @return the list of list.
   */
  public IPlotMouseAction[] getActions() {
    return _actions.toArray(new IPlotMouseAction[0]);
  }

  /**
   * Gets the default set of zoom in/out list.
   * @return the default set of zoom in/out list.
   */
  public static PlotMouseActionList getDefaultZoomInOutActions() {
    if (_defaultZoomInOutActions == null) {
      PlotMouseActionList list = new PlotMouseActionList("DefaultZoomInOutActions");
      PlotActionMask mask;
      //      mask = new PlotActionMask(ActionMask.MOUSE_CLICKED, 1, 1, SWT.BUTTON1);
      //      list.addAction(new ZoomAction(mask, "Cursor Zoom In", "Zoom in by a factor of 2.", 2));
      //      mask = new PlotActionMask(ActionMask.MOUSE_CLICKED, 2, 1, SWT.BUTTON2);
      //      list.addAction(new ZoomAction(mask, "Cursor Zoom Out", "Zoom out by a factor of 2.", 0.5));
      _defaultZoomInOutActions = list;
    }
    return _defaultZoomInOutActions;
  }

  /**
   * Gets the default set of pan actions.
   * @return the default set of pan actions.
   */
  public static PlotMouseActionList getDefaultPanActions() {
    if (_defaultPanActions == null) {
      PlotMouseActionList list = new PlotMouseActionList("DefaultPanActions");
      PlotActionMask mask;
      mask = new PlotActionMask(ActionMaskType.MOUSE_MOVE, 0, 0, 0);
      list.addAction(new PanAction(mask));
      mask = new PlotActionMask(ActionMaskType.MOUSE_MOVE, 0, 0, SWT.BUTTON1);
      list.addAction(new PanAction(mask));
      mask = new PlotActionMask(ActionMaskType.MOUSE_UP, 1, 1, SWT.BUTTON1);
      list.addAction(new PanAction(mask));
      _defaultPanActions = list;
    }
    return _defaultPanActions;
  }

  /**
   * Gets the default set of zoom window list.
   * @return the default set of zoom window list.
   */
  public static PlotMouseActionList getDefaultZoomWindowActions() {
    if (_defaultZoomWindowActions == null) {
      PlotMouseActionList list = new PlotMouseActionList("DefaultZoomWindowActions");
      PlotActionMask mask;
      mask = new PlotActionMask(ActionMaskType.MOUSE_DOWN, 1, 1, SWT.SHIFT);
      list.addAction(new StartZoomMotionAction(mask));
      mask = new PlotActionMask(ActionMaskType.MOUSE_MOVE, 0, 0, SWT.BUTTON1 | SWT.SHIFT);
      list.addAction(new ZoomMotionAction(mask));
      mask = new PlotActionMask(ActionMaskType.MOUSE_UP, 1, 1, SWT.BUTTON1 | SWT.SHIFT);
      list.addAction(new EndZoomMotionAction(mask));
      mask = new PlotActionMask(ActionMaskType.MOUSE_UP, 1, 1, SWT.BUTTON1 | SWT.SHIFT);
      list.addAction(new ZoomMotionAction(mask));
      mask = new PlotActionMask(ActionMaskType.MOUSE_UP, 1, 1, SWT.BUTTON1);
      list.addAction(new EndZoomMotionAction(mask));
      mask = new PlotActionMask(ActionMaskType.MOUSE_UP, 1, 1, SWT.BUTTON1);
      list.addAction(new EndZoomMotionAction(mask));
      _defaultZoomWindowActions = list;
    }
    return _defaultZoomWindowActions;
  }

  /**
   * Creates the default set of object list.
   * @return the default set of object list.
   */
  public static PlotMouseActionList getDefaultObjectActions() {
    if (_defaultObjectActions == null) {
      PlotMouseActionList list = new PlotMouseActionList("DefaultObjectActions");
      PlotActionMask mask;

      // Point add: mouse button#1 down
      mask = new PlotActionMask(ActionMaskType.MOUSE_DOWN, 1, 1, 0);
      //list.addAction(new AddPointAction(mask, PointInsertionMode.LAST));
      // Point delete: mouse button#2 down with shift
      mask = new PlotActionMask(ActionMaskType.MOUSE_DOWN, 2, 1, SWT.SHIFT);
      list.addAction(new DeletePointAction(mask));
      // PointGroup delete: mouse button#2 down with control
      mask = new PlotActionMask(ActionMaskType.MOUSE_DOWN, 2, 1, SWT.CTRL);
      list.addAction(new DeleteShapeAction(mask));

      // PointGroup deselect: mouse button #2 down
      mask = new PlotActionMask(ActionMaskType.MOUSE_DOWN, 2, 1, 0);
      list.addAction(new DeselectShapeAction(mask));
      // PointGroup select: mouse button#1 down with shift
      mask = new PlotActionMask(ActionMaskType.MOUSE_DOWN, 1, 1, SWT.SHIFT);
      list.addAction(new SelectShapeAction(mask, true));
      // PointGroup start move: mouse button#1 down with shift
      mask = new PlotActionMask(ActionMaskType.MOUSE_DOWN, 1, 1, SWT.SHIFT);
      list.addAction(new StartShapeMotionAction(mask));
      // PointGroup move: mouse motion with mouse button#1 and shift
      mask = new PlotActionMask(ActionMaskType.MOUSE_MOVE, 0, 0, SWT.BUTTON1 | SWT.SHIFT);
      list.addAction(new ShapeMotionAction(mask));
      // PointGroup end move: mouse button#1 up with shift
      mask = new PlotActionMask(ActionMaskType.MOUSE_UP, 1, 1, SWT.BUTTON1 | SWT.SHIFT);
      list.addAction(new EndShapeMotionAction(mask));

      // Point start move: mouse button#1 down and control
      mask = new PlotActionMask(ActionMaskType.MOUSE_DOWN, 1, 1, SWT.CTRL);
      list.addAction(new StartPointMotionAction(mask));
      // Point move: mouse motion with mouse button#1 and control
      mask = new PlotActionMask(ActionMaskType.MOUSE_MOVE, 0, 0, SWT.BUTTON1 | SWT.CTRL);
      list.addAction(new PointMotionAction(mask));
      // Point end move: mouse button#1 up and control
      mask = new PlotActionMask(ActionMaskType.MOUSE_UP, 1, 1, SWT.BUTTON1 | SWT.CTRL);
      list.addAction(new EndPointMotionAction(mask));

      //        mask = new PlotActionMask(ActionMask.MOUSE_CLICKED, 3, 1, SWT.BUTTON3_MASK);
      //        list.addAction(new PlotPointKit.EditAction(mask));
      _defaultObjectActions = list;
    }
    return _defaultObjectActions;
  }

  //  public static PlotMouseActionList getDefaultPolygonCreateActions() {
  //    if (_defaultPolygonCreateActions == null) {
  //      PlotMouseActionList list = new PlotMouseActionList("DefineAOI");
  //      PlotActionMask mask;
  //      // Point add: button#1, single-click
  //      mask = new PlotActionMask(ActionMask.MOUSE_CLICKED, 1, 1, InputEvent.BUTTON1_MASK);
  //      list.addAction(new PlotPointKit.AddAction(mask, PointInsertionMode.Last));
  //      // PointGroup deselect: button#1, single-click, shift
  //      mask = new PlotActionMask(ActionMask.MOUSE_CLICKED, 1, 1, InputEvent.BUTTON1_MASK
  //          | InputEvent.SHIFT_MASK);
  //      list.addAction(new PlotShapeKit.DeselectAction(mask));
  //      _defaultPolygonCreateActions = list;
  //    }
  //    return _defaultPolygonCreateActions;
  //  }
}
