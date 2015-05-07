/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.io;


import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.geocraft.core.io.IDatastoreAccessor;
import org.geocraft.core.io.IDatastoreAccessorService;
import org.geocraft.core.io.IDatastoreEntrySelector;
import org.geocraft.core.io.IDatastoreLocationSelector;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.internal.ui.io.DatastoreViewer;
import org.geocraft.ui.common.tree.TreeLeaf;


/**
 * Dialog that displays the registered Entity/Datastore mappers. 
 * 
 * eg JavaSeis Volume 
 * eg GOCAD PointSet
 */
public class DatastoreSelectionDialog extends FormDialog implements SelectionListener {

  protected final IOMode _ioMode;

  /** The tree viewer containing the available datastores. */
  protected DatastoreViewer _viewer;

  /** The text field for the tree filter. */
  protected Text _filterText;

  protected Entity[] _entities;

  protected Set<String> _entityClassNames;

  private static final String FILTER_TEXT = "type filter text";

  /** 
   * Static accessor for creating input dialog.
   * @param shell
   * @return
   */
  public static DatastoreSelectionDialog createInputDialog(final Shell shell) {
    return new DatastoreSelectionDialog(shell, IOMode.INPUT, new Entity[0]);
  }

  /**
   * Static accessor for creating output dialog. 
   * 
   * @param shell
   * @param entities the actual data selected for output. 
   * @return
   */
  public static DatastoreSelectionDialog createOutputDialog(final Shell shell, final Entity[] entities) {
    return new DatastoreSelectionDialog(shell, IOMode.OUTPUT, entities);
  }

  /**
   * Private constructor to ensure developers use the static factory methods. 
   * 
   * @param shell
   * @param ioMode
   * @param entities
   */
  private DatastoreSelectionDialog(final Shell shell, final IOMode ioMode, final Entity[] entities) {
    super(shell);
    _ioMode = ioMode;
    _entities = entities;
    _entityClassNames = new HashSet<String>();
    for (Entity entity : entities) {
      _entityClassNames.add(entity.getClass().getSimpleName());
    }
  }

  @Override
  public void create() {
    super.create();
    getButton(IDialogConstants.OK_ID).setEnabled(false);
    widgetDefaultSelected(null);
  }

  @Override
  protected void createFormContent(final IManagedForm mform) {
    mform.getForm().setText("Datastore Selection");
    mform.getToolkit().decorateFormHeading(mform.getForm().getForm());

    Composite composite = mform.getForm().getBody();
    FillLayout fillLayout = new FillLayout();
    //fillLayout.marginWidth = 5;
    //fillLayout.marginHeight = 5;
    fillLayout.type = SWT.HORIZONTAL | SWT.VERTICAL;
    composite.setLayout(fillLayout);

    Composite gridComposite = new Composite(composite, SWT.NULL);
    gridComposite.setBackground(composite.getBackground());
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    gridLayout.makeColumnsEqualWidth = false;
    gridComposite.setLayout(gridLayout);

    GridData gridData1 = new GridData();
    gridData1.grabExcessHorizontalSpace = false;
    gridData1.horizontalAlignment = SWT.BEGINNING;
    Label label = new Label(gridComposite, SWT.NULL);
    label.setBackground(composite.getBackground());
    label.setText("Filter: ");
    label.setLayoutData(gridData1);

    GridData gridData2 = new GridData();
    gridData2.grabExcessHorizontalSpace = true;
    gridData2.horizontalAlignment = SWT.FILL;
    _filterText = new Text(gridComposite, SWT.BORDER);
    _filterText.setText(FILTER_TEXT);
    _filterText.setLayoutData(gridData2);
    _filterText.addKeyListener(new KeyAdapter() {

      @Override
      @SuppressWarnings("unused")
      public void keyReleased(final KeyEvent event) {
        _viewer.refresh();
      }
    });
    GridData gridData3 = new GridData();
    gridData3.horizontalSpan = 2;
    gridData3.grabExcessHorizontalSpace = true;
    gridData3.grabExcessVerticalSpace = true;
    gridData3.horizontalAlignment = SWT.FILL;
    gridData3.verticalAlignment = SWT.FILL;
    gridData3.widthHint = 400;
    Composite viewComposite = new Composite(gridComposite, SWT.NULL);
    viewComposite.setLayoutData(gridData3);
    viewComposite.setLayout(fillLayout);
    _viewer = new DatastoreViewer(viewComposite, _entityClassNames, _ioMode);
    _viewer.getTree().addSelectionListener(this);
    _viewer.getTree().addMouseListener(new MouseAdapter() {

      @Override
      public void mouseDoubleClick(MouseEvent e) {
        TreeItem[] selection = _viewer.getTree().getSelection();
        boolean okEnabled = false;
        if (selection != null && selection.length == 1) {
          boolean isLeaf = selection[0].getData().getClass().equals(TreeLeaf.class);
          okEnabled = isLeaf;
          if (!isLeaf) {
            TreeLeaf treeObject = (TreeLeaf) selection[0].getData();
            boolean isExpanded = _viewer.getExpandedState(treeObject);
            if (isExpanded) {
              _viewer.collapseToLevel(treeObject, AbstractTreeViewer.ALL_LEVELS);
            } else {
              _viewer.expandToLevel(treeObject, AbstractTreeViewer.ALL_LEVELS);
            }
          }
        }
        getButton(IDialogConstants.OK_ID).setEnabled(okEnabled);
        if (okEnabled) {
          okPressed();
        }
      }

    });
    _viewer.setInput(this);
    _viewer.addFilter(new ViewerFilter() {

      @Override
      @SuppressWarnings("unused")
      public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
        if (element.getClass().equals(TreeLeaf.class)) {
          TreeLeaf object = (TreeLeaf) element;
          String filterText = getFilterText();
          return object.getName().toLowerCase().contains(filterText.toLowerCase());
        }
        return true;
      }
    });
    _viewer.getTree().deselectAll();
    _viewer.expandAll();
    //    setPageComplete(false);
    //    setControl(composite);
  }

  protected String getFilterText() {
    String text = _filterText.getText();
    if (text == null || text.equals(FILTER_TEXT)) {
      text = "";
    }
    return text;
  }

  public void widgetDefaultSelected(final SelectionEvent event) {
    widgetSelected(event);
  }

  public void widgetSelected(final SelectionEvent event) {
    TreeItem[] selection = _viewer.getTree().getSelection();
    boolean okEnabled = false;
    if (selection != null && selection.length == 1) {
      okEnabled = true;
    }
    getButton(IDialogConstants.OK_ID).setEnabled(okEnabled);
  }

  @Override
  public void okPressed() {
    Point point = getShell().getLocation();
    IDatastoreAccessor datastoreAccessor = getSelectedDatastoreAccessor();
    if (datastoreAccessor == null) {
      return;
    }
    IStatus status = datastoreAccessor.initialize();
    if (status.getSeverity() == IStatus.ERROR) {
      MessageDialog.openError(getShell(), "Connection Error: " + datastoreAccessor.getName(), status.getMessage());
      return;
    } else if (status.getSeverity() == IStatus.CANCEL) {
      return;
    }
    super.okPressed();
    // use the workbench's shell to ensure consistent layout.
    if (_ioMode.equals(IOMode.OUTPUT)) {
      IDatastoreLocationSelector locationSelector = datastoreAccessor.createOutputSelector();
      if (locationSelector != null) {
        locationSelector.select();
      }
    }

    Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    DatastoreEntitySelectionDialog selectionDialog = new DatastoreEntitySelectionDialog(shell, _ioMode, _entities,
        datastoreAccessor);
    selectionDialog.create();
    selectionDialog.getShell().setText("Datastore Entity Selection");
    selectionDialog.getShell().setSize(1000, 500);

    if (_ioMode.equals(IOMode.INPUT)) {
      IDatastoreEntrySelector selector = datastoreAccessor.createInputSelector();
      selector.select(selectionDialog.getDatastoreEntryContainer());
    }
    selectionDialog.open();

    // Dispose of the resources.
    _filterText.dispose();
  }

  /**
   * Returns the selected datastore accessor.
   * 
   * @return the selected datastore accessor.
   */
  public IDatastoreAccessor getSelectedDatastoreAccessor() {
    IDatastoreAccessorService accessorService = ServiceProvider.getDatastoreAccessorService();
    if (accessorService == null) {
      ServiceProvider.getLoggingService().getLogger(getClass()).warn("No datastore accessor service found.");
      return null;
    }
    TreeItem[] selections = _viewer.getTree().getSelection();
    if (selections == null || selections.length < 1) {
      ServiceProvider.getLoggingService().getLogger(getClass()).error("No datastore accessor selected.");
      MessageDialog.openInformation(createShell(), "Datastore Selection Error", "No datastore accessor selected.");
      return null;
    }
    IDatastoreAccessor[] datastoreAccessors = accessorService.getDatastoreAccessors();
    for (IDatastoreAccessor datastoreAccessor : datastoreAccessors) {
      if (selections[0].getText().equalsIgnoreCase(datastoreAccessor.getName())) {
        return datastoreAccessor;
      }
    }
    ServiceProvider.getLoggingService().getLogger(getClass()).error("Selected datastore accessor no longer serviced.");
    return null;
  }
}
