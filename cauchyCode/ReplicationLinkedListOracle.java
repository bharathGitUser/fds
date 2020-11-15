


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
 * Class which is responsible for recovery...whenever recovery needs to be performed an instance of this class shud be created
 * It will talk to the original and the backups, get the existing data and recover the faulty ones
 * as of now I do not allow faults in the backup but that is not a big deal..it can easily be extended
 */

public class ReplicationLinkedListOracle {

	int numStructures;
	int maxFaults;
	PrintStream[] pout;
	ObjectOutputStream[] objOut;
	ObjectInputStream[] objIn;

    
    public ReplicationLinkedListOracle(int numStructures, int maxFaults){
		this.numStructures = numStructures;
		this.maxFaults = maxFaults;
		pout = new PrintStream[numStructures + maxFaults*numStructures];
		objOut = new ObjectOutputStream[numStructures + maxFaults*numStructures];
		objIn = new ObjectInputStream[numStructures + maxFaults*numStructures];
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
			for(int i =0 ; i < maxFaults*numStructures; ++i){
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
    	Vector< Vector<Integer> > originalStructures = new Vector<Vector<Integer>>(numStructures);
    	long totalRecoveryTime = 0;
    	for(int i =0; i < erasures.size()-1;++i){
    		int killedPrimary = erasures.get(i);
    		
    		//just pick the first backup corresponding to it...need to extend this to allow backup failure
    		int backupId = maxFaults*killedPrimary + numStructures;
    		if(Util.debugFlag)System.out.println("backup id"+backupId);
			pout[backupId].println("req");
			pout[backupId].flush();

			Vector<Integer> data;
			try {
				data = (Vector<Integer>)objIn[backupId].readObject();
				
				long startTime = Util.getCurrentTime();
				originalStructures.add(data);
				long endTime = Util.getCurrentTime();
				totalRecoveryTime = totalRecoveryTime + (endTime - startTime);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

    	}
    	
		if(Util.debugFlag)System.out.print("Recovered Data:");
		if(Util.debugFlag)System.out.println(originalStructures);
		
		return totalRecoveryTime;
    }
/*    
    public void sanityCheck(){
    	//obtain original structures. 
    	Vector< Vector<Integer> > originalStructures = new Vector<Vector<Integer>>(numStructures);
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
    	
    	if(Util.debugFlag)System.out.println("Originals:");
		if(Util.debugFlag)System.out.println(originalStructures);
		
    	//obtain backups
    	Vector< Vector<Integer> > backupStructures = new Vector<Vector<Integer>>(numStructures*maxFaults);
    	for(int backupId =numStructures; backupId < numStructures+(numStructures*maxFaults) ;++backupId){
			try {
        	    
    			pout[backupId].println("req");
    			pout[backupId].flush();

				Vector<Integer> backup = (Vector<Integer>)objIn[backupId].readObject();
				backupStructures.add(backup);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}

    	if(Util.debugFlag)System.out.println("Backups:");
		if(Util.debugFlag)System.out.println(backupStructures);
    	
    	//perform sanity check
    	long startTime=Util.getCurrentTime();
    	for(int origQueueId =0; origQueueId < numStructures;++origQueueId){
    		Vector<Integer> queue = originalStructures.get(origQueueId);
    		for(int backupId = origQueueId*maxFaults; backupId < maxFaults+(origQueueId*maxFaults) ; ++backupId){
    				Vector<Integer> backup = backupStructures.get(backupId);
    				for(int i =0; i < queue.size();++i){
    					if(queue.get(i).intValue()!= backup.get(i).intValue()){
    						System.out.println("Sanity check failed");
    						System.exit(-1);
    					}
    				}
    		}
    	}
		long endTime = Util.getCurrentTime();
		System.out.print((endTime - startTime)+",");
    }
*/
    //performs updates, sends a request to all the backups to give it back the time taken for it's set of updates
    //then calculates the max of it all to give the time for updates
    public long calcUpdateTime(int numberOfUpdates){
    	complexLinkedlistTests(numberOfUpdates);
    	//simpleLinkedlistTests();

    	long updateTime =0;
    	for(int i = numStructures; i < numStructures + numStructures*maxFaults ;i = i + maxFaults){
			try {
    			pout[i].println("time");
    			pout[i].flush();
    			long time = (Long)objIn[i].readObject();
    			if(time > updateTime)
    				updateTime = time;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	return updateTime;
    }

    
    //kills the first maxfaults number of servers and finds the time taken for the operation
    public long calcrecoveryTime(){
    	//for the purpose of testing just always kill the first maxFaults number of data servers
    	int actualErasures = Math.min(numStructures, maxFaults);
    	Vector<Integer> erasures = new Vector<Integer>(actualErasures+1);    	
    	for(int i =0; i <actualErasures; ++i){
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
	    	for(int i =0; i < numStructures ;++i){
		   			pout[i].println("size");
	    			pout[i].flush();
	
					int size  = (Integer)objIn[i].readObject();
					totalSizePrimaries = totalSizePrimaries + size;

	    	}
	    	totalSizeBackups = totalSizePrimaries * maxFaults;
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

    	ReplicationLinkedListOracle orc = new ReplicationLinkedListOracle(numPrimaries,numFaults);
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
    	
    	System.out.println(numPrimaries+","+numFaults+","+numberOfUpdates+","+size+","+updateTime+","+recoveryTime);
    }
	void listAdd(int listNum, int location, int value){
		String msg = "add"+" "+location+" "+value;
		pout[listNum].println(msg);
		pout[listNum].flush();
		
		for(int i= numStructures+listNum*maxFaults; i < numStructures+ listNum*maxFaults + maxFaults; ++i){
			pout[i].println(msg);
			pout[i].flush();
		}

	}
	
	void listRemove(int listNum, int location){
		String msg = "remove"+" "+location;
		pout[listNum].println(msg);
		pout[listNum].flush();
	
		//useless code....just there to recieve the message. 
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

		for(int i= numStructures+listNum*maxFaults; i < numStructures+ listNum*maxFaults + maxFaults; ++i){
			pout[i].println(msg);
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

  //Complex test  	
		listAdd(0,0,1);
		listAdd(0,1,2);
		listAdd(0,2,3);

		listAdd(1,0,2);
		listAdd(1,1,4);
		listRemove(0,1);
		listRemove(0,1);
		
		listAdd(2,0,3);
		listAdd(2,1,5);
		listAdd(2,2,2);

//central add and merge test
    	
		listAdd(0,0,2);
		listAdd(0,1,1);
		listAdd(0,2,3);

		listAdd(1,0,2);
		listAdd(1,1,4);
		listRemove(0,1);
		
    	
    	
    }
*/

}
