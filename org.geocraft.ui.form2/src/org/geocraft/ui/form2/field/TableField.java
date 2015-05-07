/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.geocraft.ui.form2.ITableContentProvider;


public class TableField extends AbstractField {

  private Table _table;

  private TableViewer _viewer;

  private List<TableColumn> _columns = new ArrayList<TableColumn>();

  private String[] _columnProperties;

  private ITableContentProvider _contentProvider;

  public TableField(final Composite parent, IFieldListener listener, final String key, final String label, final boolean showToggle) {
    super(parent, listener, key, label, showToggle);
  }

  /**
   */
  public void createColumns(String[] labels) {
    _columnProperties = labels;
    for (String label : labels) {
      final TableColumn tc = new TableColumn(_table, SWT.CENTER);
      tc.setText(label);
      _columns.add(tc);
      tc.setWidth(100);

    }
  }

  /**
   */
  public void createColumns(int numColumns) {
    _columnProperties = new String[] {};
    for (int i = 0; i < numColumns; ++i) {
      final TableColumn tc = new TableColumn(_table, SWT.CENTER);
      _columns.add(tc);
      tc.setWidth(100);
    }
  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.form2.field.AbstractField#createControls(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public Control[] createControls(Composite formComposite) {

    createTable(formComposite);

    return new Composite[] { _table };
  }

  /**
   * 
   */
  public void createTableViewer(CellEditor[] editors) {
    _viewer = new TableViewer(_table);
    _viewer.setUseHashlookup(true);
    _viewer.setColumnProperties(_columnProperties);
    // Assign the cell editors to the viewer
    _viewer.setCellEditors(editors);

  }

  public void setCellModifier(ICellModifier modifier) {
    _viewer.setCellModifier(modifier);
  }

  public void setSorter(ViewerSorter sorter) {
    _viewer.setSorter(sorter);
  }

  public void setInput(ITableContentProvider cp, IBaseLabelProvider lp) {
    _contentProvider = cp;
    _viewer.setLabelProvider(lp);
  }

  /**
   * @param formComposite
   */
  private void createTable(Composite formComposite) {
    int style = SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

    _table = new Table(formComposite, style);

    GridData layoutData = new GridData();
    layoutData.grabExcessHorizontalSpace = true;
    layoutData.grabExcessVerticalSpace = false;
    layoutData.horizontalAlignment = SWT.FILL;
    layoutData.verticalAlignment = SWT.FILL;
    _table.setLayoutData(layoutData);

    _table.setLinesVisible(true);
    _table.setHeaderVisible(true);

    Display display = _table.getDisplay();
    final Color defaultSelectionColor = display.getSystemColor(SWT.COLOR_LIST_SELECTION);

    //this allows the selection color to not draw over the validation colors
    _table.addListener(SWT.EraseItem, new Listener() {

      public void handleEvent(Event event) {
        event.detail &= ~SWT.HOT;
        if ((event.detail & SWT.SELECTED) == 0) {
          return; /* item not selected */
        }
        int clientWidth = _table.getClientArea().width;
        GC gc = event.gc;
        Color oldForeground = gc.getForeground();
        Color oldBackground = gc.getBackground();
        gc.setBackground(defaultSelectionColor);
        gc.fillRectangle(0, event.y, clientWidth, event.height);
        gc.setForeground(oldForeground);
        gc.setBackground(oldBackground);
        event.detail &= ~SWT.SELECTED;
      }
    });

  }

  public TableViewer getViewer() {
    return _viewer;
  }

  public Table getTable() {
    return _table;
  }

  public List<String> getColumnNames() {
    return Arrays.asList(_columnProperties);
  }

  public ITableContentProvider getContentProvider() {
    return _contentProvider;
  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.form2.field.AbstractField#updateField(java.lang.Object)
   */
  @Override
  public void updateField(Object valueObject) {
    if (valueObject instanceof List) {
      _contentProvider.setInput(valueObject);
      _viewer.setContentProvider(_contentProvider);
      _viewer.setInput(valueObject);

      for (TableColumn col : _columns) {
        col.pack();
      }
    }

  }
}
