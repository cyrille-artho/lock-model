package rtems;

public class ChainControl{
	Object next;
	Object prev;
	public ChainControl(){
		this.next = null;
		this.prev = null;
	}
	public void init(){
		this.next = null;
		this.prev = null;
	}
}
