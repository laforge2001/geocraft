package org.geocraft.geomath.algorithm.utilities.areaofinterest;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.io.ExportTask;
import org.geocraft.core.io.IDatastoreAccessor;
import org.geocraft.core.io.IDatastoreAccessorService;
import org.geocraft.core.io.ImportTask;
import org.geocraft.core.model.aoi.MapPolygon;
import org.geocraft.core.model.aoi.MapPolygonAOI;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;


public class HorizonToAreaOfInterest extends StandaloneAlgorithm {

  public static final String OUTPUT_AOI_NAME = "Area of Interest Name";

  /** The input grid property. */
  protected final EntityProperty<Grid3d> _inputHorizon;

  public BooleanProperty _useCustomName;

  /** The output volume. */
  StringProperty _outputAOIName;

  public HorizonToAreaOfInterest() {
    _inputHorizon = addEntityProperty("Input Horizon", Grid3d.class);
    _outputAOIName = addStringProperty("Area of Interest Name", "aoi1");
  }

  @Override
  public void buildView(IModelForm modelForm) {
    FormSection inputSection = modelForm.addSection("Input");
    inputSection.addEntityComboField(_inputHorizon, Grid3d.class);
    FormSection outputSection = modelForm.addSection("Output");
    outputSection.addTextField(OUTPUT_AOI_NAME);
  }

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) throws CoreException {
    try {

      Grid3d inputHorizon = _inputHorizon.get();
      String aoiName = _outputAOIName.get();

      IDatastoreAccessor accessor = null;
      IDatastoreAccessorService datastoreAccessorService = ServiceProvider.getDatastoreAccessorService();
      boolean accessorFound = false;
      for (IDatastoreAccessor datastoreAccessor : datastoreAccessorService.getDatastoreAccessors()) {
        for (String entityClassName : datastoreAccessor.getSupportedEntityClassNames()) {
          if (entityClassName.equals("MapPolygonAOI")) {
            accessor = datastoreAccessor;
            accessorFound = true;
            break;
          }
        }
        if (accessorFound) {
          break;
        }
      }
      // create a new area of interest
      MapPolygonAOI aoi = new MapPolygonAOI(aoiName);

      // Initialize the contour generator based on the input horizon
      BitMap bitMap = new BitMap(inputHorizon);
      ContourGenerator cg = new ContourGenerator(bitMap);

      // dialate and erode in case we have rows or columns that are completely null
      if (bitMap.nullRowFound() || bitMap.nullColFound()) {
        cg.dialateErode();
      }

      // Generate the contours (coordinates in fractional row/col)
      cg.setBoundaryDistance(0.5f);
      cg.generateContours();

      GridGeometry3d geometry = inputHorizon.getGeometry();

      // transform the generated contours from row/col to X/Y and pass on to AOI
      for (int ci = 0; ci < cg.numGeneratedContours(); ci++) {
        double[] rowValues = cg.getRowValues(ci);
        double[] colValues = cg.getColumnValues(ci);
        double[] xVals = new double[rowValues.length];
        double[] yVals = new double[colValues.length];

        for (int v = 0; v < rowValues.length; v++) {
          double[] xy = geometry.transformRowColToXY(rowValues[v], colValues[v], false);
          xVals[v] = xy[0];
          yVals[v] = xy[1];
        }

        if (cg.getType(ci) == MapPolygon.Type.INCLUSIVE) {
          aoi.addInclusionPolygon(xVals, yVals);
        } else {
          aoi.addExclusionPolygon(xVals, yVals);
        }
      }

      if (accessor != null) {
        MapperModel mapperModel = accessor.createMapperModel(IOMode.OUTPUT);
        mapperModel.updateUniqueId(aoi.getDisplayName());
        ExportTask exportTask = accessor.createExportTask();
        exportTask.setMapperModel(mapperModel);
        exportTask.setEntity(aoi);
        TaskRunner.runTask(exportTask, "Writing AOI", TaskRunner.JOIN);

        ImportTask importTask = accessor.createImportTask();
        importTask.setMapperModel(mapperModel);
        TaskRunner.runTask(importTask, "Loading AOI", TaskRunner.JOIN);
      }
    } catch (RuntimeException e) {
      throw new CoreException(new Status(IStatus.ERROR, "CreateAreaOfInterest",
          "Problem creating the area of interest", e));
    }

  }

  @Override
  public void propertyChanged(String key) {
    if (key.equals(_inputHorizon.getKey()) && _inputHorizon.get() != null) {
      _outputAOIName.set(_inputHorizon.get().getDisplayName() + "_aoi");
    }
  }

  @Override
  public void validate(IValidation results) {
    if (_inputHorizon.get() == null) {
      results.error(_inputHorizon, "No input Horizon specified.");
    }

    // Validate the output name is non-zero length.
    if (_outputAOIName.isEmpty()) {
      results.error(_outputAOIName, "No area of interest name specified.");
    }

  }

  public List<Point3d> getPointsByColumn(final Grid3d inputGrid, final IProgressMonitor monitor) {

    GridGeometry3d geometry = inputGrid.getGeometry();
    int numRows = inputGrid.getNumRows();
    int numCols = inputGrid.getNumColumns();

    List<Point3d> outputPoints = new ArrayList<Point3d>();
    List<Integer> edgeRows = new ArrayList<Integer>();
    List<Integer> edgeCols = new ArrayList<Integer>();

    // Save the the minimum and maximum row for each column
    int[] minRows = new int[numCols];
    int[] maxRows = new int[numCols];

    // determine which columns may be all null
    boolean[] nullCols = new boolean[numCols];
    for (int col = 0; col < numCols && !monitor.isCanceled(); col++) {

      // initialize the null column flag to false
      nullCols[col] = false;

      // Determine the first row that is not null
      // (Also determine the columns that may be all null)
      boolean pointFound = false;
      int row = 0;
      minRows[col] = 0;
      while (!pointFound && row < numRows) {
        if (!inputGrid.isNull(row, col)) {
          minRows[col] = row;
          pointFound = true;
        }
        row++;
      }

      // Determine the last row that is not null
      maxRows[col] = numRows - 1;
      if (pointFound) {
        row = numRows - 1;
        pointFound = false;
        while (!pointFound && row >= 0) {
          if (!inputGrid.isNull(row, col)) {
            maxRows[col] = row;
            pointFound = true;
          }
          row--;
        }

        // if no point found then the current column is null
      } else {
        nullCols[col] = true;
      }
    }

    // determine which rows may be all null
    boolean[] nullRows = new boolean[numRows];
    for (int row = 0; row < numRows && !monitor.isCanceled(); row++) {
      // initialize the null row flag to false
      nullRows[row] = false;

      // Determine the rows that may be all null
      boolean pointFound = false;
      int col = 0;
      while (!pointFound && col < numCols) {
        if (!inputGrid.isNull(row, col)) {
          pointFound = true;
        }
        col++;
      }

      // if no point found then the current row is null
      if (!pointFound) {
        nullRows[row] = true;
      }
    }

    // Look at each point in grid to see if it is an edge point
    for (int col = 0; col < numCols && !monitor.isCanceled(); col++) {

      // look at each row      
      for (int row = 0; row < numRows; row++) {

        // If point is not null then determine if point is an edge point
        if (!inputGrid.isNull(row, col)) {

          // Determine a start and end rows & columns for points that surround the current point
          int startRow = row - 1;
          if (startRow < 0) {
            startRow = 0;
          } else {
            // Skip row that is all null
            if (nullRows[startRow]) {
              startRow = row - 2;
              // make sure that the current start row has points in it
              if (startRow < 0 || nullRows[startRow]) {
                startRow = row - 1;
              }
            }
          }

          int endRow = row + 1;
          if (endRow >= numRows) {
            endRow = numRows - 1;
          } else {
            // Skip row that is all null
            if (nullRows[endRow]) {
              endRow = row + 2;
              // make sure that the current end row has points in it
              if (endRow >= numRows || nullRows[endRow]) {
                endRow = row + 1;
              }
            }
          }

          int startCol = col - 1;
          if (startCol < 0) {
            startCol = 0;
          } else {
            // Skip column that is all null
            if (nullCols[startCol]) {
              startCol = col - 2;
              // make sure that the current start column has points in it
              if (startCol < 0 || nullCols[startCol]) {
                startCol = col - 1;
              }
            }
          }

          int endCol = col + 1;
          if (endCol >= numCols) {
            endCol = numCols - 1;
          } else {
            // Skip column that is all null
            if (nullCols[endCol]) {
              endCol = col + 2;
              // make sure that the current end column has points in it
              if (endCol >= numCols || nullCols[endCol]) {
                endCol = col + 1;
              }
            }
          }

          // Determine if a surrounding point has a null then edge point found
          boolean edgePoint = false;
          for (int i1 = startRow; i1 <= endRow && !edgePoint; i1++) {
            // Skip rows that may be all null
            if (!nullRows[i1]) {
              for (int i2 = startCol; i2 <= endCol && !edgePoint; i2++) {
                // Skip columns that may be all null
                if (!nullCols[i2]) {
                  if (inputGrid.isNull(i1, i2)) {
                    edgePoint = true;
                  }
                }
              }
            }
          }

          // Save edge point
          if (edgePoint) {

            // Save each unique column
            boolean colFound = false;
            int cIndx = 0;
            int insertIndx = 0;
            while (cIndx < edgeCols.size() && !colFound) {
              if (col == edgeCols.get(cIndx)) {
                colFound = true;
              } else if (edgeCols.get(cIndx) < col) {
                insertIndx = cIndx;
              }

              // Increment the index
              if (!colFound) {
                cIndx++;
              }
            }

            // determine if we need to modify the insert index
            if (colFound) {
              while (cIndx < edgeCols.size() && edgeCols.get(cIndx) == col) {
                insertIndx = cIndx;
                cIndx++;
              }
            }

            // Save new edge point
            if (insertIndx + 1 < edgeCols.size()) {
              edgeRows.add(insertIndx, row);
              edgeCols.add(insertIndx, col);
            } else {
              edgeRows.add(row);
              edgeCols.add(col);
            }
          }
        }
      }

      // Update the progress monitor.
      monitor.worked(1);
      monitor.subTask("Completed column " + col);
    }

    // Now add the minimum edge points
    int numEdgePoints = 0;
    for (int i1 = 0; i1 < edgeCols.size(); i1++) {
      int row = edgeRows.get(i1);
      int col = edgeCols.get(i1);

      // Save point if it is closer the minimum row
      boolean saveFlag = false;
      if (row - minRows[col] <= maxRows[col] - row) {
        saveFlag = true;
      }

      // Save point if the save flag has been set
      if (saveFlag) {
        // Save the current X,Y point
        double[] xy = geometry.transformRowColToXY(row, col);
        Point3d point = new Point3d(xy[0], xy[1], inputGrid.getValueAtRowCol(row, col));
        outputPoints.add(point);
        numEdgePoints++;
      }
    }

    // Update the progress monitor.
    monitor.worked(1);
    monitor.subTask("Added minimum edge points. numEdgePoints=" + numEdgePoints);

    // Now add the maximum edge points
    for (int i1 = edgeCols.size() - 1; i1 >= 0; i1--) {
      int row = edgeRows.get(i1);
      int col = edgeCols.get(i1);

      // Save point if it is closer the maximum row
      boolean saveFlag = false;
      if (maxRows[col] - row < row - minRows[col]) {
        saveFlag = true;
      }

      // Save point if the save flag has been set
      if (saveFlag) {
        // Save the current X,Y point
        double[] xy = geometry.transformRowColToXY(row, col);
        Point3d point = new Point3d(xy[0], xy[1], inputGrid.getValueAtRowCol(row, col));
        outputPoints.add(point);
        numEdgePoints++;
      }
    }

    // Update the progress monitor.
    monitor.worked(1);
    monitor.subTask("Added maximum edge points. numEdgePoints=" + numEdgePoints);

    return outputPoints;
  }

  public List<Point3d> getPointsByRow(final Grid3d inputGrid, final IProgressMonitor monitor) {

    GridGeometry3d geometry = inputGrid.getGeometry();
    int numRows = inputGrid.getNumRows();
    int numCols = inputGrid.getNumColumns();

    List<Point3d> outputPoints = new ArrayList<Point3d>();
    List<Integer> edgeRows = new ArrayList<Integer>();
    List<Integer> edgeCols = new ArrayList<Integer>();

    // Save the the minimum and maximum column for each row
    int[] minCols = new int[numRows];
    int[] maxCols = new int[numRows];

    // determine which rows may be all null
    boolean[] nullRows = new boolean[numRows];
    for (int row = 0; row < numRows && !monitor.isCanceled(); row++) {

      // initialize the null row flag to false
      nullRows[row] = false;

      // Determine the first column that is not null
      // (Also determine the rows that may be all null)
      boolean pointFound = false;
      int col = 0;
      minCols[row] = 0;
      while (!pointFound && col < numCols) {
        if (!inputGrid.isNull(row, col)) {
          minCols[row] = col;
          pointFound = true;
        }
        col++;
      }

      // Determine the last column that is not null
      maxCols[row] = numRows - 1;
      if (pointFound) {
        col = numCols - 1;
        pointFound = false;
        while (!pointFound && col >= 0) {
          if (!inputGrid.isNull(row, col)) {
            maxCols[row] = col;
            pointFound = true;
          }
          col--;
        }

        // if no point found then the current row is null
      } else {
        nullRows[row] = true;
      }
    }

    // determine which columns may be all null
    boolean[] nullCols = new boolean[numCols];
    for (int col = 0; col < numCols && !monitor.isCanceled(); col++) {
      // initialize the null col flag to false
      nullCols[col] = false;

      // Determine the columns that may be all null
      boolean pointFound = false;
      int row = 0;
      while (!pointFound && row < numRows) {
        if (!inputGrid.isNull(row, col)) {
          pointFound = true;
        }
        row++;
      }

      // if no point found then the current column is null
      if (!pointFound) {
        nullCols[row] = true;
      }
    }

    // Look at each point in grid to see if it is an edge point
    for (int row = 0; row < numRows && !monitor.isCanceled(); row++) {

      // look at each column      
      for (int col = 0; col < numCols; col++) {

        // If point is not null then determine if point is an edge point
        if (!inputGrid.isNull(row, col)) {

          // Determine a start and end rows & columns for points that surround the current point
          int startRow = row - 1;
          if (startRow < 0) {
            startRow = 0;
          } else {
            // Skip row that is all null
            if (nullRows[startRow]) {
              startRow = row - 2;
              // make sure that the current start row has points in it
              if (startRow < 0 || nullRows[startRow]) {
                startRow = row - 1;
              }
            }
          }

          int endRow = row + 1;
          if (endRow >= numRows) {
            endRow = numRows - 1;
          } else {
            // Skip row that is all null
            if (nullRows[endRow]) {
              endRow = row + 2;
              // make sure that the current end row has points in it
              if (endRow >= numRows || nullRows[endRow]) {
                endRow = row + 1;
              }
            }
          }

          int startCol = col - 1;
          if (startCol < 0) {
            startCol = 0;
          } else {
            // Skip column that is all null
            if (nullCols[startCol]) {
              startCol = col - 2;
              // make sure that the current start column has points in it
              if (startCol < 0 || nullCols[startCol]) {
                startCol = col - 1;
              }
            }
          }

          int endCol = col + 1;
          if (endCol >= numCols) {
            endCol = numCols - 1;
          } else {
            // Skip column that is all null
            if (nullCols[endCol]) {
              endCol = col + 2;
              // make sure that the current end column has points in it
              if (endCol >= numCols || nullCols[endCol]) {
                endCol = col + 1;
              }
            }
          }

          // Determine if a surrounding point has a null then edge point found
          boolean edgePoint = false;
          for (int i1 = startRow; i1 <= endRow && !edgePoint; i1++) {
            // Skip rows that may be all null
            if (!nullRows[i1]) {
              for (int i2 = startCol; i2 <= endCol && !edgePoint; i2++) {
                // Skip columns that may be all null
                if (!nullCols[i2]) {
                  if (inputGrid.isNull(i1, i2)) {
                    edgePoint = true;
                  }
                }
              }
            }
          }

          // Save edge point
          if (edgePoint) {

            // Save each unique row
            boolean rowFound = false;
            int rIndx = 0;
            int insertIndx = 0;
            while (rIndx < edgeRows.size() && !rowFound) {
              if (row == edgeRows.get(rIndx)) {
                rowFound = true;
              } else if (edgeRows.get(rIndx) < row) {
                insertIndx = rIndx;
              }

              // Increment the index
              if (!rowFound) {
                rIndx++;
              }
            }

            // determine if we need to modify the insert index
            if (rowFound) {
              while (rIndx < edgeRows.size() && edgeRows.get(rIndx) == row) {
                insertIndx = rIndx;
                rIndx++;
              }
            }

            // Save new edge point
            if (insertIndx + 1 < edgeRows.size()) {
              edgeRows.add(insertIndx, row);
              edgeCols.add(insertIndx, col);
            } else {
              edgeRows.add(row);
              edgeCols.add(col);
            }
          }
        }
      }

      // Update the progress monitor.
      monitor.worked(1);
      monitor.subTask("Completed row " + row);
    }

    // Now add the minimum edge points
    int numEdgePoints = 0;
    for (int i1 = 0; i1 < edgeRows.size(); i1++) {
      int row = edgeRows.get(i1);
      int col = edgeCols.get(i1);

      // Save point if it is closer the minimum column
      boolean saveFlag = false;
      if (col - minCols[row] <= maxCols[row] - col) {
        saveFlag = true;
      }

      // Save point if the save flag has been set
      if (saveFlag) {
        // Save the current X,Y point
        double[] xy = geometry.transformRowColToXY(row, col);
        Point3d point = new Point3d(xy[0], xy[1], inputGrid.getValueAtRowCol(row, col));
        outputPoints.add(point);
        numEdgePoints++;
      }
    }

    // Update the progress monitor.
    monitor.worked(1);
    monitor.subTask("Added minimum edge points. numEdgePoints=" + numEdgePoints);

    // Now add the maximum edge points
    for (int i1 = edgeRows.size() - 1; i1 >= 0; i1--) {
      int row = edgeRows.get(i1);
      int col = edgeCols.get(i1);

      // Save point if it is closer the maximum row
      boolean saveFlag = false;
      if (maxCols[row] - col < col - minCols[row]) {
        saveFlag = true;
      }

      // Save point if the save flag has been set
      if (saveFlag) {
        // Save the current X,Y point
        double[] xy = geometry.transformRowColToXY(row, col);
        Point3d point = new Point3d(xy[0], xy[1], inputGrid.getValueAtRowCol(row, col));
        outputPoints.add(point);
        numEdgePoints++;
      }
    }

    // Update the progress monitor.
    monitor.worked(1);
    monitor.subTask("Added maximum edge points. numEdgePoints=" + numEdgePoints);

    return outputPoints;
  }
}
