package catdata.aql.exp;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.tree.DefaultMutableTreeNode;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Collage;
import catdata.aql.Eq;
import catdata.aql.Kind;
import catdata.aql.Query;
import catdata.aql.RawTerm;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.Var;

//TODO aql add type params to all raws?
public class QueryExpRaw<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> 
extends QueryExp<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> {
	
	
	public void asTree(DefaultMutableTreeNode root) {
		if (imports.size() > 0) { 
			DefaultMutableTreeNode n = new DefaultMutableTreeNode();
			n.setUserObject("imports");
			for (Object t : imports) {
				DefaultMutableTreeNode m = new DefaultMutableTreeNode();
				m.setUserObject(t.toString());
				n.add(m);
			}
		}
		if (blocks.size() > 0) { 
			DefaultMutableTreeNode n = new DefaultMutableTreeNode();
			n.setUserObject("entities");
			for (Pair<En2, Block<En1, Att2>> t : blocks) {
				DefaultMutableTreeNode m = new DefaultMutableTreeNode();
				m.setUserObject(t.first);
				t.second.asTree(m);
				n.add(m);
			}
			root.add(n);
		}
		if (fks.size() > 0) { 
			DefaultMutableTreeNode n = new DefaultMutableTreeNode();
			n.setUserObject("fks");
			for (Pair<Fk2, Trans> t : fks) {
				DefaultMutableTreeNode m = new DefaultMutableTreeNode();
				m.setUserObject(t.first);
				t.second.asTree(m);
				n.add(m);
			}
			root.add(n);
		}
		if (atts.size() > 0) { 
			DefaultMutableTreeNode n = new DefaultMutableTreeNode();
			n.setUserObject("atts");
			for (Pair<Att2, RawTerm> t : atts) {
				DefaultMutableTreeNode m = new DefaultMutableTreeNode();
				m.setUserObject(t.first + " -> " + t.second);
				n.add(m);
			}
			root.add(n);
		}
		
	}
	
	private final SchExp<Ty,En1,Sym,Fk1,Att1> src;
	private final SchExp<Ty,En2,Sym,Fk2,Att2> dst;
	
	private final List<String> imports;
	
	private final Map<String, String> options;
	
	private final List<Pair<En2, Block<En1,Att2>>> blocks;

	private final List<Pair<Fk2, Trans>> fks;
	
	private final List<Pair<Att2, RawTerm>> atts;
	
	@Override
	public Map<String, String> options() {
		return options;
	}
	
	public static class Trans {
		public final List<Pair<Var, RawTerm>> gens; 

		public final Map<String, String> options;

		public void asTree(DefaultMutableTreeNode root) {
				if (gens.size() > 0) { 
				DefaultMutableTreeNode n = new DefaultMutableTreeNode();
				n.setUserObject("entities");
				for (Pair<Var, RawTerm> t : gens) {
					DefaultMutableTreeNode m = new DefaultMutableTreeNode();
					m.setUserObject(t.first + " -> " + t.second);
					n.add(m);
				}
				root.add(n);
			}
		
			
		}
		public Trans(List<Pair<String, RawTerm>> gens, List<Pair<String, String>> options) {
			this.gens = new LinkedList<>();
			for (Pair<String, RawTerm> gen : gens) {
				this.gens.add(new Pair<>(new Var(gen.first), gen.second));
			}
			this.options = Util.toMapSafely(options);
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + ((gens == null) ? 0 : gens.hashCode());
			result = prime * result + ((options == null) ? 0 : options.hashCode());
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
			Trans other = (Trans) obj;
			if (gens == null) {
				if (other.gens != null)
					return false;
			} else if (!gens.equals(other.gens))
				return false;
			if (options == null) {
				if (other.options != null)
					return false;
			} else if (!options.equals(other.options))
				return false;
			return true;
		}

		private String toString;

		@Override
		public String toString() {
			if (toString != null) {
				return toString;
			}
			toString = "";
					
			List<String> temp = new LinkedList<>();
			
			if (!gens.isEmpty()) {
				for (Pair<Var, RawTerm> en1 : gens) {
					temp.add(en1.first +  " -> " + en1.second);
				}
				
				toString += Util.sep(temp, "\n\t\t\t\t");
			}
			
			if (!options.isEmpty()) {
				toString += "\n\toptions";
				temp = new LinkedList<>();
				for (Entry<String, String> sym : options.entrySet()) {
					temp.add(sym.getKey() + " = " + sym.getValue());
				}
				
				toString += "\n\t\t\t" + Util.sep(temp, "\n\t\t\t");
			}
		
			return "\t{" + toString + "}";
		} 
		
	
		
	}
	
	
	public static class Block<En1, Att2> {
		
		public void asTree(DefaultMutableTreeNode root) {
			if (gens.size() > 0) { 
				DefaultMutableTreeNode n = new DefaultMutableTreeNode();
				n.setUserObject("gens");
				for (Pair<Var, En1> t : gens) {
					DefaultMutableTreeNode m = new DefaultMutableTreeNode();
					m.setUserObject(t.first + " : " + t.second);
					n.add(m);
				}
				root.add(n);
			}
			if (eqs.size() > 0) { 
				DefaultMutableTreeNode n = new DefaultMutableTreeNode();
				n.setUserObject("eqs");
				for (Pair<RawTerm, RawTerm> t : eqs) {
					DefaultMutableTreeNode m = new DefaultMutableTreeNode();
					m.setUserObject(t.first + "=" + t.second);
					n.add(m);
				}
				root.add(n);
			}
			
		}
		
		public final List<Pair<Var, En1>> gens; 

		public final List<Pair<RawTerm, RawTerm>> eqs;
	
		public final Map<String, String> options;

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + ((eqs == null) ? 0 : eqs.hashCode());
			result = prime * result + ((gens == null) ? 0 : gens.hashCode());
			result = prime * result + ((options == null) ? 0 : options.hashCode());
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
			Block<?,?> other = (Block<?,?>) obj;
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
			if (options == null) {
				if (other.options != null)
					return false;
			} else if (!options.equals(other.options))
				return false;
			return true;
		}

		public final List<Pair<Att2, RawTerm>> atts;
	
		public Block(List<Pair<String, En1>> gens, List<Pair<RawTerm, RawTerm>> eqs, List<Pair<String, String>> options, List<Pair<Att2, RawTerm>> atts) {
			this.gens = new LinkedList<>();
			this.atts = atts;
			for (Pair<String, En1> gen : gens) {
				this.gens.add(new Pair<>(new Var(gen.first), gen.second));
			}
			this.eqs = eqs;
			this.options = Util.toMapSafely(options);			
		}

		private String toString;

		@Override
		public String toString() {
			if (toString != null) {
				return toString;
			}
			toString = "";
					
			List<String> temp = new LinkedList<>();
			
			if (!gens.isEmpty()) {
				toString += "from\t";
						
				Map<En1, Set<Var>> x = Util.revS(Util.toMapSafely(gens));
				 temp = new LinkedList<>();
				for (En1 en1 : x.keySet()) {
					temp.add(Util.sep(x.get(en1), " ") +  " : " + en1);
				}
				
				toString += Util.sep(temp, "\n\t\t\t\t\t");
			}
			
			if (!eqs.isEmpty()) {
				toString += "\n\t\t\t\twhere\t";
				temp = new LinkedList<>();
				for (Pair<RawTerm, RawTerm> sym : eqs) {
					temp.add(sym.first + " = " + sym.second);
				}
				toString += Util.sep(temp, "\n\t\t\t\t\t");
			}
			
			if (!atts.isEmpty()) {
				toString += "\n\t\t\t\treturn\t";
				temp = new LinkedList<>();
				for (Pair<Att2, RawTerm> sym : atts) {
					temp.add(sym.first + " -> " + sym.second);
				}
				toString += Util.sep(temp, "\n\t\t\t\t\t");
			}
			
			
			if (!options.isEmpty()) {
				toString += "\n\t\t\t\toptions";
				temp = new LinkedList<>();
				for (Entry<String, String> sym : options.entrySet()) {
					temp.add(sym.getKey() + " = " + sym.getValue());
				}
				
				toString += "\n\t\t\t\t" + Util.sep(temp, "\n\t\t\t\t\t");
			}
		
			return "\t{" + toString + "}";
		} 
		
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((atts == null) ? 0 : atts.hashCode());
		result = prime * result + ((blocks == null) ? 0 : blocks.hashCode());
		result = prime * result + ((dst == null) ? 0 : dst.hashCode());
		result = prime * result + ((fks == null) ? 0 : fks.hashCode());
		result = prime * result + ((imports == null) ? 0 : imports.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		result = prime * result + ((src == null) ? 0 : src.hashCode());
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
		QueryExpRaw<?,?,?,?,?,?,?,?> other = (QueryExpRaw<?,?,?,?,?,?,?,?>) obj;
		if (atts == null) {
			if (other.atts != null)
				return false;
		} else if (!atts.equals(other.atts))
			return false;
		if (blocks == null) {
			if (other.blocks != null)
				return false;
		} else if (!blocks.equals(other.blocks))
			return false;
		if (dst == null) {
			if (other.dst != null)
				return false;
		} else if (!dst.equals(other.dst))
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
		if (src == null) {
			if (other.src != null)
				return false;
		} else if (!src.equals(other.src))
			return false;
		return true;
	}

	private String toString;
	
	@Override
	public String toString() {
		if (toString != null) {
			return toString;
		}
		toString = "";
		
		if (!imports.isEmpty()) {
			toString += "\timports";
			toString += "\n\t\t" + Util.sep(imports, " ") + "\n";
		}
			
		List<String> temp = new LinkedList<>();
		
		if (!blocks.isEmpty()) {
			toString += "\tentities";
					
			for (Pair<En2, Block<En1, Att2>> x : blocks) {
				temp.add(x.first + " -> " + x.second.toString());
			}
			
			toString += "\n\t\t" + Util.sep(temp, "\n\n\t\t") + "\n";
		}
		
		if (!fks.isEmpty()) {
			toString += "\tforeign_keys";
			temp = new LinkedList<>();
			for (Pair<Fk2, Trans> sym : fks) {
				temp.add(sym.first + " -> " + sym.second);
			}
			toString += "\n\t\t" + Util.sep(temp, "\n\n\t\t") + "\n";
		}
		
		if (!options.isEmpty()) {
			toString += "\toptions";
			temp = new LinkedList<>();
			for (Entry<String, String> sym : options.entrySet()) {
				temp.add(sym.getKey() + " = " + sym.getValue());
			}
			
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		
		return "literal : " + src + " -> " + dst + " {\n" + toString + "}";
	} 

	public QueryExpRaw(SchExp<Ty, En1, Sym, Fk1, Att1> src, SchExp<Ty, En2, Sym, Fk2, Att2> dst, List<String> imports, List<Pair<En2, Pair<Block<En1, Att2>, List<Pair<Att2, RawTerm>>>>> blocks,  List<Pair<Fk2, Trans>> fks, List<Pair<String, String>> options) {
		this.src = src;
		this.dst = dst;
		this.imports = imports;
		this.options = Util.toMapSafely(options);
		this.blocks = blocks.stream().map(x -> new Pair<>(x.first, x.second.first)).collect(Collectors.toList());
		this.fks = new LinkedList<>(fks);
        atts = new LinkedList<>();
		for (Pair<En2, Pair<Block<En1, Att2>, List<Pair<Att2, RawTerm>>>> block : blocks) {
			atts.addAll(block.second.second);
		}
	}

	@Override
	public Collection<Pair<String, Kind>> deps() {
		return Util.union(src.deps(), Util.union(dst.deps(), imports.stream().map(x -> new Pair<>(x, Kind.QUERY)).collect(Collectors.toSet())));
	}

	@Override
	public Pair<SchExp<Ty, En1, Sym, Fk1, Att1>, SchExp<Ty, En2, Sym, Fk2, Att2>> type(AqlTyping G) {
		return new Pair<>(src, dst);
	}

	
	@Override
	public Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> eval(AqlEnv env) {
		Schema<Ty, En1, Sym, Fk1, Att1> src0 = src.eval(env);
		Schema<Ty, En2, Sym, Fk2, Att2> dst0 = dst.eval(env);
	
		Ctx<En2, Triple<Ctx<Var, En1>, Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>>, AqlOptions>> ens0 = new Ctx<>();
		Ctx<Att2, Term<Ty, En1, Sym, Fk1, Att1, Var, Void>> atts0 = new Ctx<>();
		Ctx<Fk2, Pair<Ctx<Var, Term<Void, En1, Void, Fk1, Void, Var, Void>>, Boolean>> fks0 = new Ctx<>();
	
		for (String k : imports) {
			@SuppressWarnings("unchecked")
			Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> v = env.defs.qs.get(k);
			for (En2 en2 : v.ens.keySet()) {
				ens0.put(en2, new Triple<>(v.ens.get(en2).gens, v.ens.get(en2).eqs, v.ens.get(en2).options));
			}
			for (Att2 att2 : v.atts.keySet()) {
				atts0.put(att2, v.atts.get(att2));
			}
			for (Fk2 fk2 : v.fks.keySet()) {
				fks0.put(fk2, new Pair<>(v.fks.get(fk2).gens(), v.doNotValidate.get(fk2)));
			}
		}
		
		Ctx<En2, Collage<Ty, En1, Sym, Fk1, Att1, Var, Void>> cols = new Ctx<>();
		for (Pair<En2, Block<En1, Att2>> p : blocks) {
			if (!dst0.ens.contains(p.first)) {
					throw new RuntimeException("There is a block for " + p.first + ", which is not a target entity");
			}
			Ctx<Var, En1> ctx = new Ctx<>(p.second.gens);
			for (Var v : ctx.map.keySet()) {
				En1 en = ctx.get(v);
				if (!src0.ens.contains(en)) {
					throw new RuntimeException("In block for " + p.first + ", from clause contains " + v + ":" + en + ", but " + en + " is not a source entity");
				}
			}
			Collage<Ty, En1, Sym, Fk1, Att1, Var, Void> col = new Collage<>(src0.collage());
			Ctx<String, Chc<Ty, En1>> ctx0 = unVar(ctx.inRight());
			col.gens.putAll(ctx.map);
			cols.put(p.first, col);
			Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>> eqs = new HashSet<>();
			for (Pair<RawTerm, RawTerm> eq : p.second.eqs) {
				Triple<Ctx<String,Chc<Ty,En1>>,Term<Ty,En1,Sym,Fk1,Att1,Var,Void>,Term<Ty,En1,Sym,Fk1,Att1,Var,Void>> x = RawTerm.infer1(ctx0.map, eq.first, eq.second, col, src0.typeSide.js);
				eqs.add(new Eq<>(new Ctx<>(), freeze(x.second), freeze(x.third)));
			}
			Map<String, String> uu = new HashMap<>(options);
			uu.putAll(p.second.options);
			AqlOptions theops = new AqlOptions(uu,null,env.defaults);
			//System.out.println("UI " + theops.getOrDefault(AqlOption.eval_max_temp_size));
			//System.out.println("UH " + env.defaults.getOrDefault(AqlOption.eval_max_temp_size));
			Triple<Ctx<Var, En1>, Collection<Eq<Ty, En1, Sym, Fk1, Att1, Var, Void>>, AqlOptions> b = new Triple<>(ctx, eqs, theops);
			ens0.put(p.first, b);
		}
		
		for (Pair<Att2, RawTerm> p : atts) {
			Ctx<String, Chc<Ty, En1>> ctx = unVar(ens0.get(dst0.atts.get(p.first).first).first.inRight());
			Collage<Ty, En1, Sym, Fk1, Att1, Var, Void> col = cols.get(dst0.atts.get(p.first).first);
			Chc<Ty, En1> required = Chc.inLeft(dst0.atts.get(p.first).second);
			Term<Ty, En1, Sym, Fk1, Att1, Var, Void> term = RawTerm.infer0(ctx.map, p.second, required , col, "in attribute " + p.first + ", ", src0.typeSide.js);
			atts0.put(p.first, freeze(term));
		}
		
		for (Pair<Fk2, Trans> p : fks) {
			Ctx<Var, Term<Void, En1, Void, Fk1, Void, Var, Void>> trans = new Ctx<>();
			for (Pair<Var, RawTerm> v : p.second.gens) {
				Ctx<String, Chc<Ty, En1>> ctx = unVar(ens0.get(dst0.fks.get(p.first).first).first.inRight());
				Collage<Ty, En1, Sym, Fk1, Att1, Var, Void> col = cols.get(dst0.fks.get(p.first).first);
				Chc<Ty, En1> required = Chc.inRight(ens0.get(dst0.fks.get(p.first).second).first.get(v.first));
				Term<Ty, En1, Sym, Fk1, Att1, Var, Void> term = RawTerm.infer0(ctx.map, v.second, required , col, "in foreign key " + p.first + ", ", src0.typeSide.js);
				trans.put(v.first, freeze(term).convert());
			}
			boolean doNotCheckEqs = (Boolean) new AqlOptions(p.second.options, null, env.defaults).getOrDefault(AqlOption.dont_validate_unsafe); 
			fks0.put(p.first, new Pair<>(trans, doNotCheckEqs));
		}
	
		
		boolean doNotCheckEqs = (Boolean) new AqlOptions(options, null, env.defaults).getOrDefault(AqlOption.dont_validate_unsafe); 

		boolean elimRed = (Boolean) new AqlOptions(options, null, env.defaults).getOrDefault(AqlOption.query_remove_redundancy); 

		return Query.makeQuery(ens0, atts0, fks0, src0, dst0, doNotCheckEqs, elimRed);
	}

	private Term<Ty, En1, Sym, Fk1, Att1, Var, Void> freeze(Term<Ty, En1, Sym, Fk1, Att1, Var, Void> term) {
		Map<Var,Term<Ty, En1, Sym, Fk1, Att1, Var, Void>> m = new HashMap<>();
		for (Var v : term.vars()) {
			m.put(v, Term.Gen(v));
		}
		return term.subst(m);
	}

	private static <X> Ctx<String, X> unVar(Ctx<Var, X> ctx) {
		Ctx<String, X> ret = new Ctx<>();
		for (Var v : ctx.keySet()) {
			ret.put(v.var, ctx.get(v));
		}
		return ret;
	}

//TODO aql identity query
//TODO aql compose query

	
}
