/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;




public interface SegyTraceIndexerListener {

  void tracesIndexed(VolumeMapperModel mapperModel, SegyTraceIndexModel traceIndexModel);
}
