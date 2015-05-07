/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot;


import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.geocraft.ui.plot.attribute.TextProperties;


public interface ICornerRenderer {

  void render(final GC graphics, final Rectangle rectangle, final TextProperties textProperties);
}
