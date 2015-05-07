package org.geocraft.core.query;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * 
 * Quick and dirty ResultSet implementation that contains its data, and is not connected to 
 * a back-end SQL cursor.
 * 
 * @author pixtojl
 *
 */
public class StaticResultSet {

  /** Current cursor position */
  int _current;

  /** Max cursor position */
  int _max;

  String[] _columnNames;

  List<Object[]> _resultValues = new ArrayList<Object[]>();

  public StaticResultSet(final String[] columnNames) {
    _columnNames = columnNames;
    _current = -1;
    _max = -1;
  }

  public boolean relative(final int row) {
    _current = Math.min(-1, Math.max(_max + 1, _current + row));
    return _current > -1 && _current < _max + 1;
  }

  public boolean absolute(final int row) {
    if (row >= 0) {
      _current = Math.min(-1, Math.max(_max + 1, row));
    } else {
      _current = Math.min(-1, Math.max(_max + 1, _max + 1 + row));
    }
    return _current > -1 && _current < _max + 1;
  }

  public void beforeFirst() {
    _current = -1;
  }

  public void afterLast() {
    _current = _max + 1;
  }

  public boolean first() {
    _current = 0;
    return _resultValues.size() > 0;
  }

  public boolean last() {
    _current = _max - 1;
    return _resultValues.size() > 0;
  }

  public boolean isFirst() {
    return _current == 0;
  }

  public boolean isLast() {
    return _current == _max;
  }

  public boolean isBeforeFirst() {
    return _current == -1;
  }

  public boolean isAfterLast() {
    return _current == _max + 1;
  }

  public boolean next() {
    _current = Math.min(_current + 1, _max + 1);
    return _current < _max + 1;
  }

  public boolean previous() {
    _current = Math.min(_current - 1, -1);
    return _current > -1;
  }

  public int getRow() {
    return _current + 1;
  }

  public void addRow(final Object[] values) {
    _resultValues.add(values);
    _max = _resultValues.size() - 1;
  }

  public int findColumn(final String columnName) throws SQLException {
    for (int i = 0; i < _columnNames.length; i++) {
      if (columnName.equalsIgnoreCase(_columnNames[i])) {
        return i + 1;
      }
    }
    throw new SQLException("Invalid Column Label");
  }

  public void checkColumn(final int column) throws SQLException {
    if (column < 1 || column > _columnNames.length) {
      throw new SQLException("Invalid Column Number");
    }
  }

  public String getStringValue(final int column) throws SQLException {
    checkColumn(column);
    return (String) _resultValues.get(_current)[column - 1];
  }

  public int getIntegerValue(final int column) throws SQLException {
    checkColumn(column);
    return ((Integer) (_resultValues.get(_current)[column - 1])).intValue();
  }

  public double getDoubleValue(final int column) throws SQLException {
    checkColumn(column);
    return ((Double) (_resultValues.get(_current)[column - 1])).doubleValue();
  }
}
