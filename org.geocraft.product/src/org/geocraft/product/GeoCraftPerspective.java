/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.product;


import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;


public class GeoCraftPerspective implements IPerspectiveFactory {

  public void createInitialLayout(final IPageLayout layout) {
    String editorArea = layout.getEditorArea();
    layout.setEditorAreaVisible(false);
    IFolderLayout bottomRight = layout.createFolder("logging", IPageLayout.BOTTOM, 0.65f, editorArea);
    bottomRight.addView("org.eclipse.pde.runtime.LogView");
    IFolderLayout left = layout.createFolder("properties", IPageLayout.LEFT, 0.3f, "logging");
    left.addView("org.geocraft.ui.property.PropertiesView");
    IFolderLayout topLeft = layout.createFolder("repository", IPageLayout.LEFT, 0.3f, editorArea);
    topLeft.addView("org.geocraft.ui.repository.RepositoryView");
  }
}
