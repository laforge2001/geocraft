/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.ui.waveletviewer;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.ui.waveletviewer.renderer.WaveletViewRenderer;


/**
 * Defines the renderers currently registered for use with the wavelet viewer.
 * wavelet view renderers can be added using the <code>org.geocraft.ui.waveletviewer.renderer</code>
 * extension point. The registry checks all the registered renderers and can
 * instantiate a renderer instance based on the type of object supplied.
 */
public class RendererRegistry {

  /**
   * Finds and instantiates an instance of a wavelet view renderer that
   * supports the specified array of objects.
   * 
   * @param objects the array of objects for which to find a renderer.
   * @return the renderer instance, or <i>null</i> if none found.
   */
  public static List<IConfigurationElement> findRenderer(Shell shell, Object[] objects, String subplot) {
    List<IConfigurationElement> matchingConfigs = new ArrayList<IConfigurationElement>();
    if (objects.length <= 1) {
      return matchingConfigs;
    }
    String[] types = new String[objects.length];
    for (int i = 0; i < objects.length; i++) {
      types[i] = objects[i].getClass().getSimpleName();
    }
    // Get the extension registry.
    IExtensionRegistry registry = Platform.getExtensionRegistry();

    // Find all the config elements for the renderer extension point.
    IConfigurationElement[] rendererConfigs = registry
        .getConfigurationElementsFor("org.geocraft.ui.waveletviewer.renderer");
    for (IConfigurationElement rendererConfig : rendererConfigs) {
      String splot = rendererConfig.getAttribute("subplot");
      if (!splot.isEmpty() && splot.equalsIgnoreCase(subplot)) {
        IConfigurationElement[] objectConfigs = rendererConfig.getChildren("object");
        // Only match on renderers that define a single object type.
        if (objectConfigs != null && objectConfigs.length == types.length) {
          boolean[] used = new boolean[types.length];
          for (String type : types) {
            for (int j = 0; j < objectConfigs.length; j++) {
              if (!used[j]) {
                // Only match if the type matches.
                if (objectConfigs[j].getAttribute("type").equals(type)) {
                  used[j] = true;
                  continue;
                  //matchingConfigs.add(rendererConfig);
                }
              }
            }
          }
          boolean allUsed = true;
          for (int j = 0; j < objectConfigs.length; j++) {
            if (!used[j]) {
              allUsed = false;
              break;
            }
          }
          if (allUsed) {
            matchingConfigs.add(rendererConfig);
          }
        }
      }
    }
    return matchingConfigs;
  }

  /**
   * Finds and instantiates an instance of a wavelet view renderer that
   * supports the specified object.
   *
   * @param object the object for which to find a renderer.
   * @return the renderer instance, or <i>null</i> if none found.
   */
  public static List<IConfigurationElement> findRenderer(final Shell shell, final Object object, String subplot) {
    String type = object.getClass().getSimpleName();
    List<IConfigurationElement> matchingConfigs = new ArrayList<IConfigurationElement>();
    // Get the extension registry.
    IExtensionRegistry registry = Platform.getExtensionRegistry();

    // Find all the config elements for the renderer extension point.
    IConfigurationElement[] rendererConfigs = registry
        .getConfigurationElementsFor("org.geocraft.ui.waveletviewer.renderer");
    for (IConfigurationElement rendererConfig : rendererConfigs) {
      String splot = rendererConfig.getAttribute("subplot");
      if (!splot.isEmpty() && splot.equalsIgnoreCase(subplot)) {
        IConfigurationElement[] objectConfigs = rendererConfig.getChildren("object");
        // Only match on renderers that define a single object type.
        if (objectConfigs != null && objectConfigs.length == 1) {
          // Only match if the type matches.
          if (objectConfigs[0].getAttribute("type").equals(type)) {
            matchingConfigs.add(rendererConfig);
          }
        }
      }
    }
    return matchingConfigs;
  }

  /**
   * Find a registered wavelet view renderer based on its qualified class name and
   * create an instance of it.
   * @param klass Qualified class name of the section view renderer
   * @return Instance of the specified section view renderer if it is registered; otherwise, null;
   */
  public static WaveletViewRenderer findRenderer(final String klass) throws Exception {
    // Get the extension registry.
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    // Find all the config elements for the renderer extension point.
    IConfigurationElement[] rendererConfigs = registry
        .getConfigurationElementsFor("org.geocraft.ui.waveletviewer.renderer");
    for (IConfigurationElement rendererConfig : rendererConfigs) {
      if (rendererConfig.getAttribute("class").equals(klass)) {
        return createRenderer(rendererConfig);
      }
    }

    return null;
  }

  /**
   * Constructs a wavelet view renderer from an OSGI configuration element.
   * 
   * @param config the configuration element defining the renderer.
   * @return the constructed wavelet view renderer.
   */
  public static WaveletViewRenderer createRenderer(IConfigurationElement config) throws Exception {
    return (WaveletViewRenderer) config.createExecutableExtension("class");
  }

}
