package rtems;
import java.util.PriorityQueue;
import java.util.ArrayList;

public class RTEMSThread extends Thread {
  // TODO: add extra priority field etc.
	public PriorityQueue<RTEMSThread> wait;
	public int resourceCount;
	public Thread.State state;
	public int currentPriority;
	public int realPriority;
	public ArrayList<Mutex> mutexOrderList;  //it is a linkedList which stores acquired mutex objects in LIFO order.  

	public RTEMSThread() {
		this.mutexOrderList = new ArrayList<Mutex>();
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
