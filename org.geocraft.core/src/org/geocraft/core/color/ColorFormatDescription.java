/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.color;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.geocraft.core.color.format.IColorFormat;


public class ColorFormatDescription {

  private final IConfigurationElement _configElement;

  public ColorFormatDescription(final IConfigurationElement configElement) {
    _configElement = configElement;
  }

  public String getName() {
    return _configElement.getAttribute("name");
  }

  public boolean canRead() {
    return Boolean.parseBoolean(_configElement.getAttribute("canRead"));
  }

  public boolean canWrite() {
    return Boolean.parseBoolean(_configElement.getAttribute("canWrite"));
  }

  public IColorFormat createFormat() {
    try {
      IColorFormat colorFormat = (IColorFormat) _configElement.createExecutableExtension("class");
      return colorFormat;
    } catch (CoreException e) {
      throw new RuntimeException(e);
    }
  }
}
