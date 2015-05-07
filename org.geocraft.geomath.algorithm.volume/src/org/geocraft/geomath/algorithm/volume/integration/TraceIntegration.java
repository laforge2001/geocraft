package org.geocraft.geomath.algorithm.volume.integration;


import org.eclipse.core.runtime.IStatus;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.factory.model.PostStack3dFactory;
import org.geocraft.core.model.DataSource;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.aoi.AOIComboField;


public abstract class TraceIntegration extends StandaloneAlgorithm {

  private EntityProperty<SeismicDataset> _inputVolume;

  private EntityProperty<AreaOfInterest> _areaOfInterest;

  private BooleanProperty _useAreaOfInterest;

  private IntegerProperty _operatorLength;

  private StringProperty _outputVolumeName;

  public TraceIntegration() {
    _inputVolume = addEntityProperty("Input Volume", SeismicDataset.class);
    _areaOfInterest = addEntityProperty("Area of Interest", AreaOfInterest.class);
    _useAreaOfInterest = addBooleanProperty("Use AOI", false);
    _operatorLength = addIntegerProperty("Operator Length (samples)", 80);
    _outputVolumeName = addStringProperty("Output Volume Name", "");
  }

  @Override
  public void buildView(IModelForm form) {
    // Input Section.
    FormSection inputSection = form.addSection("Input");

    inputSection.addEntityComboField(_inputVolume, SeismicDataset.class);

    AOIComboField aoiField = inputSection.addAOIComboField(_areaOfInterest, 3);
    //EntityComboField aoiField = inputSection.addEntityComboField(_areaOfInterest, AreaOfInterest.class);
    aoiField.showActiveFieldToggle(_useAreaOfInterest);

    // Integration Section.
    FormSection integrateSection = form.addSection("Integration");

    integrateSection.addTextField(_operatorLength);

    // Output Section.
    FormSection outputSection = form.addSection("Output");

    outputSection.addTextField(_outputVolumeName);
  }

  public void propertyChanged(String key) {
    if (key.equals(_inputVolume.getKey()) && !_inputVolume.isNull()) {
      SeismicDataset inputVolume = _inputVolume.get();
      _outputVolumeName.set(inputVolume.getDisplayName() + "_traceint");
    }
  }

  public void validate(IValidation results) {
    if (_inputVolume.isNull()) {
      results.error(_inputVolume, "No input volume specified.");
    } else {
      SeismicDataset inputVolume = _inputVolume.get();
      if (!PostStack2dLine.class.isAssignableFrom(inputVolume.getClass())
          && !PostStack3d.class.isAssignableFrom(inputVolume.getClass())) {
        results.error(_inputVolume, "Only PostStack2d and PostStack3d currently supported.");
      }
    }

    if (_useAreaOfInterest.get() && _areaOfInterest.isNull()) {
      results.error(_areaOfInterest, "No area of interest specified.");
    }

    if (_operatorLength.get() < 0) {
      results.error(_operatorLength, "Operator length must be >= 0.");
    }

    // Validate the output volume name.
    if (_outputVolumeName.isEmpty()) {
      results.error(_outputVolumeName, "No output volume name specified.");
    } else {
      SeismicDataset inputVolume = _inputVolume.get();
      if (PostStack3d.class.isAssignableFrom(inputVolume.getClass())) {
        PostStack3d inputVolume3d = (PostStack3d) inputVolume;
        if (inputVolume3d != null) {
          IStatus status = DataSource.validateName(inputVolume, _outputVolumeName.get());
          if (!status.isOK()) {
            results.setStatus(_outputVolumeName, status);
          } else if (PostStack3dFactory.existsInStore(inputVolume3d, _outputVolumeName.get())) {
            results.warning(_outputVolumeName, "Exists in datastore and will be overwritten.");
          }
        }
      }
    }

  }

  /**
   * Trace integration. This method receives a trace, integrates it and if necessary removes the low
   * frequency drift.
   * @param trace from input volume.
   * @param operatorLength the operator length.
   * @return trace values
   */

  public static float[] integrateTrace(final Trace trace, final int operatorLength) {

    // Get the input trace for the inline and xline.
    float[] outputData = trace.getData();

    // No null traces are allowed.
    if (outputData.length > 1) {
      int firstLive = firstLive(outputData, 0.0001);
      int lastLive = lastLive(outputData, 0.0001);
      float zero = 0.0f;

      if (Double.isNaN(outputData[0])) {
        outputData[0] = zero;
      }

      // Integrates trace...also known as runSum.
      for (int i = 1; i < outputData.length; i++) {
        if (!Double.isNaN(outputData[i])) {
          outputData[i] = outputData[i] + outputData[i - 1];
        } else {
          outputData[i] = outputData[i - 1];
        }
      }

      // The user will be required to enter an operator length (> 1) for the removal of low frequency drift.
      if (operatorLength > 1) {
        outputData = removeLowFreqDrift(outputData, operatorLength);
      }

      // Re-zero to the original mute positions, if necessary.
      for (int i = 0; i < firstLive; i++) {
        outputData[i] = zero;
      }

      for (int i = lastLive + 1; i < outputData.length; i++) {
        outputData[i] = zero;
      }
    }

    return outputData;
  }

  /**
   * Removes low frequency drift from trace data, if necessary.
   * 
   * @param traceData is an array of trace values.
   * @param operatorLength is the smoother length in samples.
   * @return creates a smoothed version of trace.
   */
  public static final float[] removeLowFreqDrift(final float[] traceData, final int operatorLength) {

    if (traceData.length < operatorLength) {
      throw new IllegalArgumentException("Trace is shorter than operator.");
    }

    int imax = traceData.length;

    // Create smoothed version of trace.
    float[] smoother = new float[traceData.length];
    smoother = smoothMeanBased(traceData, smoother, operatorLength);

    // Smoothed trace subtract from original trace to remove drift.
    for (int i = 0; i < imax; i++) {
      traceData[i] = traceData[i] - smoother[i];
    }

    return traceData;
  }

  /**
   * Top mute picker.
   * @param tvals is an array of trace values.
   * @param threshold provides cutoff value for the trace samples.
   * @return index of first value exceeding threshold, starting from the top.
   */

  private static int firstLive(final float[] tvals, final double threshold) {

    int i;
    int firstLive = 0;

    for (i = 0; i < tvals.length; i++) {
      if (Math.abs(tvals[i]) > threshold) {
        firstLive = i;
        i = tvals.length + 1;
      }

    }

    return firstLive;
  }

  /**
   * Bottom mute picker
   * @param tvals is an array of trace values.
   * @param threshold provides cutoff value for the trace samples.
   * @return index of first value exceeding threshold, starting from the bottom
   */

  private static int lastLive(final float[] tvals, final double threshold) {

    int i;
    int lastLive = tvals.length - 1;

    for (i = tvals.length - 1; i >= 0; i--) {
      if (Math.abs(tvals[i]) > threshold) {
        lastLive = i;
        i = -1;
      }
    }

    return lastLive;
  }

  /**
   * Smoothes a trace using an average-based function.
   * 
   * @param inputData input trace values.
   * @param outputData output trace values.
   * @param windLen window length used to run function.
   * @return array of smoothed trace values.
   */
  private static float[] smoothMeanBased(final float[] inputData, final float[] outputData, final int windLen) {

    int i = 0;
    int imax = windLen / 2;

    // First half of first window.
    while (i < imax) {
      outputData[i] = inputData[i];
      i++;
    }

    // Calculates average for middle portion of array; the window length being the number of array
    // values that are summed for the average.
    imax = inputData.length - windLen / 2;

    while (i < imax) {
      outputData[i] = calculateAverage(inputData, windLen, i);
      i++;
    }

    // Last half of last window.
    imax = inputData.length;

    while (i < imax) {
      outputData[i] = inputData[i];
      i++;
    }

    return outputData;
  }

  /**
   * Calculates average in order to smooth data.
   * 
   * @param data array of trace values.
   * @param window number of values to average.
   * @param count keeps track of position in array.
   * @return average.
   */
  private static float calculateAverage(final float[] data, final int window, final int count) {

    float sum = 0;
    int live = 0;
    int j = count - window / 2;
    int jmax = count + window / 2;
    float v;

    while (j < jmax) {

      v = data[j];
      if (!Double.isNaN(v)) {
        sum += v;
        live++;
      }
      j++;

    }

    if (live != 0) {
      return sum / live;
    }

    return data[count];
  }
}
