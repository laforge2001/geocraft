/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.math.regression;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.math.regression.IRegressionMethodService;
import org.geocraft.math.regression.RegressionData;
import org.geocraft.math.regression.RegressionMethodDescription;
import org.geocraft.math.regression.RegressionStatistics;
import org.geocraft.math.regression.RegressionType;


public class RegressionMethodService implements IRegressionMethodService {

  /** The logger. */

  public RegressionMethodService() {
    ServiceProvider.getLoggingService().getLogger(getClass()).info("Regression method service started.");
  }

  public RegressionMethodDescription[] getRegressionMethods() {
    List<RegressionMethodDescription> list = new ArrayList<RegressionMethodDescription>();
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IConfigurationElement[] configElements = registry.getConfigurationElementsFor("org.geocraft.math.regressionMethod");
    for (IConfigurationElement configElement : configElements) {
      list.add(new RegressionMethodDescription(configElement));
    }
    return list.toArray(new RegressionMethodDescription[0]);
  }

  public RegressionStatistics compute(final RegressionMethodDescription method, final RegressionType type,
      final RegressionData data) {
    return compute(method.getAcronym(), type, data);
  }

  public RegressionStatistics compute(final String acronym, final RegressionType type, final RegressionData data) {
    for (RegressionMethodDescription method : getRegressionMethods()) {
      if (method.getAcronym().equalsIgnoreCase(acronym)) {
        return method.compute(type, data);
      }
    }
    return null;
  }
}
