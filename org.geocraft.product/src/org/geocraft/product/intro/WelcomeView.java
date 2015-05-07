/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.product.intro;


import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IIntroSite;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;


/**
 * The welcome view.
 */
public class WelcomeView implements IIntroPart {

  /** The logger. */
  private static ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(WelcomeView.class);

  private FormToolkit _toolkit;

  public void createPartControl(final Composite container) {
    _toolkit = new FormToolkit(container.getDisplay());
    ScrolledForm form = _toolkit.createScrolledForm(container);
    form.setText("Welcome to GeoCraft");
    _toolkit.decorateFormHeading(form.getForm());

    Composite body = form.getBody();
    body.setLayout(new GridLayout());
    GridData gd = new GridData(GridData.FILL_BOTH);
    body.setLayoutData(gd);

    // create about section
    Section section = _toolkit.createSection(body, ExpandableComposite.TITLE_BAR);
    section.setLayout(new GridLayout());
    section.setText("About");
    section.setLayoutData(new GridData(GridData.FILL_BOTH));

    Composite aboutPanel = _toolkit.createComposite(section);
    TableWrapLayout layout = new TableWrapLayout();
    aboutPanel.setLayout(layout);
    aboutPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

    Label image = _toolkit.createLabel(aboutPanel, "");
    Text aboutText = new Text(aboutPanel, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
    aboutText
        .setText("GeoCraft is a framework for prototyping and deploying new geoscience applications.\n\nGeoCraft is based on the Eclipse Rich Client Platform.\n");
    image.setImage(ImageRegistryUtil.getSharedImages().getImage(ISharedImages.IMG_MAIN_LOGO));
    image.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.FILL, 1, 1));
    aboutText.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.FILL, 1, 1));
    section.setClient(aboutPanel);

    // create quick start section
    section = _toolkit.createSection(body, ExpandableComposite.TITLE_BAR);
    section.setText("Quick Start");
    section.setLayoutData(new GridData(GridData.FILL_BOTH));
    Composite quickPanel = _toolkit.createComposite(section);
    quickPanel.setLayout(new GridLayout());
    quickPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

    IPerspectiveDescriptor[] perspectives = PlatformUI.getWorkbench().getPerspectiveRegistry().getPerspectives();
    int k = 0;
    for (IPerspectiveDescriptor perspective : perspectives) {
      k++;
      final String perspectiveId = perspective.getId();
      if (!perspectiveId.equals("Viewer.perspective")) {
        Hyperlink link = _toolkit.createHyperlink(quickPanel, perspective.getLabel(), SWT.NONE);
        link.addHyperlinkListener(new HyperlinkAdapter() {

          @Override
          public void linkActivated(final HyperlinkEvent e) {
            final IWorkbench workbench = PlatformUI.getWorkbench();
            try {
              workbench.showPerspective(perspectiveId, workbench.getActiveWorkbenchWindow());
              workbench.getIntroManager().closeIntro(WelcomeView.this);
            } catch (WorkbenchException ex) {
              MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                  "Perspective error", "Error opening perspective: " + ex.getMessage());
            }
          }
        });
      }
      k++;
    }
    section.setClient(quickPanel);

    // create help section
    section = _toolkit.createSection(body, ExpandableComposite.TITLE_BAR);
    section.setText("Help");
    section.setLayoutData(new GridData(GridData.FILL_BOTH));
    StringBuffer buffer = new StringBuffer();
    buffer.append("<form>");
    buffer
        .append("<li bindent=\"10\"><a href=\"http://wush.net/trac/geocraft/wiki/GeoCraftTutorial\">Online Help / Tutorial</a></li>");
    buffer
        .append("<li bindent=\"10\"><a href=\"http://groups.google.com/group/geocraft-user\">GeoCraft Mailing List</a></li>");
    buffer.append("</form>");

    FormText text = _toolkit.createFormText(section, true);
    text.setText(buffer.toString(), true, true);
    text.setLayoutData(new GridData(GridData.FILL_BOTH));
    text.addHyperlinkListener(new HyperlinkAdapter() {

      @Override
      public void linkActivated(final HyperlinkEvent e) {
        displayURL(container.getShell(), e.data.toString());
      }
    });
    section.setClient(text);

    // create developer's resource section
    section = _toolkit.createSection(body, ExpandableComposite.TITLE_BAR);
    section.setText("Developer's Resource");
    section.setLayoutData(new GridData(GridData.FILL_BOTH));
    text = _toolkit.createFormText(section, true);

    buffer = new StringBuffer();
    buffer.append("<form>");
    buffer.append("<li bindent=\"10\"><a href=\"http://wush.net/trac/geocraft/wiki\">GeoCraft Wiki</a></li>");
    buffer
        .append("<li bindent=\"10\"><a href=\"http://groups.google.com/group/geocraft-user\">GeoCraft Mailing List</a></li>");
    buffer
        .append("<li bindent=\"10\"><a href=\"http://groups.google.com/group/geocraft-trac\">Trac Update Notification</a></li>");
    buffer.append("</form>");

    text.setText(buffer.toString(), true, true);
    text.setLayoutData(new GridData(GridData.FILL_BOTH));
    text.addHyperlinkListener(new HyperlinkAdapter() {

      @Override
      public void linkActivated(final HyperlinkEvent e) {
        displayURL(container.getShell(), e.data.toString());
      }
    });

    section.setClient(text);
    form.pack();
  }

  @Override
  public void addPropertyListener(final IPropertyListener listener) {
    // TODO Auto-generated method stub

  }

  @Override
  public void dispose() {
    if (_toolkit != null) {
      _toolkit.dispose();
      _toolkit = null;
    }
  }

  @Override
  public IIntroSite getIntroSite() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getTitle() {
    return "Welcome";
  }

  @Override
  public Image getTitleImage() {
    return ImageRegistryUtil.getSharedImages().getImage(ISharedImages.IMG_SMALL_LOGO);
  }

  @Override
  public void init(final IIntroSite site, final IMemento memento) {
    // TODO Auto-generated method stub

  }

  @Override
  public void removePropertyListener(final IPropertyListener listener) {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveState(final IMemento memento) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setFocus() {
    // TODO Auto-generated method stub

  }

  @Override
  public void standbyStateChanged(final boolean standby) {
    // TODO Auto-generated method stub

  }

  @Override
  public Object getAdapter(final Class adapter) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Display the specified URL in a browser.
   * @param parent the parent shell
   * @param address the URL
   */
  public static void displayURL(final Shell parent, final String address) {
    try {
      IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
      IWebBrowser browser = support.getExternalBrowser();
      URL url = new URL(address);
      browser.openURL(url);
    } catch (PartInitException e) {
      LOGGER.warn("Could not start browser", e);
    } catch (MalformedURLException e) {
      MessageDialog.openWarning(parent, "Browser", "Could not open the web browser at this page " + address);
      LOGGER.warn("Malformed URL", e);
    }
  }
}
