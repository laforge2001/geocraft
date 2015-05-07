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
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.ui.color.ColorBarEditor;
import org.geocraft.ui.common.GridLayoutHelper;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.ModelDialog;


public class PostStack3dRendererDialog extends ModelDialog {

  /** The poststack 3d renderer. */
  private final PostStack3dRenderer _renderer;

  /** The color bar editor. */
  private ColorBarEditor _colorBarEditor;

  public PostStack3dRendererDialog(final Shell shell, String title, final PostStack3dRenderer renderer) {
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
    final PostStack3d poststack = (PostStack3d) _renderer.getRenderedObjects()[0];

    FormSection section = form.addSection("PostStack3d Attributes", false);

    section.addSpinnerField(PostStack3dRendererModel.Z_SLICE, 0, poststack.getNumSamplesPerTrace(), 0, 1);

    // Create a color bar editor below the parameter form.
    Composite container = form.createComposite("Attribute Color Bar");
    container.setLayoutData(GridLayoutHelper.createLayoutData(true, true, SWT.FILL, SWT.FILL, 1, 1));

    final ColorBar colorBar = ((PostStack3dRendererModel) _model).getColorBar();
    _colorBarEditor = new ColorBarEditor(container, colorBar);

    FormToolkit toolkit = _managedForm.getToolkit();
    toolkit.adapt(container);
    _colorBarEditor.adapt(toolkit);
  }

  @Override
  protected IModel createModel() {
    return new PostStack3dRendererModel(_renderer.getSettingsModel());
  }

  @Override
  protected void applySettings() {
    // Apply the settings to the renderer. 
    ColorBar colorBar = ((PostStack3dRendererModel) _model).getColorBar();
    colorBar.setStartValue(_colorBarEditor.getStartValue());
    colorBar.setEndValue(_colorBarEditor.getEndValue());
    _renderer.updateSettings((PostStack3dRendererModel) _model, colorBar);
  }

  @Override
  public boolean close() {
    if (_colorBarEditor != null) {
      _colorBarEditor.dispose();
    }
    return super.close();
  }

}
