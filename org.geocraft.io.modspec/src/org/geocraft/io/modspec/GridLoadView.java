package org.geocraft.io.modspec;


import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.OnsetType;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.ui.form2.AbstractModelView;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.EnhancedComboField;
import org.geocraft.ui.form2.field.LabelField;
import org.geocraft.ui.form2.field.RadioGroupField;


/**
 * A view of the mapper model for loading ModSpec grids.
 */
public class GridLoadView extends AbstractModelView {

  /**
   * Creates a view of the ModSpec grid mapper model for loading.
   */
  public GridLoadView() {
    // No action required.
  }

  @Override
  public void buildView(final IModelForm form) {
    // Add the main section.
    FormSection section = form.addSection("ModSpec Grid Properties", false);

    String comment = "ModSpec grids are stored on disk in one of two file formats, ASCII or binary. ";
    comment += "Neither contains unit information, so the x/y and data units must be specified upon loading. ";
    comment += "The other information shown below cannot be changed and is for informational purposes only.";
    section.addCommentField(comment);

    LabelField directory = section.addLabelField(GridMapperModel.DIRECTORY);
    directory.setTooltip("The storage directory for the ModSpec grid file");

    LabelField fileName = section.addLabelField(GridMapperModel.FILE_NAME);
    fileName.setTooltip("The ModSpec grid file name");

    Unit[] xyUnits = Unit.getCommonUnitsByDomain(Domain.DISTANCE);
    RadioGroupField xyUnit = section.addRadioGroupField(GridMapperModel.XY_UNIT, xyUnits);
    xyUnit.setTooltip("The unit of measurement for x,y values in the ModSpec grid file");

    Unit[] someUnits = Unit.getCommonUnitsByDomain(new Domain[] { Domain.DISTANCE, Domain.TIME, Domain.VELOCITY,
        Domain.VELOCITY_GRADIENT });
    Unit[] allUnits = Unit.getUnitsByDomain(null);
    EnhancedComboField dataUnit = section.addEnhancedComboField(GridMapperModel.DATA_UNIT, someUnits, allUnits);
    dataUnit.setTooltip("The unit of measurement for data values in the ModSpec grid file");

    ComboField onsetType = section.addComboField(GridMapperModel.ONSET_TYPE, OnsetType.values());
    onsetType.setTooltip("The onset type of the grid (e.g. minimum, maximum, zero crossing, etc.)");

    LabelField fileFormat = section.addLabelField(GridMapperModel.FILE_FORMAT);
    fileFormat.setTooltip("The file format of the ModSpec grid file (ASCII or BINARY)");
    //fileFormat.setInput(new String[] { ModSpecGridConstants.ASCII_FORMAT, ModSpecGridConstants.BINARY_FORMAT });

    //ComboField orientation = section.addComboField(GridMapperModel.ORIENTATION, GridOrientation.values());
    //orientation.setTooltip("The grid orientation in the ModSpec grid file");
  }
}
