package rtems;

import base.Lock;
import base.Condition;

public class Mutex {
	int nestCount;
	Object waitQueue;
	Object holder;
	Object orderRec; 
	final Lock parentLock = new /*Reentrant*/Lock();
   	final Condition cv1  = parentLock.newCondition(); 
	public Mutex()
	{
		this.nestCount = 0;
		this.holder = null;
		this.orderRec = new OrderList();
	}
	public void lock() throws InterruptedException{
		parentLock.lock();
		RTEMSThread thisThread = (RTEMSThread)Thread.currentThread();
		try{
			if((holder!=null) && (holder!=thisThread))
			{
				if(priorityRaiseFilter(thisThread.getPriority()))
				{
					//1. Update priority of holder thread
					updatePriority();
					//2. Re-enqueue holder thread with modified priority if its waiting 
					renqueue();
				}
				thisThread.state = Thread.State.WAITING;
				while(!(thisThread.state==Thread.State.RUNNABLE)){
					cv1.await();
				}
			}
			//if code reaches here it means it has the potential to acquire the mutex
			if(holder==null)
			{
				holder = thisThread;
				nestCount = 1;
				orderRec.priorityBefore = thisThread.currentPriority;
				thisThread.mutexOrderList.add(0, this);
				// FIXME: Really prepend? Use LinkedList if prepend is common
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
		parentLock.lock();
		//proper step down of priority.
		//remove eligible candidate thread from this.waitQueue
		//set the state of that thread to Params.RUNNABLE
		//signalAll()
		//unlockparentLock.lock()
	}
	public void updatePriority()
	{

	}

	public void renqueue()
	{
		//if holder thread is waiting on someother mutex reenqueue that thread with updated priority.
	}
}
