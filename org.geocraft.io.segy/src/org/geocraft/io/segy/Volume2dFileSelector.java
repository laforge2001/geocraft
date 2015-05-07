package org.geocraft.io.segy;


import org.geocraft.io.segy.SegyTraceIndex.IndexType;


/**
 * The class for handling the selection of SEG-Y 2D volume files on disk.
 */
public class Volume2dFileSelector extends VolumeFileSelector {

  public Volume2dFileSelector() {
    super(IndexType.POSTSTACK_2D);
  }

}
