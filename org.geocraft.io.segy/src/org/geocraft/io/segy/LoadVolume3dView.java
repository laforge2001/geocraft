/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.io.segy.SegyTraceIndex.IndexType;
import org.geocraft.ui.common.TableWrapLayoutHelper;
import org.geocraft.ui.form2.AbstractModelView;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.CheckboxField;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.EnhancedComboField;
import org.geocraft.ui.form2.field.LabelField;
import org.geocraft.ui.form2.field.TextField;


/**
 * The view of the model properties used to load a 3D SEG-Y volume.
 */
public class LoadVolume3dView extends AbstractModelView {

  public LoadVolume3dView() {
    // The no argument constructor for OSGI.
  }

  @Override
  public void buildView(IModelForm form) {
    // Add the general section.
    FormSection general = form.addSection("SEG-Y Volume Properties");

    // Add an editor for each of the model properties.
    LabelField directory = general.addLabelField(VolumeMapperModel.DIRECTORY);
    directory.setTooltip("The storage directory for the SEG-Y volume file");

    LabelField fileName = general.addLabelField(VolumeMapperModel.FILE_NAME);
    fileName.setTooltip("The SEG-Y volume file name");
    //fileName.setRequired(true);

    LabelField fileExtn = general.addLabelField(VolumeMapperModel.FILE_EXTN);
    fileExtn.setTooltip("The SEG-Y volume file extension");

    general.addCommentField("The SEG-Y format contains no information as to "
        + "what type of volume is represented (PostStack3d, PreStack3d, etc), so this must "
        + "be specified upon loading. ");

    IndexType[] volumeTypes = { IndexType.POSTSTACK_3D };
    ComboField volumeType = general.addComboField(VolumeMapperModel.VOLUME_TYPE, volumeTypes);
    volumeType.setTooltip("The type of SEG-Y volume.");

    LabelField storageOrder = general.addLabelField(VolumeMapperModel.STORAGE_ORDER);
    storageOrder.setTooltip("The storage order of the SEG-Y volume");

    general.addCommentField("Due to the inherent restrictions of the format, which limits the maximum sample rate, "
        + "the sample rate field has been made editable to allow for arbitrary values.");

    TextField sampleRate = general.addTextField(VolumeMapperModel.SAMPLE_RATE);
    sampleRate.setTooltip("The sample rate of the SEG-Y volume.");

    general.addCommentField("Unit information for z, x/y, and the data is also editable, as the SEG-Y format is "
        + "often not properly defined when files are written.");

    Unit[] zUnits = { Unit.MILLISECONDS, Unit.US_SURVEY_FOOT, Unit.FOOT, Unit.METER };
    ComboField zUnit = general.addComboField(VolumeMapperModel.UNIT_OF_Z, zUnits);
    zUnit.setTooltip("The unit of measurement for z values in SEG-Y volume file");

    Unit[] xyUnits = { Unit.METER, Unit.FOOT, Unit.US_SURVEY_FOOT };
    ComboField xyUnit = general.addComboField(VolumeMapperModel.UNIT_OF_XY, xyUnits);
    xyUnit.setTooltip("The unit of measurement for x,y values in SEG-Y volume file");

    Unit[] someUnits = Unit.getCommonUnitsByDomain(new Domain[] { Domain.VELOCITY, Domain.DIMENSIONLESS,
        Domain.VELOCITY_GRADIENT, Domain.DISTANCE, Domain.TIME });
    Unit[] allUnits = Unit.getUnitsByDomain(null);
    EnhancedComboField dataUnit = general.addEnhancedComboField(VolumeMapperModel.DATA_UNIT, someUnits, allUnits);
    dataUnit.setTooltip("The unit of measurement for data values values in SEG-Y volume file");

    LabelField sampleFormat = general.addLabelField(VolumeMapperModel.SAMPLE_FORMAT);
    sampleFormat.setTooltip("The sample format of the SEG-Y volume file");

    FormSection traceHeaders = form.addSection("SEG-Y Volume Trace Headers");

    traceHeaders
        .addCommentField("An option has been supplied below to allow for previewing the EBCDIC, binary and trace headers, so that these "
            + "byte locations can be determined, if not already known.");

    Button button = new Button(traceHeaders.getComposite(), SWT.PUSH);
    button.setText("Preview...");
    button.setLayoutData(TableWrapLayoutHelper.createLayoutData(false, false, SWT.FILL, SWT.FILL, 4, 1));
    button.addListener(SWT.Selection, new Listener() {

      @Override
      public void handleEvent(Event event) {
        Shell shell = Display.getDefault().getActiveShell();
        SegyPreviewDialog dialog = new SegyPreviewDialog(shell);
        dialog.create();
        dialog.getShell().setSize(500, 500);
        dialog.setBlockOnOpen(true);
        VolumeMapperModel model = (VolumeMapperModel) getModel();
        dialog.getShell().setText("SEG-Y Preview: " + model.getFileName() + model.getFileExtension());
        dialog.setFile(model);
        dialog.open();
      }

    });

    traceHeaders
        .addCommentField("The SEG-Y format does not provide definite trace header locations for storing the inline and crossline numbers. "
            + "These locations must be specified upon loading.\nFor SEG-Y rev1, the inline,xline coordinates are stored "
            + "at following bytes # locations:\nInline = 189\nX-line = 193");

    TextField inlineByteLoc = traceHeaders.addTextField(VolumeMapperModel.INLINE_BYTE_LOC);
    inlineByteLoc.setTooltip("The byte location of the inline # in the SEG-Y trace headers.");

    TextField xlineByteLoc = traceHeaders.addTextField(VolumeMapperModel.XLINE_BYTE_LOC);
    xlineByteLoc.setTooltip("The byte location of the xline # in the SEG-Y trace headers.");

    traceHeaders.addCommentField("For SEG-Y rev1, the x,y coordinates are stored "
        + " at the following byte # locations:\nX-Coordinate = 181\nY-Coordinate = 185");

    TextField xcoordByteLoc = traceHeaders.addTextField(VolumeMapperModel.X_COORD_BYTE_LOC);
    xcoordByteLoc.setTooltip("The byte location of the x-coordinate in the SEG-Y trace headers.");

    TextField ycoordByteLoc = traceHeaders.addTextField(VolumeMapperModel.Y_COORD_BYTE_LOC);
    ycoordByteLoc.setTooltip("The byte location of the y-coordinate in the SEG-Y trace headers.");

    traceHeaders
        .addCommentField("If the volume being loaded is prestack, then the field for specifying the offset is enabled. "
            + "This is defined in the SEG-Y header as being at byte 37, but has been made editable to allow for flexibility. ");

    TextField offsetByteLoc = traceHeaders.addTextField(VolumeMapperModel.OFFSET_BYTE_LOC);
    offsetByteLoc.setTooltip("The byte location of the offset # in the SEG-Y trace headers.");

    FormSection geometry3d = form.addSection("SEG-Y Volume 3D Geometry");

    String comment = "The geometry of a 3D SEG-Y volume can be automatically determined by scanning ";
    comment += "the x/y coordinates of the trace headers. This approach requires that the 4 corner traces ";
    comment += "exist in the file. If this is not the case, then the auto-calculate option should be turned off ";
    comment += "and the corner coordinates can then be entered manually.";
    geometry3d.addCommentField(comment);

    CheckboxField autoGeometry = geometry3d.addCheckboxField(VolumeMapperModel.AUTO_CALCULATE_GEOMETRY);
    autoGeometry.setTooltip("Calculates the geometry from the x,y coordinates of the 4 corner traces.");

    TextField inlineStart = geometry3d.addTextField(VolumeMapperModel.INLINE_START);
    inlineStart.setTooltip("The starting inline number");

    TextField inlineEnd = geometry3d.addTextField(VolumeMapperModel.INLINE_END);
    inlineEnd.setTooltip("The ending inline number");

    TextField inlineDelta = geometry3d.addTextField(VolumeMapperModel.INLINE_DELTA);
    inlineDelta.setTooltip("The increment of the inline numbers");

    TextField xlineStart = geometry3d.addTextField(VolumeMapperModel.XLINE_START);
    xlineStart.setTooltip("The starting xline number");

    TextField xlineEnd = geometry3d.addTextField(VolumeMapperModel.XLINE_END);
    xlineEnd.setTooltip("The ending xline number");

    TextField xlineDelta = geometry3d.addTextField(VolumeMapperModel.XLINE_DELTA);
    xlineDelta.setTooltip("The increment of the xline numbers");

    TextField x0 = geometry3d.addTextField(Volume3dMapperModel.X0);
    x0.setTooltip("The x-coordinate @ inline start, xline start");

    TextField y0 = geometry3d.addTextField(Volume3dMapperModel.Y0);
    y0.setTooltip("The y-coordinate @ inline start, xline start");

    TextField x1 = geometry3d.addTextField(Volume3dMapperModel.X1);
    x1.setTooltip("The x-coordinate @ inline end, xline start");

    TextField y1 = geometry3d.addTextField(Volume3dMapperModel.Y1);
    y1.setTooltip("The y-coordinate @ inline end, xline start");

    TextField x2 = geometry3d.addTextField(Volume3dMapperModel.X2);
    x2.setTooltip("The x-coordinate @ inline start, xline end");

    TextField y2 = geometry3d.addTextField(Volume3dMapperModel.Y2);
    y2.setTooltip("The y-coordinate @ inline start, xline end");

    TextField x3 = geometry3d.addTextField(Volume3dMapperModel.X3);
    x3.setTooltip("The x-coordinate @ inline end, xline end");

    TextField y3 = geometry3d.addTextField(Volume3dMapperModel.Y3);
    y3.setTooltip("The y-coordinate @ inline end, xline end");
  }

  @Override
  public void updateView(String key) {
    VolumeMapperModel model = (VolumeMapperModel) getModel();
    if (key.equals(VolumeMapperModel.VOLUME_TYPE)) {
      IndexType volumeType = model.getVolumeType();
      boolean showInlineXline = volumeType != null;
      boolean showOffset = volumeType != null && volumeType == IndexType.PRESTACK_3D;
      setFieldEnabled(VolumeMapperModel.INLINE_BYTE_LOC, showInlineXline);
      setFieldEnabled(VolumeMapperModel.XLINE_BYTE_LOC, showInlineXline);
      setFieldEnabled(VolumeMapperModel.OFFSET_BYTE_LOC, showOffset);
    } else if (key.equals(VolumeMapperModel.AUTO_CALCULATE_GEOMETRY)) {
      boolean showFields = !model.getAutoCalculateGeometry();
      setFieldEnabled(VolumeMapperModel.INLINE_START, showFields);
      setFieldEnabled(VolumeMapperModel.INLINE_END, showFields);
      setFieldEnabled(VolumeMapperModel.INLINE_DELTA, showFields);
      setFieldEnabled(VolumeMapperModel.XLINE_START, showFields);
      setFieldEnabled(VolumeMapperModel.XLINE_END, showFields);
      setFieldEnabled(VolumeMapperModel.XLINE_DELTA, showFields);
      setFieldEnabled(Volume3dMapperModel.X0, showFields);
      setFieldEnabled(Volume3dMapperModel.Y0, showFields);
      setFieldEnabled(Volume3dMapperModel.X1, showFields);
      setFieldEnabled(Volume3dMapperModel.Y1, showFields);
      setFieldEnabled(Volume3dMapperModel.X2, showFields);
      setFieldEnabled(Volume3dMapperModel.Y2, showFields);
      setFieldEnabled(Volume3dMapperModel.X3, showFields);
      setFieldEnabled(Volume3dMapperModel.Y3, showFields);
    }
  }
}
