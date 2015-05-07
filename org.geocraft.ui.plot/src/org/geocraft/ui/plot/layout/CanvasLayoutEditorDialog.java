/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.layout;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.IModel;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.ModelDialog;
import org.geocraft.ui.plot.IPlot;


public class CanvasLayoutEditorDialog extends ModelDialog {

  private final IPlot _plot;

  public CanvasLayoutEditorDialog(final Shell shell, final IPlot plot) {
    super(shell, "Layout Editor");
    _plot = plot;
  }

  @Override
  protected int getNumForms() {
    return 1;
  }

  @Override
  protected void buildModelForms(final IModelForm[] forms) {
    IModelForm form = forms[0];
    FormSection titleSection = form.addSection("Title");

    titleSection.addCheckboxField(CanvasLayoutModel.TITLE_VISIBLE);

    titleSection.addTextField(CanvasLayoutModel.TITLE_HEIGHT);

    FormSection labelsSection = form.addSection("Labels");

    labelsSection.addTextField(CanvasLayoutModel.TOP_LABEL_HEIGHT);

    labelsSection.addTextField(CanvasLayoutModel.LEFT_LABEL_WIDTH);

    labelsSection.addTextField(CanvasLayoutModel.RIGHT_LABEL_WIDTH);

    labelsSection.addTextField(CanvasLayoutModel.BOTTOM_LABEL_HEIGHT);

    FormSection axesSection = form.addSection("Axes");

    axesSection.addTextField(CanvasLayoutModel.TOP_AXIS_HEIGHT);

    axesSection.addTextField(CanvasLayoutModel.LEFT_AXIS_WIDTH);

    axesSection.addTextField(CanvasLayoutModel.RIGHT_AXIS_WIDTH);

    axesSection.addTextField(CanvasLayoutModel.BOTTOM_AXIS_HEIGHT);
  }

  @Override
  protected IModel createModel() {
    return new CanvasLayoutModel(_plot.getCanvasLayoutModel());
  }

  @Override
  protected void applySettings() {
    CanvasLayoutModel layoutModel = (CanvasLayoutModel) _model;
    _plot.getPlotComposite().updateCanvasLayout(layoutModel);
  }

  public static CanvasLayoutEditorDialog create(final IPlot plot) {
    Shell shell = new Shell(Display.getDefault());
    CanvasLayoutEditorDialog dialog = new CanvasLayoutEditorDialog(shell, plot);
    dialog.setShellStyle(SWT.DIALOG_TRIM);
    dialog.create();
    dialog.getShell().pack();
    dialog.getShell().setSize(500, 500);
    dialog.getShell().setText("Canvas Layout Editor");
    return dialog;
  }
}
