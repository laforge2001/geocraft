package org.geocraft.io.ascii.aoi;


import org.geocraft.ui.form2.AbstractModelView;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.LabelField; 
import org.geocraft.ui.form2.field.TextField;


/**
 * A view of the mapper model for ASCII AOIs.
 */
public class AsciiAOIExportView extends AbstractModelView {

  /**
   * Creates a view of the ASCII AOI mapper model for saving.
   */
  public AsciiAOIExportView() {
    // No action required.
  }

  @Override
  public void buildView(final IModelForm form) {
    // Add the main section.
    FormSection section = form.addSection("ASCII AOI Properties", false);

    // Add an editor for each of the model properties.
    LabelField directory = section.addLabelField(AsciiAOIMapperModel.DIRECTORY);
    directory.setTooltip("The storage directory for the ASCII AOI file");

    TextField fileName = section.addTextField(AsciiAOIMapperModel.FILE_NAME);
    fileName.setTooltip("The ASCII AOI file name");

    LabelField aoiType = section.addLabelField(AsciiAOIMapperModel.AOI_TYPE);
    aoiType.setTooltip("The type of AOI (e.g. Map Polygon AOI, Seismic Survey 3D AOI, etc.");

  }
}
