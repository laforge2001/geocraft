/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.common.util.Utilities;
import org.geocraft.core.io.IDatastoreAccessorUtil;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.internal.io.segy.ServiceComponent;
import org.geocraft.io.segy.SegyTraceIndex.IndexType;


public class VolumeAccessorUtil implements IDatastoreAccessorUtil {

  public VolumeAccessorUtil() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public Map<Entity, MapperModel> mapEntitiesToModels(final Entity[] entities) {
    Map<Entity, MapperModel> map = new HashMap<Entity, MapperModel>();
    // Loop thru the entities, creating a subset by extracting out only the seismic dataset entities.
    Set<Entity> set = new HashSet<Entity>();
    for (Entity entity : entities) {
      if (entity instanceof SeismicDataset) {
        set.add(entity);
      }
    }

    // Loop thru the subset of seismic datasets, setting default datastore mapper properties for each.
    for (Entity entity : set.toArray(new Entity[0])) {
      SeismicDataset seismicDataset = (SeismicDataset) entity;
      VolumeMapperModel model;
      if (seismicDataset instanceof PostStack3d || seismicDataset instanceof PreStack3d) {
        model = new Volume3dMapperModel();
      } else if (seismicDataset instanceof PostStack2dLine) {
        model = new Volume2dMapperModel();
      } else {
        throw new IllegalArgumentException("Unsupported seismic dataset: " + seismicDataset);
      }
      model.setVolumeType(IndexType.lookupByName(entity.getClass().getSimpleName()));
      // Default the directory to the working directory.
      String directory = PreferencesUtil.getPreferencesStore(ServiceComponent.PLUGIN_ID).get("SaveSegyVolume_DIR",
          Utilities.getWorkingDirectory());
      model.setDirectory(directory);
      // Default the file name to the seismic dataset display name.
      model.setFileName(seismicDataset.getDisplayName());
      // Default the file extension to .segy.
      model.setFileExtension(".segy");
      // Default the storage order to the preferred order of the seismic dataset, if poststack3d.
      if (seismicDataset instanceof PostStack3d) {
        model.setStorageOrder(((PostStack3d) seismicDataset).getPreferredOrder().getTitle());
      } else if (seismicDataset instanceof PreStack3d) {
        model.setStorageOrder(((PreStack3d) seismicDataset).getPreferredOrder().getName());
      }
      // Default the sample rate to the sample rate of the seismic dataset.
      model.setSampleRate(seismicDataset.getZDelta());
      // Default the z units to the z units of the seismic dataset.
      model.setUnitOfZ(seismicDataset.getZUnit());
      // Default the x,y units to the application x,y units.
      model.setUnitOfXY(UnitPreferences.getInstance().getHorizontalDistanceUnit());
      // Default the data unit to the seismic dataset data unit.
      Unit dataUnit = seismicDataset.getDataUnit();
      // Default the data domain to the domain of the seismic dataset data unit.
      model.setDataUnit(dataUnit);
      // Default the sample format to IEEE.
      model.setSampleFormat(SegyBytes.SAMPLE_FORMAT_FLOAT_4BYTE_IEEE);
      // Default the CDP byte location.
      model.setCdpByteLoc(21);
      // Default the inline byte location.
      model.setInlineByteLoc(189);
      // Default the xline byte location.
      model.setXlineByteLoc(193);
      // Default the offset byte location.
      model.setOffsetByteLoc(37);
      // Default the x-coord byte location.
      model.setXcoordByteLoc(181);
      // Default the y-coord byte location.
      model.setYcoordByteLoc(185);
      model.setByteOrder(ByteOrder.nativeOrder().toString());

      if (seismicDataset instanceof PostStack3d) {
        PostStack3d ps3d = (PostStack3d) seismicDataset;
        float inlineStart = ps3d.getInlineStart();
        float inlineEnd = ps3d.getInlineEnd();
        float inlineDelta = ps3d.getInlineDelta();
        float xlineStart = ps3d.getXlineStart();
        float xlineEnd = ps3d.getXlineEnd();
        float xlineDelta = ps3d.getXlineDelta();
        model.setInlineStart((int) inlineStart);
        model.setInlineEnd((int) inlineEnd);
        model.setInlineDelta((int) inlineDelta);
        model.setXlineStart((int) xlineStart);
        model.setXlineEnd((int) xlineEnd);
        model.setXlineDelta((int) xlineDelta);
        SeismicSurvey3d geometry = ps3d.getSurvey();
        float[] inlines = { inlineStart, inlineEnd, inlineEnd, inlineStart };
        float[] xlines = { xlineStart, xlineStart, xlineEnd, xlineEnd };
        CoordinateSeries coords = geometry.transformInlineXlineToXY(inlines, xlines);
        Point3d[] points = coords.getPointsDirect();
        double[] xs = new double[4];
        double[] ys = new double[4];
        for (int i = 0; i < 4; i++) {
          xs[i] = points[i].getX();
          ys[i] = points[i].getY();
        }
        Volume3dMapperModel model3d = (Volume3dMapperModel) model;
        model3d.setX0(xs[0]);
        model3d.setY0(ys[0]);
        model3d.setX1(xs[1]);
        model3d.setY1(ys[1]);
        model3d.setX2(xs[2]);
        model3d.setY2(ys[2]);
        model3d.setX3(xs[3]);
        model3d.setY3(ys[3]);
      } else if (seismicDataset instanceof PreStack3d) {
        PreStack3d ps3d = (PreStack3d) seismicDataset;
        float inlineStart = ps3d.getInlineStart();
        float inlineEnd = ps3d.getInlineEnd();
        float inlineDelta = ps3d.getInlineDelta();
        float xlineStart = ps3d.getXlineStart();
        float xlineEnd = ps3d.getXlineEnd();
        float xlineDelta = ps3d.getXlineDelta();
        model.setInlineStart((int) inlineStart);
        model.setInlineEnd((int) inlineEnd);
        model.setInlineDelta((int) inlineDelta);
        model.setXlineStart((int) xlineStart);
        model.setXlineEnd((int) xlineEnd);
        model.setXlineDelta((int) xlineDelta);
        SeismicSurvey3d geometry = ps3d.getSurvey();
        float[] inlines = { inlineStart, inlineEnd, inlineEnd, inlineStart };
        float[] xlines = { xlineStart, xlineStart, xlineEnd, xlineEnd };
        CoordinateSeries coords = geometry.transformInlineXlineToXY(inlines, xlines);
        Point3d[] points = coords.getPointsDirect();
        double[] xs = new double[4];
        double[] ys = new double[4];
        for (int i = 0; i < 4; i++) {
          xs[i] = points[i].getX();
          ys[i] = points[i].getY();
        }
        Volume3dMapperModel model3d = (Volume3dMapperModel) model;
        model3d.setX0(xs[0]);
        model3d.setY0(ys[0]);
        model3d.setX1(xs[1]);
        model3d.setY1(ys[1]);
        model3d.setX2(xs[2]);
        model3d.setY2(ys[2]);
        model3d.setX3(xs[3]);
        model3d.setY3(ys[3]);
      } else if (seismicDataset instanceof PostStack2dLine) {
        Volume2dMapperModel model2d = (Volume2dMapperModel) model;
        PostStack2dLine ps2d = (PostStack2dLine) seismicDataset;
        float lineNumber = ps2d.getLineNumber();
        float cdpStart = ps2d.getCdpStart();
        float cdpEnd = ps2d.getCdpEnd();
        float cdpDelta = ps2d.getCdpDelta();
        model2d.setInlineStart((int) lineNumber);
        model2d.setInlineEnd((int) lineNumber);
        model2d.setInlineDelta(1);
        model2d.setCdpStart((int) cdpStart);
        model2d.setCdpEnd((int) cdpEnd);
        model2d.setCdpDelta((int) cdpDelta);
      }
      map.put(entity, model);
    }
    return map;
  }

  @Override
  public IStatus initialize() {
    // No initialization necessary.
    return Status.OK_STATUS;
  }

}
