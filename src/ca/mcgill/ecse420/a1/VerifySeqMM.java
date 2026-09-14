package ca.mcgill.ecse420.a1;

import java.util.Arrays;

/** Tests that the sequential matrix multiplication produces correct results. */
public class VerifySeqMM {

  /** Runs the identity, zero and hand-computed matrix tests and prints whether each passed. */
  public static void main(String[] args) {
    
    final int[] sizes = {1, 2, 3, 4, 5, 10, 100};
    
    // Identity matrix test
    for (int n : sizes) {
      if (identityMatrixTest(n)) {
        System.out.println("✅ Identity matrix test passed for size " + n);
      } else {
        System.out.println("❌ Identity matrix test failed for size " + n);
      }
    }

    // Zero matrix test
    for (int n : sizes) {
      if (zeroMatrixTest(n)) {
        System.out.println("✅ Zero matrix test passed for size " + n);
      } else {
        System.out.println("❌ Zero matrix test failed for size " + n);
      }
    }

    // Non-square Hand computed test
    double[][] a = {{1, 2, 3}, {4, 5, 6}};
    double[][] b = {{1, 2}, {3, 4}, {5, 6}};
    double[][] expectedAB = {{22, 28}, {49, 64}};
    double[][] expectedBA = {{9, 12, 15}, {19, 26, 33}, {29, 40, 51}};
    if (preCalculatedTest(a, b, expectedAB)) {
      System.out.println("✅ Hand-computed test passed for A*B");
    } else {
      System.out.println("❌ Hand-computed test failed for A*B");
    }
    if (preCalculatedTest(b, a, expectedBA)) {
      System.out.println("✅ Hand-computed test passed for B*A");
    } else {
      System.out.println("❌ Hand-computed test failed for B*A");
    }
  }

  /**
   * Tests that multiplying a random matrix by an identity matrix returns the original matrix.
   * @param n the size of the identity matrix
   * @return true if the test passes, false otherwise
   */
  private static boolean identityMatrixTest(int n) {
    double[][] identityMatrix = identity(n);
    double[][] randomMatrix = generateRandomMatrix(n, n);
    double[][] result1 =
        MatrixMultiplication.sequentialMultiplyMatrix(randomMatrix, identityMatrix);
    double[][] result2 =
        MatrixMultiplication.sequentialMultiplyMatrix(identityMatrix, randomMatrix);
    return Arrays.deepEquals(result1, randomMatrix) && Arrays.deepEquals(result2, randomMatrix);
  }
  
  /**
   * Tests that multiplying a random matrix by a zero matrix returns a zero matrix.
   * @param n the size of the zero matrix
   * @return true if the test passes, false otherwise
   */
  private static boolean zeroMatrixTest(int n) {
    double[][] zeroMatrix = new double[n][n];
    double[][] randomMatrix = generateRandomMatrix(n, n);
    double[][] result1 =
        MatrixMultiplication.sequentialMultiplyMatrix(randomMatrix, zeroMatrix);
    double[][] result2 =
        MatrixMultiplication.sequentialMultiplyMatrix(zeroMatrix, randomMatrix);
    return Arrays.deepEquals(result1, zeroMatrix) && Arrays.deepEquals(result2, zeroMatrix);
  }

  /**
   * Tests that multiplying two matrices returns the expected result.
   * @param a
   * @param b
   * @param expected
   * @return true if the test passes, false otherwise
   */
  private static boolean preCalculatedTest(double[][] a, double[][] b, double[][] expected) {
    double[][] result = MatrixMultiplication.sequentialMultiplyMatrix(a, b);
    return Arrays.deepEquals(result, expected);
  }

  /**
   * Creates an identity matrix of the specified size.
   *
   * @param n the size of the identity matrix
   * @return the identity matrix
   */
  public static double[][] identity(int n) {
    double[][] m = new double[n][n];
    for (int i = 0; i < n; i++) {
      m[i][i] = 1;
    }
    return m;
  }

  /**
   * Generates a random matrix of the specified size.
   *
   * @param rows the number of rows
   * @param cols the number of columns
   * @return the random matrix
   */
  public static double[][] generateRandomMatrix(int rows, int cols) {
    double[][] m = new double[rows][cols];
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        m[i][j] = Math.random() * 10; // Random values between 0 and 10
      }
    }
    return m;
  }
}
