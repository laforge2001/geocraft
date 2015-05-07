package org.geocraft.ui.sectionviewer.component;


public interface ITraceRenderStyle {

  /** Constant for wiggle trace rendering. */
  public static final int WIGGLE_TRACE = 1;

  /** Constant for positive color fill rendering. */
  public static final int POSITIVE_COLOR_FILL = 2;

  /** Constant for negative color fill rendering. */
  public static final int NEGATIVE_COLOR_FILL = 4;

  /** Constant for positive density fill rendering. */
  public static final int POSITIVE_DENSITY_FILL = 8;

  /** Constant for negative density fill rendering. */
  public static final int NEGATIVE_DENSITY_FILL = 16;

  /** Constant for variable density rendering. */
  public static final int VARIABLE_DENSITY = 32;

  /** Constant for positive & negative color fill rendering. */
  public static final int COLOR_FILL = POSITIVE_COLOR_FILL | NEGATIVE_COLOR_FILL;

  /** Constant for positive & negative density fill rendering. */
  public static final int DENSITY_FILL = POSITIVE_DENSITY_FILL | NEGATIVE_DENSITY_FILL;

}
