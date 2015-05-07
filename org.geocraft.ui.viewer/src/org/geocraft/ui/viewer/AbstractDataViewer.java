package org.geocraft.ui.viewer;


import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.geocraft.core.common.progress.BackgroundTask;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.model.Entity;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.core.service.message.IMessageSubscriber;
import org.geocraft.ui.viewer.layer.FolderLayer;
import org.geocraft.ui.viewer.layer.ILayeredModel;
import org.geocraft.ui.viewer.layer.IViewLayer;
import org.geocraft.ui.viewer.layer.LayeredModel;
import org.geocraft.ui.viewer.layer.LayeredModelContentProvider;
import org.geocraft.ui.viewer.layer.LayeredModelLabelProvider;
import org.geocraft.ui.viewer.toolbar.SharedToolBar;
import org.geocraft.ui.viewer.toolbar.SimpleToolBar;
import org.geocraft.ui.viewer.tree.ReadoutPanel;


/**
 * This class defines an abstract base class for data viewers.
 */
public abstract class AbstractDataViewer extends Composite implements IViewer, IMessageSubscriber {

  /** The map of folder view layers, mapped by name. */
  private final Map<String, IViewLayer> _folderLayers = Collections
      .synchronizedMap(new LinkedHashMap<String, IViewLayer>());

  /** The composite container for tool bars. */
  private final Composite _toolBarContainer;

  /** The shared tool bar common to all viewers. */
  private final SharedToolBar _sharedToolBar;

  /** The sash form container. */
  private final SashForm _sashForm;

  /** The layered model. */
  protected ILayeredModel _layerModel;

  /** The layered model tree viewer. */
  protected final CheckboxTreeViewer _layerViewer;

  /** The tab folder panel. */
  protected TabFolder _mainFolder;

  /** The tab to display the readout data. */
  protected TabItem _readoutTab;

  /** The panel for displaying layer info based on the cursor location. */
  protected ReadoutPanel _readoutPanel;

  private String _viewerID;

  protected String getViewerID() {
    return _viewerID;
  }

  /**
   * The base constructor.
   * 
   * @param parent the parent component.
   */
  public AbstractDataViewer(final Composite parent, boolean includeWindowZoom, boolean includePan, boolean cursorBroadcastAndReception) {
    this(parent, includeWindowZoom, includePan, cursorBroadcastAndReception, true);
  }

  /**
   * The base constructor.
   * 
   * @param parent the parent component.
   */
  public AbstractDataViewer(final Composite parent, boolean includeWindowZoom, boolean includePan, boolean cursorBroadcastAndReception, boolean allowDragDrop) {
    super(parent, SWT.NONE);

    _viewerID = "" + System.currentTimeMillis();

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
    final Composite canvasComposite = new Composite(_sashForm, SWT.NONE);
    canvasComposite.setLayout(new FillLayout());

    // Create the tree view of the layer model.
    _mainFolder = new TabFolder(_sashForm, SWT.TOP);
    final TabItem entityTab = new TabItem(_mainFolder, SWT.NONE);
    entityTab.setText("Layers");
    _readoutTab = new TabItem(_mainFolder, SWT.NONE);
    _readoutTab.setText("Readout");
    _layerViewer = new CheckboxTreeViewer(_mainFolder);
    entityTab.setControl(_layerViewer.getTree());

    _readoutPanel = new ReadoutPanel(_mainFolder, SWT.NONE);
    _readoutTab.setControl(_readoutPanel);

    _layerViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);

    setLayout(layout);

    // Create the layer model.
    _layerModel = createLayeredModel(_layerViewer);

    _toolBarContainer.setBackground(new Color(null, 255, 0, 0));
    _toolBarContainer.getParent().setBackground(new Color(null, 255, 255, 0));

    // Initialize the canvas (grid lines, colors, etc).
    initializeCanvas(canvasComposite);

    // Initialize the layer model.
    initializeLayeredModel(_layerModel, _folderLayers);

    // Subscribe the viewer to listen for cursor location events from the message bus.
    initializeMessageSubscriptions();

    // Initialize viewer-specific features.
    initializeViewerSpecificFeatures();

    // Create the shared tool bar.
    _sharedToolBar = new SharedToolBar(_toolBarContainer, this, includeWindowZoom, includePan,
        cursorBroadcastAndReception);

    // Initialize the shared and custom tool bars.
    initializeToolBars();

    setLayerTreeVisible(true);

    // Hookup the context menu and add drag/drop functionality.
    hookContextMenu();
    if (allowDragDrop) {
      initDragAndDrop(canvasComposite);
    }
  }

  /**
  * Finds a folder layers based on its name.
  * 
  * @param name the name of the folder layer to find.
  * @return the folder layer matching the name, or <i>null</i> if none found.
  */
  public final IViewLayer findFolderLayer(final String name) {
    return _folderLayers.get(name);
  }

  /**
   * Creates the layered model for the viewer.
   * 
   * @param treeViewer the tree viewer in which to display the model.
   * @return the layered model.
   */
  protected final ILayeredModel createLayeredModel(final CheckboxTreeViewer treeViewer) {
    final ILayeredModel model = new LayeredModel("Model");
    final LayeredModelContentProvider contentProvider = new LayeredModelContentProvider(model);
    treeViewer.addCheckStateListener(contentProvider);
    final LayeredModelLabelProvider labelProvider = new LayeredModelLabelProvider();
    treeViewer.setContentProvider(contentProvider);
    treeViewer.setLabelProvider(labelProvider);
    treeViewer.setInput(model);
    treeViewer.expandAll();
    return model;
  }

  protected void initializeLayeredModel(final ILayeredModel model, final Map<String, IViewLayer> folderLayers) {
    folderLayers.put(SEISMIC_FOLDER,
        new FolderLayer(SEISMIC_FOLDER, "Folder containing seismic geometries and volumes"));
    folderLayers.put(GRID_FOLDER, new FolderLayer(GRID_FOLDER, "Folder containing grids and grid geometries"));
    folderLayers.put(WELL_FOLDER, new FolderLayer(WELL_FOLDER, "Folder containing well, bores, logs, etc."));
    folderLayers.put(FAULT_FOLDER, new FolderLayer(FAULT_FOLDER, "Folder containing fault surfaces"));
    folderLayers.put(CULTURE_FOLDER, new FolderLayer(CULTURE_FOLDER,
        "Folder containing culture data (lease blocks, etc.)"));
    folderLayers.put(POINTSET_FOLDER, new FolderLayer(POINTSET_FOLDER, "Folder containing point sets"));
    folderLayers.put(AOI_ROI_FOLDER, new FolderLayer(AOI_ROI_FOLDER, "Folder containing areas/regions of interest"));
    for (final IViewLayer layer : folderLayers.values()) {
      model.addLayer(layer);
    }
  }

  /**
   * Unregister listeners and dispose of resources related to the viewer.
   */
  @Override
  public void dispose() {
    disposeOfMessageSubscriptions();
    _layerModel.dispose();
    super.dispose();
  }

  /**
   * Loops thru an array of objects, and calls the load method for
   * every one that is an entity.
   */
  protected final void loadEntities(final Object[] objects) {
    for (final Object object : objects) {
      if (object instanceof Entity) {
        try {
          ((Entity) object).load();
        } catch (Exception ex) {
          ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.getMessage(), ex);
        }
      }
    }
  }

  /**
   * Invoked when the cursor is moved over the viewer canvas.
   * This method updates the layer info in the readout panel.
   * 
   * @param x the x-coordinate of the viewer.
   * @param y the y-coordinate of the viewer.
   */
  public void cursorUpdated(final double x, final double y, final boolean broadcast) {

    // If the panel is not visible we don't need to update it. 
    final ReadoutPanel readoutPanel = getReadoutPanel();
    if (readoutPanel != null && readoutPanel.isVisible()) {

      // If the panel is not visible we don't need to update it. 
      readoutPanel.update(getViewReadoutInfo(x, y));

      for (final IRenderer renderer : getRenderers()) {
        if (renderer.showReadoutInfo()) {
          final ReadoutInfo info = renderer.getReadoutInfo(x, y);
          readoutPanel.update(info);
        }
      }

      readoutPanel.updateForm();
    }
  }

  protected final ReadoutPanel getReadoutPanel() {
    return _readoutPanel;
  }

  public final ILayeredModel getLayerModel() {
    return _layerModel;
  }

  public final TreeViewer getLayerViewer() {
    return _layerViewer;
  }

  public final SharedToolBar getSharedToolBar() {
    return _sharedToolBar;
  }

  public final SimpleToolBar addCustomToolBar() {
    return new SimpleToolBar(_toolBarContainer);
  }

  public final Composite getComposite() {
    return this;
  }

  public void setCursorBroadcast(final boolean broadcast) {
    if (_sharedToolBar != null) {
      _sharedToolBar.setBroadcastStatus(broadcast);
    }
  }

  public void setCursorReception(final boolean reception) {
    if (_sharedToolBar != null) {
      _sharedToolBar.setReceptionStatus(reception);
    }
  }

  public final void setLayerTreeVisible(final boolean visible) {
    final int[] weights = { 2, 1 };
    if (!visible) {
      weights[1] = 0;
    }
    _sashForm.setWeights(weights);
    if (_sharedToolBar != null) {
      _sharedToolBar.setShowLayerModel(visible);
    }
  }

  /**
   * Initialize the drag and drop for the viewer canvas.
   */
  private void initDragAndDrop(final Composite canvasComposite) {
    final DropTarget target = new DropTarget(canvasComposite, DND.DROP_COPY | DND.DROP_MOVE);
    target.setTransfer(new Transfer[] { TextTransfer.getInstance() });
    target.addDropListener(new DropTargetAdapter() {

      @Override
      public void dragOver(final DropTargetEvent event) {
        event.detail = DND.DROP_COPY;
      }

      @Override
      public void drop(final DropTargetEvent event) {
        if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
          final String vars = (String) event.data;
          final List<Object> objects = new ArrayList<Object>();
          final Scanner scanner = new Scanner(vars).useDelimiter(",");
          while (scanner.hasNext()) {
            final Object object = ServiceProvider.getRepository().get(scanner.next());
            if (object != null) {
              objects.add(object);
            }
          }
          addObjects(true, objects.toArray());
        }
      }
    });
  }

  /**
   * Subscribes the viewer to the message service for its topics of interest.
   */
  private final void initializeMessageSubscriptions() {
    // Get the message topics.
    String[] topics = getMessageSubscriptionTopics();
    if (topics == null) {
      return;
    }
    // Subscribe to them one at a time.
    for (String topic : topics) {
      ServiceProvider.getMessageService().subscribe(topic, this);
    }
  }

  /**
   * Unsubscribes the viewer from the message service for its topics of interest.
   */
  private final void disposeOfMessageSubscriptions() {
    // Get the message topics.
    String[] topics = getMessageSubscriptionTopics();
    if (topics == null) {
      return;
    }
    // Unsubscribe from them one at a time.
    for (String topic : topics) {
      ServiceProvider.getMessageService().unsubscribe(topic, this);
    }
  }

  /**
   * Adds objects to the viewer.
   * 
   * @param objects the array of objects to add.
   */
  public void addObjects(final Object[] objects) {
    addObjects(false, objects);
  }

  /**
   * Adds objects to the viewer.
   * 
   * @param objects the array of objects to add.
   * @param block
   */
  public void addObjects(final boolean block, final Object... objects) {
    boolean hasRenderers = false;
    IRenderer[] renderers = getRenderers();
    if (renderers != null && renderers.length > 0) {
      hasRenderers = true;
    }
    final boolean autoUpdate = !hasRenderers;
    final Shell shell = getShell();
    final BackgroundTask task = new BackgroundTask() {

      /**
       * The task for adding entities to the viewer.
       * @param logger the logger.
       * @param monitor the progress monitor. 
       */
      @Override
      public Void compute(final ILogger logger, final IProgressMonitor monitor) {
        // Begin the task.
        monitor.beginTask("Adding objects to the viewer", 1 + objects.length);
        try {
          Display.getDefault().syncExec(new Runnable() {

            public void run() {

              // Lookup renderers for the individual objects.
              for (final Object obj : objects) {
                final Object object = obj;
                final List<IConfigurationElement> configsSingle = findRenderer(shell, object);
                for (final IConfigurationElement config : configsSingle) {
                  //VolumeViewRenderer rendererSingle = RendererRegistry.selectRenderer("Renderer: " + object.toString(),
                  //    configsSingle, !multiRendererSelected)
                  try {
                    boolean rendererExists = false;
                    final IRenderer newRenderer = createRenderer(config);
                    for (final IRenderer renderer : getRenderers()) {
                      if (renderer.getClass().equals(newRenderer.getClass())) {
                        if (renderer.getRenderedObjects()[0].equals(object)) {
                          rendererExists = true;
                          break;
                        }
                      }
                    }
                    if (!rendererExists) {
                      if (Entity.class.isAssignableFrom(object.getClass())) {
                        Entity entity = (Entity) object;
                        try {
                          entity.load();
                        } catch (final Exception ex) {
                          ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.getMessage(), ex);
                        }
                      }
                      setRendererData(newRenderer, shell, new Object[] { object }, autoUpdate);
                    }
                  } catch (final Exception ex) {
                    ServiceProvider.getLoggingService().getLogger(getClass())
                        .error("Error creating renderer for " + object.toString(), ex);
                  }
                }

                // Update the progress monitor.
                monitor.worked(1);
                if (monitor.isCanceled()) {
                  break;
                }
              }
            }

          });

        } finally {
          monitor.done();
        }
        // Lastly, trigger a redraw of all the renderers.
        Display.getDefault().syncExec(new Runnable() {

          public void run() {
            checkAspectRatio();
            for (final IRenderer renderer : getRenderers()) {
              renderer.redraw();
            }
          }
        });
        monitor.worked(1);
        return null;
      }
    };

    if (block) {
      // If blocking the UI, run the task with the default JOIN flag.
      TaskRunner.runTask(task, "Add entities to the viewer");
    } else {
      // Otherwise, run it in another thread.
      new Thread(new Runnable() {

        public void run() {
          TaskRunner.runTask(task, "Add entities to the viewer");
        }
      }).start();
    }
  }

  /**
   * Removes objects from the viewer.
   * 
   * @param objects the array of objects to remove.
   */
  public final void removeObjects(final Object[] objects) {
    // Loop thru the objects.
    for (final Object object : objects) {
      // Check that the object is currently being renderer.
      if (isObjectRendered(object)) {
        // Then find its renderer layer and remove it.
        final IRenderer[] renderers = getRenderers();
        for (final IRenderer renderer : renderers) {
          for (final Object renderedObject : renderer.getRenderedObjects()) {
            if (renderedObject.equals(object)) {
              //getLayerModel().removeLayer(getLayerModel().getLayer(new RendererSpecification(renderer)));
              //renderer.clear();
            }
          }
        }
      }
    }
    updateAll();
  }

  /**
   * Returns a flag indicating if the given object is currently rendered in the viewer.
   * 
   * @param object the object to check.
   * @return <i>true</i> if currently rendered; <i>false</i> if not.
   */
  protected boolean isObjectRendered(final Object object) {
    // Loop thru all the renderers.
    for (final IRenderer renderer : getRenderers()) {
      // Loop thru all of the object in each renderer.
      final Object[] objects = renderer.getRenderedObjects();
      // Compare to the specified object.
      for (final Object o : objects) {
        if (o.equals(object)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Creates a renderer from the given configuration element.
   * 
   * @param configElement the configuration element defining the renderer.
   * @return an instance of the renderer.
   */
  protected abstract IRenderer createRenderer(IConfigurationElement configElement) throws Exception;

  /**
   * Finds the list of registered renderers that support the given object.
   * 
   * @param shell the parent shell to use for dialogs.
   * @param object the object to render.
   * @return a list of renderers for the given object.
   */
  protected abstract List<IConfigurationElement> findRenderer(Shell shell, Object object);

  /**
   * Initializes the viewer's custom tool bars.
   */
  protected abstract void initializeToolBars();

  /**
   * Initializes the viewer's rendering canvas.
   * 
   * @param canvasComposite the parent canvas composite.
   */
  protected abstract void initializeCanvas(final Composite canvasComposite);

  /**
   * Returns the array of topics to subscribe to on the message service.
   * <p>
   * These topics will be automatically subscribed to upon construction
   * of the viewer, and automatically unsubscribed from upon disposal.
   * 
   * @return the array of message topics.
   */
  protected abstract String[] getMessageSubscriptionTopics();

  /**
   * Returns the viewer's readout information for the given x,y canvas coordinate.
   * <p>
   * This should return viewer information only, as the renderers will return
   * their own readout information.
   * 
   * @param x the canvas x-coordinate.
   * @param y the canvas y-coordinate.
   * @return the viewer readout information.
   */
  protected abstract ReadoutInfo getViewReadoutInfo(final double x, final double y);

  /**
   * Hooks up the viewer context menu.
   */
  protected abstract void hookContextMenu();

  /**
   * Initializes any addition viewer-specific features.
   */
  protected abstract void initializeViewerSpecificFeatures();

  /**
   * Triggers all portions of the viewer to update.
   */
  protected abstract void updateAll();

  /**
   * Sets the data for the given renderer.
   * 
   * @param renderer the renderer.
   * @param shell the shell to use for popup dialogs.
   * @param viewer the viewer containing the renderer.
   * @param objects the array of objects to render.
   * @param autoUpdate <i>true</i> to auto-update the viewer bounds based on the extents of the rendered objects; otherwise <i>false<i>.
   */
  protected abstract void setRendererData(IRenderer renderer, Shell shell, Object[] objects, boolean autoUpdate);

  protected abstract void checkAspectRatio();

  public final ILayeredModel getLayeredModel() {
    return _layerModel;
  }

  public void print() {
    throw new UnsupportedOperationException("Printing from this viewer not currently supported.");
  }

  public void removeAllObjects() {
    final IRenderer[] renderers = getRenderers();
    for (final IRenderer renderer : renderers) {
      getLayerModel().removeLayer(getLayerModel().getLayer(new RendererSpecification(renderer)));
      renderer.clear();
    }
  }

}