package org.geocraft.geomath.algorithm.velocity.flood;


import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.geomath.algorithm.velocity.flood.VelocityFloodConstants.FloodDirection;


public class GradientVelocityFloodStrategy extends AbstractVelocityFloodStrategy {

  private final FloodDirection _floodDir;

  private float _velocity;

  private float _gradient;

  private float _referenceDepth;

  public GradientVelocityFloodStrategy(final FloodDirection floodDir, final float velocity, final float gradient, final float referenceDepth) {
    _floodDir = floodDir;
    _velocity = velocity;
    _gradient = gradient;
    _referenceDepth = referenceDepth;
  }

  public void setVelocity(final float velocity) {
    _velocity = velocity;
  }

  public void setGradient(final float gradient) {
    _gradient = gradient;
  }

  public void setReferenceDepth(final float referenceDepth) {
    _referenceDepth = referenceDepth;
  }

  public Trace flood(final Trace trace, final float[] horizonValues, final ILogger logger) {
    float sampleRate = trace.getZDelta();
    float minDepth = trace.getZStart();
    float[] tvals = trace.getData();

    int[] indices = convertHorizonValuesToSampleIndices(trace, horizonValues);
    indices = adjustIndices(_floodDir, tvals.length - 1, indices);

    for (int h = 0; h < indices.length; h += 2) {

      int top = indices[h];
      int base = indices[h + 1];

      if (!(top == base)) {
        for (int i = top; i <= base; i++) {
          float currentDepth = minDepth + i * sampleRate;
          tvals[i] = _velocity + _gradient * (currentDepth - _referenceDepth);
        }
      }
    }
    return new Trace(trace, tvals);
  }
}
