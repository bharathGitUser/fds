




import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
public class Util {
    public static int max(int a, int b) {
        if (a > b) return a;
        return b;
    }
    public static void mySleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        }
    }
    public static void myWait(Object obj) {
        println("waiting");
        try {
            obj.wait();
        } catch (InterruptedException e) {
        }
    }
    public static boolean lessThan(int A[], int B[]) {
        for (int j = 0; j < A.length; j++)
            if (A[j] > B[j]) return false;
        for (int j = 0; j < A.length; j++)
            if (A[j] < B[j]) return true;
        return false;
    }
    public static int maxArray(int A[]) {
        int v = A[0];
        for (int i=0; i<A.length; i++)
            if (A[i] > v) v = A[i];
        return v;
    }
    public static String writeArray(int A[]){
        StringBuffer s = new StringBuffer();
        for (int j = 0; j < A.length; j++)
            s.append(String.valueOf(A[j]) + " ");
        return new String(s.toString());
    }
    public static void readArray(String s, int A[]) {
        StringTokenizer st = new StringTokenizer(s);
        for (int j = 0; j < A.length; j++)
            A[j] = Integer.parseInt(st.nextToken());
    }
    public static int searchArray(int A[], int x) {
        for (int i = 0; i < A.length; i++)
            if (A[i] == x) return i;
        return -1;
    }
    
    public static void println(String s){
        if (Symbols.debugFlag) {
            System.out.println(s);
            System.out.flush();
        }
    }
    
    public static long getCurrentTime(){
    	 //ThreadMXBean tb = ManagementFactory.getThreadMXBean();
    	// return tb.getCurrentThreadCpuTime();
    	return System.nanoTime()/1000;
    }
    
    public static boolean debugFlag = false;
    public static int numTries = 5;
	public static double threshold = 0.2;
	public static int nodeDataSize = 100;
	public static long encodingTime = 0;
	public static long decodingTime =0; 
	public static int cauchyW = 5; 

}
