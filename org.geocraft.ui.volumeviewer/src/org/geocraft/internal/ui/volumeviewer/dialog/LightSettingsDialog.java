/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.internal.ui.volumeviewer.dialog;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.geocraft.internal.ui.volumeviewer.canvas.ViewCanvasImplementor;
import org.geocraft.ui.common.FormLayoutHelper;
import org.geocraft.ui.volumeviewer.VolumeViewer;
import org.geocraft.ui.volumeviewer.dialog.VolumeSimpleSettingsDialog;

import com.ardor3d.math.MathUtils;


/**
 * Settings dialog for the Sun light direction.
 */
public class LightSettingsDialog extends VolumeSimpleSettingsDialog {

  /** The azimuth text field. */
  private Text _azimuthText;

  /** The elevation text field. */
  private Text _elevationText;

  /** The azimuth slider. */
  private Slider _azimuthSlider;

  /** The elevation slider. */
  private Slider _elevationSlider;

  /** The current azimuth value. */
  private final int _azimuth;

  /** The current elevation value. */
  private final int _elevation;

  /** The view canvas implementor. */
  private final ViewCanvasImplementor _viewImpl;

  /**
   * The constructor
   * @param shell the parent shell for the dialog
   * @param registry the canvas registry
   * @param viewImpl the view canvas implementor
   */
  public LightSettingsDialog(final VolumeViewer viewer) {
    super(viewer.getShell(), viewer, "Sun orientation");
    _viewImpl = viewer.getCanvasImplementor();
    _azimuth = Math.round((float) (_viewImpl.getSunAzimuth() * MathUtils.RAD_TO_DEG));
    _elevation = Math.round((float) (_viewImpl.getSunElevation() * (float) MathUtils.RAD_TO_DEG));
  }

  @Override
  protected void createPanel(final Composite parent) {
    final Composite mainPanel = new Composite(parent, SWT.NONE);
    mainPanel.setLayout(new FormLayout());
    _azimuthSlider = getSlider(mainPanel, 0, 360, _azimuth);
    _elevationSlider = getSlider(mainPanel, 0, 90, _elevation);
    _azimuthText = getTextField(mainPanel, _azimuthSlider);
    _elevationText = getTextField(mainPanel, _elevationSlider);

    final Label azimuthStartL = new Label(mainPanel, SWT.RIGHT);
    azimuthStartL.setText("Azimuth    0");
    azimuthStartL.setLayoutData(new FormLayoutHelper().getData(0, 0, 0, 100, 0, 10, 0, 30));
    _azimuthSlider.setLayoutData(new FormLayoutHelper().getData(0, 110, 80, -40, 0, 10, 0, 30));
    final Label azimuthEndL = new Label(mainPanel, SWT.NONE);
    azimuthEndL.setText("360");
    azimuthEndL.setLayoutData(new FormLayoutHelper().getData(80, -30, 80, 0, 0, 10, 0, 30));
    _azimuthText.setLayoutData(new FormLayoutHelper().getData(80, 0, 100, 0, 0, 10, 0, 30));

    final Label elevationStartL = new Label(mainPanel, SWT.RIGHT);
    elevationStartL.setText("Elevation    0");
    elevationStartL.setLayoutData(new FormLayoutHelper().getData(0, 0, 0, 100, 0, 50, 0, 70));
    _elevationSlider.setLayoutData(new FormLayoutHelper().getData(0, 110, 80, -40, 0, 50, 0, 70));
    final Label elevationEndL = new Label(mainPanel, SWT.NONE);
    elevationEndL.setText("90");
    elevationEndL.setLayoutData(new FormLayoutHelper().getData(80, -30, 80, 0, 0, 50, 0, 70));
    _elevationText.setLayoutData(new FormLayoutHelper().getData(80, 0, 100, 0, 0, 50, 0, 70));

    initHandlers();
  }

  /**
   * Build and return the slider.
   * @param start the start value
   * @param end the end value
   * @param delta the delta value
   * @return the slider
   */
  private Slider getSlider(final Composite parent, final int start, final int end, final int value) {
    final Slider slider = new Slider(parent, SWT.NULL);
    slider.setValues(value, start, end + 1, 1, 1, 10);
    return slider;
  }

  /**
   * Build and return the text field.
   * @param slider the slider corresponding to the text field
   * @return the text field
   */
  private Text getTextField(final Composite parent, final Slider slider) {
    final Text valueField = new Text(parent, SWT.BORDER | SWT.SINGLE);
    valueField.setText(slider.getSelection() + "");
    return valueField;
  }

  /**
   * Initialize the listeners.
   */
  private void initHandlers() {
    final SelectionListener listener = new SelectionAdapter() {

      @Override
      @SuppressWarnings("unused")
      public void widgetSelected(final SelectionEvent evt) {
        _viewImpl.setSunAzimuth(_azimuthSlider.getSelection() * MathUtils.DEG_TO_RAD);
        _viewImpl.setSunElevation(_elevationSlider.getSelection() * MathUtils.DEG_TO_RAD);
        _azimuthText.setText(_azimuthSlider.getSelection() + "");
        _elevationText.setText(_elevationSlider.getSelection() + "");
      }
    };
    _azimuthSlider.addSelectionListener(listener);
    _elevationSlider.addSelectionListener(listener);

    _azimuthText.addSelectionListener(new SelectionAdapter() {

      @Override
      @SuppressWarnings("unused")
      public void widgetDefaultSelected(final SelectionEvent e) {
        try {
          final int value = Integer.parseInt(_azimuthText.getText());
          if (value >= 0 && value <= 360) {
            _azimuthSlider.setSelection(value);
            _viewImpl.setSunAzimuth(value * MathUtils.DEG_TO_RAD);
          } else {
            _azimuthText.setText(_azimuthSlider.getSelection() + "");
          }
        } catch (final Exception ex) {
          _azimuthText.setText(_azimuthSlider.getSelection() + "");
        }
      }
    });
    _elevationText.addSelectionListener(new SelectionAdapter() {

      @Override
      @SuppressWarnings("unused")
      public void widgetDefaultSelected(final SelectionEvent e) {
        try {
          final int value = Integer.parseInt(_elevationText.getText());
          if (value >= 0 && value <= 90) {
            _elevationSlider.setSelection(value);
            _viewImpl.setSunElevation(value * MathUtils.DEG_TO_RAD);
          } else {
            _elevationText.setText(_elevationSlider.getSelection() + "");
          }
        } catch (final Exception ex) {
          _elevationText.setText(_elevationSlider.getSelection() + "");
        }
      }
    });
  }
}
