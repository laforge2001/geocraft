package org.geocraft.abavo;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


public class Activator extends AbstractUIPlugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "org.geocraft.abavo";

  // The shared instance
  private static Activator _activator;

  public Activator() {
    // No action.
  }

  @Override
  public void start(final BundleContext context) throws Exception {
    super.start(context);
    _activator = this;
  }

  @Override
  public void stop(final BundleContext context) throws Exception {
    _activator = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   * @return the shared instance
   */
  public static Activator getDefault() {
    return _activator;
  }

  public ImageDescriptor createImageDescriptor(final String imagePath) {
    return imageDescriptorFromPlugin(PLUGIN_ID, imagePath);
  }

  public Image createImage(final String imagePath) {
    ImageDescriptor imageDesc = createImageDescriptor(imagePath);
    return imageDesc.createImage();
  }
}
