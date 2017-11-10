package catdata.aql;

public interface SemanticsVisitor<R,G,E extends Throwable> {
	
	public <T,C> R visit(G arg, TypeSide<T,C> T) throws E;

	public <N> R visit(G arg, ColimitSchema<N> S) throws E;

	public <Ty, En, Sym, Fk, Att> R visit(G arg, Schema<Ty, En, Sym, Fk, Att> S) throws E;
	
	public <Ty, En, Sym, Fk, Att> R visit(G arg, Constraints<Ty, En, Sym, Fk, Att> S) throws E;
	
	public <Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> R visit(G arg, Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> I) throws E;

	public <Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2> R visit(G arg, Transform<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2> h) throws E;

	public R visit(G arg, Pragma P) throws E;
	
	public R visit(G arg, Comment C) throws E;
	
	public <Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> R visit(G arg, Query<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> Q) throws E;
	
	public <Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> R visit(G arg, Mapping<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> M) throws E;
	
	public <N,e> R visit(G arg, Graph<N,e> G) throws E;
	
	public default R visit(G arg, Semantics o) throws E {
		switch (o.kind()) {
		case COMMENT:
			return visit(arg, o.asComment());
		case GRAPH:
			return visit(arg, o.asGraph());
		case INSTANCE:
			return visit(arg, o.asInstance());
		case MAPPING:
			return visit(arg, o.asMapping());
		case PRAGMA:
			return visit(arg, o.asPragma());
		case QUERY:
			return visit(arg, o.asQuery());
		case SCHEMA:
			return visit(arg, o.asSchema());
		case TRANSFORM:
			return visit(arg, o.asTransform());
		case TYPESIDE:
			return visit(arg, o.asTypeSide());
		case SCHEMA_COLIMIT:
			return visit(arg, o.asSchemaColimit());
		case CONSTRAINTS:
			return visit(arg, o.asConstraints());
		default:
			break;		
		}
		throw new RuntimeException("Anomaly: please report");
	}
	
}
