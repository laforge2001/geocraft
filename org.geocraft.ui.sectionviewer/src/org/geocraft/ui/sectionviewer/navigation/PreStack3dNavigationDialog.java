/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.navigation;


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.TraceAxisKey;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.model.seismic.TraceSection;
import org.geocraft.core.model.seismic.PreStack3d.StorageOrder;
import org.geocraft.core.model.seismic.TraceSection.SectionType;
import org.geocraft.ui.common.GridLayoutHelper;
import org.geocraft.ui.sectionviewer.ISectionViewer;
import org.geocraft.ui.sectionviewer.factory.PreStack3dSectionFactory;


/**
 * This class defines the dialog used to navigate thru <code>PreStack3d</code> volumes
 * in the section viewer.
 * <p>
 * It contains slider bars for inlines, crosslines and offsets, as well as fields
 * for specifying their increments.
 * <p>
 * It also contains fields for setting the horizontal and vertical
 * scaling (e.g. traces/inch and inches/sec, respectively).
 */
public class PreStack3dNavigationDialog extends AbstractNavigationDialog {

  /** The prestack volume from which to get the inline,xline ranges. */
  private final PreStack3d _prestack;

  /** The section viewer to update with the selected sections. */
  private final ISectionViewer _viewer;

  /** The text for specifying traces-per-inch. */
  private Text _tpiText;

  /** The text for specifying inches-per-second. */
  private Text _ipsText;

  private Combo _orderCombo;

  /** The button used to toggle on an inline section. */
  private Button _inlineButton;

  /** The button used to toggle on an xline section. */
  private Button _xlineButton;

  /** The button used to toggle on an offset section. */
  private Button _offsetButton;

  /** The label displaying the current inline. */
  private Text _inlineText;

  /** The label displaying the current xline. */
  private Text _xlineText;

  /** The label displaying the current offset. */
  private Text _offsetText;

  /** The spinner used to specify the inline increment. */
  private Spinner _inlineStepSpinner;

  /** The spinner used to specify the xline increment. */
  private Spinner _xlineStepSpinner;

  /** The spinner used to specify the offset increment. */
  private Spinner _offsetStepSpinner;

  /** The slider for selecting the desired inline. */
  private Slider _inlineSlider;

  /** The slider for selecting the desired xline. */
  private Slider _xlineSlider;

  /** The slider for selecting the desired offset. */
  private Slider _offsetSlider;

  /** The check box for auto-updating on inline,xline changes. */
  private Button _autoUpdateButton;

  public PreStack3dNavigationDialog(final Shell shell, final PreStack3d prestack3d, final ISectionViewer viewer) {
    super(shell, "PreStack3d Navigation: " + prestack3d.getDisplayName());
    _prestack = prestack3d;
    _viewer = viewer;
  }

  @Override
  protected void createPanel(final Composite parent) {
    parent.setLayout(GridLayoutHelper.createLayout(2, false));

    float horizontalScale = _viewer.getHorizontalDisplayScale();
    float verticalScale = _viewer.getVerticalDisplayScale();
    Group resolutionGroup = createGroup(parent, "Resolution");
    _tpiText = addLabeledText(resolutionGroup, "Traces/Inch", horizontalScale);
    Domain zDomain = _prestack.getZDomain();
    if (zDomain.equals(Domain.TIME)) {
      _ipsText = addLabeledText(resolutionGroup, "Inches/Second", verticalScale);
    } else if (zDomain.equals(Domain.DISTANCE)) {
      Unit zUnit = UnitPreferences.getInstance().getVerticalDistanceUnit();
      if (zUnit.equals(Unit.FOOT)) {
        _ipsText = addLabeledText(resolutionGroup, "Inches/Kilofoot", verticalScale);
      } else {
        _ipsText = addLabeledText(resolutionGroup, "Inches/Kilometer", verticalScale);
      }
    }
    Listener resolutionListener = new Listener() {

      public void handleEvent(final Event e) {
        if (e.keyCode == SWT.CR || e.keyCode == NUMERIC_ENTER) {
          String tpiStr = _tpiText.getText().trim();
          String ipsStr = _ipsText.getText().trim();
          float tpiScale = 100f;
          float ipsScale = 0.001f;
          StringBuilder errors = new StringBuilder("");
          try {
            tpiScale = Float.parseFloat(tpiStr);
            if (tpiScale < 1) {
              errors.append("Horizontal scale cannot be less than 1");
            }
          } catch (NumberFormatException e1) {
            errors.append("Invalid horizontal scale: " + tpiStr + "\n");
          }
          try {
            ipsScale = Float.parseFloat(ipsStr);
            if (ipsScale > MAX_VERTICAL_SCALE) {
              errors.append("Vertical scale cannot be greater than " + MAX_VERTICAL_SCALE);
            }
          } catch (NumberFormatException e1) {
            errors.append("Invalid vertical scale: " + ipsStr + "\n");
          }
          if (errors.length() == 0) {
            updateScales(tpiScale, ipsScale);
          } else {
            MessageDialog.openError(getShell(), "Scale Error", errors.toString());
          }
        }
      }

    };
    _tpiText.addListener(SWT.KeyDown, resolutionListener);
    _ipsText.addListener(SWT.KeyDown, resolutionListener);
    createLabel(resolutionGroup, "Note: Use \'Enter\' key to apply resolution changes");

    Group navigationGroup = createGroup(parent, "Navigation", 2);
    _autoUpdateButton = createToggleButton(navigationGroup, true, 2);
    _autoUpdateButton.setText("Auto-Update on Inline,Xline or Offset changes.");
    _autoUpdateButton.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        if (_autoUpdateButton.getSelection()) {
          updateViewer();
          getButton(IDialogConstants.CANCEL_ID).setEnabled(true);
        } else {
          getButton(IDialogConstants.CANCEL_ID).setEnabled(false);
        }
      }

    });

    Label orderLabel = new Label(navigationGroup, SWT.NONE);
    orderLabel.setText("Order");
    orderLabel.setLayoutData(GridLayoutHelper.createLayoutData(false, true, SWT.FILL, SWT.FILL, 1, 1));
    _orderCombo = new Combo(navigationGroup, SWT.READ_ONLY);
    _orderCombo.addListener(SWT.Selection, new Listener() {

      @Override
      public void handleEvent(final Event event) {
        if (_autoUpdateButton.getSelection()) {
          updateViewer();
        }
      }

    });
    StorageOrder[] orders = new StorageOrder[6];
    orders[0] = StorageOrder.INLINE_XLINE_OFFSET_Z;
    orders[1] = StorageOrder.INLINE_OFFSET_XLINE_Z;
    orders[2] = StorageOrder.XLINE_INLINE_OFFSET_Z;
    orders[3] = StorageOrder.XLINE_OFFSET_INLINE_Z;
    orders[4] = StorageOrder.OFFSET_INLINE_XLINE_Z;
    orders[5] = StorageOrder.OFFSET_XLINE_INLINE_Z;
    String[] values = new String[6];
    for (int i = 0; i < 6; i++) {
      values[i] = orders[i].getName();
    }
    _orderCombo.setItems(values);
    _orderCombo.select(0);
    _orderCombo.setLayoutData(GridLayoutHelper.createLayoutData(true, true, SWT.FILL, SWT.FILL, 1, 1));

    // Create the inline group composite.
    final Group inlineGroup = createGroup(navigationGroup, "Inline");
    _inlineButton = createToggleButton(inlineGroup, false, 1);
    _inlineText = createCurrentText(inlineGroup, _prestack.getInlineStart());
    _inlineStepSpinner = createStepSpinner(inlineGroup, 10, _prestack.getNumInlines() - 1);
    createStartLabel(inlineGroup, _prestack.getInlineStart());
    _inlineSlider = createSlider(inlineGroup, 0, _prestack.getNumInlines() - 1);
    createEndLabel(inlineGroup, _prestack.getInlineEnd());

    // Create the xline group composite.
    final Group xlineGroup = createGroup(navigationGroup, "Xline");
    _xlineButton = createToggleButton(xlineGroup, false, 1);
    _xlineText = createCurrentText(xlineGroup, _prestack.getXlineStart());
    _xlineStepSpinner = createStepSpinner(xlineGroup, 10, _prestack.getNumXlines() - 1);
    createStartLabel(xlineGroup, _prestack.getXlineStart());
    _xlineSlider = createSlider(xlineGroup, 0, _prestack.getNumXlines() - 1);
    createEndLabel(xlineGroup, _prestack.getXlineEnd());

    // Create the offset group composite.
    final Group offsetGroup = createGroup(navigationGroup, "Offset");
    _offsetButton = createToggleButton(offsetGroup, false, 1);
    _offsetText = createCurrentText(offsetGroup, _prestack.getOffsetStart());
    _offsetStepSpinner = createStepSpinner(offsetGroup, 1, _prestack.getNumOffsets() - 1);
    createStartLabel(offsetGroup, _prestack.getOffsetStart());
    _offsetSlider = createSlider(offsetGroup, 0, _prestack.getNumOffsets() - 1);
    createEndLabel(offsetGroup, _prestack.getOffsetEnd());

    // Add selection listener for the inline toggle button.
    _inlineButton.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        if (_autoUpdateButton.getSelection()) {
          updateViewer();
        }
      }

    });

    // Add selection listener for the xline toggle button.
    _xlineButton.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        if (_autoUpdateButton.getSelection()) {
          updateViewer();
        }
      }

    });

    // Add selection listener for the offset toggle button.
    _offsetButton.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        if (_autoUpdateButton.getSelection()) {
          updateViewer();
        }
      }

    });

    _inlineText.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(final KeyEvent event) {
        if (event.keyCode == SWT.CR || event.keyCode == NUMERIC_ENTER) {
          if (_inlineButton.getSelection()) {
            String inlineText = _inlineText.getText().trim();
            boolean error = false;
            try {
              float inline = Float.parseFloat(inlineText);
              int index = (int) ((inline - _prestack.getInlineStart()) / _prestack.getInlineDelta());
              if (index >= 0 && index < _prestack.getNumInlines()) {
                _inlineSlider.setSelection(index);
                updateViewer();
              } else {
                error = true;
              }
            } catch (NumberFormatException e1) {
              error = true;
            }
            if (error) {
              MessageDialog.openError(PreStack3dNavigationDialog.this.getShell(), "Navigation Error",
                  "Invalid Inline #: " + inlineText);
            }
          }
        }
      }

    });

    _xlineText.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(final KeyEvent event) {
        if (event.keyCode == SWT.CR || event.keyCode == NUMERIC_ENTER) {
          if (_xlineButton.getSelection()) {
            String xlineText = _xlineText.getText().trim();
            boolean error = false;
            try {
              float xline = Float.parseFloat(xlineText);
              int index = (int) ((xline - _prestack.getXlineStart()) / _prestack.getXlineDelta());
              if (index >= 0 && index < _prestack.getNumXlines()) {
                _xlineSlider.setSelection(index);
                updateViewer();
              } else {
                error = true;
              }
            } catch (NumberFormatException e1) {
              error = true;
            }
            if (error) {
              MessageDialog.openError(PreStack3dNavigationDialog.this.getShell(), "Navigation Error",
                  "Invalid Xline #: " + xlineText);
            }
          }
        }
      }
    });

    _offsetText.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(final KeyEvent event) {
        if (event.keyCode == SWT.CR || event.keyCode == NUMERIC_ENTER) {
          if (_offsetButton.getSelection()) {
            String offsetText = _offsetText.getText().trim();
            boolean error = false;
            try {
              float offset = Float.parseFloat(offsetText);
              int index = (int) ((offset - _prestack.getOffsetStart()) / _prestack.getOffsetDelta());
              if (index >= 0 && index < _prestack.getNumOffsets()) {
                _offsetSlider.setSelection(index);
                updateViewer();
              } else {
                error = true;
              }
            } catch (NumberFormatException e1) {
              error = true;
            }
            if (error) {
              MessageDialog.openError(PreStack3dNavigationDialog.this.getShell(), "Navigation Error",
                  "Invalid Offset #: " + offsetText);
            }
          }
        }
      }
    });

    // Add listener for the inline slider.
    _inlineSlider.addListener(SWT.MouseUp, new Listener() {

      public void handleEvent(final Event event) {
        if (_autoUpdateButton.getSelection()) {
          updateViewer();
        }
      }
    });

    // Add listener for the xline slider.
    _xlineSlider.addListener(SWT.MouseUp, new Listener() {

      public void handleEvent(final Event event) {
        if (_autoUpdateButton.getSelection()) {
          updateViewer();
        }
      }

    });

    // Add selection listener for the offset slider.
    _offsetSlider.addListener(SWT.MouseUp, new Listener() {

      public void handleEvent(final Event event) {
        if (_autoUpdateButton.getSelection()) {
          updateViewer();
        }
      }

    });

    // Add listener for the inline step spinner.
    _inlineStepSpinner.addListener(SWT.MouseUp, new Listener() {

      public void handleEvent(final Event event) {
        _inlineSlider.setIncrement(_inlineStepSpinner.getSelection());
        if (_autoUpdateButton.getSelection()) {
          updateViewer();
        }
      }

    });

    // Add listener for the xline step spinner.
    _xlineStepSpinner.addListener(SWT.MouseUp, new Listener() {

      public void handleEvent(final Event event) {
        _xlineSlider.setIncrement(_xlineStepSpinner.getSelection());
        if (_autoUpdateButton.getSelection()) {
          updateViewer();
        }
      }

    });

    // Add listener for the xline step spinner.
    _offsetStepSpinner.addListener(SWT.MouseUp, new Listener() {

      public void handleEvent(final Event event) {
        _offsetSlider.setIncrement(_offsetStepSpinner.getSelection());
        if (_autoUpdateButton.getSelection()) {
          updateViewer();
        }
      }

    });

    TraceSection section = _viewer.getTraceSection();
    if (section != null) {
      SectionType type = section.getSectionType();
      float inline = section.getTraceAxisKeyValue(0, TraceAxisKey.INLINE);
      float xline = section.getTraceAxisKeyValue(0, TraceAxisKey.XLINE);
      float offset = section.getTraceAxisKeyValue(0, TraceAxisKey.OFFSET);
      int inlineIndex = Math.round((inline - _prestack.getInlineStart()) / _prestack.getInlineDelta());
      int xlineIndex = Math.round((xline - _prestack.getXlineStart()) / _prestack.getXlineDelta());
      int offsetIndex = Math.round((offset - _prestack.getOffsetStart()) / _prestack.getOffsetDelta());
      _inlineText.setText("" + inline);
      _xlineText.setText("" + xline);
      _offsetText.setText("" + offset);
      _inlineSlider.setSelection(inlineIndex);
      _xlineSlider.setSelection(xlineIndex);
      _offsetSlider.setSelection(offsetIndex);
      switch (type) {
        case INLINE_SECTION:
          _inlineButton.setSelection(true);
          _xlineButton.setSelection(false);
          _offsetButton.setSelection(false);
          break;
        case XLINE_SECTION:
          _inlineButton.setSelection(false);
          _xlineButton.setSelection(true);
          _offsetButton.setSelection(false);
          break;
        case OFFSET_SECTION:
          _inlineButton.setSelection(false);
          _xlineButton.setSelection(false);
          _offsetButton.setSelection(true);
          break;
        case INLINE_XLINE_GATHER:
          _inlineButton.setSelection(true);
          _xlineButton.setSelection(true);
          _offsetButton.setSelection(false);
          break;
        case INLINE_OFFSET_GATHER:
          _inlineButton.setSelection(true);
          _xlineButton.setSelection(false);
          _offsetButton.setSelection(true);
          break;
        case XLINE_OFFSET_GATHER:
          _inlineButton.setSelection(false);
          _xlineButton.setSelection(true);
          _offsetButton.setSelection(true);
          break;
        case INLINE_XLINE_OFFSET_TRACE:
          _inlineButton.setSelection(true);
          _xlineButton.setSelection(true);
          _offsetButton.setSelection(true);
          break;
        case IRREGULAR:
          _inlineButton.setSelection(false);
          _xlineButton.setSelection(false);
          _offsetButton.setSelection(false);
          break;
      }
    }
  }

  private void updateViewer() {
    float inline = _prestack.getInlineStart() + _inlineSlider.getSelection() * _prestack.getInlineDelta();
    _inlineText.setText("" + inline);
    float xline = _prestack.getXlineStart() + _xlineSlider.getSelection() * _prestack.getXlineDelta();
    _xlineText.setText("" + xline);
    float offset = _prestack.getOffsetStart() + _offsetSlider.getSelection() * _prestack.getOffsetDelta();
    _offsetText.setText("" + offset);
    boolean inlineOn = _inlineButton.getSelection();
    boolean xlineOn = _xlineButton.getSelection();
    boolean offsetOn = _offsetButton.getSelection();
    int inlineDecimation = Math.max(1, _inlineStepSpinner.getSelection());
    int xlineDecimation = Math.max(1, _xlineStepSpinner.getSelection());
    int offsetDecimation = Math.max(1, _offsetStepSpinner.getSelection());
    String orderName = _orderCombo.getItem(_orderCombo.getSelectionIndex());
    StorageOrder order = StorageOrder.lookupByName(orderName);
    if (inlineOn && !xlineOn && !offsetOn) {
      TraceSection section = PreStack3dSectionFactory.createInlineSection(_prestack, order, inline, xlineDecimation,
          offsetDecimation);
      updateViewer(section);
    } else if (!inlineOn && xlineOn && !offsetOn) {
      TraceSection section = PreStack3dSectionFactory.createXlineSection(_prestack, order, xline, inlineDecimation,
          offsetDecimation);
      updateViewer(section);
    } else if (!inlineOn && !xlineOn && offsetOn) {
      TraceSection section = PreStack3dSectionFactory.createOffsetSection(_prestack, order, inlineDecimation,
          xlineDecimation, offset);
      updateViewer(section);
    } else if (inlineOn && xlineOn && !offsetOn) {
      TraceSection section = PreStack3dSectionFactory.createInlineXlineGather(_prestack, inline, xline,
          offsetDecimation);
      updateViewer(section);
    } else if (inlineOn && !xlineOn && offsetOn) {
      TraceSection section = PreStack3dSectionFactory.createInlineOffsetSection(_prestack, inline, offset,
          xlineDecimation);
      updateViewer(section);
    } else if (!inlineOn && xlineOn && offsetOn) {
      TraceSection section = PreStack3dSectionFactory.createXlineOffsetSection(_prestack, xline, offset,
          inlineDecimation);
      updateViewer(section);
    } else if (inlineOn && xlineOn && offsetOn) {
      TraceSection section = PreStack3dSectionFactory.createInlineXlineOffsetSection(_prestack, inline, xline, offset);
      updateViewer(section);
    }
  }

  private void updateViewer(final TraceSection section) {
    _viewer.setTraceSection(section);
  }

  protected void updateScales(final float tpiScale, final float ipsScale) {
    _viewer.setScales(tpiScale, ipsScale);
  }
}
