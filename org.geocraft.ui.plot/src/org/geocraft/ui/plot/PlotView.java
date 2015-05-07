/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot;


import java.text.NumberFormat;
import java.util.Iterator;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.geocraft.ui.plot.action.IPlotMouseAction;
import org.geocraft.ui.plot.action.PlotMouseActionList;
import org.geocraft.ui.plot.defs.AxisDirection;
import org.geocraft.ui.plot.defs.AxisScale;
import org.geocraft.ui.plot.event.ModelSpaceEvent;
import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.plot.layout.CanvasLayoutModel;
import org.geocraft.ui.plot.listener.IPlotListener;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.model.ModelSpaceBounds;
import org.geocraft.ui.viewer.IRenderer;
import org.geocraft.ui.viewer.RendererSpecification;
import org.geocraft.ui.viewer.layer.ILayeredModel;
import org.geocraft.ui.viewer.layer.IViewLayer;
import org.geocraft.ui.viewer.layer.LayeredModel;
import org.geocraft.ui.viewer.layer.LayeredModelContentProvider;
import org.geocraft.ui.viewer.layer.LayeredModelLabelProvider;
import org.geocraft.ui.viewer.toolbar.SharedToolBar;
import org.geocraft.ui.viewer.toolbar.SimpleToolBar;
import org.geocraft.ui.viewer.tree.ReadoutPanel;


/**
 * The basic implementation of a viewer based on an underlying plot.
 * This class can be extended to create more specialized types of
 * viewers (maps, sections, etc).
 */
public abstract class PlotView extends Composite implements IPlotViewer {

  /** The underlying plot. */
  protected final IPlot _plot;

  /** The composite container for tool bars. */
  private final Composite _toolBarContainer;

  /** The shared tool bar common to all viewers. */
  private final SharedToolBar _sharedToolBar;

  /** The sash form container. */
  private final SashForm _sashForm;

  /** The layered model tree viewer. */
  protected final CheckboxTreeViewer _layerViewer;

  protected ILayeredModel _layerModel;

  /** The tab folder panel. */
  protected TabFolder _mainFolder;

  /** The tab to display the readout data. */
  protected TabItem _readoutTab;

  /** The panel for displaying layer info based on the cursor location. */
  protected ReadoutPanel _readoutPanel;

  /**
   * Constructs a viewer based on an underlying plot.
   * @param parent the parent composite.
   * @param plotTitle the title of the underlying plot.
   * @param modelSpace the default model space of the plot.
   * @param scrolled the scrolling option for the plot.
   * @param readout if readout panel supported
   * @param broadcastReceive if broadcast messages enabling button is in the shared toolbar
   */
  public PlotView(final Composite parent, final String plotTitle, final IModelSpace modelSpace, final PlotScrolling scrolled, final boolean readout, final boolean broadcastReceive) {
    super(parent, SWT.NONE);
    // Set the viewer layout.
    GridLayout layout = new GridLayout();
    layout.makeColumnsEqualWidth = false;
    layout.numColumns = 2;
    layout.horizontalSpacing = 1;
    layout.verticalSpacing = 1;

    _toolBarContainer = new Composite(this, SWT.NONE);
    GridData layoutData = new GridData();
    layoutData.grabExcessHorizontalSpace = true;
    layoutData.grabExcessVerticalSpace = false;
    layoutData.horizontalAlignment = SWT.FILL;
    layoutData.horizontalSpan = 2;
    layoutData.verticalAlignment = SWT.FILL;
    _toolBarContainer.setLayoutData(layoutData);
    layout = new GridLayout();
    layout.makeColumnsEqualWidth = true;
    layout.numColumns = 1;
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    layout.horizontalSpacing = 0;
    layout.verticalSpacing = 0;
    _toolBarContainer.setLayout(layout);

    _sashForm = new SashForm(this, SWT.HORIZONTAL);
    layoutData = new GridData();
    layoutData.grabExcessHorizontalSpace = true;
    layoutData.grabExcessVerticalSpace = true;
    layoutData.horizontalAlignment = SWT.FILL;
    layoutData.horizontalSpan = 2;
    layoutData.verticalAlignment = SWT.FILL;
    _sashForm.setLayoutData(layoutData);

    // Create the underlying plot.
    _plot = new Plot(_sashForm, modelSpace, scrolled);
    _plot.setTitle(plotTitle);

    // Create the tree view of the layer model.
    if (readout) {
      _mainFolder = new TabFolder(_sashForm, SWT.TOP);
      TabItem entityTab = new TabItem(_mainFolder, SWT.NONE);
      entityTab.setText("Layers");
      _readoutTab = new TabItem(_mainFolder, SWT.NONE);
      _readoutTab.setText("Readout");
      _layerViewer = new CheckboxTreeViewer(_mainFolder);
      entityTab.setControl(_layerViewer.getTree());

      _readoutPanel = new ReadoutPanel(_mainFolder, SWT.NONE);
      _readoutTab.setControl(_readoutPanel);
    } else {
      _layerViewer = new CheckboxTreeViewer(_sashForm);
    }
    _layerViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);

    setLayout(layout);

    // Create the layer model.
    _layerModel = initializeLayeredModel(_layerViewer);

    _toolBarContainer.setBackground(new Color(null, 255, 0, 0));
    _toolBarContainer.getParent().setBackground(new Color(null, 255, 255, 0));

    _plot.getModelSpaceCanvas().removeCursorListener(_plot);
    _plot.getModelSpaceCanvas().addCursorListener(this);

    _plot.getModelSpaceCanvas().getComposite().addKeyListener(new KeyListener() {

      public void keyPressed(KeyEvent e) {
        IModelSpace activeModelSpace = _plot.getActiveModelSpace();
        if (e.keyCode == SWT.ARROW_UP) {
          if (activeModelSpace.getAxisY().getScale() == AxisScale.LOG) {
            MessageDialog.openError(getShell(), "Shift Error",
                "Shifting of a logarithmic axis not currently supported.");
            return;
          }
          ModelSpaceBounds bounds = activeModelSpace.getViewableBounds();
          double yStart = bounds.getStartY();
          double yEnd = bounds.getEndY();
          double dist = yEnd - yStart;
          double shift = dist / 4;
          double sign = -1;
          AxisDirection direction = activeModelSpace.getAxisY().getDirection();
          if (direction.equals(AxisDirection.BOTTOM_TO_TOP)) {
            sign = 1;
          }
          yStart += shift * sign;
          yEnd += shift * sign;
          activeModelSpace.setViewableBounds(bounds.getStartX(), bounds.getEndX(), yStart, yEnd);
        } else if (e.keyCode == SWT.ARROW_DOWN) {
          if (activeModelSpace.getAxisY().getScale() == AxisScale.LOG) {
            MessageDialog.openError(getShell(), "Shift Error",
                "Shifting of a logarithmic axis not currently supported.");
            return;
          }
          ModelSpaceBounds bounds = activeModelSpace.getViewableBounds();
          double yStart = bounds.getStartY();
          double yEnd = bounds.getEndY();
          double dist = yEnd - yStart;
          double shift = dist / 4;
          double sign = 1;
          AxisDirection direction = activeModelSpace.getAxisY().getDirection();
          if (direction.equals(AxisDirection.BOTTOM_TO_TOP)) {
            sign = -1;
          }
          yStart += shift * sign;
          yEnd += shift * sign;
          activeModelSpace.setViewableBounds(bounds.getStartX(), bounds.getEndX(), yStart, yEnd);
        } else if (e.keyCode == SWT.ARROW_LEFT) {
          if (activeModelSpace.getAxisX().getScale() == AxisScale.LOG) {
            MessageDialog.openError(getShell(), "Shift Error",
                "Shifting of a logarithmic axis not currently supported.");
            return;
          }
          ModelSpaceBounds bounds = activeModelSpace.getViewableBounds();
          double xStart = bounds.getStartX();
          double xEnd = bounds.getEndX();
          double dist = xEnd - xStart;
          double shift = dist / 4;
          double sign = 1;
          AxisDirection direction = activeModelSpace.getAxisX().getDirection();
          if (direction.equals(AxisDirection.LEFT_TO_RIGHT)) {
            sign = -1;
          }
          xStart += shift * sign;
          xEnd += shift * sign;
          activeModelSpace.setViewableBounds(xStart, xEnd, bounds.getStartY(), bounds.getEndY());
        } else if (e.keyCode == SWT.ARROW_RIGHT) {
          if (activeModelSpace.getAxisX().getScale() == AxisScale.LOG) {
            MessageDialog.openError(getShell(), "Shift Error",
                "Shifting of a logarithmic axis not currently supported.");
            return;
          }
          ModelSpaceBounds bounds = activeModelSpace.getViewableBounds();
          double xStart = bounds.getStartX();
          double xEnd = bounds.getEndX();
          double dist = xEnd - xStart;
          double shift = dist / 4;
          double sign = -1;
          AxisDirection direction = activeModelSpace.getAxisX().getDirection();
          if (direction.equals(AxisDirection.LEFT_TO_RIGHT)) {
            sign = 1;
          }
          xStart += shift * sign;
          xEnd += shift * sign;
          activeModelSpace.setViewableBounds(xStart, xEnd, bounds.getStartY(), bounds.getEndY());
        } else if (e.character == ' ') {
          TreeSelection selection = (TreeSelection) _layerViewer.getSelection();
          Iterator iterator = selection.iterator();
          while (iterator.hasNext()) {
            Object object = iterator.next();
            if (object instanceof IViewLayer) {
              IViewLayer layer = (IViewLayer) object;
              boolean visible = !layer.isVisible();
              layer.setVisible(visible);
              _layerViewer.setChecked(layer, visible);
              updateAll();
            }
          }
        }

      }

      public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub

      }

    });

    // Create the shared tool bar.
    _sharedToolBar = new SharedToolBar(_toolBarContainer, this, true, true, broadcastReceive);

    setLayerTreeVisible(true);
  }

  public IPlot getPlot() {
    return _plot;
  }

  public PlotComposite getPlotComposite() {
    return _plot.getPlotComposite();
  }

  public Composite getComposite() {
    return this;
  }

  public ILayeredModel getLayerModel() {
    return _layerModel;
  }

  public TreeViewer getLayerViewer() {
    return _layerViewer;
  }

  public SharedToolBar getSharedToolBar() {
    return _sharedToolBar;
  }

  public SimpleToolBar addCustomToolBar() {
    return new SimpleToolBar(_toolBarContainer);
  }

  public void addLayerToRoot(final IViewLayer layer) {
    _layerModel.addLayer(layer);
  }

  public void removeLayer(final IViewLayer layer) {
    _layerModel.removeLayer(layer);
  }

  public IViewLayer[] getLayers() {
    return _layerModel.getLayers();
  }

  public void zoomIn() {
    _plot.zoom(_plot.getZoomFactor());
  }

  public void zoomOut() {
    _plot.zoom(1 / _plot.getZoomFactor());
  }

  public void zoomWindow(final boolean enabled) {
    if (enabled) {
      _plot.setMouseActions(PlotMouseActionList.getDefaultZoomWindowActions().getActions(), SWT.CURSOR_CROSS);
    } else {
      _plot.setMouseActions(new IPlotMouseAction[0], SWT.CURSOR_ARROW);
    }
  }

  public void pan(final boolean enabled) {
    if (enabled) {
      _plot.setMouseActions(PlotMouseActionList.getDefaultPanActions().getActions(), SWT.CURSOR_HAND);
    } else {
      _plot.setMouseActions(new IPlotMouseAction[0], SWT.CURSOR_ARROW);
    }
  }

  public void home() {
    _plot.unzoom();
  }

  public RGB getBackgroundPlotColor() {
    return _plot.getBackgroundPlotColor();
  }

  public void setBackgroundPlotColor(final RGB color) {
    _plot.setBackgroundPlotColor(color);
  }

  public RGB getBackgroundViewColor() {
    return getBackgroundPlotColor();
  }

  public void setBackgroundViewColor(final RGB color) {
    setBackgroundPlotColor(color);
  }

  public void setLayerTreeVisible(final boolean visible) {
    int[] weights = { 2, 1 };
    if (!visible) {
      weights[1] = 0;
    }
    _sashForm.setWeights(weights);
    if (_sharedToolBar != null) {
      _sharedToolBar.setShowLayerModel(visible);
    }
    //    _rootLayerViewer.getControl().setVisible(visible);
    //    int horizontalSpan = 1;
    //    if (!visible) {
    //      horizontalSpan = 2;
    //    }
    //    GridData layoutData = (GridData) _plot.getComposite().getLayoutData();
    //    layoutData.horizontalSpan = horizontalSpan;
    //    _plot.getComposite().setLayoutData(layoutData);
    //    layout(true, true);
  }

  public void setCursorBroadcast(final boolean broadcast) {
    _plot.setCursorBroadcast(broadcast);
  }

  public void setCursorReception(final boolean receive) {
    _plot.setCursorReception(receive);
  }

  public void addObjects(final Object[] objects) {
    // TODO: implement this.
  }

  public void setCursorStyle(final int cursorStyle) {
    getModelSpaceCanvas().setCursorStyle(cursorStyle);
  }

  @Override
  public void dispose() {
    _plot.dispose();
    _layerModel.dispose();
    super.dispose();
  }

  /**
   * Initializes the layered model and returns the root layer.
   * @param treeViewer the tree viewer in which to display the model.
   * @return the root layer of the model.
   */
  protected ILayeredModel initializeLayeredModel(final CheckboxTreeViewer treeViewer) {
    ILayeredModel model = new LayeredModel("Model");
    LayeredModelContentProvider contentProvider = new LayeredModelContentProvider(model);
    treeViewer.addCheckStateListener(contentProvider);
    LayeredModelLabelProvider labelProvider = new LayeredModelLabelProvider();
    treeViewer.setContentProvider(contentProvider);
    treeViewer.setLabelProvider(labelProvider);
    treeViewer.setInput(model);
    treeViewer.expandAll();
    return model;
  }

  @Override
  public void removeObjects(final Object[] objects) {
    //left blank intentionally
  }

  public void removeAllObjects() {
    final IRenderer[] renderers = getRenderers();
    for (final IRenderer renderer : renderers) {
      for (final Object renderedObject : renderer.getRenderedObjects()) {
        getLayerModel().removeLayer(getLayerModel().getLayer(new RendererSpecification(renderer)));
        renderer.clear();
      }
    }
  }

  public void moveToTop(final IPlotLayer layer) {
    _plot.moveToTop(layer);
  }

  public void addLayer(final IPlotLayer layer) {
    _plot.addLayer(layer);
  }

  public void addLayer(final IPlotLayer layer, final boolean adjustViewableBounds, final boolean adjustDefaultBounds) {
    _plot.addLayer(layer, adjustViewableBounds, adjustDefaultBounds);
  }

  public void adjustBounds(final IModelSpace modelSpace, final boolean adjustViewableBounds,
      final boolean adjustDefaultBounds) {
    _plot.adjustBounds(modelSpace, adjustViewableBounds, adjustDefaultBounds);
  }

  public void addListener(final IPlotListener listener) {
    _plot.addListener(listener);
  }

  public void addModelSpace(final IModelSpace modelSpace) {
    _plot.addModelSpace(modelSpace);
  }

  public IModelSpace getActiveModelSpace() {
    return _plot.getActiveModelSpace();
  }

  public IModelSpace[] getModelSpaces() {
    return _plot.getModelSpaces();
  }

  public String getTitle() {
    return _plot.getTitle();
  }

  public Composite getToolBarContainer() {
    return _plot.getToolBarContainer();
  }

  public double getZoomFactor() {
    return _plot.getZoomFactor();
  }

  public boolean getCursorBroadcast() {
    return _plot.getCursorBroadcast();
  }

  public boolean getCursorReception() {
    return _plot.getCursorReception();
  }

  public PlotScrolling getScrolling() {
    return _plot.getScrolling();
  }

  public void removeListener(final IPlotListener listener) {
    _plot.removeListener(listener);
  }

  public void removeModelSpace(final IModelSpace modelSpace) {
    _plot.removeModelSpace(modelSpace);
  }

  public boolean renderActiveModelOnly() {
    return _plot.renderActiveModelOnly();
  }

  public void renderActiveModelOnly(final boolean renderActiveModelOnly) {
    _plot.renderActiveModelOnly(renderActiveModelOnly);
  }

  public void setActiveModelSpace(final IModelSpace modelSpace) {
    _plot.setActiveModelSpace(modelSpace);
  }

  public void setHorizontalAxisAnnotationDensity(final int density) {
    _plot.setHorizontalAxisAnnotationDensity(density);
  }

  public void setHorizontalAxisGridLineDensity(final int density) {
    _plot.setHorizontalAxisGridLineDensity(density);
  }

  public void setMouseActions(final IPlotMouseAction[] actions, final int cursorStyle) {
    _plot.setMouseActions(actions, cursorStyle);
  }

  public void setMouseActions(final IPlotMouseAction[] actions, final Cursor cursor) {
    _plot.setMouseActions(actions, cursor);
  }

  public void setTitle(final String title) {
    _plot.setTitle(title);
  }

  public void setVerticalAxisAnnotationDensity(final int density) {
    _plot.setVerticalAxisAnnotationDensity(density);
  }

  public void setVerticalAxisGridLineDensity(final int density) {
    _plot.setVerticalAxisGridLineDensity(density);
  }

  public void setZoomFactor(final double zoomFactor) {
    _plot.setZoomFactor(zoomFactor);
  }

  public boolean showCursorToolTip() {
    return _plot.showCursorToolTip();
  }

  public void showCursorToolTip(final boolean showToolTip) {
    _plot.showCursorToolTip(showToolTip);
  }

  public void unzoom() {
    _plot.unzoom();
  }

  public void unzoom(final IModelSpace modelSpace) {
    _plot.unzoom(modelSpace);
  }

  public void zoom(final double zoomFactor) {
    _plot.zoom(zoomFactor);
  }

  public void zoom(final double zoomFactor, final int xPixel, final int yPixel) {
    _plot.zoom(zoomFactor, xPixel, yPixel);
  }

  public void zoom(final double horizontalZoomFactor, final double verticalZoomFactor) {
    _plot.zoom(horizontalZoomFactor, verticalZoomFactor);
  }

  public void zoom(final double horizontalZoomFactor, final double verticalZoomFactor, final int xPixel,
      final int yPixel) {
    _plot.zoom(horizontalZoomFactor, verticalZoomFactor, xPixel, yPixel);
  }

  public void modelSpaceUpdated(final ModelSpaceEvent modelEvent) {
    _plot.modelSpaceUpdated(modelEvent);
  }

  public void cursorSelectionUpdated(final double x, final double y) {
    _plot.cursorSelectionUpdated(x, y);
  }

  public void cursorUpdated(final double x, final double y, final boolean broadcast) {
    _plot.cursorUpdated(x, y, broadcast);
  }

  public void cursorTracked(final double x, final double y) {
    _plot.cursorTracked(x, y);
  }

  public IModelSpaceCanvas getModelSpaceCanvas() {
    return _plot.getModelSpaceCanvas();
  }

  public CanvasLayoutModel getCanvasLayoutModel() {
    return _plot.getCanvasLayoutModel();
  }

  public void setCanvasLayoutModel(final CanvasLayoutModel model) {
    _plot.setCanvasLayoutModel(model);
  }

  public void setCursorFormatterX(final NumberFormat formatter) {
    _plot.setCursorFormatterX(formatter);
  }

  public void setCursorFormatterY(final NumberFormat formatter) {
    _plot.setCursorFormatterY(formatter);
  }

  public void updateCanvasLayout(final CanvasLayoutModel model) {
    _plot.updateCanvasLayout(model);
  }

  public void editCanvasLayout() {
    _plot.editCanvasLayout();
  }

  public void updateAll() {
    _plot.updateAll();
  }

  public void print() {
    // TODO Implement print functionality.
  }

  protected CheckboxTreeViewer getLayeredModelTreeViewer() {
    return _layerViewer;
  }

}
