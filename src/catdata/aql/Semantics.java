package catdata.aql;

public interface Semantics {

	public default String sample(int size) {
		return null;
	};
	
	public Kind kind();
	
	public int size();
	
	public default TypeSide<?,?> asTypeSide() {
		return (TypeSide<?,?>) this;
	}
	
	public default Graph<?,?> asGraph() {
		return (Graph<?,?>) this;
	}
	
	public default Schema<?,?,?,?,?> asSchema() {
		return (Schema<?,?,?,?,?>) this;
	}
	
	public default Constraints<?,?,?,?,?> asConstraints() {
		return (Constraints<?,?,?,?,?>) this;
	}
	
	public default Instance<?,?,?,?,?,?,?,?,?> asInstance() {
		return (Instance<?,?,?,?,?,?,?,?,?>) this;
	}
	
	public default Transform<?,?,?,?,?,?,?,?,?,?,?,?,?> asTransform() {
		return (Transform<?,?,?,?,?,?,?,?,?,?,?,?,?>) this;
	}
	
	public default Mapping<?,?,?,?,?,?,?,?> asMapping() {
		return (Mapping<?,?,?,?,?,?,?,?>) this;
	}
	
	public default Query<?,?,?,?,?,?,?,?> asQuery() {
		return (Query<?,?,?,?,?,?,?,?>) this;
	}
	
	public default Pragma asPragma() {
		return (Pragma) this;
	}
	
	public default Comment asComment() {
		return (Comment) this;
	}
	
	public default ColimitSchema<?> asSchemaColimit() {
		return (ColimitSchema<?>) this;
	}
	
	
/*
	public <Ty, En, Sym, Fk, Att> R visit(G arg, Schema<Ty, En, Sym, Fk, Att> S) throws E;
	
	public <Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> R visit(G arg, Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> I) throws E;

	public <Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2> R visit(G arg, Transform<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2> h) throws E;

	public R visit(G arg, Pragma P);
	
	public R visit(G arg, Comment P);
	
	public <Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> R visit(G arg, Query<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> Q);
	
	public <Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> R visit(G arg, Mapping<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> Q);
	
	public <N,e> R visit(G arg, DMG<N,e> T) throws E;
*/
	
}
