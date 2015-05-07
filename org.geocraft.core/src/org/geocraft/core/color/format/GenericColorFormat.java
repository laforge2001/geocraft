/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.core.color.format;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.common.util.Utilities;


/**
 * Defines the I/O for the generic color format.
 */
public class GenericColorFormat implements IColorFormat {

  public static final String PLUGIN_ID = "org.geocraft.core.color";

  /** The unique color format description. */
  private static final String DESCRIPTION = "Generic ColorMap Files (*.cmp)";

  private static final String FILE_EXTENSION = "cmp";

  public GenericColorFormat() {
    // No action required.
  }

  public RGB[] loadColors(final Shell shell) throws IOException {
    // Build the open dialog.
    FileDialog dlgOpen = new FileDialog(new Shell(shell.getDisplay()), SWT.OPEN);

    String[] names = { DESCRIPTION };
    String[] extensions = { "*." + FILE_EXTENSION };
    dlgOpen.setFilterNames(names);
    dlgOpen.setFilterExtensions(extensions);
    dlgOpen.setFilterIndex(0);

    String directory = PreferencesUtil.getPreferencesStore(PLUGIN_ID).get("Workspace_DIR",
        Utilities.getWorkingDirectory());
    dlgOpen.setFilterPath(directory);
    String result = dlgOpen.open();
    if (result != null && result.length() > 0) {
      // Append the appropriate file extension if necessary.
      File file = new File(result);
      String formatExtension = "." + FILE_EXTENSION;
      if (!file.getAbsolutePath().endsWith(formatExtension)) {
        file = new File(file.getAbsolutePath() + formatExtension);
      }

      // Store the selected location in the preferences.
      storeDirectoryInPreferences(file.getParent());

      // Read the colors from the file.
      RGB[] colors = readColors(file);
      return colors;
    }
    return new RGB[0];
  }

  public void saveColors(final Shell shell, final RGB[] colors) throws IOException {
    // Build the save dialog.
    FileDialog dlgSave = new FileDialog(new Shell(shell.getDisplay()), SWT.SAVE);
    dlgSave.setText("Save As...");

    String[] names = { DESCRIPTION };
    String[] extensions = { "*." + FILE_EXTENSION };
    dlgSave.setFilterNames(names);
    dlgSave.setFilterExtensions(extensions);
    dlgSave.setFilterIndex(0);

    String directory = PreferencesUtil.getPreferencesStore(PLUGIN_ID).get("Workspace_DIR",
        Utilities.getWorkingDirectory());
    dlgSave.setFilterPath(directory);
    String result = dlgSave.open();
    if (result != null && result.length() > 0) {
      // Append the appropriate file extension if necessary.
      File file = new File(result);
      String formatExtension = "." + FILE_EXTENSION;
      if (!file.getAbsolutePath().endsWith(formatExtension)) {
        file = new File(file.getAbsolutePath() + formatExtension);
      }

      // Store the selected location in the preferences.
      storeDirectoryInPreferences(file.getParent());

      // Write the colors to the file.
      writeColors(file, colors);
    }
  }

  /**
   * Stores the directory location in the preferences.
   * 
   * @param directory
   *          the directory location to store.
   */
  private void storeDirectoryInPreferences(final String directory) {
    IEclipsePreferences preferencesStore = PreferencesUtil.getPreferencesStore(PLUGIN_ID);
    preferencesStore.put("Workspace_DIR", directory);
    PreferencesUtil.saveInstanceScopePreferences(PLUGIN_ID);
  }

  /**
   * Reads colors from a file on disk using the generic (ASCII) color format.
   * 
   * @param file
   *          the file from which to read the colors.
   * @return the array of color RGBs read.
   * @throws IOException
   *           thrown on a read error.
   */
  private synchronized RGB[] readColors(final File file) throws IOException {
    if (file == null) {
      throw new IOException("No file specified.");
    }
    FileReader reader = new FileReader(file);
    RGB[] colors = null;
    int ncolors = 0;
    int index;
    int red;
    int green;
    int blue;
    BufferedReader buffer = new BufferedReader(reader);
    String line = null;
    line = buffer.readLine();
    if (line == null) {
      buffer.close();
      throw new IOException("File does not match the specified format.");
    }
    String[] substrings = line.split(" ");
    if (substrings.length >= 3) {
      if (substrings[0].equalsIgnoreCase("ncolors") && substrings[1].equalsIgnoreCase("=")) {
        ncolors = Integer.parseInt(substrings[2]);
        if (ncolors < 2) {
          throw new IOException("Invalid number of colors. Must be at least 2.");
        }
        colors = new RGB[ncolors];
        for (int i = 0; i < ncolors; i++) {
          colors[i] = new RGB(0, 0, 0);
        }
      } else {
        buffer.close();
        throw new IOException("File does not match the specified format.");
      }
    } else if (substrings.length == 2) {
      if (substrings[0].equalsIgnoreCase("ncolors=")) {
        ncolors = Integer.parseInt(substrings[1]);
        if (ncolors < 2) {
          throw new IOException("Invalid number of colors. Must be at least 2.");
        }
        colors = new RGB[ncolors];
        for (int i = 0; i < ncolors; i++) {
          colors[i] = new RGB(0, 0, 0);
        }
      } else {
        buffer.close();
        throw new IOException("File does not match the specified format.");
      }
    } else {
      buffer.close();
      throw new IOException("File does not match the specified format.");
    }
    index = 0;
    while (line != null && index < ncolors) {
      line = buffer.readLine();
      if (line != null) {
        substrings = line.split(" ");
        if (substrings.length == 3) {
          red = Integer.parseInt(substrings[0]); // / 257;
          green = Integer.parseInt(substrings[1]); // / 257;
          blue = Integer.parseInt(substrings[2]); // / 257;
          colors[index] = new RGB(red, green, blue);
        }
        index = index + 1;
      }
    }
    buffer.close();
    return colors;
  }

  /**
   * Writes colors to a file on disk using the generic (ASCII) color format.
   * 
   * @param file
   *          the file from which to read the colors.
   * @param colors
   *          the array of color RGBs to write.
   * @throws IOException
   *           thrown on a write error.
   */
  private synchronized void writeColors(final File file, final RGB[] colors) throws IOException {
    if (file == null) {
      throw new IOException("No file specified.");
    }
    if (colors == null) {
      throw new IOException("No colors specified.");
    }
    if (colors.length < 2) {
      throw new IOException("Invalid number of colors. Must be at least 2.");
    }
    FileWriter writer = new FileWriter(file);
    int ncolors = colors.length;
    writer.write("ncolors = " + ncolors + "\n");
    for (int i = 0; i < ncolors; i++) {
      int red = colors[i].red;
      int green = colors[i].green;
      int blue = colors[i].blue;
      //red *= 257;
      //green *= 257;
      //blue *= 257;
      writer.write(red + " " + green + " " + blue + "\n");
    }
    writer.close();
  }
}
