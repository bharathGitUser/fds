




//import JavaReedInterface;

import java.io.Serializable;





public class Fusion implements Serializable{
	static int numStructures;
	static int maxFaults;
	static JavaReedInterface rsInterface;
	static int w,psize;
	static int[] rsArray;

	public static void initialize(int numStructures, int maxFaults){
		Fusion.numStructures = numStructures;
		Fusion.maxFaults = maxFaults;
		rsInterface = new JavaReedInterface();
		w = 5;
		psize=4; 
        rsArray = rsInterface.genCauchyMatrix(numStructures, maxFaults, w); 
	}
	
	public static int getW(){
		return w; 
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

	public static int[] getRecoveredData(int[] codeWords,int[] dataWords,int[] erasures) throws InterfaceException{
		long start = Util.getCurrentTime();
		int[] recoveredData = rsInterface.cauchyRecover(numStructures, maxFaults, w, psize, rsArray, codeWords, dataWords, erasures);
		Util.deCodingTime = Util.deCodingTime+Util.getCurrentTime()-start;
		return recoveredData;
	}
	
	public static int[] encodeData(int[] all_data) throws InterfaceException{
		int[] all_code = new int[maxFaults*w]; 
		for(int i=0; i <numStructures; ++i){
			//System.out.println("-------------------");
			int[] data = new int[w];
			for(int j=0; j < w;++j){
				data[j]= all_data[i*w+j];
			}
			for(int c =0; c < maxFaults;++c){
				int[] element = matElement(rsArray,c,i,numStructures,w);
				int[] oldCode = new int[w];
				for(int l=0; l <w;++l){
					oldCode[l] = all_code[c*w+l];
				}
				int[] code = rsInterface.cauchyUpdate(data, oldCode, element, psize, w);
				for(int l=0; l < w; ++l){
					all_code[c*w+l]= code[l];
				}
			}
		}
		return all_code; 
	}

	public static int[] getUpdatedCode(int[] oldCode,int codeIndex, int[] oldValue,int[] newValue,int sourceId) throws InterfaceException{
		long start = Util.getCurrentTime();
		int[] element = matElement(rsArray,codeIndex,sourceId,numStructures,w);
		int[] diff = new int[w];
		for(int i=0; i < w;++i)
			diff[i]=oldValue[i]^newValue[i];
		if(Util.debugFlag){
			System.out.print("diff:");
			for(int i=0; i < Fusion.getW();++i)
				System.out.print(diff[i]);
			System.out.println();
			System.out.print("old code:");
			for(int i=0; i < Fusion.getW();++i)
				System.out.print(oldCode[i]);
			System.out.println();
			System.out.print("Element:");

			for(int i=0; i < w*w;++i)
				System.out.print(element[i]);
			System.out.println();
		}
		int[] code =rsInterface.cauchyUpdate(diff, oldCode, element, psize, w);
		Util.enCodingTime = Util.enCodingTime+Util.getCurrentTime()-start;
	    return code; 
	}
	
	public static void main(String[] args) {
		System.out.println("trying to muck with fusion");
		Fusion.initialize(1, 1);  
	}
	
}
