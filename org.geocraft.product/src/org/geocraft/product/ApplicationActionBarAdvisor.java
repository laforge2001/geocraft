/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.product;


import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.views.IViewDescriptor;
import org.geocraft.product.action.FeedbackAction;
import org.geocraft.product.action.PerspectiveAction;
import org.geocraft.product.action.ResetPerspectiveAction;
import org.geocraft.product.action.WindowAction;


/**
 * An action bar advisor is responsible for creating, adding, and disposing of the actions 
 * added to a workbench window. Each window will be populated with new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

  // Actions - important to allocate these only in makeActions, and then use them
  // in the fill methods. This ensures that the actions aren't recreated
  // when fillActionBars is called with FILL_PROXY.
  private IWorkbenchAction exitAction;

  private IWorkbenchAction _showHelpAction;

  private IWorkbenchAction _searchHelpAction;

  private IWorkbenchAction _dynamicHelpAction;

  private IWorkbenchAction _intro;

  private IWorkbenchAction aboutAction;

  private IWorkbenchWindow _window;

  private IContributionItem _viewItems;

  private IWorkbenchAction _preferencesAction;

  private IWorkbenchAction _copy;

  private IWorkbenchAction _paste;

  private RetargetAction _pasteAsScript;

  private IWorkbenchAction _undo;

  private IWorkbenchAction _redo;

  //  private IWorkbenchAction _switchWorkspace;

  public ApplicationActionBarAdvisor(final IActionBarConfigurer configurer) {
    super(configurer);
  }

  @Override
  protected void makeActions(final IWorkbenchWindow window) {
    // Creates the actions and registers them.
    // Registering is needed to ensure that key bindings work.
    // The corresponding commands keybindings are defined in the plugin.xml file.
    // Registering also provides automatic disposal of the actions when
    // the window is closed.
    exitAction = ActionFactory.QUIT.create(window);
    register(exitAction);

    _showHelpAction = ActionFactory.HELP_CONTENTS.create(window);
    _searchHelpAction = ActionFactory.HELP_SEARCH.create(window);
    _dynamicHelpAction = ActionFactory.DYNAMIC_HELP.create(window);

    if (window.getWorkbench().getIntroManager().hasIntro()) {
      _intro = ActionFactory.INTRO.create(window);
      register(_intro);
    }
    aboutAction = ActionFactory.ABOUT.create(window);
    register(aboutAction);
    _window = window;
    _viewItems = ContributionItemFactory.VIEWS_SHORTLIST.create(window);
    _preferencesAction = ActionFactory.PREFERENCES.create(window);
    register(_preferencesAction);
    // Copy, Paste and Paste as Script retarget actions
    // They are enabled when the Shell view is active
    _copy = ActionFactory.COPY.create(window);
    register(_copy);
    _paste = ActionFactory.PASTE.create(window);
    register(_paste);
    _pasteAsScript = new RetargetAction("org.geocraft.product.shell.pasteAsScript", "Paste as Script");
    window.getPartService().addPartListener(_pasteAsScript);
    register(_pasteAsScript);
    _undo = ActionFactory.UNDO.create(window);
    register(_undo);
    _redo = ActionFactory.REDO.create(window);
    register(_redo);
    /* File > Switch Session no longer supported
        _switchWorkspace = new OpenWorkspaceAction(window);
        _switchWorkspace.setId("openWorkspace");
        register(_switchWorkspace);
    */
  }

  @Override
  protected void fillMenuBar(final IMenuManager menuBar) {
    MenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
    MenuManager editMenu = new MenuManager("&Edit", IWorkbenchActionConstants.M_EDIT);
    MenuManager viewMenu = new MenuManager("&View", "plotview");
    MenuManager perspectiveMenu = new MenuManager("&Perspectives", "perspectives");
    MenuManager windowMenu = new MenuManager("&Window", "windows");
    MenuManager helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
    menuBar.add(fileMenu);
    menuBar.add(editMenu);
    menuBar.add(viewMenu);
    menuBar.add(perspectiveMenu);
    menuBar.add(windowMenu);
    // Add a group marker indicating where action set menus will appear.
    menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    menuBar.add(helpMenu);
    // File
    fileMenu.add(new Separator());
    fileMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    //    fileMenu.add(new Separator());
    //    fileMenu.add(_switchWorkspace);
    //    fileMenu.add(new Separator());
    fileMenu.add(exitAction);
    // Edit
    editMenu.add(_copy);
    editMenu.add(_paste);
    editMenu.add(_pasteAsScript);
    editMenu.add(_undo);
    editMenu.add(_redo);
    editMenu.add(new Separator());
    editMenu.add(_preferencesAction);
    editMenu.add(new Separator());
    editMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    // View
    viewMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    // Perspective
    IPerspectiveDescriptor[] perspectives = PlatformUI.getWorkbench().getPerspectiveRegistry().getPerspectives();
    for (IPerspectiveDescriptor perspective : perspectives) {
      perspectiveMenu.add(new PerspectiveAction(_window, perspective.getId(), perspective));
    }
    perspectiveMenu.add(new Separator());
    perspectiveMenu.add(new ResetPerspectiveAction(_window));
    // Window
    IViewDescriptor[] views = PlatformUI.getWorkbench().getViewRegistry().getViews();
    for (IViewDescriptor view : views) {
      if (view.isRestorable()) {
        windowMenu.add(new WindowAction(_window, view.getId(), view));
      }
    }
    windowMenu.add(_viewItems);
    // Help
    if (_intro != null) {
      helpMenu.add(_intro);
    }
    helpMenu.add(new Separator());
    helpMenu.add(new FeedbackAction(_window));
    helpMenu.add(new Separator());
    helpMenu.add(_showHelpAction);
    helpMenu.add(_searchHelpAction);
    helpMenu.add(_dynamicHelpAction);
    helpMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    helpMenu.add(new Separator());
    helpMenu.add(aboutAction);
  }

  @Override
  //  @SuppressWarnings("unused")
  protected void fillCoolBar(final ICoolBarManager coolBar) {
    // IToolBarManager toolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
    // coolBar.add(new ToolBarContributionItem(toolbar, "main"));
  }

}
