package beta.cop.algorithm.p2v;

public class PositionWorldCoordPair {

	private final int[] _pos = new int[3];
	private final double[] _worldPos = new double[3];
	private double _minDistance = Double.MAX_VALUE;
	private float _attribute;

	public PositionWorldCoordPair(int[] pos, double[] wxyz) {
		if (pos != null && wxyz != null) {
			System.arraycopy(pos, 0, _pos, 0, pos.length);
			System.arraycopy(wxyz, 0, _worldPos, 0, _worldPos.length);
		}
	}

	public int[] getPos() {
		return _pos;
	}

	public double[] getWorld() {
		return _worldPos;
	}

	public double getMinDistance() {
		return _minDistance;
	}

	public void setMinDistance(double dist) {
		_minDistance = dist;
	}

	public void setValue(float attribute) {
		_attribute = attribute;
	}

	public float getValue() {
		return _attribute;
	}

}
