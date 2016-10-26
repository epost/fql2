package catdata.aql.exp;

import catdata.Unit;
import catdata.aql.Algebra;
import catdata.aql.DP;
import catdata.aql.Frozen;
import catdata.aql.Instance;
import catdata.aql.Mapping;
import catdata.aql.Pragma;
import catdata.aql.Query;
import catdata.aql.Schema;
import catdata.aql.Transform;
import catdata.aql.TypeSide;

public enum Kind {

	TYPESIDE (TypeSide.class, TyExp.class, DP.class),
	SCHEMA (Schema.class, SchExp.class, DP.class),
	INSTANCE (Instance.class, InstExp.class, Algebra.class),
	MAPPING (Mapping.class, MapExp.class, Unit.class),
	TRANSFORM (Transform.class, TransExp.class, Unit.class),
	QUERY (Query.class, QueryExp.class, Frozen.class), 
	PRAGMA (Pragma.class, PragmaExp.class, Unit.class); 
	
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
		default:
			throw new RuntimeException();
		}
	}
	
	public final Class<?> literal;
	public final Class<?> exp;
	public final Class<?> semantics;
	
	private Kind(Class<?> literal, Class<?> exp, Class<?> semantics) {
		this.literal = literal;
		this.exp = exp;
		this.semantics = semantics;
	}

	public void checkExp(String k, Object o) {
		if (!exp.isInstance(o)) {
			throw new RuntimeException(k + " is not a " + this + " expression , is a " + o.getClass());
		}
	}
	
	public void checkLiteral(String k, Object o) {
		if (!literal.isInstance(o)) {
			throw new RuntimeException(k + " is not a " + this + " literal, is a " + o.getClass());
		}
	}
	
}
