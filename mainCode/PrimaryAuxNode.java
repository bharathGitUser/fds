


public class PrimaryAuxNode<E> extends Node{
	private PrimaryNode<E> primNode; 
	public PrimaryAuxNode(){}
	public PrimaryAuxNode(PrimaryNode<E> primNode){
		this.primNode = primNode; 
	}
	public String toString(){
		return primNode.getValue()+""; 
	}
	public PrimaryNode<E> getPrimNode() {
		return primNode;
	}
	public void setPrimNode(PrimaryNode<E> primNode) {
		this.primNode = primNode;
	}
	
}
