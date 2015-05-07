/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package beta.cop.algorithm.p2v;

import beta.cop.model.grid.IRegularGrid;
import beta.cop.model.grid.OrientationType;
import beta.cop.model.points.IPointSet3D;

public abstract class PointsToVolumeStrategy implements IPointToVolumeStrategy {

	protected IPointSet3D _points;

	protected IRegularGrid _rgrid;

	// variable to track percentage complete
	protected Float _progressPercentage;

	// constants to keep track of the x,y,z indexing based on orientation of
	// RegularGrid
	protected static int RGRID_XDIM;

	protected static int RGRID_YDIM;

	protected static int RGRID_ZDIM;

	public PointsToVolumeStrategy(IPointSet3D points, IRegularGrid rgrid) {
		_points = points;
		_rgrid = rgrid;
		if (OrientationType.XYZ.equals(rgrid.getOrientation())) {
			RGRID_XDIM = 0;
			RGRID_YDIM = 1;
			RGRID_ZDIM = 2;
		} else {
			RGRID_XDIM = 1;
			RGRID_YDIM = 2;
			RGRID_ZDIM = 0;
		}
		_progressPercentage = 0f;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cop.velocity.points.IPointToVolumeStrategy#interpolate()
	 */
	@Override
	public abstract void interpolate(String attributeOfInterest);

	public float getPercentComplete() {
		return _progressPercentage;
	}

	protected long getTotalSamples() {
		long totalSamples = 1;
		for (int dim : _rgrid.getLocalLengths()) {
			totalSamples *= dim;
		}
		return totalSamples;
	}
}
