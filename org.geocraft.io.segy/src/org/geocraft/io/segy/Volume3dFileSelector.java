/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import org.geocraft.io.segy.SegyTraceIndex.IndexType;


/**
 * The class for handling the selection of SEG-Y 3D volume files on disk.
 */
public class Volume3dFileSelector extends VolumeFileSelector {

  public Volume3dFileSelector() {
    super(IndexType.POSTSTACK_3D);
  }

}
