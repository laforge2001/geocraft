package org.geocraft.core.model.datatypes;


public enum ZDomain {
  TIME("Time"),
  DEPTH("Depth");

  private String _text;

  private ZDomain(final String text) {
    _text = text;
  }

  @Override
  public String toString() {
    return _text;
  }

  public static ZDomain getFromDomain(final Domain domain) {
    if (domain == null) {
      return null;
    }
    if (domain == Domain.TIME) {
      return ZDomain.TIME;
    } else if (domain == Domain.DISTANCE) {
      return ZDomain.DEPTH;
    }
    return null;
  }
}
