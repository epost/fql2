package fql_lib.opl;

import javax.script.Invocable;

import fql_lib.opl.OplExp.OplCtx;
import fql_lib.opl.OplExp.OplDelta;
import fql_lib.opl.OplExp.OplEval;
import fql_lib.opl.OplExp.OplExpVisitor;
import fql_lib.opl.OplExp.OplJavaInst;
import fql_lib.opl.OplExp.OplMapping;
import fql_lib.opl.OplExp.OplSetInst;
import fql_lib.opl.OplExp.OplSetTrans;
import fql_lib.opl.OplExp.OplSig;
import fql_lib.opl.OplExp.OplString;
import fql_lib.opl.OplExp.OplTerm;
import fql_lib.opl.OplExp.OplTransEval;
import fql_lib.opl.OplExp.OplVar;

public class OplOps implements OplExpVisitor<OplObject, OplProgram> {
	

	OplEnvironment ENV;

	public OplOps(OplEnvironment env) {
		this.ENV = env;
	}

	@Override
	public OplObject visit(OplProgram env, OplSig e) {
		e.validate(); 
		return e;
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
			e.e.type(s0, new OplCtx<String>());
			return new OplString(e.e.eval(s0, i0, new OplCtx<String>()));
		}
		if (i instanceof OplJavaInst) {
			OplJavaInst i0 = (OplJavaInst) i;
			OplObject s = ENV.get(i0.sig);
			OplSig s0 = (OplSig) s;		
			e.e.type(s0, new OplCtx<String>());
			try {
				return new OplString(e.e.eval0((Invocable)i0.engine));
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
		e.validate(sig0);
		return e;
	}

	@Override
	public OplObject visit(OplProgram env, OplMapping e) {
		OplObject src = ENV.get(e.src0);
		OplObject dst = ENV.get(e.dst0);
		if (!(src instanceof OplSig)) {
			throw new RuntimeException("Source is not a theory in " + e);
		}
		if (!(dst instanceof OplSig)) {
			throw new RuntimeException("Target is not a theory in " + e);
		}
		OplSig src0 = (OplSig) src;
		OplSig dst0 = (OplSig) dst;
		e.validate(src0, dst0);
		return e;
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
		//if (I instanceof OplJavaInst) {
		//	OplJavaInst I0 = (OplJavaInst) I;
		//	return F0.delta(I0);			
		//}
			throw new RuntimeException("Not a model or transform: " + e.I);
	}

	@Override
	public OplObject visit(OplProgram env, OplTransEval e) {
		OplObject i = ENV.get(e.I);
		OplObject F = ENV.get(e.F);
		if (!(F instanceof OplMapping)) {
			throw new RuntimeException("Not a mapping: " + e.F);
		}
		OplMapping F0 = (OplMapping) F;
		e.e.type(F0.src, new OplCtx<>());
		OplTerm t = F0.trans(new OplCtx<>(), e.e).second;
				
		if (i instanceof OplSetInst) {
			return new OplString(t.eval(F0.dst, (OplSetInst)i, new OplCtx<String>()));
		}
		if (i instanceof OplJavaInst) {
			try {
				return new OplString(t.eval0((Invocable)((OplJavaInst)i).engine));
			} catch (Exception ee) {
				ee.printStackTrace();
				throw new RuntimeException(ee.getMessage());
			}
		}
		throw new RuntimeException("Not a set/js model: " + e.I);
	}


}
