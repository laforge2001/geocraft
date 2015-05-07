/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.toolbar;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolItem;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.action.DisableBroadcast;
import org.geocraft.ui.viewer.action.DisablePanMode;
import org.geocraft.ui.viewer.action.DisableReception;
import org.geocraft.ui.viewer.action.DisableZoomWindow;
import org.geocraft.ui.viewer.action.EnableBroadcast;
import org.geocraft.ui.viewer.action.EnablePanMode;
import org.geocraft.ui.viewer.action.EnableReception;
import org.geocraft.ui.viewer.action.EnableZoomWindow;
import org.geocraft.ui.viewer.action.HideLayerModelTree;
import org.geocraft.ui.viewer.action.HomeView;
import org.geocraft.ui.viewer.action.PrintAction;
import org.geocraft.ui.viewer.action.ShowLayerModelTree;
import org.geocraft.ui.viewer.action.ZoomIn;
import org.geocraft.ui.viewer.action.ZoomOut;


/**
 * The tool bar of shared features, used by viewers.
 */
public class SharedToolBar extends SimpleToolBar {

  protected ToolItem _layerModel;

  protected ToolItem _broadcastButton;

  protected ToolItem _receptionButton;

  /**
   * The constructor.
   * @param viewer the viewer instance
   */
  public SharedToolBar(final Composite parent, final IViewer viewer, final boolean includeWindowZoom, final boolean includePan, final boolean broadcastReceive) {
    this(parent, viewer, includeWindowZoom, includePan, broadcastReceive, false);
  }

  /**
   * The constructor.
   * @param viewer the viewer instance
   */
  public SharedToolBar(final Composite parent, final IViewer viewer, final boolean includeWindowZoom, final boolean includePan, final boolean broadcastReceive, boolean includePrint) {
    super(parent);

    // Add a push button to zoom in.
    addPushButton(new ZoomIn(viewer));

    // Add a push button to zoom out.
    addPushButton(new ZoomOut(viewer));

    // Add a toggle button to enable/disable the zoom window.
    if (includeWindowZoom) {
      addToggleButton(new EnableZoomWindow(viewer), new DisableZoomWindow(viewer));
    }

    // Add a toggle button to enable/disable the panning mode.
    if (includePan) {
      addToggleButton(new EnablePanMode(viewer), new DisablePanMode(viewer));
    }

    // Add a push button to restore the default home view.
    addPushButton(new HomeView(viewer));

    if (includePrint) {
      addPushButton(new PrintAction(viewer));
    }

    if (broadcastReceive) {
      // Add a toggle button to enable/disable cursor broadcasting.
      _broadcastButton = addToggleButton(new EnableBroadcast(viewer), new DisableBroadcast(viewer), true);

      // Add a toggle button to enable/disable cursor reception.
      _receptionButton = addToggleButton(new EnableReception(viewer), new DisableReception(viewer), true);
    }

    // Add a toggle button to show/hide the layer model tree view.
    _layerModel = addToggleButton(new ShowLayerModelTree(viewer), new HideLayerModelTree(viewer), true);
  }

  public void setShowLayerModel(final boolean show) {
    _layerModel.setSelection(show);
    if (show) {
      _layerModel.setToolTipText("Layer model is shown");
    } else {
      _layerModel.setToolTipText("Layer model is hidden");
    }
  }

  public void setBroadcastStatus(final boolean broadcast) {
    if (_broadcastButton != null) {
      _broadcastButton.setSelection(broadcast);
    }
  }

  public void setReceptionStatus(final boolean reception) {
    if (_receptionButton != null) {
      _receptionButton.setSelection(reception);
    }
  }
}
