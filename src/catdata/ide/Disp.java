package catdata.ide;

public interface Disp {
	
	public void close();
	
	public default Throwable exn() {
		return null;
	}

}
