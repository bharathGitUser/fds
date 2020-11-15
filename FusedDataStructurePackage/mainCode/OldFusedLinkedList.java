






import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.Vector;




public class OldFusedLinkedList implements MessageHandler,Serializable{

	int numQueues;
	int maxFaults;
	int id;
	int numOperations=0;


	boolean flagEnableMergeAdjacentCells = true;

	boolean flagInsertInMiddleOfBubbleCluster =true;

	LinkedList<LinkedListNode> internalList;

	int[] qLengths;

	// just for monitoring, not req by algo

	boolean debug = false;

	public int maxExpectedQueueSize = 0;

	public int maxActualQueueSize = 0;

	int numNodesDeleted = 0;

	int numTimesBubblesMerged = 0;

	int maxConcurrentQueues = 0;

	int numNodesMerged = 0;

	double bestSavingsRatio = 0;

	double worstSavingsRatio = Double.MAX_VALUE;

	long totalUpdateTime=0;//total time taken for all updates recieved in msec

	public OldFusedLinkedList(int numQueues,int maxFaults,int id) {
		this.numQueues = numQueues;
		this.maxFaults = maxFaults;
		this.id = id;
		Fusion.initialize(numQueues,maxFaults);
		internalList = new LinkedList<LinkedListNode>();
		qLengths = new int[numQueues];
		debug = Util.debugFlag;
	}

	//recives the message from the original servers and acts accordingly
	public  synchronized void handleMessage(ObjectOutputStream outStream, ObjectInputStream inStream, String msg){
        StringTokenizer st = new StringTokenizer(msg);
        String tag = st.nextToken();
        //normal add/remove
        if(tag.equals("add")){
        	++numOperations;
        	int newValue = Integer.parseInt(st.nextToken());
        	int processId = Integer.parseInt(st.nextToken());
        	int location = Integer.parseInt(st.nextToken());
        	long startTime = Util.getCurrentTime();
        	add(newValue, processId, location);
        	long endTime = Util.getCurrentTime();
        	totalUpdateTime = totalUpdateTime+ endTime-startTime;
        }else if(tag.equals("remove")){
        	++numOperations;
        	int oldValue = Integer.parseInt(st.nextToken());
        	int processId = Integer.parseInt(st.nextToken());
        	int location = Integer.parseInt(st.nextToken());
        	long startTime = Util.getCurrentTime();
        	remove(oldValue, processId, location);
        	long endTime = Util.getCurrentTime();
        	totalUpdateTime = totalUpdateTime+ endTime-startTime;
       }
        
		/* NOTE : the outStream.reset is a hack...but I do not know what to do :(
		 * Whenever objectoutstream has to resend the same object across, it just sends the pointer 
		 * which does not recognise internal changes!! ....this is supposed to save bandwidth..so I reset.
		 */
		try {
			if(msg.equals("req")){
			    //this is for recovery..the oracle will ask for the object...u give it back.
				outStream.reset();
				outStream.writeObject(this);
				outStream.flush();
			}else if(msg.equals("time")){
				//write the time taken to file
				outStream.reset();
				outStream.writeObject(totalUpdateTime/numOperations);
				outStream.flush();
			}else if(msg.equals("size")){
				//write the time taken to file
				outStream.reset();
				outStream.writeObject(sizeOfObject());
				outStream.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
        if(debug)System.out.println(this);
	}

	public synchronized int get(int index){
		return internalList.get(index).getValue();
	}
	
	public synchronized void add(int newValue, int processId, int location) {
		if(debug)System.out.println("In add , newValue:"+ newValue + " processId: " + processId + "location:" + location);
		if (debug) {
			System.out.println(internalList);
			System.out.println();
			System.out.println("Insert location " + location
					+ " | list number " + processId + " | queue size "
					+ internalList.size());
		}

		if (location > qLengths[processId]) {
			throw new Error("Add: index " + location
					+ " exceeds size of the list " + qLengths[processId]);
		}
		if (internalList.isEmpty()) {
			LinkedListNode newNode = new LinkedListNode(processId,
					numQueues);
			newNode.setValue(newValue, processId, id);
			internalList.add(newNode);
		} else {
			ListIterator<LinkedListNode> iterator = internalList.listIterator();
			int count = 0;
			LinkedListNode node = null, temp = null;
			// Find the location in the backup queue
			while (count < location) {
				if (!iterator.hasNext())
					if(debug)System.out
							.println(" error in add :  no next element , count = "
									+ count + " location= " + location);

				node = iterator.next();
				if (node.process[processId])
					count += 1;
			}
			if (iterator.hasNext()) {
				temp = iterator.next();
			}
			// Either there is no next node or the next node is not a bubble
			if (location == 0 && temp.process[processId] == true) {
				LinkedListNode newNode = new LinkedListNode(processId, numQueues);
				newNode.setValue(newValue, processId, id);
				internalList.addFirst(newNode);
				temp = null;
			} else if (temp == null || temp.process[processId] == true) {
				LinkedListNode newNode = new LinkedListNode(processId, numQueues);
				newNode.setValue(newValue, processId, id);
				if (temp != null) {
					iterator.previous();
				}
				iterator.add(newNode);
			} else {

				if (flagInsertInMiddleOfBubbleCluster) {
					// find the position to insert the new node
					int numBubbles = 0;
					while (temp.process[processId] == false) {
						numBubbles += 1;
						if (!iterator.hasNext())
							break;
						temp = iterator.next();
					}
					temp = iterator.previous();
					if (temp.process[processId] == true) {
						temp = iterator.previous();
					}
					for (int i = 0; i < numBubbles / 2; i += 1) {
						temp = iterator.previous();
					}

					numTimesBubblesMerged += 1;
				}
				temp.setValue(newValue, processId,id);
			}
		}
		qLengths[processId] += 1;
		//calculateStats();

	}

	public synchronized void remove(int oldValue, int processId, int location) {
		if (debug) {
			System.out.println(internalList);
			System.out.println();
			System.out.println("Remove location " + location
					+ " | list number " + processId + " | queue size "
					+ internalList.size());
		}
		if (qLengths[processId] <= 0) {
			throw new Error("removing from an empty queue");
		}

		ListIterator<LinkedListNode> iterator = internalList.listIterator();
		int count = 0;
		LinkedListNode node = null;
		while (count <= location) {
			if (!iterator.hasNext()) {
				if(debug)System.out
						.println(" error in remove :  no next element , count = "
								+ count + " location= " + location);
			}
			node = iterator.next();
			if (node.process[processId] == true)
				count += 1;
		}
		int newValue = node.removeValue(oldValue, processId,id);
		if (node.isEmpty()) {
			iterator.remove();
			numNodesDeleted += 1;
		}
		if (iterator.hasPrevious()) {
			iterator.previous();
			merge(iterator);
		}
		qLengths[processId] -= 1;
//		calculateStats();
	}

	// call merge after any delete
	void merge(ListIterator<LinkedListNode> iterator) {
		if (!flagEnableMergeAdjacentCells)
			return;
		LinkedListNode prev = null, current = null, next = null;
		if (!iterator.hasNext()) {
			if (iterator.hasPrevious())
				iterator.previous();
		}
		if (iterator.hasNext()) {
			current = iterator.next();
			if (iterator.hasNext())
				next = iterator.next();

			if (next == null) {
				iterator.previous();
				if (iterator.hasPrevious())
					iterator.previous();
			} else if (!current.canBeMerged(next)) {
				iterator.previous();
				if (iterator.hasPrevious())
					iterator.previous();
				if (iterator.hasPrevious())
					iterator.previous();
			} else {
				iterator.previous();
				iterator.previous();
			}
		}
		while (true) {

			if (!iterator.hasNext())
				return;
			prev = null;
			current = null;
			next = null;
			int count = 0;
			current = iterator.next();
			if (!iterator.hasNext())
				return;
			next = iterator.next();
			if (!current.canBeMerged(next))
				return;
			iterator.remove();
			iterator.previous();

			while (current.canBeMerged(next) && iterator.hasPrevious()) {
				count += 1;
				current = iterator.previous();
			}
			iterator.next();
			count -= 1;
			if (!current.canBeMerged(next)) {
				current = iterator.next();
				count -= 1;
			}
			current.merge(next,id);
			numNodesMerged += 1;
			numNodesDeleted += 1;
			if (debug)
				System.out.println("Node merged with next node");
			for (int i = 0; i < count; i += 1) {
				current = iterator.next();
			}
		}

	}
/*
	public void calculateStats() {
		int totalLength = 0;
		int numConcurrentQueues = 0;

		for (int i = 0; i < numQueues; i += 1) {
			totalLength += qLengths[i];
			if (qLengths[i] > 0)
				numConcurrentQueues += 1;
		}
		int actualLength = queue.size();
		if (totalLength > maxExpectedQueueSize) {
			maxExpectedQueueSize = totalLength;
		}
		if (actualLength > maxActualQueueSize) {
			maxActualQueueSize = actualLength;
		}
		if (numConcurrentQueues > maxConcurrentQueues) {
			maxConcurrentQueues = numConcurrentQueues;
		}
		// sanityCheck();
		if ((totalLength > 0 && actualLength <= 0)
				|| (actualLength > 0 && totalLength / actualLength > numConcurrentQueues)) {
			System.out.println("Sanity check failed");
			System.out.println("Expected queue size =  " + totalLength);
			System.out.println("Actual queue size = " + actualLength);
			System.out.println("Number of concurrent queues = "
					+ numConcurrentQueues);
			System.exit(-1);

		}
		if (actualLength != 0) {
			double savingsRatio = totalLength / (1.0 * actualLength);
			if (savingsRatio > bestSavingsRatio)
				bestSavingsRatio = savingsRatio;
			if (savingsRatio < worstSavingsRatio)
				worstSavingsRatio = savingsRatio;
		}
	}
	public String getFinalStats(){
		StringBuffer buf  = new StringBuffer();
		buf.append("*****************************************\n");
		buf.append("Max expected Queue size = " + maxExpectedQueueSize+ "\n");
		buf.append("Max actual Queue size = " + maxActualQueueSize+ "\n");
		buf.append("Max number of concurrent queues = " + maxConcurrentQueues+ "\n");
		buf.append("Best performance ratio = " + bestSavingsRatio+ "\n");
		buf.append("Actual final performance ratio = "
				+ maxExpectedQueueSize / (1.0 * maxActualQueueSize)+ "\n");
		
		buf.append("*****************************************"+"\n");
		return buf.toString();
	}
	public void outputFinalStats() {
		if (debug)
			System.out.println(queue);
		LinkedListNode current = null;
		LinkedListNode next = null;
		ListIterator<LinkedListNode> iterator = queue.listIterator();
		if (iterator.hasNext()) {
			current = iterator.next();
			while (iterator.hasNext()) {
				next = iterator.next();
				if (current.canBeMerged(next)) {
					System.out.println(" Sanity Check failed , node \n"
							+ current + "can be merged with node\n" + next
							+ "around index " + iterator.nextIndex());
				}
				current = next;
			}
			System.out.println("Sanity check complete");
		}

		System.out.println("*****************************************");
		System.out.println("Final LinkedList Size " + queue.size());
		System.out.println("Max expected List size = " + maxExpectedQueueSize);
		System.out.println("Max actual List size = " + maxActualQueueSize);
		System.out.println("Max number of concurrent Lists = "
				+ maxConcurrentQueues);
		System.out.println("Best performance ratio = " + bestSavingsRatio);
		System.out.println("Actual final performance ratio = "
				+ (maxExpectedQueueSize) / (1.0 * maxActualQueueSize));
		System.out.println("Total number of nodes deleted is "
				+ numNodesDeleted + " out of which " + numNodesMerged
				+ " were merged");
		System.out.println("*****************************************");
		// System.out.println("Total number of nodes deleted = "
		// + numNodesDeleted);

	}
*/
	// deprecated : merges adjacent nodes only
	void mergeSingle(ListIterator<LinkedListNode> iterator) {
		if (!flagEnableMergeAdjacentCells)
			return;
		if (!iterator.hasNext())
			return;
		LinkedListNode prev = null, current = null, next = null;
		if (iterator.hasPrevious()) {
			prev = iterator.previous();
			iterator.next();
			current = iterator.next();
			if (prev.canBeMerged(current)) {
				prev.merge(current,id);
				iterator.remove();
				// System.out.println("Node merged with previous node");
				numNodesMerged += 1;
				numNodesDeleted += 1;
				return;
			} else {
				iterator.previous();
			}
		}
		current = iterator.next();
		if (iterator.hasNext()) {
			next = iterator.next();

			if (current.canBeMerged(next)) {
				// System.out .println("Nodes to be merged are : \n" +
				// current.value + " "+ current.numReferences+
				// Arrays.toString(current.process) +"\n"+
				// next.value + " "+ current.numReferences+
				// Arrays.toString(next.process));
				current.merge(next,id);
				iterator.remove();
				// System.out.println("Node merged with next node");
				numNodesMerged += 1;
				numNodesDeleted += 1;
			}
		}

	}
	// TODO
	// public synchronized Vector<Integer> recoverFromFault(int faultyProcess,
	// int[][] allQueues) {
	// }
	public String toString(){
		String out = "";
		for(int i =0; i < internalList.size();++i){
			out = out + internalList.get(i).getValue() + "--";
		}
		return out;
	}
	
	
	//approximate size of the object in bytes
	public double sizeOfObject(){
		//to account for the size of the queue...each node has an integer of say 4 bytes + reference count + next pointer +a bit vector of size numQueues
		//double sizeOfOne =queue.size()*(8+ Util.nodeDataSize + 0.125*numQueues);
		double sizeOfOne =internalList.size();
		return sizeOfOne;
	}
	
	//code to start the BackupServer 
    public static void main(String[] args) {
    	int id = Integer.parseInt(args[0]);
    	int numQueues = Integer.parseInt(args[1]);
    	int maxFaults = Integer.parseInt(args[2]);

    	OldFusedLinkedList backup = new OldFusedLinkedList(numQueues,maxFaults,id); 
        if(Util.debugFlag)System.out.println("Backup "+ id+" started:");
        try {
            ServerSocket listener = new ServerSocket(Symbols.BackupPort+id);
            
            while (true) {
                Socket client = listener.accept();
                ListenerThread l = new ListenerThread(backup,client);
                l.start();
            }
        } catch (IOException e) {
            System.err.println("Server aborted:" + e);
        }

	}

}

class LinkedListNode implements Serializable{
	int value = 0;

	int numProc;

	boolean[] process;

	int numReferences = 0 ;

	public LinkedListNode(int processId, int numProc) {
		process = new boolean[numProc];
		process[processId] = true;
		numReferences += 1;
		this.numProc = numProc;
	}

	public int removeValue(int oldValue, int processId,int codeId) {
		try {
			value = Fusion.getUpdatedCode(value, codeId, oldValue, 0, processId);
		} catch (InterfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		process[processId] = false;
		numReferences -= 1;
		return value;
	}

	public void setValue(int newValue, int processId, int codeId) {
		if(Util.debugFlag)System.out.println("In set value :old value :"+value+ " newValue :" + newValue + " processId :"+processId + "codeId:"+codeId);
		try {
			value = Fusion.getUpdatedCode(value, codeId, 0, newValue, processId);
			if(Util.debugFlag)System.out.println("Updated value : "+ value);
		} catch (InterfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		process[processId] = true;
		numReferences += 1;
	}
	public int getValue(){
		return value;
	}
	public boolean isEmpty() {
		if (numReferences < 0) {
			throw new Error("Unexpected behaviour, num heads < 0 numheads ="
					+ numReferences);
		}
		return numReferences == 0;
	}

	public boolean canBeMerged(LinkedListNode node) {
//		if (Util.debugFlag) {
//			System.out.println("Nodes checked for possible merger are : \n"
//					+ value + " " + numReferences + Arrays.toString(process)
//					+ "\n" + node.value + " " + node.numReferences
//					+ Arrays.toString(node.process));
//		}
		for (int i = 0; i < numProc; i += 1) {
			if (process[i] && node.process[i])
				return false;
		}
		return true;
	}

	public void merge(LinkedListNode node,int codeId) {
		value = Fusion.getAddedCodes(value,node.value);

		numReferences = 0;
		for (int i = 0; i < numProc; i += 1) {
			process[i] = (node.process[i] || process[i]);
			if (process[i])
				numReferences += 1;
		}

	}

	public String toString() {
		int retValue = 0;
		int val = 1;
		for (int i = 0; i < numProc; i += 1, val = val * 2) {
			if (process[i])
				retValue = retValue + val;

		}
		return "(" + retValue + ")";
	}
}
