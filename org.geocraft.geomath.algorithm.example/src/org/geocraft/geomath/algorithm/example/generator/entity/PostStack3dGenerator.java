/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.example.generator.entity;


import java.util.Map;

import org.geocraft.core.factory.model.PostStack3dFactory;
import org.geocraft.core.model.EarthModel;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.CornerPointsSeries;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geologicfeature.GeologicHorizon;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.model.seismic.SurveyOrientation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.geomath.algorithm.example.generator.TestDataGenerator;


public class PostStack3dGenerator {

  TestHorizonModel _horizonModel;

  IRepository _repository;

  EarthModel _depthModel;

  CoordinateSystem _coordSys;

  Point3d _origin = new Point3d(40000, 1000000, 0);

  //use a random large float value to stop people hardcoding the value
  public static final float ZNULLXX = (float) (Math.random() * 1e10);

  public PostStack3dGenerator(final TestHorizonModel horizonModel, final IRepository repository, final CoordinateSystem coordSys) {
    _horizonModel = horizonModel;
    _repository = repository;
    _coordSys = coordSys;

    Map<String, Object> models = _repository.get(new TypeSpecification(EarthModel.class));

    for (Object obj : models.values()) {
      EarthModel tmp = (EarthModel) obj;
      if (tmp.getPrimaryZDomain() == Domain.DISTANCE) {
        _depthModel = tmp;
      }
    }

  }

  public void add3dSurvey(final int surveyNum, final int numVolumes, final boolean seisHorizons, final int nFaults,
      final GeologicHorizon[] horizons) {

    /*
     * rowN 3--------------2 
     *      |              | 
     *      |              | 
     *      |              | 
     *      |              | 
     *      |              | 
     *      |              | 
     *      |              | 
     * row0 0--------------1 
     *       col0          coln
     */

    int nRows = 400 + (int) (100 * Math.random());
    int nCols = 350 + (int) (100 * Math.random());

    int columnOffset = (nRows - 1) * 10;
    int rowOffset = (nCols - 1) * 10;

    double xmin = _origin.getX() + 10000 * Math.random();
    double ymin = _origin.getY() + 10000 * Math.random();

    Point3d[] points = TestDataGenerator.createBoundary(xmin, ymin, rowOffset, columnOffset);

    CornerPointsSeries cornerPoints = CornerPointsSeries.createDirect(points, _coordSys);
    float inlineStart = 100;
    float inlineEnd = 100 + nRows - 1;
    float xlineStart = 50;
    float xlineEnd = 100 + nCols - 1;

    float maxTime = (int) (1000 + 200 * Math.random()) * 4f;
    float maxDepth = (int) (1000 + 200 * Math.random()) * 10f;

    FloatRange inlineRange = new FloatRange(inlineStart, inlineEnd, 1);
    FloatRange xlineRange = new FloatRange(xlineStart, xlineEnd, 1);
    SeismicSurvey3d geometry = new SeismicSurvey3d("survey" + surveyNum, inlineRange, xlineRange, cornerPoints,
        SurveyOrientation.ROW_IS_XLINE);

    for (int i = 0; i < numVolumes; i++) {

      try {

        // Create a volume in the time domain.
        float signal2Noise = 3.0f;
        PostStack3dMapper accessor = new PostStack3dMapper(_horizonModel, signal2Noise);
        PostStack3d volume = PostStack3dFactory.create("TimeVolume" + i, accessor);
        volume.setZDomain(Domain.TIME);
        volume.setSurvey(geometry);
        volume.setInlineRangeAndDelta(geometry.getInlineStart(), geometry.getInlineEnd(), geometry.getInlineDelta());
        volume.setXlineRangeAndDelta(geometry.getXlineStart(), geometry.getXlineEnd(), geometry.getXlineDelta());
        volume.setZRangeAndDelta(0f, maxTime, 4f);
        volume.setZMaxRangeAndDelta(0f, maxTime, 4f);
        //volume.setSampleMin(new FloatMeasurement(-signal2Noise, Unit.UNDEFINED));
        //volume.setSampleMax(new FloatMeasurement(signal2Noise, Unit.UNDEFINED));
        volume.setDirty(false);
        _repository.add(volume);

        // Create a volume in the depth domain.
        PostStack3dMapper accessor2 = new PostStack3dMapper(_horizonModel, signal2Noise);
        PostStack3d volume2 = PostStack3dFactory.create("DepthVolume" + i, accessor2);
        volume2.setZDomain(Domain.DISTANCE);
        volume2.setSurvey(geometry);
        volume2.setInlineRangeAndDelta(geometry.getInlineStart(), geometry.getInlineEnd(), geometry.getInlineDelta());
        volume2.setXlineRangeAndDelta(geometry.getXlineStart(), geometry.getXlineEnd(), geometry.getXlineDelta());
        volume2.setZRangeAndDelta(0f, maxDepth, 10f);
        volume2.setZMaxRangeAndDelta(0f, maxDepth, 10f);
        //volume2.setSampleMin(new FloatMeasurement(-signal2Noise, Unit.UNDEFINED));
        //volume2.setSampleMax(new FloatMeasurement(signal2Noise, Unit.UNDEFINED));
        volume2.setDirty(false);
        _repository.add(volume2);

        ///survey.addPostStack(volume2);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    if (seisHorizons) {
      int i = 0;
      for (GeologicHorizon horizon : horizons) {
        addSeismicHorizon(geometry, horizon, i++);
      }
    }

    FaultGenerator faultGenerator = new FaultGenerator(_repository, _coordSys);
    for (int i = 0; i < nFaults; i++) {
      faultGenerator.addFault(geometry, i);
    }

  }

  private void addSeismicHorizon(final SeismicSurvey3d geometry, final GeologicHorizon horizon, final int horzNum) {

    Point3d[] points = geometry.getCornerPoints().getPointsDirect();

    // Point3d zPeakPoint = new Point3d();
    // zPeakPoint.interpolate(points[0], points[2], .3); //put the peak of the
    // Grid 1/3 between the
    // opposing corner points

    int nRows = geometry.getNumXlines();
    int nCols = geometry.getNumInlines();

    // CoordinateSeries cornerPoints = new CoordinateSeries( points, _coordSys
    // );

    Grid3d gridZ = new Grid3d("Depth", geometry);
    gridZ.setInterpreter("Bob");
    gridZ.setComment("a synthetic horizon");
    gridZ.setDataSource("FakeDataGenerator");

    Grid3d gridAmp = new Grid3d("Amplitude", geometry);

    float[][] z = new float[nRows][nCols];
    float[][] amp = new float[nRows][nCols];

    // Vector3d columnVector = new Vector3d(); not used????
    // columnVector.sub(points[3], points[0]);
    // Vector3d rowVector = new Vector3d();
    // rowVector.sub(points[1], points[0]);

    //double delY = 3 / nRows; // hmm? columnVector.length() / nRows;
    //double delX = 3 / nCols; // rowVector.length() / nCols;

    double delY = (points[3].getY() - points[0].getY()) / nRows;
    double delX = (points[1].getX() - points[0].getX()) / nCols;

    for (int row = 0; row < nRows; row++) {
      for (int col = 0; col < nCols; col++) {

        double x = points[0].getX() + col * delX;
        double y = points[0].getY() + row * delY;

        z[row][col] = _horizonModel.getHorizonZ(horzNum, x, y);
        amp[row][col] = _horizonModel.getAmplitude(horzNum, x, y);
      }
    }

    gridZ.setValues(z, TestHorizonModel.ZNULL, Unit.FOOT);
    gridZ.setZDomain(Domain.DISTANCE);
    gridZ.setDirty(false);

    gridAmp.setValues(amp, TestHorizonModel.ZNULL, Unit.SEISMIC_AMPLITUDE);
    gridAmp.setZDomain(Unit.SEISMIC_AMPLITUDE.getDomain());
    gridAmp.setDirty(false);

    // _depthModel.addFeatureReprensentation(grid2d); XXX seismic horizon needs to know this - parent child stuff. 

    _repository.add(gridZ);
    _repository.add(gridAmp);
  }
}
