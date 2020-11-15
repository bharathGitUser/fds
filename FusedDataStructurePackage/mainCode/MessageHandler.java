



import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

//all the nodes in the system...i.e original data structures and the backups impement this
//mainly to provide a uniform communication interface.

public interface MessageHandler {
	public  void handleMessage(ObjectOutputStream outStream, ObjectInputStream inStream, String msg);
}
