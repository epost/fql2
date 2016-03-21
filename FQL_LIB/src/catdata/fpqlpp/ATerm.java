package catdata.fpqlpp;


import java.util.List;


/**
 * 
 * @author ryan
 *
 * @param <C> typeside constants (0, "bill", +)
 * @param <F> foreign keys (manager)
 * @param <A> attributes (name)
 * @param <TG> generators at type (infinity)
 * @param <EG> generators at entity (bill)
 * @param <V> variables for substitution (v)
 */
public abstract class ATerm<C,F,A,TG,EG,V> implements Comparable<ATerm<C,F,A,TG,EG,V>> {
	
	
	
	public static class ATermSum<C,F,A,TG,EG> {
		public C c;
		public F f;
		public A a;
		public TG tg;
		public EG eg;
	}
	
	public static class ATermUnknown<C,F,A,TG,EG,V> extends ATerm<C,F,A,TG,EG,V> {
		public ATermUnknown(Object obj) {
			this.obj = obj;
		}
		public Object obj;
		public List<ATermUnknown<C,F,A,TG,EG,V>> args;
	}

	public static class ATermVar<C,F,A,TG,EG,V> extends ATerm<C,F,A,TG,EG,V> {
		public V var;
		public ATermVar(V a) {
			if (a == null) {
				throw new RuntimeException();
			}
			var = a;
		}
	}

	public static class ATermTGen<C,F,A,TG,EG,V> extends ATerm<C,F,A,TG,EG,V> {
		public TG head;
		public ATermTGen(TG h) {
			if (h == null) {
				throw new RuntimeException();
			}
			head = h;
		}
	}
	
	public static class ATermEGen<C,F,A,TG,EG,V> extends ATerm<C,F,A,TG,EG,V> {
		public EG head;
		public ATermEGen(EG h) {
			if (h == null) {
				throw new RuntimeException();
			}
			head = h;
		}
	}
	
	public static class ATermConst<C,F,A,TG,EG,V> extends ATerm<C,F,A,TG,EG,V> {
		public C head;
		public List<ATerm<C,F,A,TG,EG,V>> args;
		
		public ATermConst(C h, List<ATerm<C,F,A,TG,EG,V>> a) {
			if (h == null || a == null) {
				throw new RuntimeException();
			}
			head = h;
			args = a;
		}
	}
	
	public static class ATermEdge<C,F,A,TG,EG,V> extends ATerm<C,F,A,TG,EG,V> {
		public F head;
		public ATerm<C,F,A,TG,EG,V> arg;
		
		public ATermEdge(F e, ATerm<C,F,A,TG,EG,V> a0) {
			if (e == null || a0 == null) {
				throw new RuntimeException();
			}
			head = e;
			arg = a0;
		}
	}
	
	public static class ATermAtt<C,F,A,TG,EG,V> extends ATerm<C,F,A,TG,EG,V> {
		public A head;
		public ATerm<C,F,A,TG,EG,V> arg;
		
		public ATermAtt(A e, ATerm<C,F,A,TG,EG,V> a0) {
			if (e == null || a0 == null) {
				throw new RuntimeException();
			}
			head = e;
			arg = a0;
		}
	}

	
	///////////////////////////////////////////////////////////////////////////////////////////////////////

/*	
	public boolean isGround(OplCtx<?, V> ctx) {
		if (var != null) {
			return ctx.vars0.containsKey(var);
		}
		for (ATerm<X,V> t : args) {
			if (!t.isGround(ctx)) {
				return false;
			}
		}
		return true; 
	}  */

	
	
	///////////////////////////////////////////////////////////

	

//	public <S, C> S type(OplSig<S, C, V> sig, OplCtx<S, V> ctx) {
	//	throw new RuntimeException(); 
/*		if (var != null) {
			return ctx.get(var);
		}
		Pair<List<S>, S> t = sig.getSymbol(head);

		List<S> actual = args.stream().map(x -> x.type(sig, ctx)).collect(Collectors.toList());
		if (t.first.size() != actual.size()) {
			throw new DoNotIgnore("Argument length mismatch for " + this + ", expected "
					+ t.first.size() + " but given " + actual.size()) ;
		}
		for (int i = 0; i < actual.size(); i++) {
			if (!t.first.get(i).equals(actual.get(i))) {
				S z = actual.get(i);
				String y = z == null ? " (possible forgotten ())" : "";
				throw new DoNotIgnore("Argument mismatch for " + this
						+ ", expected term of sort " + t.first.get(i) + " but given "
						+ args.get(i) + " of sort " + z + y);
			}
		}

		return t.second; */
	//}
/*
	@Override
	public String toString() {
		if (var != null) {
			return var.toString();
		}
		Integer i = Util.termToNat(this);
		if (i != null) {
			return Integer.toString(i);
		}
		List<String> x = args.stream().map(z -> z.toString()).collect(Collectors.toList());
		String q = Util.sep(x, ", ").trim();
		if (q.length() > 0) {
			q = "(" + q + ")";
  		}
		String ret =  Util.maybeQuote(strip(head.toString())) + q;
		return ret;
	}
*/
	

//	public <S> ATerm<X,V> subst(OplCtx<S, V> G, List<ATerm<X,V>> L) {
	//	throw new RuntimeException(); 
		/*	
		if (var != null) {
			int i = G.indexOf(var);
			if (i == -1) {
				return this;
			}
			return L.get(i);
		}
		List<ATerm<C, V>> args0 = args.stream().map(x -> x.subst(G, L))
				.collect(Collectors.toList());
		return new ATerm<>(head, args0); */
	//}

//	public ATerm<X,V> subst(Map<V, ATerm<X,V>> s) {
	//	throw new RuntimeException(); 
		/*	
		if (var != null) {
			ATerm<C, V> t = s.get(var);
			if (t != null) {
				return t;
			}
			return this;
		}
		List<ATerm<C, V>> l = new LinkedList<>();
		for (ATerm<C, V> a : args) {
			l.add(a.subst(s));
		}
		return new ATerm<>(head, l); */
	//}


	

	// needed for machine learning library for some reason
	@Override
	public int compareTo(ATerm<C,F,A,TG,EG,V> o) {
		return o.toString().compareTo(toString());
	}

}