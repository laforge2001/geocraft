package org.geocraft.ui.volumeviewer.renderer.seismic;


/**
 * Enumeration of the available methods for normalization.
 */
public enum NormalizationMethod {
  BY_LIMITS("By Limits"),
  BY_MAXIMUM("By Maximum"),
  BY_AVERAGE("By Average"),
  BY_TRACE_MAXIMUM("By Trace Maximum"),
  BY_TRACE_AVERAGE("By Trace Average");

  private final String _text;

  private NormalizationMethod(final String text) {
    _text = text;
  }

  public String getName() {
    return _text;
  }

  @Override
  public String toString() {
    return _text;
  }

  public static NormalizationMethod lookup(final String name) {
    if (name == null) {
      return null;
    }
    for (final NormalizationMethod method : NormalizationMethod.values()) {
      if (method.getName().equals(name)) {
        return method;
      }
    }
    return null;
  }
}
