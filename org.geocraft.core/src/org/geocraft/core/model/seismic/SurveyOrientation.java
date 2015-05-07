/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.seismic;


public enum SurveyOrientation {
  ROW_IS_INLINE("RowIsInline"),
  ROW_IS_XLINE("RowIsXline");

  private String _text;

  SurveyOrientation(final String text) {
    _text = text;
  }

  @Override
  public String toString() {
    return _text;
  }

  public static SurveyOrientation lookupByName(final String orientationStr) {
    for (SurveyOrientation orientation : SurveyOrientation.values()) {
      if (orientation.toString().equals(orientationStr)) {
        return orientation;
      }
    }
    return null;
  }
}
