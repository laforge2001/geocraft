package org.geocraft.io.remote;


import java.io.IOException;
import java.sql.Timestamp;

import org.geocraft.core.model.AbstractMapper;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.CornerPointsSeries;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.Header;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.TraceHeaderCatalog;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.IPostStack3dMapper.StorageOrganization;
import org.geocraft.core.model.mapper.IPreStack3dMapper;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.model.seismic.PreStack3d.StorageOrder;
import org.geocraft.core.model.seismic.SeismicDataset.StorageFormat;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.model.seismic.SurveyOrientation;
import org.geocraft.core.service.ServiceProvider;


public class RemotePreStack3dMapper extends AbstractMapper<PreStack3d> implements IPreStack3dMapper {

  private final RemotePreStack3dMapperModel _model;

  private final Trace[][][] _traceCache;

  public RemotePreStack3dMapper(final RemotePreStack3dMapperModel model) {
    _model = model;
    FloatRange inlineRange = new FloatRange(model.getInlineStart(), model.getInlineEnd(), model.getInlineDelta());
    FloatRange xlineRange = new FloatRange(model.getXlineStart(), model.getXlineEnd(), model.getXlineDelta());
    FloatRange offsetRange = new FloatRange(model.getOffsetStart(), model.getOffsetEnd(), model.getOffsetDelta());
    int numInlines = inlineRange.getNumSteps();
    int numXlines = xlineRange.getNumSteps();
    int numOffsets = offsetRange.getNumSteps();
    _traceCache = new Trace[numInlines][numXlines][numOffsets];
  }

  @Override
  public RemotePreStack3dMapper factory(final MapperModel mapperModel) {
    return new RemotePreStack3dMapper((RemotePreStack3dMapperModel) mapperModel);
  }

  @Override
  protected RemotePreStack3dMapperModel getInternalModel() {
    return _model;
  }

  public RemotePreStack3dMapperModel getModel() {
    return new RemotePreStack3dMapperModel(_model);
  }

  public StorageOrganization getStorageOrganization() {
    return StorageOrganization.TRACE;
  }

  public StorageOrder getStorageOrder(final PreStack3d ps3d) {
    return StorageOrder.AUTO_CALCULATED;
  }

  @Override
  protected void createInStore(final PreStack3d ps3d) throws IOException {
    throw new IOException("Cannot create a socket-based dataset.");
  }

  @Override
  protected void readFromStore(final PreStack3d ps3d) throws IOException {
    ps3d.setDataUnit(_model.getDataUnit());
    ps3d.setZDomain(_model.getDomain());
    float zStart = _model.getStartZ();
    float zEnd = _model.getEndZ();
    float zDelta = _model.getDeltaZ();
    Unit zUnit = _model.getUnitOfZ();
    Unit zUnitApp = null;
    if (_model.getDomain().equals(Domain.TIME)) {
      zUnitApp = UnitPreferences.getInstance().getTimeUnit();
    } else if (_model.getDomain().equals(Domain.DISTANCE)) {
      zUnitApp = UnitPreferences.getInstance().getVerticalDistanceUnit();
    } else {
      throw new IllegalArgumentException("Invalid domain: " + _model.getDomain());
    }
    zStart = Unit.convert(zStart, zUnit, zUnitApp);
    zEnd = Unit.convert(zEnd, zUnit, zUnitApp);
    zDelta = Unit.convert(zDelta, zUnit, zUnitApp);
    ps3d.setZRangeAndDelta(zStart, zEnd, zDelta);
    ps3d.setLastModifiedDate(new Timestamp(System.currentTimeMillis()));
    ps3d.setTraceHeaderDefinition(_model.getTraceHeaderDef());
    float inlineStart = _model.getInlineStart();
    float inlineEnd = _model.getInlineEnd();
    float inlineDelta = _model.getInlineDelta();
    ps3d.setInlineRange(inlineStart, inlineEnd, inlineDelta);
    float xlineStart = _model.getXlineStart();
    float xlineEnd = _model.getXlineEnd();
    float xlineDelta = _model.getXlineDelta();
    ps3d.setXlineRange(xlineStart, xlineEnd, xlineDelta);
    float offsetStart = _model.getOffsetStart();
    float offsetEnd = _model.getOffsetEnd();
    float offsetDelta = _model.getOffsetDelta();
    ps3d.setOffsetRange(offsetStart, offsetEnd, offsetDelta);
    ps3d.setElevationDatum(0f);
    // Create a seismic geometry in which to put the prestack.
    Point3d[] points = new Point3d[4];
    points[0] = new Point3d(_model.getX0(), _model.getY0(), 0);
    points[1] = new Point3d(_model.getX1(), _model.getY1(), 0);
    points[3] = new Point3d(_model.getX2(), _model.getY2(), 0);
    double dx = _model.getX1() - _model.getX0() + _model.getX2() - _model.getX0();
    double dy = _model.getY1() - _model.getY0() + _model.getY2() - _model.getY0();
    points[2] = new Point3d(_model.getX0() + dx, _model.getY0() + dy, 0);
    CornerPointsSeries cornerPoints = CornerPointsSeries.createDirect(points,
        new CoordinateSystem("", _model.getDomain()));

    FloatRange inlineRange = new FloatRange(inlineStart, inlineEnd, inlineDelta);

    FloatRange xlineRange = new FloatRange(xlineStart, xlineEnd, xlineDelta);

    ps3d.setInlineRange(inlineStart, inlineEnd, inlineDelta);
    ps3d.setXlineRange(xlineStart, xlineEnd, xlineDelta);
    ps3d.setOffsetRange(offsetStart, offsetEnd, offsetDelta);

    if (ps3d.getSurvey() == null) {
      SeismicSurvey3d seismicGeometry = new SeismicSurvey3d(ps3d.getDisplayName() + " Geometry", inlineRange,
          xlineRange, cornerPoints, SurveyOrientation.ROW_IS_INLINE);
      ps3d.setSurvey(seismicGeometry);
    }
    ps3d.setDirty(false);
  }

  @Override
  protected void updateInStore(final PreStack3d ps3d) throws IOException {
    throw new IOException("Cannot currently update a socket-based dataset.");
  }

  @Override
  protected void deleteFromStore(final PreStack3d ps3d) throws IOException {
    throw new IOException("Cannot delete a socket-based dataset.");
  }

  @Override
  public TraceData getTraces(final PreStack3d ps3d, final float[] inlines, final float[] xlines, final float[] offsets,
      final float zStart, final float zEnd) {
    synchronized (_traceCache) {
      int numTraces = inlines.length;
      int numSamples = 1 + Math.round((zEnd - zStart) / ps3d.getZDelta());
      Trace[] traces = new Trace[numTraces];
      CoordinateSeries coords = ps3d.getSurvey().transformInlineXlineToXY(inlines, xlines);
      for (int i = 0; i < numTraces; i++) {
        int inlineIndex = Math.round((inlines[i] - ps3d.getInlineStart()) / ps3d.getInlineDelta());
        int xlineIndex = Math.round((xlines[i] - ps3d.getXlineStart()) / ps3d.getXlineDelta());
        int offsetIndex = Math.round((offsets[i] - ps3d.getOffsetStart()) / ps3d.getOffsetDelta());
        Trace trace = _traceCache[inlineIndex][xlineIndex][offsetIndex];
        if (trace == null) {
          trace = new Trace(zStart, ps3d.getZDelta(), ps3d.getZUnit(), coords.getX(i), coords.getY(i),
              new float[numSamples], Trace.Status.Missing);
          Header traceHeader = new Header(_model.getTraceHeaderDef());
          traceHeader.putInteger(TraceHeaderCatalog.INLINE_NO, (int) inlines[i]);
          traceHeader.putInteger(TraceHeaderCatalog.XLINE_NO, (int) xlines[i]);
          traceHeader.putFloat(TraceHeaderCatalog.OFFSET, offsets[i]);
          trace.setHeader(traceHeader);
        }
        traces[i] = trace;
      }
      return new TraceData(traces);
    }
  }

  @Override
  public TraceData getTracesByInlineOffset(final PreStack3d ps3d, final float inline, final float offset,
      final float xlineStart, final float xlineEnd, final float zStart, final float zEnd) {
    synchronized (_traceCache) {
      int inlineIndex = Math.round((inline - ps3d.getInlineStart()) / ps3d.getInlineDelta());
      int offsetIndex = Math.round((offset - ps3d.getOffsetStart()) / ps3d.getOffsetDelta());
      int xlineIndex0 = Math.round((xlineStart - ps3d.getXlineStart()) / ps3d.getXlineDelta());
      int xlineIndex1 = Math.round((xlineEnd - ps3d.getXlineStart()) / ps3d.getXlineDelta());
      int numTraces = 1 + Math.abs(xlineIndex0 - xlineIndex1);
      int numSamples = 1 + Math.round((zEnd - zStart) / ps3d.getZDelta());
      Trace[] traces = new Trace[numTraces];
      for (int i = 0; i < numTraces; i++) {
        int xlineIndex = xlineIndex0 + i;
        Trace trace = _traceCache[inlineIndex][xlineIndex][offsetIndex];
        if (trace == null) {
          float xline = xlineStart + i * ps3d.getXlineDelta();
          float[] inlines = new float[] { inline };
          float[] xlines = new float[] { xline };
          CoordinateSeries coords = ps3d.getSurvey().transformInlineXlineToXY(inlines, xlines);
          trace = new Trace(zStart, ps3d.getZDelta(), ps3d.getZUnit(), coords.getX(0), coords.getY(0),
              new float[numSamples], Trace.Status.Missing);
          Header traceHeader = new Header(_model.getTraceHeaderDef());
          traceHeader.putInteger(TraceHeaderCatalog.INLINE_NO, (int) inline);
          traceHeader.putInteger(TraceHeaderCatalog.XLINE_NO, (int) xline);
          traceHeader.putFloat(TraceHeaderCatalog.OFFSET, offset);
          trace.setHeader(traceHeader);
        }
        traces[i] = trace;
      }
      return new TraceData(traces);
    }
  }

  @Override
  public TraceData getTracesByInlineXline(final PreStack3d ps3d, final float inline, final float xline,
      final float offsetStart, final float offsetEnd, final float zStart, final float zEnd) {
    synchronized (_traceCache) {
      int inlineIndex = Math.round((inline - ps3d.getInlineStart()) / ps3d.getInlineDelta());
      int xlineIndex = Math.round((xline - ps3d.getXlineStart()) / ps3d.getXlineDelta());
      int offsetIndex0 = Math.round((offsetStart - ps3d.getOffsetStart()) / ps3d.getOffsetDelta());
      int offsetIndex1 = Math.round((offsetEnd - ps3d.getOffsetStart()) / ps3d.getOffsetDelta());
      int numTraces = 1 + Math.abs(offsetIndex0 - offsetIndex1);
      int numSamples = 1 + Math.round((zEnd - zStart) / ps3d.getZDelta());
      Trace[] traces = new Trace[numTraces];
      for (int i = 0; i < numTraces; i++) {
        int offsetIndex = offsetIndex0 + i;
        Trace trace = _traceCache[inlineIndex][xlineIndex][offsetIndex];
        if (trace == null) {
          float offset = offsetStart + i * ps3d.getOffsetDelta();
          float[] inlines = new float[] { inline };
          float[] xlines = new float[] { xline };
          CoordinateSeries coords = ps3d.getSurvey().transformInlineXlineToXY(inlines, xlines);
          trace = new Trace(zStart, ps3d.getZDelta(), ps3d.getZUnit(), coords.getX(0), coords.getY(0),
              new float[numSamples], Trace.Status.Missing);
          Header traceHeader = new Header(_model.getTraceHeaderDef());
          traceHeader.putInteger(TraceHeaderCatalog.INLINE_NO, (int) inline);
          traceHeader.putInteger(TraceHeaderCatalog.XLINE_NO, (int) xline);
          traceHeader.putFloat(TraceHeaderCatalog.OFFSET, offset);
          trace.setHeader(traceHeader);
        }
        traces[i] = trace;
      }
      return new TraceData(traces);
    }
  }

  @Override
  public TraceData getTracesByXlineOffset(final PreStack3d ps3d, final float xline, final float offset,
      final float inlineStart, final float inlineEnd, final float zStart, final float zEnd) {
    synchronized (_traceCache) {
      int xlineIndex = Math.round((xline - ps3d.getXlineStart()) / ps3d.getXlineDelta());
      int offsetIndex = Math.round((offset - ps3d.getOffsetStart()) / ps3d.getOffsetDelta());
      int inlineIndex0 = Math.round((inlineStart - ps3d.getInlineStart()) / ps3d.getInlineDelta());
      int inlineIndex1 = Math.round((inlineEnd - ps3d.getInlineStart()) / ps3d.getInlineDelta());
      int numTraces = 1 + Math.abs(inlineIndex0 - inlineIndex1);
      int numSamples = 1 + Math.round((zEnd - zStart) / ps3d.getZDelta());
      Trace[] traces = new Trace[numTraces];
      for (int i = 0; i < numTraces; i++) {
        int inlineIndex = inlineIndex0 + i;
        Trace trace = _traceCache[inlineIndex][xlineIndex][offsetIndex];
        if (trace == null) {
          float inline = inlineStart + i * ps3d.getInlineDelta();
          float[] inlines = new float[] { inline };
          float[] xlines = new float[] { xline };
          CoordinateSeries coords = ps3d.getSurvey().transformInlineXlineToXY(inlines, xlines);
          trace = new Trace(zStart, ps3d.getZDelta(), ps3d.getZUnit(), coords.getX(0), coords.getY(0),
              new float[numSamples], Trace.Status.Missing);
          Header traceHeader = new Header(_model.getTraceHeaderDef());
          traceHeader.putInteger(TraceHeaderCatalog.INLINE_NO, (int) inline);
          traceHeader.putInteger(TraceHeaderCatalog.XLINE_NO, (int) xline);
          traceHeader.putFloat(TraceHeaderCatalog.OFFSET, offset);
          trace.setHeader(traceHeader);
        }
        traces[i] = trace;
        System.out
            .println("TRACE " + i + " Z=" + trace.getZStart() + " " + trace.getZEnd() + " " + zStart + " " + zEnd);
      }
      return new TraceData(traces);
    }
  }

  public void putTraces(final PreStack3d ps3d, final TraceData traceData) {
    synchronized (_traceCache) {
      Trace[] traces = traceData.getTraces();
      for (Trace trace : traces) {
        float inline = trace.getHeader().getInteger(TraceHeaderCatalog.INLINE_NO);
        float xline = trace.getHeader().getInteger(TraceHeaderCatalog.XLINE_NO);
        float offset = trace.getHeader().getFloat(TraceHeaderCatalog.OFFSET);
        int inlineIndex = Math.round((inline - ps3d.getInlineStart()) / ps3d.getInlineDelta());
        int xlineIndex = Math.round((xline - ps3d.getXlineStart()) / ps3d.getXlineDelta());
        int offsetIndex = Math.round((offset - ps3d.getOffsetStart()) / ps3d.getOffsetDelta());
        if (inlineIndex >= 0 && inlineIndex < ps3d.getNumInlines() && xlineIndex >= 0
            && xlineIndex < ps3d.getNumXlines() && offsetIndex >= 0 && offsetIndex < ps3d.getNumOffsets()) {
          _traceCache[inlineIndex][xlineIndex][offsetIndex] = trace;
        }
      }
    }

    Object[] dataObjects = new Object[2];
    dataObjects[0] = ps3d;
    dataObjects[1] = traceData;
    ServiceProvider.getMessageService().publish("EntityChangeEvent", dataObjects);
  }

  public void setStorageOrder(final StorageOrder storageOrder) {
    throw new RuntimeException("Cannot set the storage order of a socket-based dataset.");
  }

  public void setStorageOrganization(final StorageOrganization storageOrganization) {
    throw new RuntimeException("Cannot set the storage organization of a socket-based dataset.");
  }

  public void close() {
    // TODO Auto-generated method stub
  }

  public StorageFormat getStorageFormat() {
    return StorageFormat.FLOAT_32;
  }

  public void setStorageFormat(final StorageFormat storageType) {
    throw new RuntimeException("Cannot set the storage type of a socket-based dataset.");
  }

  @Override
  public void setDomain(Domain domain) {
    _model.setDomain(domain);
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.mapper.IMapper#getDatastore()
   */
  @Override
  public String getDatastore() {
    // TODO Auto-generated method stub
    return "remote mapper";
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.AbstractMapper#getDatastoreEntryDescription()
   */
  @Override
  public String getDatastoreEntryDescription() {
    // TODO Auto-generated method stub
    return "prestack or something";
  }

}
