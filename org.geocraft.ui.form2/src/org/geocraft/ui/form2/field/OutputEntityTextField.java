/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.property.OutputEntityProperty;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.common.TableWrapLayoutHelper;


public class OutputEntityTextField extends AbstractField {

  /** The text field control. */
  protected Text _text;

  protected Text _commentsBox;

  /**The drop target associated with the entity text field. */
  private DropTarget _dropTarget;

  /**
   * Constructs a parameter text field.
   * 
   * @param parent the parent composite.
   * @param parameter the parameter key.
   * @param label the parameter label.
   * @param showToggle <i>true</i> to show a parameter toggle button; otherwise <i>false</i>.
   */
  public OutputEntityTextField(final Composite parent, IFieldListener listener, final String key, final String label, final boolean showToggle) {
    super(parent, listener, key, label, showToggle);
    initDragAndDrop();
  }

  @Override
  public Control[] createControls(final Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    TableWrapData layoutData = TableWrapLayoutHelper.createLayoutData(true, false, TableWrapData.FILL,
        TableWrapData.FILL);
    layoutData.maxWidth = 150;
    container.setLayoutData(layoutData);

    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    layout.makeColumnsEqualWidth = false;
    container.setLayout(layout);

    int style = SWT.BORDER;
    _text = new Text(container, style);
    _text.setText("");
    GridData gridData = new GridData();
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessVerticalSpace = false;
    _text.setLayoutData(gridData);
    _text.addListener(SWT.KeyUp, new Listener() {

      public void handleEvent(final Event event) {
        String valueObject = _text.getText() + OutputEntityProperty.NAME_COMMENT_SEPARATOR + _commentsBox.getText();
        _listener.fieldChanged(_key, valueObject);
      }
    });

    _commentsBox = new Text(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.WRAP);
    gridData = new GridData();
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalAlignment = SWT.FILL;
    gridData.grabExcessVerticalSpace = true;
    gridData.verticalAlignment = SWT.FILL;
    gridData.verticalSpan = 3;
    gridData.heightHint = 100;
    _commentsBox.setLayoutData(gridData);
    _commentsBox.addListener(SWT.KeyUp, new Listener() {

      public void handleEvent(final Event event) {
        String valueObject = _text.getText() + OutputEntityProperty.NAME_COMMENT_SEPARATOR + _commentsBox.getText();
        _listener.fieldChanged(_key, valueObject);
      }
    });
    return new Control[] { _text, _commentsBox };
  }

  @Override
  public void updateField(Object valueObject) {
    if (valueObject != null) {
      String text = valueObject.toString();
      if (text.contains(OutputEntityProperty.NAME_COMMENT_SEPARATOR)) {
        String[] substrings = text.split(OutputEntityProperty.NAME_COMMENT_SEPARATOR);
        _text.setText(substrings[0]);
        _text.setSelection(substrings[0].length());
        if (substrings.length > 1) {
          _commentsBox.setText(substrings[1]);
          _commentsBox.setSelection(substrings[1].length());
        }
      } else {
        _text.setText(text);
        _text.setSelection(text.length());
      }
    } else {
      _text.setText("");
      _commentsBox.setText("");
    }
    setInternalStatus(ValidationStatus.ok());
  }

  protected void setText(String text) {
    _text.setText(text);
    _listener.fieldChanged(_key, text);
  }

  /**
   * Initializes the drag-and-drop behavior for the entity text field.
   */
  private void initDragAndDrop() {
    _dropTarget = new DropTarget(_text, DND.DROP_COPY | DND.DROP_MOVE);
    _dropTarget.setTransfer(new Transfer[] { TextTransfer.getInstance() });
    _dropTarget.addDropListener(new DropTargetAdapter() {

      @Override
      public void dragOver(DropTargetEvent event) {
        event.detail = DND.DROP_COPY;
      }

      @Override
      public void drop(final DropTargetEvent event) {
        if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
          // Initialize a list of entities.
          List<Entity> entities = new ArrayList<Entity>();

          // Get the string containing the variable names.
          String vars = (String) event.data;

          // Iterate thru the variable names.
          Scanner scanner = new Scanner(vars).useDelimiter(",");
          while (scanner.hasNext()) {
            // Lookup each entity in the repository based on the variable name.
            String item = scanner.next();
            IRepository repository = ServiceProvider.getRepository();
            Entity entity = (Entity) repository.get(item);

            // If an entity is found, add it to the list.
            if (entity != null) {
              entities.add(entity);
            }
          }
          if (entities.size() == 0) {
            setText((String) event.data);
          }

          // Take the name of the 1st entity and put it into the text field.
          for (Entity entity : entities) {
            setText(entity.getDisplayName());
            break;
          }
        }
      }
    });
  }

  @Override
  public synchronized void dispose() {
    // Be sure to dispose of the drop target.
    if (_dropTarget != null && !_dropTarget.isDisposed()) {
      _dropTarget.dispose();
    }
    super.dispose();
  }
}
