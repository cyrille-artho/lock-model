package rtems;

public class OrderList{
	Object node;
	int priorityBefore;
	public OrderList(){
		this.node = new ChainControl();
	}
}
