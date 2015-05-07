/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field.aoi;


import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.io.ExportTask;
import org.geocraft.core.io.IDatastoreAccessor;
import org.geocraft.core.io.IDatastoreAccessorService;
import org.geocraft.core.io.ImportTask;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.aoi.SeismicSurvey2dAOI;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geometry.GridGeometry2d;
import org.geocraft.core.model.grid.Grid2d;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.seismic.PostStack2d;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.SeismicSurvey2d;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.message.IMessageService;


public class AOI2dModel extends Model {

  public static final String SEISMIC_SURVEY_2D_AOI_CREATED_TOPIC = "SEISMIC SURVEY 2D AOI CREATED";

  public static final String REFERENCE_ENTITY = "Reference Entity";

  public static final String HAS_Z_RANGE = "Has Z Range";

  public static final String Z_START = "Z Start";

  public static final String Z_END = "Z End";

  public static final String Z_UNIT = "Z Unit";

  public static final String OUTPUT_AOI_NAME = "Area of Interest Name";

  private EntityProperty<Entity> _referenceEntity;

  private final BooleanProperty _hasZRange;

  private final FloatProperty _zStart;

  private final FloatProperty _zEnd;

  private final EnumProperty<Unit> _zUnit;

  private Map<String, FloatRange> _cdpRanges;

  private StringProperty _outputAOIName;

  public AOI2dModel() {
    _referenceEntity = addEntityProperty(REFERENCE_ENTITY, Entity.class);
    _cdpRanges = new HashMap<String, FloatRange>();
    _hasZRange = addBooleanProperty(HAS_Z_RANGE, false);
    _zStart = addFloatProperty(Z_START, 0);
    _zEnd = addFloatProperty(Z_END, 0);
    _zUnit = addEnumProperty(Z_UNIT, Unit.class, UnitPreferences.getInstance().getTimeUnit());
    _outputAOIName = addStringProperty(OUTPUT_AOI_NAME, "");
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    super.propertyChange(event);

    String key = event.getPropertyName();
    if (key.equals(REFERENCE_ENTITY)) {
      Entity entity = _referenceEntity.get();

      float[] zStart = { 0 };
      float[] zEnd = { 0 };
      Unit[] zUnit = { Unit.UNDEFINED };
      boolean hasZRange = getReferenceZRange(entity, zStart, zEnd, zUnit);
      _hasZRange.set(hasZRange);
      if (hasZRange) {
        _zStart.set(zStart[0]);
        _zEnd.set(zEnd[0]);
        _zUnit.set(zUnit[0]);
      }
    }
  }

  public void validate(IValidation results) {
    if (_referenceEntity.isNull()) {
      results.error(REFERENCE_ENTITY, "Reference entity not selected.");
    }

    if (_hasZRange.get()) {
      if (_zEnd.get() < _zStart.get()) {
        results.warning(Z_END, "Z End < Z Start.");
      }
      if (_zUnit.isNull()) {
        results.error(Z_UNIT, "Z Unit not specified.");
      }
    }

    if (_outputAOIName.isEmpty()) {
      results.error(OUTPUT_AOI_NAME, "Output AOI name not specified.");
    }
  }

  public SeismicSurvey2dAOI createAOI(boolean inMemory) {
    Entity entity = _referenceEntity.get();
    boolean hasZRange = _hasZRange.get();
    float zStart = _zStart.get();
    float zEnd = _zEnd.get();
    Unit zUnit = _zUnit.get();
    String aoiName = _outputAOIName.get();
    String ASCII_FILE_AOI = "ASCII File AOI";

    IDatastoreAccessor accessor = null;
    IDatastoreAccessorService datastoreAccessorService = ServiceProvider.getDatastoreAccessorService();
    for (IDatastoreAccessor datastoreAccessor : datastoreAccessorService.getDatastoreAccessors()) {

      // NOTE: This is a hack to avoid a circular dependency between this bundle and the org.geocraft.io.ascii bundle.
      // TODO: Revisit this for a more sustainable solution.
      if (datastoreAccessor.getName().equals(ASCII_FILE_AOI)) {
        accessor = datastoreAccessor;
        break;
      }
      if (accessor != null) {
        break;
      }
    }

    // create a new area of interest.
    SeismicSurvey2dAOI aoi = new SeismicSurvey2dAOI(aoiName, _cdpRanges);
    if (hasZRange) {
      aoi.setZRange(zStart, zEnd, zUnit);
    }

    if (accessor != null && !inMemory) {
      MapperModel mapperModel = accessor.createMapperModel(IOMode.OUTPUT);
      mapperModel.updateUniqueId(aoi.getDisplayName());
      ExportTask exportTask = accessor.createExportTask();
      exportTask.setMapperModel(mapperModel);
      exportTask.setEntity(aoi);
      TaskRunner.runTask(exportTask, "Writing AOI", TaskRunner.JOIN);

      ImportTask importTask = accessor.createImportTask();
      importTask.setMapperModel(mapperModel);
      TaskRunner.runTask(importTask, "Loading AOI", TaskRunner.JOIN);

      String uniqueID = mapperModel.getUniqueId();

      IMessageService messageService = ServiceProvider.getMessageService();
      messageService.publish(SEISMIC_SURVEY_2D_AOI_CREATED_TOPIC, uniqueID);

      Map<String, Object> objects = ServiceProvider.getRepository()
          .get(new TypeSpecification(SeismicSurvey2dAOI.class));
      for (Object object : objects.values()) {
        aoi = (SeismicSurvey2dAOI) object;
        if (aoi.getMapper().getModel().getUniqueId().equals(uniqueID)) {
          return aoi;
        }
      }
    }

    return aoi;
  }

  public SeismicSurvey2d getReferenceSurvey() {
    Entity entity = _referenceEntity.get();
    if (entity != null) {
      if (entity instanceof PostStack2d) {
        PostStack2d volume = (PostStack2d) entity;
        return volume.getSurvey();
      } else if (entity instanceof Grid2d) {
        Grid2d grid = (Grid2d) entity;
        GridGeometry2d geometry = grid.getGridGeometry();
        if (geometry instanceof SeismicSurvey2d) {
          SeismicSurvey2d survey = (SeismicSurvey2d) geometry;
          return survey;
        }
      }
    }
    return null;
  }

  public int[] getReferenceLineNumbers() {
    Entity entity = _referenceEntity.get();
    if (entity != null) {
      if (entity instanceof PostStack2d) {
        PostStack2d volume = (PostStack2d) entity;
        return volume.getLineNumbers();
      } else if (entity instanceof Grid2d) {
        Grid2d grid = (Grid2d) entity;
        GridGeometry2d geometry = grid.getGridGeometry();
        if (geometry instanceof SeismicSurvey2d) {
          SeismicSurvey2d survey = (SeismicSurvey2d) geometry;
          return survey.getLineNumbers();
        }
      }
    }
    return null;
  }

  public void setTraceRanges(Map<String, FloatRange> cdpRanges) {
    _cdpRanges = cdpRanges;
  }

  public static List<SeismicSurvey2d> getSeismicSurveys() {
    List<SeismicSurvey2d> surveys = new ArrayList<SeismicSurvey2d>();
    Map<String, Object> objects = ServiceProvider.getRepository().get(new AOI2dReferenceSpecification());
    for (String key : objects.keySet()) {
      Object object = objects.get(key);
      if (object instanceof PostStack2dLine) {
        PostStack2dLine volume = (PostStack2dLine) object;
        SeismicSurvey2d survey = volume.getSurvey();
        if (!surveys.contains(survey)) {
          surveys.add(survey);
        }
      } else if (object instanceof PostStack2d) {
        PostStack2d volume = (PostStack2d) object;
        SeismicSurvey2d survey = volume.getSurvey();
        if (!surveys.contains(survey)) {
          surveys.add(survey);
        }
      } else if (object instanceof Grid2d) {
        Grid2d grid = (Grid2d) object;
        GridGeometry2d geometry = grid.getGridGeometry();
        if (geometry instanceof SeismicSurvey2d) {
          SeismicSurvey2d survey = (SeismicSurvey2d) geometry;
          if (!surveys.contains(survey)) {
            surveys.add(survey);
          }
        }
      }
    }
    return surveys;
  }

  private boolean getReferenceZRange(Entity entity, float[] zStart, float[] zEnd, Unit[] zUnit) {
    if (entity instanceof PostStack2d) {
      float zmin = Float.MAX_VALUE;
      float zmax = -Float.MAX_VALUE;
      PostStack2d volume = (PostStack2d) entity;
      for (int lineNum : volume.getLineNumbers()) {
        PostStack2dLine line = volume.getPostStack2dLine(lineNum);
        zmin = Math.min(zmin, line.getZStart());
        zmax = Math.max(zmin, line.getZEnd());
        zUnit[0] = line.getZUnit();
      }
      // Determine # of columns and # of rows
      zStart[0] = zmin;
      zEnd[0] = zmax;
      return true;
    }
    return false;
  }

}
