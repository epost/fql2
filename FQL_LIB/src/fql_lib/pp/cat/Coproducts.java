package fql_lib.pp.cat;

public interface Coproducts<O,A> {

	public O initial();
	
	public A initial(O o);
	
	public O coproduct(O o1, O o2);
	
	public A inleft(O o1, O o2);
	
	public A inright(O o1, O o2);
	
	public A match(A a1, A a2);

}
