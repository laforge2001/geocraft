package beta.cop.algorithm.p2v;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import beta.cop.model.grid.GridSampleIterator;
import beta.cop.model.grid.IRegularGrid;
import beta.cop.model.points.IPointSet3D;

/**
 * This class uses a pipeline-like approach for multi-threading. Basically the
 * entire point set is split evenly among the threads with no regards to
 * ordering or spatial location. Each thread will input a point to search the
 * nearest neighbor for, search its local set of points, find the closest out of
 * that set, pass it off to the next thread, and repeat. The last search thread
 * writes its output to the write buffer, where the writer thread pulls it off
 * and writes it to the volume buffer.
 * 
 * @author georde
 * 
 */
public class NearestNeighborThreadedStrategy extends PointsToVolumeStrategy {

	BlockingQueue<PositionWorldCoordPair> _positionQueueSource;
	BlockingQueue<PositionWorldCoordPair> _completedFinds;
	GridSampleIterator _giter;

	List<Thread> _processingThreads = new ArrayList<Thread>();
	private volatile long currentSample = 0;

	private final int BUFFER_LEN = 1000;
	List<PointSetThreadHelper> _threadHelpers = new ArrayList<PointSetThreadHelper>();

	public NearestNeighborThreadedStrategy(IPointSet3D points,
			IRegularGrid rgrid) {
		super(points, rgrid);
	}

	private void startThreads(Runnable r, int numThreads) {
		for (int i = 0; i < numThreads; ++i) {
			Thread someThread = new Thread(r);
			_processingThreads.add(someThread);
			someThread.start();
		}
	}

	private void waitForThreadCompletion() {
		for (Thread t : _processingThreads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cop.velocity.points.IPointToVolumeStrategy#interpolate()
	 */
	@Override
	public void interpolate(String attributeOfInterest) {

		int numThreads = Runtime.getRuntime().availableProcessors();
		int numWriters = 1;
		int numSearchers = Math.max(1, numThreads - 2);
		int numThreadsToUse = numWriters + numSearchers;

		System.out.println("Using " + numWriters + " writer threads and "
				+ numSearchers + " search threads");
		_positionQueueSource = new LinkedBlockingQueue<PositionWorldCoordPair>(
				BUFFER_LEN * numThreadsToUse);

		dividePointSet(_points, numSearchers, attributeOfInterest);

		startThreads(new PositionProducerRunnable(_rgrid), 1);
		for (PointSetThreadHelper h : _threadHelpers) {
			startThreads(h, 1);
		}
		startThreads(new WriteOutRunnable(), numWriters);

		waitForThreadCompletion();
	}

	/**
	 * Splits up the point sets evenly among the available search threads
	 * (numSearchers)
	 * 
	 * @param _points
	 *            point set to split up
	 * @param numSearchers
	 *            the number of threads that will be handling searches
	 * @param attributeOfInterest
	 *            the attribute to focus on
	 */
	private void dividePointSet(IPointSet3D _points, int numSearchers,
			String attributeOfInterest) {
		int sizePerChunk = _points.size() / numSearchers;
		boolean first = true;
		int attributeIndex = _points.getAttributes().indexOf(
				attributeOfInterest);

		for (int i = 0; i < numSearchers; ++i) {
			BlockingQueue<PositionWorldCoordPair> in;

			// the first thread gets the position source from the reader thread
			if (first) {
				in = _positionQueueSource;
				first = false;
			} else {
				// the other threads use the previous thread's output queue as
				// their input queue
				in = _threadHelpers.get(i - 1).getOutQueue();
			}
			BlockingQueue<PositionWorldCoordPair> out = new ArrayBlockingQueue<PositionWorldCoordPair>(
					BUFFER_LEN);
			PointSetThreadHelper helper = new PointSetThreadHelper(in, out,
					attributeIndex);
			_threadHelpers.add(helper);

			for (int j = 0; j < sizePerChunk; ++j) {
				helper.add(_points.size() - 1,
						_points.remove(_points.size() - 1));
			}
		}

		PointSetThreadHelper helper = _threadHelpers
				.get(_threadHelpers.size() - 1);
		while (_points.size() > 0) {
			helper.add(_points.size() - 1, _points.remove(_points.size() - 1));
		}

	}

	class PositionProducerRunnable implements Runnable {

		IRegularGrid _rgrid;

		PositionProducerRunnable(IRegularGrid rgrid) {
			_rgrid = rgrid;
		}

		@Override
		public void run() {
			try {
				int[] pos;
				double[] wxyz = new double[3];
				GridSampleIterator giter = new GridSampleIterator(_rgrid);

				while (giter.hasNext()) {

					pos = giter.next();
					_rgrid.worldCoords(pos, wxyz);
					PositionWorldCoordPair pair = new PositionWorldCoordPair(
							pos, wxyz);
					if (!_positionQueueSource.offer(pair)) {

						_positionQueueSource.put(pair);

					}
				}// end while grid
				PositionWorldCoordPair stopProcessors = new PositionWorldCoordPair(
						null, null);
				stopProcessors.setMinDistance(Double.MIN_VALUE);
				_positionQueueSource.put(stopProcessors);
				System.out.println("Reader finished!");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	class WriteOutRunnable implements Runnable {

		long totalSamples = getTotalSamples();
		private final BlockingQueue<PositionWorldCoordPair> _completedFinds;

		WriteOutRunnable() {
			_completedFinds = _threadHelpers.get(_threadHelpers.size() - 1)
					.getOutQueue();
		}

		@Override
		public void run() {

			while (true) {
				try {
					PositionWorldCoordPair pos = _completedFinds.take();
					if (pos.getMinDistance() == Double.MIN_VALUE) {
						break;
					}
					float val = pos.getValue();
					synchronized (_rgrid) {
						try {
							_rgrid.putSample(val, pos.getPos());
						} catch (Exception e) {
							System.out
									.println("Caught Exception attempting to write position: "
											+ Arrays.toString(pos.getPos()));
						}
						_progressPercentage = 100f * currentSample++
								/ totalSamples;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Writer Finished!");
		}
	}

}
