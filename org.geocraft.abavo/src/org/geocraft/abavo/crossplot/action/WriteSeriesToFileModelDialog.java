/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.action;


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.abavo.crossplot.ABDataSeries;
import org.geocraft.core.common.util.Utilities;
import org.geocraft.core.model.IModel;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.ModelDialog;


public class WriteSeriesToFileModelDialog extends ModelDialog {

  private ABDataSeries _series;

  public WriteSeriesToFileModelDialog(Shell shell, ABDataSeries series) {
    super(shell, "Write series to file");
    _series = series;
  }

  @Override
  protected int getNumForms() {
    return 1;
  }

  @Override
  protected IModel createModel() {
    WriteSeriesToFileModel model = new WriteSeriesToFileModel();
    if (_series != null) {
      String seriesName = _series.getName();
      seriesName = seriesName.replace("#", "");
      seriesName = seriesName.replace(" ", "_");
      model.setFileName(seriesName);
    }
    return model;
  }

  @Override
  protected void buildModelForms(IModelForm[] forms) {
    FormSection section = forms[0].addSection("File Parameters");
    section.addDirectoryField(WriteSeriesToFileModel.DIRECTORY, Utilities.getHomeDirectory()).setLabel("Directory");
    section.addTextField(WriteSeriesToFileModel.FILE_NAME).setLabel("File name");
    section.addCheckboxField(WriteSeriesToFileModel.COLUMN_A).setLabel("Output A column");
    section.addCheckboxField(WriteSeriesToFileModel.COLUMN_B).setLabel("Output B column");
    section.addCheckboxField(WriteSeriesToFileModel.COLUMN_X).setLabel("Output X column");
    section.addCheckboxField(WriteSeriesToFileModel.COLUMN_Y).setLabel("Output Y column");
    section.addCheckboxField(WriteSeriesToFileModel.COLUMN_Z).setLabel("Output Z column");
  }

  @Override
  protected void applySettings() {
    // Nothing to do.
  }

  @Override
  public void createButtonsForButtonBar(final Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
  }

  public WriteSeriesToFileModel getModel() {
    return (WriteSeriesToFileModel) _model;
  }
}
