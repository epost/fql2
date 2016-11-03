package catdata;

@SuppressWarnings("serial")
public class InvisibleException extends RuntimeException {
	
	public final Exception ex;
	
	public InvisibleException(Exception ex) {
		this.ex = ex;
	}
	
	public InvisibleException(String msg) {
		this.ex = new RuntimeException(msg);
	}

	@Override
	public String toString() {
		return ex.toString();
	}

	@Override
	public void printStackTrace() { }

}
