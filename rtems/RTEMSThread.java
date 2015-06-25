package rtems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class RTEMSThread extends Thread {
  // TODO: add extra priority field etc.
	public PriorityQueue<RTEMSThread> wait;
	public int resourceCount;
	public Thread.State state;
	public int currentPriority;
	public int realPriority;
	public List<Mutex> mutexOrderList;  //it is a linkedList which stores acquired mutex objects in LIFO order.

	public RTEMSThread() {
		this.mutexOrderList = /*Collections.synchronizedList(*/new ArrayList<Mutex>()/*)*/;
		this.state = this.getState();
		this.currentPriority = this.realPriority = this.getPriority();
	}

	public void setCurrentPriority(){
		this.currentPriority = getPriority();
	}

	public void setRealPriority(){
		this.realPriority = getPriority();
	}

	public int getMutexIndex(Mutex obj){
		return this.mutexOrderList.indexOf(obj);
	}

}
