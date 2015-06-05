public class TestThread extends RTEMSThread {
  int idx1, idx2;
  Lock l1, l2;

  public TestThread(int li1, int li2) {
    idx1 = li1; // for simpler diagnostics
    idx2 = li2; // for simpler diagnostics
    l1 = Environment.locks[li1];
    l2 = Environment.locks[li2];
  }

  public void run() {
    l1.lock();
    l2.lock();
    l2.unlock();
    l1.unlock();
  }
}
