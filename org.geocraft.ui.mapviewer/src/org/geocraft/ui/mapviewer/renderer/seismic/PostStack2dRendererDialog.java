/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer.renderer.seismic;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.geocraft.core.color.ColorBar;
import org.geocraft.core.model.IModel;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.ui.color.ColorBarEditor;
import org.geocraft.ui.common.GridLayoutHelper;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.ModelDialog;


public class PostStack2dRendererDialog extends ModelDialog {

  /** The poststack 2d renderer. */
  private final PostStack2dRenderer _renderer;

  /** The color bar editor. */
  private ColorBarEditor _colorBarEditor;

  public PostStack2dRendererDialog(final Shell shell, String title, final PostStack2dRenderer renderer) {
    super(shell, title);
    _renderer = renderer;
  }

  @Override
  protected int getNumForms() {
    return 1;
  }

  @Override
  protected void buildModelForms(final IModelForm[] forms) {
    IModelForm form = forms[0];
    final PostStack2dLine poststack = (PostStack2dLine) _renderer.getRenderedObjects()[0];

    FormSection section = form.addSection("PostStack2d Attributes", false);

    section.addSpinnerField(PostStack2dRendererModel.Z_SLICE, 0, poststack.getNumSamplesPerTrace(), 0, 1);

    // Create a color bar editor below the parameter form.
    Composite container = form.createComposite("Attribute Color Bar");
    container.setLayoutData(GridLayoutHelper.createLayoutData(true, true, SWT.FILL, SWT.FILL, 1, 1));

    final ColorBar colorBar = ((PostStack2dRendererModel) _model).getColorBar();
    _colorBarEditor = new ColorBarEditor(container, colorBar);

    FormToolkit toolkit = _managedForm.getToolkit();
    toolkit.adapt(container);
    _colorBarEditor.adapt(toolkit);
  }

  @Override
  protected IModel createModel() {
    return new PostStack2dRendererModel(_renderer.getSettingsModel());
  }

  @Override
  protected void applySettings() {
    // Apply the settings to the renderer. 
    ColorBar colorBar = ((PostStack2dRendererModel) _model).getColorBar();
    colorBar.setStartValue(_colorBarEditor.getStartValue());
    colorBar.setEndValue(_colorBarEditor.getEndValue());
    _renderer.updateSettings((PostStack2dRendererModel) _model, colorBar);
  }

  @Override
  public boolean close() {
    if (_colorBarEditor != null) {
      _colorBarEditor.dispose();
    }
    return super.close();
  }

}
