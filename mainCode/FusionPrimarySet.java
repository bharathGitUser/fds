




//this just uses the fused hash map as backup or else we cannot delete elements; 

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Map.Entry;



public class FusionPrimarySet implements MessageHandler{

	int numPrimaries, numFaults,primaryId; 	
	PrintStream[] pout;
	ObjectOutputStream[] objOut;
	ObjectInputStream[] objIn;
	Set<PrimaryNode<Integer>> primarySet;
	HashMap<Integer, Long> idMap; 
	MyContainer auxList; 
	Random r;
	public FusionPrimarySet(int numFaults, int primaryId, Set<PrimaryNode<Integer>> primarySet){
		this.numFaults = numFaults;
		this.primaryId = primaryId;
		this.primarySet = primarySet;
		idMap = new HashMap<Integer, Long>();
		auxList = new MyContainer();
		initializeCommunicationStreams(); 
	}
	
	void add(int value){	
		PrimaryNode<Integer> p = new PrimaryNode<Integer>(value);
		boolean result = primarySet.add(p);
		if(result == true){//it was not already present
			long key = System.currentTimeMillis();
			idMap.put(value, key);//need to store this time as a unique identifier so that we can delete later
			PrimaryAuxNode<Integer> a = new PrimaryAuxNode<Integer>(p);
			p.setAuxNode(a); 
			auxList.add(a);//adds to end of list
			//send the message to all backups
			String msgForBackups = "add"+ " "+key + " " + value + " " +primaryId;
			sendMsgToBackups(msgForBackups);
			
			if(Util.debugFlag)System.out.println("add, value:"+ value+" Queue:"+primarySet+" AuxList:"+auxList);
		}
	}
	
	void remove(int value){
		PrimaryNode<Integer> p = new PrimaryNode<Integer>(value);
		primarySet.remove(p);
		PrimaryAuxNode<Integer> a = (PrimaryAuxNode<Integer>)auxList.getLast();
		//send the message to all backups
		long key = idMap.get(value);
		String msgForBackups = "remove"+ " "+key + " " + p.getValue() + " "+ a.getPrimNode().getValue()+" "+primaryId;  
		sendMsgToBackups(msgForBackups);
		
		//shift the final aux node to the position index
		auxList.replaceNodeWithTail(p.getAuxNode());
		
		if(Util.debugFlag)System.out.println("rem, Queue:"+primarySet+" AuxList:"+auxList);
	}
	
	void sendMsgToBackups(String msgForBackups){
		for(int i = 0; i < numFaults; ++i){
			pout[i].println(msgForBackups);
			pout[i].flush();
		}
	}
	void initializeCommunicationStreams(){
		pout = new PrintStream[numFaults];
		objOut = new ObjectOutputStream[numFaults];
		objIn = new ObjectInputStream[numFaults];

		//handle to all the backup structures
		for(int i =0 ; i < numFaults; ++i){
     	   try {
			Socket s = new Socket(Symbols.nameServer, 
			        Symbols.BackupPort+i);
			pout[i] = new PrintStream(s.getOutputStream());
			objOut[i] = new ObjectOutputStream(s.getOutputStream());
			objIn[i] = new ObjectInputStream(s.getInputStream());
			if(Util.debugFlag)System.out.println("Connected to Backup Server "+ i);
     	   } catch (UnknownHostException e) {
     		   e.printStackTrace();
     	   } catch (IOException e) {
				e.printStackTrace();
     	   }
		}		
	}
	

	public synchronized void handleMessage(ObjectOutputStream outStream, ObjectInputStream inStream, String msg){
		try {
	        StringTokenizer st = new StringTokenizer(msg);
	        String tag = st.nextToken();
	        
			if(tag.equals("add")){
	        	int value = Integer.parseInt(st.nextToken());
	        	add(value);
	        }else if(tag.equals("remove")){
	        	int value = Integer.parseInt(st.nextToken());
	        	add(value);
	        	remove(value);
	        }else if(msg.equals("req")){
			//send data for recovery 	
	        	Object[] array = primarySet.toArray();
				Vector<Integer> data = new Vector<Integer>();
				for(int i =0; i < array.length;++i){
					PrimaryNode<Integer> a = (PrimaryNode<Integer>)array[i];
					data.add((Integer)a.getValue());
				}
				outStream.writeObject(data);
				outStream.flush(); 
				if(Util.debugFlag)System.out.println("data sent out");
			}else if(msg.equals("size")){
				outStream.writeObject(primarySet.size());
				outStream.flush(); 
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
    	int id = Integer.parseInt(args[0]);
		int numFaults = Integer.parseInt(args[1]);
    
		GenericSet<PrimaryNode<Integer>> queue = new GenericSet<PrimaryNode<Integer>>(); 
    	FusionPrimarySet fusibleSet = new FusionPrimarySet(numFaults, id,queue); 
        if(Util.debugFlag)System.out.println("Started Original Data Structure " + id);

        //open a socket and create a thread so that the oracle can to talk to it when necessary
        try {
        	ServerSocket listener = new ServerSocket(Symbols.DataPort+id);
            while(true){
	        	Socket client = listener.accept();
	            ListenerThread l = new ListenerThread(fusibleSet,client);
	            l.start();
            }
        }catch (IOException e) {
            System.err.println("Server aborted:" + e);
        }
    }



}
