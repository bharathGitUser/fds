




import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.LinkedList;
import java.util.Vector;






public class Test {

	/**
	 * @param args
	 */
	public static void testListVector(){
		Vector<Integer> l= new Vector<Integer>(3,9); 
		
		l.add(1, 5); 
	}
	public static void main(String[] args) {
		testListVector();
		JavaReedInterface jreed = new JavaReedInterface();
	int numStructures =7, numBackups=5;
        int[][] i2arr = JavaReedInterface.genRsMatrix(numStructures,numBackups,16);
	System.out.println("\n");
        for (int i = 0; i < numStructures; i++) {
            for (int j = 0; j < numBackups; j++) {
                 System.out.print(" " + i2arr[i][j]);
            }
            System.out.println();
        }
        int[] array = new int[numStructures*numBackups];
        for (int i = 0; i < numStructures; i++) {
            for (int j = 0; j < numBackups; j++) {
            	array[i*numBackups+j]= i2arr[i][j];
            }
            
        }
		System.out.println(array[4*numStructures+5]);
/*		// TODO Auto-generated method stub
        int[] list = new int[10000];
        LinkedList<Integer> ll= new LinkedList<Integer>();
        int x;
        long starttime, stoptime, timeused=0;

        ThreadMXBean tb = ManagementFactory.getThreadMXBean();
        
        long startCpuTime = tb.getCurrentThreadCpuTime();
    	starttime = System.nanoTime();
        for (int k = 0; k < 1000; k++){
        	ll.add(k);
        }
        long stopCpuTime = tb.getCurrentThreadCpuTime();

    	stoptime = System.nanoTime();
        long cputimeused = stopCpuTime - startCpuTime;
        timeused = stoptime - starttime;
        
//        


        System.out.println("cpu time used:  " + cputimeused + "ns");
        System.out.println("system time used:  " + timeused/1000 + "micros");
*/
	}

}
