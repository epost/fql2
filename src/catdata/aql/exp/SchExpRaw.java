package catdata.aql.exp;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Program;
import catdata.Quad;
import catdata.Triple;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.AqlProver;
import catdata.aql.AqlProver.ProverName;
import catdata.aql.Collage;
import catdata.aql.Eq;
import catdata.aql.Kind;
import catdata.aql.RawTerm;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.TypeSide;
import catdata.aql.Var;
import catdata.aql.exp.TyExpRaw.Sym;
import catdata.aql.exp.TyExpRaw.Ty;

public final class SchExpRaw extends SchExp<Ty,String,Sym,String,String> implements Raw {
	
	
	
	
	public SchExp<Ty,String,Sym,String,String> resolve(AqlTyping G, Program<Exp<?>> prog) {
		return this;
	}

	@Override
	public Collection<Pair<String, Kind>> deps() {
		Set<Pair<String, Kind>> ret = new HashSet<>();
		ret.addAll(imports.stream().map(x -> new Pair<>(x, Kind.SCHEMA)).collect(Collectors.toList()));
		ret.addAll(typeSide.deps());
		return ret;
	}
	
	@Override
	public Map<String, String> options() {
		return options;
	}

	//TODO: aql printing of contexts broken when conitain choices
	
	@SuppressWarnings("unused")
	@Override
	public synchronized Schema<Ty, String, Sym, String, String> eval(AqlEnv env) {
		TypeSide<Ty, Sym> ts = typeSide.eval(env);
		Collage<Ty, String, Sym, String, String, Void, Void> col = new Collage<>(ts.collage());
		
		Set<Triple<Pair<Var, String>, Term<Ty, String, Sym, String, String, Void, Void>, Term<Ty, String, Sym, String, String, Void, Void>>> eqs0 = new HashSet<>();

		for (String k : imports) {
			@SuppressWarnings("unchecked")
			Schema<Ty, String, Sym, String, String> v = env.defs.schs.get(k);
			col.ens.addAll(v.ens);
			col.fks.putAll(v.fks.map);
			col.atts.putAll(v.atts.map);
			eqs0.addAll(v.eqs);
		}
		
		col.ens.addAll(ens);
		
		col.fks.putAll(Util.toMapSafely(fks));
		col.atts.putAll(Util.toMapSafely(atts));
		
		for (Quad<String, String, RawTerm, RawTerm> eq : t_eqs) {
			try {
				Map<String, Chc<Ty, String>> ctx = Util.singMap(eq.first, eq.second == null ? null : Chc.inRight(eq.second));
				
				Triple<Ctx<Var,Chc<Ty,String>>,Term<Ty,String,Sym,String,String,Void,Void>,Term<Ty,String,Sym,String,String,Void,Void>>
				eq0 = RawTerm.infer1x(ctx, eq.third, eq.fourth, null, col, "", ts.js).first3();
				
				Chc<Ty, String> v = eq0.first.get(new Var(eq.first));
				if (v.left) {
					throw new RuntimeException(eq.first + " has type " + v.l + " which is not an entity");
				}
				String t = v.r;
			
				eqs0.add(new Triple<>(new Pair<>(new Var(eq.first), t), eq0.second, eq0.third));
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				throw new LocException(find("obs equations", eq), "In equation " + eq.third + " = " + eq.fourth + ", " + ex.getMessage());
			}
		}
		
		for (Pair<List<String>, List<String>> eq : p_eqs) {
			try {
				String vv = "v";
				Var var = new Var(vv);
				
				Map<String, Chc<Ty, String>> ctx = Util.singMap(vv, null);
				
				RawTerm lhs = RawTerm.fold(col.fks.keySet(), col.ens, eq.first, vv);
				RawTerm rhs = RawTerm.fold(col.fks.keySet(), col.ens, eq.second,vv);
				
				Triple<Ctx<Var,Chc<Ty,String>>,Term<Ty,String,Sym,String,String,Void,Void>,Term<Ty,String,Sym,String,String,Void,Void>>
				eq0 = RawTerm.infer1x(ctx, lhs, rhs, null, col, "", ts.js).first3();
			
				Chc<Ty, String> v = eq0.first.get(var);
				if (v.left) {
					throw new RuntimeException("the equation's source " + eq.first + " is type " + v.l + " which is not an entity");
				}
				String t = v.r;
			
				if (eq0.first.size() != 1) {
					throw new RuntimeException("java constants cannot be used ");
				}
	
				eqs0.add(new Triple<>(new Pair<>(var, t), eq0.second, eq0.third));
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				throw new LocException(find("path equations", eq), "In equation " + Util.sep(eq.first, ".") + " = " + Util.sep(eq.second, ".") + ", " + ex.getMessage());
			}
		}
		for (Triple<Pair<Var, String>, Term<Ty, String, Sym, String, String, Void, Void>, Term<Ty, String, Sym, String, String, Void, Void>> eq : eqs0) {
			col.eqs.add(new Eq<>(new Ctx<>(eq.first).inRight(), eq.second, eq.third));
		}
		
		AqlOptions strat = new AqlOptions(options, col, env.defaults);
		
		AqlOptions s = new AqlOptions(Util.singMap(AqlOption.prover.toString(), ProverName.fail.toString()), col, env.defaults);
		
		//forces type checking before prover construction
		new Schema<>(ts, col.ens, col.atts.map, col.fks.map, eqs0, AqlProver.create(s, col, ts.js), false);
		
		Schema<Ty, String, Sym, String, String> ret = new Schema<>(ts, col.ens, col.atts.map, col.fks.map, eqs0, AqlProver.create(strat, col, ts.js), !((Boolean)strat.getOrDefault(AqlOption.allow_java_eqs_unsafe)));
		return ret; 
		
	}

	
	
	public final TyExp<Ty,Sym> typeSide;
	
	public final Set<String> imports;
	
	public final Set<String> ens;

	public final Set<Pair<String, Pair<String, String>>> fks;
	public final Set<Pair<List<String>, List<String>>> p_eqs;

	public final Set<Pair<String, Pair<String, Ty>>> atts;
	public final Set<Quad<String, String, RawTerm, RawTerm>> t_eqs;
	
	public final Map<String, String> options;
	
		

	private String toString;

	private final Ctx<String, List<InteriorLabel<Object>>> raw = new Ctx<>();
	
	@Override
	public synchronized String toString() {
		if (toString != null) {
			return toString;
		}
		toString = "";
		
		if (!imports.isEmpty()) {
			toString += "\timports";
			toString += "\n\t\t" + Util.sep(imports, " ") + "\n";
		}
		
		if (!ens.isEmpty()) {
			toString += "\tentities";
			toString += "\n\t\t" + Util.sep(Util.alphabetical(ens), " ") + "\n";
		}
		
		List<String> temp = new LinkedList<>();
		
		if (!fks.isEmpty()) {
			toString += "\tforeign_keys";
			temp = new LinkedList<>();
			for (Pair<String, Pair<String, String>> sym : Util.alphabetical(fks)) {
				temp.add(sym.first + " : " + sym.second.first + " -> " + sym.second.second);
			}
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		
		if (!p_eqs.isEmpty()) {
			toString += "\tpath_equations";
			temp = new LinkedList<>();
			for (Pair<List<String>, List<String>> sym : Util.alphabetical(p_eqs)) {
				temp.add(Util.sep(sym.first, ".") + " = " + Util.sep(sym.second, ".") );
			}
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		
		if (!atts.isEmpty()) {
			toString += "\tattributes";
			temp = new LinkedList<>();
			for (Pair<String, Pair<String, Ty>> sym : Util.alphabetical((atts))) {
				temp.add(sym.first + " : " + sym.second.first + " -> " + sym.second.second);
			}
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		
		if (!t_eqs.isEmpty()) {
			toString += "\tobservation_equations";
			temp = new LinkedList<>();
			for (Quad<String, String, RawTerm, RawTerm> sym : Util.alphabetical(t_eqs)) {
				temp.add("forall " + sym.first + ". " + sym.third + " = " + sym.fourth);
			}
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		
		if (!options.isEmpty()) {
			toString += "\toptions";
			temp = new LinkedList<>();
			for (Entry<String, String> sym : options.entrySet()) {
				temp.add(sym.getKey() + " = " + sym.getValue());
			}
			
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		
		return "literal : " + typeSide + " {\n" + toString + "}";
	} 

	@Override
	public int hashCode() {
		int prime = 31;
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

	public SchExpRaw(TyExp<Ty, Sym> typeSide, List<LocStr> imports, List<LocStr> ens, List<Pair<LocStr, Pair<String, String>>> fks, List<Pair<Integer, Pair<List<String>, List<String>>>> list, List<Pair<LocStr, Pair<String, String>>> atts, List<Pair<Integer, Quad<String, String, RawTerm, RawTerm>>> list2, List<Pair<String, String>> options) {
		this.typeSide = typeSide;
		this.imports = LocStr.set1(imports);
		this.ens = LocStr.set1(ens);
		this.fks = LocStr.set2(fks);
		this.p_eqs = LocStr.proj2(list);
		this.atts = LocStr.set2x(atts, x ->  new Ty(x));
		this.t_eqs = LocStr.proj2(list2);
		this.options = Util.toMapSafely(options);
		Util.toMapSafely(fks); //check no dups here rather than wait until eval
		Util.toMapSafely(atts);	
		
		doGuiIndexing(imports, ens, fks, list, atts, list2);
		
	}

	public void doGuiIndexing(List<LocStr> imports, List<LocStr> ens, List<Pair<LocStr, Pair<String, String>>> fks,
			List<Pair<Integer, Pair<List<String>, List<String>>>> list, List<Pair<LocStr, Pair<String, String>>> atts,
			List<Pair<Integer, Quad<String, String, RawTerm, RawTerm>>> list2) {
		List<InteriorLabel<Object>> i = InteriorLabel.imports( "imports", imports);
		raw.put("imports", i);
		List<InteriorLabel<Object>> t = InteriorLabel.imports( "entities", ens);
		raw.put("entities", t);
		
		List<InteriorLabel<Object>> f = new LinkedList<>();
		for (Pair<LocStr, Pair<String, String>> p : fks) {
			f.add(new InteriorLabel<>("foreign keys", new Triple<>(p.first.str, p.second.first, p.second.second), p.first.loc,
					x -> x.first + " : " + x.second + " -> " + x.third).conv());
		}
		raw.put("foreign keys", f);
		
		List<InteriorLabel<Object>> e = new LinkedList<>();
		for (Pair<Integer, Pair<List<String>, List<String>>> p : list) {
			e.add(new InteriorLabel<>("path equations", p.second, p.first,  x -> Util.sep(x.first, ".") + " = " + Util.sep(x.second, ".")).conv());
		}
		raw.put("path equations", e);
		
		List<InteriorLabel<Object>> jt = new LinkedList<>();
		raw.put("attributes", jt);
		for (Pair<LocStr, Pair<String, String>> p : atts) {
			jt.add(new InteriorLabel<>("attributes", new Pair<>(p.first.str, p.second), p.first.loc,  x -> x.first + " : " + x.second.first + " -> " + x.second.second).conv());
		}
		
		List<InteriorLabel<Object>> jc = new LinkedList<>();
		for (Pair<Integer, Quad<String, String, RawTerm, RawTerm>> p : list2) {
			jc.add(new InteriorLabel<>("obs equations", p.second, p.first,  x -> x.third + " = " + x.fourth).conv());
		}
		raw.put("obs equations", jc);
	} 
	
	//for easik
	public SchExpRaw(TyExp<Ty, Sym> typeSide, List<String> imports, List<String> ens, List<Pair<String, Pair<String, String>>> fks, List<Pair<List<String>, List<String>>> list, List<Pair<String, Pair<String, Ty>>> atts, List<Quad<String, String, RawTerm, RawTerm>> list2, List<Pair<String, String>> options, @SuppressWarnings("unused") Object o) {
		this.typeSide = typeSide;
		this.imports = new HashSet<>(imports);
		this.ens = new HashSet<>(ens);
		this.fks = new HashSet<>(fks);
		this.p_eqs = new HashSet<>(list);
		this.atts = new HashSet<>(atts);
		this.t_eqs = new HashSet<>(list2);
		this.options = Util.toMapSafely(options);
		Util.toMapSafely(fks); //check no dups here rather than wait until eval
		Util.toMapSafely(atts);	
	}

	@Override
	public Ctx<String, List<InteriorLabel<Object>>> raw() {
		return raw ;
	} 
	
	
	

}