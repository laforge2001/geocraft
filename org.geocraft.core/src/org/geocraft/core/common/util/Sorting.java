/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.common.util;


import java.util.Comparator;


/**
 * Enhanced sort methods.
 */

public class Sorting {

  /**
   * Alphanumeric sorting ensures that keys like h1, v1, v2, v10 are sorted correctly.
   * <p>
   * Usage: Collections.sort(your list, new AlphanumComparator());
   * <p>
   * Acknowledgment: the alphanumeric sort algorithm was copied from www.davekoelle.com who says
   * "These programs may be used freely as long as they are kept intact, including comments."
   * <p>
   * Apart from some minor cleanup, making it a static class and reformatting the code is unchanged.
   */
  public static final Comparator<String> ALPHANUMERIC_COMPARATOR = new Comparator<String>() {

    private final boolean isDigit(char ch) {
      return ch >= 48 && ch <= 57;
    }

    /** 
     * Length of string is passed in for improved efficiency (only need to calculate it once). 
     */
    private final String getChunk(String s, int slength, int mark) {
      StringBuilder chunk = new StringBuilder();
      char c = s.charAt(mark);
      chunk.append(c);
      int marker = mark + 1;
      if (isDigit(c)) {
        while (marker < slength) {
          c = s.charAt(marker);
          if (!isDigit(c))
            break;
          chunk.append(c);
          marker++;
        }
      } else {
        while (marker < slength) {
          c = s.charAt(marker);
          if (isDigit(c))
            break;
          chunk.append(c);
          marker++;
        }
      }
      return chunk.toString();
    }

    public int compare(String s1, String s2) {
      int thisMarker = 0;
      int thatMarker = 0;
      int s1Length = s1.length();
      int s2Length = s2.length();

      while (thisMarker < s1Length && thatMarker < s2Length) {
        String thisChunk = getChunk(s1, s1Length, thisMarker);
        thisMarker += thisChunk.length();

        String thatChunk = getChunk(s2, s2Length, thatMarker);
        thatMarker += thatChunk.length();

        // If both chunks contain numeric characters, sort them numerically
        int result = 0;
        if (isDigit(thisChunk.charAt(0)) && isDigit(thatChunk.charAt(0))) {
          // Simple chunk comparison by length.
          int thisChunkLength = thisChunk.length();
          result = thisChunkLength - thatChunk.length();
          // If equal, the first different number counts
          if (result == 0) {
            for (int i = 0; i < thisChunkLength; i++) {
              result = thisChunk.charAt(i) - thatChunk.charAt(i);
              if (result != 0) {
                return result;
              }
            }
          }
        } else {
          result = thisChunk.compareTo(thatChunk);
        }

        if (result != 0)
          return result;
      }

      return s1Length - s2Length;
    }
  };
}
