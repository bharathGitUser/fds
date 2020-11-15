


public class FusedNode extends Node{
	private int[] codeValue;
	private int refCount; 
	private FusedAuxNode[] auxNode;
	public FusedNode(int numPrimaries){
		auxNode = new FusedAuxNode[numPrimaries];
		codeValue = new int[Fusion.getW()]; 
	}

	public int[] getCodeValue() {
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
			int[] oldCode = new int [Fusion.getW()];
			int[] oldValue = new int[Fusion.getW()];
			int[] newValue = new int[Fusion.getW()];
			for(int i=0; i < Fusion.getW();++i){
				oldCode[i]=codeValue[i];
				oldValue[i]=0; 
				newValue[i]=0;
			}
			newValue[0]=newElement; 	
			codeValue = Fusion.getUpdatedCode(oldCode,backupId,oldValue,newValue,primaryId);
			++refCount; 
		} catch (InterfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void removeElement(int oldElement, int primaryId, int backupId){
		try {
			int[] oldCode = new int [Fusion.getW()];
			int[] oldValue = new int[Fusion.getW()];
			int[] newValue = new int[Fusion.getW()];
			for(int i=0; i < Fusion.getW();++i){
				oldCode[i]=codeValue[i];
				oldValue[i]=0; 
				newValue[i]=0;
			}
			oldValue[0]=oldElement; 	
			codeValue = Fusion.getUpdatedCode(oldCode,backupId,oldValue,newValue,primaryId);
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
		String out ="( ";
		for(int i =0; i < Fusion.getW(); ++i){
			out = out + codeValue[i]+ " ";
		}
		out = out +")";
		return out;
	}
}
