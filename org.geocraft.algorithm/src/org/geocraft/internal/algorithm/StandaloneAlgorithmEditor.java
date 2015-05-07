/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.algorithm;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.editor.SharedHeaderFormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.geocraft.algorithm.ActiveAlgorithmRegistry;
import org.geocraft.algorithm.IStandaloneAlgorithmDescription;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.algorithm.StandaloneAlgorithmEditorInput;
import org.geocraft.algorithm.StandaloneAlgorithmEditorPage;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.form2.IModelFormListener;
import org.geocraft.ui.form2.ModelForm;


public class StandaloneAlgorithmEditor extends SharedHeaderFormEditor implements IPageChangedListener,
    IModelFormListener {

  public static final String ID = "org.geocraft.algorithm.StandaloneAlgorithmEditor";

  /** The collection of pages in the editor. */
  private StandaloneAlgorithmEditorPage _editorPage = null;

  /** The action to run the associated algorithm. */
  private RunStandaloneAlgorithm _runAction;

  /** The action to export the associated algorithm to a batch file. */
  private ExportStandaloneAlgorithmAsBatch _exportAction;

  /** The action to restore algorithm parameters from the last run. */
  private RestoreParamsAction _restoreAction;

  /** The action to collapse all sections of an algorithm. */
  private CollapseAllAction _collapseAll;

  public StandaloneAlgorithmEditor() {
    // No action.
  }

  public StandaloneAlgorithm getAlgorithm() {
    return _editorPage.getAlgorithm();
  }

  public IStandaloneAlgorithmDescription getAlgorithmDescription() {
    return ((StandaloneAlgorithmEditorInput) getEditorInput()).getAlgorithmDescription();
  }

  @Override
  protected void addPages() {
    try {
      // Get the algorithm description for the editor input.
      StandaloneAlgorithmEditorInput input = (StandaloneAlgorithmEditorInput) getEditorInput();
      IStandaloneAlgorithmDescription algorithmDescription = input.getAlgorithmDescription();
      StandaloneAlgorithm algorithm = input.getAlgorithm();

      // Set the image in the title tab.
      ImageDescriptor imageDesc = algorithmDescription.getIcon();
      if (imageDesc != null) {
        setTitleImage(imageDesc.createImage());
      }

      // Create the editor pages from the algorithm description.
      // Currently there is only 1 page per algorithm allowed.
      _editorPage = algorithmDescription.createEditorPage(this, algorithm);

      // Add the page to the editor.
      addPage(_editorPage);

      //register the active algorithm; used when saving session state
      ActiveAlgorithmRegistry.getInstance().registerAlgorithm(this);

      _runAction.setDescription(algorithmDescription, _editorPage.getAlgorithm());
      _exportAction.setDescription(algorithmDescription, _editorPage.getAlgorithm());
      _restoreAction.setAlgorithm(_editorPage.getAlgorithm());

      // Add the editor as a listener to the algorithm, so that updates to the
      // algorithm can trigger this editor to update the message manager as well.
      _editorPage.setListener(this);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void init(final IEditorSite site, final IEditorInput input) {
    try {
      super.init(site, input);
    } catch (PartInitException e) {
      e.printStackTrace();
    }
    setPartName(input.getName());
  }

  @Override
  public void setFocus() {
    super.setFocus();
    // we need this in order to have each form focused, such as the F1 key
    // to trigger the context-sensitive help
    FormPage activePage = (FormPage) getActivePageInstance();
    activePage.getManagedForm().getForm().setFocus();
  }

  @Override
  public void doSave(final IProgressMonitor monitor) {
    // TODO Auto-generated method stub
  }

  @Override
  public void doSaveAs() {
    // TODO Auto-generated method stub
  }

  @Override
  public boolean isSaveAsAllowed() {
    return false;
  }

  @Override
  public void pageChanged(final PageChangedEvent event) {
    System.out.println("page changed!");
    // TODO Auto-generated method stub

  }

  @Override
  protected void createPages() {
    super.createPages();
    // Hide the bottom tab if there is only one page.
    if (getPageCount() == 1 && getContainer() instanceof CTabFolder) {
      ((CTabFolder) getContainer()).setTabHeight(0);
    }
    modelFormUpdated("");
  }

  @Override
  protected Composite createPageContainer(final Composite parent) {
    Composite composite = super.createPageContainer(parent);
    // Set help
    IWorkbenchHelpSystem help = PlatformUI.getWorkbench().getHelpSystem();
    String contextId = ((StandaloneAlgorithmEditorInput) getEditorInput()).getAlgorithmDescription().getHelpId();
    help.setHelp(composite, contextId);
    return composite;
  }

  @Override
  protected void createHeaderContents(final IManagedForm headerForm) {
    ScrolledForm scrolledForm = headerForm.getForm();
    FormToolkit toolkit = headerForm.getToolkit();
    scrolledForm.setText(getEditorInput().getName());
    toolkit.decorateFormHeading(scrolledForm.getForm());

    // Add the run and help actions to the top of the editor.
    Shell shell = getSite().getShell();
    _runAction = new RunStandaloneAlgorithm(shell);
    _exportAction = new ExportStandaloneAlgorithmAsBatch(shell);
    _restoreAction = new RestoreParamsAction(shell);
    _collapseAll = new CollapseAllAction();
    scrolledForm.getToolBarManager().add(_collapseAll);
    scrolledForm.getToolBarManager().add(_restoreAction);
    scrolledForm.getToolBarManager().add(_runAction);
    scrolledForm.getToolBarManager().add(_exportAction);
    scrolledForm.getToolBarManager().add(new HelpAction());
    scrolledForm.getToolBarManager().update(true);

    ///addPageChangedListener(this);
  }

  /**
   * Invoked when the algorithm is updated (likely by the editor UI).
   */
  public void modelFormUpdated(String triggerKey) {
    if (_runAction != null) {
      //_editorPage.setAutoUpdate(false);
      int maxSeverity = _editorPage.getMaxSeverity();
      _runAction.setEnabled(maxSeverity != IStatus.ERROR);
      _exportAction.setEnabled(maxSeverity != IStatus.ERROR);
      //_editorPage.setAutoUpdate(true);
    }

    _restoreAction.setEnabled(_editorPage != null);
    _collapseAll.setEnabled(_editorPage != null);
    if (_editorPage != null) {
      _collapseAll.setForm((ModelForm) _editorPage.getModelForm());
    }
  }

  @Override
  public void dispose() {
    if (_editorPage != null) {
      ServiceProvider.getAlgorithmsService().remove(this.hashCode());
      _editorPage.dispose();
    }
    super.dispose();
  }
}
