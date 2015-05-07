/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.core.common.util;


import org.eclipse.core.runtime.NullProgressMonitor;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.service.ServiceProvider;


/**
 * Defines a file finder, used to scan directory structures and return results that match a user-specified
 * search string ('*' allowed as wildcard).
 */
public class FileFinder {

  /** Enumeration for the items type. */
  public enum ItemType {
    /** Search for files only. */
    FilesOnly("Files Only"),
    /** Search for directories only. */
    DirsOnly("Directories Only"),
    /** Search for files and directories. */
    FilesAndDirs("Both");

    private final String _displayName;

    ItemType(final String displayName) {
      _displayName = displayName;
    }

    @Override
    public String toString() {
      return _displayName;
    }
  }

  public FileFinder() {
    // The no argument constructor.
  }

  /**
   * Finds all the file and directory paths matching the specified search string.
   * This call is NOT run in the background and thus will block the UI.
   * 
   * @param basePath the base path to search.
   * @param searchStr the search string.
   * @param scanSubDirs set true to scan sub-directories; otherwise set false.
   * @return the file paths matching the specified search string.
   */
  public FileFinderResult[] find(final String basePath, final String searchStr, final boolean scanSubDirs) {
    return find(basePath, searchStr, ItemType.FilesAndDirs, scanSubDirs);
  }

  /**
   * Finds all the file paths matching the specified search string.
   * This call is NOT run in the background and thus will block the UI.
   * 
   * @param basePath the base path to search.
   * @param searchStr the search string.
   * @param scanSubDirs set true to scan sub-directories; otherwise set false.
   * @return the file paths matching the specified search string.
   */
  public FileFinderResult[] findFiles(final String basePath, final String searchStr, final boolean scanSubDirs) {
    return find(basePath, searchStr, ItemType.FilesOnly, scanSubDirs);
  }

  /**
   * Finds all the file paths matching the specified search string.
   * This call is NOT run in the background and thus will block the UI.
   * 
   * @param basePath the base path to search.
   * @param searchStr the search string.
   * @param scanSubDirs set true to scan sub-directories; otherwise set false.
   * @return the file paths matching the specified search string.
   */
  public FileFinderResult[] findDirs(final String basePath, final String searchStr, final boolean scanSubDirs) {
    return find(basePath, searchStr, ItemType.DirsOnly, scanSubDirs);
  }

  /**
   * Finds all the file paths matching the specified search string.
   * This call is NOT run in the background and thus will block the UI.
   * 
   * @param basePath the base path to search.
   * @param searchStr the search string.
   * @param scanSubDirs set true to scan sub-directories; otherwise set false.
   * @return the file paths matching the specified search string.
   */
  public FileFinderResult[] find(final String basePath, final String searchStr, final ItemType itemType,
      final boolean scanSubDirs) {
    return find(new String[] { basePath }, searchStr, itemType, scanSubDirs);
  }

  /**
   * Finds all the file paths matching the specified search string.
   * This call is NOT run in the background and thus will block the UI.
   * 
   * @param basePaths the array of base paths to search.
   * @param searchStr the search string.
   * @param scanSubDirs set true to scan sub-directories; otherwise set false.
   * @return the file paths matching the specified search string.
   */
  public FileFinderResult[] find(final String[] basePaths, final String searchStr, final ItemType itemType,
      final boolean scanSubDirs) {
    FileFinderTask task = new FileFinderTask(basePaths, searchStr, itemType, scanSubDirs, null);
    return (FileFinderResult[]) task.compute(ServiceProvider.getLoggingService().getLogger(FileFinder.class),
        new NullProgressMonitor());
    //return (FileFinderResult[]) TaskRunner.runTask(task, "Searching...", TaskRunner.JOIN);
  }

  /**
   * Finds all the file and directory paths matching the specified search string.
   * This call is run in the background and thus will not block the UI.
   * 
   * @param basePath the base path to search.
   * @param searchStr the search string.
   * @param scanSubDirs set true to scan sub-directories; otherwise set false.
   * @param listener the listener that will be notified of the results when the search is completed.
   */
  public void find(final String basePath, final String searchStr, final boolean scanSubDirs,
      final IFileFinderListener listener) {
    find(basePath, searchStr, ItemType.FilesAndDirs, scanSubDirs, listener);
  }

  /**
   * Finds all the file paths matching the specified search string.
   * This call is run in the background and thus will not block the UI.
   * 
   * @param basePath the base path to search.
   * @param searchStr the search string.
   * @param scanSubDirs set true to scan sub-directories; otherwise set false.
   * @param listener the listener that will be notified of the results when the search is completed.
   */
  public void findFiles(final String basePath, final String searchStr, final boolean scanSubDirs,
      final IFileFinderListener listener) {
    find(basePath, searchStr, ItemType.FilesOnly, scanSubDirs, listener);
  }

  /**
   * Finds all the file paths matching the specified search string.
   * This call is run in the background and thus will not block the UI.
   * 
   * @param basePath the base path to search.
   * @param searchStr the search string.
   * @param scanSubDirs set true to scan sub-directories; otherwise set false.
   * @param listener the listener that will be notified of the results when the search is completed.
   */
  public void findDirs(final String basePath, final String searchStr, final boolean scanSubDirs,
      final IFileFinderListener listener) {
    find(basePath, searchStr, ItemType.DirsOnly, scanSubDirs, listener);
  }

  /**
   * Finds all the file paths matching the specified search string.
   * This call is run in the background and thus will not block the UI.
   * 
   * @param basePath the base path to search.
   * @param searchStr the search string.
   * @param scanSubDirs set true to scan sub-directories; otherwise set false.
   * @param listener the listener that will be notified of the results when the search is completed.
   */
  public void find(final String basePath, final String searchStr, final ItemType itemType, final boolean scanSubDirs,
      final IFileFinderListener listener) {
    find(new String[] { basePath }, searchStr, itemType, scanSubDirs, listener);
  }

  /**
   * Finds all the file paths matching the specified search string.
   * This call is run in the background and thus will not block the UI.
   * 
   * @param basePaths the array of base paths to search.
   * @param searchStr the search string.
   * @param scanSubDirs set true to scan sub-directories; otherwise set false.
   * @param listener the listener that will be notified of the results when the search is completed.
   */
  public void find(final String[] basePaths, final String searchStr, final ItemType itemType,
      final boolean scanSubDirs, final IFileFinderListener listener) {
    FileFinderTask task = new FileFinderTask(basePaths, searchStr, itemType, scanSubDirs, listener);
    TaskRunner.runTask(task, "Searching...", TaskRunner.INTERACTIVE);
  }
}
