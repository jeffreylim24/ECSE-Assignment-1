package ca.mcgill.ecse420.a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class DiningPhilosophersNoDeadlock {

  private static final int DEFAULT_NUMBER_OF_PHILOSOPHERS = 5;

  /** Upper bounds (ms) for the random time spent thinking and eating. */
  private static final int MAX_THINK_MS = 10;
  private static final int MAX_EAT_MS = 10;

  /**
   * Upper bound (ms) for the random pause between picking up the first and the second
   * chopstick. 0 (the default) means no pause. In 3.1 this pause made deadlock happen quickly,
   * so here it lets us check that the same pause no longer causes deadlock. Set once in main()
   * before any philosopher starts, and only read afterwards.
   */
  private static int maxReachMs = 0;

  /** How often (ms) the main thread prints the meal counts. */
  private static final long PRINT_MS = 1000;

  /**
   * Starts one thread per philosopher and prints, once a second, how many meals each
   * philosopher ate in that second, until the program is stopped.
   *
   * @param args optional number of philosophers (at least 2, default 5), then optional
   *     maximum pause in ms between picking up the two chopsticks (default 0), then optional
   *     "fair" or "unfair" chopsticks (default fair)
   */
  public static void main(String[] args) throws InterruptedException {

    int numberOfPhilosophers =
        args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_NUMBER_OF_PHILOSOPHERS;
    if (numberOfPhilosophers < 2) {
      throw new IllegalArgumentException("Need at least 2 philosophers");
    }
    if (args.length > 1) {
      maxReachMs = Integer.parseInt(args[1]);
      if (maxReachMs < 0) {
        throw new IllegalArgumentException("maxReachMs must be non-negative");
      }
    }

    boolean fair = true;
    if (args.length > 2) {
      if (args[2].equals("unfair")) {
        fair = false;
      } else if (!args[2].equals("fair")) {
        throw new IllegalArgumentException("Third argument must be fair or unfair");
      }
    }

    Philosopher[] philosophers = new Philosopher[numberOfPhilosophers];
    Chopstick[] chopsticks = new Chopstick[numberOfPhilosophers];

    for (int i = 0; i < numberOfPhilosophers; i++) {
      chopsticks[i] = new Chopstick(i, fair);
    }

    for (int i = 0; i < numberOfPhilosophers; i++) {
      philosophers[i] = new Philosopher(
          chopsticks[i], chopsticks[(i + 1) % numberOfPhilosophers]);
    }

    System.out.println("Starting " + numberOfPhilosophers + " philosophers, "
        + (fair ? "fair" : "unfair") + " chopsticks, pause between chopsticks up to "
        + maxReachMs + " ms. Press Ctrl+C to stop.");
    long startTime = System.currentTimeMillis();
    ExecutorService executor = Executors.newFixedThreadPool(numberOfPhilosophers);
    for (Philosopher philosopher : philosophers) {
      executor.execute(philosopher);
    }

    // Runs until the user stops the program. Each line shows the meals eaten by each
    // philosopher since the previous line. A deadlock would show up as a line of all zeros,
    // and a starving philosopher as a column that stays at zero while the others keep eating.
    int[] mealsAtLastPrint = new int[numberOfPhilosophers];
    while (true) {
      Thread.sleep(PRINT_MS);
      long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
      StringBuilder line = new StringBuilder(elapsedSeconds + " s: meals in last second [");
      for (int i = 0; i < numberOfPhilosophers; i++) {
        int meals = philosophers[i].getMealsEaten();
        line.append(i == 0 ? "" : ", ").append(meals - mealsAtLastPrint[i]);
        mealsAtLastPrint[i] = meals;
      }
      line.append("]");
      System.out.println(line);
    }
  }

  /**
   * Sleeps for a random time between 0 and maxMs milliseconds.
   *
   * @param maxMs upper bound of the sleep, in milliseconds
   */
  private static void randomSleep(int maxMs) throws InterruptedException {
    Thread.sleep((long) (Math.random() * (maxMs + 1)));
  }

  public static class Philosopher implements Runnable {

    private final Chopstick first;
    private final Chopstick second;

    private volatile int mealsEaten = 0;

    Philosopher(Chopstick left, Chopstick right) {
      if (left.getId() < right.getId()) {
        this.first = left;
        this.second = right;
      } else {
        this.first = right;
        this.second = left;
      }
    }

    @Override
    public void run() {
      try {
        while (true) {
          randomSleep(MAX_THINK_MS);

          first.pickUp();
          try {
            if (maxReachMs > 0) {
              randomSleep(maxReachMs);
            }
            second.pickUp();
            try {
              randomSleep(MAX_EAT_MS);
              mealsEaten++;
            } finally {
              second.putDown();
            }
          } finally {
            first.putDown();
          }
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    int getMealsEaten() {
      return mealsEaten;
    }
  }

  public static class Chopstick {

    private final int id;
    private final ReentrantLock lock;

    Chopstick(int id, boolean fair) {
      this.id = id;
      this.lock = new ReentrantLock(fair);
    }

    void pickUp() {
      lock.lock();
    }

    void putDown() {
      lock.unlock();
    }

    int getId() {
      return id;
    }
  }
}
