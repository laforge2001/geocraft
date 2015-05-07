/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.ui.io;


import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.geocraft.core.io.IDatastoreAccessor;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.ui.form2.AbstractModelView;
import org.geocraft.ui.form2.IModelFormListener;


public class MapperPropertiesDetailsPage implements IDetailsPage {

  protected IOMode _ioMode;

  protected IDatastoreAccessor _datastoreAccessor;

  protected IManagedForm _managedForm;

  private AbstractModelView _modelView;

  private IModelFormListener _modelFormListener;

  public MapperPropertiesDetailsPage(final IOMode ioMode, final IDatastoreAccessor datastoreAccessor, final IManagedForm form, IModelFormListener listener) {
    _ioMode = ioMode;
    _datastoreAccessor = datastoreAccessor;
    _managedForm = form;
    _modelFormListener = listener;
  }

  public void createContents(final Composite parent) {
    _modelView = ServiceComponent.getDatastoreAccessorUIService().getModelView(_datastoreAccessor, _ioMode);
    _modelView.buildView(parent, _managedForm);//, false);
    _modelView.getModelForm().addListener(_modelFormListener);
    ///_view.build(_context, _form, parent);
    _modelView.collapseSections();
    _modelView.expandSections();
  }

  public void commit(final boolean onSave) {
    // not needed
  }

  public void refresh() {
    // Nothing to do.
  }

  public boolean isDirty() {
    return false;
  }

  public boolean isStale() {
    return false;
  }

  public void setFocus() {
    // Nothing to do.
  }

  public boolean setFormInput(final Object input) {
    return false;
  }

  public void dispose() {
    // Nothing to do.
  }

  public void initialize(final IManagedForm form) {
    // Nothing to do.
  }

  public void selectionChanged(final IFormPart part, final ISelection selection) {
    if (selection != null) {
      StructuredSelection structuredSelection = (StructuredSelection) selection;
      Object element = structuredSelection.getFirstElement();
      if (element instanceof Model) {
        _modelView.setModel((MapperModel) element);
      }
    }
  }
}
