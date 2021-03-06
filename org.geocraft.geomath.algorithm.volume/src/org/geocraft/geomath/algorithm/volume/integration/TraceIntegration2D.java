/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.volume.integration;


import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.factory.model.PostStack2dLineFactory;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.io.util.TraceIterator;
import org.geocraft.io.util.TraceIteratorFactory;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.aoi.AOIComboField;


public class TraceIntegration2D extends TraceIntegration {

  private EntityProperty<PostStack2dLine> _inputVolume;

  private EntityProperty<AreaOfInterest> _areaOfInterest;

  private BooleanProperty _useAreaOfInterest;

  private IntegerProperty _operatorLength;

  private StringProperty _outputVolumeName;

  public TraceIntegration2D() {
    _inputVolume = addEntityProperty("Input Volume", PostStack2dLine.class);
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

    AOIComboField aoiField = inputSection.addAOIComboField(_areaOfInterest, 2);
    aoiField.showActiveFieldToggle(_useAreaOfInterest);

    // Integration Section.
    FormSection integrateSection = form.addSection("Integration");

    integrateSection.addTextField(_operatorLength);

    // Output Section.
    FormSection outputSection = form.addSection("Output");

    outputSection.addTextField(_outputVolumeName);
  }

  @Override
  public void propertyChanged(String key) {
    if (key.equals(_inputVolume.getKey()) && !_inputVolume.isNull()) {
      SeismicDataset inputVolume = _inputVolume.get();
      _outputVolumeName.set(inputVolume.getDisplayName() + "_traceint");
    }
  }

  @Override
  public void validate(IValidation results) {
    if (_inputVolume.isNull()) {
      results.error(_inputVolume, "No input volume specified.");
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
    }

  }

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) {
    // Unpack the properties.
    PostStack2dLine inputVolume = _inputVolume.get();
    AreaOfInterest aoi = _areaOfInterest.get();
    if (!_useAreaOfInterest.get()) {
      aoi = null;
    }
    int operatorLength = _operatorLength.get();
    String outputVolumeName = _outputVolumeName.get();

    // Integrate the input volume, returning an output volume.
    integrateVolume(inputVolume, aoi, operatorLength, outputVolumeName, monitor, logger, repository);
  }

  /**
   * Performs trace integration on a 2D poststack volume.
   * 
   * @param inputVolume the volume on which to perform trace integration.
   * @param aoi the area-of-interest.
   * @param operatorLength operator length if removal of drift is necessary.
   * @return outputVolumeName the name of the output volume.
   */
  protected PostStack2dLine integrateVolume(final PostStack2dLine inputVolume, final AreaOfInterest aoi,
      final int operatorLength, final String outputVolumeName, final IProgressMonitor monitor, final ILogger logger,
      final IRepository repository) {

    try {
      // Create an output volume based on the input volume.
      PostStack2dLine outputVolume = PostStack2dLineFactory.create(repository, inputVolume, outputVolumeName);

      // Initialize the progress monitor.
      int totalTraces = inputVolume.getNumCdps();
      monitor.beginTask("Trace Integration of \'" + inputVolume.getDisplayName() + "\'", totalTraces);

      // Create a trace iterator for the input volume.
      // The iterator with read in the optimal direction for the input volume.
      TraceIterator traceIterator = TraceIteratorFactory.create(inputVolume, aoi);
      while (traceIterator.hasNext()) {
        // Get the next trace collection from the iterator.
        TraceData traceData = traceIterator.next();

        // Get the array of input traces from the trace collection.
        Trace[] tracesIn = traceData.getTraces();

        // Allocate an array of output traces.
        Trace[] tracesOut = new Trace[tracesIn.length];

        // Loop thru the traces in the trace collection obtained from the iterator.
        for (int i = 0; i < tracesIn.length; i++) {
          if (tracesIn[i].isLive()) {
            // If the trace is live, integrate it.
            float[] tvals = integrateTrace(tracesIn[i], operatorLength);
            tracesOut[i] = new Trace(tracesIn[i], tvals);
          } else {
            // Otherwise, simply pass it along.
            tracesOut[i] = tracesIn[i];
          }
          // Update the progress monitor.
          monitor.worked(1);
          if (monitor.isCanceled()) {
            break;
          }
        }

        // Create a new trace collection and put it into the output volume.
        outputVolume.putTraces(new TraceData(tracesOut));

        // Update the progress monitor message.
        monitor.subTask(traceIterator.getMessage());
      }

      // Close the input and output volumes.
      monitor.subTask("Closing volumes...");
      inputVolume.close();
      outputVolume.close();

      return outputVolume;
    } catch (Exception e) {
      logger.error("Error occurred when running trace integration.", e);
      synchronized (this) {
        notifyAll();
      }
      monitor.done();
      return null;
    }
  }

}
