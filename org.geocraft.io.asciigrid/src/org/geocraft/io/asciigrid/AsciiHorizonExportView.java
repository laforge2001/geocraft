package org.geocraft.io.asciigrid;


import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.ui.form2.AbstractModelView;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.CheckboxField;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.LabelField;
import org.geocraft.ui.form2.field.SpinnerField;
import org.geocraft.ui.form2.field.TextField;


/**
 * A view of the mapper model for Ascii horizon files.
 */
public class AsciiHorizonExportView extends AbstractModelView implements AsciiFileConstants {

  /**
   * Creates a view of the ModSpec grid mapper model for saving.
   */
  public AsciiHorizonExportView() {
    // No action required.
  }

  @Override
  public void buildView(IModelForm form) {

    // Add the main section.
    FormSection section = form.addSection("Ascii Horizon Properties", false);

    // Add an editor for each of the model properties.
    LabelField directory = section.addLabelField(AsciiHorizonMapperModel.DIRECTORY);
    directory.setTooltip("The storage directory for the Ascii horizon file");

    SpinnerField numOfHorizons = section.addSpinnerField(AsciiHorizonMapperModel.NUM_OF_HORIZONS, 1, 5, 0, 1);
    numOfHorizons.setTooltip("Number of horizons to export into the same file");

    ComboField horizon1 = section.addEntityComboField(AsciiHorizonMapperModel.HORIZON1, Grid3d.class);
    horizon1.setTooltip("Select a horizon to export");

    ComboField horizon2 = section.addEntityComboField(AsciiHorizonMapperModel.HORIZON2, Grid3d.class);
    horizon2.setTooltip("Select a horizon to export into the same file as horizon1");
    horizon2.setEnabled(false);

    ComboField horizon3 = section.addEntityComboField(AsciiHorizonMapperModel.HORIZON3, Grid3d.class);
    horizon3.setTooltip("Select a horizon to export into the same file as horizon1");
    horizon3.setEnabled(false);

    ComboField horizon4 = section.addEntityComboField(AsciiHorizonMapperModel.HORIZON4, Grid3d.class);
    horizon4.setTooltip("Select a horizon to export into the same file as horizon1");
    horizon4.setEnabled(false);

    ComboField horizon5 = section.addEntityComboField(AsciiHorizonMapperModel.HORIZON5, Grid3d.class);
    horizon5.setTooltip("Select a horizon to export into the same file as horizon1");
    horizon5.setEnabled(false);

    ComboField aoi = section.addEntityComboField(AsciiHorizonMapperModel.AREA_OF_INTEREST, AreaOfInterest.class);
    aoi.setTooltip("Select an area-of-interest (optional).");
    aoi.setEnabled(false);

    CheckboxField useAOI = section.addCheckboxField(AsciiHorizonMapperModel.USE_AREA_OF_INTEREST);
    useAOI.setTooltip("Select an area-of-interest (optional).");

    TextField fileName = section.addTextField(AsciiHorizonMapperModel.FILE_NAME);
    fileName.setTooltip("The Ascii horizon file name");
  }

  @Override
  public void updateView(String key) {
    AsciiHorizonMapperModel model = (AsciiHorizonMapperModel) getModel();
    if (key.equals(AsciiHorizonMapperModel.USE_AREA_OF_INTEREST)) {
      boolean useAOI = model.getUseAreaOfInterest();
      setFieldEnabled(AsciiHorizonMapperModel.AREA_OF_INTEREST, useAOI);
    } else if (key.equals(AsciiHorizonMapperModel.NUM_OF_HORIZONS)) {
      int numOfHorizons = model.getNumOfHorizons();
      setFieldEnabled(AsciiHorizonMapperModel.HORIZON2, numOfHorizons > 1);
      setFieldEnabled(AsciiHorizonMapperModel.HORIZON3, numOfHorizons > 2);
      setFieldEnabled(AsciiHorizonMapperModel.HORIZON4, numOfHorizons > 3);
      setFieldEnabled(AsciiHorizonMapperModel.HORIZON5, numOfHorizons > 4);
    }
  }

}
