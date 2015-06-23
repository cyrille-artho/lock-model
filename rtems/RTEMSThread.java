package rtems;
import java.util.PriorityQueue;
import java.util.ArrayList;

public class RTEMSThread extends Thread {
  // TODO: add extra priority field etc.
	PriorityQueue<RTEMSThread> wait;
	int resourceCount;
	Thread.State state;
	int currentPriority;
	int realPriority;
	ArrayList<Mutex> mutexOrderList;  //it is a linkedList which stores acquired mutex objects in LIFO order.  

	public RTEMSThread() {
		this.mutexOrderList = new ArrayList<Mutex>();
		this.state = this.getState();
		this.currentPriority = this.realPriority = this.getPriority();
		System.out.println("rtems init:" + this.currentPriority);
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
