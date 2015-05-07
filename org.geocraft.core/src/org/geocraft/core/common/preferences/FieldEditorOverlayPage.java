/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.preferences;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
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


public abstract class FieldEditorOverlayPage extends FieldEditorPreferencePage {

  /** Button to toggle preferences to the workspace (instance) scope. */
  private Button _useGlobalSettingsButton;

  /** Button to toggle preferences to the global scope. */
  private Button _useProjectSettingsButton;

  private ImageDescriptor _image;

  /** The page identifier. */
  private String _pageId;

  /** The list of editors. */
  private final List _editors = new ArrayList();

  private PropertyStore _overlayStore;

  public FieldEditorOverlayPage(final int style) {
    super(style);
    initializeOverlayStore();
    setPreferenceStore(_overlayStore);
  }

  public FieldEditorOverlayPage(final String title, final int style) {
    super(title, style);
    initializeOverlayStore();
    setPreferenceStore(_overlayStore);
  }

  public FieldEditorOverlayPage(final String title, final ImageDescriptor image, final int style) {
    super(title, image, style);
    _image = image;
    initializeOverlayStore();
    setPreferenceStore(_overlayStore);
  }

  private void initializeOverlayStore() {
    _pageId = getPageId();
    _overlayStore = PropertyStoreFactory.getStore(_pageId);
  }

  public boolean isPropertyPage() {
    return _overlayStore.useProjectSettings();
  }

  @Override
  protected Control createContents(final Composite parent) {
    createSelectionGroup(parent);
    return super.createContents(parent);
  }

  /**
   * Create a button group.
   * 
   * @param parent the parent composite.
   * @param title the group title.
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
   * Creates the group section for selecting between global and workspace preferences.
   * 
   * @param parent the parent composite.
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

    _useGlobalSettingsButton = createRadioButton(radioGroup, "Enable Global Settings");
    _useProjectSettingsButton = createRadioButton(radioGroup, "Enable Project-Specific Settings");

    String use = _overlayStore.getString(PropertyStore.USE_PROJECT_SETTINGS);
    if (use.equalsIgnoreCase("true")) {
      _useProjectSettingsButton.setSelection(true);
    } else {
      _useGlobalSettingsButton.setSelection(true);
    }
  }

  @Override
  public void createControl(final Composite parent) {
    super.createControl(parent);
    updateFieldEditors();
  }

  protected abstract String getPageId();

  @Override
  public IPreferenceStore getPreferenceStore() {
    return _overlayStore;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void addField(final FieldEditor editor) {
    _editors.add(editor);
    super.addField(editor);
  }

  private Button createRadioButton(final Composite parent, final String label) {
    final Button button = new Button(parent, SWT.RADIO);
    button.setText(label);
    button.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent e) {
        updateFieldEditors();
      }
    });

    return button;
  }

  protected void updateFieldEditors() {
    _overlayStore.setProjectSettings(_useProjectSettingsButton.getSelection());
    updateFieldEditors(_overlayStore.getActiveStore());
  }

  private void updateFieldEditors(final IPreferenceStore store) {
    Iterator it = _editors.iterator();
    while (it.hasNext()) {
      FieldEditor editor = (FieldEditor) it.next();
      editor.setPreferenceStore(store);
      editor.load();
    }
  }

  @Override
  public boolean performOk() {
    return super.performOk();
  }

  @Override
  protected void performDefaults() {
    super.performDefaults();
    if (isPropertyPage()) {
      _useGlobalSettingsButton.setSelection(true);
      _useProjectSettingsButton.setSelection(false);
      updateFieldEditors();
    }
  }

  @Override
  public void dispose() {
    _editors.clear();
    super.dispose();
  }

  /**
   * The field editor preference page implementation of this <code>IPreferencePage</code>
   * (and <code>IPropertyChangeListener</code>) method intercepts <code>IS_VALID</code> 
   * events but passes other events on to its superclass.
   */
  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    String property = event.getProperty();
    if (property != null && property.equals(PropertyStore.USE_PROJECT_SETTINGS)) {
      updateFieldEditors();
    }
    super.propertyChange(event);
  }

}
