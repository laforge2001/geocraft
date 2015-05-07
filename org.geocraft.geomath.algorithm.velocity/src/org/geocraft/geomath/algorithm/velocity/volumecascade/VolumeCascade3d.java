package org.geocraft.geomath.algorithm.velocity.volumecascade;


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
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityArrayProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.AbstractSpecification;
import org.geocraft.core.repository.specification.ISpecification;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.geomath.algorithm.velocity.volumestretch.VolumeStretch3d;
import org.geocraft.internal.geomath.algorithm.velocity.ServiceComponent;
import org.geocraft.internal.geomath.algorithm.velocity.VelocityVolumeSpecification;
import org.geocraft.internal.geomath.algorithm.velocity.VelocityArrayTimeDepthConverter.Method;
import org.geocraft.io.util.TraceIterator;
import org.geocraft.io.util.TraceIteratorFactory;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.TextField;


public class VolumeCascade3d extends StandaloneAlgorithm {

  private EntityArrayProperty<PostStack3d> _inputVolumes;

  private EntityProperty<PostStack3d> _velocityVolume1;

  private EntityProperty<PostStack3d> _velocityVolume2;

  private EntityProperty<AreaOfInterest> _aoi;

  private BooleanProperty _aoiFlag;

  private EnumProperty<Method> _conversionMethod;

  private BooleanProperty _saveTimeVolume;

  private StringProperty _volumeSuffixD2T;

  private StringProperty _volumeSuffixT2D;

  private FloatProperty _maximumTime;

  private FloatProperty _maximumDepth;

  private FloatProperty _timeSampleRate;

  private FloatProperty _depthSampleRate;

  private Unit _timeUnit = UnitPreferences.getInstance().getTimeUnit();

  private Unit _depthUnit = UnitPreferences.getInstance().getVerticalDistanceUnit();

  public VolumeCascade3d() {
    _inputVolumes = addEntityArrayProperty("Depth Volume(s)", PostStack3d.class);
    _velocityVolume1 = addEntityProperty("1st Velocity Volume", PostStack3d.class);
    _velocityVolume2 = addEntityProperty("2nd Velocity Volume", PostStack3d.class);
    _aoi = addEntityProperty("Area of Interest", AreaOfInterest.class);
    _aoiFlag = addBooleanProperty("Use Area of Interest", false);
    _conversionMethod = addEnumProperty("Conversion Method", Method.class, Method.KneeBased);
    _saveTimeVolume = addBooleanProperty("Save a time volume?", false);
    _volumeSuffixD2T = addStringProperty("Depth-To-Time Volume Suffix", "time");
    _volumeSuffixT2D = addStringProperty("Time-To-Depth Volume Suffix", "depth");
    _maximumTime = addFloatProperty("Maximum Time (" + _timeUnit.getSymbol() + ")", 0);
    _maximumDepth = addFloatProperty("Maximum Depth (" + _depthUnit.getSymbol() + ")", 0);
    _timeSampleRate = addFloatProperty("Time Sample Rate (" + _timeUnit.getSymbol() + ")", 0);
    _depthSampleRate = addFloatProperty("Depth Sample Rate (" + _depthUnit.getSymbol() + ")", 0);
  }

  @Override
  public void buildView(IModelForm form) {

    FormSection section = form.addSection("Input");
    ISpecification depthVolumeFilter = new DepthVolumeSpecification();
    section.addEntityListField(_inputVolumes, depthVolumeFilter);

    ISpecification velocityVolumeFilter = new VelocityVolumeSpecification();
    section.addEntityComboField(_velocityVolume1, velocityVolumeFilter);
    section.addEntityComboField(_velocityVolume2, velocityVolumeFilter);
    section.addEntityComboField(_aoi, AreaOfInterest.class).showActiveFieldToggle(_aoiFlag);

    section = form.addSection("Parameters");
    section.addRadioGroupField(_conversionMethod, Method.values());

    section = form.addSection("Output");
    section.addCheckboxField(_saveTimeVolume);
    TextField textField = section.addTextField(_volumeSuffixD2T);

    // Disable the depth to time volume suffix if we are not saving a time volume
    textField.setEnabled(_saveTimeVolume.get());

    section.addTextField(_volumeSuffixT2D);

    section.addTextField(_timeSampleRate);
    section.addTextField(_depthSampleRate);
    section.addTextField(_maximumTime);
    section.addTextField(_maximumDepth);
  }

  @Override
  public void propertyChanged(String key) {
    if (key.equals(_saveTimeVolume.getKey())) {
      // Disable the depth to time volume suffix if we are not saving a time volume
      setFieldEnabled(_volumeSuffixD2T, _saveTimeVolume.get());
    }

  }

  @Override
  public void validate(IValidation results) {
    // Check that at least 1 input volume is specified.
    if (_inputVolumes.isEmpty()) {
      results.error(_inputVolumes, "No depth volume(s) specified.");
    }

    // Check that the 1st volume is specified and a velocity volume.
    if (_velocityVolume1.isNull()) {
      results.error(_velocityVolume1, "The 1st velocity volume not specified.");
    } else {
      // Make sure the velocity units are correct
      Unit velocityUnit = _velocityVolume1.get().getDataUnit();
      if (velocityUnit != Unit.METERS_PER_SECOND && velocityUnit != Unit.FEET_PER_SECOND) {
        results.error(_velocityVolume1, "Invalid velocity units. Must be " + Unit.METERS_PER_SECOND.getSymbol()
            + " or " + Unit.FEET_PER_SECOND.getSymbol() + ".");
      }
    }

    // Check that the 2nd volume is specified and a velocity volume.
    if (_velocityVolume2.isNull()) {
      results.error(_velocityVolume2, "The 2nd velocity volume not specified.");
    } else {
      // Make sure the velocity units are correct
      Unit velocityUnit = _velocityVolume2.get().getDataUnit();
      if (velocityUnit != Unit.METERS_PER_SECOND && velocityUnit != Unit.FEET_PER_SECOND) {
        results.error(_velocityVolume2, "Invalid velocity units. Must be " + Unit.METERS_PER_SECOND.getSymbol()
            + " or " + Unit.FEET_PER_SECOND.getSymbol() + ".");
      }
    }

    // Check that a suffix has been supplied for the output volumes.
    if (_volumeSuffixD2T.isEmpty()) {
      results.error(_volumeSuffixD2T, "No output time volume suffix specified.");
    }
    if (_volumeSuffixT2D.isEmpty()) {
      results.error(_volumeSuffixT2D, "No output depth volume suffix specified.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) throws CoreException {
    // Unpack the parameters.
    PostStack3d[] inputVolumes = _inputVolumes.get();
    PostStack3d velocityVolume1 = _velocityVolume1.get();
    PostStack3d velocityVolume2 = _velocityVolume2.get();
    AreaOfInterest aoi = _aoi.get();
    if (!_aoiFlag.get()) {
      aoi = null;
    }
    Method conversionMethod = _conversionMethod.get();
    boolean saveTimeVolume = _saveTimeVolume.get();
    String outputVolumeSuffixD2T = _volumeSuffixD2T.get();
    String outputVolumeSuffixT2D = _volumeSuffixT2D.get();

    float timeSampleRate = _timeSampleRate.get();
    int maximumTimeIndex = (int) (_maximumTime.get() / timeSampleRate);
    float maximumTime = timeSampleRate * maximumTimeIndex;
    Unit timeUnit = _timeUnit;

    float depthSampleRate = _depthSampleRate.get();
    int maximumDepthIndex = (int) (_maximumDepth.get() / depthSampleRate);
    float maximumDepth = depthSampleRate * maximumDepthIndex;
    Unit depthUnit = _depthUnit;

    int totalWork = 100 * inputVolumes.length;

    monitor.beginTask("Cascading Volumes (D->T->D)", totalWork);

    try {
      for (PostStack3d inputVolume : inputVolumes) {
        String inputVolumeName = inputVolume.getDisplayName();
        monitor.subTask("Cascading " + inputVolumeName);
        String outputVolumeNameD2T = inputVolumeName + "_" + outputVolumeSuffixD2T;
        String outputVolumeNameT2D = inputVolumeName + "_" + outputVolumeSuffixT2D;

        // Convert the volume from depth to time using the 1st velocity volume.
        // Then Convert the converted volume from time back to depth using the 2nd velocity volume.
        convertVolume(inputVolume, velocityVolume1, velocityVolume2, conversionMethod, aoi, timeSampleRate,
            depthSampleRate, maximumTime, maximumDepth, timeUnit, depthUnit, outputVolumeNameD2T, outputVolumeNameT2D,
            saveTimeVolume, monitor, repository);
      }
    } catch (Exception e1) {
      throw new CoreException(new Status(IStatus.ERROR, ServiceComponent.PLUGIN_ID, e1.getMessage()));

    }
  }

  public PostStack3d convertVolume(final PostStack3d inputVolume, final PostStack3d velocityVolume1,
      final PostStack3d velocityVolume2, final Method conversionMethod, final AreaOfInterest areaOfInterest,
      final float timeSampleRate, final float depthSampleRate, final float maximumTime, final float maximumDepth,
      final Unit timeUnit, final Unit depthUnit, final String outputVolumeNameD2T, final String outputVolumeNameT2D,
      final boolean saveTimeVolume, final IProgressMonitor monitor, final IRepository repository) throws CoreException {

    try {

      // Determine the new volume domain type and conversion mode.
      Unit velocityUnit1 = velocityVolume1.getDataUnit();
      Unit velocityUnit2 = velocityVolume2.getDataUnit();
      Domain inputDomain1 = inputVolume.getZDomain();
      Domain inputDomain2 = null;
      Domain outputDomain1 = null;
      Domain outputDomain2 = null;

      if (inputDomain1.equals(Domain.DISTANCE)) {
        outputDomain1 = Domain.TIME;
        outputDomain2 = Domain.DISTANCE;
        inputDomain2 = outputDomain1;
      } else {
        String msg = "Invalid domain type. Must be depth domain.";
        throw new CoreException(new Status(IStatus.ERROR, ServiceComponent.PLUGIN_ID, msg));
      }

      PostStack3d timeVolume = null;
      if (saveTimeVolume) {
        timeVolume = PostStack3dFactory.create(repository, inputVolume, outputVolumeNameD2T, outputDomain1, 0,
            maximumTime, timeSampleRate);
        timeVolume.setZDomain(outputDomain1);
      }

      PostStack3d depthVolume = PostStack3dFactory.create(repository, inputVolume, outputVolumeNameT2D, outputDomain2,
          0, maximumDepth, depthSampleRate);
      depthVolume.setZDomain(outputDomain2);

      // Set up to use volume stretch routines
      VolumeStretch3d volumeStretch = new VolumeStretch3d();

      // Create a trace iterator for reading both the input and velocity volumes.
      // The volumes will be read in the optimal direction of the input volume.
      TraceIterator traceIterator = TraceIteratorFactory.create(inputVolume, areaOfInterest);

      int completionOld = 0;
      while (traceIterator.hasNext()) {
        TraceData traceData = traceIterator.next();
        Trace[] inputTraces = traceData.getTraces();

        int numTraces = traceData.getNumTraces();
        float[] inlines = new float[numTraces];
        float[] xlines = new float[numTraces];
        for (int i = 0; i < numTraces; i++) {
          inlines[i] = inputTraces[i].getInline();
          xlines[i] = inputTraces[i].getXline();
        }
        Trace[] velocityTraces1 = velocityVolume1.getTraces(inlines, xlines, velocityVolume1.getZStart(),
            velocityVolume1.getZEnd()).getTraces();
        Trace[] velocityTraces2 = velocityVolume2.getTraces(inlines, xlines, velocityVolume2.getZStart(),
            velocityVolume2.getZEnd()).getTraces();

        Trace[] tracesd2t = new Trace[numTraces];
        Trace[] tracest2d = new Trace[numTraces];
        // Loop thru each of the input traces.
        for (int i = 0; i < numTraces; i++) {

          Trace inputTrace = inputTraces[i];
          Trace velocityTrace1 = velocityTraces1[i];
          Trace velocityTrace2 = velocityTraces2[i];

          // Determine the current inline and xline.
          float inline = inputTrace.getInline();
          float xline = inputTrace.getXline();

          // Make the conversion from depth to time
          tracesd2t[i] = volumeStretch.convertTrace(inputTrace, velocityTrace1, inputDomain1, timeUnit,
              conversionMethod, timeSampleRate, 0, maximumTime, velocityVolume1.getZDomain(), velocityUnit1);

          // Add the 3d trace headers.
          add3DTraceHeader(tracesd2t[i], inline, xline);

          // Now Make the conversion from time to depth
          tracest2d[i] = volumeStretch.convertTrace(tracesd2t[i], velocityTrace2, inputDomain2, depthUnit,
              conversionMethod, depthSampleRate, 0, maximumDepth, velocityVolume2.getZDomain(), velocityUnit2);

          // Add the 3d trace headers.
          add3DTraceHeader(tracest2d[i], inline, xline);

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

        // Write to the time volume 
        if (saveTimeVolume) {
          timeVolume.putTraces(new TraceData(tracesd2t));
        }

        // Write the converted traces to the depth volume.
        depthVolume.putTraces(new TraceData(tracest2d));

      }

      // Close the input, velocity and output volumes.
      inputVolume.close();
      velocityVolume1.close();
      velocityVolume2.close();
      depthVolume.close();

      // Close time volume if we are saving it
      if (saveTimeVolume) {
        timeVolume.close();
      }

      // Update the progress monitor.
      monitor.done();

      return depthVolume;

    } catch (Exception e1) {
      throw new CoreException(new Status(IStatus.ERROR, ServiceComponent.PLUGIN_ID, e1.getMessage()));
    }
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

  /**
   * Defines a filter for <code>PostStack3d</code> entities with data in the depth domain.
   */
  class DepthVolumeSpecification extends AbstractSpecification {

    @Override
    public boolean isSatisfiedBy(Object obj) {
      if (obj != null && PostStack3d.class.isAssignableFrom(obj.getClass())) {
        PostStack3d volume = (PostStack3d) obj;
        // Check that the volume is in depth.
        return volume.getZDomain().equals(Domain.DISTANCE);
      }
      return false;
    }
  }
}
