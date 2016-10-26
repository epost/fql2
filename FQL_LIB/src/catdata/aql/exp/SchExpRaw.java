package catdata.aql.exp;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Pair;
import catdata.Quad;
import catdata.Triple;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.Collage;
import catdata.aql.Ctx;
import catdata.aql.RawTerm;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.TypeSide;
import catdata.aql.Var;

public final class SchExpRaw extends SchExp<Object,Object,Object,Object,Object>  {
	
	@Override
	public Collection<Pair<String, Kind>> deps() {
		Set<Pair<String, Kind>> ret = new HashSet<>();
		ret.addAll(imports.stream().map(x -> new Pair<>(x, Kind.SCHEMA)).collect(Collectors.toList()));
		ret.addAll(typeSide.deps());
		return ret;
	}

	//TODO: printing of contexts broken when conitain choices
	
	@Override
	public Schema<Object, Object, Object, Object, Object> eval(AqlEnv env) {
		TypeSide<Object, Object> ts = typeSide.eval(env);
		Collage<Object, Object, Object, Object, Object, Void, Void> col = new Collage<>(ts);
		
		Set<Triple<Pair<Var, Object>, Term<Object, Object, Object, Object, Object, Void, Void>, Term<Object, Object, Object, Object, Object, Void, Void>>> eqs0 = new HashSet<>();

		for (String k : imports) {
			Schema<Object, Object, Object, Object, Object> v = env.getSchema(k);
			col.ens.addAll(v.ens);
			Util.putAllSafely(col.fks, v.fks);
			Util.putAllSafely(col.atts, v.atts);
			eqs0.addAll(v.eqs);
		}
		
		col.ens.addAll(ens);
		
		Util.putAllSafely(col.fks, Util.toMapSafely(fks));
		Util.putAllSafely(col.atts, Util.toMapSafely(atts));
	
		for (Quad<String, Object, RawTerm, RawTerm> eq : t_eqs) {
				Map<String, Chc<Object, Object>> ctx = Util.singMap(eq.first, eq.second == null ? null : Chc.inLeft(eq.second));
				
				Triple<Ctx<String,Chc<Object,Object>>,Term<Object,Object,Object,Object,Object,Void,Void>,Term<Object,Object,Object,Object,Object,Void,Void>>
				eq0 = RawTerm.infer1(ctx, eq.third, eq.fourth, col);
				
				if (eq0.first.size() != 1) {
					throw new RuntimeException("In " + eq.third + " = " + eq.fourth + ", there are either unbound variables or java primitives (without annotations), neither of which are not allowed"); 
				}

				Chc<Object, Object> v = eq0.first.get(eq.first);
				if (v.left) {
					throw new RuntimeException("In " + eq.third + " = " + eq.fourth + ", variable " + eq.first + " has type " + v.l + " which is not an entity");
				}
				Object t = v.r;
			
				eqs0.add(new Triple<Pair<Var, Object>, Term<Object, Object, Object, Object, Object, Void, Void>, Term<Object, Object, Object, Object, Object, Void, Void>>
				(new Pair<>(new Var(eq.first), t), eq0.second, eq0.third));
		}
		
		for (Pair<List<Object>, List<Object>> eq : p_eqs) {
			String vv = "v";
			Var var = new Var(vv);
			
			Map<String, Chc<Object, Object>> ctx = Util.singMap(vv, null);
			
			RawTerm lhs = RawTerm.fold(col.fks.keySet(), col.ens, eq.first, vv);
			RawTerm rhs = RawTerm.fold(col.fks.keySet(), col.ens, eq.second,vv);
			
			Triple<Ctx<String,Chc<Object,Object>>,Term<Object,Object,Object,Object,Object,Void,Void>,Term<Object,Object,Object,Object,Object,Void,Void>>
			eq0 = RawTerm.infer1(ctx, lhs, rhs, col);
		
			Chc<Object, Object> v = eq0.first.get(vv);
			if (v.left) {
				throw new RuntimeException("In " + Util.sep(eq.first, ".") + " = " + Util.sep(eq.second, ".") + ", the equations source " + eq.first + " is type " + v.l + " which is not an entity");
			}
			Object t = v.r;
		
			if (eq0.first.size() != 1) {
				throw new RuntimeException("In " + Util.sep(eq.first, ".") + " = " + Util.sep(eq.second, ".") + ", java constants cannot be used ");
			}

			eqs0.add(new Triple<Pair<Var, Object>, Term<Object, Object, Object, Object, Object, Void, Void>, Term<Object, Object, Object, Object, Object, Void, Void>>
			(new Pair<>(var, t), eq0.second, eq0.third));
	}
		
		AqlOptions strat = new AqlOptions(Util.toMapSafely(options), col);
		
		Schema<Object, Object, Object, Object, Object> ret = new Schema<>(ts, col.ens, col.atts, col.fks, eqs0, strat);
		return ret; 
		
	}

	
	
	//could be ? - don't think so
	public final TyExp<Object,Object> typeSide;
	
	public final List<String> imports;
	
	public final List<Object> ens;

	public final List<Pair<Object, Pair<Object, Object>>> fks;		
	public final List<Pair<List<Object>, List<Object>>> p_eqs;

	public final List<Pair<Object, Pair<Object, Object>>> atts; 
	public final List<Quad<String, Object, RawTerm, RawTerm>> t_eqs;
	
	public final List<Pair<String, String>> options;

	@Override
	public String toString() {
		return "SchExpRaw [typeSide=" + typeSide + ", imports=" + imports + ", ens=" + ens + ", fks=" + fks + ", p_eqs=" + p_eqs + ", atts=" + atts + ", t_eqs=" + t_eqs + ", options=" + options + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((atts == null) ? 0 : atts.hashCode());
		result = prime * result + ((ens == null) ? 0 : ens.hashCode());
		result = prime * result + ((fks == null) ? 0 : fks.hashCode());
		result = prime * result + ((imports == null) ? 0 : imports.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		result = prime * result + ((p_eqs == null) ? 0 : p_eqs.hashCode());
		result = prime * result + ((t_eqs == null) ? 0 : t_eqs.hashCode());
		result = prime * result + ((typeSide == null) ? 0 : typeSide.hashCode());
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
		SchExpRaw other = (SchExpRaw) obj;
		if (atts == null) {
			if (other.atts != null)
				return false;
		} else if (!atts.equals(other.atts))
			return false;
		if (ens == null) {
			if (other.ens != null)
				return false;
		} else if (!ens.equals(other.ens))
			return false;
		if (fks == null) {
			if (other.fks != null)
				return false;
		} else if (!fks.equals(other.fks))
			return false;
		if (imports == null) {
			if (other.imports != null)
				return false;
		} else if (!imports.equals(other.imports))
			return false;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		if (p_eqs == null) {
			if (other.p_eqs != null)
				return false;
		} else if (!p_eqs.equals(other.p_eqs))
			return false;
		if (t_eqs == null) {
			if (other.t_eqs != null)
				return false;
		} else if (!t_eqs.equals(other.t_eqs))
			return false;
		if (typeSide == null) {
			if (other.typeSide != null)
				return false;
		} else if (!typeSide.equals(other.typeSide))
			return false;
		return true;
	}

	//type safe by covariance of read only collections
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SchExpRaw(TyExp<?, ?> typeSide, List<String> imports, List<String> ens, List<Pair<String, Pair<String, String>>> fks, List<Pair<List<String>, List<String>>> p_eqs, List<Pair<String, Pair<String, String>>> atts, List<Quad<String, String, RawTerm, RawTerm>> t_eqs, List<Pair<String, String>> options) {
		this.typeSide = (TyExp<Object, Object>) typeSide;
		this.imports = imports;
		this.ens = new LinkedList<>(ens);
		this.fks = new LinkedList(fks);
		this.p_eqs = new LinkedList(p_eqs);
		this.atts = new LinkedList(atts);
		this.t_eqs = new LinkedList(t_eqs);
		this.options = options;
	} 
	
	
	

}