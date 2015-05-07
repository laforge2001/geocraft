/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.las.table;


import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.TableItem;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.io.las.LasColumnModelArrayProperty;
import org.geocraft.io.las.LasMnemonicDescriptionModel;
import org.geocraft.ui.form2.field.TableField;


public class LasTableCellModifier implements ICellModifier {

  private TableField _tf;

  public LasTableCellModifier(TableField tf) {
    super();
    _tf = tf;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
   */
  /**
   * @param element  
   */
  @Override
  public boolean canModify(Object element, String property) {

    int index = _tf.getColumnNames().indexOf(property);

    switch (index) {
      case 0: //Can Load
        return true;
      case 1: // Mnemonic
        return false;
      case 2: // Log Description
        return false;
      case 3: // LAS Units from file
        return false;
      case 4: // Interpreted Units
        return true;
      case 5: // Name
        return false;
      default:
        return false;
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
   */
  @Override
  public Object getValue(Object element, String property) {
    Object result = null;
    LasMnemonicDescriptionModel task = (LasMnemonicDescriptionModel) element;

    int index = _tf.getColumnNames().indexOf(property);

    switch (index) {
      case 0:
        result = new Boolean(task.getCanLoad());
        break;
      case 1:
        result = task.getMnemonic();
        break;
      case 2:
        result = task.getDescription();
        break;
      case 3:
        result = task.getFileUnit();
        break;
      case 4:
        String stringValue = task.getInterpUnit().toString();
        String[] choices = Unit.getListOfAllNames();
        int i = choices.length - 1;
        while (!stringValue.equals(choices[i]) && i > 0) {
          --i;
        }
        result = new Integer(i);
        break;
      case 5:
        result = task.getName();
        break;
      default:
        result = "";
    }
    return result;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
   */
  @Override
  public void modify(Object element, String property, Object value) {
    // Find the index of the column 
    int columnIndex = _tf.getColumnNames().indexOf(property);

    TableItem item = (TableItem) element;
    if (item != null) {
      LasMnemonicDescriptionModel task = (LasMnemonicDescriptionModel) item.getData();
      String valueString;

      switch (columnIndex) {
        case 0: // Can Load  
          task.setValueObject(LasMnemonicDescriptionModel.CAN_LOAD, ((Boolean) value).booleanValue());
          break;
        case 1: //Mnemonic
          break;
        case 2: // Log Description
          break;
        case 3: // LAS unit from file
          break;
        case 4: // Interpreted Unit
          valueString = Unit.getListOfAllNames()[(Integer) value];
          //        valueString = tableViewerExample.getChoices(property)[((Integer) value).intValue()].trim();

          if (!task.getInterpUnit().toString().equals(valueString)) {
            task.setValueObject(LasMnemonicDescriptionModel.INTERP_UNITS, Unit.lookupByName(valueString));
          }
          break;
        case 5: // Name 
          valueString = ((String) value).trim();
          if (valueString.length() == 0) {
            valueString = "0";
          }
          task.setValueObject(LasMnemonicDescriptionModel.NAME, valueString);
          break;
        default:
      }
      ((LasColumnModelArrayProperty) _tf.getContentProvider().getInput()).updateModel(task);

    }
  }

}
