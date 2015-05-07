/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;


public class OrderedListFieldLabelProvider extends LabelProvider {

  @Override
  public Image getImage(final Object element) {
    return null;
  }

  @Override
  public String getText(final Object element) {
    return element.toString();
  }

}
