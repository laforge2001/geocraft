/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.preferences;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class GeocraftPreferenceServiceFactory {

  private static Map<String, GeocraftPreferenceService> _services = Collections.synchronizedMap(new HashMap<String, GeocraftPreferenceService>());

  private GeocraftPreferenceServiceFactory() {
    //Purposely left blank
  }

  public static GeocraftPreferenceService getInstance(final String bundleId) {
    GeocraftPreferenceService retrieveMe = _services.get(bundleId);
    if (retrieveMe == null) {
      retrieveMe = new GeocraftPreferenceService(bundleId);
      _services.put(bundleId, retrieveMe);
    }
    return retrieveMe;
  }

}
