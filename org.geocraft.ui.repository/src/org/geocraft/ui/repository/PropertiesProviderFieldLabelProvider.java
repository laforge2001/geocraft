/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.ui.repository;


import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.geocraft.core.model.base.AbstractPropertiesProvider;
import org.geocraft.core.model.base.IPropertiesProvider;
import org.geocraft.ui.model.IModelSharedImages;
import org.geocraft.ui.model.ModelUI;


public class PropertiesProviderFieldLabelProvider implements ILabelProvider {

  private final IModelSharedImages _sharedImages = ModelUI.getSharedImages();

  public Image getImage(final Object element) {
    if (element != null && element instanceof AbstractPropertiesProvider) {
      return _sharedImages.getImage((IPropertiesProvider) element);
    }
    return null;
  }

  public String getText(final Object element) {
    if (element != null) {
      return element.toString();
    }
    return "";
  }

  public void addListener(final ILabelProviderListener listener) {
    // TODO Auto-generated method stub

  }

  public void dispose() {
    // TODO Auto-generated method stub

  }

  public boolean isLabelProperty(final Object element, final String property) {
    // TODO Auto-generated method stub
    return false;
  }

  public void removeListener(final ILabelProviderListener listener) {
    // TODO Auto-generated method stub

  }
}
