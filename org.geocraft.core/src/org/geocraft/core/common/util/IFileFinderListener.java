/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.util;


/**
 * The interface for a listener that will be notified when a file finder has completed its search.
 */
public interface IFileFinderListener {

  /**
   * Invoked when the file finder has completed its search.
   * @param results the array of search results.
   */
  void searchCompleted(FileFinderResult[] results);
}
