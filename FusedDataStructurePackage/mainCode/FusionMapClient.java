







import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
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

public class FusionMapClient {

	int numPrimaries;
	int numFaults;
	Fusion fusionObject;
	PrintStream[] pout;
	ObjectOutputStream[] objOut;
	ObjectInputStream[] objIn; 
	HashMap<Integer,Integer>[] testMaps; 

    public FusionMapClient(int numPrimaries, int numFaults){
		this.numPrimaries = numPrimaries;
		this.numFaults = numFaults;
		Fusion.initialize(numPrimaries, numFaults);
		testMaps = new HashMap[numPrimaries]; 
		for(int i =0; i < numPrimaries; ++i){
			testMaps[i] = new HashMap<Integer,Integer>(); 
		}
		pout = new PrintStream[numPrimaries+numFaults];
		objOut = new ObjectOutputStream[numPrimaries+numFaults];
		objIn = new ObjectInputStream[numPrimaries+numFaults];
		try {
			//handle to all the original data structures
			for(int i =0; i < numPrimaries ; ++i){
			    Socket s = new Socket(Symbols.nameServer, 
			            Symbols.DataPort+i);
				pout[i] = new PrintStream(s.getOutputStream());
				objOut[i] = new ObjectOutputStream(s.getOutputStream());
				objIn[i] = new ObjectInputStream(s.getInputStream());

				if(Util.debugFlag)System.out.println("Connected to Data Server "+ i);
			}
			
			//handle to all the backup structures
			for(int i =0 ; i < numFaults; ++i){
			    Socket s = new Socket(Symbols.nameServer, 
			            Symbols.BackupPort+i);
				pout[i+numPrimaries] = new PrintStream(s.getOutputStream());
				objOut[i+numPrimaries] = new ObjectOutputStream(s.getOutputStream());
				objIn[i+numPrimaries] = new ObjectInputStream(s.getInputStream());

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
    	long recoveryTime =0; 
		int[] erasuresArray = new int[numFaults+1];
		for(int i =0; i < erasures.size();++i){
			erasuresArray[i] = erasures.get(i);
		}

		Vector< HashMap<Integer,Integer> > primaries = getPrimaries(erasures);
		Vector< Vector<Integer> > backups =  getBackups(erasures);
    	Vector<Vector<Integer>> indexInfo = getIndexInfo();
    			
		long startTime = Util.getCurrentTime();
		int[] code	 = new int[numFaults];
		int[] data = new int[numPrimaries];
		
		for(int fusedNodeNumber =0; fusedNodeNumber < indexInfo.size();++fusedNodeNumber){
			for(int backupId =0; backupId < numFaults; ++backupId){
				code[backupId]=backups.get(backupId).get(fusedNodeNumber); 
			}
			for(int k =0; k < numPrimaries; ++k){
				int keyOfPrimary = indexInfo.get(fusedNodeNumber).get(k);  
				if(!erasures.contains(k) && (keyOfPrimary != -1)){
					HashMap<Integer,Integer> original =  primaries.get(k);					
					data[k] = original.get(keyOfPrimary);
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
			
			for(int k =0; k < numPrimaries; ++k){
				int keyOfPrimary = indexInfo.get(fusedNodeNumber).get(k);
				if(erasures.contains(k) && (keyOfPrimary != -1)){
					HashMap<Integer,Integer> original =  primaries.get(k);
					original.put(keyOfPrimary,data[k]);
				}
			}
			
		}
		
		
		long endTime = Util.getCurrentTime();
		recoveryTime = recoveryTime+endTime-startTime; 

		if(Util.debugFlag){
			System.out.print("Recovered Originals:");
			System.out.println(primaries);
		}
		for(int i = 0; i < numPrimaries ; ++i){
			HashMap<Integer,Integer> recovered =  primaries.get(i);
			HashMap<Integer,Integer> actual = testMaps[i];
			try {
				if(!actual.equals(recovered))
					throw new Exception("Error in Recovery");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		
		return (recoveryTime);
		

    }

    
    public Vector<HashMap<Integer,Integer>> getPrimaries(Vector<Integer> erasures){
    	Vector<HashMap<Integer,Integer>> primaries = new Vector<HashMap<Integer,Integer>>(); 
    	for(int i =0; i < numPrimaries ;++i){

    		if(!erasures.contains(i)){
    			try {
        			pout[i].println("req");
        			pout[i].flush();
        			HashMap<Integer,Integer> data = (HashMap<Integer,Integer>)objIn[i].readObject();
					primaries.add(data);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    		else{//for all the faulty ones...just fill in dummy data
    			HashMap<Integer,Integer> dummy = new HashMap<Integer,Integer>();
    			primaries.add(dummy);
    		}
    	}
    	return primaries; 
    }

    public Vector<Vector<Integer>> getBackups(Vector<Integer> erasures){
    	Vector<Vector<Integer>> backups = new Vector<Vector<Integer>>(); 
    	
    	for(int i = numPrimaries; i < numPrimaries+numFaults ;++i){
    		if(!erasures.contains(i)){
    			try {
        			pout[i].println("reqData");
        			pout[i].flush();
    				Vector<Integer> data = (Vector<Integer>)objIn[i].readObject();
					backups.add(data);
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
   				backups.add(dummy);
    		}
    	}
    	return backups; 
    }

    public Vector<Vector<Integer>> getIndexInfo(){
    	Vector<Vector<Integer>> indexInfo = new Vector<Vector<Integer>>(); 
		try {
			pout[numPrimaries ].println("reqIndex");
			pout[numPrimaries].flush();
			indexInfo = (Vector<Vector<Integer>>)objIn[numPrimaries].readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return indexInfo;    	
    }

    //performs updates, sends a request to all the backups to give it back the time taken for it's set of updates
    //then calculates the max of it all to give the time for updates
    public long calcUpdateTime(int numberOfOperations){
     	complexMapTests(numberOfOperations);
    	
    	int backupId = numPrimaries;
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

    
    //kills the first numFaults number of servers and finds the time taken for the operation
    public long calcrecoveryTime(){
    	int actualErasures = Math.min(numPrimaries, numFaults);
    	Vector<Integer> erasures = new Vector<Integer>(actualErasures+1);
    	//for the purpose of testing just always kill the first numFaults number of data servers
    	for(int i =0; i < actualErasures; ++i){
    		erasures.add(i);
    	}
    	//just to put the -1 in
    	erasures.add(-1);
		long recoveryTime = recover(erasures);
		return recoveryTime;
    }
    
    //sends a request to all the backups to give it back the time taken for it's set of updates
    //then calculates the max of it all to give the time for updates
    public int calcSize(){
    	int totalSizePrimaries =0;
    	int totalSizeBackups=0;
    	try{
    		//the size of each primary will be different..hence we need to get size from all
	    	for(int i =0; i < numPrimaries ;++i){
		   			pout[i].println("size");
	    			pout[i].flush();
	
					int size  = (Integer)objIn[i].readObject();
					totalSizePrimaries = totalSizePrimaries + size;
	    	}
	    	
	    	//Each backup has the same size...so just get from one and multiply
	 		pout[numPrimaries].println("size");
	    	pout[numPrimaries].flush();
			int  sizeOfOneBackup  = (Integer)objIn[numPrimaries].readObject();
			
			totalSizeBackups = numFaults*sizeOfOneBackup;

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

    	FusionMapClient orc = new FusionMapClient(numPrimaries,numFaults);
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
    	int size = orc.calcSize();
    	System.out.println(numPrimaries+","+numFaults+","+numberOfUpdates+","+size+",$"+updateTime+"$,"+recoveryTime+","+Util.encodingTime+","+Util.decodingTime);
    }

    void mapPut(int listNum, int key, int element){
    	assert (key >= 0);
    	assert (element >0); 
    	testMaps[listNum].put(key, element); 
		String msgForOriginals = "put"+" "+key+" "+element;
		//System.out.println(msgForOriginals+" "+listNum);
		pout[listNum].println(msgForOriginals);
		pout[listNum].flush();
	}
	
	void mapRemove(int listNum, int key){
		assert (key >= 0); 
    	testMaps[listNum].remove(key);
		String msgForOriginals = "remove"+" "+key;
		pout[listNum].println(msgForOriginals);
		pout[listNum].flush();
	}
	
    
    //complex final linked list tests
    public void complexMapTests(int numberOfUpdates){
		int mapId = 0;
		int noOfadds =0;
		int noOfdeletes=0;
		Vector[] inserts = new Vector[numPrimaries]; 
		for(int i =0; i < numPrimaries; ++i)
			inserts[i] = new Vector<Integer>(); 
		
		Random random = new Random(5);
		for (int i = 0; i < numberOfUpdates; i += 1) {
			// q = random.nextInt(numPrimaries);
			mapId = (mapId + 1) % numPrimaries;
			double operation = random.nextDouble();
			int key;
			if (operation < Util.threshold) {
				if (inserts[mapId].size() == 0) {//no inserts yet
					i = i - 1;
					continue;
				}
				int index = random.nextInt(inserts[mapId].size());
				key = (Integer)inserts[mapId].get(index);
				inserts[mapId].remove(index);//this is no longer going to be there. 
				mapRemove(mapId, key);
				
			} else {
				key = i;
				inserts[mapId].add(key);  
				mapPut(mapId, key, i+1);
			}
		}

    }
    
    
    public void simpleMapTests(){
    	//linked list tests
   	
		mapPut(0, 0, 1);
		mapPut(0,1,4);
		mapRemove(0,0);
		mapPut(0,0,5);
		mapPut(0,3,6);
		mapPut(0,5,7);
		mapRemove(0,3);
		mapPut(0,3,11);
		mapPut(0,7,12);
		mapPut(0,1,13);
		mapPut(0,2,14);

		mapPut(1, 0, 2);
		mapPut(1,0,8);
		mapPut(1,1,9);
		mapRemove(1,0);
		mapPut(1,0,24);
		mapPut(1,1,25);
		mapPut(1,2,26);
		mapRemove(1,1);
		
		mapPut(2, 0, 3);
    }
}
