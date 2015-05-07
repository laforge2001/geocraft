/*
 * Copyright (C) ConocoPhillips 2006 - 2008 All Rights Reserved.
 */
package org.geocraft.core.common.util;


import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.framework.internal.core.BundleURLConnection;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;


/**
 * Utility class that contains random stuff that doesn't fit anywhere else.
 */
public class Utilities {

  /** The logger. */
  private static ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(Utilities.class);

  /**
   * The constructor.
   */
  private Utilities() {
    // intentionally left blank
  }

  /**
   * Returns the location of the current workspace.
   * <p>
   * NOTE: This should not be confused with the current working directory!
   * 
   * @return the current workspace directory.
   */
  public static String getWorkspaceDirectory() {
    return Platform.getLocation().toOSString();
  }

  /**
   * Returns the location of the current working directory.
   * 
   * @return the current working directory.
   */
  public static String getWorkingDirectory() {
    return System.getProperty("user.dir", ".");
  }

  /**
   * Returns the location of the user's home directory.
   * 
   * @return the user's home directory.
   */
  public static String getHomeDirectory() {
    return System.getProperty("user.home");
  }

  /**
   * Helper function for getting the path in the file system to the bundle location
   */
  public static String getPath(final String bundleName) {

    URL entry = Platform.getBundle(bundleName).getEntry(".");
    if (entry != null) {
      URLConnection connection;
      try {
        connection = entry.openConnection();
        if (connection instanceof BundleURLConnection) {
          URL fileURL = ((BundleURLConnection) connection).getFileURL();
          URI uri = new URI(fileURL.toString());
          String path = new File(uri).getAbsolutePath();
          return path.substring(0, path.length() - 1);
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (URISyntaxException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    }

    String location = Platform.getBundle(bundleName).getLocation();
    LOGGER.info("The workspace directory: " + getWorkspaceDirectory());
    LOGGER.info("The working directory: " + getWorkingDirectory());
    LOGGER.info("The located path: " + location);
    return location.substring(location.lastIndexOf(':') + 1);
  }

  /**
   * Returns <code>true</code> if running on a Windows platform.
   * 
   * @return <code>true</code> if running on a Windows platform
   */
  public static boolean isWindowsPlatform() {
    String osName = System.getProperty("os.name");
    return osName != null && osName.toLowerCase().contains("win");
  }

  /**
   * Set the system properties for the network proxy.
   */
  public static void setNetworkProxy() {
    String host = "";
    int port = 0;
    boolean setProxy = false;
    try {
      System.setProperty("java.net.useSystemProxies", "true");
      List<Proxy> l = ProxySelector.getDefault().select(new URI("http://www.google.com/"));
      for (Proxy proxy : l) {
        System.out.println("proxy hostname : " + proxy.type());
        InetSocketAddress addr = (InetSocketAddress) proxy.address();
        if (addr == null) {
          System.out.println("No Proxy");
        } else {
          setProxy = true;
          host = addr.getHostName();
          port = addr.getPort();
          System.out.println("proxy hostname : " + addr.getHostName());
          System.out.println("proxy port : " + addr.getPort());
        }
      }
    } catch (Exception e) {
      LOGGER.warn(e.getMessage(), e);
    }
    if (setProxy) {
      // setting the proxy data
      // TODO: create a preferences panel for these settings
      System.getProperties().put("proxySet", "true");
      System.getProperties().put("proxyHost", host);
      System.getProperties().put("proxyPort", port + "");
    }
  }

  public static void beep(final int times) {
    for (int i = 0; i < times; i++) {
      try {
        Thread.sleep(50);
        LOGGER.error("beep!");
        // Toolkit.getDefaultToolkit().beep();
      } catch (InterruptedException e) {
        LOGGER.debug("Interrupted");
        // can ignore this one.
      }
    }
  }

  /**
   * Creates a copy of a given float array.
   * This is used for defensive purposes in the get(),set() methods.
   * 
   * @param input the float array to copy.
   * @return the float array copy.
   */
  public static float[] copyFloatArray(final float[] input) {
    float[] output = new float[input.length];
    System.arraycopy(input, 0, output, 0, input.length);
    return output;
  }

  /**
   * Creates a copy of a given float array.
   * This is used for defensive purposes in the get(),set() methods.
   * 
   * @param input the float array to copy.
   * @return the float array copy.
   */
  public static double[] copyDoubleArray(final double[] input) {
    double[] output = new double[input.length];
    System.arraycopy(input, 0, output, 0, input.length);
    return output;
  }

  public static float[] copyDoubleArrayToFloatArray(final double[] input) {
    float[] output = new float[input.length];
    for (int i = 0; i < input.length; i++) {
      output[i] = (float) input[i];
    }
    return output;
  }

  public static double[] copyFloatArrayToDoubleArray(final float[] input) {
    double[] output = new double[input.length];
    for (int i = 0; i < input.length; i++) {
      output[i] = input[i];
    }
    return output;
  }

  public static List<String> sortKeys(final Set<String> keySet) {
    List<String> keys = new ArrayList<String>();
    for (String key : keySet) {
      keys.add(key);
    }
    Collections.sort(keys);
    return keys;
  }
}
