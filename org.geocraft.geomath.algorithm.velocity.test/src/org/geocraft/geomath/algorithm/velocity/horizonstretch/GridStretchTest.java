package org.geocraft.geomath.algorithm.velocity.horizonstretch;


import junit.framework.TestCase;

import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.internal.geomath.algorithm.velocity.VelocityArrayTimeDepthConverter;


public class GridStretchTest extends TestCase {

  //	  static {
  //		    InMemoryMapperFactory.register();
  //		    SegyPostStack3dMapperFactory.register();
  //		  }

  //private PostStack3d _volume;

  @Override
  public void setUp() {
    //String dir = "test/data";
    //String name = "test";
    //int inlineByteLoc = 181;
    //int xlineByteLoc = 185;
    //Unit zUnit = Unit.MILLISECONDS;
    //		    Properties storeProps = new Properties();
    //		    storeProps.setProperty(SegyStoreProperty.DIRECTORY.getKey(), dir);
    //		    storeProps.setProperty(SegyStoreProperty.FILE_NAME.getKey(), name);
    //		    storeProps.setProperty(SegyStoreProperty.FILE_EXTN.getKey(), ".segy");
    //		    storeProps.setProperty(SegyStoreProperty.INLINE_BYTE_LOC.getKey(), Integer.toString(inlineByteLoc));
    //		    storeProps.setProperty(SegyStoreProperty.XLINE_BYTE_LOC.getKey(), Integer.toString(xlineByteLoc));
    //		    storeProps.setProperty(SegyStoreProperty.Z_UNITS.getKey(), zUnit.getName());
    //		    SegyPostStack3dMapper mapper = (SegyPostStack3dMapper) SegyPostStack3dMapperFactory.getInstance().create(storeProps);
    //		    _volume = PostStack3dFactory.getInstance().create(name, mapper);
  }

  static final float[][] DATA_A = new float[][] {
      { 0, 0.616f, 6.575f, 11.532f, 14.734f, 15.693f, 14.262f, 10.660f, 5.435f, 0 },
      { 0, 6.649f, 70.907f, 124.371f, 158.900f, 169.238f, 153.811f, 114.967f, 58.621f, 0 },
      { 0, 12.025f, 128.238f, 224.927f, 287.374f, 306.070f, 278.170f, 207.921f, 106.017f, 2.217E-15f },
      { 0, 16.235f, 173.132f, 303.671f, 387.979f, 413.221f, 375.553f, 280.711f, 143.133f, 0 },
      { 0, 18.850f, 201.018f, 352.582f, 450.469f, 479.776f, 436.042f, 325.924f, 166.186f, 0 },
      { 0, 19.610f, 209.126f, 366.804f, 468.639f, 499.128f, 453.629f, 339.070f, 172.890f, 3.615E-15f },
      { 0, 18.469f, 196.954f, 345.454f, 441.363f, 470.077f, 427.227f, 319.335f, 162.827f, 0 },
      { 0, 15.510f, 165.403f, 290.114f, 370.658f, 394.772f, 358.786f, 268.178f, 136.743f, 0 },
      { 0, 11.033f, 117.660f, 206.375f, 263.670f, 280.825f, 255.226f, 190.771f, 97.273f, 0 },
      { 0, 5.476f, 58.401f, 102.434f, 130.873f, 139.387f, 126.681f, 94.689f, 48.281f, 0 }, };

  public void testDepVelCellD2T() {
    UnitPreferences.getInstance().setVerticalDistanceUnit(Unit.FOOT);
    UnitPreferences.getInstance().setTimeUnit(Unit.SECOND);

    // setup
    float sampleRate = 0.5f;
    float depth = (float) 1.79;
    float expected = 3.956667f;
    float[] velTrace = { 0, 0.5f, 1.0f, 1.5f, 2, 2.5f, 3, 3.5f, 4, 4.5f };

    VelocityArrayTimeDepthConverter td = new VelocityArrayTimeDepthConverter(velTrace, sampleRate, Domain.DISTANCE,
        Unit.FEET_PER_SECOND, VelocityArrayTimeDepthConverter.Method.CellBased);
    assertEquals(expected, td.getTime(depth));

  }

  public void testTimeToDepth() {

    UnitPreferences.getInstance().setTimeUnit(Unit.SECOND);

    // setup
    float sampleRate = 0.1f;
    float[] depth = { 250.0f, 750.0f, 874.999f, 950.0f, 1000.0f, 1750.0f, 2000.0f, 2249.999f, 2658.045f, 2954.385f,
        3561.429f, 4507.695f, 5496.519f };
    float[] time = { 0.1f, 0.3f, 0.35f, 0.38f, 0.4f, 0.7f, 0.8f, 0.9f, 1.0f, 1.1f, 1.3f, 1.6f, 1.9f };
    float[] velTrace = { 5000.0f, 5000.0f, 5000.0f, 5000.0f, 5000.0f, 5000.0f, 5000.0f, 5000.0f, 5000.0f, 5000.0f,
        8160.9f, 5926.8f, 6022.7f, 6118.2f, 6213.5f, 6308.5f, 6403.3f, 6497.9f, 6592.2f, 6686.4f };

    VelocityArrayTimeDepthConverter td = new VelocityArrayTimeDepthConverter(velTrace, sampleRate, Domain.TIME,
        Unit.FEET_PER_SECOND, VelocityArrayTimeDepthConverter.Method.CellBased);
    for (int i = 0; i < depth.length; ++i) {
      assertEquals(depth[i], td.getDepth(time[i]), .001);
    }
  }

  public void testTimeToDepth2() {

    UnitPreferences.getInstance().setTimeUnit(Unit.MILLISECONDS);

    // setup
    float sampleRate = 10.0f;
    float[] depth = { 500.0f, 1000.0f, 1250.0f, 2000.0f, 2580.0f, 4109.47f, 7522.65f, 9403.968f, 11400.529f, 13511.588f };
    float[] time = { 200.0f, 400.0f, 500.0f, 800.0f, 1000.0f, 1500.0f, 2500.0f, 3000.0f, 3500.0f, 4000.0f };

    double[] velTraceDbl = { 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0,
        5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0,
        5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0,
        5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0,
        5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0,
        5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0,
        5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 5000.0,
        5000.0, 5000.0, 5000.0, 5000.0, 5000.0, 21000.0, 5883.6, 5893.2, 5902.8, 5912.4, 5922.0, 5931.6, 5941.2,
        5950.8, 5960.4, 5969.9, 5979.5, 5989.1, 5998.7, 6008.3, 6017.8, 6027.4, 6037.0, 6046.5, 6056.1, 6065.6, 6075.2,
        6084.7, 6094.3, 6103.8, 6113.4, 6122.9, 6132.5, 6142.0, 6151.5, 6161.1, 6170.6, 6180.1, 6189.6, 6199.2, 6208.7,
        6218.2, 6227.7, 6237.2, 6246.7, 6256.2, 6265.7, 6275.2, 6284.7, 6294.2, 6303.7, 6313.2, 6322.7, 6332.2, 6341.7,
        6351.2, 6360.6, 6370.1, 6379.6, 6389.1, 6398.5, 6408.0, 6417.5, 6426.9, 6436.4, 6445.9, 6455.3, 6464.8, 6474.2,
        6483.7, 6493.1, 6502.6, 6512.0, 6521.4, 6530.9, 6540.3, 6549.8, 6559.2, 6568.6, 6578.0, 6587.5, 6596.9, 6606.3,
        6615.7, 6625.2, 6634.6, 6644.0, 6653.4, 6662.8, 6672.2, 6681.6, 6691.0, 6700.4, 6709.8, 6719.2, 6728.6, 6738.0,
        6747.4, 6756.8, 6766.2, 6775.6, 6784.9, 6794.3, 6803.7, 6813.1, 6822.5, 6831.8, 6841.2, 6850.6, 6860.0, 6869.3,
        6878.7, 6888.0, 6897.4, 6906.8, 6916.1, 6925.5, 6934.8, 6944.2, 6953.5, 6962.9, 6972.2, 6981.6, 6990.9, 7000.3,
        7009.6, 7018.9, 7028.3, 7037.6, 7046.9, 7056.3, 7065.6, 7074.9, 7084.3, 7093.6, 7102.9, 7112.2, 7121.5, 7130.9,
        7140.2, 7149.5, 7158.8, 7168.1, 7177.4, 7186.7, 7196.0, 7205.3, 7214.7, 7224.0, 7233.3, 7242.5, 7251.8, 7261.1,
        7270.4, 7279.7, 7289.0, 7298.3, 7307.6, 7316.9, 7326.2, 7335.4, 7344.7, 7354.0, 7363.3, 7372.6, 7381.8, 7391.1,
        7400.4, 7409.7, 7418.9, 7428.2, 7437.5, 7446.7, 7456.0, 7465.2, 7474.5, 7483.8, 7493.0, 7502.3, 7511.5, 7520.8,
        7530.0, 7539.3, 7548.5, 7557.8, 7567.0, 7576.3, 7585.5, 7594.8, 7604.0, 7613.2, 7622.5, 7631.7, 7641.0, 7650.2,
        7659.4, 7668.7, 7677.9, 7687.1, 7696.3, 7705.6, 7714.8, 7724.0, 7733.2, 7742.5, 7751.7, 7760.9, 7770.1, 7779.3,
        7788.5, 7797.8, 7807.0, 7816.2, 7825.4, 7834.6, 7843.8, 7853.0, 7862.2, 7871.4, 7880.6, 7889.8, 7899.0, 7908.2,
        7917.4, 7926.6, 7935.8, 7945.0, 7954.2, 7963.4, 7972.6, 7981.8, 7990.9, 8000.1, 8009.3, 8018.5, 8027.7, 8036.9,
        8046.1, 8055.2, 8064.4, 8073.6, 8082.8, 8091.9, 8101.1, 8110.3, 8119.5, 8128.6, 8137.8, 8147.0, 8156.1, 8165.3,
        8174.5, 8183.6, 8192.8, 8201.9, 8211.1, 8220.3, 8229.4, 8238.6, 8247.7, 8256.9, 8266.0, 8275.2, 8284.4, 8293.5,
        8302.7, 8311.8, 8320.9, 8330.1, 8339.2, 8348.4, 8357.5, 8366.7, 8375.8, 8385.0, 8394.1, 8403.2, 8412.4, 8421.5,
        8430.6, 8439.8, 8448.9, 8458.0, 8467.2, 8476.3, 8485.4, 8494.6, 8503.7, 8512.8, 8521.9, 8531.1, 8540.2, 8549.3,
        8558.4, 8567.6, 8576.7, 8585.8, 8594.9, 8604.0, 8613.1, 8622.3, 8631.4, 8640.5, 8649.6, 8658.7, 8667.8, 8676.9,
        8686.0, 8695.1, 8704.2, 8713.4, 8722.5, 8731.6, 8740.7, 8749.8, 8758.9, 8768.0, 8777.1, 8786.2, 8795.3, 8804.4,
        8813.5, 8822.6, 8831.6, 8840.7, 8849.8, 8858.9, 8868.0, 8877.1, 8886.2, 8895.3, 8904.4, 8913.5, 8922.5, 8931.6,
        8940.7, 8949.8, 8958.9, 8968.0, 8977.0, 8986.1, 8995.2, 9004.3, 9013.3, 9022.4, 9031.5, 9040.6, 9049.7, 9058.7,
        9067.8, 9076.9, 9085.9, 9095.0, 9104.1, 9113.1, 9122.2 };

    float[] velTrace = new float[velTraceDbl.length];
    for (int i1 = 0; i1 < velTraceDbl.length; ++i1) {
      velTrace[i1] = (float) velTraceDbl[i1];
    }

    VelocityArrayTimeDepthConverter td = new VelocityArrayTimeDepthConverter(velTrace, sampleRate, Domain.TIME,
        Unit.FEET_PER_SECOND, VelocityArrayTimeDepthConverter.Method.CellBased);
    for (int i = 0; i < depth.length; ++i) {
      assertEquals(depth[i], td.getDepth(time[i]), .001);
    }
  }

  public void testTimeVelCellD2T() {
    UnitPreferences.getInstance().setTimeUnit(Unit.SECOND);
    UnitPreferences.getInstance().setVerticalDistanceUnit(Unit.FOOT);

    // setup
    float sampleRate = 0.5f;
    float depth = (float) 1.79;
    // float expected = 1.645f;
    float expected = 2.432f;
    float[] velTrace = { 0, 0.5f, 1.0f, 1.5f, 2, 2.5f, 3, 3.5f, 4, 4.5f };

    VelocityArrayTimeDepthConverter td = new VelocityArrayTimeDepthConverter(velTrace, sampleRate, Domain.TIME,
        Unit.FEET_PER_SECOND, VelocityArrayTimeDepthConverter.Method.CellBased);
    assertEquals(expected, td.getTime(depth));
  }

  public void testDepVelCellT2D() {

    UnitPreferences.getInstance().setTimeUnit(Unit.SECOND);
    UnitPreferences.getInstance().setVerticalDistanceUnit(Unit.FOOT);

    // setup
    float sampleRate = 0.5f;
    float time = (float) 1.98;
    // float expected = 2.46f;
    float expected = 0.495f;
    float[] velTrace = { 0, 0.5f, 1.0f, 1.5f, 2, 2.5f, 3, 3.5f, 4, 4.5f };

    VelocityArrayTimeDepthConverter td = new VelocityArrayTimeDepthConverter(velTrace, sampleRate, Domain.DISTANCE,
        Unit.FEET_PER_SECOND, VelocityArrayTimeDepthConverter.Method.CellBased);
    assertEquals(expected, td.getDepth(time));

  }

  public void testTimeVelCellT2D() {
    UnitPreferences.getInstance().setTimeUnit(Unit.SECOND);
    UnitPreferences.getInstance().setVerticalDistanceUnit(Unit.FOOT);

    // setup
    float sampleRate = 0.5f;
    float time = (float) 1.98;
    float expected = 1.23f;
    float[] velTrace = { 0, 0.5f, 1.0f, 1.5f, 2, 2.5f, 3, 3.5f, 4, 4.5f };

    VelocityArrayTimeDepthConverter td = new VelocityArrayTimeDepthConverter(velTrace, sampleRate, Domain.TIME,
        Unit.FEET_PER_SECOND, VelocityArrayTimeDepthConverter.Method.CellBased);
    assertEquals(expected, td.getDepth(time));

  }

  public void xtestHorizonStretch() {

    // create a new grid2d object
    //    float zNull = (float) -999.25;
    //    Grid3d propertyA = TestHelperUtil.createGrid("A", 0, 0, 50, 50, 10, 10, 0.0, zNull, DATA_A, Domain.TIME,
    //        Unit.MILLISECONDS, DataType.FLOAT);
    //
    //    Grid3d result = null;
    //    HorizonStretchAlgorithm task = new HorizonStretchAlgorithm();
    //    try {
    //
    //      //execute
    //
    //      result = (Grid3d) task.compute(Activator.getLogger(), null);
    //    } catch (Exception e) {
    //      //ServiceProvider.getLoggingService().getLogger(getClass()).error("Error occurred when testing the horizonStretch algorithm", e);
    //    }
    //
    //    assert result != null : "result should not be null";
    //
    //    float[][] expected = new float[][] { { 0, 0.462f, 4.931f, 8.649f, 11.050f, 11.769f, 10.696f, 7.995f, 4.076f, 0 },
    //        { 0, 4.986f, 53.180f, 93.278f, 119.175f, 126.928f, 115.358f, 86.225f, 43.965f, 0 },
    //        { 0, 9.018f, 96.178f, 168.695f, 215.530f, 229.552f, 208.627f, 155.940f, 79.512f, 1.662E-15f },
    //        { 0, 12.176f, 129.849f, 227.753f, 290.984f, 309.915f, 281.664f, 210.533f, 107.349f, 0 },
    //        { 0, 14.137f, 150.763f, 264.436f, 337.851f, 359.832f, 327.031f, 244.443f, 124.639f, 0 },
    //        { 0, 14.707f, 156.844f, 275.103f, 351.479f, 374.346f, 340.221f, 254.302f, 129.667f, 2.711E-15f },
    //        { 0, 13.851f, 147.715f, 259.090f, 331.022f, 352.557f, 320.420f, 239.501f, 122.120f, 0 },
    //        { 0, 11.632f, 124.052f, 217.585f, 277.993f, 296.079f, 269.089f, 201.133f, 102.557f, 0 },
    //        { 0, 8.274f, 88.245f, 154.781f, 197.752f, 210.618f, 191.419f, 143.078f, 72.954f, 0 },
    //        { 0, 4.106f, 43.800f, 76.825f, 98.154f, 104.540f, 95.010f, 71.016f, 36.210f, 0 } };
    //
    //    // check the results
    //    assertEquals(true, TestHelperUtil.testResult(result, expected, 10, 10, 0.001));

  }
}
