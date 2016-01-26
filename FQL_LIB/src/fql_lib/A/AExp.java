package fql_lib.A;


public abstract class AExp {

	public abstract <R, E> R accept(E env, AExpVisitor<R, E> v);

	public interface AExpVisitor<R, E> {
		
	}
		
}
