/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.example.generator.entity;


import java.util.ArrayList;
import java.util.List;

import org.geocraft.core.model.PointSet;
import org.geocraft.core.model.PointSetAttribute;
import org.geocraft.core.model.PointSetAttribute.Type;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geologicfeature.GeologicHorizon;
import org.geocraft.core.repository.IRepository;


/**
 * This class generates synthetic, in-memory pointsets.
 */
public final class PointSetGenerator {

  /** The repository in which to add pointsets. */
  private IRepository _repository;

  /** The horizon model to use when generating well picks. */
  private TestHorizonModel _horizonModel;

  /** The location to use as an origin. */
  private Point3d _origin;

  /** The coordinate system in which locations are defined. */
  private CoordinateSystem _coordSys;

  /**
   * Constructs a synthetic pointset generator.
   * 
   * @param repository the repository in which to add generated wells and their children.
   * @param origin the location to use an an origin.
   * @param coordSystem the coordinate system in which locations are defined.
   * @param horizonModel the horizon model to use when generating well picks.
   */
  public PointSetGenerator(final IRepository repository, final Point3d origin, final CoordinateSystem coordSys, final TestHorizonModel horizonModel) {
    _repository = repository;
    _origin = origin;
    _coordSys = coordSys;
    _horizonModel = horizonModel;
  }

  /**
   * Creates an in-memory pointset for a given geologic horizon.
   * 
   * @param pointsetNumber the pointset #.
   * @param width the size of the pointset bounding area in the x direction.
   * @param height the size of the pointset bounding area in the y direction.
   * @param horizon the geologic horizon.
   * @param horizonNumber the horizon #.
   */
  public void addHorizonPointSet(final int pointsetNumber, final double width, final double height,
      final GeologicHorizon horizon, int horizonNumber) {
    String pointsetName = horizon.getDisplayName() + "-Pointset" + pointsetNumber;
    addHorizonPointSet(pointsetName, width, height, horizonNumber);
  }

  /**
   * Creates an in-memory pointset and adds it to the repository.
   * 
   * @param pointsetName the pointset name.
   * @param width the size of the pointset bounding area in the x direction.
   * @param height the size of the pointset bounding area in the y direction.
   * @param horizon the geologic horizon.
   * @param horizonNumber the horizon number (0-n).
   */
  private void addHorizonPointSet(final String pointsetName, final double width, final double height,
      final int horizonNumber) {

    // Create an in-memory pointset.
    PointSet pointset = new PointSet(pointsetName);

    // Set the pointset attributes.
    pointset.setComment("test data");
    List<Point3d> points = new ArrayList<Point3d>();
    float[] attrValues = new float[200];
    for (int i = 0; i < 200; i++) {
      double x = _origin.getX() + (Math.random() * 2 - 1) * width * 5280;
      double y = _origin.getY() + (Math.random() * 2 - 1) * height * 5280;
      double z = _horizonModel.getHorizonZ(horizonNumber, x, y);
      pointset.addPoint(new Point3d(x, y, z));
      attrValues[i] = 100 * (float) Math.random() + 50.f;
    }
    pointset.setZUnit(Unit.FOOT);
    String attributeName = "net pay value";
    PointSetAttribute attribute = pointset.addAttribute(Type.FLOAT, attributeName);
    attribute.setFloats(attrValues);

    // Add the pointset to the repository.
    pointset.setDirty(false);
    _repository.add(pointset);
  }
}
