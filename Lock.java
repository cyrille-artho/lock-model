class Lock {
  Object owner;
  int count = 0;

  synchronized void lock() {
    while (count != 0 && owner != Thread.currentThread()) {
      try {
	wait();
      } catch (InterruptedException e) {
      }
    }
    owner = Thread.currentThread();
    count++;
  }

  synchronized void unlock() {
    if (--count == 0) {
      owner = null;
      notifyAll();
    }
  }
}
