/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */
package org.geocraft.ui.common.image;


import java.net.URL;

import javax.swing.ImageIcon;

import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.internal.ui.common.SharedImages;


/**
 * Miscellaneous static helper methods for Swing applications.
 */
public class ImageRegistryUtil {

  /** The logger. */
  private static ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(ImageRegistryUtil.class);

  private static ISharedImages _sharedImages = new SharedImages();

  /**
   * Don't let people instantiate the class.
   */
  private ImageRegistryUtil() {
    // intentionally blank
  }

  public static ISharedImages getSharedImages() {
    return _sharedImages;
  }

  /**
   * Create an icon using a path that is related to the location of a specified class. Removed the null check - we may as well
   * raise an Exception. Bizarre - I tried using this approach because I thought
   * there was an issue with the NB classloader but it doesn't work ..
   * _iconColorBar = ImageRegistryUtil.createImageIcon(this.getClass(), imagePathGlobal +
   * "ColorBar.30x30.gif"); URL imgURL = cl.getResource(path); return new
   * ImageIcon(imgURL);
   * 
   * @param path to the file.
   * @return the image icon.
   */
  public static ImageIcon createImageIcon(final String path) {
    URL url = createURL(path);
    if (url == null) {
      LOGGER.error("Could not find an image " + path + " " + url);
      return null;
    }
    return new ImageIcon(url);
  }

  /**
   * Create a URL using a path that is related to the location of a specified
   * class.
   * 
   * @param path
   * @return the URL.
   */
  public static URL createURL(final String path) {
    // this does not work for the JNLP class loader
    // URL url = ClassLoader.getSystemResource(path);
    URL url = ImageRegistryUtil.class.getClassLoader().getResource(path);
    // extra debug message since this seems to be a common problem
    if (url == null && LOGGER != null) {
      LOGGER.warn("Could not find URL for: " + path);
    }
    return url;
  }

}
