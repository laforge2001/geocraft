/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Shell;


public class VolumeViewRendererRegistry {

  /**
   * Finds and instantiates an instance of a volume view renderer that
   * supports the specified object.
   *
   * @param object the object for which to find a renderer.
   * @return the renderer instance, or <i>null</i> if none found.
   */
  public static List<IConfigurationElement> findRenderer(final Shell shell, final Object object) {
    String type = object.getClass().getSimpleName();
    List<IConfigurationElement> matchingConfigs = new ArrayList<IConfigurationElement>();
    // Get the extension registry.
    IExtensionRegistry registry = Platform.getExtensionRegistry();

    // Find all the config elements for the renderer extension point.
    IConfigurationElement[] rendererConfigs = registry
        .getConfigurationElementsFor("org.geocraft.ui.volumeviewer.renderer");
    for (IConfigurationElement rendererConfig : rendererConfigs) {
      // Only match if the type matches.
      if (rendererConfig.getAttribute("type").equals(type)) {
        matchingConfigs.add(rendererConfig);
      }
    }
    return matchingConfigs;
  }

  /**
   * Constructs a volume view renderer from an OSGI configuration element.
   * 
   * @param config the configuration element defining the renderer.
   * @return the constructed volume view renderer.
   */
  public static VolumeViewRenderer createRenderer(final IConfigurationElement config) throws Exception {
    return (VolumeViewRenderer) config.createExecutableExtension("class");
  }

  /**
   * Find a registered volume view renderer based on its qualified class name and
   * create an instance of it.
   * @param klass Qualified class name of the section view renderer
   * @return Instance of the specified section view renderer if it is registered; otherwise, null;
   */
  public static VolumeViewRenderer findRenderer(final String klass) throws Exception {
    // Get the extension registry.
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    // Find all the config elements for the renderer extension point.
    IConfigurationElement[] rendererConfigs = registry
        .getConfigurationElementsFor("org.geocraft.ui.volumeviewer.renderer");
    for (IConfigurationElement rendererConfig : rendererConfigs) {
      if (rendererConfig.getAttribute("class").equals(klass)) {
        return createRenderer(rendererConfig);
      }
    }

    return null;
  }
}
