






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
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;




public class StandardPrimaryMap extends GenericMap<Integer,Integer> implements MessageHandler,Serializable{
	int myId;
	
	public StandardPrimaryMap(int myId){
		this.myId = myId;
	}

	public String toString(){
		String out = "List[" + myId + "]:";
		for(int i =0; i < this.size(); ++i){
			out = out + " "+ this.get(i) + "--";
		}
		
		return out;
	}

	public  synchronized void handleMessage(ObjectOutputStream outStream, ObjectInputStream inStream, String msg){
		try {
	        StringTokenizer st = new StringTokenizer(msg);
	        String tag = st.nextToken();
			if(tag.equals("put")){
	        	int location = Integer.parseInt(st.nextToken());
	        	int newValue = Integer.parseInt(st.nextToken());
	        	put(location,newValue);
	        	if(Util.debugFlag)System.out.println("put, location:"+location+" value:"+newValue);
	        }else if(tag.equals("remove")){
	        	int location = Integer.parseInt(st.nextToken());
	        	if(Util.debugFlag)System.out.println("remove, key:"+location);
	        	int oldValue = remove(location);
	        	outStream.writeObject(oldValue);
	        	outStream.flush();
	        }else if(msg.equals("req")){
			//send data for recovery 	
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
			}
			else if(msg.equals("size")){
//				outStream.writeObject(this.size()*(4+Util.nodeDataSize));//data + next
				outStream.writeObject(this.size());
				outStream.flush(); 
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		if(Util.debugFlag)System.out.println(this);
		
	}
	
    public static void main(String[] args) {
    	int id = Integer.parseInt(args[0]);
    	StandardPrimaryMap list = new StandardPrimaryMap(id);

        //open a socket and create a thread so that the recovery agent can to talk to it when necessary
        try {
        	ServerSocket listener = new ServerSocket(Symbols.DataPort+id);
            if(Util.debugFlag)System.out.println("Started Original Data Structure " + id);
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
