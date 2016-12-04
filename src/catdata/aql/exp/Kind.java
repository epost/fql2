package catdata.aql.exp;

import catdata.aql.Instance;
import catdata.aql.Mapping;
import catdata.aql.Pragma;
import catdata.aql.Query;
import catdata.aql.Schema;
import catdata.aql.Transform;
import catdata.aql.TypeSide;
import catdata.graph.DMG;

public enum Kind {

	TYPESIDE (TypeSide.class, TyExp.class),
	SCHEMA (Schema.class, SchExp.class),
	INSTANCE (Instance.class, InstExp.class),
	MAPPING (Mapping.class, MapExp.class),
	TRANSFORM (Transform.class, TransExp.class),
	QUERY (Query.class, QueryExp.class), 
	PRAGMA (Pragma.class, PragmaExp.class), 
	GRAPH (DMG.class, GraphExp.class); //
	
	@Override
	public String toString() {
		switch (this) {
		case INSTANCE:
			return "instance";
		case MAPPING:
			return "mapping";
		case PRAGMA:
			return "pragma";
		case QUERY:
			return "query";
		case SCHEMA:
			return "schema";
		case TRANSFORM:
			return "transform";
		case TYPESIDE:
			return "typeside";
		case GRAPH:
			return "graph";
		default:
			throw new RuntimeException();
		}
		
		
	}
	
	public final Class<?> literal;
	public final Class<?> exp;
	
	private Kind(Class<?> literal, Class<?> exp) {
		this.literal = literal;
		this.exp = exp;
	} 

	public void checkExp(String k, Object o) {
		if (!exp.isInstance(o)) {
			throw new RuntimeException(k + " is not a " + this + " expression , is a " + o.getClass());
		}
	}
	
	public <X> void checkLiteral(X k, Object o) {
		if (!literal.isInstance(o)) {
			throw new RuntimeException(k + " is not a " + this + " literal, is a " + o.getClass());
		}
	}
	
}
