


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Map.Entry;




public class ReplicatedMap extends GenericMap<Integer, Integer> implements MessageHandler,Serializable{
	int myId;
	boolean countNotStarted = true;
	long startTime;
	long endTime;
	long totalUpdateTime = 0;
	int numOperations =0;
	public ReplicatedMap(int myId){
		this.myId = myId;
	}
	
	public  synchronized void handleMessage(ObjectOutputStream outStream, ObjectInputStream inStream, String msg){
		//the only message it can receieve is a request for it's data during recovery
/*		if(countNotStarted){
			startTime = Util.getCurrentTime();
			countNotStarted = false;
		}*/
		
		try {
	        StringTokenizer st = new StringTokenizer(msg);
	        String tag = st.nextToken();
			if(tag.equals("put")){
				++numOperations;
	        	int key = Integer.parseInt(st.nextToken());
	        	int newValue = Integer.parseInt(st.nextToken());
	        	startTime = Util.getCurrentTime();
	        	put(key,newValue);
	        	endTime = Util.getCurrentTime();
	        	totalUpdateTime = totalUpdateTime + (endTime - startTime);
	        	if(Util.debugFlag)System.out.println("************************Total Update time:"+totalUpdateTime+" ***************************");
	        	if(Util.debugFlag)System.out.println("B["+myId+"]:"+this);
	        }else if(tag.equals("remove")){
	        	++numOperations;
	        	int key = Integer.parseInt(st.nextToken());
	        	startTime = Util.getCurrentTime();
	        	remove(key);
	        	endTime = Util.getCurrentTime();
	        	totalUpdateTime = totalUpdateTime + (endTime - startTime);
	        	if(Util.debugFlag)System.out.println("Total Update time:"+totalUpdateTime);
	        	if(Util.debugFlag)System.out.println("B["+myId+"]:"+this);
	        }else if(msg.equals("req")){
			//send data for recovery 	
				HashMap<Integer,Integer> data = new HashMap<Integer,Integer>();
				Set<Entry<Integer,Integer>> s= this.entrySet(); 
				Iterator<Entry<Integer,Integer>> it = s.iterator(); 
				while (it.hasNext()) {
			        Map.Entry<Integer,Integer> pairs = it.next();
			        data.put(pairs.getKey(), pairs.getValue());
				}
				outStream.writeObject(data);
				outStream.flush(); 
				if(Util.debugFlag)System.out.println("data sent out");					
			}else if(msg.equals("time")){
				outStream.reset();
				outStream.writeObject(totalUpdateTime/numOperations);
				outStream.flush();
			}	
					
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
    public static void main(String[] args) {
    	int id = Integer.parseInt(args[0]);

    	ReplicatedMap list = new ReplicatedMap(id);

        //open a socket and create a thread so that the recovery agent can to talk to it when necessary
        try {
        	ServerSocket listener = new ServerSocket(Symbols.BackupPort+id);
            if(Util.debugFlag)System.out.println("Started Backup Data Structure " + id);
            while(true){
	            Socket client = listener.accept();
	            ListenerThread l = new ListenerThread(list,client);
	            l.start();
            }
        } catch (IOException e) {
            System.err.println("Server aborted:" + e);
        }
    }

	

}
