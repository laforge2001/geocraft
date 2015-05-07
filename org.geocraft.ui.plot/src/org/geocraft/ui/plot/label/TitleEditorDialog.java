/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.label;


import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.IModel;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.ModelDialog;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.defs.Alignment;


public class TitleEditorDialog extends ModelDialog {

  private final IPlot _plot;

  private final ILabel _title;

  public TitleEditorDialog(final Shell shell, final IPlot plot, final ILabel title) {
    super(shell, "Title Editor");
    _plot = plot;
    _title = title;
  }

  @Override
  protected int getNumForms() {
    return 1;
  }

  @Override
  protected void buildModelForms(final IModelForm[] forms) {
    IModelForm form = forms[0];
    FormSection section = form.addSection("Title", false);

    section.addTextField(Label.TEXT);

    section.addComboField(Label.ALIGNMENT, Alignment.values());
  }

  @Override
  protected IModel createModel() {
    return new TitleEditorModel(_title);
  }

  @Override
  protected void applySettings() {
    TitleEditorModel model = (TitleEditorModel) _model;
    _title.setText(model.getText());
    _title.setAlignment(model.getAlignment());
    _plot.updateAll();
  }
}
