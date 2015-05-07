/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.classbkg;


import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.geocraft.abavo.ABavoBaseAlgorithm2d;
import org.geocraft.abavo.OutputVolumeType;
import org.geocraft.abavo.crossplot.IABavoCrossplot;
import org.geocraft.abavo.defs.ClassificationMethod;
import org.geocraft.abavo.ellipse.EllipseRegionsClassifier;
import org.geocraft.abavo.ellipse.EllipseRegionsModel;
import org.geocraft.abavo.polygon.PolygonRegionsClassifier;
import org.geocraft.abavo.polygon.PolygonRegionsModel;
import org.geocraft.core.factory.model.PostStack2dLineFactory;
import org.geocraft.core.model.DataSource;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.SeismicDataset.StorageFormat;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.internal.abavo.ABavoCrossplotRegistry;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.plot.model.ModelSpaceBounds;


public class GenerateClassVolume2d extends ABavoBaseAlgorithm2d implements IGenerateClassVolumeConstants {

  /** The classification method. */
  private EnumProperty<ClassificationMethod> _classificationMethod;

  /** The normalization factor (default 128 for 8-bit). */
  private FloatProperty _normalizationFactor;

  /** The region definition string. */
  private StringProperty _regionDefinition;

  /** The flag for processing polarity reversals only. */
  // TODO private BooleanProperty _polarityReversalsOnly;

  /** The output Class volume name. */
  private StringProperty _outputVolumeName;

  /** The output Class volume type. */
  private EnumProperty<OutputVolumeType> _outputVolumeType;

  protected EllipseRegionsModel _ellipseRegionsModel;

  protected PolygonRegionsModel _polygonRegionsModel;

  protected double _xStart;

  protected double _yStart;

  protected double _xEnd;

  protected double _yEnd;

  private ClassificationProcess _classificationProcess;

  private OutputPostStack2dProcess _outputProcess;

  public GenerateClassVolume2d() {
    super(true, true);
    _classificationMethod = addEnumProperty("Classification Method", ClassificationMethod.class,
        ClassificationMethod.CLASS_OF_SAMPLE);
    _normalizationFactor = addFloatProperty("Normalization Factor", 128);
    _regionDefinition = addStringProperty("Region Definition", ELLIPSES);
    // TODO _polarityReversalsOnly = addBooleanProperty("Polarity Reversals Only", false);
    _outputVolumeName = addStringProperty("Class Volume Name", "avoClass");
    _outputVolumeType = addEnumProperty("Class Volume Type", OutputVolumeType.class, OutputVolumeType.INTEGER_08);

    _ellipseRegionsModel = new EllipseRegionsModel();
    _polygonRegionsModel = new PolygonRegionsModel();
  }

  @Override
  public void buildView(IModelForm form) {
    super.buildView(form);

    // Add a section for classification.
    FormSection section = form.addSection("Classification");

    // Add a radio field containing the classification methods.
    section.addRadioGroupField(_classificationMethod, ClassificationMethod.values());

    section.addTextField(_normalizationFactor);

    // Add a radio field containing the region definitions.
    // There are currently only 2 options, so this is a radio field.
    // However, region definition might become an extension point, which
    // would allow for N region definitions, which would require a combo.
    String[] regionDefinitions = { ELLIPSES, POLYGONS };
    section.addRadioGroupField(_regionDefinition, regionDefinitions);

    // Add a checkbox for the polarity reversals flag.
    //CheckBoxField polarityReversals = addCheckBoxField(section, POLARITY_REVERSALS_ONLY);
    //polarityReversals.setLabel("Polarity Reversals Only");

    // Add a section for data output.
    FormSection output = form.addSection("Output Data");

    // Add a text field for the output class volume name.
    output.addTextField(_outputVolumeName);

    // Add a text field for the output class volume type.
    OutputVolumeType[] outputTypes = { OutputVolumeType.INTEGER_08, OutputVolumeType.INTEGER_16 };
    output.addRadioGroupField(_outputVolumeType, outputTypes);

    output.addComboField(_outputSampleRate, new Float[0]);
  }

  @Override
  public void validate(IValidation results) {
    super.validate(results);

    // Validate the classification method.
    if (_classificationMethod.isNull()) {
      results.error(_classificationMethod, "Classification method not specified");
    }

    // Validate the region definition.
    String regionDefinition = getRegionDefinition();
    if (regionDefinition.isEmpty() || !regionDefinition.equals(ELLIPSES) && !regionDefinition.equals(POLYGONS)) {
      results.error(_regionDefinition, "Invalid region definition");
    }

    // Validate the output class volume name.
    if (_outputVolumeName.isEmpty()) {
      results.error(_outputVolumeName, "Class volume name not specified");
    } else {
      if (!_volumeA.isNull()) {
        IStatus nameStatus = DataSource.validateName(_volumeA.get(), _outputVolumeName.get());
        if (!nameStatus.isOK()) {
          results.error(_outputVolumeName, nameStatus.getMessage());
        }
      }
    }

    OutputVolumeType volumeType = getOutputVolumeType();
    if (_outputVolumeType.isNull()) {
      results.error(_outputVolumeType, "Class volume type not specified");
    } else {
      float normalizationFactor = getNormalizationFactor();
      if (normalizationFactor <= 0) {
        results.error(_normalizationFactor, "Normalization factor must be positive");
      }
      if (normalizationFactor > 128 && volumeType.equals(OutputVolumeType.INTEGER_08)) {
        results.error(_normalizationFactor, "Normalization factor cannot be greater than 128 for 8-bit");
      } else if (normalizationFactor > 32768 && volumeType.equals(OutputVolumeType.INTEGER_16)) {
        results.error(_normalizationFactor, "Normalization factor cannot be greater than 32768 for 16-bit");
      }
    }
  }

  @Override
  protected String getTaskName() {
    return "Generate Class Volume";
  }

  @Override
  public void initialize(IRepository repository, final String lineName) {
    super.initialize(repository, lineName);

    EllipseRegionsModel ellipseRegionsModel = new EllipseRegionsModel();
    PolygonRegionsModel polygonRegionsModel = new PolygonRegionsModel();

    IABavoCrossplot[] crossplots = ABavoCrossplotRegistry.get().getCrossplots();
    if (crossplots != null && crossplots.length > 0) {
      // If a crossplot exists, then it is running from the UI, so first update from the crossplot models.
      IABavoCrossplot crossplot = ABavoCrossplotRegistry.get().getCrossplots()[0];
      _ellipseRegionsModel.updateModel(crossplot.getEllipseRegionsModel());
      _polygonRegionsModel.updateModel(crossplot.getPolygonRegionsModel());
      ModelSpaceBounds bounds = crossplot.getActiveModelSpace().getDefaultBounds();
      _xStart = bounds.getStartX();
      _yStart = bounds.getStartY();
      _xEnd = bounds.getEndX();
      _yEnd = bounds.getEndY();
    }

    ellipseRegionsModel.updateModel(_ellipseRegionsModel);
    polygonRegionsModel.updateModel(_polygonRegionsModel);

    // Create the regions classifier based on the region definition.
    IRegionsClassifier classifier = null;
    String regionDefinition = getRegionDefinition();
    if (regionDefinition.equals(ELLIPSES)) {
      classifier = new EllipseRegionsClassifier(ellipseRegionsModel, getNormalizationFactor(), _xStart, _xEnd, _yStart,
          _yEnd);
    } else if (regionDefinition.equals(POLYGONS)) {
      classifier = new PolygonRegionsClassifier(polygonRegionsModel);
    } else {
      throw new RuntimeException("Invalid region definition: " + regionDefinition);
    }

    // Create and initialize the classification process.
    _classificationProcess = new ClassificationProcess(classifier, getClassificationMethod());

    try {
      // Create the output volume.
      OutputVolumeType type = getOutputVolumeType();
      StorageFormat storageFormat = StorageFormat.INTEGER_08;
      switch (type) {
        case FLOAT_32:
          storageFormat = StorageFormat.FLOAT_32;
          break;
        case INTEGER_08:
          storageFormat = StorageFormat.INTEGER_08;
          break;
        case INTEGER_16:
          storageFormat = StorageFormat.INTEGER_16;
          break;
      }
      PostStack2dLine outputVolume = PostStack2dLineFactory.create(repository, getVolumeA()
          .getPostStack2dLine(lineName), getOutputVolumeName(), storageFormat, _outputSampleRate.get());

      // Initialize the volume output process.
      _outputProcess = new OutputPostStack2dProcess("Output Class Volume", outputVolume);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void cleanup() {
    super.cleanup();
    if (_classificationProcess != null) {
      _classificationProcess.cleanup();
    }
    if (_outputProcess != null) {
      _outputProcess.cleanup();
    }
  }

  @Override
  protected void processTraceData(TraceData[] traceDataIn, final String lineName) {
    // Classify the trace data.
    TraceData[] traceDataOut = _classificationProcess.process(traceDataIn);

    // Output the trace data.
    _outputProcess.process(traceDataOut);
  }

  private ClassificationMethod getClassificationMethod() {
    return _classificationMethod.get();
  }

  private float getNormalizationFactor() {
    return _normalizationFactor.get();
  }

  private String getRegionDefinition() {
    return _regionDefinition.get();
  }

  public String getOutputVolumeName() {
    return _outputVolumeName.get();
  }

  public OutputVolumeType getOutputVolumeType() {
    return _outputVolumeType.get();
  }

  @Override
  public Map<String, String> pickle() {
    Map<String, String> parms = super.pickle();

    // Add to the standard pickle method by adding in the ellipse or polygons region model.
    IABavoCrossplot[] crossplots = ABavoCrossplotRegistry.get().getCrossplots();
    if (crossplots != null && crossplots.length > 0) {

      IABavoCrossplot crossplot = crossplots[0];

      ModelSpaceBounds bounds = crossplot.getActiveModelSpace().getDefaultBounds();
      _xStart = bounds.getStartX();
      _yStart = bounds.getStartY();
      _xEnd = bounds.getEndX();
      _yEnd = bounds.getEndY();
      parms.put(CROSSPLOT_START_X, Double.toString(_xStart));
      parms.put(CROSSPLOT_START_Y, Double.toString(_yStart));
      parms.put(CROSSPLOT_END_X, Double.toString(_xEnd));
      parms.put(CROSSPLOT_END_Y, Double.toString(_yEnd));

      _ellipseRegionsModel.updateModel(crossplot.getEllipseRegionsModel());
      _polygonRegionsModel.updateModel(crossplot.getPolygonRegionsModel());
      if (!_regionDefinition.isEmpty()) {
        String regionDef = _regionDefinition.get();

        // Pickle only the ellipse regions model or polygons region model, depending on
        // the region definition selected.
        if (regionDef.equals(ELLIPSES)) {
          Map<String, String> ellipseMap = _ellipseRegionsModel.pickle();
          for (String key : ellipseMap.keySet()) {
            parms.put(key, ellipseMap.get(key));
          }
        } else if (regionDef.equals(POLYGONS)) {
          Map<String, String> polygonMap = _polygonRegionsModel.pickle();
          for (String key : polygonMap.keySet()) {
            parms.put(key, polygonMap.get(key));
          }
        }
      }
    }
    return parms;
  }

  @Override
  public void unpickle(final Map<String, String> parms) {
    super.unpickle(parms);

    if (parms.containsKey(CROSSPLOT_START_X) && parms.containsKey(CROSSPLOT_START_Y)
        && parms.containsKey(CROSSPLOT_END_X) && parms.containsKey(CROSSPLOT_END_Y)) {
      _xStart = Double.parseDouble(parms.get(CROSSPLOT_START_X));
      _yStart = Double.parseDouble(parms.get(CROSSPLOT_START_Y));
      _xEnd = Double.parseDouble(parms.get(CROSSPLOT_END_X));
      _yEnd = Double.parseDouble(parms.get(CROSSPLOT_END_Y));
    }

    // Add to the standard unpickle method by unpicking the ellipse or polygons region model.
    if (!_regionDefinition.isEmpty()) {
      String regionDef = _regionDefinition.get();

      // Unpickle only the ellipse regions model or polygons region model, depending on
      // the region definition selected.
      if (regionDef.equals(ELLIPSES)) {
        _ellipseRegionsModel.unpickle(parms);
      } else if (regionDef.equals(POLYGONS)) {
        _polygonRegionsModel.unpickle(parms);
      }
    }
  }

}
