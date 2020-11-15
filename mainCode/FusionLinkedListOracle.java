





import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;







/*
 * Class which is responsible for testing...
 * it updates, calculates the time taken for that update, nodes added, and time taken to recover
 * whenever recovery needs to be performed an instance of this class shud be created
 * It will talk to the original and the backups, get the existing data and recover the faulty ones
 * as of now I do not allow faults in the backup but that is not a big deal..it can easily be extended
 */

public class FusionLinkedListOracle {

	int numStructures;
	int maxFaults;
	PrintStream[] pout;
	ObjectOutputStream[] objOut;
	ObjectInputStream[] objIn;

    public FusionLinkedListOracle(int numStructures, int maxFaults){
		this.numStructures = numStructures;
		this.maxFaults = maxFaults;
		Fusion.initialize(numStructures, maxFaults);
		pout = new PrintStream[numStructures + maxFaults];
		objOut = new ObjectOutputStream[numStructures + maxFaults];
		objIn = new ObjectInputStream[numStructures + maxFaults];
		try {
			//handle to all the original data structures
			for(int i =0; i < numStructures ; ++i){
			    Socket s = new Socket(Symbols.nameServer, 
			            Symbols.DataPort+i);
				pout[i] = new PrintStream(s.getOutputStream());
				objOut[i] = new ObjectOutputStream(s.getOutputStream());
				objIn[i] = new ObjectInputStream(s.getInputStream());

				if(Util.debugFlag)System.out.println("Connected to Data Server "+ i);
			}
			
			//handle to all the backup structures
			for(int i =0 ; i < maxFaults; ++i){
			    Socket s = new Socket(Symbols.nameServer, 
			            Symbols.BackupPort+i);
				pout[i+numStructures] = new PrintStream(s.getOutputStream());
				objOut[i+numStructures] = new ObjectOutputStream(s.getOutputStream());
				objIn[i+numStructures] = new ObjectInputStream(s.getInputStream());

				if(Util.debugFlag)System.out.println("Connected to Backup Server "+ i);
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
    
    
    public long recover(Vector<Integer> erasures){
    	long recTime =0;
    	Vector< Vector<Integer> > originalStructures = new Vector<Vector<Integer>>(numStructures);
		Vector<OldFusedLinkedList> backupStructures = new Vector<OldFusedLinkedList>(maxFaults);
		getNonErasedStructures(erasures,originalStructures,backupStructures);
		

		int[] erasuresArray = new int[maxFaults+1];
		for(int i =0; i < erasures.size();++i){
			erasuresArray[i] = erasures.get(i);
		}
		
		long startTime = Util.getCurrentTime();
		//need to know the structure of the backups....they are all the same
		OldFusedLinkedList guide = backupStructures.get(0);
		
		//not every original data node has a representation in the backup..use pointers to keep track of them
		int[] pointers = new int[numStructures];
		LinkedList<LinkedListNode> guideList = guide.internalList; 
		//System.out.println("GuideList size:"+ guideList.size());
		for(int i =0; i < guideList.size();++i){
			LinkedListNode node = guideList.get(i);
			int[] code	 = new int[maxFaults];
			for(int j =0; j < maxFaults; ++j){
				OldFusedLinkedList backup = backupStructures.get(j); 
				//System.out.println("Backup size : "+backup.queue.size());
				try{
					code[j] = backup.internalList.get(i).getValue();
				}catch(Exception e)
				{
					System.out.println("Error");	
				}
				
			}
			int[] data = new int[numStructures];
		 	
			for(int k =0; k < numStructures; ++k){
				if(!erasures.contains(k) && node.process[k] ){
					Vector<Integer> original =  originalStructures.get(k);
					data[k] = original.get(pointers[k]);
					pointers[k]++;
				}
				else  
					data[k] = 0;
			}
			
			
			try {

							data = Fusion.getRecoveredData(code,data,erasuresArray);
			} catch (InterfaceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for(int k =0; k < numStructures; ++k){
				if(erasures.contains(k) && node.process[k]){
					Vector<Integer> original  =  originalStructures.get(k);
					original.add(data[k]);
				}
			}
		}
		
		long endTime = Util.getCurrentTime();
		recTime = recTime + (endTime - startTime); 
		if(Util.debugFlag)System.out.print("Recovered Originals:");
		if(Util.debugFlag)System.out.println(originalStructures);

		if(Util.debugFlag)System.out.print("Recovered Backups");
		if(Util.debugFlag)System.out.println(backupStructures);

/*
 * 		
		//Sent the recovered data back to the structures.
		for(int i =0; i < numStructures; ++i){
			if(erasures.contains(i)){
				try {
					Vector<Integer> recovered  =  originalStructures.get(i);
					pout[i].println("recover");
					
					objOut[i].writeObject(recovered);
					objOut[i].flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
*/
		return (recTime);
    }
    public void getNonErasedStructures(Vector<Integer> erasures,Vector<Vector<Integer>>originalStructures,Vector<OldFusedLinkedList> backupStructures){
    	/*get the data structures sequentially from both the original and the backups
    	toDo : is it useful to get them in parallel...I think not...after all we do need to
    	get all the data elements before proceeding for recovery 
    	*/
    	for(int i =0; i < numStructures ;++i){

    		if(!erasures.contains(i)){
    			try {
        			pout[i].println("req");
        			

        			pout[i].flush();
    				Vector<Integer> data = (Vector<Integer>)objIn[i].readObject();
					originalStructures.add(data);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    		else{//for all the faulty ones...just fill in dummy data
    			Vector<Integer> dummy = new Vector<Integer>();
    			originalStructures.add(dummy);

    		}
    		
    	}
    	

    	for(int i = numStructures; i < numStructures+maxFaults ;++i){

    		if(!erasures.contains(i)){
    			try {
        			pout[i].println("req");
        			pout[i].flush();

        			OldFusedLinkedList backup = (OldFusedLinkedList)objIn[i].readObject();


					backupStructures.add(backup);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}else{//for all the faulty ones...just fill in dummy data
    			OldFusedLinkedList dummy = new OldFusedLinkedList(numStructures,maxFaults,i-numStructures);
    			backupStructures.add(dummy);

    		}

    	}
    	
    	

    }

    public void getAllStructures(Vector<Vector<Integer>>originalStructures,Vector<OldFusedLinkedList> backupStructures){
    	/*get the data structures sequentially from both the original and the backups
    	toDo : is it useful to get them in parallel...I think not...after all we do need to
    	get all the data elements before proceeding for recovery 
    	*/
    	for(int i =0; i < numStructures ;++i){
			try {
    			pout[i].println("req");
    			pout[i].flush();
				Vector<Integer> data = (Vector<Integer>)objIn[i].readObject();
				originalStructures.add(data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	

    	for(int i = numStructures; i < numStructures+maxFaults ;++i){
			try {
    			pout[i].println("req");
    			pout[i].flush();
    			OldFusedLinkedList backup = (OldFusedLinkedList)objIn[i].readObject();
				backupStructures.add(backup);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }

    public void sanityCheck(){
    	Vector< Vector<Integer> > originalStructures = new Vector<Vector<Integer>>(numStructures);
		Vector<OldFusedLinkedList> backupStructures = new Vector<OldFusedLinkedList>(maxFaults);
		getAllStructures(originalStructures,backupStructures);
/*		System.out.println("In sanity check:");
		for(int i =0; i < originalStructures.size();++i){
			System.out.println("List "+ i +" :"+originalStructures.get(i)+"\n");
		}
		
		for(int i =0; i < backupStructures.size();++i){
			System.out.println("Backup "+ i +" :"+backupStructures.get(i)+"\n");
		}
*/
		long startTime = Util.getCurrentTime();
		//every original data node does not have a representation in the backup..use pointers to keep track of them
		int[] pointers = new int[numStructures];
		int[] data = new int[numStructures];
		int[] generatedCode	 = new int[maxFaults];
		int[] code	 = new int[maxFaults];
		OldFusedLinkedList guide = backupStructures.get(0); 
		LinkedList<LinkedListNode> guideList =guide.internalList;
//		System.out.println("GuideList size:"+ guideList.size());
		for(int backupNodeNum =0; backupNodeNum < guideList.size();++backupNodeNum){
			LinkedListNode node = guideList.get(backupNodeNum);
			for(int listNum =0; listNum < numStructures; ++listNum){
				if(node.process[listNum] ){
					Vector<Integer> original =  originalStructures.get(listNum);
					data[listNum] = original.get(pointers[listNum]);
					pointers[listNum]++;
				}
				else  
					data[listNum] = 0;
			}

			try {
				generatedCode = Fusion.encodeData(code,data);
			} catch (InterfaceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(int j =0; j < maxFaults; ++j){
				OldFusedLinkedList backupList = backupStructures.get(j); 
					code[j] = backupList.internalList.get(backupNodeNum).getValue();
					if(generatedCode[j]!=code[j]){
						System.out.println("Sanity check failed at node:"+backupNodeNum+ " GenCode:"+generatedCode[j]+" existing code:"+code[j]);
						System.exit(-1);
					}
				
			}
		}
		long endTime = Util.getCurrentTime();
		System.out.print((endTime - startTime)+",");		
    }

    //performs updates, sends a request to all the backups to give it back the time taken for it's set of updates
    //then calculates the max of it all to give the time for updates
    public long calcUpdateTime(int numberOfUpdates){
     	complexLinkedlistTests(numberOfUpdates);
//    	simpleLinkedlistTests();
    	//bubbleUpdateTest();
	
    	int backupId = numStructures;
    	long updateTime = 0;
    	
		try {
			pout[backupId].println("time");
			pout[backupId].flush();
			updateTime = (Long)objIn[backupId].readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return updateTime; 
    }

    
    //kills the first maxfaults number of servers and finds the time taken for the operation
    public long calcrecoveryTime(){
    	Vector<Integer> erasures = new Vector<Integer>(maxFaults+1);
    	//for the purpose of testing just always kill the first maxFaults number of data servers
    	for(int i =0; i < maxFaults; ++i){
    		erasures.add(i);
    	}
    	//just to put the -1 in
    	erasures.add(-1);
		long recoveryTime = recover(erasures);
		return recoveryTime;
    }
    
    //sends a request to all the backups to give it back the time taken for it's set of updates
    //then calculates the max of it all to give the time for updates
    public double calcSize(){
    	int totalSizePrimaries =0;
    	double totalSizeBackups=0;
    	try{
    		//the size of each primary will be different..hence we need to get size from all
	    	for(int i =0; i < numStructures ;++i){
		   			pout[i].println("size");
	    			pout[i].flush();
	
					int size  = (Integer)objIn[i].readObject();
					totalSizePrimaries = totalSizePrimaries + size;
	    	}
	    	
	    	//Each backup has the same size...so just get from one and multiply
	 		pout[numStructures].println("size");
	    	pout[numStructures].flush();
			double  sizeOfOneBackup  = (Double)objIn[numStructures].readObject();
			
			totalSizeBackups = maxFaults*sizeOfOneBackup;
//	    	System.out.println("Total Primary Size :"+totalSizePrimaries);
			//System.out.println(totalSizeBackups);
    	} catch (IOException e) {
		// TODO Auto-generated catch block
    		e.printStackTrace();
    	} catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	return totalSizeBackups;
    }
    public static void main(String[] args) {
    	int numPrimaries = Integer.parseInt(args[0]);
    	int numFaults = Integer.parseInt(args[1]);
    	int numberOfUpdates =Integer.parseInt(args[2]);

    	FusionLinkedListOracle orc = new FusionLinkedListOracle(numPrimaries,numFaults);
    	//performs updates and based on that calculates update time

    	long updateTime= orc.calcUpdateTime(numberOfUpdates);
    	//recovers the state of the system w.r.t to the updates peformed above
    	try {
			Thread.sleep(60000L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		long recoveryTime = orc.calcrecoveryTime();
    	//compares the total size of the primaries vis a vis the backups
    	double size = orc.calcSize();
    	
    	System.out.println(numPrimaries+","+numFaults+","+numberOfUpdates+","+size+",$"+updateTime+"$,"+recoveryTime+","+Util.encodingTime+","+Util.decodingTime);
    }

    void listAdd(int listNum, int location, int value){
		String msgForOriginals = "add"+" "+location+" "+value;
		pout[listNum].println(msgForOriginals);
		pout[listNum].flush();
		
		String msgForBackups = "add"+ " " + value + " " + listNum + " " + location;
		for(int i =numStructures; i < maxFaults+numStructures; ++i){
			pout[i].println(msgForBackups);
			pout[i].flush();
		}

	}
	
	void listRemove(int listNum, int location){
		String msgForOriginals = "remove"+" "+location;
		pout[listNum].println(msgForOriginals);
		pout[listNum].flush();
		
		int oldValue =0;
		try {
			oldValue = (Integer)objIn[listNum].readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String msgForBackups = "remove"+ " " + oldValue + " " + listNum + " " + location;
		for(int i =numStructures; i < maxFaults+numStructures; ++i){
			pout[i].println(msgForBackups);
			pout[i].flush();
		}

	}
	
    
    //complex final linked list tests
    public void complexLinkedlistTests(int numberOfUpdates){
		int listId = 0;
		int noOfadds =0;
		int noOfdeletes=0;

		int[] inserts = new int[numStructures];
		int[] deletes = new int[numStructures];
		
		Random random = new Random(5);
		Vector<Integer> rand = new Vector<Integer>();
		for (int i = 0; i < numberOfUpdates; i += 1) {
			// q = random.nextInt(numStructures);
			listId = (listId + 1) % numStructures;
			int size = inserts[listId] - deletes[listId];
			double operation = random.nextDouble();
			int location;
			if (operation < Util.threshold) {
				if (size == 0) {
					i = i - 1;
					continue;
				}
				location = random.nextInt(size);
				listRemove(listId, location);
				++deletes[listId];
				noOfdeletes++;
			} else {
				location = random.nextInt(size + 1);
				listAdd(listId, location, i);
				++inserts[listId];
				noOfadds++;
			}
		}
    }
    
/*    
    public void simpleLinkedlistTests(){
    	//linked list tests
   	 //Simple add remove test
    	
		listAdd(0, 0, 1);
		listAdd(1, 0, 2);
		listAdd(2, 0, 4);
		listRemove(1,0);

//central add and merge test
    	
		listAdd(0,0,2);
		listAdd(0,1,1);
		listAdd(0,2,3);

		listAdd(1,0,2);
		listAdd(1,1,4);
		listRemove(0,1);
		
    	
    	
    }
    
    public void bubbleUpdateTest(){
		listAdd(0,0,11);
		listAdd(0,1,12);
		listAdd(0,2,13);

		listAdd(1,0,24);
		listAdd(1,1,25);
		listAdd(1,2,26);

		listAdd(2,0,37);
		listAdd(2,1,38);
		listAdd(2,2,39);
		
		listRemove(2,1);listRemove(2,1);
		
		listAdd(0,1,14);
		
		listRemove(1,1);
    }
*/
}
