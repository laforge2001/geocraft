/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.common;


import org.eclipse.swt.SWT;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;


public class TableWrapLayoutHelper {

  public static TableWrapLayout createLayout(final int numColumns, final boolean makeColumnsEqualWidth) {
    return createLayout(numColumns, makeColumnsEqualWidth, 5, 5);
  }

  public static TableWrapLayout createLayout(final int numColumns, final boolean makeColumnsEqualWidth,
      final int horizontalSpacing, final int verticalSpacing) {
    return createLayout(numColumns, makeColumnsEqualWidth, horizontalSpacing, verticalSpacing, 0, 0, 0, 0);
  }

  public static TableWrapLayout createLayout(final int numColumns, final boolean makeColumnsEqualWidth,
      final int horizontalSpacing, final int verticalSpacing, final int marginTop, final int marginLeft,
      final int marginRight, final int marginBottom) {
    TableWrapLayout layout = new TableWrapLayout();
    layout.numColumns = numColumns;
    layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
    layout.horizontalSpacing = horizontalSpacing;
    layout.verticalSpacing = verticalSpacing;
    layout.topMargin = marginTop;
    layout.leftMargin = marginLeft;
    layout.rightMargin = marginRight;
    layout.bottomMargin = marginBottom;
    return layout;
  }

  public static TableWrapData createLayoutData(final boolean grabExcessHorizontalSpace,
      final boolean grabExessVerticalSpace, final int horizontalAlignment, final int verticalAlignment) {
    return createLayoutData(grabExcessHorizontalSpace, grabExessVerticalSpace, horizontalAlignment, verticalAlignment,
        1, 1, SWT.DEFAULT, SWT.DEFAULT);
  }

  public static TableWrapData createLayoutData(final boolean grabExcessHorizontalSpace,
      final boolean grabExessVerticalSpace, final int horizontalAlignment, final int verticalAlignment,
      final int horizontalSpan, final int verticalSpan) {
    return createLayoutData(grabExcessHorizontalSpace, grabExessVerticalSpace, horizontalAlignment, verticalAlignment,
        horizontalSpan, verticalSpan, SWT.DEFAULT, SWT.DEFAULT);
  }

  public static TableWrapData createLayoutData(final boolean grabExcessHorizontalSpace,
      final boolean grabExessVerticalSpace, final int horizontalAlignment, final int verticalAlignment,
      final int horizontalSpan, final int verticalSpan, final int maxWidth, final int heightHint) {
    TableWrapData data = new TableWrapData();
    data.grabHorizontal = grabExcessHorizontalSpace;
    data.grabVertical = grabExessVerticalSpace;
    data.align = horizontalAlignment;
    data.valign = verticalAlignment;
    data.colspan = horizontalSpan;
    data.rowspan = verticalSpan;
    data.maxWidth = maxWidth;
    data.heightHint = heightHint;
    return data;
  }
}
