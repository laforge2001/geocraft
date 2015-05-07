///*
// * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
// */
//package org.geocraft.internal.ui.volumeviewer.toolbar;
//
//
//import org.eclipse.jface.action.Action;
//import org.eclipse.jface.action.ContributionItem;
//import org.eclipse.jface.action.ControlContribution;
//import org.eclipse.jface.action.IToolBarManager;
//import org.eclipse.jface.preference.ColorSelector;
//import org.eclipse.jface.util.IPropertyChangeListener;
//import org.eclipse.jface.util.PropertyChangeEvent;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.graphics.RGB;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Control;
//import org.geocraft.internal.ui.volumeviewer.canvas.ViewCanvasImplementor;
//import org.geocraft.ui.common.image.ISharedImages;
//import org.geocraft.ui.common.image.ImageRegistryUtil;
//import org.geocraft.ui.viewer.action.HelpAction;
//import org.geocraft.ui.volumeviewer.VolumeView;
//import org.geocraft.ui.volumeviewer.renderer.util.VolumeViewerHelper;
//
//
///**
// * The main 3d viewer toolbar.
// */
//public class MainVolumeToolbar {
//
//  /** The background color selector. */
//  private ColorSelector _backgroundColor;
//
//  /** The volume view implementor. */
//  private final ViewCanvasImplementor _viewImpl;
//
//  /** The volume view. */
//  private final VolumeView _view;
//
//  /** The cursor receive action. */
//  private Action _cursorReceive;
//
//  /**
//   * The constructor.
//   * @param view the volume view
//   * @param viewImpl the volume view implementor
//   */
//  public MainVolumeToolbar(final VolumeView view, final ViewCanvasImplementor viewImpl) {
//    _view = view;
//    _viewImpl = viewImpl;
//  }
//
//  /**
//   * Initialize the main toolbar.
//   * @param manager the view toolbar manager
//   */
//  public void initToolbar(final IToolBarManager manager) {
//    ContributionItem backgroundColorItem = new ControlContribution("") {
//
//      @Override
//      protected Control createControl(final Composite parent) {
//        _backgroundColor = new ColorSelector(parent);
//        _backgroundColor.setColorValue(new RGB(0, 0, 0));
//        _backgroundColor.addListener(new IPropertyChangeListener() {
//
//          @SuppressWarnings("unused")
//          public void propertyChange(final PropertyChangeEvent event) {
//            RGB selectedColor = _backgroundColor.getColorValue();
//            _viewImpl.setBackground(VolumeViewerHelper.rgbToColorRGBA(selectedColor, 1));
//          }
//        });
//        return _backgroundColor.getButton();
//      }
//    };
//    manager.add(backgroundColorItem);
//
//    final Action cursorBroadcast = new Action("", ImageRegistryUtil.getSharedImages().getImageDescriptor(
//        ISharedImages.IMG_SEND_CURSOR)) {
//
//      @Override
//      public void run() {
//        _view.addCursor(true, false);
//      }
//    };
//    cursorBroadcast.setToolTipText("Broadcast cursor location");
//    manager.add(cursorBroadcast);
//
//    _cursorReceive = new Action("", SWT.TOGGLE) {
//
//      @Override
//      public void run() {
//        _view.setCursorReceive(isChecked());
//        if (isChecked()) {
//          _cursorReceive.setToolTipText("Cursor receive is enabled");
//        } else {
//          _cursorReceive.setToolTipText("Cursor receive is disabled");
//        }
//      }
//    };
//    _cursorReceive.setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(
//        ISharedImages.IMG_RECEIVE_CURSOR));
//    _cursorReceive.setToolTipText("Cursor receive is disabled");
//    manager.add(_cursorReceive);
//
//    Action showList = new Action("", SWT.TOGGLE) {
//
//      @Override
//      public void run() {
//        _view.setEntityListVisible(isChecked());
//      }
//    };
//    showList.setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_PREFERENCES));
//    showList.setChecked(true);
//    manager.add(showList);
//    manager.add(new HelpAction("org.geocraft.ui.volumeviewer.volumeview"));
//  }
//
//}
