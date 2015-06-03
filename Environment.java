import gov.nasa.jpf.vm.Verify;

class Environment {
  public final static int N_THREADS = 3;

  public final static void main(String[] args) {
    final Lock l = new PrioLock();
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
