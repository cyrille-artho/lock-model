package rtems;

public class Mutex{
	int nestCount;
	Object waitQueue;
	Object holder;
	Object orderRec; 
	final Lock parentLock = new ReentrantLock();
   	final Condition cv1  = lock.newCondition(); 
	public Mutex()
	{
		this.nestCount = 0;
		this.holder = null;
		this.orderRec = new OrderList();
	}
	public void lock() throws InterruptedException{
		parentLock.lock();
		thisThread = Thread.currentThread();
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
				thisThread.state = Params.WAIT;
				while(!(thisThread.state==PARAM.RUNNABLE)){
					cv1.await();
				}
			}
			//if code reaches here it means it has the potential to acquire the mutex
			if(holder==null)
			{
				holder = thisThread;
				nestCount = 1;
				orderRec.priorityBefore = thisThread.currentPriority;
				thisThread.mutexList.prepend(this);
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
