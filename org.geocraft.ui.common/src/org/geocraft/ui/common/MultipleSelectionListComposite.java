/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.ui.common;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;
import org.geocraft.core.common.util.StringUtil;
import org.geocraft.core.model.datatypes.Unit;


/**
 * MultipleSelectionListComposite is used for selecting multiple objects from a list.
 * The composite contains a list of available objects and a list of selected objects.
 * The objects are moved between the lists using arrows buttons. A text field is
 * also available to filter the list of available objects.
 */
public class MultipleSelectionListComposite extends Composite implements ISelectionListComposite {

  /** The list sorting method. */
  protected Sorting _sorting;

  /** The list. */
  protected org.eclipse.swt.widgets.List _availableList;

  /** The selected list. */
  protected org.eclipse.swt.widgets.List _selectedList;

  /** The available label. */
  protected Label _lblAvailable;

  /** The selected label. */
  protected Label _lblSelected;

  /** The selection text field. */
  protected Text _txtFilter;

  /** The button to move items from the available list to the selected list. */
  protected Button _btnSelect;

  /** The button to move items from the selected list to the available list. */
  protected Button _btnDeselect;

  /** The list of available items. */
  protected List<Object> _availableListItems;

  /** The filtered list of available items. */
  protected List<Object> _filteredListItems;

  /** The list of selected items. */
  protected List<Object> _selectedListItems;

  /** The List of items eligible for the selected list */
  protected List<Object> _eligibleSelectedListItems;

  /** The size of the list ROWS (for fixed size lists) */
  protected final int _rows = 220;

  /** The size of the list COLUMNS (for the fixed size lists) */
  protected final int _columns = 100;

  protected int _limitSelectedList = 0;

  /**
   * Creates a ListMultipleSelectionPanel with visible row count = 10.
   */
  public MultipleSelectionListComposite(final Composite parent) {
    this(parent, 10, Sorting.ByName, true);
  }

  /**
   * Creates a ListMultipleSelectionPanel with visible row count = 10.
   */
  public MultipleSelectionListComposite(final Composite parent, final boolean autoFit) {
    this(parent, 10, Sorting.ByName, autoFit);
  }

  /**
   * Creates a ListMultipleSelectionPanel with visible row count specified.
   * @param visibleRowCount the visible row count of the list.
   */
  public MultipleSelectionListComposite(final Composite parent, final int visibleRowCount, final Sorting sorting) {
    this(parent, visibleRowCount, sorting, true);
  }

  /**
   * Creates a ListMultipleSelectionPanel with visible row count specified.
   * @param visibleRowCount the visible row count of the list.
   * @param sorting how the list will be sorted
   * @param autoFit if the list should be displayed to automatically adjust its size, based on the size of list entries and window
   */
  public MultipleSelectionListComposite(final Composite parent, final int visibleRowCount, final Sorting sorting, final boolean autoFit) {
    super(parent, SWT.NONE);
    setBackground(parent.getBackground());
    build(visibleRowCount, sorting, autoFit);
  }

  /**
   * Builds the scrollable list and selection text field.
   * @param visibleRowCount the visible row count of the list.
   */
  protected void build(final int visibleRowCount, final Sorting sorting, final boolean autoFit) {

    _sorting = sorting;

    GridLayout layout = new GridLayout();
    layout.makeColumnsEqualWidth = false;
    layout.numColumns = 3;
    layout.marginWidth = 5;
    layout.marginHeight = 5;
    setLayout(layout);

    createFilter();

    // Build the label for the list box.
    _lblAvailable = new Label(this, SWT.NONE);
    GridData ld1 = new GridData();
    ld1.grabExcessHorizontalSpace = false;
    ld1.horizontalAlignment = SWT.BEGINNING;
    ld1.horizontalSpan = 2;
    ld1.grabExcessVerticalSpace = false;
    ld1.verticalAlignment = SWT.FILL;
    ld1.verticalSpan = 1;
    _lblAvailable.setLayoutData(ld1);
    _lblAvailable.setText("Available:");
    _lblAvailable.setBackground(getBackground());

    _lblSelected = new Label(this, SWT.NONE);
    GridData ld2 = new GridData();
    ld2.grabExcessHorizontalSpace = false;
    ld2.horizontalAlignment = SWT.BEGINNING;
    ld2.horizontalSpan = 1;
    ld2.grabExcessVerticalSpace = false;
    ld2.verticalAlignment = SWT.FILL;
    ld2.verticalSpan = 1;
    _lblSelected.setLayoutData(ld2);
    _lblSelected.setText("Selected:");
    _lblSelected.setBackground(getBackground());

    // Build the list box and its scroll pane.
    _availableList = new org.eclipse.swt.widgets.List(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
    GridData gd1 = new GridData();
    gd1.grabExcessHorizontalSpace = true;
    gd1.horizontalAlignment = SWT.FILL;
    gd1.horizontalSpan = 1;
    gd1.grabExcessVerticalSpace = true;
    gd1.verticalAlignment = SWT.FILL;
    gd1.verticalSpan = 1;
    _availableList.setLayoutData(gd1);
    ///_availableList.setVisibleRowCount(visibleRowCount);

    if (!autoFit) {
      //scroll.setPreferredSize(new Dimension(_rows, _columns));
    }

    SelectionListener arrowListener = new SelectionListener() {

      @Override
      public void widgetSelected(final SelectionEvent event) {

        if (event.getSource().equals(_btnSelect)) {
          int[] indices = _availableList.getSelectionIndices();
          Object[] entries = new Object[indices.length];
          for (int i = 0; i < indices.length; i++) {
            entries[i] = _filteredListItems.get(indices[i]);
          }
          for (Object entry : entries) {
            if (_limitSelectedList > 0) {
              if (_selectedListItems.size() < _limitSelectedList) {
                _availableListItems.remove(entry);
                _filteredListItems.remove(entry);
                _selectedListItems.add(entry);
                if (_selectedListItems.size() == _limitSelectedList) {
                  _btnSelect.setEnabled(false);
                }
              }
            } else {
              _availableListItems.remove(entry);
              _selectedListItems.add(entry);
            }

          }
          sortLists(_sorting);
        } else if (event.getSource().equals(_btnDeselect)) {
          int[] indices = _selectedList.getSelectionIndices();
          Object[] entries = new Object[indices.length];
          for (int i = 0; i < indices.length; i++) {
            entries[i] = _selectedListItems.get(indices[i]);
          }
          for (Object entry : entries) {
            if (!_btnSelect.isEnabled()) {
              _btnSelect.setEnabled(true);
            }
            _selectedListItems.remove(entry);
            _availableListItems.add(entry);
          }
          sortLists(_sorting);
        }
      }

      @Override
      public void widgetDefaultSelected(final SelectionEvent event) {
        widgetSelected(event);
      }
    };

    Composite buttonComposite = new Composite(this, SWT.NONE);
    buttonComposite.setBackground(getBackground());
    GridLayout bcLayout = new GridLayout();
    bcLayout.makeColumnsEqualWidth = true;
    bcLayout.numColumns = 1;
    buttonComposite.setLayout(bcLayout);
    GridData bd = new GridData();
    bd.grabExcessHorizontalSpace = false;
    bd.horizontalAlignment = SWT.FILL;
    bd.horizontalSpan = 1;
    bd.grabExcessVerticalSpace = true;
    bd.verticalAlignment = SWT.FILL;
    bd.verticalSpan = 1;
    buttonComposite.setLayoutData(bd);

    _btnSelect = new Button(buttonComposite, SWT.ARROW | SWT.RIGHT);
    GridData bd1 = new GridData();
    bd1.grabExcessHorizontalSpace = false;
    bd1.horizontalAlignment = SWT.FILL;
    bd1.horizontalSpan = 1;
    bd1.grabExcessVerticalSpace = true;
    bd1.verticalAlignment = SWT.BOTTOM;
    bd1.verticalSpan = 1;
    _btnSelect.setLayoutData(bd1);
    _btnSelect.addSelectionListener(arrowListener);

    _btnDeselect = new Button(buttonComposite, SWT.ARROW | SWT.LEFT);
    GridData bd2 = new GridData();
    bd2.grabExcessHorizontalSpace = false;
    bd2.horizontalAlignment = SWT.FILL;
    bd2.horizontalSpan = 1;
    bd2.grabExcessVerticalSpace = true;
    bd2.verticalAlignment = SWT.TOP;
    bd2.verticalSpan = 1;
    _btnDeselect.setLayoutData(bd2);
    _btnDeselect.addSelectionListener(arrowListener);

    // Build the selected list box and its scroll pane.
    _selectedList = new org.eclipse.swt.widgets.List(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
    GridData gd2 = new GridData();
    gd2.grabExcessHorizontalSpace = true;
    gd2.horizontalAlignment = SWT.FILL;
    gd2.horizontalSpan = 1;
    gd2.grabExcessVerticalSpace = true;
    gd2.verticalAlignment = SWT.FILL;
    gd2.verticalSpan = 1;
    _selectedList.setLayoutData(gd2);
    ///_selectedList.setVisibleRowCount(visibleRowCount);

    if (visibleRowCount <= 0) {
      //scroll.setPreferredSize(new Dimension(_rows, _columns));
    }

    setVisible(true);

    // Create a key listener for the filter field.
    KeyListener keyListener = new KeyAdapter() {

      @Override
      public void keyReleased(final KeyEvent e) {

        Text src = (Text) e.getSource();
        String text = src.getText();

        filterList(text);
      }
    };

    // Add key listener for the selection filter.
    _txtFilter.addKeyListener(keyListener);

    // Create an initial (empty) vector of list items.
    _availableListItems = Collections.synchronizedList(new ArrayList<Object>());
    _selectedListItems = Collections.synchronizedList(new ArrayList<Object>());
    _filteredListItems = Collections.synchronizedList(new ArrayList<Object>());
    _eligibleSelectedListItems = Collections.synchronizedList(new ArrayList<Object>());
  }

  /**
   * 
   */
  private void createFilter() {
    Composite filterPanel = new Composite(this, SWT.NONE);
    GridData gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    gd.horizontalAlignment = SWT.FILL;
    gd.horizontalSpan = 3;
    gd.grabExcessVerticalSpace = false;
    gd.verticalAlignment = SWT.FILL;
    gd.verticalSpan = 1;
    filterPanel.setLayoutData(gd);
    filterPanel.setBackground(getBackground());

    GridLayout filterLayout = new GridLayout();
    filterLayout.numColumns = 2;
    filterLayout.makeColumnsEqualWidth = false;
    filterLayout.marginWidth = 5;
    filterLayout.marginHeight = 5;
    filterPanel.setLayout(filterLayout);

    Label filterLabel = new Label(filterPanel, SWT.NONE);
    filterLabel.setText("Filter:");
    filterLabel.setBackground(getBackground());
    GridData fld = new GridData();
    fld.grabExcessHorizontalSpace = false;
    fld.grabExcessVerticalSpace = true;
    fld.horizontalAlignment = SWT.FILL;
    fld.verticalAlignment = SWT.FILL;
    filterLabel.setLayoutData(fld);

    // Build the filter field.
    _txtFilter = new Text(filterPanel, SWT.BORDER);
    _txtFilter.setEditable(true);
    GridData fd = new GridData();
    fd.grabExcessHorizontalSpace = true;
    fd.grabExcessVerticalSpace = true;
    fd.horizontalAlignment = SWT.FILL;
    fd.verticalAlignment = SWT.FILL;
    _txtFilter.setLayoutData(fd);
  }

  /**
   * Clears all the items in the list.
   */
  public void clearList() {
    _availableList.removeAll();
    _selectedList.removeAll();
    _availableListItems.clear();
    _selectedListItems.clear();
    _filteredListItems.clear();
    _eligibleSelectedListItems.clear();
    sortLists(_sorting);
  }

  /**
   * Sets the items in the available list.
   * @param items the list of items to set.
   */
  public void setListItems(final Object[] items) {
    clearList();
    for (Object item : items) {
      _availableListItems.add(item);
    }
    sortLists(_sorting);
  }

  /**
   * Adds an item to the list.
   * @param item the item to add.
   */
  public void addListItem(final Object item) {
    _availableListItems.add(item);
    sortLists(_sorting);
  }

  /**
   * Adds an item to the list.
   * @param item the item to add.
   */
  public void addSelectedListItem(final Object item) {
    if (_limitSelectedList > 0) {
      if (_selectedListItems.size() < _limitSelectedList) {
        _selectedListItems.add(item);
        sortLists(_sorting);
      }
    } else {
      _selectedListItems.add(item);
      sortLists(_sorting);
    }
  }

  /**
   *  Mark item as ready to be shown in selected list
   *  This is used in saving the state of the selected list to preserve item ordering on update
   */
  public void addEligibleforSelectedListItem(final Object item) {
    _eligibleSelectedListItems.add(item);
  }

  /**
   * Updates the selected item list to protect ordering.
   */
  public void updateSelectedList(final Object[] savedSelectionList) {

    if (_eligibleSelectedListItems != null) {

      for (int i = 0; i < savedSelectionList.length; ++i) {
        if (StringUtil.isStringInArray(savedSelectionList[i].toString(), _eligibleSelectedListItems.toArray())) {
          this.addSelectedListItem(savedSelectionList[i]);
          _eligibleSelectedListItems.remove(savedSelectionList[i]);
        }
      }
      while (!_eligibleSelectedListItems.isEmpty()) {
        this.addSelectedListItem(_eligibleSelectedListItems.get(_eligibleSelectedListItems.size() - 1));
        _eligibleSelectedListItems.remove(_eligibleSelectedListItems.size() - 1);
      }
    }
  }

  /**
   * Clears the selected item(s) in the list.
   */
  public void clearSelected() {

    for (int i = _selectedListItems.size() - 1; i >= 0; i--) {
      Object object = _selectedListItems.get(i);

      _selectedListItems.remove(i);
      _availableListItems.add(object);
    }
    sortLists(_sorting);
  }

  /**
   * Gets the selected item count in the list.
   * @return the selected item count.
   */
  public int getSelectedCount() {
    return getSelectedItems().length;
  }

  /**
   * Gets the selected items in the list.
   * @return the selected item.
   */
  public Object[] getSelectedItems() {
    Object[] items = new Object[_selectedListItems.size()];
    for (int i = 0; i < items.length; i++) {
      items[i] = _selectedListItems.get(i);
    }
    return items;
  }

  /**
   * Gets the text in the filter field.
   * @return the text in the filter field.
   */
  public String getFilterText() {
    return _txtFilter.getText();
  }

  /**
   * Scans the items in the list for the one that best matches the specified
   * text String; The index location of this closest item in the list is
   * returned; Note: A return value of -1 indicates no match was found.
   * @param text the text string to search in the list.
   */
  protected void filterList(final String text) {
    _filteredListItems.clear();
    int lengthOfText;
    int numItems = 0;
    boolean flag = false;

    List<Integer> indices = new ArrayList<Integer>();

    String searchText = "*" + text.toLowerCase() + "*";

    numItems = _availableListItems.size();
    lengthOfText = text.length();
    for (int index = 0; index < numItems; index++) {

      Object object = _availableListItems.get(index);

      if (lengthOfText <= object.toString().length()) {
        String regex = StringUtil.wildcardToRegex(searchText);
        flag = object.toString().toLowerCase().matches(regex); // ignores case and matches string anywhere in the word
        //flag = object.toString().matches("(?i).*" + text + ".*"); // ignores case and matches string anywhere in the word
        if (flag) {
          indices.add(new Integer(index));
        }
      }
    }

    Object[] entries = new Object[indices.size()];

    for (int i = 0; i < indices.size(); i++) {
      int index = indices.get(i).intValue();
      entries[i] = _availableListItems.get(index);
      _filteredListItems.add(entries[i]);
    }
    String[] items = new String[entries.length];
    for (int i = 0; i < entries.length; i++) {
      items[i] = entries[i].toString();
    }
    _availableList.setItems(items);
  }

  public void sortLists(final Sorting sorting) {
    if (sorting.equals(Sorting.ByName)) {
      sortByName();
    }

    filterList(getFilterText());

    Object[] objects = _selectedListItems.toArray(new Object[0]);
    String[] items = new String[objects.length];
    for (int i = 0; i < objects.length; i++) {
      items[i] = objects[i].toString();
    }
    _selectedList.setItems(items);
  }

  protected void sortByName() {
    for (int i = 1; i < _availableListItems.size(); i++) {

      Object entry = _availableListItems.get(i);
      String name = entry.toString();
      int j;
      for (j = i - 1; j >= 0; j--) {
        String name0 = _availableListItems.get(j).toString();
        if (name.compareTo(name0) > 0) {
          break;
        }
      }
      _availableListItems.add(j + 1, entry);
      _availableListItems.remove(i + 1);
    }
  }

  /**
   * Adds a list selection listener to the list.
   * @param listener the list selection listener to add.
   */
  public void addSelectionListener(final SelectionListener listener) {
    _availableList.addSelectionListener(listener);
  }

  public void removeSelectionListener(final SelectionListener listener) {
    _availableList.removeSelectionListener(listener);
  }

  public Composite getComposite() {
    return this;
  }

  @Override
  public void dispose() {
    _availableListItems.clear();
    _selectedListItems.clear();
    _eligibleSelectedListItems.clear();
    super.dispose();
  }

  public void setLabels(final String availableLabel, final String selectedLabel) {
    _lblAvailable.setText(availableLabel);
    _lblSelected.setText(selectedLabel);
  }

  public void setLimitSelectedList(final int limit) {
    _limitSelectedList = limit;
  }

  public static void main(final String[] args) {
    String[] names = { "Cat", "Dog", "Zebra", "Horse", "Mouse", "Rat", "Snake", "Spider", "Rabbit", "Unicorn" };
    Object[] list = new Object[names.length];
    for (int i = 0; i < names.length; i++) {
      list[i] = names[i];
    }
    list = new Object[] { Unit.FOOT, Unit.METER, Unit.AMPERE, Unit.YARDS };
    Display display = Display.getDefault();
    Shell shell = new Shell(display);
    //    MultipleSelectionListDialog dialog = new MultipleSelectionListDialog(shell, "Zoo");
    //
    //    dialog.create();
    //    ISelectionListComposite selectionPanel = dialog.getListComposite();
    //    selectionPanel.setListItems(list);
    //    dialog.open();

    TwoPaneElementSelector selector = new TwoPaneElementSelector(shell, new LabelProvider(), new LabelProvider());
    selector.setUpperListLabel("Available:");
    selector.setLowerListLabel("Selected:");
    selector.create();
    selector.open();
  }

}
