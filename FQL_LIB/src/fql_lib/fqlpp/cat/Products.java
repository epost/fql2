package fql_lib.fqlpp.cat;

public interface Products<O,A> {

	public O terminal();
	
	public A terminal(O o);
	
	public O product(O o1, O o2);
	
	public A first(O o1, O o2);
	
	public A second(O o1, O o2);
	
	public A pair(A a1, A a2);
	
}
