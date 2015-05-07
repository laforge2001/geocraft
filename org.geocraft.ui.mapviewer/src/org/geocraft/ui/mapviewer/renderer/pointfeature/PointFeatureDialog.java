/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer.renderer.pointfeature;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.geocraft.ui.plot.settings.PointPropertiesPanel;
import org.geocraft.ui.viewer.dialog.AbstractSettingsDialog;


public class PointFeatureDialog extends AbstractSettingsDialog {

  /** The dialog main panel. */
  private TabFolder _mainPanel;

  /** The point feature renderer. */
  private final PointFeatureRenderer _renderer;

  private PointPropertiesPanel _pointProperties;

  /**
   * @param title
   */
  public PointFeatureDialog(final Shell shell, String title, final PointFeatureRenderer renderer) {
    super(shell, title);
    _renderer = renderer;
  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.viewer.dialog.AbstractSettingsDialog#createPanel(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void createPanel(final Composite parent) {
    _mainPanel = new TabFolder(parent, SWT.TOP);
    TabItem pointTab = new TabItem(_mainPanel, SWT.NONE);
    _pointProperties = new PointPropertiesPanel(_mainPanel, SWT.NONE, _renderer.getPointProperties(), true);
    pointTab.setText("Point Properties");
    pointTab.setControl(_pointProperties);
  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.viewer.dialog.AbstractSettingsDialog#setSettings()
   */
  @Override
  protected void applySettings() {
    _renderer.setPointProperties(_pointProperties.getProperties());
  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.viewer.dialog.AbstractSettingsDialog#undoSettings()
   */
  @Override
  protected void undoSettings() {
    // TODO Auto-generated method stub

  }

}
