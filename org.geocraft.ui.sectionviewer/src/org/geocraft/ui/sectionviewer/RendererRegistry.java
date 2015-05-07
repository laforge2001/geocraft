/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Shell;


/**
 * Defines the renderers currently registered for use with the section viewer.
 * Section view renderers can be added using the <code>org.geocraft.ui.SectionViewer.renderer</code>
 * extension point. The registry checks all the registered renderers and can
 * instantiate a renderer instance based on the type of object supplied.
 */
public class RendererRegistry {

  /**
   * Finds and instantiates an instance of a section view renderer that
   * supports the specified array of objects.
   * 
   * @param objects the array of objects for which to find a renderer.
   * @return the renderer instance, or <i>null</i> if none found.
   */
  public static List<IConfigurationElement> findRenderer(final Shell shell, final Object[] objects) {
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
        .getConfigurationElementsFor("org.geocraft.ui.SectionViewer.renderer");
    for (IConfigurationElement rendererConfig : rendererConfigs) {
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
    return matchingConfigs;
  }

  /**
   * Finds and instantiates an instance of a section view renderer that
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
        .getConfigurationElementsFor("org.geocraft.ui.sectionviewer.renderer");
    for (IConfigurationElement rendererConfig : rendererConfigs) {
      IConfigurationElement[] objectConfigs = rendererConfig.getChildren("object");
      // Only match on renderers that define a single object type.
      if (objectConfigs != null && objectConfigs.length == 1) {
        // Only match if the type matches.
        if (objectConfigs[0].getAttribute("type").equals(type)) {
          matchingConfigs.add(rendererConfig);
        }
      }
    }
    return matchingConfigs;
  }

  /**
   * Constructs a section view renderer from an OSGI configuration element.
   * 
   * @param config the configuration element defining the renderer.
   * @return the constructed section view renderer.
   */
  public static SectionViewRenderer createRenderer(final IConfigurationElement config) throws Exception {
    return (SectionViewRenderer) config.createExecutableExtension("class");
  }

  /**
   * Find a registered section view renderer based on its qualified class name and
   * create an instance of it.
   * @param klass Qualified class name of the section view renderer
   * @return Instance of the specified section view renderer if it is registered; otherwise, null;
   */
  public static SectionViewRenderer findRenderer(final String klass) throws Exception {
    // Get the extension registry.
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    // Find all the config elements for the renderer extension point.
    IConfigurationElement[] rendererConfigs = registry
        .getConfigurationElementsFor("org.geocraft.ui.sectionviewer.renderer");
    for (IConfigurationElement rendererConfig : rendererConfigs) {
      if (rendererConfig.getAttribute("class").equals(klass)) {
        return createRenderer(rendererConfig);
      }
    }

    return null;
  }
  //  public static AbstractRenderer selectRenderer(String title, List<IConfigurationElement> configs, boolean required) {
  //    if (configs.size() == 0) {
  //      return null;
  //    }
  //    if (configs.size() == 1 && required) {
  //      try {
  //        return (AbstractRenderer) configs.get(0).createExecutableExtension("class");
  //      } catch (CoreException e) {
  //        // TODO Auto-generated catch block
  //        e.printStackTrace();
  //        return null;
  //      }
  //    }
  //    // More than one renderer found, prompt the user to pick one.
  //    Shell shell = Display.getDefault().getActiveShell();
  //    Dialog dialog = new RendererSelectionDialog(shell, configs.toArray(new IConfigurationElement[0]), required);
  //    dialog.create();
  //    dialog.getShell().setText(title);
  //    dialog.getShell().pack();
  //    Point size = dialog.getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
  //    dialog.getShell().setSize(size);
  //    int index = dialog.open();
  //    if (index >= 0) {
  //      IConfigurationElement config = configs.get(index);
  //      try {
  //        return (AbstractRenderer) config.createExecutableExtension("class");
  //      } catch (CoreException e) {
  //        e.printStackTrace();
  //      }
  //    }
  //    return null;
  //  }

}
