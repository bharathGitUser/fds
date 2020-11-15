





import java.io.Serializable;


public class JavaReedInterface implements Serializable{
    public native int[] update(int numStructures , int numBackups, int w, int[]coding_int,int old_int_value, int new_int_value,int position);
    public native int updateSingleCode(int numStructures , int numBackups, int w, int code_int,int code_index,int old_int_value, int new_int_value,int position,int matElement);
    public native int[] recover(int numStructures , int numBackups, int w, int[] codeWords,int[] dataWords,int[] erasures);
    public native int[] addCodeWords(int numBackups,int[] codeWordsCurrent,int[] codeWordsNext);
    public native int addCodes(int codeWordCurrent,int codeWordNext);
    public native int[] encode(int numStructures , int numBackups, int w, int[] codeWords,int[] dataWords);
    public static native int[][] genRsMatrix(int numStructures , int numBackups, int w);
    static {
        System.loadLibrary("CReedInterface");
    }
}
