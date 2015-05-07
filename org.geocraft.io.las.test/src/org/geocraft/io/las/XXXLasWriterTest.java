/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.las;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import org.geocraft.core.common.io.TextFile;
import org.geocraft.core.common.util.FileUtil;
import org.geocraft.core.model.well.Well;
import org.geocraft.core.model.well.WellBore;
import org.geocraft.core.model.well.WellLogTrace;


public class XXXLasWriterTest extends TestCase {

  private LasReader _reader;

  private File _fwrite;

  private File _fread;

  //private static final String TEST_FILE = "LAS30.las";

  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    //    super.setUp();
    //
    //    String path = Utilities.getPath("org.geocraft.io.las.test") + "data";
    //    String fileName = "testWrite.las";
    //    _fwrite = new File(path + File.separatorChar + fileName);
    //    if (!_fwrite.exists()) {
    //      try {
    //        _fwrite.createNewFile();
    //      } catch (IOException e) {
    //        // TODO Auto-generated catch block
    //        e.printStackTrace();
    //      }
    //    }
    //
    //    _fread = new File(path + File.separatorChar + TEST_FILE);
  }

  @Override
  protected void tearDown() throws Exception {
    //_fwrite.delete();
  }

  /**
   * @param file
   * @return
   */
  private WellMapperModel createMapperModel(File file) {
    WellMapperModel lmm = new WellMapperModel();
    String filePath = file.getAbsolutePath();
    lmm.setValueObject(WellMapperModel.DIRECTORY, FileUtil.getPathName(filePath));
    lmm.setValueObject(WellMapperModel.FILE_NAME, FileUtil.getShortName(filePath));
    TextFile tf = createTextFile(lmm.getDirectory(), lmm.getFileName());
    _reader = new LasReader(tf);
    float[] dataRange = _reader.getDataRange();
    lmm.setValueObject(WellMapperModel.BEG_DEPTH_DEFAULT, dataRange[0]);
    lmm.setValueObject(WellMapperModel.BEG_DEPTH, dataRange[0]);
    lmm.setValueObject(WellMapperModel.END_DEPTH_DEFAULT, dataRange[1]);
    lmm.setValueObject(WellMapperModel.END_DEPTH, dataRange[1]);
    lmm.setValueObject(WellMapperModel.STEP_DEFAULT, dataRange[2]);
    lmm.setValueObject(WellMapperModel.STEP, dataRange[2]);
    //    lmm.setValueObject(LasMapperModel.DESCRIPTION_LIST, createDescriptionList(reader));
    lmm.setValueObject(WellMapperModel.COLUMN_NAMES, _reader.getColumnNames());
    lmm.setValueObject(WellMapperModel.SELECTED_COLUMN_NAMES, _reader.getColumnNames());

    return lmm;
  }

  /**
   * Simulates the role of the <code>WellAccessorUtil</code> class to update the new mapper model with
   * values from the entity to export.
   * 
   * @param well the well.
   */
  private WellMapperModel updateMapperModel(Well well, WellMapperModel mappermodel) {

    //save the destination 
    String dir = mappermodel.getDirectory();
    String filename = mappermodel.getFileName();

    //update mapper model from model in entity
    WellMapperModel readerModel = (WellMapperModel) well.getMapper().getModel();
    mappermodel.updateFrom(readerModel);

    //make sure the the place to write to is not overwritten
    mappermodel.setValueObject(WellMapperModel.FILE_NAME, filename);
    mappermodel.setValueObject(WellMapperModel.DIRECTORY, dir);

    return mappermodel;
  }

  private WellMapperModel createWriteMapperModel(String dir, String filename, Well well) {
    WellMapperModel lmm = new WellMapperModel();
    lmm.setValueObject(WellMapperModel.DIRECTORY, dir);
    lmm.setValueObject(WellMapperModel.FILE_NAME, filename);
    return updateMapperModel(well, lmm);
  }

  /**
   * @param directory
   * @param fileName
   * @return
   */
  private TextFile createTextFile(String directory, String fileName) {
    final String filePath = directory + File.separator + fileName;
    File file = new File(filePath);
    if (!file.exists() || !file.canRead() || file.isDirectory()) {
      throw new RuntimeException("Cannot acces the LAS file: " + filePath);
    }
    return new TextFile(filePath);
  }

  public void testNoOp() {
    // TODO:
  }

  public void xxtestWriteModel() {
    assertTrue(_fwrite.exists());
    assertTrue(_fread.exists());

    // Create a new well entity from the input file.
    WellMapperModel mapperModelRead = createMapperModel(_fread);
    WellMapper mapper = new WellMapper(mapperModelRead);
    Well well = new Well("test well", mapper);
    WellBore wellBore = well.getWellBore();

    // Create a writer mapper model with the new location to write to.
    WellMapperModel mapperModelWrite = createWriteMapperModel(FileUtil.getPathName(_fwrite.getAbsolutePath()), FileUtil
        .getShortName(_fwrite.getAbsolutePath()), well);

    // Writer the well to an LAS file using the mapper model.
    try {
      LasWriter.write(well, mapperModelWrite);
    } catch (FileNotFoundException ex) {
      fail(ex.toString());
    } catch (IOException ex) {
      fail(ex.toString());
    }

    // Build new entity using writer mapper model (populated using Las reader)
    WellMapper mapperWrite = new WellMapper(mapperModelWrite);
    Well well2 = new Well("after write", mapperWrite);
    WellBore wellBore2 = well2.getWellBore();

    // Compare the attributes between the input and output mapper models.
    Well wellRead = well;
    Well wellWrite = well2;

    assertEquals(wellRead.getCurrentOperator(), wellWrite.getCurrentOperator());
    assertEquals(wellRead.getDisplayName(), wellWrite.getDisplayName());
    assertEquals(wellRead.getField(), wellWrite.getField());
    assertEquals(wellRead.getLocation().toString(), wellWrite.getLocation().toString());
    assertEquals(wellRead.getDataSource(), wellWrite.getDataSource());
    assertEquals(wellRead.getCountry(), wellWrite.getCountry());
    assertEquals(wellRead.getSpudDate().toString(), wellWrite.getSpudDate().toString());
    assertEquals(wellRead.getLocation().getX(), wellWrite.getLocation().getX());
    assertEquals(wellRead.getLocation().getY(), wellWrite.getLocation().getY());
    assertEquals(wellRead.getLocation().getSystem().getDatum(), wellWrite.getLocation().getSystem().getDatum());
    assertEquals(wellRead.getLocation().getSystem().getProjection(), wellWrite.getLocation().getSystem()
        .getProjection());
    assertEquals(wellRead.getStateOrProvince(), wellWrite.getStateOrProvince());
    assertEquals(wellRead.getIdentifier(), wellWrite.getIdentifier());

    WellLogTrace[] tracesRead = well.getWellLogTraces();
    WellLogTrace[] tracesWrite = well2.getWellLogTraces();
    assertEquals(tracesRead.length, tracesWrite.length);

  }
}
