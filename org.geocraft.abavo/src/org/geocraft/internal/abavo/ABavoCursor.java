/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.abavo;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.geocraft.abavo.ABavoImages;
import org.geocraft.abavo.Activator;


/**
 * A utility class for creating ABAVO-specific cursors.
 */
public class ABavoCursor {

  private static Cursor _pencil;

  public static Cursor getPencil() {
    if (_pencil == null) {
      ImageDescriptor imageDesc = Activator.getDefault().createImageDescriptor(ABavoImages.PENCIL);
      _pencil = new Cursor(Display.getCurrent(), imageDesc.getImageData(), 0, 15);
    }
    return _pencil;
  }
}
