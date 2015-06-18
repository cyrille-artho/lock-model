package rtems;
import java.util.PriorityQueue;
import java.util.ArrayList;

public class RTEMSThread extends Thread {
  // TODO: add extra priority field etc.
	PriorityQueue<RTEMSThread> wait;
	int resourceCount;
	Thread.State state;
	//Object lockMutex; mutexOrderList will suffice
	int currentPriority;
	int realPriority;
	ArrayList<Mutex> mutexOrderList;  //it is a linkedList which stores acquired mutex objects in LIFO order.  
	public RTEMSThread(int priority, Thread.State state){
		this();
		this.currentPriority = this.realPriority = priority;
		this.state = state;
		//this.lockMutex = new ChainControl();
		PriorityQueue<RTEMSThread> wait = null;
	}

	public RTEMSThread() {
		currentPriority = 1;
		this.mutexOrderList = new ArrayList<Mutex>();
		state = Thread.State.NEW;
	}

	public int getMutexIndex(Mutex obj){
		return this.mutexOrderList.indexOf(obj);
	}

}
