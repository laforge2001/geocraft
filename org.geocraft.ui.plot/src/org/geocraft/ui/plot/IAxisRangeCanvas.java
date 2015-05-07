/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot;


import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.ScrollBar;


public interface IAxisRangeCanvas extends IAxisCanvas {

  void scrolled(ScrollBar scrollBar);

  void setRenderer(IAxisRangeRenderer renderer);

  Point getSize();
}