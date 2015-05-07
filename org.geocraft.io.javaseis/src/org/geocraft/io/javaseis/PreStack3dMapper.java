/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.javaseis;


import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;

import org.geocraft.core.common.math.MathUtil;
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
import org.geocraft.core.model.mapper.IPreStack3dMapper;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.model.seismic.PreStack3d.StorageOrder;
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


public class PreStack3dMapper extends SeismicDatasetMapper<PreStack3d> implements IPreStack3dMapper {

  private StorageOrder _storageOrder;

  private int _inlineAxis;

  private int _xlineAxis;

  private int _offsetAxis;

  public PreStack3dMapper(final VolumeMapperModel model) {
    super(model);

    String storageOrderStr = _model.getStorageOrder();
    if (!storageOrderStr.equals(StorageOrder.AUTO_CALCULATED.getName())) {
      _storageOrder = StorageOrder.lookupByName(storageOrderStr);
    }
  }

  @Override
  public PreStack3dMapper factory(final MapperModel mapperModel) {
    return new PreStack3dMapper((VolumeMapperModel) mapperModel);
  }

  @Override
  public StorageOrder getStorageOrder(final PreStack3d ps3d) {
    return _storageOrder;
  }

  @Override
  public void setStorageOrder(final StorageOrder storageOrder) {
    _storageOrder = storageOrder;
  }

  @Override
  protected void readFromStore(final PreStack3d ps3d) {
    _status = open("r");
    if (_status != null) {
      _gridDef = _seisio.getGridDefinition();
      _inlineAxis = JavaSeisUtil.getInlineAxis(_storageOrder);
      _xlineAxis = JavaSeisUtil.getCrosslineAxis(_storageOrder);
      _offsetAxis = JavaSeisUtil.getOffsetAxis(_storageOrder);
      int[] lengths = JavaSeisUtil.getAxisLengthsForPreStack3d(_seisio.getGridDefinition());
      // Update the PreStack3d entity.
      File file = new File(_model.getDirectory() + File.separator + _model.getFileName());
      ps3d.setZDomain(_zDomain);
      float zStart = (float) JavaSeisUtil.findZPhysicalCoordinate(_gridDef, 0, _storageOrder);
      float zEnd = (float) JavaSeisUtil.findZPhysicalCoordinate(_gridDef, lengths[0] - 1, _storageOrder);
      float zDelta = (float) JavaSeisUtil.findZPhysicalDelta(_gridDef, _storageOrder);

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
      ps3d.setLastModifiedDate(new Timestamp(file.lastModified()));
      ps3d.setProjectName("");
      Unit dataUnit = _model.getDataUnit();
      ps3d.setDataUnit(dataUnit);
      ps3d.setTraceHeaderDefinition(_headerDef);
      float inlineStart = JavaSeisUtil.findInlineLogicalCoordinate(_gridDef, 0, _storageOrder);
      float inlineEnd = JavaSeisUtil.findInlineLogicalCoordinate(_gridDef, lengths[_inlineAxis] - 1, _storageOrder);
      float inlineDelta = JavaSeisUtil.findInlineLogicalDelta(_gridDef, _storageOrder);
      ps3d.setInlineRange(inlineStart, inlineEnd, inlineDelta);
      float xlineStart = JavaSeisUtil.findCrosslineLogicalCoordinate(_gridDef, 0, _storageOrder);
      float xlineEnd = JavaSeisUtil.findCrosslineLogicalCoordinate(_gridDef, lengths[_xlineAxis] - 1, _storageOrder);
      float xlineDelta = JavaSeisUtil.findCrosslineLogicalDelta(_gridDef, _storageOrder);
      ps3d.setXlineRange(xlineStart, xlineEnd, xlineDelta);
      float offsetStart = (float) JavaSeisUtil.findOffsetPhysicalCoordinate(_gridDef, 0, _storageOrder);
      float offsetEnd = (float) JavaSeisUtil.findOffsetPhysicalCoordinate(_gridDef, lengths[_offsetAxis] - 1,
          _storageOrder);
      float offsetDelta = (float) JavaSeisUtil.findOffsetPhysicalDelta(_gridDef, _storageOrder);
      ps3d.setOffsetRange(offsetStart, offsetEnd, offsetDelta);
      ps3d.setElevationDatum(0);

      // Create a seismic geometry in which to put the prestack.
      if (ps3d.getSurvey() == null) {
        Unit xyUnitApp = UnitPreferences.getInstance().getHorizontalDistanceUnit();
        double x0 = Unit.convert(_model.getX0(), _model.getUnitOfXY(), xyUnitApp);
        double y0 = Unit.convert(_model.getY0(), _model.getUnitOfXY(), xyUnitApp);
        double x1 = Unit.convert(_model.getX1(), _model.getUnitOfXY(), xyUnitApp);
        double y1 = Unit.convert(_model.getY1(), _model.getUnitOfXY(), xyUnitApp);
        double x2 = Unit.convert(_model.getX2(), _model.getUnitOfXY(), xyUnitApp);
        double y2 = Unit.convert(_model.getY2(), _model.getUnitOfXY(), xyUnitApp);

        Point3d[] points = new Point3d[4];
        points[0] = new Point3d(x0, y0, 0);
        points[1] = new Point3d(x1, y1, 0);
        points[3] = new Point3d(x2, y2, 0);
        double dx = x1 - x0 + x2 - x0;
        double dy = y1 - y0 + y2 - y0;
        points[2] = new Point3d(x0 + dx, y0 + dy, 0);
        CoordinateSystem coordSystem = new CoordinateSystem("", _zDomain);
        CornerPointsSeries cornerPoints = CornerPointsSeries.create(points, coordSystem);
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

        FloatRange inlineRange = new FloatRange(JavaSeisUtil.findInlineLogicalCoordinate(_gridDef, 0, _storageOrder),
            JavaSeisUtil.findInlineLogicalCoordinate(_gridDef, lengths[_inlineAxis] - 1, _storageOrder),
            JavaSeisUtil.findInlineLogicalDelta(_gridDef, _storageOrder));

        FloatRange xlineRange = new FloatRange(JavaSeisUtil.findCrosslineLogicalCoordinate(_gridDef, 0, _storageOrder),
            JavaSeisUtil.findCrosslineLogicalCoordinate(_gridDef, lengths[_xlineAxis] - 1, _storageOrder),
            JavaSeisUtil.findCrosslineLogicalDelta(_gridDef, _storageOrder));

        //        FloatRange offsetRange = new FloatRange((float) JavaSeisUtil.findOffsetPhysicalCoordinate(_gridDef, 0,
        //            _storageOrder), (float) JavaSeisUtil.findOffsetPhysicalCoordinate(_gridDef, lengths[_offsetAxis] - 1,
        //            _storageOrder), (float) JavaSeisUtil.findOffsetPhysicalDelta(_gridDef, _storageOrder));

        SeismicSurvey3d seismicGeometry = new SeismicSurvey3d("Survey - " + ps3d.getDisplayName(), inlineRange,
            xlineRange, cornerPoints, orientation);
        ps3d.setSurvey(seismicGeometry);
      }
      ps3d.setDirty(false);
    }
  }

  @Override
  protected void updateInStore(final PreStack3d ps3d) throws IOException {
    SeismicSurvey3d geometry = ps3d.getSurvey();
    float[] inlines = { geometry.getInlineStart() };
    float[] xlines = { geometry.getXlineStart() };
    CoordinateSeries coords00 = geometry.transformInlineXlineToXY(inlines, xlines);
    inlines[0] = geometry.getInlineStart() + geometry.getInlineDelta();
    CoordinateSeries coords10 = geometry.transformInlineXlineToXY(inlines, xlines);
    inlines[0] = geometry.getInlineStart();
    xlines[0] = geometry.getXlineStart() + geometry.getXlineDelta();
    CoordinateSeries coords01 = geometry.transformInlineXlineToXY(inlines, xlines);
    Point3d p00 = coords00.getPoint(0);
    Point3d p10 = coords10.getPoint(0);
    Point3d p01 = coords01.getPoint(0);
    double physicalInlineDelta = p00.distanceTo(p10);
    double physicalXlineDelta = p00.distanceTo(p01);
    double physicalOffsetDelta = ps3d.getOffsetDelta();
    // Allocate and create 3 JS axis definitions.
    Unit xyUnit = UnitPreferences.getInstance().getHorizontalDistanceUnit();
    Units xyUnitJS = Units.FEET;
    if (xyUnit.equals(Unit.FOOT)) {
      xyUnitJS = Units.FEET;
    } else if (xyUnit.equals(Unit.METER)) {
      xyUnitJS = Units.METERS;
    } else {
      throw new RuntimeException("Invalid application x,y units: " + xyUnit);
    }
    AxisDefinition[] axisDefs = new AxisDefinition[4];

    // Create the z axis based on the domain of the volume.
    AxisLabel zAxisLabel = AxisLabel.TIME;
    DataDomain zAxisDomain = DataDomain.TIME;
    Units zAxisUnits = Units.MILLISECONDS;
    if (ps3d.getZDomain().equals(Domain.DISTANCE)) {
      zAxisLabel = AxisLabel.DEPTH;
      zAxisDomain = DataDomain.DEPTH;
      zAxisUnits = Units.FEET;
      if (ps3d.getZUnit().equals(Unit.METER)) {
        zAxisUnits = Units.METERS;
      }
    }

    float zStart = Unit.convert(ps3d.getZStart(), ps3d.getZUnit(), _model.getUnitOfZ());
    float zDelta = Unit.convert(ps3d.getZDelta(), ps3d.getZUnit(), _model.getUnitOfZ());

    AxisDefinition zAxis = new AxisDefinition(zAxisLabel, zAxisUnits, zAxisDomain, ps3d.getNumSamplesPerTrace(), 0, 1,
        zStart, zDelta);

    AxisDefinition xlineAxis = new AxisDefinition(AxisLabel.CROSSLINE, xyUnitJS, DataDomain.SPACE, ps3d.getNumXlines(),
        (long) ps3d.getXlineStart(), (long) ps3d.getXlineDelta(), 0, physicalXlineDelta);
    AxisDefinition inlineAxis = new AxisDefinition(AxisLabel.INLINE, xyUnitJS, DataDomain.SPACE, ps3d.getNumInlines(),
        (long) ps3d.getInlineStart(), (long) ps3d.getInlineDelta(), 0, physicalInlineDelta);
    AxisDefinition offsetAxis = new AxisDefinition(AxisLabel.OFFSET, xyUnitJS, DataDomain.SPACE, ps3d.getNumOffsets(),
        1, 1, 0, physicalOffsetDelta);

    // Fill out the JS axis definitions array based on the desired storage order.
    if (_storageOrder.equals(StorageOrder.INLINE_XLINE_OFFSET_Z)) {
      axisDefs[0] = zAxis;
      axisDefs[1] = offsetAxis;
      axisDefs[2] = xlineAxis;
      axisDefs[3] = inlineAxis;
    } else if (_storageOrder.equals(StorageOrder.INLINE_OFFSET_XLINE_Z)) {
      axisDefs[0] = zAxis;
      axisDefs[1] = xlineAxis;
      axisDefs[2] = offsetAxis;
      axisDefs[3] = inlineAxis;
    } else if (_storageOrder.equals(StorageOrder.XLINE_INLINE_OFFSET_Z)) {
      axisDefs[0] = zAxis;
      axisDefs[1] = offsetAxis;
      axisDefs[2] = inlineAxis;
      axisDefs[3] = xlineAxis;
    } else if (_storageOrder.equals(StorageOrder.XLINE_OFFSET_INLINE_Z)) {
      axisDefs[0] = zAxis;
      axisDefs[1] = inlineAxis;
      axisDefs[2] = offsetAxis;
      axisDefs[3] = xlineAxis;
    } else if (_storageOrder.equals(StorageOrder.OFFSET_INLINE_XLINE_Z)) {
      axisDefs[0] = zAxis;
      axisDefs[1] = xlineAxis;
      axisDefs[2] = inlineAxis;
      axisDefs[3] = offsetAxis;
    } else if (_storageOrder.equals(StorageOrder.OFFSET_XLINE_INLINE_Z)) {
      axisDefs[3] = zAxis;
      axisDefs[0] = inlineAxis;
      axisDefs[1] = xlineAxis;
      axisDefs[2] = offsetAxis;
    }
    // Create a JS grid definition.
    GridDefinition gridDef = new GridDefinition(4, axisDefs);
    DataDefinition dataDef = new DataDefinition(DataType.STACK, DataFormat.get(_model.getDataFormat()));
    TraceProperties traceProps = JavaSeisUtil.buildTraceProperties(ps3d.getTraceHeaderDefinition());
    try {
      // Create a JS Seisio instance.
      _seisio = new Seisio(getFilePath(), gridDef, dataDef, traceProps);
      _seisio.setMapped();
      // Create a JS bin grid.
      CoordinateSeries cornerPoints = geometry.getCornerPoints();
      double worldX0 = cornerPoints.getX(0);
      double worldY0 = cornerPoints.getY(0);
      double worldX1 = cornerPoints.getX(3);
      double worldY1 = cornerPoints.getY(3);
      double worldX2 = cornerPoints.getX(1);
      double worldY2 = cornerPoints.getY(1);
      double gridX0 = 0;
      double gridY0 = 0;
      double distance01 = Math.sqrt(Math.pow(worldX1 - worldX0, 2) + Math.pow(worldY1 - worldY0, 2));
      double distance02 = Math.sqrt(Math.pow(worldX2 - worldX0, 2) + Math.pow(worldY2 - worldY0, 2));
      double gridDX = distance01 / (geometry.getNumXlines() - 1);
      double gridDY = distance02 / (geometry.getNumInlines() - 1);
      float[] coords0 = geometry.transformXYToInlineXline(worldX0, worldY0, true);
      float[] coords1 = geometry.transformXYToInlineXline(worldX1, worldY1, true);
      float[] coords2 = geometry.transformXYToInlineXline(worldX2, worldY2, true);
      long logicalX0 = 0;
      long logicalY0 = 0;
      long logicalX1 = 0;
      long logicalY1 = 0;
      long logicalX2 = 0;
      long logicalY2 = 0;
      long logicalDX = 1;
      long logicalDY = 1;
      // TODO: need to figure out the correct logic for this.
      if (_storageOrder.equals(StorageOrder.INLINE_XLINE_OFFSET_Z)
          || _storageOrder.equals(StorageOrder.INLINE_OFFSET_XLINE_Z)
          || _storageOrder.equals(StorageOrder.OFFSET_INLINE_XLINE_Z)) {
        logicalX0 = Math.round(coords0[0]);
        logicalY0 = Math.round(coords0[1]);
        logicalX1 = Math.round(coords1[0]);
        logicalY1 = Math.round(coords1[1]);
        logicalX2 = Math.round(coords2[0]);
        logicalY2 = Math.round(coords2[1]);
        logicalDX = Math.round(geometry.getInlineDelta());
        logicalDY = Math.round(geometry.getXlineDelta());
      } else if (_storageOrder.equals(StorageOrder.XLINE_INLINE_OFFSET_Z)
          || _storageOrder.equals(StorageOrder.XLINE_OFFSET_INLINE_Z)
          || _storageOrder.equals(StorageOrder.OFFSET_XLINE_INLINE_Z)) {
        logicalX0 = Math.round(coords0[1]);
        logicalY0 = Math.round(coords0[0]);
        logicalX1 = Math.round(coords1[1]);
        logicalY1 = Math.round(coords1[0]);
        logicalX2 = Math.round(coords2[1]);
        logicalY2 = Math.round(coords2[0]);
        logicalDX = Math.round(geometry.getXlineDelta());
        logicalDY = Math.round(geometry.getInlineDelta());
      } else {
        throw new RuntimeException("Storage order " + _storageOrder + " not yet supported.");
      }
      BinGrid binGrid = new BinGrid(worldX0, worldY0, worldX1, worldY1, worldX2, worldY2, logicalX0, logicalY0,
          logicalX1, logicalY1, logicalX2, logicalY2, logicalDX, logicalDY, gridX0, gridY0, gridDX, gridDY);
      _seisio.writeBinGridToFileProperties(binGrid.toParameterSet());
      _gridDef = _seisio.getGridDefinition();
      _inlineAxis = JavaSeisUtil.getInlineAxis(_storageOrder);
      _xlineAxis = JavaSeisUtil.getCrosslineAxis(_storageOrder);
      _offsetAxis = JavaSeisUtil.getOffsetAxis(_storageOrder);

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
        if (gridDef.getAxisLabels().length != 4) {
          throw new RuntimeException("Not a prestack 3d volume.");
        }
        setStorageOrder(JavaSeisUtil.computePreStackStorageOrder(_seisio.getGridDefinition()));
        //_zDomain = JavaSeisUtil.getZAxisDomain(gridDef, _storageOrder);
        //_zUnit = JavaSeisUtil.getAxisUnit(gridDef, JavaSeisUtil.getZAxis(_storageOrder));
        //Unit xlineUnit = JavaSeisUtil.getAxisUnit(gridDef, JavaSeisUtil.getCrosslineAxis(_storageOrder));
        //Unit inlineUnit = JavaSeisUtil.getAxisUnit(gridDef, JavaSeisUtil.getInlineAxis(_storageOrder));
        //Unit offsetUnit = JavaSeisUtil.getAxisUnit(gridDef, JavaSeisUtil.getOffsetAxis(_storageOrder));
      } catch (SeisException e) {
        getLogger().error(e.toString(), e);
      }
      _headerDef = JavaSeisUtil.buildHeaderDefinition(_seisio.getTraceProperties().getTraceProperties(), true, true);
    }
    return retval;
  }

  @Override
  public TraceData getTraces(final PreStack3d ps3d, final float[] inlines, final float[] xlines, final float[] offsets,
      final float zStart, final float zEnd) {
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
        int offsetIndex;
        for (int j = 0; j < traces.length; j++) {
          inlineIndex = JavaSeisUtil.findInlineIndex(gridDef, inlines[j], _storageOrder);
          xlineIndex = JavaSeisUtil.findCrosslineIndex(gridDef, xlines[j], _storageOrder);
          offsetIndex = JavaSeisUtil.findOffsetIndex(gridDef, offsets[j], _storageOrder);
          traceIndex = JavaSeisUtil.findTraceIndex(gridDef, inlineIndex, xlineIndex, offsetIndex, _storageOrder);
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
          header.putFloat(TraceHeaderCatalog.OFFSET, offsets[j]);
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

  @Override
  public TraceData getTracesByInlineOffset(final PreStack3d ps3d, final float inline, final float offset,
      final float xlineStart, final float xlineEnd, final float zStart, final float zEnd) {
    TraceData retval = null;
    // If not the storage order, then we have to read trace at a time, so use getTraces().
    if (!_storageOrder.equals(StorageOrder.INLINE_OFFSET_XLINE_Z)) {
      GridDefinition gridDef = _seisio.getGridDefinition();
      int xlineStartIndex = JavaSeisUtil.findCrosslineIndex(gridDef, xlineStart, _storageOrder);
      int xlineEndIndex = JavaSeisUtil.findCrosslineIndex(gridDef, xlineEnd, _storageOrder);
      int numTraces = Math.abs(xlineEndIndex - xlineStartIndex) + 1;
      float[] inlines = new float[numTraces];
      float[] xlines = new float[numTraces];
      float[] offsets = new float[numTraces];
      for (int i = 0; i < numTraces; i++) {
        inlines[i] = inline;
        xlines[i] = xlineStart + i * ps3d.getXlineDelta();
        offsets[i] = offset;
      }
      return getTraces(ps3d, inlines, xlines, offsets, zStart, zEnd);
    }
    _status = open("r");
    if (_status != null) {
      GridDefinition gridDef = _seisio.getGridDefinition();
      int inlineIndex = JavaSeisUtil.findInlineIndex(gridDef, inline, _storageOrder);
      int offsetIndex = JavaSeisUtil.findOffsetIndex(gridDef, offset, _storageOrder);
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
      // Most Efficient -> inline,offset,xline,z storage order matches inline,offset,xline,z read order.
      try {
        int[] position = JavaSeisUtil.orderPosition(0, 0, offsetIndex, inlineIndex, _storageOrder);
        _seisio.readFrame(position);
        int count = 0;
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
          header.putFloat(TraceHeaderCatalog.OFFSET, offset);
          header.putDouble(TraceHeaderCatalog.X, xy[0]);
          header.putDouble(TraceHeaderCatalog.Y, xy[1]);
          traces[count] = new Trace(zStart, zDelta, _model.getUnitOfZ(), Arrays.copyOf(samples, samples.length),
              status, header);
          count++;
        }
        retval = new TraceData(traces);
      } catch (SeisException e) {
        getLogger().error(e.toString(), e);
      }
    }
    return retval;
  }

  @Override
  public TraceData getTracesByInlineXline(final PreStack3d ps3d, final float inline, final float xline,
      final float offsetStart, final float offsetEnd, final float zStart, final float zEnd) {
    TraceData retval = null;
    // If not the storage order, then we have to read trace at a time, so use getTraces().
    if (!_storageOrder.equals(StorageOrder.INLINE_XLINE_OFFSET_Z)) {
      GridDefinition gridDef = _seisio.getGridDefinition();
      int offsetStartIndex = JavaSeisUtil.findOffsetIndex(gridDef, offsetStart, _storageOrder);
      int offsetEndIndex = JavaSeisUtil.findOffsetIndex(gridDef, offsetEnd, _storageOrder);
      int numTraces = Math.abs(offsetEndIndex - offsetStartIndex) + 1;
      float[] inlines = new float[numTraces];
      float[] xlines = new float[numTraces];
      float[] offsets = new float[numTraces];
      for (int i = 0; i < numTraces; i++) {
        inlines[i] = inline;
        xlines[i] = xline;
        offsets[i] = offsetStart + i * ps3d.getOffsetDelta();
      }
      return getTraces(ps3d, inlines, xlines, offsets, zStart, zEnd);
    }
    _status = open("r");
    if (_status != null) {
      GridDefinition gridDef = _seisio.getGridDefinition();
      int inlineIndex = JavaSeisUtil.findInlineIndex(gridDef, inline, _storageOrder);
      int xlineIndex = JavaSeisUtil.findCrosslineIndex(gridDef, xline, _storageOrder);
      int offsetStartIndex = JavaSeisUtil.findOffsetIndex(gridDef, offsetStart, _storageOrder);
      int offsetEndIndex = JavaSeisUtil.findOffsetIndex(gridDef, offsetEnd, _storageOrder);
      int offsetCount = Math.abs(offsetEndIndex - offsetStartIndex) + 1;
      int offsetIncr = offsetStartIndex > offsetEndIndex ? -1 : 1;
      Unit zUnit = ps3d.getZUnit();
      float zStart2 = Unit.convert(zStart, zUnit, _model.getUnitOfZ());
      float zEnd2 = Unit.convert(zEnd, zUnit, _model.getUnitOfZ());
      int zStartIndex = JavaSeisUtil.findZIndex(gridDef, zStart2, _storageOrder);
      int zEndIndex = JavaSeisUtil.findZIndex(gridDef, zEnd2, _storageOrder);
      int zCount = Math.abs(zEndIndex - zStartIndex) + 1;
      float zDelta = ps3d.getZDelta();//(float) JavaSeisUtil.findZPhysicalDelta(gridDef, _storageOrder);
      float[][] jsData = _seisio.getTraceDataArray();
      float[] samples = new float[zCount];
      Trace[] traces = new Trace[offsetCount];
      TraceProperties traceProps = _seisio.getTraceProperties();
      // Most Efficient -> inline,xline,offset,z storage order matches inline,xline,offset,z read order.
      try {
        int[] position = JavaSeisUtil.orderPosition(0, 0, xlineIndex, inlineIndex, _storageOrder);
        _seisio.readFrame(position);
        int count = 0;
        for (int j = 0; j < offsetCount; j++) {
          int offsetIndex = offsetStartIndex + j * offsetIncr;
          float offset = offsetStart + j * ps3d.getOffsetDelta();
          System.arraycopy(jsData[offsetIndex], zStartIndex, samples, 0, zCount);
          Status status = Status.Live;
          if (Trace.isDead(samples)) {
            status = Status.Dead;
          }
          double[] xy = ps3d.getSurvey().transformInlineXlineToXY(inline, xline);
          Header header = JavaSeisUtil.buildHeader(_headerDef, traceProps, offsetIndex);
          header.putInteger(TraceHeaderCatalog.INLINE_NO, Math.round(inline));
          header.putInteger(TraceHeaderCatalog.XLINE_NO, Math.round(xline));
          header.putFloat(TraceHeaderCatalog.OFFSET, offset);
          header.putDouble(TraceHeaderCatalog.X, xy[0]);
          header.putDouble(TraceHeaderCatalog.Y, xy[1]);
          traces[count] = new Trace(zStart, zDelta, _model.getUnitOfZ(), Arrays.copyOf(samples, samples.length),
              status, header);
          count++;
        }
        retval = new TraceData(traces);
      } catch (SeisException e) {
        getLogger().error(e.toString(), e);
      }
    }
    return retval;
  }

  @Override
  public TraceData getTracesByXlineOffset(final PreStack3d ps3d, final float xline, final float offset,
      final float inlineStart, final float inlineEnd, final float zStart, final float zEnd) {
    TraceData retval = null;
    // If not the storage order, then we have to read trace at a time, so use getTraces().
    if (!_storageOrder.equals(StorageOrder.XLINE_OFFSET_INLINE_Z)) {
      GridDefinition gridDef = _seisio.getGridDefinition();
      int inlineStartIndex = JavaSeisUtil.findInlineIndex(gridDef, inlineStart, _storageOrder);
      int inlineEndIndex = JavaSeisUtil.findInlineIndex(gridDef, inlineEnd, _storageOrder);
      int numTraces = Math.abs(inlineEndIndex - inlineStartIndex) + 1;
      float[] inlines = new float[numTraces];
      float[] xlines = new float[numTraces];
      float[] offsets = new float[numTraces];
      for (int i = 0; i < numTraces; i++) {
        inlines[i] = inlineStart + i * ps3d.getInlineDelta();
        xlines[i] = xline;
        offsets[i] = offset;
      }
      return getTraces(ps3d, inlines, xlines, offsets, zStart, zEnd);
    }
    _status = open("r");
    if (_status != null) {
      GridDefinition gridDef = _seisio.getGridDefinition();
      int xlineIndex = JavaSeisUtil.findCrosslineIndex(gridDef, xline, _storageOrder);
      int offsetIndex = JavaSeisUtil.findOffsetIndex(gridDef, offset, _storageOrder);
      int inlineStartIndex = JavaSeisUtil.findCrosslineIndex(gridDef, inlineStart, _storageOrder);
      int inlineEndIndex = JavaSeisUtil.findCrosslineIndex(gridDef, inlineEnd, _storageOrder);
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
      // Most Efficient -> xline,offset,inline,z storage order matches xline,offset,inline,z read order.
      try {
        int[] position = JavaSeisUtil.orderPosition(0, 0, offsetIndex, xlineIndex, _storageOrder);
        _seisio.readFrame(position);
        int count = 0;
        for (int j = 0; j < inlineCount; j++) {
          int inlineIndex = inlineStartIndex + j * inlineIncr;
          float inline = inlineStart + j * ps3d.getInlineDelta();
          System.arraycopy(jsData[inlineIndex], zStartIndex, samples, 0, zCount);
          Status status = Status.Live;
          if (Trace.isDead(samples)) {
            status = Status.Dead;
          }
          double[] xy = ps3d.getSurvey().transformInlineXlineToXY(inline, xline);
          Header header = JavaSeisUtil.buildHeader(_headerDef, traceProps, inlineIndex);
          header.putInteger(TraceHeaderCatalog.INLINE_NO, Math.round(inline));
          header.putInteger(TraceHeaderCatalog.XLINE_NO, Math.round(xline));
          header.putFloat(TraceHeaderCatalog.OFFSET, offset);
          header.putDouble(TraceHeaderCatalog.X, xy[0]);
          header.putDouble(TraceHeaderCatalog.Y, xy[1]);
          traces[count] = new Trace(zStart, zDelta, _model.getUnitOfZ(), Arrays.copyOf(samples, samples.length),
              status, header);
          count++;
        }
        retval = new TraceData(traces);
      } catch (SeisException e) {
        getLogger().error(e.toString(), e);
      }
    }
    return retval;
  }

  @Override
  public void putTraces(final PreStack3d ps3d, final TraceData traceData) {
    _status = open("rw");
    if (_status != null) {
      int numTraces = traceData.getNumTraces();
      float[] inlines = new float[numTraces];
      float[] xlines = new float[numTraces];
      float[] offsets = new float[numTraces];
      Trace[] traces = traceData.getTraces();
      for (int i = 0; i < numTraces; i++) {
        Header header = traces[i].getHeader();
        inlines[i] = header.getInteger(TraceHeaderCatalog.INLINE_NO);
        xlines[i] = header.getInteger(TraceHeaderCatalog.XLINE_NO);
        offsets[i] = header.getFloat(TraceHeaderCatalog.OFFSET);
      }
      float zStart = traceData.getStartZ();
      float zEnd = traceData.getEndZ();

      Unit zUnit = ps3d.getZUnit();
      zStart = Unit.convert(zStart, zUnit, _model.getUnitOfZ());
      zEnd = Unit.convert(zEnd, zUnit, _model.getUnitOfZ());

      GridDefinition gridDef = _seisio.getGridDefinition();
      float zStart2 = Unit.convert(zStart, zUnit, _model.getUnitOfZ());
      float zEnd2 = Unit.convert(zEnd, zUnit, _model.getUnitOfZ());
      int zStartIndex = JavaSeisUtil.findZIndex(gridDef, zStart2, _storageOrder);
      int zEndIndex = JavaSeisUtil.findZIndex(gridDef, zEnd2, _storageOrder);
      int zCount = Math.abs(zEndIndex - zStartIndex) + 1;
      float[][] jsData = _seisio.getTraceDataArray();
      float[] data = traceData.getData();
      long traceIndex;
      TraceProperties traceProps = _seisio.getTraceProperties();
      //String order = guessStorageOrder(numTraces, inlines, xlines, offsets);
      try {
        for (int j = 0; j < traces.length; j++) {
          int index = j * traceData.getNumSamples();
          int inlineIndex = JavaSeisUtil.findInlineIndex(gridDef, inlines[j], _storageOrder);
          int xlineIndex = JavaSeisUtil.findCrosslineIndex(gridDef, xlines[j], _storageOrder);
          int offsetIndex = JavaSeisUtil.findOffsetIndex(gridDef, offsets[j], _storageOrder);
          traceIndex = JavaSeisUtil.findTraceIndex(gridDef, inlineIndex, xlineIndex, offsetIndex, _storageOrder);
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

  /**
   * @param inlines
   * @param xlines
   * @param offsets
   * @return
   */
  private String guessStorageOrder(final int numTraces, final float[] inlines, final float[] xlines,
      final float[] offsets) {
    int numInlineChanges = 0;
    int numXlineChanges = 0;
    int numOffsetChanges = 0;
    for (int i = 1; i < numTraces; i++) {
      if (!MathUtil.isEqual(inlines[i], inlines[i - 1])) {
        numInlineChanges++;
      }
      if (!MathUtil.isEqual(xlines[i], xlines[i - 1])) {
        numXlineChanges++;
      }
      if (!MathUtil.isEqual(offsets[i], offsets[i - 1])) {
        numOffsetChanges++;
      }
    }
    long[] lengths = _seisio.getGridDefinition().getAxisLengths();
    if (numXlineChanges == 0 && numOffsetChanges == 0 && numInlineChanges == lengths[_inlineAxis] - 1) {
      return "Inline";
    } else if (numInlineChanges == 0 && numOffsetChanges == 0 && numXlineChanges == lengths[_xlineAxis] - 1) {
      return "Xline";
    } else if (numInlineChanges == 0 && numXlineChanges == 0 && numOffsetChanges == lengths[_offsetAxis] - 1) {
      return "Offset";
    }
    return "Unknown";
  }

  @Override
  public String getDatastoreEntryDescription() {
    return "JavaSeis PreStack3d";
  }

  public String getDatastore() {
    return "JavaSeis";
  }

}
