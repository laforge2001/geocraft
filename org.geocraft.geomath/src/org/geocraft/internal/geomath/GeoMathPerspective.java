/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.internal.geomath;


import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;


public class GeoMathPerspective implements IPerspectiveFactory {

  public void createInitialLayout(final IPageLayout layout) {
    String editorArea = layout.getEditorArea();
    layout.setEditorAreaVisible(true);
    IFolderLayout bottomRight = layout.createFolder("logging", IPageLayout.BOTTOM, 0.65f, editorArea);
    bottomRight.addView("org.eclipse.pde.runtime.LogView");
    //bottomRight.addView("org.geocraft.ui.ijython.IJythonView");
    IFolderLayout left = layout.createFolder("properties", IPageLayout.LEFT, 0.25f, "logging");
    left.addView("org.geocraft.ui.property.PropertiesView");
    IFolderLayout topLeft = layout.createFolder("repository", IPageLayout.LEFT, 0.25f, editorArea);
    topLeft.addView("org.geocraft.ui.repository.RepositoryView");
    IFolderLayout right = layout.createFolder("geomath_algorithms", IPageLayout.RIGHT, 0.75f, editorArea);
    right.addView("org.geocraft.geomath.views.AlgorithmsView");

    IPlaceholderFolderLayout viewers = layout.createPlaceholderFolder("viewers", IPageLayout.RIGHT, 0.5f, editorArea);
    viewers.addPlaceholder("org.geocraft.ui.map*.MapView*:*");
    viewers.addPlaceholder("org.geocraft.ui.volume*.VolumeView*:*");
    viewers.addPlaceholder("org.geocraft.ui.sectionviewer.SectionView*:*");
  }
}
