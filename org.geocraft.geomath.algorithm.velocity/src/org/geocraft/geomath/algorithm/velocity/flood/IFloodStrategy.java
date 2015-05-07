package org.geocraft.geomath.algorithm.velocity.flood;


import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.service.logging.ILogger;


public interface IFloodStrategy {

  /**
   * Floods the specified trace.
   * The horizon values are used to control the z range of the flooding.
   * @param trace the trace to flood.
   * @param horizonValues the horizon values.
   * @param logger the logger to use.
   * @return the flooded trace.
   */
  Trace flood(Trace trace, float[] horizonValues, final ILogger logger);

  // set the velocity value
  void setVelocity(float velocity);

  // set the gradient value
  void setGradient(float gradient);

  //  set the referenceDepth
  void setReferenceDepth(float referenceDepth);

}
