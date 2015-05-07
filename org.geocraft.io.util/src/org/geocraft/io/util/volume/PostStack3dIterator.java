/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util.volume;


import java.util.Iterator;


/**
 * The abstract base class for all PostStack3d trace iterators.
 */
public abstract class PostStack3dIterator<T extends TraceBlock> implements Iterator<T> {

  public void remove() {
    throw new UnsupportedOperationException("Cannot remove traces.");
  }

  public abstract int getCompletion();

  public abstract String getMessage();

  public abstract int getTotalWork();
}
