package org.geocraft.ui.viewer;


import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;


public class ViewerPerspective implements IPerspectiveFactory {

  public void createInitialLayout(final IPageLayout layout) {
    layout.setEditorAreaVisible(false);
    IPlaceholderFolderLayout viewers = layout.createPlaceholderFolder("viewers", IPageLayout.RIGHT, 0, layout
        .getEditorArea());
    viewers.addPlaceholder("org.geocraft.ui.map*.MapView*:*");
    viewers.addPlaceholder("org.geocraft.ui.volume*.VolumeView*:*");
    viewers.addPlaceholder("org.geocraft.ui.sectionviewer.SectionView*:*");
  }

}
