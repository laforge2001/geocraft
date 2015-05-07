/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.common;


import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;


/**
 * A helper class for using the FormLayout.
 */
public class FormLayoutHelper {

  /** The form layout data. */
  private FormData _data = new FormData();

  /**
   * Creates a form data with the specified settings.
   * @param leftNumerator
   * @param leftOffset
   * @param rightNumerator
   * @param rightOffset
   * @param bottomNumerator
   * @param bottomOffset
   * @param topNumerator
   * @param topOffset
   * @return the form layout data
   */
  public FormData getData(int leftNumerator, int leftOffset, int rightNumerator, int rightOffset, int topNumerator,
      int topOffset, int bottomNumerator, int bottomOffset) {
    _data.left = new FormAttachment(leftNumerator, leftOffset);
    _data.right = new FormAttachment(rightNumerator, rightOffset);
    _data.bottom = new FormAttachment(bottomNumerator, bottomOffset);
    _data.top = new FormAttachment(topNumerator, topOffset);
    return _data;
  }
}
