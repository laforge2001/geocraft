package org.geocraft.io.asciigrid;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.geocraft.core.common.util.Utilities;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.geomath.algorithm.util.asciiexport.AsciiExportRegistry;
import org.geocraft.geomath.algorithm.util.parameters.DoubleParameter;
import org.geocraft.geomath.algorithm.util.parameters.FloatParameter;
import org.geocraft.geomath.algorithm.util.parameters.IParameter;
import org.geocraft.geomath.algorithm.util.parameters.IntegerParameter;
import org.geocraft.geomath.algorithm.util.parameters.StringParameter;


/**
 * Exports a Horizon data into an ascii file 
 */
public abstract class AbstractAsciiHorizonWriter {

  /** The model of mapper parameters. */
  protected final AsciiHorizonMapperModel _model;

  public enum AsciiFormatType {
    HORIZON("Horizon Ascii data"),
    OTHER("Other types of data");

    private final String _displayName;

    AsciiFormatType(final String displayName) {
      _displayName = displayName;
    }

    @Override
    public String toString() {
      return _displayName;
    }
  }

  /** The ASCII export strategy */
  private ExportHorizonAsciiData _exportStrategy;

  /** The output name. */
  private String _outputFilePath;

  public final static String ONSET_TYPE = "onsetType";

  public final static String ONSET_TYPE_ID = "ONSET_TYPE";

  public final static String X_ORIGIN = "xOrigin";

  public final static String X_ORIGIN_ID = "X_ORIGIN";

  public final static String Y_ORIGIN = "yOrigin";

  public final static String Y_ORIGIN_ID = "Y_ORIGIN";

  public final static String COLUMN_SPACING = "columnSpacing";

  public final static String COLUMN_SPACING_ID = "COLUMN_SPACING";

  public final static String ROW_SPACING = "rowSpacing";

  public final static String ROW_SPACING_ID = "ROW_SPACING";

  public final static String NUM_OF_COLUMNS = "numOfColumns";

  public final static String NUM_OF_COLUMNS_ID = "NUM_OF_COLUMNS";

  public final static String NUM_OF_ROWS = "numOfRows";

  public final static String NUM_OF_ROWS_ID = "NUM_OF_ROWS";

  public final static String PRIMARY_ANGLE = "primaryAngle";

  public final static String PRIMARY_ANGLE_ID = "PRIMARY_ANGLE";

  public final static String NULL_VALUE = "nullValue";

  public final static String NULL_VALUE_ID = "NULL_VALUE";

  private boolean _exitFlag = false;

  public AbstractAsciiHorizonWriter(final AsciiHorizonMapperModel model) {
    _model = model;
  }

  // This routine does just the basics in writing one ascii horizon
  public void writeHorizon(final Grid3d horizon, final GridGeometry3d geometry, final float nullValue,
      final String dirName, final String outputFileName) {

    // Initialize Horizon
    List<Grid3d> horizons = new ArrayList<Grid3d>();
    horizons.add(horizon);

    // Area of interest not used in this basic horizon data write
    AreaOfInterest areaOfInterest = null;

    // Initialize parameter information
    AsciiFormatType asciiFormat = AsciiFormatType.HORIZON;
    List<String> stringParameters = new ArrayList<String>();
    List<String> parameterHdrs = new ArrayList<String>();

    String asciiFormatString = asciiFormat.toString();

    // Determine the header parameters based on the geometry
    List<IParameter> globalHeaderParameters = determineGlobalHeaders(geometry, nullValue);

    // Start the processing task.
    // (The User will not be prompted if the file exists)
    boolean overWriteWarning = false;
    initExportOfHorizonData(horizons, areaOfInterest, dirName, outputFileName, asciiFormatString,
        globalHeaderParameters, stringParameters, parameterHdrs, overWriteWarning);

    // Run the process
    // (Don't run the process if the exit flag has been set)
    if (!_exitFlag) {
      runExportOfHorizonData(geometry, areaOfInterest);
    }
  }

  // Write horizon data to an ascii file
  public void writeHorizonData(final List<Grid3d> horizons, final AreaOfInterest areaOfInterest, final String dirName,
      final String outputFileName, final List<String> stringParameters, final List<String> parameterHdrs) {

    // Initialize parameter information
    AsciiFormatType asciiFormat = AsciiFormatType.HORIZON;
    String asciiFormatString = asciiFormat.toString();

    // Add global header parameters based on the geometry
    Grid3d horizon1 = horizons.get(0);
    GridGeometry3d geometry = horizon1.getGeometry();
    List<IParameter> globalHeaderParameters = determineGlobalHeaders(geometry, horizon1.getNullValue());

    // Start the processing task.
    // (The User will be prompted if the file exists)
    boolean overWriteWarning = true;
    initExportOfHorizonData(horizons, areaOfInterest, dirName, outputFileName, asciiFormatString,
        globalHeaderParameters, stringParameters, parameterHdrs, overWriteWarning);

    // Run the process
    // (Don't run the process if the exit flag has been set)
    if (!_exitFlag) {
      runExportOfHorizonData(geometry, areaOfInterest);
    }
  }

  /**
   * Constructs the poststack task.
   * @param inputVolume the input poststack.
   * @param aoi the area of interest.
   * @param smoothWindow the smoothing window.
   * @param outputName the output name.
   * @param outputComments the output comments.
   * @param globalHeaderParameters the global header parameters.
   */
  public void initExportOfHorizonData(final List<Grid3d> horizons, final AreaOfInterest aoi, final String dirName,
      final String outputName, final String outputFormat, final List<IParameter> globalHeaderParameters,
      final List<String> stringParameters, final List<String> parameterHdrs, final boolean overWriteWarning) {

    // Initialize the exit flag
    _exitFlag = false;

    Class exportClass = AsciiExportRegistry.getInstance().find(outputFormat);
    try {
      _exportStrategy = (ExportHorizonAsciiData) exportClass.newInstance();
    } catch (Exception ex) {
      throw new RuntimeException(ex.getMessage());
    }

    // Generate the directory name
    String outputDirectory = dirName;
    if (dirName.equals("currentDirectory")) {
      outputDirectory = Utilities.getWorkingDirectory();
    }

    // Generate the output file name, adding the appropriate extension if necessary.
    if (outputName.startsWith("/")) {
      _outputFilePath = outputName;
    } else {
      _outputFilePath = outputDirectory + File.separator + outputName;
    }
    String extension = "." + _exportStrategy.getFileExtension();
    if (!_outputFilePath.endsWith(extension)) {
      _outputFilePath += extension;
    }

    File file = new File(_outputFilePath);
    if (file.exists() && overWriteWarning) {
      String message = "The output file \'" + _outputFilePath + "\' already exists.\nDo you want to overwrite?";
      int rtn = JOptionPane.showConfirmDialog(null, message, "File Exists", JOptionPane.YES_OPTION,
          JOptionPane.WARNING_MESSAGE);
      if (rtn == JOptionPane.NO_OPTION) {
        _exitFlag = true;
        return;
      }
    }
    _exportStrategy.setGlobalHeaders(globalHeaderParameters);
    _exportStrategy.setHorizonsWithData(horizons);
    _exportStrategy.setStringParameters(stringParameters, parameterHdrs);
  }

  public void runExportOfHorizonData(final GridGeometry3d geometry, final AreaOfInterest aoi) {

    int numRows = geometry.getNumRows();
    int numCols = geometry.getNumColumns();

    // Create the output ASCII file.
    File outputFile = new File(_outputFilePath);
    PrintWriter writer;
    try {
      writer = new PrintWriter(outputFile);
    } catch (FileNotFoundException ex) {
      throw new RuntimeException(ex.getMessage());
    }
    writer.write(_exportStrategy.formatGlobalHeaders());

    int nVals = 0;

    // Loop over the rows.
    for (int row = 0; row < numRows; row++) {

      // Loop over the columns.
      for (int col = 0; col < numCols; col++) {

        // Determine the x,y coordinates.
        double[] xy = geometry.transformRowColToXY(row, col);
        double x = xy[0];
        double y = xy[1];

        // If no AOI specified, or the coordinate is contained in the
        // AOI, then continue to the next test.
        if (aoi == null || aoi.contains(x, y)) {

          // Write the data to the output file.
          writer.write(_exportStrategy.formatHorizonValues(x, y, nVals));
          nVals++;
        }
      }
    }

    // Close the output file.
    writer.close();
  }

  public abstract double convertUnit(double fromUnit);

  // This routine determines the global header parameters
  public List<IParameter> determineGlobalHeaders(final GridGeometry3d geometry, final float nullValue) {

    // initialize the parameters
    List<IParameter> globalHeaderParameters = new ArrayList<IParameter>();

    // Calculate the header parameters based on the geometry
    Point3d[] points = geometry.getCornerPoints().getCopyOfPoints();
    for (int i = 0; i < points.length; i++) {
      double x = points[i].getX();
      double y = points[i].getY();
      double z = points[i].getZ();
      x = convertUnit(x);
      y = convertUnit(y);
      points[i] = new Point3d(x, y, z);
    }
    double angle = Math.atan2(points[1].getY() - points[0].getY(), points[1].getX() - points[0].getX());
    double dx01 = points[1].getX() - points[0].getX();
    double dy01 = points[1].getY() - points[0].getY();
    dx01 /= geometry.getNumColumns() - 1;
    dy01 /= geometry.getNumColumns() - 1;
    double colSpacing = Math.sqrt(dx01 * dx01 + dy01 * dy01);
    double dx03 = points[3].getX() - points[0].getX();
    double dy03 = points[3].getY() - points[0].getY();
    dx03 /= geometry.getNumRows() - 1;
    dy03 /= geometry.getNumRows() - 1;
    double rowSpacing = Math.sqrt(dx03 * dx03 + dy03 * dy03);

    // Add global header parameters
    globalHeaderParameters.add(new DoubleParameter(X_ORIGIN_ID, X_ORIGIN, points[0].getX()));
    globalHeaderParameters.add(new DoubleParameter(Y_ORIGIN_ID, Y_ORIGIN, points[0].getY()));
    globalHeaderParameters.add(new StringParameter(ONSET_TYPE_ID, ONSET_TYPE, _model.getOnsetType().toString()));
    globalHeaderParameters.add(new DoubleParameter(COLUMN_SPACING_ID, COLUMN_SPACING, colSpacing));
    globalHeaderParameters.add(new DoubleParameter(ROW_SPACING_ID, ROW_SPACING, rowSpacing));
    globalHeaderParameters.add(new IntegerParameter(NUM_OF_COLUMNS_ID, NUM_OF_COLUMNS, geometry.getNumColumns()));
    globalHeaderParameters.add(new IntegerParameter(NUM_OF_ROWS_ID, NUM_OF_ROWS, geometry.getNumRows()));
    globalHeaderParameters.add(new DoubleParameter(PRIMARY_ANGLE_ID, PRIMARY_ANGLE, Math.toDegrees(angle)));
    globalHeaderParameters.add(new FloatParameter(NULL_VALUE_ID, NULL_VALUE, nullValue));

    return globalHeaderParameters;
  }
}
