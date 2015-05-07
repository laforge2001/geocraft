///*
// * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
// */
//package org.geocraft.internal.ui.volumeviewer.action;
//
//
//import org.eclipse.jface.action.IAction;
//import org.eclipse.jface.dialogs.Dialog;
//import org.eclipse.jface.viewers.ISelection;
//import org.eclipse.jface.viewers.StructuredSelection;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.graphics.Point;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.ui.IActionDelegate;
//import org.eclipse.ui.IWorkbenchPart;
//import org.eclipse.ui.PlatformUI;
//import org.geocraft.core.model.Entity;
//import org.geocraft.ui.repository.PropertiesProviderTreeObject;
//import org.geocraft.ui.volumeviewer.VolumeCanvasRegistry;
//import org.geocraft.ui.volumeviewer.VolumeView;
//import org.geocraft.ui.volumeviewer.renderer.AbstractRenderer;
//
//
///**
// * An action to be used to open the settings dialog for the selected entity.
// */
//public class NodeSettings implements IActionDelegate {
//
//  /** The selected spatial renderer. */
//  private AbstractRenderer _selectedRenderer;
//
//  /** The volume registry. */
//  private VolumeCanvasRegistry _registry;
//
//  @Override
//  @SuppressWarnings("unused")
//  public void run(final IAction action) {
//    if (_registry != null && _selectedRenderer != null) {
//      final Dialog dialog = _selectedRenderer.getSettingsDialog();
//      if (dialog.getShell() == null || dialog.getShell().isDisposed()) {
//        dialog.create();
//        dialog.getShell().pack();
//        Point size = dialog.getShell().computeSize(SWT.DEFAULT, 600);
//        dialog.getShell().setSize(size);
//      }
//      // we are already on the SWT thread, but we ensure this way that setActive() will execute after open()
//      Display.getDefault().asyncExec(new Runnable() {
//
//        public void run() {
//          dialog.getShell().setActive();
//        }
//      });
//      dialog.open();
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
//        _selectedRenderer = _registry.getRendererForEntity(selectedEntity);
//        if (_selectedRenderer == null || _selectedRenderer.getSettingsDialog() == null) {
//          action.setEnabled(false);
//        }
//      } else {
//        action.setEnabled(false);
//      }
//    }
//  }
//
//}
