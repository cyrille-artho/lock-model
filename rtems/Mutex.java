package rtems;
import base.Lock;
import base.Condition;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Mutex extends Lock {
	int nestCount = 0;
	RTEMSThread holder;
	//Object orderRec;
	int priorityBefore = -1; 
	final Lock parentLock = new /*Reentrant*/Lock();
	MyComparator comparator = new MyComparator();
	PriorityQueue<RTEMSThread> waitQueue = new PriorityQueue<RTEMSThread>(7, comparator);

	public synchronized void lock() {
		RTEMSThread thisThread = (RTEMSThread)Thread.currentThread();
			if((holder!=null) && (holder!=thisThread))
			{
				if(priorityRaiseFilter(thisThread.currentPriority))
				{
					//1. Update priority of holder thread
					updatePriority(thisThread.currentPriority);
					//for solution to nested mutex problem call below
					//updateRecPriority(thisThread.currentPriority);
					//2. Re-enqueue holder thread with modified priority if its waiting
					if(holder.wait!=null) 
						reEnqueue();
				}
				thisThread.state = Thread.State.WAITING;
				while(thisThread.state !=Thread.State.RUNNABLE){
					this.waitQueue.offer(thisThread);
					thisThread.wait = waitQueue;
					try{
						wait();
					} catch (InterruptedException e) {
					}
				}
			}
			//if code reaches here it means it has the potential to acquire the mutex
			if(holder == null)
			{
				holder = thisThread;
				this.nestCount = 1;
				this.priorityBefore = thisThread.currentPriority;
				thisThread.mutexOrderList.add(0, this);
				// FIXME: Really prepend? Use LinkedList if prepend is common
				//Also have to chain it to threads lockMutex chain
			}
			else
			{
				assert nestCount>0;
				this.nestCount++;
				//how should we prepend here???Doubt the orderRec as it is already present in thisThread.mutexList
			}
			thisThread.resourceCount++;
	}

	public synchronized void unlock() {
		Mutex topMutex=null;
		RTEMSThread thisThread = (RTEMSThread)Thread.currentThread();
		RTEMSThread candidateThr;
		int stepdownPri;
		//proper step down of priority.
		//remove eligible candidate thread from this.waitQueue
		//set the state of that thread to Params.RUNNABLE
		//signalAll()
		//unlockparentLock.lock()
			//1.Assertion Check on this mutex.nestCount!=0
		assert nestCount>0;
		assert thisThread.resourceCount>0;
		this.nestCount--;
		if(this.nestCount==0)
		{
			topMutex = thisThread.mutexOrderList.get(0);
			//Assertion on nestCount!=0
			/*if(topMutex!=this){
				//assertion error for strict order mutexes
			}*/
			//System.out.println(topMutex);
			assert this==topMutex;
			
			topMutex = thisThread.mutexOrderList.remove(0);
			thisThread.setPriority(this.priorityBefore);
			//5. Re-enqueue if thread is waiting
			if(holder.wait!=null)
				reEnqueue();
			holder = null;
			candidateThr = waitQueue.poll();
			if(candidateThr != null){
				candidateThr.state = Thread.State.RUNNABLE;
				//Logically only candidate will go through lock and rest will again get queued up
				notifyAll();
			}
		}
		thisThread.resourceCount--;

			
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
		PriorityQueue<RTEMSThread> pqueue;
		pqueue = holder.wait;
		pqueue.remove(holder);
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

