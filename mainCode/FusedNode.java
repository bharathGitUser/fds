




public class FusedNode extends Node{
	private int codeValue;
	private int refCount; 
	private FusedAuxNode[] auxNode;
	public FusedNode(int numPrimaries){
		auxNode = new FusedAuxNode[numPrimaries];
	}

	public int getCodeValue() {
		return codeValue;
	}
	public FusedAuxNode[] getAuxNode() {
		return auxNode;
	}
	public void setAuxNode(int primaryId, FusedAuxNode auxNode) {
		this.auxNode[primaryId] = auxNode;
	}

	public void addElement(int newElement, int primaryId, int backupId){
		try {
			codeValue = Fusion.getUpdatedCode(codeValue, backupId, 0, newElement, primaryId);
			++refCount; 
		} catch (InterfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void removeElement(int oldElement, int primaryId, int backupId){
		try {
			codeValue = Fusion.getUpdatedCode(codeValue, backupId, oldElement, 0, primaryId);
			--refCount; 
		} catch (InterfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean isEmpty(){
		if(refCount ==0)
			return true;
		else
			return false; 
	}

	public String toString(){
		return ""+codeValue;
	}
}
