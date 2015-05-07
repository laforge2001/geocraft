package org.geocraft.geomath.algorithm.velocity.velocitysmoothing;


import java.text.NumberFormat;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.factory.model.PostStack3dFactory;
import org.geocraft.core.model.DataSource;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.Header;
import org.geocraft.core.model.datatypes.HeaderDefinition;
import org.geocraft.core.model.datatypes.HeaderEntry;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.TraceHeaderCatalog;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.TextField;


public class VelocitySmoothing extends StandaloneAlgorithm {

  public enum KernelType {
    BOXCAR("Boxcar"),
    GAUSSIAN("Gaussian");

    private final String _displayName;

    KernelType(final String displayName) {
      _displayName = displayName;
    }

    @Override
    public String toString() {
      return _displayName;
    }
  }

  public enum SmoothingDomain {
    VELOCITY("Velocity"),
    SLOWNESS("Slowness (Do NOT use on Seismic)");

    private final String _displayName;

    SmoothingDomain(final String displayName) {
      _displayName = displayName;
    }

    @Override
    public String toString() {
      return _displayName;
    }
  }

  /** Input volume of background velocities. */
  private EntityProperty<PostStack3d> _inputVolume;

  private EntityProperty<AreaOfInterest> _areaOfInterest;

  private BooleanProperty _useAreaOfInterest;

  // width of kernel in each direction
  private IntegerProperty _widthX;// = 42;

  private IntegerProperty _widthY;// = 42;

  private IntegerProperty _widthZ;// = 42;

  // width of kernel in each direction displayed with real units
  private StringProperty _widthUnitsX;// = "-";

  private StringProperty _widthUnitsY;// = "-";

  private StringProperty _widthUnitsZ;// = "-";

  private EnumProperty<KernelType> _kernelType;// = KernelType.BOXCAR;

  private EnumProperty<SmoothingDomain> _smoothingDomain;// = SmoothingDomain.VELOCITY;

  private IntegerProperty _boxcarAmplitude;// = 5;

  private IntegerProperty _boxcarWidth;// = 25;

  private FloatProperty _gaussianWidth;// = 5;

  private StringProperty _outputVolumeName;// = "vs1";

  private float _kx, _ky; // kernel size in real units

  public VelocitySmoothing() {
    _inputVolume = addEntityProperty("Input Volume", PostStack3d.class);
    _areaOfInterest = addEntityProperty("Area of Interest", AreaOfInterest.class);
    _useAreaOfInterest = addBooleanProperty("Use Area of Interest", false);
    _widthX = addIntegerProperty("Kernel Index Size (Inlines)", 42);
    _widthY = addIntegerProperty("Kernel Index Size (Xlines)", 42);
    _widthZ = addIntegerProperty("Kernel Index Size (Samples)", 42);
    _widthUnitsX = addStringProperty("Kernel World Size (Inlines)", "-");
    _widthUnitsY = addStringProperty("Kernel World Size (Xlines)", "-");
    _widthUnitsZ = addStringProperty("Kernel World Size (Samples)", "-");
    _kernelType = addEnumProperty("Smoothing Kernel Type", KernelType.class, KernelType.BOXCAR);
    _smoothingDomain = addEnumProperty("Smoothing Domain", SmoothingDomain.class, SmoothingDomain.VELOCITY);
    _boxcarAmplitude = addIntegerProperty("Boxcar Amplitude", 5);
    _boxcarWidth = addIntegerProperty("Boxcar Width (Percentage)", 25);
    _gaussianWidth = addFloatProperty("Gaussian Width", 5);
    _outputVolumeName = addStringProperty("Output Volume Name", "");
  }

  @Override
  public void buildView(IModelForm form) {
    FormSection section = form.addSection("Input");

    ComboField volumeField = section.addEntityComboField(_inputVolume, PostStack3d.class);
    volumeField.setTooltip("The volume to be smoothed.");

    ComboField aoiField = section.addEntityComboField(_areaOfInterest, AreaOfInterest.class);
    aoiField.setTooltip("Restrict computation to within an area of interest");
    aoiField.showActiveFieldToggle(_useAreaOfInterest);

    section = form.addSection("Kernel Size (Indexed Values)");

    TextField widthXField = section.addTextField(_widthX);
    widthXField.setLabel("Kernel Size X (# inlines)");
    widthXField.setTooltip("The width of X based on the number of indexed values");

    TextField widthYField = section.addTextField(_widthY);
    widthYField.setLabel("Kernel Size Y (# xlines)");
    widthYField.setTooltip("The width of Y based on the number of indexed values");

    TextField widthZField = section.addTextField(_widthZ);
    widthZField.setLabel("Kernel Size Z (# samples)");
    widthZField.setTooltip("The width of Z based on the number of indexed values");

    section = form.addSection("Kernel Size (Real-World Units)");

    section.addLabelField(_widthUnitsX).setLabel("Kernel Size X");

    section.addLabelField(_widthUnitsY).setLabel("Kernel Size Y");

    section.addLabelField(_widthUnitsZ).setLabel("Kernel Size Z");

    section = form.addSection("Parameters");

    section.addRadioGroupField(_kernelType, KernelType.values());

    section.addRadioGroupField(_smoothingDomain, SmoothingDomain.values());

    section.addTextField(_boxcarAmplitude);

    section.addTextField(_boxcarWidth);

    section.addTextField(_gaussianWidth).setTooltip("The full width at half maximum.");

    section = form.addSection("Output");

    section.addTextField(_outputVolumeName);
  }

  @Override
  public void propertyChanged(String key) {
    if (key.equals(_inputVolume.getKey()) && !_inputVolume.isNull()) {
      PostStack3d inputVolume = _inputVolume.get();
      _outputVolumeName.set(inputVolume.getDisplayName() + "_velsmooth");
      updateDimensions();
    } else if (key.equals(_widthX.getKey()) || key.equals(_widthY.getKey()) || key.equals(_widthZ.getKey())) {
      if (!_inputVolume.isNull()) {
        updateDimensions();
      }
    } else if (key.equals(_kernelType.getKey()) && !_kernelType.isNull()) {
      setFieldVisible(_boxcarAmplitude, _kernelType.get().equals(KernelType.BOXCAR), true);
      setFieldVisible(_boxcarWidth, _kernelType.get().equals(KernelType.BOXCAR), true);
      setFieldVisible(_gaussianWidth, _kernelType.get().equals(KernelType.GAUSSIAN), true);
    }
  }

  private void updateDimensions() {
    PostStack3d inputVolume = _inputVolume.get();
    SeismicSurvey3d survey = inputVolume.getSurvey();
    double rowHeight = survey.getRowSpacing();
    double colWidth = survey.getColumnSpacing();
    int kix = _widthX.get();
    int kiy = _widthY.get();
    int kiz = _widthZ.get();
    double kixWidth = Math.abs(kix * inputVolume.getInlineDelta() * colWidth);
    double kiyWidth = Math.abs(kiy * inputVolume.getXlineDelta() * rowHeight);
    double kizWidth = Math.abs(kiz * inputVolume.getZDelta());
    Unit xyUnit = UnitPreferences.getInstance().getHorizontalDistanceUnit();
    Unit zUnit = UnitPreferences.getInstance().getTimeUnit();
    if (inputVolume.getZDomain().equals(Domain.DISTANCE)) {
      zUnit = UnitPreferences.getInstance().getVerticalDistanceUnit();
    }
    NumberFormat formatter = NumberFormat.getNumberInstance();
    formatter.setMaximumFractionDigits(2);
    formatter.setMaximumIntegerDigits(10);
    formatter.setGroupingUsed(false);
    _widthUnitsX.set(formatter.format(kixWidth) + " " + xyUnit.getSymbol());
    _widthUnitsY.set(formatter.format(kiyWidth) + " " + xyUnit.getSymbol());
    _widthUnitsZ.set(formatter.format(kizWidth) + " " + zUnit.getSymbol());
  }

  @Override
  public void validate(IValidation results) {
    // Validate the input volume.
    if (_inputVolume.isNull()) {
      results.error(_inputVolume, "No input volume specified.");
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
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) throws CoreException {
    // Unpack the parameters.
    PostStack3d inputVolume = _inputVolume.get();
    AreaOfInterest areaOfInterest = _areaOfInterest.get();
    if (!_useAreaOfInterest.get()) {
      areaOfInterest = null;
    }
    KernelType kernelType = _kernelType.get();
    int kernelSizeInlines = _widthX.get();
    int kernelSizeXlines = _widthY.get();
    int kernelSizeSlices = _widthZ.get();
    float boxcarWidth = _boxcarWidth.get();
    float boxcarAmp = _boxcarAmplitude.get();
    float gaussianWidth = _gaussianWidth.get();
    SmoothingDomain smoothingDomain = _smoothingDomain.get();
    String outputVolumeName = _outputVolumeName.get();

    try {
      // Smoothing the input velocity volume, creating an output volume.
      smoothVelocityVolume(monitor, repository, inputVolume, areaOfInterest, kernelType, smoothingDomain,
          kernelSizeInlines, kernelSizeXlines, kernelSizeSlices, boxcarWidth, boxcarAmp, gaussianWidth,
          outputVolumeName);
    } catch (Exception ex) {
      throw new CoreException(ValidationStatus.error(ex.getMessage()));
    }
  }

  public void smoothVelocityVolume(final IProgressMonitor monitor, final IRepository repository,
      final PostStack3d inputVolume, final AreaOfInterest areaOfInterest, final KernelType kernelType,
      final SmoothingDomain smoothingDomain, final int kernelSizeInlines, final int kernelSizeXlines,
      final int kernelSizeSlices, final float boxcarWidth, final float boxcarAmp, final float gaussianWidth,
      final String outputVolumeName) throws Exception {

    // Create the output volume.
    PostStack3d outputVolume = PostStack3dFactory.create(repository, inputVolume, outputVolumeName);
    float inlineStart = inputVolume.getInlineStart();
    float inlineDelta = inputVolume.getInlineDelta();
    float inlineEnd = inputVolume.getInlineEnd();
    float xlineStart = inputVolume.getXlineStart();
    float xlineEnd = inputVolume.getXlineEnd();
    float xlineDelta = inputVolume.getXlineDelta();
    FloatRange zRange = new FloatRange(inputVolume.getZStart(), inputVolume.getZEnd(), inputVolume.getZDelta());
    Unit zUnit = inputVolume.getZUnit();
    _kx = kernelSizeInlines * inlineDelta;
    _ky = kernelSizeXlines * xlineDelta;
    //    _kz = kernelSizeSlices * _deltaZ;

    int numInlines = inputVolume.getNumInlines();
    int numXlines = inputVolume.getNumXlines();
    int numSlices = inputVolume.getNumSamplesPerTrace();

    int xMin, xMax, yMin, yMax, zMin, zMax;

    int progTotal = numInlines / kernelSizeInlines * (numXlines / kernelSizeXlines);
    monitor.beginTask("Velocity Smoothing of \'" + inputVolume.getDisplayName() + "\'", progTotal);
    int progIndex = 0;

    // This is a temporary hack so that the storage order is computed before it
    // gets into the getSlice() method, so it doesn't block itself.
    inputVolume.getPreferredOrder();

    // Loop through volume to pull sub volumes, smooth, and write the sub volumes.
    for (int il = 0; il < numInlines; il += kernelSizeInlines + 1) {
      float inline = inlineStart + il * inlineDelta;
      monitor.subTask("Processing inline #" + inline);

      for (int xl = 0; xl < numXlines; xl += kernelSizeXlines + 1) {
        float xline = xlineStart + xl * xlineDelta;

        FloatRange inlineRange = new FloatRange(inline - _kx, inline + 2 * _kx, inputVolume.getInlineDelta());
        FloatRange xlineRange = new FloatRange(xline - _ky, xline + 2 * _ky, inputVolume.getXlineDelta());
        float[][][] slices = getVolumeArray(inputVolume, smoothingDomain, inlineRange, xlineRange);
        float[][][] smoothSlices = new float[slices.length][slices[0].length][slices[0][0].length];

        float[] xkernel, ykernel, zkernel;

        if (kernelType == KernelType.GAUSSIAN) {
          xkernel = getGaussianKernel(kernelSizeInlines, inlineDelta, gaussianWidth);
          ykernel = getGaussianKernel(kernelSizeXlines, xlineDelta, gaussianWidth);
          zkernel = getGaussianKernel(kernelSizeSlices, inputVolume.getZDelta(), gaussianWidth);
        } else {
          xkernel = getBoxcarKernel(kernelSizeInlines, boxcarWidth, boxcarAmp);
          ykernel = getBoxcarKernel(kernelSizeXlines, boxcarWidth, boxcarAmp);
          zkernel = getBoxcarKernel(kernelSizeSlices, boxcarWidth, boxcarAmp);
        }

        xMin = 0;
        xMax = slices[0].length;
        yMin = 0;
        yMax = slices[0][0].length;
        zMin = 0;
        zMax = numSlices;

        // Smooth x direction.
        for (int iz = zMin; iz < zMax; ++iz) {
          for (int iy = yMin; iy < yMax; ++iy) {
            for (int ix = xMin; ix < xMax; ++ix) {
              if (areaOfInterest == null || areaOfInterest.contains((ix + 1) * inlineDelta, (iy + 1) * xlineDelta)) {
                float sum = 0;
                for (int c = 0; c < xkernel.length; ++c) {
                  int index = ix - xkernel.length / 2 + c;

                  if (index < 0) {
                    sum += xkernel[c] * slices[iz][0][iy];
                  } else if (index < xMax) {
                    sum += xkernel[c] * slices[iz][index][iy];
                  } else {
                    sum += xkernel[c] * slices[iz][xMax - 1][iy];
                  }
                }
                smoothSlices[iz][ix][iy] = sum;
              } else {
                smoothSlices[iz][ix][iy] = slices[iz][ix][iy];
              }
            }
          }
        }

        // Smooth y direction.
        for (int iz = zMin; iz < zMax; ++iz) {
          float[][] intSlice = slices[iz];
          for (int ix = xMin; ix < xMax; ++ix) {
            for (int iy = yMin; iy < yMax; ++iy) {
              if (areaOfInterest == null || areaOfInterest.contains((ix + 1) * inlineDelta, (iy + 1) * xlineDelta)) {
                float sum = 0;
                for (int c = 0; c < ykernel.length; ++c) {
                  int index = iy - ykernel.length / 2 + c;
                  if (index < 0) {
                    sum += ykernel[c] * intSlice[ix][0];
                  } else if (index < yMax) {
                    sum += ykernel[c] * intSlice[ix][index];
                  } else {
                    sum += ykernel[c] * intSlice[ix][yMax - 1];
                  }
                }
                smoothSlices[iz][ix][iy] = sum;
              } else {
                smoothSlices[iz][ix][iy] = slices[iz][ix][iy];
              }
            }
          }
        }

        // Smooth z direction.
        float[] trace = new float[numSlices];
        for (int ix = xMin; ix < xMax; ++ix) {
          for (int iy = yMin; iy < yMax; ++iy) {
            for (int iz = zMin; iz < zMax; ++iz) { // make trace array so we don't "resmooth"
              trace[iz] = smoothSlices[iz][ix][iy];
            }
            for (int iz = 0; iz < numSlices; ++iz) {
              if (areaOfInterest == null || areaOfInterest.contains((ix + 1) * inlineDelta, (iy + 1) * xlineDelta)) {
                float sum = 0;
                for (int c = 0; c < zkernel.length; ++c) {
                  int index = iz - zkernel.length / 2 + c;
                  if (index < 0) {
                    sum += zkernel[c] * trace[0];
                  } else if (index < numSlices) {
                    sum += zkernel[c] * trace[index];
                  } else {
                    sum += zkernel[c] * trace[numSlices - 1];
                  }
                }
                smoothSlices[iz][ix][iy] = sum;
              } else {
                smoothSlices[iz][ix][iy] = slices[iz][ix][iy];
              }
            }
          }
        }

        // Edge detection, only write partial sub-volume if we've run off the ends.
        SeismicSurvey3d survey = inputVolume.getSurvey();

        // Determine if we are at the end of an inline.
        boolean inlineEndFlag = false;
        if (inlineDelta > 0) {
          if (inline + _kx >= inlineEnd) {
            inlineEndFlag = true;
          }
        } else {
          if (inline + _kx <= inlineEnd) {
            inlineEndFlag = true;
          }
        }

        // Determine if we are at the end of an xline.
        boolean xlineEndFlag = false;
        if (xlineDelta > 0) {
          if (xline + _ky >= xlineEnd) {
            xlineEndFlag = true;
          }
        } else {
          if (xline + _ky <= xlineEnd) {
            xlineEndFlag = true;
          }
        }

        if (inlineEndFlag && xlineEndFlag) {
          inlineRange = new FloatRange(inline, inlineEnd, inlineDelta);
          xlineRange = new FloatRange(xline, xlineEnd, xlineDelta);
          writeSlices(outputVolume, survey, smoothingDomain, kernelSizeInlines, kernelSizeXlines, zUnit, smoothSlices,
              inlineRange, xlineRange, zRange);
        } else if (inlineEndFlag) {
          inlineRange = new FloatRange(inline, inlineEnd, inlineDelta);
          xlineRange = new FloatRange(xline, xline + _ky, xlineDelta);
          writeSlices(outputVolume, survey, smoothingDomain, kernelSizeInlines, kernelSizeXlines, zUnit, smoothSlices,
              inlineRange, xlineRange, zRange);
        } else if (xlineEndFlag) {
          inlineRange = new FloatRange(inline, inline + _kx, inlineDelta);
          xlineRange = new FloatRange(xline, xlineEnd, xlineDelta);
          writeSlices(outputVolume, survey, smoothingDomain, kernelSizeInlines, kernelSizeXlines, zUnit, smoothSlices,
              inlineRange, xlineRange, zRange);
        } else {
          inlineRange = new FloatRange(inline, inline + _kx, inlineDelta);
          xlineRange = new FloatRange(xline, xline + _ky, xlineDelta);
          writeSlices(outputVolume, survey, smoothingDomain, kernelSizeInlines, kernelSizeXlines, zUnit, smoothSlices,
              inlineRange, xlineRange, zRange);
        }
        System.gc();
        monitor.worked(1);
        if (monitor.isCanceled()) {
          break;
        }
        progIndex++;
      }
      if (monitor.isCanceled()) {
        break;
      }
    }
    monitor.done();

    // Close the input and output volumes.
    inputVolume.close();
    outputVolume.close();
  }

  // gets volume into 3D array 
  private float[][][] getVolumeArray(final PostStack3d inputVolume, final SmoothingDomain smoothingDomain,
      final FloatRange inlineRange, final FloatRange xlineRange) {

    int numInlines = inlineRange.getNumSteps();
    int numXlines = xlineRange.getNumSteps();
    int numSlices = inputVolume.getNumSamplesPerTrace();
    int numTraces = numXlines * numInlines;

    float[] inlines = new float[numTraces];
    float[] xlines = new float[numTraces];

    float data[][][] = new float[numSlices][numInlines][numXlines];
    float data1D[] = new float[numTraces];
    int index = 0;
    for (int i = 0; i < numInlines; ++i) {
      float inline = inlineRange.getStart() + i * inlineRange.getDelta();
      // Determine if we are at the beginning or at end of an inline.
      boolean inlineFlag1 = false;
      boolean inlineFlag2 = false;
      if (inlineRange.getDelta() > 0) {
        if (inline >= inputVolume.getInlineStart()) {
          inlineFlag1 = true;
        }
        if (inline <= inputVolume.getInlineEnd()) {
          inlineFlag2 = true;
        }
      } else {
        if (inline <= inputVolume.getInlineStart()) {
          inlineFlag1 = true;
        }
        if (inline >= inputVolume.getInlineEnd()) {
          inlineFlag2 = true;
        }
      }
      for (int j = 0; j < numXlines; ++j) {
        float xline = xlineRange.getStart() + j * xlineRange.getDelta();
        // Determine if we are at the beginning or at end of an xline.
        boolean xlineFlag1 = false;
        boolean xlineFlag2 = false;
        if (xlineRange.getDelta() > 0) {
          if (xline >= inputVolume.getXlineStart()) {
            xlineFlag1 = true;
          }
          if (xline <= inputVolume.getXlineEnd()) {
            xlineFlag2 = true;
          }
        } else {
          if (xline <= inputVolume.getXlineStart()) {
            xlineFlag1 = true;
          }
          if (xline >= inputVolume.getXlineEnd()) {
            xlineFlag2 = true;
          }
        }
        // Check to see if we've run off the end.
        // If not, use the next value.
        // If so, use the last value.
        if (inlineFlag1 && xlineFlag1) {
          if (inlineFlag2 && xlineFlag2) {
            xlines[index] = xline;
            inlines[index] = inline;
          } else {
            xlines[index] = inputVolume.getXlineEnd();
            inlines[index] = inputVolume.getInlineEnd();
          }
        } else {
          xlines[index] = inputVolume.getXlineStart();
          inlines[index] = inputVolume.getInlineStart();
        }
        index++;
      }
    }

    TraceData traceDataIn = inputVolume.getTraces(inlines, xlines, inputVolume.getZStart(), inputVolume.getZEnd());
    data1D = traceDataIn.getData();
    index = 0;
    for (int x = 0; x < numInlines; ++x) {
      for (int y = 0; y < numXlines; ++y) {
        for (int z = 0; z < numSlices; ++z) {
          if (smoothingDomain == SmoothingDomain.SLOWNESS) {
            data[z][x][y] = 1 / data1D[index];
          } else {
            data[z][x][y] = data1D[index];
          }
          ++index;
        }
      }
    }

    return data;
  }

  private void writeSlices(final PostStack3d outputVolume, final SeismicSurvey3d survey,
      final SmoothingDomain smoothingDomain, final int kernelSizeInlines, final int kernelSizeXlines, final Unit zUnit,
      final float[][][] slices, final FloatRange inlineRange, final FloatRange xlineRange, final FloatRange zRange) {

    int numInlines = inlineRange.getNumSteps();
    int numXlines = xlineRange.getNumSteps();
    int numSlices = slices.length;
    int numTraces = numXlines * numInlines;

    // Allocate the output traces.
    Trace[] tracesOut = new Trace[numTraces];

    // Define a minimal trace header definition.
    HeaderDefinition headerDef = new HeaderDefinition(new HeaderEntry[] { TraceHeaderCatalog.INLINE_NO,
        TraceHeaderCatalog.XLINE_NO, TraceHeaderCatalog.X, TraceHeaderCatalog.Y });

    int traceIndex = 0;
    for (int i = 0; i < numInlines; ++i) {
      float inline = inlineRange.getStart() + i * inlineRange.getDelta();
      for (int j = 0; j < numXlines; ++j) {
        float xline = xlineRange.getStart() + j * xlineRange.getDelta();
        float[] traceData = new float[numSlices];
        for (int k = 0; k < numSlices; ++k) {
          float value = 0;
          // Compute in the appropriate domain.
          if (smoothingDomain == SmoothingDomain.SLOWNESS) {
            value = 1 / slices[k][i + kernelSizeInlines - 1][j + kernelSizeXlines - 1];
          } else {
            value = slices[k][i + kernelSizeInlines - 1][j + kernelSizeXlines - 1];
          }
          traceData[k] = value;
        }
        double[] xy = survey.transformInlineXlineToXY(inline, xline);
        Trace.Status status = Trace.Status.Live;
        if (Trace.isDead(traceData)) {
          status = Trace.Status.Dead;
        }
        // Create each output trace.
        tracesOut[traceIndex] = new Trace(zRange.getStart(), zRange.getDelta(), zUnit, xy[0], xy[1], traceData, status);

        // Add the x,y,inline and xline values to the header.
        Header header = new Header(headerDef);
        header.putInteger(TraceHeaderCatalog.INLINE_NO, Math.round(inline));
        header.putInteger(TraceHeaderCatalog.XLINE_NO, Math.round(xline));
        header.putDouble(TraceHeaderCatalog.X, xy[0]);
        header.putDouble(TraceHeaderCatalog.Y, xy[1]);
        tracesOut[traceIndex].setHeader(header);

        traceIndex++;
      }
    }

    // Write the output traces to the output volume.
    outputVolume.putTraces(new TraceData(tracesOut));
  }

  /**
   * Returns a boxcar kernel.
   */
  private float[] getBoxcarKernel(final int length, final float boxcarWidth, final float boxcarAmp) {
    float[] kernel = new float[length];
    float sum = 0;
    for (int i = 0; i < length; ++i) {
      if (i < length * (50.0 - boxcarWidth / 2.0) / 100.0) {
        kernel[i] = 1;
      } else if (i >= length * (50.0 - boxcarWidth / 2.0) / 100.0 && i < length * (50.0 + boxcarWidth / 2.0) / 100.0) {
        kernel[i] = boxcarAmp;
      } else {
        kernel[i] = 1;
      }
      sum += Math.abs(kernel[i]);
    }

    // Normalize the kernel.
    for (int i = 0; i < length; ++i) {
      kernel[i] = kernel[i] / sum;
    }
    return kernel;
  }

  /**
   * Returns a Gaussian kernel.
   */
  private float[] getGaussianKernel(final int length, final float delta, final float gaussianWidth) {
    // Gaussian Width is full width at half maximum for Gaussian function

    float c = (float) (gaussianWidth / (2 * Math.sqrt(2 * Math.log(2))));
    float a = 1.0f / (c * (float) Math.sqrt(2.0f * (float) Math.PI));
    float[] kernel = new float[length];
    float sum = 0;
    for (int i = 0; i < length; ++i) {
      float x = -_kx / 2.0f + i * delta;
      kernel[i] = (float) (a * Math.exp(-(x * x) / (2 * c * c)));
      sum += Math.abs(kernel[i]);
    }

    // Normalize the kernel.
    for (int i = 0; i < length; ++i) {
      kernel[i] = kernel[i] / sum;
    }

    return kernel;
  }
}
