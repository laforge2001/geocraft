/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package beta.cop.algorithm.p2v;

import java.util.Arrays;

import beta.cop.model.grid.IRegularGrid;
import beta.cop.model.points.IPointSet3D;

/**
 * This approach will simply traverse the list of points, putting the values in
 * the approximate locations in the volume
 * 
 * @author georde
 * 
 */
public class DirectInsertStrategy extends PointsToVolumeStrategy {

	/**
	 * @param points
	 * @param rgrid
	 */
	public DirectInsertStrategy(IPointSet3D points, IRegularGrid rgrid) {
		super(points, rgrid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cop.velocity.points.PointsToVolumeStrategy#interpolate()
	 */
	@Override
	public void interpolate(String attributeOfInterest) {
		System.out.println("LOCAL LENGTHS: "
				+ Arrays.toString(_rgrid.getLocalLengths()));
		System.out.println("GLOBAL LENGTHS: "
				+ Arrays.toString(_rgrid.getLengths()));
		for (int i = 0; i < _points.size(); ++i) {
			computeWeights(i, attributeOfInterest);
			_progressPercentage = i / (float) _points.size() * 100.0f;
		}
	}

	public void computeWeights(int index, String attributeOfInterest) {

		float attr = _points.getAttributeValue(index, attributeOfInterest);

		long[] loc = _points.getIndexLocation(index);
		int[] ix = new int[3];
		ix[RGRID_XDIM] = (int) loc[0];
		ix[RGRID_YDIM] = (int) loc[1];
		ix[RGRID_ZDIM] = (int) loc[2];

		// verify the position is a local position
		if (_rgrid.isPositionLocal(ix)) {
			// use local position on current node - if operating distributed
			_rgrid.putSample(attr, ix);
		}
	}
}
