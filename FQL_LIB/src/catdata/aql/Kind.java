package catdata.aql;

import catdata.Unit;
import catdata.aql.Exp.InstExp;
import catdata.aql.Exp.MapExp;
import catdata.aql.Exp.PragmaExp;
import catdata.aql.Exp.QueryExp;
import catdata.aql.Exp.SchExp;
import catdata.aql.Exp.TransExp;
import catdata.aql.Exp.TyExp;

public enum Kind {

	TYPESIDE (TypeSide.class, TyExp.class, DP.class),
	SCHEMA (Schema.class, SchExp.class, DP.class),
	INSTANCE (Instance.class, InstExp.class, Algebra.class),
	MAPPING (Instance.class, MapExp.class, Unit.class),
	TRANSFORM (Transform.class, TransExp.class, Unit.class),
	QUERY (Query.class, QueryExp.class, Frozen.class), 
	PRAGMA (Pragma.class, PragmaExp.class, Unit.class); 
	
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
