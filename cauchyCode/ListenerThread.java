


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;



public class ListenerThread extends Thread{
	MessageHandler struct;
	Socket client;
	ObjectOutputStream objOut;
	ObjectInputStream objIn;
	
	public ListenerThread(MessageHandler struct, Socket client) {
		this.struct = struct;
		this.client = client;	
	}
	
	public void run()
	{
        try {
        	BufferedReader din = new BufferedReader
			(new InputStreamReader(client.getInputStream()));
        	objOut = new ObjectOutputStream(client.getOutputStream());
        	objIn = new ObjectInputStream(client.getInputStream());
        	
        	String msg;
        	while ((msg = din.readLine()) != null){ 
				//if(Util.debugFlag)System.out.println("Recieved Message :" + msg);
				struct.handleMessage(objOut,objIn, msg);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
}
