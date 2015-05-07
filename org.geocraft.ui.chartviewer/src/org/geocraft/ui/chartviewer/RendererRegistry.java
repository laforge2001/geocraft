package org.geocraft.ui.chartviewer;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.geocraft.ui.chartviewer.renderer.ChartViewRenderer;


public class RendererRegistry {

  private static RendererRegistry _registry = new RendererRegistry();

  private static IConfigurationElement[] _configs;

  public static RendererRegistry getInstance() {
    if (_configs == null) {
      buildRendererConfig();
    }
    return _registry;
  }

  private static void buildRendererConfig() {
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    _configs = registry.getConfigurationElementsFor("org.geocraft.ui.chartviewer.renderer");
  }

  public ChartViewRenderer getRendererForObjectType(final String objectType, String chartType) {
    ChartViewRenderer renderer = null;
    for (int i = 0; i < _configs.length && renderer == null; i++) {
      if (_configs[i].getAttribute("objectType").equals(objectType)
          && _configs[i].getAttribute("chartType").equals(chartType)) {
        try {
          renderer = (ChartViewRenderer) _configs[i].createExecutableExtension("class");
        } catch (CoreException e) {
          e.printStackTrace();
        }
      }
    }
    return renderer;
  }

}
