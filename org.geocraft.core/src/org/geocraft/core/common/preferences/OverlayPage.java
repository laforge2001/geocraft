/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.preferences;


import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;


public abstract class OverlayPage extends PreferencePage implements IPropertyChangeListener {

  // Additional buttons for property pages
  private Button _useWorkspaceSettingsButton, _useProjectSettingsButton;

  // Overlay preference store for property pages
  private PropertyStore _overlayStore;

  // Container for subclass controls
  private Composite _contents;

  private final ArrayList<FieldEditor> _editors = new ArrayList<FieldEditor>();

  /**
   * Constructor
   */
  public OverlayPage() {
    super();
    initializeOverlayStore();
  }

  /**
   * Constructor
   * @param title - title string
   */
  public OverlayPage(final String title) {
    super();
    setTitle(title);
    initializeOverlayStore();
  }

  /**
   * Constructor
   * @param title - title string
   * @param image - title image
   */
  public OverlayPage(final String title, final ImageDescriptor image) {
    super();
    setTitle(title);
    setImageDescriptor(image);
    initializeOverlayStore();
  }

  /**
   * 
   */
  private void initializeOverlayStore() {
    _overlayStore = PropertyStoreFactory.getStore(getPageId());
    setPreferenceStore(_overlayStore);
    _overlayStore.addPropertyChangeListener(this);
  }

  /**
   * Returns true if this instance represents a property page
   * @return - true for property pages, false for preference pages
   */
  public boolean isPropertyPage() {
    return _overlayStore.useProjectSettings();
  }

  /**
   * Create a buttons group.
   * 
   * @param parent composite
   * @param title the group title
   */
  private Group createGroup(final Composite parent, final String title) {
    Group buttonGroup = new Group(parent, SWT.NONE);
    buttonGroup.setText(title);
    GridLayout layout = new GridLayout();
    buttonGroup.setLayout(layout);
    buttonGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    return buttonGroup;
  }

  /**
   *  We need to implement createContents method. In case of property pages we insert two radio buttons
   * at the top of the page. Below this group we create a new composite for the contents
   * created by subclasses.
   * 
   * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createContents(final Composite parent) {
    createSelectionGroup(parent);
    _contents = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    _contents.setLayout(layout);
    _contents.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    return _contents;
  }

  /**
   * Creates and initializes a selection group with two choice buttons and one push button.
   * @param parent - the parent composite
   */
  private void createSelectionGroup(final Composite parent) {
    Group selectionGroup = createGroup(parent, "Preference Scope");
    Composite comp = new Composite(selectionGroup, SWT.NONE);
    GridLayout layout = new GridLayout(2, false);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    comp.setLayout(layout);
    comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    Composite radioGroup = new Composite(comp, SWT.NONE);
    radioGroup.setLayout(new GridLayout());
    radioGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    _useWorkspaceSettingsButton = createRadioButton(radioGroup, "Enable Global Settings");
    _useProjectSettingsButton = createRadioButton(radioGroup, "Enable Project-Specific Settings");

    String use = _overlayStore.getString(PropertyStore.USE_PROJECT_SETTINGS);
    if ("true".equals(use)) {
      _useProjectSettingsButton.setSelection(true);
    } else {
      _useWorkspaceSettingsButton.setSelection(true);
    }
  }

  /**
   * In case of property pages we create a new PropertyStore as local overlay store.
   * After all controls have been create, we enable/disable these controls
   * 
   * @see org.eclipse.jface.preference.PreferencePage#createControl()
   */
  @Override
  public void createControl(final Composite parent) {
    super.createControl(parent);
    setControlsEnabled();
  }

  /**
   * Returns the id of the current preference page as defined in plugin.xml
   * Subclasses must implement. 
   * 
   * @return - the qualifier
   */
  protected abstract String getPageId();

  /* 
   * Returns in case of property pages the overlay store - otherwise the standard preference store
   * @see org.eclipse.jface.preference.PreferencePage#getPreferenceStore()
   */
  @Override
  public IPreferenceStore getPreferenceStore() {
    return _overlayStore;
  }

  /**
   * Convenience method creating a radio button
   * @param parent - the parent composite
   * @param label - the button label
   * @return - the new button
   */
  private Button createRadioButton(final Composite parent, final String label) {
    final Button button = new Button(parent, SWT.RADIO);
    button.setText(label);
    button.addSelectionListener(new SelectionAdapter() {

      /**
       * @param e  
       */
      @Override
      public void widgetSelected(final SelectionEvent e) {
        setControlsEnabled();
      }
    });
    return button;
  }

  /**
   * Enables or disables the controls of this page
   */
  protected void setControlsEnabled() {
    if (_useProjectSettingsButton != null) {
      _overlayStore.setProjectSettings(_useProjectSettingsButton.getSelection());
    }
    updatePreferenceStore();
  }

  protected void updatePreferenceStore() {
    if (_editors != null) {
      for (FieldEditor fe : _editors) {
        updateStore(fe);
      }
    }
  }

  protected void updateStore(final FieldEditor field) {
    field.setPreferenceStore(_overlayStore.getActiveStore());
    field.load();
  }

  /**
   * We override the performDefaults method. In case of property pages we
   * switch back to the workspace settings and disable the page controls.
   * 
   * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
   */
  @Override
  protected void performDefaults() {
    super.performDefaults();
    if (isPropertyPage()) {
      _useWorkspaceSettingsButton.setSelection(true);
      _useProjectSettingsButton.setSelection(false);
      setControlsEnabled();
    }
  }

  @Override
  public boolean performOk() {
    if (_editors != null) {
      Iterator e = _editors.iterator();
      while (e.hasNext()) {
        FieldEditor pe = (FieldEditor) e.next();
        pe.store();
      }
    }
    savePostAction();
    return super.performOk();
  }

  protected void savePostAction() {
    //TODO: if needed can be overridden by subclasses
  }

  protected void addField(final FieldEditor fe) {
    _editors.add(fe);
  }

  @Override
  public void dispose() {
    _editors.clear();
    _overlayStore.removePropertyChangeListener(this);
    super.dispose();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
   */
  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    String property = event.getProperty();

    if ("useProjectSettings".equals(property)) {
      updatePreferenceStore();
    }

  }
}
