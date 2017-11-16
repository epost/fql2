package catdata.aql;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Util;  

public abstract class Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> implements Semantics {

	@Override

	public String sample(int size) {
		int en_i = 0;
		List<String> u = new LinkedList<>();
		for (En en : Util.alphabetical(schema().ens)) {
			if (algebra().en(en).isEmpty()) {
				continue;
			}
			int x_i = 0;
			List<String> h = new LinkedList<>();
			h.add(en.toString() + " (" + algebra().en(en).size() + " rows)");
		//	h.add("========");
			for (X x : algebra().en(en)) {
				List<String> l = new LinkedList<>();
				l.add("");
				l.add("ID: " + x);
				/* int fk_i = 0;
				for (Fk fk : schema().fksFrom(en)) {
					l.add(fk.toString() + ": " + algebra().fk(fk, x));
					fk_i++;
					if (fk_i > size) {
						break;
					}
				} */
				
				int att_i = 0;
				for (Att att : schema().attsFrom(en)) {
					l.add(att.toString() + ": " + algebra().att(att, x));
					att_i++;
					if (att_i > (2*size)) {
						break;
					}
				}
						
				x_i++;
				h.add(Util.sep(l, "\n"));
				
				if (x_i > size) {
					break;
				}
			}
			
			en_i++;
			u.add(Util.sep(h, "\n"));
			
			if (en_i > size*size) {
				break;
			}
		}
		
		return Util.sep(u, "\n\n");
	}
	
	@Override
	public Kind kind() {
		return Kind.INSTANCE;
	}
	
	/**	  
	 * @return sum of rows in the algebra
	 */
	@Override
	public int size() {
		return algebra().size();
	}

	
	public abstract Schema<Ty, En, Sym, Fk, Att> schema();
	
	public abstract Ctx<Gen, En> gens(); 
	public abstract Ctx<Sk, Ty> sks();

	
	public abstract boolean requireConsistency();
	
	public abstract boolean allowUnsafeJava();
	
	public abstract Set<Pair<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> eqs();

	public abstract DP<Ty,En,Sym,Fk,Att,Gen,Sk> dp();
	
	
	public final Chc<Ty,En> type(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {		
		return term.type(new Ctx<>(), new Ctx<>(), schema().typeSide.tys, schema().typeSide.syms.map, schema().typeSide.js.java_tys.map, schema().ens, schema().atts.map, schema().fks.map, gens().map, sks().map);
	}
	
	public final void validate() {	
		validateNoTalg();
		
		if (requireConsistency() && !algebra().hasFreeTypeAlgebra()) {
			throw new RuntimeException("Not necessarily consistent.  This isn't necessarily an error, but is unusual.  Set require_consistency=false to proceed.  Type algebra is\n\n" + algebra().talg());
		}
		if (!allowUnsafeJava() && !algebra().hasFreeTypeAlgebraOnJava()) {
			throw new RuntimeException("Unsafe use of java - AQL's behavior is undefined.  Possible solution: add allow_java_eqs_unsafe=true, change the equations, or contact support at info@catinf.com.  Type algebra is\n\n" + algebra().talg());
		}
		
		
	}

	public final void validateNoTalg() {		
			//check that each gen/sk is in tys/ens
			for (Gen gen : gens().keySet()) {
				En en = gens().get(gen);
				if (!schema().ens.contains(en)) {
					throw new RuntimeException("On generator " + gen + ", the entity " + en + " is not declared.");
				}
			}
			for (Sk sk : sks().keySet()) {
				Ty ty = sks().get(sk);
				if (!schema().typeSide.tys.contains(ty)) {
					throw new RuntimeException("On labelled null " + sk + ", the type " + ty + " is not declared.");
				}
			}
			
			for (Pair<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> eq : eqs()) {
				//check lhs and rhs types match in all eqs
				Chc<Ty, En> lhs = type(eq.first);
				Chc<Ty, En> rhs = type(eq.second);
				if (!lhs.equals(rhs)) {
					throw new RuntimeException("In instance equation " + toString(eq) + ", lhs sort is " + lhs.toStringMash() + " but rhs sort is " + rhs.toStringMash());
				}
			}				
		}	
	
	private String toString(Pair<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> eq) {
		return eq.first + " = " + eq.second;
	}
	
	
	
	public abstract Algebra<Ty,En,Sym,Fk,Att,Gen,Sk,X,Y> algebra();

	private Collage<Ty, En, Sym, Fk, Att, Gen, Sk> collage;
	public final synchronized Collage<Ty, En, Sym, Fk, Att, Gen, Sk> collage() {
		if (collage != null) {
			return collage;
		}
		collage = new Collage<>(schema().collage());
		collage.gens.putAll(gens().map);
		collage.sks.putAll(sks().map);
		collage.eqs.addAll(eqs().stream().map(x -> new Eq<>(new Ctx<>(), x.first, x.second)).collect(Collectors.toSet()));
		return collage;
	}
	
	
	@Override
	public final int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((eqs() == null) ? 0 : eqs().hashCode());
		result = prime * result + ((gens() == null) ? 0 : gens().hashCode());
		result = prime * result + ((schema() == null) ? 0 : schema().hashCode());
		result = prime * result + ((sks() == null) ? 0 : sks().hashCode());
		return result;
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		Instance<?,?,?,?,?,?,?,?,?> other = (Instance<?,?,?,?,?,?,?,?,?>) obj;
		if (eqs() == null) {
			if (other.eqs() != null)
				return false;
		} else if (!eqs().equals(other.eqs()))
			return false;
		if (gens() == null) {
			if (other.gens() != null)
				return false;
		} else if (!gens().equals(other.gens()))
			return false;
		if (schema() == null) {
			if (other.schema() != null)
				return false;
		} else if (!schema().equals(other.schema()))
			return false;
		if (sks() == null) {
			if (other.sks() != null)
				return false;
		} else if (!sks().equals(other.sks()))
			return false;
		return true;
	}
	
	public final String toString(String g, String w) {
		String toString;
		List<String> eqs0 = eqs().stream().map(x -> x.first + " = " + x.second).collect(Collectors.toList());
		toString = g;
		if (!gens().isEmpty()) {
			toString += "\n\t" + Util.sep(gens().map, " : ", "\n\t");
		}
		if (!sks().isEmpty()) {
			toString += "\n\t" + Util.sep(sks().map, " : " , "\n\t");			
		}
		if (!eqs0.isEmpty()) {
			toString += "\n\n" + w + "\n\t" + Util.sep(eqs0, "\n\t");
		}
		return toString.trim();
	} 

	@Override
	public String toString() {
		return toString("generators", "equations");
	} 

	//TODO aql validate instances against algebras
	
}
