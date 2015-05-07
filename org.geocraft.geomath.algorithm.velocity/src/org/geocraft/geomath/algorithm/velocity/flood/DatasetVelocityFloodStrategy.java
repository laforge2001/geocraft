package org.geocraft.geomath.algorithm.velocity.flood;


import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.geomath.algorithm.velocity.flood.VelocityFloodConstants.FloodDirection;


public class DatasetVelocityFloodStrategy extends AbstractVelocityFloodStrategy {

  private final PostStack3d _replaceVolume;

  private final FloodDirection _floodDir;

  private final float _multiplier;

  private final float _adder;

  public DatasetVelocityFloodStrategy(final PostStack3d replaceVolume, final FloodDirection floodDir, final float multiplier, final float adder) {
    _replaceVolume = replaceVolume;
    _floodDir = floodDir;
    _multiplier = multiplier;
    _adder = adder;
  }

  /**
   * @param velocity  
   */
  public void setVelocity(final float velocity) {
    // velocity not used in the DataSetFloodStrategy
  }

  /**
   * @param gradient  
   */
  public void setGradient(final float gradient) {
    // gradient not used in the DataSetFloodStrategy
  }

  /**
   * @param referenceDepth  
   */
  public void setReferenceDepth(final float referenceDepth) {
    // The reference depth not used in the DataSetFloodStrategy
  }

  public Trace flood(final Trace trace, final float[] horizonValues, final ILogger logger) {

    float[] tvals = trace.getData();

    SeismicSurvey3d geom = _replaceVolume.getSurvey();
    float[] ix = geom.transformXYToInlineXline(trace.getX(), trace.getY(), true);

    TraceData data;
    try {
      data = _replaceVolume.getTraces(new float[] { ix[0] }, new float[] { ix[1] }, trace.getZStart(), trace
          .getZEnd());
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return trace;
    }

    float[] rvals = data.getTrace(0).getData();

    int[] indices = convertHorizonValuesToSampleIndices(trace, horizonValues);
    indices = adjustIndices(_floodDir, tvals.length - 1, indices);

    for (int h = 0; h < indices.length; h += 2) {

      int top = indices[h];
      int base = indices[h + 1];

      if (!(top == base)) {
        for (int i = top; i <= base; i++) {
          if (_multiplier != 1.0f || _adder != 0.0f) {
            tvals[i] = scaleValue(rvals[i]);
          } else {
            tvals[i] = rvals[i];
          }
        }
      }
    }
    return new Trace(trace, tvals);
  }

  private float scaleValue(final float scaleMe) {
    return scaleMe * _multiplier + _adder;
  }
}
