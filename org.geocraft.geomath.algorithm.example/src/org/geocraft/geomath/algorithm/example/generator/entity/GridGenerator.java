/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.example.generator.entity;


import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.CornerPointsSeries;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geologicfeature.GeologicHorizon;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.repository.IRepository;
import org.geocraft.geomath.algorithm.example.generator.TestDataGenerator;


public class GridGenerator {

  private IRepository _repository;

  private Point3d _origin;

  private CoordinateSystem _coordSys;

  private TestHorizonModel _horizonModel;

  public GridGenerator(final IRepository repository, final Point3d origin, final CoordinateSystem coordSys, final TestHorizonModel horizonModel) {
    _repository = repository;
    _origin = origin;
    _coordSys = coordSys;
    _horizonModel = horizonModel;
  }

  public void addGrid3d(final int gridNum, final GeologicHorizon[] horizons) {
    int horzNum = 0;
    for (GeologicHorizon horizon : horizons) {
      addGrid3d(gridNum, horizon, horzNum++);
    }
  }

  public void addGrid3d(final int gridNum, final GeologicHorizon horizon, final int horizonNum) {

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
     *      col0        colN
     */
    int nRows = 500 + (int) (100 * Math.random());
    int nCols = 700 + (int) (100 * Math.random());

    int columnOffset = (nRows - 1) * 10;
    int rowOffset = (nCols - 1) * 10;

    double xmin = _origin.getX() + 10000 * Math.random();
    double ymin = _origin.getY() + 10000 * Math.random();

    Point3d[] points = TestDataGenerator.createBoundary(xmin, ymin, rowOffset, columnOffset);

    CornerPointsSeries cornerPoints = CornerPointsSeries.createDirect(points, _coordSys);

    GridGeometry3d geometry = new GridGeometry3d(horizon.getDisplayName() + "-Grid" + gridNum, nRows, nCols,
        cornerPoints);

    Grid3d depthGrid = new Grid3d("Depth", geometry);
    depthGrid.setInterpreter("Bob");
    depthGrid.setComment("A synthetic grid");
    depthGrid.setDataSource("Test Data Generator");

    Grid3d thicknessGrid = new Grid3d("Isopach", geometry);

    float[][] z = new float[nRows][nCols];
    float[][] thickness = new float[nRows][nCols];

    double delY = (points[2].getY() - points[1].getY()) / nRows;
    double delX = (points[1].getX() - points[0].getX()) / nCols;

    for (int row = 0; row < nRows; row++) {
      for (int col = 0; col < nCols; col++) {
        double x = points[0].getX() + col * delX;
        double y = points[0].getY() + row * delY;
        z[row][col] = _horizonModel.getHorizonZ(horizonNum, x, y);
        thickness[row][col] = _horizonModel.getThickness(horizonNum, x, y);
      }
    }

    depthGrid.setValues(z, TestHorizonModel.ZNULL, Unit.FOOT);
    depthGrid.setZDomain(Domain.DISTANCE);
    depthGrid.setDirty(false);
    thicknessGrid.setValues(thickness, TestHorizonModel.ZNULL, Unit.FOOT);
    thicknessGrid.setZDomain(Domain.DISTANCE);
    thicknessGrid.setDirty(false);

    _repository.add(thicknessGrid);
    _repository.add(depthGrid);

  }
}
