/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;


public class CoreToSegyApp {

  public static void convertCoreToSegy(String directory, String fileName) throws Exception {
    CoreToSegyApp app = new CoreToSegyApp();
    TextFile nhdrFile = app.new TextFile(directory + File.separator + fileName + ".nhdr");
    short numSamples = 0;
    short numInlines = 0;
    short numXlines = 0;
    float sampleSpacing = 0;
    float inlineSpacing = 0;
    float xlineSpacing = 0;
    String dataFileName = directory + File.separator + fileName + ".ascii";
    for (String line : nhdrFile) {
      String[] substrings = line.split(" ");
      if (substrings.length > 0) {
        if (line.startsWith("sizes:")) {
          numSamples = Short.parseShort(substrings[1]); // z = samples
          numXlines = Short.parseShort(substrings[2]); // x = xlines
          numInlines = Short.parseShort(substrings[3]); // y = inlines
        } else if (line.startsWith("spacings:")) {
          sampleSpacing = Float.parseFloat(substrings[1]); // z = samples
          xlineSpacing = Float.parseFloat(substrings[2]); // x = xlines
          inlineSpacing = Float.parseFloat(substrings[3]); // y = inlines
        } else if (line.startsWith("data file:")) {
          dataFileName = substrings[2];
          if (dataFileName.startsWith("./")) {
            // The data file path is relative, so preprend the directory.
            dataFileName = directory + File.separator + dataFileName.substring(2);
          } else if (dataFileName.startsWith("/")) {
            // Nothing to do, the data file path is absolute.
          } else {
            // The data file path is relative, so preprend the directory.
            dataFileName = directory + File.separator + dataFileName;
          }
        }
      }
    }
    short cdpFold = 1; // 1 trace per location.
    short fixedTraceLength = 1; // True
    short measurementSys = 1; // Meters
    short sampleRate = (short) Math.round(sampleSpacing * 1000);
    short sampleFormat = 1; // 4-byte IBM.
    short coordScalar = -1000; // Divide by 1000
    short coordUnits = 1; // Length in meters or feet.

    File dataFile = new File(dataFileName);
    File segyFile = new File(directory + File.separator + fileName + ".sgy");
    SegyEbcdicHeader ebcdicHeader = new SegyEbcdicHeader();
    ebcdicHeader.set("Directory: " + directory + "\nHeader File: " + fileName + ".nhdr\nAscii File: " + dataFileName);
    SegyBinaryHeader binaryHeader = new SegyBinaryHeader();
    binaryHeader.putShort(SegyBinaryHeaderCatalog.CDP_FOLD, cdpFold);
    binaryHeader.putShort(SegyBinaryHeaderCatalog.FIXED_TRACE_LENGTH_FLAG, fixedTraceLength);
    binaryHeader.putShort(SegyBinaryHeaderCatalog.SAMPLES_PER_TRACE, numSamples);
    binaryHeader.putShort(SegyBinaryHeaderCatalog.MEASUREMENT_SYSTEM, measurementSys);
    binaryHeader.putShort(SegyBinaryHeaderCatalog.SAMPLE_INTERVAL, sampleRate);
    binaryHeader.putShort(SegyBinaryHeaderCatalog.SAMPLE_FORMAT_CODE, sampleFormat);
    SegyUtil.writeEbcdicHeader(segyFile.getAbsolutePath(), ebcdicHeader);
    SegyUtil.writeBinaryHeader(segyFile.getAbsolutePath(), binaryHeader);
    RandomAccessFile raf = new RandomAccessFile(segyFile.getAbsolutePath(), "rw");
    FileChannel channel = raf.getChannel();
    channel.position(3600);
    BufferedReader dataReader = new BufferedReader(new FileReader(dataFile));
    int counter = 0;
    int inline = 1;
    int xline = 1;
    int x = 0;
    int y = 0;
    SegyTraceHeader traceHeader = new SegyTraceHeader(SegyTraceHeader.POSTSTACK3D_HEADER_DEF);
    ByteBuffer sampleBuffer = ByteBuffer.allocate(4 * numSamples);
    String line = null;
    byte[] bs = new byte[numSamples];
    float[] fs = new float[numSamples];
    int numTracesTotal = numInlines * numXlines;
    int numTracesWritten = 0;
    int completion = 0;
    int completionOld = 0;
    while ((line = dataReader.readLine()) != null) {
      String[] valueStrings = line.split(" ");
      for (String valueString : valueStrings) {
        fs[counter] = Float.parseFloat(valueString);
        counter++;
        if (counter == numSamples) {
          // CREATE AND WRITE TRACE.
          x = Math.round((xline - 1) * xlineSpacing * 1000);
          y = Math.round((inline - 1) * inlineSpacing * 1000);
          //System.out.println("writing inline: " + inline + " xline: " + xline + " x: " + x + " y: " + y + " ns: "
          //    + numSamples + " sr: " + sampleRate);
          traceHeader.putShort(SegyTraceHeaderCatalog.TRACE_ID, (short) 1);
          traceHeader.putShort(SegyTraceHeaderCatalog.COORDINATE_SCALAR, coordScalar);
          traceHeader.putShort(SegyTraceHeaderCatalog.COORDINATE_UNITS, coordUnits);
          traceHeader.putInteger(SegyTraceHeaderCatalog.SOURCE_COORDINATE_X, x);
          traceHeader.putInteger(SegyTraceHeaderCatalog.SOURCE_COORDINATE_Y, y);
          traceHeader.putInteger(SegyTraceHeaderCatalog.GROUP_COORDINATE_X, x);
          traceHeader.putInteger(SegyTraceHeaderCatalog.GROUP_COORDINATE_Y, y);
          traceHeader.putShort(SegyTraceHeaderCatalog.NUM_SAMPLES, numSamples);
          traceHeader.putShort(SegyTraceHeaderCatalog.SAMPLE_INTERVAL, sampleRate);
          traceHeader.updateBufferFromHeader();
          ByteBuffer buffer = traceHeader.getBuffer();
          buffer.position(180);
          buffer.putInt(inline);
          buffer.position(184);
          buffer.putInt(xline);
          buffer.position(0);
          channel.write(buffer);
          sampleBuffer.position(0);
          channel.write(sampleBuffer);
          //SegyBytes.putFloatsTo4BytesFloatIEEE(numSamples, fs, sampleBuffer.array(), 0);
          SegyBytes.putFloatsTo4BytesFloatIBMorig(numSamples, fs, sampleBuffer.array(), 0);
          channel.write(sampleBuffer);

          xline++;
          if (xline > numXlines) {
            xline = 1;
            inline++;
          }
          counter = 0;
          numTracesWritten++;
          completion = numTracesWritten * 100 / numTracesTotal;
          if (completion % 5 == 0 && completion != completionOld) {
            System.out.println("Completion: " + completion + " %");
          }
          completionOld = completion;
        }
      }
    }
    channel.close();
    System.out.println("File converted: " + segyFile.getAbsolutePath());
  }

  /**
   * Make java i/o as simple as Python. :-)
   */
  private class TextFile extends ArrayList<String> {

    /**
     * Initialize an empty TextFile.
     */
    public TextFile() {
      super(100);
    }

    /**
     * Initialize the TextFile by loading records from a file.
     * @param fileName Pathname of the file
     */
    public TextFile(String fileName) {

      this();
      read(fileName);
    }

    /**
     * Initialize the TextFile by loading records from a file.
     * @param fd Descriptor of the file
     */
    public TextFile(FileDescriptor fd) {
      this();
      read(fd);
    }

    /**
     * Read the file into the array and remove the end of line characters.
     */
    public void read(String fileName) {

      try {

        BufferedReader in = new BufferedReader(new FileReader(new File(fileName).getAbsoluteFile()));

        try {
          String line;

          while ((line = in.readLine()) != null) {
            add(line);
          }
        } finally {
          in.close();
        }

      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }

    /**
     * Read the file into the array and remove the end of line characters.
     */
    public void read(FileDescriptor fd) {

      try {

        BufferedReader in = new BufferedReader(new FileReader(fd));

        try {
          String line;

          while ((line = in.readLine()) != null) {
            add(line);
          }
        } finally {
          in.close();
        }

      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }

    /**
     * Save the array to the specified filename. Adds back the end of line
     * characters.
     */

    public void write(String fileName) {

      try {

        PrintWriter out = new PrintWriter(new File(fileName).getAbsoluteFile());

        try {
          for (String record : this) {
            out.println(record);
          }
        } finally {
          out.close();
        }
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }

    }

    /**
     * Put this in a method as a reminder that this is the idiomatic way to do
     * this.
     */
    public String[] getRecords() {
      return toArray(new String[size()]);
    }

  }

  public static void main2(String[] args) throws Exception {
    main2(new String[] { "/home/walucas/dev/Anastasia/001.nhdr" });
  }

  public static void main(String[] args) throws Exception {
    if (args == null || args.length == 0) {
      System.out.println("No arguments specified!");
      return;
    }
    String fullName = args[0];
    if (!fullName.startsWith("/")) {
      String pwd = System.getProperty("user.dir", ".");
      fullName = pwd + File.separator + fullName;
    }
    if (fullName.endsWith(".nhdr")) {
      fullName = fullName.substring(0, fullName.length() - 5);
    }
    File f = new File(fullName);
    if (!f.exists()) {
      System.out.println("Invalid file: " + f.getAbsolutePath());
    }
    String directory = f.getParent();
    String fileName = f.getName();
    System.out.println("File found: " + directory + " " + fileName);
    convertCoreToSegy(directory, fileName);
  }
}
