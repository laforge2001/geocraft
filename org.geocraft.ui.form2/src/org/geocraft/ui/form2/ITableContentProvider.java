/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2;


import org.eclipse.jface.viewers.IStructuredContentProvider;


public interface ITableContentProvider extends IStructuredContentProvider {

  /**
   * 
   * @param o
   */
  public void setInput(Object o);

  public Object getInput();

}
