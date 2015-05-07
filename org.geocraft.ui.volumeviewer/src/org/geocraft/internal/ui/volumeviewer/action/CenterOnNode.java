///*
// * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
// */
//package org.geocraft.internal.ui.volumeviewer.action;
//
//
//import org.eclipse.jface.action.IAction;
//import org.eclipse.jface.viewers.ISelection;
//import org.eclipse.jface.viewers.StructuredSelection;
//import org.eclipse.ui.IActionDelegate;
//import org.eclipse.ui.IWorkbenchPart;
//import org.eclipse.ui.PlatformUI;
//import org.geocraft.core.model.Entity;
//import org.geocraft.ui.repository.PropertiesProviderTreeObject;
//import org.geocraft.ui.volumeviewer.VolumeCanvasRegistry;
//import org.geocraft.ui.volumeviewer.VolumeView;
//
//import com.ardor3d.scenegraph.Spatial;
//
//
///**
// * An action to be used to center the camera on the selected node.
// */
//public class CenterOnNode implements IActionDelegate {
//
//  /** The selected spatial node. */
//  private Spatial _selectedNode;
//
//  /** The volume registry. */
//  private VolumeCanvasRegistry _registry;
//
//  @Override
//  @SuppressWarnings("unused")
//  public void run(final IAction action) {
//    if (_registry != null && _selectedNode != null) {
//      _registry.centerOnSpatial(_selectedNode);
//    }
//  }
//
//  @Override
//  public void selectionChanged(final IAction action, final ISelection selection) {
//    Object selectedElement = ((StructuredSelection) selection).getFirstElement();
//    if (selectedElement instanceof PropertiesProviderTreeObject
//        && ((PropertiesProviderTreeObject) selectedElement).getPropertiesProvider() instanceof Entity) {
//      Entity selectedEntity = (Entity) ((PropertiesProviderTreeObject) selectedElement).getPropertiesProvider();
//      IWorkbenchPart activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
//      if (activePart instanceof VolumeView) {
//        VolumeView view = (VolumeView) activePart;
//        _registry = view.getRegistry();
//        _selectedNode = _registry.getNodeForEntity(selectedEntity);
//        if (_selectedNode == null) {
//          action.setEnabled(false);
//        }
//      } else {
//        action.setEnabled(false);
//      }
//    }
//  }
//
//}
