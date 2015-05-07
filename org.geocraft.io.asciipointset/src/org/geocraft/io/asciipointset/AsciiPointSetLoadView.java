/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.asciipointset;

import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.ui.form2.AbstractModelView;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.LabelField;
import org.geocraft.ui.form2.field.RadioGroupField;
import org.geocraft.ui.form2.field.SpinnerField;

public class AsciiPointSetLoadView extends AbstractModelView {

	@Override
	public void buildView(IModelForm form) {

		FormSection section = form.addSection("Ascii point set");

		LabelField directory = section
				.addLabelField(AsciiPointSetMapperModel.DIRECTORY);
		directory.setTooltip("The storage directory for the pointset file");

		LabelField fileName = section
				.addLabelField(AsciiPointSetMapperModel.FILE_NAME);
		fileName.setTooltip("The Ascii file name");

		Unit[] xyUnits = { Unit.FOOT, Unit.METER };
		RadioGroupField xyUnit = section.addRadioGroupField(
				AsciiPointSetMapperModel.XY_UNIT, xyUnits);
		xyUnit.setTooltip("The x,y unit of measurement");

		Unit[] someUnits = Unit.getCommonUnitsByDomain(new Domain[] {
				Domain.DISTANCE, Domain.TIME });
		// Unit[] allUnits = Unit.getUnitsByDomain("");
		ComboField zUnit = section.addComboField(
				AsciiPointSetMapperModel.Z_UNIT, someUnits);
		zUnit.setTooltip("The z unit of measurement");

		SpinnerField startingLineNum = section.addSpinnerField(
				AsciiPointSetMapperModel.STARTING_LINE_NUM, 0, 100, 0, 1);
		startingLineNum.setTooltip("Starting line number in the ascii file");

		FormSection colLocationSection = form.addSection("Column Locations",
				false);

		SpinnerField xColumnNum = colLocationSection.addSpinnerField(
				AsciiPointSetMapperModel.X_COLUMN_NUM, 1, 50, 0, 1);
		xColumnNum
				.setTooltip("Column # in the ascii file containing X locations");

		SpinnerField yColumnNum = colLocationSection.addSpinnerField(
				AsciiPointSetMapperModel.Y_COLUMN_NUM, 1, 50, 0, 1);
		yColumnNum
				.setTooltip("Column # in the ascii file containing Y locations");

		SpinnerField zColumnNum = colLocationSection.addSpinnerField(
				AsciiPointSetMapperModel.Z_COLUMN_NUM, 1, 50, 0, 1);
		zColumnNum
				.setTooltip("Column # in the ascii file containing Z locations");
	}

}
