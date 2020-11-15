

public  class Node {
	private Node next;
	private Node prev;
	private int value =0;
	public void setValue(int value){
		this.value = value;
	}
	public int getValue(){
		return value;
		} 
	public Node getNext() {
		return next;
	}
	public void setNext(Node next) {
		this.next = next;
	}
	public Node getPrev() {
		return prev;
	}
	public void setPrev(Node previous) {
		this.prev = previous;
	}
	public String toString(){
		return value+"";
	}

}
