/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.volume.mvxp;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.factory.model.PostStack3dFactory;
import org.geocraft.core.model.DataSource;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.SeismicDataset.StorageFormat;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.io.util.TraceProducer;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.EntityComboField;
import org.geocraft.ui.form2.field.SpinnerField;
import org.geocraft.ui.form2.field.TextField;


public class MVXP3D extends StandaloneAlgorithm {

  /** The maximum number of worker threads. */
  private static final int MAX_THREADS = Math.max(1, Runtime.getRuntime().availableProcessors() - 2);

  private static final String INPUT_VOLUME = "Input Volume";

  private static final String OUTPUT_VOLUME_NAME = "Output Volume Name";

  /** The input 3D volume. */
  private EntityProperty<PostStack3d> _inputVolume;

  /** The area-of-interest (optional). */
  private EntityProperty<AreaOfInterest> _areaOfInterest;

  /** The area-of-interest usage flag. */
  private BooleanProperty _useAreaOfInterest;

  /** The starting z value. */
  private FloatProperty _zStart;

  /** The ending z value. */
  private FloatProperty _zEnd;

  /** The scaling factor. */
  private FloatProperty _scaleFactor;

  /** The clipping factor (optional). */
  private FloatProperty _clipFactor;

  /** The clipping factor usage flag. */
  private BooleanProperty _useClipFactor;

  /** The window length for the MVXP. */
  private IntegerProperty _windowLength;

  /** The number of workers (i.e. threads) to use. */
  private IntegerProperty _numWorkers;

  /** The output volume name. */
  private StringProperty _outputVolumeName;

  /** The output volume type. */
  private EnumProperty<OutputVolumeType> _outputVolumeType;

  public MVXP3D() {
    _inputVolume = addEntityProperty(INPUT_VOLUME, PostStack3d.class);
    _areaOfInterest = addEntityProperty("Area of Interest", AreaOfInterest.class);
    _useAreaOfInterest = addBooleanProperty("Use Area of Interest", false);
    _zStart = addFloatProperty("Start Time (or Depth)", 0);
    _zEnd = addFloatProperty("End Time (or Depth)", 0);
    _scaleFactor = addFloatProperty("Scale Factor", 5.08f);
    _clipFactor = addFloatProperty("Clip Factor", 0);
    _useClipFactor = addBooleanProperty("Use Clip Factor", false);
    _windowLength = addIntegerProperty("Window Length (# of samples)", 101);
    _numWorkers = addIntegerProperty("# of Threads", MAX_THREADS);
    _outputVolumeName = addStringProperty(OUTPUT_VOLUME_NAME, "mvxp");
    _outputVolumeType = addEnumProperty("Output Volume Type", OutputVolumeType.class, OutputVolumeType.SAME_AS_INPUT);
  }

  @Override
  public String[] getUnpickleKeyOrder() {
    // Ensure that the input volume gets set before the output volume name.
    return new String[] { INPUT_VOLUME, OUTPUT_VOLUME_NAME };
  }

  @Override
  public void buildView(IModelForm form) {
    FormSection section = form.addSection("Input Data");

    EntityComboField inputVolume = section.addEntityComboField(_inputVolume, PostStack3d.class);
    inputVolume.setTooltip("The input volume (PostStack2d or PostStack3d only");

    // Set input to just a list of area of interest items
    EntityComboField aoiField = section.addEntityComboField(_areaOfInterest, AreaOfInterest.class);
    aoiField.showActiveFieldToggle(_useAreaOfInterest);

    TextField zStartField = section.addTextField(_zStart);
    zStartField.setTooltip("The starting time (or depth) to process.");

    TextField zEndField = section.addTextField(_zEnd);
    zEndField.setTooltip("The ending time (or depth) to process.");

    section = form.addSection("Parameters");

    TextField scaleFactor = section.addTextField(_scaleFactor);
    scaleFactor.setTooltip("The factor to by which to scale the results");

    TextField clipFactor = section.addTextField(_clipFactor);
    clipFactor.setTooltip("The threshold for which the algorithm will limit the median value");
    clipFactor.showActiveFieldToggle(_useClipFactor);

    TextField windowLength = section.addTextField(_windowLength);
    windowLength.setTooltip("The window (in samples) for calculating the median value");

    SpinnerField numThreads = section.addSpinnerField(_numWorkers, 1, MAX_THREADS, 0, 1);
    numThreads.setTooltip("The number of worker threads.");

    section = form.addSection("Output");

    TextField outputVolumeName = section.addTextField(_outputVolumeName);
    outputVolumeName.setTooltip("The name of the output volume");

    ComboField outputVolumeType = section.addComboField(_outputVolumeType, OutputVolumeType.values());
    outputVolumeType.setTooltip("The type of the output volume");
  }

  public void propertyChanged(String key) {
    if (key.equals(_inputVolume.getKey())) {
      // If the input volume is changed (and non-null), then auto-generate an output volume name.
      if (!_inputVolume.isNull()) {
        PostStack3d inputVolume = _inputVolume.get();
        _zStart.set(inputVolume.getZStart());
        _zEnd.set(inputVolume.getZEnd());
        _outputVolumeName.set(inputVolume.getDisplayName() + "_mvxp");
      }
    }
  }

  public void validate(IValidation results) {
    // Validate the input volume.
    if (_inputVolume.isNull()) {
      results.error(_inputVolume, "No input volume specified.");
    }

    // Validate the AOI.
    if (_useAreaOfInterest.get() && _areaOfInterest.isNull()) {
      results.error(_areaOfInterest, "No area of interest specified.");
    }

    // Validate the start/end z values.
    if (_zEnd.get() < _zStart.get()) {
      results.error(_zEnd, "The end time (or depth) must be >= the start time (or depth).");
    }

    // Validate the window length.
    if (_windowLength.get() < 1) {
      results.error(_windowLength, "The window length must be > 0.");
    }

    // Validate the start/end times against the input volume.
    if (!_inputVolume.isNull()) {
      PostStack3d inputVolume = _inputVolume.get();
      float zStart = inputVolume.getZStart();
      float zEnd = inputVolume.getZEnd();
      if (_zStart.get() < zStart) {
        results.error(_zStart, "Start time (or depth) outside of range " + zStart + " to " + zEnd + ".");
      }
      if (_zEnd.get() > zEnd) {
        results.error(_zEnd, "End time (or depth) outside of range " + zStart + " to " + zEnd + ".");
      }
    }

    // Validate there is at least 1 worker thread.
    if (_numWorkers.get() < 1) {
      results.error(_numWorkers, "Must have at least 1 worker thread.");
    }

    // Validate the output volume name.
    if (_outputVolumeName.isEmpty()) {
      results.error(_outputVolumeName, "No output volume name specified.");
    } else {
      if (!_inputVolume.isNull()) {
        IStatus status = DataSource.validateName(_inputVolume.get(), _outputVolumeName.get());
        if (!status.isOK()) {
          results.setStatus(_outputVolumeName, status);
        } else if (PostStack3dFactory.existsInStore(_inputVolume.get(), _outputVolumeName.get())) {
          results.warning(_outputVolumeName, "Exists in datastore and will be overwritten.");
        }
      }
    }
  }

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) {

    // Unpack the properties.
    PostStack3d inputVolume = _inputVolume.get();
    AreaOfInterest aoi = _areaOfInterest.get();
    if (!_useAreaOfInterest.get()) {
      aoi = null;
    }
    float zStart = _zStart.get();
    float zEnd = _zEnd.get();
    float scaleFactor = _scaleFactor.get();
    float clipFactor = _clipFactor.get();
    if (!_useClipFactor.get()) {
      clipFactor = 0;
    }
    int windowLength = _windowLength.get();
    int numWorkers = _numWorkers.get();
    String outputVolumeName = _outputVolumeName.get();
    OutputVolumeType outputVolumeType = _outputVolumeType.get();

    try {
      mvxpPostStack3d(inputVolume, aoi, zStart, zEnd, windowLength, scaleFactor, clipFactor, numWorkers,
          outputVolumeName, outputVolumeType, monitor, repository);
    } catch (Exception ex) {
      throw new RuntimeException(ex.toString(), ex);
    }
  }

  /**
   * Runs the MVXP algorithm on a <code>PostStack3d</code> volume.
   * 
   * @param inputVolume the input 3D volume.
   * @param aoi the area-of-interest (null for none).
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @param windowLength the window length (in samples).
   * @param scaleFactor the scale factor.
   * @param clipFactor the clipping factor.
   * @param numWorkers the number of worker threads.
   * @param outputVolumeName the output volume name.
   * @param outputVolumeType the output volume type.
   * @param monitor the progress monitor.
   * @param repository the data repository.
   */
  public void mvxpPostStack3d(final PostStack3d inputVolume, final AreaOfInterest inputAOI, final double zStart,
      final double zEnd, final int windowLength, final float scaleFactor, final float clipFactor, final int numWorkers,
      final String outputVolumeName, final OutputVolumeType outputVolumeType, final IProgressMonitor monitor,
      final IRepository repository) throws Exception {

    // Create an output volume based on the input volume.
    StorageFormat storageFormat = StorageFormat.FLOAT_32;
    switch (outputVolumeType) {
      case SAME_AS_INPUT:
        storageFormat = inputVolume.getStorageFormat();
        break;
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
    PostStack3d outputVolume = PostStack3dFactory.create(repository, inputVolume, outputVolumeName, storageFormat,
        inputVolume.getZDelta());

    // Determine the index values for the start and end time (depth).
    float zDelta = inputVolume.getZDelta();
    float zStartVol = inputVolume.getZStart();
    float zEndVol = inputVolume.getZEnd();
    int zStartIndex = (int) Math.rint((zStart - zStartVol) / zDelta);
    int zEndIndex = (int) Math.rint((zEnd - zStartVol) / zDelta);

    // Initialize the progress monitor.
    int totalTraces = inputVolume.getNumInlines() * inputVolume.getNumXlines();
    monitor.beginTask("MVXP \'" + inputVolume.getDisplayName() + "\'", totalTraces);

    // Create a trace iterator for the input volume.
    TraceProducer producer = new TraceProducer(inputVolume, inputAOI, inputVolume.getPreferredOrder(), zStartVol,
        zEndVol, true, 5);
    List<Thread> workers = new ArrayList<Thread>();
    for (int i = 0; i < numWorkers; i++) {
      MVXPWorker worker = new MVXPWorker(producer, outputVolume, zStartIndex, zEndIndex, windowLength, scaleFactor,
          clipFactor, monitor);
      Thread t = new Thread(worker);
      workers.add(t);
      t.start();
    }
    // Wait for all the worker threads to finish.
    for (int i = 0; i < numWorkers; i++) {
      workers.get(i).join();
    }

    // Close the input and output volumes.
    monitor.subTask("Closing volumes...");
    inputVolume.close();
    outputVolume.close();

    monitor.done();
  }
}
