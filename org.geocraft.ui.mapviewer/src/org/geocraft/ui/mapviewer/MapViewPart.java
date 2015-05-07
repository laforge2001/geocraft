/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.geocraft.core.model.Entity;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.message.IMessageSubscriber;
import org.geocraft.core.service.message.Topic;
import org.geocraft.ui.viewer.AbstractViewerPart;
import org.geocraft.ui.viewer.IViewer;


/**
 * Defines a view part to contain a map viewer.
 */
public class MapViewPart extends AbstractViewerPart implements IMessageSubscriber {

  @Override
  public void createPartControl(final Composite parent) {
    super.createPartControl(parent);

    // Initialize the drag and drop functionality.
    initDragAndDrop();
    getSite().setSelectionProvider(getViewer().getLayerViewer());
    ServiceProvider.getMessageService().subscribe(Topic.REPOSITORY_OBJECTS_REMOVED, this);
  }

  /**
   * Unregister listeners and dispose of resources related to the map viewer.
   */
  @Override
  public void dispose() {
    // Unsubscribe the view part from the event bus.
    ServiceProvider.getMessageService().unsubscribe(Topic.REPOSITORY_OBJECTS_REMOVED, this);
    //unregister this map viewer part
    ServiceProvider.getViewersService().remove(this.hashCode());
    super.dispose();
  }

  @Override
  public void setFocus() {
    // No action required.
  }

  /**
   * Initialize the drag and drop for the volume canvas.
   */
  private void initDragAndDrop() {
    // Set the map viewer composite as a drop target.
    DropTarget target = new DropTarget(getViewer().getComposite(), DND.DROP_COPY | DND.DROP_MOVE);
    target.setTransfer(new Transfer[] { TextTransfer.getInstance() });
    target.addDropListener(new DropTargetAdapter() {

      @Override
      public void dragOver(final DropTargetEvent event) {
        event.detail = DND.DROP_COPY;
      }

      @Override
      public void drop(final DropTargetEvent event) {
        if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
          // Initialize a list of entities.
          List<Entity> entities = new ArrayList<Entity>();

          // Get the string containing the variable names.
          String vars = (String) event.data;

          // Iterate thru the variable names.
          Scanner scanner = new Scanner(vars).useDelimiter(",");
          while (scanner.hasNext()) {
            // Lookup each entity in the repository based on the variable name.
            String item = scanner.next();
            IRepository repository = ServiceProvider.getRepository();
            Entity entity = (Entity) repository.get(item);

            // If an entity is found, add it to the list.
            if (entity != null) {
              entities.add(entity);
            }
          }

          // Add all the entities in the list to the map viewer.
          IViewer viewer = getViewer();
          viewer.addObjects(entities.toArray(new Entity[0]));
        }
      }
    });
  }

  /**
   * Creates the map viewer contained in the view part.
   * @param parent the parent composite of the viewer.
   * @return the map viewer.
   */
  @Override
  protected IViewer createViewer(final Composite parent) {
    final MapViewer viewer = new MapViewer(parent, "Map View");

    //register map viewer part which contains the viewer
    ServiceProvider.getViewersService().add(this);
    return viewer;
  }

  /**
   * Invoked upon receiving an event from the event bus.
   * @param topic the topic of the received event.
   * @param data the data of the received event.
   */
  @Override
  public void messageReceived(final String topic, final Object data) {
    if (topic.equals(Topic.REPOSITORY_OBJECTS_REMOVED)) {
      HashMap<String, Object> deletedItems = (HashMap<String, Object>) data;
      getViewer().removeObjects(deletedItems.values().toArray());
    }
  }

  public IViewer[] getViewers() {
    IViewer viewer = getViewer();
    return new IViewer[] { viewer };
  }
}
