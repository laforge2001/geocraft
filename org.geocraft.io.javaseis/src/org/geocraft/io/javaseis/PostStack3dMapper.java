package org.geocraft.io.javaseis;


import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.CornerPointsSeries;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.Header;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.PolygonUtil.PolygonType;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.Trace.Status;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.TraceHeaderCatalog;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.IPostStack3dMapper;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PostStack3d.SliceBufferOrder;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;
import org.geocraft.core.model.seismic.SeismicDataset.StorageFormat;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.model.seismic.SurveyOrientation;
import org.javaseis.grid.BinGrid;
import org.javaseis.grid.GridDefinition;
import org.javaseis.io.ExtentPolicyMinMax;
import org.javaseis.io.Seisio;
import org.javaseis.io.VirtualFoldersSimple;
import org.javaseis.properties.AxisDefinition;
import org.javaseis.properties.AxisLabel;
import org.javaseis.properties.DataDefinition;
import org.javaseis.properties.DataDomain;
import org.javaseis.properties.DataFormat;
import org.javaseis.properties.DataType;
import org.javaseis.properties.TraceProperties;
import org.javaseis.properties.Units;
import org.javaseis.util.SeisException;


public class PostStack3dMapper extends SeismicDatasetMapper<PostStack3d> implements IPostStack3dMapper {

  private StorageOrder _storageOrder;

  private Unit _inlineUnit;

  private Unit _xlineUnit;

  public PostStack3dMapper(final VolumeMapperModel model) {
    super(model);

    String storageOrderStr = _model.getStorageOrder();
    if (!storageOrderStr.equals(StorageOrder.AUTO_CALCULATED.getTitle())) {
      _storageOrder = StorageOrder.lookupByName(storageOrderStr);
    }
  }

  @Override
  public PostStack3dMapper factory(final MapperModel mapperModel) {
    return new PostStack3dMapper((VolumeMapperModel) mapperModel);
  }

  public StorageOrder getStorageOrder(final PostStack3d ps3d) {
    return _storageOrder;
  }

  public void setStorageOrder(final StorageOrder storageOrder) {
    _storageOrder = storageOrder;
  }

  @Override
  protected void readFromStore(final PostStack3d ps3d) {
    _status = open("r");
    if (_status != null) {
      GridDefinition gridDef = _seisio.getGridDefinition();
      int[] lengths = JavaSeisUtil.getAxisLengthsForPostStack3d(_seisio.getGridDefinition());
      // Update the PostStack3d entity.
      File file = new File(_model.getDirectory() + File.separator + _model.getFileName());
      ps3d.setZDomain(_zDomain);
      int zAxis = JavaSeisUtil.getZAxis(_storageOrder);
      int inlineAxis = JavaSeisUtil.getInlineAxis(_storageOrder);
      int xlineAxis = JavaSeisUtil.getCrosslineAxis(_storageOrder);
      float zStart = (float) JavaSeisUtil.findZPhysicalCoordinate(gridDef, 0, _storageOrder);
      float zEnd = (float) JavaSeisUtil.findZPhysicalCoordinate(gridDef, lengths[zAxis] - 1, _storageOrder);
      float zDelta = (float) JavaSeisUtil.findZPhysicalDelta(gridDef, _storageOrder);

      UnitPreferences unitPrefs = UnitPreferences.getInstance();
      Unit zUnitTime = unitPrefs.getTimeUnit();
      Unit zUnitDepth = unitPrefs.getVerticalDistanceUnit();

      // Get the z-units, based on the domain.
      Unit zUnit = Unit.UNDEFINED;
      if (_zDomain.equals(Domain.TIME)) {
        zUnit = zUnitTime;
      } else if (_zDomain.equals(Domain.DISTANCE)) {
        zUnit = zUnitDepth;
      } else {
        throw new RuntimeException("Invalid domain type (must be TIME or DEPTH)");
      }
      zStart = Unit.convert(zStart, _model.getUnitOfZ(), zUnit);
      zEnd = Unit.convert(zEnd, _model.getUnitOfZ(), zUnit);
      zDelta = Unit.convert(zDelta, _model.getUnitOfZ(), zUnit);

      ps3d.setZRangeAndDelta(zStart, zEnd, zDelta);

      //TODO setting the z maximum possible range to match the z range just calculated
      //is this correct ?
      ps3d.setZMaxRangeAndDelta(zStart, zEnd, zDelta);

      ps3d.setLastModifiedDate(new Timestamp(file.lastModified()));
      ps3d.setProjectName("");
      Unit dataUnit = _model.getDataUnit();
      ps3d.setDataUnit(dataUnit);
      ps3d.setTraceHeaderDefinition(_headerDef);
      float inlineStart = JavaSeisUtil.findInlineLogicalCoordinate(gridDef, 0, _storageOrder);
      float inlineEnd = JavaSeisUtil.findInlineLogicalCoordinate(gridDef, lengths[inlineAxis] - 1, _storageOrder);
      float inlineDelta = JavaSeisUtil.findInlineLogicalDelta(gridDef, _storageOrder);
      ps3d.setInlineRangeAndDelta(inlineStart, inlineEnd, inlineDelta);
      float xlineStart = JavaSeisUtil.findCrosslineLogicalCoordinate(gridDef, 0, _storageOrder);
      float xlineEnd = JavaSeisUtil.findCrosslineLogicalCoordinate(gridDef, lengths[xlineAxis] - 1, _storageOrder);
      float xlineDelta = JavaSeisUtil.findCrosslineLogicalDelta(gridDef, _storageOrder);
      ps3d.setXlineRangeAndDelta(xlineStart, xlineEnd, xlineDelta);
      ps3d.setElevationDatum(0);

      // Create a seismic geometry in which to put the poststack.
      if (ps3d.getSurvey() == null) {
        Point3d[] points = new Point3d[4];
        Unit xyUnitPref = UnitPreferences.getInstance().getHorizontalDistanceUnit();
        double x0 = Unit.convert(_model.getX0(), _model.getUnitOfXY(), xyUnitPref);
        double y0 = Unit.convert(_model.getY0(), _model.getUnitOfXY(), xyUnitPref);
        double x1 = Unit.convert(_model.getX1(), _model.getUnitOfXY(), xyUnitPref);
        double y1 = Unit.convert(_model.getY1(), _model.getUnitOfXY(), xyUnitPref);
        double x2 = Unit.convert(_model.getX2(), _model.getUnitOfXY(), xyUnitPref);
        double y2 = Unit.convert(_model.getY2(), _model.getUnitOfXY(), xyUnitPref);

        points[0] = new Point3d(x0, y0, 0);
        points[1] = new Point3d(x1, y1, 0);
        points[3] = new Point3d(x2, y2, 0);
        double dx = x1 - x0 + x2 - x0;
        double dy = y1 - y0 + y2 - y0;
        points[2] = new Point3d(x0 + dx, y0 + dy, 0);
        CoordinateSystem coordSystem = new CoordinateSystem("", _zDomain);
        CornerPointsSeries cornerPoints = CornerPointsSeries.create(points, coordSystem);
        //        PolygonType direction = PolygonUtil.getDirection(cornerPoints);
        PolygonType direction = _model.getCornerPointDirection();
        SurveyOrientation orientation = SurveyOrientation.ROW_IS_INLINE;
        if (direction.equals(PolygonType.Clockwise)) {
          orientation = SurveyOrientation.ROW_IS_INLINE;
          // Flip the order of the points to be counter-clockwise.
          Point3d temp = points[1];
          points[1] = points[3];
          points[3] = temp;
          cornerPoints = CornerPointsSeries.create(points, coordSystem);
        } else if (direction.equals(PolygonType.CounterClockwise)) {
          orientation = SurveyOrientation.ROW_IS_XLINE;
        } else {
          throw new RuntimeException("Invalid corner points for survey geometry.");
        }
        FloatRange inlineRange = new FloatRange(JavaSeisUtil.findInlineLogicalCoordinate(gridDef, 0, _storageOrder),
            JavaSeisUtil.findInlineLogicalCoordinate(gridDef, lengths[inlineAxis] - 1, _storageOrder),
            JavaSeisUtil.findInlineLogicalDelta(gridDef, _storageOrder));
        FloatRange xlineRange = new FloatRange(JavaSeisUtil.findCrosslineLogicalCoordinate(gridDef, 0, _storageOrder),
            JavaSeisUtil.findCrosslineLogicalCoordinate(gridDef, lengths[xlineAxis] - 1, _storageOrder),
            JavaSeisUtil.findCrosslineLogicalDelta(gridDef, _storageOrder));

        SeismicSurvey3d seismicGeometry = new SeismicSurvey3d("Survey - " + ps3d.getDisplayName(), inlineRange,
            xlineRange, cornerPoints, orientation);
        ps3d.setSurvey(seismicGeometry);
      }
      ps3d.setDirty(false);
    }
  }

  @Override
  protected void updateInStore(final PostStack3d ps3d) throws IOException {
    SeismicSurvey3d survey = ps3d.getSurvey();
    float[] inlines = { survey.getInlineStart() };
    float[] xlines = { survey.getXlineStart() };
    CoordinateSeries coords00 = survey.transformInlineXlineToXY(inlines, xlines);
    inlines[0] = survey.getInlineStart() + survey.getInlineDelta();
    CoordinateSeries coords10 = survey.transformInlineXlineToXY(inlines, xlines);
    inlines[0] = survey.getInlineStart();
    xlines[0] = survey.getXlineStart() + survey.getXlineDelta();
    CoordinateSeries coords01 = survey.transformInlineXlineToXY(inlines, xlines);
    Point3d p00 = coords00.getPoint(0);
    Point3d p10 = coords10.getPoint(0);
    Point3d p01 = coords01.getPoint(0);
    double physicalInlineDelta = p00.distanceTo(p10);
    double physicalXlineDelta = p00.distanceTo(p01);
    // Allocate and create 3 JS axis definitions.
    AxisDefinition[] axisDefs = new AxisDefinition[3];
    Unit xyUnit = UnitPreferences.getInstance().getHorizontalDistanceUnit();
    Units xyUnitJS = Units.FEET;
    if (xyUnit.equals(Unit.FOOT)) {
      xyUnitJS = Units.FEET;
    } else if (xyUnit.equals(Unit.METER)) {
      xyUnitJS = Units.METERS;
    } else {
      throw new RuntimeException("Invalid application x,y units: " + xyUnit);
    }

    // Create the z axis based on the domain of the volume.
    AxisLabel zAxisLabel = AxisLabel.TIME;
    DataDomain zAxisDomain = DataDomain.TIME;
    Units zAxisUnits = Units.MILLISECONDS;

    float zStart = Unit.convert(ps3d.getZStart(), ps3d.getZUnit(), _model.getUnitOfZ());
    float zDelta = Unit.convert(ps3d.getZDelta(), ps3d.getZUnit(), _model.getUnitOfZ());

    AxisDefinition zAxis = new AxisDefinition(AxisLabel.TIME, Units.MILLISECONDS, DataDomain.TIME,
        ps3d.getNumSamplesPerTrace(), 0, 1, zStart, zDelta);
    if (ps3d.getZDomain().equals(Domain.DISTANCE)) {
      zAxisLabel = AxisLabel.DEPTH;
      zAxisDomain = DataDomain.DEPTH;
      zAxisUnits = Units.FEET;
      if (ps3d.getZUnit().equals(Unit.METER)) {
        zAxisUnits = Units.METERS;
      }
    }
    zAxis = new AxisDefinition(zAxisLabel, zAxisUnits, zAxisDomain, ps3d.getNumSamplesPerTrace(), 0, 1,
        ps3d.getZStart(), ps3d.getZDelta());

    AxisDefinition xlineAxis = new AxisDefinition(AxisLabel.CROSSLINE, xyUnitJS, DataDomain.SPACE,
        survey.getNumXlines(), (long) survey.getXlineStart(), (long) survey.getXlineDelta(), 0, physicalXlineDelta);
    AxisDefinition inlineAxis = new AxisDefinition(AxisLabel.INLINE, xyUnitJS, DataDomain.SPACE,
        survey.getNumInlines(), (long) survey.getInlineStart(), (long) survey.getInlineDelta(), 0, physicalInlineDelta);
    // Fill out the JS axis definitions array based on the desired storage order.
    if (_storageOrder.equals(StorageOrder.INLINE_XLINE_Z)) {
      axisDefs[0] = zAxis;
      axisDefs[1] = xlineAxis;
      axisDefs[2] = inlineAxis;
    } else if (_storageOrder.equals(StorageOrder.XLINE_INLINE_Z)) {
      axisDefs[0] = zAxis;
      axisDefs[1] = inlineAxis;
      axisDefs[2] = xlineAxis;
    } else if (_storageOrder.equals(StorageOrder.Z_INLINE_XLINE)) {
      axisDefs[0] = xlineAxis;
      axisDefs[1] = inlineAxis;
      axisDefs[2] = zAxis;
    } else if (_storageOrder.equals(StorageOrder.Z_XLINE_INLINE)) {
      axisDefs[0] = inlineAxis;
      axisDefs[1] = xlineAxis;
      axisDefs[2] = zAxis;
    }
    // Create a JS grid definition.
    GridDefinition gridDef = new GridDefinition(3, axisDefs);
    DataDefinition dataDef = new DataDefinition(DataType.STACK, DataFormat.get(_model.getDataFormat()));
    TraceProperties traceProps = JavaSeisUtil.buildTraceProperties(ps3d.getTraceHeaderDefinition());
    try {
      // Create a JS Seisio instance.
      _seisio = new Seisio(getFilePath(), gridDef, dataDef, traceProps);
      _seisio.setMapped();
      // Create a JS bin grid.
      CoordinateSeries cornerPoints = survey.getCornerPoints();
      SurveyOrientation orientation = survey.getOrientation();

      double worldX0 = 0;
      double worldY0 = 0;
      double worldX1 = 0;
      double worldY1 = 0;
      double worldX2 = 0;
      double worldY2 = cornerPoints.getY(3);
      if (orientation == SurveyOrientation.ROW_IS_INLINE) {
        worldX0 = cornerPoints.getX(0);
        worldY0 = cornerPoints.getY(0);
        worldX1 = cornerPoints.getX(1);
        worldY1 = cornerPoints.getY(1);
        worldX2 = cornerPoints.getX(3);
        worldY2 = cornerPoints.getY(3);
      } else if (orientation == SurveyOrientation.ROW_IS_XLINE) {
        worldX0 = cornerPoints.getX(0);
        worldY0 = cornerPoints.getY(0);
        worldX1 = cornerPoints.getX(3);
        worldY1 = cornerPoints.getY(3);
        worldX2 = cornerPoints.getX(1);
        worldY2 = cornerPoints.getY(1);
      }
      double distance01 = Math.sqrt(Math.pow(worldX1 - worldX0, 2) + Math.pow(worldY1 - worldY0, 2));
      double distance02 = Math.sqrt(Math.pow(worldX2 - worldX0, 2) + Math.pow(worldY2 - worldY0, 2));
      double gridDX = distance01 / (survey.getNumInlines() - 1);
      double gridDY = distance02 / (survey.getNumXlines() - 1);
      float[] coords0 = survey.transformXYToInlineXline(worldX0, worldY0, true);
      float[] coords1 = survey.transformXYToInlineXline(worldX1, worldY1, true);
      float[] coords2 = survey.transformXYToInlineXline(worldX2, worldY2, true);
      long logicalX0 = 0;
      long logicalY0 = 0;
      long logicalX1 = 0;
      long logicalY1 = 0;
      long logicalX2 = 0;
      long logicalY2 = 0;
      long logicalDX = 1;
      long logicalDY = 1;
      double gridX0 = 0;
      double gridY0 = 0;
      //if (_storageOrder.equals(StorageOrder.INLINE_XLINE_Z)) {
      logicalX0 = Math.round(coords0[0]);
      logicalY0 = Math.round(coords0[1]);
      logicalX1 = Math.round(coords1[0]);
      logicalY1 = Math.round(coords1[1]);
      logicalX2 = Math.round(coords2[0]);
      logicalY2 = Math.round(coords2[1]);
      logicalDX = Math.round(survey.getInlineDelta());
      logicalDY = Math.round(survey.getXlineDelta());
      //} else if (_storageOrder.equals(StorageOrder.XLINE_INLINE_Z)) {
      //        logicalX0 = Math.round(coords0[1]);
      //        logicalY0 = Math.round(coords0[0]);
      //        logicalX1 = Math.round(coords1[1]);
      //        logicalY1 = Math.round(coords1[0]);
      //        logicalX2 = Math.round(coords2[1]);
      //        logicalY2 = Math.round(coords2[0]);
      //        logicalDX = Math.round(geometry.getXlineDelta());
      //        logicalDY = Math.round(geometry.getInlineDelta());
      //} else {
      //  throw new RuntimeException("Storage order " + _storageOrder + " not yet supported.");
      //}
      BinGrid binGrid = new BinGrid(worldX0, worldY0, worldX1, worldY1, worldX2, worldY2, logicalX0, logicalY0,
          logicalX1, logicalY1, logicalX2, logicalY2, logicalDX, logicalDY, gridX0, gridY0, gridDX, gridDY);

      // Resolve the secondary storage issue.
      _seisio.writeBinGridToFileProperties(binGrid.toParameterSet());
      if (_model.getSecondaryStorageFlag()) {
        String[] paths = _model.getVirtualFoldersLoc();
        String[] dirs = new String[paths.length];
        for (int i = 0; i < paths.length; i++) {
          String dirName = paths[i] + File.separator + _model.getFileName();
          File dir = new File(dirName);
          if (!dir.exists()) {
            if (!dir.mkdir()) {
              throw new IOException("Could not create virtual folder: " + dirName);
            }
          }
          dirs[i] = dirName;
        }
        VirtualFoldersSimple folders = new VirtualFoldersSimple(getFilePath(), dirs, new ExtentPolicyMinMax());
        _seisio.create(folders, _model.getNumExtents());
      } else {
        _seisio.create();
      }
    } catch (SeisException ex) {
      throw new IOException(ex.toString());
    }
  }

  private String open(final String mode) {
    String retval = _status;
    if (retval != null) {
      if (retval.equals("r") && mode.equals("rw")) {
        // if mode has changed from read-only to read-write then force reopen
        try {
          _seisio.close();
        } catch (SeisException e) {
          getLogger().error(e.toString(), e);
        }
        retval = null;
      }
    }
    if (retval == null) {
      try {
        if (mode.equalsIgnoreCase("r")) {
          _seisio.open(Seisio.MODE_READ_ONLY);
          retval = "r";
        } else if (mode.equalsIgnoreCase("rw")) {
          _seisio.open(Seisio.MODE_READ_WRITE);
          retval = "rw";
        } else {
          throw new RuntimeException("Invalid open mode. Must be \'r\' or \'rw\'");
        }
        GridDefinition gridDef = _seisio.getGridDefinition();
        if (gridDef.getAxisLabels().length != 3) {
          throw new RuntimeException("Not a poststack 3d volume.");
        }
        setStorageOrder(JavaSeisUtil.computePostStackStorageOrder(_seisio.getGridDefinition()));
        //_zDomain = JavaSeisUtil.getZAxisDomain(gridDef, _storageOrder);
        //_zUnit = JavaSeisUtil.getAxisUnit(gridDef, JavaSeisUtil.getZAxis(_storageOrder));
        _xlineUnit = JavaSeisUtil.getAxisUnit(gridDef, JavaSeisUtil.getCrosslineAxis(_storageOrder));
        _inlineUnit = JavaSeisUtil.getAxisUnit(gridDef, JavaSeisUtil.getInlineAxis(_storageOrder));
      } catch (SeisException e) {
        getLogger().error(e.toString(), e);
      }
      _headerDef = JavaSeisUtil.buildHeaderDefinition(_seisio.getTraceProperties().getTraceProperties(), false, true);
    }
    return retval;
  }

  /**
   * Implement default getBrick method by delegating to getTraces
   */

  public TraceData getBrick(final PostStack3d ps3d, final float inlineStart, final float inlineEnd,
      final float xlineStart, final float xlineEnd, final float zStart, final float zEnd) {
    float xlineDelta = Math.abs(ps3d.getSurvey().getXlineDelta());
    float inlineDelta = Math.abs(ps3d.getSurvey().getInlineDelta());

    if (xlineEnd < xlineStart) {
      xlineDelta = -xlineDelta;
    }
    if (inlineEnd < inlineStart) {
      inlineDelta = -inlineDelta;
    }

    // Build the array of inlines/xlines to read.
    int numXlTraces = 1 + Math.round((xlineEnd - xlineStart) / xlineDelta);
    int numIlTraces = 1 + Math.round((inlineEnd - inlineStart) / inlineDelta);
    int numTraces = numXlTraces * numIlTraces;
    float[] inlines = new float[numTraces];
    float[] xlines = new float[numTraces];

    int ndx = 0;
    for (int l = 0; l < numIlTraces; l++) {
      float il = inlineStart + l * inlineDelta;
      for (int t = 0; t < numXlTraces; t++, ndx++) {
        inlines[ndx] = il;
        xlines[ndx] = xlineStart + t * xlineDelta;
      }
    }

    // Get the traces and return them.
    return getTraces(ps3d, inlines, xlines, zStart, zEnd);
  }

  public TraceData getInline(final PostStack3d ps3d, final float inline, final float xlineStart, final float xlineEnd,
      final float zStart, final float zEnd) {
    TraceData retval = null;
    _status = open("r");
    if (_status != null) {
      GridDefinition gridDef = _seisio.getGridDefinition();
      int inlineIndex = JavaSeisUtil.findInlineIndex(gridDef, inline, _storageOrder);
      int xlineStartIndex = JavaSeisUtil.findCrosslineIndex(gridDef, xlineStart, _storageOrder);
      int xlineEndIndex = JavaSeisUtil.findCrosslineIndex(gridDef, xlineEnd, _storageOrder);
      int xlineCount = Math.abs(xlineEndIndex - xlineStartIndex) + 1;
      int xlineIncr = xlineStartIndex > xlineEndIndex ? -1 : 1;
      Unit zUnit = ps3d.getZUnit();
      float zStart2 = Unit.convert(zStart, zUnit, _model.getUnitOfZ());
      float zEnd2 = Unit.convert(zEnd, zUnit, _model.getUnitOfZ());
      int zStartIndex = JavaSeisUtil.findZIndex(gridDef, zStart2, _storageOrder);
      int zEndIndex = JavaSeisUtil.findZIndex(gridDef, zEnd2, _storageOrder);
      int zCount = Math.abs(zEndIndex - zStartIndex) + 1;
      float zDelta = ps3d.getZDelta();//(float) JavaSeisUtil.findZPhysicalDelta(gridDef, _storageOrder);
      float[][] jsData = _seisio.getTraceDataArray();
      float[] samples = new float[zCount];
      Trace[] traces = new Trace[xlineCount];
      TraceProperties traceProps = _seisio.getTraceProperties();
      switch (_storageOrder) {
        case INLINE_XLINE_Z:
          // Most Efficient -> inline,xline,z storage order matches inline,xline,z read order.
          try {
            int count = 0;
            int[] position = JavaSeisUtil.orderPosition(0, 0, inlineIndex, _storageOrder);
            int numTracesRead = _seisio.readFrame(position);
            if (numTracesRead > 0) {
              for (int j = 0; j < xlineCount; j++) {
                int xlineIndex = xlineStartIndex + j * xlineIncr;
                float xline = xlineStart + j * ps3d.getXlineDelta();
                System.arraycopy(jsData[xlineIndex], zStartIndex, samples, 0, zCount);
                Status status = Status.Live;
                if (Trace.isDead(samples)) {
                  status = Status.Dead;
                }
                double[] xy = ps3d.getSurvey().transformInlineXlineToXY(inline, xline);
                Header header = JavaSeisUtil.buildHeader(_headerDef, traceProps, xlineIndex);
                header.putInteger(TraceHeaderCatalog.INLINE_NO, Math.round(inline));
                header.putInteger(TraceHeaderCatalog.XLINE_NO, Math.round(xline));
                header.putDouble(TraceHeaderCatalog.X, xy[0]);
                header.putDouble(TraceHeaderCatalog.Y, xy[1]);
                traces[count] = new Trace(zStart, zDelta, _model.getUnitOfZ(), Arrays.copyOf(samples, samples.length),
                    status, header);
                count++;
              }
            } else {
              for (int j = 0; j < xlineCount; j++) {
                int xlineIndex = xlineStartIndex + j * xlineIncr;
                float xline = xlineStart + j * ps3d.getXlineDelta();
                double[] xy = ps3d.getSurvey().transformInlineXlineToXY(inline, xline);
                Header header = JavaSeisUtil.buildHeader(_headerDef, traceProps, xlineIndex);
                header.putInteger(TraceHeaderCatalog.INLINE_NO, Math.round(inline));
                header.putInteger(TraceHeaderCatalog.XLINE_NO, Math.round(xline));
                header.putDouble(TraceHeaderCatalog.X, xy[0]);
                header.putDouble(TraceHeaderCatalog.Y, xy[1]);
                traces[count] = new Trace(zStart, zDelta, _model.getUnitOfZ(), new float[samples.length],
                    Status.Missing, header);
                count++;
              }
            }
            retval = new TraceData(traces);
          } catch (SeisException e) {
            getLogger().error(e.toString(), e);
          }
          break;
        case XLINE_INLINE_Z:
          // Less Efficent -> xline,inline,z storage order does not match inline,xline,z read order.
          long traceIndex;
          try {
            int count = 0;
            for (int j = 0; j < xlineCount; j++) {
              float xline = xlineStart + j * ps3d.getXlineDelta();
              int xlineIndex = xlineStartIndex + j * xlineIncr;
              traceIndex = JavaSeisUtil.findTraceIndex(gridDef, inlineIndex, xlineIndex, _storageOrder);
              _seisio.readTrace(traceIndex);
              System.arraycopy(jsData[0], zStartIndex, samples, 0, zCount);
              Status status = Status.Live;
              if (Trace.isDead(samples)) {
                status = Status.Dead;
              }
              double x = JavaSeisUtil.getXPhysicalCoordinate(gridDef, inlineIndex, xlineIndex, _inlineUnit, _xlineUnit,
                  _storageOrder);
              double y = JavaSeisUtil.getYPhysicalCoordinate(gridDef, inlineIndex, xlineIndex, _inlineUnit, _xlineUnit,
                  _storageOrder);
              Header header = JavaSeisUtil.buildHeader(_headerDef, traceProps, 0);
              header.putInteger(TraceHeaderCatalog.INLINE_NO, Math.round(inline));
              header.putInteger(TraceHeaderCatalog.XLINE_NO, Math.round(xline));
              header.putDouble(TraceHeaderCatalog.X, x);
              header.putDouble(TraceHeaderCatalog.Y, y);
              traces[count] = new Trace(zStart, zDelta, _model.getUnitOfZ(), Arrays.copyOf(samples, samples.length),
                  status, header);
              count++;
            }
            retval = new TraceData(traces);
          } catch (SeisException e) {
            getLogger().error(e.toString(), e);
          }
          break;
        case Z_INLINE_XLINE:
          // Even Less Efficent -> z,inline,xline storage order does not match inline,xline,z read order.
          throw new UnsupportedOperationException("Does not support slice orientated data yet.");
        case Z_XLINE_INLINE:
          // Least Efficent -> z,xline,inline storage order does not match inline,xline,z read order.
          throw new UnsupportedOperationException("Does not support slice orientated data yet.");
        default:
          throw new RuntimeException("Invalid storage order: " + _storageOrder);
      }
    }
    return retval;
  }

  @SuppressWarnings("unused")
  public float[] getSamples(final PostStack3d ps3d, final float[] inline, final float[] xline, final float[] z) {
    throw new UnsupportedOperationException("Does not support reading samples yet.");
  }

  public float[] getSlice(final PostStack3d ps3d, final float z, final float inlineStart, final float inlineEnd,
      final float xlineStart, final float xlineEnd, final SliceBufferOrder order) {
    return getSlice(ps3d, z, inlineStart, inlineEnd, xlineStart, xlineEnd, order, 0);
  }

  public float[] getSlice(final PostStack3d ps3d, final float z, final float inlineStart, final float inlineEnd,
      final float xlineStart, final float xlineEnd, final SliceBufferOrder order, final float missingValue) {
    throw new UnsupportedOperationException("Does not support reading slices yet.");
  }

  public TraceData getTraces(final PostStack3d ps3d, final float[] inlines, final float[] xlines, final float zStart,
      final float zEnd) {
    TraceData retval = null;
    _status = open("r");
    if (_status != null) {
      GridDefinition gridDef = _seisio.getGridDefinition();
      Unit zUnit = ps3d.getZUnit();
      float zStart2 = Unit.convert(zStart, zUnit, _model.getUnitOfZ());
      float zEnd2 = Unit.convert(zEnd, zUnit, _model.getUnitOfZ());
      int zStartIndex = JavaSeisUtil.findZIndex(gridDef, zStart2, _storageOrder);
      int zEndIndex = JavaSeisUtil.findZIndex(gridDef, zEnd2, _storageOrder);
      int zCount = Math.abs(zEndIndex - zStartIndex) + 1;
      float zDelta = ps3d.getZDelta();//(float) JavaSeisUtil.findZPhysicalDelta(gridDef, _storageOrder);
      float[][] jsData = _seisio.getTraceDataArray();
      float[] samples = new float[zCount];
      Trace[] traces = new Trace[inlines.length];
      long traceIndex;
      TraceProperties traceProps = _seisio.getTraceProperties();
      try {
        int xlineIndex;
        int inlineIndex;
        for (int j = 0; j < traces.length; j++) {
          inlineIndex = JavaSeisUtil.findInlineIndex(gridDef, inlines[j], _storageOrder);
          xlineIndex = JavaSeisUtil.findCrosslineIndex(gridDef, xlines[j], _storageOrder);
          traceIndex = JavaSeisUtil.findTraceIndex(gridDef, inlineIndex, xlineIndex, _storageOrder);
          _seisio.readTrace(traceIndex);
          System.arraycopy(jsData[0], zStartIndex, samples, 0, zCount);
          Status status = Status.Live;
          if (Trace.isDead(samples)) {
            status = Status.Dead;
          }
          double[] xy = ps3d.getSurvey().transformInlineXlineToXY(inlines[j], xlines[j]);
          Header header = JavaSeisUtil.buildHeader(_headerDef, traceProps, 0);
          header.putInteger(TraceHeaderCatalog.INLINE_NO, Math.round(inlines[j]));
          header.putInteger(TraceHeaderCatalog.XLINE_NO, Math.round(xlines[j]));
          header.putDouble(TraceHeaderCatalog.X, xy[0]);
          header.putDouble(TraceHeaderCatalog.Y, xy[1]);
          traces[j] = new Trace(zStart, zDelta, _model.getUnitOfZ(), Arrays.copyOf(samples, samples.length), status,
              header);
        }
      } catch (SeisException e) {
        getLogger().error(e.toString(), e);
      }
      retval = new TraceData(traces);
    }
    return retval;
  }

  public TraceData getXline(final PostStack3d ps3d, final float xline, final float inlineStart, final float inlineEnd,
      final float zStart, final float zEnd) {
    TraceData retval = null;
    _status = open("r");
    if (_status != null) {
      GridDefinition gridDef = _seisio.getGridDefinition();
      int[] position = null;
      int xlineIndex = JavaSeisUtil.findCrosslineIndex(gridDef, xline, _storageOrder);
      int inlineStartIndex = JavaSeisUtil.findInlineIndex(gridDef, inlineStart, _storageOrder);
      int inlineEndIndex = JavaSeisUtil.findInlineIndex(gridDef, inlineEnd, _storageOrder);
      int inlineCount = Math.abs(inlineEndIndex - inlineStartIndex) + 1;
      int inlineIncr = inlineStartIndex > inlineEndIndex ? -1 : 1;
      Unit zUnit = ps3d.getZUnit();
      float zStart2 = Unit.convert(zStart, zUnit, _model.getUnitOfZ());
      float zEnd2 = Unit.convert(zEnd, zUnit, _model.getUnitOfZ());
      int zStartIndex = JavaSeisUtil.findZIndex(gridDef, zStart2, _storageOrder);
      int zEndIndex = JavaSeisUtil.findZIndex(gridDef, zEnd2, _storageOrder);
      int zCount = Math.abs(zEndIndex - zStartIndex) + 1;
      float zDelta = ps3d.getZDelta();//(float) JavaSeisUtil.findZPhysicalDelta(gridDef, _storageOrder);
      float[][] jsData = _seisio.getTraceDataArray();
      float[] samples = new float[zCount];
      Trace[] traces = new Trace[inlineCount];
      TraceProperties traceProps = _seisio.getTraceProperties();
      switch (_storageOrder) {
        case XLINE_INLINE_Z:
          /* efficient */
          try {
            int count = 0;
            position = JavaSeisUtil.orderPosition(0, xlineIndex, 0, _storageOrder);
            int numTracesRead = _seisio.readFrame(position);
            if (numTracesRead > 0) {
              for (int j = 0; j < inlineCount; j++) {
                float inline = inlineStart + j * ps3d.getInlineDelta();
                int inlineIndex = inlineStartIndex + j * inlineIncr;
                System.arraycopy(jsData[inlineIndex], zStartIndex, samples, 0, zCount);
                Status status = Status.Live;
                if (Trace.isDead(samples)) {
                  status = Status.Dead;
                }
                double[] xy = ps3d.getSurvey().transformInlineXlineToXY(inline, xline);
                Header header = JavaSeisUtil.buildHeader(_headerDef, traceProps, inlineIndex);
                header.putInteger(TraceHeaderCatalog.INLINE_NO, Math.round(inline));
                header.putInteger(TraceHeaderCatalog.XLINE_NO, Math.round(xline));
                header.putDouble(TraceHeaderCatalog.X, xy[0]);
                header.putDouble(TraceHeaderCatalog.Y, xy[1]);
                traces[count] = new Trace(zStart, zDelta, _model.getUnitOfZ(), Arrays.copyOf(samples, samples.length),
                    status, header);
                count++;
              }
            } else {
              for (int j = 0; j < inlineCount; j++) {
                float inline = inlineStart + j * ps3d.getInlineDelta();
                int inlineIndex = inlineStartIndex + j * inlineIncr;
                double[] xy = ps3d.getSurvey().transformInlineXlineToXY(inline, xline);
                Header header = JavaSeisUtil.buildHeader(_headerDef, traceProps, inlineIndex);
                header.putInteger(TraceHeaderCatalog.INLINE_NO, Math.round(inline));
                header.putInteger(TraceHeaderCatalog.XLINE_NO, Math.round(xline));
                header.putDouble(TraceHeaderCatalog.X, xy[0]);
                header.putDouble(TraceHeaderCatalog.Y, xy[1]);
                traces[count] = new Trace(zStart, zDelta, _model.getUnitOfZ(), new float[samples.length],
                    Status.Missing, header);
                count++;
              }
            }
            retval = new TraceData(traces);
          } catch (SeisException e) {
            getLogger().error(e.toString(), e);
          }
          break;
        case INLINE_XLINE_Z:
          /* less efficient */
          long traceIndex;
          try {
            int count = 0;
            for (int jj = 0; jj < inlineCount; jj++) {
              float inline = inlineStart + jj * ps3d.getInlineDelta();
              int inlineIndex = inlineStartIndex + jj * inlineIncr;
              traceIndex = JavaSeisUtil.findTraceIndex(gridDef, inlineIndex, xlineIndex, _storageOrder);
              _seisio.readTrace(traceIndex);
              System.arraycopy(jsData[0], zStartIndex, samples, 0, zCount);
              Status status = Status.Live;
              if (Trace.isDead(samples)) {
                status = Status.Dead;
              }
              double x = JavaSeisUtil.getXPhysicalCoordinate(gridDef, inlineIndex, xlineIndex, _inlineUnit, _xlineUnit,
                  _storageOrder);
              double y = JavaSeisUtil.getYPhysicalCoordinate(gridDef, inlineIndex, xlineIndex, _inlineUnit, _xlineUnit,
                  _storageOrder);
              Header header = JavaSeisUtil.buildHeader(_headerDef, traceProps, 0);
              header.putInteger(TraceHeaderCatalog.INLINE_NO, Math.round(inline));
              header.putInteger(TraceHeaderCatalog.XLINE_NO, Math.round(xline));
              header.putDouble(TraceHeaderCatalog.X, x);
              header.putDouble(TraceHeaderCatalog.Y, y);
              traces[count] = new Trace(zStart, zDelta, _model.getUnitOfZ(), Arrays.copyOf(samples, samples.length),
                  status, header);
              count++;
            }
            retval = new TraceData(traces);
          } catch (SeisException e) {
            getLogger().error(e.toString(), e);
          }
          break;
        case Z_INLINE_XLINE:
          /* considerably inefficient */
          throw new UnsupportedOperationException("Does not support slice orientated data yet.");
        case Z_XLINE_INLINE:
          /* terribly inefficient */
          throw new UnsupportedOperationException("Does not support slice orientated data yet.");
        default:
          throw new RuntimeException("Invalid storage order: " + _storageOrder);
      }
    }
    return retval;
  }

  public void putInline(final PostStack3d ps3d, final float inline, final float xlineStart, final float xlineEnd,
      final float zStart, final float zEnd, final TraceData traceData) {
    _status = open("rw");
    if (_status != null) {
      GridDefinition gridDef = _seisio.getGridDefinition();
      int inlineIndex = JavaSeisUtil.findInlineIndex(gridDef, inline, _storageOrder);
      int xlineStartIndex = JavaSeisUtil.findCrosslineIndex(gridDef, xlineStart, _storageOrder);
      int xlineEndIndex = JavaSeisUtil.findCrosslineIndex(gridDef, xlineEnd, _storageOrder);
      int xlineIndexIncr = xlineStartIndex > xlineEndIndex ? -1 : 1;
      Unit zUnit = ps3d.getZUnit();
      float zStart2 = Unit.convert(zStart, zUnit, _model.getUnitOfZ());
      float zEnd2 = Unit.convert(zEnd, zUnit, _model.getUnitOfZ());
      int zStartIndex = JavaSeisUtil.findZIndex(gridDef, zStart2, _storageOrder);
      int zEndIndex = JavaSeisUtil.findZIndex(gridDef, zEnd2, _storageOrder);
      int numSamples = Math.abs(zEndIndex - zStartIndex) + 1;
      float[][] traceDataArrayJS = _seisio.getTraceDataArray();
      long numTracesPerFrame = _seisio.getGridDefinition().getNumTracesPerFrame();
      float[] traceDataArray = traceData.getData();
      Trace[] traces = traceData.getTraces();
      TraceProperties traceProps = _seisio.getTraceProperties();
      switch (_storageOrder) {
        case INLINE_XLINE_Z:
          // Most Efficient -> inline,xline,z storage order matches inline,xline,z read order.
          try {
            // Determine the JS position array.
            int[] position = JavaSeisUtil.orderPosition(0, 0, inlineIndex, _storageOrder);
            // Read the frame from the JS dataset.
            _seisio.readFrame(position);
            // Loop thru the traces to be written.
            for (int j = 0; j < traceData.getNumTraces(); j++) {
              int index = j * traceData.getNumSamples();
              int xlineIndex = xlineStartIndex + j * xlineIndexIncr;
              // Replace the sections of the trace data to be overwritten.
              System.arraycopy(traceDataArray, index, traceDataArrayJS[xlineIndex], zStartIndex, numSamples);
              // Replace the sections of the trace headers to be overwritten.
              JavaSeisUtil.copyHeaderValuesToJSTraceProperties(traces[j].getHeader(), traceProps, xlineIndex);
            }
            // Write the frame to the JS dataset.
            _seisio.writeFrame(position, (int) numTracesPerFrame);
          } catch (SeisException e) {
            getLogger().error(e.toString(), e);
          }
          break;
        case XLINE_INLINE_Z:
          // Less Efficent -> xline,inline,z storage order does not match inline,xline,z read order.
          long traceIndex;
          try {
            for (int j = 0; j < traceData.getNumTraces(); j++) {
              int index = j * traceData.getNumSamples();
              int xlineIndex = xlineStartIndex + j * xlineIndexIncr;
              traceIndex = JavaSeisUtil.findTraceIndex(gridDef, inlineIndex, xlineIndex, _storageOrder);
              // Read the trace from the JS dataset.
              _seisio.readTrace(traceIndex);
              // Replace the section of the trace data to be overwritten.
              System.arraycopy(traceDataArray, index, traceDataArrayJS[0], zStartIndex, numSamples);
              // Replace the section of he trace header to be overwritten.
              JavaSeisUtil.copyHeaderValuesToJSTraceProperties(traces[j].getHeader(), traceProps, 0);
              // Write the trace to the JS dataset.
              _seisio.writeTrace(traceIndex);
            }
          } catch (SeisException e) {
            getLogger().error(e.toString(), e);
          }
          break;
        case Z_INLINE_XLINE:
          // Even Less Efficent -> z,inline,xline storage order does not match inline,xline,z read order.
          throw new UnsupportedOperationException("Does not support slice orientated data yet.");
        case Z_XLINE_INLINE:
          // Least Efficent -> z,xline,inline storage order does not match inline,xline,z read order.
          throw new UnsupportedOperationException("Does not support slice orientated data yet.");
        default:
          throw new RuntimeException("Invalid storage order: " + _storageOrder);
      }
    }
  }

  @SuppressWarnings("unused")
  public void putSamples(final PostStack3d ps3d, final float[] inline, final float[] xline, final float[] z,
      final float[] samples) {
    throw new UnsupportedOperationException("Does not support writing samples yet.");
  }

  @SuppressWarnings("unused")
  public void putSlice(final PostStack3d ps3d, final float z, final float inlineStart, final float inlineEnd,
      final float xlineStart, final float xlineEnd, final SliceBufferOrder order, final float[] samples) {
    throw new UnsupportedOperationException("Does not support writing slices yet.");
  }

  public void putTraces(final PostStack3d ps3d, final TraceData traceData) {
    _status = open("rw");
    if (_status != null) {
      int numTraces = traceData.getNumTraces();
      StorageOrder storageOrder = ps3d.getPreferredOrder();

      Map<Float, List<Float>> inlines = new HashMap<Float, List<Float>>();
      Map<Float, List<Float>> xlines = new HashMap<Float, List<Float>>();
      Trace[] traces = traceData.getTraces();
      for (int i = 0; i < numTraces; i++) {
        if (!traces[i].isMissing()) {
          Float inlineKey = traces[i].getInline();
          Float xlineKey = traces[i].getXline();
          if (!inlines.containsKey(inlineKey)) {
            List<Float> xlns = new ArrayList<Float>();
            xlns.add(xlineKey);
            inlines.put(inlineKey, xlns);
          } else {
            List<Float> xlns = inlines.get(inlineKey);
            xlns.add(xlineKey);
          }
          if (!xlines.containsKey(xlineKey)) {
            List<Float> ilns = new ArrayList<Float>();
            ilns.add(inlineKey);
            xlines.put(xlineKey, ilns);
          } else {
            List<Float> ilns = xlines.get(xlineKey);
            ilns.add(inlineKey);
          }
        }
      }

      try {
        GridDefinition gridDef = _seisio.getGridDefinition();
        if (storageOrder.equals(StorageOrder.INLINE_XLINE_Z)) {
          for (float inline : inlines.keySet()) {
            int inlineIndex = JavaSeisUtil.findInlineIndex(gridDef, inline, storageOrder);
            int[] position = JavaSeisUtil.orderPosition(0, 0, inlineIndex, _storageOrder);
            int tracesRead = _seisio.readFrame(position);
            //System.out.println("inline " + inline + " traces read = " + tracesRead);
            TraceProperties traceProps = _seisio.getTraceProperties();
            float[][] frameArray = _seisio.getTraceDataArray();
            for (Trace trace : traces) {
              if (!trace.isMissing()) {
                float zStart = trace.getZStart();
                float zEnd = trace.getZEnd();
                Unit zUnit = ps3d.getZUnit();
                zStart = Unit.convert(zStart, zUnit, _model.getUnitOfZ());
                zEnd = Unit.convert(zEnd, zUnit, _model.getUnitOfZ());
                int zStartIndex = JavaSeisUtil.findZIndex(gridDef, zStart, _storageOrder);
                int zEndIndex = JavaSeisUtil.findZIndex(gridDef, zEnd, _storageOrder);
                int zCount = Math.abs(zEndIndex - zStartIndex) + 1;
                int traceIndex = JavaSeisUtil.findCrosslineIndex(gridDef, trace.getXline(), storageOrder);
                float[] traceArray = frameArray[traceIndex];
                Header header = trace.getHeader();
                JavaSeisUtil.copyHeaderValuesToJSTraceProperties(header, traceProps, traceIndex);
                System.arraycopy(trace.getDataReference(), 0, traceArray, zStartIndex, zCount);
              }
            }
            int tracesWritten = _seisio.writeFrame(position, (int) gridDef.getAxisLength(1));
            //System.out.println("traces written = " + tracesWritten);
          }
        } else if (storageOrder.equals(StorageOrder.XLINE_INLINE_Z)) {
          for (float xline : xlines.keySet()) {
            int xlineIndex = JavaSeisUtil.findCrosslineIndex(gridDef, xline, storageOrder);
            int[] position = JavaSeisUtil.orderPosition(0, 0, xlineIndex, _storageOrder);
            int tracesRead = _seisio.readFrame(position);
            //System.out.println("xline " + xline + " traces read = " + tracesRead);
            TraceProperties traceProps = _seisio.getTraceProperties();
            float[][] frameArray = _seisio.getTraceDataArray();
            for (Trace trace : traces) {
              if (!trace.isMissing()) {
                float zStart = trace.getZStart();
                float zEnd = trace.getZEnd();
                Unit zUnit = ps3d.getZUnit();
                zStart = Unit.convert(zStart, zUnit, _model.getUnitOfZ());
                zEnd = Unit.convert(zEnd, zUnit, _model.getUnitOfZ());
                int zStartIndex = JavaSeisUtil.findZIndex(gridDef, zStart, _storageOrder);
                int zEndIndex = JavaSeisUtil.findZIndex(gridDef, zEnd, _storageOrder);
                int zCount = Math.abs(zEndIndex - zStartIndex) + 1;
                int traceIndex = JavaSeisUtil.findInlineIndex(gridDef, trace.getInline(), storageOrder);
                float[] traceArray = frameArray[traceIndex];
                Header header = trace.getHeader();
                JavaSeisUtil.copyHeaderValuesToJSTraceProperties(header, traceProps, traceIndex);
                System.arraycopy(trace.getDataReference(), 0, traceArray, zStartIndex, zCount);
              }
            }
            int tracesWritten = _seisio.writeFrame(position, (int) gridDef.getAxisLength(1));
            //System.out.println("traces written = " + tracesWritten);
          }
        } else {
          throw new UnsupportedOperationException("Cannot write to volume stored in slice order yet.");
        }
      } catch (SeisException ex) {
        getLogger().error(ex.toString(), ex);
      }
    }
  }

  public void putTracesOld(final PostStack3d ps3d, final TraceData traceData) {
    _status = open("rw");
    if (_status != null) {
      int numTraces = traceData.getNumTraces();
      float[] inlines = new float[numTraces];
      float[] xlines = new float[numTraces];
      Trace[] traces = traceData.getTraces();
      for (int i = 0; i < numTraces; i++) {
        inlines[i] = traces[i].getInline();
        xlines[i] = traces[i].getXline();
      }
      float zStart = traceData.getStartZ();
      float zEnd = traceData.getEndZ();
      GridDefinition gridDef = _seisio.getGridDefinition();
      int zStartIndex = JavaSeisUtil.findZIndex(gridDef, zStart, _storageOrder);
      int zEndIndex = JavaSeisUtil.findZIndex(gridDef, zEnd, _storageOrder);
      int zCount = Math.abs(zEndIndex - zStartIndex) + 1;
      float[][] jsData = _seisio.getTraceDataArray();
      float[] data = traceData.getData();
      long traceIndex;
      TraceProperties traceProps = _seisio.getTraceProperties();
      try {
        for (int j = 0; j < traces.length; j++) {
          int index = j * traceData.getNumSamples();
          traceIndex = JavaSeisUtil.findTraceIndex(gridDef,
              JavaSeisUtil.findInlineIndex(gridDef, inlines[j], _storageOrder),
              JavaSeisUtil.findCrosslineIndex(gridDef, xlines[j], _storageOrder), _storageOrder);
          System.arraycopy(data, index, jsData[0], zStartIndex, zCount);
          Header header = traces[j].getHeader();
          JavaSeisUtil.copyHeaderValuesToJSTraceProperties(header, traceProps, 0);
          _seisio.writeTrace(traceIndex);
        }
      } catch (SeisException e) {
        getLogger().error(e.toString(), e);
      }
    }
  }

  public void putXline(final PostStack3d ps3d, final float xline, final float inlineStart, final float inlineEnd,
      final float zStart, final float zEnd, final TraceData traceData) {
    _status = open("rw");
    if (_status != null) {
      GridDefinition gridDef = _seisio.getGridDefinition();
      int xlineIndex = JavaSeisUtil.findCrosslineIndex(gridDef, xline, _storageOrder);
      int inlineStartIndex = JavaSeisUtil.findInlineIndex(gridDef, inlineStart, _storageOrder);
      int inlineEndIndex = JavaSeisUtil.findInlineIndex(gridDef, inlineEnd, _storageOrder);
      int inlineIndexIncr = inlineStartIndex > inlineEndIndex ? -1 : 1;
      Unit zUnit = ps3d.getZUnit();
      float zStart2 = Unit.convert(zStart, zUnit, _model.getUnitOfZ());
      float zEnd2 = Unit.convert(zEnd, zUnit, _model.getUnitOfZ());
      int zStartIndex = JavaSeisUtil.findZIndex(gridDef, zStart2, _storageOrder);
      int zEndIndex = JavaSeisUtil.findZIndex(gridDef, zEnd2, _storageOrder);
      int zCount = Math.abs(zEndIndex - zStartIndex) + 1;
      float[][] jsData = _seisio.getTraceDataArray();
      float[] data = traceData.getData();
      Trace[] traces = traceData.getTraces();
      TraceProperties traceProps = _seisio.getTraceProperties();
      switch (_storageOrder) {
        case XLINE_INLINE_Z:
          // Most Efficient -> xline,inline,z storage order matches xline,inline,z read order.
          try {
            int[] position = JavaSeisUtil.orderPosition(0, 0, xlineIndex, _storageOrder);
            for (int j = 0; j < traceData.getNumTraces(); j++) {
              int index = j * traceData.getNumSamples();
              int inlineIndex = inlineStartIndex + j * inlineIndexIncr;
              System.arraycopy(data, index, jsData[inlineIndex], zStartIndex, zCount);
              Header header = traces[j].getHeader();
              JavaSeisUtil.copyHeaderValuesToJSTraceProperties(header, traceProps, inlineIndex);
            }
            _seisio.writeFrame(position, jsData.length);
          } catch (SeisException e) {
            getLogger().error(e.toString(), e);
          }
          break;
        case INLINE_XLINE_Z:
          // Less Efficent -> inline,xline,z storage order does not match xline,inline,z read order.
          long traceIndex;
          try {
            for (int j = 0; j < traceData.getNumTraces(); j++) {
              int index = j * traceData.getNumSamples();
              int inlineIndex = inlineStartIndex + j * inlineIndexIncr;
              traceIndex = JavaSeisUtil.findTraceIndex(gridDef, inlineIndex, xlineIndex, _storageOrder);
              System.arraycopy(data, index, jsData[0], zStartIndex, zCount);
              Header header = traces[j].getHeader();
              JavaSeisUtil.copyHeaderValuesToJSTraceProperties(header, traceProps, 0);
              _seisio.writeTrace(traceIndex);
            }
          } catch (SeisException e) {
            getLogger().error(e.toString(), e);
          }
          break;
        case Z_INLINE_XLINE:
          // Even Less Efficent -> z,inline,xline storage order does not match inline,xline,z read order.
          throw new UnsupportedOperationException("Does not support slice orientated data yet.");
        case Z_XLINE_INLINE:
          // Least Efficent -> z,xline,inline storage order does not match inline,xline,z read order.
          throw new UnsupportedOperationException("Does not support slice orientated data yet.");
        default:
          throw new RuntimeException("Invalid storage order: " + _storageOrder);
      }
    }
  }

  public void setStorageOrganizationAndFormat(final StorageOrganization storageOrganization,
      final StorageFormat storageFormat, final BrickType brickType, final float fidelity) {
    // NOTE: The brick type and fidelity have no meaning for JavaSeis volumes.
    switch (storageOrganization) {
      case TRACE:
        switch (storageFormat) {
          case FLOAT_32:
            _model.setDataFormat(DataFormat.FLOAT.toString());
            break;
          case INTEGER_16:
            _model.setDataFormat(DataFormat.INT16.toString());
            break;
          case INTEGER_08:
            _model.setDataFormat(DataFormat.INT08.toString());
            break;
          default:
            throw new IllegalArgumentException("Invalid storage format for JavaSeis trace volume: " + storageFormat);
        }
        break;
      case COMPRESSED:
        switch (storageFormat) {
          case INTEGER_16:
            _model.setDataFormat(DataFormat.COMPRESSED_INT16.toString());
            break;
          case INTEGER_08:
            _model.setDataFormat(DataFormat.COMPRESSED_INT08.toString());
            break;
          default:
            throw new IllegalArgumentException("Invalid storage format for JavaSeis compressed volume: "
                + storageFormat);
        }
        break;
      default:
        throw new IllegalArgumentException("Invalid storage organization for JavaSeis volume: " + storageOrganization);
    }
  }

  public String canCreate(StorageOrganization storageOrganization, StorageFormat storageFormat) {
    switch (storageOrganization) {
      case TRACE:
        switch (storageFormat) {
          case FLOAT_32:
          case INTEGER_16:
          case INTEGER_08:
            return "";
          case FLOAT_16:
          case FLOAT_08:
          case INTEGER_32:
            return storageFormat + " format not supported for JavaSeis volumes.";
        }
        break;
      case BRICK:
        return storageOrganization + " volumes not supported in JavaSeis.";
      case COMPRESSED:
        switch (storageFormat) {
          case INTEGER_16:
          case INTEGER_08:
            return "";
          case FLOAT_32:
          case FLOAT_16:
          case FLOAT_08:
          case INTEGER_32:
            return storageFormat + " format not supported for JavaSeis compresed volumes.";
        }
        break;
    }
    return "Invalid storage organization: " + storageOrganization;
  }

  @Override
  public String getDatastoreEntryDescription() {
    return "JavaSeis PostStack3d";
  }

  public String getDatastore() {
    return "JavaSeis";
  }

}
