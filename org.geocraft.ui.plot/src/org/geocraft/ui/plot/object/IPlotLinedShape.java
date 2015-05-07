/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.ui.plot.object;


import org.eclipse.swt.graphics.RGB;
import org.geocraft.ui.plot.attribute.LineProperties;
import org.geocraft.ui.plot.defs.LineStyle;


public interface IPlotLinedShape extends IPlotMovableShape {

  /**
   * Gets the line properties.
   * @return the line properties.
   */
  LineProperties getLineProperties();

  LineStyle getLineStyle();

  RGB getLineColor();

  int getLineWidth();

  void setLineStyle(final LineStyle style);

  void setLineColor(final RGB rgb);

  void setLineWidth(final int width);
}
