package catdata;

@SuppressWarnings("serial")
public class RuntimeInterruptedException extends RuntimeException {
	
	public final Exception ex;
	
	public RuntimeInterruptedException(InterruptedException ie) {
		ex = ie;
	}
	
	public RuntimeInterruptedException(ThreadDeath ie) {
		ex = new RuntimeException("Thread death");
	}
	/*public RuntimeInterruptedException(Exception ex) {
		this.ex = ex;
	}
	
	public RuntimeInterruptedException(String msg) {
		this.ex = new RuntimeException(msg);
	} */

	@Override
	public String toString() {
		return ex.toString();
	}

	@Override
	public void printStackTrace() { }

}
