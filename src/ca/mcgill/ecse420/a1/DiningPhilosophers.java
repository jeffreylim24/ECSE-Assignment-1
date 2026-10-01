package ca.mcgill.ecse420.a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class DiningPhilosophers {

  private static final int DEFAULT_NUMBER_OF_PHILOSOPHERS = 5;

  /** Upper bounds (ms) for the random time spent thinking and eating. */
  private static final int MAX_THINK_MS = 10;
  private static final int MAX_EAT_MS = 10;

  /**
   * Upper bound (ms) for the random pause between picking up the left and the right
   * chopstick. 0 (the default) means no pause. Set once in main() before any philosopher
   * starts, and only read afterwards.
   */
  private static int maxReachMs = 0;

  /** How often (ms) the main thread checks for deadlock. */
  private static final long POLL_MS = 100;
  /** How long (ms) a suspected deadlock must last, with no meals eaten, to be reported. */
  private static final long DEADLOCK_CONFIRM_MS = 1000;
  /** Give up (ms) if no deadlock has been seen after this long. */
  private static final long MAX_RUN_MS = 60000;
	
  /**
   * Starts one thread per philosopher, waits until they deadlock (or MAX_RUN_MS passes),
   * and prints the state of every philosopher.
   *
   * @param args optional number of philosophers (at least 2, default 5), then optional
   *     maximum pause in ms between picking up the two chopsticks (default 0)
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

    Philosopher[] philosophers = new Philosopher[numberOfPhilosophers];
    Chopstick[] chopsticks = new Chopstick[numberOfPhilosophers];

    for (int i = 0; i < numberOfPhilosophers; i++) {
      chopsticks[i] = new Chopstick(i);
    }

    for (int i = 0; i < numberOfPhilosophers; i++) {
      philosophers[i] = new Philosopher(
          i, chopsticks[i], chopsticks[(i + 1) % numberOfPhilosophers]);
    }

    System.out.println("Starting " + numberOfPhilosophers + " philosophers, pause between "
        + "chopsticks up to " + maxReachMs + " ms");
    long startTime = System.currentTimeMillis();
    ExecutorService executor = Executors.newFixedThreadPool(numberOfPhilosophers);
    for (Philosopher philosopher : philosophers) {
      executor.execute(philosopher);
    }

    boolean deadlocked = waitForDeadlock(philosophers, startTime);
    long elapsed = System.currentTimeMillis() - startTime;

    if (deadlocked) {
      System.out.println("DEADLOCK detected after " + elapsed + " ms, "
          + totalMeals(philosophers) + " meals eaten in total");
    } else {
      System.out.println("No deadlock within " + elapsed + " ms, "
          + totalMeals(philosophers) + " meals eaten in total");
    }
    for (Philosopher philosopher : philosophers) {
      System.out.println("  " + philosopher);
    }

    System.exit(0);
  }

  /**
   * Sleeps for a random time between 0 and maxMs milliseconds.
   *
   * @param maxMs upper bound of the sleep, in milliseconds
   */
  private static void randomSleep(int maxMs) throws InterruptedException {
    Thread.sleep((long) (Math.random() * (maxMs + 1)));
  }
  
  /** 
   * What the philosopher is currently doing. 
   * */
  public enum State {
    THINKING, WAITING_FOR_LEFT, WAITING_FOR_RIGHT, EATING
  }

  /**
   * Returns the total number of meals eaten so far by all philosophers.
   *
   * @param philosophers all philosophers at the table
   */
  private static int totalMeals(Philosopher[] philosophers) {
    int total = 0;
    for (Philosopher philosopher : philosophers) {
      total += philosopher.getMealsEaten();
    }
    return total;
  }

  /**
   * Returns true if every philosopher is holding their left chopstick and waiting for their
   * right one.
   *
   * @param philosophers all philosophers at the table
   */
  private static boolean allWaitingForRight(Philosopher[] philosophers) {
    for (Philosopher philosopher : philosophers) {
      if (philosopher.getState() != State.WAITING_FOR_RIGHT) {
        return false;
      }
    }
    return true;
  }

  /**
   * Waits until either a deadlock is detected or MAX_RUN_MS has passed. A deadlock is
   * detected if all philosophers are waiting for their right chopstick and no meals have
   * been eaten for DEADLOCK_CONFIRM_MS.
   *
   * @param philosophers all philosophers at the table
   * @param startTime time (ms) at which the philosophers were started
   * @return true if deadlock was detected, false if MAX_RUN_MS passed first
   */
  private static boolean waitForDeadlock(Philosopher[] philosophers, long startTime)
      throws InterruptedException {
    while (System.currentTimeMillis() - startTime < MAX_RUN_MS) {
      Thread.sleep(POLL_MS);
      if (allWaitingForRight(philosophers)) {
        int mealsBefore = totalMeals(philosophers);
        Thread.sleep(DEADLOCK_CONFIRM_MS);
        if (allWaitingForRight(philosophers) && totalMeals(philosophers) == mealsBefore) {
          return true;
        }
      }
    }
    return false;
  }

	public static class Philosopher implements Runnable {

    private final int id;
    private final Chopstick left;
    private final Chopstick right;

    private volatile State state = State.THINKING;
    private volatile int mealsEaten = 0;

    Philosopher(int id, Chopstick left, Chopstick right) {
      this.id = id;
      this.left = left;
      this.right = right;
    }

		@Override
    public void run() {
      try {
        while (true) {
          state = State.THINKING;
          randomSleep(MAX_THINK_MS);

          state = State.WAITING_FOR_LEFT;
          left.pickUp();
          try {
            state = State.WAITING_FOR_RIGHT;
            // Optional time taken to reach for the second chopstick (off by default). It
            // does not cause the deadlock, which happens with no pause at all. It only
            // widens the window in which the others can also take their left chopstick,
            // so that deadlock shows up quickly even for large tables.
            if (maxReachMs > 0) {
              randomSleep(maxReachMs);
            }
            right.pickUp();
            try {
              state = State.EATING;
              randomSleep(MAX_EAT_MS);
              mealsEaten++;
            } finally {
              right.putDown();
            }
          } finally {
            left.putDown();
          }
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    State getState() {
      return state;
    }

    int getMealsEaten() {
      return mealsEaten;
    }

    @Override
    public String toString() {
      String doing;
      switch (state) {
        case WAITING_FOR_LEFT:
          doing = "is waiting for chopstick " + left.getId();
          break;
        case WAITING_FOR_RIGHT:
          doing = "holds chopstick " + left.getId() + ", waiting for chopstick "
              + right.getId();
          break;
        case EATING:
          doing = "is eating with chopsticks " + left.getId() + " and " + right.getId();
          break;
        default:
          doing = "is thinking";
      }
      return "Philosopher " + id + " " + doing + " (" + mealsEaten + " meals)";
    }
	}

  public static class Chopstick {

    private final int id;
    private final ReentrantLock lock = new ReentrantLock();

    Chopstick(int id) {
      this.id = id;
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
