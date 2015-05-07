/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.internal.ui.volumeviewer.dialog;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.geocraft.core.model.datatypes.Coordinate;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.event.CursorLocation;
import org.geocraft.core.model.event.CursorLocation.TimeOrDepth;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.core.service.message.Topic;
import org.geocraft.internal.ui.volumeviewer.canvas.ViewCanvasImplementor;
import org.geocraft.ui.common.FormLayoutHelper;
import org.geocraft.ui.volumeviewer.VolumeViewer;
import org.geocraft.ui.volumeviewer.dialog.VolumeSimpleSettingsDialog;


/**
 * Settings dialog for the cursor location.
 */
public class CursorDialog extends VolumeSimpleSettingsDialog {

  /** The logger. */
  private static ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(CursorDialog.class);

  /** The x text field. */
  private Text _xText;

  /** The y text field. */
  private Text _yText;

  /** The z text field. */
  private Text _zText;

  /** The x slider. */
  private Slider _xSlider;

  /** The y slider. */
  private Slider _ySlider;

  /** The z slider. */
  private Slider _zSlider;

  /** The cursor x start value. */
  private final double _startx;

  /** The cursor y start value. */
  private final double _starty;

  /** The cursor z start value. */
  private final double _startz;

  /** The difference between the maximum and the minimum values that the sliders can have. */
  private final int _maximum;

  /** The encapsulated cursor. */
  private final Cursor _cursor;

  /** The view canvas implementor. */
  private final ViewCanvasImplementor _viewImpl;

  /** The sliders middle value, half the _maximum value. */
  private int _middle;

  /**
   * The constructor
   * @param shell the parent shell for the dialog
   * @param registry the volume canvas registry
   * @param viewImpl the view canvas implementor
   */
  public CursorDialog(final VolumeViewer viewer, final Cursor cursor, final int maximum) {
    super(viewer.getShell(), viewer, "Cursor location");
    _viewImpl = viewer.getCanvasImplementor();
    _cursor = cursor;
    _startx = _cursor.getX();
    _starty = _cursor.getY();
    _startz = _cursor.getZ();
    _maximum = maximum;
  }

  @Override
  protected void createPanel(final Composite parent) {
    final Composite mainPanel = new Composite(parent, SWT.NONE);
    mainPanel.setLayout(new FormLayout());
    _middle = _maximum / 2;
    _xSlider = getSlider(mainPanel, 0, _maximum, _middle);
    _ySlider = getSlider(mainPanel, 0, _maximum, _middle);
    _zSlider = getSlider(mainPanel, 0, _maximum, _middle);

    _xText = getTextField(mainPanel, _startx, _xSlider);
    _yText = getTextField(mainPanel, _starty, _ySlider);
    _zText = getTextField(mainPanel, _startz, _zSlider);

    final Label xL = new Label(mainPanel, SWT.RIGHT);
    xL.setText("X");
    xL.setLayoutData(new FormLayoutHelper().getData(0, 0, 0, 20, 0, 10, 0, 30));
    _xSlider.setLayoutData(new FormLayoutHelper().getData(0, 30, 80, -10, 0, 10, 0, 30));
    _xText.setLayoutData(new FormLayoutHelper().getData(80, 0, 100, 0, 0, 10, 0, 30));

    final Label yL = new Label(mainPanel, SWT.RIGHT);
    yL.setText("Y");
    yL.setLayoutData(new FormLayoutHelper().getData(0, 0, 0, 20, 0, 50, 0, 70));
    _ySlider.setLayoutData(new FormLayoutHelper().getData(0, 30, 80, -10, 0, 50, 0, 70));
    _yText.setLayoutData(new FormLayoutHelper().getData(80, 0, 100, 0, 0, 50, 0, 70));

    final Label zL = new Label(mainPanel, SWT.RIGHT);
    zL.setText("Z");
    zL.setLayoutData(new FormLayoutHelper().getData(0, 0, 0, 20, 0, 90, 0, 110));
    _zSlider.setLayoutData(new FormLayoutHelper().getData(0, 30, 80, -10, 0, 90, 0, 110));
    _zText.setLayoutData(new FormLayoutHelper().getData(80, 0, 100, 0, 0, 90, 0, 110));
  }

  /**
   * Build and return the slider.
   * @param parent the parent composite
   * @param start the start value_reg
   * @param end the end value
   * @param delta the delta value
   * @return the slider
   */
  private Slider getSlider(final Composite parent, final int start, final int end, final int value) {
    final Slider slider = new Slider(parent, SWT.NULL);
    slider.setValues(value, start, end, 1, 1, 10);
    slider.addSelectionListener(new SelectionAdapter() {

      @Override
      @SuppressWarnings("unused")
      public void widgetSelected(final SelectionEvent e) {
        updateData();
      }
    });
    return slider;
  }

  /**
   * Update the text fields and publish a cursor event.
   */
  private void updateData() {
    final double x = _startx + _xSlider.getSelection() - _middle;
    final double y = _starty + _ySlider.getSelection() - _middle;
    final double z = _startz + _zSlider.getSelection() - _middle;
    _cursor.setPosition(x, y, z, _viewImpl.getExaggeration());
    _xText.setText(x + "");
    _yText.setText(y + "");
    _zText.setText(z + "");
    final VolumeViewer volumeViewer = (VolumeViewer) _viewImpl.getViewer();
    TimeOrDepth timeDepth = TimeOrDepth.NONE;
    final Domain domain = volumeViewer.getCurrentDomain();
    if (domain != null) {
      if (domain == Domain.TIME) {
        timeDepth = TimeOrDepth.TIME;
      } else if (domain == Domain.DISTANCE) {
        timeDepth = TimeOrDepth.DEPTH;
      }
    }
    final TimeOrDepth timeOrDepth = timeDepth;

    ServiceProvider.getMessageService().publish(
        Topic.CURSOR_LOCATION,
        new CursorLocation(new Coordinate(new Point3d(_cursor.getX(), _cursor.getY(), _cursor.getZ()), _cursor
            .getCoordinateSystem()), timeOrDepth, "CursorDialog"));
    _viewImpl.makeDirty();
  }

  /**
   * Build and return the text field.
   * @param parent the parent composite
   * @param start the start / minimum value
   * @param slider the slider corresponding to the text field
   * @return the text field
   */
  private Text getTextField(final Composite parent, final double start, final Slider slider) {
    final Text valueField = new Text(parent, SWT.BORDER | SWT.SINGLE);
    valueField.setText(start + slider.getSelection() - _middle + "");

    valueField.addSelectionListener(new SelectionAdapter() {

      @Override
      @SuppressWarnings("unused")
      public void widgetDefaultSelected(final SelectionEvent e) {
        try {
          final float value = Float.parseFloat(valueField.getText());
          final int sliderValue = (int) Math.round(value - start + _middle);
          if (sliderValue >= slider.getMinimum() && sliderValue <= slider.getMaximum()) {
            slider.setSelection(sliderValue);
            updateData();
          }
        } catch (final Exception ex) {
          LOGGER.warn("Invalid numeric value " + valueField.getText(), ex);
        }
      }
    });
    return valueField;
  }
}
