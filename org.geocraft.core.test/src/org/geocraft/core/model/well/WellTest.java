/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.well;


import java.sql.Timestamp;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.datatypes.Coordinate;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.MapperModel;
import org.geocraft.core.query.IProjectQuery;


/**
 * Unit tests for the well classes (Well, WellBore, WellLogTrace).
 */
public class WellTest extends TestCase {

  public static final String COMMENT = "Kilroy way here";

  public static final String COUNTRY = "USA";

  public static final String COUNTY = "Galveston";

  public static final String STATE_OR_PROVINCE = "Texas";

  public static final String OPERATOR = "Kilroy";

  public static final String DATA_SOURCE = "Thin Air";

  public static final String FIELD = "Backyard";

  public static final float GROUND_ELEVATION = 80;

  public static final String IDENTIFIER = "123-45-6789";

  public static final String IDENTIFIER_TYPE = "SSN";

  public static final Timestamp LAST_MODIFIED_DATE = new Timestamp(System.currentTimeMillis());

  public static final String LEASE_NAME = "Dry Hole Gulch";

  public static final String LEASE_NUMBER = "12345";

  public static final String PERMIT_NUMBER = "555";

  public static final String OCS_NUMBER = "42";

  public static final String OFFSHORE_AREA = "Shipwreck Bay";

  public static final String OFFSHORE_BLOCK = "99";

  public static final String PLATFORM_IDENTIFIER = "SS Rickety Rowboat";

  public static final String PROJECT_NAME = "My 6th Grade Science Project";

  public static final Timestamp SPUD_DATE = new Timestamp(System.currentTimeMillis());

  public static final float WATER_DEPTH = 50;

  public static final Coordinate LOCATION = new Coordinate(new Point3d(10, 20, 0), new CoordinateSystem("NAD", "0",
      Domain.DISTANCE));

  public static final String WELL_TYPE = "Offshore";

  public static final String WELL_STATUS = "Completed";

  public static final String[] WELL_ALIASES = { "Well #1", "The Duster" };

  public static final String[] WELL_ALIAS_TYPES = { "Common Name", "Cool Name" };

  /**
   * Creates a well entity for unit testing.
   * 
   * @param name
   *          the name of the well.
   * @return the well.
   */
  private static Well createWell(final String name) {
    Well well = new Well(name, new IMapper() {

      @Override
      public void create(final Entity entity) {
        // TODO Auto-generated method stub

      }

      @Override
      public void delete(final Entity entity) {
        // TODO Auto-generated method stub

      }

      @Override
      public boolean existsInStore() {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public boolean existsInStore(final String proposedName) {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public IMapper factory(final MapperModel mapperModel) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public MapperModel getModel() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public String getUniqueID() {
        // TODO Auto-generated method stub
        return null;
      }

      public String getStorageDirectory() {
        return "";
      }

      @Override
      public void read(final Entity entity, final IProgressMonitor monitor) {
        // TODO Auto-generated method stub

      }

      @Override
      public void reinitialize() {
        // TODO Auto-generated method stub

      }

      @Override
      public void update(final Entity entity) {
        // TODO Auto-generated method stub

      }

      @Override
      public IStatus validateName(final String proposedName) {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public String getDatastoreEntryDescription() {
        return "Unit Test Well";
      }

      @Override
      public String getDatastore() {
        return "Unit Test";
      }

	@Override
	public IProjectQuery getProjectQuery() {
		// TODO Auto-generated method stub
		return null;
	}
	  public String createOutputDisplayName(String inputDisplayName, String nameSuffix) {
		  return inputDisplayName + nameSuffix;
	  }

    });

    well.setComment(COMMENT);
    well.setCountry(COUNTRY);
    well.setCounty(COUNTY);
    well.setCurrentOperator(OPERATOR);
    well.setDataSource(DATA_SOURCE);
    well.setField(FIELD);
    well.setGroundElevation(GROUND_ELEVATION);
    well.setIdentifierAndType(IDENTIFIER, IDENTIFIER_TYPE);
    well.setLastModifiedDate(LAST_MODIFIED_DATE);
    well.setLeaseName(LEASE_NAME);
    well.setLeaseNumber(LEASE_NUMBER);
    well.setLocation(LOCATION);
    well.setOCSNumber(OCS_NUMBER);
    well.setOffshoreArea(OFFSHORE_AREA);
    well.setOffshoreBlock(OFFSHORE_BLOCK);
    well.setPermitNumber(PERMIT_NUMBER);
    well.setPlatformIdentifier(PLATFORM_IDENTIFIER);
    well.setProjectName(PROJECT_NAME);
    well.setSpudDate(SPUD_DATE);
    well.setStateOrProvince(STATE_OR_PROVINCE);
    well.setWaterDepth(WATER_DEPTH);
    well.setAliasesAndTypes(WELL_ALIASES, WELL_ALIAS_TYPES);
    well.setWellStatus(WELL_STATUS);
    well.setWellType(WELL_TYPE);
    well.markLoaded();

    return well;
  }

  /**
   * Simple test of the well getters and setters.
   */
  public void testWellSimple() {
    Well well = createWell("foo");

    assertEquals(COMMENT, well.getComment());
    assertEquals(COUNTRY, well.getCountry());
    assertEquals(COUNTY, well.getCounty());
    assertEquals(OPERATOR, well.getCurrentOperator());
    assertEquals(DATA_SOURCE, well.getDataSource());
    assertEquals(FIELD, well.getField());
    assertEquals(GROUND_ELEVATION, well.getGroundElevation());
    assertEquals(IDENTIFIER, well.getIdentifier());
    assertEquals(IDENTIFIER_TYPE, well.getIdentifierType());
    assertEquals(LAST_MODIFIED_DATE, well.getLastModifiedDate());
    assertEquals(LEASE_NAME, well.getLeaseName());
    assertEquals(LEASE_NUMBER, well.getLeaseNumber());
    assertEquals(LOCATION, well.getLocation());
    assertEquals(OCS_NUMBER, well.getOCSNumber());
    assertEquals(OFFSHORE_AREA, well.getOffshoreArea());
    assertEquals(OFFSHORE_BLOCK, well.getOffshoreBlock());
    assertEquals(PERMIT_NUMBER, well.getPermitNumber());
    assertEquals(PLATFORM_IDENTIFIER, well.getPlatformIdentifier());
    assertEquals(PROJECT_NAME, well.getProjectName());
    assertEquals(SPUD_DATE, well.getSpudDate());
    assertEquals(STATE_OR_PROVINCE, well.getStateOrProvince());
    assertEquals(WATER_DEPTH, well.getWaterDepth());
    String[] wellAliases = well.getAliases();
    assertEquals(WELL_ALIASES.length, wellAliases.length);
    if (WELL_ALIASES.length == wellAliases.length) {
      for (int i = 0; i < WELL_ALIASES.length; i++) {
        assertEquals(WELL_ALIASES[i], wellAliases[i]);
      }
    }
    String[] wellAliasTypes = well.getAliasTypes();
    assertEquals(WELL_ALIAS_TYPES.length, wellAliasTypes.length);
    if (WELL_ALIAS_TYPES.length == wellAliasTypes.length) {
      for (int i = 0; i < WELL_ALIAS_TYPES.length; i++) {
        assertEquals(WELL_ALIAS_TYPES[i], wellAliasTypes[i]);
      }
    }
    assertEquals(WELL_STATUS, well.getWellStatus());
    assertEquals(WELL_TYPE, well.getWellType());
  }

  /**
   * Unit test for the well utility methods.
   */
  public void testWellUtil() {
    String sourceName = "TVD";
    float[] sourceData = { 40, 60, 70, 100, 105, 120, 140, 180, 190, 195, 200 };
    String targetName = "TWT";
    float[] targetData = { 0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 };

    // Test a value in the middle (interpolation).
    float sourceValue1 = 85;
    float targetValue1 = WellUtil.lookupValue(sourceValue1, sourceData, sourceName, targetData, targetName);
    assertEquals(25f, targetValue1);

    float sourceValue1x = WellUtil.lookupValue(targetValue1, targetData, targetName, sourceData, sourceName);
    assertEquals(sourceValue1, sourceValue1x);

    // Test a value before the first (extrapolation).
    float sourceValue2 = 30;
    float targetValue2 = WellUtil.lookupValue(sourceValue2, sourceData, sourceName, targetData, targetName);
    assertEquals(-5f, targetValue2);

    float sourceValue2x = WellUtil.lookupValue(targetValue2, targetData, targetName, sourceData, sourceName);
    assertEquals(sourceValue2, sourceValue2x);

    // Test a value after the last (extrapolation).
    float sourceValue3 = 205;
    float targetValue3 = WellUtil.lookupValue(sourceValue3, sourceData, sourceName, targetData, targetName);
    assertEquals(110f, targetValue3);

    float sourceValue3x = WellUtil.lookupValue(targetValue3, targetData, targetName, sourceData, sourceName);
    assertEquals(sourceValue3, sourceValue3x);
  }
}
