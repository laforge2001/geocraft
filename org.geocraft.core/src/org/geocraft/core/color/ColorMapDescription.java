/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.color;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.geocraft.core.color.map.IColorMap;


public class ColorMapDescription {

  private final IConfigurationElement _configElement;

  public ColorMapDescription(final IConfigurationElement configElement) {
    _configElement = configElement;
  }

  public String getName() {
    return _configElement.getAttribute("name");
  }

  public String getDescription() {
    return _configElement.getAttribute("description");
  }

  public IColorMap createMap() {
    try {
      IColorMap colorMap = (IColorMap) _configElement.createExecutableExtension("class");
      return colorMap;
    } catch (CoreException e) {
      throw new RuntimeException(e);
    }
  }

  public Image createImage(final Display display) {
    try {
      IColorMap colorMap = (IColorMap) _configElement.createExecutableExtension("class");
      RGB[] rgbs = colorMap.getRGBs(64);
      int width = 64;
      int height = 16;
      byte[] pixels = new byte[width * height];
      PaletteData palette = new PaletteData(rgbs);
      int depth = 8;
      for (int i = 0; i < width; i++) {
        for (int j = 0; j < height; j++) {
          int index = j * width + i;
          pixels[index] = (byte) i;
        }
      }
      ImageData imageData = new ImageData(width, height, depth, palette, 1, pixels);
      imageData.alpha = 1;
      return new Image(display, imageData);
    } catch (CoreException e) {
      throw new RuntimeException(e);
    }
  }
}
