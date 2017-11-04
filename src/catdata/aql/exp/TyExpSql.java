package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import catdata.Pair;
import catdata.aql.Kind;
import catdata.aql.SqlTypeSide;
import catdata.aql.TypeSide;
import catdata.aql.exp.TyExpRaw.Sym;
import catdata.aql.exp.TyExpRaw.Ty;

public final class TyExpSql extends TyExp<Ty,Sym> {
	
	@Override
	public Collection<Pair<String, Kind>> deps() {
		return Collections.emptyList();
	}
	@Override
	public Map<String, String> options() {
		return Collections.emptyMap();
	}
		
	@Override
	public TypeSide<Ty,Sym> eval(AqlEnv env) {
		return new SqlTypeSide(env.defaults);
	}

	@Override
	public String toString() {
		return "sql";
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		return (o != null && o instanceof TyExpSql);
	}
	
}