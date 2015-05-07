/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package beta.cop.model.math;

import java.util.Arrays;

public class ConjugateSolver {

  public static int CGsolver(float[] a, int[] iRow, int[] iCol, int nEnt, float[] b, int nEqn, float[] x, int nPar,
      int niter, float eps) {
    int iter;
    int i;
    float[] s;
    float[] p;
    float[] r;
    float[] q;
    float rnorm2old = 0f;
    float rnorm2new = 0.0f;
    float qnorm2;
    float alpha, beta;
    float rnorm2orig = 0f;

    s = new float[nEqn];

    p = new float[nPar];
    r = new float[nPar];

    q = new float[nEqn];

    Arrays.fill(p, '\0');

    // s = b - A * x0
    matrixMultiplyAx(a, iRow, iCol, nEnt, x, q, nEqn);
    for (i = 0; i < nEqn; i++) {
      s[i] = b[i] - q[i];
    }
    for (iter = 0; iter < niter; iter++) {

      // r = At * s
      matrixMultiplyAx(a, iCol, iRow, nEnt, s, r, nPar);

      rnorm2new = norm2(r, nPar);

      System.out.printf("iter=%d rnorm=%g\n", iter, rnorm2new);

      if (rnorm2new <= 0.0) {
        break;
      }

      if (iter == 0) {
        alpha = 0.0f;
        rnorm2orig = rnorm2new;
      } else {
        alpha = rnorm2new / rnorm2old;
      }
      rnorm2old = rnorm2new;

      // p(k+1) = r(k) + alpha * p(k)
      for (i = 0; i < nPar; i++) {
        p[i] = r[i] + alpha * p[i];
      }

      // q(k+1) = A * p(k+1)
      matrixMultiplyAx(a, iRow, iCol, nEnt, p, q, nEqn);

      qnorm2 = norm2(q, nEqn);

      beta = rnorm2old / qnorm2;

      // x(k+1) = x(k) + beta * p
      for (i = 0; i < nPar; i++) {
        x[i] += beta * p[i];
      }

      if (rnorm2old < eps * rnorm2orig) {
        break;
      }

      // s(k+1) = s(k) - beta * q
      for (i = 0; i < nEqn; i++) {
        s[i] -= beta * q[i];
      }

    }

    s = null;
    p = null;
    r = null;
    q = null;

    return 0;
  }

  /*
  * Multiplication of sparse matrix A with vector x to get b.
  * 
  * a:    sparse matrix A with row and column indices recorded in irow and icol;
  * irow: row indices;
  * icol: column indices;
  * na:   size of matrix A;
  * x:    input vector of x;
  * b:    output vector b=A*x
  * nB:   size of b
  *
  * to calculate At * b = x, exchange row and column indices
  *
  * Jerry.Yuan@conocoPhillips.com, 3/1/2010
  */
  static void matrixMultiplyAx(float[] a, int[] irow, int[] icol, int na, float[] x, float[] b, int nb) {
    int i;

    Arrays.fill(b, '\0');

    for (i = 0; i < na; i++) {
      b[irow[i]] += a[i] * x[icol[i]];
    }
  }

  public static float norm2(float r[], int n) {

    double sum = 0.0;
    float sumf;
    int i;

    for (i = 0; i < n; i++) {
      sum += r[i] * r[i];
    }
    sumf = (float) sum;
    return sumf;
  }

}
