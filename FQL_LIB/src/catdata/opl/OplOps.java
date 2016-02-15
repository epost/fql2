package catdata.opl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import catdata.opl.OplExp.OplVar;

@SuppressWarnings({"unchecked", "rawtypes"})
public class OplOps implements OplExpVisitor<OplObject, OplProgram> {
	
	OplEnvironment ENV;

	public OplOps(OplEnvironment env) {
		this.ENV = env;
	}
	
	@Override
	public OplObject visit(OplProgram env, OplPivot e) {
		OplObject o = ENV.get(e.I0);
		if (o instanceof OplInst) {
			OplInst I = (OplInst) o;
			e.validate(I);
			return e;
		}
		throw new RuntimeException("Not an instnce: " + e.I0);
	}

	@Override
	public OplObject visit(OplProgram env, OplSig e) {
		e.validate(); 
		return e;
	}
	
	@Override
	public OplObject visit(OplProgram Env, OplDelta0 e) {
		OplObject o = ENV.get(e.F0);
		if (o instanceof OplTyMapping) {
			OplTyMapping F = (OplTyMapping) o;
			e.validate(F);
			return e.toQuery();
		}
		throw new RuntimeException("Not a typed mapping: " + e.F);
	}
	
	@Override
	public OplObject visit(OplProgram env, OplSCHEMA0 e) {
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
	public OplObject visit(OplProgram env, OplSetInst e) {
		OplObject o = ENV.get(e.sig);
		if (!(o instanceof OplSig)) {
			throw new RuntimeException("Not a theory: " + e.sig);
		}
		OplSig s = (OplSig) o;
		e.validate(s);
		return e;
	}

	@Override
	public OplObject visit(OplProgram env, OplEval e) {
		OplObject i = ENV.get(e.I);
		if (i instanceof OplSetInst) {
			OplSetInst i0 = (OplSetInst) i;
			OplObject s = ENV.get(i0.sig);
			OplSig s0 = (OplSig) s;		
			e.e.type(s0, new OplCtx<String, String>());
			return new OplString(e.e.eval(s0, i0, new OplCtx<String, String>()).toString());
		}
		
		if (i instanceof OplJavaInst) {
			OplJavaInst i0 = (OplJavaInst) i;
			OplObject s = ENV.get(i0.sig);
			OplSig s0 = (OplSig) s;		
			e.e.type(s0, new OplCtx<String, String>());
			try {
				return new OplString(OplExp.strip(OplToKB.redBy(i0, OplToKB.convert(OplSig.inject(e.e))).toString()));
//				return new OplString(e.e.eval((Invocable)i0.engine).toString());
			} catch (Exception ee) {
				ee.printStackTrace();
				throw new RuntimeException(ee.getMessage());
			}
		}
		throw new RuntimeException("Not a set/js model: " + e.I);
	}

	@Override
	public OplObject visit(OplProgram env, OplVar e) {
		return ENV.get(e.name);
	}

	@Override
	public OplObject visit(OplProgram env, OplSetTrans e) {
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
			throw new RuntimeException("Theories of source and target do not match in " + e);
		}
		OplSig sig = (OplSig) ENV.get(src0.sig);
		e.validate(sig, src0, dst0);
		return e;
	}

	@Override
	public OplObject visit(OplProgram env, OplJavaInst e) {
		OplObject sig = ENV.get(e.sig);
		if (!(sig instanceof OplSig)) {
			throw new RuntimeException("Not a signature: " + e.sig);
		}
		OplSig sig0 = (OplSig) sig;
		e.validate(sig0, ENV);
		return e;
	}

	@Override
	public OplObject visit(OplProgram env, OplMapping e) {
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
			//e.validate(src0.projEA(), dst0.projEA());
			OplTyMapping ret = new OplTyMapping<>(e.src0, e.dst0, src0, dst0, e);
			return ret;
		}
		throw new RuntimeException("Source or Target is not a theory/schema in " + e);
	}

	@Override
	public OplObject visit(OplProgram env, OplDelta e) {
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
	public OplObject visit(OplProgram env, OplPres e) {
		OplObject i = ENV.get(e.S);
		if (i instanceof OplSig) {
			OplSig S = (OplSig) i;
			OplPres ret = OplPres.OplPres0(e.prec, e.S, S, e.gens, e.equations);
			ret.toSig();
			return ret;
		} else if (i instanceof OplSchema) {
			OplSchema S = (OplSchema) i;
			OplPres ret = OplPres.OplPres0(e.prec, e.S, S.sig, e.gens, e.equations);
			ret.toSig();
			return ret;
		} else {
			throw new RuntimeException("Not a presentation or schema: " + e.S);
		}
	}
	
	@Override
	public OplObject visit(OplProgram env, OplPushout e) {
		OplObject s1 = ENV.get(e.s1);
		OplObject s2 = ENV.get(e.s2);		
		if (!(s1 instanceof OplPresTrans)) {
			throw new RuntimeException(e.s1 + " is not a transform");
		}
		if (!(s2 instanceof OplPresTrans)) {
			throw new RuntimeException(e.s2 + " is not a transform");
		}
		e.validate((OplPresTrans)s1, (OplPresTrans)s2);
		return e;
	}

	@Override
	public OplObject visit(OplProgram env, OplSat e) {
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
	public OplObject visit(OplProgram env, OplUberSat e) {
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
	public OplObject visit(OplProgram env, OplUnSat e) {
		OplObject i = ENV.get(e.I);
		if (!(i instanceof OplSetInst)) {
			throw new RuntimeException("Not a model: " + e.I);
		}
		OplSetInst S = (OplSetInst) i;
		return OplUnSat.desaturate(S.sig, S);
	}
	
	@Override
	public OplObject visit(OplProgram env, OplSigma e) {
		
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
			throw new RuntimeException("Not a presentation of an instance or transform: " + e.I);
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
	public OplObject visit(OplProgram env, OplPresTrans e) {
		OplObject src = ENV.get(e.src0);
		OplObject dst = ENV.get(e.dst0);
		if (src instanceof OplPres && dst instanceof OplPres) {
			OplPres src0 = (OplPres) src;
			OplPres dst0 = (OplPres) dst;
			e.validateNotReally(src0, dst0); //?
			//e.toMapping(); redundant
			return e;
		} else if (src instanceof OplInst && dst instanceof OplInst) {
			OplInst src0 = (OplInst) src;
			OplInst dst0 = (OplInst) dst;
			e.validateNotReally(src0, dst0); //?
			//e.toMapping(); redundant
			return e;
		}
		throw new RuntimeException("Source or target is not a presentation or instance in " + e);
	}

	@Override
	public OplObject visit(OplProgram env, OplFlower e) {
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
	public OplObject visit(OplProgram env, OplSchema e) {
		OplObject I0 = ENV.get(e.sig0);
		if (I0 instanceof OplSig) {
			OplSig I = (OplSig) I0;
			e.validate(I);
			return e;
		}
		throw new RuntimeException("Not a theory: " + e.sig0);
	}
	
	@Override
	public OplObject visit(OplProgram env, OplSchemaProj e) {
		OplObject I0 = ENV.get(e.sch0);
		if (I0 instanceof OplSchema) {
			OplSchema I = (OplSchema) I0;
			return e.proj(I);
		}
		throw new RuntimeException("Not a schema: " + e.sch0);
	}
	
	@Override
	public OplObject visit(OplProgram env, OplInst e) {
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
	public OplObject visit(OplProgram env, OplQuery e) {
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
	public OplObject visit(OplProgram env, OplId e) {
		OplObject I0 = ENV.get(e.s);
		if (I0 instanceof OplSchema) {
			return OplQuery.id(e.s, (OplSchema)I0);
		} 
		throw new RuntimeException("Not a schema: " + e.s);
	}
	
	@Override
	public OplObject visit(OplProgram env, OplApply e) {
		OplObject Q0 = ENV.get(e.Q0);
		if (!(Q0 instanceof OplQuery)) {
			throw new RuntimeException("Not a query: " + e.Q0);
		} 
		OplQuery Q = (OplQuery) Q0;
		OplObject I0 = ENV.get(e.I0);
		if (I0 instanceof OplInst) {
			return Q.eval((OplInst)I0);
		}
		if (I0 instanceof OplPresTrans) {
			return Q.eval((OplPresTrans)I0);
		}
		throw new RuntimeException("Not an instance or transform: " + e.I0);

	}

	@Override
	public OplObject visit(OplProgram env, OplTyMapping e) {
		return e;
	}

	@Override
	public OplObject visit(OplProgram env, OplInst0 e) {
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
	
}
