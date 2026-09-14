package ca.mcgill.ecse420.a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

/** Simulates the dining philosophers problem with one thread per philosopher. */
public class DiningPhilosophers {

  /** Sets up the philosophers and the chopsticks they share. */
  public static void main(String[] args) {
    int numberOfPhilosophers = 5;
    Philosopher[] philosophers = new Philosopher[numberOfPhilosophers];
    Object[] chopsticks = new Object[numberOfPhilosophers];
  }

  /** A philosopher that alternates between thinking and eating. */
  public static class Philosopher implements Runnable {

    @Override
    public void run() {}
  }
}
