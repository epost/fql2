package catdata.aql;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Ref;
import catdata.Triple;
import catdata.Util;

public final class RawTerm {

	private final String head;
	private final List<RawTerm> args;
	
	private final String annotation;
	
	@Override
	public String toString() {
		String str = (annotation == null ? "" : "@" + annotation);
		if (args.isEmpty()) {
			return head + str;
		}
		if (args.size() == 1) {
			return args.get(0) + "." + head;
		}
		return head + "(" + Util.sep(args, ", ") + ")";
	}
	
	//TODO aql inefficient bitwise operations

	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Term<Ty, En, Sym, Fk, Att, Gen, Sk>
	infer0(Map<String, Chc<Ty,En>> ctx0, RawTerm lhs, Chc<Ty,En> expected, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col, String pre, AqlJs<Ty,Sym> js) {
 
		Map<String, Chc<Ty,En>> ctx = new HashMap<>(ctx0);		
		String fresh = "expected sort of " + lhs;
		ctx.put(fresh, expected);

		try {
		
			Triple<Ctx<String, Chc<Ty,En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> ret = infer1(ctx, new RawTerm(fresh, Collections.emptyList()), lhs, col, js);
			
			Chc<Ty, En> actual = ctx.get(fresh);
			actual.assertNeitherNull();
			expected.assertNeitherNull();
			
			if (!actual.equals(expected)) {
				throw new RuntimeException("in " + lhs + ", infered sort is " + actual.toStringMash() + " which is not the expected sort " + expected.toStringMash());												
			} 
		
			return ret.third;

		} catch (RuntimeException ex) {
			ex.printStackTrace();
			throw new RuntimeException(pre + ex.getMessage());
		}
			
		
	}
	
	/**
	 * 
	 * @return it is misleading to return a context, because strings for primitives can come out
	 */
	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Triple<Ctx<String, Chc<Ty,En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>
	infer1(Map<String, Chc<Ty,En>> ctx, RawTerm lhs, RawTerm rhs, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col, AqlJs<Ty,Sym> js) {
			
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
			throw new RuntimeException(lhs + " is " + lhs_t.x.toStringMash() + " but " + rhs + " actually has sort " + rhs_t.x.toStringMash());
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
		return new Triple<>(ret, lhs.trans(vars, ctx0, col, js), rhs.trans(vars, ctx0, col, js));
	}
	
	//TODO aql statically typesafe coerce
	private static <K,V> K find(Map<K,V> m, String k) {
		for (K k0 : m.keySet()) {
			if (k.equals(k0)) {
				return k0;
			}
		}
		throw new RuntimeException("Anomaly, please report");
	}
	
	@SuppressWarnings("unchecked")
	private static <X> X resolve(String name) {
		return (X) name; //TODO aql make this smarter
 	}
	
	@SuppressWarnings("unchecked")
	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Head<Ty, En, Sym, Fk, Att, Gen, Sk> toHeadNoPrim(String head, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
		int n = boolToInt(col.syms.containsKey(resolve(head))) + boolToInt(col.atts.containsKey(resolve(head))) + boolToInt(col.fks.containsKey(resolve(head))) +  boolToInt(col.gens.containsKey(resolve(head))) + boolToInt(col.sks.containsKey(resolve(head)));
		if (n == 0) {
			throw new RuntimeException(head + " is not a symbol");						
		} else if (n > 1) {
			throw new RuntimeException(head + " is ambiguous");			
		}
		if (col.syms.containsKey(resolve(head))) {
			return Head.Sym((Sym)head);
		} else if (col.atts.containsKey(resolve(head))) {
			return Head.Att((Att)head);
		} else if (col.fks.containsKey(resolve(head))) {
			return Head.Fk((Fk)head);
		} else if (col.gens.containsKey(resolve(head))) {
			return Head.Gen((Gen)head);
		} if (col.sks.containsKey(resolve(head))) {
			return Head.Sk((Sk)head);
		}
		throw new RuntimeException("Anomaly: please report");
	}
	
	
	
	
	private <Ty, En, Sym, Fk, Att, Gen, Sk> Term<Ty, En, Sym, Fk, Att, Gen, Sk> trans(Set<String> vars, Map<String, Ref<Chc<Ty, En>>> ctx0, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col, AqlJs<Ty, Sym> js) {
		List<Term<Ty, En, Sym, Fk, Att, Gen, Sk>> args0 = args.stream().map(x -> x.trans(vars, ctx0, col, js)).collect(Collectors.toList());
			
		int n = boolToInt(vars.contains(head)) + boolToInt(col.syms.containsKey(resolve(head))) + boolToInt(col.atts.containsKey(resolve(head))) + boolToInt(col.fks.containsKey(resolve(head))) + boolToInt(col.gens.containsKey(resolve(head))) + boolToInt(col.sks.containsKey(resolve(head)));
		if (n > 1) {
			throw new RuntimeException(head + " is ambiguously a variable/function/attribute/foreign key/generator/lablled null");			
		}
		
		if (annotation != null) {
			@SuppressWarnings("unchecked")
			Ty ty = (Ty) annotation; 
			Object o = js.parse(ty, head); //compile(code);
			return Term.Obj(o, ty);
		} else if (vars.contains(head)) {
			return Term.Var(new Var(head));
		} else if (col.syms.containsKey(resolve(head))) {
			return Term.Sym(find(col.syms.map, resolve(head)), args0);
		} else if (col.atts.containsKey(resolve(head))) {
			return Term.Att(find(col.atts.map, resolve(head)), args0.get(0));
		} else if (col.fks.containsKey(resolve(head))) {
			return Term.Fk(find(col.fks.map, resolve(head)), args0.get(0)); 
		} else if (col.gens.containsKey(resolve(head))) {
			return Term.Gen(find(col.gens.map, resolve(head)));
		} else if (col.sks.containsKey(resolve(head))) {
			return Term.Sk(find(col.sks.map, head));
		} else if (ctx0.containsKey(head) && !ctx0.get(head).x.left) {
			//this must be a generator - but why isn't it in gens?
			throw new RuntimeException("Error: " + this + " inferred as a generator, but available generators at this location are: " + col.gens.keySet());
		} else if (ctx0.containsKey(head) && ctx0.get(head).x.left) {
			Ty ty = ctx0.get(head).x.l;
			if (ty == null) {
				throw new RuntimeException("Anomaly: please report");
			}
			if (!col.java_tys.containsKey(ty) && col.java_parsers.containsKey(ty)) {
				throw new RuntimeException("Error in " + this + ", " + ty + " has a java parser but is not declared as a java type");		
			}
			if (col.java_tys.containsKey(ty) && !col.java_parsers.containsKey(ty)) {
				throw new RuntimeException("Error in " + this + ", " + ty + " is a java type but does not hava a java parser");		
			}
			if (!col.java_tys.containsKey(ty) && !col.java_parsers.containsKey(ty)) {
				throw new RuntimeException("Error in " + this + ", symbol not defined");		
			}
			Object o = js.parse(ty, head);
			return Term.Obj(o, ty);
		} 
		throw new RuntimeException("Anomaly, please report: " + this);
	}

	
	private static int boolToInt(boolean b) {
		return b ? 1 : 0;
	}

	@SuppressWarnings({"ConstantConditions"})
    private <Ty, En, Sym, Fk, Att, Gen, Sk> Ref<Chc<Ty,En>> infer(Set<String> vars, Map<String, Ref<Chc<Ty, En>>> ctx, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
		boolean isSym, isAtt, isFk, isGen, isSk, isVar, isObj ;
				
		isObj = (annotation != null);
		
		Pair<List<Ty>, Ty> syms_t = col.syms.map.get(resolve(head)); //actually want possible null here
		isSym = syms_t != null && !isObj;
		Pair<En, Ty> atts_t = col.atts.map.get(resolve(head));
		isAtt = atts_t != null && !isObj;
		Pair<En, En> fks_t = col.fks.map.get(resolve(head));
		isFk = fks_t != null && !isObj;
		En gens_t = col.gens.map.get(resolve(head));
		isGen = gens_t != null && !isObj;
		Ty sks_t = col.sks.map.get(resolve(head));
		isSk = sks_t != null && !isObj;
		
		isVar = vars.contains(new Var(resolve(head))) && !isObj;
			
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
				System.out.println(arg_t.x);
				throw new RuntimeException("In " + this + ", the head " + head + " is an attribute/foreign key expecting argument type " + ty + " but its argument has actual type " + arg_t.x.toStringMash());					
			}
			arg_t.set(Chc.inRight(ty)); //redundant sometimes

            return isAtt ? new Ref<>(Chc.inLeft(atts_t.second)) : new Ref<>(Chc.inRight(fks_t.second));
		} else if (isSym) {
			if (args.size() != syms_t.first.size()) {
				throw new RuntimeException("In " + this + ", the head " + head + " is a typeside symbol of arity " + syms_t.first.size() + " but it is given " + args.size() + " arguments");
			}
			int i = 0;
			for (RawTerm arg : args) {
				Ref<Chc<Ty,En>> arg_t = arg.infer(vars, ctx, col);
				if (arg_t.x != null && !Chc.inLeft(syms_t.first.get(i)).equals(arg_t.x)) {
					throw new RuntimeException("In " + this + ", the head " + head + " at position " + i + " is expecting argument type " + syms_t.first.get(i) + " but its argument has actual type " + arg_t.x.toStringMash());					
				}
				arg_t.set(Chc.inLeft(syms_t.first.get(i))); //redundant sometimes
				i++;
			}
			return new Ref<>(Chc.inLeft(syms_t.second));
		} else if (ctx.containsKey(head)) {
			return ctx.get(head);
		} else if (head != null && args.isEmpty()) { 
			Ref<Chc<Ty,En>> ref = new Ref<>();
			ctx.put(head, ref);
			return ref; 
		} else if (head != null && !args.isEmpty()) {
			throw new RuntimeException("In " + this + ", the head " + head + " is not a function symbol");
		} else {
			throw new RuntimeException("Anomaly: please report");
		}
		
	}
	
	@Override
	public int hashCode() {
		int prime = 31;
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
	

	
	public RawTerm(String head) {
		this(head, Collections.emptyList(), null);
	}

	//TODO: aql use of toString here is ugly
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
	
	public static RawTerm fold (List<String> l, String v) {
		RawTerm ret = new RawTerm(v, (String) null);
		for (Object o : l) {
			ret = new RawTerm(o.toString(), Util.singList(ret));
		}
		return ret;
	}
	
	public static <Ty, En, Sym, Fk, Att, Gen, Sk> Triple<Ctx<Var, Chc<Ty,En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>> infer2(List<Pair<String, String>> l, RawTerm a, RawTerm b, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col, AqlJs<Ty,Sym> js) {
		Map<String, Chc<Ty, En>> ctx = new HashMap<>();
		for (Pair<String, ?> p : l) {
			if (ctx.containsKey(p.first)) {
				throw new RuntimeException("Duplicate variable " + p.first + " in context " + Ctx.toString(l));
			}
			if (p.second != null) {
				if (col.tys.contains(p.second) && col.ens.contains(p.second)) {
					throw new RuntimeException("Ambiguous: " + p.second + " is an entity and a type");
				} else if (col.tys.contains(p.second)) {
					@SuppressWarnings("unchecked")
					Ty tt = (Ty) p.second;
					ctx.put(p.first, Chc.inLeft(tt));		//TODO aql remove for loops for other ones	
				} else if (col.ens.contains(p.second)) {
					@SuppressWarnings("unchecked")
					En tt = (En) p.second;
					ctx.put(p.first, Chc.inRight(tt));										
				} else {
					throw new RuntimeException(p.second + " is neither a type nor entity");
				}
			} else {
				ctx.put(p.first, null);
			}
		}
		Triple<Ctx<String,Chc<Ty,En>>,Term<Ty, En, Sym, Fk, Att, Gen, Sk>,Term<Ty, En, Sym, Fk, Att, Gen, Sk>>
		eq0 = infer1(ctx, a, b, col, js);

		LinkedHashMap<Var, Chc<Ty,En>> map = new LinkedHashMap<>();
		for (String k : ctx.keySet()) {
			Chc<Ty, En> v = eq0.first.get(k);
			map.put(new Var(k), v);
		}
		/*for (String k : eq0.first.keys()) {
			if (!ctx.keySet().contains(k)) {
				throw new RuntimeException("In " + eq.second + " = " + eq.third + ", not a variable or symbol: " + k + " . (Note: java not allowed in typeside equations)");
			}
		}	*/
		Ctx<Var, Chc<Ty,En>> ctx2 = new Ctx<>(map);
		
		Triple<Ctx<Var, Chc<Ty,En>>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>
		 tr = new Triple<>(ctx2, eq0.second, eq0.third);
		return tr;
	}
	
	
}
