package catdata.aql;

import java.util.List;
import java.util.Map;
import java.util.Set;

import catdata.Chc;
import catdata.Pair;
import catdata.Util;

public final class Term<Ty, En, Sym, Fk, Att, Gen, Sk> {

	public final Var var;
	public final Sym sym;
	public final Fk fk; 
	public final Att att;
	public final Gen gen;
	public final Sk sk;
	public final List<Term<Ty, En, Sym, Fk, Att, Gen, Sk>> args;
	public final Term<Ty, En, Sym, Fk, Att, Gen, Sk> arg;
	public final Object obj;
	public final Ty ty;

	//these do not care about java
	public boolean isTypeSide() {
		if (var != null) {
			return true;
		} else if (sym != null) {
			for (Term<Ty, En, Sym, Fk, Att, Gen, Sk> t : args) {
				if (!t.isTypeSide()) {
					return false;
				}
			}
			return true;
		} else if (obj != null) {
			return true;
		}
		return false;
	}

	public boolean isSchema() {
		if (isTypeSide()) {
			return true;
		} else if (fk != null) {
			return arg.isSchema();
		} else if (att != null) {
			return arg.isSchema();
		}
		return false;
	}

	public boolean isGround() {
		if (var != null) {
			return false;
		} else if (args != null) {
			for (Term<Ty, En, Sym, Fk, Att, Gen, Sk> t : args) {
				if (!t.isGround()) {
					return false;
				}
			}
			return true;
		} else if (obj != null) {
			return true;
		} else {
			return arg.isGround();
		}
	}

	public Chc<Ty, En> type(Ctx<Var, Ty> ctxt, Ctx<Var, En> ctxe, Set<Ty> tys, Map<Sym, Pair<List<Ty>, Ty>> syms, Map<Ty, String> java_tys_string, Set<En> ens, Map<Att, Pair<En, Ty>> atts, Map<Fk, Pair<En, En>> fks, Map<Gen, En> gens, Map<Sk, Ty> sks) {
		Chc<Ty, En> ret = null;
		if (var != null) {
			if (ctxt.containsKey(var) && ctxe.containsKey(var)) {
				throw new RuntimeException("In " + this + ", " + "name collision on " + var + " in " + ctxt + " and " + ctxe);
			}
			if (ctxe.containsKey(var)) {
				ret = Chc.inRight(ctxe.get(var));
			} else if (ctxt.containsKey(var)) {
				ret = Chc.inLeft(ctxt.get(var));
			} else {
				throw new RuntimeException("In " + this + ", " + "neither " + ctxt + " nor " + ctxe + " contain " + var);
			}
		} else if (obj != null) {
			Class<?> c = AqlJs.load(java_tys_string.get(ty));
			if (c == null) {
				throw new RuntimeException("In " + this + ", " + ty + " is not a java type. ");
			} else if (!c.isInstance(obj)) {
				throw new RuntimeException("In " + this + ", " + "primitive " + obj + " is given type " + ty + " but is not an instance of " + c + ", is an instance of " + obj.getClass());
			}
			ret =  Chc.inLeft(ty);
		} else if (sym != null) {
			Pair<List<Ty>, Ty> t = syms.get(sym);
			if (t == null) {
				throw new RuntimeException("In " + this + ", " + sym + " is not a typeside symbol");
			} else if (t.first.size() != args.size()) {
				throw new RuntimeException("In " + this + ", " + sym + " given " + args.size() + "arguments but requires " + t.first.size());
			}
			for (int i = 0; i < t.first.size(); i++) {
				Chc<Ty, En> u = args.get(i).type(ctxt, ctxe, tys, syms, java_tys_string, ens, atts, fks, gens, sks);
				if (!Chc.inLeft(t.first.get(i)).equals(u)) {
					throw new RuntimeException("In " + this + ", " + "Argument " + args.get(i) + " has sort " + u.toStringMash() + " but requires " + t.first.get(i));
				}
			}
			ret =  Chc.inLeft(t.second);
		} else if (att != null) {
			Pair<En, Ty> t = atts.get(att);
			if (t == null) {
				throw new RuntimeException("In " + this + ", " + att + " is not an attribute");
			} 
			Chc<Ty, En> u = arg.type(ctxt, ctxe, tys, syms, java_tys_string, ens, atts, fks, gens, sks);
			if (!Chc.inRight(t.first).equals(u)) {
				throw new RuntimeException("In " + this + ", " + "argument " + arg + " has sort " + u.toStringMash() + " but requires " + t.first);
			}
			ret =  Chc.inLeft(t.second);
		} else if (fk != null) {
			Chc<Ty, En> u = arg.type(ctxt, ctxe, tys, syms, java_tys_string, ens, atts, fks, gens, sks);
			if (u.left) {
				throw new RuntimeException("In " + this + ", " + arg + " has type " + u.toStringMash() + " which is not an entity");
			}
			Pair<En, En> t = fks.get(fk);
			if (t == null) {
				throw new RuntimeException("In " + this + ", " + fk + " is not a foreign key");
			}		
			if (!Chc.inRight(t.first).equals(u)) {
				throw new RuntimeException("In " + this + ", " + "argument " + arg + " has sort " + u.toStringMash() + " but requires " + t.first);
			}
			ret = Chc.inRight(t.second);
		} else if (gen != null) {
			En en = gens.get(gen);
			if (en == null) {
				throw new RuntimeException("In " + this + ", " + "generator " + gen + " has no defined entity");	
			}
			ret = Chc.inRight(en);
		} else if (sk != null) {
			Ty ty = sks.get(sk);
			if (ty == null) {
				throw new RuntimeException("In " + this + ", " + "labelled null " + sk + " has no defined type");	
			}
			ret = Chc.inLeft(ty);
		}
		if (ret == null || (ret.left && ret.l == null) || (!ret.left && ret.r == null)) {
			throw new RuntimeException("In " + this + "," + " typing encountered an ill-formed term.  Should be impossible, report to Ryan.  " + this);
		} else if (ret.left && !tys.contains(ret.l)) {
			throw new RuntimeException("In " + this + "," + " return type is " + ret.l + " which is not a type");
		} else if (!ret.left && !ens.contains(ret.r)) {
			throw new RuntimeException("In " + this + "," + " return type is " + ret.r + " which is not a entity");		
		}
		return ret;
	}

	private Term(Var var, Sym sym, Fk fks, Att att, Gen gen, Sk sk, List<Term<Ty, En, Sym, Fk, Att, Gen, Sk>> args, Term<Ty, En, Sym, Fk, Att, Gen, Sk> arg, Object obj, Ty ty) {
		this.var = var;
		this.sym = sym;
		this.fk = fks;
		this.att = att;
		this.gen = gen;
		this.sk = sk;
		this.args = args;
		this.arg = arg;
		this.obj = obj;
		this.ty = ty;
	}

	@Override
	public String toString() {
		if (var != null) {
			return var.toString();
		} else if (sym != null) {
			if (args.size() == 0) {
				return sym.toString();
			} else if (args.size() == 1) {
				return args.get(0) + "." + sym;
			} else if (args.size() == 2) {
				return "(" + args.get(0) + " " + sym + " " + args.get(1) + ")";
			} else {
				return sym + "(" + Util.sep(args, ", ") + ")";
			}
		} else if (att != null) {
			return arg + "." + att;
		} else if (fk != null) {
			return arg + "." + fk;
		} else if (gen != null) {
			return gen.toString();
		} else if (sk != null) {
			return sk.toString();
		} else if (obj != null) {
			return obj.toString() + "@" + ty;
		}
		throw new RuntimeException("Encountered an empty term");
	}

	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Term<Ty, En, Sym, Fk, Att, Gen, Sk> Var(Var var) {
		if (var == null) {
			throw new RuntimeException("Attempt to create a term with a null variable");
		}
		return new Term<>(var, null, null, null, null, null, null, null, null, null);
	}

	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Term<Ty, En, Sym, Fk, Att, Gen, Sk> Sym(Sym sym, List<Term<Ty, En, Sym, Fk, Att, Gen, Sk>> args) {
		if (sym == null) {
			throw new RuntimeException("Attempt to create a term with a null symbol");
		}
		if (args == null) {
			throw new RuntimeException("Attempt to create a term with null arguments");
		}
		return new Term<>(null, sym, null, null, null, null, args, null, null, null);
	}

	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Term<Ty, En, Sym, Fk, Att, Gen, Sk> Att(Att att, Term<Ty, En, Sym, Fk, Att, Gen, Sk> arg) {
		if (att == null) {
			throw new RuntimeException("Attempt to create a term with a null attribute");
		}
		if (arg == null) {
			throw new RuntimeException("Attempt to create a term with null attribute argument");
		}
		return new Term<>(null, null, null, att, null, null, null, arg, null, null);
	}

	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Term<Ty, En, Sym, Fk, Att, Gen, Sk> Fk(Fk fk, Term<Ty, En, Sym, Fk, Att, Gen, Sk> arg) {
		if (fk == null) {
			throw new RuntimeException("Attempt to create a term with a null foreign key");
		}
		if (arg == null) {
			throw new RuntimeException("Attempt to create a term with null foreign keys argument");
		}
		return new Term<>(null, null, fk, null, null, null, null, arg, null, null);
	}
	
	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Term<Ty, En, Sym, Fk, Att, Gen, Sk> Fks(List<Fk> fks, Term<Ty, En, Sym, Fk, Att, Gen, Sk> arg) {
		if (fks == null) {
			throw new RuntimeException("Attempt to create a term with null foreign keys");
		}
		if (arg == null) {
			throw new RuntimeException("Attempt to create a term with null foreign keys argument");
		}
		for (Fk fk : fks) {
			arg = Term.Fk(fk, arg);
		}
		return arg;
	}

	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Term<Ty, En, Sym, Fk, Att, Gen, Sk> Gen(Gen gen) {
		if (gen == null) {
			throw new RuntimeException("Attempt to create a term with a null generator");
		}
		return new Term<>(null, null, null, null, gen, null, null, null, null, null);
	}

	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Term<Ty, En, Sym, Fk, Att, Gen, Sk> Sk(Sk sk) {
		if (sk == null) {
			throw new RuntimeException("Attempt to create a term with a null labelled null");
		}
		return new Term<>(null, null, null, null, null, sk, null, null, null, null);
	}

	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Term<Ty, En, Sym, Fk, Att, Gen, Sk> Obj(Object obj, Ty ty) {
		if (ty == null) {
			throw new RuntimeException("Attempt to create a primitive with a null type");
		}
		if (obj == null) {
			throw new RuntimeException("Attempt to create a primitive with null object");
		}
		return new Term<>(null, null, null, null, null, null, null, null, obj, ty);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((arg == null) ? 0 : arg.hashCode());
		result = prime * result + ((args == null) ? 0 : args.hashCode());
		result = prime * result + ((att == null) ? 0 : att.hashCode());
		result = prime * result + ((fk == null) ? 0 : fk.hashCode());
		result = prime * result + ((gen == null) ? 0 : gen.hashCode());
		result = prime * result + ((obj == null) ? 0 : obj.hashCode());
		result = prime * result + ((sk == null) ? 0 : sk.hashCode());
		result = prime * result + ((sym == null) ? 0 : sym.hashCode());
		result = prime * result + ((ty == null) ? 0 : ty.hashCode());
		result = prime * result + ((var == null) ? 0 : var.hashCode());
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
		Term<?, ?, ?, ?, ?, ?, ?> other = (Term<?, ?, ?, ?, ?, ?, ?>) obj;
		if (arg == null) {
			if (other.arg != null)
				return false;
		} else if (!arg.equals(other.arg))
			return false;
		if (args == null) {
			if (other.args != null)
				return false;
		} else if (!args.equals(other.args))
			return false;
		if (att == null) {
			if (other.att != null)
				return false;
		} else if (!att.equals(other.att))
			return false;
		if (fk == null) {
			if (other.fk != null)
				return false;
		} else if (!fk.equals(other.fk))
			return false;
		if (gen == null) {
			if (other.gen != null)
				return false;
		} else if (!gen.equals(other.gen))
			return false;
		if (this.obj == null) {
			if (other.obj != null)
				return false;
		} else if (!this.obj.equals(other.obj))
			return false;
		if (sk == null) {
			if (other.sk != null)
				return false;
		} else if (!sk.equals(other.sk))
			return false;
		if (sym == null) {
			if (other.sym != null)
				return false;
		} else if (!sym.equals(other.sym))
			return false;
		if (ty == null) {
			if (other.ty != null)
				return false;
		} else if (!ty.equals(other.ty))
			return false;
		if (var == null) {
			if (other.var != null)
				return false;
		} else if (!var.equals(other.var))
			return false;
		return true;
	}

	//returns null if no var
	public Var getOnlyVar() {
		if (var != null) {
			return var;
		} else if (sym != null) {
			Var var = null;
			for (Term<Ty, En, Sym, Fk, Att, Gen, Sk> arg : args) {
				Var var2 = arg.getOnlyVar();
				if (var2 == null) {
					continue;
				}
				if (var == null) {
					var = var2;
					continue;
				}
				if (!var.equals(var2)) {
					return null;
				}
			}
			return var;
		} else if (fk != null || att != null) {
			return arg.getOnlyVar();
		} else if (gen != null || sk != null || obj != null) {
			return null;
		}
		throw new RuntimeException("getOnlyVar encountered ill-formed term.  Should be impossible, please report");
	}
	
}
