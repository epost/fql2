package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import catdata.Pair;
import catdata.Util;
import catdata.aql.Kind;
import catdata.aql.Transform;
import catdata.aql.fdm.ComposeTransform;

public class TransExpCompose<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2,Gen3,Sk3,X3,Y3> 
	extends TransExp<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen3,Sk3,X1,Y1,X3,Y3> {

	public final TransExp<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2> t1;
	public final TransExp<Ty,En,Sym,Fk,Att,Gen2,Sk2,Gen3,Sk3,X2,Y2,X3,Y3> t2;
	
	@Override
	public Map<String, String> options() {
		return Collections.emptyMap();
	}
	public TransExpCompose(TransExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> t1, TransExp<Ty, En, Sym, Fk, Att, Gen2, Sk2, Gen3, Sk3, X2, Y2, X3, Y3> t2) {
		this.t1 = t1;
		this.t2 = t2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((t1 == null) ? 0 : t1.hashCode());
		result = prime * result + ((t2 == null) ? 0 : t2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TransExpCompose<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> other = (TransExpCompose<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) obj;
		if (t1 == null) {
			if (other.t1 != null)
				return false;
		} else if (!t1.equals(other.t1))
			return false;
		if (t2 == null) {
			if (other.t2 != null)
				return false;
		} else if (!t2.equals(other.t2))
			return false;
		return true;
	}

	@Override
	public Pair<InstExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, X1, Y1>, InstExp<Ty, En, Sym, Fk, Att, Gen3, Sk3, X3, Y3>> type(AqlTyping G) {
		Pair<InstExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, X1, Y1>, InstExp<Ty, En, Sym, Fk, Att, Gen2, Sk2, X2, Y2>> l = t1.type(G);
		Pair<InstExp<Ty, En, Sym, Fk, Att, Gen2, Sk2, X2, Y2>, InstExp<Ty, En, Sym, Fk, Att, Gen3, Sk3, X3, Y3>> r = t2.type(G);
		if (!l.second.equals(r.first)) {
			throw new RuntimeException("Anomaly: in compose transform, dst of t1 is \n\n" + l.second +" \n\n but src of t2 is \n\n" + r.first);
		}
		return new Pair<>(l.first, r.second);
	}

	

	@Override
	public Transform<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen3, Sk3, X1, Y1, X3, Y3> eval(AqlEnv env) {
		return new ComposeTransform<>(t1.eval(env), t2.eval(env));
	}

	@Override
	public String toString() {
		return "(" + t1 + " ; " + t2 + ")"; 
	}

	@Override
	public Collection<Pair<String, Kind>> deps() {
		return Util.union(t1.deps(), t2.deps());
	}

	
	
}