/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.las.table;


import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.io.las.LasMnemonicDescriptionModel;


public class LasTableLabelProvider extends StyledCellLabelProvider {

  //We use icons
  private static final Image CHECKED = AbstractUIPlugin.imageDescriptorFromPlugin("org.geocraft.io.las",
      "icons/checked.gif").createImage();

  private static final Image UNCHECKED = AbstractUIPlugin.imageDescriptorFromPlugin("org.geocraft.io.las",
      "icons/unchecked.gif").createImage();

  private static final Color ERROR_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

  private static final Color DEFAULT_COLOR = null;

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
   */
  private Image getColumnImage(Object element, int columnIndex) {
    // In case you don't like image just return null here
    if (columnIndex == 0) {
      if (((LasMnemonicDescriptionModel) element).getCanLoad()) {
        return CHECKED;
      }
      return UNCHECKED;
    }

    return null;

  }

  @Override
  /**
   * currently this is where validation for each cell happens
   */
  public void update(ViewerCell cell) {
    LasMnemonicDescriptionModel element = (LasMnemonicDescriptionModel) cell.getElement();
    int index = cell.getColumnIndex();
    String columnText = getColumnText(element, index);
    cell.setText(columnText);
    cell.setImage(getColumnImage(element, index));

    if (element.getInterpUnit().equals(Unit.UNDEFINED)) {
      if (index == 4) {
        cell.setBackground(ERROR_COLOR);
      }
    } else {
      cell.setBackground(DEFAULT_COLOR);
    }

    super.update(cell);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
   */
  private String getColumnText(Object element, int columnIndex) {
    Object result = null;
    if (element instanceof LasMnemonicDescriptionModel) {
      LasMnemonicDescriptionModel model = (LasMnemonicDescriptionModel) element;
      switch (columnIndex) {
        case 0:
          break;
        case 1:
          result = model.getMnemonic();
          break;
        case 2:
          result = model.getDescription();
          break;
        case 3:
          result = model.getFileUnit();
          break;
        case 4:
          result = model.getInterpUnit();
          break;
        case 5:
          result = model.getName();
          break;
        default:
          break;
      }
    }

    return result != null ? result.toString() : "";
  }

}
