public class RTEMSThread extends Thread {
  // TODO: add extra priority field etc.
	Object wait;
	int resourceCount;
	int state;
	Object lockMutex;
	int currentPriority;
	int realPriority;
	Object mutexOrderList;  //it is a linkedList which stores acquired mutex objects in LIFO order.  
	public RTEMSThread(int priority, int state){
		this.currentPriority = this.realPriority = priority;
		this.mutexOrderList = new MutexList();
		this.state = state;
	}
}
