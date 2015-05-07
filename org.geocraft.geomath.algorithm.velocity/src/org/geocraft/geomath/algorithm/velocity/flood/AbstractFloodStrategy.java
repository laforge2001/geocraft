package org.geocraft.geomath.algorithm.velocity.flood;


import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.geomath.algorithm.velocity.flood.VelocityFloodConstants.FloodDirection;


public abstract class AbstractFloodStrategy implements IFloodStrategy {

  /**
   * Converts the depth or time (defined by the input horizons) to the corresponding index of the
   * seismic trace. Note that this routine will return negative numbers and indexes larger than
   * length of the array if the depth/time does not occur inside the range of the trace.
   * TODO: check units are compatible
   * TODO: math overflow?
   *
   * @param trace of seismic data
   * @param horizonValues the z values (times or depths) to convert
   * @return corresponding index in the seismic trace.
   */
  protected int[] convertHorizonValuesToSampleIndices(final Trace trace, final float[] horizonValues) {
    int[] indices = new int[horizonValues.length];
    float zmin = trace.getZStart();
    float sampleRate = trace.getZDelta();
    for (int i = 0; i < horizonValues.length; i++) {
      long index = Math.round((horizonValues[i] - zmin) / sampleRate);
      indices[i] = (int) index;
    }
    return indices;
  }

  /**
   * Clips indices so that they fit in the range of the seismic data.
   *
   * @param floodDirection of the flood fill (above, below etc.)
   * @param maxLength number of samples in the seismic trace
   * @param indices array to be converted.
   * @return array of indices clipped to 0, maxLength
   */
  protected int[] adjustIndices(final FloodDirection floodDirection, final int maxLength, final int[] indices) {

    int size = Math.max(2, indices.length);
    int[] result = new int[size];

    for (int i = 0; i < indices.length; i = i + 2) {

      int top = -1;
      int base = -1;

      switch (floodDirection) {
        case Below:
          top = Math.max(0, indices[i]);
          base = maxLength;
          break;

        case Above:
          top = 0;
          base = Math.min(indices[i], maxLength);
          break;

        case Between:
          top = Math.max(0, indices[i]);
          base = Math.min(indices[i + 1], maxLength);
          break;

        default:
          assert false : "Invalid flood direction " + floodDirection;
          break;
      }

      result[i] = top;
      result[i + 1] = base;
    }

    return result;
  }
}
