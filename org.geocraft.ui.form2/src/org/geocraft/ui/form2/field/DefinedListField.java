/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


import java.util.ArrayList;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.geocraft.ui.common.TableWrapLayoutHelper;


public class DefinedListField extends AbstractField {

  protected Composite _container;

  protected ListViewer _listViewer;

  protected Button _removeButton;

  protected Button _upButton;

  protected Button _downButton;

  protected Object[] _availableOptions;

  /**
   * Constructs a parameter list field.
   * 
   * @param parent the parent composite.
   * @param parameter the parameter key.
   * @param label the parameter label.
   * @param showToggle <i>true</i> to show a parameter toggle button; otherwise <i>false</i>.
   */
  public DefinedListField(final Composite parent, IFieldListener listener, final String key, final String label, final boolean showToggle) {
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
    //getLabelControl().setLayoutData(td);

    _listViewer = new ListViewer(_container, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
    _listViewer.setContentProvider(new OrderedListFieldContentProvider());
    _listViewer.setLabelProvider(new OrderedListFieldLabelProvider());
    _listViewer.setInput(new Object[0]);
    wrapData = new TableWrapData(TableWrapData.FILL_GRAB);
    wrapData.rowspan = 4;
    wrapData.heightHint = 350;
    _listViewer.getControl().setLayoutData(wrapData);

    _availableOptions = new Object[0];

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

    _listViewer.addSelectionChangedListener(new ISelectionChangedListener() {

      public void selectionChanged(final SelectionChangedEvent event) {
        int selectionSize = ((StructuredSelection) _listViewer.getSelection()).size();
        int selectionIndex = _listViewer.getList().getSelectionIndex();
        _removeButton.setEnabled(!event.getSelection().isEmpty());
        _upButton.setEnabled(selectionSize == 1 && selectionIndex > 0);
        _downButton.setEnabled(selectionSize == 1 && selectionIndex < _listViewer.getList().getItemCount() - 1);
      }
    });

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

    Display.getDefault().asyncExec(new Runnable() {

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
    if (valueObject.getClass().isArray()) {
      Object[] array = (Object[]) valueObject;
      _listViewer.setInput(array);
      _listViewer.refresh(true);
    }
  }

}
