/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.abavo.ellipse;


import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.geocraft.abavo.ellipse.EllipseModel;


public class EllipseTableLabelProvider implements ITableLabelProvider {

  public EllipseTableLabelProvider() {
    // No action required.
  }

  public Image getColumnImage(final Object element, final int columnIndex) {
    return null;
  }

  public String getColumnText(final Object element, final int columnIndex) {
    EllipseModel ellipse = (EllipseModel) element;
    return ellipse.getText(columnIndex);
  }

  public boolean isLabelProperty(final Object element, final String property) {
    return false;
  }

  public void addListener(final ILabelProviderListener listener) {
    //throw new UnsupportedOperationException("Cannot add/remove listeners to " + getClass().getSimpleName());
  }

  public void removeListener(final ILabelProviderListener listener) {
    //throw new UnsupportedOperationException("Cannot add/remove listeners to " + getClass().getSimpleName());
  }

  public void dispose() {
    // No action required.
  }
}
