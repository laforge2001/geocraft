/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer.renderer.aoi;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.geocraft.ui.plot.settings.FillPropertiesPanel;
import org.geocraft.ui.plot.settings.LinePropertiesPanel;
import org.geocraft.ui.plot.settings.PointPropertiesPanel;
import org.geocraft.ui.viewer.dialog.AbstractSettingsDialog;


public class SeismicSurvey3dAOIRendererDialog extends AbstractSettingsDialog {

  /** The dialog main panel. */
  private TabFolder _mainPanel;

  /** The volume renderer. */
  private final SeismicSurvey3dAOIRenderer _renderer;

  private PointPropertiesPanel _pointProperties;

  private FillPropertiesPanel _fillProperties;

  private LinePropertiesPanel _lineProperties;

  private boolean _showPointTab = false;

  public SeismicSurvey3dAOIRendererDialog(final Shell shell, final String title, final SeismicSurvey3dAOIRenderer renderer) {
    super(shell, title);
    _renderer = renderer;
  }

  @Override
  protected void createPanel(final Composite parent) {
    _mainPanel = new TabFolder(parent, SWT.TOP);
    if (_renderer.getPointProperties() != null) {
      _showPointTab = true;
    }

    if (_showPointTab) {
      TabItem pointTab = new TabItem(_mainPanel, SWT.NONE);
      _pointProperties = new PointPropertiesPanel(_mainPanel, SWT.NONE, _renderer.getPointProperties(), true);
      pointTab.setText("Point Properties");
      pointTab.setControl(_pointProperties);
    }

    TabItem fillTab = new TabItem(_mainPanel, SWT.NONE);
    _fillProperties = new FillPropertiesPanel(_mainPanel, SWT.NONE, _renderer.getFillProperties());
    fillTab.setText("Fill Properties");
    fillTab.setControl(_fillProperties);
    TabItem lineTab = new TabItem(_mainPanel, SWT.NONE);
    _lineProperties = new LinePropertiesPanel(_mainPanel, SWT.NONE, _renderer.getLineProperties());
    lineTab.setText("Line Properties");
    lineTab.setControl(_lineProperties);
  }

  @Override
  protected void applySettings() {
    if (_showPointTab) {
      _renderer.setPointProperties(_pointProperties.getProperties());
    }
    _renderer.setFillProperties(_fillProperties.getProperties());
    _renderer.setLineProperties(_lineProperties.getProperties());
  }

  @Override
  protected void undoSettings() {
    // TODO Auto-generated method stub

  }

}
