package org.geocraft.geomath.algorithm.velocity.flood;


import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.geomath.algorithm.velocity.flood.VelocityFloodConstants.FloodDirection;


public class ConstantVelocityFloodStrategy extends AbstractVelocityFloodStrategy {

  private final FloodDirection _floodDir;

  private float _velocity;

  public ConstantVelocityFloodStrategy(final FloodDirection floodDir, final float velocity) {
    _floodDir = floodDir;
    _velocity = velocity;
  }

  public void setVelocity(final float velocity) {
    _velocity = velocity;
  }

  public void setGradient(final float gradient) {
    // Gradient not used in this flood strategy.
  }

  public void setReferenceDepth(final float referenceDepth) {
    // Reference depth not used in this flood strategy.
  }

  public Trace flood(final Trace trace, final float[] horizonValues, final ILogger logger) {

    float[] tvals = trace.getData();

    int[] indices = convertHorizonValuesToSampleIndices(trace, horizonValues);
    indices = adjustIndices(_floodDir, tvals.length - 1, indices);

    for (int h = 0; h < indices.length; h += 2) {

      int top = indices[h];
      int base = indices[h + 1];

      if (!(top == base)) {
        for (int i = top; i <= base; i++) {
          tvals[i] = _velocity;
        }
      }
    }
    return new Trace(trace, tvals);
  }
}
