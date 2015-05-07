/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer;


import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolItem;
import org.geocraft.core.common.math.MathUtil;
import org.geocraft.internal.ui.volumeviewer.canvas.ViewCanvasImplementor;
import org.geocraft.internal.ui.volumeviewer.dialog.LightSettingsDialog;
import org.geocraft.ui.viewer.toolbar.SimpleToolBar;
import org.geocraft.ui.volumeviewer.action.EditLightSource;
import org.geocraft.ui.volumeviewer.action.SetOrthgraphicProjection;
import org.geocraft.ui.volumeviewer.action.SetPerspectiveProjection;
import org.geocraft.ui.volumeviewer.action.ShowPickLocation;


public class VolumeViewToolBar implements IVolumeViewerConstants {

  /** The 3D viewer associated with the toolbar. */
  private final VolumeViewer _viewer;

  /** The view canvas within the 3D viewer. */
  private final ViewCanvasImplementor _viewCanvas;

  /** The background color selector control. */
  //private final ColorSelector _colorSelector;

  /** The z-domain combo control. */
  private final Combo _zDomainCombo;

  /** The z-scaling combo control. */
  private final Combo _zScalingCombo;

  /** The projection method toggle control. */
  private final ToolItem _projectionToggle;

  /** The pick location toggle control. */
  private final ToolItem _showPickLocToggle;

  /** The action that sets the orthographic projection in the 3D viewer. */
  private final SetOrthgraphicProjection _setOrthographicProjection;

  /** The action that sets the perspective projection in the 3D viewer. */
  private final SetPerspectiveProjection _setPerspectiveProjection;

  /** The action that sets the turns on pick location in the 3D viewer. */
  private final ShowPickLocation _showPickLoc;

  /** The action that sets the turns off pick location in the 3D viewer. */
  private final ShowPickLocation _hidePickLoc;

  public VolumeViewToolBar(final SimpleToolBar toolbar, final VolumeViewer viewer, final ViewCanvasImplementor viewCanvas) {
    _viewer = viewer;
    _viewCanvas = viewCanvas;

    // Add a color selector for choosing the background color.
    //_colorSelector = toolbar.addColorSelector(_viewer.getBackgroundViewColor());
    //_colorSelector.getButton().setToolTipText("Select background color");
    //_colorSelector.addListener(new IPropertyChangeListener() {
    //
    //  /**
    //   * Invoked when a color is chosen in the color selector.
    //   * @param event the property change event.
    //   */
    //  public void propertyChange(final PropertyChangeEvent event) {
    //    final RGB newColor = _colorSelector.getColorValue();
    //    _viewer.setBackgroundViewColor(newColor);
    //  }
    //});

    // Add a combo for selecting the z-domain.
    _zDomainCombo = toolbar.addCombo("Domain",
        new String[] { VolumeViewZDomain.TIME.getText(), VolumeViewZDomain.DEPTH.getText() });
    _zDomainCombo.select(0);
    _zDomainCombo.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        final String zDomainStr = _zDomainCombo.getText();
        final VolumeViewZDomain zDomain = VolumeViewZDomain.lookup(zDomainStr);
        if (zDomain != null) {
          _viewer.setCurrentDomain(zDomain.getDomain());
        }
      }

    });

    // Add a combo for selecting the z-scaling.
    _zScalingCombo = toolbar.addCombo("Z-Scaling", new String[] { ".001", ".01", ".1", ".5", "1", "2", "5", "10", "20",
        "50", "100" });
    _zScalingCombo.select(4);
    _zScalingCombo.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        final String zScalingStr = _zScalingCombo.getText();
        final double zScaling = Double.parseDouble(zScalingStr);
        _viewer.setExaggeration(zScaling);
      }

    });

    // Add a toggle for setting the projection method.
    _setOrthographicProjection = new SetOrthgraphicProjection(_viewCanvas);
    _setPerspectiveProjection = new SetPerspectiveProjection(_viewCanvas);
    _projectionToggle = toolbar.addToggleButton(_setOrthographicProjection, _setPerspectiveProjection);

    // Add a toggle for turning on/off the pick location.
    _hidePickLoc = new ShowPickLocation(_viewCanvas, false);
    _showPickLoc = new ShowPickLocation(_viewCanvas, true);
    _showPickLocToggle = toolbar.addToggleButton(_showPickLoc, _hidePickLoc, false);

    // Add a button for editing the light source.
    final Action lightAction = new EditLightSource(_viewer);
    toolbar.addPushButton(lightAction);

    toolbar.addPushButton(new Action() {

      @Override
      public void run() {
        new LightSettingsDialog(_viewer).open();
      }
    });

    toolbar.getToolBar().pack(true);
  }

  //public void setBackgroundColor(final RGB color) {
  //_colorSelector.setColorValue(color);
  //_viewer.setBackgroundViewColor(color);
  //}

  public void setZDomain(final VolumeViewZDomain zDomain) {
    final String[] items = _zDomainCombo.getItems();
    for (int i = 0; i < items.length; i++) {
      final String domain = items[i];
      if (domain.equals(zDomain.toString())) {
        _zDomainCombo.select(i);
        _viewer.setCurrentDomain(zDomain.getDomain());
        break;
      }
    }
  }

  public void setZScaling(final double zScaling) {
    final String[] items = _zScalingCombo.getItems();
    for (int i = 0; i < items.length; i++) {
      final double scaling = Double.parseDouble(items[i]);
      if (MathUtil.isEqual(scaling, zScaling)) {
        _zScalingCombo.select(i);
        _viewer.setExaggeration(zScaling);
        break;
      }
    }
  }

  public void setProjection(final ProjectionMode projection) {
    switch (projection) {
      case ORTHOGRAPHIC:
        _projectionToggle.setSelection(false);
        _setOrthographicProjection.run();
        break;
      case PERSPECTIVE:
        _projectionToggle.setSelection(true);
        _setPerspectiveProjection.run();
        break;
    }
  }

  public void setShowPickLocation(final boolean showPickLoc) {
    _showPickLocToggle.setSelection(showPickLoc);
    if (showPickLoc) {
      _showPickLoc.run();
    } else {
      _hidePickLoc.run();
    }
  }
}
