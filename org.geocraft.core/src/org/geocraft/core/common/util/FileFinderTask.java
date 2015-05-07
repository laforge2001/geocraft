/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.util;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.filechooser.FileSystemView;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.common.progress.BackgroundTask;
import org.geocraft.core.common.util.FileFinder.ItemType;
import org.geocraft.core.service.logging.ILogger;


public class FileFinderTask extends BackgroundTask {

  private String[] _basePaths;

  private String _searchStr;

  private ItemType _itemType;

  private boolean _scanSubDirs;

  private IFileFinderListener _listener;

  public FileFinderTask(final String[] basePaths, final String searchStr, final ItemType itemType, final boolean scanSubDirs, IFileFinderListener listener) {
    _basePaths = new String[basePaths.length];
    System.arraycopy(basePaths, 0, _basePaths, 0, basePaths.length);
    _searchStr = searchStr;
    _itemType = itemType;
    _scanSubDirs = scanSubDirs;
    _listener = listener;
  }

  @Override
  public Object compute(ILogger logger, IProgressMonitor monitor) {
    monitor.beginTask("Searching file system...", 100);
    List<FileFinderResult> searchResults = new ArrayList<FileFinderResult>();
    searchResults.clear();
    int oldWork = 0;
    for (int i = 0; i < _basePaths.length; i++) {
      List<String> subDirsList = new ArrayList<String>();
      int subDirsIndex = -1;

      while (subDirsIndex < subDirsList.size()) {
        String dirName = "";
        if (subDirsIndex == -1) {
          dirName = _basePaths[i];
        } else {
          dirName = _basePaths[i] + File.separator + subDirsList.get(subDirsIndex);
        }
        monitor.subTask("Searching directory \'" + dirName + "\'...");
        searchDirectory(dirName, _searchStr, _itemType, _scanSubDirs, subDirsList, subDirsIndex, searchResults, logger);
        subDirsIndex++;
      }
      subDirsList.clear();
      int completion = (int) (100f * (i + 1) / _basePaths.length);
      int work = completion - oldWork;
      oldWork = completion;
      monitor.worked(work);
      if (monitor.isCanceled()) {
        break;
      }
    }
    int numResults = searchResults.size();
    FileFinderResult[] results = new FileFinderResult[numResults];
    for (int i = 0; i < numResults; i++) {
      results[i] = searchResults.get(i);

      String owner = "";
      String fullPathName = results[i].getPath() + File.separator + results[i].getName();
      String command = "ls -l " + fullPathName + " | cut -d\" \" -f4";
      ProcessBuilder pb = new ProcessBuilder("sh", "-c", command);
      try {
        Process process = pb.start();
        InputStream streamIn = process.getInputStream();
        owner = readBufferedLine(streamIn, logger);
        results[i].setOwner(owner);
      } catch (IOException ex) {
        logger.warn(ex.toString(), ex);
      }
    }
    monitor.subTask("Done searching directories!");
    monitor.worked(100 - oldWork);
    monitor.done();
    if (_listener != null) {
      _listener.searchCompleted(results);
    }

    return results;
  }

  private void searchDirectory(final String dirName, final String searchStr, final ItemType itemType,
      final boolean scanSubDirs, final List<String> subDirsList, final int subDirsIndex,
      final List<FileFinderResult> searchResults, ILogger logger) {
    File dir = new File(dirName);
    if (dir.exists() && dir.isDirectory()) {
      FileSystemView fsv = FileSystemView.getFileSystemView();
      File[] files = fsv.getFiles(new File(dirName), true);
      for (File f : files) {
        String entryName = f.getName();
        String filePath = f.getParent();
        if (filePath == null) {
          filePath = "";
        }

        boolean isOkToAdd = true;

        // Check if the entry is a "." or ".." directory.
        boolean isDotDir = false;
        if (entryName.equals(".") || entryName.equals("..")) {
          isDotDir = true;
        }
        boolean isSubDir = f.isDirectory();
        if (isSubDir && !isDotDir && scanSubDirs) {
          if (subDirsIndex == -1) {
            subDirsList.add(entryName);
          } else {
            subDirsList.add(subDirsList.get(subDirsIndex) + File.separator + entryName);
          }
        }

        // Compare the entry name to the search string.
        boolean fStringsMatch = StringUtil.compareStrings(entryName, searchStr);

        // Determine if all the criteria are met.
        boolean checkIt = false;
        if (isSubDir && fStringsMatch && !isDotDir && !itemType.equals(ItemType.FilesOnly)) {
          checkIt = true;
        }
        if (!isSubDir && fStringsMatch && !itemType.equals(ItemType.DirsOnly)) {
          checkIt = true;
        }
        if (checkIt) {
          // Check if the entry is already in the list. If not add it.
          //boolean isAlreadyListed = _searchResults.contains(entryName) && _filePaths.contains(f.getPath());
          boolean isAlreadyListed = false;
          if (!isAlreadyListed && isOkToAdd) {
            long size = 0;
            if (f.isFile()) {
              try {
                RandomAccessFile raf = new RandomAccessFile(f.getAbsolutePath(), "r");
                FileChannel channel = raf.getChannel();
                size = channel.size();
                channel.close();
              } catch (Exception ex) {
                size = -1;
                logger.warn(ex.toString());
              }
            }
            searchResults.add(new FileFinderResult(entryName, filePath, "", new Timestamp(f.lastModified()), size));
          }
        }
      }
    }
  }

  /**
   * Compares a file or directory name to a search string.
   * @param fileName the file or directory name.
   * @param searchString the search string.
   * @return true if the search string matches the file or directory name; false if not.
   */
  public static boolean compareStrings(final String fileName, final String searchString) {
    boolean startsWithWildcard = searchString.startsWith("*");
    boolean endsWithWildcard = searchString.endsWith("*");
    StringTokenizer tokenizer = new StringTokenizer(searchString, "*");
    boolean firstToken = true;
    String tempName = fileName;
    String token = "";
    while (tokenizer.hasMoreTokens()) {
      token = tokenizer.nextToken();
      if (firstToken) {
        if (!startsWithWildcard && !tempName.startsWith(token)) {
          return false;
        }
        firstToken = false;
      }
      int index = tempName.indexOf(token);
      if (index == -1) {
        return false;
      }
      tempName = tempName.substring(index);
    }
    if (!endsWithWildcard && !fileName.endsWith(token)) {
      return false;
    }
    return true;
  }

  /**
   * Read the line from an input stream.
  * (Assume we only want the last line or there is only one line)
   */
  private String readBufferedLine(final InputStream streamIn, ILogger logger) {
    String readLine = "";
    String line = "";
    BufferedReader reader = new BufferedReader(new InputStreamReader(streamIn));
    try {
      while ((readLine = reader.readLine()) != null) {
        if (!readLine.isEmpty()) {
          line = readLine;
        }
      }
    } catch (IOException ex) {
      logger.warn(ex.toString(), ex);
    }
    return line;
  }
}
