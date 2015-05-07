package org.geocraft.io.las;


import org.geocraft.core.model.IModel;
import org.geocraft.ui.form2.AbstractModelView;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.LabelField;
import org.geocraft.ui.form2.field.OrderedListField;
import org.geocraft.ui.form2.field.TextField;


public class WellExportView extends AbstractModelView {

  private OrderedListField _selectedLogNames;

  public WellExportView() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public void buildView(IModelForm modelForm) {
    // Add the main section.
    FormSection section = modelForm.addSection("LAS Well Properties", false);

    // Add an editor for each of the model properties.
    LabelField directory = section.addLabelField(WellMapperModel.DIRECTORY);
    directory.setTooltip("The storage directory for the ModSpec grid file");

    TextField fileName = section.addTextField(WellMapperModel.FILE_NAME);
    fileName.setTooltip("The ModSpec grid file name");

    FormSection depthTimeProp = modelForm.addSection("Depth/Time");

    LabelField begDepthDef = depthTimeProp.addLabelField(WellMapperModel.BEG_DEPTH);
    begDepthDef.setTooltip("Beginning depth read in from file");

    LabelField endDepthDef = depthTimeProp.addLabelField(WellMapperModel.END_DEPTH);
    endDepthDef.setTooltip("Ending depth read in from file");

    LabelField stepDef = depthTimeProp.addLabelField(WellMapperModel.STEP);
    stepDef.setTooltip("default step value read in from file");

    FormSection logProperties = modelForm.addSection("Log Properties");

    _selectedLogNames = logProperties.addOrderedListField(WellMapperModel.SELECTED_COLUMN_NAMES, new String[0]);
    _selectedLogNames.setLabel("Logs");

  }

  @Override
  public void setModel(IModel model) {
    if (_selectedLogNames != null) {
      _selectedLogNames.setOptions(((WellMapperModel) model).getColumnNames());
    }
    super.setModel(model);
  }
}
