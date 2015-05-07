///*
// * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
// */
//package org.geocraft.internal.ui.volumeviewer.toolbar;
//
//
//import java.util.concurrent.Callable;
//
//import org.eclipse.jface.viewers.ComboViewer;
//import org.eclipse.jface.viewers.StructuredSelection;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.events.SelectionAdapter;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.layout.RowData;
//import org.eclipse.swt.layout.RowLayout;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Combo;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.Event;
//import org.eclipse.swt.widgets.Label;
//import org.eclipse.swt.widgets.Listener;
//import org.geocraft.core.model.datatypes.Domain;
//import org.geocraft.core.model.seismic.SeismicDataset;
//import org.geocraft.core.service.ServiceProvider;
//import org.geocraft.core.service.logging.ILogger;
//import org.geocraft.internal.ui.volumeviewer.canvas.Orientation;
//import org.geocraft.internal.ui.volumeviewer.canvas.ViewCanvasImplementor;
//import org.geocraft.internal.ui.volumeviewer.dialog.LightSettingsDialog;
//import org.geocraft.ui.common.image.ISharedImages;
//import org.geocraft.ui.common.image.ImageRegistryUtil;
//import org.geocraft.ui.volumeviewer.VolumeViewer;
//
//import com.ardor3d.scenegraph.Spatial;
//
//
///**
// * The volume viewer toolbar.
// */
//public class VolumeToolbar extends Composite {
//
//  /** The logger. */
//  private static ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(VolumeToolbar.class);
//
//  /** The volume canvas registry. */
//  private VolumeViewer _registry;
//
//  /** The volume view. */
//  private final ViewCanvasImplementor _viewImpl;
//
//  /** The domain combo. */
//  private ComboViewer _domainCombo;
//
//  /**
//   * The constructor.
//   * @param parent the parent composite
//   * @param style the style
//   * @param viewImpl the view canvas implementor
//   */
//  public VolumeToolbar(final Composite parent, final int style, final ViewCanvasImplementor viewImpl) {
//    super(parent, style);
//    _viewImpl = viewImpl;
//    init();
//  }
//
//  /**
//   * Set the volume canvas.
//   * @param registry the volume canvas registry
//   */
//  public void setCanvasRegistry(final VolumeViewer registry) {
//    _registry = registry;
//  }
//
//  public void setDomain(final Domain domain) {
//    Display.getDefault().asyncExec(new Runnable() {
//
//      public void run() {
//        _domainCombo.setSelection(new StructuredSelection(domain));
//      }
//    });
//  }
//
//  /**
//   * Initialize the toolbar.
//   */
//  private void init() {
//    final RowLayout layout = new RowLayout(SWT.HORIZONTAL);
//    layout.spacing = 5;
//    setLayout(layout);
//
//    final Button viewFocusBtn = new Button(this, SWT.TOGGLE);
//    viewFocusBtn.setImage(ImageRegistryUtil.getSharedImages().getImage(ISharedImages.IMG_ANCHOR));
//    viewFocusBtn.setToolTipText("Focus point select is disabled");
//    final Listener viewFocusListener = new Listener() {
//
//      @Override
//      @SuppressWarnings("unused")
//      public void handleEvent(final Event event) {
//        if (viewFocusBtn.getSelection()) {
//          viewFocusBtn.setToolTipText("MB3 click on canvas to set the focus point");
//          viewFocusBtn.setGrayed(true);
//          _viewImpl.getInput().armFocusTrigger(new Callable<Object>() {
//
//            public Object call() throws Exception {
//              viewFocusBtn.setToolTipText("Focus point select is disabled");
//              viewFocusBtn.setSelection(false);
//              viewFocusBtn.setGrayed(false);
//              return null;
//            }
//          });
//        }
//      }
//    };
//    viewFocusBtn.addListener(SWT.Selection, viewFocusListener);
//
//    Label label = new Label(this, SWT.NULL);
//    label.setText("Domain");
//    _domainCombo = new ComboViewer(this, SWT.READ_ONLY);
//    _domainCombo.add(new Domain[] { Domain.DISTANCE, Domain.TIME });
//    _domainCombo.getCombo().select(0);
//    final Listener domainListener = new Listener() {
//
//      @Override
//      @SuppressWarnings("unused")
//      public void handleEvent(final Event event) {
//        final Domain domain = (Domain) ((StructuredSelection) _domainCombo.getSelection()).getFirstElement();
//        _registry.setCurrentDomain(domain);
//      }
//    };
//    _domainCombo.getCombo().addListener(SWT.Selection, domainListener);
//
//    final Button homeBtn = new Button(this, SWT.PUSH);
//    homeBtn.setImage(ImageRegistryUtil.getSharedImages().getImage(ISharedImages.IMG_HOME));
//    homeBtn.setToolTipText("Switch orientation to Map View");
//    final Listener orListener = new Listener() {
//
//      @Override
//      @SuppressWarnings("unused")
//      public void handleEvent(final Event event) {
//        _viewImpl.setViewFocus(Orientation.MAP_VIEW);
//        final Spatial[] spatial = null;
//        _viewImpl.centerOnSpatial(Orientation.MAP_VIEW, spatial);
//      }
//    };
//    homeBtn.addListener(SWT.Selection, orListener);
//
//    final Button lightBtn = new Button(this, SWT.PUSH);
//    lightBtn.setImage(ImageRegistryUtil.getSharedImages().getImage(ISharedImages.IMG_LIGHT));
//    lightBtn.setToolTipText("Open the Sun light settings");
//    final Listener lightListener = new Listener() {
//
//      @Override
//      @SuppressWarnings("unused")
//      public void handleEvent(final Event event) {
//        new LightSettingsDialog(_registry).open();
//      }
//    };
//    lightBtn.addListener(SWT.Selection, lightListener);
//
//    final Button parallelBtn = new Button(this, SWT.TOGGLE);
//    parallelBtn.setImage(ImageRegistryUtil.getSharedImages().getImage(ISharedImages.IMG_VOLUME));
//    parallelBtn.setToolTipText("Change to orthographic projection");
//    final Listener parallelListener = new Listener() {
//
//      @Override
//      @SuppressWarnings("unused")
//      public void handleEvent(final Event event) {
//        _viewImpl.setUsePerspective(!parallelBtn.getSelection());
//        if (parallelBtn.getSelection()) {
//          parallelBtn.setToolTipText("Change to perspective projection");
//          parallelBtn.setImage(ImageRegistryUtil.getSharedImages().getImage(ISharedImages.IMG_ORTHOGRAPHIC));
//        } else {
//          parallelBtn.setToolTipText("Change to orthographic projection");
//          parallelBtn.setImage(ImageRegistryUtil.getSharedImages().getImage(ISharedImages.IMG_VOLUME));
//        }
//      }
//    };
//    parallelBtn.addListener(SWT.Selection, parallelListener);
//
//    final Button pickPosBtn = new Button(this, SWT.TOGGLE);
//    pickPosBtn.setImage(ImageRegistryUtil.getSharedImages().getImage(ISharedImages.IMG_POS_SIZE));
//    pickPosBtn.setToolTipText("The display of the pick location is disabled");
//    final Listener pickPosListener = new Listener() {
//
//      @Override
//      @SuppressWarnings("unused")
//      public void handleEvent(final Event event) {
//        _viewImpl.setShowPickPos(pickPosBtn.getSelection());
//        if (pickPosBtn.getSelection()) {
//          pickPosBtn.setToolTipText("The display of the pick location is enabled");
//        } else {
//          pickPosBtn.setToolTipText("The display of the pick location is disabled");
//        }
//      }
//    };
//    pickPosBtn.addListener(SWT.Selection, pickPosListener);
//
//    label = new Label(this, SWT.NULL);
//    label.setText("Exaggeration");
//    final Combo exCombo = new Combo(this, SWT.NONE);
//    final RowData data = new RowData();
//    data.width = 50;
//    exCombo.setLayoutData(data);
//    exCombo.setItems(new String[] { ".1", ".25", ".5", "1", "2", "4", "10" });
//    exCombo.select(3);
//    final Listener exListener = new Listener() {
//
//      @Override
//      @SuppressWarnings("unused")
//      public void handleEvent(final Event event) {
//        try {
//          if (exCombo.getText().trim().length() > 0) {
//            final float exagValue = Float.parseFloat(exCombo.getText());
//            if (exagValue > 0 && exagValue <= 100) {
//              _registry.setExaggeration(exagValue);
//              _viewImpl.setExaggeration(exagValue);
//              final Spatial[] nodes = _registry.getNodes();
//              for (final Spatial node : nodes) {
//                final AbstractRenderer renderer = _registry.getRendererForNode(node);
//                if (renderer != null && renderer.getEntity() instanceof SeismicDataset) {
//                  renderer.refresh();
//                }
//              }
//            } else {
//              LOGGER.warn("Exaggeration value should be in the (0, 100] range");
//            }
//          }
//        } catch (final Exception e) {
//          LOGGER.warn("Invalid exaggeration value " + exCombo.getText());
//        }
//      }
//
//    };
//    exCombo.addListener(SWT.Modify, exListener);
//
//    final Button deleteBtn = new Button(this, SWT.PUSH);
//    deleteBtn.setText("Delete");
//    deleteBtn.setToolTipText("Delete the selected entity from the viewer");
//    deleteBtn.addSelectionListener(new SelectionAdapter() {
//
//      @Override
//      @SuppressWarnings("unused")
//      public void widgetSelected(final SelectionEvent e) {
//        _registry.removeSelectedNodes();
//      }
//    });
//
//    final Button clearBtn = new Button(this, SWT.PUSH);
//    clearBtn.setText("Clear all");
//    clearBtn.setToolTipText("Delete all entities from the viewer");
//    clearBtn.addSelectionListener(new SelectionAdapter() {
//
//      @Override
//      @SuppressWarnings("unused")
//      public void widgetSelected(final SelectionEvent e) {
//        _registry.clearAll();
//      }
//    });
//
//  }
//}
