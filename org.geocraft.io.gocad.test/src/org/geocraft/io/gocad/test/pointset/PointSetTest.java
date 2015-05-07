package org.geocraft.io.gocad.test.pointset;


import junit.framework.TestCase;

import org.geocraft.core.common.util.Utilities;
import org.geocraft.core.model.PointSet;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.io.gocad.pointset.PointSetMapper;
import org.geocraft.io.gocad.pointset.PointSetMapperModel;


public class PointSetTest extends TestCase {

  @Override
  public void setUp() {
    UnitPreferences.getInstance().setHorizontalDistanceUnit(Unit.FOOT);
    UnitPreferences.getInstance().setVerticalDistanceUnit(Unit.FOOT);
  }

  public void testLoad() {

    PointSetMapperModel model = new PointSetMapperModel();

    model.setValueObject(PointSetMapperModel.DIRECTORY, Utilities.getPath("org.geocraft.io.gocad.test") + "data");
    model.setValueObject(PointSetMapperModel.FILE_NAME, "points.vs");
    model.setValueObject(PointSetMapperModel.XY_UNIT, Unit.FOOT);
    model.setValueObject(PointSetMapperModel.Z_UNIT, Unit.FOOT);

    IMapper mapper = new PointSetMapper(model);
    PointSet points = new PointSet("PointSet name", mapper);

    // sanity check total number of points
    assertEquals(7, points.getNumPoints());
    assertEquals(4, points.getAttributeNames().length);
    assertEquals("curly", points.getAttributeNames()[0]);

    // check first point
    assertEquals(2619531, points.getX(0), 0.01);
    assertEquals(9914471, points.getY(0), 0.01);
    assertEquals(4169.244629, points.getZ(0), 0.01);

    // check first point's attributes
    assertEquals(0.842093, points.getAttribute("curly").getFloat(0), 0.01);
    assertEquals(0.853747, points.getAttribute("larry").getFloat(0), 0.01);
    assertEquals(1f, points.getAttribute("flag").getFloat(0));
    assertEquals(0.093900, points.getAttribute("moe").getFloat(0), 0.01);

    // check last point
    assertEquals(2619537, points.getX(6), 0.01);
    assertEquals(9914477, points.getY(6), 0.01);
    assertEquals(5966.856934, points.getZ(6), 0.01);
  }

  public void testFileWithMultipleSpacesBetweenFields() {

    PointSetMapperModel model = new PointSetMapperModel();

    model.setValueObject(PointSetMapperModel.DIRECTORY, Utilities.getPath("org.geocraft.io.gocad.test") + "data");
    model.setValueObject(PointSetMapperModel.FILE_NAME, "picked_velocity.vs");
    model.setValueObject(PointSetMapperModel.XY_UNIT, Unit.METER);
    model.setValueObject(PointSetMapperModel.Z_UNIT, Unit.METER);

    IMapper mapper = new PointSetMapper(model);
    PointSet points = new PointSet("PointSet name", mapper);

    assertEquals(16, points.getNumPoints());
  }

  // problem here is that if you skip loading a point because the xyz are invalid then 
  // you also need to not load the attributes for this record either. Commented out test
  // until we fix this. 
  public void XXXtestErrorHandling() {
    PointSetMapperModel model = new PointSetMapperModel();

    model.setValueObject(PointSetMapperModel.DIRECTORY, Utilities.getPath("org.geocraft.io.gocad.test") + "data");
    model.setValueObject(PointSetMapperModel.FILE_NAME, "nonsense.vs");
    model.setValueObject(PointSetMapperModel.XY_UNIT, Unit.FOOT);
    model.setValueObject(PointSetMapperModel.Z_UNIT, Unit.FOOT);

    IMapper mapper = new PointSetMapper(model);
    PointSet points = new PointSet("PointSet name", mapper);

    assertEquals(15, points.getNumPoints());
    assertEquals(594500, points.getX(14), 0.01);
    assertEquals(7444500, points.getY(14), 0.01);
    assertEquals(7000, points.getZ(14), 0.01);
    assertEquals(6000f, points.getAttribute("velocity").getFloat(14), 0.01);
  }

  // TODO fix this
  public void XXtestMissingFile() {
    PointSetMapperModel model = new PointSetMapperModel();

    model.setValueObject(PointSetMapperModel.DIRECTORY, Utilities.getPath("org.geocraft.io.gocad.test") + "data");
    model.setValueObject(PointSetMapperModel.FILE_NAME, "bogus.vs");

    IMapper mapper = new PointSetMapper(model);
    PointSet points = new PointSet("PointSet is invalid", mapper);
    try {
      assertEquals(7, points.getNumPoints());
      assertTrue("should not get here: ", false);
    } catch (Exception ex) {
      assertTrue("Failed to catch invalid file", ("" + ex).startsWith("Cannot "));
    }
  }
}
