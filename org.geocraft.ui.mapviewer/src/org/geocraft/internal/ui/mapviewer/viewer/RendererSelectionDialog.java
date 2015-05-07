/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.ui.mapviewer.viewer;


import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.geocraft.ui.common.GridLayoutHelper;


/**
 * Defines the dialog used for selecting a map view renderer when
 * more than one is found that supports a given object type.
 */
public class RendererSelectionDialog extends FormDialog {

  /** The array of matching OSGI configuration elements. */
  private final IConfigurationElement[] _elements;

  /** The index of the current selection. */
  private int _index;

  private boolean _required;

  public RendererSelectionDialog(final Shell shell, final IConfigurationElement[] elements, boolean required) {
    super(shell);
    _elements = elements;
    _index = -1;
    _required = required;
  }

  @Override
  protected void createFormContent(final IManagedForm mForm) {
    FormToolkit toolkit = mForm.getToolkit();
    Composite container = mForm.getForm().getBody();
    container.setLayout(GridLayoutHelper.createLayout(1, false));

    Label label = new Label(container, SWT.NONE);
    label.setText("Renderers:");
    label.setLayoutData(GridLayoutHelper.createLayoutData(true, false, SWT.LEFT, SWT.FILL, 1, 1));
    toolkit.adapt(label, true, true);

    Group group = new Group(container, SWT.SHADOW_ETCHED_IN);
    GridData layoutData = new GridData();
    layoutData.grabExcessHorizontalSpace = true;
    layoutData.grabExcessVerticalSpace = false;
    layoutData.horizontalAlignment = SWT.FILL;
    layoutData.verticalAlignment = SWT.FILL;
    layoutData.verticalSpan = 2;
    group.setLayoutData(layoutData);
    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    layout.makeColumnsEqualWidth = false;
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    group.setLayout(layout);
    for (int i = 0; i < _elements.length; i++) {
      final Button button = new Button(group, SWT.RADIO);
      button.setText(_elements[i].getAttribute("name"));
      button.setData("" + i);
      button.setLayoutData(GridLayoutHelper.createLayoutData(true, false, SWT.LEFT, SWT.CENTER, 1, 1));
      button.addListener(SWT.Selection, new Listener() {

        public void handleEvent(final Event event) {
          // A radio button was selected, so update the index.
          if (button.getSelection() && button.equals(event.widget)) {
            _index = Integer.parseInt(button.getData().toString());
          }
        }
      });
      toolkit.adapt(button, true, true);
      if (_required) {
        _index = 0;
        button.setSelection(i == 0);
      }
    }
    toolkit.adapt(container);
    toolkit.adapt(group);
  }

  @Override
  public int open() {
    int result = super.open();
    // If the OK button was pressed, then return the current index selection.
    if (result == OK) {
      return _index;
    }
    // Otherwise return -1 (an invalid index).
    return -1;
  }
}
