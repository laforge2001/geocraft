package org.geocraft.core.query;


public enum QueryConstraintOperator {
  EQUAL_TO("="),
  NOT_EQUAL_TO("!="),
  GREATER_THAN(">"),
  GREATER_THAN_OR_EQUAL_TO(">="),
  LESS_THAN("<"),
  LESS_THAN_OR_EQUAL_TO("<="),
  IN("IN"),
  IS_NULL("IS NULL"),
  LIKE("LIKE"),
  NOT_IN("NOT IN"),
  NOT_LIKE("NOT LIKE"),
  NOT_NULL("NOT NULL");

  private String _op;

  QueryConstraintOperator(final String op) {
    _op = op;
  }
}
