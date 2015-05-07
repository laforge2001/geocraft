package org.geocraft.ui.sectionviewer;


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
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.widgets.Composite;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.message.IMessageSubscriber;
import org.geocraft.core.service.message.Topic;
import org.geocraft.ui.viewer.AbstractViewerPart;
import org.geocraft.ui.viewer.IViewer;


public class SectionViewerPart extends AbstractViewerPart implements IMessageSubscriber {

  @Override
  public void createPartControl(final Composite parent) {
    super.createPartControl(parent);

    initDragAndDrop();
    ServiceProvider.getMessageService().subscribe(Topic.REPOSITORY_OBJECTS_REMOVED, this);
  }

  @Override
  public void dispose() {
    ServiceProvider.getMessageService().unsubscribe(Topic.REPOSITORY_OBJECTS_REMOVED, this);
    //unregister this section viewer part
    ServiceProvider.getViewersService().remove(this.hashCode());
    super.dispose();
  }

  @Override
  public void setFocus() {
    // TODO Auto-generated method stub
  }

  /**
   * Initialize the drag and drop for the volume canvas.
   */
  private void initDragAndDrop() {
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
          String vars = (String) event.data;
          List<Object> objects = new ArrayList<Object>();
          Scanner scanner = new Scanner(vars).useDelimiter(",");
          while (scanner.hasNext()) {
            String item = scanner.next();
            IRepository repository = ServiceProvider.getRepository();
            Object object = repository.get(item);
            if (object != null) {
              objects.add(object);
            }
          }
          IViewer viewer = getViewer();
          viewer.addObjects(objects.toArray(new Object[0]));
        }
      }
    });
  }

  @Override
  protected IViewer createViewer(final Composite parent) {
    final SectionViewer viewer = new SectionViewer(parent, "Section View");

    //register section viewer part which contains the viewer
    ServiceProvider.getViewersService().add(this);

    final Composite canvas = viewer.getPlot().getPlotComposite().getModelSpaceCanvas().getComposite();
    canvas.addMouseTrackListener(new MouseTrackAdapter() {

      @Override
      public void mouseEnter(final MouseEvent e) {
        canvas.forceFocus();
      }

    });
    canvas.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(final KeyEvent e) {
        String charStr = "" + e.character;
        try {
          int toggleGroupId = Integer.parseInt(charStr);
          viewer.setToggleGroup(toggleGroupId);
        } catch (NumberFormatException e1) {
          // Ignore other non-numeric keystrokes.
        }
      }

    });
    return viewer;
  }

  @Override
  public void messageReceived(final String topic, final Object data) {
    if (topic.equals(Topic.REPOSITORY_OBJECTS_REMOVED)) {
      HashMap<String, Object> deletedItems = (HashMap<String, Object>) data;
      getViewer().removeObjects(deletedItems.values().toArray());
    }

  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.viewer.IViewerPart#getViewers()
   */
  @Override
  public IViewer[] getViewers() {
    IViewer viewer = getViewer();
    return new IViewer[] { viewer };
  }
}
