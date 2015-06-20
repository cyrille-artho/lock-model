package harness;

import base.Lock;
import rtems.Mutex;

//import gov.nasa.jpf.jvm.Verify;
import gov.nasa.jpf.vm.Verify;

public class Environment {
  public final static int N_THREADS = 2;
  static final Lock[] locks = { createLock(), createLock(), createLock() };

  static Lock createLock() {
    // factory method to swap out lock impl. in one place
    return new /*Prio*/Mutex();
  }

  public final static void main(String[] args) {
    int li1 = Verify.getInt(0, locks.length - 1);
    int li2 = Verify.getInt(0, locks.length - 1);
    int li3 = Verify.getInt(0, locks.length - 1);
    Thread t0 = new TestThread(new int[]{li1, li2, li3});
    t0.setPriority(Verify.getInt(1, 3));
    System.out.println("Thread 0 has priority " + t0.getPriority() +
		       " and uses locks " + li1 + ", " + li2 +
		       ", and " + li3 + ".");
    t0.start();
    for (int i = 1; i < N_THREADS; i++) {
      int li = Verify.getInt(0, locks.length - 1);
      Thread t = new TestThread(new int[]{li});
      t.setPriority(Verify.getInt(1, 3));
      System.out.println("Thread " + Integer.toString(i + 1) +
			 " has priority " + t.getPriority() +
			 " and uses lock " + li + ".");
      t.start();
    }
  }
}
