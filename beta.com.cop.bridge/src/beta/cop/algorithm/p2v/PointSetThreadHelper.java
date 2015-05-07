package beta.cop.algorithm.p2v;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;

import beta.cop.model.points.IMultiPoint;

public class PointSetThreadHelper implements Runnable {

	private final Map<Integer, IMultiPoint> _map = new HashMap<Integer, IMultiPoint>();
	private final BlockingQueue<PositionWorldCoordPair> _incoming;
	private final BlockingQueue<PositionWorldCoordPair> _outgoing;
	private boolean _amDone;
	private final int _attributeIndex;

	public PointSetThreadHelper(BlockingQueue<PositionWorldCoordPair> in,
			BlockingQueue<PositionWorldCoordPair> out, int attributeIndex) {
		_incoming = in;
		_outgoing = out;
		_attributeIndex = attributeIndex;
	}

	public void add(int index, IMultiPoint p) {
		_map.put(index, p);
	}

	@Override
	public void run() {
		PositionWorldCoordPair pos = null;
		while (true) {
			try {
				pos = _incoming.take();
				if (pos.getMinDistance() == Double.MIN_VALUE) {
					_outgoing.put(pos);
					break;
				}
				findNearestIndex(pos);
				_outgoing.put(pos);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		_amDone = true;
	}

	public boolean isDone() {
		return _amDone;
	}

	private double getDistance(double[] wxyz1, double[] wxyz2, int zdim,
			int ydim, int xdim) {
		double distance = (wxyz1[zdim] - wxyz2[zdim])
				* (wxyz1[zdim] - wxyz2[zdim]);
		distance += (wxyz1[xdim] - wxyz2[xdim]) * (wxyz1[xdim] - wxyz2[xdim]);
		distance += (wxyz1[ydim] - wxyz2[ydim]) * (wxyz1[ydim] - wxyz2[ydim]);
		distance = Math.sqrt(distance);

		return distance;
	}

	private double getDistance(double[] wxyz1, double[] wxyz2) {
		return getDistance(wxyz1, wxyz2, 2, 1, 0);
	}

	private void findNearestIndex(PositionWorldCoordPair pos) {
		double[] wxyz = pos.getWorld();
		for (Entry<Integer, IMultiPoint> e : _map.entrySet()) {
			double currentMinDist = pos.getMinDistance();
			double distance = getDistance(wxyz, e.getValue().getLocation());
			if (Double.compare(distance, currentMinDist) < 0) {
				pos.setMinDistance(distance);
				pos.setValue(e.getValue().getAttribute(_attributeIndex));
			}

		}

	}

	public BlockingQueue<PositionWorldCoordPair> getOutQueue() {
		return _outgoing;
	}

}
