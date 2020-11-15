import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;



public class ReplicatedLinkedList extends LinkedList<Integer> implements MessageHandler,Serializable{
	int myId;
	boolean countNotStarted = true;
	long startTime;
	long endTime;
	long totalUpdateTime = 0;
	public ReplicatedLinkedList(int myId){
		this.myId = myId;
	}
	
	public  synchronized void handleMessage(ObjectOutputStream outStream, ObjectInputStream inStream, String msg){
		//the only message it can receieve is a request for it's data during recovery
		if(countNotStarted){
			startTime = Util.getCurrentTime();
			countNotStarted = false;
		}
		
		try {
	        StringTokenizer st = new StringTokenizer(msg);
	        String tag = st.nextToken();
			if(tag.equals("add")){
	        	int location = Integer.parseInt(st.nextToken());
	        	int newValue = Integer.parseInt(st.nextToken());
	        	startTime = Util.getCurrentTime();
	        	add(location,newValue);
	        	endTime = Util.getCurrentTime();
	        	totalUpdateTime = totalUpdateTime + (endTime - startTime);
	        	if(Util.debugFlag)System.out.println("************************Total Update time:"+totalUpdateTime+" ***************************");
	        	if(Util.debugFlag)System.out.println("B["+myId+"]:"+this);
	        }else if(tag.equals("remove")){
	        	int location = Integer.parseInt(st.nextToken());
	        	startTime = Util.getCurrentTime();
	        	remove(location);
	        	endTime = Util.getCurrentTime();
	        	totalUpdateTime = totalUpdateTime + (endTime - startTime);
	        	if(Util.debugFlag)System.out.println("Total Update time:"+totalUpdateTime);
	        	if(Util.debugFlag)System.out.println("B["+myId+"]:"+this);
	        }else if(msg.equals("req")){
			//send data for recovery 	
					Vector<Integer> data = new Vector<Integer>();
					for(int i =0; i < super.size();++i){
						data.add(this.get(i));
					}
	
					outStream.writeObject(data);
	
					outStream.flush(); 
					
					if(Util.debugFlag)System.out.println("data sent out");
			}else if(msg.equals("recover")){
				//obtain recovered data..assuming that this was one of the erased structure
				
        		Vector<Integer> data = (Vector<Integer>)inStream.readObject();
        		
        		if(Util.debugFlag)System.out.println("Setting the data elements to zero, inorder to show that recovery is working...");
        		for(int i =0; i < this.size();++i){
        			this.set(i, 0);
        		}
        		if(Util.debugFlag)System.out.println("Q"+ myId +" " +this);
        		
        		//populate the data
        		for(int i =0; i < data.size();++i){
        			this.set(i, data.get(i));
        		}
        		
        		if(Util.debugFlag)System.out.println("Recovered Data"); System.out.println("Q"+ myId +" " +this);
			}else if(msg.equals("time")){
				outStream.reset();
				outStream.writeObject(totalUpdateTime);
				outStream.flush();
			}	
					
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException c){
			c.printStackTrace();
		}

		
	}
	
    public static void main(String[] args) {
    	int id = Integer.parseInt(args[0]);

    	ReplicatedLinkedList list = new ReplicatedLinkedList(id);

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
