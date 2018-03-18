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

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Collage;
import catdata.aql.Eq;
import catdata.aql.Instance;
import catdata.aql.It;
import catdata.aql.It.ID;
import catdata.aql.Kind;
import catdata.aql.RawTerm;
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

//TODO aql merge with InstExpRaw
public final class InstExpQuotient<X,Y> extends InstExp<Ty,En,Sym,Fk,Att,Gen,Sk,ID,Chc<Sk,Pair<ID,Att>>> implements Raw {
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((I == null) ? 0 : I.hashCode());
		result = prime * result + ((eqs == null) ? 0 : eqs.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof InstExpQuotient))
			return false;
		InstExpQuotient<?, ?> other = (InstExpQuotient<?, ?>) obj;
		if (I == null) {
			if (other.I != null)
				return false;
		} else if (!I.equals(other.I))
			return false;
		if (eqs == null) {
			if (other.eqs != null)
				return false;
		} else if (!eqs.equals(other.eqs))
			return false;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		return true;
	}

	public final InstExp<Ty,En,Sym,Fk,Att,Gen,Sk,X,Y> I;
	
	private Ctx<String, List<InteriorLabel<Object>>> raw = new Ctx<>();
	
	public Ctx<String, List<InteriorLabel<Object>>> raw() {
		return raw;
	}
	
	@Override
	public Collection<Pair<String, Kind>> deps() {
		return I.deps();
	}
 
	public final Set<Pair<RawTerm, RawTerm>> eqs;
	
	public final Map<String, String> options;
	
	@Override
	public Map<String, String> options() {
		return options;
	}

	//typesafe by covariance of read-only collections
	public InstExpQuotient(InstExp<Ty,En,Sym,Fk,Att,Gen,Sk,X,Y> I, List<Pair<Integer,Pair<RawTerm, RawTerm>>> eqs, List<Pair<String, String>> options) {
		this.eqs = LocStr.proj2(eqs);
		this.options = Util.toMapSafely(options);
		this.I = I;
		
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
		
		List<String> temp = new LinkedList<>();
		
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
		
		return "quotient " + I + "{\n" + toString + "}";
	} 


	
	

	@Override
	public Instance<Ty, En, Sym, Fk, Att, Gen, Sk, ID, Chc<Sk, Pair<ID, Att>>> eval(AqlEnv env) {
		Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> J = I.eval(env);
		Collage<Ty,En,Sym,Fk,Att,Gen,Sk> col = new Collage<>(J.collage());
		
		Set<Pair<Term<Ty,En,Sym,Fk,Att,Gen,Sk>, Term<Ty,En,Sym,Fk,Att,Gen,Sk>>> eqs0 = new HashSet<>();

		for (Pair<RawTerm, RawTerm> eq : eqs) {
			try {
				Map<String, Chc<Ty, En>> ctx = Collections.emptyMap();
				
				Triple<Ctx<Var,Chc<Ty,En>>,Term<Ty,En,Sym,Fk,Att,Gen,Sk>,Term<Ty,En,Sym,Fk,Att,Gen,Sk>>
				eq0 = RawTerm.infer1x(ctx, eq.first, eq.second, null, col, "", J.schema().typeSide.js).first3();
						
				if (J.type(eq0.second).left) {
					throw new RuntimeException("Attempt to equate values at type in quotient: " + eq0.second + " at type " + J.type(eq0.second).l);
				}
				if (J.type(eq0.third).left) {
					throw new RuntimeException("Attempt to equate values at type in quotient: " + eq0.third + " at type " + J.type(eq0.third).l);
				}
			
				eqs0.add(new Pair<>(eq0.second, eq0.third));
				col.eqs.add(new Eq<>(new Ctx<>(), eq0.second, eq0.third));
			} catch (RuntimeException ex) {
				ex.printStackTrace();
				throw new LocException(find("equations", eq), "In equation " + eq.first + " = " + eq.second + ", " + ex.getMessage());
			}
		}

		
		
		AqlOptions strat = new AqlOptions(options, col, env.defaults);
		InitialAlgebra<Ty,En,Sym,Fk,Att,Gen,Sk,ID> 
		initial = new InitialAlgebra<>(strat, J.schema(), col, new It(), Object::toString, Object::toString);
				 
		return new LiteralInstance<>(J.schema(), col.gens.map, col.sks.map, eqs0, initial.dp(), initial, (Boolean) strat.getOrDefault(AqlOption.require_consistency), (Boolean) strat.getOrDefault(AqlOption.allow_java_eqs_unsafe)); 
	}
	
	//TODO aql: schema eval should happen first, so can typecheck before running
	
	@Override
	public SchExp<Ty, En, Sym, Fk, Att> type(AqlTyping G) {
		return I.type(G);
	}
	
}