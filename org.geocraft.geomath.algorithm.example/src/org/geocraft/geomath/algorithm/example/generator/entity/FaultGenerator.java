/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.example.generator.entity;


import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.PolylinePick;
import org.geocraft.core.model.fault.FaultInterpretation;
import org.geocraft.core.model.fault.FaultType;
import org.geocraft.core.model.geologicfeature.GeologicFault;
import org.geocraft.core.model.mapper.InMemoryMapper;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.repository.IRepository;


public class FaultGenerator {

  private IRepository _repository;

  private CoordinateSystem _coordSys;

  private Color[] colors = { Color.red, Color.blue, Color.green, Color.yellow, Color.orange, Color.magenta };

  public FaultGenerator(final IRepository repository, final CoordinateSystem coordSys) {
    _repository = repository;
    _coordSys = coordSys;
  }

  public void addFault(final SeismicSurvey3d survey, final int faultNum) {

    GeologicFault geologicFault = new GeologicFault("Fault" + faultNum);

    geologicFault.setFaultType(FaultType.NORMAL);

    // fault.setColor(colors[faultNum % 6]);

    FaultInterpretation fault = new FaultInterpretation("Fault" + faultNum, new InMemoryMapper(
        FaultInterpretation.class));
    //PolylineSet polylineSet = new PolylineSet("Fault" + faultNum, geologicFault, Domain.DISTANCE, new FloatMeasurement(
    //    0.0f, Unit.FOOT));

    float[] inlines = new float[1];
    float[] xlines = new float[1];

    // xlines[0] = ( survey.getXlineStart() + survey.getXlineEnd())/ 2;

    float slope = (float) Math.random();
    float inLineStart = (float) (survey.getInlineStart() + Math.random() * survey.getNumInlines()
        * survey.getInlineDelta());
    float xLineStart = (float) (survey.getXlineStart() + Math.random() * survey.getNumXlines() * survey.getXlineDelta());

    for (int i = 0; i < survey.getNumInlines(); i = i + 5) {

      inlines[0] = inLineStart + i * survey.getInlineDelta();
      xlines[0] = xLineStart + slope * i * survey.getXlineDelta();

      int nPts = 20; // number of pts in polyline
      //Point3d[] points = new Point3d[nPts];

      double x = 0;
      double y = 0;

      try {
        x = survey.transformInlineXlineToXY(inlines, xlines).getX(0);
        y = survey.transformInlineXlineToXY(inlines, xlines).getY(0);
      } catch (Exception e) {
        e.printStackTrace();
      }

      List<Point3d> pickPoints = new ArrayList<Point3d>();
      for (int j = 0; j < nPts; j++) {
        pickPoints.add(new Point3d(x, y - 10 * j * j, j * 200));
      }

      //CoordinateSeries coordSeries = CoordinateSeries.createDirect(points, _coordSys);

      //Polyline polyline = new Polyline("Fault" + faultNum + "-segment" + i, polylineSet, coordSeries);

      //polylineSet.addPolyline(polyline);

      String pickName = "Fault" + faultNum + "-segment" + (i + 1);
      PolylinePick polylinePick = new PolylinePick(pickName, Domain.TIME, pickPoints);
      fault.addPickSegment(pickName, polylinePick);
    }
    fault.setZDomain(Domain.TIME);
    fault.setDisplayColor(new RGB(0, 255, 0));
    fault.setGeologicFeature(geologicFault);
    fault.setDirty(false);

    _repository.add(geologicFault);
    _repository.add(fault);

  }
}
