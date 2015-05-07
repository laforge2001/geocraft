/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.io.modspec;


/**
 * Constants used in the I/O of ModSpec grids.
 */
public interface ModSpecGridConstants {

  /** The string constant for ModSpec grid file extensions. */
  String GRID_FILE_EXTN = ".grid";

  /** The number of bytes in a float. */
  int LENGTH_OF_FLOAT = 4;

  /** The number of bytes in an integer. */
  int LENGTH_OF_INT = 4;

  /** The number of 4 byte parameters in a binary grid data header. */
  int WORDS_IN_DATA_HEADER = 6;

  /** The byte address return value indicating an error */
  long ERROR_WRITING_HEADER = -1;

}
