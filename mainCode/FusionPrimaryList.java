



import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;




public class FusionPrimaryList implements MessageHandler{

	int numPrimaries, numFaults,primaryId; 	
	PrintStream[] pout;
	ObjectOutputStream[] objOut;
	ObjectInputStream[] objIn;
	List<PrimaryNode<Integer>> primaryList;
	MyContainer auxList; 
	LinkedList<Integer> testList; 
	public FusionPrimaryList(int numFaults, int primaryId, List<PrimaryNode<Integer>> primaryList){
		this.numFaults = numFaults;
		this.primaryId = primaryId;
		this.primaryList = primaryList;
		auxList = new MyContainer();
		testList = new LinkedList<Integer>();
		initializeCommunicationStreams(); 
	}
	
	void add(int index, int value){
		testList.add(index,value);
		PrimaryNode<Integer> p = new PrimaryNode<Integer>(value);
		PrimaryAuxNode<Integer> a = new PrimaryAuxNode<Integer>(p);
		p.setAuxNode(a); 
		primaryList.add(index, p); 
		auxList.add(a);//adds to end of list
		
		//send the message to all backups
		String msgForBackups = "add"+ " "+index + " " + value + " "+ primaryId;
		sendMsgToBackups(msgForBackups);
		
		if(Util.debugFlag)System.out.println("add, index:"+index+" value:"+ value+" test list:"+testList+ " List:"+primaryList+" AuxList:"+auxList);
	}
	
	void remove(int index){
		testList.remove(index);
		PrimaryNode<Integer> p = primaryList.remove(index);
		PrimaryAuxNode<Integer> a = (PrimaryAuxNode<Integer>)auxList.getLast();
		//send the message to all backups
		String msgForBackups = "remove"+ " "+index + " " + p.getValue() + " "+ a.getPrimNode().getValue()+" "+primaryId;  
		sendMsgToBackups(msgForBackups);
		
		//shift the final aux node to the position index
		auxList.replaceNodeWithTail(p.getAuxNode());
		
		if(Util.debugFlag)System.out.println("rem, index:"+index+" test list:"+testList+ " List:"+primaryList+" AuxList:"+auxList);
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
				int index = Integer.parseInt(st.nextToken());
	        	int value = Integer.parseInt(st.nextToken());
	        	add(index,value);
	        }else if(tag.equals("remove")){
	        	int index = Integer.parseInt(st.nextToken());
	        	remove(index);
	        }else if(msg.equals("req")){
			//send data for recovery 	
					Vector<Integer> data = new Vector<Integer>();
					for(int i =0; i < primaryList.size();++i){
						data.add((Integer)primaryList.get(i).getValue());
					}
					outStream.writeObject(data);
					outStream.flush(); 
					if(Util.debugFlag)System.out.println("data sent out");
			}else if(msg.equals("size")){
				outStream.writeObject(primaryList.size());
				outStream.flush(); 
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
    	int id = Integer.parseInt(args[0]);
		int numFaults = Integer.parseInt(args[1]);
    
		GenericList<PrimaryNode<Integer>> list = new GenericList<PrimaryNode<Integer>>(); 
    	FusionPrimaryList fusibleList = new FusionPrimaryList(numFaults, id,list); 
        if(Util.debugFlag)System.out.println("Started Original Data Structure " + id);

        //open a socket and create a thread so that the oracle can to talk to it when necessary
        try {
        	ServerSocket listener = new ServerSocket(Symbols.DataPort+id);
            while(true){
	        	Socket client = listener.accept();
	            ListenerThread l = new ListenerThread(fusibleList,client);
	            l.start();
            }
        }catch (IOException e) {
            System.err.println("Server aborted:" + e);
        }
    }



}
