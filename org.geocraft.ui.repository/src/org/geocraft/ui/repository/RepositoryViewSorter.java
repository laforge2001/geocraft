/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.ui.repository;


import org.eclipse.jface.viewers.ViewerSorter;
import org.geocraft.ui.repository.RepositoryViewCollator.SortMethod;


/**
 * Defines the sorter to be used by the repository view. The sorter is a simple numeric & alphabetic sorter
 */
public class RepositoryViewSorter extends ViewerSorter {

  private RepositoryViewCollator _collator;

  /**
   * Creates the rule-based collator needed for the repository view.
   * 
   * @return the collator.
   */
  private static RepositoryViewCollator createCollator(SortMethod sortMethod) {
    return new RepositoryViewCollator(sortMethod);
  }

  public RepositoryViewSorter() {
    this(createCollator(SortMethod.BY_ENTITY_NAME));
  }

  public RepositoryViewSorter(SortMethod sortMethod) {
    this(createCollator(sortMethod));
  }

  public RepositoryViewSorter(final RepositoryViewCollator viewCollator) {
    super(viewCollator);
    _collator = viewCollator;
  }

  public void setSortMethod(SortMethod sortMethod) {
    _collator.setSortMethod(sortMethod);
  }
}
