package org.geocraft.geomath.algorithm.utilities.areaofinterest;


import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.io.ExportTask;
import org.geocraft.core.io.IDatastoreAccessor;
import org.geocraft.core.io.IDatastoreAccessorService;
import org.geocraft.core.io.ImportTask;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.aoi.SeismicSurvey3dAOI;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.core.service.message.IMessageService;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;


public class CreateAreaOfInterestAlgorithm extends StandaloneAlgorithm {

  public static final String SEISMIC_SURVEY_3D_AOI_CREATED_TOPIC = "SEISMIC SURVEY 3D AOI CREATED";

  public static final String REFERENCE_ENTITY = "Reference Geometry";

  public static final String INPUT_INLINE_START = "Input Inline Start";

  public static final String INPUT_INLINE_END = "Input Inline End";

  public static final String INPUT_INLINE_DELTA = "Input Inline Delta";

  public static final String INPUT_XLINE_START = "Input Xline Start";

  public static final String INPUT_XLINE_END = "Input Xline End";

  public static final String INPUT_XLINE_DELTA = "Input Xline Delta";

  public static final String INLINE_START = "Inline Start";

  public static final String INLINE_END = "Inline End";

  public static final String INLINE_DELTA = "Inline Delta";

  public static final String XLINE_START = "Xline Start";

  public static final String XLINE_END = "Xline End";

  public static final String XLINE_DELTA = "Xline Delta";

  public static final String HAS_Z_RANGE = "Has Z Range";

  public static final String Z_START = "Z Start";

  public static final String Z_END = "Z End";

  public static final String Z_UNIT = "Z Unit";

  public static final String OUTPUT_AOI_NAME = "Area of Interest Name";

  public static final String USE_CUSTOM = "Input your own area of interest name?";

  public EntityProperty<Entity> _referenceEntity;

  public FloatProperty _inputInlineStart;

  public FloatProperty _inputInlineEnd;

  public FloatProperty _inputInlineDelta;

  public FloatProperty _inputXlineStart;

  public FloatProperty _inputXlineEnd;

  public FloatProperty _inputXlineDelta;

  public FloatProperty _inlineStart;

  public FloatProperty _inlineEnd;

  public FloatProperty _inlineDelta;

  public FloatProperty _xlineStart;

  public FloatProperty _xlineEnd;

  public FloatProperty _xlineDelta;

  private final BooleanProperty _hasZRange;

  private final FloatProperty _zStart;

  private final FloatProperty _zEnd;

  private final EnumProperty<Unit> _zUnit;

  public BooleanProperty _useCustomName;

  /** The output volume. */
  StringProperty _outputAOIName;// = "";

  public CreateAreaOfInterestAlgorithm() {
    _referenceEntity = addEntityProperty(REFERENCE_ENTITY, Entity.class);
    _inputInlineStart = addFloatProperty(INPUT_INLINE_START, 0);
    _inputInlineEnd = addFloatProperty(INPUT_INLINE_END, 0);
    _inputInlineDelta = addFloatProperty(INPUT_INLINE_DELTA, 0);
    _inputXlineStart = addFloatProperty(INPUT_XLINE_START, 0);
    _inputXlineEnd = addFloatProperty(INPUT_XLINE_END, 0);
    _inputXlineDelta = addFloatProperty(INPUT_XLINE_DELTA, 0);
    _inlineStart = addFloatProperty(INLINE_START, 0);
    _inlineEnd = addFloatProperty(INLINE_END, 0);
    _inlineDelta = addFloatProperty(INLINE_DELTA, 0);
    _xlineStart = addFloatProperty(XLINE_START, 0);
    _xlineEnd = addFloatProperty(XLINE_END, 0);
    _xlineDelta = addFloatProperty(XLINE_DELTA, 0);
    _hasZRange = addBooleanProperty(HAS_Z_RANGE, false);
    _zStart = addFloatProperty(Z_START, 0);
    _zEnd = addFloatProperty(Z_END, 0);
    _zUnit = addEnumProperty(Z_UNIT, Unit.class, UnitPreferences.getInstance().getTimeUnit());
    _outputAOIName = addStringProperty(OUTPUT_AOI_NAME, "");
    _useCustomName = addBooleanProperty(USE_CUSTOM, false);

  }

  @Override
  public void buildView(IModelForm modelForm) {
    FormSection inputSection = modelForm.addSection("Input");
    inputSection.addEntityComboField(REFERENCE_ENTITY, PostStack3d.class);

    FormSection inputVolParam = modelForm.addSection("Input Volume Parameters");
    inputVolParam.addLabelField(INPUT_INLINE_START);
    inputVolParam.addLabelField(INPUT_INLINE_END);
    inputVolParam.addLabelField(INPUT_INLINE_DELTA);
    inputVolParam.addLabelField(INPUT_XLINE_START);
    inputVolParam.addLabelField(INPUT_XLINE_END);
    inputVolParam.addLabelField(INPUT_XLINE_DELTA);

    FormSection outputAoiParam = modelForm.addSection("Output Area of Interest Parameters");
    outputAoiParam.addTextField(INLINE_START);
    outputAoiParam.addTextField(INLINE_END);
    outputAoiParam.addTextField(INLINE_DELTA);
    outputAoiParam.addTextField(XLINE_START);
    outputAoiParam.addTextField(XLINE_END);
    outputAoiParam.addTextField(XLINE_DELTA);

    FormSection outputZRange = modelForm.addSection("Z Range (Optional)");
    outputZRange.addCheckboxField(HAS_Z_RANGE);
    outputZRange.addTextField(Z_START);
    outputZRange.addTextField(Z_END);
    Unit[] zUnits = { UnitPreferences.getInstance().getTimeUnit(),
        UnitPreferences.getInstance().getVerticalDistanceUnit() };
    outputZRange.addComboField(Z_UNIT, zUnits);

    FormSection outputSection = modelForm.addSection("Output");
    outputSection.addTextField(OUTPUT_AOI_NAME).showActiveFieldToggle(_useCustomName);
  }

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) {
    Entity entity = _referenceEntity.get();
    FloatRange refInlineRange = getReferenceInlineRange(entity);
    FloatRange refXlineRange = getReferenceXlineRange(entity);
    SeismicSurvey3d survey = getReferenceSurvey(entity);
    float inlineStart = _inlineStart.get();
    float inlineEnd = _inlineEnd.get();
    float inlineDelta = _inlineDelta.get();
    float xlineStart = _xlineStart.get();
    float xlineEnd = _xlineEnd.get();
    float xlineDelta = _xlineDelta.get();

    // Make sure inline numbers are valid numbers for the volume
    int minNum = 0;
    int maxNum = (int) ((refInlineRange.getEnd() - refInlineRange.getStart()) / refInlineRange.getDelta());
    int num = (int) Math.rint((inlineStart - refInlineRange.getStart()) / refInlineRange.getDelta());
    if (num < minNum) {
      num = minNum;
    }
    if (num > maxNum) {
      num = maxNum;
    }
    inlineStart = refInlineRange.getStart() + num * refInlineRange.getDelta();

    num = (int) Math.rint((inlineEnd - refInlineRange.getStart()) / refInlineRange.getDelta());
    if (num < minNum) {
      num = minNum;
    }
    if (num > maxNum) {
      num = maxNum;
    }
    inlineEnd = refInlineRange.getStart() + num * refInlineRange.getDelta();

    // Make sure xline numbers are valid numbers for the volume
    minNum = 0;
    maxNum = (int) Math.rint((refXlineRange.getEnd() - refXlineRange.getStart()) / refXlineRange.getDelta());
    num = (int) Math.rint((xlineStart - refXlineRange.getStart()) / refXlineRange.getDelta());
    if (num < minNum) {
      num = minNum;
    }
    if (num > maxNum) {
      num = maxNum;
    }
    xlineStart = refXlineRange.getStart() + num * refXlineRange.getDelta();

    num = (int) Math.rint((xlineEnd - refXlineRange.getStart()) / refXlineRange.getDelta());
    if (num < minNum) {
      num = minNum;
    }
    if (num > maxNum) {
      num = maxNum;
    }
    xlineEnd = refXlineRange.getStart() + num * refXlineRange.getDelta();

    String aoiName = _outputAOIName.get();
    if (aoiName.length() == 0) {
      aoiName = "";
    }

    IDatastoreAccessor accessor = null;
    IDatastoreAccessorService datastoreAccessorService = ServiceProvider.getDatastoreAccessorService();
    for (IDatastoreAccessor datastoreAccessor : datastoreAccessorService.getDatastoreAccessors()) {
      for (String entityClassName : datastoreAccessor.getSupportedEntityClassNames()) {
        if (entityClassName.equals("SeismicSurvey3dAOI")) {
          accessor = datastoreAccessor;
          break;
        }
      }
    }
    // create a new area of interest
    SeismicSurvey3dAOI aoi = new SeismicSurvey3dAOI(aoiName, survey, inlineStart, inlineEnd, inlineDelta, xlineStart,
        xlineEnd, xlineDelta);
    if (_hasZRange.get()) {
      float zStart = _zStart.get();
      float zEnd = _zEnd.get();
      Unit zUnit = _zUnit.get();
      aoi.setZRange(zStart, zEnd, zUnit);
    }

    if (accessor != null) {
      MapperModel mapperModel = accessor.createMapperModel(IOMode.OUTPUT);
      mapperModel.updateUniqueId(aoi.getDisplayName());
      ExportTask exportTask = accessor.createExportTask();
      exportTask.setMapperModel(mapperModel);
      exportTask.setEntity(aoi);
      TaskRunner.runTask(exportTask, "Writing AOI", TaskRunner.JOIN);

      ImportTask importTask = accessor.createImportTask();
      importTask.setMapperModel(mapperModel);
      TaskRunner.runTask(importTask, "Loading AOI", TaskRunner.JOIN);

      IMessageService messageService = ServiceProvider.getMessageService();
      messageService.publish(SEISMIC_SURVEY_3D_AOI_CREATED_TOPIC, mapperModel.getUniqueId());
    }
  }

  @Override
  public void propertyChanged(String key) {
    if (key.equals(REFERENCE_ENTITY)) {
      Entity entity = _referenceEntity.get();

      FloatRange inputInlineRange = getReferenceInlineRange(entity);
      FloatRange inputXlineRange = getReferenceXlineRange(entity);

      // Change input parameters based on the input volume
      _inputInlineStart.set(inputInlineRange.getStart());
      _inputInlineEnd.set(inputInlineRange.getEnd());
      _inputInlineDelta.set(inputInlineRange.getDelta());
      _inputXlineStart.set(inputXlineRange.getStart());
      _inputXlineEnd.set(inputXlineRange.getEnd());
      _inputXlineDelta.set(inputXlineRange.getDelta());

      // Default the output area of interest parameters
      _inlineStart.set(inputInlineRange.getStart());
      _inlineEnd.set(inputInlineRange.getEnd());
      _inlineDelta.set(inputInlineRange.getDelta());
      _xlineStart.set(inputXlineRange.getStart());
      _xlineEnd.set(inputXlineRange.getEnd());
      _xlineDelta.set(inputXlineRange.getDelta());

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

  @Override
  public void validate(IValidation results) {
    if (_referenceEntity.get() == null) {
      results.error(REFERENCE_ENTITY, "cannot be empty");
    }
    if (_hasZRange.get()) {
      if (_zEnd.get() < _zStart.get()) {
        results.warning(Z_END, "Z End < Z Start.");
      }
      if (_zUnit.isNull()) {
        results.error(Z_UNIT, "Z Unit not specified.");
      }
    }
  }

  private SeismicSurvey3d getReferenceSurvey(Entity entity) {
    if (entity instanceof PostStack3d) {
      PostStack3d volume = (PostStack3d) entity;
      return volume.getSurvey();
    } else if (entity instanceof PreStack3d) {
      PreStack3d volume = (PreStack3d) entity;
      return volume.getSurvey();
    } else if (entity instanceof Grid3d) {
      Grid3d grid = (Grid3d) entity;
      GridGeometry3d geometry = grid.getGeometry();
      if (geometry instanceof SeismicSurvey3d) {
        SeismicSurvey3d survey = (SeismicSurvey3d) geometry;
        return survey;
      }
    }
    return null;
  }

  private FloatRange getReferenceInlineRange(Entity entity) {
    float inlineStart = 0;
    float inlineEnd = 0;
    float inlineDelta = 0;
    if (entity instanceof PostStack3d) {
      PostStack3d volume = (PostStack3d) entity;
      // Determine # of columns and # of rows
      inlineStart = volume.getInlineStart();
      inlineEnd = volume.getInlineEnd();
      inlineDelta = volume.getInlineDelta();
    } else if (entity instanceof PreStack3d) {
      PreStack3d volume = (PreStack3d) entity;
      // Determine # of columns and # of rows
      inlineStart = volume.getInlineStart();
      inlineEnd = volume.getInlineEnd();
      inlineDelta = volume.getInlineDelta();
    } else if (entity instanceof Grid3d) {
      Grid3d grid = (Grid3d) entity;
      GridGeometry3d geometry = grid.getGeometry();
      if (geometry instanceof SeismicSurvey3d) {
        SeismicSurvey3d survey = (SeismicSurvey3d) geometry;
        // Determine # of columns and # of rows
        inlineStart = survey.getInlineStart();
        inlineEnd = survey.getInlineEnd();
        inlineDelta = survey.getInlineDelta();
      }
    }
    return new FloatRange(inlineStart, inlineEnd, inlineDelta);
  }

  private FloatRange getReferenceXlineRange(Entity entity) {
    float xlineStart = 0;
    float xlineEnd = 0;
    float xlineDelta = 0;
    if (entity instanceof PostStack3d) {
      PostStack3d volume = (PostStack3d) entity;
      // Determine # of columns and # of rows
      xlineStart = volume.getXlineStart();
      xlineEnd = volume.getXlineEnd();
      xlineDelta = volume.getXlineDelta();
    } else if (entity instanceof PreStack3d) {
      PreStack3d volume = (PreStack3d) entity;
      // Determine # of columns and # of rows
      xlineStart = volume.getXlineStart();
      xlineEnd = volume.getXlineEnd();
      xlineDelta = volume.getXlineDelta();
    } else if (entity instanceof Grid3d) {
      Grid3d grid = (Grid3d) entity;
      GridGeometry3d geometry = grid.getGeometry();
      if (geometry instanceof SeismicSurvey3d) {
        SeismicSurvey3d survey = (SeismicSurvey3d) geometry;
        // Determine # of columns and # of rows
        xlineStart = survey.getXlineStart();
        xlineEnd = survey.getXlineEnd();
        xlineDelta = survey.getXlineDelta();
      }
    }
    return new FloatRange(xlineStart, xlineEnd, xlineDelta);
  }

  private boolean getReferenceZRange(Entity entity, float[] zStart, float[] zEnd, Unit[] zUnit) {
    if (entity instanceof PostStack3d) {
      PostStack3d volume = (PostStack3d) entity;
      // Determine # of columns and # of rows
      zStart[0] = volume.getZStart();
      zEnd[0] = volume.getZEnd();
      zUnit[0] = volume.getZUnit();
      return true;
    } else if (entity instanceof PreStack3d) {
      PreStack3d volume = (PreStack3d) entity;
      // Determine # of columns and # of rows
      zStart[0] = volume.getXlineStart();
      zEnd[0] = volume.getXlineEnd();
      zUnit[0] = volume.getZUnit();
      return true;
    }
    return false;
  }
}
