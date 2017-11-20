package catdata.aql.exp;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.ColimitSchema;
import catdata.aql.Collage;
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

public final class ColimSchExpModify<N> extends ColimSchExp<N> implements Raw {
	
	private Ctx<String, List<InteriorLabel<Object>>> raw = new Ctx<>();
	
	@Override 
	public Ctx<String, List<InteriorLabel<Object>>> raw() {
		return raw;
	}
	
	@Override
	public SchExp<Ty, En, Sym, Fk, Att> getNode(N n, AqlTyping G) {
		return colim.getNode(n, G);
	}
	
	public final ColimSchExp<N> colim;
	
	public final List<Pair<String, String>> ens;

	public final List<Pair<Pair<String,String>, String>> fks0;

	public final List<Pair<Pair<String,String>, String>> atts0;
	public final List<Pair<Pair<String,String>, List<String>>> fks;
	public final List<Pair<Pair<String,String>, Triple<String, String, RawTerm>>> atts;
	
	public final Map<String, String> options; 
	
	@Override
	public Map<String, String> options() {
		return options;
	}
	
	public ColimSchExpModify(ColimSchExp<N> colim, List<Pair<LocStr, String>> ens, List<Pair<Pair<String, LocStr>, String>> fks0, List<Pair<Pair<String,LocStr>, String>> atts0, List<Pair<Pair<String, LocStr>, List<String>>> fks, List<Pair<Pair<String,LocStr>, Triple<String, String, RawTerm>>> atts, List<Pair<String, String>> options) {
		this.ens = LocStr.list2(ens);
		this.atts = LocStr.list2x(atts);
		this.fks = LocStr.list2x(fks);
		this.fks0 = LocStr.list2x(fks0);
		
		this.atts0= LocStr.list2x(atts0);
		this.options = Util.toMapSafely(options);
		Util.toMapSafely(this.ens);
		Util.toMapSafely(this.fks);
		Util.toMapSafely(this.atts); //do here rather than wait
		this.colim = colim;
					
		List<InteriorLabel<Object>> f = new LinkedList<>();
		for (Pair<LocStr, String> p : ens) {
			f.add(new InteriorLabel<>("rename_entities", new Pair<>(p.first.str, p.second), p.first.loc,
					x -> x.first + " -> " + x.second).conv());
		}
		raw.put("rename_entities", f);
		
		f = new LinkedList<>();
		for (Pair<Pair<String, LocStr>, String> p : fks0) {
			f.add(new InteriorLabel<>("rename_fks", new Pair<>(p.first.second.str, p.second), p.first.second.loc,
					x -> x.first + " -> " + x.second).conv());
		}
		raw.put("rename_fks", f);
		
		f = new LinkedList<>();
		for (Pair<Pair<String, LocStr>, String> p : atts0) {
			f.add(new InteriorLabel<>("rename_atts", new Pair<>(p.first.second.str, p.second), p.first.second.loc,
					x -> x.first + " -> " + x.second).conv());
		}
		raw.put("rename_atts", f);
		
		f = new LinkedList<>();
		for (Pair<Pair<String, LocStr>, List<String>> p : fks) {
			f.add(new InteriorLabel<>("remove_fks", new Pair<>(p.first.second.str, p.second), p.first.second.loc,
					x -> x.first + " -> " + Util.sep(x.second, ".")).conv());
		}
		raw.put("remove_fks", f);
		
		f = new LinkedList<>();
		for (Pair<Pair<String, LocStr>, Triple<String, String, RawTerm>> p : atts) {
			f.add(new InteriorLabel<>("remove_atts", new Pair<>(p.first.second.str, p.second), p.first.second.loc,
					x -> x.first + " -> \\" + x.second.first + ". " + x.second.third).conv());
		}
		raw.put("remove_atts", f);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((atts == null) ? 0 : atts.hashCode());
		result = prime * result + ((atts0 == null) ? 0 : atts0.hashCode());
		result = prime * result + ((colim == null) ? 0 : colim.hashCode());
		result = prime * result + ((ens == null) ? 0 : ens.hashCode());
		result = prime * result + ((fks == null) ? 0 : fks.hashCode());
		result = prime * result + ((fks0 == null) ? 0 : fks0.hashCode());
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
		ColimSchExpModify<?> other = (ColimSchExpModify<?>) obj;
		if (atts == null) {
			if (other.atts != null)
				return false;
		} else if (!atts.equals(other.atts))
			return false;
		if (atts0 == null) {
			if (other.atts0 != null)
				return false;
		} else if (!atts0.equals(other.atts0))
			return false;
		if (colim == null) {
			if (other.colim != null)
				return false;
		} else if (!colim.equals(other.colim))
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
		if (fks0 == null) {
			if (other.fks0 != null)
				return false;
		} else if (!fks0.equals(other.fks0))
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
	public synchronized String toString() {
		if (toString != null) {
			return toString;
		}
		toString = "";
			
		List<String> temp = new LinkedList<>();
		
		if (!ens.isEmpty()) {
			toString += "\trename entities";
					
			for (Pair<String, String> x : ens) {
				temp.add(x.first + " -> " + x.second);
			}
			
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		
		if (!fks0.isEmpty()) {
			toString += "\trename foreign_keys";
					
			for (Pair<Pair<String, String>, String> x : fks0) {
				temp.add(x.first.first + "." + x.first.second + " -> " + x.second);
			}
			
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		
		if (!atts0.isEmpty()) {
			toString += "\trename attributes";
					
			for (Pair<Pair<String, String>, String> x : atts0) {
				temp.add(x.first.second + " -> " + x.second);
			}
			
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		
		if (!fks.isEmpty()) {
			toString += "\tremove foreign_keys";
			temp = new LinkedList<>();
			for (Pair<Pair<String, String>, List<String>> sym : fks) {
				temp.add(sym.first.first + "." + sym.first.second + " -> " + Util.sep(sym.second, "."));
			}
			toString += "\n\t\t" + Util.sep(temp, "\n\t\t") + "\n";
		}
		
		if (!fks.isEmpty()) {
			toString += "\tremove attributes";
			temp = new LinkedList<>();
			for (Pair<Pair<String, String>, Triple<String, String, RawTerm>> sym : atts) {
				temp.add(sym.first.second + " -> lambda " + sym.second.first + ". " + sym.second.third);
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
		
		toString = "modify " + colim + " {\n" + toString + "}";
		return toString;
	} 

	//TODO aql add options
	@Override
	public ColimitSchema<N> eval(AqlEnv env) {
		boolean checkJava = ! (Boolean) env.defaults.getOrDefault(options, AqlOption.allow_java_eqs_unsafe);
		ColimitSchema<N> colim0 = colim.eval(env);
		//colim0.schemaStr.co
		for (Pair<String, String> k : ens) {
			colim0 = colim0.renameEntity(new En(k.first), new En(k.second), checkJava);
		}
		for (Pair<Pair<String, String>, String> k : fks0) {
			colim0 = colim0.renameFk(new Fk(new En(k.first.first), k.first.second), new Fk(new En(k.first.first), k.second), checkJava);
		}
		for (Pair<Pair<String, String>, String> k : atts0) {
			colim0 = colim0.renameAtt(new Att(new En(k.first.first), k.first.second), new Att(new En(k.first.first), k.second), checkJava);
		}
		for (Pair<Pair<String, String>, List<String>> k : fks) {
			if (!colim0.schemaStr.fks.containsKey(new Fk(new En(k.first.first),k.first.second))) {
				throw new RuntimeException("Not an fk: " + k.first + " in\n\n" + colim0.schemaStr);
			}
			String pre = "In processing " + k.first + " -> " + k.second  +", ";
			Collage<Ty, En, Sym, Fk, Att,Void,Void> xxx = colim0.schemaStr.collage();
			RawTerm term = RawTerm.fold(k.second, "v");
			En tr = colim0.schemaStr.fks.get(new Fk(new En(k.first.first),k.first.second)).second;

			Ctx<String,Chc<Ty,En>> ctx = new Ctx<>("v", Chc.inRight(new En(k.first.first)));
			Term<Ty, En, Sym, Fk, Att, Gen, Sk> t = 
			RawTerm.infer1x(ctx.map, term, null, Chc.inRight(tr), xxx.convert(), pre, colim0.schemaStr.typeSide.js).second;
			//colim0 = colim0.removeAtt(new Att(k.first), new Var(k.second.first), t.convert(), checkJava);
			
			
			colim0 = colim0.removeFk(new Fk(new En(k.first.first), k.first.second), t.toFkList(), checkJava);
		}
		for (Pair<Pair<String, String>, Triple<String, String, RawTerm>> k : atts) {
			if (!colim0.schemaStr.atts.containsKey(new Att(new En(k.first.first), k.first.second))) {
				throw new RuntimeException("Not an attribute: " + k.first + " in\n\n" + colim0.schemaStr);
			}
			String pre = "In processing " + k.first + " -> lambda " + k.second.first + "." + k.second.third + ", ";
			Pair<En, Ty> r = colim0.schemaStr.atts.get(new Att(new En(k.first.first), k.first.second));
			if (k.second.second != null && !k.second.second.equals(r.first)) {
				throw new RuntimeException(pre + " given type is " + k.second.second + " but expected " + r.first);
			}
			Collage<Ty, En, Sym, Fk, Att,Void,Void> xxx = colim0.schemaStr.collage();
			Ctx<String,Chc<Ty,En>> ctx = new Ctx<>(k.second.first, Chc.inRight(r.first));
			Term<Ty, En, Sym, Fk, Att, Gen, Sk> t = 
			RawTerm.infer1x(ctx.map, k.second.third, null, Chc.inLeft(r.second), xxx.convert(), pre, colim0.schemaStr.typeSide.js).second;
			colim0 = colim0.removeAtt(new Att(new En(k.first.first), k.first.second), new Var(k.second.first), t.convert(), checkJava);
		}
		
		return colim0;
	
	}


	@Override
	public ColimSchExp<N> type(AqlTyping G) {
		return colim.type(G);
	}

	@Override
	public Collection<Pair<String, Kind>> deps() {
		return colim.deps();
	}
	
}