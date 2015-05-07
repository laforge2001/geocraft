/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.model;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


public class ValidationMessage extends Status {

  public static final String PLUGIN_ID = "org.geocraft.core.common";

  private final String _key;

  /**
   * Validation status that is associated with a key, which should be a bean's property name.
   * 
   * @param key the key that is associated with a bean's property
   * @param severity
   * @param message
   */
  public ValidationMessage(final String key, final int severity, final String message) {
    super(severity, PLUGIN_ID, message);
    _key = key;
  }

  public String getKey() {
    return _key;
  }

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(getKey());
    buffer.append(" - ");
    buffer.append(getMessage());
    return buffer.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;

    String message = getMessage();
    int severity = getSeverity();
    Throwable throwable = getException();

    result = prime * result + (message == null ? 0 : message.hashCode());
    result = prime * result + severity;
    result = prime * result + (throwable == null ? 0 : throwable.hashCode());
    result = prime * result + _key.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }

    if (!(obj instanceof ValidationMessage)) {
      return false;
    }

    ValidationMessage status = (ValidationMessage) obj;

    if (!_key.equals(status.getKey())) {
      return false;
    }

    if (isMultiStatus() ^ status.isMultiStatus()) {
      return false;
    }

    if (isOK() ^ status.isOK()) {
      return false;
    }

    if (getSeverity() != status.getSeverity()) {
      return false;
    }

    String message = getMessage();
    if (message == null) {
      if (status.getMessage() != null) {
        return false;
      }
    } else {
      if (!message.equals(status.getMessage())) {
        return false;
      }
    }

    Throwable exception = getException();
    if (exception == null) {
      if (status.getException() != null) {
        return false;
      }
    } else {
      if (!exception.equals(status.getException())) {
        return false;
      }
    }

    if (getCode() != status.getCode()) {
      return false;
    }

    if (getPlugin() != status.getPlugin()) {
      return false;
    }

    if (isMultiStatus()) {
      IStatus[] s = getChildren();
      IStatus[] t = status.getChildren();
      if (s.length != t.length) {
        return false;
      }
      for (int i = 0; i < s.length; i++) {
        if (s[i] instanceof ValidationMessage) {
          if (!((ValidationMessage) s[i]).equals(t[i])) {
            return false;
          }
        } else {
          if (!s[i].equals(t[i])) {
            return false;
          }
        }
      }
    }
    return true;
  }
}