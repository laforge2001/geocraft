package org.geocraft.ui.multiplot;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.geocraft.core.model.Entity;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.plot.IPlotViewer;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.IViewerPart;
import org.geocraft.ui.viewer.ViewerUtilities;
import org.geocraft.ui.viewer.layer.IViewLayer;


/**
 * A view part capable of displaying multiple viewers in a single gridded layout.
 */
public class MultiPlotPart extends ViewPart implements IViewerPart {

  private static final String PART_ID = "PartID";

  /** The title of the view part. */
  protected String _title;

  protected Composite _parent;

  /** The list of viewers contained in the view part. */
  protected List<IViewer> _viewers;

  /** The maximum number of columns in the gridded layout (default is 3). */
  protected int _numColumns = 3;

  /** Flag indicating if the number of columns has already been set. It can only be set once. */
  protected boolean _numColumnsSet = false;

  public IViewer[] getViewers() {
    return _viewers.toArray(new IViewer[0]);
  }

  /**
   * Sets the title of the multi-plot view part.
   * @param title the title to set.
   */
  public void setPartTitle(final String title) {
    setPartName(title);
  }

  /**
   * Sets the title and image of the multi-plot view part.
   * @param title the title to set.
   * @param image the image to set.
   */
  public void setTitleAndImage(final String title, final Image image) {
    setPartTitle(title);
    setTitleImage(image);
  }

  /**
   * Sets the maximum number of columns of the gridded layout.
   * Note, this can only be set once.
   * 
   * @param numColumns the number of columns to set.
   */
  public void setNumColumns(final int numColumns) {
    if (_numColumnsSet) {
      throw new RuntimeException("The number of columns has already been set to " + _numColumns + ".");
    } else if (numColumns <= 0) {
      throw new IllegalArgumentException("The number of columns must be positive.");
    }
    GridLayout layout = (GridLayout) _parent.getLayout();
    _numColumns = numColumns;
    _numColumnsSet = true;
    layout.numColumns = _numColumns;
    _parent.setLayout(layout);
  }

  @Override
  public void createPartControl(final Composite parent) {
    //Create a unique viewer part ID used when restoring state
    String partID = getPartProperty(PART_ID);
    String pid = ViewerUtilities.getViewerPartID(partID);
    if (partID == null || partID.isEmpty()) {
      setPartProperty(PART_ID, pid);
    }

    _parent = parent;
    Color color = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
    parent.setBackground(color);
    GridLayout layout = new GridLayout();
    layout.numColumns = _numColumns;
    layout.makeColumnsEqualWidth = true;
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    layout.horizontalSpacing = 1;
    layout.verticalSpacing = 1;
    parent.setLayout(layout);
    _viewers = Collections.synchronizedList(new ArrayList<IViewer>());
    setPartName("Multi-Plot");
  }

  /**
   * Returns the parent composite of the contained viewers.
   */
  public Composite getViewerParent() {
    return _parent;
  }

  /**
   * Adds a viewer to the multi-plot view part.
   * This will be appended in the next column, or the next row if
   * the maximum number of columns is exceeded.
   * 
   * @param viewer the viewer to add.
   */
  public void addViewer(final IViewer viewer) {
    GridData layoutData = new GridData();
    layoutData.grabExcessHorizontalSpace = true;
    layoutData.grabExcessVerticalSpace = true;
    layoutData.horizontalAlignment = SWT.FILL;
    layoutData.verticalAlignment = SWT.FILL;
    viewer.getComposite().setLayoutData(layoutData);
    _viewers.add(viewer);
    getSite().setSelectionProvider(viewer.getLayerViewer());
    hookContextMenu(viewer);
    if (viewer instanceof IPlotViewer) {
      IPlotViewer view = (IPlotViewer) viewer;
      view.updateAll();
    }
    _parent.redraw();
    _parent.update();
    Point size = _parent.getSize();
    _parent.setSize(size.x + 1, size.y + 1);
    _parent.setSize(size.x, size.y);
    initDragAndDrop(viewer);
  }

  /**
   * Hook the tree context menu.
   */
  protected void hookContextMenu(final IViewer viewer) {
    MenuManager menuMgr = new MenuManager("#PopupMenu");
    menuMgr.setRemoveAllWhenShown(true);
    menuMgr.addMenuListener(new IMenuListener() {

      public void menuAboutToShow(final IMenuManager manager) {
        fillContextMenu(manager);
      }
    });
    Menu menu = menuMgr.createContextMenu(viewer.getLayerViewer().getControl());
    viewer.getLayerViewer().getControl().setMenu(menu);
    getSite().registerContextMenu(menuMgr, viewer.getLayerViewer());
  }

  /**
   * Fill the tree context menu.
   * 
   * @param manager the menu manager
   */
  protected void fillContextMenu(final IMenuManager manager) {
    ISelection selection = getSite().getSelectionProvider().getSelection();
    MenuManager layerMenu = new MenuManager("Layer", "org.geocraft.ui.viewer.layer");
    layerMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    manager.add(layerMenu);
    addSelectionSpecificActions(manager, selection);
  }

  public void addSelectionSpecificActions(final IMenuManager manager, final ISelection selection) {
    if (selection == null) {
      return;
    }
    if (selection.getClass().equals(TreeSelection.class)) {
      TreeSelection treeSelection = (TreeSelection) selection;
      Iterator iterator = treeSelection.iterator();
      while (iterator.hasNext()) {
        Object element = iterator.next();
        if (element instanceof IViewLayer) {
          IViewLayer viewLayer = (IViewLayer) element;
          for (IAction action : viewLayer.getActions()) {
            manager.add(action);
          }
        }
      }
    }
  }

  @Override
  public void setFocus() {
    // Nothing to do.
  }

  @Override
  public void dispose() {
    super.dispose();
    for (IViewer viewer : _viewers) {
      viewer.dispose();
    }
    //unregister this viewer part
    ServiceProvider.getViewersService().remove(this.hashCode());
  }

  /**
   * Insert a blank viewer entry in the gridded layout.
   */
  public void insertBlank() {
    Composite composite = new Composite(_parent, SWT.NONE);
    GridData layoutData = new GridData();
    layoutData.grabExcessHorizontalSpace = true;
    layoutData.grabExcessVerticalSpace = true;
    layoutData.horizontalAlignment = SWT.FILL;
    layoutData.verticalAlignment = SWT.FILL;
    composite.setLayoutData(layoutData);
    composite.setBackground(_parent.getBackground());
  }

  /**
   * Initialize the drag and drop for the viewer.
   */
  protected void initDragAndDrop(final IViewer viewer) {
    DropTarget target = new DropTarget(viewer.getComposite(), DND.DROP_COPY | DND.DROP_MOVE);
    target.setTransfer(new Transfer[] { TextTransfer.getInstance() });
    target.addDropListener(new DropTargetAdapter() {

      @Override
      public void dragOver(DropTargetEvent event) {
        event.detail = DND.DROP_COPY;
      }

      @Override
      public void drop(final DropTargetEvent event) {
        if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
          String vars = (String) event.data;
          List<Entity> entities = new ArrayList<Entity>();
          Scanner scanner = new Scanner(vars).useDelimiter(",");
          while (scanner.hasNext()) {
            String item = scanner.next();
            IRepository repository = ServiceProvider.getRepository();
            Entity entity = (Entity) repository.get(item);
            if (entity != null) {
              entities.add(entity);
            }
          }
          viewer.addObjects(entities.toArray(new Entity[0]));
        }
      }
    });
  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.viewer.IViewerPart#getPartId()
   */
  @Override
  public String getPartId() {
    return this.getPartProperty(PART_ID);
    //return this.getConfigurationElement().getAttribute("id");
  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.viewer.IViewerPart#setViewerPartName(java.lang.String)
   */
  @Override
  public void setViewerPartName(String partName) {
    _title = partName;
  }
}
