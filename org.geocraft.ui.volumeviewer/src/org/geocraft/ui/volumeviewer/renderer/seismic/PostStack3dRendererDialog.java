/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.renderer.seismic;


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.geocraft.core.color.ColorMapEvent;
import org.geocraft.core.color.ColorMapListener;
import org.geocraft.core.model.IModel;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.ui.color.ColorBarEditor;
import org.geocraft.ui.common.GridLayoutHelper;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.PreferencesModelDialog;
import org.geocraft.ui.form2.field.CheckboxField;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.SpinnerField;


/**
 * Defines the dialog for editing the display properties
 * of a <code>PostStack3d</code> renderer in a 3D viewer.
 */
public class PostStack3dRendererDialog extends PreferencesModelDialog implements ColorMapListener,
    ISeismicDatasetRendererConstants {

  protected static final int NUMERIC_ENTER = 16777296;

  /** The 3D volume whose being rendered. */
  private final PostStack3d _poststack;

  /** The 3D volume renderer. */
  private final PostStack3dRenderer _renderer;

  /** The color bar editor. */
  private ColorBarEditor _colorBarEditor;

  /** The toggle button for inline slice visibility. */
  private Button _inlineButton;

  /** The slider for selecting the inline slice to render. */
  private Slider _inlineSlider;

  /** The toggle button for xline slice visibility. */
  private Button _xlineButton;

  /** The slider for selecting the xline slice to render. */
  private Slider _xlineSlider;

  /** The toggle button for z slice visibility. */
  private Button _zButton;

  /** The slider for selecting the z slice to render. */
  private Slider _zSlider;

  /**
   * Constructs a dialog for editing the display properties of
   * a <code>PostStack3d</code> renderer.
   * 
   * @param shell the parent shell.
   * @param title the dialog title.
   * @param renderer the renderer.
   * @param poststack the volume being rendered.
   */
  public PostStack3dRendererDialog(final Shell shell, final String title, final PostStack3dRenderer renderer, final PostStack3d poststack) {
    super(shell, title);
    _renderer = renderer;
    _poststack = poststack;
  }

  @Override
  protected int getNumForms() {
    return 2;
  }

  @Override
  protected void buildModelForms(final IModelForm[] forms) {
    IModelForm form = forms[0];
    form.setTitle("Rendering");

    final FormSection generalSection = form.addSection("General", false);

    final ComboField interpolationField = generalSection.addComboField(INTERPOLATION_METHOD, InterpolationMethod
        .values());
    interpolationField.setTooltip("Select the interpolation method used in the vertical direction.");

    final NormalizationMethod[] normMethods = { NormalizationMethod.BY_LIMITS, NormalizationMethod.BY_MAXIMUM };
    final ComboField normalizationField = generalSection.addComboField(NORMALIZATION_METHOD, normMethods);
    normalizationField.setTooltip("Select the normalization method used for coloring traces.");

    final SpinnerField percentileField = generalSection.addSpinnerField(PERCENTILE, 0, 50, 0, 1);
    percentileField.setTooltip("Reduces the min/max range of data. Used to eliminate spikes, etc.");

    //final SpinnerField transparencyField = generalSection.addSpinnerField(TRANSPARENCY, 0, 100, 0, 1);
    //transparencyField
    //    .setTooltip("Select the level of transparency for the traces (0=fully opaque, 100=fully transparent).");

    final CheckboxField polarityField = generalSection.addCheckboxField(REVERSE_POLARITY);
    polarityField.setTooltip("Switches the displayed polarity of the trace samples.");

    final Composite sliceComposite = form.createComposite("Slices", false);
    sliceComposite.setLayout(new GridLayout(2, false));

    final FormToolkit toolkit = _managedForm.getToolkit();

    final Button _autoUpdateButton = createToggleButton(sliceComposite, true, 2, toolkit);
    _autoUpdateButton.setText("Auto-Update on slice changes.");
    _autoUpdateButton.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        if (_autoUpdateButton.getSelection()) {
          updateViewerSlices();
          getButton(IDialogConstants.CANCEL_ID).setEnabled(true);
        } else {
          getButton(IDialogConstants.CANCEL_ID).setEnabled(false);
        }
      }

    });

    // Create the inline slice group composite.
    final Group inlineGroup = createGroup(sliceComposite, "Inline", toolkit);
    _inlineButton = createToggleButton(inlineGroup, false, 1, toolkit);
    final Text inlineText = createCurrentText(inlineGroup, _poststack.getInlineStart(), toolkit);
    final Spinner inlineStepSpinner = createStepSpinner(inlineGroup, 1, _poststack.getNumInlines() - 1, toolkit);
    createStartLabel(inlineGroup, _poststack.getInlineStart(), toolkit);
    _inlineSlider = createSlider(inlineGroup, 0, _poststack.getNumInlines() - 1, toolkit);
    createEndLabel(inlineGroup, _poststack.getInlineEnd(), toolkit);

    // Create the xline slice group composite.
    final Group xlineGroup = createGroup(sliceComposite, "Xline", toolkit);
    _xlineButton = createToggleButton(xlineGroup, false, 1, toolkit);
    final Text xlineText = createCurrentText(xlineGroup, _poststack.getXlineStart(), toolkit);
    final Spinner xlineStepSpinner = createStepSpinner(xlineGroup, 1, _poststack.getNumXlines() - 1, toolkit);
    createStartLabel(xlineGroup, _poststack.getXlineStart(), toolkit);
    _xlineSlider = createSlider(xlineGroup, 0, _poststack.getNumXlines() - 1, toolkit);
    createEndLabel(xlineGroup, _poststack.getXlineEnd(), toolkit);

    final PostStack3dRendererModel model = (PostStack3dRendererModel) _model;

    // Create the inline slice group composite.
    String zLabel = "Z";
    final Domain zDomain = _poststack.getZDomain();
    if (zDomain == Domain.TIME) {
      zLabel = "Time";
    } else if (zDomain == Domain.DISTANCE) {
      zLabel = "Depth";
    }
    final Group zGroup = createGroup(sliceComposite, zLabel, toolkit);
    _zButton = createToggleButton(zGroup, false, 1, toolkit);
    final Text zText = createCurrentText(zGroup, _poststack.getZStart(), toolkit);
    final Spinner zStepSpinner = createStepSpinner(zGroup, 1, _poststack.getNumSamplesPerTrace() - 1, toolkit);
    createStartLabel(zGroup, _poststack.getZStart(), toolkit);
    _zSlider = createSlider(zGroup, 0, _poststack.getNumSamplesPerTrace() - 1, toolkit);
    createEndLabel(zGroup, _poststack.getZEnd(), toolkit);

    _inlineButton.setSelection(model.getInlineSliceVisible());
    _xlineButton.setSelection(model.getXlineSliceVisible());
    _zButton.setSelection(model.getZSliceVisible());
    inlineText.setText("" + model.getInlineSlice());
    xlineText.setText("" + model.getXlineSlice());
    zText.setText("" + model.getZSlice());
    final int inlineIndex = Math.round((model.getInlineSlice() - _poststack.getInlineStart())
        / _poststack.getInlineDelta());
    final int xlineIndex = Math
        .round((model.getXlineSlice() - _poststack.getXlineStart()) / _poststack.getXlineDelta());
    final int zIndex = Math.round((model.getZSlice() - _poststack.getZStart()) / _poststack.getZDelta());
    _inlineSlider.setSelection(inlineIndex);
    _xlineSlider.setSelection(xlineIndex);
    _zSlider.setSelection(zIndex);

    // Add selection listener for the inline slice toggle button.
    _inlineButton.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        final float inlineSlice = _poststack.getInlineStart() + _inlineSlider.getSelection()
            * _poststack.getInlineDelta();
        inlineText.setText("" + inlineSlice);
        if (_autoUpdateButton.getSelection()) {
          final boolean inlineSliceVisible = _inlineButton.getSelection();
          model.setInlineSliceVisible(inlineSliceVisible);
          model.setInlineSlice(inlineSlice);
          _renderer.setInlineSlice(inlineSliceVisible, inlineSlice);
        }
      }

    });

    // Add selection listener for the xline slice toggle button.
    _xlineButton.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        final float xlineSlice = _poststack.getXlineStart() + _xlineSlider.getSelection() * _poststack.getXlineDelta();
        xlineText.setText("" + xlineSlice);
        if (_autoUpdateButton.getSelection()) {
          final boolean xlineSliceVisible = _xlineButton.getSelection();
          model.setXlineSliceVisible(xlineSliceVisible);
          model.setXlineSlice(xlineSlice);
          _renderer.setXlineSlice(xlineSliceVisible, xlineSlice);
        }
      }

    });

    // Add selection listener for the z slice toggle button.
    _zButton.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        final float zSlice = _poststack.getZStart() + _zSlider.getSelection() * _poststack.getZDelta();
        zText.setText("" + zSlice);
        if (_autoUpdateButton.getSelection()) {
          final boolean zSliceVisible = _zButton.getSelection();
          model.setZSliceVisible(zSliceVisible);
          model.setZSlice(zSlice);
          _renderer.setZSlice(zSliceVisible, zSlice);
        }
      }

    });

    inlineText.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(final KeyEvent e) {
        if (e.keyCode == SWT.CR || e.keyCode == NUMERIC_ENTER) {
          final boolean inlineSliceVisible = _inlineButton.getSelection();
          model.setInlineSliceVisible(inlineSliceVisible);
          if (inlineSliceVisible) {
            final String inlineTextStr = inlineText.getText().trim();
            boolean error = false;
            try {
              final float inlineSlice = Float.parseFloat(inlineTextStr);
              final int index = (int) ((inlineSlice - _poststack.getInlineStart()) / _poststack.getInlineDelta());
              if (index >= 0 && index < _poststack.getNumInlines()) {
                model.setInlineSlice(inlineSlice);
                _inlineSlider.setSelection(index);
                _renderer.setInlineSlice(true, inlineSlice);
              } else {
                error = true;
              }
            } catch (final Exception ex) {
              error = true;
            }
            if (error) {
              MessageDialog.openError(PostStack3dRendererDialog.this.getShell(), "Navigation Error",
                  "Invalid Inline Slice: " + inlineTextStr);
            }
          }
        }

      }

    });

    xlineText.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(final KeyEvent e) {
        if (e.keyCode == SWT.CR || e.keyCode == NUMERIC_ENTER) {
          final boolean xlineSliceVisible = _xlineButton.getSelection();
          model.setXlineSliceVisible(xlineSliceVisible);
          if (xlineSliceVisible) {
            final String xlineTextStr = xlineText.getText().trim();
            boolean error = false;
            try {
              final float xlineSlice = Float.parseFloat(xlineTextStr);
              final int index = (int) ((xlineSlice - _poststack.getXlineStart()) / _poststack.getXlineDelta());
              if (index >= 0 && index < _poststack.getNumXlines()) {
                model.setXlineSlice(xlineSlice);
                _xlineSlider.setSelection(index);
                _renderer.setXlineSlice(true, xlineSlice);
              } else {
                error = true;
              }
            } catch (final NumberFormatException ex) {
              error = true;
            }
            if (error) {
              MessageDialog.openError(PostStack3dRendererDialog.this.getShell(), "Navigation Error",
                  "Invalid Xline Slice: " + xlineTextStr);
            }
          }
        }
      }

    });

    zText.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(final KeyEvent e) {
        if (e.keyCode == SWT.CR || e.keyCode == NUMERIC_ENTER) {
          final boolean zSliceVisible = _zButton.getSelection();
          model.setZSliceVisible(zSliceVisible);
          if (zSliceVisible) {
            final String zTextStr = zText.getText().trim();
            boolean error = false;
            try {
              final float zSlice = Float.parseFloat(zTextStr);
              final int index = (int) ((zSlice - _poststack.getZStart()) / _poststack.getZDelta());
              if (index >= 0 && index < _poststack.getNumSamplesPerTrace()) {
                model.setZSlice(zSlice);
                _zSlider.setSelection(index);
                _renderer.setZSlice(true, zSlice);
              } else {
                error = true;
              }
            } catch (final NumberFormatException ex) {
              error = true;
            }
            if (error) {
              MessageDialog.openError(PostStack3dRendererDialog.this.getShell(), "Navigation Error",
                  "Invalid Z Slice: " + zTextStr);
            }
          }
        }
      }

    });

    // Add listener for the inline slice slider.
    _inlineSlider.addListener(SWT.MouseUp, new Listener() {

      public void handleEvent(final Event event) {
        final float inlineSlice = _poststack.getInlineStart() + _inlineSlider.getSelection()
            * _poststack.getInlineDelta();
        inlineText.setText("" + inlineSlice);
        if (_autoUpdateButton.getSelection()) {
          final boolean inlineSliceVisible = _inlineButton.getSelection();
          model.setInlineSliceVisible(inlineSliceVisible);
          model.setInlineSlice(inlineSlice);
          _renderer.setInlineSlice(inlineSliceVisible, inlineSlice);
        }
      }

    });

    // Add listener for the xline slice slider.
    _xlineSlider.addListener(SWT.MouseUp, new Listener() {

      public void handleEvent(final Event event) {
        final float xlineSlice = _poststack.getXlineStart() + _xlineSlider.getSelection() * _poststack.getXlineDelta();
        xlineText.setText("" + xlineSlice);
        if (_autoUpdateButton.getSelection()) {
          final boolean xlineSliceVisible = _xlineButton.getSelection();
          model.setXlineSliceVisible(xlineSliceVisible);
          model.setXlineSlice(xlineSlice);
          _renderer.setXlineSlice(xlineSliceVisible, xlineSlice);
        }
      }

    });

    // Add listener for the z slice slider.
    _zSlider.addListener(SWT.MouseUp, new Listener() {

      public void handleEvent(final Event event) {
        final float zSlice = _poststack.getZStart() + _zSlider.getSelection() * _poststack.getZDelta();
        zText.setText("" + zSlice);
        if (_autoUpdateButton.getSelection()) {
          final boolean zSliceVisible = _zButton.getSelection();
          model.setZSliceVisible(zSliceVisible);
          model.setZSlice(zSlice);
          _renderer.setZSlice(zSliceVisible, zSlice);
        }
      }

    });

    // Add listener for the inline step spinner.
    inlineStepSpinner.addListener(SWT.MouseUp, new Listener() {

      public void handleEvent(final Event event) {
        _inlineSlider.setIncrement(inlineStepSpinner.getSelection());
        if (_autoUpdateButton.getSelection()) {
          updateViewerSlices();
        }
      }

    });

    // Add listener for the xline step spinner.
    xlineStepSpinner.addListener(SWT.MouseUp, new Listener() {

      public void handleEvent(final Event event) {
        _xlineSlider.setIncrement(xlineStepSpinner.getSelection());
        if (_autoUpdateButton.getSelection()) {
          updateViewerSlices();
        }
      }

    });

    // Add listener for the z step spinner.
    zStepSpinner.addListener(SWT.MouseUp, new Listener() {

      public void handleEvent(final Event event) {
        _zSlider.setIncrement(zStepSpinner.getSelection());
        if (_autoUpdateButton.getSelection()) {
          updateViewerSlices();
        }
      }

    });

    form = forms[1];
    form.setTitle("Color Bar");

    // Create a color bar editor below the parameter form.
    //Composite container = new Composite(colorbarSection.getComposite(), SWT.NONE);
    final Composite container = form.createComposite("Color Bar", false);
    container.setLayoutData(GridLayoutHelper.createLayoutData(true, true, SWT.FILL, SWT.FILL, 1, 1));

    _colorBarEditor = new ColorBarEditor(container, ((PostStack3dRendererModel) _model).getColorBar(), 384);

    toolkit.adapt(container);
    _colorBarEditor.adapt(toolkit);

    //    form = forms[2];
    //    form.setTitle("AGC and Geometric Gain");
    //
    //    FormSection agcSection = form.addSection("AGC");
    //    agcSection.addCheckboxField(PostStack3dRendererModel.AGC_APPLY);
    //    agcSection.addComboField(PostStack3dRendererModel.AGC_TYPE, AGC.Type.values());
    //    agcSection.addTextField(PostStack3dRendererModel.AGC_WINDOW_LENGTH);
    //
    //    FormSection gainSection = form.addSection("Geometric Gain");
    //    gainSection.addCheckboxField(PostStack3dRendererModel.GEOMETRIC_GAIN_APPLY);
    //    gainSection.addTextField(PostStack3dRendererModel.GEOMETRIC_GAIN_T0);
    //    gainSection.addTextField(PostStack3dRendererModel.GEOMETRIC_GAIN_N);
    //    gainSection.addTextField(PostStack3dRendererModel.GEOMETRIC_GAIN_TMAX);
  }

  @Override
  public void propertyChanged(final String key) {
    final PostStack3dRendererModel model = (PostStack3dRendererModel) _model;
    if (key.equals(NORMALIZATION_METHOD)) {
      final NormalizationMethod normalization = model.getNormalizationMethod();
      final boolean showPercentile = normalization.equals(NormalizationMethod.BY_MAXIMUM);
      setFieldEnabled(PERCENTILE, showPercentile);
    }
    super.propertyChanged(key);
  }

  @Override
  protected void applySettings() {
    final PostStack3dRendererModel model = (PostStack3dRendererModel) _model;

    double normalizationMin = 0;
    double normalizationMax = 0;
    final NormalizationMethod overlayNormalization = model.getNormalizationMethod();
    if (overlayNormalization.equals(NormalizationMethod.BY_LIMITS)) {
      normalizationMin = _colorBarEditor.getStartValue();
      normalizationMax = _colorBarEditor.getEndValue();
    } else if (overlayNormalization.equals(NormalizationMethod.BY_MAXIMUM)) {
      final double[] dataMinMax = _renderer.getDataMinimumAndMaximum();
      normalizationMin = dataMinMax[0];
      normalizationMax = dataMinMax[1];
      final double minmax = Math.max(Math.abs(normalizationMin), Math.abs(normalizationMax));
      normalizationMin = -minmax;
      normalizationMax = minmax;
    } else if (overlayNormalization.equals(NormalizationMethod.BY_AVERAGE)) {
      //      normalizationMax = _renderer.getDataAverage();
      //      normalizationMin = -normalizationMax;
    }
    model.getColorBar().setStartValue(normalizationMin);
    model.getColorBar().setEndValue(normalizationMax);
    _colorBarEditor.setColorBar(model.getColorBar());

    _renderer.updateRendererModel(model);
  }

  @Override
  protected IModel createModel() {
    final PostStack3dRendererModel model = new PostStack3dRendererModel(_renderer.getSettingsModel());
    model.getColorBar().addColorMapListener(this);
    return model;
  }

  @Override
  public boolean close() {
    // Dispose of the color bar editor first.
    if (_colorBarEditor != null) {
      _colorBarEditor.dispose();
    }
    return super.close();
  }

  public void colorsChanged(final ColorMapEvent event) {
    _renderer.colorsChanged(event);
  }

  /**
   * Creates a group in which to put controls for one of the volume slices.
   * 
   * @param parent the parent composite.
   * @param title the title for the group.
   * @param toolkit the form toolkit.
   * @return the created group.
   */
  private Group createGroup(final Composite parent, final String title, final FormToolkit toolkit) {
    final Group group = createGroup(parent, title, 5, toolkit);
    return group;
  }

  /**
   * Creates a group in which to put controls for one of the volume slices.
   * 
   * @param parent the parent composite.
   * @param title the title for the group.
   * @param numColumns the number of columns for the group.
   * @param toolkit the form toolkit.
   * @return the created group.
   */
  private Group createGroup(final Composite parent, final String title, final int numColumns, final FormToolkit toolkit) {
    final Group group = new Group(parent, SWT.NONE);
    group.setText(title);
    group.setLayout(GridLayoutHelper.createLayout(numColumns, false));
    group.setLayoutData(GridLayoutHelper.createLayoutData(true, true, SWT.FILL, SWT.FILL, 2, 1));
    toolkit.adapt(group);
    return group;
  }

  /**
   * Creates a slider control for the "Current" value of a slice.
   * 
   * @param group the group in which to put the slider control.
   * @param min the minimum slider value.
   * @param max the maximum slider value.
   * @param toolkit the form toolkit.
   * @return the created slider control.
   */
  private Slider createSlider(final Group group, final int min, final int max, final FormToolkit toolkit) {
    final Slider slider = new Slider(group, SWT.HORIZONTAL);
    final int thumb = 1;
    slider.setMinimum(min);
    slider.setMaximum(max + thumb);
    slider.setThumb(thumb);
    slider.setLayoutData(GridLayoutHelper.createLayoutData(true, false, SWT.FILL, SWT.FILL, 2, 1));
    toolkit.adapt(slider, true, true);
    return slider;
  }

  /**
   * Creates a toggle button control for the "visibility" of a slice.
   * 
   * @param parent the parent composite.
   * @param selected the current visibility state of the toggle.
   * @param hSpan the horizontal span.
   * @param toolkit the form toolkit.
   * @return the created button control.
   */
  private Button createToggleButton(final Composite parent, final boolean selected, final int hSpan,
      final FormToolkit toolkit) {
    final Button button = new Button(parent, SWT.CHECK);
    button.setSelection(selected);
    button.setLayoutData(GridLayoutHelper.createLayoutData(false, false, SWT.FILL, SWT.FILL, hSpan, 1));
    toolkit.adapt(button, true, true);
    return button;
  }

  /**
   * Creates a text control for the "Current" value of a slice.
   * 
   * @param group the group in which to put the text control.
   * @param value the current slice value.
   * @param toolkit the form toolkit.
   * @return the created text control.
   */
  private Text createCurrentText(final Group group, final float value, final FormToolkit toolkit) {
    final Label label = new Label(group, SWT.NONE);
    label.setText("Current");
    label.setLayoutData(GridLayoutHelper.createLayoutData(false, false, SWT.LEFT, SWT.FILL, 1, 1));
    final Text text = new Text(group, SWT.NONE);
    text.setText("" + value);
    text.setLayoutData(GridLayoutHelper.createLayoutData(true, false, SWT.FILL, SWT.FILL, 1, 1));
    toolkit.adapt(label, true, true);
    toolkit.adapt(text, true, true);
    return text;
  }

  /**
   * Creates a "Step" spinner control.
   * 
   * @param group the group in which to put the spinner control.
   * @param selection the initial selection value.
   * @param maximum the maximum selection value.
   * @param toolkit the form toolkit.
   * @return the created spinner control.
   */
  private Spinner createStepSpinner(final Group group, final int selection, final int maximum, final FormToolkit toolkit) {
    final Label label = new Label(group, SWT.NONE);
    label.setText("Step");
    label.setLayoutData(GridLayoutHelper.createLayoutData(true, false, SWT.RIGHT, SWT.FILL, 1, 1));
    final Spinner spinner = new Spinner(group, SWT.BORDER);
    spinner.setValues(selection, 1, maximum, 0, 1, 10);
    spinner.setLayoutData(GridLayoutHelper.createLayoutData(false, false, SWT.FILL, SWT.FILL, 1, 1));
    toolkit.adapt(label, true, true);
    toolkit.adapt(spinner, true, true);
    return spinner;
  }

  /**
   * Creates a "Start" label control.
   * 
   * @param group the group in which to put the label control.
   * @param value the start value to display in the label.
   * @param toolkit the form toolkit.
   * @return the created label control.
   */
  private Label createStartLabel(final Group group, final float value, final FormToolkit toolkit) {
    final Label label = new Label(group, SWT.NONE);
    label.setText("" + value);
    label.setLayoutData(GridLayoutHelper.createLayoutData(false, false, SWT.RIGHT, SWT.FILL, 2, 1));
    toolkit.adapt(label, true, true);
    return label;
  }

  /**
   * Creates an "End" label control.
   * 
   * @param group the group in which to put the label control.
   * @param value the end value to display in the label.
   * @param toolkit the form toolkit.
   * @return the created label control.
   */
  private Label createEndLabel(final Group group, final float value, final FormToolkit toolkit) {
    final Label label = new Label(group, SWT.NONE);
    label.setText("" + value);
    label.setLayoutData(GridLayoutHelper.createLayoutData(false, false, SWT.LEFT, SWT.FILL, 1, 1));
    toolkit.adapt(label, true, true);
    return label;
  }

  /**
   * Updates the slices being displayed by the renderer.
   */
  private void updateViewerSlices() {
    final boolean inlineSliceVisible = _inlineButton.getSelection();
    final boolean xlineSliceVisible = _xlineButton.getSelection();
    final boolean zSliceVisible = _zButton.getSelection();
    final float inlineSlice = _poststack.getInlineStart() + _inlineSlider.getSelection() * _poststack.getInlineDelta();
    final float xlineSlice = _poststack.getXlineStart() + _xlineSlider.getSelection() * _poststack.getXlineDelta();
    final float zSlice = _poststack.getZStart() + _zSlider.getSelection() * _poststack.getZDelta();
    _renderer.setSlices(inlineSliceVisible, inlineSlice, xlineSliceVisible, xlineSlice, zSliceVisible, zSlice);
  }

  @Override
  protected void updatePreferences() {
    final SeismicDatasetRendererModel model = (SeismicDatasetRendererModel) _model;

    // Update the default renderer settings to the preferences.
    final IPreferenceStore preferences = SeismicDatasetRendererPreferencePage.PREFERENCE_STORE;

    preferences.setValue(REVERSE_POLARITY, model.getReversePolarity());
    preferences.setValue(INTERPOLATION_METHOD, model.getInterpolationMethod().getName());
    preferences.setValue(NORMALIZATION_METHOD, model.getNormalizationMethod().getName());
    preferences.setValue(TRANSPARENCY, model.getTransparency());
    preferences.setValue(PERCENTILE, model.getPercentile());
    //preferences.setValue(COLOR_MAP, colorMap);

    //    preferences.setValue(ISeismicDatasetRendererConstants.AGC_APPLY, model.getAgcApply());
    //    preferences.setValue(AGC_TYPE, model.getAgcType().getName());
    //    preferences.setValue(AGC_WINDOW_LENGTH, model.getAgcWindowLength());
    //
    //    preferences.setValue(ISeismicDatasetRendererConstants.GEOMETRIC_GAIN_APPLY, model.getGeometricGainApply());
    //    preferences.setValue(ISeismicDatasetRendererConstants.GEOMETRIC_GAIN_T0, model.getGeometricGainT0());
    //    preferences.setValue(ISeismicDatasetRendererConstants.GEOMETRIC_GAIN_TMAX, model.getGeometricGainTMax());
    //    preferences.setValue(ISeismicDatasetRendererConstants.GEOMETRIC_GAIN_N, model.getGeometricGainN());
  }
}
