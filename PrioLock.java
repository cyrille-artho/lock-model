class PrioLock extends Lock {
  Object owner;
  int count = 0;
  int topPrio = Integer.MAX_VALUE; // dummy value
  int waitCount = 0;
  Thread waitingThreads[] = new Thread[Environment.N_THREADS];

  void lock() {
    Thread thisThread = Thread.currentThread();
    int prio = thisThread.getPriority();
    synchronized(this) {
      if (prio < topPrio) {
	topPrio = prio;
      }
      waitingThreads[waitCount++] = thisThread;
    }
    synchronized(this) {
      while (count != 0 && owner != thisThread && prio > topPrio) {
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
    // update topPrio of waiting threads
    topPrio = Integer.MAX_VALUE;
    for (int i = 0; i < waitCount; i++) {
      int prio = waitingThreads[i].getPriority();
      if (prio < topPrio) {
	topPrio = prio;
      }
    }
  }
}
