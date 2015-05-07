package org.geocraft.io.asciigrid;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.common.util.Utilities;
import org.geocraft.core.io.IDatastoreAccessorUtil;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.OnsetType;
import org.geocraft.core.model.datatypes.PolygonUtil;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.model.preferences.UnitPreferences;


/**
 * The datastore accessor implementation for ModSpec grids.
 */
public class AsciiHorizonAccessorUtil implements IDatastoreAccessorUtil, AsciiFileConstants {

  public Map<Entity, MapperModel> mapEntitiesToModels(final Entity[] entities) {
    Map<Entity, MapperModel> map = new HashMap<Entity, MapperModel>();
    // Loop thru the entities, creating a subset by extracting out only the grid properties.
    Set<Entity> set = new HashSet<Entity>();
    for (Entity entity : entities) {
      if (entity instanceof Grid3d) {
        set.add(entity);
      }
    }

    // Loop thru the subset of grid properties, setting default datastore mapper properties for each.
    int horNumber = 1;
    for (Entity entity : set.toArray(new Entity[0])) {
      Grid3d grid = (Grid3d) entity;
      AsciiHorizonMapperModel model = new AsciiHorizonMapperModel();
      // Default the directory to the working directory if the Preferences key has not been set yet.
      String directory = PreferencesUtil.getPreferencesStore(AsciiFileConstants.PLUGIN_ID).get("LoadModSpecGrid_DIR",
          Utilities.getWorkingDirectory());
      model.setDirectory(directory);
      // Default the file name to the grid display name.
      model.setFileName("hData" + horNumber);
      // default the horizon
      model.setHorizon1(grid);
      // Default the x,y units to the application x,y units (this is persisted in the meta file).
      model.setXyUnits(UnitPreferences.getInstance().getHorizontalDistanceUnit());
      // Default the data domain to the domain of the grid data unit (this is persisted in the meta file).
      Unit dataUnit = grid.getDataUnit();
      // Default the data unit to the grid data unit (this is persisted in the meta file).
      model.setDataUnits(dataUnit);
      // Default the onset to the onset of the grid (this is persisted in the meta file).
      OnsetType onsetType = grid.getOnsetType();
      // Default the data unit to the grid data unit (this is persisted in the meta file).
      model.setOnsetType(onsetType);
      // Default the orientation to the grid orientation (this is persisted in the meta file).
      model.setOrientation(computeGridOrientation(grid));
      // Default the X Origin
      CoordinateSeries points = grid.getGeometry().getCornerPoints();
      model.setXorigin(points.getX(0));
      // Default the Y Origin
      model.setYorigin(points.getY(0));

      // Default the Column Spacing
      double dx01 = points.getX(1) - points.getX(0);
      double dy01 = points.getY(1) - points.getY(0);
      dx01 /= grid.getNumColumns() - 1;
      dy01 /= grid.getNumColumns() - 1;
      double colSpacing = Math.sqrt(dx01 * dx01 + dy01 * dy01);
      model.setColSpacing(colSpacing);

      // Default the Row Spacing
      double dx03 = points.getX(3) - points.getX(0);
      double dy03 = points.getY(3) - points.getY(0);
      dx03 /= grid.getNumRows() - 1;
      dy03 /= grid.getNumRows() - 1;
      double rowSpacing = Math.sqrt(dx03 * dx03 + dy03 * dy03);
      model.setRowSpacing(rowSpacing);

      // Default the Primary angle
      double angle = Math.atan2(points.getY(1) - points.getY(0), points.getX(1) - points.getX(0));
      angle *= 180 / Math.PI;
      model.setPrimaryAngle(angle);

      // Default the null value
      model.setNullValue(grid.getNullValue());

      map.put(entity, model);

      horNumber++;
    }
    return map;
  }

  @Override
  public IStatus initialize() {
    // No initialization necessary.
    return Status.OK_STATUS;
  }

  /**
   * Computes the row,col <-> x,y orientation from the specified grid entity.
   * @param grid the grid entity.
   * @return the row,col <-> x,y orientation.
   */
  private static GridOrientation computeGridOrientation(final Grid3d grid) {
    CoordinateSeries cornerPoints = grid.getGeometry().getCornerPoints();
    GridOrientation orientation = GridOrientation.X_IS_COLUMN;
    if (PolygonUtil.getDirection(cornerPoints) != PolygonUtil.PolygonType.CounterClockwise) {
      orientation = GridOrientation.Y_IS_COLUMN;
    }
    return orientation;
  }
}
