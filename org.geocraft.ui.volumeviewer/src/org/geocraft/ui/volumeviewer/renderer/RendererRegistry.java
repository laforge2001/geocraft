///*
// * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
// */
//package org.geocraft.ui.volumeviewer.renderer;
//
//
//import org.eclipse.core.runtime.CoreException;
//import org.eclipse.core.runtime.IConfigurationElement;
//import org.eclipse.core.runtime.IExtensionRegistry;
//import org.eclipse.core.runtime.Platform;
//
//
///**
// * A registry for the 3d viewer renderers.
// */
//public class RendererRegistry {
//
//  /** The singleton instance. */
//  private static RendererRegistry _registry = new RendererRegistry();
//
//  /** The config elements. */
//  private static IConfigurationElement[] _configs;
//
//  public static RendererRegistry getInstance() {
//    if (_configs == null) {
//      buildRendererConfig();
//    }
//    return _registry;
//  }
//
//  private static void buildRendererConfig() {
//    final IExtensionRegistry registry = Platform.getExtensionRegistry();
//    _configs = registry.getConfigurationElementsFor("org.geocraft.ui.volumeviewer.renderers");
//  }
//
//  /**
//   * Build and return a renderer for the provided type.
//   * @param type the object data type
//   * @return the renderer or null is a renderer for the data type doesn't exist
//   */
//  public AbstractRenderer getRendererForEntityType(final String type) {
//    AbstractRenderer renderer = null;
//    for (int i = 0; i < _configs.length && renderer == null; i++) {
//      final String entityType = _configs[i].getAttribute("entityType");
//      if (entityType.equals(type)) {
//        try {
//          renderer = (AbstractRenderer) _configs[i].createExecutableExtension("class");
//        } catch (final CoreException e) {
//          e.printStackTrace();
//        }
//      }
//    }
//    return renderer;
//  }
//
//}
