package ca.mcgill.ecse420.a1;

import java.util.Arrays;

/** Tests that the sequential matrix multiplication produces correct results. */
public class VerifySeqMM {

  /** Runs the identity, zero and hand-computed matrix tests and prints whether each passed. */
  public static void main(String[] args) {
    // Identity matrix test
    double[][] identityMatrix = identity(3);
    double[][] randomMatrix = generateRandomMatrix(3, 3);
    double[][] result1 =
        MatrixMultiplication.sequentialMultiplyMatrix(randomMatrix, identityMatrix);
    double[][] result2 =
        MatrixMultiplication.sequentialMultiplyMatrix(identityMatrix, randomMatrix);
    if (Arrays.deepEquals(result1, randomMatrix) && Arrays.deepEquals(result2, randomMatrix)) {
      System.out.println("✅ Identity matrix test passed.");
    } else {
      System.out.println("❌ Identity matrix test failed.");
    }

    // Zero matrix test
    double[][] zeroMatrix = new double[3][3];
    double[][] result3 = MatrixMultiplication.sequentialMultiplyMatrix(randomMatrix, zeroMatrix);
    double[][] result4 = MatrixMultiplication.sequentialMultiplyMatrix(zeroMatrix, randomMatrix);
    if (Arrays.deepEquals(result3, zeroMatrix) && Arrays.deepEquals(result4, zeroMatrix)) {
      System.out.println("✅ Zero matrix test passed.");
    } else {
      System.out.println("❌ Zero matrix test failed.");
    }

    // Non-square Hand computed test
    double[][] a = {{1, 2, 3}, {4, 5, 6}};
    double[][] b = {{1, 2}, {3, 4}, {5, 6}};
    double[][] expected = {{22, 28}, {49, 64}};
    double[][] result = MatrixMultiplication.sequentialMultiplyMatrix(a, b);
    if (Arrays.deepEquals(result, expected)) {
      System.out.println("✅ Non-square Hand computed test passed.");
    } else {
      System.out.println("❌ Non-square Hand computed test failed.");
    }
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
