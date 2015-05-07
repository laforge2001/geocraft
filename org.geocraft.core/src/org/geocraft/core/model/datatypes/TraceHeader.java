/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.datatypes;


public class TraceHeader extends Header {

  private int _inline;

  private int _xline;

  private int _cdp;

  private float _shotpoint;

  private float _offset;

  public TraceHeader(final HeaderDefinition headerDef) {
    super(headerDef);
  }

  public int getInline() {
    return _inline;
  }

  public void setInline(final int inline) {
    _inline = inline;
    putInteger(TraceHeaderCatalog.INLINE_NO, inline);
  }

  public int getXline() {
    return _xline;
  }

  public void setXline(final int xline) {
    _xline = xline;
    putInteger(TraceHeaderCatalog.XLINE_NO, xline);
  }

  public int getCDP() {
    return _cdp;
  }

  public void setCDP(final int cdp) {
    _cdp = cdp;
    putInteger(TraceHeaderCatalog.CDP_NO, cdp);
  }

  public float getShotpoint() {
    return _shotpoint;
  }

  public void setShotpoint(final float shotpoint) {
    _shotpoint = shotpoint;
    putFloat(TraceHeaderCatalog.SHOTPOINT_NO, shotpoint);
  }

  public float getOffset() {
    return _offset;
  }

  public void setOffset(final float offset) {
    _offset = offset;
    putFloat(TraceHeaderCatalog.OFFSET, offset);
  }
}
