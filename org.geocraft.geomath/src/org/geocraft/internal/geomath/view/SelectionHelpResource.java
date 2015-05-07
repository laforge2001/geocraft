/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.internal.geomath.view;


import org.eclipse.help.IHelpResource;


public class SelectionHelpResource implements IHelpResource {

  private final String _label;

  private final String _url;

  public SelectionHelpResource(final String label, final String url) {
    _label = label;
    _url = url;
  }

  public String getHref() {
    return _url;
  }

  public String getLabel() {
    return _label;
  }
}
