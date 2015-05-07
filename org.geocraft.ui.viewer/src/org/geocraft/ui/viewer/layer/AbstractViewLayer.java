/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.layer;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.IPropertySource;
import org.geocraft.ui.viewer.action.RemoveViewLayer;
import org.geocraft.ui.viewer.action.RenameViewLayer;
import org.geocraft.ui.viewer.layer.ViewLayerEvent.EventType;


/**
 * Defines an abstract implementation of a view layer.
 */
public abstract class AbstractViewLayer implements IViewLayer {

  /** The view layer name. */
  protected String _name;

  /** The view layer image. */
  protected Image _image;

  /** The view layer unique ID. */
  protected String _uniqueID;

  /** The popup actions available for the view layer. */
  protected List<IAction> _actions;

  /** The view ayer listeners. */
  protected List<ViewLayerListener> _listeners;

  /** The view layer background color. */
  protected Color _backgroundColor;

  /** The view layer visibility flag. */
  protected boolean _isVisible;

  /** The view layer alpha value (for transparency). */
  protected float _alpha;

  /** The checked status of the view layer. */
  protected boolean _checked = true;

  /** The parent layer. */
  protected IViewLayer _parentLayer;

  /** The list of child layers. */
  protected List<IViewLayer> _childLayers;

  /** The model of toggle group settings. */
  private ToggleGroupModel _toggleGroupModel;

  /** The current toggle group ID. */
  private int _toggleGroupId = 0;

  /**
   * The base constructor.
   * 
   * @param name the name of the view layer.
   * @param uniqueID the unique ID of the view layer.
   * @param allowsChildren <i>true</i> if the view layer allows children; <i>false</i> if not.
   * @param allowsRename <i>true</i> if the view layer allows renaming; <i>false</i> if not.
   * @param allowsDelete <i>true</i> if the view layer allows deletion; <i>false</i> if not.
   */
  public AbstractViewLayer(final String name, final String uniqueID, final boolean allowsChildren, final boolean allowsRename, final boolean allowsDelete) {
    _uniqueID = uniqueID;
    _actions = Collections.synchronizedList(new ArrayList<IAction>());
    _listeners = Collections.synchronizedList(new ArrayList<ViewLayerListener>());
    _childLayers = Collections.synchronizedList(new ArrayList<IViewLayer>());
    _toggleGroupModel = new ToggleGroupModel();
    _parentLayer = null;
    _backgroundColor = new Color(null, 0, 0, 0);
    if (allowsChildren) {
      _image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
    } else {
      _image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
    }
    _isVisible = true;
    _alpha = 1;
    if (allowsRename) {
      addAction(new RenameViewLayer(this));
    }
    if (allowsDelete) {
      addAction(new RemoveViewLayer(this, true));
    }
    setName(name);
    showReadoutInfo(false);
  }

  public String getUniqueID() {
    return _uniqueID;
  }

  public void addListener(final ViewLayerListener listener) {
    _listeners.add(listener);
  }

  public void removeListener(final ViewLayerListener listener) {
    _listeners.remove(listener);
  }

  public void addAction(final IAction action) {
    _actions.add(action);
  }

  public IAction[] getActions() {
    return _actions.toArray(new IAction[0]);
  }

  public Image getImage() {
    return _image;
  }

  public void setImage(final Image image) {
    _image = image;
    updated(EventType.LAYER_UPDATED);
  }

  public Color getBackgroundColor() {
    return _backgroundColor;
  }

  public String getName() {
    return _name;
  }

  public boolean isVisible() {
    return _isVisible;
  }

  public boolean isChecked() {
    return _checked;
  }

  public void setBackgroundColor(final RGB rgb) {
    Color backgroundColor = new Color(null, rgb);
    _backgroundColor.dispose();
    _backgroundColor = backgroundColor;
  }

  public void setName(final String name) {
    _name = name;
    updated(ViewLayerEvent.EventType.LAYER_RENAMED);
  }

  public void setTransparency(final float alpha) {
    _alpha = alpha;
    redraw();
    updated(ViewLayerEvent.EventType.LAYER_UPDATED);
  }

  public void setVisible(final boolean flag) {
    _isVisible = flag;
    _toggleGroupModel.setIncluded(_toggleGroupId, flag);
    updated(ViewLayerEvent.EventType.LAYER_UPDATED);
  }

  public void setChecked(final boolean checked) {
    _checked = checked;
    updated(ViewLayerEvent.EventType.LAYER_UPDATED);
  }

  public void setToggleGroup(final int toggleGroupId) {
    _toggleGroupId = toggleGroupId;
    //boolean flag = _isVisible && _toggleGroupModel.isIncluded(toggleGroupId);
    setVisible(_toggleGroupModel.isIncluded(toggleGroupId));
  }

  @Override
  public String toString() {
    return getName();
  }

  public String getToolTipText() {
    return "";
  }

  public void remove() {
    updated(ViewLayerEvent.EventType.LAYER_REMOVED);
  }

  /**
   * Fires a view layer event to the current listeners.
   * 
   * @param eventType the type of view layer event.
   */
  protected void updated(final ViewLayerEvent.EventType eventType) {
    // Create a new event.
    ViewLayerEvent event = new ViewLayerEvent(this, eventType);

    // Fire the event to the listeners.
    notifyListeners(event);
  }

  /**
   * Fires the given view layer event to the current listeners.
   * 
   * @param event the view layer event.
   */
  protected void notifyListeners(final ViewLayerEvent event) {
    ViewLayerListener[] listeners = new ViewLayerListener[_listeners.size()];
    for (int i = 0; i < listeners.length; i++) {
      listeners[i] = _listeners.get(i);
    }
    for (ViewLayerListener listener : listeners) {
      listener.viewLayerUpdated(event);
    }
  }

  public Object getAdapter(final Class adapter) {
    if (adapter.equals(IPropertySource.class)) {
      return new LayerPropertySource(getName(), "");
    }
    return null;
  }

  public void dispose() {
    // Dispose of the background color.
    _backgroundColor.dispose();

    // Clear the collections.
    _actions.clear();
    _listeners.clear();
  }

  public Object getUserObject() {
    return getName();
  }

  public ToggleGroupModel getToggleGroupModel() {
    return _toggleGroupModel;
  }

}
