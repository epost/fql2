package catdata.aql.fdm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import catdata.Ctx;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.AqlProver;
import catdata.aql.AqlProver.ProverName;
import catdata.aql.Collage;
import catdata.aql.DP;
import catdata.aql.Eq;
import catdata.aql.Mapping;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.TypeSide;
import catdata.aql.Var;
import catdata.graph.DMG;
import catdata.graph.UnionFind;

public class ColimitSchema<N, E, Ty, En, Sym, Fk, Att>  {

	public final DMG<N, E> shape;
	
	public final TypeSide<Ty, Sym> ty;
	
	public final Ctx<N, Schema<Ty, En, Sym, Fk, Att>> nodes;
	
	public final Ctx<E, Mapping<Ty,En,Sym,Fk,Att,En,Fk,Att>> edges;
	
	public final Schema<Ty, Set<Pair<N,En>>, Sym, Pair<N,Fk>, Pair<N,Att>> schema;
	
	public final Ctx<N, Mapping<Ty,En,Sym,Fk,Att,Set<Pair<N,En>>,Pair<N,Fk>,Pair<N,Att>>> mappings;

	public ColimitSchema(DMG<N, E> shape, TypeSide<Ty, Sym> ty, Ctx<N, Schema<Ty, En, Sym, Fk, Att>> nodes, Ctx<E, Mapping<Ty, En, Sym, Fk, Att, En, Fk, Att>> edges, Map<String, String> options) {
		this.shape = shape;
		this.ty = ty;
		this.nodes = nodes;
		this.edges = edges;
		
		Collage<Ty, Set<Pair<N,En>>, Sym, Pair<N,Fk>, Pair<N,Att>, Void, Void> col = new Collage<>(ty.collage()); 
		Set<Triple<Pair<Var, Set<Pair<N, En>>>, Term<Ty, Set<Pair<N, En>>, Sym, Pair<N, Fk>, Pair<N, Att>, Void, Void>, Term<Ty, Set<Pair<N, En>>, Sym, Pair<N, Fk>, Pair<N, Att>, Void, Void>>> eqs = new HashSet<>();
		
		Set<Pair<N,En>> ens = new HashSet<>();
		for (N n : shape.nodes) {
			Schema<Ty, En, Sym, Fk, Att> s = nodes.get(n);
			for (En en : s.ens) {
				ens.add(new Pair<>(n, en));
			}
		}
		UnionFind<Pair<N,En>> uf = new UnionFind<>(ens);
		for (E e : shape.edges.keySet()) {
			Mapping<Ty, En, Sym, Fk, Att, En, Fk, Att> s = edges.get(e);
			for (En en : s.src.ens) {
				uf.union(new Pair<>(shape.edges.get(e).first , en),
						 new Pair<>(shape.edges.get(e).second, s.ens.get(en)));
			}
		}
		
		Ctx<Pair<N, En>, Set<Pair<N, En>>> eqcs = new Ctx<>(uf.toMap());
		col.ens.addAll(eqcs.values());
		for (N n : shape.nodes) {
			Schema<Ty, En, Sym, Fk, Att> s = nodes.get(n);
			for (Att att : s.atts.keySet()) {
				col.atts.put(new Pair<>(n, att), new Pair<>(eqcs.get(new Pair<>(n, s.atts.get(att).first)), s.atts.get(att).second));
			}
			for (Fk fk : s.fks.keySet()) {
				col.fks.put(new Pair<>(n, fk), new Pair<>(eqcs.get(new Pair<>(n, s.fks.get(fk).first)), eqcs.get(new Pair<>(n, s.fks.get(fk).second))));
			}
			for (Triple<Pair<Var, En>, Term<Ty, En, Sym, Fk, Att, Void, Void>, Term<Ty, En, Sym, Fk, Att, Void, Void>> eq : s.eqs) {
				Term<Ty, Set<Pair<N, En>>, Sym, Pair<N, Fk>, Pair<N, Att>, Void, Void> 
				lhs = eq.second.map(Function.identity(), Function.identity(), z -> new Pair<>(n, z), z -> new Pair<>(n, z), Function.identity(), Function.identity());
				Term<Ty, Set<Pair<N, En>>, Sym, Pair<N, Fk>, Pair<N, Att>, Void, Void> 
				rhs = eq.third.map(Function.identity(), Function.identity(), z -> new Pair<>(n, z), z -> new Pair<>(n, z), Function.identity(), Function.identity());
				Pair<Var, Set<Pair<N, En>>> x = new Pair<>(eq.first.first, eqcs.get(new Pair<>(n, eq.first.second)));
				eqs.add(new Triple<>(x, lhs, rhs));
				col.eqs.add(new Eq<>(new Ctx<>(x).inRight(), lhs, rhs));
			}
		}
		
		for (E e : shape.edges.keySet()) {
			Mapping<Ty, En, Sym, Fk, Att, En, Fk, Att> s = edges.get(e);
			N src = shape.edges.get(e).first;
			N dst = shape.edges.get(e).second;
			
			for (Fk fk : s.src.fks.keySet()) {
				Pair<En, List<Fk>> fk2 = s.fks.get(fk);
				Var v = new Var("v");
				Pair<Var, Set<Pair<N, En>>> x = new Pair<>(v, eqcs.get(new Pair<>(dst, fk2.first)));
				Term<Ty, Set<Pair<N, En>>, Sym, Pair<N, Fk>, Pair<N, Att>, Void, Void> 
				lhs = Term.Fk(new Pair<>(src, fk), Term.Var(v));
				Term<Ty, Set<Pair<N, En>>, Sym, Pair<N, Fk>, Pair<N, Att>, Void, Void> 
				rhs = Term.Fks(fk2.second.stream().map(z -> new Pair<>(dst, z)).collect(Collectors.toList()), Term.Var(v));
				eqs.add(new Triple<>(x, lhs, rhs));
				col.eqs.add(new Eq<>(new Ctx<>(x).inRight(), lhs, rhs));
			}
			for (Att att : s.src.atts.keySet()) {
				Triple<Var, En, Term<Ty, En, Sym, Fk, Att, Void, Void>> fk2 = s.atts.get(att);
				Pair<Var, Set<Pair<N, En>>> x = new Pair<>(fk2.first, eqcs.get(new Pair<>(src, fk2.second)));
				Term<Ty, Set<Pair<N, En>>, Sym, Pair<N, Fk>, Pair<N, Att>, Void, Void> 
				lhs = Term.Att(new Pair<>(src, att), Term.Var(fk2.first));
				Term<Ty, Set<Pair<N, En>>, Sym, Pair<N, Fk>, Pair<N, Att>, Void, Void> 
				rhs = fk2.third.map(Function.identity(), Function.identity(), z -> new Pair<>(dst, z), z -> new Pair<>(dst, z), Function.identity(), Function.identity());
				eqs.add(new Triple<>(x, lhs, rhs));
				col.eqs.add(new Eq<>(new Ctx<>(x).inRight(), lhs, rhs));
			}
		}
	
		boolean b = ! (Boolean) AqlOptions.getOrDefault(options, AqlOption.allow_java_eqs_unsafe);
			
		AqlOptions ops = new AqlOptions(Util.singMap(AqlOption.prover.toString(), ProverName.fail.toString()), col);
		DP<Ty, Set<Pair<N, En>>, Sym, Pair<N, Fk>, Pair<N, Att>, Void, Void> dp = AqlProver.create(ops, col, ty.js);
		
		//TODO aql dont forget to add equations to collage and to schema
		schema = new Schema<>(ty, col.ens, col.atts.map, col.fks.map, eqs, dp, b);
		
		mappings = new Ctx<>();
		
		for (N n : shape.nodes) {
			Map<Att, Triple<Var, Set<Pair<N, En>>, Term<Ty, Set<Pair<N, En>>, Sym, Pair<N, Fk>, Pair<N, Att>, Void, Void>>> atts = new HashMap<>();
			Map<Fk, Pair<Set<Pair<N, En>>, List<Pair<N, Fk>>>> fks = new HashMap<>();
			Map<En, Set<Pair<N, En>>> ens0 = new HashMap<>();
			
			Schema<Ty, En, Sym, Fk, Att> s = nodes.get(n);
			for (En en : s.ens) {
				ens0.put(en, eqcs.get(new Pair<>(n, en)));
			}
			for (Fk fk : s.fks.keySet()) {
				fks.put(fk, new Pair<>(eqcs.get(new Pair<>(n, s.fks.get(fk).first)), Util.singList(new Pair<>(n, fk))));
			}
			for (Att att : s.atts.keySet()) {
				Var v = new Var("v");
				Term<Ty, Set<Pair<N, En>>, Sym, Pair<N, Fk>, Pair<N, Att>, Void, Void> 
				t = Term.Att(new Pair<>(n, att), Term.Var(v));
				atts.put(att, new Triple<>(v, eqcs.get(new Pair<>(n, s.atts.get(att).first)), t));
			}
			
			
			Mapping<Ty,En,Sym,Fk,Att,Set<Pair<N,En>>,Pair<N,Fk>,Pair<N,Att>> m = new Mapping<>(ens0 , atts, fks, nodes.get(n), schema, false); //TODO aql allow as option?
			mappings.put(n, m);
		}
		
		//System.out.println(this);
	}
	
	

	@Override
	public String toString() {
		return "ColimitSchema [shape=" + shape + ", ty=" + ty + ", nodes=" + nodes + ", edges=" + edges + ", schema=" + schema + ", mappings=" + mappings + "]";
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((edges == null) ? 0 : edges.hashCode());
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
		result = prime * result + ((shape == null) ? 0 : shape.hashCode());
		result = prime * result + ((ty == null) ? 0 : ty.hashCode());
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
		ColimitSchema<?, ?, ?, ?, ?, ?, ?> other = (ColimitSchema<?, ?, ?, ?, ?, ?, ?>) obj;
		if (edges == null) {
			if (other.edges != null)
				return false;
		} else if (!edges.equals(other.edges))
			return false;
		if (nodes == null) {
			if (other.nodes != null)
				return false;
		} else if (!nodes.equals(other.nodes))
			return false;
		if (shape == null) {
			if (other.shape != null)
				return false;
		} else if (!shape.equals(other.shape))
			return false;
		if (ty == null) {
			if (other.ty != null)
				return false;
		} else if (!ty.equals(other.ty))
			return false;
		return true;
	}

//	
	


}