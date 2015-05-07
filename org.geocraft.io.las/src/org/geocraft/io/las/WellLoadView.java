package org.geocraft.io.las;


import java.io.File;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.geocraft.core.model.IModel;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.internal.io.las.LasPreviewDialog;
import org.geocraft.ui.common.TableWrapLayoutHelper;
import org.geocraft.ui.form2.AbstractModelView;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.LabelField;
import org.geocraft.ui.form2.field.OrderedListField;
import org.geocraft.ui.form2.field.TableField;


public class WellLoadView extends AbstractModelView {

  private OrderedListField _selectedLogNames;

  public WellLoadView() {
    // intentionally blank
  }

  @Override
  public void buildView(IModelForm modelForm) {

    FormSection fileProperties = modelForm.addSection("LAS Log Properties");

    // Add an editor for each of the model properties.
    LabelField directory = fileProperties.addLabelField(WellMapperModel.DIRECTORY);
    directory.setTooltip("The storage directory for the LAS log file");

    LabelField fileName = fileProperties.addLabelField(WellMapperModel.FILE_NAME);
    fileName.setTooltip("The LAS log file name");

    Button button = new Button(fileProperties.getComposite(), SWT.PUSH);
    button.setText("Preview...");
    button.setLayoutData(TableWrapLayoutHelper.createLayoutData(false, false, TableWrapData.FILL, TableWrapData.FILL,
        2, 1));
    button.addListener(SWT.Selection, new Listener() {

      @Override
      public void handleEvent(Event event) {
        Shell shell = Display.getDefault().getActiveShell();
        final LasPreviewDialog dialog = new LasPreviewDialog(shell);
        dialog.create();
        dialog.getShell().setSize(750, 750);
        dialog.setBlockOnOpen(false);
        WellMapperModel model = (WellMapperModel) getModel();
        dialog.getShell().setText("LAS Preview: " + model.getDirectory() + File.separatorChar + model.getFileName());
        dialog.setFile(model);
        dialog.open();
      }
    });

    FormSection depthTimeProp = modelForm.addSection("Depth/Time");

    depthTimeProp.addComboField(WellMapperModel.DEPTH_TYPE, WellMapperModel.DepthType.values());

    LabelField depthUnitsLabel = depthTimeProp.addLabelField(WellMapperModel.DEPTH_UNITS);
    depthUnitsLabel.setTooltip("The units as listed in the LAS file");

    LabelField begDepthDef = depthTimeProp.addLabelField(WellMapperModel.BEG_DEPTH_DEFAULT);
    begDepthDef.setTooltip("Beginning depth read in from file");

    LabelField endDepthDef = depthTimeProp.addLabelField(WellMapperModel.END_DEPTH_DEFAULT);
    endDepthDef.setTooltip("Ending depth read in from file");

    LabelField stepDef = depthTimeProp.addLabelField(WellMapperModel.STEP_DEFAULT);
    stepDef.setTooltip("default step value read in from file");

    FormSection locationSection = modelForm.addSection("Location Information");

    locationSection.addTextField(WellMapperModel.XCOORD);
    locationSection.addTextField(WellMapperModel.YCOORD);
    locationSection.addTextField(WellMapperModel.HZCS);
    locationSection.addTextField(WellMapperModel.DATUM);

    FormSection logProperties = modelForm.addSection("Log Properties");

    _selectedLogNames = logProperties.addOrderedListField(WellMapperModel.SELECTED_COLUMN_NAMES, new String[0]);
    _selectedLogNames.setLabel("Logs");

  }

  @Override
  public void updateView(String key) {
    //    if (key.equals(WellMapperModel.SELECTED_COLUMN_NAMES)) {
    //      _selectedLogNames.updateField(((WellMapperModel) getModel())
    //          .getValueObject(WellMapperModel.SELECTED_COLUMN_NAMES));
    //    }
  }

  @Override
  public void setModel(IModel model) {
    if (_selectedLogNames != null) {
      _selectedLogNames.setOptions(((WellMapperModel) model).getColumnNames());
    }
    super.setModel(model);
  }

  /**
   * @param tableField 
   * @return
   */
  private CellEditor[] createEditors(final TableField tableField) {
    CellEditor[] editors = new CellEditor[6];

    final Table table = tableField.getTable();

    // Column 1 : Completed (Checkbox)
    editors[0] = new CheckboxCellEditor(null, SWT.CHECK | SWT.READ_ONLY);

    // Column 2 : Mnemonic (Free text)
    TextCellEditor textEditor = new TextCellEditor(table);
    ((Text) textEditor.getControl()).setTextLimit(60);
    editors[1] = textEditor;

    // Column 3 : Log Description (Free text)
    textEditor = new TextCellEditor(table);
    ((Text) textEditor.getControl()).setTextLimit(60);
    editors[2] = textEditor;

    //Column 4 : Las Unit symbol from file
    editors[3] = new TextCellEditor(table);

    // Column 4 : Geocraft Units (Combo Box)
    ComboBoxCellEditor comboEditor = new ComboBoxCellEditor(table, Unit.getListOfAllNames(), SWT.READ_ONLY);
    editors[4] = comboEditor;

    // Column 5 : Name (Text with digits only)
    textEditor = new TextCellEditor(table);

    editors[5] = textEditor;
    return editors;
  }
}
