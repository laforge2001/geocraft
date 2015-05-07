/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.io;


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.geocraft.core.io.IDatastoreAccessor;
import org.geocraft.core.io.IDatastoreEntrySelections;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.internal.ui.io.MapperPropertiesPart;
import org.geocraft.ui.form2.IModelFormListener;


/**
 * Displays the data to (un)load on the left hand side of the panel and 
 * the editable mapper properties for each entity on the right side. 
 * 
 * When used to (un)load the data there is a different set of mapper
 * properties that may need to be adjusted so there are two ui panels.
 */
public class DatastoreEntitySelectionDialog extends FormDialog implements IModelFormListener {

  protected final IOMode _ioMode;

  protected final IDatastoreAccessor _datastoreAccessor;

  protected MapperPropertiesPart _masterDetailsBlock;

  protected Entity[] _entities;

  public DatastoreEntitySelectionDialog(final Shell shell, final IOMode ioMode, final Entity[] entities, final IDatastoreAccessor datastoreAccessor) {
    super(shell);
    setShellStyle(SWT.SHELL_TRIM);
    _ioMode = ioMode;
    _datastoreAccessor = datastoreAccessor;
    _entities = entities;
  }

  public IDatastoreEntrySelections getDatastoreEntryContainer() {
    return _masterDetailsBlock;
  }

  @Override
  protected void createFormContent(final IManagedForm managedForm) {
    managedForm.getForm().setText(_datastoreAccessor.getName() + " Selection");

    _masterDetailsBlock = new MapperPropertiesPart(_ioMode, _datastoreAccessor, this);
    _masterDetailsBlock.createContent(managedForm);
    _masterDetailsBlock.setEntities(_entities);
  }

  public void modelFormUpdated(String key) {
    Button button = getButton(IDialogConstants.OK_ID);
    if (button == null) {
      return;
    }
    button.setEnabled(_masterDetailsBlock.isComplete());

  }

  @Override
  public void okPressed() {
    _masterDetailsBlock.unbindModels();
    if (_ioMode.equals(IOMode.INPUT)) {
      _masterDetailsBlock.loadEntities();
    } else if (_ioMode.equals(IOMode.OUTPUT)) {
      _masterDetailsBlock.saveEntities();
    }
    super.okPressed();
  }
}
