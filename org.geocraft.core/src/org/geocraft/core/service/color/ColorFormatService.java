/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.service.color;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.geocraft.core.color.ColorFormatDescription;
import org.geocraft.core.service.ServiceProvider;


public class ColorFormatService implements IColorFormatService {

  public ColorFormatService() {
    ServiceProvider.getLoggingService().getLogger(getClass()).debug("Default Color Format service started.");
  }

  public ColorFormatDescription[] getAll() {
    List<ColorFormatDescription> colorFormatList = new ArrayList<ColorFormatDescription>();
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IConfigurationElement[] configElements = registry
        .getConfigurationElementsFor("org.geocraft.core.color.colorFormat");
    for (IConfigurationElement configElement : configElements) {
      colorFormatList.add(new ColorFormatDescription(configElement));
    }
    return colorFormatList.toArray(new ColorFormatDescription[0]);
  }

}
