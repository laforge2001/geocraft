package org.geocraft.io.segy;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Timestamp;

import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.HeaderDefinition;
import org.geocraft.core.model.datatypes.IntRange;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.TraceHeaderCatalog;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.IPostStack2dMapper;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.SeismicLine2d;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.io.segy.SegyTraceIndex.IndexType;


public class PostStack2dLineMapper extends SegyMapper<PostStack2dLine> implements IPostStack2dMapper {

  /** A reference to the unit preferences. */
  private static final UnitPreferences UNIT_PREFS = UnitPreferences.getInstance();

  /** The SEG-Y trace index file. */
  private SegyTraceIndex _traceIndex;

  public PostStack2dLineMapper(Volume2dMapperModel mapperModel) {
    this(mapperModel, true);
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
  public PostStack2dLineMapper(final VolumeMapperModel model, final boolean createIndex) {
    super(model);
    getHeaderDefinition();

    // Generate the trace index file, if necessary.
    if (createIndex) {
      checkTraceIndex();
    }
  }

  public SegyTraceIndex getTraceIndex() {
    return _traceIndex;
  }

  @Override
  public IMapper factory(MapperModel mapperModel) {
    // Use false as the 2nd argument to prevent trying to index, since this factory
    // method is used trying to create new volumes, which don't yet exist.
    return new PostStack2dLineMapper((Volume2dMapperModel) mapperModel);
  }

  @Override
  public void createInStore(PostStack2dLine volume) throws IOException {
    super.createInStore(volume);

    String ndxFilePath = getFilePath() + ".ndx";
    String[] traceKeyNames = { TraceHeaderCatalog.INLINE_NO.getName(), TraceHeaderCatalog.CDP_NO.getName() };
    int[] traceKeyByteLocs = new int[2];
    int[] traceKeyMinVals = new int[2];
    int[] traceKeyMaxVals = new int[2];
    int[] traceKeyIncVals = new int[2];
    traceKeyByteLocs[0] = _model.getInlineByteLoc();
    traceKeyByteLocs[1] = _model.getXlineByteLoc();
    SeismicLine2d seismicLine = volume.getSeismicLine();
    traceKeyMinVals[0] = seismicLine.getNumber();
    traceKeyMinVals[1] = (int) Math.min(seismicLine.getCDPStart(), seismicLine.getCDPEnd());
    traceKeyMaxVals[0] = seismicLine.getNumber();
    traceKeyMaxVals[1] = (int) Math.max(seismicLine.getCDPStart(), seismicLine.getCDPEnd());
    traceKeyIncVals[0] = 1;
    traceKeyIncVals[1] = (int) Math.abs(seismicLine.getCDPDelta());
    SegyTraceIndexModel traceIndexModel = new SegyTraceIndexModel("", traceKeyNames, traceKeyByteLocs, traceKeyMinVals,
        traceKeyMaxVals, traceKeyIncVals);
    try {
      _traceIndex = new SegyTraceIndex(ndxFilePath, IndexType.POSTSTACK_3D, traceIndexModel);
    } catch (Exception ex) {
      throw new IOException(ex);
    }
  }

  @Override
  protected void readFromStore(PostStack2dLine seismicDataset) throws IOException {
    Volume2dMapperModel model = (Volume2dMapperModel) _model;
    synchronized (getSynchronizeToken()) {
      try {
        getFileAccessor().read();

        // Update the PostStack3d entity.
        seismicDataset.setComment(getFileAccessor().getEbcdicHeader().toString());
        Domain domain = getFileAccessor().getDomain();
        seismicDataset.setZDomain(domain);
        seismicDataset.setZRangeAndDelta(getFileAccessor().getStartZ(), getFileAccessor().getEndZ(), getFileAccessor()
            .getDeltaZ());

        //TODO setting the z maximum possible range to match the z range just calculated
        //is this correct ?
        seismicDataset.setZMaxRangeAndDelta(getFileAccessor().getStartZ(), getFileAccessor().getEndZ(),
            getFileAccessor().getDeltaZ());

        Timestamp lastModifiedDate = new Timestamp(new File(getFilePath()).lastModified());
        seismicDataset.setLastModifiedDate(lastModifiedDate);
        seismicDataset.setProjectName("");
        seismicDataset.setDataUnit(model.getDataUnit());
        seismicDataset.setTraceHeaderDefinition(getHeaderDefinition());

        // TODO: what to do about the logic below, because a trace index is not created when the file is written.
        if (_traceIndex != null) {
          seismicDataset.setCdpRange(_traceIndex.getTraceKeyMin(0), _traceIndex.getTraceKeyMax(0),
              _traceIndex.getTraceKeyInc(0));
        }
        seismicDataset.setElevationDatum(0);

        // TODO Bill - may not need to create a new one every time?
        if (_traceIndex != null) {
          {
            //            // Get x,y corner points from the trace index, converting from model units to app units.
            //            CoordinateSeries xyPoints = getCoordinateSeries(_traceIndex, model.getXcoordByteLoc(),
            //                model.getYcoordByteLoc(), true);
            //            int lineNo = 1;
            //            int cdpStart = _traceIndex.getTraceKeyMin(0);
            //            int cdpEnd = _traceIndex.getTraceKeyMax(0);
            //            int cdpDelta = _traceIndex.getTraceKeyInc(0);
            //            CoordinateSystem cs = new CoordinateSystem("", getFileAccessor().getDomain());
            //            PolygonType direction = PolygonUtil.getDirection(xyPoints);
            //            SeismicLine2d[] seismicLines = new SeismicLine2d[1];
            //            SeismicSurvey2d seismicSurvey = new SeismicSurvey2d("Geometry" + model.getFileName(), seismicLines);
            //
            //            // Create a survey only if one is not already associated with the volume.
            //            if (seismicDataset.getSurvey() == null) {
            //              seismicDataset.setLastModifiedDate(lastModifiedDate);
            //              seismicDataset.setDirty(false);
            //            }
            //
            //            // Update the model with the inline,xline start/end/delta values.
            //            model.setCdpStart(cdpStart);
            //            model.setCdpEnd(cdpEnd);
            //            model.setCdpDelta(cdpDelta);
          }
        }
      } catch (Exception e) {
        getLogger().error(e.toString());
      }
    }
  }

  private CoordinateSeries getCoordinateSeries(final SegyTraceIndex traceIndex, final int xCoordByteLoc,
      final int yCoordByteLoc, final boolean convertToAppUnits) throws IOException {
    return getCoordinateSeries(traceIndex, xCoordByteLoc, yCoordByteLoc, convertToAppUnits, getFilePath(),
        _model.getUnitOfXY(), getFileAccessor().getDomain());
  }

  public static CoordinateSeries getCoordinateSeries(final SegyTraceIndex traceIndex, final int xCoordByteLoc,
      final int yCoordByteLoc, final boolean convertToAppUnits, final String filePath, Unit xyUnit, Domain zDomain) throws IOException {
    // Check if the corner points have already been computed.
    // If not, compute them.
    ByteBuffer byteBuffer = ByteBuffer.allocate(4);
    RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "r");
    FileChannel fileChannel = randomAccessFile.getChannel();
    int[] keys = new int[1];

    // Allocate inline,xline arrays for the corner points.
    int lineNo = 0;
    int cdpStart = traceIndex.getTraceKeyMin(0);
    int cdpEnd = traceIndex.getTraceKeyMax(0);
    int cdpInc = traceIndex.getTraceKeyInc(0);
    IntRange cdpRange = new IntRange(cdpStart, cdpEnd, cdpInc);
    int numCdps = cdpRange.getNumSteps();
    Point3d[] points = new Point3d[numCdps];
    System.out.println(" NUM CDPS=" + numCdps);

    // Loop thru each corner point.
    for (int i = 0; i < numCdps; i++) {
      // Lookup each corner point.
      keys[0] = cdpRange.getValue(i);
      long pos = traceIndex.getTracePosition(keys);

      // If any corner point does not exist in the trace index,
      // then the geometry cannot be determined, so throw an exception.
      if (pos == 0) {
        randomAccessFile.close();
        throw new IOException("One or more points missing.");
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
          x = Unit.convert(x, xyUnit, UNIT_PREFS.getHorizontalDistanceUnit());
          y = Unit.convert(y, xyUnit, UNIT_PREFS.getHorizontalDistanceUnit());
        } catch (Exception ex) {
          ServiceProvider.getLoggingService().getLogger(PostStack2dLineMapper.class).error(ex.toString(), ex);
        }
      }
      points[i] = new Point3d(x, y, 0);
    }
    randomAccessFile.close();

    return CoordinateSeries.createDirect(points, new CoordinateSystem("", zDomain));
  }

  @Override
  protected void deleteFromStore(PostStack2dLine entity) throws IOException {
    // Delete the trace index file, if it exists.
    if (_traceIndex != null) {
      _traceIndex.deleteFromStore();
      _traceIndex = null;
    }
    // Then delete the main data file.
    super.deleteFromStore();
  }

  @Override
  public String getDatastore() {
    return "SEG-Y";
  }

  @Override
  public String getDatastoreEntryDescription() {
    return "SEG-Y PostStack2d";
  }

  @Override
  public TraceData getTraces(PostStack2dLine ps2d, float[] cdps, float zStart, float zEnd) {
    // Synchronize to prevent other access.
    synchronized (getSynchronizeToken()) {
      // Open the file accessor.
      getFileAccessor().openForRead();
      int numTraces = cdps.length;
      int numSamples = 0;
      CoordinateSeries coords = null;
      float deltaZ = ps2d.getZDelta();
      Unit zUnits = ps2d.getZUnit();

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
        int jz0 = Math.round(Unit.convert(ps2d.getZStart(), ps2d.getZUnit(), _model.getUnitOfZ())
            / _model.getSampleRate());
        int jz1 = Math.round(Unit.convert(ps2d.getZEnd(), ps2d.getZUnit(), _model.getUnitOfZ())
            / _model.getSampleRate());
        numSamples = iz1 - iz0 + 1;
        ByteBuffer buffer = ByteBuffer.allocate(ps2d.getNumSamplesPerTrace() * 4);
        int[] keys = new int[1];
        float[] traceData = new float[ps2d.getNumSamplesPerTrace()];
        coords = ps2d.getSeismicLine().transformCDPsToXYs(cdps);

        // Loop over the required number of traces.
        for (int i = 0; i < numTraces; i++) {

          // Create the header for this trace.
          traceHeaders[i] = new SegyTraceHeader(_headerDef);
          traceHeaders[i].getBuffer().position(0);
          int cdp = Math.round(cdps[i]);
          keys[0] = cdp;
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
            getFileAccessor().getFloatsFromBytes(ps2d.getNumSamplesPerTrace(), buffer.array(), traceData);
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
        traceHeaders[i].putInteger(TraceHeaderCatalog.INLINE_NO, ps2d.getLineNumber());
        traceHeaders[i].putInteger(TraceHeaderCatalog.CDP_NO, Math.round(cdps[i]));
        traceHeaders[i].putFloat(TraceHeaderCatalog.SHOTPOINT_NO, cdps[i]);

        // Override the x,y values with those calculated from the seismic geometry.
        traceHeaders[i].putDouble(TraceHeaderCatalog.X, coords.getX(i));
        traceHeaders[i].putDouble(TraceHeaderCatalog.Y, coords.getY(i));
        traces[i].setHeader(traceHeaders[i]);
      }
      return new TraceData(traces);
    }
  }

  @Override
  public void putTraces(PostStack2dLine ps2d, TraceData traceData) {
    Volume2dMapperModel model = (Volume2dMapperModel) _model;

    // Synchronize to prevent other access.
    synchronized (getSynchronizeToken()) {
      int numTraces = traceData.getNumTraces();
      float[] cdps = new float[numTraces];
      Trace[] traces = traceData.getTraces();
      for (int i = 0; i < numTraces; i++) {
        if (traces[i].isMissing()) {
          cdps[i] = 0;
        } else {
          cdps[i] = traces[i].getHeader().getInteger(TraceHeaderCatalog.CDP_NO);
        }
      }
      float zStart = traceData.getStartZ();
      float zEnd = traceData.getEndZ();
      getFileAccessor().openForWrite();
      Unit zUnits = ps2d.getZUnit();
      try {
        // Convert the z-start and z-end values of the trace data to the
        // z unit of measurement for storage.
        zStart = Unit.convert(zStart, zUnits, model.getUnitOfZ());
        zEnd = Unit.convert(zEnd, zUnits, model.getUnitOfZ());

        // Convert the z-start and z-delta values of the volume to
        // the z unit of measurement for storage.
        float zStartVolume = Unit.convert(ps2d.getZStart(), zUnits, model.getUnitOfZ());
        float zDelta = Unit.convert(ps2d.getZDelta(), zUnits, model.getUnitOfZ());

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
        int numSamplesInVolume = ps2d.getNumSamplesPerTrace();

        // Allocate a byte buffer of sufficient size to hold the number of
        // samples for a single trace.
        ByteBuffer buffer = ByteBuffer.allocate(numSamplesInVolume * numBytesPerSample);
        int[] keys = new int[2];
        float[] data = new float[numSamplesInVolume];
        Unit xyUnitsSEGY = model.getUnitOfXY();
        Unit xyUnits = UNIT_PREFS.getHorizontalDistanceUnit();
        getFileAccessor().setFilePositionEOF();
        SegyTraceHeader segyTraceHeader = new SegyTraceHeader(_headerDef);
        Point3d[] points = ps2d.getSeismicLine().transformCDPsToXYs(cdps).getPointsDirect();

        // Loop over each trace being written.
        for (int i = 0; i < numTraces; i++) {
          Trace trace = traces[i];

          // Check the trace status. If 'Missing', do not write.
          if (trace.getStatus().equals(Trace.Status.Missing)) {
            continue;
          }
          int cdp = Math.round(cdps[i]);
          keys[0] = cdp;
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
          hdrBuffer.putInt(model.getCdpByteLoc() - 1, cdp);

          // Update the trace header with the x,y coordinates.
          hdrBuffer.putInt(model.getXcoordByteLoc() - 1, x);
          hdrBuffer.putInt(model.getYcoordByteLoc() - 1, y);

          int[] traceKeyVals = { cdp };
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
  public float[] getSamples(PostStack2dLine ps2d, float[] cdps, float[] z) {
    throw new UnsupportedOperationException("Reading of arbitrary samples not yet implemented for SEG-Y.");
  }

  @Override
  public void putSamples(PostStack2dLine ps2d, float[] cdp, float[] z, Unit zUnit, float[] samples) {
    throw new UnsupportedOperationException("Writing of arbitrary samples not yet implemented for SEG-Y.");
  }

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
   * Returns the header definition for a SEG-Y PostStack3d volume.
   */
  private synchronized HeaderDefinition getHeaderDefinition() {
    if (_headerDef == null) {
      _headerDef = SegyTraceHeader.POSTSTACK2D_HEADER_DEF;
    }
    return _headerDef;
  }

  public static Point3d readXY(final FileChannel fileChannel, final ByteBuffer byteBuffer, final long pos,
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
}
