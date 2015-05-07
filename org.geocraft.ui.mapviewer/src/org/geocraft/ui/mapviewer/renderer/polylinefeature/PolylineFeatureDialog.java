/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.ui.mapviewer.renderer.polylinefeature;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.geocraft.ui.plot.settings.LinePropertiesPanel;
import org.geocraft.ui.viewer.dialog.AbstractSettingsDialog;


/**
 * Settings dialog for the seismic lines.
 */
public class PolylineFeatureDialog extends AbstractSettingsDialog {

  /** The dialog main panel. */
  private TabFolder _mainPanel;

  /** The volume renderer. */
  private final PolylineFeatureRenderer _renderer;

  private LinePropertiesPanel _lineProperties;

  /**
   * The constructor
   * @param renderer the volume renderer
   */
  public PolylineFeatureDialog(final Shell shell, String title, final PolylineFeatureRenderer renderer) {
    super(shell, title);
    _renderer = renderer;
  }

  @Override
  protected void createPanel(final Composite parent) {
    _mainPanel = new TabFolder(parent, SWT.TOP);
    if (_renderer.getLineProperties() != null) {
      TabItem lineTab = new TabItem(_mainPanel, SWT.NONE);
      _lineProperties = new LinePropertiesPanel(_mainPanel, SWT.NONE, _renderer.getLineProperties());
      lineTab.setText("Line Properties");
      lineTab.setControl(_lineProperties);
    }
  }

  @Override
  public void applySettings() {
    if (_lineProperties != null) {
      _renderer.setLineProperties(_lineProperties.getProperties());
    }
  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.viewer.dialog.AbstractSettingsDialog#undoSettings()
   */
  @Override
  protected void undoSettings() {
    // TODO Auto-generated method stub

  }

}
