/*
 * Copyright (C) ConocoPhillips 2009 All Rights Reserved.
 */
package org.geocraft.ui.repository;


import org.eclipse.ui.views.properties.IPropertySheetEntry;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetSorter;


/** PropertySheetPage which sets its sorter to do nothing, i.e., leave items
unsorted. */
public class UnsortedPropertySheetPage extends PropertySheetPage {

  public UnsortedPropertySheetPage() {
    super();
    setSorter(new UnsortedPropertySheetSorter());
  }

  class UnsortedPropertySheetSorter extends PropertySheetSorter {

    @Override
    public int compare(IPropertySheetEntry entryA, IPropertySheetEntry entryB) {
      return 0;
    }

    @Override
    public int compareCategories(String categoryA, String categoryB) {
      return 0;
    }
  }
}