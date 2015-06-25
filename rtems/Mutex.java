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
	public Mutex(int idx){

		this.id = idx;
		this.nestCount = 0;
		this.priorityBefore = -1;
		this.holder=null;
	}
	 
	public synchronized void lock() {
		RTEMSThread thisThread = (RTEMSThread)Thread.currentThread();	
			while((holder!=null) && (holder!=thisThread))
			{
				assert (thisThread.currentPriority == thisThread.getPriority());
				try{
					thisThread.state = Thread.State.WAITING;
					if(priorityRaiseFilter(thisThread.currentPriority))
					{
						System.out.println("raising pr of tid : "+ holder.getId() + "by tid: " + thisThread.getId()+" frm :"+holder.currentPriority + " to: "+ thisThread.currentPriority);
						updateRecPriority(thisThread.currentPriority);
						//<-----------------------------------Thread 2 reaches till here(fails in updateRecPriority------------->
						System.out.println("updated pr for tid: "+holder.getId() +" current pr: "+holder.currentPriority);
						if(holder.wait!=null) 
							reEnqueue();
					}
					if(this.waitQueue.contains(thisThread)==false){
						System.out.println("Adding thread :" + thisThread.getId() + " in waitQ of mutex: "+this.id);
						this.waitQueue.offer(thisThread);
					}
					thisThread.wait = waitQueue;
					//validator(1);
					wait();
							
					}catch (InterruptedException e) 
					{}
				
			}
			//if code reaches here it means it has the potential to acquire the mutex
			System.out.println("thread-id:"+ thisThread.getId() + " acquiring mutex "+ this.id);
			assert thisThread.getState() != Thread.State.WAITING;
			if(holder==null)
			{
				holder = thisThread;
				assert nestCount==0;
			}
			if(this.nestCount==0)
			{
				assert !(thisThread.mutexOrderList.contains(this));
				System.out.println("thread: "+thisThread.getId() + "adding mutex: "+ this.id + " to its mutexOrderList");
				this.priorityBefore = thisThread.currentPriority;
				thisThread.mutexOrderList.add(0, this);
				assert thisThread.mutexOrderList.contains(this);
			}
			this.nestCount++;
			thisThread.resourceCount++;
	}

	public synchronized void unlock() {
		Mutex topMutex=null;
		RTEMSThread thisThread = (RTEMSThread)Thread.currentThread();
		RTEMSThread candidateThr;
		int stepdownPri;
		assert nestCount>0;
		assert thisThread.resourceCount>0;
		this.nestCount--;
		thisThread.resourceCount--;
		if(this.nestCount==0)
		{
			topMutex = thisThread.mutexOrderList.get(0);
			assert this==topMutex;		
			topMutex = thisThread.mutexOrderList.remove(0);
		//<---------------------------------------------------------------Thread 1 crosses this-------------->

			System.out.println("Holder Thread: "+thisThread.getId()+"before resetting priority_before : "+ thisThread.getPriority()+" while releasing mutex: " + this.id);
			thisThread.setPriority(this.priorityBefore);
			thisThread.currentPriority = this.priorityBefore;
			System.out.println("Holder Thread: "+thisThread.getId()+ " after stepdown ops-->current priority: " + thisThread.getPriority() + " while releasing mutex: " + this.id);
			validator();
			assert holder!=null;
			assert holder.wait==null;
		//<--------------------------------------------But Thread 1 does not crosses this----------------------->	
			this.holder = waitQueue.poll();			
			if(holder != null){
				assert holder.state==Thread.State.WAITING;
				holder.state = Thread.State.RUNNABLE;
				holder.wait=null;
				notifyAll();
			//	System.out.println("Released Mutex: "+topMutex.id+" by thread: "+thisThread.getId());
			}
			//System.out.println(" holder = null Released Mutex: "+topMutex.id+" by thread: "+thisThread.getId());
		}			
	}

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
		//RTEMSThread thisThread = (RTEMSThread)Thread.currentThread();
		//System.out.println("thread id "+ thisThread.getId() + " in updateRecPriority");
		//Mutex chkMtx;
		assert this.holder!=null;
		//Iterator<Mutex> mItr = this.holder.mutexOrderList.iterator();
		int mutexIdx = this.holder.getMutexIndex(this);
		//System.out.println("chcking holder id before crashing: "+this.holder.getId()+" And iterating over its list of mutex");
		//while (mItr.hasNext()){
		//	chkMtx = mItr.next();
		//	System.out.println("Checking--->Mutex: "+chkMtx.id);
		//}

		//Assertion check
		//assert mutexIdx!=-1;	
		for(i=mutexIdx-1;i>=0;i--)
		{
			candidate = holder.mutexOrderList.get(i);
			if(candidate.priorityBefore < priority)
				break;
			candidate.priorityBefore = priority;
		}
		updatePriority(priority);
	
	}
	
	public void reEnqueue()
	{
		//if holder thread is waiting on someother mutex reenqueue that thread with updated priority.
		PriorityQueue<RTEMSThread> pqueue;
		RTEMSThread thisThread = (RTEMSThread)Thread.currentThread();
		System.out.println("thread: "+holder.getId()+" being re-enqued by thread: " + thisThread.getId());
		pqueue = holder.wait;
		pqueue.remove(holder);
    //<--------- Nice bug uncovered!!----------------->
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
