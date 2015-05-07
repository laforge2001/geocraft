package org.geocraft.abavo;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.abavo.classbkg.ABavoAlgorithm3dWorker;
import org.geocraft.abavo.defs.ABavoDataMode;
import org.geocraft.abavo.defs.ABavoTimeMode;
import org.geocraft.abavo.input.InputProcess3d;
import org.geocraft.abavo.input.TraceReader3d;
import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.aoi.SeismicSurvey3dAOI;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.Property;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.Wavelet;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.io.util.MultiVolumeTraceIterator;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.field.aoi.AOIComboField;


public abstract class ABavoBaseAlgorithm3d extends ABavoAbstractAlgorithm {

  public static final int MAX_THREADS = Math.max(1, Runtime.getRuntime().availableProcessors() - 2);

  protected IntegerProperty _numWorkers;

  /** Input data properties. */

  protected EntityProperty<PostStack3d> _volumeA;

  protected EntityProperty<PostStack3d> _volumeB;

  /** The starting inline property. */
  protected FloatProperty _inlineStart;

  /** The ending inline property. */
  protected FloatProperty _inlineEnd;

  /** The seismic starting xline property. */
  protected FloatProperty _xlineStart;

  /** The seismic ending xline property. */
  protected FloatProperty _xlineEnd;

  protected EntityProperty<Grid3d> _topGrid;

  protected EntityProperty<Grid3d> _baseGrid;

  protected InputProcess3d _inputProcess;

  public ABavoBaseAlgorithm3d(boolean alwaysUseAllData) {
    super(alwaysUseAllData);
  }

  @Override
  protected void addVolumeProperties() {
    _numWorkers = addIntegerProperty("# of Worker Threads", 1);
    _volumeA = addEntityProperty(VOLUME_A, PostStack3d.class);
    _volumeB = addEntityProperty(VOLUME_B, PostStack3d.class);
  }

  @Override
  protected void addBoundsProperties() {
    _inlineStart = addFloatProperty("Inline Start", 0);
    _inlineEnd = addFloatProperty("Inline End", 0);
    _xlineStart = addFloatProperty("X-line Start", 0);
    _xlineEnd = addFloatProperty("X-line End", 0);
  }

  @Override
  protected void addGridProperties() {
    _topGrid = addEntityProperty("Top Grid", Grid3d.class);
    _baseGrid = addEntityProperty("Base Grid", Grid3d.class);
  }

  @Override
  protected void addVolumeFields(FormSection section) {
    section.addSpinnerField(_numWorkers, 1, MAX_THREADS, 0, 1);
    section.addEntityComboField(_volumeA, PostStack3d.class);
    section.addEntityComboField(_volumeB, PostStack3d.class);
  }

  @Override
  protected void addBoundsFields(FormSection section) {
    AOIComboField aoiField = section.addAOIComboField(_areaOfInterest, 3);
    aoiField.showActiveFieldToggle(_useAreaOfInterest);

    section.addTextField(_inlineStart);
    section.addTextField(_inlineEnd);
    section.addTextField(_xlineStart);
    section.addTextField(_xlineEnd);
  }

  @Override
  protected void addGridFields(FormSection section) {
    section.addEntityComboField(_topGrid, Grid3d.class);
    section.addEntityComboField(_baseGrid, Grid3d.class);
  }

  public void propertyChanged(String key) {
    String propertyName = key;
    if (propertyName.equals(_volumeA.getKey())) {
      setFieldEnabled(_outputSampleRate, !_volumeA.isNull());
      if (!_volumeA.isNull()) {
        PostStack3d volumeA = _volumeA.get();
        float deltaZ = volumeA.getZDelta();
        Float[] options = new Float[3];
        for (int i = 0; i < 3; i++) {
          options[i] = new Float(deltaZ);
          deltaZ *= 0.5f;
        }
        setFieldOptions(_outputSampleRate, options);
        _outputSampleRate.set(volumeA.getZDelta());
      }
    }

    if (propertyName.equals(_convertNearFarToInterceptGradient.getKey())) {
      boolean convert = _convertNearFarToInterceptGradient.get();
      setFieldEnabled(_interceptScalar, convert);
      setFieldEnabled(_gradientScalar, convert);
    } else if (propertyName.equals(_autoAlignBtoA.getKey())) {
      boolean autoAlign = _autoAlignBtoA.get();
      setFieldEnabled(_correlationWindow, autoAlign);
      setFieldEnabled(_maximumShift, autoAlign);
      setFieldEnabled(_correlationThreshold, autoAlign);
      setFieldEnabled(_smoothingFilterLength, autoAlign);
      setFieldEnabled(_nearAmplitudeThreshold, autoAlign);
      setFieldEnabled(_amplitudeThresholdWindow, autoAlign);
    } else if (propertyName.equals(_volumeA.getKey())) {
      PostStack3d volumeA = _volumeA.get();
      if (volumeA != null) {
        String zUnitStr = volumeA.getZUnit().getSymbol();
        setFieldLabel(_correlationWindow, "Correlation Window (" + zUnitStr + ")");
        setFieldLabel(_maximumShift, "Maximum Shift (" + zUnitStr + ")");
        setFieldLabel(_smoothingFilterLength, "Smoothing Filter Length (" + zUnitStr + ")");
        setFieldLabel(_amplitudeThresholdWindow, "Ampl. Threshold Window (" + zUnitStr + ")");
      }
    }

    // Update the bounds sections.
    boolean useAOI = useAreaOfInterest();
    if (propertyName.equals(_timeMode.getKey())) {
      setFieldEnabled(_timeStart, !useTopGrid());
      setFieldEnabled(_timeEnd, !useTopGrid());
      setFieldEnabled(_topGrid, useTopGrid());
      setFieldEnabled(_baseGrid, useBaseGrid());
      setFieldEnabled(_relativeStart, useTopGrid() || useBaseGrid());
      setFieldEnabled(_relativeEnd, useTopGrid() || useBaseGrid());
    } else if (propertyName.equals(_volumeA.getKey())) {
      PostStack3d volumeA = _volumeA.get();
      if (volumeA != null) {
        _inlineStart.set(volumeA.getInlineStart());
        _inlineEnd.set(volumeA.getInlineEnd());
        _xlineStart.set(volumeA.getXlineStart());
        _xlineEnd.set(volumeA.getXlineEnd());
        _timeStart.set(volumeA.getZStart());
        _timeEnd.set(volumeA.getZEnd());
        Domain domain = volumeA.getZDomain();
        String zUnitStr = volumeA.getZUnit().getSymbol();
        setFieldLabel(_timeMode, domain + " Mode");
        setFieldLabel(_timeStart, "Start " + domain + " (" + zUnitStr + ")");
        setFieldLabel(_timeEnd, "End " + domain + " (" + zUnitStr + ")");
        setFieldLabel(_relativeStart, "Relative Start (" + zUnitStr + ")");
        setFieldLabel(_relativeEnd, "Relative End (" + zUnitStr + ")");
      }
    } else if (propertyName.equals(_useAreaOfInterest.getKey())) {
      setFieldEnabled(_inlineStart, !useAOI);
      setFieldEnabled(_inlineEnd, !useAOI);
      setFieldEnabled(_xlineStart, !useAOI);
      setFieldEnabled(_xlineEnd, !useAOI);
    }
  }

  public void validate(IValidation results) {
    int numWorkers = _numWorkers.get();
    if (numWorkers < 1 || numWorkers > MAX_THREADS) {
      results.error(_numWorkers, "# of worker threads must be in the range 1-" + MAX_THREADS);
    }
    PostStack3d volumeA = _volumeA.get();
    if (volumeA == null) {
      results.error(_volumeA, "Volume A not specified");
    } else {
      float outputSampleRate = _outputSampleRate.get();
      if (MathUtil.isEqual(outputSampleRate, 0)) {
        results.error(_outputSampleRate, "Output sample rate cannot be zero.");
      } else {
        int ratio = Math.round(volumeA.getZDelta() / outputSampleRate);
        if (ratio != 1 && ratio != 2 && ratio != 4) {
          results.error(_outputSampleRate, "Output sample rate is invalid: " + outputSampleRate + ".");
        }
      }
    }
    PostStack3d volumeB = _volumeB.get();
    if (volumeB == null) {
      results.error(_volumeB, "Volume B not specified");
    }
    if (volumeA != null && volumeB != null) {
      if (!volumeA.getZDomain().equals(volumeB.getZDomain())) {
        results.error(_volumeB, "Volume B domain (" + volumeB.getZDomain() + ") does not match volume A domain ("
            + volumeA.getZDomain() + ")");
      }
      if (!volumeA.getClass().equals(volumeB.getClass())) {
        results.error(_volumeB, "Volume B dataset type (" + volumeB.getClass().getSimpleName()
            + ") does not match volume A dataset type (" + volumeA.getClass().getSimpleName() + ")");
      }

      // Check that the geometries of A and B match.
      if (volumeA != null && volumeB != null && !volumeA.getSurvey().matchesGeometry(volumeB.getSurvey())) {
        results.error(_volumeB, "Volume B geometry does not match volume A geometry.");
      }
    }
    ABavoDataMode dataMode = _dataMode.get();
    if (dataMode == null) {
      results.error(_dataMode, "Invalid data mode: " + dataMode);
    }
    float nearAngle = _nearAngle.get();
    if (nearAngle < 0 || nearAngle > 90) {
      results.error(_nearAngle, "Near angle must be between 0 and 90");
    }
    float farAngle = _farAngle.get();
    if (farAngle < 0 || farAngle > 90) {
      results.error(_farAngle, "Far angle must be between 0 and 90");
    }
    if (farAngle < nearAngle) {
      results.error(_farAngle, "Far angle must be greater than near angle");
    }
    if (_autoAlignBtoA.get()) {
      if (_correlationWindow.get() < 0) {
        results.error(_correlationWindow, "Correlation window cannot be negative");
      }
      if (_maximumShift.get() < 0) {
        results.error(_maximumShift, "Maximum shift cannot be negative");
      }
      if (_correlationThreshold.get() < 0 || _correlationThreshold.get() > 1) {
        results.error(_correlationThreshold, "Correlation threshold must be between 0 and 1");
      }
      if (_smoothingFilterLength.get() < 0) {
        results.error(_smoothingFilterLength, "Smoothing filter length cannot be negative");
      }
      if (_nearAmplitudeThreshold.get() < 0) {
        results.error(_nearAmplitudeThreshold, "Near amplitude threshold cannot be negative");
      }
      if (_amplitudeThresholdWindow.get() < 0) {
        results.error(_amplitudeThresholdWindow, "Amplitude threshold window cannot be negative");
      }
    }
    float outputSampleRate = _outputSampleRate.get();

    if (useWaveletScalarA()) {
      if (_waveletScalarA.isNull()) {
        results.error(_waveletScalarA, "The A wavelet filter must be specified.");
      } else {
        if (!_volumeA.isNull()) {
          Wavelet waveletA = _waveletScalarA.get();
          if (Float.compare(outputSampleRate, waveletA.getTimeInterval()) != 0) {
            results.error(_waveletScalarA, "The interval of the A wavelet filter (" + waveletA.getTimeInterval()
                + ") does not match the output sample rate (" + outputSampleRate + ").");
          }
        }
      }
    }

    if (useWaveletScalarB()) {
      if (_waveletScalarB.isNull()) {
        results.error(_waveletScalarB, "The B wavelet filter must be specified.");
      } else {
        if (!_volumeB.isNull()) {
          Wavelet waveletB = _waveletScalarB.get();
          if (Float.compare(outputSampleRate, waveletB.getTimeInterval()) != 0) {
            results.error(_waveletScalarB, "The interval of the B wavelet filter (" + waveletB.getTimeInterval()
                + ") does not match the output sample rate (" + outputSampleRate + ").");
          }
        }
      }
    }
    if (useAreaOfInterest()) {
      if (_areaOfInterest.isNull()) {
        results.error(_areaOfInterest, "The area of interest must be specified");
      }
    } else {
      if (!_volumeA.isNull()) {
        float inlineStart = _inlineStart.get();
        float inlineEnd = _inlineEnd.get();
        float xlineStart = _xlineStart.get();
        float xlineEnd = _xlineEnd.get();
        validateValueInRange(results, _inlineStart, inlineStart, volumeA.getInlineStart(), volumeA.getInlineEnd(),
            volumeA.getInlineDelta());
        validateValueInRange(results, _inlineEnd, inlineEnd, volumeA.getInlineStart(), volumeA.getInlineEnd(), volumeA
            .getInlineDelta());
        validateValueInRange(results, _xlineStart, xlineStart, volumeA.getXlineStart(), volumeA.getXlineEnd(), volumeA
            .getXlineDelta());
        validateValueInRange(results, _xlineEnd, xlineEnd, volumeA.getXlineStart(), volumeA.getXlineEnd(), volumeA
            .getXlineDelta());
      }
    }
    ABavoTimeMode timeMode = _timeMode.get();
    if (timeMode == null) {
      results.error(_timeMode, "Invalid time bounds: " + timeMode);
    } else {
      if (timeMode.equals(ABavoTimeMode.BETWEEN_TIMES)) {
        float timeStart = _timeStart.get();
        float timeEnd = _timeEnd.get();
        if (timeEnd < timeStart) {
          results.error(_timeEnd, "End time must be >= start time");
        }
        if (volumeA != null) {
          if (timeStart < volumeA.getZStart() || timeStart > volumeA.getZEnd()) {
            results
                .error(_timeStart, "Start time must be between " + volumeA.getZStart() + " and " + volumeA.getZEnd());
          }
          if (timeEnd < volumeA.getZStart() || timeEnd > volumeA.getZEnd()) {
            results.error(_timeEnd, "End time must be between " + volumeA.getZStart() + " and " + volumeA.getZEnd());
          }
        }
      }
      Grid3d topGrid = _topGrid.get();
      Grid3d baseGrid = _baseGrid.get();
      if (useTopGrid() && _topGrid.isNull()) {
        results.error(_topGrid, "Top horizon must be specified.");
      }
      if (useBaseGrid() && _baseGrid.isNull()) {
        results.error(_baseGrid, "Base horizon must be specified.");
      }
      if (useTopGrid() && topGrid != null && volumeA != null) {
        Unit topGridUnit = topGrid.getDataUnit();
        Domain topGridDomain = topGridUnit.getDomain();
        if (topGridDomain == null || !volumeA.getZDomain().equals(topGridDomain)) {
          results.error(_topGrid, "Top horizon domain (" + topGridDomain + ") does not match seismic z domain ("
              + volumeA.getZDomain() + ")");
        }
      }
      if (useBaseGrid() && baseGrid != null && volumeA != null) {
        Unit baseGridUnit = baseGrid.getDataUnit();
        Domain baseGridDomain = baseGridUnit.getDomain();
        if (baseGridDomain == null || !volumeA.getZDomain().equals(baseGridDomain)) {
          results.error(_baseGrid, "Base horizon domain (" + baseGridDomain + ") does not match seismic z domain ("
              + volumeA.getZDomain() + ")");
        }
      }
    }
  }

  protected abstract ABavoAlgorithm3dWorker createWorker(int workerID, IProgressMonitor monitor, ILogger logger,
      IRepository repository, InputProcess3d inputProcess);

  public int getNumWorkerThreads() {
    return _numWorkers.get();
  }

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) {
    int numWorkers = getNumWorkerThreads();

    // Initialize.
    initialize(repository);

    // Begin the task.
    String taskName = getTaskName();
    monitor.beginTask(taskName, 100 * numWorkers);

    long time0 = System.currentTimeMillis();

    // Create and start the trace reader.
    PostStack3d volumeA = getVolumeA();
    PostStack3d volumeB = getVolumeB();
    AreaOfInterest aoi = getAreaOfInterest();
    if (aoi == null || !useAreaOfInterest()) {
      float iln0 = getInlineStart();
      float iln1 = getInlineEnd();
      float xln0 = getXlineStart();
      float xln1 = getXlineEnd();
      aoi = new SeismicSurvey3dAOI("aoi", volumeA.getSurvey(), iln0, iln1, xln0, xln1);
    }
    TraceReader3d traceReader = new TraceReader3d(new MultiVolumeTraceIterator(aoi, volumeA.getZStart(), volumeA
        .getZEnd(), volumeA, volumeB), numWorkers);
    new Thread(traceReader).start();

    List<ABavoAlgorithm3dWorker> workers = new ArrayList<ABavoAlgorithm3dWorker>();
    List<Thread> workerThreads = new ArrayList<Thread>();
    for (int i = 0; i < numWorkers; i++) {
      InputProcess3d inputProcess = new InputProcess3d(this, traceReader);
      int workerID = i;
      ABavoAlgorithm3dWorker worker = createWorker(workerID, monitor, logger, repository, inputProcess);
      workers.add(worker);
      Thread thread = new Thread(worker);
      workerThreads.add(thread);
      thread.start();
    }
    for (int i = 0; i < numWorkers; i++) {
      try {
        workerThreads.get(i).join();
      } catch (InterruptedException ex) {
        throw new RuntimeException(ex);
      }
    }
    long time1 = System.currentTimeMillis();
    System.out.println("ElapsedTime(msec) " + (time1 - time0));
    for (int i = 0; i < numWorkers; i++) {
      workers.get(i).cleanup();
    }

    // Long the appropriate end-of-task message.
    if (!monitor.isCanceled()) {
      logger.info(taskName + " task done!");
    } else {
      logger.info(taskName + " task canceled!");
    }
    monitor.done();

    // Cleanup.
    cleanup();
  }

  protected void initialize(IRepository repository) {
    // Nothing to do.
  }

  protected void cleanup() {
    // Nothing to do.
  }

  protected abstract void processTraceData(final TraceData[] traceData);

  @Override
  protected abstract String getTaskName();

  public PostStack3d getVolumeA() {
    return _volumeA.get();
  }

  public PostStack3d getVolumeB() {
    return _volumeB.get();
  }

  public float getInlineStart() {
    return _inlineStart.get();
  }

  public float getInlineEnd() {
    return _inlineEnd.get();
  }

  public float getXlineStart() {
    return _xlineStart.get();
  }

  public float getXlineEnd() {
    return _xlineEnd.get();
  }

  public Grid3d getTopGrid() {
    return _topGrid.get();
  }

  public Grid3d getBaseGrid() {
    return _baseGrid.get();
  }

  @Override
  public int getDataDimension() {
    return 3;
  }

  @Override
  protected void validateValueInRange(final IValidation results, final Property property, final float value,
      final float start, final float end, final float delta) {
    String key = property.getKey();
    if (Math.abs((value - start) % delta) > EPSILON && Math.abs((value - start) % delta) < Math.abs(delta) - EPSILON) {
      results.error(key, key + " value is invalid");
    }
    if (start < end && (value < start || value > end)) {
      results.error(key, key + " value " + value + " not between " + start + " and " + end);
    }
    if (start > end && (value > start || value < end)) {
      results.error(key, key + " value " + value + " not between " + end + " and " + start);
    }
  }

  @Override
  protected String[] getUnpickleKeyOrder() {
    return new String[] { VOLUME_A, VOLUME_B };
  }
}
