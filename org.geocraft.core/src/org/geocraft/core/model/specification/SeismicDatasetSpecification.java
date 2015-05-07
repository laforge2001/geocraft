/*
 * Copyright (C) ConocoPhillips 2008-2009 All Rights Reserved. 
 */
package org.geocraft.core.model.specification;


import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.repository.specification.AbstractSpecification;


public class SeismicDatasetSpecification extends AbstractSpecification {

  String _sampleDomain;

  public SeismicDatasetSpecification(final String sampleDomain) {
    _sampleDomain = sampleDomain;
  }

  /**
   * Check if data file is derived from SeismicDataset and if the domain or domain of
   * the units match the expected domain.
   */
  // TODO this is not cool. Sample domain should be typed and not confuse domain and units. 
  public boolean isSatisfiedBy(final Object obj) {
    if (obj instanceof SeismicDataset) {
      SeismicDataset seismic = (SeismicDataset) obj;
      // check if the domain of the units match
      if (seismic.getDataUnit().getDomain().equals(_sampleDomain)) {
        return true;
      } else if (seismic.getZDomain().toString().equals(_sampleDomain)) {
        return true;
      }
    }
    return false;
  }
}
