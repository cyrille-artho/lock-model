package rtems;
import base.Lock;
import base.Condition;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Iterator;

public class Mutex extends Lock {
	RTEMSThread holder;
	int id;
	int nestCount;
	int priorityBefore=-1;
	MyComparator comparator = new MyComparator();
	PriorityQueue<RTEMSThread> waitQueue = new PriorityQueue<RTEMSThread>(7, comparator);
	static Object globalLock = new Object(); // models kernel-wide lock

	public Mutex(int idx){

		this.id = idx;
		this.nestCount = 0;
		this.priorityBefore = -1;
		this.holder=null;
	}
	 
	public void lock() {
		synchronized(globalLock) {
			RTEMSThread thisThread = (RTEMSThread)Thread.currentThread();
			while((holder!=null) && (holder!=thisThread))
			{
				assert (thisThread.currentPriority == thisThread.getPriority());
				try{
					thisThread.state = Thread.State.WAITING;
					if(priorityRaiseFilter(thisThread.currentPriority))
					{
						System.out.println("raising priority of thread: "+ holder.getId() + "by thread : " + thisThread.getId()+" frm :"+holder.currentPriority + " to: "+ thisThread.currentPriority);
						/*
							- updateRecPriority() solves the unbounded priority inheritance problem.
							- updatePriority() is the current behavior of RTEMS which has this problem.
						*/
						updateRecPriority(thisThread.currentPriority);
						System.out.println("updated pr for tid: "+holder.getId() +" current pr: "+holder.currentPriority);
						if(holder.wait!=null) 
							reEnqueue();
					}
					if(waitQueue.contains(thisThread)==false){
						System.out.println("Adding thread :" + thisThread.getId() + " in waitQ of mutex: "+id);
						waitQueue.offer(thisThread);
					}
					thisThread.wait = waitQueue;
					validator();
					globalLock.wait();
							
					}catch (InterruptedException e) 
					{}
				
			}
			//if code reaches here it means it has the potential to acquire the mutex
			System.out.println("thread-id:"+ thisThread.getId() + " acquiring mutex "+ id);
			assert thisThread.getState() != Thread.State.WAITING;
			if(holder==null)
			{
				holder = thisThread;
				assert nestCount==0;
			}
			if(nestCount==0)
			{
				assert !(thisThread.mutexOrderList.contains(this));
				System.out.println("thread: "+thisThread.getId() + "adding mutex: "+ id + " to its mutexOrderList");
				priorityBefore = thisThread.currentPriority;
				thisThread.mutexOrderList.add(0, this);
				assert thisThread.mutexOrderList.contains(this);
			}
			nestCount++;
			thisThread.resourceCount++;
		}
	}

	public void unlock() {
		synchronized(globalLock) {
		Mutex topMutex=null;
		RTEMSThread thisThread = (RTEMSThread)Thread.currentThread();
		RTEMSThread candidateThr;
		int stepdownPri;
		assert nestCount>0;
		assert thisThread.resourceCount>0;
		nestCount--;
		thisThread.resourceCount--;
		if(nestCount==0)
		{
			topMutex = thisThread.mutexOrderList.get(0);
			assert this==topMutex;		
			topMutex = thisThread.mutexOrderList.remove(0);
		//<---------------------------------------------------------------Thread 1 crosses this-------------->

			System.out.println("Holder Thread: "+thisThread.getId()+"before resetting priority_before : "+ thisThread.getPriority()+" while releasing mutex: " + id);
			thisThread.setPriority(priorityBefore);
			thisThread.currentPriority = priorityBefore;
			System.out.println("Holder Thread: "+thisThread.getId()+ " after stepdown ops-->current priority: " + thisThread.getPriority() + " while releasing mutex: " + id);
			validator();
			assert holder!=null;
			assert holder.wait==null;
		//<--------------------------------------------But Thread 1 does not crosses this----------------------->	
			holder = waitQueue.poll();			
			if(holder != null){
				assert holder.state==Thread.State.WAITING;
				holder.state = Thread.State.RUNNABLE;
				holder.wait=null;
				globalLock.notifyAll();
			}
		}			
		}
	}

/*
Validator function checks that after stepping down the priority, on unlock() operation, 
there should be no higher priority thread contending on any of the mutex still held by holder. 
*/

	public void validator(){
		RTEMSThread chkThr;
		Mutex chkMtx;
		RTEMSThread thisThread = (RTEMSThread)Thread.currentThread();
		Iterator<Mutex> mItr = thisThread.mutexOrderList.iterator();
		while (mItr.hasNext()){
			chkMtx = mItr.next();
			System.out.println("--->Mutex: "+chkMtx.id);
			chkThr = chkMtx.waitQueue.peek();
			if(chkThr!=null)
			{
				System.out.println("------>Thread-id: "+ chkThr.getId()+" priority: "+ chkThr.getPriority());
				assert (thisThread.getPriority()<=chkThr.getPriority());	
			}
			
		}

	}

	public boolean priorityRaiseFilter(int priority){
		int holderPriority = holder.getPriority();
		return (priority < holderPriority);
	}

	public void updatePriority(int priority)
	{
		holder.currentPriority = priority;
		holder.setPriority(priority);
	}

	public void updateRecPriority(int priority)
	{
		
		
		int i;
		Mutex candidate;
		int mutexIdx = this.holder.getMutexIndex(this);
		assert this.holder!=null;		
		//Assertion check
		assert mutexIdx!=-1;
		for(i=mutexIdx-1;i>=0;i--)
		{
			candidate = holder.mutexOrderList.get(i);
			if(candidate.priorityBefore < priority)
				break;
			candidate.priorityBefore = priority;
		}
		updatePriority(priority);

		/* need to include fix for spsem03 test case of indirect reference */
	
	}
	
	public void reEnqueue()
	{
		//if holder thread is waiting on someother mutex reenqueue that thread with updated priority.
		PriorityQueue<RTEMSThread> pqueue;
		RTEMSThread thisThread = (RTEMSThread)Thread.currentThread();
		System.out.println("thread: "+holder.getId()+" being re-enqued by thread: " + thisThread.getId());
		pqueue = holder.wait;
		pqueue.remove(holder);
    //<--------- Nice bug uncovered!! and coincidently matches with spsem03----------------->
	/* thread 2 raised priority of thread 1 from 3 to 1. Thread 1 was already waiting in queue for mutex 2 hold
	by thread 3 whose priority is 2. So again we got unbounded priority inheritance problem. We should now  for 
	correct behavior should raise thread 3 priority for avoiding UPI"*/

		pqueue.offer(holder);
	}
}

class MyComparator implements Comparator<RTEMSThread>
{
	@Override
	public int compare(RTEMSThread t1, RTEMSThread t2)
	{
		return t1.getPriority() - t2.getPriority();
	}
}
