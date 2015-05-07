/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.geocraft.core.io.IDatastoreAccessor;
import org.geocraft.core.io.IDatastoreAccessorService;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.core.model.mapper.InMemoryMapper;
import org.geocraft.core.model.mapper.InMemoryMapperModel;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.common.TableWrapLayoutHelper;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.form2.AbstractModelView;
import org.geocraft.ui.form2.DatastorePropertyData;
import org.geocraft.ui.form2.IDatastoreAccessorUIService;
import org.geocraft.ui.form2.ServiceComponent;


public class DatastoreComboField extends ComboField {

  private Class _entityClass;

  private Button _editButton;

  public DatastoreComboField(final Composite parent, IFieldListener listener, String key, final String label, final boolean showToggle, final Class entityClass) {
    super(parent, listener, key, label, showToggle, true);
    _entityClass = entityClass;
  }

  @Override
  public Control[] createControls(final Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    container.setLayoutData(TableWrapLayoutHelper.createLayoutData(true, true, SWT.FILL, SWT.FILL, 1, 1));
    container.setLayout(TableWrapLayoutHelper.createLayout(2, false));
    super.createControls(container);
    updateOptions();

    _combo.addListener(SWT.Selection, new Listener() {

      @Override
      public void handleEvent(final Event event) {
        _editButton.setEnabled(_combo.getSelectionIndex() > 0);
      }

    });

    _editButton = new Button(container, SWT.PUSH);
    _editButton.setEnabled(false);
    _editButton.setLayoutData(TableWrapLayoutHelper.createLayoutData(false, true, SWT.FILL, SWT.FILL, 1, 1));
    //_editButton.setText("Edit...");
    _editButton.setImage(ImageRegistryUtil.getSharedImages().getImage(ISharedImages.IMG_EDIT));
    _editButton.addListener(SWT.Selection, new Listener() {

      public void handleEvent(Event event) {
        System.out.println("Editing " + _combo.getItem(_combo.getSelectionIndex()));
        int index = _combo.getSelectionIndex();
        if (index >= 1 && index < _combo.getItemCount()) {
          DatastorePropertyData[] data = (DatastorePropertyData[]) _combo.getData();
          IDatastoreAccessorUIService service = ServiceComponent.getDatastoreAccessorUIService();
          AbstractModelView view = service.getModelView(data[index]._accessor, IOMode.OUTPUT);
          System.out.println("editing data: " + data[index] + " model: " + data[index]._model);
          DatastoreModelEditorDialog dialog = new DatastoreModelEditorDialog(_combo.getShell(), data[index]._accessor,
              view, data[index]._model);
          dialog.open();
        }
      }

    });
    return new Control[] { container };
  }

  private void updateOptions() {
    //    if (true) {
    //      return;
    //    }
    Display.getDefault().asyncExec(new Runnable() {

      public void run() {
        IDatastoreAccessorService service = ServiceProvider.getDatastoreAccessorService();
        IDatastoreAccessor[] accessors = service.getDatastoreAccessors(new Class[] { _entityClass }, IOMode.OUTPUT);
        DatastorePropertyData[] data = new DatastorePropertyData[1 + accessors.length];
        data[0] = new DatastorePropertyData(null, new InMemoryMapperModel(), new InMemoryMapper(_entityClass));
        for (int i = 0; i < accessors.length; i++) {
          data[i + 1] = new DatastorePropertyData(accessors[i]);
        }
        setOptions(data);
        _combo.select(0);
        _combo.setData(data);
      }
    });
  }
}
