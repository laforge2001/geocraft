package org.geocraft.io.modspec;


import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.internal.io.modspec.ModSpecGridConstants;
import org.geocraft.ui.form2.AbstractModelView;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.DirectoryField;
import org.geocraft.ui.form2.field.RadioGroupField;
import org.geocraft.ui.form2.field.TextField;


/**
 * A view of the mapper model for ModSpec grids.
 */
public class GridExportView extends AbstractModelView implements ModSpecGridConstants {

  /**
   * Creates a view of the ModSpec grid mapper model for saving.
   */
  public GridExportView() {
    // No action required.
  }

  @Override
  public void buildView(final IModelForm form) {
    // Add the main section.
    FormSection section = form.addSection("ModSpec Grid Properties", false);

    // Add an editor for each of the model properties.
    DirectoryField directory = section.addDirectoryField(GridMapperModel.DIRECTORY, "");
    directory.setTooltip("The storage directory for the ModSpec grid file");

    TextField fileName = section.addTextField(GridMapperModel.FILE_NAME);
    fileName.setTooltip("The ModSpec grid file name");

    Unit[] xyUnits = Unit.getCommonUnitsByDomain(Domain.DISTANCE);
    ComboField xyUnit = section.addComboField(GridMapperModel.XY_UNIT, xyUnits);
    xyUnit.setTooltip("The unit of measurement for x,y values in the ModSpec grid file");

    ComboField dataUnit = section.addComboField(GridMapperModel.DATA_UNIT, Unit.values());
    dataUnit.setTooltip("The unit of measurement for data values in the ModSpec grid file");

    RadioGroupField fileFormat = section.addRadioGroupField(GridMapperModel.FILE_FORMAT, GridFileFormat.values());
    fileFormat.setTooltip("The format of the ModSpec grid file (ASCII or BINARY)");

    //    RadioGroupField orientation = section.addRadioGroupField(GridMapperModel.ORIENTATION, GridOrientation.values());
    //    orientation.setTooltip("The grid orientation in the ModSpec grid file");
  }
}
