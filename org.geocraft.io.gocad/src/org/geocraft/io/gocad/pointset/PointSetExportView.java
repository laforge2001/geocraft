/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.gocad.pointset;


import org.geocraft.ui.form2.AbstractModelView;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.LabelField;


public class PointSetExportView extends AbstractModelView {

  @Override
  public void buildView(IModelForm form) {

    FormSection section = form.addSection("GOCAD point set");

    LabelField directory = section.addLabelField(PointSetMapperModel.DIRECTORY);
    directory.setLabel("Directory");
    directory.setTooltip("The storage directory for the pointset file");

    LabelField fileName = section.addLabelField(PointSetMapperModel.FILE_NAME);
    fileName.setLabel("File Name");
    fileName.setTooltip("The GOCAD pointset name");
  }

}
