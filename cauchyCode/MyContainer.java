/*
 * my own implementation of a doubly linked list...
 */
public class MyContainer{
	private Node sentinelHead;
	private Node sentinelTail;

	public MyContainer() {
		
	//set up sentinel heads and tails..makes the flipping very easy	
	 sentinelHead = new Node();
	 sentinelHead.setValue(-1);
	 
	 sentinelTail = new Node();	 
	 sentinelTail.setValue(-1);
	 
	 sentinelHead.setNext(sentinelTail);
	 sentinelHead.setPrev(null);
	 
	 sentinelTail.setNext(null);
	 sentinelTail.setPrev(sentinelHead);
	}

	public void add(Node node){//adding to end of list
		Node tail = sentinelTail.getPrev();  
		tail.setNext(node);
		node.setPrev(tail); 
		
		node.setNext(sentinelTail);
		sentinelTail.setPrev(node); 
	}
	
	public Node getLast(){
		try{
			if(isEmpty())
				throw new Exception("List is empty!!"); 
		}catch(Exception e){
			return null;
		}
		return sentinelTail.getPrev();
	}

	public Node getFirst(){
		try{
			if(isEmpty())
				throw new Exception("List is empty!!"); 
		}catch(Exception e){
			return null;
		}
		return sentinelHead.getNext();
	}

	public Node getNext(Node n){
		Node next = n.getNext(); 
		try {
			if(next == sentinelTail)
				throw new Exception("No element!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return null; 
		}
		return next;  
	}
	public boolean isEmpty(){
		return (sentinelHead.getNext() == sentinelTail); 
	}
	public Node getPrevious(Node n){
		Node prev = n.getPrev();  
		try {
			if(prev == sentinelHead)
				throw new Exception("No element!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return null; 
		}
		return prev;  
	}

	public void replaceNodeWithTail(Node node){
		Node tail = sentinelTail.getPrev(); 
		Node secondLast = tail.getPrev(); 
		
		if(node == secondLast){//no shifting to be done
			node.getPrev().setNext(tail);
			tail.setPrev(node.getPrev());
			return; 
		}
		
		//Left hand side
		node.getPrev().setNext(tail);
		tail.setPrev(node.getPrev());

		//right hand side
		tail.setNext(node.getNext());
		node.getNext().setPrev(tail); 

		secondLast.setNext(sentinelTail);
		sentinelTail.setPrev(secondLast);
	}
	
	public void pop(){
		Node tail = sentinelTail.getPrev();
		tail.getPrev().setNext(sentinelTail); 
		sentinelTail.setPrev(tail.getPrev()); 
	}
	
	public int size(){
		int size =0; 
		Node node = sentinelHead.getNext(); 
		while(node.getValue()!= -1){
			node = node.getNext();
			size++; 
		}
		return size; 
	}
	public String toString(){
		String out = "[";
		out = out+ sentinelHead+" ";
		Node node = sentinelHead.getNext(); 
		while(node.getValue()!= -1){
			out = out + node+" "; 
			node = node.getNext(); 
		}
		out = out+ node +" ";
		return out+"]"; 
	}
}
