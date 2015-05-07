/*
 * Copyright (C) ConocoPhillips 2009 All Rights Reserved.
 */
package org.geocraft.geomath.algorithm.horizon.convert;


import java.awt.geom.Point2D;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.factory.model.Grid3dFactory;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.property.DoubleProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;


public class ConvertHorizon extends StandaloneAlgorithm {

  // UI TYPES
  // Input section
  /** Input horizon */
  private EntityProperty<Grid3d> _inputHorizon;

  /** The input xOrigin */
  private DoubleProperty _inputXOrigin;

  /** The input yOrigin */
  private DoubleProperty _inputYOrigin;

  /** The input column spacing */
  private DoubleProperty _inputColumnSpacing;

  /** The input row spacing */
  private DoubleProperty _inputRowSpacing;

  /** The input number of columns */
  private IntegerProperty _inputNumColumns;

  /** The input number of rows */
  private IntegerProperty _inputNumRows;

  /** The input primary angle */
  private StringProperty _inputPrimaryAngle;

  /** The xOrigin */
  private DoubleProperty _xOrigin;

  /** The yOrigin */
  private DoubleProperty _yOrigin;

  /** The column spacing */
  private DoubleProperty _columnSpacing;

  /** The row spacing */
  private DoubleProperty _rowSpacing;

  /** The number of columns */
  private IntegerProperty _numColumns;

  /** The number of rows */
  private IntegerProperty _numRows;

  /** The primary angle */
  private DoubleProperty _primaryAngle;

  /** The output horizon name */
  private StringProperty _outputHorizonName;

  /** The output grid comments property. */
  public StringProperty _outputComments;

  public ConvertHorizon() {
    super();
    _inputHorizon = addEntityProperty("Horizon", Grid3d.class);
    _inputXOrigin = addDoubleProperty("Input X Origin", 0);
    _inputYOrigin = addDoubleProperty("Input Y Origin", 0);
    _inputColumnSpacing = addDoubleProperty("Input Column Spacing", 0);
    _inputRowSpacing = addDoubleProperty("Input Row Spacing", 0);
    _inputNumColumns = addIntegerProperty("Input # of columns", 0);
    _inputNumRows = addIntegerProperty("Input # of rows", 0);
    _inputPrimaryAngle = addStringProperty("Input Primary angle", "0\u00B0");
    _xOrigin = addDoubleProperty("X Origin", 0);
    _yOrigin = addDoubleProperty("Y Origin", 0);
    _columnSpacing = addDoubleProperty("Column Spacing", 0);
    _rowSpacing = addDoubleProperty("Row Spacing", 0);
    _numColumns = addIntegerProperty("# of columns", 0);
    _numRows = addIntegerProperty("# of rows", 0);
    _primaryAngle = addDoubleProperty("Primary angle", 0);
    _outputHorizonName = addStringProperty("Name", "ch1");
    _outputComments = addStringProperty("Output Comments", "");
  }

  @Override
  public void propertyChanged(String key) {
    if (key.equals(_inputHorizon.getKey()) && _inputHorizon.get() != null) {

      String outputHorizonName = _inputHorizon.get().getMapper()
          .createOutputDisplayName(_inputHorizon.get().getDisplayName(), "_ch1");
      _outputHorizonName.set(outputHorizonName);

      // determine the geometry
      GridGeometry3d geometry = _inputHorizon.get().getGeometry();

      // Determine # of columns and # of rows
      int nCols = geometry.getNumColumns();
      int nRows = geometry.getNumRows();

      // Determine the corner points of the horizon
      Point3d[] cornerPoints = geometry.getCornerPoints().getPointsDirect();

      // Determine Column & Row Spacing
      double dx01 = cornerPoints[1].getX() - cornerPoints[0].getX();
      double dy01 = cornerPoints[1].getY() - cornerPoints[0].getY();
      double dx03 = cornerPoints[3].getX() - cornerPoints[0].getX();
      double dy03 = cornerPoints[3].getY() - cornerPoints[0].getY();
      double colSpacing = Math.sqrt(Math.pow(dx01, 2) + Math.pow(dy01, 2)) / (nCols - 1);
      double rowSpacing = Math.sqrt(Math.pow(dx03, 2) + Math.pow(dy03, 2)) / (nRows - 1);

      // determine the rotation angle based of the corner points
      double horRadians = Math.atan2(dy01, dx01);
      double horAngle = horRadians / Math.PI * 180.;

      // Change input parameters based on the input horizon
      _inputXOrigin.set(cornerPoints[0].getX());
      _inputYOrigin.set(cornerPoints[0].getY());
      _inputColumnSpacing.set(colSpacing);
      _inputRowSpacing.set(rowSpacing);
      _inputNumColumns.set(nCols);
      _inputNumRows.set(nRows);
      _inputPrimaryAngle.set(String.format("%.2f\u00B0", horAngle));

      // Default the output horizon parameters
      _xOrigin.set(cornerPoints[0].getX());
      _yOrigin.set(cornerPoints[0].getY());
      _columnSpacing.set(colSpacing);
      _rowSpacing.set(rowSpacing);
      _numColumns.set(nCols);
      _numRows.set(nRows);
      _primaryAngle.set(horAngle);
    }
  }

  @Override
  public void validate(IValidation results) {

    // Validate the input grid is non-null and of the correct type.
    if (_inputHorizon.isNull()) {
      results.error(_inputHorizon, "No input grid specified.");
    }

    // Validate the output name is non-zero length.
    if (_outputHorizonName.isEmpty()) {
      results.error(_outputHorizonName, "No output grid name specified.");
    }

    // Check if an entry already exists in the datastore.
    if (!_inputHorizon.isNull() && !_outputHorizonName.isEmpty()) {
      if (Grid3dFactory.existsInStore(_inputHorizon.get(), _outputHorizonName.get())) {
        results.warning(_outputHorizonName, "Exists in datastore and will be overwritten.");
      }
    }
  }

  /* (non-Javadoc)
   * Construct the algorithm's UI consisting of form fields partitioned into sections: Input,
   * Output, and algorithm Parameters.
   * @see org.geocraft.algorithm.StandaloneAlgorithm#buildView(org.geocraft.algorithm.IModelForm)
   */
  @Override
  public void buildView(IModelForm modelForm) {
    // Build the input parameters section.
    FormSection inputSection = modelForm.addSection("Input", false);
    inputSection.addEntityComboField(_inputHorizon, Grid3d.class);

    FormSection inputHorizonSection = modelForm.addSection("Input Horizon Parameters", false);
    inputHorizonSection.addLabelField(_inputXOrigin);
    inputHorizonSection.addLabelField(_inputYOrigin);
    inputHorizonSection.addLabelField(_inputColumnSpacing);
    inputHorizonSection.addLabelField(_inputRowSpacing);
    inputHorizonSection.addLabelField(_inputNumColumns);
    inputHorizonSection.addLabelField(_inputNumRows);
    inputHorizonSection.addLabelField(_inputPrimaryAngle);

    FormSection outputHorizonSection = modelForm.addSection("Output Horizon Parameters", false);
    outputHorizonSection.addTextField(_xOrigin);
    outputHorizonSection.addTextField(_yOrigin);
    outputHorizonSection.addTextField(_columnSpacing);
    outputHorizonSection.addTextField(_rowSpacing);
    outputHorizonSection.addTextField(_numColumns);
    outputHorizonSection.addTextField(_numRows);
    outputHorizonSection.addTextField(_primaryAngle);

    FormSection outputSection = modelForm.addSection("Output", false);
    outputSection.addTextField(_outputHorizonName);
    outputSection.addTextBox(_outputComments);
  }

  /**
   * Runs the domain logic of the algorithm.
   * @param monitor the progress monitor.
   * @param logger the logger to log messages.
   * @param repository the repository in which to add output entities.
   */

  @Override
  public void run(IProgressMonitor monitor, ILogger logger, IRepository repository) {

    // Unpack the model parameters.
    Grid3d property = _inputHorizon.get();
    double xOrigin = _xOrigin.get();
    double yOrigin = _yOrigin.get();
    double colSpacing = _columnSpacing.get();
    double rowSpacing = _rowSpacing.get();
    int nRows = _numRows.get();
    int nCols = _numColumns.get();
    double angle = _primaryAngle.get();

    String propertyName = _outputHorizonName.get();
    String outputComments = _outputComments.get();

    // Start the progress monitor.
    monitor.beginTask("Convert Horizon...", nRows);

    Grid3d outputGrid = convertHorizon(property, propertyName, xOrigin, yOrigin, colSpacing, rowSpacing, nRows, nCols,
        angle, property.getNullValue(), property.getDataUnit(), true, logger, repository);

    // add comments to the output grid
    if (!monitor.isCanceled()) {
      outputGrid.setComment(property.getComment() + "\n" + outputComments);
    }

    // Task is done.
    monitor.done();
  }

  /**
   * Converts the rectangular fromHor to a toHor with a different
   * geometry.
   * @param fromProp The horizon to be converted
   * @param toProp    The horizon to be converted to
   * @param noExtrap  noExtrap = true All values outside of the incoming
   *                                  horizon boundaries will be set to null.
   *                                  will be set to null.
   *                  noExtrap = false All values outside of the incoming
   *                                   horizon will be extrapolated.
   * @param propName The name of the horizon to create
   * @param logger The logger to log messages.
   * @param repository The repository in which to add output entities.
   */

  public static Grid3d convertHorizon(final Grid3d fromProp, final Grid3d toProp, final boolean noExtrap,
      final String propName, final ILogger logger, IRepository repository) {

    logger.debug("running ConvertHorizonImpl.java cnvrtHorizon()");

    // determine the horizons
    GridGeometry3d fromHor = fromProp.getGeometry();
    GridGeometry3d toHor = toProp.getGeometry();

    // Initialize a new property
    Grid3d newProp = toProp;

    // converting the horizon
    try {
      // convert the new property
      float[][] result = convert(fromHor, fromProp, toHor, toProp, noExtrap, logger);

      // Create an in memory grid3d property
      newProp = Grid3dFactory.createInMemory(propName, toHor, fromProp.getDataUnit(), result, fromProp.getNullValue());
      newProp.setDirty(true);
    } catch (Exception e) {
      logger.warn("Could not convert the horizon", e);
      return null;
    }

    // return the new property in the new horizon
    return newProp;
  }

  /**
   * Converts an rectangular fromHor with one geometry and outputs a rectangular horizon with
   * the incoming specified geometry.
   * Initialize and Create a new desired horizon with the incoming geometry by
   * calling Gridded Horizon.
   * Regrid the values with the new geometry.
   * @param fromProp the incoming horizon
   * @param name the name of the incoming horizon
   * @param x the origin x point of the desired horizon
   * @param y the origin y point of the desired horizon
   * @param colSpacing the col spacing of the desired horizon.
   * @param rowSpacing the row spacing of the desired horizon.
   * @param numRows the number of rows of the desired horizon.
   * @param numCols the number of columns of the desired horizon.
   * @param rotation the rotation value of the desired horizon
   * @param nullValue the null value of the desired horizon.
   * @param dataUnits the units of the desired horizon.
   * @param noExtrap values of the desired horizon that fall outside of the input horizon.
   *                 true to be null or
   *                 false extrapolate the values
   * @param logger The logger to log messages.
   * @param repository The repository in which to add output entities.
   *                 
   * @return the converted horizon.
   */

  public static Grid3d convertHorizon(final Grid3d fromProp, final String name, final double x, final double y,
      final double colSpacing, final double rowSpacing, final int numRows, final int numCols, final double rotation,
      final float nullValue, final Unit dataUnits, final boolean noExtrap, final ILogger logger, IRepository repository) {
    logger.debug("running ConvertHorizonImpl.java cnvrtHorizon()");

    // default the output data to nulls
    float[][] data = new float[numRows][numCols];
    for (int i1 = 0; i1 < numRows; i1++) {
      for (int i2 = 0; i2 < numCols; i2++) {
        data[i1][i2] = nullValue;
      }
    }

    // create a new grid3d object
    GridGeometry3d toHor = new GridGeometry3d(name, x, y, colSpacing, rowSpacing, numRows, numCols, rotation);

    // determine the horizon
    GridGeometry3d fromHor = fromProp.getGeometry();

    Grid3d toProp = null;

    try {
      // Create just in memory grid3d property
      toProp = Grid3dFactory.createInMemory(name, toHor, fromProp.getDataUnit(), data, nullValue);

      // convert the new property
      float[][] result = convert(fromHor, fromProp, toHor, toProp, noExtrap, logger);

      // add data to the property
      toProp.setValues(result, nullValue, dataUnits);
      toProp.setDirty(true);

      // Add property to the repository
      repository.add(toProp);

    } catch (Exception e) {
      logger.warn("Could not convert horizon", e);
      return null;
    }

    // return the new property in the new horizon
    return toProp;
  }

  /**
   * convert the horizon
   *
   * TODO: The current algorithm for deciding whether an output grid point
   * that is halfway between two input grid points should be null or non-null
   * is subject to numerical precision problems.  A more intelligent algorithm
   * that uses some other constraints (tolerance level, explicit polygons,
   * etc.) should be implemented to correct this deficiency.
   *
   * @throws Exception
   */
  private static float[][] convert(final GridGeometry3d horIn, final Grid3d propIn, final GridGeometry3d horOut,
      final Grid3d propOut, final boolean noExtrap, final ILogger logger) throws Exception {

    // Get size of horInput
    int horInRows = horIn.getNumRows();
    int horInCols = horIn.getNumColumns();
    logger.debug("InRows,InCols=" + horInRows + "," + horInCols);

    int horOutRows = horOut.getNumRows();
    int horOutCols = horOut.getNumColumns();
    logger.debug("OutRows,OutCols=" + horOutRows + "," + horOutCols);

    // convert series data to a float multi-dimension array
    float[][] gvalsIn = propIn.getValues();
    float[][] gvalsOut = propOut.getValues();

    // Initialize to extrapolate
    // boolean endLoop = false;
    double tolr = 0.01;
    double gnull = propOut.getNullValue();

    // Change Units in horizon object 1 to those of horizon object 2 if necessary
    Unit inHorUnits = propIn.getDataUnit();
    Unit outHorUnits = propOut.getDataUnit();

    //Domain inHorDomain = horIn.getDomain();
    //Domain outHorDomain = horOut.getDomain();
    if (!inHorUnits.equals(outHorUnits)) {
      throw new RuntimeException("Input and output horizons must have same units!");
    }

    // Convert the units
    for (int numCol = 0; numCol < horInCols; numCol++) {
      for (int numRow = 0; numRow < horInRows; numRow++) {
        gvalsIn[numRow][numCol] = Unit.convert(gvalsIn[numRow][numCol], inHorUnits, outHorUnits);
      }
    }

    // Make the horizon is rectangular
    if (!horIn.isRectangular()) {
      throw new RuntimeException("Input and output horizons currently need to be rectangular!");
    }

    // Check if new horizon is sub-area of old
    CoordinateSeries inCornerPoints = horIn.getCornerPoints();
    CoordinateSeries outCornerPoints = horOut.getCornerPoints();

    // determine the rotation angle based on the corner points
    Point3d[] inPoints = inCornerPoints.getPointsDirect();
    double dx01in = inPoints[1].getX() - inPoints[0].getX();
    double dy01in = inPoints[1].getY() - inPoints[0].getY();
    double horInRadians = Math.atan2(dy01in, dx01in);
    double horInDegrees = horInRadians / Math.PI * 180.;

    Point3d[] outPoints = outCornerPoints.getPointsDirect();
    double dx01out = outPoints[1].getX() - outPoints[0].getX();
    double dy01out = outPoints[1].getY() - outPoints[0].getY();
    double horOutRadians = Math.atan2(dy01out, dx01out);
    double horOutDegrees = horOutRadians / Math.PI * 180.;

    // Determine origin
    Point2D.Double horInOrigin = new Point2D.Double();
    horInOrigin.x = inPoints[0].getX();
    horInOrigin.y = inPoints[0].getY();

    Point2D.Double horOutOrigin = new Point2D.Double();
    horOutOrigin.x = outPoints[0].getX();
    horOutOrigin.y = outPoints[0].getY();

    // Determine Column & Row Spacing
    double dx03in = inPoints[3].getX() - inPoints[0].getX();
    double dy03in = inPoints[3].getY() - inPoints[0].getY();
    double horInColSpacing = Math.sqrt(Math.pow(dx01in, 2) + Math.pow(dy01in, 2)) / (horInCols - 1);
    double horInRowSpacing = Math.sqrt(Math.pow(dx03in, 2) + Math.pow(dy03in, 2)) / (horInRows - 1);

    double dx03out = outPoints[3].getX() - outPoints[0].getX();
    double dy03out = outPoints[3].getY() - outPoints[0].getY();
    double horOutColSpacing = Math.sqrt(Math.pow(dx01out, 2) + Math.pow(dy01out, 2)) / (horOutCols - 1);
    double horOutRowSpacing = Math.sqrt(Math.pow(dx03out, 2) + Math.pow(dy03out, 2)) / (horOutRows - 1);

    logger.debug("Out Col Spacing, In Col Spacing " + horOutColSpacing + "," + horInColSpacing);

    logger.debug("Out Row Spacing, In Row Spacing " + horOutRowSpacing + "," + horOutRowSpacing);

    logger.debug("Out Rotation, In Rotation " + horOutDegrees + "," + horInDegrees);

    if (Math.abs(horOutColSpacing - horInColSpacing) < tolr && Math.abs(horOutRowSpacing - horInRowSpacing) < tolr
        && Math.abs(horOutDegrees - horInDegrees) < tolr) {

      double shiftX = horOutOrigin.x - horInOrigin.x;
      double shiftY = horOutOrigin.y - horInOrigin.y;

      double xMove = shiftX * Math.cos(horInRadians) + shiftY * Math.sin(horInRadians);
      double yMove = -shiftX * Math.sin(horInRadians) + shiftY * Math.cos(horInRadians);

      int ix1 = (int) Math.round(xMove / horInColSpacing);
      int iy1 = (int) Math.round(yMove / horInRowSpacing);

      if (ix1 >= 0 && iy1 >= 0 && Math.abs(ix1 * horInColSpacing - xMove) < tolr
          && Math.abs(iy1 * horInRowSpacing - yMove) < tolr && ix1 + horOutCols <= horInCols
          && iy1 + horOutRows <= horInRows) {

        // ServiceProvider.getLoggingService().getLogger(getClass()).debug("The requested output horizon is sub-area(or same) as
        // the input horizon ");
        for (int ix2 = 0; ix2 < horOutCols; ix2++) {
          for (int iy2 = 0; iy2 < horOutRows; iy2++) {
            gvalsOut[iy2][ix2] = gvalsIn[iy1 + iy2][ix1 + ix2];
          }
        }

        // ServiceProvider.getLoggingService().getLogger(getClass()).debug("The requested output horizon is sub-area(or same) as
        // the input horizon ");
        return gvalsOut;
      }
    }

    // New horizon is not sub-area of old horizon and hence we resample
    // resample the horizon

    float xp;
    float yp;
    float xx;
    float yy;
    float xndx;
    float yndx;
    double xf;
    double yf;
    float g1;
    float g2;
    float g11;
    float g21;
    float g12;
    float g22;
    int ix1;
    int iy1;
    float s;
    float u;
    float w;

    for (int ix2 = 0; ix2 < horOutCols; ix2++) {
      // for (int ix2 = 18; ix2 < 22; ix2++) {
      for (int iy2 = 0; iy2 < horOutRows; iy2++) {
        // for (int iy2 = 0; iy2 < 10; iy2++) {
        xp = (float) (ix2 * horOutColSpacing);
        yp = (float) (iy2 * horOutRowSpacing);

        // ServiceProvider.getLoggingService().getLogger(getClass()).debug(" Horizon Out Radians "+horOutRadians);
        // ServiceProvider.getLoggingService().getLogger(getClass()).debug(" Horizon In Radians "+horInRadians);

        // ServiceProvider.getLoggingService().getLogger(getClass()).debug(" Col Spacing "+xp+" Row Spacing "+yp);

        xf = xp * Math.cos(horOutRadians) - yp * Math.sin(horOutRadians) + horOutOrigin.x;
        yf = xp * Math.sin(horOutRadians) + yp * Math.cos(horOutRadians) + horOutOrigin.y;

        // ServiceProvider.getLoggingService().getLogger(getClass()).debug(" New X "+xf+" New Y "+yf);

        xx = (float) (xf - horInOrigin.x);
        yy = (float) (yf - horInOrigin.y);

        // ServiceProvider.getLoggingService().getLogger(getClass()).debug(" New X Shift from Old Origin X "+xx+" New
        // Y Shift from Old Origin Y "+yy);

        xndx = (float) (xx * Math.cos(horInRadians) + yy * Math.sin(horInRadians));
        yndx = (float) (-xx * Math.sin(horInRadians) + yy * Math.cos(horInRadians));

        // ServiceProvider.getLoggingService().getLogger(getClass()).debug(" X in terms of old horizon"+xndx);
        // ServiceProvider.getLoggingService().getLogger(getClass()).debug(" Y in terms of old horizon"+yndx);

        // ServiceProvider.getLoggingService().getLogger(getClass()).debug(" New X in number of old column terms
        // "+xndx+" New Y in terms of old rows "+yndx);

        xndx = xndx / (float) horInColSpacing;
        yndx = yndx / (float) horInRowSpacing;

        // ServiceProvider.getLoggingService().getLogger(getClass()).debug(" New X in number of old column terms "
        // + xndx + " New Y in terms of old rows " + yndx);

        ix1 = (int) xndx;
        iy1 = (int) yndx;

        // ServiceProvider.getLoggingService().getLogger(getClass()).debug(" Old XY " + ix1 + " " + iy1);
        // ServiceProvider.getLoggingService().getLogger(getClass()).debug(" New XY " + ix2 + " " + iy2);

        /*
         * ------------------------------ | | | | | case1 | case 7 | case4 | | | | | ------------------------------ | | | | | | | | |
         * case3 | case 9 | case6 | | | | | | | | | ------------------------------ | | | | | case2 | case 8 | case5 | | | | |
         * ------------------------------
         */

        if (noExtrap && (xndx < 0 || xndx >= horInCols || yndx < 0 || yndx >= horInRows)) {
          gvalsOut[iy2][ix2] = (float) gnull;
          // ServiceProvider.getLoggingService().getLogger(getClass()).debug("HERE FOR NULL");
        } else if (ix1 < 0) {
          if (iy1 < 0) {
            // case 1
            // ServiceProvider.getLoggingService().getLogger(getClass()).debug("Case 1 ix1, iy1 " + ix1 + "," + iy1);
            gvalsOut[iy2][ix2] = gvalsIn[0][0];
          } else if (iy1 >= horInRows - 1) {
            // case 2
            // ServiceProvider.getLoggingService().getLogger(getClass()).debug("Case 2 ix1, iy1 " + ix1 + "," + iy1);
            gvalsOut[iy2][ix2] = gvalsIn[horInRows - 1][0];
          } else {
            // case 3
            // ServiceProvider.getLoggingService().getLogger(getClass()).debug("Case 3 ix1, iy1 " + ix1 + "," + iy1);
            g1 = gvalsIn[iy1][0];
            g2 = gvalsIn[iy1 + 1][0];
            gvalsOut[iy2][ix2] = (float) gnull;
            if (g1 != gnull && g2 != gnull) { // !linear y-interp
              gvalsOut[iy2][ix2] = g1 + (g2 - g1) * (yndx - iy1);
            } else if (yndx - iy1 > 0.5) { // !nearest neighbor
              gvalsOut[iy2][ix2] = g2;
            } else {
              gvalsOut[iy2][ix2] = g1;
            }
          }
        } else if (ix1 >= horInCols - 1) {
          if (iy1 < 0) {
            // case 4
            // ServiceProvider.getLoggingService().getLogger(getClass()).debug("Case 4 ix1, iy1 " + ix1 + "," + iy1);
            gvalsOut[iy2][ix2] = gvalsIn[0][horInCols - 1];
          } else if (iy1 >= horInRows - 1) {
            // case 5
            // ServiceProvider.getLoggingService().getLogger(getClass()).debug("Case 5 ix1, iy1 " + ix1 + "," + iy1);
            gvalsOut[iy2][ix2] = gvalsIn[horInRows - 1][horInCols - 1];
          } else {
            // case 6
            // ServiceProvider.getLoggingService().getLogger(getClass()).debug("Case 6 ix1, iy1 " + ix1 + "," + iy1);
            g1 = gvalsIn[iy1][horInCols - 1];
            g2 = gvalsIn[iy1 + 1][horInCols - 1];
            gvalsOut[iy2][ix2] = (float) gnull;
            if (g1 != gnull && g2 != gnull) { // !linear
              // y-interp
              gvalsOut[iy2][ix2] = g1 + (g2 - g1) * (yndx - iy1);
            } else if (yndx - iy1 > 0.5) { // !nearest
              // neighbor
              gvalsOut[iy2][ix2] = g2;
            } else {
              gvalsOut[iy2][ix2] = g1;
            }
          }
        } else if (iy1 < 0 && ix1 >= 0 && ix1 < horInCols - 1) {
          // case 7
          // ServiceProvider.getLoggingService().getLogger(getClass()).debug("Case 7 ix1, iy1 " + ix1 + "," + iy1);
          g1 = gvalsIn[0][ix1];
          g2 = gvalsIn[0][ix1 + 1];
          gvalsOut[iy2][ix2] = (float) gnull;
          if (g1 != gnull && g2 != gnull) { // !linear x-interp
            gvalsOut[iy2][ix2] = g1 + (g2 - g1) * (xndx - ix1);
          } else if (xndx - ix1 > 0.5) { // !nearest neighbor
            gvalsOut[iy2][ix2] = g2;
          } else {
            gvalsOut[iy2][ix2] = g1;
          }
        } else if (iy1 >= horInRows - 1 && ix1 >= 0 && ix1 < horInCols - 1) {
          // case 8
          // ServiceProvider.getLoggingService().getLogger(getClass()).debug("Case 8 ix1, iy1 " + ix1 + "," + iy1);
          g1 = gvalsIn[horInRows - 1][ix1];
          g2 = gvalsIn[horInRows - 1][ix1 + 1];
          gvalsOut[iy2][ix2] = (float) gnull;
          if (g1 != gnull && g2 != gnull) { // !linear x-interp
            gvalsOut[iy2][ix2] = g1 + (g2 - g1) * (xndx - ix1);
          } else if (xndx - ix1 > 0.5) { // !nearest neighbor
            gvalsOut[iy2][ix2] = g2;
          } else {
            gvalsOut[iy2][ix2] = g1;
          }
          // case 9
          // interpolate along y's first
        } else if (ix1 >= 0 && ix1 < horInCols - 1 && iy1 >= 0 && iy1 < horInRows - 1) {
          // ServiceProvider.getLoggingService().getLogger(getClass()).debug("Case 9 ix2 " + ix2 + " iy2 " + iy2
          // + " ix1 " + ix1 + " iy1 " + iy1);
          gvalsOut[iy2][ix2] = (float) gnull;

          g11 = gvalsIn[iy1][ix1];
          g21 = gvalsIn[iy1][ix1 + 1];
          g12 = gvalsIn[iy1 + 1][ix1];
          g22 = gvalsIn[iy1 + 1][ix1 + 1];

          if (g11 != gnull && g21 != gnull && // !bi-linear interp
              g12 != gnull && g22 != gnull) {
            g1 = g11 + (g21 - g11) * (xndx - ix1);
            g2 = g12 + (g22 - g12) * (xndx - ix1);
            gvalsOut[iy2][ix2] = g1 + (g2 - g1) * (yndx - iy1);
          } else if (xndx - ix1 > 0.5 && yndx - iy1 > 0.5) {
            // ! (xndx,yndx) closest to (ix1+1,iy1+1)
            if (g22 == gnull) {
              gvalsOut[iy2][ix2] = (float) gnull;
            } else if (g22 != gnull && g21 != gnull && g12 != gnull) {
              // tri-linear interpolation
              s = (float) 1.0;
              u = ix1 + 1 - xndx;
              w = iy1 + 1 - yndx;
              gvalsOut[iy2][ix2] = s * (u * (g12 - g22) + w * (g21 - g22)) + g22;
            } else if (g22 != gnull && g12 != gnull && g11 != gnull && xndx - ix1 <= yndx - iy1) {
              // !tri-linear interp
              s = (float) 1.0;
              u = iy1 + 1 - yndx;
              w = xndx - ix1;
              gvalsOut[iy2][ix2] = s * (u * (g11 - g12) + w * (g22 - g12)) + g12;
            } else if (g22 != gnull && g21 != gnull && g11 != gnull && xndx - ix1 >= yndx - iy1) {
              // !tri-linear interp
              s = (float) 1.0;
              u = yndx - iy1;
              w = ix1 + 1 - xndx;
              gvalsOut[iy2][ix2] = s * (u * (g22 - g21) + w * (g11 - g21)) + g21;
            } else if (g22 != gnull && g11 != gnull && xndx - ix1 >= yndx - iy1) {
              // !modified tri-linear interp
              w = ix1 + 1 - xndx;
              gvalsOut[iy2][ix2] = w * (g11 - g22) + g22;
            } else if (g22 != gnull && g11 != gnull && xndx - ix1 < yndx - iy1) {
              // !modified tri-linear interp
              u = iy1 + 1 - yndx;
              gvalsOut[iy2][ix2] = u * (g11 - g22) + g22;
            } else if (g22 != gnull && g12 != gnull) {
              // !linear x-interp
              gvalsOut[iy2][ix2] = (g22 - g12) * (xndx - ix1) + g12;
            } else if (g22 != gnull && g21 != gnull) {
              // !linear y-interp
              gvalsOut[iy2][ix2] = (g22 - g21) * (yndx - iy1) + g21;
            } else {
              // !nearest neighbor
              gvalsOut[iy2][ix2] = g22;
            }
          } else if (xndx - ix1 > 0.5) {
            // ! (xndx,yndx) closest to (ix1+1,iy1)
            if (g21 == gnull) {
              gvalsOut[iy2][ix2] = (float) gnull;
            } else if (g21 != gnull && g11 != gnull && g22 != gnull) {
              // !tri-linear interp
              s = (float) 1.0;
              u = yndx - iy1;
              w = ix1 + 1 - xndx;
              gvalsOut[iy2][ix2] = s * (u * (g22 - g21) + w * (g11 - g21)) + g21;
            } else if (g21 != gnull && g11 != gnull && g12 != gnull && xndx - ix1 <= 1.0 - (yndx - iy1)) {
              // !tri-linear interp
              s = (float) 1.0;
              u = xndx - ix1;
              w = yndx - iy1;
              gvalsOut[iy2][ix2] = s * (u * (g21 - g11) + w * (g12 - g11)) + g11;
            } else if (g21 != gnull && g12 != gnull && g22 != gnull && xndx - ix1 >= 1.0 - (yndx - iy1)) {
              // !tri-linear interp
              s = (float) 1.0;
              u = ix1 + 1 - xndx;
              w = iy1 + 1 - yndx;
              gvalsOut[iy2][ix2] = s * (u * (g12 - g22) + w * (g21 - g22)) + g22;
            } else if (g21 != gnull && g12 != gnull && xndx - ix1 >= 1.0 - (yndx - iy1)) {
              // !modified tri-linear interp
              u = ix1 + 1 - xndx;
              gvalsOut[iy2][ix2] = u * (g12 - g21) + g21;
            } else if (g21 != gnull && g12 != gnull && xndx - ix1 < 1.0 - (yndx - iy1)) {
              // !modified tri-linear interp
              w = yndx - iy1;
              gvalsOut[iy2][ix2] = w * (g12 - g21) + g21;
            } else if (g21 != gnull && g11 != gnull) {
              // !linear x-interp
              gvalsOut[iy2][ix2] = (g21 - g11) * (xndx - ix1) + g11;
            } else if (g21 != gnull && g22 != gnull) {
              // !linear y-interp
              gvalsOut[iy2][ix2] = (g22 - g21) * (yndx - iy1) + g21;
            } else {
              // !nearest neighbor
              gvalsOut[iy2][ix2] = g21;
            }
          } else if (yndx - iy1 > 0.5) {
            // ! (xndx,yndx) closest to (ix1, iy1+1)
            if (g12 == gnull) {
              gvalsOut[iy2][ix2] = (float) gnull;
            } else if (g12 != gnull && g11 != gnull && g22 != gnull) {
              // !tri-linear interp
              s = (float) 1.0;
              u = iy1 + 1 - yndx;
              w = xndx - ix1;
              gvalsOut[iy2][ix2] = s * (u * (g11 - g12) + w * (g22 - g12)) + g12;
            } else if (g12 != gnull && g21 != gnull && g22 != gnull && xndx - ix1 >= 1.0 - (yndx - iy1)) {
              // !tri-linear interp
              s = (float) 1.0;
              u = ix1 + 1 - xndx;
              w = iy1 + 1 - yndx;
              gvalsOut[iy2][ix2] = s * (u * (g12 - g22) + w * (g21 - g22)) + g22;
            } else if (g12 != gnull && g11 != gnull && g21 != gnull && xndx - ix1 <= 1.0 - (yndx - iy1)) {
              // !tri-linear interp
              s = (float) 1.0;
              u = xndx - ix1;
              w = yndx - iy1;
              gvalsOut[iy2][ix2] = s * (u * (g21 - g11) + w * (g12 - g11)) + g11;
            } else if (g12 != gnull && g21 != gnull && xndx - ix1 <= 1.0 - (yndx - iy1)) {
              // !modified tri-linear interp
              u = xndx - ix1;
              gvalsOut[iy2][ix2] = u * (g21 - g12) + g12;
            } else if (g12 != gnull && g21 != gnull && xndx - ix1 > 1.0 - (yndx - iy1)) {
              // !modified tri-linear interp
              w = iy1 + 1 - yndx;
              gvalsOut[iy2][ix2] = w * (g21 - g12) + g12;
            } else if (g12 != gnull && g22 != gnull) {
              // !linear x-interp
              gvalsOut[iy2][ix2] = (g22 - g12) * (xndx - ix1) + g12;
            } else if (g12 != gnull && g11 != gnull) {
              // !linear y-interp
              gvalsOut[iy2][ix2] = (g12 - g11) * (yndx - iy1) + g11;
            } else {
              // !nearest neighbor
              gvalsOut[iy2][ix2] = g12;
            }
            // ! (xndx,yndx) closest to (ix1, iy1)
          } else if (g11 == gnull) {
            gvalsOut[iy2][ix2] = (float) gnull;
          } else if (g11 != gnull && g21 != gnull && g12 != gnull) {
            // !tri-linear interp
            s = (float) 1.0;
            u = xndx - ix1;
            w = yndx - iy1;
            gvalsOut[iy2][ix2] = s * (u * (g21 - g11) + w * (g12 - g11)) + g11;
          } else if (g11 != gnull && g21 != gnull && g22 != gnull && xndx - ix1 >= yndx - iy1) {
            // !tri-linear interp
            s = (float) 1.0;
            u = yndx - iy1;
            w = ix1 + 1 - xndx;
            gvalsOut[iy2][ix2] = s * (u * (g22 - g21) + w * (g11 - g21)) + g21;
          } else if (g11 != gnull && g12 != gnull && g22 != gnull && xndx - ix1 <= yndx - iy1) {
            // !tri-linear interp
            s = (float) 1.0;
            u = iy1 + 1 - yndx;
            w = xndx - ix1;
            gvalsOut[iy2][ix2] = s * (u * (g11 - g12) + w * (g22 - g12)) + g12;
          } else if (g11 != gnull && g22 != gnull && xndx - ix1 <= yndx - iy1) {
            // !modified tri-linear interp
            w = xndx - ix1;
            gvalsOut[iy2][ix2] = w * (g22 - g11) + g11;
          } else if (g11 != gnull && g22 != gnull && xndx - ix1 > yndx - iy1) {
            // !modified tri-linear interp
            u = yndx - iy1;
            gvalsOut[iy2][ix2] = u * (g22 - g11) + g11;
          } else if (g11 != gnull && g21 != gnull) {
            // !linear x-interp
            gvalsOut[iy2][ix2] = (g21 - g11) * (xndx - ix1) + g11;
          } else if (g11 != gnull && g12 != gnull) {
            // !linear y-interp
            gvalsOut[iy2][ix2] = (g12 - g11) * (yndx - iy1) + g11;
          } else {
            // !nearest neighbor
            gvalsOut[iy2][ix2] = g11;
          }
          // case '10' - just in case we missed a case
        } else {
          // ServiceProvider.getLoggingService().getLogger(getClass()).debug("Case 10 set to null ix1, iy1 " + ix1
          // + "," + iy1);
          gvalsOut[iy2][ix2] = (float) gnull;
        }
      }
    }
    logger.debug("MIN VALUE " + propOut.getMinValue());
    logger.debug("MIN VALUE " + propOut.getMinValue());
    return gvalsOut;
  }

}
