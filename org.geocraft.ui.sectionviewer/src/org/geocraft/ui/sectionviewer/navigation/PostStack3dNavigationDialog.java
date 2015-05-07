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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.TraceAxisKey;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.TraceSection;
import org.geocraft.core.model.seismic.TraceSection.SectionType;
import org.geocraft.ui.common.GridLayoutHelper;
import org.geocraft.ui.sectionviewer.ISectionViewer;
import org.geocraft.ui.sectionviewer.factory.PostStack3dSectionFactory;


/**
 * This class defines the dialog used to navigate thru <code>PostStack3d</code> volumes
 * in the section viewer.
 * <p>
 * It contains slider bars for inlines and crosslines, as well as fields for specifying
 * their increments.
 * <p>
 * It also contains fields for setting the horizontal and vertical
 * scaling (e.g. traces/inch and inches/sec, respectively).
 */
public class PostStack3dNavigationDialog extends AbstractNavigationDialog {

  /** The poststack volume from which to get the inline,xline ranges. */
  private final PostStack3d _poststack;

  /** The section viewer to update with the selected sections. */
  private final ISectionViewer _viewer;

  /** The text for specifying traces-per-inch. */
  private Text _tpiText;

  /** The text for specifying inches-per-second. */
  private Text _ipsText;

  /** The button used to toggle on an inline section. */
  private Button _inlineButton;

  /** The button used to toggle on an xline section. */
  private Button _xlineButton;

  /** The label displaying the current inline. */
  private Text _inlineText;

  /** The label displaying the current xline. */
  private Text _xlineText;

  /** The spinner used to specify the inline increment. */
  private Spinner _inlineStepSpinner;

  /** The spinner used to specify the xline increment. */
  private Spinner _xlineStepSpinner;

  /** The Slider for selecting the desired inline. */
  private Slider _inlineSlider;

  /** The Slider for selecting the desired xline. */
  private Slider _xlineSlider;

  /** The check box for auto-updating on inline,xline changes. */
  private Button _autoUpdateButton;

  public PostStack3dNavigationDialog(final Shell shell, final PostStack3d poststack3d, final ISectionViewer viewer) {
    super(shell, "PostStack3d Navigation: " + poststack3d.getDisplayName());
    _poststack = poststack3d;
    _viewer = viewer;
  }

  @Override
  protected void createPanel(final Composite parent) {
    parent.setLayout(GridLayoutHelper.createLayout(1, false));

    float horizontalScale = _viewer.getHorizontalDisplayScale();
    float verticalScale = _viewer.getVerticalDisplayScale();
    Group resolutionGroup = createGroup(parent, "Resolution");
    _tpiText = addLabeledText(resolutionGroup, "Traces/Inch", horizontalScale);
    Domain zDomain = _poststack.getZDomain();
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
    _autoUpdateButton.setText("Auto-Update on Inline or Xline changes.");
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

    // Create the inline group composite.
    Group inlineGroup = createGroup(navigationGroup, "Inline");
    _inlineButton = createToggleButton(inlineGroup, false, 1);
    _inlineText = createCurrentText(inlineGroup, _poststack.getInlineStart());
    _inlineStepSpinner = createStepSpinner(inlineGroup, 1, _poststack.getNumInlines() - 1);
    createStartLabel(inlineGroup, _poststack.getInlineStart());
    _inlineSlider = createSlider(inlineGroup, 0, _poststack.getNumInlines() - 1);
    createEndLabel(inlineGroup, _poststack.getInlineEnd());

    // Create the xline group composite.
    Group xlineGroup = createGroup(navigationGroup, "Xline");
    _xlineButton = createToggleButton(xlineGroup, false, 1);
    _xlineText = createCurrentText(xlineGroup, _poststack.getXlineStart());
    _xlineStepSpinner = createStepSpinner(xlineGroup, 1, _poststack.getNumXlines() - 1);
    createStartLabel(xlineGroup, _poststack.getXlineStart());
    _xlineSlider = createSlider(xlineGroup, 0, _poststack.getNumXlines() - 1);
    createEndLabel(xlineGroup, _poststack.getXlineEnd());

    // Add selection listener for the inline toggle button.
    _inlineButton.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        _xlineButton.setSelection(!_inlineButton.getSelection());
        if (_autoUpdateButton.getSelection()) {
          updateViewer();
        }
      }

    });

    // Add selection listener for the xline toggle button.
    _xlineButton.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        _inlineButton.setSelection(!_xlineButton.getSelection());
        if (_autoUpdateButton.getSelection()) {
          updateViewer();
        }
      }

    });

    _inlineText.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(final KeyEvent e) {
        if (e.keyCode == SWT.CR || e.keyCode == NUMERIC_ENTER) {
          if (_inlineButton.getSelection()) {
            String inlineText = _inlineText.getText().trim();
            boolean error = false;
            try {
              float inline = Float.parseFloat(inlineText);
              int index = (int) ((inline - _poststack.getInlineStart()) / _poststack.getInlineDelta());
              if (index >= 0 && index < _poststack.getNumInlines()) {
                _inlineSlider.setSelection(index);
                int xlineDecimation = _xlineStepSpinner.getSelection();
                TraceSection section = PostStack3dSectionFactory.createInlineSection(_poststack, inline,
                    xlineDecimation);
                updateViewer(section);
              } else {
                error = true;
              }
            } catch (Exception ex) {
              error = true;
            }
            if (error) {
              MessageDialog.openError(PostStack3dNavigationDialog.this.getShell(), "Navigation Error",
                  "Invalid Inline #: " + inlineText);
            }
          }
        }

      }

    });

    _xlineText.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(final KeyEvent e) {
        if (e.keyCode == SWT.CR || e.keyCode == NUMERIC_ENTER) {
          if (_xlineButton.getSelection()) {
            String xlineText = _xlineText.getText().trim();
            boolean error = false;
            try {
              float xline = Float.parseFloat(xlineText);
              int index = (int) ((xline - _poststack.getXlineStart()) / _poststack.getXlineDelta());
              if (index >= 0 && index < _poststack.getNumXlines()) {
                _xlineSlider.setSelection(index);
                int inlineDecimation = _inlineStepSpinner.getSelection();
                TraceSection section = PostStack3dSectionFactory
                    .createXlineSection(_poststack, xline, inlineDecimation);
                updateViewer(section);
              } else {
                error = true;
              }
            } catch (NumberFormatException ex) {
              error = true;
            }
            if (error) {
              MessageDialog.openError(PostStack3dNavigationDialog.this.getShell(), "Navigation Error",
                  "Invalid Xline #: " + xlineText);
            }
          }
        }
      }

    });

    // Add listener for the inline Slider.
    _inlineSlider.addListener(SWT.MouseUp, new Listener() {

      public void handleEvent(final Event event) {
        float inline = _poststack.getInlineStart() + _inlineSlider.getSelection() * _poststack.getInlineDelta();
        _inlineText.setText("" + inline);
        if (_inlineButton.getSelection() && _autoUpdateButton.getSelection()) {
          int xlineDecimation = _xlineStepSpinner.getSelection();
          TraceSection section = PostStack3dSectionFactory.createInlineSection(_poststack, inline, xlineDecimation);
          updateViewer(section);
        }
      }

    });

    // Add listener for the xline Slider.
    _xlineSlider.addListener(SWT.MouseUp, new Listener() {

      public void handleEvent(final Event event) {
        float xline = _poststack.getXlineStart() + _xlineSlider.getSelection() * _poststack.getXlineDelta();
        _xlineText.setText("" + xline);
        if (_xlineButton.getSelection() && _autoUpdateButton.getSelection()) {
          int inlineDecimation = _inlineStepSpinner.getSelection();
          TraceSection section = PostStack3dSectionFactory.createXlineSection(_poststack, xline, inlineDecimation);
          updateViewer(section);
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

    TraceSection section = _viewer.getTraceSection();
    if (section != null) {
      SectionType type = section.getSectionType();
      float inline = section.getTraceAxisKeyValue(0, TraceAxisKey.INLINE);
      float xline = section.getTraceAxisKeyValue(0, TraceAxisKey.XLINE);
      int inlineIndex = Math.round((inline - _poststack.getInlineStart()) / _poststack.getInlineDelta());
      int xlineIndex = Math.round((xline - _poststack.getXlineStart()) / _poststack.getXlineDelta());
      _inlineText.setText("" + inline);
      _xlineText.setText("" + xline);
      _inlineSlider.setSelection(inlineIndex);
      _xlineSlider.setSelection(xlineIndex);
      switch (type) {
        case INLINE_SECTION:
          _inlineButton.setSelection(true);
          _xlineButton.setSelection(false);
          break;
        case XLINE_SECTION:
          _inlineButton.setSelection(false);
          _xlineButton.setSelection(true);
          break;
        default:
          _inlineButton.setSelection(false);
          _xlineButton.setSelection(false);
          break;
      }
    }
  }

  private void updateViewer() {
    float inline = _poststack.getInlineStart() + _inlineSlider.getSelection() * _poststack.getInlineDelta();
    _inlineText.setText("" + inline);
    float xline = _poststack.getXlineStart() + _xlineSlider.getSelection() * _poststack.getXlineDelta();
    _xlineText.setText("" + xline);
    if (_inlineButton.getSelection()) {
      int xlineDecimation = _xlineStepSpinner.getSelection();
      TraceSection section = PostStack3dSectionFactory.createInlineSection(_poststack, inline, xlineDecimation);
      updateViewer(section);
    } else if (_xlineButton.getSelection()) {
      int inlineDecimation = _inlineStepSpinner.getSelection();
      TraceSection section = PostStack3dSectionFactory.createXlineSection(_poststack, xline, inlineDecimation);
      updateViewer(section);
    }
  }

  private void updateViewer(final TraceSection section) {
    _viewer.setTraceSection(section);
  }

  private void updateScales(final float tpiScale, final float ipsScale) {
    _viewer.setScales(tpiScale, ipsScale);
  }
}
