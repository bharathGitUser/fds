





//import JavaReedInterface;


import java.io.Serializable;





public class Fusion implements Serializable{
	static int numStructures;
	static int maxFaults;
	static JavaReedInterface rsInterface;
	static int galoisW;
	static int[] rsArray;

	public static void initialize(int numStructures, int maxFaults){
		Fusion.numStructures = numStructures;
		Fusion.maxFaults = maxFaults;
		rsInterface = new JavaReedInterface();
		galoisW = 16;
        rsArray = new int[numStructures*maxFaults];
        int[][] matrix = JavaReedInterface.genRsMatrix(numStructures, maxFaults, galoisW);
        for(int m=0; m < Util.cauchyW;++m){//doing this to simulate the fact that in cauchy rs we encode 5 packets and here we only encode one.
	        for (int i = 0; i < numStructures; i++) {
	            for (int j = 0; j < maxFaults; j++) {
	            	rsArray[i*maxFaults+j]= matrix[i][j];
	            }
	            
	        }
        }
        		
	}
	public static int[] getUpdatedCodeWords(int[] codeWords,int oldValue,int newValue,int position) throws InterfaceException{

		long start = Util.getCurrentTime(); 
		//it has to be a valid index
//		if(position >= maxFaults) throw new InterfaceException();
		int[] newCode= rsInterface.update(numStructures, maxFaults, galoisW, codeWords, oldValue, newValue, position);
		for(int i=0; i < Util.cauchyW-1;++i)
			newCode= rsInterface.update(numStructures, maxFaults, galoisW, codeWords, oldValue, newValue, position);
		Util.encodingTime = Util.getCurrentTime()-start + Util.encodingTime; 
		return newCode; 
	}
	
	public static int[] getRecoveredData(int[] codeWords,int[] dataWords,int[] erasures) throws InterfaceException{
	
		long start = Util.getCurrentTime(); 
		int[] recovered = rsInterface.recover(numStructures, maxFaults, galoisW,codeWords,dataWords,erasures);	
		for(int i=0; i < Util.cauchyW-1;++i)
			recovered = rsInterface.recover(numStructures, maxFaults, galoisW,codeWords,dataWords,erasures);
		Util.decodingTime = Util.getCurrentTime()-start + Util.decodingTime; 
		return recovered; 
	}
	
	public static int[] encodeData(int[] codeWords,int[] dataWords) throws InterfaceException{
		long start = Util.getCurrentTime(); 
		int[] dat = rsInterface.encode(numStructures, maxFaults, galoisW,codeWords,dataWords);
		for(int i=0; i < Util.cauchyW-1;++i)
			dat = rsInterface.encode(numStructures, maxFaults, galoisW,codeWords,dataWords);
		Util.encodingTime = Util.getCurrentTime()-start + Util.encodingTime; 
		return dat;
	}
	public static int[] getAddedCodeWords(int[] codeWordsCurrent,int[] codeWordsNext){
		long start = Util.getCurrentTime(); 
		int[] dat = rsInterface.addCodeWords(maxFaults,codeWordsCurrent,codeWordsNext);
		for(int i=0; i < Util.cauchyW-1;++i)
			dat = rsInterface.addCodeWords(maxFaults,codeWordsCurrent,codeWordsNext);
		Util.encodingTime = Util.getCurrentTime()-start + Util.encodingTime; 
		return dat; 
	}

	public static int getAddedCodes(int code1,int code2){
		int addedCode=0; 
		for(int i=0; i < Util.cauchyW;++i)
			addedCode = rsInterface.addCodes(code1,code2);
		return addedCode;
	}

	public static int getUpdatedCode(int code,int codeIndex, int oldValue,int newValue,int sourceId) throws InterfaceException{
		long start = Util.getCurrentTime();
		int newCode=0; 
		for(int i=0; i < Util.cauchyW;++i){ 
			int matElement = rsArray[codeIndex*numStructures+sourceId];
		    newCode = rsInterface.updateSingleCode(numStructures , maxFaults, galoisW, code,codeIndex,oldValue, newValue,sourceId,matElement);
		}
		Util.encodingTime = Util.getCurrentTime()-start + Util.encodingTime; 
		return newCode;
	}
	
	public static void main(String[] args) {
		System.out.println("trying to muck with fusion");
		Fusion.initialize(1, 1);  
	}
	
}
