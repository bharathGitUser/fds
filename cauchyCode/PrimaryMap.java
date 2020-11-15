


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
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Map.Entry;


public class PrimaryMap implements MessageHandler{

	int numPrimaries, numFaults,primaryId; 	
	PrintStream[] pout;
	ObjectOutputStream[] objOut;
	ObjectInputStream[] objIn;
	Map<Integer,PrimaryNode<Integer>> primaryMap;
	MyContainer auxList; 
	public PrimaryMap(int numFaults, int primaryId, Map<Integer,PrimaryNode<Integer>> primaryMap){
		this.numFaults = numFaults;
		this.primaryId = primaryId;
		this.primaryMap = primaryMap;
		auxList = new MyContainer();
		initializeCommunicationStreams(); 
	}
	
	void put(int key, int value){
		
		int oldValue = -1;
		if(primaryMap.containsKey(key)){//if key already exists, just update its value
			PrimaryNode<Integer> oldNode = 	primaryMap.get(key);
			oldValue = oldNode.getValue();//the backup will need this value for update.  
			oldNode.setValue(value); 
		}else{
			PrimaryNode<Integer> p = new PrimaryNode<Integer>(value);
			PrimaryAuxNode<Integer> a = new PrimaryAuxNode<Integer>(p);
			p.setAuxNode(a); 
			primaryMap.put(key, p);
			auxList.add(a);//adds to end of list
		}
		//send the message to all backups
		String msgForBackups = "add"+ " "+key + " " + value + " "+oldValue+" " +primaryId;
		sendMsgToBackups(msgForBackups);
		
		if(Util.debugFlag)System.out.println("add, index:"+key+" value:"+ value+" oldValue:"+oldValue+" Map:"+primaryMap+" AuxList:"+auxList);
	}
	
	void remove(int key){
		if(!primaryMap.containsKey(key))
			try {
				throw new Exception("Key "+ key+" Not Present");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		PrimaryNode<Integer> p = primaryMap.remove(key);
		PrimaryAuxNode<Integer> a = (PrimaryAuxNode<Integer>)auxList.getLast();
		//send the message to all backups
		String msgForBackups = "remove"+ " "+key + " " + p.getValue() + " "+ a.getPrimNode().getValue()+" "+primaryId;  
		sendMsgToBackups(msgForBackups);
		
		//shift the final aux node to the position index
		auxList.replaceNodeWithTail(p.getAuxNode());
		
		if(Util.debugFlag)System.out.println("rem, index:"+key+" Map:"+primaryMap+" AuxList:"+auxList);
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
	        
			if(tag.equals("put")){
				int key = Integer.parseInt(st.nextToken());
	        	int value = Integer.parseInt(st.nextToken());
	        	put(key,value);
	        }else if(tag.equals("remove")){
	        	int key = Integer.parseInt(st.nextToken());
	        	remove(key);
	        }else if(msg.equals("req")){
			//send data for recovery 	
					HashMap<Integer,Integer> data = new HashMap<Integer,Integer>();
					Set<Entry<Integer,PrimaryNode<Integer>>> s= primaryMap.entrySet(); 
					Iterator<Entry<Integer,PrimaryNode<Integer>>> it = s.iterator(); 
					while (it.hasNext()) {
				        Map.Entry<Integer,PrimaryNode<Integer>> pairs = it.next();
				        data.put(pairs.getKey(), pairs.getValue().getValue());
					}
					outStream.writeObject(data);
					outStream.flush(); 
					if(Util.debugFlag)System.out.println("data sent out");
			}else if(msg.equals("size")){
				outStream.writeObject(primaryMap.size());
				outStream.flush(); 
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
    	int id = Integer.parseInt(args[0]);
		int numFaults = Integer.parseInt(args[1]);
    
		GenericMap<Integer, PrimaryNode<Integer>> map = new GenericMap<Integer,PrimaryNode<Integer>>(); 
    	PrimaryMap fusibleMap = new PrimaryMap(numFaults, id,map); 
        if(Util.debugFlag)System.out.println("Started Original Data Structure " + id);

        //open a socket and create a thread so that the oracle can to talk to it when necessary
        try {
        	ServerSocket listener = new ServerSocket(Symbols.DataPort+id);
            while(true){
	        	Socket client = listener.accept();
	            ListenerThread l = new ListenerThread(fusibleMap,client);
	            l.start();
            }
        }catch (IOException e) {
            System.err.println("Server aborted:" + e);
        }
    }



}
