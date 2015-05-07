package org.geocraft.internal.ui.mapviewer.viewer.action;


import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.IModel;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.io.ascii.aoi.AsciiAOIConstants;
import org.geocraft.io.ascii.aoi.AsciiAOIMapperModel;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.ModelDialog;
import org.geocraft.ui.form2.field.CheckboxField;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.LabelField;
import org.geocraft.ui.form2.field.TextField;


public class AsciiAOIExportDialog extends ModelDialog {

  private final String _aoiName;

  public AsciiAOIExportDialog(final Shell shell, final String aoiName) {
    super(shell, "Save ASCII AOI");
    _aoiName = aoiName;
  }

  @Override
  protected void applySettings() {
    // Nothing to do.
    //    AsciiAOIMapperModel mapperModel = (AsciiAOIMapperModel) _model;
    //    String aoiName = mapperModel.getFileName();
    //
    //    MapPolygonAOI aoi = new MapPolygonAOI(aoiName);
    //    // TODO get the units from the map view. _mapView.getPlotModel().getAxisX().getUnits());
    //    int numPoints = _polygon.getPointCount();
    //    double[] x = new double[numPoints];
    //    double[] y = new double[numPoints];
    //    for (int i = 0; i < numPoints; i++) {
    //      x[i] = _polygon.getPoint(i).getX();
    //      y[i] = _polygon.getPoint(i).getY();
    //    }
    //    aoi.addInclusionPolygon(x, y);
    //    aoi.setLastModifiedDate(new Timestamp(System.currentTimeMillis()));
    //
    //    if (mapperModel.existsInStore()) {
    //      MessageDialog.openError(_shell, "ASCII AOI Error", "An ASCII AOI file with this name already exists:\n"
    //          + mapperModel.getFilePath());
    //    }
    //
    //    AsciiAOIExportTask task = new AsciiAOIExportTask();
    //    task.setMapperModel(mapperModel);
    //    task.setEntity(aoi);
    //    try {
    //      TaskRunner.runTask(task, "Writing ASCII AOI", TaskRunner.JOIN);
    //      aoi = new MapPolygonAOI(aoiName, new AsciiAOIMapper(mapperModel));
    //      aoi.load();
    //      ServiceProvider.getRepository().add(aoi);
    //      _viewer.getPlot().getActiveModelSpace().removeLayer(_polygon.getLayer());
    //      _viewer.addObjects(false, new Object[] { aoi });
    //    } catch (Exception ex) {
    //      ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.getMessage());
    //    }
  }

  @Override
  protected void buildModelForms(final IModelForm[] forms) {
    // Add the main section.
    FormSection section = forms[0].addSection("ASCII AOI Properties", false);

    // Add an editor for each of the model properties.
    LabelField directory = section.addLabelField(AsciiAOIMapperModel.DIRECTORY);
    directory.setTooltip("The storage directory for the ASCII AOI file");

    TextField fileName = section.addTextField(AsciiAOIMapperModel.FILE_NAME);
    fileName.setTooltip("The ASCII AOI file name");

    CheckboxField zRangeField = section.addCheckboxField(AsciiAOIMapperModel.HAS_Z_RANGE);
    zRangeField.setTooltip("The flag indicating if the AOI has an associated z-range.");

    TextField zStartField = section.addTextField(AsciiAOIMapperModel.Z_START);
    zStartField.setEnabled(false);

    TextField zEndField = section.addTextField(AsciiAOIMapperModel.Z_END);
    zEndField.setEnabled(false);

    Unit[] zUnits = { Unit.MILLISECONDS, Unit.FOOT, Unit.METER };
    ComboField zUnitField = section.addComboField(AsciiAOIMapperModel.Z_UNIT, zUnits);
    zUnitField.setEnabled(false);
  }

  @Override
  public void propertyChanged(final String triggerKey) {
    super.propertyChanged(triggerKey);
    AsciiAOIMapperModel model = getModel();
    if (triggerKey != null && triggerKey.equals(AsciiAOIMapperModel.HAS_Z_RANGE)) {
      boolean hasZRange = model.getZRangeFlag();
      setFieldEnabled(AsciiAOIMapperModel.Z_START, hasZRange);
      setFieldEnabled(AsciiAOIMapperModel.Z_END, hasZRange);
      setFieldEnabled(AsciiAOIMapperModel.Z_UNIT, hasZRange);
    }
  }

  @Override
  protected IModel createModel() {
    AsciiAOIMapperModel mapperModel = new AsciiAOIMapperModel();
    mapperModel.setAOIType(AsciiAOIConstants.MAP_POLYGON_AOI);
    mapperModel.setDirectory(Platform.getLocation().toString());
    mapperModel.setFileName(_aoiName);
    mapperModel.setIOMode(IOMode.OUTPUT);
    return mapperModel;
  }

  @Override
  protected int getNumForms() {
    return 1;
  }

  @Override
  public void createButtonsForButtonBar(final Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
  }

  public AsciiAOIMapperModel getModel() {
    return (AsciiAOIMapperModel) _model;
  }
}
