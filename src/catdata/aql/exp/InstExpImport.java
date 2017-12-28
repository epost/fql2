package catdata.aql.exp;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import catdata.Chc;
import catdata.Ctx;
import catdata.IntRef;
import catdata.Null;
import catdata.Pair;
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
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.exp.InstExpRaw.Gen;
import catdata.aql.exp.SchExpRaw.Att;
import catdata.aql.exp.SchExpRaw.En;
import catdata.aql.exp.SchExpRaw.Fk;
import catdata.aql.exp.TyExpRaw.Sym;
import catdata.aql.exp.TyExpRaw.Ty;
import catdata.aql.fdm.InitialAlgebra;
import catdata.aql.fdm.LiteralInstance;
import catdata.aql.fdm.SaturatedInstance;

//TODO: aql change type of this to not be a lie
public abstract class InstExpImport<Handle, Q>
		extends InstExp<Ty, En, Sym, Fk, Att, Gen, Null<?>, Gen, Null<?>> implements Raw {
	
	public abstract boolean equals(Object o);
	public abstract int hashCode();

	private Ctx<String, List<InteriorLabel<Object>>> raw = new Ctx<>();

	@Override
	public Ctx<String, List<InteriorLabel<Object>>> raw() {
		return raw;
	}
	
	public static <En> Gen toGen(En en, String o, boolean b, String sep) {
		if (b) {
			return new Gen(en + sep + o);
		} else {
			return new Gen(o);
		}
	}
	
	public static Gen toGen(En en, String o, AqlOptions op) {
		boolean b = (boolean) op.getOrDefault(AqlOption.prepend_entity_on_ids);
		String sep = (String) op.getOrDefault(AqlOption.import_col_seperator);
//		String pre = (String) op.getOrDefault(AqlOption.csv_import_prefix);
		return toGen(en, o, b, sep);
	}

	public Gen toGen(En en, String o) {
		return toGen(en, o, prepend_entity_on_ids, import_col_seperator);
	}
	
	public final SchExp<Ty, En, Sym, Fk, Att> schema;

	public final Map<String, String> options;

	public final Map<String, Q> map;

	@Override
	public Map<String, String> options() {
		return options;
	}

	public static IntRef counter = new IntRef(0);

	public InstExpImport(SchExp<Ty, En, Sym, Fk, Att> schema, List<Pair<LocStr, Q>> map, List<Pair<String, String>> options) {
		this.schema = schema;

		this.options = Util.toMapSafely(options);
		this.map = Util.toMapSafely(LocStr.set2(map));

		List<InteriorLabel<Object>> f = new LinkedList<>();
		for (Pair<LocStr, Q> p : map) {
			f.add(new InteriorLabel<>("imports", new Pair<>(p.first.str, p.second), p.first.loc,
					x -> x.first + " -> " + x.second).conv());
		}
		raw.put("imports", f);
	}

	@Override
	public SchExp<Ty, En, Sym, Fk, Att> type(AqlTyping G) {
		return schema;
	}

	public static  Term<Ty, Void, Sym, Void, Void, Void, Null<?>> objectToSk(
			Schema<Ty, En, Sym, Fk, Att> sch, Object rhs, Gen x, Att att,
			Ctx<Ty, Collection<Null<?>>> sks, 
			Ctx<Null<?>, Term<Ty, En, Sym, Fk, Att, Gen, Null<?>>> extraRepr, boolean shouldJS, boolean errMeansNull) {
		Ty ty = sch.atts.get(att).second;
		if (rhs == null) {
			Null<?> n = new Null<>(Term.Att(att, Term.Gen(x)));
			extraRepr.put(n, Term.Att(att, Term.Gen(x)));
			sks.get(ty).add(n);
			return Term.Sk(n);
		} else if (sch.typeSide.js.java_tys.containsKey(ty)) {
			if (shouldJS) {
				try {
					return Term.Obj(sch.typeSide.js.parse(ty, (String) rhs), ty);		
				} catch (Exception ex) {
					if (errMeansNull) {
						return objectToSk(sch, null, x, att, sks, extraRepr, shouldJS, errMeansNull);
					} else {
						ex.printStackTrace();
						throw new RuntimeException("Error while importing " + rhs + " of class " + rhs.getClass() + ".  Consider option import_null_on_err_unsafe.  Error was " + ex.getMessage() );
					}
				}
			}
			try {
				if (!Class.forName(sch.typeSide.js.java_tys.get(ty)).isInstance(rhs)) {
					if (errMeansNull) {
						return objectToSk(sch, null, x, att, sks, extraRepr, shouldJS, errMeansNull);
					} else {
						throw new RuntimeException("On " + x + "." + att + ", error while importing " + rhs + " of " + rhs.getClass() + " was expecting " + sch.typeSide.js.java_tys.get(ty) + ".\n\nConsider option " + AqlOption.import_null_on_err_unsafe);
				}
				}
			} catch (ClassNotFoundException ex) {
				Util.anomaly();
			}
			return Term.Obj(rhs, ty);
		} 
		return Util.anomaly();
	}

	protected AqlOptions op;

	protected String idCol;
	private boolean import_as_theory;
	protected boolean isJoined;
	protected boolean nullOnErr;
	protected boolean prepend_entity_on_ids;
	protected String import_col_seperator;
	protected String prefix;
	protected boolean dont_check_closure;

	protected Ctx<En, Collection<Gen>> ens0;
	protected Ctx<Ty, Collection<Null<?>>> tys0;
	protected Ctx<Gen, Ctx<Fk, Gen>> fks0;
	protected Ctx<Gen, Ctx<Att, Term<Ty, Void, Sym, Void, Void, Void, Null<?>>>> atts0;
	protected Ctx<Null<?>, Term<Ty, En, Sym, Fk, Att, Gen, Null<?>>> extraRepr;

	@Override
	public Instance<Ty, En, Sym, Fk, Att, Gen, Null<?>, Gen, Null<?>> eval(AqlEnv env) {
		Schema<Ty, En, Sym, Fk, Att> sch = schema.eval(env);
		for (Ty ty : sch.typeSide.tys) {
			if (!sch.typeSide.js.java_tys.containsKey(ty)) {
				throw new RuntimeException("Import is only allowed onto java types");
			}
		}

		 op = new AqlOptions(options, null, env.defaults);

		 import_as_theory = (boolean) op.getOrDefault(AqlOption.import_as_theory);
		 isJoined = (boolean) op.getOrDefault(AqlOption.import_joined);
		 idCol = (String) op.getOrDefault(AqlOption.id_column_name);
		 nullOnErr = (Boolean) op.getOrDefault(AqlOption.import_null_on_err_unsafe);
		 prepend_entity_on_ids = (Boolean) op.getOrDefault(AqlOption.prepend_entity_on_ids);
		 import_col_seperator = (String) op.getOrDefault(AqlOption.import_col_seperator);
		 prefix = (String) op.getOrDefault(AqlOption.csv_import_prefix);
		 dont_check_closure = (boolean) op.getOrDefault(AqlOption.import_dont_check_closure_unsafe);
		 ens0 = new Ctx<>(Util.newSetsFor0(sch.ens));
		 tys0 = new Ctx<>(Util.newSetsFor0(sch.typeSide.tys));
		 fks0 = new Ctx<>();
		 atts0 = new Ctx<>();
		 extraRepr = new Ctx<>();
			En last = null;

		try {
			Handle h = start(sch);

			if (!isJoined) {
				
				/*
				Map<En, Q> ens = new HashMap<>();
				Map<Ty, Q> tys = new HashMap<>();
				Map<Att, Q> atts = new HashMap<>();
				Map<Fk, Q> fks = new HashMap<>();
	
				for (String o : map.keySet()) {
					assertUnambig(o, sch);
					Q q = map.get(o);
					if (sch.typeSide.tys.contains(new Ty(o))) {
						tys.put(new Ty(o), q);
					} else if (sch.ens.contains(new En(o))) {
						ens.put(new En(o), q);
					} else if (sch.atts.map.containsKey(new Att(o))) {
						atts.put(new Att(o), q);
					} else if (sch.fks.map.containsKey(new Fk(o))) {
						fks.put(new Fk(o), q);
					} //TODO aql
				}
				totalityCheck(sch, ens, tys, atts, fks);
	
				for (En en : ens.keySet()) {
					shreddedEn(h, en, ens.get(en), sch);
				}
				for (Fk fk : fks.keySet()) {
					shreddedFk(h, fk, fks.get(fk), sch);
				}
				for (Att att : atts.keySet()) {
					shreddedAtt(h, att, atts.get(att), sch);
				}
	*/
				throw new RuntimeException("Unjoined form no longer supported.  To request, contact us.");
			} else {
				for (En en : sch.ens) {
					last = en;
					Q z = map.get(en.str);
					//TODO: aql: this check isn't needed
					//if (z == null) {
					//	throw new RuntimeException("No binding given for " + en);
					//}
					joinedEn(h, en, z, sch);
				}
			}
			
			end(h);
			
		} catch (Exception exn) {
			exn.printStackTrace();
			String pre = "";
			if (last != null) {
				pre = "On entity " + last + ", ";
			}
			throw new RuntimeException(pre + exn.getMessage() + "\n\n" + getHelpStr());
		}
		
		if (import_as_theory) {
			return forTheory(sch, ens0, tys0, fks0, atts0, op);
		}

		ImportAlgebra<Ty, En, Sym, Fk, Att, Gen, Null<?>> alg = new ImportAlgebra<>(sch, ens0, tys0, fks0, atts0,
				Object::toString, Object::toString, dont_check_closure);

		return new SaturatedInstance<>(alg, alg, (Boolean) op.getOrDefault(AqlOption.require_consistency),
				(Boolean) op.getOrDefault(AqlOption.allow_java_eqs_unsafe), true, extraRepr);

	}

	protected abstract String getHelpStr();

	protected abstract Handle start(Schema<Ty, En, Sym, Fk, Att> sch) throws Exception;
	
	protected abstract void end(Handle h) throws Exception;

	protected abstract void shreddedAtt(Handle h, Att att, Q s, Schema<Ty, En, Sym, Fk, Att> sch) throws Exception;

	protected abstract void shreddedFk(Handle h, Fk fk, Q s, Schema<Ty, En, Sym, Fk, Att> sch) throws Exception;

	protected abstract void shreddedEn(Handle h, En en, Q s, Schema<Ty, En, Sym, Fk, Att> sch) throws Exception;
	
	protected abstract void joinedEn(Handle h, En en, Q s, Schema<Ty, En, Sym, Fk, Att> sch) throws Exception;
	

	public static <Ty, En, Sym, Fk, Att, Gen> Instance<Ty, En, Sym, Fk, Att, Gen, Null<?>, Gen, Null<?>> forTheory(
			Schema<Ty, En, Sym, Fk, Att> sch, Ctx<En, Collection<Gen>> ens0, Ctx<Ty, Collection<Null<?>>> tys0,
			Ctx<Gen, Ctx<Fk, Gen>> fks0, Ctx<Gen, Ctx<Att, Term<Ty, Void, Sym, Void, Void, Void, Null<?>>>> atts0,
			AqlOptions op) {

		Set<Pair<Term<Ty, En, Sym, Fk, Att, Gen, Null<?>>, Term<Ty, En, Sym, Fk, Att, Gen, Null<?>>>> eqs0 = new HashSet<>();
		Collage<Ty, En, Sym, Fk, Att, Gen, Null<?>> col = new Collage<>(sch.collage());
		for (Gen gen : fks0.keySet()) {
			for (Fk fk : fks0.get(gen).keySet()) {
				eqs0.add(new Pair<>(Term.Fk(fk, Term.Gen(gen)), Term.Gen(fks0.get(gen).get(fk))));
				col.eqs.add(new Eq<>(new Ctx<>(), Term.Fk(fk, Term.Gen(gen)), Term.Gen(fks0.get(gen).get(fk))));
			}
		}
		for (Gen gen : atts0.keySet()) {
			for (Att att : atts0.get(gen).keySet()) {
				eqs0.add(new Pair<>(Term.Att(att, Term.Gen(gen)), atts0.get(gen).get(att).convert()));
				col.eqs.add(new Eq<>(new Ctx<>(), Term.Att(att, Term.Gen(gen)), atts0.get(gen).get(att).convert()));
			}
		}
		for (En en : ens0.keySet()) {
			for (Gen gen : ens0.get(en)) {
				col.gens.put(gen, en);
			}
		}
		for (Ty ty : tys0.keySet()) {
			for (Null<?> sk : tys0.get(ty)) {
				col.sks.put(sk, ty);
			}
		}

		InitialAlgebra<Ty, En, Sym, Fk, Att, Gen, Null<?>, ID> initial = new InitialAlgebra<>(op, sch, col, new It(),
				(Gen x) -> x.toString(), (Null<?> x) -> x.toString());

		Instance<Ty, En, Sym, Fk, Att, Gen, Null<?>, ID, Chc<Null<?>, Pair<ID, Att>>> I = new LiteralInstance<>(sch,
				col.gens.map, col.sks.map, eqs0, initial.dp(), initial,
				(Boolean) op.getOrDefault(AqlOption.require_consistency),
				(Boolean) op.getOrDefault(AqlOption.allow_java_eqs_unsafe));

		@SuppressWarnings("unchecked")
		Instance<Ty, En, Sym, Fk, Att, Gen, Null<?>, Gen, Null<?>> J = (Instance<Ty, En, Sym, Fk, Att, Gen, Null<?>, Gen, Null<?>>) ((Object) I);

		return J;
	}

	/*
	//different than rawterms
	private void assertUnambig(String o, Schema<Ty, En, Sym, Fk, Att> sch) {
		int i = 0;
		if (sch.typeSide.tys.contains(new Ty(o))) {
			i++;
		}
		if (sch.ens.contains(new En(o))) {
			i++;
		}
		for (En en : sch.ens) {
			if (sch.fks.map.containsKey(new Fk(en, o))) {
				i++;
			}
			if (sch.atts.map.containsKey(new Att(en, o))) {
				i++;
			}
		}
		if (i > 1) {
			throw new RuntimeException(o + " is ambiguously a type/entity/attribute/foreign key");
		} else if (i == 0) {
			throw new RuntimeException(o + " is not a type/entity/attribute/foreign key");
		}
	} */
	
	private void totalityCheck(Schema<Ty, En, Sym, Fk, Att> sch, Map<En, Q> ens, Map<Ty, Q> tys,
			Map<Att, Q> atts, Map<Fk, Q> fks) {
		// for (En En : sch.ens) {
		// if (!ens.containsKey(En)) {
		// throw new RuntimeException("no query for " + En);
		// }
		// }
		for (En En : ens.keySet()) {
			if (!sch.ens.contains(En)) {
				throw new RuntimeException("there is an import for " + En + ", which is not an entity in the schema");
			}
		}
		for (Ty ty : tys.keySet()) {
			if (!sch.ens.contains(ty)) {
				throw new RuntimeException("there is an import for " + ty + ", which is not a type in the schema");
			}
		}
		// for (Att Att : sch.atts.keySet()) {
		// if (!atts.containsKey(Att)) {
		// throw new RuntimeException("no query for attribute " + Att);
		// }
		// }
		for (Att Att : atts.keySet()) {
			if (!sch.atts.containsKey(Att)) {
				throw new RuntimeException("there is an import for " + Att + ", which is not an attribute in the schema");
			}
		}
		// for (Fk Fk : sch.fks.keySet()) {
		// if (!fks.containsKey(Fk)) {
		// throw new RuntimeException("no query for foreign key " + Fk);
		// }
		// }
		for (Fk Fk : fks.keySet()) {
			if (!sch.fks.containsKey(Fk)) {
				throw new RuntimeException("there is an import for " + Fk + ", which is not a foreign key in the schema");
			}
		}
	}

	@Override
	public Collection<Pair<String, Kind>> deps() {
		return schema.deps();
	}
/*
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((map == null) ? 0 : map.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		result = prime * result + ((schema == null) ? 0 : schema.hashCode());
		return result;
	}

	//TODO aql important to use instanceof here
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof InstExpImport))
			return false;
		InstExpImport<?, ?> other = (InstExpImport<?, ?>) obj;
		if (map == null) {
			if (other.map != null)
				return false;
		} else if (!map.equals(other.map))
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

	*/

}


