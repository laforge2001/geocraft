///*
// * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
// */
//package org.geocraft.internal.abavo.ellipse;
//
//
//import org.eclipse.jface.viewers.IContentProvider;
//import org.eclipse.jface.viewers.ITableLabelProvider;
//import org.eclipse.jface.viewers.TableViewer;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.graphics.Point;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Table;
//import org.eclipse.swt.widgets.TableColumn;
//import org.eclipse.ui.forms.widgets.FormToolkit;
//import org.eclipse.ui.forms.widgets.Section;
//import org.geocraft.abavo.ellipse.EllipseRegionsModel;
//import org.geocraft.abavo.ellipse.EllipseRegionsModelEvent;
//import org.geocraft.abavo.ellipse.EllipseRegionsModelListener;
//import org.geocraft.ui.form.ViewComposite;
//import org.geocraft.ui.form.field.ButtonField;
//
//
///**
// * Creates a composite for editing an ellipse regions model.
// * The editor consists of 3 sections...
// * 1) General - currently the only general property is region symmetry.
// * 2) Ellipses - contains a table showing properties of the background
// * and maximum ellipses.
// * 3) Region Boundaries - contains a table showing properties of the
// * region boundary lines.
// * At present the table fields are non-editable, and only the
// * toggle for region symmetry is editable.
// */
//public class EllipseRegionsModelEditor extends ViewComposite implements EllipseRegionsModelListener {
//
//  /**
//   * Constructs an editor for ellipse regions models.
//   * @param model the model to edit.
//   */
//  public EllipseRegionsModelEditor(final EllipseRegionsModel model) {
//    model.addEllipseModelListener(this);
//    setModel(new EllipseRegionsModel(model));
//  }
//
//  @Override
//  public void buildForm() {
//    EllipseRegionsModel model = (EllipseRegionsModel) getModel();
//
//    // Add the general section.
//    Section generalSection = addSection("General");
//
//    ButtonField symmetry = addButtonField(generalSection, SWT.CHECK, EllipseRegionsModel.SYMMETRIC_REGIONS);
//    symmetry.setLabel("Symmetry Lock on Region Pairs");
//
//    FormToolkit toolkit = getToolkit();
//
//    // Add the ellipses section.
//    Section ellipsesSection = addSection("Ellipses");
//
//    // Create a custom composite.
//    final Composite composite1 = toolkit.createComposite(ellipsesSection, SWT.NONE);
//    composite1.setLayout(createGridLayout());
//    ellipsesSection.setClient(composite1);
//
//    // Create the ellipses table.
//    TableViewer ellipseTableViewer = createEllipseTable(composite1, model);
//    ellipseTableViewer.getControl().setLayoutData(createGridData());
//    ellipseTableViewer.getControl().setSize(800, 250);
//
//    // Add the region boundaries section.
//    Section boundariesSection = addSection("Region Boundaries");
//
//    // Create a custom composite.
//    final Composite composite2 = toolkit.createComposite(boundariesSection, SWT.NONE);
//    composite2.setLayout(createGridLayout());
//    boundariesSection.setClient(composite2);
//
//    // Crate the region boundaries table.
//    TableViewer boundsTableViewer = createBoundsTable(composite2, model);
//    boundsTableViewer.getControl().setLayoutData(createGridData());
//    boundsTableViewer.getControl().setSize(800, 500);
//  }
//
//  /**
//   * Returns a grid layout with common settings.
//   * @return a grid layout with common settings
//   */
//  private GridLayout createGridLayout() {
//    GridLayout gridLayout;
//    gridLayout = new GridLayout();
//    gridLayout.makeColumnsEqualWidth = false;
//    gridLayout.horizontalSpacing = 1;
//    gridLayout.numColumns = 3;
//    return gridLayout;
//  }
//
//  /**
//   * Returns grid layout data with common settings.
//   * @return grid layout data with common settings
//   */
//  private GridData createGridData() {
//    GridData gridData = new GridData();
//    gridData.grabExcessHorizontalSpace = false;
//    gridData.horizontalAlignment = SWT.FILL;
//    gridData.horizontalSpan = 3;
//    gridData.grabExcessVerticalSpace = false;
//    return gridData;
//  }
//
//  /**
//   * Creates the ellipses table.
//   * @param model the ellipse regions model.
//   */
//  private TableViewer createEllipseTable(final Composite parent, final EllipseRegionsModel model) {
//    // Create the custom content and label providers.
//    IContentProvider contentProvider = new EllipseTableContentProvider();
//    ITableLabelProvider labelProvider = new EllipseTableLabelProvider();
//
//    // Create the table and columns.
//    Table table = new Table(parent, SWT.BORDER);
//    String[] columnNames = { "Ellipse", "Slope", "Length", "Width", "Center X", "Center Y" };
//    for (int i = 0; i < columnNames.length; i++) {
//      TableColumn col = new TableColumn(table, SWT.FILL, i);
//      col.setText(columnNames[i]);
//      col.setWidth(150);
//    }
//    table.setLinesVisible(true);
//    table.setHeaderVisible(true);
//
//    // Create the table viewer.
//    TableViewer tableViewer = new TableViewer(table);
//    tableViewer.setContentProvider(contentProvider);
//    tableViewer.setLabelProvider(labelProvider);
//    tableViewer.setColumnProperties(columnNames);
//    tableViewer.setInput(model);
//    Point size = tableViewer.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
//    tableViewer.getControl().setSize(size);
//
//    return tableViewer;
//  }
//
//  /**
//   * Creates the region boundaries table.
//   * @param model the ellipse regions model.
//   */
//  private TableViewer createBoundsTable(final Composite parent, final EllipseRegionsModel model) {
//    // Create the custom content and label providers.
//    IContentProvider contentProvider = new RegionsBoundaryTableContentProvider();
//    ITableLabelProvider labelProvider = new RegionsBoundaryTableLabelProvider();
//
//    // Create the table and columns.
//    Table table = new Table(parent, SWT.BORDER);
//    String[] columnNames = { "Regions Boundary", "Outer X", "Outer Y", "Inner X", "Inner Y" };
//    for (int i = 0; i < columnNames.length; i++) {
//      TableColumn col = new TableColumn(table, SWT.FILL, i);
//      col.setText(columnNames[i]);
//      if (i == 0) {
//        col.setWidth(400);
//      } else {
//        col.setWidth(150);
//      }
//    }
//    table.setLinesVisible(true);
//    table.setHeaderVisible(true);
//
//    // Create the table viewer.
//    TableViewer tableViewer = new TableViewer(table);
//    tableViewer.setContentProvider(contentProvider);
//    tableViewer.setLabelProvider(labelProvider);
//    tableViewer.setColumnProperties(columnNames);
//    tableViewer.setInput(model);
//    Point size = tableViewer.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
//    tableViewer.getControl().setSize(size);
//    return tableViewer;
//  }
//
//  public void ellipseModelUpdated(final EllipseRegionsModelEvent event) {
//    // For now, simple copy the entire model every time.
//    EllipseRegionsModel model = (EllipseRegionsModel) getModel();
//    model.updateModel(event.getEllipseRegionsModel());
//  }
//
//  /**
//   * 
//   */
//  public void dispose() {
//    EllipseRegionsModel model = (EllipseRegionsModel) getModel();
//    model.dispose();
//  }
//}
