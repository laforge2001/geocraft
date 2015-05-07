/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


import java.util.ArrayList;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.geocraft.ui.common.TableWrapLayoutHelper;


public class OrderedListField extends AbstractField {

  /** The container for all the widgets. */
  protected Composite _container;

  /** The viewer to display the selected items. */
  protected ListViewer _listViewer;

  /** The button used to add items to the selected list. */
  protected Button _addButton;

  /** The button used to remove items from the selected list. */
  protected Button _removeButton;

  /** The button used to shift items up in the selected list. */
  protected Button _upButton;

  /** The button used to shift items down in the selected list. */
  protected Button _downButton;

  /** The array of all the available items to choose from. */
  protected Object[] _availableOptions;

  /**
   * Constructs an ordered list field.
   * 
   * @param parent the parent composite.
   * @param parameter the parameter key.
   * @param label the parameter label.
   * @param showToggle <i>true</i> to show a parameter toggle button; otherwise <i>false</i>.
   */
  public OrderedListField(final Composite parent, IFieldListener listener, final String key, final String label, final boolean showToggle) {
    super(parent, listener, key, label, showToggle);
  }

  @Override
  public Control[] createControls(final Composite parent) {

    _container = new Composite(parent, SWT.NONE);
    TableWrapData layoutData = TableWrapLayoutHelper.createLayoutData(true, false, TableWrapData.FILL,
        TableWrapData.FILL);
    layoutData.rowspan = 2;
    _container.setLayoutData(layoutData);
    TableWrapLayout layout = new TableWrapLayout();
    layout.numColumns = 2;
    layout.topMargin = 0;
    layout.bottomMargin = 0;
    layout.leftMargin = 0;
    layout.rightMargin = 0;
    _container.setLayout(layout);

    Composite blank = new Composite(parent, SWT.NONE);
    layoutData = TableWrapLayoutHelper.createLayoutData(false, true, TableWrapData.FILL, TableWrapData.FILL);
    layoutData.colspan = 3;
    layoutData.rowspan = 1;
    blank.setLayoutData(layoutData);

    TableWrapData wrapData = new TableWrapData();
    wrapData.valign = TableWrapData.TOP;

    _listViewer = new ListViewer(_container, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
    _listViewer.setContentProvider(new OrderedListFieldContentProvider());
    _listViewer.setLabelProvider(new OrderedListFieldLabelProvider());
    _listViewer.setInput(new Object[0]);
    wrapData = new TableWrapData(TableWrapData.FILL_GRAB);
    wrapData.rowspan = 4;
    wrapData.heightHint = 125;
    _listViewer.getControl().setLayoutData(wrapData);

    _availableOptions = new Object[0];

    _addButton = new Button(_container, SWT.PUSH);
    _addButton.setText("Add...");
    _addButton.setLayoutData(new TableWrapData(TableWrapData.FILL));

    _removeButton = new Button(_container, SWT.PUSH);
    _removeButton.setText("Remove");
    _removeButton.setLayoutData(new TableWrapData(TableWrapData.FILL));
    _removeButton.setEnabled(!_listViewer.getSelection().isEmpty());

    _upButton = new Button(_container, SWT.PUSH);
    _upButton.setText("Up");
    _upButton.setLayoutData(new TableWrapData(TableWrapData.FILL));
    _upButton.setEnabled(!_listViewer.getSelection().isEmpty());

    _downButton = new Button(_container, SWT.PUSH);
    _downButton.setText("Down");
    _downButton.setLayoutData(new TableWrapData(TableWrapData.FILL));
    _downButton.setEnabled(((StructuredSelection) _listViewer.getSelection()).size() == 1);

    // Add the listener for enabling/disabling the add,remove,up and down buttons.
    _listViewer.addSelectionChangedListener(new ISelectionChangedListener() {

      public void selectionChanged(final SelectionChangedEvent event) {
        int selectionSize = ((StructuredSelection) _listViewer.getSelection()).size();
        int selectionIndex = _listViewer.getList().getSelectionIndex();
        _removeButton.setEnabled(!event.getSelection().isEmpty());
        _upButton.setEnabled(selectionSize == 1 && selectionIndex > 0);
        _downButton.setEnabled(selectionSize == 1 && selectionIndex < _listViewer.getList().getItemCount() - 1);
      }
    });

    // Add the listener for adding items to the selection list.
    _addButton.addListener(SWT.Selection, new Listener() {

      @Override
      public void handleEvent(final Event event) {
        String key = _key;
        ILabelProvider labelProvider = new LabelProvider();
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(event.display.getActiveShell(),
            labelProvider);
        dialog.setMultipleSelection(true);
        dialog.setTitle(key + " List Selection");
        dialog.setEmptyListMessage("No available options");
        java.util.List<Object> filteredOptions = new ArrayList<Object>();
        for (Object object : _availableOptions) {
          filteredOptions.add(object);
        }
        for (Object object : (Object[]) _listViewer.getInput()) {
          filteredOptions.remove(object);
        }
        dialog.setElements(filteredOptions.toArray(new Object[0]));
        dialog.setMessage("Select Elements:");

        if (dialog.open() == Window.OK) {
          Object addedObjects[] = dialog.getResult();
          java.util.List<Object> input = new ArrayList<Object>();
          Object[] currentObjects = (Object[]) _listViewer.getInput();
          for (Object object : currentObjects) {
            input.add(object);
          }
          for (Object object : addedObjects) {
            input.add(object);
          }
          _listViewer.setInput(input.toArray(new Object[0]));
          _listViewer.refresh(true);
          _listener.fieldChanged(_key, input.toArray(new Object[0]));
        }
      }

    });

    // Add the listener for removing items from the selection list.
    _removeButton.addListener(SWT.Selection, new Listener() {

      @Override
      public void handleEvent(final Event event) {
        List list = _listViewer.getList();
        Object[] selectedObjects = (Object[]) _listViewer.getInput();
        int numItems = selectedObjects.length;
        StructuredSelection selection = (StructuredSelection) _listViewer.getSelection();
        int[] indices = list.getSelectionIndices();
        Object[] input = new Object[numItems - indices.length];
        int j = 0;
        for (int i = 0; i < numItems; i++) {
          boolean ok = true;
          for (int index : indices) {
            if (i == index) {
              ok = false;
              break;
            }
          }
          if (ok) {
            input[j] = selectedObjects[i];
            j++;
          }
        }
        _listViewer.setInput(input);
        _listViewer.refresh(true);
        _listViewer.setSelection(selection);
        _listener.fieldChanged(_key, input);
      }

    });

    // Add the listener for shifting items up in the selection list.
    _upButton.addListener(SWT.Selection, new Listener() {

      @Override
      public void handleEvent(final Event event) {
        List list = _listViewer.getList();
        Object[] selectedObjects = (Object[]) _listViewer.getInput();
        int numItems = selectedObjects.length;
        StructuredSelection selection = (StructuredSelection) _listViewer.getSelection();
        // Only do this operation on a single selection.
        if (selection.size() != 1) {
          return;
        }
        Object[] input = new Object[numItems];
        System.arraycopy(selectedObjects, 0, input, 0, numItems);
        int currentIndex = list.getSelectionIndex();
        if (currentIndex > 0) {
          Object temp = input[currentIndex - 1];
          input[currentIndex - 1] = input[currentIndex];
          input[currentIndex] = temp;
        }
        _listViewer.setInput(input);
        _listViewer.refresh(true);
        _listViewer.setSelection(selection);
        _listener.fieldChanged(_key, input);
      }
    });

    // Add the listener for shifting items down in the selection list.
    _downButton.addListener(SWT.Selection, new Listener() {

      @Override
      public void handleEvent(final Event event) {
        List list = _listViewer.getList();
        Object[] selectedObjects = (Object[]) _listViewer.getInput();
        int numItems = selectedObjects.length;
        StructuredSelection selection = (StructuredSelection) _listViewer.getSelection();
        // Only do this operation on a single selection.
        if (selection.size() != 1) {
          return;
        }
        Object[] input = new Object[numItems];
        System.arraycopy(selectedObjects, 0, input, 0, numItems);
        int currentIndex = list.getSelectionIndex();
        if (currentIndex < numItems - 1) {
          Object temp = input[currentIndex + 1];
          input[currentIndex + 1] = selectedObjects[currentIndex];
          input[currentIndex] = temp;
        }
        _listViewer.setInput(input);
        _listViewer.refresh(true);
        _listViewer.setSelection(selection);
        _listener.fieldChanged(_key, input);
      }
    });

    return new Control[] { _container };
  }

  /**
   * Sets the list of available options for the list field.
   * 
   * @param options the array of options.
   */
  public void setOptions(final Object[] options) {
    // Rebuild the list of available options.
    _availableOptions = new Object[options.length];
    System.arraycopy(options, 0, _availableOptions, 0, options.length);

    Display.getDefault().syncExec(new Runnable() {

      public void run() {
        // Get the list of objects currently selected and compare
        // it against the new list of options. Keep the selected
        // objects present in the new list, and throw out the rest.
        Object[] selectedObjects = (Object[]) _listViewer.getInput();
        java.util.List<Object> itemsToKeep = new ArrayList<Object>();
        for (Object selectedObject : selectedObjects) {
          for (Object option : options) {
            if (selectedObject.equals(option)) {
              itemsToKeep.add(selectedObject);
              break;
            }
          }
        }
        _listViewer.setInput(itemsToKeep.toArray(new Object[0]));
        _listViewer.refresh(true);
      }
    });

  }

  @Override
  public void updateField(Object valueObject) {
    if (valueObject != null && valueObject.getClass().isArray()) {
      Object[] array = (Object[]) valueObject;
      if (_listViewer.getContentProvider() != null) {
        _listViewer.setInput(array);
      }
      _listViewer.refresh(true);
    }
  }

}
