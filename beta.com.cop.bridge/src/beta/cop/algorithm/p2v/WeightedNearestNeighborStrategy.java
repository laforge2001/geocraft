/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package beta.cop.algorithm.p2v;


import beta.cop.model.grid.GridSampleIterator;
import beta.cop.model.grid.IRegularGrid;
import beta.cop.model.points.IPointSet3D;



public class WeightedNearestNeighborStrategy extends PointsToVolumeStrategy {

  public WeightedNearestNeighborStrategy(IPointSet3D points, IRegularGrid rgrid) {
    super(points, rgrid);
  }

  /* (non-Javadoc)
   * @see com.cop.velocity.points.IPointToVolumeStrategy#interpolate()
   */
  @Override
  public void interpolate(String attributeOfInterest) {
    int[] pos;
    double[] wxyz = new double[3];

    GridSampleIterator giter = new GridSampleIterator(_rgrid);
    long totalSamples = getTotalSamples();
    long sampleCounter = 0;

    while (giter.hasNext()) {
      pos = giter.next();
      ++sampleCounter;
      _rgrid.worldCoords(pos, wxyz);
      double distance;
      double sumValues = 0;
      double sumWeights = 0;
      double weight = 0;
      for (int i = 0; i < _points.size(); ++i) {
        int xdim = _points.getXdim();
        int ydim = _points.getYdim();
        int zdim = _points.getZdim();
        double xyz[] = _points.getWorldLocation(i);

        // TODO: create an outside method as distance calculation.
        distance = Math.sqrt((wxyz[2] - xyz[zdim]) * (wxyz[2] - xyz[zdim]) + (wxyz[0] - xyz[xdim])
            * (wxyz[0] - xyz[xdim]) + (wxyz[1] - xyz[ydim]) * (wxyz[1] - xyz[ydim]));
        if (distance < 1) {
          distance = 1;
        }
        weight = 1. / distance;
        sumWeights = sumWeights + weight;
        sumValues += _points.getAttributeValue(i, attributeOfInterest) * weight;
      } // end for i
      giter.putSampleFloat((float) (sumValues / sumWeights));
      _progressPercentage = (float) sampleCounter / (float) totalSamples * 100f;
    }// end while grid
  }

}
