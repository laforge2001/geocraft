package org.geocraft.ui.volumeviewer.preference;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.geocraft.core.common.preferences.OverlayPage;
import org.geocraft.core.common.preferences.PropertyStore;
import org.geocraft.core.common.preferences.PropertyStoreFactory;
import org.geocraft.core.preferences.IGeocraftPreferencePage;


/**
 * A preference page for the 3d viewer related preference pages.
 */
public abstract class AbstractViewerPreferencePage extends OverlayPage implements IWorkbenchPreferencePage,
    IGeocraftPreferencePage {

  /** The bundle id. */
  public static final String ID = "org.geocraft.ui.viewer";

  /** The preferences store. */
  private final PropertyStore _store = PropertyStoreFactory.getStore(getPageId());

  public AbstractViewerPreferencePage() {
    // TODO Auto-generated constructor stubPreferencePage
  }

  public AbstractViewerPreferencePage(final String title) {
    super(title);
  }

  public AbstractViewerPreferencePage(final String title, final ImageDescriptor image) {
    super(title, image);
  }

  protected IPreferenceStore getStore() {
    return _store;
  }

  /**
   * Create the preference page main panel.
   * @param parent the parent composite
   * @return the panel
   */
  protected Composite createMainPanel(final Composite parent) {
    final Composite mainPanel = new Composite(parent, SWT.NONE);
    final GridLayout layout = new GridLayout();
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    layout.verticalSpacing = 10;
    mainPanel.setLayout(layout);
    final GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
    mainPanel.setLayoutData(data);

    return mainPanel;
  }

  /**
   * Create a buttons group.
   * 
   * @param parent composite
   * @param title the group title
   */
  protected Group createGroup(final Composite parent, final String title) {
    final Group buttonGroup = new Group(parent, SWT.NONE);
    buttonGroup.setText(title);
    final GridLayout layout = new GridLayout();
    buttonGroup.setLayout(layout);
    buttonGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    return buttonGroup;
  }

  /**
   * Set the grid layout data for the specified controls. 
   * @param label the label
   * @param other the other control
   * @param otherWidth the other control preferred width
   */
  protected void setFieldEditorLayout(final Label label, final Control other, final int otherWidth) {
    GridData data = new GridData();
    data.horizontalIndent = 10;
    data.verticalIndent = 5;
    label.setLayoutData(data);
    data = new GridData();
    data.horizontalIndent = 10;
    data.verticalIndent = 5;
    data.widthHint = otherWidth;
    data.verticalAlignment = SWT.CENTER;
    other.setLayoutData(data);
  }

  @Override
  @SuppressWarnings("unused")
  public void init(final IWorkbench workbench) {
    // TODO Auto-generated method stub
  }

  @Override
  public void setErrorMessage(final String message) {
    super.setErrorMessage(message);
    setValid(getErrorMessage() == null);
  }

  @Override
  public String getPageId() {
    return ID;
  }

}
