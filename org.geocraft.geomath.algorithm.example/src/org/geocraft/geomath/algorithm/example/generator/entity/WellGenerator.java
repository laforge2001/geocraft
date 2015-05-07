/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.example.generator.entity;


import java.sql.Timestamp;

import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.model.datatypes.Coordinate;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.FloatMeasurementSeries;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.well.Well;
import org.geocraft.core.model.well.WellBore;
import org.geocraft.core.model.well.WellCheckShot;
import org.geocraft.core.model.well.WellDomain;
import org.geocraft.core.model.well.WellLogTrace;
import org.geocraft.core.model.well.WellPick;
import org.geocraft.core.repository.IRepository;
import org.geocraft.geomath.algorithm.example.generator.TestDataGenerator;


/**
 * This class generates synthetic, in-memory wells, bores, logs and picks.
 */
public final class WellGenerator {

  /** The null value to use for well log traces. */
  public static final float ZNULL = (float) (Math.random() * 1e10);

  /** The repository in which to add wells and their children. */
  private IRepository _repository;

  /** The horizon model to use when generating well picks. */
  private TestHorizonModel _horizonModel;

  /** The location to use as an origin. */
  private Point3d _origin = new Point3d(40000, 1000000, 0);

  /** The coordinate system in which locations are defined. */
  private CoordinateSystem _coordSystem;

  /**
   * Constructs a synthetic well generator.
   * 
   * @param repository the repository in which to add generated wells and their children.
   * @param horizonModel the horizon model to use when generating well picks.
   * @param coordSystem the coordinate system in which locations are defined.
   */
  public WellGenerator(final IRepository repository, final TestHorizonModel horizonModel, final CoordinateSystem coordSystem) {
    _repository = repository;
    _coordSystem = coordSystem;
    _horizonModel = horizonModel;
  }

  /**
   * Creates an in-memory well and adds it to the repository.
   * 
   * @param wellNumber the well # (UWI).
   * @param width the width of the area.
   * @param height the height of the area.
   * @param numLogs the number of well logs to add to each well bore.
   * @param picks <i>true</i> to add picks; otherwise <i>false</i>.
   */
  public void addWell(final int wellNumber, final double width, final double height, final int numLogs,
      final boolean picks) {

    // Construct an in-memory well.
    Well well = new Well("Well #" + wellNumber);
    well.setIdentifierAndType("" + wellNumber, "UWI");

    // Set the properties of the well (surface location, country, operator, etc).
    well.setGroundElevation((float) Math.random() * 80 + 20);
    Point3d locationPoint = new Point3d(_origin.getX() + (Math.random() * 2 - 1) * width * 5280, _origin.getY()
        + (Math.random() * 2 - 1) * height * 5280, well.getGroundElevation());
    Coordinate location = new Coordinate(locationPoint, _coordSystem);
    well.setLocation(location);

    well.setComment("This is a randomly generated well.");
    well.setCountry("Outer Space");
    well.setCounty("Harris");
    well.setCurrentOperator(TestDataGenerator.getRandomOperator());
    well.setDataSource("Saucer");
    well.setField("Vacuum");
    well.setLeaseName("Invasion");
    well.setLeaseNumber(Double.toString(Math.random() * 100));
    well.setOCSNumber(Double.toString(Math.random() * 1243));
    well.setOffshoreArea("void");
    well.setOffshoreBlock(Double.toString(Math.random() * 30));
    well.setPermitNumber(Double.toString(Math.random() * 4323));
    well.setPlatformIdentifier("Saucer" + wellNumber);
    well.setProjectName("Test");
    well.setStateOrProvince("The MotherShip");
    well.setSpudDate(new Timestamp(1000000000));

    // Add well bore to the well.
    addWellBore(wellNumber, 1, numLogs, picks, well);

    // Add the well to the repository.
    well.setDirty(false);
    _repository.add(well);
  }

  /**
   * Creates and adds an in-memory well bore to a given well.
   * 
   * @param wellNumber the number of the well.
   * @param boreNumber the number of the well bore.
   * @param numLogs the number of well logs to add to the well bore.
   * @param picks <i>true</i> to add picks to the well bore; otherwise <i>false</i>.
   * @param parentWell the parent well.
   */
  private void addWellBore(final int wellNumber, final int boreNumber, final int numLogs, final boolean picks,
      final Well parentWell) {

    // Create an in-memory well bore.
    WellBore wellBore = parentWell.getWellBore();

    // Set the properties of the well bore.
    wellBore.setIdentifierType("UWI");
    wellBore.setIdentifier(parentWell.getIdentifier() + "_" + boreNumber);
    parentWell.setTotalDepth((float) (Math.random() * 10000 + 5000));

    int n = (int) (parentWell.getTotalDepth() / 50);
    Point3d[] pointArray = new Point3d[n];
    float[] pathTVD = new float[n];
    float[] pathMD = new float[n];
    float[] pathAzm = new float[n];
    float[] pathDip = new float[n];
    double build = Math.random() * 0.9;
    double turn = Math.random() * 4 - 2;
    double xtop = parentWell.getLocation().getPoint().getX();
    double ytop = parentWell.getLocation().getPoint().getY();
    double ztop = parentWell.getLocation().getPoint().getZ();

    pointArray[0] = new Point3d(xtop, ytop, ztop);
    pathMD[0] = 0;
    pathAzm[0] = 45;
    pathDip[0] = 0;

    double[] xOffsets = new double[n];
    double[] yOffsets = new double[n];
    xOffsets[0] = 0;
    yOffsets[0] = 0;
    for (int j = 1; j < n; j++) {

      double r = 50 * Math.sin(pathDip[j - 1] * Math.PI / 180);
      double x = r * Math.cos(pathAzm[j - 1] * Math.PI / 180) + pointArray[j - 1].getX();
      double y = r * Math.sin(pathAzm[j - 1] * Math.PI / 180) + pointArray[j - 1].getY();
      double z = pointArray[j - 1].getZ() + 50 * Math.cos(pathDip[j - 1] * Math.PI / 180);

      xOffsets[j] = x - xtop;
      yOffsets[j] = y - ytop;

      pointArray[j] = new Point3d(x, y, z);

      pathTVD[j] = (float) pointArray[j].getZ();
      pathDip[j] = (float) (pathDip[j - 1] + 0.5 * build);
      pathAzm[j] = (float) (pathAzm[j - 1] + 0.5 * turn);
      pathMD[j] = pathMD[j - 1] + 50;

      // Add picks to the well bore, if requested.
      if (picks) {
        findHorizonIntersection(parentWell, pointArray[j - 1], pathMD[j - 1], pointArray[j], pathMD[j]);
      }
    }
    wellBore.setBottomLocation(new Coordinate(pointArray[n - 1], _coordSystem));
    wellBore.setPathAzimuth(new FloatMeasurementSeries(pathAzm, Unit.DEGREE_OF_AN_ANGLE));
    wellBore.setPathDip(new FloatMeasurementSeries(pathDip, Unit.DEGREE_OF_AN_ANGLE));
    wellBore.setXYOffsets(xOffsets, yOffsets);
    wellBore.setDepthsAndTimes(pathMD, pathTVD, new float[0]);
    wellBore.setElevation((float) (Math.random() * 20. + 50));
    wellBore.setElevationDatum("KB");

    // Add well logs to the well bore.
    for (int i = 0; i < numLogs; i++) {
      addWellLog(parentWell);
    }

    // Add a checkshot to the well bore.
    addCheckShot(parentWell);

    wellBore.setBoreStatus("Invading");
    //wellBore.setComment("No comment.");
    wellBore.setAzimuthNorthType("test");
    wellBore.setBottomLocation(new Coordinate(pointArray[pointArray.length - 1], _coordSystem));
    wellBore.setCalcMethod("test");
    wellBore.setCompletionDate(new Timestamp((long) (Math.random() * 2117345636)));
    wellBore.setDataSource("testgenerator");
    wellBore.setElevationDatum("test");
    wellBore.setFlowDirection("outer space");
    wellBore.setFluidType("HCOH");
    wellBore.setFormationAtTD("rock");
    wellBore.setIdentifierType("test generator");
    //wellBore.setLastModifiedDate(new Timestamp((long) (Math.random() * 2144043333)));
    wellBore.setPlugBackTotalDepth(parentWell.getTotalDepth());
    //wellBore.setProjectName("test generator");
    wellBore.setShowType("HCOH");
    wellBore.setSpudDate(new Timestamp((long) (Math.random() * 2104433454)));
    parentWell.setTotalDepthType("test");
    //wellBore.setDirty(false);

    // Add the well bore to the parent well.
    //parentWell.addWellBore(wellBore);

    // Add the well bore to the repository.
    // wellBore.setDirty(false);
    //_repository.add(wellBore);
  }

  /**
   * If a horizon intersects the log between these two log measurements then create a pick. Also interpolate the measured depths at this pick location.
   * 
   * @param wellBore the parent well bore.
   * @param point1 the x,y,z location of the upper log measurement
   * @param md1 the measured depth at the upper location.
   * @param point2 the x,y,z location of the lower log measurement
   * @param md2 the measured depth at the lower location.
   */
  private void findHorizonIntersection(final Well well, final Point3d point1, final float md1, final Point3d point2,
      final float md2) {

    double pointZ1 = point1.getZ();
    double pointZ2 = point2.getZ();

    for (int i = 1; i <= _horizonModel.getNumHorizons(); i++) {
      // Compute the horizon depth at the x,y location of each log measurement.

      // TODO: added some hacks in order to make this code generate well picks, we will probably need to improve this code
      float horizonZ1 = _horizonModel.getHorizonZ(i, point1.getX(), point1.getY());
      float horizonZ2 = _horizonModel.getHorizonZ(i, point2.getX(), point2.getY()) + 100;

      // Check if there is an intersection..
      if (horizonZ1 <= pointZ1 && horizonZ2 >= pointZ2) {

        Point3d pick = point2;
        float md = md2;

        // Interpolate if needed.
        if (horizonZ2 - pointZ2 != 0) {
          double frac = Math.abs((horizonZ1 - pointZ1) / (horizonZ2 - pointZ2));
          pick = Point3d.interpolate(point1, point2, frac);
          md = md1 + (float) frac * (md2 - md1);
        }

        float azimuth = _horizonModel.getAzumith(pick.getX(), pick.getY());
        float dip = _horizonModel.getDip(pick.getX(), pick.getY());

        // Create and add a well pick.
        addPick("Horizon" + i, well, md, azimuth, dip);
      }
    }
  }

  /**
   * Creates and adds an in-memory well log to the given well bore.
   * 
   * @param wellBore the parent well bore.
   */
  private void addWellLog(final Well well) {
    WellBore wellBore = well.getWellBore();

    String[] traceNames = { "GR", "SP", "ILD", "DT", "RHOB", "SN" };

    String traceName = traceNames[(int) (Math.random() * 6)];

    // Create an in-memory well log trace.
    WellLogTrace wellLog = new WellLogTrace(traceName, well);

    // Set the properties of the well log.
    wellLog.setTraceVersion(1);
    wellLog.setTraceMnemonic(wellLog.getDisplayName());
    wellLog.setTraceName(traceName);

    // Compute the depth index ...
    double interval = 0.5f;
    double top = Math.random() * 500 + 500;
    double bottom = top + Math.random() * (well.getTotalDepth() - top);

    int numPoints = (int) ((bottom - top) / interval);
    double[] traceIndexArray = new double[numPoints];

    for (int i = 0; i < numPoints; i++) {
      traceIndexArray[i] = top + interval * i;
    }

    wellLog.setZValues(traceIndexArray, WellDomain.MEASURED_DEPTH);

    // Compute fake log data for one trace ...
    Unit traceUnit = null;
    float traceMin = 0;
    float traceMax = 0;

    if (traceName.equals("GR")) {
      traceUnit = Unit.API_GAMMA_RAY_UNITS;
      traceMin = 0;
      traceMax = 100;
    } else if (traceName.equals("SP")) {
      traceUnit = Unit.MILLIVOLTS;
      traceMin = -100;
      traceMax = 0;
    } else if (traceName.equals("ILD")) {
      traceUnit = Unit.MHOS;
      traceMin = .01f;
      traceMax = 100;
    } else if (traceName.equals("SN")) {
      traceUnit = Unit.MHOS;
      traceMin = .01f;
      traceMax = 100;
    } else if (traceName.equals("DT")) {
      traceUnit = Unit.MILLISECOND_PER_FOOT;
      traceMin = 30;
      traceMax = 140;
    } else if (traceName.equals("RHOB")) {
      traceUnit = Unit.GRAMS_PER_CUBIC_CENTIMETER;
      traceMin = 1.5f;
      traceMax = 2.7f;
    }

    // Construct some fake data for the log trace.
    float[] traceData = new float[numPoints];

    // Change the frequency of the logs waves.
    double sinFreq = 200 + Math.random() * 400;
    double cosFreq = 20 + Math.random() * 20;

    // Toggle between showing and nulling out the test data.
    boolean isNull = false;

    for (int i = 0; i < numPoints; i++) {
      // Put in some random null segments.
      if (isNull) {
        if (Math.random() < 0.1) {
          isNull = false;
        }
      } else if (Math.random() < 0.01) {
        isNull = true;
      }

      if (isNull) {
        traceData[i] = ZNULL;
      } else {
        traceData[i] = (float) (traceMin + (traceMax - traceMin) / 2
            * (1 + Math.sin(traceIndexArray[i] / sinFreq) * Math.cos(traceIndexArray[i] / cosFreq)));
      }
    }
    wellLog.setTraceData(traceData, traceUnit, ZNULL);

    // Add the well log to the well bore.
    wellBore.getWell().addWellLogTrace(wellLog);

    // Add the well log to the repository.
    wellLog.setDirty(false);
    _repository.add(wellLog);
  }

  /**
   * Creates and adds an in-memory well checkshot to the given well bore.
   * 
   * @param wellBore the parent well bore.
   */
  private void addCheckShot(final Well well) {

    // Create an in-memory well checkshot.
    WellBore wellBore = well.getWellBore();
    WellCheckShot checkshot = new WellCheckShot("Default Checkshot", well);

    // Set the properties of the checkshot.
    VelocityModel velocityModel = new VelocityModel(5000, .5);
    int numPoints = 100;
    float deltaZ = (float) Math.abs(wellBore.getBottomLocation().getPoint().getZ() / 100);

    float datum = 0.f;

    float[] md = new float[numPoints];
    float[] twt = new float[numPoints];
    float[] tvd = new float[numPoints];
    for (int i = 0; i < numPoints; i++) {
      md[i] = i * deltaZ;
      tvd[i] = i * deltaZ;
      twt[i] = 1000 * velocityModel.getTwoWayTime(tvd[i]);
    }

    checkshot.setDepthsAndTimes(datum, tvd, twt);
    checkshot.setComment("test");
    checkshot.setDataSource("test generator");
    checkshot.setLastModifiedDate(new Timestamp((long) (Math.random() * 2123542222)));
    checkshot.setProjectName("test");

    // Add the checkshot to the well bore.
    wellBore.setDefaultCheckShot(checkshot);
    wellBore.getWell().addWellCheckShot(checkshot);

    // Add the checkshot to the repository.
    checkshot.setDirty(false);
    _repository.add(checkshot);
  }

  /**
   * Creates and adds an in-memory well pick to the given well bore.
   * 
   * @param name the name of the well pick.
   * @param wellBore the parent well bore.
   * @param location
   * @param pickMD the measured depth of the pick.
   * @param azimuth the pick azimuth.
   * @param dip the pick dip.
   */
  private void addPick(final String name, final Well well, final float pickMD, final float azimuth, final float dip) {

    // Create an in-memory well pick.
    WellPick wellPick = new WellPick(name, well);

    // Set the properties of the well pick.
    wellPick.setPickDepth(pickMD, WellDomain.MEASURED_DEPTH);
    wellPick.setDipAngle(dip);
    wellPick.setDipAzimuth(azimuth);
    wellPick
        .setPickColor(new RGB((int) (255 * Math.random()), (int) (255 * Math.random()), (int) (255 * Math.random())));
    wellPick.setInterpreter("Bob");
    wellPick.setComment("test");
    wellPick.setConfidenceFactor("good");
    wellPick.setLastModifiedDate(new Timestamp((long) (Math.random() * 2100030303)));

    // Add the pick to the well bore.
    well.addWellPick(wellPick);

    // Add the pick to the repository.
    wellPick.setDirty(false);
    _repository.add(wellPick);
  }
}
