/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import java.util.ArrayList;
import java.util.List;

import org.geocraft.core.model.datatypes.HeaderDefinition;
import org.geocraft.core.model.datatypes.HeaderEntry;


/**
 * Defines a standard SEG-Y binary header definition.
 */
public class SegyBinaryHeaderDefinition extends HeaderDefinition {

  /**
   * Constructs a standard SEG-Y binary header definition.
   */
  public SegyBinaryHeaderDefinition() {
    super(getBinaryHeaderEntries());
  }

  private static HeaderEntry[] getBinaryHeaderEntries() {
    List<HeaderEntry> list = new ArrayList<HeaderEntry>();
    HeaderEntry[] headerEntriesStd = SegyBinaryHeaderCatalog.getStandardList();
    for (HeaderEntry headerEntry : headerEntriesStd) {
      list.add(headerEntry);
    }
    list.add(SegyBinaryHeaderCatalog.SEGY_FORMAT_REVISION_NUMBER);
    list.add(SegyBinaryHeaderCatalog.FIXED_TRACE_LENGTH_FLAG);
    list.add(SegyBinaryHeaderCatalog.NUMBER_OF_EXTENDED_HEADERS);
    return list.toArray(new HeaderEntry[0]);
  }
}
