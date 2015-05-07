package org.geocraft.io.javaseis;


import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.geocraft.core.model.IModel;
import org.geocraft.ui.form2.AbstractModelView;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.CheckboxField;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.LabelField;
import org.geocraft.ui.form2.field.OrderedListField;
import org.geocraft.ui.form2.field.TextField;
import org.javaseis.properties.DataFormat;


public class VolumeExportView extends AbstractModelView {

  private Button _editButton;

  /**
   * Creates a view of the JavaSeis volume mapper model for loading.
   */
  public VolumeExportView() {
    // The no argument constructor for OSGI.
  }

  @Override
  public void buildView(IModelForm form) {
    // Add the main section.
    FormSection section = form.addSection("JavaSeis Volume Properties");

    LabelField directory = section.addLabelField(VolumeMapperModel.DIRECTORY);
    directory.setTooltip("The storage directory for the JavaSeis volume");

    TextField fileName = section.addTextField(VolumeMapperModel.FILE_NAME);
    fileName.setTooltip("The JavaSeis volume name");

    LabelField storageOrder = section.addLabelField(VolumeMapperModel.STORAGE_ORDER);
    storageOrder.setTooltip("The storage order of the JavaSeis volume");
    //storageOrder.setInput(StorageOrder.valuesAsStrings());

    LabelField dataUnit = section.addLabelField(VolumeMapperModel.DATA_UNIT);
    dataUnit.setLabel("Data Unit");
    dataUnit.setTooltip("The unit of measurement for data values values in JavaSeis volume");

    ComboField dataFormat = section.addComboField(VolumeMapperModel.DATA_FORMAT, new String[] {
        DataFormat.FLOAT.toString(), DataFormat.INT16.toString(), DataFormat.INT08.toString(),
        DataFormat.COMPRESSED_INT16.toString(), DataFormat.COMPRESSED_INT08.toString() });
    dataFormat.setTooltip("The format of the JavaSeis data");

    section = form.addSection("Secondary Storage", false);

    CheckboxField secnStorage = section.addCheckboxField(VolumeMapperModel.USE_SECONDARY_STORAGE);
    secnStorage.setTooltip("Toggle on to store trace/header data in secondary locations");

    _editButton = section.addPushButton("Edit list of available secondary storage locations...");

    String[] virtualFolders = SecondaryStorage.get();
    final OrderedListField secnLocation = section.addOrderedListField(VolumeMapperModel.SECONDARY_STORAGE_LOCATIONS,
        virtualFolders);
    secnLocation.setTooltip("Select the locations to use for secondary storage of trace/header data");

    TextField numExtents = section.addTextField(VolumeMapperModel.NUM_EXTENTS);
    numExtents.setTooltip("Specify the # of trace/header extents to create");

    _editButton.addListener(SWT.Selection, new Listener() {

      public void handleEvent(Event event) {
        final SecondaryStorageDialog dialog = new SecondaryStorageDialog(VolumeExportView.this.getManagedForm()
            .getForm().getShell());
        dialog.create();
        dialog.getShell().setText("JavaSeis Secondary Storage");
        dialog.getShell().pack();
        Point size = dialog.getShell().computeSize(500, 400);
        dialog.getShell().setSize(size);
        int rtn = dialog.open();
        if (rtn == Window.OK) {
          secnLocation.setOptions(SecondaryStorage.get());
        }
      }

    });
  }

  @Override
  public void modelFormUpdated(final String triggerKey) {
    super.modelFormUpdated(triggerKey);
    IModel model = getModel();
    if (model != null) {
      boolean useSecnStorage = ((VolumeMapperModel) model).getSecondaryStorageFlag();
      setFieldEnabled(VolumeMapperModel.SECONDARY_STORAGE_LOCATIONS, useSecnStorage);
      setFieldEnabled(VolumeMapperModel.NUM_EXTENTS, useSecnStorage);
      _editButton.setEnabled(useSecnStorage);
    }
  }
}
