package ca.mcgill.ecse420.a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Sequential and parallel multiplication of randomly generated square matrices. */
public class MatrixMultiplication {

  private static final int NUMBER_THREADS = 1;
  private static final int MATRIX_SIZE = 2000;

  /** Multiplies two random {@code MATRIX_SIZE} square matrices sequentially and in parallel. */
  public static void main(String[] args) {
    // Generate two random matrices, same size
    double[][] a = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
    double[][] b = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
    sequentialMultiplyMatrix(a, b);
    parallelMultiplyMatrix(a, b);
  }

  /**
   * Returns the result of a sequential matrix multiplication. The two matrices are randomly
   * generated.
   *
   * @param a is the first matrix
   * @param b is the second matrix
   * @return the result of the multiplication
   */
  public static double[][] sequentialMultiplyMatrix(double[][] a, double[][] b) {
    double[][] result = new double[a.length][b[0].length];
    for (int i = 0; i < a.length; i++) {
      for (int j = 0; j < b[0].length; j++) {
        for (int k = 0; k < a[0].length; k++) {
          result[i][j] += a[i][k] * b[k][j];
        }
      }
    }
    return result;
  }

  /**
   * Returns the result of a concurrent matrix multiplication. The two matrices are randomly
   * generated.
   *
   * @param a is the first matrix
   * @param b is the second matrix
   * @return the result of the multiplication
   */
  public static double[][] parallelMultiplyMatrix(double[][] a, double[][] b) {
    int numRows = a.length;
    double[][] result = new double[numRows][b[0].length];

    // To ensure there are no empty tasks
    int numTasks = Math.min(NUMBER_THREADS, numRows);
    int rowsPerTask = numRows / numTasks;
    int leftoverRows = numRows % numTasks;

    ExecutorService executor = Executors.newFixedThreadPool(NUMBER_THREADS);
    int startRow = 0;
    for (int task = 0; task < numTasks; task++) {
      // The first leftoverRows tasks take one extra row each
      int endRow = startRow + rowsPerTask - 1 + (task < leftoverRows ? 1 : 0);
      final int firstRow = startRow;
      final int lastRow = endRow;
      executor.execute(() -> multiplyRows(a, b, result, firstRow, lastRow));
      startRow = endRow + 1;
    }
    executor.shutdown();

    try {
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException("Matrix multiplication interrupted", e);
    }
    return result;
  }

  /**
   * Computes rows {@code firstRow} to {@code lastRow} of the
   * product {@code a * b} and stores them in {@code result}.
   *
   * @param a is the first matrix
   * @param b is the second matrix
   * @param result is the matrix the computed rows are written to
   * @param firstRow is the first row to compute
   * @param lastRow is the last row to compute
   */
  private static void multiplyRows(
      double[][] a, double[][] b, double[][] result, int firstRow, int lastRow) {
    for (int i = firstRow; i <= lastRow; i++) {
      for (int j = 0; j < b[0].length; j++) {
        for (int k = 0; k < a[0].length; k++) {
          result[i][j] += a[i][k] * b[k][j];
        }
      }
    }
  }

  /**
   * Populates a matrix of given size with randomly generated integers between 0-10.
   *
   * @param numRows number of rows
   * @param numCols number of cols
   * @return matrix
   */
  private static double[][] generateRandomMatrix(int numRows, int numCols) {
    double[][] matrix = new double[numRows][numCols];
    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numCols; col++) {
        matrix[row][col] = (double) ((int) (Math.random() * 10.0));
      }
    }
    return matrix;
  }
}
