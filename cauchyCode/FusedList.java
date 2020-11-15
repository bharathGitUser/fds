

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;





public class FusedList implements MessageHandler,Serializable{
	public Vector<List<FusedAuxNode>> auxStructure;//:( cannot create array of parameterized objects in java 
	FusedNode[] tos;
	int numOperations =0;
	int numPrimaries, numFaults, backupId;
	private MyContainer dataStack; 
	//int fusedStructureSize = 0; 
	
	long totalUpdateTime=0;//total time taken for all updates received in milliseconds. 
	
	public FusedList(int numPrimaries, int numFaults, int backupId, Vector<List<FusedAuxNode>> auxStructure){
		this.auxStructure = auxStructure;  
		tos = new FusedNode[numPrimaries]; 
		this.numPrimaries = numPrimaries;
		this.numFaults = numFaults; 
		this.backupId = backupId;
		dataStack = new MyContainer(); 
		Fusion.initialize(numPrimaries, numFaults); 
	}
	
	/* dumps the new element at the end of the list for that primary and updates the correct position
	 	in the auxStructure
	*/
	public void add(int index, int value, int primaryId){
		if(Util.debugFlag)System.out.println("-------In Add, i:"+index+" v:"+value+" prim:"+primaryId);
		FusedNode nodeToUpdate;
		
		if(dataStack.isEmpty() ||(tos[primaryId] == dataStack.getLast()) ){
			 FusedNode node = new FusedNode(numPrimaries);
			 dataStack.add(node); 
   			 tos[primaryId] = node;
			 nodeToUpdate = node;
		}else 
		if ((tos[primaryId] == null) && (dataStack.isEmpty() == false)){
			nodeToUpdate = (FusedNode)dataStack.getFirst(); 
			tos[primaryId] = nodeToUpdate; 
		}else{// there is place, just update the node
			nodeToUpdate = (FusedNode)dataStack.getNext(tos[primaryId]);
			tos[primaryId] = nodeToUpdate; 
		}
		
		nodeToUpdate.addElement(value, primaryId,backupId);
		
		//create a pointer to this newly added node and insert into the index structure
		FusedAuxNode auxNode = new FusedAuxNode(nodeToUpdate);
		nodeToUpdate.setAuxNode(primaryId,auxNode); 
		auxStructure.get(primaryId).add(index, auxNode);		
		sanityCheck();		
		
		if(Util.debugFlag){
			System.out.println("After add, data store:"+dataStack);
			System.out.println("Aux Structures:");
			for(int i=0; i < numPrimaries;++i){
				System.out.println("aux["+i+"]:"+auxStructure.get(i));
			}
			System.out.println(auxStructuresInVectorFormat());
		}
	}
		
	public void remove(int index, int elementToDelete, int finalElement, int primaryId){
		if(Util.debugFlag)System.out.println("-------In Remove, i:"+index+" eToD:"+elementToDelete+" eFin:"+finalElement+" prim:"+primaryId);

		FusedAuxNode auxNode = auxStructure.get(primaryId).remove(index);
		FusedNode nodeToUpdate = auxNode.getFusNode(); 
		if(nodeToUpdate != tos[primaryId]){//hole has been created
			nodeToUpdate.removeElement(elementToDelete,primaryId,backupId);
			nodeToUpdate.addElement(finalElement,primaryId,backupId);
			FusedAuxNode finalAux = tos[primaryId].getAuxNode()[primaryId];
			finalAux.setFusNode(nodeToUpdate);
			nodeToUpdate.setAuxNode(primaryId, finalAux);
			tos[primaryId].setAuxNode(primaryId, null); 
		}
		
		tos[primaryId].removeElement(finalElement,primaryId,backupId);
		FusedNode finalNode = (FusedNode)tos[primaryId]; 
		if(finalNode.isEmpty()){//no elements in this node
			dataStack.pop(); 
		}
		tos[primaryId] =  (FusedNode)dataStack.getPrevious(tos[primaryId]); 		
		sanityCheck();		
		if(Util.debugFlag){
			System.out.println("After remove, data stack:"+dataStack);
			System.out.println("Aux Structures:");
			for(int i=0; i < numPrimaries;++i){
				System.out.println(auxStructure.get(i));
			}
			System.out.println(auxStructuresInVectorFormat());
		}
	}

	public int size(){
		return dataStack.size(); 
	}
	
	public MyContainer getDataStack(){
		return dataStack; 
	}
	
	
	public Vector<int[]> dataStackinVectorFormat(){
		Vector<int[]> v = new Vector<int[]>(); 
		FusedNode node =(FusedNode) dataStack.getFirst();
		while(node != null){
			v.add(node.getCodeValue()); 
			node = (FusedNode)dataStack.getNext(node); 
		}
		return v; 	
	}
	
	public Vector<Vector<Integer>> auxStructuresInVectorFormat(){
		Vector<Vector<Integer>> v = new Vector<Vector<Integer>>(); 
		FusedNode node =(FusedNode) dataStack.getFirst();
		while(node != null){
			Vector<Integer> positions = new Vector<Integer>();
			for(int i =0; i < numPrimaries; ++i){
				FusedAuxNode aux = node.getAuxNode()[i];
				if(aux != null)	{
					positions.add(auxStructure.get(i).indexOf(aux));
				}
				else
					positions.add(-1); 
			}
			v.add(positions); 
			node = (FusedNode)dataStack.getNext(node); 
		}
		return v; 	
	}
	
	public void sanityCheck(){
		int max=0;
		for(int i = 0; i < numPrimaries; ++i){
			if(max < auxStructure.get(i).size())
				max = auxStructure.get(i).size();
		}
		try {
			if(Util.debugFlag)System.out.println("sanity: "+dataStack.size()+" "+max);
			if(dataStack.size() != max)
				throw new Exception("Data Stack size is not max of primaries");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0); 
		}
	}
	//handling communication...
	public  synchronized void handleMessage(ObjectOutputStream outStream, ObjectInputStream inStream, String msg){
        StringTokenizer st = new StringTokenizer(msg);
        String tag = st.nextToken();
        //normal add/remove
        if(tag.equals("add")){
        	++numOperations;
        	//if(Util.debugFlag)System.out.println("message rec:"+msg);
        	int index = Integer.parseInt(st.nextToken());
        	int value = Integer.parseInt(st.nextToken());
        	int primaryId = Integer.parseInt(st.nextToken());
        	long startTime = Util.getCurrentTime();
        	add(index, value, primaryId);
        	long endTime = Util.getCurrentTime();
        	totalUpdateTime = totalUpdateTime+ endTime-startTime;
        }else if(tag.equals("remove")){
        	++numOperations;
        	int index = Integer.parseInt(st.nextToken());
        	int elementToDelete = Integer.parseInt(st.nextToken());
        	int finalElement = Integer.parseInt(st.nextToken());
        	int primaryId = Integer.parseInt(st.nextToken());
        	long startTime = Util.getCurrentTime();
        	remove(index, elementToDelete, finalElement, primaryId);
        	long endTime = Util.getCurrentTime();
        	totalUpdateTime = totalUpdateTime+ endTime-startTime;
       }
        
		/* NOTE : the outStream.reset is a hack...but I do not know what to do :(
		 * Whenever objectoutstream has to resend the same object across, it just sends the pointer 
		 * which does not recognise internal changes!! ....this is supposed to save bandwidth..so I reset.
		 */
		try {
			if(msg.equals("reqData")){
			    //this is for recovery..the oracle will ask for the object...u give it back.
				outStream.reset();
				outStream.writeObject(dataStackinVectorFormat());
				outStream.flush();
			}else if(msg.equals("reqIndex")){
				    //this is for recovery..the oracle will ask for the object...u give it back.
					outStream.reset();
					outStream.writeObject(auxStructuresInVectorFormat());
					outStream.flush();

			}else if(msg.equals("time")){
				//write the time taken to file
				outStream.reset();
				outStream.writeObject(totalUpdateTime/numOperations);
				outStream.flush();
			}else if(msg.equals("size")){
				//write the time taken to file
				outStream.reset();
				outStream.writeObject(dataStack.size());
				outStream.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


    public static void main(String[] args) {
    	int id = Integer.parseInt(args[0]);
    	int numPrimaries = Integer.parseInt(args[1]);
    	int numFaults = Integer.parseInt(args[2]);
    	
    	Vector<List<FusedAuxNode>> v = new Vector<List<FusedAuxNode>>(numPrimaries); 
    	for(int i =0; i < numPrimaries; ++i){
    		GenericList<FusedAuxNode> l = new GenericList<FusedAuxNode>();
    		v.add(l);
    	}
    	
    	FusedList backup = new FusedList(numPrimaries, numFaults, id,v); 

    	if(Util.debugFlag)System.out.println("Backup "+ id+" started:");
        try {
            ServerSocket listener = new ServerSocket(Symbols.BackupPort+id);
            while (true) {
                Socket client = listener.accept();//word client is used for anyone who sends a message to backup
                ListenerThread l = new ListenerThread(backup,client);
                l.start();
            }
        } catch (IOException e) {
            System.err.println("Server aborted:" + e);
        }
	}
}

