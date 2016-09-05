package catdata.aql;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Pair;
import catdata.Ref;
import catdata.Triple;
import catdata.Util;

public final class RawTerm {

	public final String head;
	public final List<RawTerm> args;
	
	public final String annotation;
	
	@Override
	public String toString() {
		String str = (annotation == null ? "" : "@" + annotation);
		if (args.isEmpty()) {
			return head + str;
		}
		return head + "(" + Util.sep(args, ",") + ")" + str;
	}
	
	//TODO inefficient bitwise operations

	//it is misleading to return a context, because strings for primitives can come out
	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Triple<Ctx<String, Chc<Ty,En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>
	infer1(Map<String, Chc<Ty,En>> ctx, RawTerm lhs, RawTerm rhs, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
			
		Map<String, Ref<Chc<Ty,En>>> ctx0 = new HashMap<>();
		Set<String> vars = ctx.keySet();
		for (String p : ctx.keySet()) {
			Chc<Ty, En> ty = ctx.get(p);
			if (ty != null) {
				ty.assertNeitherNull();
				ctx0.put(p, new Ref<>(ty));
			} else {
				ctx0.put(p, new Ref<>());
			}	
		}
		Ref<Chc<Ty,En>> lhs_t = lhs.infer(vars, ctx0, col);
		Ref<Chc<Ty,En>> rhs_t = rhs.infer(vars, ctx0, col);
		if (lhs_t.x == null && rhs_t.x == null) {
			throw new RuntimeException("Ambiguous result type (cannot infer) for " + lhs + " = " + rhs);
		} else if (lhs_t.x == null && rhs_t.x != null) {
			lhs_t.set(rhs_t);
		} else if (rhs_t.x == null && lhs_t.x != null) {
			rhs_t.set(lhs_t);
		} 
		if (!lhs_t.x.equals(rhs_t.x)) {
			throw new RuntimeException(lhs + " has type " + lhs_t.x.toStringMash() + " but " + rhs + " has type " + rhs_t.x.toStringMash());
		}
		Ctx<String, Chc<Ty,En>> ret = new Ctx<>();
		for (String var : ctx0.keySet()) {
			Ref<Chc<Ty, En>> ref = ctx0.get(var);
			if (ref.x == null) {
				throw new RuntimeException("Ambiguous type (cannot infer) for variable/primitive " + var + " in " + lhs + " = " + rhs);
			}
			ref.x.assertNeitherNull();
			ret.put(var, ref.x);
		}
		return new Triple<>(ret, lhs.trans(vars, ctx0, col), rhs.trans(vars, ctx0, col));
	}
	
	//staticall typesafe coerce
	private <K,V> K find(Map<K,V> m, String k) {
		for (K k0 : m.keySet()) {
			if (k.equals(k0)) {
				return k0;
			}
		}
		throw new RuntimeException("Anomaly, please report");
	}
	
	public <Ty, En, Sym, Fk, Att, Gen, Sk> Term<Ty, En, Sym, Fk, Att, Gen, Sk> trans(Set<String> vars, Map<String, Ref<Chc<Ty, En>>> ctx0, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
		List<Term<Ty, En, Sym, Fk, Att, Gen, Sk>> args0 = args.stream().map(x -> x.trans(vars, ctx0, col)).collect(Collectors.toList());
			
		if (vars.contains(head)) {
			if (annotation != null) {
				throw new RuntimeException(this + " is annotated but is also a variable or symbol");
			}
			return Term.Var(new Var(head));
		} else if (col.syms.containsKey(head)) {
			if (annotation != null) {
				throw new RuntimeException(this + " is annotated but is also a variable or symbol");
			}
			return Term.Sym(find(col.syms, head), args0);
		} else if (col.atts.containsKey(head)) {
			if (annotation != null) {
				throw new RuntimeException(this + " is annotated but is also a variable or symbol");
			}
			return Term.Att(find(col.atts, head), args0.get(0));
		} else if (col.fks.containsKey(head)) {
			if (annotation != null) {
				throw new RuntimeException(this + " is annotated but is also a variable or symbol");
			}
			return Term.Fk(find(col.fks, head), args0.get(0)); 
		} else if (col.gens.containsKey(head)) {
			if (annotation != null) {
				throw new RuntimeException(this + " is annotated but is also a variable or symbol");
			}
			return Term.Gen(find(col.gens, head));
		} else if (col.sks.containsKey(head)) {
			if (annotation != null) {
				throw new RuntimeException(this + " is annotated but is also a variable or symbol");
			}
			return Term.Sk(find(col.sks, head));
		} else if (annotation != null || (ctx0.containsKey(head) && ctx0.get(head).x.left)) {
			Ty ty = null; 
			if (annotation != null) {
				for (Ty tyX : col.tys) {
					if (annotation.equals(tyX)) {
						ty = tyX;
					}
				}
			} else if (ctx0.containsKey(head)) {
				ty = ctx0.get(head).x.l;
			}	
			if (ty == null) {
				throw new RuntimeException("Anomaly: please report");
			}
			String code = col.java_parsers.get(ty);
			if (code == null) {
				throw new RuntimeException("No java constant parser defined for java type " + ctx0.get(head).x.l);
			}
			Function<List<Object>, Object> f = AqlJs.compile(code);
			
			return Term.Obj(f.apply(Util.singList(head)), ty);
		} else if (ctx0.containsKey(head) && !ctx0.get(head).x.left) {
			return Term.Var(new Var(head));
		}
		throw new RuntimeException("Anomaly, please report: " + this);
	}

	
	public <Ty, En, Sym, Fk, Att, Gen, Sk> Ref<Chc<Ty,En>> infer(Set<String> vars, Map<String, Ref<Chc<Ty,En>>> ctx, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
		boolean isSym, isAtt, isFk, isGen, isSk, isVar, isObj ;
				
		Pair<List<Ty>, Ty> syms_t = col.syms.get(head);
		isSym = syms_t != null;
		Pair<En, Ty> atts_t = col.atts.get(head);
		isAtt = atts_t != null;
		Pair<En, En> fks_t = col.fks.get(head);
		isFk = fks_t != null;
		En gens_t = col.gens.get(head);
		isGen = gens_t != null;
		Ty sks_t = col.sks.get(head);
		isSk = sks_t != null;
		isObj = annotation != null;
		isVar = vars.contains(new Var(head));
			
		int sum = Util.list(isSym, isAtt, isFk, isGen, isSk, isVar, isObj).stream().filter(x -> x.equals(Boolean.TRUE)).collect(Collectors.toList()).size();
		if (sum > 2) {
			throw new RuntimeException("In " + this + ", the head " + head + " is ambiguous: " + (isSym ? "is a typeside symbol " : " ") + (isVar ? "is a variable " : " ") + (isFk ? "is a foreign key " : " ") + (isAtt ? "is an attribute " : " ") + (isGen ? "is a generator " : " ") + (isSk ? "is a labelled null " : " ") + (isObj ? "is a primitive" : ""));
		}
		
		if (isGen) {
			return new Ref<>(Chc.inRight(gens_t));
		} else if (isSk) {
			return new Ref<>(Chc.inLeft(sks_t));
		} else if (isObj) {
			for (Ty ty : col.tys) {
				if (ty.equals(annotation)) { //avoids cast
					return new Ref<>(Chc.inLeft(ty));
				}
			}
			throw new RuntimeException("In " + this + ", the annotation " + annotation + " is not a type");
		} else if (isAtt || isFk) {
			if (args.size() != 1) {
				throw new RuntimeException("In " + this + ", the head " + head + " is an attribute/foreign key but it is given more than one or less than one argument, namely " + args.size());
			}
			Ref<Chc<Ty,En>> arg_t = args.get(0).infer(vars, ctx, col);
			En ty = atts_t == null ? fks_t.first : atts_t.first;
			if (arg_t.x != null && !Chc.inRight(ty).equals(arg_t.x)) {
					throw new RuntimeException("In " + this + ", the head " + head + " is an attribute/foreign key expecting argument type " + atts_t + " but its argument has actual type " + arg_t.x.toStringMash());					
			}
			arg_t.set(Chc.inRight(ty)); //redundant sometimes

			if (isAtt) {
				return new Ref<>(Chc.inLeft(atts_t.second));
			} else {
				return new Ref<>(Chc.inRight(fks_t.second));				
			}
		} else if (isSym) {
			if (args.size() != syms_t.first.size()) {
				throw new RuntimeException("In " + this + ", the head " + head + " is a typeside symbol of arity " + syms_t.first.size() + " but it is given " + args.size() + " arguments");
			}
			int i = 0;
			for (RawTerm arg : args) {
				Ref<Chc<Ty,En>> arg_t = arg.infer(vars, ctx, col);
				if (arg_t.x != null && !Chc.inLeft(syms_t.first.get(i)).equals(arg_t.x)) {
					throw new RuntimeException("In " + this + ", the head " + head + " at position " + i + " is expecting argument type " + atts_t + " but its argument has actual type " + arg_t.x.toStringMash());					
				}
				arg_t.set(Chc.inLeft(syms_t.first.get(i))); //redundant sometimes
				i++;
			}
			return new Ref<>(Chc.inLeft(syms_t.second));
		} else if (isVar && ctx.containsKey(head)) {
			return ctx.get(head);
		} else {
			Ref<Chc<Ty,En>> ref = new Ref<>();
			ctx.put(head, ref);
			return ref; 
		}
		
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotation == null) ? 0 : annotation.hashCode());
		result = prime * result + ((args == null) ? 0 : args.hashCode());
		result = prime * result + ((head == null) ? 0 : head.hashCode());
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
		RawTerm other = (RawTerm) obj;
		if (annotation == null) {
			if (other.annotation != null)
				return false;
		} else if (!annotation.equals(other.annotation))
			return false;
		if (args == null) {
			if (other.args != null)
				return false;
		} else if (!args.equals(other.args))
			return false;
		if (head == null) {
			if (other.head != null)
				return false;
		} else if (!head.equals(other.head))
			return false;
		return true;
	}

	private RawTerm(String head, List<RawTerm> args, String annotation) {
		if (head == null) {
			throw new RuntimeException("Attempt to create raw term with null head");
		} else if (args == null) {
			throw new RuntimeException("Attempt to create raw term with null args");			
		} else if (annotation != null && !args.isEmpty()) {
			throw new RuntimeException("Attempt to annotate raw term with arguments");
		}
		this.head = head;
		this.args = args;
		this.annotation = annotation;
	}
	
	public RawTerm(String head, String annotation) {
		this(head, new LinkedList<>(), annotation);
	}
	
	public RawTerm(String head, List<RawTerm> args) {
		this(head, args, null);
	}

	//TODO: use of toString here is ugly
	public static RawTerm fold (Set<Object> fks, Set<Object> entities, List<Object> l, String v) {
		RawTerm ret = new RawTerm(v, (String) null);
		for (Object o : l) {
			if (entities.contains(o)) {
				if (fks.contains(o)) {
					throw new RuntimeException("In " + Util.sep(l, ".") + ", " + o + "is ambiguous: it can refer to an entity (0-length path) or a foreign key (1-length path)");
				} 
				continue;
			}
			ret = new RawTerm(o.toString(), Util.singList(ret));
		}
		return ret;
	}
	
	
	
}
