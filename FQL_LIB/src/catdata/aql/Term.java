package catdata.aql;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import catdata.Chc;
import catdata.Pair;
import catdata.ide.Util;

public final class Term<Ty, En, Sym, Fk, Att, Gen, Sk> {

	public final Var var;
	public final Sym sym;
	public final Fk[] fks; //ok to be empty
	public final Att att;
	public final Gen gen;
	public final Sk sk;
	public final Term<Ty, En, Sym, Fk, Att, Gen, Sk>[] args;
	public final Term<Ty, En, Sym, Fk, Att, Gen, Sk> arg;
	public final Object obj;
	public final Ty ty;

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
		} else if (fks != null) {
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

	public Chc<Ty, En> type(Ctx<Var, Ty> ctxt, Ctx<Var, En> ctxe, Set<Ty> tys, Map<Sym, Pair<Ty[], Ty>> syms, Map<Ty, Class<?>> java_tys, Set<En> ens, Map<Att, Pair<En, Ty>> atts, Map<Fk, Pair<En, En>> fks, Map<Gen, En> gens, Map<Sk, Ty> sks) {
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
			Class<?> c = java_tys.get(ty);
			if (c == null) {
				throw new RuntimeException("In " + this + ", " + ty + " is not a java type");
			} else if (!c.isInstance(obj)) {
				throw new RuntimeException("In " + this + ", " + "primitive " + obj + " is given type " + ty + " but is not an instanceof " + c);
			}
			ret =  Chc.inLeft(ty);
		} else if (sym != null) {
			Pair<Ty[], Ty> t = syms.get(sym);
			if (t == null) {
				throw new RuntimeException("In " + this + ", " + sym + " is not a typeside symbol");
			} else if (t.first.length != args.length) {
				throw new RuntimeException("In " + this + ", " + sym + " given " + args.length + "arguments but requires " + t.first.length);
			}
			for (int i = 0; i < t.first.length; i++) {
				Chc<Ty, En> u = args[i].type(ctxt, ctxe, tys, syms, java_tys, ens, atts, fks, gens, sks);
				if (!Chc.inLeft(t.first[i]).equals(u)) {
					throw new RuntimeException("In " + this + ", " + "Argument " + args[i] + " has sort " + u.toStringMash() + " but requires " + t.first[i]);
				}
			}
			ret =  Chc.inLeft(t.second);
		} else if (att != null) {
			Pair<En, Ty> t = atts.get(att);
			if (t == null) {
				throw new RuntimeException("In " + this + ", " + att + " is not an attribute");
			} 
			Chc<Ty, En> u = arg.type(ctxt, ctxe, tys, syms, java_tys, ens, atts, fks, gens, sks);
			if (!Chc.inRight(t.first).equals(u)) {
				throw new RuntimeException("In " + this + ", " + "argument " + arg + " has sort " + u.toStringMash() + " but requires " + t.first);
			}
			ret =  Chc.inLeft(t.second);
		} else if (fks != null) {
			Chc<Ty, En> u = arg.type(ctxt, ctxe, tys, syms, java_tys, ens, atts, fks, gens, sks);
			if (u.left) {
				throw new RuntimeException("In " + this + ", " + arg + " has type " + u.toStringMash() + " which is not an entity");
			}
			En src = u.r;
			En dst = src;
			Fk last = this.fks[0];
			for (Fk fk : this.fks) {	
				Pair<En, En> t = fks.get(fk);
				if (t == null) {
					throw new RuntimeException("In " + this + ", " + fk + " is not a foreign key");
				}
				if (!t.first.equals(dst)) {
					throw new RuntimeException("In " + this + ", " + "target of " + last + " [" + src + "] is not source of " + fk + " [" + dst  + "]");
				}
				dst = t.second;
				last = fk;
			} 				
			if (!Chc.inRight(src).equals(u)) {
				throw new RuntimeException("In " + this + ", " + "argument " + arg + " has sort " + u.toStringMash() + " but requires " + src);
			}
			ret = Chc.inRight(dst);
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

	private Term(Var var, Sym sym, Fk[] fks, Att att, Gen gen, Sk sk, Term<Ty, En, Sym, Fk, Att, Gen, Sk>[] args, Term<Ty, En, Sym, Fk, Att, Gen, Sk> arg, Object obj, Ty ty) {
		this.var = var;
		this.sym = sym;
		this.fks = fks;
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
			if (args.length == 0) {
				return sym.toString();
			} else if (args.length == 1) {
				return args[0] + "." + sym;
			} else if (args.length == 2) {
				return "(" + args[0] + " " + sym + " " + args[1] + ")";
			} else {
				return sym + "(" + Util.sep(Arrays.asList(args), ", ") + ")";
			}
		} else if (att != null) {
			return arg + "." + att;
		} else if (fks != null) {
			return arg + "." + Util.sep(Arrays.asList(fks), ".");
		} else if (gen != null) {
			return gen.toString();
		} else if (sk != null) {
			return sk.toString();
		} else if (obj != null) {
			return obj.toString(); // TODO:choice, print type?
		}
		throw new RuntimeException("Encountered an empty term");
	}

	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Term<Ty, En, Sym, Fk, Att, Gen, Sk> Var(Var var) {
		if (var == null) {
			throw new RuntimeException("Attempt to create a term with a null variable");
		}
		return new Term<>(var, null, null, null, null, null, null, null, null, null);
	}

	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Term<Ty, En, Sym, Fk, Att, Gen, Sk> Sym(Sym sym, Term<Ty, En, Sym, Fk, Att, Gen, Sk>[] args) {
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

	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Term<Ty, En, Sym, Fk, Att, Gen, Sk> Fks(Fk[] fks, Term<Ty, En, Sym, Fk, Att, Gen, Sk> arg) {
		if (fks == null) {
			throw new RuntimeException("Attempt to create a term with a null foreign keys");
		}
		if (arg == null) {
			throw new RuntimeException("Attempt to create a term with null foreign keys argument");
		}
		if (fks.length == 0) {
			throw new RuntimeException("Attempt to create a 0-ary list of foreign keys");
		}
		return new Term<>(null, null, fks, null, null, null, null, arg, null, null);
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
		result = prime * result + ((fks == null) ? 0 : fks.hashCode());
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
		if (fks == null) {
			if (other.fks != null)
				return false;
		} else if (!fks.equals(other.fks))
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
		} else if (fks != null || att != null) {
			return arg.getOnlyVar();
		} else if (gen != null || sk != null || obj != null) {
			return null;
		}
		throw new RuntimeException("getOnlyVar encountered ill-formed term.  Should be impossible, please report");
	}

	
	
	
	
}
