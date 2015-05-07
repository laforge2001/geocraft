/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import java.nio.ByteOrder;

import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;


public class SegyBytes {

  /** The logger. */
  private static ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(SegyBytes.class);

  /** The constant for sample format -> 4-byte IBM float. */
  public static final int SAMPLE_FORMAT_CODE_FLOAT_4BYTE_IBM = 1;

  /** The constant for sample format -> 4-byte fixed. */
  public static final int SAMPLE_FORMAT_CODE_FIXED_4BYTE = 2;

  /** The constant for sample format -> 2-byte fixed. */
  public static final int SAMPLE_FORMAT_CODE_FIXED_2BYTE = 3;

  /** The constant for sample format -> 4-byte fixed w/ gain. */
  public static final int SAMPLE_FORMAT_CODE_FIXED_4BYTE_WITH_GAIN = 4;

  /** The constant for sample format -> 4-byte IEEE float. */
  public static final int SAMPLE_FORMAT_CODE_FLOAT_4BYTE_IEEE = 5;

  /** The constant for 1-byte fixed. */
  public static final int SAMPLE_FORMAT_CODE_FIXED_1BYTE = 8;

  /** The constant for sample format -> 4-byte IBM float. */
  public static final String SAMPLE_FORMAT_FLOAT_4BYTE_IBM = "IBM Floating Point (4-bytes)";

  /** The constant for sample format -> 4-byte fixed. */
  public static final String SAMPLE_FORMAT_FIXED_4BYTE = "Fixed Point (4-bytes)";

  /** The constant for sample format -> 2-byte fixed. */
  public static final String SAMPLE_FORMAT_FIXED_2BYTE = "Fixed Point (2-bytes)";

  /** The constant for sample format -> 4-byte fixed w/ gain. */
  public static final String SAMPLE_FORMAT_FIXED_4BYTE_WITH_GAIN = "Fixed Point w/Gain (4-bytes)";

  /** The constant for sample format -> 4-byte IEEE float. */
  public static final String SAMPLE_FORMAT_FLOAT_4BYTE_IEEE = "IEEE Floating Point (4-bytes)";

  /** The constant for 1-byte fixed. */
  public static final String SAMPLE_FORMAT_FIXED_1BYTE = "Fixed Point (1-byte)";

  public static final double POW2TO24 = Math.pow(2, 24);

  public static final double LOG16 = Math.log(16);

  public static final long IMASK = 0x000000FF;

  private static final int IBM2IEEE = 0;

  private static final int IEEE2IBM = 1;

  private static int[][] _m1 = new int[512][2];

  private static float[][] _r1 = new float[512][2];
  static {
    initLookupTable();
  }

  public static final void getFloatsFromBytes(final int format, final int count, final byte[] bs, final float[] fs,
      final ByteOrder byteOrder) {
    getFloatsFromBytes(format, count, bs, fs, 0, byteOrder);
  }

  public static final void getFloatsFromBytes(final int format, final int count, final byte[] bs, final float[] fs,
      final int index, final ByteOrder byteOrder) {

    //boolean swapBytes = !byteOrder.equals(ByteOrder.nativeOrder());
    switch (format) {

      case SAMPLE_FORMAT_CODE_FLOAT_4BYTE_IBM:
        //if(swapBytes) {
        //  swapBytes(bs, 4);
        //}
        getFloatsFrom4BytesFloatIBMcps(count, bs, fs, index, true);
        break;

      case SAMPLE_FORMAT_CODE_FIXED_4BYTE:
        //if(swapBytes) {
        //  swapBytes(bs, 4);
        //}
        getFloatsFrom4BytesFixed(count, bs, fs, index);
        break;

      case SAMPLE_FORMAT_CODE_FIXED_2BYTE:
        //if(swapBytes) {
        //  swapBytes(bs, 2);
        //}
        getFloatsFrom2BytesFixed(count, bs, fs, index);
        break;

      case SAMPLE_FORMAT_CODE_FIXED_4BYTE_WITH_GAIN:
        //if(swapBytes) {
        //  swapBytes(bs, 4);
        //}
        getFloatsFrom4BytesFixedWithGain(count, bs, fs, index);
        break;

      case SAMPLE_FORMAT_CODE_FLOAT_4BYTE_IEEE:
        //if(swapBytes) {
        //  swapBytes(bs, 4);
        //}
        getFloatsFrom4BytesFloatIEEE(count, bs, fs, index);
        break;

      case SAMPLE_FORMAT_CODE_FIXED_1BYTE:
        getFloatsFrom1BytesFixed(count, bs, fs, index);
        break;

      default:
        throw new IllegalArgumentException("Error: This format cannot currently be read!");
    }
  }

  public static final void getFloatsFrom4BytesFloatIBMorig(final int count, final byte[] bs, final float[] fs,
      final int index) {
    int ibits;
    double qf;
    float qs;
    int qc;
    int ndx = index;
    for (int k = 0; k < count; k++) {
      ibits = byte2char(bs[ndx + 1]) << 16;
      ibits |= byte2char(bs[ndx + 2]) << 8;
      ibits |= byte2char(bs[ndx + 3]);

      qf = (double) ibits / 0x00FFFFFF;
      qc = bs[ndx] & 0x0000007F;
      qs = 1;
      if (bs[ndx] < 0) {
        qs = -1;
      }
      fs[k] = (float) (qs * Math.pow(16, qc - 64) * qf);
      ndx += 4;
    }
  }

  /**
   * float_to_ibm - convert between 32 bit IBM and IEEE floating numbers
   *                seperate input and output buffers
   *  Notes:
   *  Up to 3 bits lost on IEEE -> IBM
   *  Looks like we always write big-endian after convert to ibm.
   *  IBM -> IEEE may overflow or underflow, taken care of by
   *  substituting large number or zero
   *  Only integer shifting and masking are used.
   *  @param count the # of floats to convert.
   *  @param bs the byte array to store in.
   *  @param fs the float array to convert.
   *  @param index the starting index in the byte array.
   *  @param isEndian false for little-endian machine; true for big-endian machine.
   */
  public static final void getFloatsFrom4BytesFloatIBMcps(final int count, final byte[] bs, final float[] fs,
      final int index, final boolean isEndian) {

    int fconv;
    int fmant;
    int t;
    int ndx = index;

    for (int i1 = 0; i1 < count; i1++) {
      fconv = byte2char(bs[ndx + 1]) << 16;
      fconv |= byte2char(bs[ndx + 2]) << 8;
      fconv |= byte2char(bs[ndx + 3]);
      fconv |= byte2char(bs[ndx]) << 24;

      /* if little endian, i.e. endian=0 do this */
      if (!isEndian) {
        fconv = fconv << 24 | fconv >> 24 & 0xff | (fconv & 0xff00) << 8 | (fconv & 0xff0000) >> 8;
      }
      if (fconv != 0) {
        fmant = 0x00ffffff & fconv;
        /* The next two lines were added by Toralf Foerster */
        /* to trap non-IBM format data i.e. conv=0 data  */
        if (fmant == 0) {
          System.out.println(" data are not in IBM FLOAT Format !\n");
        } else {
          t = ((0x7f000000 & fconv) >> 22) - 130;
          while ((fmant & 0x00800000) == 0) {
            --t;
            fmant = fmant << 1;
          }
          if (t > 254) {
            fconv = 0x80000000 & fconv | 0x7f7fffff;
          } else if (t <= 0) {
            fconv = 0;
          } else {
            fconv = 0x80000000 & fconv | t << 23 | 0x007fffff & fmant;
          }
        }
      }
      fs[i1] = Float.intBitsToFloat(fconv);
      ndx += 4;
    }
  }

  public static final void getFloatsFrom4BytesFixed(final int count, final byte[] bs, final float[] fs, final int index) {
    int ibits;
    int ndx = index;
    for (int k = 0; k < count; k++) {
      ibits = byte2char(bs[ndx]) << 24;
      ibits |= byte2char(bs[ndx + 1]) << 16;
      ibits |= byte2char(bs[ndx + 2]) << 8;
      ibits |= byte2char(bs[ndx + 3]);
      fs[k] = ibits;
      ndx += 4;
    }
  }

  public static final void getFloatsFrom2BytesFixed(final int count, final byte[] bs, final float[] fs, final int index) {
    int ibits;
    int ndx = index;
    for (int k = 0; k < count; k++) {
      ibits = byte2char(bs[ndx]);
      ibits <<= 8;
      ibits |= byte2char(bs[ndx + 1]);
      ibits <<= 16;
      ibits >>= 16;
      fs[k] = (short) ibits;
      ndx += 2;
    }
  }

  public static final void getFloatsFrom4BytesFixedWithGain(final int count, final byte[] bs, final float[] fs,
      final int index) {
    byte[] bbs = new byte[2];
    float[] ffs = new float[1];
    float qd;
    int gain;
    int ndx = index;
    for (int k = 0; k < count; k++) {
      bbs[0] = bs[ndx + 2];
      bbs[1] = bs[ndx + 3];
      getFloatsFrom2BytesFixed(1, bbs, ffs, 0);
      qd = ffs[0];
      gain = bs[ndx + 1] & 0x0000FFFF;
      fs[k] = qd * gain;
      ndx += 4;
    }
  }

  public static final void getFloatsFrom4BytesFloatIEEE(final int count, final byte[] bs, final float[] fs,
      final int index) {
    int ibits;
    double fraction;
    int iterm1;
    int iterm2;
    float qs;
    int ndx = index;
    for (int k = 0; k < count; k++) {
      ibits = (byte2char(bs[ndx + 1]) << 16 | byte2char(bs[ndx + 2]) << 8 | byte2char(bs[ndx + 3])) & 0x007FFFFF;
      fraction = (double) ibits / 0x007FFFFF;
      iterm1 = byte2char(bs[ndx]) << 24 | byte2char(bs[ndx + 1]) << 16;
      iterm2 = (iterm1 & 0x7F800000) >>> 23;
      qs = 1;
      if (iterm1 < 0) {
        qs = -1;
      }
      fs[k] = (float) (qs * Math.pow(2, iterm2 - 127) * (1 + fraction));
      ndx += 4;
    }
  }

  public static final void getFloatsFrom1BytesFixed(final int count, final byte[] bs, final float[] fs, final int index) {
    int ndx = index;
    for (int k = 0; k < count; k++) {
      fs[k] = bs[ndx++];
    }
  }

  public static final char byte2char(final byte b) {
    if (b >= 0) {
      return (char) b;
    }
    return (char) (256 + b);
  }

  public static final byte char2byte(final char c) {
    if (c >= 128) {
      return (byte) (c - 256);
    }
    return (byte) c;
  }

  public static final void putFloatsToBytes(final int format, final int count, final float[] fs, final byte[] bs,
      final ByteOrder byteOrder) {
    putFloatsToBytes(format, count, fs, bs, 0, byteOrder);
  }

  public static final void putFloatsToBytes(final int format, final int count, final float[] fs, final byte[] bs,
      final int index, final ByteOrder byteOrder) {

    //boolean swapBytes = !byteOrder.equals(ByteOrder.nativeOrder());
    switch (format) {

      case SAMPLE_FORMAT_CODE_FLOAT_4BYTE_IBM:
        putFloatsTo4BytesFloatIBMcps(count, fs, bs, index, true);
        //if(swapBytes) {
        //  swapBytes(bs, 4);
        //}
        break;

      case SAMPLE_FORMAT_CODE_FIXED_4BYTE:
        putFloatsTo4BytesFixed(count, fs, bs, index);
        //if(swapBytes) {
        //  swapBytes(bs, 4);
        //}
        break;

      case SAMPLE_FORMAT_CODE_FIXED_2BYTE:
        putFloatsTo2BytesFixed(count, fs, bs, index);
        //if(swapBytes) {
        //  swapBytes(bs, 2);
        //}
        break;

      case SAMPLE_FORMAT_CODE_FIXED_4BYTE_WITH_GAIN:

        // TODO
        // getFloatsFrom4BytesFixedWithGain(count, bs, fs, index);
        //if(swapBytes) {
        //  int[] ibytes = bs.asIntBuffer().array();
        //  swapBytes(ibytes);
        //}
        break;

      case SAMPLE_FORMAT_CODE_FLOAT_4BYTE_IEEE:
        putFloatsTo4BytesFloatIEEE(count, fs, bs, index);
        //if(swapBytes) {
        //  swapBytes(bs, 4);
        //}
        break;

      case SAMPLE_FORMAT_CODE_FIXED_1BYTE:
        putFloatsTo1BytesFixed(count, fs, bs, index);
        break;

      default:
        String msg = "Error: This format cannot currently be written!";
        LOGGER.debug(msg);
        throw new IllegalArgumentException(msg);
    }
  }

  public static final void putFloatsTo4BytesFloatIBMorig(final int count, final float[] fs, final byte[] bs,
      final int index) {
    double temp;
    double fabs;
    double qf;
    float qs;
    int qc;
    int base;
    int ibits;
    int ndx = index;
    for (int k = 0; k < count; k++) {
      qs = 1;
      if (fs[k] < 0) {
        qs = -1;
      }
      fabs = Math.abs(fs[k]);
      qc = 0;
      if (fs[k] != 0) {
        qc = (int) (Math.log(fabs) / LOG16) - 1;
      }
      temp = 0;
      while (qc < 256) {
        temp = Math.pow(16, qc);
        if (temp > fabs) {
          break;
        }
        qc++;
      }
      qc += 64;

      qf = fabs / temp;

      base = (int) (POW2TO24 * qf);
      ibits = 0;

      if (qs < 0) {
        ibits = base | qc << 24 | 0x80000000;
      } else {
        ibits = base | qc << 24;
      }
      if (fs[k] == 0f) {
        ibits = 0x00800000;
      }

      bs[ndx++] = (byte) (ibits >>> 24 & IMASK);
      bs[ndx++] = (byte) (ibits >>> 16 & IMASK);
      bs[ndx++] = (byte) (ibits >>> 8 & IMASK);
      bs[ndx++] = (byte) (ibits >>> 0 & IMASK);
    }
  }

  /**
   * float_to_ibm - convert between 32 bit IBM and IEEE floating numbers
   *                seperate input and output buffers
   *  Notes:
   *  Up to 3 bits lost on IEEE -> IBM
   *  Looks like we always write big-endian after convert to ibm.
   *  IBM -> IEEE may overflow or underflow, taken care of by
   *  substituting large number or zero
   *  Only integer shifting and masking are used.
   *  @param count the # of floats to convert.
   *  @param fs the float array to convert.
   *  @param bs the byte array to store in.
   *  @param index the starting index in the byte array.
   *  @param isEndian false for little-endian machine; true for big-endian machine.
   */
  public static final void putFloatsTo4BytesFloatIBMcps(final int count, final float[] fs, final byte[] bs,
      final int index, final boolean isEndian) {

    int fconv;
    int fmant;
    int t;
    int ndx = index;

    for (int i1 = 0; i1 < count; i1++) {
      fconv = Float.floatToIntBits(fs[i1]);
      if (fconv != 0) {
        fmant = 0x007fffff & fconv | 0x00800000;
        t = ((0x7f800000 & fconv) >> 23) - 126;
        while ((t & 0x3) != 0) {
          ++t;
          fmant = fmant >> 1;
        }
        fconv = 0x80000000 & fconv | (t >> 2) + 64 << 24 | fmant;
      }
      if (!isEndian) {
        fconv = fconv << 24 | fconv >> 24 & 0xff | (fconv & 0xff00) << 8 | (fconv & 0xff0000) >> 8;
      }
      bs[ndx] = (byte) (fconv >>> 24 & IMASK);
      bs[ndx + 1] = (byte) (fconv >>> 16 & IMASK);
      bs[ndx + 2] = (byte) (fconv >>> 8 & IMASK);
      bs[ndx + 3] = (byte) (fconv >>> 0 & IMASK);
      ndx += 4;
    }
  }

  public static final void putFloatsTo4BytesFixed(final int count, final float[] fs, final byte[] bs, final int index) {
    int ibits;
    int ndx = index;
    for (int k = 0; k < count; k++) {
      ibits = Math.round(fs[k]);
      bs[ndx++] = (byte) (ibits >>> 24 & IMASK);
      bs[ndx++] = (byte) (ibits >>> 16 & IMASK);
      bs[ndx++] = (byte) (ibits >>> 8 & IMASK);
      bs[ndx++] = (byte) (ibits >>> 0 & IMASK);
    }
  }

  public static final void putFloatsTo2BytesFixed(final int count, final float[] fs, final byte[] bs, final int index) {
    short sbits;
    int ndx = index;
    for (int k = 0; k < count; k++) {
      sbits = (short) Math.round(fs[k]);
      bs[ndx++] = (byte) ((sbits & 0xFF00) >>> 8);
      bs[ndx++] = (byte) ((sbits & 0x00FF) >>> 0);
    }
  }

  public static final void putFloatsTo4BytesFloatIEEE(final int count, final float[] fs, final byte[] bs,
      final int index) {
    int ibits;
    int ndx = index;
    for (int k = 0; k < count; k++) {
      ibits = Float.floatToIntBits(fs[k]);
      bs[ndx++] = (byte) (ibits >>> 24 & IMASK);
      bs[ndx++] = (byte) (ibits >>> 16 & IMASK);
      bs[ndx++] = (byte) (ibits >>> 8 & IMASK);
      bs[ndx++] = (byte) (ibits >>> 0 & IMASK);
    }
  }

  public static final void putFloatsTo1BytesFixed(final int count, final float[] fs, final byte[] bs, final int index) {
    byte bbits;
    int ndx = index;
    for (int k = 0; k < count; k++) {
      bbits = 0;
      if (fs[k] > 0) {
        bbits = (byte) (fs[k] + 0.5);
      } else {
        bbits = (byte) (fs[k] - 0.5);
      }
      bs[ndx++] = bbits;
    }
  }

  public static final void putFloatTo4BytesFixedWithGain(final float f, final byte[] b) {
    throw new UnsupportedOperationException();
  }

  public static final void getFloatsFrom4BytesFloatIBMppc(final int count, final byte[] bs, final float[] fs,
      final int index) {
    int ibits;
    int j;
    int ndx = index;
    for (int k = 0; k < count; k++) {
      ibits = byte2char(bs[ndx++]) << 24;
      ibits |= byte2char(bs[ndx++]) << 16;
      ibits |= byte2char(bs[ndx++]) << 8;
      ibits |= byte2char(bs[ndx++]);
      j = ibits >> 23 & 511;
      ibits = _m1[j][IBM2IEEE] ^ ibits;
      fs[k] = Float.intBitsToFloat(ibits) + _r1[j][IBM2IEEE];
    }
  }

  public static final void putFloatsTo4BytesFloatIBMppc(final int count, final float[] fs, final byte[] bs,
      final int index) {
    int ibits;
    int j;
    float tempf;
    int ndx = index;
    for (int k = 0; k < count; k++) {
      ibits = Float.floatToIntBits(fs[k]);
      j = ibits >> 23 & 511;
      ibits = _m1[j][IEEE2IBM] ^ ibits;
      tempf = Float.intBitsToFloat(ibits) + _r1[j][IEEE2IBM];
      ibits = Float.floatToIntBits(tempf);

      bs[ndx++] = (byte) (ibits >>> 24 & IMASK);
      bs[ndx++] = (byte) (ibits >>> 16 & IMASK);
      bs[ndx++] = (byte) (ibits >>> 8 & IMASK);
      bs[ndx++] = (byte) (ibits >>> 0 & IMASK);
    }

  }

  private static void initLookupTable() {
    int eIBM;
    int mhIBM;
    int eIEEE;
    int emod;
    int ibits;
    for (int i = 0; i < 512; i++) {
      eIBM = (i & 255) >> 1;
      mhIBM = i & 1;
      eIEEE = 4 * eIBM - 130;
      if (eIEEE > 0 && eIEEE <= 255) {
        _m1[i][0] = (eIEEE ^ i & 255) << 23;
        if (mhIBM == 1) {
          _r1[i][0] = 0.0f;
        } else {
          ibits = (eIEEE | i & 256) << 23;
          _r1[i][0] = -Float.intBitsToFloat(ibits);
        }
      } else if (eIEEE <= 0) {
        _m1[i][0] = i << 23;
        _r1[i][0] = 0.0f;
      } else {
        _m1[i][0] = i << 23;
        if (i < 256) {
          _r1[i][0] = Float.POSITIVE_INFINITY;
        } else {
          _r1[i][0] = Float.NEGATIVE_INFINITY;
        }
      }
      if (i == 0) {
        _m1[i][1] = 0;
        _r1[i][1] = 0.0f;
      } else {
        eIEEE = i & 255;
        eIBM = eIEEE + 133 >> 2;
        emod = 4 * eIBM - eIEEE - 130;
        if (emod == 0) {
          _m1[i][1] = (eIEEE ^ 2 * eIBM + 1) << 23;
          _r1[i][1] = 0.0f;
        } else {
          _m1[i][1] = (eIEEE ^ 2 * eIBM - emod) << 23;
          ibits = (i & 256) + 2 * eIBM << 23;
          _r1[i][1] = Float.intBitsToFloat(ibits);
        }
      }
    }
  }

  public static void swapBytes(final byte[] bs, final int size) {
    if (size == 4) {
      byte temp1;
      byte temp2;
      for (int i = 0; i < bs.length; i += size) {
        temp1 = bs[i];
        temp2 = bs[i + 1];
        bs[i] = bs[i + 3];
        bs[i + 1] = bs[i + 2];
        bs[i + 2] = temp2;
        bs[i + 3] = temp1;
      }
    } else if (size == 2) {
      byte temp;
      for (int i = 0; i < bs.length; i += size) {
        temp = bs[i];
        bs[i] = bs[i + 1];
        bs[i + 1] = temp;
      }
    }
  }

  public static void swapBytes(final short[] sbits) {
    for (int i = 0; i < sbits.length; i++) {
      sbits[i] = swapBytes(sbits[i]);
    }
  }

  public static void swapBytes(final int[] ibits) {
    for (int i = 0; i < ibits.length; i++) {
      ibits[i] = swapBytes(ibits[i]);
    }
  }

  public static void swapBytes(final float[] fbits) {
    for (int i = 0; i < fbits.length; i++) {
      fbits[i] = swapBytes(fbits[i]);
    }
  }

  public static short swapBytes(final short sbits) {
    return (short) (sbits << 8 | sbits >> 8 & 0xff);
  }

  public static int swapBytes(final int ibits) {
    return ibits << 24 | ibits >> 24 & 0xff | (ibits & 0xff00) << 8 | (ibits & 0xff0000) >> 8;
  }

  public static float swapBytes(final float value) {
    return Float.intBitsToFloat(swapBytes(Float.floatToIntBits(value)));
  }
}
