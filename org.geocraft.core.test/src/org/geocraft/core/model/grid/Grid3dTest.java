package org.geocraft.core.model.grid;

import junit.framework.TestCase;

import org.geocraft.core.model.datatypes.CornerPointsSeries;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.geometry.GridGeometry3d;

public class Grid3dTest extends TestCase {
	private Grid3d _grid;
	private GridGeometry3d _geometry;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		int numRows = 10;
		int numCols = 10;
		Point3d[] pts = new Point3d[4];
		pts[0] = new Point3d(1948043, 10217509, 0);
		pts[1] = new Point3d(1928818, 10249840, 0);
		pts[2] = new Point3d(1966838, 10272075, 0);
		pts[3] = new Point3d(1986385, 10239636, 0);
		CornerPointsSeries cornerPoints = CornerPointsSeries.createDirect(pts,
				null);
		_geometry = new GridGeometry3d("", numRows, numCols, cornerPoints);
		_grid = new Grid3d("test", _geometry);
		int count = 0;
		float[][] values = new float[numRows][numCols];
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				values[i][j] = count += 4;
			}
		}
		_grid.setValues(values);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetValueAtXYDoubleDoubleBoolean() {
		assertEquals(4.0, _grid.getValueAtXY(1948043, 10217509), 0.001);
		assertEquals(4.0, _grid.getValueAtXY(1949043, 10219509, true), 0.001);
		assertEquals(20.485516, _grid.getValueAtXY(1949043, 10219509, false),
				0.001);

		assertEquals(352.0, _grid.getValueAtXY(1966838, 10260000), 0.001);
		assertEquals(339.20886, _grid.getValueAtXY(1966838, 10260000, false),
				0.001);
	}
}
