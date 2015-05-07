/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.renderer.fault;


import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.IModel;
import org.geocraft.core.model.fault.FaultInterpretation;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.ModelDialog;


/**
 * Defines the dialog for editing the display properties
 * of a <code>FaultInterpretation</code> renderer in a 3D viewer.
 */
public class FaultRendererDialog extends ModelDialog {

  /** The fault renderer. */
  private final FaultRenderer _renderer;

  /** The fault being rendered. */
  private final FaultInterpretation _fault;

  /**
   * Constructs a dialog for editing the display properties of
   * a <code>FaultInterpretation</code> renderer.
   * 
   * @param shell the parent shell.
   * @param title the dialog title.
   * @param renderer the renderer.
   * @param fault the fault being rendered.
   */
  public FaultRendererDialog(final Shell shell, final String title, final FaultRenderer renderer, final FaultInterpretation fault) {
    super(shell, title);
    _renderer = renderer;
    _fault = fault;
  }

  @Override
  protected int getNumForms() {
    return 1;
  }

  @Override
  protected void buildModelForms(final IModelForm[] forms) {
    final FaultRendererModel model = (FaultRendererModel) _model;
    final IModelForm form = forms[0];
    final FormSection segmentSection = form.addSection("Segments", false);
    segmentSection.addCheckboxField(FaultRendererModel.SHOW_SEGMENTS);
    segmentSection.addColorField(FaultRendererModel.SEGMENT_COLOR);
    segmentSection.addTextField(FaultRendererModel.SEGMENT_WIDTH);

    final FormSection triangleSection = form.addSection("Triangulation", false);
    triangleSection.addCheckboxField(FaultRendererModel.SHOW_TRIANGULATION);
    triangleSection.addColorField(FaultRendererModel.TRIANGLE_COLOR);
    //    section.addSpinnerField(FaultRendererModel.TRANSPARENCY, 0, 100, 0, 1);
    //    section.addSpinnerField(FaultRendererModel.PERCENTILE, 0, 100, 0, 1);
    //    section.addRadioGroupField(FaultRendererModel.SMOOTHING_METHOD, SmoothingMethod.values());
    //    section.addCheckboxField(FaultRendererModel.SHOW_MESH);
    //    section.addEntityComboField(FaultRendererModel.RGB_GRID, FaultInterpretation.class).setFilter(
    //        new FaultInterpretationSpecification(_fault));
  }

  @Override
  protected void applySettings() {
    final FaultRendererModel model = (FaultRendererModel) _model;
    _renderer.updateRendererModel(model);
  }

  @Override
  public IModel createModel() {
    final FaultRendererModel model = new FaultRendererModel(_renderer.getSettingsModel());
    return model;
  }
}
