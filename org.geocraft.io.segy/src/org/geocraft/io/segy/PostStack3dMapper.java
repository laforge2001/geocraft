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

import org.geocraft.core.io.PostStack3dStorageOrderTask;
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
import org.geocraft.core.model.mapper.IPostStack3dMapper;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PostStack3d.SliceBufferOrder;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;
import org.geocraft.core.model.seismic.SeismicDataset.StorageFormat;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.model.seismic.SurveyOrientation;
import org.geocraft.io.segy.SegyTraceIndex.IndexType;


/**
 * Defines the class for mapping between a PostStack3d volume and a SEG-Y file on disk.
 * It is extended from the SegyMapper abstract base class.
 */
public class PostStack3dMapper extends SegyMapper<PostStack3d> implements IPostStack3dMapper {

  /** A reference to the unit preferences. */
  private static final UnitPreferences UNIT_PREFS = UnitPreferences.getInstance();

  /**
   * The storage order of trace data in the SEG-Y file.
   * Useful for determining the optimal processing direction.
   */
  private StorageOrder _storageOrder;

  /** The SEG-Y trace index file. */
  private SegyTraceIndex _traceIndex;

  /**
   * The default constructor.
   * See the <code>SegyMapper</code> constructor for more information.
   */
  public PostStack3dMapper(final VolumeMapperModel model) {
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
  public PostStack3dMapper(final VolumeMapperModel model, final boolean createIndex) {
    super(model);
    getHeaderDefinition();

    // Generate the trace index file, if necessary.
    if (createIndex) {
      checkTraceIndex();
    }

    // If the storage order is already set in the model, then go ahead and set it in the mapper
    // to prevent its calculation again.
    String storageOrderStr = _model.getStorageOrder();
    if (storageOrderStr != null && storageOrderStr.equals(PostStack3d.StorageOrder.AUTO_CALCULATED.getTitle())) {
      _storageOrder = PostStack3d.StorageOrder.lookupByName(_model.getStorageOrder());
    }
  }

  @Override
  public PostStack3dMapper factory(final MapperModel mapperModel) {
    // Use false as the 2nd argument to prevent trying to index, since this factory
    // method is used trying to create new volumes, which don't yet exist.
    return new PostStack3dMapper((VolumeMapperModel) mapperModel, false); // TODO index?
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

  /**
   * Gets an inline of traces from the SEG-Y file.
   * This method simply creates arrays of inlines and xlines and
   * the calls into the general <code>getTraces</code> method.
   * 
   * @param ps3d the poststack3d mapped to the SEG-Y file.
   * @param inline the inline.
   * @param xlineStart the starting xline.
   * @param xlineEnd the ending xline.
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   */
  public TraceData getInline(final PostStack3d ps3d, final float inline, final float xlineStart, final float xlineEnd,
      final float zStart, final float zEnd) {
    float xlineDelta = Math.abs(ps3d.getSurvey().getXlineDelta());
    if (xlineEnd < xlineStart) {
      xlineDelta = -xlineDelta;
    }
    int numTraces = 1 + Math.round((xlineEnd - xlineStart) / xlineDelta);
    float[] inlines = new float[numTraces];
    float[] xlines = new float[numTraces];
    for (int i = 0; i < numTraces; i++) {
      inlines[i] = inline;
      xlines[i] = xlineStart + i * xlineDelta;
    }
    return getTraces(ps3d, inlines, xlines, zStart, zEnd);
  }

  /**
   * Gets an xline of traces from the SEG-Y file.
   * This method simply creates arrays of inlines and xlines and
   * the calls into the general <code>getTraces</code> method.
   * 
   * @param ps3d the poststack3d mapped to the SEG-Y file.
   * @param xline the xline.
   * @param inlineStart the starting inline.
   * @param inlineEnd the ending inline.
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   */
  public TraceData getXline(final PostStack3d ps3d, final float xline, final float inlineStart, final float inlineEnd,
      final float zStart, final float zEnd) {
    float inlineDelta = Math.abs(ps3d.getSurvey().getInlineDelta());
    if (inlineEnd < inlineStart) {
      inlineDelta = -inlineDelta;
    }
    int numTraces = 1 + Math.round((inlineEnd - inlineStart) / inlineDelta);
    float[] inlines = new float[numTraces];
    float[] xlines = new float[numTraces];
    for (int i = 0; i < numTraces; i++) {
      inlines[i] = inlineStart + i * inlineDelta;
      xlines[i] = xline;
    }
    return getTraces(ps3d, inlines, xlines, zStart, zEnd);
  }

  /**
   * Gets traces from the SEG-Y file.
   * 
   * @param ps3d the poststack3d mapped to the SEG-Y file.
   * @param inlines the array of inlines.
   * @param xlines the array of xlines.
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   */
  public TraceData getTraces(final PostStack3d ps3d, final float[] inlines, final float[] xlines, final float zStart,
      final float zEnd) {

    // Synchronize to prevent other access.
    synchronized (getSynchronizeToken()) {
      if (inlines.length != xlines.length) {
        throw new IllegalArgumentException("Number of inlines (" + inlines.length + ") and xlines (" + xlines.length
            + ") do not match.");
      }

      // Open the file accessor.
      getFileAccessor().openForRead();
      int numTraces = inlines.length;
      int numSamples = 0;
      CoordinateSeries coords = null;
      float deltaZ = ps3d.getZDelta();
      Unit zUnits = ps3d.getZUnit();

      // Allocate arrays for the output trace and trace headers.
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
        int[] keys = new int[2];
        float[] traceData = new float[ps3d.getNumSamplesPerTrace()];
        coords = ps3d.getSurvey().transformInlineXlineToXY(inlines, xlines);

        // Loop over the required number of traces.
        for (int i = 0; i < numTraces; i++) {

          // Create the header for this trace.
          traceHeaders[i] = new SegyTraceHeader(_headerDef);
          traceHeaders[i].getBuffer().position(0);
          int iln = Math.round(inlines[i]);
          int xln = Math.round(xlines[i]);
          keys[0] = iln;
          keys[1] = xln;
          long tracePos = _traceIndex.getTracePosition(keys);

          // Determine the number of traces to read.
          // In the event that the trace index contains more
          // than 1 trace for a given inline,xline location,
          // simply read the 1st trace. If the trace index does
          // not contain an entry for the inline,xline location,
          // then no traces will be read.
          int numTracesToRead = tracePos == 0 ? 0 : 1;

          for (int j = 0; j < numTracesToRead; j++) {
            // Read the trace header.
            getFileAccessor().setFilePosition(tracePos);
            getFileAccessor().readByteBuffer(traceHeaders[i].getBuffer());

            // Read the trace data.
            buffer.position(0);
            getFileAccessor().readByteBuffer(buffer);
            getFileAccessor().getFloatsFromBytes(ps3d.getNumSamplesPerTrace(), buffer.array(), traceData);
            if (iz0 >= jz0 && iz1 <= jz1) {
              // Flag the trace as 'Live' or 'Dead', depending on if
              // there are any non-zero values.
              float[] tempData = new float[numSamples];
              System.arraycopy(traceData, iz0 - jz0, tempData, 0, numSamples);
              Trace.Status status = Trace.Status.Dead;
              for (int k = 0; k < numSamples; k++) {
                if (traceData[iz0 - jz0 + k] != 0f) {
                  status = Trace.Status.Live;
                  break;
                }
              }

              // Create an output trace.
              traces[i] = new Trace(zStart, deltaZ, zUnits, coords.getX(i), coords.getY(i), tempData, status);
            } else {
              String msg = "Requested z coordinates outside of extents (" + zStart + "-" + zEnd + " " + zUnits + ").";
              getLogger().error(msg);
              throw new Exception(msg);
            }
          }
          // If no traces were found, then create a 'Missing' trace.
          if (tracePos == 0) {
            traces[i] = new Trace(zStart, deltaZ, zUnits, coords.getX(i), coords.getY(i), new float[numSamples],
                Trace.Status.Missing);
          }
        }
      } catch (Exception ex) {
        throw new RuntimeException(ex.toString(), ex);
      }

      // Set the trace headers.
      for (int i = 0; i < numTraces; i++) {
        // Update the header from the byte buffer.
        traceHeaders[i].updateHeaderFromBuffer();

        // Override the header values for inline,xline with the requested values.
        traceHeaders[i].putInteger(TraceHeaderCatalog.INLINE_NO, Math.round(inlines[i]));
        traceHeaders[i].putInteger(TraceHeaderCatalog.XLINE_NO, Math.round(xlines[i]));

        // Override the x,y values with those calculated from the seismic geometry.
        traceHeaders[i].putDouble(TraceHeaderCatalog.X, coords.getX(i));
        traceHeaders[i].putDouble(TraceHeaderCatalog.Y, coords.getY(i));
        traces[i].setHeader(traceHeaders[i]);
      }
      return new TraceData(traces);
    }
  }

  public void putTraces(final PostStack3d ps3d, final TraceData traceData) {
    Volume3dMapperModel model = (Volume3dMapperModel) _model;

    // Synchronize to prevent other access.
    synchronized (getSynchronizeToken()) {
      int numTraces = traceData.getNumTraces();
      float[] inlines = new float[numTraces];
      float[] xlines = new float[numTraces];
      Trace[] traces = traceData.getTraces();
      for (int i = 0; i < numTraces; i++) {
        if (traces[i].isMissing()) {
          inlines[i] = 0;
          xlines[i] = 0;
        } else {
          inlines[i] = traces[i].getInline();
          xlines[i] = traces[i].getXline();
        }
      }
      float zStart = traceData.getStartZ();
      float zEnd = traceData.getEndZ();
      getFileAccessor().openForWrite();
      Unit zUnits = ps3d.getZUnit();
      try {
        // Convert the z-start and z-end values of the trace data to the
        // z unit of measurement for storage.
        zStart = Unit.convert(zStart, zUnits, model.getUnitOfZ());
        zEnd = Unit.convert(zEnd, zUnits, model.getUnitOfZ());

        // Convert the z-start and z-delta values of the volume to
        // the z unit of measurement for storage.
        float zStartVolume = Unit.convert(ps3d.getZStart(), zUnits, model.getUnitOfZ());
        float zDelta = Unit.convert(ps3d.getZDelta(), zUnits, model.getUnitOfZ());

        int iz0 = Math.round((zStart - zStartVolume) / zDelta);
        int iz1 = Math.round((zEnd - zStartVolume) / zDelta);
        int numSamples = iz1 - iz0 + 1;

        // Determine the # of bytes per sample based on the SEG-Y format code.
        int numBytesPerSample = 4;
        int sampleFormatCode = getFileAccessor().getSampleFormatCode();
        if (sampleFormatCode == SegyBytes.SAMPLE_FORMAT_CODE_FIXED_2BYTE) {
          numBytesPerSample = 2;
        } else if (sampleFormatCode == SegyBytes.SAMPLE_FORMAT_CODE_FIXED_1BYTE) {
          numBytesPerSample = 1;
        }
        int numSamplesInVolume = ps3d.getNumSamplesPerTrace();

        // Allocate a byte buffer of sufficient size to hold the number of
        // samples for a single trace.
        ByteBuffer buffer = ByteBuffer.allocate(numSamplesInVolume * numBytesPerSample);
        int[] keys = new int[2];
        float[] data = new float[numSamplesInVolume];
        Unit xyUnitsSEGY = model.getUnitOfXY();
        Unit xyUnits = UNIT_PREFS.getHorizontalDistanceUnit();
        getFileAccessor().setFilePositionEOF();
        SegyTraceHeader segyTraceHeader = new SegyTraceHeader(_headerDef);
        Point3d[] points = ps3d.getSurvey().transformInlineXlineToXY(inlines, xlines).getPointsDirect();

        // Loop over each trace being written.
        for (int i = 0; i < numTraces; i++) {
          Trace trace = traces[i];

          // Check the trace status. If 'Missing', do not write.
          if (trace.getStatus().equals(Trace.Status.Missing)) {
            continue;
          }
          int iln = Math.round(inlines[i]);
          int xln = Math.round(xlines[i]);
          keys[0] = iln;
          keys[1] = xln;
          int x = (int) Math.round(points[i].getX());
          int y = (int) Math.round(points[i].getY());

          // Convert the x,y coordinates to the units of storage.
          x = (int) Unit.convert(x, xyUnits, xyUnitsSEGY);
          y = (int) Unit.convert(y, xyUnits, xyUnitsSEGY);

          // Determine the offset.
          // This can either be the SEG-Y trace header element or the
          // general trace header element.
          //          }

          // Update the trace header with the start time (or depth).
          segyTraceHeader.putShort(SegyTraceHeaderCatalog.DELAY_RECORDING_TIME, (short) zStartVolume);

          // Update the trace header with the # of sample and sample rate.
          segyTraceHeader.putShort(SegyTraceHeaderCatalog.NUM_SAMPLES, (short) numSamplesInVolume);
          segyTraceHeader.putShort(SegyTraceHeaderCatalog.SAMPLE_INTERVAL, (short) (zDelta * 1000));

          // Update the trace header with the offset.
          int offset = 0;
          if (trace.getHeader().getHeaderDefinition().contains(TraceHeaderCatalog.OFFSET)) {
            offset = Math.round(trace.getHeader().getFloat(TraceHeaderCatalog.OFFSET));
            segyTraceHeader.putInteger(SegyTraceHeaderCatalog.SOURCE_RECEIVER_DISTANCE, offset);
          }

          // Update the byte buffer from the header.
          segyTraceHeader.updateBufferFromHeader();
          ByteBuffer hdrBuffer = segyTraceHeader.getBuffer();

          // Update the trace header with the inline,xline coordinates.
          hdrBuffer.putInt(model.getInlineByteLoc() - 1, iln);
          hdrBuffer.putInt(model.getXlineByteLoc() - 1, xln);

          // Update the trace header with the x,y coordinates.
          hdrBuffer.putInt(model.getXcoordByteLoc() - 1, x);
          hdrBuffer.putInt(model.getYcoordByteLoc() - 1, y);

          int[] traceKeyVals = { iln, xln };
          long tracePosition = _traceIndex.getTracePosition(traceKeyVals);
          if (tracePosition == 0) {
            tracePosition = getFileAccessor().getFileSize();
          }
          getFileAccessor().setFilePosition(tracePosition);

          // Write the trace header.
          hdrBuffer.position(0);
          getFileAccessor().writeByteBuffer(hdrBuffer);

          // Write the trace data.
          System.arraycopy(trace.getDataReference(), 0, data, iz0, numSamples);
          buffer.position(0);
          getFileAccessor().putFloatsToBytes(data.length, data, buffer.array());
          buffer.position(0);
          getFileAccessor().writeByteBuffer(buffer);

          //int[] traceKeyVals = { iln, xln };
          _traceIndex.mapTrace(traceKeyVals, tracePosition);
        }
      } catch (Exception ex) {
        getLogger().error(ex.toString(), ex);
      }
    }
  }

  @Override
  public void createInStore(final PostStack3d volume) throws IOException {
    super.createInStore(volume);

    String ndxFilePath = getFilePath() + ".ndx";
    String[] traceKeyNames = { TraceHeaderCatalog.INLINE_NO.getName(), TraceHeaderCatalog.XLINE_NO.getName() };
    int[] traceKeyByteLocs = new int[2];
    int[] traceKeyMinVals = new int[2];
    int[] traceKeyMaxVals = new int[2];
    int[] traceKeyIncVals = new int[2];
    traceKeyByteLocs[0] = _model.getInlineByteLoc();
    traceKeyByteLocs[1] = _model.getXlineByteLoc();
    SeismicSurvey3d survey = volume.getSurvey();
    traceKeyMinVals[0] = (int) Math.min(survey.getInlineStart(), survey.getInlineEnd());
    traceKeyMinVals[1] = (int) Math.min(survey.getXlineStart(), survey.getXlineEnd());
    traceKeyMaxVals[0] = (int) Math.max(survey.getInlineStart(), survey.getInlineEnd());
    traceKeyMaxVals[1] = (int) Math.max(survey.getXlineStart(), survey.getXlineEnd());
    traceKeyIncVals[0] = (int) Math.abs(survey.getInlineDelta());
    traceKeyIncVals[1] = (int) Math.abs(survey.getXlineDelta());
    traceKeyMinVals[0] = (int) Math.min(volume.getInlineStart(), volume.getInlineEnd());
    traceKeyMinVals[1] = (int) Math.min(volume.getXlineStart(), volume.getXlineEnd());
    traceKeyMaxVals[0] = (int) Math.max(volume.getInlineStart(), volume.getInlineEnd());
    traceKeyMaxVals[1] = (int) Math.max(volume.getXlineStart(), volume.getXlineEnd());
    traceKeyIncVals[0] = (int) Math.abs(volume.getInlineDelta());
    traceKeyIncVals[1] = (int) Math.abs(volume.getXlineDelta());
    SegyTraceIndexModel traceIndexModel = new SegyTraceIndexModel("", traceKeyNames, traceKeyByteLocs, traceKeyMinVals,
        traceKeyMaxVals, traceKeyIncVals);
    try {
      _traceIndex = new SegyTraceIndex(ndxFilePath, IndexType.POSTSTACK_3D, traceIndexModel);
    } catch (Exception ex) {
      throw new IOException(ex);
    }
  }

  /**
   * Reads the EBCDIC and binary headers for the SEG-Y file
   * on disk and updates the entity accordingly.
   * 
   * @param the entity mapped to the SEG-Y file.
   */
  @Override
  public void readFromStore(final PostStack3d ps3d) {
    Volume3dMapperModel model = (Volume3dMapperModel) _model;
    synchronized (getSynchronizeToken()) {
      try {
        getFileAccessor().read();

        // Update the PostStack3d entity.
        ps3d.setComment(getFileAccessor().getEbcdicHeader().toString());
        Domain domain = getFileAccessor().getDomain();
        ps3d.setZDomain(domain);
        ps3d.setZRangeAndDelta(getFileAccessor().getStartZ(), getFileAccessor().getEndZ(), getFileAccessor()
            .getDeltaZ());

        //TODO setting the z maximum possible range to match the z range just calculated
        //is this correct ?
        ps3d.setZMaxRangeAndDelta(getFileAccessor().getStartZ(), getFileAccessor().getEndZ(), getFileAccessor()
            .getDeltaZ());

        Timestamp lastModifiedDate = new Timestamp(new File(getFilePath()).lastModified());
        ps3d.setLastModifiedDate(lastModifiedDate);
        ps3d.setProjectName("");
        ps3d.setDataUnit(model.getDataUnit());
        ps3d.setTraceHeaderDefinition(getHeaderDefinition());

        // TODO: what to do about the logic below, because a trace index is not created when the file is written.
        if (_traceIndex != null) {
          ps3d.setInlineRangeAndDelta(_traceIndex.getTraceKeyMin(0), _traceIndex.getTraceKeyMax(0),
              _traceIndex.getTraceKeyInc(0));
          ps3d.setXlineRangeAndDelta(_traceIndex.getTraceKeyMin(1), _traceIndex.getTraceKeyMax(1),
              _traceIndex.getTraceKeyInc(1));
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
            double x0 = Unit.convert(model.getX0(), model.getUnitOfXY(), UNIT_PREFS.getHorizontalDistanceUnit());
            double y0 = Unit.convert(model.getY0(), model.getUnitOfXY(), UNIT_PREFS.getHorizontalDistanceUnit());
            double x1 = Unit.convert(model.getX1(), model.getUnitOfXY(), UNIT_PREFS.getHorizontalDistanceUnit());
            double y1 = Unit.convert(model.getY1(), model.getUnitOfXY(), UNIT_PREFS.getHorizontalDistanceUnit());
            double x2 = Unit.convert(model.getX2(), model.getUnitOfXY(), UNIT_PREFS.getHorizontalDistanceUnit());
            double y2 = Unit.convert(model.getY2(), model.getUnitOfXY(), UNIT_PREFS.getHorizontalDistanceUnit());
            double x3 = Unit.convert(model.getX3(), model.getUnitOfXY(), UNIT_PREFS.getHorizontalDistanceUnit());
            double y3 = Unit.convert(model.getY3(), model.getUnitOfXY(), UNIT_PREFS.getHorizontalDistanceUnit());
            Point3d[] points = new Point3d[4];
            points[0] = new Point3d(x0, y0, 0);
            points[1] = new Point3d(x1, y1, 0);
            points[2] = new Point3d(x2, y2, 0);
            points[3] = new Point3d(x3, y3, 0);
            CoordinateSystem cs = new CoordinateSystem("", getFileAccessor().getDomain());
            CornerPointsSeries cornerPoints = CornerPointsSeries.create(points, cs);
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
            SeismicSurvey3d seismicGeometry = new SeismicSurvey3d("Geometry " + model.getFileName(), inlineRange,
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
            SeismicSurvey3d seismicGeometry = new SeismicSurvey3d("Geometry " + model.getFileName(), inlineRange,
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
   * @param ps3d the PostStack3d volume.
   */
  @Override
  public void close() {
    super.close();
    checkTraceIndex();
  }

  /**
   * Checks if the trace index file needs to be created.
   */
  public void checkTraceIndex() {
    try {
      if (_traceIndex != null) {
        _traceIndex.close();
      }
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
          try {
            _traceIndex = new SegyTraceIndex(ndxFilePath);
          } catch (Exception ex) {
            // If an exception occurs, such as one caused by an incompatible format
            // of the index file, then delete the existing index file and rebuild.
            ndxFile.delete();
            needsIndexing = true;
          }
        }
      } else {
        needsIndexing = true;
      }
      if (needsIndexing) {
        _traceIndex = SegyUtil.getTraceIndex(_model, SegyTraceIndex.IndexType.POSTSTACK_3D);
      }
    } catch (Exception ex) {
      getLogger().error("Error accessing SEG-Y trace index file.", ex);
    }
  }

  /**
   * Gets the corner points of the SEG-Y geometry.
   * 
   * @param traceIndex the SEG-Y trace index.
   * @param xCoordByteLoc the x-coordinate byte location in the trace header.
   * @param yCoordByteLoc the y-coordinate byte location in the trace header.
   * @param convertToAppUnits <i>true</i> to convert the x,y coordinates to application units; otherwise <i>false</i>.
   * @return the corner points of the PostStack3d geometry.
   * @throws IOException
   */
  private CornerPointsSeries getCornerPoints(final SegyTraceIndex traceIndex, final int xCoordByteLoc,
      final int yCoordByteLoc, final boolean convertToAppUnits) throws IOException {
    // Check if the corner points have already been computed.
    // If not, compute them.
    ByteBuffer byteBuffer = ByteBuffer.allocate(4);
    RandomAccessFile randomAccessFile = new RandomAccessFile(getFilePath(), "r");
    FileChannel fileChannel = randomAccessFile.getChannel();
    int[] keys = new int[2];
    Point3d[] points = new Point3d[4];

    // Allocate inline,xline arrays for the corner points.
    int ilineStart = traceIndex.getTraceKeyMin(0);
    int ilineEnd = traceIndex.getTraceKeyMax(0);
    int ilineInc = traceIndex.getTraceKeyInc(0);
    int xlineStart = traceIndex.getTraceKeyMin(1);
    int xlineEnd = traceIndex.getTraceKeyMax(1);
    int xlineInc = traceIndex.getTraceKeyInc(1);
    int[] ilines = { ilineStart, ilineEnd, ilineEnd, ilineStart };
    int[] xlines = { xlineStart, xlineStart, xlineEnd, xlineEnd };
    int numInlines = 1 + (ilineEnd - ilineStart) / ilineInc;
    int numXlines = 1 + (xlineEnd - xlineStart) / xlineInc;

    // Loop thru each corner point.
    for (int i = 0; i < 4; i++) {
      // Lookup each corner point.
      keys[0] = ilines[i];
      keys[1] = xlines[i];
      long pos = traceIndex.getTracePosition(keys);

      // If any corner point does not exist in the trace index,
      // then the geometry cannot be determined, so throw an exception.
      if (pos == 0) {
        // Check inline distances.
        double inlineDistanceDxSum = 0;
        double inlineDistanceDySum = 0;
        int inlineDistanceCount = 0;
        long firstTracePos = 0;
        int firstTraceInline = -999;
        int firstTraceXline = -999;
        for (int ii = 0; ii < numInlines; ii++) {
          keys[0] = ilineStart + ii * ilineInc;
          int jj0 = -1;
          int jj1 = -1;
          long pos0 = 0;
          long pos1 = 0;
          for (int jj = 0; jj < numXlines; jj++) {
            keys[1] = xlineStart + jj * xlineInc;
            pos = traceIndex.getTracePosition(keys);
            if (pos != 0) {
              if (firstTracePos == 0) {
                firstTracePos = pos;
                firstTraceInline = keys[0];
                firstTraceXline = keys[1];
              }
              if (pos0 == 0) {
                pos0 = pos;
                jj0 = jj;
              }
              pos1 = pos;
              jj1 = jj;
            }
          }
          if (jj0 != -1 && jj1 > jj0) {
            Point3d pt0 = readXY(fileChannel, byteBuffer, pos0, xCoordByteLoc, yCoordByteLoc, convertToAppUnits,
                _model.getUnitOfXY());
            Point3d pt1 = readXY(fileChannel, byteBuffer, pos1, xCoordByteLoc, yCoordByteLoc, convertToAppUnits,
                _model.getUnitOfXY());
            double dx = (pt1.getX() - pt0.getX()) / (jj1 - jj0);
            double dy = (pt1.getY() - pt0.getY()) / (jj1 - jj0);
            inlineDistanceDxSum += dx;
            inlineDistanceDySum += dy;
            inlineDistanceCount++;
          }
        }
        // Check xline distances.
        double xlineDistanceDxSum = 0;
        double xlineDistanceDySum = 0;
        int xlineDistanceCount = 0;
        for (int jj = 0; jj < numXlines; jj++) {
          keys[1] = xlineStart + jj * xlineInc;
          int ii0 = -1;
          int ii1 = -1;
          long pos0 = 0;
          long pos1 = 0;
          for (int ii = 0; ii < numInlines; ii++) {
            keys[0] = ilineStart + ii * ilineInc;
            pos = traceIndex.getTracePosition(keys);
            if (pos != 0) {
              if (firstTracePos == 0) {
                firstTracePos = pos;
                firstTraceInline = keys[0];
                firstTraceXline = keys[1];
              }
              if (pos0 == 0) {
                pos0 = pos;
                ii0 = ii;
              }
              pos1 = pos;
              ii1 = ii;
            }
          }
          if (ii0 != -1 && ii1 > ii0) {
            Point3d pt0 = readXY(fileChannel, byteBuffer, pos0, xCoordByteLoc, yCoordByteLoc, convertToAppUnits,
                _model.getUnitOfXY());
            Point3d pt1 = readXY(fileChannel, byteBuffer, pos1, xCoordByteLoc, yCoordByteLoc, convertToAppUnits,
                _model.getUnitOfXY());
            double dx = (pt1.getX() - pt0.getX()) / (ii1 - ii0);
            double dy = (pt1.getY() - pt0.getY()) / (ii1 - ii0);
            xlineDistanceDxSum += dx;
            xlineDistanceDySum += dy;
            xlineDistanceCount++;
          }
        }
        if (inlineDistanceCount > 0 && xlineDistanceCount > 0) {
          double inlineDistanceDx = inlineDistanceDxSum / inlineDistanceCount;
          double inlineDistanceDy = inlineDistanceDySum / inlineDistanceCount;
          double xlineDistanceDx = xlineDistanceDxSum / xlineDistanceCount;
          double xlineDistanceDy = xlineDistanceDySum / xlineDistanceCount;
          Point3d ptFirstTrace = readXY(fileChannel, byteBuffer, firstTracePos, xCoordByteLoc, yCoordByteLoc,
              convertToAppUnits, _model.getUnitOfXY());
          int[] inlineOffsets = new int[4];
          int[] xlineOffsets = new int[4];
          inlineOffsets[0] = ilineStart - firstTraceInline;
          xlineOffsets[0] = xlineStart - firstTraceXline;
          inlineOffsets[1] = ilineEnd - firstTraceInline;
          xlineOffsets[1] = xlineStart - firstTraceXline;
          inlineOffsets[2] = ilineEnd - firstTraceInline;
          xlineOffsets[2] = xlineEnd - firstTraceXline;
          inlineOffsets[3] = ilineStart - firstTraceInline;
          xlineOffsets[3] = xlineEnd - firstTraceXline;
          for (int k = 0; k < 4; k++) {
            double dx = inlineOffsets[k] * inlineDistanceDx + xlineOffsets[k] * xlineDistanceDx;
            double dy = inlineOffsets[k] * inlineDistanceDy + xlineOffsets[k] * xlineDistanceDy;
            points[k] = new Point3d(ptFirstTrace.getX() + dx, ptFirstTrace.getY() + dy, 0);
          }
          randomAccessFile.close();
          return CornerPointsSeries.createDirect(points, new CoordinateSystem("", getFileAccessor().getDomain()));
        } else {
          randomAccessFile.close();
          throw new IOException("Could not establish survey from existing traces.");
        }
        //throw new IOException("One or more corner points missing.");
      }

      // Read the x,y scalar from the trace header.
      fileChannel.position(pos + 70);
      byteBuffer.position(0);
      fileChannel.read(byteBuffer);
      byteBuffer.position(0);
      short scalar = byteBuffer.getShort();

      // Read the x coordinate from the trace header.
      fileChannel.position(pos + xCoordByteLoc - 1);
      byteBuffer.position(0);
      fileChannel.read(byteBuffer);
      byteBuffer.position(0);
      double x = byteBuffer.getInt(0);

      // Read the y coordinate from the trace header.
      fileChannel.position(pos + yCoordByteLoc - 1);
      byteBuffer.position(0);
      fileChannel.read(byteBuffer);
      byteBuffer.position(0);
      double y = byteBuffer.getInt(0);

      // Scale the x,y coordinate accordingly.
      if (scalar > 0) {
        x *= scalar;
        y *= scalar;
      } else if (scalar < 0) {
        x /= -scalar;
        y /= -scalar;
      }

      // Convert x,y to application units, if necessary.
      if (convertToAppUnits) {
        try {
          x = Unit.convert(x, _model.getUnitOfXY(), UNIT_PREFS.getHorizontalDistanceUnit());
          y = Unit.convert(y, _model.getUnitOfXY(), UNIT_PREFS.getHorizontalDistanceUnit());
        } catch (Exception ex) {
          getLogger().error(ex.toString(), ex);
        }
      }
      points[i] = new Point3d(x, y, 0);
    }
    randomAccessFile.close();

    return CornerPointsSeries.createDirect(points, new CoordinateSystem("", getFileAccessor().getDomain()));
  }

  private static Point3d readXY(final FileChannel fileChannel, final ByteBuffer byteBuffer, final long pos,
      final int xCoordByteLoc, final int yCoordByteLoc, final boolean convertToAppUnits, final Unit xyUnitStorage) throws IOException {
    // Read the x,y scalar from the trace header.
    fileChannel.position(pos + 70);
    byteBuffer.position(0);
    fileChannel.read(byteBuffer);
    byteBuffer.position(0);
    short scalar = byteBuffer.getShort();

    // Read the x coordinate from the trace header.
    fileChannel.position(pos + xCoordByteLoc - 1);
    byteBuffer.position(0);
    fileChannel.read(byteBuffer);
    byteBuffer.position(0);
    double x = byteBuffer.getInt(0);

    // Read the y coordinate from the trace header.
    fileChannel.position(pos + yCoordByteLoc - 1);
    byteBuffer.position(0);
    fileChannel.read(byteBuffer);
    byteBuffer.position(0);
    double y = byteBuffer.getInt(0);

    // Scale the x,y coordinate accordingly.
    if (scalar > 0) {
      x *= scalar;
      y *= scalar;
    } else if (scalar < 0) {
      x /= -scalar;
      y /= -scalar;
    }

    // Convert x,y to application units, if necessary.
    if (convertToAppUnits) {
      try {
        Unit xyUnit = UnitPreferences.getInstance().getHorizontalDistanceUnit();
        x = Unit.convert(x, xyUnitStorage, xyUnit);
        y = Unit.convert(y, xyUnitStorage, xyUnit);
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }
    return new Point3d(x, y, 0);
  }

  /**
   * Gets a slice of data from the SEG-Y file.
   * 
   * @param ps3d the poststack3d mapped to the SEG-Y file.
   * @param z the slice z value.
   * @param inlineStart the starting inline.
   * @param inlineEnd the ending inline.
   * @param xlineStart the starting xline.
   * @param xlineEnd the ending xline.
   * @param order the order of the data to return.
   */
  public float[] getSlice(final PostStack3d ps3d, final float z, final float inlineStart, final float inlineEnd,
      final float xlineStart, final float xlineEnd, final SliceBufferOrder order) {
    return getSlice(ps3d, z, inlineStart, inlineEnd, xlineStart, xlineEnd, order, 0);
  }

  /**
   * Gets a slice of data from the SEG-Y file.
   * 
   * @param ps3d the poststack3d mapped to the SEG-Y file.
   * @param z the slice z value.
   * @param inlineStart the starting inline.
   * @param inlineEnd the ending inline.
   * @param xlineStart the starting xline.
   * @param xlineEnd the ending xline.
   * @param order the order of the data to return.
   */
  public float[] getSlice(final PostStack3d ps3d, final float z, final float inlineStart, final float inlineEnd,
      final float xlineStart, final float xlineEnd, final SliceBufferOrder order, final float missingValue) {
    StorageOrder storageOrder = getStorageOrder(ps3d);
    synchronized (getSynchronizeToken()) {
      getFileAccessor().openForRead();
      int numInlines = 1 + Math.round((inlineEnd - inlineStart) / ps3d.getInlineDelta());
      int numXlines = 1 + Math.round((xlineEnd - xlineStart) / ps3d.getXlineDelta());
      int numTraces = numInlines * numXlines;
      float[] sliceData = new float[numTraces];
      for (int i = 0; i < numTraces; i++) {
        sliceData[i] = missingValue;
      }
      Unit zUnits = ps3d.getZUnit();
      try {
        // Compute the requested start/end z-indices (in storage units).
        float startz = Unit.convert(z, zUnits, _model.getUnitOfZ());
        int iz = Math.round(startz / _model.getSampleRate());

        // Compute the volume start/end z-indices (in storage units).
        int jz0 = Math.round(Unit.convert(ps3d.getZStart(), ps3d.getZUnit(), _model.getUnitOfZ())
            / _model.getSampleRate());
        int jz1 = Math.round(Unit.convert(ps3d.getZEnd(), ps3d.getZUnit(), _model.getUnitOfZ())
            / _model.getSampleRate());
        ByteBuffer buffer = ByteBuffer.allocate(ps3d.getNumSamplesPerTrace() * 4);
        int[] keys = new int[2];
        float[] traceData = new float[ps3d.getNumSamplesPerTrace()];
        float inline;
        float xline;

        if (storageOrder.equals(StorageOrder.INLINE_XLINE_Z)) {
          for (int i = 0; i < numInlines; i++) {
            inline = inlineStart + i * ps3d.getInlineDelta();
            for (int j = 0; j < numXlines; j++) {
              xline = xlineStart + j * ps3d.getXlineDelta();
              int iln = Math.round(inline);
              int xln = Math.round(xline);
              keys[0] = iln;
              keys[1] = xln;
              long tracePos = _traceIndex.getTracePosition(keys);
              int numTracesToRead = tracePos == 0 ? 0 : 1;
              for (int k = 0; k < numTracesToRead; k++) {
                // Skip past the trace header.
                getFileAccessor().setFilePosition(tracePos + 240);
                buffer.position(0);
                getFileAccessor().readByteBuffer(buffer);
                buffer.position(0);
                getFileAccessor().getFloatsFromBytes(ps3d.getNumSamplesPerTrace(), buffer.array(), traceData);
                if (iz >= jz0 && iz <= jz1) {
                  if (order.equals(SliceBufferOrder.INLINE_XLINE)) {
                    sliceData[i * numXlines + j] = traceData[iz - jz0];
                  } else if (order.equals(SliceBufferOrder.XLINE_INLINE)) {
                    sliceData[j * numInlines + i] = traceData[iz - jz0];
                  }
                } else {
                  String msg = "Requested z coordinate outside of extents (" + z + " " + zUnits + ").";
                  getLogger().error(msg);
                  throw new Exception(msg);
                }
              }
            }
          }
        } else if (storageOrder.equals(StorageOrder.XLINE_INLINE_Z)) {
          for (int i = 0; i < numXlines; i++) {
            xline = xlineStart + i * ps3d.getXlineDelta();
            for (int j = 0; j < numInlines; j++) {
              inline = inlineStart + j * ps3d.getInlineDelta();
              int iln = Math.round(inline);
              int xln = Math.round(xline);
              keys[0] = iln;
              keys[1] = xln;
              long tracePos = _traceIndex.getTracePosition(keys);
              int numTracesToRead = tracePos == 0 ? 0 : 1;
              for (int k = 0; k < numTracesToRead; k++) {
                // Skip past the trace header.
                getFileAccessor().setFilePosition(tracePos + 240);
                buffer.position(0);
                getFileAccessor().readByteBuffer(buffer);
                getFileAccessor().getFloatsFromBytes(ps3d.getNumSamplesPerTrace(), buffer.array(), traceData);
                if (iz >= jz0 && iz <= jz1) {
                  if (order.equals(SliceBufferOrder.INLINE_XLINE)) {
                    sliceData[j * numXlines + i] = traceData[iz - jz0];
                  } else if (order.equals(SliceBufferOrder.XLINE_INLINE)) {
                    sliceData[i * numInlines + j] = traceData[iz - jz0];
                  }
                } else {
                  String msg = "Requested z coordinates outside of extents (" + z + " " + zUnits + ").";
                  getLogger().error(msg);
                  throw new Exception(msg);
                }
              }
            }
          }
        }
      } catch (Exception ex) {
        getLogger().error(ex.toString(), ex);
      }
      return sliceData;
    }
  }

  /**
   * Gets an array of data samples from the SEG-Y file.
   * 
   * @param ps3d the poststack3d mapped to the SEG-Y file.
   * @param inlines the array of inlines.
   * @param xlines the array of xlines.
   * @param zs the array of z values.
   */
  public float[] getSamples(final PostStack3d ps3d, final float[] inlines, final float[] xlines, final float[] zs) {
    throw new UnsupportedOperationException("Reading of arbitrary samples not yet implemented for SEG-Y.");
  }

  /**
   * Puts an array of data samples to the SEG-Y file. Not currently implemented for SEG-Y.
   */
  @SuppressWarnings("unused")
  public void putSamples(final PostStack3d ps3d, final float[] inline, final float[] xline, final float[] z,
      final float[] samples) {
    throw new UnsupportedOperationException("Writing of arbitrary samples not yet implemented for SEG-Y.");
  }

  /**
   * Puts a slice of data to the SEG-Y file. Not currently implemented for SEG-Y.
   */
  @SuppressWarnings("unused")
  public void putSlice(final PostStack3d ps3d, final float z, final float inlineStart, final float inlineEnd,
      final float xlineStart, final float xlineEnd, final SliceBufferOrder order, final float[] samples) {
    throw new UnsupportedOperationException("Writing of slices not yet implemented for SEG-Y.");
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
  //      checkTraceIndex();
  //    }
  //  }

  public StorageOrder getStorageOrder(final PostStack3d ps3d) {
    // Check if the storage order has been determined.
    if (_storageOrder == null) {
      // If not look it up from the mapper model.
      _storageOrder = StorageOrder.lookupByName(_model.getStorageOrder());
    }

    // If it is a known and valid value, then return it.
    if (_storageOrder != null && !_storageOrder.equals(StorageOrder.AUTO_CALCULATED)) {
      return _storageOrder;
    }

    // Otherwise, try to determine it be reading parts of the volume.
    _storageOrder = PostStack3dStorageOrderTask.getStorageOrder(ps3d);
    return _storageOrder;
  }

  public void setStorageOrder(final StorageOrder storageOrder) {
    _storageOrder = storageOrder;
  }

  /**
   * Gets the storage organization of the SEG-Y file.
   * For SEG-Y, the storage organization is always <i>TRACE</i>.
   */
  public StorageOrganization getStorageOrganization() {
    return StorageOrganization.TRACE;
  }

  @Override
  public void deleteFromStore(final PostStack3d ps3d) throws IOException {
    // Delete the trace index file, if it exists.
    if (_traceIndex != null) {
      _traceIndex.deleteFromStore();
      _traceIndex = null;
    }
    // Then delete the main data file.
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
   * Returns the header definition for a SEG-Y PostStack3d volume.
   */
  private synchronized HeaderDefinition getHeaderDefinition() {
    if (_headerDef == null) {
      _headerDef = SegyTraceHeader.POSTSTACK3D_HEADER_DEF;
    }
    return _headerDef;
  }

  public void setStorageOrganizationAndFormat(final StorageOrganization storageOrganization,
      final StorageFormat storageFormat, final BrickType brickType, final float fidelity) {
    // NOTE: The brick type and fidelity have no meaning for SEG-Y volumes.
    switch (storageOrganization) {
      case TRACE:
        switch (storageFormat) {
          case FLOAT_32:
            _model.setSampleFormat(SegyBytes.SAMPLE_FORMAT_FLOAT_4BYTE_IEEE);
            break;
          case INTEGER_16:
            _model.setSampleFormat(SegyBytes.SAMPLE_FORMAT_FIXED_2BYTE);
            break;
          case INTEGER_08:
            _model.setSampleFormat(SegyBytes.SAMPLE_FORMAT_FIXED_1BYTE);
            break;
          default:
            throw new IllegalArgumentException("Invalid storage format for SEG-Y volume: " + storageFormat);
        }
        break;
      default:
        throw new IllegalArgumentException("Invalid storage organization for SEG-Y volume: " + storageOrganization);
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
            return storageFormat + " format not supported for SEG-Y volumes.";
        }
        break;
      case BRICK:
      case COMPRESSED:
        return storageOrganization + " volumes not supported in SEG-Y.";
    }
    return "Invalid storage organization: " + storageOrganization;
  }

  @Override
  public String getDatastoreEntryDescription() {
    return "SEG-Y PostStack3d";
  }

  public String getDatastore() {
    return "SEG-Y";
  }

}
