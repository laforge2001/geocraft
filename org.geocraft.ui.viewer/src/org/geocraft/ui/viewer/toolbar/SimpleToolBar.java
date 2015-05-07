/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.toolbar;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;


/**
 * A simple SWT tool bar wrapper.
 * Convenience method exist for adding simple items like
 * push buttons and toggle buttons. For more flexibility,
 * it is also possible to get the actual SWT tool bar that
 * is wrapped by this and add to it directory.
 */
public class SimpleToolBar {

  /** The SWT tool bar. */
  protected final ToolBar _toolBar;

  /**
   * Constructs a simple tool bar.
   * @param parent the parent composite.
   */
  public SimpleToolBar(final Composite parent) {
    _toolBar = new ToolBar(parent, SWT.BORDER);
    RowLayout layout = new RowLayout();
    layout.type = SWT.HORIZONTAL;
    layout.wrap = true;
    //layout.marginLeft = 2;
    //layout.marginTop = 2;
    //layout.marginBottom = 2;
    //layout.marginRight = 2;
    layout.marginHeight = 5;
    layout.marginWidth = 5;
    layout.spacing = 5;
    layout.fill = false;
    layout.pack = true;
    _toolBar.setLayout(layout);
    GridData constraints = new GridData();
    constraints.grabExcessHorizontalSpace = true;
    constraints.grabExcessVerticalSpace = false;
    constraints.horizontalSpan = 3;
    constraints.verticalSpan = 1;
    constraints.horizontalAlignment = SWT.FILL;
    constraints.verticalAlignment = SWT.FILL;
    _toolBar.setLayoutData(constraints);
  }

  /**
   * Adds a push button tool item to the tool bar.
   * @param action the action for the push button.
   * @return the tool item.
   */
  public ToolItem addPushButton(final Action action) {
    return addToolItem(action, SWT.PUSH);
  }

  /**
   * Adds a toggle button tool item to the tool bar.
   * @param action the action for the toggle button.
   * @return the tool item.
   */
  public ToolItem addToggleButton(final Action selectAction, final Action deselectAction) {
    return addToggleButton(selectAction, deselectAction, false);
  }

  /**
   * Adds a toggle button tool item to the tool bar.
   * @param action the action for the toggle button.
   * @return the tool item.
   */
  public ToolItem addToggleButton(final Action selectAction, final Action deselectAction, final boolean initialState) {
    final ToolItem toolItem = new ToolItem(_toolBar, SWT.CHECK | SWT.BORDER_SOLID);
    toolItem.setSelection(initialState);
    if (selectAction != null && deselectAction != null) {
      Listener listener = new Listener() {

        @Override
        public void handleEvent(final Event event) {
          ImageDescriptor imageDesc = null;
          Image oldImage = toolItem.getImage();
          if (toolItem.getSelection()) {
            selectAction.run();
            toolItem.setToolTipText(selectAction.getToolTipText());
            imageDesc = selectAction.getImageDescriptor();
          } else {
            deselectAction.run();
            toolItem.setToolTipText(deselectAction.getToolTipText());
            imageDesc = deselectAction.getImageDescriptor();
          }
          if (imageDesc != null) {
            toolItem.setImage(imageDesc.createImage());
          }
          if (oldImage != null) {
            oldImage.dispose();
          }
        }

      };
      toolItem.addListener(SWT.Selection, listener);
      ImageDescriptor imageDesc = null;
      if (initialState) {
        imageDesc = selectAction.getImageDescriptor();
        toolItem.setToolTipText(selectAction.getToolTipText());
      } else {
        imageDesc = deselectAction.getImageDescriptor();
        toolItem.setToolTipText(deselectAction.getToolTipText());
      }
      if (imageDesc != null) {
        toolItem.setImage(imageDesc.createImage());
      }
      listener.handleEvent(null);
    }
    return toolItem;
  }

  /**
   * Adds a combo to the tool bar.
   * @param options the array of string options to put into the combo.
   * @return the combo.
   */
  public Combo addCombo(String labelText, final String[] options) {
    if (options == null) {
      throw new IllegalArgumentException("The options array cannot be null.");
    }
    ToolItem toolItem = addSeparator();
    Composite composite = new Composite(_toolBar, SWT.NONE);
    GridLayout layout = new GridLayout(2, false);
    layout.marginHeight = 0;
    layout.marginWidth = 10;
    composite.setLayout(layout);
    Label label = new Label(composite, SWT.NONE);
    label.setText(labelText);
    GridData layoutData = new GridData();
    layoutData.grabExcessHorizontalSpace = false;
    layoutData.grabExcessVerticalSpace = false;
    label.setLayoutData(layoutData);
    Combo combo = new Combo(composite, SWT.READ_ONLY);
    for (final String option : options) {
      combo.add(option);
    }
    composite.pack();
    layoutData = new GridData();
    layoutData.grabExcessHorizontalSpace = true;
    layoutData.grabExcessVerticalSpace = false;
    combo.setLayoutData(layoutData);
    toolItem.setWidth(composite.getSize().x);
    toolItem.setControl(composite);
    return combo;
  }

  /**
   * Adds a color selector to the tool bar.
   * @param color the initial color
   * @return the color selector
   */
  public ColorSelector addColorSelector(final RGB color) {
    ToolItem toolItem = addSeparator();
    ColorSelector selector = new ColorSelector(_toolBar);
    selector.setColorValue(color);
    selector.getButton().pack();
    toolItem.setWidth(selector.getButton().getSize().x);
    toolItem.setControl(selector.getButton());
    return selector;
  }

  /**
   * Adds a colored rectangle or button to the tool bar.
   * @param color of the square
   * @return the button
   */
  public Button addColorRectangle(final RGB rgbColor) {
    ToolItem toolItem = addSeparator();
    Button button = new Button(_toolBar, SWT.BUTTON1);

    // Display the color with a rectangle on the button
    Color color = new Color(button.getDisplay(), rgbColor);
    Point fExtent = computeImageSize(_toolBar);
    Image fImage = new Image(_toolBar.getDisplay(), fExtent.x, fExtent.y);
    GC gc = new GC(fImage);
    gc.drawRectangle(0, 0, fExtent.x, fExtent.y);
    gc.setBackground(color);
    gc.fillRectangle(0, 0, fExtent.x, fExtent.y);
    gc.dispose();
    button.setImage(fImage);

    button.pack();
    toolItem.setWidth(fExtent.x);
    toolItem.setControl(button);
    return button;
  }

  /**
   * Compute the size of the colored square to be displayed.
   * 
   * @param window -
   *            the window used to calculate
   * @return <code>Point</code>
   */
  private Point computeImageSize(Control window) {
    GC gc = new GC(window);
    Font f = JFaceResources.getFontRegistry().get(JFaceResources.DIALOG_FONT);
    gc.setFont(f);
    int height = gc.getFontMetrics().getHeight();
    int width = height;
    gc.dispose();
    Point p = new Point(width, height);
    return p;
  }

  public Combo addCombo(final Action[] actions) {
    if (actions == null) {
      throw new IllegalArgumentException("The actions array cannot be null.");
    }
    ToolItem toolItem = addSeparator();
    final Combo combo = new Combo(_toolBar, SWT.READ_ONLY);
    for (Action action : actions) {
      combo.add(action.getText());
    }
    combo.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        actions[combo.getSelectionIndex()].run();
      }
    });
    combo.pack();
    toolItem.setWidth(combo.getSize().x);
    toolItem.setControl(combo);
    return combo;
  }

  public ToolItem addMenu(String name, Action[] actions) {
    final ToolItem toolItem = new ToolItem(_toolBar, SWT.DROP_DOWN);
    toolItem.setText(name);
    final Menu menu = new Menu(_toolBar.getShell(), SWT.POP_UP);
    for (final Action action : actions) {
      MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
      menuItem.addListener(SWT.Selection, new Listener() {

        @Override
        public void handleEvent(Event event) {
          action.run();
        }

      });
      ImageDescriptor imageDesc = action.getImageDescriptor();
      if (imageDesc != null) {
        menuItem.setImage(imageDesc.createImage());
      }
      String text = action.getText();
      if (text != null && text.length() > 0) {
        menuItem.setText(text);
      }
    }
    toolItem.addListener(SWT.Selection, new Listener() {

      @Override
      public void handleEvent(Event event) {
        Rectangle rect = toolItem.getBounds();
        Point pt = new Point(rect.x, rect.y + rect.height);
        pt = _toolBar.toDisplay(pt);
        menu.setLocation(pt.x, pt.y);
        menu.setVisible(true);
      }

    });
    return toolItem;
  }

  /**
   * Adds a tool item to the tool bar.
   * @param action the action tool item.
   * @return the tool item.
   */
  private ToolItem addToolItem(final Action action, final int style) {
    ToolItem toolItem = new ToolItem(_toolBar, style);
    if (action != null) {
      Listener listener = new Listener() {

        @Override
        public void handleEvent(final Event event) {
          action.run();
        }

      };
      toolItem.addListener(SWT.Selection, listener);
      ImageDescriptor imageDesc = action.getImageDescriptor();
      if (imageDesc != null) {
        toolItem.setImage(imageDesc.createImage());
      }
      String text = action.getText();
      if (text != null && text.length() > 0) {
        toolItem.setText(text);
      }
      toolItem.setToolTipText(action.getToolTipText());
    }
    return toolItem;
  }

  /**
   * Adds a label to the tool bar.
   * @return the label.
   */
  public Label addLabel(String labelText) {
    Composite composite = new Composite(_toolBar, SWT.NONE);
    GridLayout layout = new GridLayout(1, false);
    layout.marginHeight = 0;
    ToolItem toolItem = null;
    // If empty label is used to initialize the tool bar
    // (So limit the amount of space used)
    if (labelText.isEmpty()) {
      layout.marginWidth = 1;
      toolItem = addSeparator(1);
    } else {
      layout.marginWidth = 10;
      toolItem = addSeparator();
    }
    composite.setLayout(layout);
    Label label = new Label(composite, SWT.NONE);
    label.setText(labelText);
    GridData layoutData = new GridData();
    layoutData.grabExcessHorizontalSpace = false;
    layoutData.grabExcessVerticalSpace = false;
    label.setLayoutData(layoutData);
    composite.pack();
    layoutData = new GridData();
    layoutData.grabExcessHorizontalSpace = true;
    layoutData.grabExcessVerticalSpace = false;
    toolItem.setWidth(composite.getSize().x);
    toolItem.setControl(composite);
    return label;
  }

  /**
   * Adds a separator to the tool bar.
   * @return
   */
  public ToolItem addSeparator() {
    return addSeparator(10);
  }

  /**
   * Adds a separator to the tool bar.
   * @return
   */
  public ToolItem addSeparator(final int width) {
    ToolItem toolItem = new ToolItem(_toolBar, SWT.SEPARATOR);
    toolItem.setWidth(width);
    return toolItem;
  }

  /**
   * Returns the SWT tool bar.
   * @return the SWT tool bar.
   */
  public ToolBar getToolBar() {
    return _toolBar;
  }

}