/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.example.generator.entity;


import org.geocraft.core.factory.model.PostStack2dFactory;
import org.geocraft.core.io.Grid2dInMemoryMapper;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.grid.Grid2d;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.seismic.PostStack2d;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.SeismicLine2d;
import org.geocraft.core.model.seismic.SeismicSurvey2d;
import org.geocraft.core.model.seismic.SimpleSeismicLineCoordinateTransform;
import org.geocraft.core.repository.IRepository;


public class PostStack2dGenerator {

  IRepository _repository;

  CoordinateSystem _coordinateSystem;

  public PostStack2dGenerator(final IRepository repository, final CoordinateSystem coordinateSystem) {
    _repository = repository;
    _coordinateSystem = coordinateSystem;
  }

  public void addSurvey(final int surveyNum) {

    int numLines = 20 + (int) (10 * Math.random()); // TODO this should be 20 but it hangs
    String surveyName = "2D Survey: " + surveyNum;
    String poststackName = "Foo" + surveyNum;

    SeismicLine2d[] seismicLines = new SeismicLine2d[numLines];
    String[] lineNames = new String[numLines];
    int[] lineNumbers = new int[numLines];
    FloatRange[] cdpRange = new FloatRange[numLines];
    FloatRange[] shotRange = new FloatRange[numLines];

    for (int i = 0; i < numLines; i++) {
      // TODO this should be 5000 which doesn't render at all. 
      // It does render, but it is so far away, so it not visible
      int numControlPoints = 2 + (int) (50 * Math.random());
      cdpRange[i] = new FloatRange(1, numControlPoints, 1);
      shotRange[i] = new FloatRange(100, 100 + numControlPoints - 1, 1);
      lineNumbers[i] = 10 * i + 1000;
      lineNames[i] = "Line " + lineNumbers[i];
      seismicLines[i] = new SeismicLine2d(lineNames[i], lineNumbers[i], cdpRange[i], shotRange[i].getStart(),
          shotRange[i].getEnd(), getControlPoints(numControlPoints, i), new SimpleSeismicLineCoordinateTransform(
              cdpRange[i], shotRange[i]));
    }

    SeismicSurvey2d survey = new SeismicSurvey2d(surveyName, seismicLines);

    PostStack2d poststack = PostStack2dFactory.createInMemory(poststackName, survey, Domain.TIME);
    Object[] poststackLines = new Object[numLines];
    Object[] grids = new Object[numLines];
    //    System.out.println(numLines);
    for (int i = 0; i < numLines; i++) {

      // add the seismic data ...
      String uniqueID = "Test Data PostStack2dLine " + poststackName + "-" + lineNames[i] + " "
          + System.currentTimeMillis();
      PostStack2dLine poststackLine = new PostStack2dLine(poststackName + "-" + lineNames[i], new PostStack2dMapper(
          uniqueID), survey, lineNames[i], lineNumbers[i], poststack);

      poststackLine.setCdpRange(cdpRange[i].getStart(), cdpRange[i].getEnd(), cdpRange[i].getDelta());
      poststackLine.setZRangeAndDelta(0, 6000, 4);
      poststackLine.setProjectName("Bar");
      poststackLine.setZDomain(Domain.TIME);
      poststackLines[i] = poststackLine;
      poststack.addPostStack2dLine(lineNumbers[i], poststackLine);
      //      _repository.add(ps2d);

      // add the horizon picks .... 
      Grid2d grid = new Grid2d(poststackName + "-" + lineNames[i], new Grid2dInMemoryMapper(), survey);

      // create a dc offset between 1000 and 5000 milliseconds 
      float offset = (float) (1000 + 4000 * Math.random());
      float[] picks = new float[seismicLines[i].getNumBins()];
      for (int j = 0; j < seismicLines[i].getNumBins(); j++) {
        // create a sinusoid to represent the horizon
        picks[j] = (float) (offset + (Math.random() - 0.5) * 500 * Math.sin(j * 314.0 / seismicLines[i].getNumBins()));
      }

      grid.setDataUnit(UnitPreferences.getInstance().getTimeUnit());
      grid.setNullValue(999f);
      grid.putValues(seismicLines[i].getNumber(), picks);
      grids[i] = grid;
      //      _repository.add(la);
    }
    _repository.add(poststack);
    _repository.add(poststackLines);
    _repository.add(grids);
  }

  private CoordinateSeries getControlPoints(final int numControlPoints, final int lineNum) {
    Point3d[] points = new Point3d[numControlPoints];

    // default direction is /
    int direction = 1;
    int startX = 50000;
    if (lineNum % 2 == 0) {
      // but draw some lines like \
      direction = -1;
      startX = 50000 + numControlPoints * 1000;
    }

    for (int i = 0; i < numControlPoints; i++) {
      points[i] = new Point3d(startX + direction * i * 1000, 10000000 + (lineNum + i) * 1200, 0);
    }

    // switch the direction of some of the lines ..... 
    if (Math.random() > 0.5) {
      Point3d[] newPoints = new Point3d[numControlPoints];
      for (int i = 0; i < numControlPoints; i++) {
        newPoints[i] = points[numControlPoints - i - 1];
      }
      points = newPoints;
    }

    return CoordinateSeries.createDirect(points, _coordinateSystem);

  }

}
