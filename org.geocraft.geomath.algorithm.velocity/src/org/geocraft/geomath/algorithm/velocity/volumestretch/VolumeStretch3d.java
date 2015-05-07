package org.geocraft.geomath.algorithm.velocity.volumestretch;


import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.factory.model.PostStack3dFactory;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Header;
import org.geocraft.core.model.datatypes.HeaderDefinition;
import org.geocraft.core.model.datatypes.HeaderEntry;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.TraceHeaderCatalog;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.internal.geomath.algorithm.velocity.ServiceComponent;
import org.geocraft.internal.geomath.algorithm.velocity.VelocityArrayTimeDepthConverter;
import org.geocraft.internal.geomath.algorithm.velocity.VelocityArrayTimeDepthConverter.Method;
import org.geocraft.internal.geomath.algorithm.velocity.VelocityVolumeSpecification;
import org.geocraft.io.util.TraceIterator;
import org.geocraft.io.util.TraceIteratorFactory;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;


public class VolumeStretch3d extends StandaloneAlgorithm {

  private EntityProperty<PostStack3d> _inputVolume;

  private EntityProperty<PostStack3d> _velocityVolume;

  private EntityProperty<AreaOfInterest> _aoi;

  private BooleanProperty _useAoi;

  private EnumProperty<Method> _conversionMethod;

  private StringProperty _outputVolumeName;

  private FloatProperty _sampleRate;

  private FloatProperty _maximumTimeOrDepth;

  public VolumeStretch3d() {
    _inputVolume = addEntityProperty("Input Volume", PostStack3d.class);
    _velocityVolume = addEntityProperty("Velocity Volume", PostStack3d.class);
    _aoi = addEntityProperty("Area of Interest", AreaOfInterest.class);
    _useAoi = addBooleanProperty("Use AOI?", false);
    _conversionMethod = addEnumProperty("Conversion Method", Method.class, Method.KneeBased);
    _sampleRate = addFloatProperty("Sample Rate", 0);
    _maximumTimeOrDepth = addFloatProperty("Maximum Time or Depth", 0);
    _outputVolumeName = addStringProperty("Output Volume Name", "");
  }

  @Override
  public void buildView(IModelForm modelForm) {

    FormSection inputSection = modelForm.addSection("Input");
    inputSection.addEntityComboField(_inputVolume, PostStack3d.class);
    inputSection.addEntityComboField(_velocityVolume, new VelocityVolumeSpecification());

    inputSection.addEntityComboField(_aoi, AreaOfInterest.class).showActiveFieldToggle(_useAoi);

    FormSection paramSection = modelForm.addSection("Parameters");
    paramSection.addRadioGroupField(_conversionMethod, Method.values());

    FormSection outSection = modelForm.addSection("Output");
    outSection.addTextField(_sampleRate);
    outSection.addTextField(_maximumTimeOrDepth);
    outSection.addTextField(_outputVolumeName);
  }

  @Override
  public void propertyChanged(String key) {
    // No conditional view logic.
    if (key.equals(_inputVolume.getKey()) && !_inputVolume.isNull()) {
      String outputVolumeName = _inputVolume.get().getDisplayName() + "_stretch";
      _outputVolumeName.set(outputVolumeName);
    }
  }

  @Override
  public void validate(IValidation results) {
    if (_inputVolume.get() == null) {
      results.error(_inputVolume, "input required");
    }

    if (_velocityVolume.get() == null) {
      results.error(_velocityVolume, "input required");
    } else {
      // Make sure the velocity units are correct
      Unit velocityUnit = _velocityVolume.get().getDataUnit();
      if (velocityUnit != Unit.METERS_PER_SECOND && velocityUnit != Unit.FEET_PER_SECOND) {
        results.error(_velocityVolume, "Invalid velocity units. Must be " + Unit.METERS_PER_SECOND.getSymbol() + " or "
            + Unit.FEET_PER_SECOND.getSymbol() + ".");
      }
    }

    // Check if an area of interest is to be used, one was specified
    if (_useAoi.get()) {
      if (_aoi.isNull()) {
        results.error(_aoi, "No area-of-interest specified.");
      }
    }

    if (_sampleRate.get() <= 0) {
      results.error(_sampleRate, "The sample rate must be greater than zero.");
    }

    if (_maximumTimeOrDepth.get() <= 0) {
      results.error(_maximumTimeOrDepth, "The maximum time or depth must be greater than zero.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) throws CoreException {
    // Unpack the parameters.
    PostStack3d inputVolume = _inputVolume.get();
    PostStack3d velocityVolume = _velocityVolume.get();

    Method conversionMethod = _conversionMethod.get();
    AreaOfInterest areaOfInterest = _aoi.get();
    if (!_useAoi.get()) {
      areaOfInterest = null;
    }
    float sampleRate = _sampleRate.get();
    float maximumTimeOrDepth = _maximumTimeOrDepth.get();
    String outputVolumeName = _outputVolumeName.get();

    try {

      // Initialize the progress monitor.
      int totalWork = 100;
      monitor.beginTask("Volume Stretch 3D", totalWork);

      convertVolume(inputVolume, velocityVolume, conversionMethod, areaOfInterest, sampleRate, maximumTimeOrDepth,
          outputVolumeName, monitor, repository);

    } catch (Exception ex) {
      throw new CoreException(ValidationStatus.error(ex.getMessage()));
    }

  }

  public PostStack3d convertVolume(final PostStack3d inputVolume, final PostStack3d velocityVolume,
      final Method conversionMethod, final AreaOfInterest areaOfInterest, final float sampleRate,
      final float maximumTimeOrDepth, final String outputVolumeName, final IProgressMonitor monitor,
      final IRepository repository) throws CoreException {

    try {

      // Determine the new volume domain type and conversion mode.
      Unit velocityUnit = velocityVolume.getDataUnit();
      Domain inputDomain = inputVolume.getZDomain();
      Domain outputDomain = null;

      if (inputDomain.equals(Domain.TIME)) {
        outputDomain = Domain.DISTANCE;
      } else if (inputDomain.equals(Domain.DISTANCE)) {
        outputDomain = Domain.TIME;
      } else {
        String msg = "Invalid domain type. Must be time or depth.";
        throw new CoreException(new Status(IStatus.ERROR, ServiceComponent.PLUGIN_ID, msg));
      }

      PostStack3d outputVolume = PostStack3dFactory.create(repository, inputVolume, outputVolumeName, outputDomain, 0,
          maximumTimeOrDepth, sampleRate);
      outputVolume.setZDomain(outputDomain);

      // Create a trace iterator for reading the input volume.
      // The volume will be read in its optimal direction.
      // The velocity volume can have a different sample rate and therefore cannot
      // be read using a MultiVolumeTraceIterator. It will be read after traces
      // from the input volume are read.
      TraceIterator traceIterator = TraceIteratorFactory.create(inputVolume, areaOfInterest);

      int completionOld = 0;
      while (traceIterator.hasNext()) {
        TraceData traceData = traceIterator.next();
        Trace[] inputTraces = traceData.getTraces();

        int numTraces = traceData.getNumTraces();
        Trace[] tracesOut = new Trace[numTraces];
        float[] inlines = new float[numTraces];
        float[] xlines = new float[numTraces];
        for (int i = 0; i < numTraces; i++) {
          inlines[i] = inputTraces[i].getInline();
          xlines[i] = inputTraces[i].getXline();
        }
        Trace[] velocityTraces = velocityVolume.getTraces(inlines, xlines, velocityVolume.getZStart(),
            velocityVolume.getZEnd()).getTraces();

        // Loop thru each of the input traces.
        for (int i = 0; i < numTraces; i++) {

          Trace inputTrace = inputTraces[i];
          Trace velocityTrace = velocityTraces[i];

          // Determine the current inline and xline.
          float inline = inputTrace.getInline();
          float xline = inputTrace.getXline();

          // Convert the input trace.
          tracesOut[i] = convertTrace(inputTrace, velocityTrace, inputVolume.getZDomain(), outputVolume.getZUnit(),
              conversionMethod, sampleRate, 0, maximumTimeOrDepth, velocityVolume.getZDomain(), velocityUnit);

          // Add the 3d trace headers.
          add3DTraceHeader(tracesOut[i], inline, xline);

          // Update the progress monitor.
          if (monitor.isCanceled()) {
            break;
          }
        }
        int completion = Math.round(traceIterator.getCompletion());
        int work = completion - completionOld;
        if (work > 0) {
          monitor.worked(work);
        }
        completionOld = completion;

        // Update the progress monitor message.
        monitor.subTask(traceIterator.getMessage());

        // Write the converted traces to the output volume.
        outputVolume.putTraces(new TraceData(tracesOut));

      }

      // Close the input, velocity and output volumes.
      inputVolume.close();
      velocityVolume.close();
      outputVolume.close();

      // Update the progress monitor.
      monitor.done();

      return outputVolume;
    } catch (Exception e1) {
      throw new CoreException(new Status(IStatus.ERROR, ServiceComponent.PLUGIN_ID, e1.getMessage()));
    }
  }

  public Trace convertTrace(final Trace inputTrace, final Trace velocityTrace, final Domain inputDomain,
      final Unit zUnit, final Method conversionMethod, final float deltaZ, final float startZ, final float endZ,
      final Domain velocityDomain, final Unit velocityUnit) {

    double x = inputTrace.getX();
    double y = inputTrace.getY();

    // If the input or velocity trace is not 'live', do not convert.
    if (!inputTrace.isLive() || !velocityTrace.isLive()) {
      Trace zeroTrace = createZeroTrace(x, y, zUnit, deltaZ, startZ, endZ);
      zeroTrace.setStatus(inputTrace.getStatus());
      return zeroTrace;
    }

    // Create a time/depth converter for the velocity trace.
    VelocityArrayTimeDepthConverter tdconv = new VelocityArrayTimeDepthConverter(velocityTrace.getDataReference(),
        velocityTrace.getZDelta(), velocityDomain, velocityUnit, conversionMethod);

    // Allocate the output trace data array.
    int numSamples = 1 + (int) ((endZ - startZ) / deltaZ);
    float[] dataOut = new float[numSamples];

    // Loop thru each sample in the input trace.
    for (int k = 0; k < numSamples; k++) {

      dataOut[k] = 0;

      // Convert the current point in the Volume
      float zOut = startZ + k * deltaZ;
      float zIn = 0;

      if (inputDomain.equals(Domain.TIME)) {
        zIn = tdconv.getTime(zOut);
      } else {
        zIn = tdconv.getDepth(zOut);
      }

      if (!Float.isNaN(zIn) && !Float.isInfinite(zIn)) {

        float kt = (zIn - inputTrace.getZStart()) / inputTrace.getZDelta();
        int k0 = (int) Math.floor(kt);
        int k1 = (int) Math.ceil(kt);

        if (k1 == k0) { // YYY what is this? 
          k1++;
        }

        float percent = (kt - k0) / (k1 - k0);
        float v = 0;
        float[] dataIn = inputTrace.getDataReference(); // YYY what is this???

        if (k0 >= 0 && k1 < inputTrace.getNumSamples()) {
          float v0 = dataIn[k0];
          float v1 = dataIn[k1];

          v = v0 + percent * (v1 - v0);
        }
        dataOut[k] = v;
      }
    }

    // Create a converted trace for output.
    Trace outputTrace = new Trace(startZ, deltaZ, zUnit, x, y, dataOut);

    // Return the converted trace.
    return outputTrace;
  }

  private Trace createZeroTrace(final double x, final double y, final Unit zUnit, final float deltaZ,
      final float startZ, final float endZ) {

    int numSamples = 1 + (int) ((endZ - startZ) / deltaZ);
    float[] dataOut = new float[numSamples];

    // Create a zero trace for output.
    Trace zeroTrace = new Trace(startZ, deltaZ, zUnit, x, y, dataOut);

    // Return the zero trace.
    return zeroTrace;
  }

  private void add3DTraceHeader(final Trace trace, final float inline, final float xline) {

    // Create a trace header and set the inline,xline coordinates.
    HeaderDefinition headerDefinition = new HeaderDefinition(new HeaderEntry[] { TraceHeaderCatalog.INLINE_NO,
        TraceHeaderCatalog.XLINE_NO, TraceHeaderCatalog.X, TraceHeaderCatalog.Y });
    Header header = new Header(headerDefinition);
    header.putInteger(TraceHeaderCatalog.INLINE_NO, Math.round(inline));
    header.putInteger(TraceHeaderCatalog.XLINE_NO, Math.round(xline));
    header.putDouble(TraceHeaderCatalog.X, trace.getX());
    header.putDouble(TraceHeaderCatalog.Y, trace.getY());
    trace.setHeader(header);
  }

}
