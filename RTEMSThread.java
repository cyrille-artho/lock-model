public class RTEMSThread extends Thread {
  // TODO: add extra priority field etc.
	Object wait;
	int resourceCount;
	int state;
	Object lockMutex;
	int currentPriority;
	int realPriority;
	Object mutexList;
	public RTEMSThread(int priority, int state){
		this.currentPriority = this.realPriority = priority;
		this.mutexList = new MutexList();
		this.state = state;
	}
}
