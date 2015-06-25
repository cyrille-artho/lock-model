package harness;

import base.Lock;
import rtems.Mutex;
import rtems.RTEMSThread;

//import gov.nasa.jpf.jvm.Verify;
import gov.nasa.jpf.vm.Verify;

public class Environment {
  public final static int N_THREADS = 3;
  static final Lock[] locks = { createLock(0), createLock(1), createLock(2), createLock(3), createLock(4), createLock(5) };

  static Lock createLock(int id) {
    // factory method to swap out lock impl. in one place
    System.out.println("mutex id: "+id);
    return new /*Prio*/Mutex(id);
  }

  public final static void main(String[] args) {
    /*int li1 = Verify.getInt(0, locks.length - 1);
    int li2 = Verify.getInt(0, locks.length - 1);
    int li3 = Verify.getInt(0, locks.length - 1);*/
    int li1 = 1;
    int li2 = 2;
    RTEMSThread t0 = new TestThread(new int[]{li1, li2});
    //t0.setPriority(Verify.getInt(1, 3));
    t0.setPriority(3);
    t0.setRealPriority();
    t0.setCurrentPriority();
    System.out.println("Thread 0 has priority " + t0.getPriority() +
		       " and uses locks " + li1 + //", " + li2 +
		       ", and " + li2 + ".");
    t0.start();
    for (int i = 1; i < N_THREADS; i++) {
      int li = i;
      RTEMSThread t = new TestThread(new int[]{li});
      t.setPriority(i);
      t.setRealPriority();
      t.setCurrentPriority();
      System.out.println("Thread " + Integer.toString(i + 1) +
			 " has priority " + t.getPriority() +
			 " and uses lock " + li + ".");
      t.start();
    }
    System.exit(1);
  }
}
