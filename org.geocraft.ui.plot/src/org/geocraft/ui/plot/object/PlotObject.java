/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.ui.plot.object;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.ui.plot.attribute.PointProperties;
import org.geocraft.ui.plot.defs.ObjectType;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.defs.RenderLevel;
import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.plot.model.IModelSpace;


/**
 * The abstract base case for a plot object.
 * The classes derived from this include <i>PlotPoint</i>
 * and <i>PlotShape</i>.
 */
public abstract class PlotObject implements IPlotObject {

  /** The object type. */
  protected ObjectType _objectType;

  /** The object name. */
  protected String _name;

  /** The object point properties. */
  protected PointProperties _pointProperties;

  /** The group in which the object is included. */
  protected IPlotLayer _group;

  /** The model in which the object is included. */
  protected IModelSpace _model;

  /** The plot render level. */
  protected RenderLevel _renderLevel;

  /** The visibility status of the object. */
  protected boolean _isVisible;

  /** The editable status of the object. */
  protected boolean _isEditable;

  /** The selectable status of the object. */
  protected boolean _isSelectable;

  /** The selected status of the object. */
  protected boolean _isSelected;

  /** The in-motion status of the object. */
  protected boolean _isInMotion;

  /** The update blocking status of the object. */
  protected boolean _isUpdateBlocked;

  /** The tree pop-up actions registered with the object. */
  protected List<Action> _actions;

  /**
   * Constructs a plot object of the given type with the given name.
   * @param type the object type (<i>Point</i> or <i>Shape</i>).
   * @param name the object name.
   */
  public PlotObject(final ObjectType type, final String name) {
    _objectType = type;
    setName(name);
    setVisible(true);
    setEditable(true);
    setSelectable(true);
    setSelected(false);
    setInMotion(false);
    setRenderLevel(RenderLevel.STANDARD);
    unblockUpdate();
    _pointProperties = new PointProperties();
    _actions = Collections.synchronizedList(new ArrayList<Action>());
  }

  /**
   * Constructs a plot object.
   * @param type the object type.
   * @param name the object name.
   * @param textProps the text properties.
   * @param pointProps the point properties.
   */
  public PlotObject(final ObjectType type, final String name, final PointProperties pointProps) {
    _objectType = type;
    setName(name);
    setVisible(true);
    setEditable(true);
    setSelectable(true);
    setSelected(false);
    setInMotion(false);
    setRenderLevel(RenderLevel.STANDARD);
    unblockUpdate();
    //_textProperties = new TextProperties(textProps.getFont(), textProps.getColor(), TextAnchor.CENTER);
    //_textProperties.addPropertyChangeListener(this);
    _pointProperties = new PointProperties(pointProps.getStyle(), pointProps.getColor(), pointProps.getSize());
    //_pointProperties.addPropertyChangeListener(this);
    _actions = Collections.synchronizedList(new ArrayList<Action>());
  }

  public ObjectType getObjectType() {
    return _objectType;
  }

  public String getName() {
    return _name;
  }

  public void setName(final String name) {
    _name = name;
    updated();
  }

  public IPlotLayer getLayer() {
    return _group;
  }

  public void setLayer(final IPlotLayer group) {
    _group = group;
    updated();
  }

  public IModelSpace getModelSpace() {
    return _model;
  }

  public void setModelSpace(final IModelSpace model) {
    _model = model;
  }

  public RenderLevel getRenderLevel() {
    return _renderLevel;
  }

  public void setRenderLevel(final RenderLevel renderLevel) {
    _renderLevel = renderLevel;
    updated();
  }

  public PointProperties getPointProperties() {
    return _pointProperties;
  }

  public PointStyle getPointStyle() {
    return _pointProperties.getStyle();
  }

  public int getPointSize() {
    return _pointProperties.getSize();
  }

  public RGB getPointColor() {
    return _pointProperties.getColor();
  }

  public void setPointStyle(final PointStyle style) {
    _pointProperties.setStyle(style);
    updated();
  }

  public void setPointSize(final int size) {
    _pointProperties.setSize(size);
    updated();
  }

  public void setPointColor(final RGB rgb) {
    _pointProperties.setColor(rgb);
    updated();
  }

  public boolean isVisible() {
    return _isVisible;
  }

  public void setVisible(final boolean visible) {
    _isVisible = visible;
    updated();
  }

  public boolean isEditable() {
    return _isEditable;
  }

  public void setEditable(final boolean editable) {
    _isEditable = editable;
  }

  public boolean isSelectable() {
    return _isSelectable;
  }

  public void setSelectable(final boolean selectable) {
    _isSelectable = selectable;
  }

  public boolean isSelected() {
    return _isSelected;
  }

  public void select() {
    setSelected(true);
  }

  public void deselect() {
    setSelected(false);
  }

  public void setSelected(final boolean selected) {
    boolean update = _isSelected != selected;
    _isSelected = selected;
    if (!update) {
      return;
    }
    if (_isSelected) {
      selected();
    } else {
      deselected();
    }
  }

  public boolean isInMotion() {
    return _isInMotion;
  }

  public void setInMotion(final boolean inMotion) {
    _isInMotion = inMotion;
  }

  public void blockUpdate() {
    _isUpdateBlocked = true;
  }

  public void unblockUpdate() {
    _isUpdateBlocked = false;
  }

  public boolean isUpdateBlocked() {
    return _isUpdateBlocked;
  }

  public void addPopupAction(final Action action) {
    _actions.add(action);
  }

  public Action[] getPopupActions() {
    Action[] actions = new Action[_actions.size()];
    for (int i = 0; i < actions.length; i++) {
      actions[i] = _actions.get(i);
    }
    return actions;
  }

  public void dispose() {
    //_textProperties.dispose();
    _pointProperties.dispose();
    _actions.clear();
  }
}
