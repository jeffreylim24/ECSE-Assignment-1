package ca.mcgill.ecse420.a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatrixMultiplication {
	
	private static final int NUMBER_THREADS = 4;
	private static final int MATRIX_SIZE = 2000;

	public static void main(String[] args) {
		
    validateSequential();
    validateParallel();

    // Commented out to avoid long runtime during validation. Uncomment to run as needed.

		// Generate two random matrices, same size
		// double[][] a = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
		// double[][] b = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
		// sequentialMultiplyMatrix(a, b);
		// parallelMultiplyMatrix(a, b);	
	}
	
	/**
   * Returns the result of a sequential matrix multiplication.
   * The two matrices are randomly generated.
   *
   * @param a is the first matrix
   * @param b is the second matrix
   * @return the result of the multiplication
   */
  public static double[][] sequentialMultiplyMatrix(double[][] a, double[][] b) {
    int rowsA = a.length;
    int colsA = a[0].length;
    int rowsB = b.length;
    int colsB = b[0].length;

    if (colsA != rowsB) {
      throw new IllegalArgumentException(
          "Cannot multiply: a is " + rowsA + "x" + colsA + ", b is " + rowsB + "x" + colsB);
    }

    double[][] c = new double[rowsA][colsB];

    for (int i = 0; i < rowsA; i++) {
      for (int j = 0; j < colsB; j++) {
        double sum = 0.0;
        for (int k = 0; k < colsA; k++) {
          sum += a[i][k] * b[k][j];
        }
        c[i][j] = sum;
      }
    }
    return c;
  }

  /**
   * Returns the result of a concurrent matrix multiplication using NUMBER_THREADS threads.
   * The two matrices are randomly generated.
   *
   * @param a is the first matrix
   * @param b is the second matrix
   * @return the result of the multiplication
   */
  public static double[][] parallelMultiplyMatrix(double[][] a, double[][] b) {
    return parallelMultiplyMatrix(a, b, NUMBER_THREADS);
  }

  /**
   * Returns the result of a concurrent matrix multiplication using the given number of
   * threads. The rows of the result matrix are split into numThreads contiguous blocks,
   * and each block is computed by one task running in a fixed-size thread pool.
   *
   * @param a is the first matrix
   * @param b is the second matrix
   * @param numThreads number of threads in the pool (and number of row blocks), capped at
   *     the number of rows of a
   * @return the result of the multiplication
   */
  public static double[][] parallelMultiplyMatrix(double[][] a, double[][] b, int numThreads) {
    int rowsA = a.length;
    int colsA = a[0].length;
    int rowsB = b.length;
    int colsB = b[0].length;

    if (colsA != rowsB) {
      throw new IllegalArgumentException(
          "Cannot multiply: a is " + rowsA + "x" + colsA + ", b is " + rowsB + "x" + colsB);
    }
    if (numThreads < 1) {
      throw new IllegalArgumentException("Number of threads must be at least 1");
    }
    // No point creating more threads than there are rows to compute
    numThreads = Math.min(numThreads, rowsA);

    // Shared result matrix. Each task writes only to its own rows, so no locking is needed.
    double[][] c = new double[rowsA][colsB];

    ExecutorService executor = Executors.newFixedThreadPool(numThreads);
    for (int t = 0; t < numThreads; t++) {
      // Split the rows as evenly as possible: block sizes differ by at most one row.
      int startRow = t * rowsA / numThreads;
      int endRow = (t + 1) * rowsA / numThreads;
      executor.execute(new RowBlockTask(a, b, c, startRow, endRow));
    }

    // Stop accepting new tasks, then block until every submitted task has finished.
    executor.shutdown();
    try {
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted while waiting for multiplication tasks", e);
    }
    return c;
  }

  /**
   * Task that computes rows startRow (inclusive) to endRow (exclusive) of c = a * b.
   * It only reads a and b, and only writes to its own rows of c.
   */
  private static class RowBlockTask implements Runnable {
    private final double[][] a;
    private final double[][] b;
    private final double[][] c;
    private final int startRow;
    private final int endRow;

    RowBlockTask(double[][] a, double[][] b, double[][] c, int startRow, int endRow) {
      this.a = a;
      this.b = b;
      this.c = c;
      this.startRow = startRow;
      this.endRow = endRow;
    }

    @Override
    public void run() {
      int colsA = a[0].length;
      int colsB = b[0].length;
      // Same dot-product loop as the sequential version, restricted to this task's rows
      for (int i = startRow; i < endRow; i++) {
        for (int j = 0; j < colsB; j++) {
          double sum = 0.0;
          for (int k = 0; k < colsA; k++) {
            sum += a[i][k] * b[k][j];
          }
          c[i][j] = sum;
        }
      }
    }
  }

	/**
	 * Populates a matrix of given size with randomly generated integers between 0-10.
	 * @param numRows number of rows
	 * @param numCols number of cols
	 * @return matrix
	 */
	private static double[][] generateRandomMatrix (int numRows, int numCols) {
		double matrix[][] = new double[numRows][numCols];
		for (int row = 0 ; row < numRows ; row++ ) {
			for (int col = 0 ; col < numCols ; col++ ) {
				matrix[row][col] = (double) ((int) (Math.random() * 10.0));
			}
		}
		return matrix;
  }

  /**
   * Validates sequentialMultiplyMatrix against cases whose correct answers are
   * known in advance. Prints PASS or FAIL for each case.
   */
  private static void validateSequential() {
    // Case 1: small non-square example worked out by hand (2x3 times 3x2)
    double[][] a = {{1, 2, 3}, {4, 5, 6}};
    double[][] b = {{7, 8}, {9, 10}, {11, 12}};
    double[][] expected = {{58, 64}, {139, 154}};
    printResult("Sequential: hand-computed 2x3 * 3x2", matricesEqual(sequentialMultiplyMatrix(a, b), expected));

    // Case 2: multiplying by the identity matrix must return the original matrix
    double[][] random = generateRandomMatrix(50, 50);
    double[][] identity = generateIdentityMatrix(50);
    printResult("Sequential: A * I = A", matricesEqual(sequentialMultiplyMatrix(random, identity), random));
    printResult("Sequential: I * A = A", matricesEqual(sequentialMultiplyMatrix(identity, random), random));

    // Case 3: multiplying by the zero matrix must return the zero matrix
    double[][] zero = new double[50][50];
    printResult("Sequential: A * 0 = 0", matricesEqual(sequentialMultiplyMatrix(random, zero), zero));

    // Case 4: incompatible dimensions must be rejected
    boolean threw = false;
    try {
      sequentialMultiplyMatrix(new double[2][3], new double[2][3]);
    } catch (IllegalArgumentException e) {
      threw = true;
    }
    printResult("Sequential: rejects 2x3 * 2x3", threw);
  }

  /**
   * Validates parallelMultiplyMatrix. First reruns the known-answer cases used for the
   * sequential method, then compares the parallel result against the (already validated)
   * sequential result on random matrices, for several thread counts and repeated trials.
   * Prints PASS or FAIL for each case.
   */
  private static void validateParallel() {
    // Case 1: same hand-computed example as the sequential test. With the default
    // NUMBER_THREADS (4) and only 2 rows, the thread count is capped at 2.
    double[][] a = {{1, 2, 3}, {4, 5, 6}};
    double[][] b = {{7, 8}, {9, 10}, {11, 12}};
    double[][] expected = {{58, 64}, {139, 154}};
    printResult("Parallel: hand-computed 2x3 * 3x2",
        matricesEqual(parallelMultiplyMatrix(a, b), expected));

    // Case 2: identity and zero matrices
    double[][] random = generateRandomMatrix(50, 50);
    double[][] identity = generateIdentityMatrix(50);
    double[][] zero = new double[50][50];
    printResult("Parallel: A * I = A",
        matricesEqual(parallelMultiplyMatrix(random, identity), random));
    printResult("Parallel: I * A = A",
        matricesEqual(parallelMultiplyMatrix(identity, random), random));
    printResult("Parallel: A * 0 = 0",
        matricesEqual(parallelMultiplyMatrix(random, zero), zero));

    // Case 3: incompatible dimensions must be rejected
    boolean threw = false;
    try {
      parallelMultiplyMatrix(new double[2][3], new double[2][3]);
    } catch (IllegalArgumentException e) {
      threw = true;
    }
    printResult("Parallel: rejects 2x3 * 2x3", threw);

    // Case 4: compare against the sequential result on random matrices.
    // 101 rows is prime, so it does not divide evenly by any thread count above 1,
    // which checks that no row is skipped or assigned to two blocks. 16 threads on a
    // 7-row matrix checks that capping the thread count at the number of rows still
    // gives the correct result.
    // Each configuration is repeated because a race condition may not show up every run.
    int trials = 20;
    int[] threadCounts = {1, 2, 3, 4, 7, 16};
    int[][] shapes = {{101, 101, 101}, {37, 53, 29}, {7, 5, 9}};
    for (int[] shape : shapes) {
      for (int numThreads : threadCounts) {
        boolean allMatched = true;
        for (int trial = 0; trial < trials; trial++) {
          double[][] x = generateRandomMatrix(shape[0], shape[1]);
          double[][] y = generateRandomMatrix(shape[1], shape[2]);
          double[][] parallel = parallelMultiplyMatrix(x, y, numThreads);
          if (!matricesEqual(parallel, sequentialMultiplyMatrix(x, y))) {
            allMatched = false;
          }
        }
        printResult("Parallel == sequential, " + shape[0] + "x" + shape[1] + " * "
            + shape[1] + "x" + shape[2] + ", " + numThreads + " threads, " + trials
            + " trials", allMatched);
      }
    }
  }

  /**
   * Compares two matrices for equality.
   * @param x matrix 1
   * @param y matrix 2
   * @return true if the two matrices have the same dimensions and entries
   */
  private static boolean matricesEqual(double[][] x, double[][] y) {
    if (x.length != y.length || x[0].length != y[0].length) {
      return false;
    }
    for (int i = 0; i < x.length; i++) {
      for (int j = 0; j < x[0].length; j++) {
        if (x[i][j] != y[i][j]) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Generates an identity matrix of size n x n.
   * @param n size of the matrix
   * @return identity matrix of size n x n
   */
  private static double[][] generateIdentityMatrix(int n) {
    double[][] identity = new double[n][n];
    for (int i = 0; i < n; i++) {
      identity[i][i] = 1.0;
    }
    return identity;
  }

  /**
   * Prints the result of a test case.
   * @param testName name of the test case
   * @param passed true if the test case passed, false otherwise
   */
  private static void printResult(String testName, boolean passed) {
    System.out.println((passed ? "PASS: " : "FAIL: ") + testName);
  }
}
