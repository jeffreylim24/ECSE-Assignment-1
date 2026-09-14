package ca.mcgill.ecse420.a1;

import java.util.Arrays;

/** Tests that the parallel matrix multiplication produces correct results and is faster. */
public class VerifyParallelMM {

  // Large enough that thread start-up overhead is small compared to the multiplication
  private static final int TIMING_SIZE = 1000;

  /** Runs the identity, zero, hand-computed and speedup tests and prints whether each passed. */
  public static void main(String[] args) {

    // Includes primes so the rows don't split evenly across threads
    final int[] sizes = {1, 2, 3, 4, 5, 7, 10, 13, 100, 101};

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

    // Speedup test (NOTE: update NUMBER_THREADS in MatrixMultiplication.java to test with more threads)
    if (speedupTest(TIMING_SIZE)) {
      System.out.println("✅ Parallel is faster than sequential for size " + TIMING_SIZE);
    } else {
      System.out.println("❌ Parallel is not faster than sequential for size " + TIMING_SIZE);
    }
  }

  /**
   * Tests that multiplying a random matrix by an identity matrix returns the original matrix.
   * @param n the size of the identity matrix
   * @return true if the test passes, false otherwise
   */
  private static boolean identityMatrixTest(int n) {
    double[][] identityMatrix = VerifySeqMM.identity(n);
    double[][] randomMatrix = VerifySeqMM.generateRandomMatrix(n, n);
    double[][] result1 =
        MatrixMultiplication.parallelMultiplyMatrix(randomMatrix, identityMatrix);
    double[][] result2 =
        MatrixMultiplication.parallelMultiplyMatrix(identityMatrix, randomMatrix);
    return Arrays.deepEquals(result1, randomMatrix) && Arrays.deepEquals(result2, randomMatrix);
  }

  /**
   * Tests that multiplying a random matrix by a zero matrix returns a zero matrix.
   * @param n the size of the zero matrix
   * @return true if the test passes, false otherwise
   */
  private static boolean zeroMatrixTest(int n) {
    double[][] zeroMatrix = new double[n][n];
    double[][] randomMatrix = VerifySeqMM.generateRandomMatrix(n, n);
    double[][] result1 =
        MatrixMultiplication.parallelMultiplyMatrix(randomMatrix, zeroMatrix);
    double[][] result2 =
        MatrixMultiplication.parallelMultiplyMatrix(zeroMatrix, randomMatrix);
    return Arrays.deepEquals(result1, zeroMatrix) && Arrays.deepEquals(result2, zeroMatrix);
  }

  /**
   * Tests that multiplying two matrices returns the expected result.
   * @param a the first matrix
   * @param b the second matrix
   * @param expected the expected product of a and b
   * @return true if the test passes, false otherwise
   */
  private static boolean preCalculatedTest(double[][] a, double[][] b, double[][] expected) {
    double[][] result = MatrixMultiplication.parallelMultiplyMatrix(a, b);
    return Arrays.deepEquals(result, expected);
  }

  /**
   * Tests that the parallel multiplication of two random matrices is faster than the sequential
   * one. Both versions are run once on smaller matrices first so the JIT compiler has warmed up
   * before timing, otherwise whichever runs first is penalised.
   * @param n the size of the matrices to time
   * @return true if the parallel version is faster, false otherwise
   */
  private static boolean speedupTest(int n) {
    double[][] a = VerifySeqMM.generateRandomMatrix(n, n);
    double[][] b = VerifySeqMM.generateRandomMatrix(n, n);

    long start = System.nanoTime();
    MatrixMultiplication.sequentialMultiplyMatrix(a, b);
    long sequentialTime = System.nanoTime() - start;

    start = System.nanoTime();
    MatrixMultiplication.parallelMultiplyMatrix(a, b);
    long parallelTime = System.nanoTime() - start;

    System.out.printf(
        "   Sequential: %.3f s, Parallel: %.3f s, Speedup: %.2fx%n",
        sequentialTime / 1e9, parallelTime / 1e9, (double) sequentialTime / parallelTime);
    return parallelTime < sequentialTime;
  }
}
