package catdata.opl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.ide.Environment;
import catdata.ide.Program;
import catdata.ide.Util;
import catdata.opl.OplExp.OplApply;
import catdata.opl.OplExp.OplDelta;
import catdata.opl.OplExp.OplDelta0;
import catdata.opl.OplExp.OplEval;
import catdata.opl.OplExp.OplExpVisitor;
import catdata.opl.OplExp.OplFlower;
import catdata.opl.OplExp.OplId;
import catdata.opl.OplExp.OplInst;
import catdata.opl.OplExp.OplInst0;
import catdata.opl.OplExp.OplJavaInst;
import catdata.opl.OplExp.OplMapping;
import catdata.opl.OplExp.OplPivot;
import catdata.opl.OplExp.OplPres;
import catdata.opl.OplExp.OplPresTrans;
import catdata.opl.OplExp.OplPushout;
import catdata.opl.OplExp.OplPushoutBen;
import catdata.opl.OplExp.OplPushoutSch;
import catdata.opl.OplExp.OplSCHEMA0;
import catdata.opl.OplExp.OplSat;
import catdata.opl.OplExp.OplSchema;
import catdata.opl.OplExp.OplSchemaProj;
import catdata.opl.OplExp.OplSetInst;
import catdata.opl.OplExp.OplSetTrans;
import catdata.opl.OplExp.OplSig;
import catdata.opl.OplExp.OplSigma;
import catdata.opl.OplExp.OplTyMapping;
import catdata.opl.OplExp.OplUberSat;
import catdata.opl.OplExp.OplUnSat;
import catdata.opl.OplExp.OplUnion;
import catdata.opl.OplExp.OplVar;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class OplOps implements OplExpVisitor<OplObject, Program<OplExp>> {

	Environment<OplObject> ENV;

	public OplOps(Environment<OplObject> env) {
		this.ENV = env;
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplPivot e) {
		OplObject o = ENV.get(e.I0);
		if (o instanceof OplInst) {
			OplInst I = (OplInst) o;
			e.validate(I);
			return e;
		}
		throw new RuntimeException("Not an instnce: " + e.I0);
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplSig e) {
		e.validate();
		return e;
	}

	@Override
	public OplObject visit(Program<OplExp> Env, OplDelta0 e) {
		OplObject o = ENV.get(e.F0);
		if (o instanceof OplTyMapping) {
			OplTyMapping F = (OplTyMapping) o;
			e.validate(F);
			return e.toQuery();
		}
		throw new RuntimeException("Not a typed mapping: " + e.F0);
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplSCHEMA0 e) {
		OplObject t0 = ENV.get(e.typeSide);
		if (!(t0 instanceof OplSig)) {
			throw new RuntimeException("Not a theory: " + e.typeSide);
		}
		OplSig t = (OplSig) t0;
		if (!t.implications.isEmpty()) {
			throw new RuntimeException("Can't use implications with SCHEMA");
		}

		OplSchema ret = new OplSchema("?", e.entities);
		Map prec = new HashMap();
		prec.putAll(t.prec);
		prec.putAll(e.prec);

		Set sorts = new HashSet();
		sorts.addAll(t.sorts);
		sorts.addAll(e.entities);

		Map symbols = new HashMap();
		symbols.putAll(t.symbols);
		symbols.putAll(e.attrs);
		symbols.putAll(e.edges);

		List equations = new LinkedList();
		equations.addAll(t.equations);
		equations.addAll(e.pathEqs);
		equations.addAll(e.obsEqs);

		OplSig sig = new OplSig(t.fr, prec, sorts, symbols, equations);
		ret.validate(sig);
		e.validate(sig);
		return ret;
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplSetInst e) {
		OplObject o = ENV.get(e.sig);
		if (!(o instanceof OplSig)) {
			throw new RuntimeException("Not a theory: " + o + " . is "
					+ o.getClass());
		}
		OplSig s = (OplSig) o;
		e.validate(s);
		return e;
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplEval e) {
		OplObject i = ENV.get(e.I);
		if (i instanceof OplSetInst) {
			OplSetInst i0 = (OplSetInst) i;
			OplObject s = ENV.get(i0.sig);
			OplSig s0 = (OplSig) s;
			e.e.type(s0, new OplCtx<String, String>());
			return new OplString(e.e.eval(s0, i0, new OplCtx<String, String>())
					.toString());
		}

		if (i instanceof OplJavaInst) {
			OplJavaInst i0 = (OplJavaInst) i;
			OplObject s = ENV.get(i0.sig);
			OplSig s0 = (OplSig) s;
			e.e.type(s0, new OplCtx<String, String>());
			try {
				return new OplString(OplTerm.strip(OplToKB.redBy(i0,
						OplToKB.convert(e.e.inLeft())).toString()));
				// return new
				// OplString(e.e.eval((Invocable)i0.engine).toString());
			} catch (Exception ee) {
				ee.printStackTrace();
				throw new RuntimeException(ee.getMessage());
			}
		}
		throw new RuntimeException("Not a set/js model: " + e.I);
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplVar e) {
		return ENV.get(e.name);
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplSetTrans e) {
		OplObject src = ENV.get(e.src);
		OplObject dst = ENV.get(e.dst);
		if (!(src instanceof OplSetInst)) {
			throw new RuntimeException("Source is not a model in " + e);
		}
		if (!(dst instanceof OplSetInst)) {
			throw new RuntimeException("Target is not a model in " + e);
		}
		OplSetInst src0 = (OplSetInst) src;
		OplSetInst dst0 = (OplSetInst) dst;
		if (!src0.sig.equals(dst0.sig)) {
			throw new RuntimeException(
					"Theories of source and target do not match in " + e);
		}
		OplSig sig = (OplSig) ENV.get(src0.sig);
		e.validate(sig, src0, dst0);
		return e;
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplJavaInst e) {
		OplObject sig = ENV.get(e.sig);
		if (!(sig instanceof OplSig)) {
			throw new RuntimeException("Not a signature: " + e.sig);
		}
		OplSig sig0 = (OplSig) sig;
		e.validate(sig0, ENV);
		return e;
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplMapping e) {
		OplObject src = ENV.get(e.src0);
		OplObject dst = ENV.get(e.dst0);
		if (src instanceof OplSig && dst instanceof OplSig) {
			OplSig src0 = (OplSig) src;
			OplSig dst0 = (OplSig) dst;
			e.validate(src0, dst0);
			return e;
		} else if (src instanceof OplSchema && dst instanceof OplSchema) {
			OplSchema src0 = (OplSchema) src;
			OplSchema dst0 = (OplSchema) dst;
			// e.validate(src0.projEA(), dst0.projEA());
			OplTyMapping ret = new OplTyMapping<>(e.src0, e.dst0, src0, dst0, e);
			return ret;
		}
		throw new RuntimeException(
				"Source or Target is not a theory/schema in " + e);
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplDelta e) {
		OplObject F = ENV.get(e.F);
		if (!(F instanceof OplMapping)) {
			throw new RuntimeException("Not a mapping: " + e.F);
		}
		OplMapping F0 = (OplMapping) F;

		OplObject I = ENV.get(e.I);
		if (I instanceof OplSetInst) {
			OplSetInst I0 = (OplSetInst) I;
			return F0.delta(I0);
		}
		if (I instanceof OplSetTrans) {
			OplSetTrans h = (OplSetTrans) I;
			return F0.delta(h);
		}

		throw new RuntimeException("Not a model or transform: " + e.I);
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplPres e) {
		OplObject i = ENV.get(e.S);
		if (i instanceof OplSig) {
			OplSig S = (OplSig) i;
			OplPres ret = OplPres.OplPres0(e.prec, e.S, S, e.gens, e.equations);
			ret.toSig();
			return ret;
		} else if (i instanceof OplSchema) {
			OplSchema S = (OplSchema) i;
			OplPres ret = OplPres.OplPres0(e.prec, e.S, S.sig, e.gens,
					e.equations);
			ret.toSig();
			return ret;
		} else {
			throw new RuntimeException("Not a presentation or schema: " + e.S);
		}
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplPushout e) {
		OplObject s1 = ENV.get(e.s1);
		OplObject s2 = ENV.get(e.s2);
		if (!(s1 instanceof OplPresTrans)) {
			throw new RuntimeException(e.s1 + " is not a transform");
		}
		if (!(s2 instanceof OplPresTrans)) {
			throw new RuntimeException(e.s2 + " is not a transform");
		}
		e.validate((OplPresTrans) s1, (OplPresTrans) s2);
		return e;
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplPushoutSch e) {
		OplObject s1 = ENV.get(e.s1);
		OplObject s2 = ENV.get(e.s2);
		if (!(s1 instanceof OplTyMapping)) {
			throw new RuntimeException(e.s1 + " is not a ty mapping");
		}
		if (!(s2 instanceof OplTyMapping)) {
			throw new RuntimeException(e.s2 + " is not a ty mapping");
		}
		e.validate((OplTyMapping) s1, (OplTyMapping) s2);
		return e.pushout();
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplPushoutBen e) {
		OplObject s1 = ENV.get(e.s1);
		OplObject s2 = ENV.get(e.s2);
		if (!(s1 instanceof OplMapping)) {
			throw new RuntimeException(e.s1 + " is not a mapping");
		}
		if (!(s2 instanceof OplMapping)) {
			throw new RuntimeException(e.s2 + " is not a mapping");
		}
		e.validate((OplMapping) s1, (OplMapping) s2);
		return e.pushout();
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplSat e) {
		OplObject i = ENV.get(e.I);
		if (i instanceof OplPres) {
			OplPres S = (OplPres) i;
			return OplSat.saturate(S);
		}
		if (i instanceof OplSig) {
			OplSig S = (OplSig) i;
			return S.saturate(e.I);
		}
		throw new RuntimeException("Not a presentation or theory");
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplUberSat e) {
		OplObject p = ENV.get(e.P);
		if (!(p instanceof OplPres)) {
			throw new RuntimeException("Not a presentation: " + e.P);
		}
		OplPres S = (OplPres) p;

		OplObject i = ENV.get(e.I);
		if (!(i instanceof OplJavaInst)) {
			throw new RuntimeException("Not a javascript model: " + e.I);
		}
		OplJavaInst I = (OplJavaInst) i;

		return OplUberSat.saturate(I, S);
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplUnSat e) {
		OplObject i = ENV.get(e.I);
		if (!(i instanceof OplSetInst)) {
			throw new RuntimeException("Not a model: " + e.I);
		}
		OplSetInst S = (OplSetInst) i;
		return OplUnSat.desaturate(S.sig, S);
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplSigma e) {

		OplObject F = ENV.get(e.F);
		OplObject I = ENV.get(e.I);

		if (F instanceof OplMapping) {
			OplMapping F0 = (OplMapping) F;
			if (I instanceof OplPres) {
				OplPres I0 = (OplPres) I;
				return F0.sigma(I0);
			} else if (I instanceof OplPresTrans) {
				OplPresTrans h = (OplPresTrans) I;
				return F0.sigma(h);
			}
			throw new RuntimeException(
					"Not a presentation of an instance or transform: " + e.I);
		}
		if (F instanceof OplTyMapping) {
			OplTyMapping F0 = (OplTyMapping) F;
			if (I instanceof OplInst) {
				OplInst I0 = (OplInst) I;
				OplInst ret = new OplInst<>(F0.dst.sig0, "?", I0.J0);
				ret.validate(F0.dst, F0.extend().sigma(I0.P), I0.J);
				return ret;
			} else if (I instanceof OplPresTrans) {
				OplPresTrans h = (OplPresTrans) I;
				OplPresTrans z = F0.extend().sigma(h);
				z.src1 = new OplInst<>("?", "?", h.src1.J0);
				z.dst1 = new OplInst<>("?", "?", h.src1.J0);
				z.src1.validate(F0.dst, z.src, h.src1.J);
				z.dst1.validate(F0.dst, z.dst, h.src1.J);
				return z;
			}
			throw new RuntimeException("Not an instance: " + e.I);

		}
		throw new RuntimeException("Not a mapping: " + e.F);
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplPresTrans e) {
		OplObject src = ENV.get(e.src0);
		OplObject dst = ENV.get(e.dst0);
		if (src instanceof OplPres && dst instanceof OplPres) {
			OplPres src0 = (OplPres) src;
			OplPres dst0 = (OplPres) dst;
			e.validateNotReally(src0, dst0); // ?
			// e.toMapping(); redundant
			return e;
		} else if (src instanceof OplInst && dst instanceof OplInst) {
			OplInst src0 = (OplInst) src;
			OplInst dst0 = (OplInst) dst;
			e.validateNotReally(src0, dst0); // ?
			// e.toMapping(); redundant
			return e;
		}
		throw new RuntimeException(
				"Source or target is not a presentation or instance in " + e);
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplFlower e) {
		OplObject I0 = ENV.get(e.I0);
		if (I0 instanceof OplSetInst) {
			OplSetInst I = (OplSetInst) I0;
			return (OplObject) e.eval(I).second;
		}
		if (I0 instanceof OplSetTrans) {
			OplSetTrans h = (OplSetTrans) I0;
			return e.eval(h);
		}
		throw new RuntimeException("Not a set model or transform: " + e.I0);
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplSchema e) {
		OplObject I0 = ENV.get(e.sig0);
		if (I0 instanceof OplSig) {
			OplSig I = (OplSig) I0;
			e.validate(I);
			return e;
		}
		throw new RuntimeException("Not a theory: " + e.sig0);
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplSchemaProj e) {
		OplObject I0 = ENV.get(e.sch0);
		if (I0 instanceof OplSchema) {
			OplSchema I = (OplSchema) I0;
			return e.proj(I);
		}
		throw new RuntimeException("Not a schema: " + e.sch0);
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplInst e) {
		OplSchema S;
		OplPres P;
		OplJavaInst J = null;

		OplObject S0 = ENV.get(e.S0);
		if (S0 instanceof OplSchema) {
			S = (OplSchema) S0;
		} else {
			throw new RuntimeException("Not a schema: " + e.S0);
		}

		OplObject P0 = ENV.get(e.P0);
		if (P0 instanceof OplPres) {
			P = (OplPres) P0;
		} else {
			throw new RuntimeException("Not a presentation: " + e.P0);
		}

		if (e.J0.equals("none")) {

		} else {
			OplObject J0 = ENV.get(e.J0);
			if (J0 instanceof OplJavaInst) {
				J = (OplJavaInst) J0;
			} else {
				throw new RuntimeException("Not a JS model: " + e.J0);
			}
		}

		e.validate(S, P, J);
		return e;
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplQuery e) {
		OplSchema I, J;

		OplObject I0 = ENV.get(e.src_e);
		if (I0 instanceof OplSchema) {
			I = (OplSchema) I0;
		} else {
			throw new RuntimeException("Not a schema: " + e.src_e);
		}

		OplObject J0 = ENV.get(e.dst_e);
		if (J0 instanceof OplSchema) {
			J = (OplSchema) J0;
		} else {
			throw new RuntimeException("Not a schema: " + e.dst_e);
		}

		e.validate(I, J);
		return e;
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplId e) {
		OplObject I0 = ENV.get(e.s);
		if (I0 instanceof OplSchema) {
			return OplQuery.id(e.s, (OplSchema) I0);
		}
		throw new RuntimeException("Not a schema: " + e.s);
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplApply e) {
		OplObject Q0 = ENV.get(e.Q0);
		if (!(Q0 instanceof OplQuery)) {
			throw new RuntimeException("Not a query: " + e.Q0);
		}
		OplQuery Q = (OplQuery) Q0;
		OplObject I0 = ENV.get(e.I0);
		if (I0 instanceof OplInst) {
			return Q.eval((OplInst) I0);
		}
		if (I0 instanceof OplPresTrans) {
			return Q.eval((OplPresTrans) I0);
		}
		throw new RuntimeException("Not an instance or transform: " + e.I0);

	}

	@Override
	public OplObject visit(Program<OplExp> env, OplTyMapping e) {
		return e;
	}

	@Override
	public OplObject visit(Program<OplExp> env, OplInst0 e) {
		OplObject zzz = ENV.get(e.P.S);
		if (!(zzz instanceof OplSchema)) {
			throw new RuntimeException("Not a SCHEMA: " + e.P.S);
		}
		OplPres P = (OplPres) visit(env, e.P);
		OplInst ret = new OplInst(e.P.S, "?", "none");
		OplObject S0 = ENV.get(e.P.S);
		if (!(S0 instanceof OplSchema)) {
			throw new RuntimeException("Not a schema: " + e.P.S);
		}
		OplSchema S = (OplSchema) S0;

		ret.validate(S, P, null);
		return ret;
	}

	static int temp = 0;
	
	@Override
	public OplObject visit(Program<OplExp> env, OplUnion e) {
		boolean isSchema = false;
		boolean isInstance = false;
		String typeSide = null;

		String s0 = null;
		for (String zz : e.names) {
			s0 = zz;
			break; 
		}
		OplExp exp = env.exps.get(s0);
		if (exp == null) {
			throw new RuntimeException("Missing expression: " + s0);
		}
		if (exp instanceof OplSCHEMA0) {
			isSchema = true;
			OplSCHEMA0 toAdd = (OplSCHEMA0) exp;
			typeSide = toAdd.typeSide;
		} else if (exp instanceof OplInst0) {
			isInstance = true;
		} else {
			throw new RuntimeException("Not a schema or instance: " + s0);
		}
		
		Map<String, Integer> prec = new HashMap<>();
		Set<String> entities = new HashSet<>();
		Map<String, Pair<List<String>, String>> edges = new HashMap<>();
		Map<String, Pair<List<String>, String>> attrs = new HashMap<>();
		List<Triple<OplCtx<String, String>, OplTerm<String, String>, OplTerm<String, String>>> pathEqs = new LinkedList<>();
		List<Triple<OplCtx<String, String>, OplTerm<String, String>, OplTerm<String, String>>> obsEqs = new LinkedList<>();
		
		Set<String> schs = new HashSet<>();
		for (String s : e.names) {
			exp = env.exps.get(s);
			if (exp == null) {
				throw new RuntimeException("Missing expression: " + s);
			}
			if (isSchema) {
				if (!(exp instanceof OplSCHEMA0)) {
					throw new RuntimeException("Not a schema: " + s);
				}
				OplSCHEMA0<String, String, String> toAdd = (OplSCHEMA0<String, String, String>) exp;
				if (!toAdd.typeSide.equals(typeSide)) {
					throw new RuntimeException("not all equal typesides in " + e);
				}
				for (Object entity : toAdd.entities) {
					String proposed = s + "_" + entity;
					if (entities.contains(proposed)) {
						throw new RuntimeException("name clash: " + entity);
					}
					entities.add(proposed);
				}
				
				for (Object edge : toAdd.edges.keySet()) {
					String proposed = s + "_" + edge;
					if (edges.containsKey(proposed)) {
						throw new RuntimeException("name clash: " + edge);
					}
					edges.put(proposed, new Pair<>(Util.singList(s + "_" + toAdd.edges.get(edge).first.get(0)), s + "_" + toAdd.edges.get(edge).second));
					if (toAdd.prec.containsKey(edge)) {
						prec.put(proposed, toAdd.prec.get(edge));
					}
				}
				for (Object att : toAdd.attrs.keySet()) {
					String proposed = s + "_" + att;
					if (attrs.containsKey(proposed)) {
						throw new RuntimeException("name clash: " + att);
					}
					attrs.put(proposed, new Pair<>(Util.singList(s + "_" + toAdd.attrs.get(att).first.get(0)), toAdd.attrs.get(att).second));
					if (toAdd.prec.containsKey(att)) {
						prec.put(proposed, toAdd.prec.get(att));
					}
				}
				for (Triple<OplCtx<String, String>, OplTerm<String, String>, OplTerm<String, String>> tr : toAdd.pathEqs) {
					String v = tr.first.names().get(0);
					String t = s + "_" + tr.first.values().get(0);
					OplCtx<String, String> ctx = new OplCtx<>(Util.singList(new Pair<>(v, t)));
					OplTerm<String, String> lhs1 = prepend(s, tr.second);
					OplTerm<String, String> rhs1 = prepend(s, tr.third);
					pathEqs.add(new Triple<>(ctx, lhs1, rhs1));
				}
				for (Triple<OplCtx<String, String>, OplTerm<String, String>, OplTerm<String, String>> tr : toAdd.obsEqs) {
					String v = tr.first.names().get(0);
					String t = s + "_" + tr.first.values().get(0);
					OplCtx<String, String> ctx = new OplCtx<>(Util.singList(new Pair<>(v, t)));
					OplTerm<String, String> lhs1 = prepend(s, tr.second);
					OplTerm<String, String> rhs1 = prepend(s, tr.third);
					obsEqs.add(new Triple<>(ctx, lhs1, rhs1));
				}

			} else if (isInstance) {
				if (!(exp instanceof OplInst0)) {
					throw new RuntimeException("Not an instance: " + s);
				}
				exp = env.exps.get(s);
				OplInst0 toAdd = (OplInst0) exp;
				schs.add(toAdd.P.S);
			}
		}
		
		if (isSchema) {
			return new OplSCHEMA0<>(prec, entities, edges, attrs, pathEqs, obsEqs, typeSide).accept(env, this);
		} else if (isInstance) {
			OplSchema sch = (OplSchema) new OplUnion(schs).accept(env, this);
			ENV.put("_temp" + temp, sch);
			OplSig<String, String, String> sig = sch.sig;
			Map<String, String> gens = new HashMap<>();
			List<Pair<OplTerm<Object, String>, OplTerm<Object, String>>> equations1 = new LinkedList<>();
			List<Pair<OplTerm<Chc<String,String>, String>, OplTerm<Chc<String,String>, String>>> equations2 = new LinkedList<>();
			for (String s : e.names) {
				exp = env.exps.get(s);
				OplInst0<String, String, String, String> toAdd = (OplInst0<String, String, String, String>) exp;
				for (Object gen : toAdd.P.gens.keySet()) {
					Object ty = toAdd.P.gens.get(gen);
					gens.put(s + "_" + gen, toAdd.P.S + "_" + ty);
				}
				for (Object gen : toAdd.P.prec.keySet()) {
					if (toAdd.P.gens.keySet().contains(gen)) {
						if (toAdd.P.prec.containsKey(gen)) {
						prec.put(s + "_" + gen, toAdd.P.prec.get(gen));
						}
					} else {
						if (toAdd.P.prec.containsKey(gen)) {
							prec.put(toAdd.P.S + "_" + gen, toAdd.P.prec.get(gen));
						}
					}
				}
				//System.out.println("prec is " + prec + " and " + prec.keySet() + " and " + prec.values());
				//System.out.println("was " + toAdd.P.prec + " and " + toAdd.P.prec.keySet() + " and " + prec.values());
				for (Pair<OplTerm<Chc<String, String>, String>, OplTerm<Chc<String, String>, String>> tr : toAdd.P.equations) {
					//System.out.println("processing " + tr);
					//OplTerm<Chc<String,String>, String> lhs1 = prepend(toAdd.P.S, tr.first, s, toAdd.P.gens.keySet());
					//OplTerm<Chc<String,String>, String> rhs1 = prepend(toAdd.P.S, tr.second, s, toAdd.P.gens.keySet());
					OplTerm<Object, String> lhs1 = prepend2(toAdd.P.S, (OplTerm) tr.first, s, toAdd.P.gens.keySet());
					OplTerm<Object, String> rhs1 = prepend2(toAdd.P.S, (OplTerm) tr.second, s, toAdd.P.gens.keySet());
					//equations2.add(new Pair<>(lhs1, rhs1));
					equations1.add(new Pair<>(lhs1, rhs1));
				}
			}
			
			//OplPres<String, String, String, String> pres = new OplPres(prec, "_temp" + temp, sig, gens, equations2);
			OplPres<String, String, String, String> pres = new OplPres(prec, "_temp" + temp, sig, gens, equations1);
			temp++;
			return new OplInst0<String, String, String, String>(pres).accept(env, this); 
		}  else {
			throw new RuntimeException();
		}
		
	}
	
	private  OplTerm<Object, String> prepend2(String s, OplTerm<Object, String> first, String i, Set<String> gens) {
		if (first.var != null) {
			return first;
		}
		List<OplTerm<Object, String>> args = new LinkedList<>();
		for (OplTerm<Object, String> arg : first.args) {
			args.add(prepend2(s, arg, i, gens));
		}
		if (gens.contains(first.head)) {
			return new OplTerm<>(i + "_" + first.head, args);			
		} else {
			return new OplTerm<>(s + "_" + first.head, args);						
		}
		
	}
	
	

	
	private  OplTerm<Chc<String, String>, String> prepend(String s, OplTerm<Chc<String, String>, String> first, String i, Set<String> gens) {
		if (first.var != null) {
			return first;
		}
		List<OplTerm<Chc<String, String>, String>> args = new LinkedList<>();
		for (OplTerm<Chc<String, String>, String> arg : first.args) {
			args.add(prepend(s, arg, i, gens));
		}
		if (first.head.left) {
			return new OplTerm<>(Chc.inLeft(s + "_" + first.head), args);			
		} else {
			return new OplTerm<>(Chc.inRight(i + "_" + first.head), args);						
		}
		
	}

	
	private OplTerm<String, String> prepend(String s, OplTerm<String, String> e) {
		if (e.var != null) {
			return e;
		}
		List<OplTerm<String, String>> args = new LinkedList<>();
		for (OplTerm<String, String> arg : e.args) {
			args.add(prepend(s, arg));
		}
		
		return new OplTerm<>(s + "_" + e.head, args);
	}

}
