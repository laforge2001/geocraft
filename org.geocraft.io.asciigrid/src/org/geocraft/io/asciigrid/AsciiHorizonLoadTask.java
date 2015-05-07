package org.geocraft.io.asciigrid;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.geocraft.core.io.ImportTask;
import org.geocraft.core.model.datatypes.OnsetType;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.ISpecification;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.io.asciigrid.AsciiHorizonMapperModel.IndexType;


public class AsciiHorizonLoadTask extends ImportTask {

  /** The mapper model. */
  protected AsciiHorizonMapperModel _mapperModel;

  /**
   * The default constructor.
   * @param parent the parent composite (used for pop-up dialogs).
   * @param mapperModels the array of models for grid properties.
   */
  public AsciiHorizonLoadTask() {
    // The no argument constructor for OSGI.
  }

  @Override
  public void setMapperModel(final MapperModel mapperModel) {
    _mapperModel = (AsciiHorizonMapperModel) mapperModel;
  }

  @Override
  public void run(final ILogger logger, final IProgressMonitor progress, IRepository repository) throws CoreException {
    AsciiHorizonMapperModel mapper = _mapperModel;
    if (mapper == null) {
      throw new RuntimeException("The task for loading the Ascii file has not been initialized.");
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
    progress.beginTask("Loading Ascii file: " + inputFileName, numWorkItems);

    // Initialize the implementation reading and writing of ascii files
    AbstractAsciiHorizonReader reader = new AsciiHorizonReader(mapper);
    AbstractAsciiHorizonWriter writer = new AsciiHorizonWriter(mapper);

    // Generate the input file name
    String inputFilePath = inputDirectory + File.separator + inputFileName;

    // read the file
    float[][] results = null;
    try {
      results = reader.readAsciiHorizon(inputFilePath, startingLineNum, numOfHorizons, colNumbers);
    } catch (IOException ex) {
      throw new CoreException(new Status(IStatus.WARNING, AsciiFileConstants.PLUGIN_ID,
          "Unable to read the Ascii Horizon", ex));
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
      progress.subTask("Creating datastore mapper");
      AsciiHorizonMapper gridMapper = new AsciiHorizonMapper(mapperModel);
      progress.worked(1);

      // Create the grid entity.
      progress.subTask("Creating grid entity");
      String gridID = gridMapper.getUniqueID();
      Grid3d grid = new Grid3d(gridName, gridMapper);
      progress.worked(1);

      // Check if grid already exists in repository.
      progress.subTask("Checking repository");
      ISpecification filter = new TypeSpecification(Grid3d.class);
      Map<String, Object> map = repository.get(filter);
      for (Object object : map.values()) {
        Grid3d temp = (Grid3d) object;
        if (gridID.equals(temp.getUniqueID())) {
          String errorMessage = "Grid already exists in repository: " + grid.getDisplayName();
          System.out.println(errorMessage);
          Status status = new Status(IStatus.ERROR, "org.geocraft.algorithm.utilities.asciihorizonimport",
              errorMessage.toString());
          //          progress.done();
          //          throw new CoreException(status);
          break;
        }
      }
      progress.worked(1);

      // Update the grid geometry.
      progress.subTask("Updating grid");
      GridGeometry3d geometry = null;
      try {
        geometry = reader.updateGridGeometry(grid, x0, y0, dx, dy, nX, nY, angle, nullValue);
      } catch (IOException ex) {
        throw new CoreException(new Status(IStatus.WARNING, AsciiFileConstants.PLUGIN_ID,
            "Unable to update the GridGeometry", ex));
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
        double[] rowcol = grid.getGeometry().transformXYToRowCol(xVal, yVal, true);
        int row = (int) Math.round(rowcol[0]);
        int col = (int) Math.round(rowcol[1]);

        // save value into the row & column
        if (row >= 0 && row < nY && col >= 0 && col < nX) {
          gridVals[row][col] = results[i3 + 2][i4];
        }
      }

      // Update the grid.
      String gridPath = inputDirectory + File.separator + gridName + AsciiFileConstants.TEXT_FILE_EXTN;
      reader.updateGrid(grid, gridPath, nX, nY, nullValue, gridVals);
      i3++;
      progress.worked(1);

      // Write out an ascii
      String outputName = gridName;
      outputName = gridName + AsciiFileConstants.TEXT_FILE_EXTN;
      writer.writeHorizon(grid, geometry, nullValue, inputDirectory, outputName);

      // Add the grid to the repository.
      progress.subTask("Adding grid to repository");
      repository.add(grid);
      progress.worked(1);
    }

    // Task is done.
    progress.done();
  }
}
