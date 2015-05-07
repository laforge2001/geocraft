/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.javaseis;


import java.io.File;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.geocraft.core.common.util.Utilities;
import org.geocraft.ui.form2.field.OrderedListFieldContentProvider;
import org.geocraft.ui.form2.field.OrderedListFieldLabelProvider;


public class SecondaryStorageDialog extends FormDialog {

  /** The container for all the widgets. */
  protected Composite _container;

  /** The viewer to display the selected items. */
  protected ListViewer _listViewer;

  /** The button used to add items to the selected list. */
  protected Button _addButton;

  /** The button used to remove items from the selected list. */
  protected Button _removeButton;

  protected String _lastDirectory = Utilities.getWorkingDirectory();

  public SecondaryStorageDialog(Shell shell) {
    super(shell);
  }

  @Override
  protected void createFormContent(final IManagedForm mform) {
    mform.getForm().setText("Storage Directories");
    mform.getToolkit().decorateFormHeading(mform.getForm().getForm());

    Composite parent = mform.getForm().getBody();
    FillLayout fillLayout = new FillLayout();
    //fillLayout.marginWidth = 5;
    //fillLayout.marginHeight = 5;
    fillLayout.type = SWT.HORIZONTAL | SWT.VERTICAL;
    parent.setLayout(fillLayout);
    _container = new Composite(parent, SWT.NONE);

    TableWrapLayout layout = new TableWrapLayout();
    layout.numColumns = 2;
    layout.topMargin = 0;
    layout.bottomMargin = 0;
    layout.leftMargin = 0;
    layout.rightMargin = 0;
    _container.setLayout(layout);

    TableWrapData wrapData = new TableWrapData();
    wrapData.valign = TableWrapData.TOP;

    _listViewer = new ListViewer(_container, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
    _listViewer.setContentProvider(new OrderedListFieldContentProvider());
    _listViewer.setLabelProvider(new OrderedListFieldLabelProvider());
    _listViewer.setInput(new Object[0]);
    wrapData = new TableWrapData(TableWrapData.FILL_GRAB);
    wrapData.rowspan = 5;
    wrapData.heightHint = 400;
    _listViewer.getControl().setLayoutData(wrapData);

    _addButton = new Button(_container, SWT.PUSH);
    _addButton.setText("Add...");
    _addButton.setLayoutData(new TableWrapData(TableWrapData.FILL));

    _removeButton = new Button(_container, SWT.PUSH);
    _removeButton.setText("Remove");
    _removeButton.setLayoutData(new TableWrapData(TableWrapData.FILL));
    _removeButton.setEnabled(!_listViewer.getSelection().isEmpty());

    // Add the listener for enabling/disabling the add,remove,up and down buttons.
    _listViewer.addSelectionChangedListener(new ISelectionChangedListener() {

      public void selectionChanged(final SelectionChangedEvent event) {
        _removeButton.setEnabled(!event.getSelection().isEmpty());
      }
    });
    _listViewer.setInput(SecondaryStorage.get());

    // Add the listener for adding items to the selection list.
    _addButton.addListener(SWT.Selection, new Listener() {

      @Override
      public void handleEvent(final Event event) {
        DirectoryDialog dialog = new DirectoryDialog(SecondaryStorageDialog.this.getShell());
        dialog.setFilterPath(_lastDirectory);
        String directory = dialog.open();
        if (directory != null && !directory.isEmpty()) {
          File file = new File(directory);
          _lastDirectory = file.getParent();
          SecondaryStorage.add(directory);
          _listViewer.setInput(SecondaryStorage.get());
          _listViewer.refresh(true);
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
        for (int i = 0; i < numItems; i++) {
          for (int index : indices) {
            if (i == index) {
              SecondaryStorage.remove(selectedObjects[i].toString());
              break;
            }
          }
        }
        _listViewer.setInput(SecondaryStorage.get());
        _listViewer.refresh(true);
        _listViewer.setSelection(selection);
      }

    });

  }

}
