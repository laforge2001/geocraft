/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import org.geocraft.core.model.IModel;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.io.segy.SegyTraceIndex.IndexType;
import org.geocraft.ui.form2.AbstractModelView;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.LabelField;
import org.geocraft.ui.form2.field.TextField;


/**
 * The view of the SEG-Y mapper model for saving.
 */
public class ExportVolume3dView extends AbstractModelView {

  private ComboField _storageOrder;

  /**
   * Creates a view of the SEG-Y volume mapper model for saving.
   */
  public ExportVolume3dView() {
    // The no argument constructor for OSGI.
  }

  @Override
  public void buildView(IModelForm form) {
    // Add the general section.
    FormSection general = form.addSection("SEG-Y Volume Properties");

    // Add an editor for each of the model properties.
    LabelField directory = general.addLabelField(VolumeMapperModel.DIRECTORY);
    directory.setTooltip("The storage directory for the SEG-Y volume file");

    TextField fileName = general.addTextField(VolumeMapperModel.FILE_NAME);
    fileName.setTooltip("The SEG-Y volume file name");

    String[] extensions = new String[] { ".segy", ".sgy" };
    ComboField fileExtn = general.addComboField(VolumeMapperModel.FILE_EXTN, extensions);
    fileExtn.setTooltip("The SEG-Y volume file extension");

    LabelField volumeType = general.addLabelField(VolumeMapperModel.VOLUME_TYPE);
    volumeType.setTooltip("The type of SEG-Y volume.");

    Object[] storageOrders = new Object[0];
    _storageOrder = general.addComboField(VolumeMapperModel.STORAGE_ORDER, storageOrders);
    _storageOrder.setTooltip("The storage order of the SEG-Y volume");

    LabelField sampleRate = general.addLabelField(VolumeMapperModel.SAMPLE_RATE);
    sampleRate.setTooltip("The sample rate of the SEG-Y volume.");

    LabelField zUnit = general.addLabelField(VolumeMapperModel.UNIT_OF_Z);
    zUnit.setTooltip("The unit of measurement for z values in SEG-Y volume file");

    LabelField dataUnit = general.addLabelField(VolumeMapperModel.DATA_UNIT);
    dataUnit.setTooltip("The unit of measurement for data values in the SEG-Y volume file");

    Unit[] xyUnits = Unit.getCommonUnitsByDomain(Domain.DISTANCE);
    ComboField xyUnit = general.addComboField(VolumeMapperModel.UNIT_OF_XY, xyUnits);
    xyUnit.setTooltip("The unit of measurement for x,y values in SEG-Y volume file");

    ComboField sampleFormat = general.addComboField(VolumeMapperModel.SAMPLE_FORMAT, SegyUtil.getSampleFormats());
    sampleFormat.setTooltip("The sample format of the SEG-Y volume file");

    FormSection traceHeaders = form.addSection("SEG-Y Volume Trace Headers");

    TextField inlineByteLoc = traceHeaders.addTextField(VolumeMapperModel.INLINE_BYTE_LOC);
    inlineByteLoc.setTooltip("The byte location of the inline # in the SEG-Y trace headers.");

    TextField xlineByteLoc = traceHeaders.addTextField(VolumeMapperModel.XLINE_BYTE_LOC);
    xlineByteLoc.setTooltip("The byte location of the xline # in the SEG-Y trace headers.");

    TextField offsetByteLoc = traceHeaders.addTextField(VolumeMapperModel.OFFSET_BYTE_LOC);
    offsetByteLoc.setTooltip("The byte location of the offset # in the SEG-Y trace headers.");

    TextField xcoordByteLoc = traceHeaders.addTextField(VolumeMapperModel.X_COORD_BYTE_LOC);
    xcoordByteLoc.setTooltip("The byte location of the x-coordinate in the SEG-Y trace headers.");

    TextField ycoordByteLoc = traceHeaders.addTextField(VolumeMapperModel.Y_COORD_BYTE_LOC);
    ycoordByteLoc.setTooltip("The byte location of the y-coordinate in the SEG-Y trace headers.");

    FormSection geometry3d = form.addSection("SEG-Y Volume 3D Geometry");

    LabelField inlineStart = geometry3d.addLabelField(VolumeMapperModel.INLINE_START);
    inlineStart.setTooltip("The starting inline number");
    LabelField inlineEnd = geometry3d.addLabelField(VolumeMapperModel.INLINE_END);
    inlineEnd.setTooltip("The ending inline number");
    LabelField inlineDelta = geometry3d.addLabelField(VolumeMapperModel.INLINE_DELTA);
    inlineDelta.setTooltip("The increment of the inline numbers");
    LabelField xlineStart = geometry3d.addLabelField(VolumeMapperModel.XLINE_START);
    xlineStart.setTooltip("The starting xline number");
    LabelField xlineEnd = geometry3d.addLabelField(VolumeMapperModel.XLINE_END);
    xlineEnd.setTooltip("The ending xline number");
    LabelField xlineDelta = geometry3d.addLabelField(VolumeMapperModel.XLINE_DELTA);
    xlineDelta.setTooltip("The increment of the xline numbers");

    LabelField x0 = geometry3d.addLabelField(Volume3dMapperModel.X0);
    x0.setTooltip("X-Coordinate @ inline start, xline start");
    LabelField y0 = geometry3d.addLabelField(Volume3dMapperModel.Y0);
    y0.setTooltip("Y-Coordinate @ inline start, xline start");

    LabelField x1 = geometry3d.addLabelField(Volume3dMapperModel.X1);
    x1.setTooltip("X-Coordinate @ inline end, xline start");
    LabelField y1 = geometry3d.addLabelField(Volume3dMapperModel.Y1);
    y1.setTooltip("Y-Coordinate @ inline end, xline start");

    LabelField x2 = geometry3d.addLabelField(Volume3dMapperModel.X2);
    x2.setTooltip("X-Coordinate @ inline end, xline end");
    LabelField y2 = geometry3d.addLabelField(Volume3dMapperModel.Y2);
    y2.setTooltip("Y-Coordinate @ inline end, xline end");

    LabelField x3 = geometry3d.addLabelField(Volume3dMapperModel.X3);
    x3.setTooltip("X-Coordinate @ inline start, xline end");
    LabelField y3 = geometry3d.addLabelField(Volume3dMapperModel.Y3);
    y3.setTooltip("Y-Coordinate @ inline start, xline end");
  }

  @Override
  public void setModel(IModel model) {
    Volume3dMapperModel vmodel = (Volume3dMapperModel) model;
    Object[] storageOrders = new Object[0];
    if (vmodel.getVolumeType().equals(IndexType.POSTSTACK_3D)) {
      storageOrders = StorageOrder.valuesAsStrings();
    } else if (vmodel.getVolumeType().equals(IndexType.PRESTACK_3D)) {
      storageOrders = PreStack3d.StorageOrder.valuesAsStrings();
    } else {
      throw new RuntimeException("Only PostStack3d and PreStack3d currently supported.");
    }
    _storageOrder.setOptions(storageOrders);
    super.setModel(model);
  }
}
