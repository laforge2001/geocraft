/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.javaseis;


import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.ui.form2.AbstractModelView;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.EnhancedComboField;
import org.geocraft.ui.form2.field.LabelField;


/**
 * The view of the model properties used to load a JavaSeis volume.
 */
public class VolumeLoadView extends AbstractModelView {

  public VolumeLoadView() {
    // The no argument constructor for OSGI.
  }

  @Override
  public void buildView(IModelForm form) {
    // Add the main section.
    FormSection section = form.addSection("JavaSeis Volume Properties");

    LabelField directory = section.addLabelField(VolumeMapperModel.DIRECTORY);
    directory.setTooltip("The storage directory for the JavaSeis volume");

    LabelField fileName = section.addLabelField(VolumeMapperModel.FILE_NAME);
    fileName.setTooltip("The JavaSeis volume name");

    LabelField volumeType = section.addLabelField(VolumeMapperModel.VOLUME_TYPE);
    volumeType.setTooltip("The type of volume (PostStack3d, PreStack3d, etc)");

    LabelField storageOrder = section.addLabelField(VolumeMapperModel.STORAGE_ORDER);
    storageOrder.setTooltip("The storage order of the JavaSeis volume");

    section.addCommentField("The unit of measurement for the data is not defined in the JavaSeis format "
        + "and must be specified. The default unit is seismic amplitude, which is dimensionless");

    Unit[] someUnits = Unit.getCommonUnitsByDomain(new Domain[] { Domain.VELOCITY, Domain.DIMENSIONLESS,
        Domain.VELOCITY_GRADIENT });
    Unit[] allUnits = Unit.getUnitsByDomain(null);
    EnhancedComboField dataUnit = section.addEnhancedComboField(VolumeMapperModel.DATA_UNIT, someUnits, allUnits);
    dataUnit.setTooltip("The unit of measurement for data values values in JavaSeis volume");

    LabelField dataFormat = section.addLabelField(VolumeMapperModel.DATA_FORMAT);
    dataFormat.setTooltip("The format of the JavaSeis data");

    LabelField secnStorage = section.addLabelField(VolumeMapperModel.USE_SECONDARY_STORAGE);
    secnStorage.setTooltip("Toggled on indicates data storage in secondary locations");

    FormSection geometry = form.addSection("JavaSeis Volume 3D Geometry");

    LabelField inlineStart = geometry.addLabelField(VolumeMapperModel.INLINE_START);
    inlineStart.setTooltip("The starting inline number");

    LabelField inlineEnd = geometry.addLabelField(VolumeMapperModel.INLINE_END);
    inlineEnd.setTooltip("The ending inline number");

    LabelField inlineDelta = geometry.addLabelField(VolumeMapperModel.INLINE_DELTA);
    inlineDelta.setTooltip("The increment of the inline numbers");

    LabelField xlineStart = geometry.addLabelField(VolumeMapperModel.XLINE_START);
    xlineStart.setTooltip("The starting xline number");

    LabelField xlineEnd = geometry.addLabelField(VolumeMapperModel.XLINE_END);
    xlineEnd.setTooltip("The ending xline number");

    LabelField xlineDelta = geometry.addLabelField(VolumeMapperModel.XLINE_DELTA);
    xlineDelta.setTooltip("The increment of the xline numbers");

    ComboField xyUnit = geometry.addComboField(VolumeMapperModel.UNIT_OF_XY, new Unit[] { Unit.FOOT, Unit.METER });
    xyUnit.setTooltip("The x,y unit of measurement defined in the file");

    LabelField x0 = geometry.addLabelField(VolumeMapperModel.X0);
    x0.setTooltip("The x-coordinate @ inline start, xline start");

    LabelField y0 = geometry.addLabelField(VolumeMapperModel.Y0);
    y0.setTooltip("The y-coordinate @ inline start, xline start");

    LabelField x1 = geometry.addLabelField(VolumeMapperModel.X1);
    x1.setTooltip("The x-coordinate @ inline end, xline start");

    LabelField y1 = geometry.addLabelField(VolumeMapperModel.Y1);
    y1.setTooltip("The y-coordinate @ inline end, xline start");

    LabelField x2 = geometry.addLabelField(VolumeMapperModel.X2);
    x2.setTooltip("The y-coordinate @ inline start, xline end");

    LabelField y2 = geometry.addLabelField(VolumeMapperModel.Y2);
    y2.setTooltip("The y-coordinate @ inline start, xline end");
  }

  @Override
  public void updateView(String key) {
    VolumeMapperModel model = (VolumeMapperModel) getModel();
    boolean binGridMissing = !model.getBinGridExists();
    setFieldEnabled(VolumeMapperModel.X0, binGridMissing);
    setFieldEnabled(VolumeMapperModel.Y0, binGridMissing);
    setFieldEnabled(VolumeMapperModel.X1, binGridMissing);
    setFieldEnabled(VolumeMapperModel.Y1, binGridMissing);
    setFieldEnabled(VolumeMapperModel.X2, binGridMissing);
    setFieldEnabled(VolumeMapperModel.Y2, binGridMissing);
  }
  //  @Override
  //  public void updateUI() {
  //    JavaSeisVolumeMapperModel model = (JavaSeisVolumeMapperModel) getModel();
  //    if (key.equals(JavaSeisVolumeMapperModel.BIN_GRID_EXISTS)) {
  //
  //      boolean binGridExists = model.getBinGridExists();
  //      System.out.println("BinGridExists=" + binGridExists);
  //
  //      getField(JavaSeisVolumeMapperModel.X0).setEnabled(!binGridExists);
  //      getField(JavaSeisVolumeMapperModel.Y0).setEnabled(!binGridExists);
  //      getField(JavaSeisVolumeMapperModel.X1).setEnabled(!binGridExists);
  //      getField(JavaSeisVolumeMapperModel.Y1).setEnabled(!binGridExists);
  //      getField(JavaSeisVolumeMapperModel.X2).setEnabled(!binGridExists);
  //      getField(JavaSeisVolumeMapperModel.Y2).setEnabled(!binGridExists);
  //    }
  //  }

}
