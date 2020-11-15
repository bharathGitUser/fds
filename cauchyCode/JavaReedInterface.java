




import java.io.Serializable;


public class JavaReedInterface implements Serializable{
   
    //cauchy RS
    public native int[] encode(int numStructures , int numBackups, int w, int[] codeWords,int[] dataWords);
    public native int[] genCauchyMatrix(int numStructures , int numBackups, int w);
    public native int[] cauchyUpdate(int[] diff,int[] oldClode, int[] matrixElement, int psize, int w);
    public native int[] cauchyRecover(int numStructures , int numBackups, int w, int psize, int[] matrix,int[] codeWords,int[] dataWords,int[] erasures);
    static {
        System.loadLibrary("CReedInterface");
    }
}
