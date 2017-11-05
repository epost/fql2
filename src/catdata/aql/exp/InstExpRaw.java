package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Ctx;
import catdata.Null;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Collage;
import catdata.aql.Eq;
import catdata.aql.ImportAlgebra;
import catdata.aql.Instance;
import catdata.aql.It;
import catdata.aql.It.ID;
import catdata.aql.Kind;
import catdata.aql.RawTerm;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.Var;
import catdata.aql.exp.InstExpRaw.Gen;
import catdata.aql.exp.InstExpRaw.Sk;
import catdata.aql.exp.SchExpRaw.Att;
import catdata.aql.exp.SchExpRaw.En;
import catdata.aql.exp.SchExpRaw.Fk;
import catdata.aql.exp.TyExpRaw.Sym;
import catdata.aql.exp.TyExpRaw.Ty;
import catdata.aql.fdm.InitialAlgebra;
import catdata.aql.fdm.LiteralInstance;
import catdata.aql.fdm.SaturatedInstance;


public final class InstExpRaw extends InstExp<Ty,En,Sym,Fk,Att,Gen,Sk,ID,Chc<Sk,Pair<ID,Att>>> implements Raw {

	public static class Gen implements Comparable<Gen> {
		public final String str;

		public Gen(String str) {
			Util.assertNotNull(str);
			this.str = str;
		}

		@Override
		public int hashCode() {
			return str.hashCode(); //must work with compareTo - cant use auto gen one
		} 

		@Override
		public int compareTo(Gen o) {
			if (!(o instanceof Gen)) {
				Util.anomaly();
			}
			return str.compareTo(o.str);
		}
		
		@Override
		public boolean equals(Object obj) {
			//if (!(obj instanceof Sym)) {
			//	Util.anomaly();
		//	}
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof Gen))
				return false;
			Gen other = (Gen) obj;
			if (str == null) {
				if (other.str != null)
					return false;
			} else if (!str.equals(other.str))
				return false;
			return true;
		} 

		@Override
		public String toString() {
			return str;
		}

	}
	
	public static class Sk implements Comparable<Sk> {
		public final String str;

		public Sk(String str) {
			Util.assertNotNull(str);
			this.str = str;
		}

		@Override
		public int hashCode() {
			return str.hashCode(); //must work with compareTo - cant use auto gen one
		} 

		@Override
		public int compareTo(Sk o) {
			if (!(o instanceof Sk)) {
				Util.anomaly();
			}
			return str.compareTo(o.str);
		}
		
		@Override
		public boolean equals(Object obj) {
			//if (!(obj instanceof Sym)) {
			//	Util.anomaly();
		//	}
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof Sk))
				return false;
			Sk other = (Sk) obj;
			if (str == null) {
				if (other.str != null)
					return false;
			} else if (!str.equals(other.str))
				return false;
			return true;
		} 

		@Override
		public String toString() {
			return str;
		}

	}
	
private Ctx<String, List<InteriorLabel<Object>>> raw = new Ctx<>();
	
	public Ctx<String, List<InteriorLabel<Object>>> raw() {
		return raw;
	}
	
	@Override
	public Collection<Pair<String, Kind>> deps() {
		Set<Pair<String, Kind>> ret = new HashSet<>();
		ret.addAll(schema.deps());
		ret.addAll(imports.stream().map(x -> new Pair<>(x, Kind.INSTANCE)).collect(Collectors.toList()));
		return ret;
	}
 
	public final SchExp<Ty,En,Sym,Fk,Att> schema;
	
	public final Set<String> imports;

	public final Set<Pair<String, String>> gens; 

	public final Set<Pair<RawTerm, RawTerm>> eqs;
	
	public final Map<String, String> options;
	
	@Override
	public Map<String, String> options() {
		return options;
	}

	//typesafe by covariance of read-only collections
	@SuppressWarnings("unchecked")
	public InstExpRaw(SchExp<?,?,?,?,?> schema, List<LocStr> imports, List<Pair<LocStr, String>> gens, List<Pair<Integer,Pair<RawTerm, RawTerm>>> eqs, List<Pair<String, String>> options) {
		this.schema =  (SchExp<Ty, En, Sym, Fk, Att>) schema;
		this.imports = LocStr.set1(imports);
		this.gens = LocStr.set2(gens);
		this.eqs = LocStr.proj2(eqs);
		this.options = Util.toMapSafely(options);

		List<InteriorLabel<Object>> i = InteriorLabel.imports( "imports", imports);
		raw.put("imports", i);
		
		List<InteriorLabel<Object>> e = new LinkedList<>();
		for (Pair<LocStr, String> p : gens) {
			e.add(new InteriorLabel<>("generators",new Pair<>(p.first.str, p.second), p.first.loc,  x -> x.first + " : " + x.second).conv());
		}
		raw.put("generators", e);
		
		List<InteriorLabel<Object>> xx = new LinkedList<>();
		for (Pair<Integer, Pair<RawTerm, RawTerm>> p : eqs) {
			xx.add(new InteriorLabel<>("equations", p.second, p.first,  x -> x.first + " = " + x.second).conv());
		}
		raw.put("equations", xx);
	}

	private String toString;
	
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
			
		List<String> temp = new LinkedList<>();
		
		if (!gens.isEmpty()) {
			toString += "\tgenerators";
					
			Map<String, Set<String>> n = Util.revS(Util.toMapSafely(gens));
			
			temp = new LinkedList<>();
			for (Object x : Util.alphabetical(n.keySet())) {
				temp.add(Util.sep(Util.alphabetical(n.get(x)), " ") + " : " + x);
			}
			
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		
		if (!eqs.isEmpty()) {
			toString += "\tequations";
			temp = new LinkedList<>();
			for (Pair<RawTerm, RawTerm> sym : Util.alphabetical(eqs)) {
				temp.add(sym.first + " = " + sym.second);
			}
			if (eqs.size() < 9) {
				toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
			} else {
				int step = 3;
				int longest = 32;
				for (String s : temp) {
					if (s.length() > longest) {
						longest = s.length() + 4;
					}
				}
				for (int i = 0; i < temp.size(); i += step) {
					StringBuilder sb = new StringBuilder();
					Formatter formatter = new Formatter(sb, Locale.US);
					List<String> args = new LinkedList<>();
					List<String> format = new LinkedList<>();
					for (int j = i; j < Integer.min(temp.size(), i + step); j++) {
						args.add(temp.get(j));
						format.add("%-" + longest + "s");
					}
					String x = formatter.format(Util.sep(format, ""), args.toArray(new String[0])).toString();
					formatter.close();
					toString += "\n\t\t" + x;
				}
				toString += "\n";
			}
		}
		
		if (!options.isEmpty()) {
			toString += "\toptions";
			temp = new LinkedList<>();
			for (Entry<String, String> sym : options.entrySet()) {
				temp.add(sym.getKey() + " = " + sym.getValue());
			}
			
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		
		return "literal : " + schema + " {\n" + toString + "}";
	} 


	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((eqs == null) ? 0 : eqs.hashCode());
		result = prime * result + ((gens == null) ? 0 : gens.hashCode());
		result = prime * result + ((imports == null) ? 0 : imports.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		result = prime * result + ((schema == null) ? 0 : schema.hashCode());
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
		InstExpRaw other = (InstExpRaw) obj;
		if (eqs == null) {
			if (other.eqs != null)
				return false;
		} else if (!eqs.equals(other.eqs))
			return false;
		if (gens == null) {
			if (other.gens != null)
				return false;
		} else if (!gens.equals(other.gens))
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
		if (schema == null) {
			if (other.schema != null)
				return false;
		} else if (!schema.equals(other.schema))
			return false;
		return true;
	}

	@Override
	public synchronized Instance<Ty, En, Sym, Fk, Att, Gen, Sk, ID, Chc<Sk, Pair<ID, Att>>> eval(AqlEnv env) {
		Schema<Ty, En, Sym, Fk, Att> sch = schema.eval(env);
		Collage<Ty,En,Sym,Fk,Att,Gen,Sk> col = new Collage<>(sch.collage());
		
		Set<Pair<Term<Ty,En,Sym,Fk,Att,Gen,Sk>, Term<Ty,En,Sym,Fk,Att,Gen,Sk>>> eqs0 = new HashSet<>();

		
		for (String k : imports) {
			@SuppressWarnings("unchecked")
			Instance<Ty, En, Sym, Fk, Att, Gen, Sk, ID, Chc<Sk, Pair<ID, Att>>> v = env.defs.insts.get(k);
			col.addAll(v.collage());
			eqs0.addAll(v.eqs());
		}
	
		/* for (String k : imports) {
			Instance<Ty, En, Sym, Fk, Att, Gen, Sk, ID, Chc<Sk, Pair<ID, Att>>> u = env.defs.insts.get(k);
			for (Object o : u.gens().keySet()) {
				if (!(o instanceof Gen)) {
					throw new RuntimeException("Cannot import " + o + " from " + k + " because it is not a generator");
				}
			}
			for (Object o : u.sks().keySet()) {
				if (!(o instanceof Sk)) {
					throw new RuntimeException("Cannot import " + o + " from " + k + " because it is not a labelled null");
				}
			}
			
			@SuppressWarnings("unchecked")
			Instance<Ty, En, Sym, Fk, Att, String, String, ID, Chc<String, Pair<ID, String>>> v = env.defs.insts.get(k);
			
			col.gens.putAll(v.gens().map);
			col.sks.putAll(v.sks().map);
			eqs0.addAll(v.eqs());
			col.eqs.addAll(v.eqs().stream().map(x -> new Eq<>(new Ctx<>(), x.first, x.second)).collect(Collectors.toList()));
		} */
		
		for (Pair<String, String> p : gens) {
			String gen = p.first;
			String ty = p.second;
			if (col.ens.contains(new En(ty))) {
				col.gens.put(new Gen(gen), new En(ty));
			} else if (col.tys.contains(new Ty(ty))) {
				col.sks.put(new Sk(gen), new Ty(ty));
			} else {
				throw new LocException(find("generators", p), "The sort for " + gen + ", namely " + ty + ", is not declared as a type or entity");
			}
		}
	
		for (Pair<RawTerm, RawTerm> eq : eqs) {
			try {
				Map<String, Chc<Ty, En>> ctx = Collections.emptyMap();
				
				Triple<Ctx<Var, Chc<Ty, En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>
				eq0 = RawTerm.infer1x(ctx, eq.first, eq.second, null, col, "", sch.typeSide.js).first3();
						
				eqs0.add(new Pair<>(eq0.second, eq0.third));
				col.eqs.add(new Eq<>(new Ctx<>(), eq0.second, eq0.third));
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				throw new LocException(find("equations", eq), "In equation " + eq.first + " = " + eq.second + ", " + ex.getMessage());
			}
		}

		AqlOptions strat = new AqlOptions(options, col, env.defaults);

		boolean interpret_as_algebra = (boolean) strat.getOrDefault(AqlOption.interpret_as_algebra);
	
		if (interpret_as_algebra) {
			Ctx<En, Set<Gen>> ens0x= new Ctx<>(Util.revS(col.gens.map));
			Ctx<En, Collection<Gen>> ens0 = ens0x.map(x -> (Collection<Gen>)x);
			
			if (!col.sks.isEmpty()) {
				throw new RuntimeException("Cannot have generating labelled nulls with import_as_theory");
			}
			Ctx<Ty, Collection<Null<?>>> tys0 = new Ctx<>();
			for (Ty ty : sch.typeSide.tys) {
				tys0.put(ty, new HashSet<>());
			}
			Ctx<Gen, Ctx<Fk, Gen>> fks0 = new Ctx<>();
			Ctx<Gen, Ctx<Att, Term<Ty, Void, Sym, Void, Void, Void, Null<?>>>> atts0 = new Ctx<>();
			for (Gen gen : col.gens.keySet()) {
				fks0.put(gen, new Ctx<>());
				atts0.put(gen, new Ctx<>());
			}
			for (Pair<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> e : eqs0) {
				Term<Ty, En, Sym, Fk, Att, Gen, Sk> lhs = e.first;
				Term<Ty, En, Sym, Fk, Att, Gen, Sk> rhs = e.second;
				if (rhs.gen != null && lhs.fk != null && lhs.arg.gen != null) {
					fks0.get(lhs.arg.gen).put(lhs.fk, rhs.gen);
				} else if (lhs.gen != null && rhs.fk != null && rhs.arg.gen != null) {
					fks0.get(rhs.arg.gen).put(rhs.fk, lhs.gen);					
				} else if (rhs.obj != null && lhs.att != null && lhs.arg.gen != null) {
					atts0.get(lhs.arg.gen).put(lhs.att, Term.Obj(rhs.obj, rhs.ty));					
				} else if (lhs.obj != null && rhs.att != null && rhs.arg.gen != null) {
					atts0.get(rhs.arg.gen).put(rhs.att, Term.Obj(lhs.obj, lhs.ty));										
				} else {
					throw new RuntimeException("import_as_theory not compatible with equation " + lhs + " = " + rhs + "; each equation must be of the form gen.fk=gen or gen.att=javaobject");
				}
			}
			Ctx<Null<?>, Term<Ty, En, Sym, Fk, Att, Gen, Null<?>>> extraRepr = new Ctx<>();
			for (Gen gen : col.gens.keySet()) {
				for (Att att : sch.attsFrom(col.gens.get(gen))) {
					if (!atts0.get(gen).containsKey(att)) {
						atts0.get(gen).put(att, InstExpImport.objectToSk(sch, null, gen, att, tys0, extraRepr, false, false));
					}
				}
			}
			
			ImportAlgebra<Ty, En, Sym, Fk, Att, Gen, Null<?>> alg = 
					new ImportAlgebra<Ty, En, Sym, Fk, Att, Gen, Null<?>>(sch, ens0, tys0, fks0, atts0,
					Object::toString, Object::toString);

			return new SaturatedInstance(alg, alg, (Boolean) strat.getOrDefault(AqlOption.require_consistency),
					(Boolean) strat.getOrDefault(AqlOption.allow_java_eqs_unsafe), true, extraRepr);
			
		}
		
		InitialAlgebra<Ty,En,Sym,Fk,Att,Gen,Sk,ID> 
		initial = new InitialAlgebra<>(strat, sch, col, new It(), Object::toString, Object::toString);
				 
		return new LiteralInstance<>(sch, col.gens.map, col.sks.map, eqs0, initial.dp(), initial, (Boolean) strat.getOrDefault(AqlOption.require_consistency), (Boolean) strat.getOrDefault(AqlOption.allow_java_eqs_unsafe)); 
	}
	
	//TODO aql: schema eval should happen first, so can typecheck before running
	
	@Override
	public SchExp<Ty, En, Sym, Fk, Att> type(AqlTyping G) {
		return schema;
	}
	
}