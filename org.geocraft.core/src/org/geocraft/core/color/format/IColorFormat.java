/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */

package org.geocraft.core.color.format;


import java.io.IOException;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;


/**
 * A simple interface for color format I/O.
 */
public interface IColorFormat {

  /**
   * Reads colors that have been stored in the specified format.
   * This method must handle the selection of the colors to read,
   * whether it be using a file selection dialog or some other mechanism.
   * @param file the file from which to read the colors.
   * @return the colors.
   */
  RGB[] loadColors(Shell shell) throws IOException;

  /**
   * Saves colors in the specified format.
   * This method must handle the selection of where/how to save the colors,
   * whether it be using a file selection dialog or some other mechanism.
   * @param file the file to write the colors.
   * @param colors the colors to write.
   */
  void saveColors(Shell shell, RGB[] colors) throws IOException;
}
