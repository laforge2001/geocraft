package org.geocraft.internal.ui.volumeviewer;


import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;


public class Activator extends AbstractUIPlugin {

  /** The logger for the bundle. */
  private static ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(Activator.class);

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
}
