



import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;





public class Test {
	static JavaReedInterface jreed = new JavaReedInterface();
	/**
	 * @param args
	 */
	public static void testListVector(){
		Vector<Integer> l= new Vector<Integer>(3,9); 
		
		l.add(1, 5); 
	}
	public static void main(String[] args) {
		for(int i=0; i < 1;++i)
			test(); 
		
	}
	public static void test(){
		Random gen = new Random();
		//int k = (gen.nextInt(10)+1),m =k-1, w= 5,psize =4;   
		int k = 3,m =2, w= 5,psize =4;
		int[] bitMatrix = jreed.genCauchyMatrix(k, m, w);
		
 
		int[] all_data = new int[k*w];
		int[] all_code = new int[m*w]; 
		for(int i=0; i <k; ++i){
			//System.out.println("-------------------");
			int[] data = new int[w];
			for(int j=0; j < w;++j){
//				data[j]= gen.nextInt();
				data[j]=0;
				//System.out.print(data[j]+" ");
			}
//			data[0]=i+1; 
//			all_data[i*w]= i+1;
			System.out.println("----------------------");
			for(int t=0; t < w; ++t){
				System.out.print(data[t]);
			}
			System.out.println();
			for(int c =0; c < m;++c){
				System.out.print(c+":");
				int[] element = matElement(bitMatrix,c,i,k,w);
				int[] oldCode = new int[w];
				for(int l=0; l <w;++l){
					oldCode[l] = all_code[c*w+l];
				}
				System.out.print("element");
				for(int s=0; s < w*w;++s)
					System.out.print(element[s]);
				System.out.println();
				int[] code = jreed.cauchyUpdate(data, oldCode, element, psize, w);
				for(int l=0; l < w; ++l){
					all_code[c*w+l]= code[l];
					System.out.print(code[l]);
				}
				System.out.print("|");
			}
			System.out.println();
		}
		int primaryId = 0;
		int value =6; 
		updateCode(value,primaryId,all_data, all_code, bitMatrix,k,m, w,psize); 

/*		primaryId = 2;
		value =3; 
		updateCode(value,primaryId,all_data, all_code, bitMatrix,k,m, w,psize); 
*/
		System.out.println("all data");
		for(int i=0; i < k*w; ++i)
			System.out.print(all_data[i]+" ");
		System.out.println();
		
		System.out.println("all code");
		for(int i=0; i < m*w; ++i)
			System.out.print(all_code[i]+" ");
		System.out.println();

		int[] old_data = new int[k*w];
		for(int i=0; i < k*w;++i){
			old_data[i]=all_data[i]; 
		}
		int[] erasures = new int[m+1];
		int i; 
		for(i=0; i < m;++i){
			erasures[i]=i;
			for(int j=0; j < w;++j){
				all_data[erasures[i]*w+j]=0;
			}
		}
		erasures[i]=-1;
/*		System.out.println("erased data");
		for(i=0; i < k*w; ++i){
			System.out.print(all_data[i]+" ");
		}
		System.out.println();
*/		
		all_data = jreed.cauchyRecover(k, m, w, psize, bitMatrix, all_code, all_data, erasures);
		
/*		System.out.println("recovered data");
		for(i=0; i < k*w; ++i)
			System.out.print(all_data[i]+" ");
		System.out.println();
*/
		try {
			for(i=0; i < k*w;++i){
				if(all_data[i]!=old_data[i])
					throw new Exception();
	//			else
		//			System.out.println("Correct");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Wrong");
			System.exit(0);
		}

	}
	public static void updateCode(int value,int primaryId, int[] all_data, int[] all_code, int[] bitMatrix,int k, int m, int w,int psize){
		int[] data = new int[w]; 
		for(int i=0; i < w; ++i)
			data[i]=0;
		data[0]= value; 
		all_data[primaryId*w]=data[0]; 

		for(int c =0; c < m;++c){
			System.out.print(c+":");
			int[] element = matElement(bitMatrix,c,primaryId,k,w);
			int[] oldCode = new int[w];
			for(int l=0; l <w;++l){
				oldCode[l] = all_code[c*w+l];
			}
			System.out.print("element");
			for(int s=0; s < w*w;++s)
				System.out.print(element[s]);
			System.out.println();
			int[] code = jreed.cauchyUpdate(data, oldCode, element, psize, w);
			for(int l=0; l < w; ++l){
				all_code[c*w+l]= code[l];
				System.out.print(code[l]);
			}
			System.out.print("|");
		}
		
	}
	public static int[] matElement(int[] matrix, int codeId, int dataId, int k, int w){ 
		int[] matrixElement = new int[w*w];
		for(int i=0; i < w; ++i){
			for(int j=0; j < w;++j){
				matrixElement[i*w+j] = matrix[codeId*k*w*w+dataId*w+i*k*w+j];
			}
		}
		return matrixElement; 
	}

}
