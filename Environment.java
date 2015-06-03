import gov.nasa.jpf.vm.Verify;

class Environment {
  public final static int N_THREADS = 3;

  static Lock createLock() {
    // factory method to swap out lock impl. in one place
    return new /*Prio*/Lock();
  }

  public final static void main(String[] args) {
    final Lock[] locks = { createLock(), createLock() };
    for (int i = 0; i < N_THREADS; i++) {
      Thread t = new Thread() {
	public void run() {
	  Lock l1 = locks[Verify.getInt(0, 1)];
	  Lock l2 = locks[Verify.getInt(0, 1)];
	  l1.lock();
	  l2.lock();
	  l2.unlock();
	  l1.unlock();
	}
      };
      t.setPriority(Verify.getInt(1, 3));
      t.start();
    }
  }
}
