package org.geocraft.internal.geomath.algorithm.horizon;


import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;


public class Activator extends Plugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "org.geocraft.geomath.algorithm.horizon";

  // The shared instance
  private static Activator plugin;

  /**
   * The constructor
   */
  public Activator() {
    // Empty for now
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
    // _context = context;

    Bundle bundle = context.getBundle();

    // x will be something like: "bundle@57"
    URL x = FileLocator.find(bundle, new Path(""), null);

    URL url = FileLocator.toFileURL(x);
    String pluginsFolder = url.getFile() + "plugins";
    System.setProperty("horizonscript.path", pluginsFolder);
  }

  /*
   * (non-Javadoc)aasa
   * 
   * @see
   * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static Activator getDefault() {
    return plugin;
  }

}
