/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.ui.io;


import java.util.Set;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.geocraft.core.model.mapper.IOMode;


public class DatastoreViewer extends TreeViewer {

  public DatastoreViewer(final Composite parent, final Set<String> entityClassNames, final IOMode ioMode) {
    super(parent, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
    setContentProvider(new DatastoreContentProvider(getTree(), entityClassNames, ioMode));
    setLabelProvider(new DatastoreLabelProvider());
  }
}
