import java.io.Serializable;

public class RecoveryObject implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public int[] getPositions() {
		return positions;
	}
	public void setPositions(int[] positions) {
		this.positions = positions;
	}
	private int value; 
	private int[] positions; 
	public RecoveryObject(int value, int[] positions){
		this.value = value;
		this.positions = positions; 
	}
	

}
