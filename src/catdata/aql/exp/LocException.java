
package catdata.aql.exp;

public class LocException extends RuntimeException {

	public final int loc;

	public LocException(int loc, String msg) {
		super(msg);
		this.loc = loc;
	}
			
}
