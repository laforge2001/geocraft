/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.algorithm.thread;


import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.seismic.PostStack3d;


/**
 * This class defines a data sink for trace data, writing them to a PostStack3d volume.
 */
public class PostStack3dDataSink implements IDataSink<TraceData> {

  /** The PostStack3d volume for writing traces. */
  private PostStack3d _outputVolume;

  /**
   * Constructs a PostStack3d data sink.
   * 
   * @param outputVolume the PostStack3d volume for writing traces.
   */
  public PostStack3dDataSink(PostStack3d outputVolume) {
    _outputVolume = outputVolume;
  }

  public void put(TraceData outputTraces) {
    _outputVolume.putTraces(outputTraces);
  }

  public void close() {
    synchronized (_outputVolume) {
      _outputVolume.setDirty(false);
      _outputVolume.close();
    }
  }
}
