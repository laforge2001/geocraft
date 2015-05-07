package org.geocraft.io.asciigrid;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.geocraft.core.common.util.Utilities;
import org.geocraft.core.model.datatypes.OnsetType;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.io.asciigrid.AsciiHorizonMapperModel.IndexType;

public class AsciiHorizonMapperTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
	}

	public void testFromReadStore() {

		String fileName = Utilities.getPath("org.geocraft.io.asciigrid.test")
				+ "data/hData1.txt";
		File file = new File(fileName);
		AsciiHorizonMapperModel model = AsciiFileSelector
				.createMapperModelNoRestore(file);

		Grid3d grid = null;

		AsciiHorizonMapperModel mapper = model;
		if (mapper == null) {
			throw new RuntimeException(
					"The task for loading the Ascii file has not been initialized.");
		}

		int startingLineNum = mapper.getStartingLineNum();

		IndexType indexType = mapper.getIndexType();

		Unit dataUnits = mapper.getDataUnits();

		Unit xyUnits = mapper.getXyUnits();

		OnsetType onsetType = mapper.getOnsetType();

		int numOfHorizons = mapper.getNumOfHorizons();
		List<String> horizonNames = new ArrayList<String>();
		horizonNames.add(mapper.getH1Name());
		if (numOfHorizons > 1) {
			horizonNames.add(mapper.getH2Name());
		}
		if (numOfHorizons > 2) {
			horizonNames.add(mapper.getH3Name());
		}
		if (numOfHorizons > 3) {
			horizonNames.add(mapper.getH4Name());
		}
		if (numOfHorizons > 4) {
			horizonNames.add(mapper.getH5Name());
		}

		int numOfCols = numOfHorizons + 2;
		int[] colNumbers = new int[numOfCols];
		colNumbers[0] = mapper.getXcolumnNum();
		colNumbers[1] = mapper.getYcolumnNum();
		for (int i1 = 0; i1 < numOfHorizons; i1++) {
			int horNum = i1 + 1;
			int horCol = mapper.getH1ColumnNum();
			if (horNum == 2) {
				horCol = mapper.getH2ColumnNum();
			} else if (horNum == 3) {
				horCol = mapper.getH3ColumnNum();
			} else if (horNum == 4) {
				horCol = mapper.getH4ColumnNum();
			} else if (horNum == 5) {
				horCol = mapper.getH5ColumnNum();
			}
			colNumbers[i1 + 2] = horCol;
		}

		// Generate the input file name
		String inputDirectory = mapper.getDirectory();
		String inputFileName = mapper.getFileName();

		// determine the output grid geometry.
		double x0 = mapper.getXorigin();
		double y0 = mapper.getYorigin();
		double dx = mapper.getColSpacing();
		double dy = mapper.getRowSpacing();
		int nX = mapper.getNumOfColumns();
		int nY = mapper.getNumOfRows();
		double angle = mapper.getPrimaryAngle();
		float nullValue = mapper.getNullValue();

		int numWorkItems = numOfHorizons * 5;

		// Initialize the implementation reading and writing of ascii files
		AbstractAsciiHorizonReader reader = new AsciiHorizonReader(mapper);
		AbstractAsciiHorizonWriter writer = new AsciiHorizonWriter(mapper);

		// Generate the input file name
		String inputFilePath = inputDirectory + File.separator + inputFileName
				+ AsciiFileConstants.TEXT_FILE_EXTN;

		// read the file
		float[][] results = null;
		try {
			results = reader.readAsciiHorizon(inputFilePath, startingLineNum,
					numOfHorizons, colNumbers);
		} catch (IOException ex) {
			fail();
		}

		// Determine the number of lines in file
		int numOfLines = reader.getNumOfLines();

		// Create each of the grids
		int i3 = 0;
		for (String gridName : horizonNames) {

			// update the mapper
			AsciiHorizonMapperModel mapperModel = new AsciiHorizonMapperModel();

			mapperModel.setDataUnits(dataUnits);
			mapperModel.setDirectory(inputDirectory);
			mapperModel.setFileName(gridName);
			mapperModel.setOrientation(GridOrientation.X_IS_COLUMN);
			mapperModel.setIndexType(indexType);
			mapperModel.setXyUnits(xyUnits);
			mapperModel.setOnsetType(onsetType);
			mapperModel.setXorigin(x0);
			mapperModel.setYorigin(y0);
			mapperModel.setColSpacing(dx);
			mapperModel.setRowSpacing(dy);
			mapperModel.setNumOfRows(nY);
			mapperModel.setNumOfColumns(nX);
			mapperModel.setPrimaryAngle(angle);
			mapperModel.setNullValue(nullValue);
			mapperModel.setH1Name(gridName);

			// Create the ModSpec grid mapper.
			AsciiHorizonMapper gridMapper = new AsciiHorizonMapper(mapperModel);

			// Create the grid entity.
			String gridID = gridMapper.getUniqueID();
			grid = new Grid3d(gridName, gridMapper);

			// Update the grid geometry.
			GridGeometry3d geometry = null;
			try {
				geometry = reader.updateGridGeometry(grid, x0, y0, dx, dy, nX,
						nY, angle, nullValue);
			} catch (IOException ex) {
				fail();
			}

			double[][] gridVals = new double[nY][nX];
			// initialize grid Values to null
			for (int i1 = 0; i1 < nY; i1++) {
				for (int i2 = 0; i2 < nX; i2++) {
					gridVals[i1][i2] = nullValue;
				}
			}

			// Save each value read from the file
			for (int i4 = 0; i4 < numOfLines; i4++) {

				// Transform x & y value to a row & column
				float xVal = results[0][i4];
				float yVal = results[1][i4];
				double[] rowcol = grid.getGeometry().transformXYToRowCol(xVal,
						yVal, true);
				int row = (int) Math.round(rowcol[0]);
				int col = (int) Math.round(rowcol[1]);

				// save value into the row & column
				if (row >= 0 && row < nY && col >= 0 && col < nX) {
					gridVals[row][col] = results[i3 + 2][i4];
				}
			}

			// Update the grid.
			String gridPath = inputDirectory + File.separator + gridName
					+ AsciiFileConstants.TEXT_FILE_EXTN;
			reader.updateGrid(grid, gridPath, nX, nY, nullValue, gridVals);
			i3++;

			// Write out an ascii
			String outputName = gridName;
			outputName = gridName + AsciiFileConstants.TEXT_FILE_EXTN;
			writer.writeHorizon(grid, geometry, nullValue, inputDirectory,
					outputName);

		}

		assertEquals(grid.getNullValue(),
				grid.getValueAtXY(872.4648, 8871.8604), 0.001);
	}

}
