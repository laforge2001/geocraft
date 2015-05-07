/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */
package org.geocraft.geomath.algorithm;


import java.io.File;
import java.io.FileOutputStream;
import java.util.EnumSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.algorithm.StandaloneAlgorithmTask;
import org.geocraft.core.common.progress.TaskRunner;


public class AlgorithmScript {

  public AlgorithmScript() {
    // Empty for Now
  }

  @SuppressWarnings("unchecked")
  public Enum restoreEnum(final String name, final Class klazz) {
    for (Object enumType : EnumSet.allOf(klazz)) {
      if (enumType.toString().equals(name)) {
        return (Enum) enumType;
      }
    }
    return null;
  }

  /**
   * @save the Script
   */
  public void saveScript(final String directory, final String fileName, final String scriptLine) {

    // Generate the output file name
    // (add the .py extension if needed)
    String outputFileName = fileName;
    if (!outputFileName.contains(".")) {
      outputFileName = outputFileName + ".py";
    }
    String filePath = outputFileName;
    if (!outputFileName.startsWith("/")) {
      filePath = directory + File.separator + outputFileName;
    }

    File file = new File(filePath);

    try {
      writeScript(file, scriptLine);
    } catch (Exception ex) {
      String text = "File I/O Error: " + ex.getMessage();
      IStatus status = new Status(IStatus.ERROR, getClass().getName(), text, null);
      ErrorDialog.openError(null, "Error while writing the script to the file", "Unable to write script to the file",
          status);
    }
  }

  /**
   * Writes the equations file.
   * 
   * @param file
   *          the equations file to write.
   * @exception Exception
   *              if there is an error writing the equations file.
   */
  protected void writeScript(final File file, final String scriptLine) {

    FileOutputStream ostream;
    String text;

    // Check file pointer.
    text = "Exception:\nwriteScript(file, scriptLine)\nNull File Pointer";
    if (file == null) {
      IStatus status = new Status(IStatus.ERROR, getClass().getName(), text, null);
      ErrorDialog.openError(null, "Error while writing script to the file", "Unable to write the script to the file",
          status);
      return;
    }

    try {
      // Create file output stream.
      ostream = new FileOutputStream(file);
      // Write equations
      writeScript(ostream, scriptLine);
      // Close file output stream.
      ostream.close();

    } catch (Exception ex) {
      text = "Exception:\nwriteScript(file, scriptLine)\n" + ex.getMessage();
      IStatus status = new Status(IStatus.ERROR, getClass().getName(), text, null);
      ErrorDialog.openError(null, "Error while writing scirpt to file", "Unable to write the script to the file",
          status);
      return;
    }
  }

  /**
   * Writes the equations file.
   * 
   * @param ostream
   *          the equations file output stream to write.
   * @exception Exception
   *              if there is an error writing the equations file output stream.
   */
  protected void writeScript(final FileOutputStream ostream, final String scriptLine) throws Exception {

    String equationTxt = scriptLine + "\n";
    ostream.write(equationTxt.getBytes());
  }

  /**
   * runs the algorithm
   * 
   * @param StandaloneAlgorithm algorithm to run
   */
  public void runAlgorithm(final StandaloneAlgorithm algorithm, final String algorithmName) {

    StandaloneAlgorithmTask task = new StandaloneAlgorithmTask(algorithm, algorithmName);

    // Run the task.
    TaskRunner.runTask(task, algorithmName, TaskRunner.JOIN);

  }

}
