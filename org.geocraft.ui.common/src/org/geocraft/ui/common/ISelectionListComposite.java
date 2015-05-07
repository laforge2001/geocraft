package org.geocraft.ui.common;


import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;


public interface ISelectionListComposite {

  public enum Sorting {
    None, ByName
  }

  Composite getComposite();

  /**
   * Clears all the items in the list.
   */
  void clearList();

  /**
   * Sets the items in the list.
   * @param items the list of items to set.
   */
  void setListItems(Object[] items);

  /**
   * Adds an item to the list.
   * @param item the item to add.
   */
  void addListItem(Object item);

  /**
   * Clears the selected item(s) in the list.
   */
  void clearSelected();

  /**
   * Gets the selected item count in the list.
   * @return the selected item count.
   */
  int getSelectedCount();

  /**
   * Gets the selected items in the list.
   * @return the selected item.
   */
  Object[] getSelectedItems();

  /**
   * Gets the text in the filter field.
   * @return the text in the filter field.
   */
  String getFilterText();

  void sortLists(Sorting sorting);

  /**
   * Adds a list selection listener to the list.
   * @param listener the list selection listener to add.
   */
  void addSelectionListener(SelectionListener listener);

  /**
   * Removes a list selection listener to the list.
   * @param listener the list selection listener to remove.
   */
  void removeSelectionListener(SelectionListener listener);

}
