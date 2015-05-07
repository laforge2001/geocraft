/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.ui.mapviewer.viewer;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.ui.mapviewer.MapViewRenderer;


/**
 * Defines the renderers currently registered for use with the map viewer.
 * Map view renderers can be added using the <code>org.geocraft.ui.mapviewer.renderer</code>
 * extension point. The registry checks all the registered renderers and can
 * instantiate a renderer instance based on the type of object supplied.
 */
public class RendererRegistry {

  private static final String RENDERER_EXTENSION_POINT = "org.geocraft.ui.mapviewer.renderer";

  private static final String CLASS_ATTR = "class";

  private static final String OBJECT_TYPE_ATTR = "objectType";

  /**
   * Finds and instantiates an instance of a map view renderer that
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
    IConfigurationElement[] rendererConfigs = registry.getConfigurationElementsFor(RENDERER_EXTENSION_POINT);
    for (IConfigurationElement rendererConfig : rendererConfigs) {
      String objectType = rendererConfig.getAttribute(OBJECT_TYPE_ATTR);
      if (objectType.equals(type)) {
        matchingConfigs.add(rendererConfig);
      }
    }
    return matchingConfigs;
  }

  /**
   * Constructs a map view renderer from an OSGI configuration element.
   * 
   * @param config the configuration element defining the renderer.
   * @return the constructed map view renderer.
   */
  public static MapViewRenderer createRenderer(final IConfigurationElement config) throws Exception {
    return (MapViewRenderer) config.createExecutableExtension(CLASS_ATTR);
  }

  /**
   * Find a registered map view renderer based on its qualified class name and
   * create an instance of it.
   * @param klassName the qualified class name of the section view renderer
   * @return Instance of the specified section view renderer if it is registered; otherwise, null;
   */
  public static MapViewRenderer findRenderer(final String klassName) throws Exception {
    // Get the extension registry.
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    // Find all the config elements for the renderer extension point.
    IConfigurationElement[] rendererConfigs = registry.getConfigurationElementsFor(RENDERER_EXTENSION_POINT);
    for (IConfigurationElement rendererConfig : rendererConfigs) {
      if (rendererConfig.getAttribute(CLASS_ATTR).equals(klassName)) {
        return createRenderer(rendererConfig);
      }
    }

    return null;
  }
}
