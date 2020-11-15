


public class PrimaryNode<E> {
	private E value;
	private PrimaryAuxNode<Integer> auxNode; 
	public PrimaryNode(E value){
		this.value = value; 
	}
	public String toString(){
		return value+""; 
	}
	public E getValue() {
		return value;
	}
	public void setValue(E value) {
		this.value = value;
	}
	public PrimaryAuxNode<Integer> getAuxNode() {
		return auxNode;
	}
	public void setAuxNode(PrimaryAuxNode<Integer> auxNode) {
		this.auxNode = auxNode;
	}
	
}
