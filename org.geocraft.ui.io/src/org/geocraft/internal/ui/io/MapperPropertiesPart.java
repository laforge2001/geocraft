/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.ui.io;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.io.ExportTask;
import org.geocraft.core.io.IDatastoreAccessor;
import org.geocraft.core.io.IDatastoreEntrySelections;
import org.geocraft.core.io.IDatastoreEntrySelector;
import org.geocraft.core.io.ImportTask;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.validation.Validation;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.session.MapperParameterStore;
import org.geocraft.ui.form2.IModelFormListener;
import org.geocraft.ui.model.ModelUI;


public class MapperPropertiesPart extends MasterDetailsBlock implements IDatastoreEntrySelections, IModelFormListener,
    Listener {

  /** The logger. */

  /** The parent composite. */
  protected Composite _parent;

  /** The table viewer of selected items. */
  protected TableViewer _tableViewer;

  /** The I/O mode. */
  protected IOMode _ioMode;

  /** The button to add to the list of selected items. */
  protected Button _addButton;

  /** The button to remove from the list of selected items. */
  protected Button _removeButton;

  /** The button to clear the list of selected items. */
  protected Button _clearButton;

  /** The datastore accessor. */
  protected IDatastoreAccessor _datastoreAccessor;

  /** The map of persistence properties. */
  public Map<Object, MapperModel> _mapperModels;

  /** The list of data store entries currently selected for input. */
  protected List<String> _datastoreEntries;

  /** The list of entities selected for output. */
  protected List<Entity> _entityList;

  /** List of items that the user has selected */
  IStructuredSelection _selectedItems;

  private IManagedForm _managedForm;

  private IModelFormListener _modelFormListener;

  /**
   * The default constructor.
   * @param ioMode the I/O mode.
   * @param datastoreAccessor the datastore accessor.
   */
  public MapperPropertiesPart(final IOMode ioMode, final IDatastoreAccessor datastoreAccessor, IModelFormListener listener) {
    _ioMode = ioMode;
    _datastoreAccessor = datastoreAccessor;
    _mapperModels = Collections.synchronizedMap(new HashMap<Object, MapperModel>());
    _datastoreEntries = Collections.synchronizedList(new ArrayList<String>());
    _entityList = Collections.synchronizedList(new ArrayList<Entity>());
    _modelFormListener = listener;
  }

  @Override
  protected void createMasterPart(final IManagedForm managedForm, final Composite parent) {
    _parent = parent;
    _managedForm = managedForm;
    _managedForm.getToolkit().decorateFormHeading(_managedForm.getForm().getForm());
    Composite composite = _managedForm.getToolkit().createComposite(parent);
    GridLayout gridLayout = new GridLayout();
    gridLayout.marginWidth = 5;
    gridLayout.marginHeight = 5;
    composite.setLayout(gridLayout);
    createTable(managedForm, composite);
  }

  public void modelFormUpdated(String key) {
    detailsPageUpdated();
    _modelFormListener.modelFormUpdated(key);
  }

  public void setFocus() {
    _tableViewer.getTable().setFocus();
  }

  @Override
  protected void createToolBarActions(final IManagedForm managedForm) {
    // No action.
  }

  /**
   * Registers a persistence properties details page for a specific datastore accessor
   * with the details part.
   */
  @Override
  protected void registerPages(final DetailsPart detailPart) {
    MapperPropertiesDetailsPage detailsPage = new MapperPropertiesDetailsPage(_ioMode, _datastoreAccessor,
        _managedForm, this);
    //    detailsPage.addListener(this);
    detailPart.registerPage(_datastoreAccessor.createMapperModel(_ioMode).getClass(), detailsPage);
  }

  /**
   * Returns <i>true</i> if the part is correctly parameterized, <i>false</i> if not.
   * To be correctly parameterized, the persistence properties for all of the selected
   * items need to be valid.
   * @return <i>true</i> if the part is correctly parameterized, <i>false</i> if not.
   */
  public boolean isComplete() {
    MapperModel[] models = _mapperModels.values().toArray(new MapperModel[0]);
    for (MapperModel model : models) {
      Validation validation = new Validation();
      model.validate(validation);
      if (validation.getMaxSeverity() == IStatus.ERROR) {
        return false;
      }
    }
    return true;
  }

  /**
   * Invoked when the details page has been updated.
   * @param details page the updated details page.
   */
  public void detailsPageUpdated() {
    //    validatePage();
    _tableViewer.refresh();
    //    notifyListeners();
    sashForm.setWeights(new int[] { 1, 2 });
  }

  //  /**
  //   * Adds a listener to the master-details block.
  //   * @param listener the listener to add.
  //   */
  //  public void addListener(final IMasterDetailsBlockListener listener) {
  //    _listeners.add(listener);
  //  }
  //
  //  /**
  //   * Removes a listener from the master-details block.
  //   * @param listener the listener to remove.
  //   */
  //  public void removeListener(final IMasterDetailsBlockListener listener) {
  //    _listeners.remove(listener);
  //  }

  //  /**
  //   * Notifies listeners that the master-details block has changed.
  //   */
  //  public void notifyListeners() {
  //    IMasterDetailsBlockListener[] listeners = _listeners.toArray(new IMasterDetailsBlockListener[0]);
  //    for (IMasterDetailsBlockListener listener : listeners) {
  //      listener.masterDetailsBlockUpdated(this);
  //    }
  //  }

  /**
   * Creates the table viewer of selected items for the master-details part.
   * @param managedForm the managed for in which to place the table viewer.
   * @param parent the parent composite.
   */
  protected void createTable(final IManagedForm managedForm, final Composite parent) {
    FormToolkit toolkit = managedForm.getToolkit();
    GridData gridData = new GridData(GridData.FILL_BOTH);
    Section section = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR);
    if (_ioMode.equals(IOMode.INPUT)) {
      section.setText(_datastoreAccessor.getName() + "(s) to Load");
    } else if (_ioMode.equals(IOMode.OUTPUT)) {
      section.setText("Entities to Export");
    }
    section.setLayoutData(gridData);
    Composite client = toolkit.createComposite(section, SWT.NONE);
    GridLayout layout = new GridLayout();
    int numCols = 2;
    if (_ioMode.equals(IOMode.INPUT)) {
      numCols = 3;
    }
    layout.numColumns = numCols;
    layout.makeColumnsEqualWidth = true;
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    client.setLayout(layout);
    toolkit.paintBordersFor(client);
    GridData gd = new GridData(GridData.FILL_BOTH);
    if (_ioMode.equals(IOMode.INPUT)) {
      _addButton = toolkit.createButton(client, "Add...", SWT.PUSH);
      _addButton.addListener(SWT.Selection, this);
      gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
      gd.horizontalAlignment = SWT.FILL;
      _addButton.setLayoutData(gd);
    }

    _removeButton = toolkit.createButton(client, "Remove", SWT.PUSH);
    _removeButton.addListener(SWT.Selection, this);
    gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
    gd.horizontalAlignment = SWT.FILL;
    _removeButton.setLayoutData(gd);

    _clearButton = toolkit.createButton(client, "Clear", SWT.PUSH);
    _clearButton.addListener(SWT.Selection, this);
    gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
    gd.horizontalAlignment = SWT.FILL;
    _clearButton.setLayoutData(gd);
    section.setClient(client);
    final SectionPart sectionPart = new SectionPart(section);
    managedForm.addPart(sectionPart);

    Table table = toolkit.createTable(client, SWT.NULL);
    gd = new GridData(GridData.FILL_BOTH);
    gd.heightHint = 100;
    gd.widthHint = 100;
    gd.verticalSpan = 5;
    gd.horizontalSpan = numCols;
    table.setLayoutData(gd);
    _tableViewer = new TableViewer(table);
    _tableViewer.setContentProvider(new ArrayContentProvider());
    _tableViewer.setLabelProvider(createLabelProvider());
    if (_ioMode.equals(IOMode.INPUT)) {
      _tableViewer.setInput(getInputList());
    } else if (_ioMode.equals(IOMode.OUTPUT)) {
      _tableViewer.setInput(getOutputList());
    }

    _tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

      public void selectionChanged(final SelectionChangedEvent event) {
        // Determine selected items
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();

        // save selected items
        _selectedItems = selection;

        Object object = selection.getFirstElement();
        if (_mapperModels.size() > 0) {
          Model model = _mapperModels.get(object);
          if (model != null) {
            selection = new StructuredSelection(model);
            managedForm.fireSelectionChanged(sectionPart, selection);
          } else {
            // if properties were removed then don't display them
            managedForm.fireSelectionChanged(sectionPart, null);
          }
        } else {
          managedForm.fireSelectionChanged(sectionPart, null);
        }
      }
    });
  }

  public void handleEvent(final Event event) {
    if (event.type == SWT.Selection) {
      try {
        if (event.widget.equals(_addButton)) {
          addItems();
        } else if (event.widget.equals(_removeButton)) {
          removeItems();
        } else if (event.widget.equals(_clearButton)) {
          clearItems();
        }
      } catch (RuntimeException ex) {
        ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.toString());
      }
    }
  }

  /**
   * Returns the list of items selected for input (this will be a list of data store items).
   * @return the list of items selected for input.
   */
  public List getInputList() {
    return _datastoreEntries;
  }

  /**
   * Returns the list of items selected for output (this will be a list of entities).
   * @return the list of items selected for output.
   */
  public List getOutputList() {
    return _entityList;
  }

  /**
   * Clears all items from the selection list.
   */
  public void clearItems() {
    _datastoreEntries.clear();
    _entityList.clear();
    _mapperModels.clear();
    _tableViewer.refresh();
    //    notifyListeners();
  }

  /**
   * Sets the entities that have been selected for output.
   * @param entities the entities selected for output.
   */
  public void setEntities(final Entity[] entities) {
    boolean autoSelect = _entityList.size() == 0;
    // Clear the existing lists.
    _entityList.clear();
    _mapperModels.clear();
    if (entities.length > 0) {
      Map<Entity, MapperModel> map = _datastoreAccessor.mapEntitiesToModels(entities);
      Set<Entity> entitySet = map.keySet();

      // Loop thru the subset of grid properties, setting default persistence properties for each.
      Iterator<Entity> iterator = entitySet.iterator();
      while (iterator.hasNext()) {
        Entity entity = iterator.next();
        _entityList.add(entity);
        MapperModel model = map.get(entity);
        ///MapperModelValidator validator = _datastoreAccessor.createMapperModelValidator(_ioMode);
        ///validator.setIOMode(_ioMode);
        ///model.setValidator(validator);
        model.setIOMode(_ioMode);
        _mapperModels.put(entity, model);
      }
    }

    // Set the input of the table viewer to be the output list of entities.
    _tableViewer.setInput(getOutputList());
    _tableViewer.refresh();

    // Notify listeners of the update.
    //    notifyListeners();

    if (autoSelect && _entityList.size() > 0) {
      _tableViewer.getTable().select(0);
      ISelection selection = _tableViewer.getSelection();
      _tableViewer.setSelection(selection, true);
    }
  }

  /**
   * Initiates the addition of items to the selection list.
   */
  public void addItems() {
    IDatastoreEntrySelector selector = _datastoreAccessor.createInputSelector();
    selector.select(this);
  }

  /**
   * Removes any selected items from the selection list.
   */
  public void removeItems() {

    // determine selected item
    Object object = _selectedItems.getFirstElement();

    // remove the item selected
    _mapperModels.remove(object);
    _datastoreEntries.remove(object);
    _entityList.remove(object);
    _tableViewer.remove(object);
    _tableViewer.refresh();

    // Notify listeners of the update.
    //    notifyListeners();
  }

  //  /**
  //   * Check that each of the entities on the page are valid.
  //   */
  //  public void validatePage() {
  //    List<String> invalidList = new ArrayList<String>();
  //
  //    Iterator iterator = _datastoreEntries.iterator();
  //    while (iterator.hasNext()) {
  //      DatastoreEntryDescription datastoreEntry = (DatastoreEntryDescription) iterator.next();
  //      Model model = _parametersMap.get(datastoreEntry);
  //      if (!model.validate().isOK()) {
  //        invalidList.add(datastoreEntry.getShortName());
  //      }
  //    }
  //    iterator = _entityList.iterator();
  //    while (iterator.hasNext()) {
  //      Entity entity = (Entity) iterator.next();
  //      Model model = _parametersMap.get(entity);
  //      if (!model.validate().isOK()) {
  //        invalidList.add(entity.getEntityType());
  //      }
  //    }
  //
  //    if (invalidList.size() > 0) {
  //      StringBuilder errorMessage = new StringBuilder();
  //      errorMessage.append("The following items contain invalid properties: ");
  //      for (int i = 0; i < invalidList.size(); i++) {
  //        if (i > 0) {
  //          errorMessage.append(", ");
  //        }
  //        errorMessage.append(invalidList.get(i));
  //      }
  //      errorMessage.append(".");
  //    }
  //  }

  /**
   * Starts the Task that loads the items in the selection list from the data store 
   * into the repository as entities.
   * 
   * @param repository the repository in which to insert the entities.
   */
  public void loadEntities() {
    MapperModel[] mapperModels = getMapperModels();
    for (MapperModel model : mapperModels) {
      // Remove all listeners from the model so the UI is no longer notified.
      model.removeAllListeners();
    }

    MapperParameterStore.save(mapperModels);

    for (MapperModel model : mapperModels) {
      ImportTask task = _datastoreAccessor.createImportTask();
      task.setMapperModel(model);
      String taskName = "Load " + _datastoreAccessor.getName();
      // Run the import task.
      new RunIOTask(task, taskName, TaskRunner.JOIN).run();
    }
  }

  /**
   * Saves the items in the selection list to the data store.
   */
  public void saveEntities() {
    Entity[] entities = getEntities();
    MapperModel[] models = getMapperModels();
    if (entities.length != models.length) {
      throw new RuntimeException("Mismatch between # of entities (" + entities.length + ") and # of models ("
          + models.length + ").");
    }

    for (int i = 0; i < models.length; i++) {
      ExportTask task = _datastoreAccessor.createExportTask();
      task.setEntity(entities[i]);
      task.setMapperModel(models[i]);
      String taskName = "Export " + _datastoreAccessor.getName() + "...";
      // Run the export task.
      new RunIOTask(task, taskName, TaskRunner.NO_JOIN).run();
    }

  }

  public Entity[] getEntities() {
    return _entityList.toArray(new Entity[0]);
  }

  public MapperModel[] getMapperModels() {
    return _mapperModels.values().toArray(new MapperModel[0]);
  }

  public void add(final String[] datastoreEntries, final MapperModel[] models) {
    boolean autoSelect = _datastoreEntries.size() == 0;
    for (int i = 0; i < datastoreEntries.length; i++) {
      MapperModel model = models[i];
      model.setIOMode(_ioMode);
      //MapperParameterStore.restore(model);
      //      MapperModelValidator validator = _datastoreAccessor.createMapperModelValidator(_ioMode);
      //      validator.setIOMode(_ioMode);
      //      model.setValidator(validator);
      _datastoreEntries.add(datastoreEntries[i]);
      _mapperModels.put(datastoreEntries[i], models[i]);
    }
    _tableViewer.setInput(getInputList());
    _tableViewer.refresh();
    //        validatePage();
    //    notifyListeners();

    if (autoSelect && _datastoreEntries.size() > 0) {
      _tableViewer.getTable().select(0);
      ISelection selection = _tableViewer.getSelection();
      _tableViewer.setSelection(selection, true);
    }
  }

  /**
   * Creates a custom label provider for the table view that is specific
   * to the datastore accessor.
   * @return a custom label provider.
   */
  public LabelProvider createLabelProvider() {
    return new IOWizardLabelProvder();
  }

  class IOWizardLabelProvder extends LabelProvider implements ITableLabelProvider {

    public Image getColumnImage(final Object element, final int columnIndex) {
      if (element instanceof String) {
        String item = (String) element;
        MapperModel model = _mapperModels.get(item);
        Validation validation = new Validation();
        model.validate(validation);
        int maxSeverity = validation.getMaxSeverity();
        if (maxSeverity == IStatus.OK) {
          return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
        } else if (maxSeverity == IStatus.WARNING) {
          return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
        } else if (maxSeverity == IStatus.INFO) {
          return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
        }
      } else if (element instanceof Entity) {
        Entity entity = (Entity) element;
        MapperModel model = _mapperModels.get(entity);
        Validation validation = new Validation();
        model.validate(validation);
        int maxSeverity = validation.getMaxSeverity();
        if (maxSeverity == IStatus.OK) {
          return ModelUI.getSharedImages().getImage(entity);
        } else if (maxSeverity == IStatus.WARNING) {
          return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
        } else if (maxSeverity == IStatus.INFO) {
          return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
        }
      }
      return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
    }

    public String getColumnText(final Object element, final int columnIndex) {
      if (element instanceof String) {
        return (String) element;
      } else if (element instanceof Entity) {
        Entity entity = (Entity) element;
        return entity.getDisplayName();
      }
      return element.toString();
    }
  }

  public void unbindModels() {
    for (Object key : _mapperModels.keySet()) {
      MapperModel model = _mapperModels.get(key);
      if (model != null) {
        model.removeAllListeners();
      }
    }
  }
}
