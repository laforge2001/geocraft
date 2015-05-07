/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.volume.mvxp;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.common.math.MVXP;
import org.geocraft.core.factory.model.PostStack2dLineFactory;
import org.geocraft.core.model.DataSource;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.StringArrayProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.seismic.PostStack2d;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.SeismicDataset.StorageFormat;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.io.util.TraceIterator;
import org.geocraft.io.util.TraceIteratorFactory;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.EntityComboField;
import org.geocraft.ui.form2.field.OrderedListField;
import org.geocraft.ui.form2.field.TextField;


public class MVXP2D extends StandaloneAlgorithm {

  private EntityProperty<PostStack2d> _inputVolume;

  private StringArrayProperty _inputLines;

  private FloatProperty _scaleFactor;

  private FloatProperty _clipFactor;

  private BooleanProperty _useClipFactor;

  private IntegerProperty _windowLength;

  private StringProperty _outputVolumeName;

  private EnumProperty<OutputVolumeType> _outputVolumeType;

  public MVXP2D() {
    _inputVolume = addEntityProperty("Input Volume", PostStack2d.class);
    _inputLines = addStringArrayProperty("Input Lines");
    _scaleFactor = addFloatProperty("Scale Factor", 5.08f);
    _clipFactor = addFloatProperty("Clip Factor", 0);
    _useClipFactor = addBooleanProperty("Use Clip Factor", false);
    _windowLength = addIntegerProperty("Window Length (# of samples)", 101);
    _outputVolumeName = addStringProperty("Output Volume Name", "");
    _outputVolumeType = addEnumProperty("Output Volume Type", OutputVolumeType.class, OutputVolumeType.SAME_AS_INPUT);
  }

  @Override
  public void buildView(IModelForm form) {
    FormSection section = form.addSection("Input Data");

    EntityComboField inputVolume = section.addEntityComboField(_inputVolume, PostStack2d.class);
    inputVolume.setTooltip("The input volume (PostStack2d only");

    OrderedListField inputLines = section.addOrderedListField(_inputLines, new String[0]);
    inputLines.setTooltip("The input lines");

    section = form.addSection("Parameters");

    TextField scaleFactor = section.addTextField(_scaleFactor);
    scaleFactor.setTooltip("The factor to by which to scale the results");

    TextField clipFactor = section.addTextField(_clipFactor);
    clipFactor.setTooltip("The threshold for which the algorithm will limit the median value");
    clipFactor.showActiveFieldToggle(_useClipFactor);

    TextField windowLength = section.addTextField(_windowLength);
    windowLength.setTooltip("The window (in samples) for calculating the median value");

    section = form.addSection("Output");

    TextField outputVolumeName = section.addTextField(_outputVolumeName);
    outputVolumeName.setTooltip("The name of the output volume");

    ComboField outputVolumeType = section.addComboField(_outputVolumeType, OutputVolumeType.values());
    outputVolumeType.setTooltip("The type of the output volume");
  }

  @Override
  public void propertyChanged(String key) {
    // No UI updates.
    if (key.equals(_inputVolume.getKey())) {
      if (!_inputVolume.isNull()) {
        PostStack2d collection = _inputVolume.get();
        setFieldOptions(_inputLines, collection.getLineNames(true));
        //_startTime.set(inputVolume.getZStart());
        //_endTime.set(inputVolume.getZEnd());
      }
    }
  }

  @Override
  public void validate(IValidation results) {
    // Validate the input volume.
    if (_inputVolume.isNull()) {
      results.error(_inputVolume, "No input volume specified.");
    }

    if (_inputLines.isEmpty()) {
      results.error(_inputLines, "No input lines specified.");
    }

    // Validate the window length.
    if (_windowLength.get() < 1) {
      results.error(_windowLength, "The window length must be > 0.");
    }

    // Validate the start/end times against the input volume.
    if (!_inputVolume.isNull()) {
      //      PostStack2d inputVolume = _inputVolume.get();
      //      float zStart = inputVolume.getZStart();
      //      float zEnd = inputVolume.getZEnd();
      //      if (_startTime.get() < zStart) {
      //        results.error(_startTime, "Start time (or depth) outside of range " + zStart + " to " + zEnd + ".");
      //      }
      //      if (_endTime.get() > zEnd) {
      //        results.error(_endTime, "End time (or depth) outside of range " + zStart + " to " + zEnd + ".");
      //      }
    }

    // Validate the output volume name.
    if (_outputVolumeName.isEmpty()) {
      results.error(_outputVolumeName, "No output volume name specified.");
    } else {
      if (!_inputVolume.isNull()) {
        IStatus status = DataSource.validateName(_inputVolume.get(), _outputVolumeName.get());
        if (!status.isOK()) {
          results.setStatus(_outputVolumeName, status);
        }
      }
    }
  }

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) {

    // Unpack the properties.
    PostStack2d poststack = _inputVolume.get();
    String[] lineNames = _inputLines.get();
    PostStack2dLine[] inputVolumes = new PostStack2dLine[lineNames.length];
    for (int i = 0; i < lineNames.length; i++) {
      inputVolumes[i] = poststack.getPostStack2dLine(lineNames[i]);
    }
    AreaOfInterest aoi = null;
    float scaleFactor = _scaleFactor.get();
    float clipFactor = _clipFactor.get();
    if (!_useClipFactor.get()) {
      clipFactor = 0;
    }
    int windowLength = _windowLength.get();
    String outputVolumeName = _outputVolumeName.get();
    OutputVolumeType outputVolumeType = _outputVolumeType.get();

    try {
      mvxpPostStack2d(poststack, inputVolumes, aoi, windowLength, scaleFactor, clipFactor, outputVolumeName,
          outputVolumeType, monitor, logger, repository);
    } catch (Exception ex) {
      throw new RuntimeException(ex.toString(), ex);
    }
  }

  /**
   * Runs the MVXP algorithm on a PostStack2d volume.
   */
  public void mvxpPostStack2d(final PostStack2d poststack, final PostStack2dLine[] inputVolumes,
      final AreaOfInterest inputAOI, final int windowLength, final float scaleFactor, final float clipFactor,
      final String outputVolumeName, final OutputVolumeType outputVolumeType, IProgressMonitor monitor,
      final ILogger logger, IRepository repository) throws Exception {

    int ivCount = 0;
    int reportedProgress = 0;
    int cumulativeProgress = 0;
    monitor.beginTask("MVXP \'" + poststack.getDisplayName() + "\'", 100 * inputVolumes.length);

    for (PostStack2dLine inputVolume : inputVolumes) {

      // Update the progress monitor.
      monitor.subTask("Processing line " + inputVolume.getLineName() + " ...");

      cumulativeProgress = 100 * ivCount;
      if (cumulativeProgress > reportedProgress) {
        monitor.worked(cumulativeProgress - reportedProgress);
        reportedProgress = cumulativeProgress;
      }

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
      // Create an output volume based on the input volume.
      PostStack2dLine outputVolume = PostStack2dLineFactory.create(repository, inputVolume, outputVolumeName,
          storageFormat, inputVolume.getZDelta());

      // determine the index values for the start and end time
      float startTime = inputVolume.getZStart();
      float endTime = inputVolume.getZEnd();
      float zDelta = inputVolume.getZDelta();
      float zStart = inputVolume.getZStart();
      int iStartTime = (int) Math.rint((startTime - zStart) / zDelta);
      int iEndTime = (int) Math.rint((endTime - zStart) / zDelta);

      monitor.subTask("Reading traces from line " + inputVolume.getLineName() + " ...");

      int nTraces = inputVolume.getNumCdps();

      // Create a trace iterator for the input volume.
      // The iterator with read in the optimal direction for the input volume.
      TraceIterator traceIterator = TraceIteratorFactory.create(inputVolume, inputAOI);
      traceIterator.omitMissingTraces(true);

      while (traceIterator.hasNext()) {
        // Get the next trace collection from the iterator.
        TraceData traceData = traceIterator.next();

        // Get the array of input traces from the trace collection.
        Trace[] tracesIn = traceData.getTraces();

        // Allocate an array of output traces.
        Trace[] tracesOut = new Trace[tracesIn.length];

        cumulativeProgress = 100 * ivCount + (int) (traceIterator.getCompletion() / 3.0f);
        if (cumulativeProgress > reportedProgress) {
          monitor.worked(cumulativeProgress - reportedProgress);
          reportedProgress = cumulativeProgress;
        }

        monitor.subTask("Processing " + tracesIn.length + " traces from line " + inputVolume.getLineName() + " ...");

        // Loop thru the traces in the trace collection obtained from the iterator.
        for (int i = 0; i < tracesIn.length; i++) {
          if (tracesIn[i].isLive()) {
            // If the trace is live, run mvxp over the trace
            float[] tvals = MVXP.mvxpTrace(tracesIn[i], iStartTime, iEndTime, windowLength, scaleFactor, clipFactor);
            tracesOut[i] = new Trace(tracesIn[i], tvals);
          } else {
            // Otherwise, simply pass it along.
            tracesOut[i] = tracesIn[i];
          }
        }

        cumulativeProgress = 100 * ivCount + (int) (2.0f * traceIterator.getCompletion() / 3.0f);
        if (cumulativeProgress > reportedProgress) {
          monitor.worked(cumulativeProgress - reportedProgress);
          reportedProgress = cumulativeProgress;
        }

        monitor.subTask("Writing " + tracesOut.length + " traces to line " + outputVolume.getLineName() + " ...");

        // Create a new trace collection and put it into the output volume.
        outputVolume.putTraces(new TraceData(tracesOut));

        cumulativeProgress = 100 * ivCount + (int) traceIterator.getCompletion();
        if (cumulativeProgress > reportedProgress) {
          monitor.worked(cumulativeProgress - reportedProgress);
          reportedProgress = cumulativeProgress;
        }

        if (monitor.isCanceled()) {
          break;
        }

        monitor.subTask("Reading traces from line " + inputVolume.getLineName() + " ...");
      }

      // Update the progress monitor.
      monitor.subTask("Completed line " + inputVolume.getLineName() + ".");
      ivCount++;

      inputVolume.close();
      outputVolume.close();

      if (monitor.isCanceled()) {
        break;
      }
    }

    monitor.done();
  }
}
