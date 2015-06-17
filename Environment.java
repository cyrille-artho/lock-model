//import gov.nasa.jpf.jvm.Verify;
import gov.nasa.jpf.vm.Verify;

class Environment {
  public final static int N_THREADS = 3;
  static final Lock[] locks = { createLock(), createLock() };

  static Lock createLock() {
    // factory method to swap out lock impl. in one place
    return new /*Prio*/Lock();
  }

  public final static void main(String[] args) {
    for (int i = 0; i < N_THREADS; i++) {
      int li1 = Verify.getInt(0, 1);
      int li2 = Verify.getInt(0, 1);
      Thread t = new TestThread(li1, li2);
      t.setPriority(Verify.getInt(1, 3));
      System.out.println("Thread " + Integer.toString(i + 1) +
			 " has priority " + t.getPriority() +
			 " and uses locks " + li1 + ", " + li2 + ".");
      t.start();
    }
  }
}
