public class Mutex{
	int nestCount;
	Object waitQueue;
	Object holder;
	Object orderRec; 
	public Mutex()
	{
		this.nestCount = 0;
		this.holder = null;
		this.orderRec = new OrderList()
	}
	
}
public class ChainControl{
	Object next;
	Object prev;
	public ChainControl(){
		this.next = null;
		this prev = null;
	}
}
public class OrderList{
	Object node;
	int priorityBefore;
	public OrderList(){
		this.node = new ChainControl()
	}
}