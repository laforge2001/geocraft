/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Timestamp;

import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.CornerPointsSeries;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.HeaderDefinition;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.PolygonUtil;
import org.geocraft.core.model.datatypes.PolygonUtil.PolygonType;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.TraceHeaderCatalog;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.IPostStack3dMapper.StorageOrganization;
import org.geocraft.core.model.mapper.IPreStack3dMapper;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.model.seismic.SurveyOrientation;


/**
 * Defines the class for mapping between a PreStack3d volume and a SEG-Y file on disk.
 * It is extended from the SegyMapper abstract base class.
 */
public class PreStack3dMapper extends SegyMapper<PreStack3d> implements IPreStack3dMapper {

  /** A reference to the unit preferences. */
  private static final UnitPreferences UNIT_PREFS = UnitPreferences.getInstance();

  /** 
   * The storage order of trace data in the SEG-Y file.
   * Useful for determining the optimal processing direction.
   */
  private PreStack3d.StorageOrder _storageOrder;

  /** The SEG-Y trace index file. */
  private SegyTraceIndex _traceIndex;

  /**
   * The default constructor.
   * See the <code>SegyMapper</code> constructor for more information.
   */
  public PreStack3dMapper(final Volume3dMapperModel model) {
    this(model, true);
  }

  /**
   * A secondary constructor.
   * See the <code>SegyMapper</code> constructor for more information.
   * This constructor also allows for automatically generating the
   * necessary trace index file.
   * 
   * @param model the model of mapper parameters.
   * @param createIndex flag indicating need to create a trace index.
   */
  public PreStack3dMapper(final Volume3dMapperModel model, final boolean createIndex) {
    super(model);
    // Generate the trace index file, if necessary.
    checkTraceIndex(createIndex);
  }

  @Override
  public PreStack3dMapper factory(final MapperModel mapperModel) {
    return new PreStack3dMapper((Volume3dMapperModel) mapperModel);
  }

  /**
   * Reads the EBCDIC and binary headers for the SEG-Y file
   * on disk and updates the entity accordingly.
   * 
   * @param the entity mapped to the SEG-Y file.
   */
  @Override
  public void readFromStore(final PreStack3d ps3d) {
    Volume3dMapperModel model = (Volume3dMapperModel) _model;
    synchronized (getSynchronizeToken()) {
      try {
        getFileAccessor().read();
        // Update the PreStack3d entity.
        ps3d.setComment(getFileAccessor().getEbcdicHeader().toString());
        Domain domain = getFileAccessor().getDomain();
        ps3d.setZDomain(domain);
        ps3d.setZRangeAndDelta(getFileAccessor().getStartZ(), getFileAccessor().getEndZ(), getFileAccessor()
            .getDeltaZ());
        Timestamp lastModifiedDate = new Timestamp(new File(getFilePath()).lastModified());
        ps3d.setLastModifiedDate(lastModifiedDate);
        ps3d.setProjectName("");
        ps3d.setDataUnit(model.getDataUnit());
        ps3d.setTraceHeaderDefinition(getHeaderDefinition());
        // TODO: what to do about the logic below, because a trace index is not created when the file is written.
        if (_traceIndex != null) {
          ps3d.setInlineRange(_traceIndex.getTraceKeyMin(0), _traceIndex.getTraceKeyMax(0),
              _traceIndex.getTraceKeyInc(0));
          ps3d.setXlineRange(_traceIndex.getTraceKeyMin(1), _traceIndex.getTraceKeyMax(1),
              _traceIndex.getTraceKeyInc(1));
          ps3d.setOffsetRange(_traceIndex.getTraceKeyMin(2), _traceIndex.getTraceKeyMax(2),
              _traceIndex.getTraceKeyInc(2));
        }
        ps3d.setElevationDatum(0);
        // TODO Bill - may not need to create a new one every time?
        if (_traceIndex != null) {
          if (!model.getAutoCalculateGeometry()) {
            int inlineStart = model.getInlineStart();
            int inlineEnd = model.getInlineEnd();
            int inlineDelta = model.getInlineDelta();
            int xlineStart = model.getXlineStart();
            int xlineEnd = model.getXlineEnd();
            int xlineDelta = model.getXlineDelta();
            // Get x,y corner points from model, converting from model units to app units.
            Unit xyUnitApp = UnitPreferences.getInstance().getHorizontalDistanceUnit();
            double x0 = Unit.convert(model.getX0(), model.getUnitOfXY(), xyUnitApp);
            double y0 = Unit.convert(model.getY0(), model.getUnitOfXY(), xyUnitApp);
            double x1 = Unit.convert(model.getX1(), model.getUnitOfXY(), xyUnitApp);
            double y1 = Unit.convert(model.getY1(), model.getUnitOfXY(), xyUnitApp);
            double x2 = Unit.convert(model.getX2(), model.getUnitOfXY(), xyUnitApp);
            double y2 = Unit.convert(model.getY2(), model.getUnitOfXY(), xyUnitApp);
            double x3 = Unit.convert(model.getX3(), model.getUnitOfXY(), xyUnitApp);
            double y3 = Unit.convert(model.getY3(), model.getUnitOfXY(), xyUnitApp);
            Point3d[] points = new Point3d[4];
            points[0] = new Point3d(x0, y0, 0);
            points[1] = new Point3d(x1, y1, 0);
            points[2] = new Point3d(x2, y2, 0);
            points[3] = new Point3d(x3, y3, 0);
            CornerPointsSeries cornerPoints = CornerPointsSeries.create(points, new CoordinateSystem("",
                getFileAccessor().getDomain()));
            CoordinateSystem cs = new CoordinateSystem("", getFileAccessor().getDomain());
            PolygonType direction = PolygonUtil.getDirection(cornerPoints);
            SurveyOrientation orientation = SurveyOrientation.ROW_IS_INLINE;
            if (direction.equals(PolygonType.Clockwise)) {
              orientation = SurveyOrientation.ROW_IS_INLINE;
              // Flip the order of the points to be counter-clockwise.
              Point3d temp = points[1];
              points[1] = points[3];
              points[3] = temp;
              cornerPoints = CornerPointsSeries.create(points, cs);
            } else if (direction.equals(PolygonType.CounterClockwise)) {
              orientation = SurveyOrientation.ROW_IS_XLINE;
            } else {
              throw new RuntimeException("Invalid corner points for survey geometry.");
            }
            FloatRange inlineRange = new FloatRange(inlineStart, inlineEnd, inlineDelta);
            FloatRange xlineRange = new FloatRange(xlineStart, xlineEnd, xlineDelta);
            SeismicSurvey3d seismicGeometry = new SeismicSurvey3d("Geometry - " + model.getFileName(), inlineRange,
                xlineRange, cornerPoints, orientation);

            // Create a survey only if one is not already associated with the volume.
            if (ps3d.getSurvey() == null) {
              ps3d.setLastModifiedDate(lastModifiedDate);
              ps3d.setSurvey(seismicGeometry);
              ps3d.setDirty(false);
            }
          } else {
            // Get x,y corner points from the trace index, converting from model units to app units.
            CornerPointsSeries cornerPoints = getCornerPoints(_traceIndex, model.getXcoordByteLoc(),
                model.getYcoordByteLoc(), true);
            int inlineStart = _traceIndex.getTraceKeyMin(0);
            int inlineEnd = _traceIndex.getTraceKeyMax(0);
            int inlineDelta = _traceIndex.getTraceKeyInc(0);
            int xlineStart = _traceIndex.getTraceKeyMin(1);
            int xlineEnd = _traceIndex.getTraceKeyMax(1);
            int xlineDelta = _traceIndex.getTraceKeyInc(1);
            CoordinateSystem cs = new CoordinateSystem("", getFileAccessor().getDomain());
            PolygonType direction = PolygonUtil.getDirection(cornerPoints);
            SurveyOrientation orientation = SurveyOrientation.ROW_IS_INLINE;
            Point3d[] points = cornerPoints.getCopyOfPoints();
            if (direction.equals(PolygonType.Clockwise)) {
              orientation = SurveyOrientation.ROW_IS_INLINE;
              // Flip the order of the points to be counter-clockwise.
              Point3d temp = points[1];
              points[1] = points[3];
              points[3] = temp;
              cornerPoints = CornerPointsSeries.create(points, cs);
            } else if (direction.equals(PolygonType.CounterClockwise)) {
              orientation = SurveyOrientation.ROW_IS_XLINE;
            } else {
              throw new RuntimeException("Invalid corner points for survey geometry.");
            }
            FloatRange inlineRange = new FloatRange(inlineStart, inlineEnd, inlineDelta);
            FloatRange xlineRange = new FloatRange(xlineStart, xlineEnd, xlineDelta);
            SeismicSurvey3d seismicGeometry = new SeismicSurvey3d("Geometry - " + model.getFileName(), inlineRange,
                xlineRange, cornerPoints, orientation);

            // Create a survey only if one is not already associated with the volume.
            if (ps3d.getSurvey() == null) {
              ps3d.setLastModifiedDate(lastModifiedDate);
              ps3d.setSurvey(seismicGeometry);
              ps3d.setDirty(false);
            }

            // Update the model with the inline,xline start/end/delta values.
            model.setInlineStart(inlineStart);
            model.setInlineEnd(inlineEnd);
            model.setInlineDelta(inlineDelta);
            model.setXlineStart(xlineStart);
            model.setXlineEnd(xlineEnd);
            model.setXlineDelta(xlineDelta);

            // Get x,y corner points from the trace index.
            CoordinateSeries cornerPointSeries = getCornerPoints(_traceIndex, model.getXcoordByteLoc(),
                model.getYcoordByteLoc(), false);

            // Update the model with the x,y corner points from the trace index.
            switch (orientation) {
              case ROW_IS_INLINE:
                model.setX0(cornerPointSeries.getX(0));
                model.setY0(cornerPointSeries.getY(0));
                model.setX1(cornerPointSeries.getX(3));
                model.setY1(cornerPointSeries.getY(3));
                model.setX2(cornerPointSeries.getX(2));
                model.setY2(cornerPointSeries.getY(2));
                model.setX3(cornerPointSeries.getX(1));
                model.setY3(cornerPointSeries.getY(1));
                break;
              case ROW_IS_XLINE:
                model.setX0(cornerPointSeries.getX(0));
                model.setY0(cornerPointSeries.getY(0));
                model.setX1(cornerPointSeries.getX(1));
                model.setY1(cornerPointSeries.getY(1));
                model.setX2(cornerPointSeries.getX(2));
                model.setY2(cornerPointSeries.getY(2));
                model.setX3(cornerPointSeries.getX(3));
                model.setY3(cornerPointSeries.getY(3));
                break;
            }
          }
        }
      } catch (Exception e) {
        getLogger().error(e.toString());
      }
    }
  }

  /**
   * Closes the SEG-Y file from I/O access.
   * 
   * @param ps3d the PreStack3d volume.
   */
  @Override
  public void close() {
    super.close();
    checkTraceIndex(true);
  }

  /**
   * Checks if the trace index file needs to be created.
   * 
   * @param createIndex set <i>true<i> to build if needed.
   */
  public void checkTraceIndex(final boolean createIndex) {
    try {
      boolean needsIndexing = false;
      String ndxFilePath = getFilePath() + ".ndx";
      File datFile = new File(getFilePath());
      File ndxFile = new File(ndxFilePath);
      // Check if the index file exists.
      if (ndxFile.exists()) {
        // Check if the index file is older than the data file.
        if (ndxFile.lastModified() < datFile.lastModified()) {
          needsIndexing = true;
        } else {
          _traceIndex = new SegyTraceIndex(getFilePath() + ".ndx");
        }
      } else {
        needsIndexing = true;
      }
      if (needsIndexing && createIndex) {
        _traceIndex = SegyUtil.getTraceIndex(_model, SegyTraceIndex.IndexType.PRESTACK_3D);
      }
    } catch (Exception ex) {
      getLogger().error("Error accessing SEG-Y trace index file.", ex);
    }
  }

  /**
   * Gets the corner points of the SEG-Y geometry.
   * @param traceIndex the SEG-Y trace index.
   * @param xCoordByteLoc the x-coordinate byte location in the trace header.
   * @param yCoordByteLoc the y-coordinate byte location in the trace header.
   * @return the corner points of the PreStack3d geometry.
   * @throws IOException
   */
  private CornerPointsSeries getCornerPoints(final SegyTraceIndex traceIndex, final int xCoordByteLoc,
      final int yCoordByteLoc, final boolean convertToAppUnits) throws IOException {
    // Check if the corner points have already been computed. If not, compute them.
    ByteBuffer byteBuffer = ByteBuffer.allocate(4);
    RandomAccessFile randomAccessFile = new RandomAccessFile(getFilePath(), "r");
    FileChannel fileChannel = randomAccessFile.getChannel();
    int[] keys = new int[2];
    Point3d[] points = new Point3d[4];
    int ilineStart = traceIndex.getTraceKeyMin(0);
    int ilineEnd = traceIndex.getTraceKeyMax(0);
    int xlineStart = traceIndex.getTraceKeyMin(1);
    int xlineEnd = traceIndex.getTraceKeyMax(1);
    int[] ilines = { ilineStart, ilineEnd, ilineEnd, ilineStart };
    int[] xlines = { xlineStart, xlineStart, xlineEnd, xlineEnd };
    for (int i = 0; i < 4; i++) {
      keys[0] = ilines[i];
      keys[1] = xlines[i];
      long[] pos = new long[0];// TODO traceIndex.getTracePosition(keys);
      fileChannel.position(pos[0] + 70);
      byteBuffer.position(0);
      fileChannel.read(byteBuffer);
      byteBuffer.position(0);
      short scalar = byteBuffer.getShort();
      fileChannel.position(pos[0] + xCoordByteLoc - 1);
      byteBuffer.position(0);
      fileChannel.read(byteBuffer);
      byteBuffer.position(0);
      double x = byteBuffer.getInt(0);
      fileChannel.position(pos[0] + yCoordByteLoc - 1);
      byteBuffer.position(0);
      fileChannel.read(byteBuffer);
      byteBuffer.position(0);
      double y = byteBuffer.getInt(0);
      if (scalar > 0) {
        x *= scalar;
        y *= scalar;
      } else if (scalar < 0) {
        x /= -scalar;
        y /= -scalar;
      }
      if (convertToAppUnits) {
        try {
          x = Unit.convert(x, _model.getUnitOfXY(), UNIT_PREFS.getHorizontalDistanceUnit());
          y = Unit.convert(y, _model.getUnitOfXY(), UNIT_PREFS.getHorizontalDistanceUnit());
        } catch (Exception ex) {
          getLogger().error(ex.toString(), ex);
        }
      }
      points[i] = new Point3d(x, y, 0);
      //System.out.println("I=" + i + " X=" + x + " Y=" + y + " KEY0=" + keys[0] + " KEY1=" + keys[1]);
    }
    randomAccessFile.close();
    CornerPointsSeries cornerPoints = CornerPointsSeries.createDirect(points, new CoordinateSystem("",
        getFileAccessor().getDomain()));

    return cornerPoints;
  }

  //  /**
  //   * Forces a synchronization of the open/close. Upon closing, it will also create the trace index file if none exists.
  //   * 
  //   * @param operation the operation (Open or Close).
  //   */
  //  @Override
  //  protected void openclose(final OpenClose operation) {
  //    super.openclose(operation);
  //    if (operation.equals(OpenClose.CLOSE)) {
  //      checkTraceIndex(true);
  //    }
  //  }

  /**
   * Gets the storage order of the SEG-Y file. For SEG-Y, this will be either <i>Inline-Xline-Z</i> or <i>Xline-Inline-Z</i>.
   * 
   * @param the prestack3d mapped to the SEG-Y file.
   */
  public PreStack3d.StorageOrder getStorageOrder(final PreStack3d ps3d) {
    // Check if the storage order has been determined.
    if (_storageOrder == null) {
      // If not look it up from the mapper model.
      _storageOrder = PreStack3d.StorageOrder.lookupByName(_model.getStorageOrder());
    }

    // If it is a known and valid value, then return it.
    if (_storageOrder != null && !_storageOrder.equals(PreStack3d.StorageOrder.AUTO_CALCULATED)) {
      return _storageOrder;
    }

    // Otherwise, try to determine it be reading parts of the volume.
    _storageOrder = PreStack3d.StorageOrder.INLINE_XLINE_OFFSET_Z; // TODO: PreStack3dPreStack3d.StorageOrderTask.getPreStack3d.StorageOrder(ps3d);
    return _storageOrder;
  }

  /**
   * Gets the storage organization of the SEG-Y file.
   * For SEG-Y, the storage organization is always <i>TRACE</i>.
   */
  public StorageOrganization getStorageOrganization() {
    return StorageOrganization.TRACE;
  }

  public void setStorageOrder(final PreStack3d.StorageOrder storageOrder) {
    _storageOrder = storageOrder;
  }

  @Override
  public void deleteFromStore(final PreStack3d ps3d) throws IOException {
    if (_traceIndex != null) {
      _traceIndex.deleteFromStore();
      _traceIndex = null;
    }
    super.deleteFromStore();
  }

  @Override
  public void reinitialize() throws IOException {
    super.reinitialize();
    if (_traceIndex != null) {
      _traceIndex.deleteFromStore();
      _traceIndex = null;
    }
  }

  /**
   * Returns the header definition for a SEG-Y PreStack3d volume.
   */
  private HeaderDefinition getHeaderDefinition() {
    if (_headerDef == null) {
      _headerDef = SegyTraceHeader.PRESTACK3D_HEADER_DEF;
    }
    return _headerDef;
  }

  public TraceData getTraces(final PreStack3d ps3d, final float[] inlines, final float[] xlines, final float[] offsets,
      final float zStart, final float zEnd) {
    // Synchronize to prevent other access.
    synchronized (getSynchronizeToken()) {
      if (inlines.length != xlines.length) {
        throw new RuntimeException("Number of inlines (" + inlines.length + ") and xlines (" + xlines.length
            + ") do not match.");
      }
      getFileAccessor().openForRead();
      int numTraces = inlines.length;
      int numSamples = 0;
      CoordinateSeries coords = null;
      float deltaZ = ps3d.getZDelta();
      Unit zUnits = ps3d.getZUnit();
      Trace[] traces = new Trace[numTraces];
      SegyTraceHeader[] traceHeaders = new SegyTraceHeader[numTraces];
      try {
        // Compute the requested start/end z-indices (in storage units).
        float startz = Unit.convert(zStart, zUnits, _model.getUnitOfZ());
        float endz = Unit.convert(zEnd, zUnits, _model.getUnitOfZ());
        int iz0 = Math.round(startz / _model.getSampleRate());
        int iz1 = Math.round(endz / _model.getSampleRate());
        // Compute the volume start/end z-indices (in storage units).
        int jz0 = Math.round(Unit.convert(ps3d.getZStart(), ps3d.getZUnit(), _model.getUnitOfZ())
            / _model.getSampleRate());
        int jz1 = Math.round(Unit.convert(ps3d.getZEnd(), ps3d.getZUnit(), _model.getUnitOfZ())
            / _model.getSampleRate());
        numSamples = iz1 - iz0 + 1;
        ByteBuffer buffer = ByteBuffer.allocate(ps3d.getNumSamplesPerTrace() * 4);
        int[] keys = new int[3];
        float[] traceData = new float[ps3d.getNumSamplesPerTrace()];
        coords = ps3d.getSurvey().transformInlineXlineToXY(inlines, xlines);
        for (int i = 0; i < numTraces; i++) {
          // Create the header for this trace.
          traceHeaders[i] = new SegyTraceHeader(_headerDef);
          traceHeaders[i].getBuffer().position(0);
          int inline = Math.round(inlines[i]);
          int xline = Math.round(xlines[i]);
          int offset = Math.round(offsets[i]);
          keys[0] = inline;
          keys[1] = xline;
          keys[2] = offset;
          long[] tracePos = new long[0]; // TODO _traceIndex.getTracePosition(keys);
          for (int j = 0; j < Math.min(1, tracePos.length); j++) {
            // Read the trace header.
            getFileAccessor().setFilePosition(tracePos[j]);
            getFileAccessor().readByteBuffer(traceHeaders[i].getBuffer());
            // Read the trace data.
            buffer.position(0);
            getFileAccessor().readByteBuffer(buffer);
            getFileAccessor().getFloatsFromBytes(ps3d.getNumSamplesPerTrace(), buffer.array(), traceData);
            if (iz0 >= jz0 && iz1 <= jz1) {
              float[] tempData = new float[numSamples];
              System.arraycopy(traceData, iz0 - jz0, tempData, 0, numSamples);
              Trace.Status status = Trace.Status.Dead;
              for (int k = 0; k < numSamples; k++) {
                if (traceData[iz0 - jz0 + k] != 0f) {
                  status = Trace.Status.Live;
                  break;
                }
              }
              traces[i] = new Trace(zStart, deltaZ, zUnits, coords.getX(i), coords.getY(i), tempData, status);
            } else {
              String msg = "Requested z coordinates outside of extents (" + zStart + "-" + zEnd + " " + zUnits + ").";
              getLogger().error(msg);
              throw new Exception(msg);
            }
          }
          // If no traces were found, them create a 'missing' trace.
          if (tracePos.length < 1) {
            traces[i] = new Trace(zStart, deltaZ, zUnits, coords.getX(i), coords.getY(i), new float[numSamples],
                Trace.Status.Missing);
          }
        }
      } catch (Exception ex) {
        getLogger().error(ex.toString(), ex);
      }
      // Set the trace headers.
      for (int i = 0; i < numTraces; i++) {
        // Update the header from the byte buffer.
        traceHeaders[i].updateHeaderFromBuffer();

        // Override the header values for inline,xline,offset with the requested values.
        traceHeaders[i].putInteger(TraceHeaderCatalog.INLINE_NO, Math.round(inlines[i]));
        traceHeaders[i].putInteger(TraceHeaderCatalog.XLINE_NO, Math.round(xlines[i]));
        traceHeaders[i].putFloat(TraceHeaderCatalog.OFFSET, offsets[i]);

        // Override the x,y values with those calculated from the seismic geometry.
        traceHeaders[i].putDouble(TraceHeaderCatalog.X, coords.getX(i));
        traceHeaders[i].putDouble(TraceHeaderCatalog.Y, coords.getY(i));
        traces[i].setHeader(traceHeaders[i]);
      }
      return new TraceData(traces);
    }
  }

  public TraceData getTracesByInlineOffset(final PreStack3d ps3d, final float inline, final float offset,
      final float xlineStart, final float xlineEnd, final float zStart, final float zEnd) {
    float xlineDelta = Math.abs(ps3d.getSurvey().getXlineDelta());
    if (xlineEnd < xlineStart) {
      xlineDelta = -xlineDelta;
    }
    int numTraces = 1 + Math.round((xlineEnd - xlineStart) / xlineDelta);
    float[] inlines = new float[numTraces];
    float[] xlines = new float[numTraces];
    float[] offsets = new float[numTraces];
    for (int i = 0; i < numTraces; i++) {
      inlines[i] = inline;
      xlines[i] = xlineStart + i * xlineDelta;
      offsets[i] = offset;
    }
    return getTraces(ps3d, inlines, xlines, offsets, zStart, zEnd);
  }

  public TraceData getTracesByInlineXline(final PreStack3d ps3d, final float inline, final float xline,
      final float offsetStart, final float offsetEnd, final float zStart, final float zEnd) {
    float offsetDelta = ps3d.getOffsetDelta();
    if (offsetEnd < offsetStart) {
      offsetDelta = -offsetDelta;
    }
    int numTraces = 1 + Math.round((offsetEnd - offsetStart) / offsetDelta);
    float[] inlines = new float[numTraces];
    float[] xlines = new float[numTraces];
    float[] offsets = new float[numTraces];
    for (int i = 0; i < numTraces; i++) {
      inlines[i] = inline;
      xlines[i] = xline;
      offsets[i] = offsetStart + i * offsetDelta;
    }
    return getTraces(ps3d, inlines, xlines, offsets, zStart, zEnd);
  }

  public TraceData getTracesByXlineOffset(final PreStack3d ps3d, final float xline, final float offset,
      final float inlineStart, final float inlineEnd, final float zStart, final float zEnd) {
    float inlineDelta = Math.abs(ps3d.getSurvey().getInlineDelta());
    if (inlineEnd < inlineStart) {
      inlineDelta = -inlineDelta;
    }
    int numTraces = 1 + Math.round((inlineEnd - inlineStart) / inlineDelta);
    float[] inlines = new float[numTraces];
    float[] xlines = new float[numTraces];
    float[] offsets = new float[numTraces];
    for (int i = 0; i < numTraces; i++) {
      inlines[i] = inlineStart + i * inlineDelta;
      xlines[i] = xline;
      offsets[i] = offset;
    }
    return getTraces(ps3d, inlines, xlines, offsets, zStart, zEnd);
  }

  public void putTraces(final PreStack3d ps3d, final TraceData traceData) {
    Volume3dMapperModel model = (Volume3dMapperModel) _model;
    // Synchronize to prevent other access.
    synchronized (getSynchronizeToken()) {
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
      getFileAccessor().openForWrite();
      Unit zUnits = ps3d.getZUnit();
      try {
        float zstart = Unit.convert(zStart, zUnits, model.getUnitOfZ());
        float zend = Unit.convert(zEnd, zUnits, model.getUnitOfZ());
        float zstartVolume = Unit.convert(ps3d.getZStart(), zUnits, model.getUnitOfZ());
        float deltaz = Unit.convert(ps3d.getZDelta(), zUnits, model.getUnitOfZ());
        int iz0 = Math.round((zstart - zstartVolume) / deltaz);
        int iz1 = Math.round((zend - zstartVolume) / deltaz);
        int numSamples = iz1 - iz0 + 1;
        int numBytesPerSample = 4;
        int sampleFormatCode = getFileAccessor().getSampleFormatCode();
        if (sampleFormatCode == SegyBytes.SAMPLE_FORMAT_CODE_FIXED_2BYTE) {
          numBytesPerSample = 2;
        } else if (sampleFormatCode == SegyBytes.SAMPLE_FORMAT_CODE_FIXED_1BYTE) {
          numBytesPerSample = 1;
        }
        int numSamplesInVolume = ps3d.getNumSamplesPerTrace();
        ByteBuffer buffer = ByteBuffer.allocate(numSamplesInVolume * numBytesPerSample);
        int[] keys = new int[2];
        float[] data = new float[numSamplesInVolume];
        Unit xyUnitsSEGY = model.getUnitOfXY();
        Unit xyUnits = UNIT_PREFS.getHorizontalDistanceUnit();
        getFileAccessor().setFilePositionEOF();
        SegyTraceHeader segyTraceHeader = new SegyTraceHeader(_headerDef);
        Point3d[] points = ps3d.getSurvey().transformInlineXlineToXY(inlines, xlines).getPointsDirect();
        for (int i = 0; i < numTraces; i++) {
          Trace trace = traces[i];
          // Check the trace status. If missing, do not write.
          if (trace.getStatus().equals(Trace.Status.Missing)) {
            continue;
          }
          int iln = Math.round(inlines[i]);
          int xln = Math.round(xlines[i]);
          keys[0] = iln;
          keys[1] = xln;
          int x = (int) Math.round(points[i].getX());
          int y = (int) Math.round(points[i].getY());
          x = (int) Unit.convert(x, xyUnits, xyUnitsSEGY);
          y = (int) Unit.convert(y, xyUnits, xyUnitsSEGY);

          // Update the trace header with the start time (or depth).
          segyTraceHeader.putShort(SegyTraceHeaderCatalog.DELAY_RECORDING_TIME.getKey(), (short) zstart);

          // Update the trace header with the # of sample and sample rate.
          segyTraceHeader.putShort(SegyTraceHeaderCatalog.NUM_SAMPLES.getKey(), (short) numSamples);
          segyTraceHeader.putShort(SegyTraceHeaderCatalog.SAMPLE_INTERVAL.getKey(), (short) (deltaz * 1000));

          // Update the trace header with the offset.
          int offset = Math.round(trace.getHeader().getFloat(TraceHeaderCatalog.OFFSET));
          segyTraceHeader.putInteger(SegyTraceHeaderCatalog.SOURCE_RECEIVER_DISTANCE.getKey(), offset);

          // Update the byte buffer from the header.
          segyTraceHeader.updateBufferFromHeader();
          ByteBuffer hdrBuffer = segyTraceHeader.getBuffer();
          // Update the trace header with the inline,xline,offset coordinates.
          hdrBuffer.putInt(model.getInlineByteLoc() - 1, iln);
          hdrBuffer.putInt(model.getXlineByteLoc() - 1, xln);
          hdrBuffer.putInt(model.getOffsetByteLoc() - 1, offset);

          // Update the trace header with the x,y coordinates.
          hdrBuffer.putInt(model.getXcoordByteLoc() - 1, x);
          hdrBuffer.putInt(model.getYcoordByteLoc() - 1, y);

          long tracePosition = getFileAccessor().getFilePosition();

          // Write the trace header.
          hdrBuffer.position(0);
          getFileAccessor().writeByteBuffer(hdrBuffer);

          // Write the trace data.
          System.arraycopy(trace.getDataReference(), 0, data, iz0, numSamples);
          buffer.position(0);
          getFileAccessor().putFloatsToBytes(data.length, data, buffer.array());
          buffer.position(0);
          getFileAccessor().writeByteBuffer(buffer);

          int[] traceKeyVals = { iln, xln, offset };
          _traceIndex.mapTrace(traceKeyVals, tracePosition);
        }
      } catch (Exception ex) {
        getLogger().error(ex.toString(), ex);
      }
    }
  }

  @Override
  public String getDatastoreEntryDescription() {
    return "SEG-Y PreStack3d";
  }

  public String getDatastore() {
    return "SEG-Y";
  }

}
