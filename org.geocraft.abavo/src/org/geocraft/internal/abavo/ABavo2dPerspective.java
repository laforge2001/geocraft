/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.abavo;


import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.geocraft.abavo.crossplot.CrossplotAvsB2d;
import org.geocraft.algorithm.StandaloneAlgorithmRegistry;


public class ABavo2dPerspective implements IPerspectiveFactory {

  public void createInitialLayout(final IPageLayout layout) {
    String editorArea = layout.getEditorArea();
    layout.setEditorAreaVisible(true);

    IFolderLayout topLeft = layout.createFolder("repository", IPageLayout.LEFT, 0.3f, editorArea);
    topLeft.addView("org.geocraft.ui.repository.RepositoryView");
    IFolderLayout left = layout.createFolder("properties", IPageLayout.BOTTOM, 0.3f, "repository");
    left.addView("org.geocraft.ui.property.PropertiesView");
    IFolderLayout bottomRight = layout.createFolder("logging", IPageLayout.BOTTOM, 0.5f, "properties");
    bottomRight.addView("org.eclipse.pde.runtime.LogView");
    IFolderLayout right = layout.createFolder("abavo", IPageLayout.RIGHT, 0.5f, editorArea);
    right.addView("org.geocraft.abavo.abavoCrossplotViewPart");

    try {
      IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
          StandaloneAlgorithmRegistry.getInstance().getEditorInput(CrossplotAvsB2d.class.getName()),
          "org.geocraft.algorithm.StandaloneAlgorithmEditor");
      //PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
      //    AlgorithmRegistry.getInstance().getEditorInput(new GenerateClassVolumeModel()),
      //    "org.geocraft.geomath.forms.AlgorithmFormEditor");
      PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(part);
    } catch (PartInitException e) {
      e.printStackTrace();
    }

    //right.addView("org.geocraft.geomath.views.AlgorithmsView");
  }
}
