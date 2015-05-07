/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.service.color;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.geocraft.core.color.ColorMapDescription;
import org.geocraft.core.service.ServiceProvider;


public class ColorMapService implements IColorMapService {

  public ColorMapService() {
    ServiceProvider.getLoggingService().getLogger(getClass()).debug("Default Color Map service started.");
  }

  public ColorMapDescription[] getAll() {
    List<ColorMapDescription> colorMapList = new ArrayList<ColorMapDescription>();
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IConfigurationElement[] configElements = registry.getConfigurationElementsFor("org.geocraft.core.color.colorMap");
    for (IConfigurationElement configElement : configElements) {
      colorMapList.add(new ColorMapDescription(configElement));
    }
    return colorMapList.toArray(new ColorMapDescription[0]);
  }

}
