/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package beta.cop.algorithm.p2v;

import beta.cop.model.grid.GridSampleIterator;
import beta.cop.model.grid.IRegularGrid;
import beta.cop.model.points.IPointSet3D;
import beta.cop.model.points.PointSet3DUtil;

/**
 * applies the point's attribute whose location is closest to the trace sample
 * 
 */
//public class NearestNeighborStrategy extends PointsToVolumeStrategy {
//
//	BlockingQueue<PositionWorldCoordPair> _positionQueueSource;
//	BlockingQueue<PositionWorldCoordPair> _completedFinds;
//	GridSampleIterator _giter;
//
//	List<Thread> _processingThreads = new ArrayList<Thread>();
//	private volatile Boolean _threadDone = false;
//	private volatile long currentSample = 0;
//
//	private final int BUFFER_LEN = 1000;
//	List<PointSetThreadHelper> _threadHelpers = new ArrayList<PointSetThreadHelper>();
//
//	public NearestNeighborStrategy(IPointSet3D points, IRegularGrid rgrid) {
//		super(points, rgrid);
//	}
//
//	private void startThreads(Runnable r, int numThreads) {
//		for (int i = 0; i < numThreads; ++i) {
//			Thread someThread = new Thread(r);
//			_processingThreads.add(someThread);
//			someThread.start();
//		}
//	}
//
//	private void waitForThreadCompletion() {
//		for (Thread t : _processingThreads) {
//			try {
//				t.join();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.cop.velocity.points.IPointToVolumeStrategy#interpolate()
//	 */
//	@Override
//	public void interpolate(String attributeOfInterest) {
//
//		int numThreads = Runtime.getRuntime().availableProcessors();
//		int numWriters = 1; // Math.max(1, numThreads / 2);
//		int numSearchers = Math.max(1, numThreads - 2);
//		int numThreadsToUse = numWriters + numSearchers;
//
//		System.out.println("Using " + numWriters + " writer threads and "
//				+ numSearchers + " search threads");
//		_positionQueueSource = new LinkedBlockingQueue<PositionWorldCoordPair>(
//				BUFFER_LEN * numThreadsToUse);
//
//		dividePointSet(_points, numSearchers, attributeOfInterest);
//
//		startThreads(new PositionProducerRunnable(_rgrid, attributeOfInterest),
//				1);
//		for (PointSetThreadHelper h : _threadHelpers) {
//			startThreads(h, 1);
//		}
//		startThreads(new WriteOutRunnable(), numWriters);
//
//		waitForThreadCompletion();
//	}
//
//	private void dividePointSet(IPointSet3D _points, int numSearchers,
//			String attributeOfInterest) {
//		int sizePerChunk = _points.size() / numSearchers;
//		boolean first = true;
//		int attributeIndex = _points.getAttributes().indexOf(
//				attributeOfInterest);
//
//		for (int i = 0; i < numSearchers; ++i) {
//			BlockingQueue<PositionWorldCoordPair> in;
//			if (first) {
//				in = _positionQueueSource;
//				first = false;
//			} else {
//				in = _threadHelpers.get(i - 1).getOutQueue();
//			}
//			BlockingQueue<PositionWorldCoordPair> out = new ArrayBlockingQueue<PositionWorldCoordPair>(
//					BUFFER_LEN);
//			PointSetThreadHelper helper = new PointSetThreadHelper(in, out,
//					attributeIndex);
//			_threadHelpers.add(helper);
//
//			for (int j = 0; j < sizePerChunk; ++j) {
//				helper.add(_points.size() - 1,
//						_points.remove(_points.size() - 1));
//			}
//		}
//
//		PointSetThreadHelper helper = _threadHelpers
//				.get(_threadHelpers.size() - 1);
//		while (_points.size() > 0) {
//			helper.add(_points.size() - 1, _points.remove(_points.size() - 1));
//		}
//
//	}
//
//	class PositionProducerRunnable implements Runnable {
//
//		GridSampleIterator _giter;
//		IRegularGrid _rgrid;
//		String attribute;
//		long totalSamples = getTotalSamples();
//		long currentSample = 0;
//
//		PositionProducerRunnable(IRegularGrid rgrid, String attr) {
//			_rgrid = rgrid;
//			attribute = attr;
//		}
//
//		@Override
//		public void run() {
//			try {
//				int[] pos;
//				double[] wxyz = new double[3];
//				_giter = new GridSampleIterator(_rgrid);
//
//				while (_giter.hasNext()) {
//
//					pos = _giter.next();
//					_rgrid.worldCoords(pos, wxyz);
//					PositionWorldCoordPair pair = new PositionWorldCoordPair(
//							pos, wxyz);
//					if (!_positionQueueSource.offer(pair)) {
//
//						_positionQueueSource.put(pair);
//
//					}
//				}// end while grid
//				_threadDone = true;
//				PositionWorldCoordPair stopProcessors = new PositionWorldCoordPair(
//						null, null);
//				stopProcessors.setMinDistance(Double.MIN_VALUE);
//				_positionQueueSource.put(stopProcessors);
//				System.out.println("Reader finished!");
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//
//	// class PositionConsumerRunnable implements Runnable {
//	//
//	// private final IPointSet3D _points;
//	//
//	// public PositionConsumerRunnable(IPointSet3D points,
//	// String attributeOfInterest) {
//	// _points = points;
//	// }
//	//
//	// @Override
//	// public void run() {
//	// PositionWorldCoordPair pos = null;
//	// while (!_positionQueueSource.isEmpty() || !_threadDone) {
//	//
//	// try {
//	// pos = _positionQueueSource.take();
//	// int imin = PointSet3DUtil.findNearest(_points,
//	// pos.getWorld());
//	//
//	// pos.setIndex(imin);
//	// _completedFinds.put(pos);
//	// } catch (InterruptedException e1) {
//	// e1.printStackTrace();
//	// }
//	//
//	// }
//	// }
//	// }
//
//	class WriteOutRunnable implements Runnable {
//
//		long totalSamples = getTotalSamples();
//		private final BlockingQueue<PositionWorldCoordPair> _completedFinds;
//
//		WriteOutRunnable() {
//			_completedFinds = _threadHelpers.get(_threadHelpers.size() - 1)
//					.getOutQueue();
//		}
//
//		private boolean processorsDone() {
//			for (PointSetThreadHelper h : _threadHelpers) {
//				if (!h.isDone())
//					return false;
//			}
//			return true;
//		}
//
//		@Override
//		public void run() {
//
//			while (true) {
//				try {
//					PositionWorldCoordPair pos = _completedFinds.take();
//					if (pos.getMinDistance() == Double.MIN_VALUE) {
//						break;
//					}
//					float val = pos.getValue();
//					synchronized (_rgrid) {
//						try {
//							_rgrid.putSample(val, pos.getPos());
//						} catch (Exception e) {
//							System.out
//									.println("Caught Exception attempting to write position: "
//											+ Arrays.toString(pos.getPos()));
//						}
//						_progressPercentage = 100f * currentSample++
//								/ totalSamples;
//					}
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//			System.out.println("Writer Finished!");
//		}
//	}
//
//}
/**
 * applies the point's attribute whose location is closest to the trace sample
 * 
 */
public class NearestNeighborStrategy extends PointsToVolumeStrategy {

	public NearestNeighborStrategy(IPointSet3D points, IRegularGrid rgrid) {
		super(points, rgrid);
	}

	/*
	 * (non-Javadoc)
	 * 
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

			int imin = PointSet3DUtil.findNearest(_points, wxyz);

			giter.putSampleFloat(_points.getAttributeValue(imin,
					attributeOfInterest));
			_progressPercentage = (float) sampleCounter / (float) totalSamples
					* 100f;
		}// end while grid
	}
}
