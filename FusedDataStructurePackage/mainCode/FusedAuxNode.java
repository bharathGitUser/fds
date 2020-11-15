




public class FusedAuxNode {
	private FusedNode fusNode; 
	public FusedNode getFusNode() {
		return fusNode;
	}

	public void setFusNode(FusedNode fusNode) {
		this.fusNode = fusNode;
	}

	public FusedAuxNode(FusedNode node){
		this.fusNode = node; 
	}
	
	public String toString(){
		return fusNode.getCodeValue()+""; 
	}

}
