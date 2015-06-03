class PrioLock extends Lock {
  Object owner;
  int count = 0;
  int maxPrio = 0;
  int waitCount = 0;
  Thread waitingThreads[] = new Thread[Environment.N_THREADS];

  void lock() {
    Thread thisThread = Thread.currentThread();
    int prio = thisThread.getPriority();
    synchronized(this) {
      if (prio > maxPrio) {
	maxPrio = prio;
      }
      waitingThreads[waitCount++] = thisThread;
    }
    synchronized(this) {
      while (count != 0 && owner != thisThread && prio < maxPrio) {
	// cannot obtain lock; wait for unlock
	try {
	  wait();
	} catch (InterruptedException e) {
	}
      }
      owner = Thread.currentThread();
      count++;
      for (int i = 0; i < waitCount; i++) {
	if (waitingThreads[i] == thisThread) { // remove thread from array
          waitingThreads[i] = waitingThreads[waitCount - 1];
	  waitingThreads[waitCount - 1] = null;
        }
      }
      waitCount--;
    }
  }

  synchronized void unlock() {
    super.unlock();
    // update maxPrio of waiting threads
    maxPrio = 0;
    for (int i = 0; i < waitCount; i++) {
      int prio = waitingThreads[i].getPriority();
      if (prio > maxPrio) {
	maxPrio = prio;
      }
    }
  }
}
