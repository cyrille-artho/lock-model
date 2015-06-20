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
			while((holder!=null) && (holder!=thisThread))
			{
				try{
					thisThread.state = Thread.State.WAITING;
					if(priorityRaiseFilter(thisThread.currentPriority))
					{
						updatePriority(thisThread.currentPriority);
						if(holder.wait!=null) 
							reEnqueue();
					}
					if(this.waitQueue.contains(thisThread)==false){
						this.waitQueue.offer(thisThread);
					}
					thisThread.wait = waitQueue;
					wait();
							
					}catch (InterruptedException e) 
					{}
				
			}
			//if code reaches here it means it has the potential to acquire the mutex
			assert thisThread.getState() != Thread.State.WAITING;
			if(holder==null)
			{
				holder = thisThread;
				assert nestCount==0;
			}
			if(this.nestCount==0)
			{
				this.priorityBefore = thisThread.currentPriority;
				thisThread.mutexOrderList.add(0, this);
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
			thisThread.setPriority(this.priorityBefore);
			assert holder!=null;
			assert holder.wait==null;
			//if(holder.wait!=null)
			//	reEnqueue();
			holder = waitQueue.poll();			
			if(holder != null){
				assert holder.state==Thread.State.WAITING;
				holder.state = Thread.State.RUNNABLE;
				holder.wait=null;
				notifyAll();
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

