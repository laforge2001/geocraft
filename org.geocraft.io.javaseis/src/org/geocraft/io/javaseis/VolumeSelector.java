/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.javaseis;


import java.io.File;

import org.geocraft.core.common.util.FileUtil;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.CornerPointsSeries;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.PolygonUtil;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.datatypes.PolygonUtil.PolygonType;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.model.seismic.SurveyOrientation;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.core.session.MapperParameterStore;
import org.geocraft.ui.io.DatastoreDirectorySelector;
import org.javaseis.grid.BinGrid;
import org.javaseis.grid.GridDefinition;
import org.javaseis.io.Seisio;
import org.javaseis.io.VirtualFolders;
import org.javaseis.properties.AxisLabel;
import org.javaseis.properties.DataFormat;
import org.javaseis.util.SeisException;

import edu.mines.jtk.util.ParameterSet;


public class VolumeSelector extends DatastoreDirectorySelector {

  /** The logger. */
  private static ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(VolumeSelector.class);

  /**
   * The default constructor.
   */
  public VolumeSelector() {
    super("JavaSeis Volume", new String[] { "JavaSeis Volumes (.js)" }, new String[] { "*.js" },
        "LoadJavaSeisVolume_DIR");
  }

  @Override
  protected MapperModel[] createMapperModelsFromSelectedFiles(final File[] files) {
    int numFiles = files.length;
    VolumeMapperModel[] mapperModels = new VolumeMapperModel[numFiles];
    for (int i = 0; i < numFiles; i++) {
      mapperModels[i] = createMapperModel(files[i]);
    }
    return mapperModels;
  }

  /**
   * Scans the specified JavaSeis volume file and returns a mapper model of datastore properties.
   * @param file the JavaSeis volume file.
   * @return the mapper model of datastore properties.
   */
  public static VolumeMapperModel createMapperModel(final File file) {
    // Create a new mapper model.
    VolumeMapperModel model = new VolumeMapperModel();
    String filePath = file.getAbsolutePath();
    model.setDirectory(FileUtil.getPathName(filePath));
    model.setFileName(FileUtil.getShortName(filePath));
    model.setDataUnit(Unit.SEISMIC_AMPLITUDE);

    // Restore the previously specified settings.
    MapperParameterStore.restore(model);

    // Override certain previously specified settings with current datastore settings.
    try {
      Seisio seisio = new Seisio(file.getAbsolutePath());
      seisio.open("r");

      GridDefinition gridDef = seisio.getGridDefinition();
      DataFormat dataFormat = seisio.getDataDefinition().getTraceFormat();

      AxisLabel[] axisLabels = gridDef.getAxisLabels();
      // Determine if the data set is polar plot data
      boolean polarData = false;
      for (AxisLabel currentLabel : axisLabels) {
        if (currentLabel.getName().equalsIgnoreCase("AZI_IDX")) {
          polarData = true;
          break;
        }
      }

      int numDimensions = gridDef.getNumDimensions();
      if (numDimensions < 3 || numDimensions > 4) {
        throw new UnsupportedOperationException(numDimensions + " not yet supported.");
      }

      model.setDataFormat(dataFormat.toString());

      BinGrid binGrid = null;
      try {
        binGrid = seisio.getBinGrid();
        model.setBinGridExists(true);
      } catch (NullPointerException e) {
        // TODO: this is a hack until the JavaSeis API returns null if the bin grid does not exist.
        model.setBinGridExists(false);
      }

      boolean isVirtual = seisio.isVirtual();
      model.setSecondaryStorageFlag(isVirtual);
      if (isVirtual) {
        VirtualFolders folders = seisio.getVirtualFolders();
        model.setVirtualFoldersLoc(folders.getSecondary());
        model.setNumExtents(folders.count());
        for (String secn : folders.getSecondary()) {
          System.out.println("secn: " + secn);
        }
      }

      if (numDimensions == 3) {
        PostStack3d.StorageOrder poststackStorageOrder = JavaSeisUtil.computePostStackStorageOrder(gridDef);
        Unit zUnit = JavaSeisUtil.getAxisUnit(gridDef, JavaSeisUtil.getZAxis(poststackStorageOrder));
        Domain zDomain = JavaSeisUtil.getZAxisDomain(gridDef, poststackStorageOrder);
        model.setUnitOfZ(zUnit);
        model.setStorageOrder(poststackStorageOrder.getTitle());

        int inlineAxis = JavaSeisUtil.getInlineAxis(poststackStorageOrder);
        int xlineAxis = JavaSeisUtil.getCrosslineAxis(poststackStorageOrder);
        long numInlines = gridDef.getAxisLength(inlineAxis);
        long numXlines = gridDef.getAxisLength(xlineAxis);
        model.setInlineStart(JavaSeisUtil.findInlineLogicalCoordinate(gridDef, 0, poststackStorageOrder));
        model.setInlineEnd(JavaSeisUtil.findInlineLogicalCoordinate(gridDef, (int) (numInlines - 1),
            poststackStorageOrder));
        model.setInlineDelta(JavaSeisUtil.findInlineLogicalDelta(gridDef, poststackStorageOrder));
        model.setXlineStart(JavaSeisUtil.findCrosslineLogicalCoordinate(gridDef, 0, poststackStorageOrder));
        model.setXlineEnd(JavaSeisUtil.findCrosslineLogicalCoordinate(gridDef, (int) (numXlines - 1),
            poststackStorageOrder));
        model.setXlineDelta(JavaSeisUtil.findCrosslineLogicalDelta(gridDef, poststackStorageOrder));

        Unit xlineUnit = JavaSeisUtil.getAxisUnit(gridDef, JavaSeisUtil.getCrosslineAxis(poststackStorageOrder));
        Unit inlineUnit = JavaSeisUtil.getAxisUnit(gridDef, JavaSeisUtil.getInlineAxis(poststackStorageOrder));
        Point3d[] cornerPoints = JavaSeisUtil.getCornerPoints(gridDef, binGrid, inlineUnit, xlineUnit,
            poststackStorageOrder, zDomain);
        if (!xlineUnit.equals(inlineUnit)) {
          throw new IllegalArgumentException("The inline and x-line units do not match.");
        }

        model.setUnitOfXY(inlineUnit);

        double x00 = cornerPoints[0].getX();
        double y00 = cornerPoints[0].getY();
        double x10 = cornerPoints[1].getX();
        double y10 = cornerPoints[1].getY();
        double x01 = cornerPoints[3].getX();
        double y01 = cornerPoints[3].getY();

        if (!model.getBinGridExists()) {
          // Ignore geometry data if polar plot data
          if (!polarData) {
            ParameterSet geometryParms = seisio.getCustomProperties().getParameterSet("Geometry");
            if (geometryParms != null) {
              x00 = geometryParms.getFloat("xILine1Start", (float) x00);
              y00 = geometryParms.getFloat("yILine1Start", (float) y00);
              x10 = geometryParms.getFloat("xXLine1End", (float) x10);
              y10 = geometryParms.getFloat("yXLine1End", (float) y10);
              x01 = geometryParms.getFloat("xILine1End", (float) x01);
              y01 = geometryParms.getFloat("yILine1End", (float) y01);
              int minInline = geometryParms.getInt("minILine", 1);
              int maxInline = geometryParms.getInt("maxILine", 1);
              int minXline = geometryParms.getInt("minXLine", 1);
              int maxXline = geometryParms.getInt("maxXLine", 1);
              int nInlines = geometryParms.getInt("nILines", 1);
              int nXlines = geometryParms.getInt("nXLines", 1);
              int incInline = (maxInline - minInline + 1) / nInlines;
              int incXline = (maxXline - minXline + 1) / nXlines;
              FloatRange inlineRange = new FloatRange(minInline, maxInline, incInline);
              FloatRange xlineRange = new FloatRange(minXline, maxXline, incXline);
              Point3d[] points = new Point3d[4];
              points[0] = new Point3d(x00, y00, 0);
              points[1] = new Point3d(x10, y10, 0);
              points[2] = new Point3d(x10 + x01 - x00, y10 + y01 - y00, 0);
              points[3] = new Point3d(x01, y01, 0);
              CoordinateSystem coordSystem = new CoordinateSystem("", zDomain);
              CornerPointsSeries cornerPointSeries = CornerPointsSeries.create(points, coordSystem);
              PolygonType direction = PolygonUtil.getDirection(cornerPointSeries);
              model.setCornerPointDirection(direction);
              SurveyOrientation orientation = SurveyOrientation.ROW_IS_INLINE;
              if (direction.equals(PolygonType.Clockwise)) {
                orientation = SurveyOrientation.ROW_IS_INLINE;
                // Flip the order of the points to be counter-clockwise.
                Point3d temp = points[1];
                points[1] = points[3];
                points[3] = temp;
                cornerPointSeries = CornerPointsSeries.create(points, coordSystem);
              } else if (direction.equals(PolygonType.CounterClockwise)) {
                orientation = SurveyOrientation.ROW_IS_XLINE;
              } else {
                throw new RuntimeException("Invalid corner points for survey geometry.");
              }
              SeismicSurvey3d geometry = new SeismicSurvey3d("", inlineRange, xlineRange, cornerPointSeries,
                  orientation);
              float[] inlines = { model.getInlineStart(), model.getInlineEnd(), model.getInlineStart() };
              float[] xlines = { model.getXlineStart(), model.getXlineStart(), model.getXlineEnd() };
              double[] xs = new double[3];
              double[] ys = new double[3];
              for (int i = 0; i < 3; i++) {
                double[] xy = geometry.transformInlineXlineToXY(inlines[i], xlines[i]);
                xs[i] = xy[0];
                ys[i] = xy[1];
              }
              x00 = xs[0];
              y00 = ys[0];
              x10 = xs[1];
              y10 = ys[1];
              x01 = xs[2];
              y01 = ys[2];
            }
          }
        }

        model.setX0((float) x00);
        model.setY0((float) y00);
        model.setX1((float) x10);
        model.setY1((float) y10);
        model.setX2((float) x01);
        model.setY2((float) y01);

        model.setVolumeType("PostStack3d");
      } else if (numDimensions == 4) {
        PreStack3d.StorageOrder prestackStorageOrder = JavaSeisUtil.computePreStackStorageOrder(gridDef);
        Unit zUnit = JavaSeisUtil.getAxisUnit(gridDef, JavaSeisUtil.getZAxis(prestackStorageOrder));
        Domain zDomain = JavaSeisUtil.getZAxisDomain(gridDef, prestackStorageOrder);
        model.setUnitOfZ(zUnit);
        model.setStorageOrder(prestackStorageOrder.getName());

        int inlineAxis = JavaSeisUtil.getInlineAxis(prestackStorageOrder);
        int xlineAxis = JavaSeisUtil.getCrosslineAxis(prestackStorageOrder);
        int offsetAxis = JavaSeisUtil.getOffsetAxis(prestackStorageOrder);
        long numInlines = gridDef.getAxisLength(inlineAxis);
        long numXlines = gridDef.getAxisLength(xlineAxis);
        long numOffsets = gridDef.getAxisLength(offsetAxis);
        model.setInlineStart(JavaSeisUtil.findInlineLogicalCoordinate(gridDef, 0, prestackStorageOrder));
        model.setInlineEnd(JavaSeisUtil.findInlineLogicalCoordinate(gridDef, (int) (numInlines - 1),
            prestackStorageOrder));
        model.setInlineDelta(JavaSeisUtil.findInlineLogicalDelta(gridDef, prestackStorageOrder));
        model.setXlineStart(JavaSeisUtil.findCrosslineLogicalCoordinate(gridDef, 0, prestackStorageOrder));
        model.setXlineEnd(JavaSeisUtil.findCrosslineLogicalCoordinate(gridDef, (int) (numXlines - 1),
            prestackStorageOrder));
        model.setXlineDelta(JavaSeisUtil.findCrosslineLogicalDelta(gridDef, prestackStorageOrder));
        model.setOffsetStart((float) JavaSeisUtil.findOffsetPhysicalCoordinate(gridDef, 0, prestackStorageOrder));
        model.setOffsetEnd((float) JavaSeisUtil.findOffsetPhysicalCoordinate(gridDef, (int) (numOffsets - 1),
            prestackStorageOrder));
        model.setOffsetDelta((float) JavaSeisUtil.findOffsetPhysicalDelta(gridDef, prestackStorageOrder));

        Unit xlineUnit = JavaSeisUtil.getAxisUnit(gridDef, JavaSeisUtil.getCrosslineAxis(prestackStorageOrder));
        Unit inlineUnit = JavaSeisUtil.getAxisUnit(gridDef, JavaSeisUtil.getInlineAxis(prestackStorageOrder));
        Point3d[] cornerPoints = JavaSeisUtil.getCornerPoints(gridDef, binGrid, inlineUnit, xlineUnit,
            prestackStorageOrder, zDomain);
        if (!xlineUnit.equals(inlineUnit)) {
          throw new IllegalArgumentException("The inline and x-line units do not match.");
        }

        model.setUnitOfXY(inlineUnit);

        double x00 = cornerPoints[0].getX();
        double y00 = cornerPoints[0].getY();
        double x10 = cornerPoints[1].getX();
        double y10 = cornerPoints[1].getY();
        double x01 = cornerPoints[3].getX();
        double y01 = cornerPoints[3].getY();

        if (!model.getBinGridExists()) {
          ParameterSet geometryParms = seisio.getCustomProperties().getParameterSet("Geometry");
          if (geometryParms != null) {
            x00 = geometryParms.getFloat("xILine1Start", (float) x00);
            y00 = geometryParms.getFloat("yILine1Start", (float) y00);
            x10 = geometryParms.getFloat("xXLine1End", (float) x10);
            y10 = geometryParms.getFloat("yXLine1End", (float) y10);
            x01 = geometryParms.getFloat("xILine1End", (float) x01);
            y01 = geometryParms.getFloat("yILine1End", (float) y01);
            int minInline = geometryParms.getInt("minILine", 1);
            int maxInline = geometryParms.getInt("maxILine", 1);
            int minXline = geometryParms.getInt("minXLine", 1);
            int maxXline = geometryParms.getInt("maxXLine", 1);
            int nInlines = geometryParms.getInt("nILines", 1);
            int nXlines = geometryParms.getInt("nXLines", 1);
            int incInline = (maxInline - minInline + 1) / nInlines;
            int incXline = (maxXline - minXline + 1) / nXlines;
            FloatRange inlineRange = new FloatRange(minInline, maxInline, incInline);
            FloatRange xlineRange = new FloatRange(minXline, maxXline, incXline);
            Point3d[] points = new Point3d[4];
            points[0] = new Point3d(x00, y00, 0);
            points[1] = new Point3d(x10, y10, 0);
            points[2] = new Point3d(x10 + x01 - x00, y10 + y01 - y00, 0);
            points[3] = new Point3d(x01, y01, 0);
            CoordinateSystem coordSystem = new CoordinateSystem("", zDomain);
            CornerPointsSeries cornerPointSeries = CornerPointsSeries.create(points, coordSystem);
            PolygonType direction = PolygonUtil.getDirection(cornerPointSeries);
            model.setCornerPointDirection(direction);
            SurveyOrientation orientation = SurveyOrientation.ROW_IS_INLINE;
            if (direction.equals(PolygonType.Clockwise)) {
              orientation = SurveyOrientation.ROW_IS_INLINE;
              // Flip the order of the points to be counter-clockwise.
              Point3d temp = points[1];
              points[1] = points[3];
              points[3] = temp;
              cornerPointSeries = CornerPointsSeries.create(points, coordSystem);
            } else if (direction.equals(PolygonType.CounterClockwise)) {
              orientation = SurveyOrientation.ROW_IS_XLINE;
            } else {
              throw new RuntimeException("Invalid corner points for survey geometry.");
            }
            SeismicSurvey3d geometry = new SeismicSurvey3d("", inlineRange, xlineRange, cornerPointSeries, orientation);
            float[] inlines = { model.getInlineStart(), model.getInlineEnd(), model.getInlineStart() };
            float[] xlines = { model.getXlineStart(), model.getXlineStart(), model.getXlineEnd() };
            double[] xs = new double[3];
            double[] ys = new double[3];
            for (int i = 0; i < 3; i++) {
              double[] xy = geometry.transformInlineXlineToXY(inlines[i], xlines[i]);
              xs[i] = xy[0];
              ys[i] = xy[1];
            }
            x00 = xs[0];
            y00 = ys[0];
            x10 = xs[1];
            y10 = ys[1];
            x01 = xs[2];
            y01 = ys[2];
          }
        }

        model.setX0((float) x00);
        model.setY0((float) y00);
        model.setX1((float) x10);
        model.setY1((float) y10);
        model.setX2((float) x01);
        model.setY2((float) y01);

        model.setVolumeType("PreStack3d");
      }

      seisio.close();
    } catch (SeisException ex) {
      LOGGER.error("Problem loading " + file.getAbsolutePath() + " " + ex.toString(), ex);
    }

    // Update the mapper model from the meta-data.
    //updateMapperModelFromMetaData(model);

    return model;
  }
}
