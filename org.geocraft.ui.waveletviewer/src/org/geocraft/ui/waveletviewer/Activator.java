package org.geocraft.ui.waveletviewer;


import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

  /** The plugin ID. */
  public static final String PLUGIN_ID = "org.geocraft.ui.waveletviewer";

  /** The logger for the bundle. */
  private static final ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(Activator.class);

  // The shared instance
  private static Activator plugin;

  /**
   * The constructor
   */
  public Activator() {
    // Empty constructor.
  }

  /**
   * Returns the logger for the bundle.
   * 
   * @return the logger.
   */
  public static ILogger getLogger() {
    return LOGGER;
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
    PreferencePage.setDefaults();
  }

  @Override
  public void stop(BundleContext context) throws Exception {
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
