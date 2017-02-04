package catdata.aql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.AqlProver.ProverName;
import catdata.graph.DMG;
import catdata.graph.UnionFind;

public class ColimitSchema<N, E, Ty, En, Sym, Fk, Att> implements Semantics {

	public final DMG<N, E> shape;
	
	public final TypeSide<Ty, Sym> ty;
	
	public final Ctx<N, Schema<Ty, En, Sym, Fk, Att>> nodes;
	
	public final Ctx<E, Mapping<Ty,En,Sym,Fk,Att,En,Fk,Att>> edges;
	
	public final Schema<Ty, Set<Pair<N,En>>, Sym, Pair<N,Fk>, Pair<N,Att>> schema;
	
	public final Ctx<N, Mapping<Ty,En,Sym,Fk,Att,Set<Pair<N,En>>,Pair<N,Fk>,Pair<N,Att>>> mappings;

	public final Schema<Ty, String, Sym, String, String> schemaStr;
	
	public final Ctx<N, Mapping<Ty,En,Sym,Fk,Att,String,String,String>> mappingsStr;
	
	public ColimitSchema<N, E, Ty, En, Sym, Fk, Att> wrap(		
			Mapping<Ty,String,Sym,String,String,String,String,String> isoToUser, 
			Mapping<Ty,String,Sym,String,String,String,String,String> isoFromUser) {
		if (!isoToUser.src.equals(schemaStr)) {
			throw new RuntimeException("Source of " + isoToUser + " \n, namely " + isoToUser.src + "\ndoes not match canonical colimit, namely " + schemaStr);
		}
		checkIso(isoToUser, isoFromUser);
		Ctx<N, Mapping<Ty,En,Sym,Fk,Att,String,String,String>> newMapping = mappingsStr.map((k,v) -> new Pair<>(k, Mapping.compose(v, isoToUser)));
		return new ColimitSchema<>(shape, ty, nodes, edges, schema, mappings, isoToUser.dst, newMapping);
	}
	
	/*public Mapping<Ty,En,Sym,Fk,Att,String,String,String> getMapping(N n) {
		return Mapping.compose(mappingsStr.get(n), isoToUser);
	}*/

	private ColimitSchema(DMG<N, E> shape, TypeSide<Ty, Sym> ty, Ctx<N, Schema<Ty, En, Sym, Fk, Att>> nodes, Ctx<E, Mapping<Ty, En, Sym, Fk, Att, En, Fk, Att>> edges, Schema<Ty, Set<Pair<N, En>>, Sym, Pair<N, Fk>, Pair<N, Att>> schema, Ctx<N, Mapping<Ty, En, Sym, Fk, Att, Set<Pair<N, En>>, Pair<N, Fk>, Pair<N, Att>>> mappings, Schema<Ty, String, Sym, String, String> schemaStr,
			Ctx<N, Mapping<Ty, En, Sym, Fk, Att, String, String, String>> mappingsStr) {
		super();
		this.shape = shape;
		this.ty = ty;
		this.nodes = nodes;
		this.edges = edges;
		this.schema = schema;
		this.mappings = mappings;
		this.schemaStr = schemaStr;
		this.mappingsStr = mappingsStr;
	}

	private static <N,En> String conv1(Set<Pair<N,En>> eqc) {
		List<String> l = eqc.stream().map(ColimitSchema::conv2).collect(Collectors.toList());
		l = Util.alphabetical(l);
		return Util.sep(l, "__");
	}
	
	private static <X,Y> String conv2(Pair<X,Y> p) {
		return p.first + "_" + p.second;
	}
	
	private static <N, Ty, En, Sym, Fk, Att> Term<Ty, String, Sym, String, String, Void, Void> conv3(Term<Ty, Set<Pair<N,En>>, Sym, Pair<N,Fk>, Pair<N,Att>, Void, Void> t) {
		return t.map(Function.identity(), Function.identity(), ColimitSchema::conv2, ColimitSchema::conv2, Function.identity(), Function.identity());
	}
	
	private Chc<Ty, String> conv4(Chc<Ty, Set<Pair<N, En>>> v) {
		if (v.left) {
			return Chc.inLeft(v.l);
		}
		return Chc.inRight(conv1(v.r));
	}
	
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
		
		Collage<Ty, String, Sym, String, String, Void, Void> colX = new Collage<>(ty.collage()); 
		
		colX.ens.addAll(col.ens.stream().map(ColimitSchema::conv1).collect(Collectors.toSet()));
		colX.atts.map.putAll(col.atts.map((k,v) -> new Pair<>(conv2(k), new Pair<>(conv1(v.first), v.second))).map);
		colX.fks.putAll(col.fks.map((k,v) -> new Pair<>(conv2(k), new Pair<>(conv1(v.first), conv1(v.second)))).map);
		
		Set<Triple<Pair<Var, String>, Term<Ty, String, Sym, String, String, Void, Void>, Term<Ty, String, Sym, String, String, Void, Void>>> 
		eqsX = eqs.stream().map(t -> new Triple<>(new Pair<>(t.first.first, conv1(t.first.second)), conv3(t.second), conv3(t.third))).collect(Collectors.toSet());
		colX.eqs.addAll(col.eqs.stream().map(t -> new Eq<>(t.ctx.map((k,v) -> new Pair<>(k, conv4(v))), conv3(t.lhs), conv3(t.rhs))).collect(Collectors.toSet()));
		
		AqlOptions opsX = new AqlOptions(options, colX);
		DP<Ty, String, Sym, String, String, Void, Void> dpX = AqlProver.create(opsX, colX, ty.js);
	
		schemaStr = new Schema<>(ty, colX.ens, colX.atts.map, colX.fks.map, eqsX, dpX, false);
		
		mappingsStr = new Ctx<>();
		for (N n : mappings.keySet()) {
			mappingsStr.put(n, conv5(mappings.get(n)));
		}
		
/*		schemaUser = schemaStr;
		isoToUser = Mapping.id(schemaUser);
		isoFromUser = isoToUser;
*/
		//TODO: aql check for collisions
	}
	
	private void checkIso(Mapping<Ty, String, Sym, String, String, String, String, String> F, Mapping<Ty, String, Sym, String, String, String, String, String> G) {
		isoOneWay(F, G, " when composing (toUser ; fromUser)");
		isoOneWay(G, F, " when composing (fromUser ; toUser)");
	}

	private void isoOneWay(Mapping<Ty, String, Sym, String, String, String, String, String> F, Mapping<Ty, String, Sym, String, String, String, String, String> G, String str) {
		if (!F.dst.equals(G.src)) {
			throw new RuntimeException("Target of " + F + " \n, namely " + F.dst + "\ndoes not match source of " + G + ", namely " + F.src + "\n" + str);
		}
		Mapping<Ty, String, Sym, String, String, String, String, String> f = Mapping.compose(F, G);
		for (String en : f.src.ens) {
			String en2 = f.ens.get(en);
			if (!en.equals(en2)) {
				throw new RuntimeException(en + " taken to " + en2 + ", rather than itself, " + str);
			}
		}
		for (String fk : f.src.fks.keySet()) {
			Pair<String, List<String>> fk2 = f.fks.get(fk);
			Var v = new Var("v");
			Term<Ty, String, Sym, String, String, Void, Void> t = Term.Fks(fk2.second, Term.Var(v));
			Term<Ty, String, Sym, String, String, Void, Void> s = Term.Fk(fk, Term.Var(v));
			boolean eq = F.src.dp.eq(new Ctx<>(new Pair<>(v, Chc.inRight(fk2.first))), s, t);
			if (!eq) {
				throw new RuntimeException(fk + " taken to " + t + ", which is not provably equal to itself, " + str);
			}
		}
		for (String att : f.src.atts.keySet()) {
			Triple<Var, String, Term<Ty, String, Sym, String, String, Void, Void>> att2 = f.atts.get(att);
			Var v = att2.first;
			Term<Ty, String, Sym, String, String, Void, Void> t = att2.third; //Term.Fks(att2.second, Term.Var(v));
			Term<Ty, String, Sym, String, String, Void, Void> s = Term.Att(att, Term.Var(v));
			boolean eq = F.src.dp.eq(new Ctx<>(new Pair<>(v, Chc.inRight(att2.second))), s, t);
			if (!eq) {
				throw new RuntimeException(att + " taken to " + t + ", which is not provably equal to itself, " + str);
			}
		}
	}

	private Mapping<Ty, En, Sym, Fk, Att, String, String, String> conv5(Mapping<Ty, En, Sym, Fk, Att, Set<Pair<N, En>>, Pair<N, Fk>, Pair<N, Att>> m) {
		Map<En, String> ens = m.ens.map((k,v) -> new Pair<>(k, conv1(v))).map;
		Map<Att, Triple<Var, String, Term<Ty, String, Sym, String, String, Void, Void>>> 
		atts = m.atts.map((k,v) -> new Pair<>(k, new Triple<>(v.first, conv1(v.second), conv3(v.third)))).map;
		Map<Fk, Pair<String, List<String>>> fks = m.fks.map((k,v) -> new Pair<>(k, new Pair<>(conv1(v.first), 
				v.second.stream().map(ColimitSchema::conv2).collect(Collectors.toList())))).map;
		return new Mapping<>(ens, atts, fks, m.src, schemaStr, false);
	}

	@Override
	public String toString() {
		return schemaStr.toString();

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

	@Override
	public Kind kind() {
		return Kind.SCHEMA_COLIMIT;
	}

//	
	


}