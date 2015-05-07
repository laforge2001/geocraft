package org.geocraft.geomath.algorithm.utilities.areaofinterest;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.io.ExportTask;
import org.geocraft.core.io.IDatastoreAccessor;
import org.geocraft.core.io.IDatastoreAccessorService;
import org.geocraft.core.io.ImportTask;
import org.geocraft.core.model.aoi.SeismicSurvey2dAOI;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.SeismicLine2d;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.core.service.message.IMessageService;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;


public class Create2dAreaOfInterestAlgorithm extends StandaloneAlgorithm {

  public static final String SEISMIC_SURVEY_2D_AOI_CREATED_TOPIC = "SEISMIC LINE 2D AOI CREATED";

  public static final String REFERENCE_ENTITY = "Reference Geometry";

  public static final String INPUT_SHOTPOINT_START = "Input Shot Point Start";

  public static final String INPUT_SHOTPOINT_END = "Input Shot Point End";

  public static final String SHOTPOINT_START = "Shotpoint Start";

  public static final String SHOTPOINT_END = "Shotpoint End";

  public static final String TRACE_DECIMATION = "Trace Decimation";

  public static final String OUTPUT_AOI_NAME = "Output AOI Name";

  public static final String CUSTOM_NAME = "Input your own area of interest name?";

  EntityProperty<PostStack2dLine> _referenceEntity;

  public FloatProperty _inputShotpointStart;

  public FloatProperty _inputShotpointEnd;

  public FloatProperty _shotpointStart;

  public FloatProperty _shotpointEnd;

  public IntegerProperty _traceDecimation;

  BooleanProperty _useCustomName;

  /** The output volume. */
  StringProperty _outputAOIName;// = "";

  public Create2dAreaOfInterestAlgorithm() {
    _referenceEntity = addEntityProperty(REFERENCE_ENTITY, PostStack2dLine.class);
    _inputShotpointStart = addFloatProperty(INPUT_SHOTPOINT_START, 0);
    _inputShotpointEnd = addFloatProperty(INPUT_SHOTPOINT_END, 0);
    _shotpointStart = addFloatProperty(SHOTPOINT_START, 0);
    _shotpointEnd = addFloatProperty(SHOTPOINT_END, 0);
    _traceDecimation = addIntegerProperty(TRACE_DECIMATION, 1);

    _useCustomName = addBooleanProperty(CUSTOM_NAME, false);
    _outputAOIName = addStringProperty(OUTPUT_AOI_NAME, "");
  }

  @Override
  public void buildView(IModelForm modelForm) {
    FormSection inputSection = modelForm.addSection("Input");
    inputSection.addEntityComboField(REFERENCE_ENTITY, PostStack2dLine.class);

    FormSection inputVolParamSection = modelForm.addSection("Input 2D Volume Parameters");
    inputVolParamSection.addLabelField(INPUT_SHOTPOINT_START);
    inputVolParamSection.addLabelField(INPUT_SHOTPOINT_END);

    inputVolParamSection.addTextField(SHOTPOINT_START);
    inputVolParamSection.addTextField(SHOTPOINT_END);
    inputVolParamSection.addTextField(TRACE_DECIMATION);

    FormSection outputSection = modelForm.addSection("Output");
    outputSection.addTextField(OUTPUT_AOI_NAME).showActiveFieldToggle(_useCustomName);
  }

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) throws CoreException {
    try {
      PostStack2dLine vol = _referenceEntity.get();
      SeismicLine2d line = vol.getSeismicLine();
      float shotpointStart = _shotpointStart.get();
      float shotpointEnd = _shotpointEnd.get();
      String aoiName = _outputAOIName.get();
      float cdpDelta = _traceDecimation.get();
      float cdpStartOfVol = vol.getCdpStart();
      float cdpEndOfVol = vol.getCdpEnd();
      float cdpDeltaOfVol = vol.getCdpDelta();

      float[] cdps = line.transformShotpointsToCDPs(new float[] { shotpointStart, shotpointEnd });
      float cdpStart = Math.min(cdps[0], cdps[1]);
      float cdpEnd = Math.max(cdps[0], cdps[1]);

      // Make sure cdp numbers are valid numbers for the volume
      int minNum = 0;
      int maxNum = (int) ((cdpEndOfVol - cdpStartOfVol) / cdpDeltaOfVol);
      int num = (int) Math.rint((cdpStart - cdpStartOfVol) / cdpDeltaOfVol);
      if (num < minNum) {
        num = minNum;
      }
      if (num > maxNum) {
        num = maxNum;
      }
      cdpStart = cdpStartOfVol + num * cdpDeltaOfVol;

      num = (int) Math.rint((cdpEnd - cdpStartOfVol) / cdpDeltaOfVol);
      if (num < minNum) {
        num = minNum;
      }
      if (num > maxNum) {
        num = maxNum;
      }
      cdpEnd = cdpStartOfVol + num * cdpDeltaOfVol;

      minNum = 1;
      num = (int) Math.rint(cdpDelta / cdpDeltaOfVol);
      if (num < minNum) {
        num = minNum;
      }
      if (num > maxNum) {
        num = maxNum;
      }
      cdpDelta = num * cdpDeltaOfVol;

      shotpointStart = line.transformCDPToShotpoint(cdpStart);
      shotpointEnd = line.transformCDPToShotpoint(cdpEnd);

      IDatastoreAccessor accessor = null;
      IDatastoreAccessorService datastoreAccessorService = ServiceProvider.getDatastoreAccessorService();
      for (IDatastoreAccessor datastoreAccessor : datastoreAccessorService.getDatastoreAccessors()) {
        for (String entityClassName : datastoreAccessor.getSupportedEntityClassNames()) {
          if (entityClassName.equals("SeismicSurvey2dAOI")) {
            accessor = datastoreAccessor;
            break;
          }
        }
        if (accessor != null) {
          break;
        }
      }

      Map<String, FloatRange> cdpRanges = new HashMap<String, FloatRange>();
      cdpRanges.put(line.getDisplayName(), new FloatRange(cdpStart, cdpEnd, cdpDelta));

      // create a new area of interest
      SeismicSurvey2dAOI aoi = new SeismicSurvey2dAOI(aoiName, cdpRanges);
      //      if (hasRange) {
      //        aoi.setZRange(zStart, zEnd, zUnit);
      //      }

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
        messageService.publish(SEISMIC_SURVEY_2D_AOI_CREATED_TOPIC, mapperModel.getUniqueId());
      }

    } catch (RuntimeException e) {
      throw new CoreException(new Status(IStatus.ERROR, "CreateAreaOfInterest",
          "Problem creating the area of interest", e));
    }

  }

  @Override
  public void propertyChanged(String key) {
    if (key.equals(REFERENCE_ENTITY)) {
      PostStack2dLine volume = _referenceEntity.get();

      if (volume != null) {
        // Determine shotpoints or cdps
        float shotpointStart = volume.getShotpointStart();
        float shotpointEnd = volume.getShotpointEnd();

        // Change input parameters based on the input volume
        _inputShotpointStart.set(shotpointStart);
        _inputShotpointEnd.set(shotpointEnd);

        // Default the output area of interest parameters
        _shotpointStart.set(shotpointStart);
        _shotpointEnd.set(shotpointEnd);
      }
    }

  }

  @Override
  public void validate(IValidation results) {
    if (_referenceEntity.get() == null) {
      results.error(REFERENCE_ENTITY, "Reference entity not specified.");
    }
    if (_traceDecimation.get() < 1) {
      results.error(TRACE_DECIMATION, "Trace decimation cannot be less than 1; 1 means output every trace");
    }
  }

}
