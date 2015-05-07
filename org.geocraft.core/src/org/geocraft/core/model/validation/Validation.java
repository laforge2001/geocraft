/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.validation;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.geocraft.core.model.property.Property;


/**
 * A default implementation of model validation results.
 */
public class Validation implements IValidation {

  /** A collection of parameter statuses, mapped by parameter keys. */
  private final Map<String, IStatus> _statusMap;

  public Validation() {
    _statusMap = Collections.synchronizedMap(new HashMap<String, IStatus>());
  }

  public void setStatus(final Property property, final IStatus status) {
    setStatus(property.getKey(), status);
  }

  public void error(final Property property, final String message) {
    error(property.getKey(), message);
  }

  public void warning(final Property property, final String message) {
    warning(property.getKey(), message);
  }

  public void info(final Property property, final String message) {
    info(property.getKey(), message);
  }

  public void setStatus(final String key, final IStatus status) {
    if (_statusMap.containsKey(key)) {
      IStatus oldStatus = _statusMap.get(key);
      if (oldStatus.getSeverity() > status.getSeverity()) {
        return;
      }
    }
    _statusMap.put(key, status);
  }

  public void error(final String key, final String message) {
    if (_statusMap.containsKey(key)) {
      IStatus oldStatus = _statusMap.get(key);
      if (oldStatus.getSeverity() > IStatus.ERROR) {
        return;
      }
    }
    _statusMap.put(key, ValidationStatus.error(message));
  }

  public void warning(final String key, final String message) {
    if (_statusMap.containsKey(key)) {
      IStatus oldStatus = _statusMap.get(key);
      if (oldStatus.getSeverity() > IStatus.WARNING) {
        return;
      }
    }
    _statusMap.put(key, ValidationStatus.warning(message));
  }

  public void info(final String key, final String message) {
    if (_statusMap.containsKey(key)) {
      IStatus oldStatus = _statusMap.get(key);
      if (oldStatus.getSeverity() > IStatus.INFO) {
        return;
      }
    }
    _statusMap.put(key, ValidationStatus.info(message));
  }

  public IStatus getStatus(final String key) {
    // If the key is not found, then nothing was reported, so simply return OK.
    if (!_statusMap.containsKey(key)) {
      return ValidationStatus.ok();
    }
    // Otherwise, return the stored status.
    return _statusMap.get(key);
  }

  public IStatus[] getStatus() {
    return _statusMap.values().toArray(new IStatus[0]);
  }

  public String[] getStatusKeys() {
    return _statusMap.keySet().toArray(new String[0]);
  }

  public boolean containsError() {
    for (IStatus status : _statusMap.values()) {
      if (status.getSeverity() == IStatus.ERROR) {
        return true;
      }
    }
    return false;
  }

  public void clear() {
    _statusMap.clear();
  }

  /**
   * Prints the validation results (for debug purposes only).
   * @deprecated for debugging only - do not use.
   */
  @Deprecated
  public void dump(final String title) {
    for (String key : getStatusKeys()) {
      IStatus status = getStatus(key);
      System.out.println(key + " = " + status.getMessage() + " " + status.getSeverity());
    }
  }

  public String getStatusMessages(final int minSeverity) {
    String messages = "";
    for (String key : getStatusKeys()) {
      IStatus status = getStatus(key);
      if (status.getSeverity() >= minSeverity) {
        messages += key + ": " + status.getMessage() + "\n";
      }
    }
    return messages;
  }

  public int getMaxSeverity() {
    int maxSeverity = IStatus.OK;
    for (IStatus status : getStatus()) {
      maxSeverity = Math.max(maxSeverity, status.getSeverity());
    }
    return maxSeverity;
  }
}
