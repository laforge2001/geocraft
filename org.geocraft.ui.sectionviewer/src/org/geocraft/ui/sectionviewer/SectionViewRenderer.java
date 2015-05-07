/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer;


import org.eclipse.swt.widgets.Display;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.ui.plot.AbstractRenderer;
import org.geocraft.ui.plot.RendererViewLayer;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.viewer.layer.IViewLayer;


/**
 * Defines the abstract base class for section view renderers.
 * The schema for the <code>org.geocraft.ui.sectionviewer.renderer</code>
 * extension point requires that all section view renderers extend
 * this class.
 */
public abstract class SectionViewRenderer extends AbstractRenderer<ISectionViewer> {

  public SectionViewRenderer(final String name) {
    super(name);
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof SectionViewRenderer && ((SectionViewRenderer) object).getRenderedObjects().length > 0) {
      Object[] thisRenderedObjects = getRenderedObjects();
      Object[] thatRenderedObjects = ((SectionViewRenderer) object).getRenderedObjects();
      if (thisRenderedObjects.length != thatRenderedObjects.length) {
        return false;
      }
      for (int i = 0; i < thisRenderedObjects.length; i++) {
        if (!thisRenderedObjects[i].equals(thatRenderedObjects[i])) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  @Override
  public ReadoutInfo getReadoutInfo(final double x, final double y) {
    int traceNum = Math.round((float) x);
    float z = (float) y;
    return getReadoutInfo(traceNum, z);
  }

  /**
   * Returns the cursor readout information for the renderer.
   * @param trace the cursor trace (x) coordinate.
   * @param z the the cursor z (y) coordinate.
   * @return the cursor readout information; or <i>null</i> if no readout info available.
   */
  public abstract ReadoutInfo getReadoutInfo(final int traceNum, final float z);

  /**
   * Set the values to be used for the current display.
   * By default this does nothing, so subclasses need to override this method
   * to provide their own functionality.
   * 
   * @param position the current display position
   */
  public void setCurrentPosition(final float[] position) {
    // To be implemented by the subclassed renderers.
  }

  /**
   * Adds the renderer to the layered model tree.
   * This method first adds the parent object and then insert the renderer
   * underneath it in the layered model. If the parent object is <i>null</i>,
   * then it simply attempts to add the renderer under the folder layer
   * specified by the folder name.
   * 
   * @param viewer the section viewer in which to add the renderer.
   * @param parentObject the parent object to add before this renderer.
   * @param parentObjectName the name of the parent object to add.
   * @param folderName the name of the folder layer.
   */
  @Override
  protected void addToLayerTree(final Object parentObject, final String parentObjectName, final String folderName,
      final boolean autoUpdate) {
    Display.getDefault().syncExec(new Runnable() {

      public void run() {
        ISectionViewer viewer = getViewer();

        // First add the parent object.
        if (parentObject != null) {
          viewer.addObjects(new Object[] { parentObject }, true);
        }

        Object renderedObject = getRenderedObjects()[0];

        // Check if the entity is the primary seismic dataset.
        SeismicDataset referenceDataset = viewer.getReferenceDataset();
        boolean isPrimaryDataset = false;
        if (referenceDataset == null) {
          isPrimaryDataset = true;
        } else {
          isPrimaryDataset = referenceDataset.equals(renderedObject);
        }

        // Only allow layer remove is the entity is not the primary seismic dataset.
        boolean allowsRemove = !isPrimaryDataset;

        // Create a view layer for this renderer.
        RendererViewLayer viewLayer = new RendererViewLayer(SectionViewRenderer.this, false, false, allowsRemove);

        // Add the layer to the viewer.
        if (renderedObject instanceof SeismicDataset) {
          IModelSpace modelSpace = viewer.getPlot().getActiveModelSpace();
          modelSpace.addLayer(viewLayer.getPlotLayer(), isPrimaryDataset);
        } else {
          viewer.getPlot().addLayer(viewLayer.getPlotLayer(), false, false);
        }

        // Attempt to find the parent layer in the layered model.
        IViewLayer[] layers = viewer.getLayerModel().getChildren(viewer.findFolderLayer(folderName));
        IViewLayer parentLayer = null;
        for (int k = 0; k < layers.length && parentLayer == null; k++) {
          if (layers[k].getName().equals(parentObjectName)) {
            parentLayer = layers[k];
          }
        }

        // If no parent layer found, default to a top-level folder layer.
        if (parentLayer == null) {
          parentLayer = viewer.findFolderLayer(folderName);
        }

        // Add the layer to the layered model.
        viewer.getLayerModel().addLayer(viewLayer, parentLayer);
        //        viewLayer.addListener(new ViewLayerListener() {
        //
        //          @Override
        //          public void viewLayerUpdated(final ViewLayerEvent event) {
        //            EventType eventType = event.getEventType();
        //            System.out.println("SectionViewRenderer.viewLayerUpdated(event=" + eventType);
        //            if (eventType.equals(EventType.LAYER_REMOVED)) {
        //              Object[] objects = getRenderedObjects();
        //              for (Object obj : objects) {
        //                System.out.println("...removing " + obj);
        //              }
        //              _viewer.removeObjects(objects);
        //            }
        //          }
        //
        //        });
      }
    });
  }
}
