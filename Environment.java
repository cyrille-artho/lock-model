import gov.nasa.jpf.vm.Verify;

class Environment {
  public final static int N_THREADS = 3;

  static Lock createLock() {
    // factory method to swap out lock impl. in one place
    return new PrioLock();
  }

  public final static void main(String[] args) {
    final Lock l = createLock();
    for (int i = 0; i < N_THREADS; i++) {
      Thread t = new Thread() {
	public void run() {
	  l.lock();
	  l.unlock();
	}
      };
      t.setPriority(Verify.getInt(1, 3));
      t.start();
    }
  }
}
