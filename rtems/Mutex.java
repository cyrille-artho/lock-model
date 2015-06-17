package rtems;
import base.Lock;
import base.Condition;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Mutex {
	int nestCount;
	PriorityQueue<Object> waitQueue;
	RTEMSThread holder;
	//Object orderRec;
	int priorityBefore; 
	final Lock parentLock = new /*Reentrant*/Lock();
	final Condition cv1  = parentLock.newCondition(); 
	MyComparator comparator = new MyComparator();
	public Mutex()
	{
		this.nestCount = 0;
		this.holder = null;
		this.priorityBefore = -1;
		PriorityQueue<RTEMSThread> waitQueue = new PriorityQueue<RTEMSThread>(7, comparator);
	}
	public void lock() throws InterruptedException{
		parentLock.lock();
		RTEMSThread thisThread = (RTEMSThread)Thread.currentThread();
		try{
			if((holder!=null) && (holder!=thisThread))
			{
				if(priorityRaiseFilter(thisThread.currentPriority))
				{
					//1. Update priority of holder thread
					updatePriority(thisThread.currentPriority);
					//for solution to nested mutex problem call below
					//updateRecPriority(thisThread.currentPriority);
					//2. Re-enqueue holder thread with modified priority if its waiting 
					reEnqueue();
				}
				thisThread.state = Thread.State.WAITING;
				while(!(thisThread.state==Thread.State.RUNNABLE)){
					this.waitQueue.offer(thisThread);
					thisThread.wait = waitQueue;
					cv1.wait();
				}
			}
			//if code reaches here it means it has the potential to acquire the mutex
			if(holder==null)
			{
				holder = thisThread;
				nestCount = 1;
				this.priorityBefore = thisThread.currentPriority;
				thisThread.mutexOrderList.add(0, this);
				// FIXME: Really prepend? Use LinkedList if prepend is common
				//Also have to chain it to threads lockMutex chain
			}
			else
			{
				nestCount++;
				//how should we prepend here???Doubt the orderRec as it is already present in thisThread.mutexList
			}
		  }finally{
			parentLock.unlock();
		  }

	}
	public void unlock() throws InterruptedException{
		Object topMutex=null;
		RTEMSThread thisThread = (RTEMSThread)Thread.currentThread();
		RTEMSThread candidateThr;
		int stepdownPri;
		parentLock.lock();
		//proper step down of priority.
		//remove eligible candidate thread from this.waitQueue
		//set the state of that thread to Params.RUNNABLE
		//signalAll()
		//unlockparentLock.lock()
		try{
			//1.Assertion Check
			topMutex = thisThread.mutexOrderList.get(0);
			if(topMutex!=this){
				//assertion error for strict order mutexes
			}
			//Not handling the nesting case right now.
			//2. Remove top element from mutexOrderList
			//mutexOrderList is acting as chaincontrol structure for us.
			//Right now avoiding complications
			topMutex = thisThread.mutexOrderList.remove(0);
			//3. Initialize the mutex orderList
			//this.orderRec.node.init();
			//4. stepdown of priority for this thread
			stepdownPri = this.priorityBefore;

			thisThread.setPriority(stepdownPri);
			//5. Re-enqueue if thread is waiting
			reEnqueue();
			holder = null;
			candidateThr = (RTEMSThread)waitQueue.poll();
			if(candidateThr != null){
				candidateThr.state = Thread.State.RUNNABLE;
				//Logically only candidate will go through lock and rest will again get queued up
				cv1.notifyAll();
			}

		}finally{
			parentLock.unlock();
		}
	}

	public boolean priorityRaiseFilter(int priority){
		int holderPriority = holder.getPriority();
		if(priority < holderPriority){
			return true;
		}
		return false;
	}

	public void updatePriority(int priority)
	{
		holder.currentPriority = priority;
		holder.setPriority(priority);
	}

	public void updateRecPriority(int priority)
	{
		updatePriority(priority);
		holder.setPriority(priority);
		int mutexIdx = holder.getMutexIndex(this);
		//Assertion check
		if(mutexIdx == -1)
		{
			System.out.println("Mutex not found in holder's mutexOrderList");
		}
		else
		{
			int i;
			Mutex candidate;
			for(i=mutexIdx-1;i>=0;i--)
			{
				candidate = holder.mutexOrderList.get(i);
				if(candidate.priorityBefore < priority)
					break;
				candidate.priorityBefore = priority;
			}
		}
	}
	
	public void reEnqueue()
	{
		//if holder thread is waiting on someother mutex reenqueue that thread with updated priority.
		PriorityQueue<Object> pqueue;
		if(holder.wait!=null)
		{
			pqueue = holder.wait;
			pqueue.remove(holder);
			pqueue.offer(holder);
		}
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

