/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;


public class OrderedListFieldContentProvider implements IStructuredContentProvider {

  @Override
  public Object[] getElements(final Object inputElement) {
    return (Object[]) inputElement;
  }

  public void dispose() {
    // No action required.
  }

  @Override
  public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
    // No action required.
  }

}
