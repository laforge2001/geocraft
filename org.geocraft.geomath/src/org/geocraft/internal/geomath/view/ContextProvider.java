/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.internal.geomath.view;


import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.help.IContextProvider;
import org.eclipse.jface.viewers.IStructuredSelection;


public class ContextProvider implements IContextProvider {

  private final String _fContextId;

  private final IStructuredSelection _fSelected;

  public ContextProvider(final String id, final IStructuredSelection selection) {
    _fContextId = id;
    _fSelected = selection;
  }

  public IContext getContext(final Object target) {
    if (_fSelected != null && !_fSelected.isEmpty()) {
      TreeObject object = (TreeObject) _fSelected.getFirstElement();
      if (object != null && object.getStandaloneAlgorithm() != null) {
        return HelpSystem.getContext(object.getStandaloneAlgorithm().getHelpId());
      }
    }
    return null;
  }

  public int getContextChangeMask() {
    return SELECTION;
  }

  public String getSearchExpression(final Object target) {
    return null;
  }
}