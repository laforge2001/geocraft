/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.asciigrid;


/**
 * Constants used in the I/O of Ascii file grids.
 */
public interface AsciiFileConstants {

  public static final String PLUGIN_ID = "org.geocraft.io.asciigrid";

  /** The string constant for the geologic feature type. */
  String META_FEATURE_TYPE = "FEATURE_TYPE";

  /** The string constant for the geologic feature name. */
  String META_FEATURE_NAME = "FEATURE_NAME";

  /** The string constant for the grid name key. */
  String META_GRID_NAME = "GRID_NAME";

  /** The string constant for the x,y unit key. */
  String META_XY_UNIT = "XY_UNIT";

  /** The string constant for the data unit key. */
  String META_DATA_UNIT = "DATA_UNIT";

  /** The string constant for the onset type. */
  String META_ONSET_TYPE = "ONSET_TYPE";

  /** The string constant for the orientation key. */
  String META_ORIENTATION = "ORIENTATION";

  /** The string constant for the ASCII format. */
  String ASCII_FORMAT = "ASCII";

  /** The string constant for the binary format. */
  String BINARY_FORMAT = "Binary";

  /** The string constant for ascii text file extensions. */
  String TEXT_FILE_EXTN = ".txt";

  /** The string constant for x=column, y=row. */
  String X_IS_COLUMN = "X->Column,Y->Row";

  /** The string constant for y=column, x=row. */
  String Y_IS_COLUMN = "X->Row,Y->Column";

  /** The number of bytes in a float. */
  int LENGTH_OF_FLOAT = 4;

  /** The number of bytes in an integer. */
  int LENGTH_OF_INT = 4;

  /** The number of 4 byte parameters in a binary grid data header. */
  int WORDS_IN_DATA_HEADER = 6;

  /** The byte address return value indicating an error */
  long ERROR_WRITING_HEADER = -1;

}
