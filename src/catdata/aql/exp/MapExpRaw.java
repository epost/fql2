package catdata.aql.exp;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.swing.tree.DefaultMutableTreeNode;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Collage;
import catdata.aql.Kind;
import catdata.aql.Mapping;
import catdata.aql.RawTerm;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.Var;

public final class MapExpRaw extends MapExp<Object,Object,Object,Object,Object,Object,Object,Object> {
	
	@Override
	public void asTree(DefaultMutableTreeNode root, boolean alpha) {
		if (imports.size() > 0) { 
			DefaultMutableTreeNode n = new DefaultMutableTreeNode();
			n.setUserObject("imports");
			for (Object t : Util.alphaMaybe(alpha, imports)) {
				DefaultMutableTreeNode m = new DefaultMutableTreeNode();
				m.setUserObject(t.toString());
				n.add(m);
			}
		}
		if (ens.size() > 0) { 
			DefaultMutableTreeNode n = new DefaultMutableTreeNode();
			n.setUserObject("entities");
			for (Pair<Object, Object> t : Util.alphaMaybe(alpha, ens)) {
				DefaultMutableTreeNode m = new DefaultMutableTreeNode();
				m.setUserObject(t.first + " -> " + t.second);
				n.add(m);
			}
			root.add(n);
		}
		if (fks.size() > 0) { 
			DefaultMutableTreeNode n = new DefaultMutableTreeNode();
			n.setUserObject("eqs");
			for (Pair<Object, List<Object>> t : Util.alphaMaybe(alpha, fks)) {
				DefaultMutableTreeNode m = new DefaultMutableTreeNode();
				m.setUserObject(t.first + " -> " + Util.sep(t.second, "."));
				n.add(m);
			}
			root.add(n);
		}
		if (atts.size() > 0) { 
			DefaultMutableTreeNode n = new DefaultMutableTreeNode();
			n.setUserObject("atts");
			for (Pair<Object, Triple<String, Object, RawTerm>> t : Util.alphaMaybe(alpha, atts)) {
				DefaultMutableTreeNode m = new DefaultMutableTreeNode();
				m.setUserObject(t.first + " -> \\" + t.second + ". " + t.second.third);
				n.add(m);
			}
			root.add(n);
		}
		
	}
	
	@Override
	public Collection<Pair<String, Kind>> deps() {
		Set<Pair<String, Kind>> ret = new HashSet<>();
		ret.addAll(src.deps());
		ret.addAll(dst.deps());
		ret.addAll(imports.stream().map(x -> new Pair<>(x, Kind.MAPPING)).collect(Collectors.toList()));
		return ret;
	}
	private final SchExp<Object,Object,Object,Object,Object> src;
    private final SchExp<Object,Object,Object,Object,Object> dst;
	
	private final List<String> imports;
	
	private final List<Pair<Object, Object>> ens;
	private final List<Pair<Object, List<Object>>> fks;
	private final List<Pair<Object, Triple<String, Object, RawTerm>>> atts;
	
	private final Map<String, String> options; 
	
	@Override
	public Map<String, String> options() {
		return options;
	}
	
	//typesafe by covariance of read only collections
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MapExpRaw(SchExp<?, ?, ?, ?, ?> src, SchExp<?, ?, ?, ?, ?> dst, List<String> imports, List<Pair<String, String>> ens, List<Pair<String, List<String>>> fks, List<Pair<String, Triple<String, String, RawTerm>>> atts, List<Pair<String, String>> options) {
		this.src = (SchExp<Object, Object, Object, Object, Object>) src;
		this.dst = (SchExp<Object, Object, Object, Object, Object>) dst;
		this.imports = imports;
		this.ens = new LinkedList(ens);
		this.fks = new LinkedList(fks);
		this.atts = new LinkedList(atts);
		this.options = Util.toMapSafely(options);
		Util.toMapSafely(this.ens);
		Util.toMapSafely(this.fks);
		Util.toMapSafely(this.atts); //do here rather than wait
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
		
		if (!ens.isEmpty()) {
			toString += "\tentities";
					
			for (Pair<Object, Object> x : ens) {
				temp.add(x.first + " -> " + x.second);
			}
			
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		
		if (!fks.isEmpty()) {
			toString += "\tforeign_keys";
			temp = new LinkedList<>();
			for (Pair<Object, List<Object>> sym : fks) {
				temp.add(sym.first + " -> " + Util.sep(sym.second, "."));
			}
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		
		if (!fks.isEmpty()) {
			toString += "\tattributes";
			temp = new LinkedList<>();
			for (Pair<Object, Triple<String, Object, RawTerm>> sym : atts) {
				temp.add(sym.first + " -> lambda " + sym.second.first + ". " + sym.second.third);
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
		
		return "literal : " + src + " -> " + dst + " {\n" + toString + "}";
	} 


	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((atts == null) ? 0 : atts.hashCode());
		result = prime * result + ((dst == null) ? 0 : dst.hashCode());
		result = prime * result + ((ens == null) ? 0 : ens.hashCode());
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
		MapExpRaw other = (MapExpRaw) obj;
		if (atts == null) {
			if (other.atts != null)
				return false;
		} else if (!atts.equals(other.atts))
			return false;
		if (dst == null) {
			if (other.dst != null)
				return false;
		} else if (!dst.equals(other.dst))
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
		if (src == null) {
			if (other.src != null)
				return false;
		} else if (!src.equals(other.src))
			return false;
		return true;
	}

	
		
	@Override
	public Mapping<Object, Object, Object, Object, Object, Object, Object, Object> eval(AqlEnv env) {
		Schema<Object, Object, Object, Object, Object> src0 = src.eval(env);
		Schema<Object, Object, Object, Object, Object> dst0 = dst.eval(env);
		//Collage<Object, Object, Object, Object, Object, Void, Void> scol = new Collage<>(src0);
		Collage<Object, Object, Object, Object, Object, Void, Void> dcol = new Collage<>(dst0.collage());
		
		Map<Object, Object> ens0 = new HashMap<>();
		Map<Object, Pair<Object, List<Object>>> fks0 = new HashMap<>();
		Map<Object, Triple<Var, Object, Term<Object, Object, Object, Object, Object, Void, Void>>> atts0 = new HashMap<>();
		for (String k : imports) {
			@SuppressWarnings("unchecked")
			Mapping< Object, Object, Object, Object, Object, Object, Object, Object> v = env.defs.maps.get(k);
			Util.putAllSafely(ens0, v.ens.map);
			Util.putAllSafely(fks0, v.fks.map);
			Util.putAllSafely(atts0, v.atts.map);
		}
		
		Util.putAllSafely(ens0, Util.toMapSafely(ens));
		List<Pair<Object, Pair<Object, List<Object>>>> fksX = new LinkedList<>();
		for (Pair<Object, List<Object>> p : fks) {
			Object start_en = null;
			List<Object> r = new LinkedList<>();
			for (Object o : p.second) {
				if (ens0.containsValue(o)) {
					if (fks0.containsKey(o)) {
						throw new RuntimeException("in foreign key mapping " + p.first + " -> " + Util.sep(p.second, ".") + ", " + o + " is both a target foreign key and a target entity, so the path is ambiguous");
					}
					if (start_en == null) {
						start_en = p.second.get(0);
					}
				} else {
					if (start_en == null) {
						Pair<Object, Object> j = dst0.fks.get(o);
						if (j == null) {
							throw new RuntimeException("in foreign key mapping " + p.first + " -> " + Util.sep(p.second, ".") + ", " + p.second.get(0) + " is not a foreign key in the target");
						}
						start_en = j.first;
					}
					r.add(o);
				}
			}
			if (start_en == null) {
				throw new RuntimeException("Anomaly: please report");
			}
			fksX.add(new Pair<>(p.first, new Pair<>(start_en, r)));
		}
		Util.putAllSafely(fks0, Util.toMapSafely(fksX));
		
		for (Pair<Object, Triple<String, Object, RawTerm>> att : atts) {
			String var = att.second.first;
			Object var_en = att.second.second;
			RawTerm term = att.second.third;

			Pair<Object, Object> p = src0.atts.map.get(att.first);
			if (p == null) {
				throw new RuntimeException("in mapping for " + att.first + ", " + att.first + " is not a source attribute.");
			} 
			Object src_att_dom_en = p.first;
			Object dst_att_dom_en = ens0.get(src_att_dom_en);
			if (dst_att_dom_en == null) {
				throw new RuntimeException("in mapping for " + att.first + ", no entity mapping for " + src_att_dom_en + " , required for domain for " + att.first);
			}
			
			if (var_en != null && !var_en.equals(dst_att_dom_en)) {
				throw new RuntimeException("in mapping for " + att.first + ", the given source entity for the variable, " + var_en + ", is not " + dst_att_dom_en + " as expected.");
			}
										
			Object src_att_cod_ty = p.second;
			if (!dst0.typeSide.tys.contains(src_att_cod_ty)) {
				throw new RuntimeException("in mapping for " + att.first + ", type " + p.second + " does not exist in target typeside.");
			}
			Chc<Object,Object> proposed_ty2 = Chc.inLeft(src_att_cod_ty);	
			
			Chc<Object, Object> var_en2 = Chc.inRight(dst_att_dom_en);
			
			Map<String, Chc<Object, Object>> ctx = Util.singMap(var, var_en2);
	
			Term<Object, Object, Object, Object, Object, Void, Void> term0 = RawTerm.infer0(ctx, term, proposed_ty2, dcol, "In checking mapping for attribute " + att.first + ", ", src0.typeSide.js);

			Util.putSafely(atts0, att.first, new Triple<>(new Var(var), dst_att_dom_en, term0));
		} 
		
		AqlOptions ops = new AqlOptions(options, null, env.defaults);
		
		Mapping<Object, Object, Object, Object, Object, Object, Object, Object> ret = new Mapping<>(ens0, atts0, fks0, src0, dst0, (Boolean) ops.getOrDefault(AqlOption.dont_validate_unsafe));
		return ret; 
	}

	@Override
	public Pair<SchExp<Object, Object, Object, Object, Object>, SchExp<Object, Object, Object, Object, Object>> type(AqlTyping G) {
		return new Pair<>(src, dst);
	}
}