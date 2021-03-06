package harness;

import base.Lock;

import rtems.RTEMSThread;

public class TestThread extends RTEMSThread {
  int idx[]; // for simpler diagnosis later (if I get some help on JPF)
  Lock locks[];

  public TestThread(int lockIdx[]) {
    idx = lockIdx;
    for (int i = 0; i < idx.length; i++) {
      locks[i] = Environment.locks[idx[i]];
    }
  }

  public void run() {
    for (int i = 0; i < idx.length; i++) {
      locks[i].lock();
    }
    for (int i = 0; i < idx.length; i++) {
      locks[i].unlock();
    }
  }
}
