package catdata.fpql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import catdata.Pair;
import catdata.fpql.XExp.XTy;

public class XEnvironment {

	XCtx<String> global;
	public XProgram prog;
	public String str;
	public Map<String, XObject> objs;
//	public Set<String> types;
//	public Map<String, Pair<String, String>> fns;
//	public Set<Pair<List<String>, List<String>>> eqs = new HashSet<>();
	// a.b.c = p.q.r
	
	public XEnvironment(XProgram prog, String str) {
		this.prog = prog;
		this.str = str;
		
		Set<String> types;
		Map<String, Pair<String, String>> fns;
		Set<Pair<List<String>, List<String>>> eqs = new HashSet<>();

		objs = new HashMap<>();
		
		types = new HashSet<>();
		types.add("_1");
		fns = new HashMap<>();
		fns.put("_1", new Pair<>("_1", "_1"));
		eqs = new HashSet<>();

		for (Entry<String, XExp> k : prog.exps.entrySet()) {
			XExp e = k.getValue();
			if (e instanceof XTy) {
				if (k.getKey().equals("_1")) {
					throw new RuntimeException("Type 1 re-bound by user");
				}
				types.add(k.getKey());
				fns.put(k.getKey(), new Pair<>(k.getKey(), k.getKey()));
//				eqs.add(new Pair)
			}
			if (e instanceof XExp.XFn) {
				if (!types.contains(((XExp.XFn) e).src)) {
					throw new RuntimeException("Unknown type: " + ((XExp.XFn) e).src);
				}
				if (!types.contains(((XExp.XFn) e).dst)) {
					throw new RuntimeException("Unknown type: " + ((XExp.XFn) e).dst);
				}
				fns.put(k.getKey(), new Pair<>(((XExp.XFn) e).src, ((XExp.XFn) e).dst));
			}
			if (e instanceof XExp.XConst) {
				if (!types.contains(((XExp.XConst) e).dst)) {
					throw new RuntimeException("Unknown type: " + ((XExp.XConst) e).dst);
				}
				fns.put(k.getKey(), new Pair<>("_1", ((XExp.XConst) e).dst));
			}
			//: check these here?
			if (e instanceof XExp.XEq) {
				eqs.add(new Pair<>(((XExp.XEq) e).lhs, ((XExp.XEq) e).rhs));
			}
		}
		
		global = new XCtx<>(types, fns, eqs, null, null, "global");		
	}

	@Override
	public String toString() {
		return "XEnvironment [global=" + global + ", prog=" + prog + ", str=" + str + ", objs="
				+ objs + "]";
	}
	
}
