package org.geocraft.geomath.algorithm.velocity.horizonstretch;


import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.factory.model.Grid3dFactory;
import org.geocraft.core.model.GeologicInterpretation;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityArrayProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.model.specification.SeismicDatasetUnitsSpecification;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.AbstractSpecification;
import org.geocraft.core.repository.specification.ISpecification;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.geomath.algorithm.util.HorizonUtil;
import org.geocraft.internal.geomath.algorithm.velocity.VelocityArrayTimeDepthConverter;
import org.geocraft.internal.geomath.algorithm.velocity.VelocityArrayTimeDepthConverter.Method;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.EntityComboField;
import org.geocraft.ui.form2.field.EntityListField;


public class HorizonStretchAlgorithm extends StandaloneAlgorithm {

  public enum HorizonType {
    TWO_DIM("2D"),
    THREE_DIM("3D");

    private final String _displayName;

    HorizonType(final String displayName) {
      _displayName = displayName;
    }

    @Override
    public String toString() {
      return _displayName;
    }
  }

  private EntityArrayProperty<GeologicInterpretation> _inputHorizons;

  private EntityProperty<SeismicDataset> _velocityVolume;

  private EntityProperty<AreaOfInterest> _aoi;

  private BooleanProperty _useAoi;

  /** Selection to use a constant value or a variable horizon */
  private EnumProperty<Method> _conversionMethod;

  private StringProperty _outputHorizonSuffix;

  private StringProperty _outputComments;

  EnumProperty<HorizonType> _horizonType;

  private EntityListField _horizonListField;

  private EntityComboField _velVolumeField;

  public static final String HORIZON_TYPE = "Horizon Type";

  public final static String INPUT_HORIZONS = "Input Horizons";

  public final static String VELOCITY_VOLUME = "Velocity Volume";

  public final static String UNITS = "units";

  public final static String AOI = "Area of Interest";

  public final static String CONVERSION_METHOD = "Conversion Method";

  public final static String OUTPUT_HORIZON_SUFFIX = "Output Horizon Suffix";

  public final static String OUTPUT_HORIZON_COMMENTS = "Output Comments";

  // FILTERS
  private final AbstractSpecification _poststack3dSpec = new TypeSpecification(PostStack3d.class);

  private final AbstractSpecification _poststack2dSpec = new TypeSpecification(PostStack2dLine.class);

  private final AbstractSpecification _feetPerSecSpec = new SeismicDatasetUnitsSpecification(
      Unit.FEET_PER_SECOND.toString());

  private final AbstractSpecification _metersPerSecSpec = new SeismicDatasetUnitsSpecification(
      Unit.METERS_PER_SECOND.toString());

  private final AbstractSpecification _unitsSpec = (AbstractSpecification) _feetPerSecSpec.or(_metersPerSecSpec);

  private final AbstractSpecification _postStack3dFilter = (AbstractSpecification) _poststack3dSpec.and(_unitsSpec);

  private final AbstractSpecification _postStack2dFilter = (AbstractSpecification) _poststack2dSpec.and(_unitsSpec);

  // FILTER SETTERS
  private void setVolumeFilter(ISpecification filter) {
    setFieldFilter(_velocityVolume, filter);
  }

  private enum CMode {
    T2D,
    D2T
  }

  /** The progress object. */
  private IProgressMonitor _progress = new NullProgressMonitor();

  public HorizonStretchAlgorithm() {
    _inputHorizons = addEntityArrayProperty(INPUT_HORIZONS, GeologicInterpretation.class);
    _velocityVolume = addEntityProperty(VELOCITY_VOLUME, SeismicDataset.class);
    _useAoi = addBooleanProperty("Use AOI?", false);
    _aoi = addEntityProperty(AOI, AreaOfInterest.class);
    _conversionMethod = addEnumProperty(CONVERSION_METHOD, Method.class, Method.KneeBased);
    _outputHorizonSuffix = addStringProperty(OUTPUT_HORIZON_SUFFIX, "hs1");
    _outputComments = addStringProperty(OUTPUT_HORIZON_COMMENTS, "");
    _horizonType = addEnumProperty(HORIZON_TYPE, HorizonType.class, HorizonType.THREE_DIM);
  }

  @Override
  public void buildView(IModelForm modelForm) {
    FormSection inputSec = modelForm.addSection("Input");
    //inputSec.addRadioGroupField(HORIZON_TYPE, HorizonType.values());
    _horizonListField = inputSec.addEntityListField(INPUT_HORIZONS, Grid3d.class);

    //default entity filter
    setVolumeFilter(_postStack3dFilter);

    inputSec.addEntityComboField(AOI, AreaOfInterest.class).showActiveFieldToggle(_useAoi);
    _velVolumeField = inputSec.addEntityComboField(VELOCITY_VOLUME, PostStack3d.class);

    FormSection paramSec = modelForm.addSection("Parameters");
    paramSec.addRadioGroupField(CONVERSION_METHOD, Method.values());

    FormSection outputSec = modelForm.addSection("Output");
    outputSec.addTextField(OUTPUT_HORIZON_SUFFIX);
    outputSec.addTextBox(OUTPUT_HORIZON_COMMENTS);
  }

  //  private Grid2d createNewLineAttribute(final String displayName, final float[] values, final float nullValue,
  //      final CoordinateSeries points) {
  //    Grid2d resultLineAttribute = new Grid2d(displayName, new Grid2dInMemoryMapper(), null);
  //    resultLineAttribute.putValues(values, nullValue);
  //
  //    LineGeometry lineGeometry = new LineGeometry(displayName, points);
  //    Grid2dCollection lineCollection = new Grid2dCollection(displayName);
  //
  //    resultLineAttribute.setGeometry(lineGeometry);
  //    resultLineAttribute.setCollection(lineCollection);
  //
  //    System.err.println("ID:" + resultLineAttribute.getUniqueID());
  //
  //    return resultLineAttribute;
  //  }

  /**
  *
  */
  //  public Grid2d runHorizonStretch(final Grid2d gridPropIn, final PostStack2d velVol, final Method conversionMethod,
  //      final AreaOfInterest areaOfInterest, final String propName, final ILogger logger, final IRepository repository) {
  //
  //    ApplicationPreferences appPrefs = ApplicationPreferences.getApplicationPreferences();
  //
  //    // Make sure the velocity units are correct
  //    Unit velUnits = null;
  //
  //    if (velVol != null) {
  //      velUnits = velVol.getDataUnit();
  //    }
  //    if (velUnits != Unit.METERS_PER_SECOND && velUnits != Unit.FEET_PER_SECOND) {
  //      logger.error("Invalid velocity units. Must be METERS_PER_SECOND or FEET_PER_SECOND.");
  //      return null;
  //    }
  //
  //    // determine the horizon
  //    LineGeometry gridIn = gridPropIn.getGeometry();
  //    Domain domainTypeIn = getEnum(gridPropIn.getDataUnit().getDomain());
  //    CMode conversionMode = null;
  //
  //    if (domainTypeIn == null) {
  //      return null;
  //    }
  //
  //    if (domainTypeIn.equals(Domain.TIME)) {
  //      conversionMode = CMode.T2D;
  //    } else if (domainTypeIn.equals(Domain.LENGTH)) {
  //      conversionMode = CMode.D2T;
  //    } else {
  //      logger.error("Invalid domain type. Must be time or depth.");
  //      return null;
  //    }
  //
  //    boolean addObjects = true;
  //    if (repository == null) {
  //      addObjects = false;
  //    }
  //
  //    Grid2d gridPropOut = null;
  //
  //    try {
  //
  //      float[] result = getConvertedHorizon(gridPropIn, velVol, velUnits, conversionMethod, conversionMode,
  //          areaOfInterest, logger);
  //
  //      gridPropOut = createNewLineAttribute(propName, result, gridPropIn.getNullValue(), gridIn.getPoints());
  //      // create a new grid2d object and new property
  //      switch (conversionMode) {
  //
  //        case T2D:
  //          gridPropOut.setDataUnit(appPrefs.getVerticalDistanceUnit());
  //          break;
  //
  //        case D2T:
  //          gridPropOut.setDataUnit(appPrefs.getTimeUnit());
  //          break;
  //
  //        default:
  //          logger.error("Invalid conversion mode - Convert from (Depth to Time) or (Time to Depth)");
  //          return null;
  //      }
  //
  //      // add the horizon
  //      if (addObjects) {
  //        repository.add(gridPropOut);
  //      }
  //
  //    } catch (Exception e) {
  //      logger.error("Error occurred when executing the horizonStretch algorithm", e);
  //      gridPropOut = null;
  //    }
  //
  //    return gridPropOut;
  //  }

  /**
   *
   */
  public Grid3d runHorizonStretch(final Grid3d gridPropIn, final PostStack3d velVol, final Method conversionMethod,
      final AreaOfInterest areaOfInterest, final String propName, final ILogger logger, final IRepository repository) {

    UnitPreferences unitPreferences = UnitPreferences.getInstance();

    // Make sure the velocity units are correct
    Unit velUnits = velVol.getDataUnit();
    if (velUnits != Unit.METERS_PER_SECOND && velUnits != Unit.FEET_PER_SECOND) {
      logger.error("Invalid velocity units. Must be METERS_PER_SECOND or FEET_PER_SECOND.");
      return null;
    }

    // determine the horizon
    GridGeometry3d gridIn = gridPropIn.getGeometry();
    Domain domainTypeIn = gridPropIn.getZDomain();
    CMode conversionMode = null;

    if (domainTypeIn.equals(Domain.TIME)) {
      conversionMode = CMode.T2D;
    } else if (domainTypeIn.equals(Domain.DISTANCE)) {
      conversionMode = CMode.D2T;
    } else {
      logger.error("Invalid domain type. Must be time or depth.");
      return null;
    }

    boolean addObjects = true;
    if (repository == null) {
      addObjects = false;
    }

    // Initialize
    Grid3d gridPropOut = null;

    try {

      float[][] result = getConvertedHorizon(gridPropIn, velVol, velUnits, conversionMethod, conversionMode,
          areaOfInterest, logger);

      // create a new grid2d object and new property
      switch (conversionMode) {

        case T2D:
          // If the repository is null we just want an in memory grid
          if (repository == null) {
            gridPropOut = Grid3dFactory.createInMemory(propName, gridIn, unitPreferences.getVerticalDistanceUnit(),
                result, gridPropIn.getNullValue());
            // Otherwise create the grid in the repository
          } else {
            gridPropOut = Grid3dFactory.create(repository, gridPropIn, result, propName, gridIn,
                unitPreferences.getVerticalDistanceUnit());
            gridPropOut.update();
          }
          gridPropOut.setZDomain(Domain.DISTANCE);
          break;
        case D2T:
          // If the repository is null we just want an in memory grid
          if (repository == null) {
            gridPropOut = Grid3dFactory.createInMemory(propName, gridIn, unitPreferences.getTimeUnit(), result,
                gridPropIn.getNullValue());
            // Otherwise create the grid in the repository
          } else {

            gridPropOut = Grid3dFactory.create(repository, gridPropIn, result, propName, gridIn,
                unitPreferences.getTimeUnit());
            gridPropOut.update();
          }
          gridPropOut.setZDomain(Domain.TIME);
          break;
        default:
          logger.error("Invalid conversion mode - Convert from (Depth to Time) or (Time to Depth)");
          return null;
      }
      gridPropOut.update();

    } catch (Exception e) {
      logger.error("Error occurred when executing the horizonStretch algorithm", e);
      gridPropOut = null;
    }

    return gridPropOut;
  }

  //  public float[] getConvertedHorizon(final Grid2d property, final PostStack2d velVol, final Unit velUnits,
  //      final Method conversionMethod, final CMode conversionMode, final AreaOfInterest areaOfInterest,
  //      final ILogger logger) {
  //
  //    // determine the geometry
  //    LineGeometry gridGeometry = property.getGeometry();
  //    float[] convertedHvals = new float[gridGeometry.getNumBins()];
  //
  //    // Get size of horizon
  //    int nRows = 1;
  //    int nCols = gridGeometry.getNumBins();
  //
  //    // determine grid values of the horizon
  //    if (!property.getDataUnit().getDomain().equals("TIME") && !property.getDataUnit().getDomain().equals("LENGTH")) {
  //      logger.error("Exception:\nGrid must be time or depth\nNull Pointer");
  //      return null;
  //    }
  //
  //    // determine the domain type of the horzion
  //    Domain horDomainType = getEnum(property.getDataUnit().getDomain());
  //
  //    if (conversionMode.equals(CMode.T2D)) {
  //      if (horDomainType.equals(Domain.LENGTH)) {
  //        String text = "Exception:\nHorizon should contain times in order to go from time to depth";
  //
  //        logger.error(text);
  //        return null;
  //      }
  //    } else if (conversionMode.equals(CMode.D2T)) {
  //      if (horDomainType.equals(Domain.TIME)) {
  //        String text = "Exception:\nHorizon should contain depths in order to go from depth to time";
  //
  //        logger.error(text);
  //        return null;
  //      }
  //    }
  //
  //    // determine Units of the Horizon
  //    Unit horUnits = property.getDataUnit();
  //
  //    // We just want one inline and one xline
  //    boolean processingDone = false;
  //    boolean validXYdata = false;
  //
  //    _progress.beginTask("HorizonStretch", nRows);
  //    for (int row = 0; row < nRows; row++) {
  //
  //      for (int col = 0; col < nCols; col++) {
  //
  //        float horValue = property.getValue(col);
  //
  //        convertedHvals[col] = property.getNullValue();
  //        if (!property.isNull(col)) {
  //
  //          // Determine trace based on the inline and crossline
  //          // (Make sure trace is valid)
  //          Trace trace = null;
  //          Boolean processTrace = false;
  //          trace = null;
  //          processTrace = false;
  //          TraceIterator traceIterator = TraceIteratorFactory.create(velVol, areaOfInterest);
  //          while (traceIterator.hasNext()) {
  //            TraceData traceData = traceIterator.next();
  //            trace = traceData.getTrace(0);
  //            // Make sure that the trace is a live trace
  //            if (trace.isLive()) {
  //              processTrace = true;
  //            } else {
  //              processTrace = false;
  //            }
  //          }
  //
  //          // process trace if it is valid
  //          if (processTrace) {
  //            VelocityArrayTimeDepthConverter tdconv = new VelocityArrayTimeDepthConverter(trace.getData(), trace
  //                .getDeltaZValue(), velVol.getDomain(), velUnits, conversionMethod);
  //            float hval = Float.NaN;
  //
  //            if (conversionMode.equals(CMode.T2D)) {
  //              hval = Unit.convert(horValue, horUnits, ApplicationPreferences.getApplicationPreferences().getTimeUnit());
  //              hval = tdconv.getDepth(hval);
  //            } else if (conversionMode.equals(CMode.D2T)) {
  //              hval = Unit.convert(horValue, horUnits, ApplicationPreferences.getApplicationPreferences()
  //                  .getVerticalDistanceUnit());
  //              hval = tdconv.getTime(hval);
  //            }
  //            if (!Float.isNaN(hval)) {
  //              convertedHvals[col] = hval;
  //              processingDone = true;
  //            }
  //          }
  //        }
  //      }
  //      _progress.worked(1);
  //      if (_progress.isCanceled()) {
  //        break;
  //      }
  //    }
  //
  //    // Make sure some processing has been done
  //    if (!processingDone) {
  //      String msg = "Unable to convert horizon depths from depth to time";
  //      if (validXYdata) {
  //        if (conversionMode.equals(CMode.T2D)) {
  //          msg = "Unable to convert horizon times from time to depth";
  //        }
  //      } else {
  //        msg = "X,Y locations in the horizon do not match the volume";
  //      }
  //      logger.error(msg);
  //      throw new RuntimeException(msg);
  //    }
  //
  //    return convertedHvals;
  //  }

  public float[][] getConvertedHorizon(final Grid3d property, final PostStack3d velVol, final Unit velUnits,
      final Method conversionMethod, final CMode conversionMode, final AreaOfInterest areaOfInterest,
      final ILogger logger) {

    // determine the geometry
    GridGeometry3d gridGeometry = property.getGeometry();
    float[][] convertedHvals = new float[gridGeometry.getNumRows()][gridGeometry.getNumColumns()];

    // Get size of horizon
    int nRows = gridGeometry.getNumRows();
    int nCols = gridGeometry.getNumColumns();

    float zMin = velVol.getZStart();
    float zMax = velVol.getZEnd();

    // determine grid values of the horizon
    if (!property.isTimeGrid() && !property.isDepthGrid()) {
      logger.error("Exception:\nGrid must be time or depth\nNull Pointer");
      return null;
    }

    // determine the domain type of the horzion
    Domain horDomainType = property.getZDomain();

    if (conversionMode.equals(CMode.T2D)) {
      if (horDomainType.equals(Domain.DISTANCE)) {
        String text = "Exception:\nHorizon should contain times in order to go from time to depth";

        logger.error(text);
        return null;
      }
    } else if (conversionMode.equals(CMode.D2T)) {
      if (horDomainType.equals(Domain.TIME)) {
        String text = "Exception:\nHorizon should contain depths in order to go from depth to time";

        logger.error(text);
        return null;
      }
    }

    // determine Units of the Horizon
    Unit horUnits = property.getDataUnit();

    // We just want one inline and one xline
    float[] inlines = new float[1];
    float[] xlines = new float[1];
    float inlineMin = Math.min(velVol.getInlineStart(), velVol.getInlineEnd());
    float inlineMax = Math.max(velVol.getInlineStart(), velVol.getInlineEnd());
    float xlineMin = Math.min(velVol.getXlineStart(), velVol.getXlineEnd());
    float xlineMax = Math.max(velVol.getXlineStart(), velVol.getXlineEnd());
    boolean processingDone = false;
    boolean validXYdata = false;

    // convert values for each point in the horizon
    if (!HorizonUtil.isHorizonRowPreferred(velVol, gridGeometry)) {
      _progress.beginTask("HorizonStretch", nCols);
      for (int col = 0; col < nCols; col++) {
        for (int row = 0; row < nRows; row++) {

          double horValue = property.getValueAtRowCol(row, col);

          convertedHvals[row][col] = property.getNullValue();
          if (!property.isNull(row, col)) {

            // determine Xval and Yval at the current point
            double[] xy = gridGeometry.transformRowColToXY(row, col);
            double xVal = xy[0];
            double yVal = xy[1];

            // Determine Inline and Xline that correspond to Xval and Yval
            // (Get the closest Inline and Xline)
            SeismicSurvey3d geometry = velVol.getSurvey();
            float[] lines = geometry.transformXYToInlineXline(xVal, yVal, true);

            inlines[0] = lines[0];
            xlines[0] = lines[1];

            // Determine trace based on the inline and crossline
            // (Make sure trace is valid)
            Trace trace = null;
            Boolean processTrace = false;
            if (inlines[0] >= inlineMin && inlines[0] <= inlineMax && xlines[0] >= xlineMin && xlines[0] <= xlineMax) {
              validXYdata = true;
              // Determine if Inline and crossline is in the area of interest
              if (areaOfInterest == null) {
                processTrace = true;
              } else if (areaOfInterest.contains(xVal, yVal)) {
                processTrace = true;
              } else {
                processTrace = false;
              }
              // determine the trace in the volume
              if (processTrace) {
                TraceData traceData = velVol.getTraces(inlines, xlines, zMin, zMax);
                trace = traceData.getTrace(0);
                // Make sure that the trace is a live trace
                if (trace.isLive()) {
                  processTrace = true;
                } else {
                  processTrace = false;
                }
              }
            }

            // process trace if it is valid
            if (processTrace) {
              VelocityArrayTimeDepthConverter tdconv = new VelocityArrayTimeDepthConverter(trace.getData(),
                  trace.getZDelta(), velVol.getZDomain(), velUnits, conversionMethod);
              float hval = Float.NaN;

              if (conversionMode.equals(CMode.T2D)) {
                hval = (float) Unit.convert(horValue, horUnits, UnitPreferences.getInstance().getTimeUnit());
                hval = tdconv.getDepth(hval);
              } else if (conversionMode.equals(CMode.D2T)) {
                hval = (float) Unit
                    .convert(horValue, horUnits, UnitPreferences.getInstance().getVerticalDistanceUnit());
                hval = tdconv.getTime(hval);
              }
              if (!Float.isNaN(hval)) {
                convertedHvals[row][col] = hval;
                processingDone = true;
              }
            }
          }
        }
        _progress.worked(1);
        if (_progress.isCanceled()) {
          break;
        }
      }
    } else {
      _progress.beginTask("HorizonStretch", nRows);
      for (int row = 0; row < nRows; row++) {

        for (int col = 0; col < nCols; col++) {

          float horValue = property.getValueAtRowCol(row, col);

          convertedHvals[row][col] = property.getNullValue();
          if (!property.isNull(row, col)) {

            // determine Xval and Yval at the current point
            double[] xy = gridGeometry.transformRowColToXY(row, col);
            double xVal = xy[0];
            double yVal = xy[1];

            // Determine Inline and Xline that correspond to Xval and Yval
            // (Get the closest Inline and Xline)
            SeismicSurvey3d geometry = velVol.getSurvey();
            float[] lines = geometry.transformXYToInlineXline(xVal, yVal, true);

            inlines[0] = lines[0];
            xlines[0] = lines[1];

            // Determine trace based on the inline and crossline
            // (Make sure trace is valid)
            Trace trace = null;
            Boolean processTrace = false;
            if (inlines[0] >= inlineMin && inlines[0] <= inlineMax && xlines[0] >= xlineMin && xlines[0] <= xlineMax) {
              validXYdata = true;
              // Determine if Inline and crossline is in the area of interest
              if (areaOfInterest == null) {
                processTrace = true;
              } else if (areaOfInterest.contains(xVal, yVal)) {
                processTrace = true;
              } else {
                processTrace = false;
              }
              // determine the trace in the volume
              if (processTrace) {
                TraceData traceData = velVol.getTraces(inlines, xlines, zMin, zMax);
                trace = traceData.getTrace(0);
                // Make sure that the trace is a live trace
                if (trace.isLive()) {
                  processTrace = true;
                } else {
                  processTrace = false;
                }
              }
            }

            // process trace if it is valid
            if (processTrace) {
              VelocityArrayTimeDepthConverter tdconv = new VelocityArrayTimeDepthConverter(trace.getData(),
                  trace.getZDelta(), velVol.getZDomain(), velUnits, conversionMethod);
              float hval = Float.NaN;

              if (conversionMode.equals(CMode.T2D)) {
                hval = Unit.convert(horValue, horUnits, UnitPreferences.getInstance().getTimeUnit());
                hval = tdconv.getDepth(hval);
              } else if (conversionMode.equals(CMode.D2T)) {
                hval = Unit.convert(horValue, horUnits, UnitPreferences.getInstance().getVerticalDistanceUnit());
                hval = tdconv.getTime(hval);
              }
              if (!Float.isNaN(hval)) {
                convertedHvals[row][col] = hval;
                processingDone = true;
              }
            }
          }
        }
        _progress.worked(1);
        if (_progress.isCanceled()) {
          break;
        }
      }
    }

    // Make sure some processing has been done
    if (!processingDone) {
      String msg = "Unable to convert horizon depths from depth to time";
      if (validXYdata) {
        if (conversionMode.equals(CMode.T2D)) {
          msg = "Unable to convert horizon times from time to depth";
        }
      } else {
        msg = "X,Y locations in the horizon do not match the volume";
      }
      logger.error(msg);
      throw new RuntimeException(msg);
    }

    return convertedHvals;
  }

  /**
   * @throws CoreException  
   */
  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) throws CoreException {
    _progress = monitor;
    List<GeologicInterpretation> gridPropIn = Arrays.asList(_inputHorizons.get());
    SeismicDataset velVol = _velocityVolume.get();
    Method method = _conversionMethod.get();
    AreaOfInterest aoi = _aoi.get();
    GeologicInterpretation gridPropOut = null;

    for (GeologicInterpretation grid : gridPropIn) {
      String propName = grid.getMapper().createOutputDisplayName(grid.getDisplayName(),
          "_" + _outputHorizonSuffix.get());
      //  String propName = grid.getDisplayName() + "_" + _outputHorizonSuffix.get();

      if (grid instanceof Grid3d) {
        gridPropOut = runHorizonStretch((Grid3d) grid, (PostStack3d) velVol, method, aoi, propName, logger, repository);
      }
      //      if (grid instanceof Grid2d) {
      //        gridPropOut = runHorizonStretch((Grid2d) grid, (PostStack2d) velVol, method, aoi, propName, logger, repository);
      //      }
      if (gridPropOut != null) {
        gridPropOut.setComment(_outputComments.get());
      }
    }

  }

  @Override
  public void propertyChanged(String key) {
    if (key.equals(_horizonType.getKey())) {
      if (_horizonType.get().equals(HorizonType.TWO_DIM)) { //2D
        setVolumeFilter(_postStack2dFilter);
      } else { //3D
        setVolumeFilter(_postStack3dFilter);
      }
    }
  }

  @Override
  public void validate(IValidation results) {
    // Check a velocity volume was specified.
    if (_velocityVolume.isNull()) {
      results.error(_velocityVolume, "No velocity volume (PostStack2d/3d) specified");
    }

    // Check if an area of interest is to be used, one was specified
    if (_useAoi.get()) {
      if (_aoi.isNull()) {
        results.error(_aoi, "No area-of-interest specified.");
      }
    }
  }
}
