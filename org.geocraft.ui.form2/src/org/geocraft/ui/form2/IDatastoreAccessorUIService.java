/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2;


import org.geocraft.core.io.IDatastoreAccessor;
import org.geocraft.core.model.mapper.IOMode;


public interface IDatastoreAccessorUIService {

  AbstractModelView getModelView(IDatastoreAccessor datastoreAccessor, final IOMode ioMode);
}
