/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer.renderer.polygonfeature;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.geocraft.ui.plot.settings.FillPropertiesPanel;
import org.geocraft.ui.plot.settings.LinePropertiesPanel;
import org.geocraft.ui.plot.settings.PointPropertiesPanel;
import org.geocraft.ui.viewer.dialog.AbstractSettingsDialog;


public class PolygonFeatureDialog extends AbstractSettingsDialog {

  /** The dialog main panel. */
  private TabFolder _mainPanel;

  /** The volume renderer. */
  private final PolygonFeatureRenderer _renderer;

  private PointPropertiesPanel _pointProperties;

  private FillPropertiesPanel _fillProperties;

  private LinePropertiesPanel _lineProperties;

  /**
   * @param title
   */
  public PolygonFeatureDialog(final Shell shell, String title, final PolygonFeatureRenderer renderer) {
    super(shell, title);
    _renderer = renderer;
  }

  /** 
   * @see org.geocraft.ui.viewer.dialog.AbstractSettingsDialog#createPanel(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void createPanel(final Composite parent) {
    _mainPanel = new TabFolder(parent, SWT.TOP);
    TabItem pointTab = new TabItem(_mainPanel, SWT.NONE);
    _pointProperties = new PointPropertiesPanel(_mainPanel, SWT.NONE, _renderer.getPointProperties(), true);
    pointTab.setText("Point Properties");
    pointTab.setControl(_pointProperties);

    TabItem fillTab = new TabItem(_mainPanel, SWT.NONE);
    _fillProperties = new FillPropertiesPanel(_mainPanel, SWT.NONE, _renderer.getFillProperties());
    fillTab.setText("Fill Properties");
    fillTab.setControl(_fillProperties);
    TabItem lineTab = new TabItem(_mainPanel, SWT.NONE);
    _lineProperties = new LinePropertiesPanel(_mainPanel, SWT.NONE, _renderer.getLineProperties());
    lineTab.setText("Line Properties");
    lineTab.setControl(_lineProperties);
  }

  /**
   * @see org.geocraft.ui.viewer.dialog.AbstractSettingsDialog#applySettings()
   */
  @Override
  protected void applySettings() {
    _renderer.setPointProperties(_pointProperties.getProperties());
    _renderer.setFillProperties(_fillProperties.getProperties());
    _renderer.setLineProperties(_lineProperties.getProperties());
  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.viewer.dialog.AbstractSettingsDialog#undoSettings()
   */
  @Override
  protected void undoSettings() {
    // TODO Auto-generated method stub

  }

}
