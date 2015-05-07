/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.specification;


import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.repository.specification.AbstractSpecification;


public class SeismicDatasetUnitsSpecification extends AbstractSpecification {

  String _sampleUnits;

  public SeismicDatasetUnitsSpecification(final String sampleUnits) {
    _sampleUnits = sampleUnits;
  }

  /**
   * Check if data file is derived from SeismicDataset and if the units match
   * the expected units.
   */
  public boolean isSatisfiedBy(final Object obj) {
    if (obj instanceof SeismicDataset) {
      SeismicDataset seismic = (SeismicDataset) obj;
      // check if the units match
      if (seismic.getDataUnit().toString().equals(_sampleUnits)) {
        return true;
      }
    }
    return false;
  }
}
