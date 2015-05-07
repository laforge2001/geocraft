package org.geocraft.ui.traceviewer;


import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.viewer.AbstractViewerPart;
import org.geocraft.ui.viewer.IViewer;


public class TraceViewPart extends AbstractViewerPart {

  @Override
  public void createPartControl(final Composite parent) {
    super.createPartControl(parent);

    // Initialize the drag and drop functionality.
    //initDragAndDrop();
    ITraceViewer viewer = (ITraceViewer) getViewer();
    getSite().setSelectionProvider(viewer.getLayerViewer());

    // EXAMPLE OF HOW TO ADD SOME DUMMY TRACE DATA.
    int numTraces = 0;
    int numSamples = 251;
    float z0 = 0;
    float dz = 4;
    Trace[] traces = new Trace[numTraces];
    float[] picks = new float[numTraces];
    for (int i = 0; i < numTraces; i++) {
      float[] data = new float[numSamples];
      for (int k = 0; k < numSamples; k++) {
        data[k] = (float) (Math.random() + Math.sin(k * 0.05) + Math.cos(k * 0.25));
      }
      traces[i] = new Trace(z0, dz, Unit.MILLISECONDS, i, 0, data);
      picks[i] = z0 + (float) (Math.random() * dz * numSamples);
    }
    TraceData traceData = new TraceData(traces);
    addTraces(traceData);
    addPicks(picks, "Top Picks", new RGB(0, 255, 100));
  }

  public void addTraces(final Trace[] traces) {
    addTraces(new TraceData(traces));
  }

  public void addTraces(final TraceData traceData) {
    ITraceViewer viewer = (ITraceViewer) getViewer();
    viewer.addTraces(traceData);
  }

  public void addPicks(final float[] picks, final String name, final RGB color) {
    ITraceViewer viewer = (ITraceViewer) getViewer();
    viewer.addPicks(picks, name, color);
  }

  /**
   * Unregister listeners and dispose of resources related to the trace viewer.
   */
  @Override
  public void dispose() {
    //unregister this trace viewer part
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
  //  private void initDragAndDrop() {
  //    // Set the trace viewer composite as a drop target.
  //    DropTarget target = new DropTarget(getViewer().getComposite(), DND.DROP_COPY | DND.DROP_MOVE);
  //    target.setTransfer(new Transfer[] { TextTransfer.getInstance() });
  //    target.addDropListener(new DropTargetAdapter() {
  //
  //      @Override
  //      public void dragOver(final DropTargetEvent event) {
  //        event.detail = DND.DROP_COPY;
  //      }
  //
  //      @Override
  //      public void drop(final DropTargetEvent event) {
  //        if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
  //          // Initialize a list of entities.
  //          List<Entity> entities = new ArrayList<Entity>();
  //
  //          // Get the string containing the variable names.
  //          String vars = (String) event.data;
  //
  //          // Iterate thru the variable names.
  //          Scanner scanner = new Scanner(vars).useDelimiter(",");
  //          while (scanner.hasNext()) {
  //            // Lookup each entity in the repository based on the variable name.
  //            String item = scanner.next();
  //            IRepository repository = ServiceProvider.getRepository();
  //            Entity entity = (Entity) repository.get(item);
  //
  //            // If an entity is found, add it to the list.
  //            if (entity != null) {
  //              entities.add(entity);
  //            }
  //          }
  //
  //          // Add all the entities in the list to the trace viewer.
  //          IViewer viewer = getViewer();
  //          viewer.addObjects(entities.toArray(new Entity[0]));
  //        }
  //      }
  //    });
  //  }

  /**
   * Creates the trace viewer contained in the view part.
   * @param parent the parent composite of the viewer.
   * @return the trace viewer.
   */
  @Override
  protected IViewer createViewer(final Composite parent) {
    final TraceViewer viewer = new TraceViewer(parent, "Trace View");

    //register trace viewer part which contains the viewer
    ServiceProvider.getViewersService().add(this);
    return viewer;
  }

  public IViewer[] getViewers() {
    IViewer viewer = getViewer();
    return new IViewer[] { viewer };
  }
}
