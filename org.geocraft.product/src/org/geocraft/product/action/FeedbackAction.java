/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.product.action;


import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.geocraft.product.intro.WelcomeView;


/**
 * A feedback action.
 */
public class FeedbackAction extends Action {

  /** The parent window. */
  private final IWorkbenchWindow _window;

  public FeedbackAction(final IWorkbenchWindow window) {
    _window = window;
    setText("Tutorial");
  }

  @Override
  public void run() {
    WelcomeView.displayURL(_window.getShell(), "http://wush.net/trac/geocraft/wiki/GeoCraftTutorial");
  }

}
