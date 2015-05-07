/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.common;


import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;


public class GridLayoutHelper {

  public static GridLayout createLayout(final int numColumns, final boolean makeColumnsEqualWidth) {
    return createLayout(numColumns, makeColumnsEqualWidth, 5, 5);
  }

  public static GridLayout createLayout(final int numColumns, final boolean makeColumnsEqualWidth,
      final int horizontalSpacing, final int verticalSpacing) {
    return createLayout(numColumns, makeColumnsEqualWidth, horizontalSpacing, verticalSpacing, 0, 0, 0, 0);
  }

  public static GridLayout createLayout(final int numColumns, final boolean makeColumnsEqualWidth,
      final int horizontalSpacing, final int verticalSpacing, final int marginTop, final int marginLeft,
      final int marginRight, final int marginBottom) {
    GridLayout layout = new GridLayout();
    layout.numColumns = numColumns;
    layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
    layout.horizontalSpacing = horizontalSpacing;
    layout.verticalSpacing = verticalSpacing;
    layout.marginTop = marginTop;
    layout.marginLeft = marginLeft;
    layout.marginRight = marginRight;
    layout.marginBottom = marginBottom;
    return layout;
  }

  public static GridData createLayoutData(final boolean grabExcessHorizontalSpace,
      final boolean grabExessVerticalSpace, final int horizontalAlignment, final int verticalAlignment) {
    return createLayoutData(grabExcessHorizontalSpace, grabExessVerticalSpace, horizontalAlignment, verticalAlignment,
        1, 1, SWT.DEFAULT, SWT.DEFAULT);
  }

  public static GridData createLayoutData(final boolean grabExcessHorizontalSpace,
      final boolean grabExessVerticalSpace, final int horizontalAlignment, final int verticalAlignment,
      final int horizontalSpan, final int verticalSpan) {
    return createLayoutData(grabExcessHorizontalSpace, grabExessVerticalSpace, horizontalAlignment, verticalAlignment,
        horizontalSpan, verticalSpan, SWT.DEFAULT, SWT.DEFAULT);
  }

  public static GridData createLayoutData(final boolean grabExcessHorizontalSpace,
      final boolean grabExessVerticalSpace, final int horizontalAlignment, final int verticalAlignment,
      final int horizontalSpan, final int verticalSpan, final int widthHint, final int heightHint) {
    GridData data = new GridData();
    data.grabExcessHorizontalSpace = grabExcessHorizontalSpace;
    data.grabExcessVerticalSpace = grabExessVerticalSpace;
    data.horizontalAlignment = horizontalAlignment;
    data.verticalAlignment = verticalAlignment;
    data.horizontalSpan = horizontalSpan;
    data.verticalSpan = verticalSpan;
    data.widthHint = widthHint;
    data.heightHint = heightHint;
    return data;
  }
}
