/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.aplusb;


import org.eclipse.core.runtime.IStatus;
import org.geocraft.abavo.ABavoBaseAlgorithm2d;
import org.geocraft.abavo.OutputVolumeType;
import org.geocraft.abavo.classbkg.OutputPostStack2dProcess;
import org.geocraft.abavo.crossplot.ABDataSeries;
import org.geocraft.abavo.crossplot.IABavoCrossplot;
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
import org.geocraft.internal.abavo.ServiceComponent;
import org.geocraft.math.regression.RegressionMethodDescription;
import org.geocraft.math.regression.RegressionStatistics;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;


public class GenerateAplusBVolume2d extends ABavoBaseAlgorithm2d {

  public static final String REGRESSION_LINE = "Regression Line";

  private StringProperty _regressionLine;

  private FloatProperty _sectionScalar;

  private FloatProperty _sectionOffset;

  /** The output A+B volume name. */
  private StringProperty _outputVolumeName;

  /** The output A+B volume type. */
  private EnumProperty<OutputVolumeType> _outputVolumeType;

  private AplusBProcess _aplusbProcess;

  private OutputPostStack2dProcess _outputProcess;

  public GenerateAplusBVolume2d() {
    super(true, true);
    _regressionLine = addStringProperty(REGRESSION_LINE, "");
    _sectionScalar = addFloatProperty("B-Section Scalar", 1);
    _sectionOffset = addFloatProperty("B-Section Offset", 0);
    _outputVolumeName = addStringProperty("A+B Volume Name", "avoAplusB");
    _outputVolumeType = addEnumProperty("A+B Volume Type", OutputVolumeType.class, OutputVolumeType.FLOAT_32);
  }

  @Override
  public void buildView(IModelForm form) {
    super.buildView(form);

    FormSection section = form.addSection("Offset and Scalar");

    RegressionMethodDescription[] methods = ServiceComponent.getRegressionMethodService().getRegressionMethods();
    String[] regressionOptions = new String[methods.length];
    for (int i = 0; i < methods.length; i++) {
      regressionOptions[i] = methods[i].getName();
    }
    section.addComboField(_regressionLine, regressionOptions);

    section.addTextField(_sectionScalar);

    section.addTextField(_sectionOffset);

    // Add a section for data output.
    FormSection output = form.addSection("Output Volume");

    // Add a text field for the output class volume name.
    output.addTextField(_outputVolumeName);

    // Add a text field for the output class volume type.
    output.addComboField(_outputVolumeType, OutputVolumeType.values());

    output.addComboField(_outputSampleRate, new Float[0]);
  }

  @Override
  public void propertyChanged(String triggerKey) {
    super.propertyChanged(triggerKey);
    if (triggerKey != null && triggerKey.equals(REGRESSION_LINE)) {
      String regressionName = _regressionLine.get();
      IABavoCrossplot[] crossplots = ABavoCrossplotRegistry.get().getCrossplots();
      if (crossplots != null && crossplots.length > 0) {
        IABavoCrossplot crossplot = crossplots[0];
        int activeSeriesIndex = crossplot.getActiveSeriesIndex();
        if (activeSeriesIndex >= 0) {
          ABDataSeries series = crossplot.getDataSeries(activeSeriesIndex);
          if (series != null) {
            RegressionMethodDescription[] methods = ServiceComponent.getRegressionMethodService()
                .getRegressionMethods();
            for (RegressionMethodDescription method : methods) {
              if (method.getName().equals(regressionName)) {
                RegressionStatistics stats = series.getRegression(method);
                _sectionScalar.set((float) (-1f / stats.getSlope()));
                _sectionOffset.set((float) stats.getIntercept());
                return;
              }
            }
          }
        }
      }
      _sectionScalar.set(Float.NEGATIVE_INFINITY);
      _sectionOffset.set(0);
    }
  }

  @Override
  public void validate(IValidation results) {
    super.validate(results);

    // Validate the section scalar.
    float scalar = getSectionScalar();
    if (Float.isInfinite(scalar) || Float.isNaN(scalar)) {
      results.error(_sectionScalar, "Invalid section scalar value: " + scalar);
    }

    // Validate the section offset.
    float offset = getSectionOffset();
    if (Float.isInfinite(offset) || Float.isNaN(offset)) {
      results.error(_sectionOffset, "Invalid section offset value: " + offset);
    }

    // Validate the output class volume name.
    if (_outputVolumeName.isEmpty()) {
      results.error(_outputVolumeName, "A+B volume name not specified");
    } else {
      if (!_volumeA.isNull()) {
        IStatus nameStatus = DataSource.validateName(_volumeA.get(), _outputVolumeName.get());
        if (!nameStatus.isOK()) {
          results.error(_outputVolumeName, nameStatus.getMessage());
        }
      }
    }

    if (_outputVolumeType.isNull()) {
      results.error(_outputVolumeType, "A+B volume type not specified");
    }
  }

  @Override
  protected String getTaskName() {
    return "Generate A+B Volume";
  }

  @Override
  public void initialize(IRepository repository, final String lineName) {
    super.initialize(repository, lineName);

    // Create and initialize the A+B process.
    _aplusbProcess = new AplusBProcess(getSectionScalar(), getSectionOffset());

    try {
      // Create the output volume.
      OutputVolumeType type = _outputVolumeType.get();
      StorageFormat storageFormat = StorageFormat.INTEGER_08;
      switch (type) {
        case FLOAT_32:
          storageFormat = StorageFormat.FLOAT_32;
          break;
        case INTEGER_08:
          storageFormat = StorageFormat.INTEGER_08;
          break;
        case INTEGER_16:
          storageFormat = StorageFormat.INTEGER_08;
          break;
      }
      PostStack2dLine outputVolume = PostStack2dLineFactory.create(repository, getVolumeA()
          .getPostStack2dLine(lineName), getOutputVolumeName(), storageFormat, _outputSampleRate.get());

      // Initialize the volume output process.
      _outputProcess = new OutputPostStack2dProcess("Output A+B Volume", outputVolume);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void cleanup() {
    super.cleanup();
    if (_aplusbProcess != null) {
      _aplusbProcess.cleanup();
    }
    if (_outputProcess != null) {
      _outputProcess.cleanup();
    }
  }

  @Override
  protected void processTraceData(TraceData[] traceDataIn, final String lineName) {
    // Add the trace data.
    TraceData[] traceDataOut = _aplusbProcess.process(traceDataIn);

    // Output the trace data.
    _outputProcess.process(traceDataOut);
  }

  public float getSectionScalar() {
    return _sectionScalar.get();
  }

  public float getSectionOffset() {
    return _sectionOffset.get();
  }

  public String getOutputVolumeName() {
    return _outputVolumeName.get();
  }

  public OutputVolumeType getOutputVolumeType() {
    return _outputVolumeType.get();
  }
}
