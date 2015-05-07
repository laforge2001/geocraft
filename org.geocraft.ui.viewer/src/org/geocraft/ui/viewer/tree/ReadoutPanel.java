/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.tree;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.geocraft.ui.viewer.ReadoutInfo;


/**
 * The readout panel displays data provided by the selected renderer. 
 */
public class ReadoutPanel extends Composite {

  /** The form toolkit. */
  private final FormToolkit _toolkit;

  private final ScrolledForm _form;

  private final Composite _body;

  private Font _font = null;

  /** The widget for each data key. */
  private final Map<String, Widget> _keyToWidget = new LinkedHashMap<String, Widget>();

  /** Collection of each layer's contribution to the display. */
  private final List<ReadoutInfo> _readoutInfos = new ArrayList<ReadoutInfo>();

  /**
   * The constructor which is only called once.
   * @param parent the parent composite
   * @param style the SWT style
   */
  public ReadoutPanel(final Composite parent, final int style) {
    super(parent, style);
    setLayout(new FillLayout());
    _toolkit = new FormToolkit(getDisplay());
    _form = _toolkit.createScrolledForm(this);
    _font = new Font(parent.getDisplay(), "SansSerif", 8, SWT.NORMAL);

    _toolkit.decorateFormHeading(_form.getForm());
    _body = _form.getBody();
    TableWrapLayout layout = new TableWrapLayout();
    layout.numColumns = 1;
    _body.setLayout(layout);
    _form.setText("Layer Information");
  }

  public synchronized void update(ReadoutInfo info) {
    if (info == null) {
      //throw new IllegalArgumentException("Null ReadoutInfo");
      return;
    }

    int index = getReadoutIndex(info.getTitle());
    if (index == -1) {
      _readoutInfos.add(info);
    } else {
      _readoutInfos.set(index, info);
    }
  }

  /**
   * Update an existing form using the current data.
   */
  public synchronized void updateForm() {

    for (ReadoutInfo info : _readoutInfos) {

      Table table = getTable(info.getTitle());

      boolean redoLayout = false;
      // if there are more items in table than in the info record
      // blank the ones at the end that are not needed
      if (table.getItemCount() > info.getNumRecords()) {
        for (int i = info.getNumRecords(); i < table.getItemCount(); i++) {
          redoLayout = true; // causes flicker
          TableItem item = table.getItem(i);
          item.setText(new String[] { "", "" });
        }
      }

      // if there are not enough items in the table create new ones
      // and add to the end of the list
      if (table.getItemCount() < info.getNumRecords()) {
        for (int i = table.getItemCount(); i < info.getNumRecords(); i++) {
          redoLayout = true;
          TableItem item = new TableItem(table, SWT.NONE);
          item.setText(new String[] { info.getKey(i), info.getValue(i) });
        }
      }

      // the default case where we just change each record unless
      // it is the same as last time in which case we skip it. 
      for (int i = 0; i < info.getNumRecords(); i++) {

        TableItem item = table.getItem(i);

        if (!item.getText(0).equals(info.getKey(i))) {
          item.setText(0, info.getKey(i));
        }

        if (!item.getText(1).equals(info.getValue(i))) {
          item.setText(1, info.getValue(i));
        }

      }

      // adjust the width of the table columns to accommodate the latest data
      for (int j = 0; j < 2; j++) {
        table.getColumn(j).pack();
      }

      if (redoLayout) {
        _form.reflow(true);
      }

    }
  }

  /**
   * Lookup the ui component and create it if necessary. 
   * 
   * @param info - the information that will be displayed
   * @return table to display the information in
   */
  private Table getTable(String title) {

    if (!_keyToWidget.containsKey(title)) {
      initSection(title);
    }

    return (Table) _keyToWidget.get(title);
  }

  private void initSection(String title) {

    Section section = _toolkit.createSection(_body, ExpandableComposite.TITLE_BAR | ExpandableComposite.TREE_NODE
        | ExpandableComposite.EXPANDED);
    section.setText(title);

    TableWrapData gData = new TableWrapData(TableWrapData.FILL_GRAB);
    gData.colspan = 1;
    section.setLayoutData(gData);

    Table table = _toolkit.createTable(section, SWT.NONE);
    _keyToWidget.put(title, table);
    table.setFont(_font);
    table.setLinesVisible(true);
    table.setHeaderVisible(false);

    // initialize the columns even though they are not visible .... 
    for (int k = 0; k < 2; k++) {
      TableColumn column = new TableColumn(table, SWT.NONE);
      column.setText("lkjljlkjlk"); // 
    }

    section.setClient(table);

    _form.reflow(true);
  }

  /** 
   * Find the index of the ReadoutInfo with this title. 
   * 
   * @param title
   * @return
   */
  private int getReadoutIndex(String title) {
    int index = -1;
    for (int i = 0; i < _readoutInfos.size(); i++) {
      if (_readoutInfos.get(i).getTitle().equals(title)) {
        index = i;
        break;
      }
    }
    return index;
  }

  @Override
  public void dispose() {
    _toolkit.dispose();
    _font.dispose();
    super.dispose();
  }

  public void removeNode(ReadoutInfo info) {
    if (info == null) {
      throw new IllegalArgumentException("Null ReadoutInfo");
    }

    int index = getReadoutIndex(info.getTitle());
    if (index != -1) {
      _readoutInfos.remove(index);
    }
  }

}
