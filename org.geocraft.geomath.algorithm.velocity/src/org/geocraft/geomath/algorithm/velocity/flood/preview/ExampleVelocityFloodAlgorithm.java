package org.geocraft.geomath.algorithm.velocity.flood.preview;


import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.Trace.Status;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;
import org.geocraft.core.model.seismic.SeismicDataset.StorageFormat;
import org.geocraft.geomath.algorithm.util.IPostStack3dAlgorithm;
import org.geocraft.geomath.algorithm.velocity.flood.VelocityFloodConstants.ConstantOrGridSelection;
import org.geocraft.geomath.algorithm.velocity.flood.VelocityFloodConstants.FloodDirection;
import org.geocraft.geomath.algorithm.velocity.flood.VelocityFloodConstants.FloodType;
import org.geocraft.geomath.algorithm.velocity.flood.VelocityFloodConstants.ReferenceSelection;


public class ExampleVelocityFloodAlgorithm implements IPostStack3dAlgorithm {

  /** The algorithm type string. */
  private static final String ALGORITHM_TYPE = "PostStack3d Velocity Flood";

  /** The poststack3d input volume. */
  private final PostStack3d _inputVolume;

  /** The seismic geometry of the input volume. */
  private final SeismicSurvey3d _geometry1;

  /** The area-of-interest. */
  private final AreaOfInterest _aoi;

  /** The flood type (constant, gradient or dataset). */
  private final FloodType _floodType;

  /** The data source of the velocity (constant or horizon). */
  private final ConstantOrGridSelection _velocityDataSource;

  /** The constant flood velocity. */
  private final float _floodVelocity;

  /** The constant flood gradient. */
  private final float _floodGradient;

  /** The velocity horizon. */
  private final Grid3d _velocityHorizon;

  /** The data source of the gradient (constant or horizon). */
  private final ConstantOrGridSelection _gradientDataSource;

  /** The gradient horizon. */
  private final Grid3d _gradientHorizon;

  /** The data source of the reference z value (constant or horizon). */
  private final ReferenceSelection _referenceZDataSource;

  /** The constant reference z value. */
  private final float _referenceZConstant;

  /** The reference z horizon. */
  private final Grid3d _referenceZHorizon;

  /** The flood direction (below, above or between). */
  private final FloodDirection _floodDirection;

  /** The top horizon for flooding. */
  private final Grid3d _topHorizon;

  /** The base horizon for flooding. */
  private final Grid3d _baseHorizon;

  /** The poststack3d input volume. */
  private final PostStack3d _dataSetVolume;

  /** The multiplier to scale the output volume */
  private final float _multiplier;

  /** The multiplier to scale the output volume */
  private final float _adder;

  /** The output poststack3d volume name. */
  private final String _outputName;

  /** The velocity flood comments to append. */
  private final String _outputComments;

  /**
   * The full constructor.
   * @param inputVolume the input poststack3d volume to flood.
   * @param aoi the area-of-interest.
   * @param floodType the flood type (constant, gradient or dataset).
   * @param velocityDataSource the velocity data source (constants or horizons).
   * @param floodVelocity the constant flood velocity.
   * @param velocityHorizon the velocity horizon.
   * @param velocityDataSource the gradient data source (constants or horizons).
   * @param floodGradient the constant flood gradient.
   * @param gradientHorizon the gradient horizon.
   * @param referenceZDataSource the reference z data source (constant or horizon).
   * @param referenceZConstant the reference z value.
   * @param referenceZHorizon the reference z horizon.
   * @param floodDirection the flood direction (below, above or between).
   * @param topHorizon the top horizon for flooding.
   * @param baseHorizon the base horizon for flooding.
   * @param dataSetVolume the volume used when flood type is dataset
   * @param outputName the output poststack3d volume name.
   * @param outputComments the velocity flood comments to append.
   */
  public ExampleVelocityFloodAlgorithm(final PostStack3d inputVolume, final AreaOfInterest aoi, final FloodType floodType, final ConstantOrGridSelection velocityDataSource, final float floodVelocity, final Grid3d velocityHorizon, final ConstantOrGridSelection gradientDataSource, final float floodGradient, final Grid3d gradientHorizon, final ReferenceSelection referenceZDataSource, final float referenceZConstant, final Grid3d referenceZHorizon, final FloodDirection floodDirection, final Grid3d topHorizon, final Grid3d baseHorizon, final PostStack3d dataSetVolume, final float multiplier, final float adder, final String outputName, final String outputComments) {
    _inputVolume = inputVolume;
    _geometry1 = inputVolume.getSurvey();
    _aoi = aoi;
    _floodType = floodType;
    _velocityDataSource = velocityDataSource;
    _floodVelocity = floodVelocity;
    _velocityHorizon = velocityHorizon;
    _gradientDataSource = gradientDataSource;
    _floodGradient = floodGradient;
    _gradientHorizon = gradientHorizon;
    _referenceZDataSource = referenceZDataSource;
    _referenceZConstant = referenceZConstant;
    _referenceZHorizon = referenceZHorizon;
    _floodDirection = floodDirection;
    _topHorizon = topHorizon;
    _baseHorizon = baseHorizon;
    _dataSetVolume = dataSetVolume;
    _multiplier = multiplier;
    _adder = adder;
    _outputName = outputName;
    _outputComments = outputComments;
  }

  /**
   * {@inheritDoc}
   */
  public String getAlgorithmType() {
    return ALGORITHM_TYPE;
  }

  /**
   * {@inheritDoc}
   */
  public StorageOrder getStorageOrder() {
    return _inputVolume.getPreferredOrder();
  }

  public void close() {
    _inputVolume.close();
  }

  @Override
  public Trace computeTrace(final PostStack3d ps3d, final float inline, final float xline, final float zStart,
      final float zEnd) {
    float[] inlines = { inline };
    float[] xlines = { xline };
    CoordinateSeries coords = _geometry1.transformInlineXlineToXY(inlines, xlines);
    double x = coords.getX(0);
    double y = coords.getY(0);
    TraceData traceData = _inputVolume.getTraces(inlines, xlines, zStart, zEnd);
    Trace trace = traceData.getTrace(0);
    float[] data = trace.getDataReference();

    float[] rvals = null;
    if (_floodType.equals(FloodType.Dataset)) {
      SeismicSurvey3d geom = _dataSetVolume.getSurvey();
      float[] ix = geom.transformXYToInlineXline(trace.getX(), trace.getY(), true);
      TraceData datasetTrace = _dataSetVolume.getTraces(new float[] { ix[0] }, new float[] { ix[1] },
          trace.getZStart(), trace.getZEnd());
      rvals = datasetTrace.getTrace(0).getData();
    }

    float topValue = Float.NaN;
    float baseValue = Float.NaN;
    if (_floodDirection.equals(FloodDirection.Above)) {
      topValue = zStart;
      baseValue = _topHorizon.getValueAtXY(x, y);
      if (_topHorizon.isNull(baseValue)) {
        baseValue = Float.NaN;
      }
    } else if (_floodDirection.equals(FloodDirection.Below)) {
      topValue = _topHorizon.getValueAtXY(x, y);
      if (_topHorizon.isNull(topValue)) {
        topValue = Float.NaN;
      }
      baseValue = zEnd;
    } else if (_floodDirection.equals(FloodDirection.Between)) {
      topValue = _topHorizon.getValueAtXY(x, y);
      if (_topHorizon.isNull(topValue)) {
        topValue = Float.NaN;
      }
      baseValue = _baseHorizon.getValueAtXY(x, y);
      if (_baseHorizon.isNull(baseValue)) {
        baseValue = Float.NaN;
      }
    }
    float floodVelocity = 0;
    float floodGradient = 0;
    float referenceZ = 0;
    if (_floodType.equals(FloodType.Constant)) {
      if (_velocityDataSource.equals(ConstantOrGridSelection.Constant)) {
        floodVelocity = _floodVelocity;
      } else if (_velocityDataSource.equals(ConstantOrGridSelection.Grid)) {
        floodVelocity = _velocityHorizon.getValueAtXY(x, y);
        if (_velocityHorizon.isNull(floodVelocity)) {
          floodVelocity = Float.NaN;
        }
      }
    } else if (_floodType.equals(FloodType.Gradient)) {
      if (_velocityDataSource.equals(ConstantOrGridSelection.Constant)) {
        floodVelocity = _floodVelocity;
      } else if (_velocityDataSource.equals(ConstantOrGridSelection.Grid)) {
        floodVelocity = _velocityHorizon.getValueAtXY(x, y);
        if (_velocityHorizon.isNull(floodVelocity)) {
          floodVelocity = Float.NaN;
        }
      }
      if (_gradientDataSource.equals(ConstantOrGridSelection.Constant)) {
        floodGradient = _floodGradient;
      } else if (_gradientDataSource.equals(ConstantOrGridSelection.Grid)) {
        floodGradient = _gradientHorizon.getValueAtXY(x, y);
        if (_gradientHorizon.isNull(floodGradient)) {
          floodGradient = Float.NaN;
        }
      }
      if (_referenceZDataSource.equals(ReferenceSelection.Constant)) {
        referenceZ = _referenceZConstant;
      } else if (_referenceZDataSource.equals(ReferenceSelection.Grid)) {
        referenceZ = _referenceZHorizon.getValueAtXY(x, y);
        if (_referenceZHorizon.isNull(referenceZ)) {
          referenceZ = Float.NaN;
        }
      }
    }

    if (!_floodType.equals(FloodType.Dataset)) {
      if (!Float.isNaN(topValue) && !Float.isNaN(baseValue) && !Float.isNaN(floodVelocity)
          && !Float.isNaN(floodGradient) && !Float.isNaN(referenceZ)) {
        for (int k = 0; k < trace.getNumSamples(); k++) {
          float z = trace.getZStart() + k * trace.getZDelta();
          if (z >= topValue && z <= baseValue) {
            data[k] = floodVelocity + floodGradient * (z - referenceZ);
          }
        }
      }
    } else {
      if (!Float.isNaN(topValue) && !Float.isNaN(baseValue)) {
        for (int k = 0; k < trace.getNumSamples(); k++) {
          float z = trace.getZStart() + k * trace.getZDelta();
          if (z >= topValue && z <= baseValue) {
            if (_multiplier != 1.0f || _adder != 0.0f) {
              data[k] = rvals[k] * _multiplier + _adder;
            } else {
              data[k] = rvals[k];
            }
          }
        }
      }

    }
    Status status = trace.getStatus();
    if (_aoi != null && !_aoi.contains(x, y)) {
      status = Status.Missing;
      for (int k = 0; k < trace.getNumSamples(); k++) {
        data[k] = 0;
      }
    }
    trace.setStatus(status);

    return trace;
  }

  @Override
  public void update(final PostStack3d ps3d) {

    ps3d.setInlineRangeAndDelta(_inputVolume.getInlineStart(), _inputVolume.getInlineEnd(), _inputVolume
        .getInlineDelta());
    ps3d.setXlineRangeAndDelta(_inputVolume.getXlineStart(), _inputVolume.getXlineEnd(), _inputVolume.getXlineDelta());
    ps3d.setZRangeAndDelta(_inputVolume.getZStart(), _inputVolume.getZEnd(), _inputVolume.getZDelta());
    ps3d.setZMaxRangeAndDelta(_inputVolume.getZMaxStart(), _inputVolume.getZMaxEnd(), _inputVolume.getZMaxDelta());
    ps3d.setComment(_inputVolume.getComment());
    ps3d.setActualExtent(_inputVolume.getActualExtent());
    ps3d.setExtent(_inputVolume.getExtent());
    ps3d.setDataUnit(_inputVolume.getDataUnit());
    ps3d.setZDomain(_inputVolume.getZDomain());

    ps3d.setProjectName(_inputVolume.getProjectName());
    ps3d.setElevationDatum(_inputVolume.getElevationDatum());
    ps3d.setElevationReferences(_inputVolume.getElevationReference());

    ps3d.setSurvey(_inputVolume.getSurvey());
    ps3d.setDirty(true);

  }

  public StorageFormat getStorageFormat() {
    return _inputVolume.getStorageFormat();
  }

}
