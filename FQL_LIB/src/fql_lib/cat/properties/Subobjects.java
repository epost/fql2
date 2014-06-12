package fql_lib.cat.properties;

public interface Subobjects<O,A> {

	public O prop();
	
	public A tru();
	
	public A fals();
	
	public A chr(A a);
	
	public A kernel(A a);
	
}
