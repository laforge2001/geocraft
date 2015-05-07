/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.asciipointset;


import org.geocraft.ui.form2.AbstractModelView;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.LabelField;


public class AsciiPointSetExportView extends AbstractModelView {

  @Override
  public void buildView(IModelForm form) {

    FormSection section = form.addSection("Ascii point set");

    LabelField directory = section.addLabelField(AsciiPointSetMapperModel.DIRECTORY);
    directory.setLabel("Directory");
    directory.setTooltip("The storage directory for the pointset file");

    LabelField fileName = section.addLabelField(AsciiPointSetMapperModel.FILE_NAME);
    fileName.setLabel("File Name");
    fileName.setTooltip("The Ascii file name");
  }

}
