package org.geocraft.core.query;


public class QueryConstraint {

  String _columnName;

  QueryConstraintOperator _operator;

  Object _value;

  public QueryConstraint(final String columnName, final QueryConstraintOperator op, final Object value) {
    _columnName = columnName;
    _operator = op;
    _value = value;
  }

  public String getColumnName() {
    return _columnName;
  }

  public QueryConstraintOperator getOperator() {
    return _operator;
  }

  public Object getValue() {
    return _value;
  }

  public Comparable getComparable() {
    return (Comparable) _value;
  }

  public String getStringValue() {
    return (String) _value;
  }

  public String[] getStringArrayValue() {
    return (String[]) _value;
  }

  public int getIntegerValue() {
    return ((Integer) _value).intValue();
  }

  public float getFloatValue() {
    return ((Float) _value).floatValue();
  }

  public double getDoubleValue() {
    return ((Double) _value).doubleValue();
  }
}
