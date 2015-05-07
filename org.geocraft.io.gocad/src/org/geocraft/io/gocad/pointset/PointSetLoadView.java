/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.gocad.pointset;


import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.ui.form2.AbstractModelView;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.LabelField;
import org.geocraft.ui.form2.field.RadioGroupField;


public class PointSetLoadView extends AbstractModelView {

  @Override
  public void buildView(IModelForm form) {

    FormSection section = form.addSection("GOCAD point set");

    LabelField directory = section.addLabelField(PointSetMapperModel.DIRECTORY);
    directory.setTooltip("The storage directory for the pointset file");

    LabelField fileName = section.addLabelField(PointSetMapperModel.FILE_NAME);
    fileName.setTooltip("The GOCAD pointset name");

    Unit[] xyUnits = { Unit.FOOT, Unit.METER };
    RadioGroupField xyUnit = section.addRadioGroupField(PointSetMapperModel.XY_UNIT, xyUnits);
    xyUnit.setTooltip("The x,y unit of measurement");

    Unit[] someUnits = Unit.getCommonUnitsByDomain(new Domain[] { Domain.DISTANCE, Domain.TIME });
    //Unit[] allUnits = Unit.getUnitsByDomain("");
    ComboField zUnit = section.addComboField(PointSetMapperModel.Z_UNIT, someUnits);
    zUnit.setTooltip("The z unit of measurement");
  }

}
