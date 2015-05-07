/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.core.common.util;




/**
 * Class for string utility functions
 */
public class StringUtil {

  /** Converts an array of strings into one string **/
  public static String arrayToString(final String[] a, final String separator) {
    StringBuffer result = new StringBuffer();

    if (a.length > 0) {
      result.append(a[0]);

      for (int i = 1; i < a.length; i++) {
        result.append(separator);
        result.append(a[i]);
      }
    }

    return result.toString();
  }

  public static boolean isStringInArray(final String findMe, final Object[] arrayOfStrings) {
    for (int i = 0; i < arrayOfStrings.length; ++i) {
      if (findMe.equals(arrayOfStrings[i].toString())) {
        return true;
      }
    }
    return false;
  }

  public static String stripString(final String stringThatNeedsStrip, final String substringToStrip) {
    if (stringThatNeedsStrip.endsWith(substringToStrip)) {
      int slen = stringThatNeedsStrip.length();
      return stringThatNeedsStrip.substring(0, slen - substringToStrip.length());
    }
    return stringThatNeedsStrip;
  }

  public static String wildcardToRegex(final String wildcard) {
    StringBuffer s = new StringBuffer(wildcard.length());
    s.append('^');
    for (int i = 0, is = wildcard.length(); i < is; i++) {
      char c = wildcard.charAt(i);
      switch (c) {
        case '*':
          s.append(".*");
          break;
        case '?':
          s.append(".");
          break;
        // escape special regexp-characters
        case '(':
        case ')':
        case '[':
        case ']':
        case '$':
        case '^':
        case '.':
        case '{':
        case '}':
        case '|':
        case '\\':
          s.append("\\");
          s.append(c);
          break;
        default:
          s.append(c);
          break;
      }
    }
    s.append('$');
    return s.toString();
  }

  /**
   * Compares a string with a search string. Returns true if the string contains the search string. false otherwise. The search string can contain wildcards * and ?.
   * @param a string to be searched.
   * @param searchString the search string.
   * @return true if the search string matches the file or directory name; false if not.
   */
  public static boolean compareStrings(final String string, final String searchString) {
    String search = wildcardToRegex(searchString);
    return string.matches(search);
  }
}