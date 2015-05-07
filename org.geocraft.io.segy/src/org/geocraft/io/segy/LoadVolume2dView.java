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
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.EnhancedComboField;
import org.geocraft.ui.form2.field.LabelField;
import org.geocraft.ui.form2.field.TextField;


public class LoadVolume2dView extends AbstractModelView {

  public LoadVolume2dView() {
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

    IndexType[] volumeTypes = { IndexType.POSTSTACK_2D };
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
    inlineByteLoc.setTooltip("The byte location of the line # in the SEG-Y trace headers.");

    TextField cdpByteLoc = traceHeaders.addTextField(VolumeMapperModel.CDP_BYTE_LOC);
    cdpByteLoc.setTooltip("The byte location of the CDP # in the SEG-Y trace headers.");

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

  }

  @Override
  public void updateView(String key) {
    VolumeMapperModel model = (VolumeMapperModel) getModel();
    if (key.equals(VolumeMapperModel.VOLUME_TYPE)) {
      IndexType volumeType = model.getVolumeType();
      boolean showInlineXline = volumeType != null;
      boolean showOffset = volumeType != null && volumeType == IndexType.PRESTACK_2D;
      setFieldEnabled(VolumeMapperModel.INLINE_BYTE_LOC, showInlineXline);
      setFieldEnabled(VolumeMapperModel.CDP_BYTE_LOC, showInlineXline);
      setFieldEnabled(VolumeMapperModel.OFFSET_BYTE_LOC, showOffset);
    }
  }

}
