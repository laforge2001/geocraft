/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer;


import org.geocraft.core.model.datatypes.Domain;


/**
 * Enumeration of the available 3D viewer z-domains.
 */
public enum VolumeViewZDomain {
  /** The time domain. */
  TIME(Domain.TIME, IVolumeViewerConstants.TIME_DOMAIN),
  /** The depth domain. */
  DEPTH(Domain.DISTANCE, IVolumeViewerConstants.DEPTH_DOMAIN);

  private Domain _domain;

  private String _text;

  private VolumeViewZDomain(final Domain domain, final String text) {
    _domain = domain;
    _text = text;
  }

  public Domain getDomain() {
    return _domain;
  }

  public String getText() {
    return _text;
  }

  @Override
  public String toString() {
    return _text;
  }

  public static VolumeViewZDomain lookup(final String name) {
    if (name == null) {
      return null;
    }
    for (final VolumeViewZDomain zDomain : VolumeViewZDomain.values()) {
      if (zDomain.getText().equals(name)) {
        return zDomain;
      }
    }
    return null;
  }

  public static VolumeViewZDomain lookup(final Domain domain) {
    if (domain == null) {
      return null;
    }
    for (final VolumeViewZDomain zDomain : VolumeViewZDomain.values()) {
      if (zDomain.getDomain() == domain) {
        return zDomain;
      }
    }
    return null;
  }
}
