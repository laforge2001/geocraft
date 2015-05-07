/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geocraft.core.model.datatypes.HeaderDefinition;
import org.geocraft.core.model.datatypes.HeaderEntry;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.datatypes.HeaderEntry.Format;


public class SegyTraceIterator implements Iterator {

  private final SegyFileAccessor _accessor;

  private HeaderDefinition _headerDef;

  public SegyTraceIterator(VolumeMapperModel model) {
    _accessor = new SegyFileAccessor(model);
    try {
      _accessor.read();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    _accessor.openForRead();

    List<HeaderEntry> list = new ArrayList<HeaderEntry>();
    for (HeaderEntry headerEntry : SegyTraceHeaderCatalog.STANDARD_ENTRIES) {
      list.add(headerEntry);
    }
    for (int byteLoc = 181; byteLoc <= 237; byteLoc += 4) {
      list.add(new HeaderEntry("UNASSIGNED_TRACE_HEADER_" + byteLoc, "Unassigned Trace Header @" + byteLoc,
          "The unassigned trace header at byte #" + byteLoc, Format.INTEGER, 1));
      //byteLoc-1;
    }
    _headerDef = new HeaderDefinition(list.toArray(new HeaderEntry[0]));
  }

  public boolean hasNext() {
    long diff = 0;
    try {
      diff = _accessor.getFileSize() - _accessor.getFilePosition();
    } catch (IOException e) {
      return false;
    }
    return diff >= 240;
  }

  public Trace next() {
    Trace.Status status = Trace.Status.Missing;
    SegyTraceHeader header;
    double x = 0;
    double y = 0;
    float startZ = 0;
    float deltaZ = 0;
    float[] data = new float[0];
    try {
      if (_accessor.getFilePosition() < 3600) {
        _accessor.setFilePosition(3600);
      }
      header = new SegyTraceHeader(_headerDef);
      header.getBuffer().position(0);
      _accessor.readByteBuffer(header.getBuffer());
      header.updateHeaderFromBuffer();
      for (int byteLoc = 181; byteLoc <= 237; byteLoc += 4) {
        String key = "UNASSIGNED_TRACE_HEADER_" + byteLoc;
        int value = header.getFromBufferAsInt(byteLoc - 1);
        header.putInteger(key, value);
      }
      x = header.getInteger(SegyTraceHeaderCatalog.SOURCE_COORDINATE_X);
      y = header.getInteger(SegyTraceHeaderCatalog.SOURCE_COORDINATE_Y);
      int xyScalar = header.getShort(SegyTraceHeaderCatalog.COORDINATE_SCALAR);
      // x = header.getInteger(SegyTraceHeaderCatalog.SOURCE_COORDINATE_X);
      // y = header.getInteger(SegyTraceHeaderCatalog.SOURCE_COORDINATE_Y);
      // int xyScalar = header.getShort(SegyTraceHeaderCatalog.COORDINATE_SCALAR);
      if (xyScalar > 0) {
        x *= xyScalar;
        y *= xyScalar;
      } else if (xyScalar < 0) {
        x /= xyScalar;
        y /= xyScalar;
      }
      startZ = header.getShort(SegyTraceHeaderCatalog.DELAY_RECORDING_TIME);
      deltaZ = header.getShort(SegyTraceHeaderCatalog.SAMPLE_INTERVAL);
      // startZ = header.getShort(SegyTraceHeaderCatalog.DELAY_RECORDING_TIME);
      // deltaZ = header.getShort(SegyTraceHeaderCatalog.SAMPLE_INTERVAL);
      deltaZ /= 1000;
      int numSamples = header.getShort(SegyTraceHeaderCatalog.NUM_SAMPLES);
      // int numSamples = header.getShort(SegyTraceHeaderCatalog.NUM_SAMPLES);
      ByteBuffer dataBuffer = ByteBuffer.allocate(numSamples * 4);
      dataBuffer.position(0);
      _accessor.readByteBuffer(dataBuffer);
      data = new float[numSamples];
      _accessor.getFloatsFromBytes(numSamples, dataBuffer.array(), data);
      status = Trace.Status.Live;
    } catch (IOException ex) {
      throw new RuntimeException(ex.toString());
    }
    Trace trace = new Trace(startZ, deltaZ, Unit.MILLISECONDS, x, y, data, status);
    trace.setHeader(header);
    return trace;
  }

  public void remove() {
    throw new UnsupportedOperationException("Cannot remove traces from a SEG-Y iterator.");
  }

  public SegyEbcdicHeader getEbcdicHeader() {
    return _accessor.getEbcdicHeader();
  }

  public SegyBinaryHeader getBinaryHeader() {
    return _accessor.getBinaryHeader();
  }

  public float getCompletion() {
    float completion = 0;
    try {
      completion = 100f * _accessor.getFilePosition() / _accessor.getFileSize();
    } catch (IOException e) {
      return 50;
    }
    return completion;
  }
}
