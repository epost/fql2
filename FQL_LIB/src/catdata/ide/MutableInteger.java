package catdata.ide;

public  class MutableInteger {
	int i;

	public MutableInteger(int i) {
		this.i = i;
	}

	public String pp() {
		return Integer.toString(i++);
	}  
}