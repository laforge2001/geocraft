/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


public class TableEntry {

  private String _label;

  private final Object[] _values;

  public TableEntry(final String label, final Object[] values) {
    _label = label;
    _values = values;
  }

  public TableEntry(final String label, final TableEntry entry) {
    this(label, entry.getValues());
  }

  public String getLabel() {
    return _label;
  }

  public Object[] getValues() {
    return _values;
  }

  public void setLabel(final String label) {
    _label = label;
  }
}
