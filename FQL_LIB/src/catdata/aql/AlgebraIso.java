package catdata.aql;

//TODO aql redo using functions instead of maps
public class AlgebraIso<Ty,En,Sym,Fk,Att,X,Y,A,B> {

/* extends Algebra<Ty,En,Sym,Fk,Att,A,B,A,B> {

	
	public AlgebraIso(Algebra<Ty, En, Sym, Fk, Att, X, Y, X, Y> alg, Map<X, A> xa, Map<A, X> ax, Map<Y, B> yb, Map<B, Y> by) {
		this.alg = alg;
		this.xa = xa;
		this.ax = ax;
		this.yb = yb;
		this.by = by;
	}
	
	@Override
	public String printGen(A a) {
		return alg.printGen(ax(a));
	}
	@Override
	public String printSk(B b) {
		return alg.printSk(by(b));
	}
	@Override
	public String printX(A a) {
		return alg.printX(ax(a));
	}
	@Override
	public String printY(B b) {
		return alg.printY(by(b));
	}

	Algebra<Ty,En,Sym,Fk,Att,X,Y,X,Y> alg;
	
	Map<X,A> xa;
	Map<A,X> ax;
	Map<Y,B> yb;
	Map<B,Y> by;
	
	private A xa(X x) {
		if (!xa.containsKey(x)) {
			throw new RuntimeException("Not an xa " + x + " in " + xa);
		}
		return xa.get(x);
	}
	private X ax(A a) {
		if (!ax.containsKey(a)) {
			throw new RuntimeException("Not an ax " + a + " in " + ax);
		}
		return ax.get(a);
	}
	private Y by(B b) {
		if (!by.containsKey(b)) {
			throw new RuntimeException("Not an by " + b + " in " + by);
		}
		return by.get(b);	
	}
	private B yb(Y y) {
		if (!yb.containsKey(y)) {
			throw new RuntimeException("Not an yb " + y + " in " + yb);
		}
		return yb.get(y);
	}
	
	
	@Override
	public boolean eq(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, A, B> lhs, Term<Ty, En, Sym, Fk, Att, A, B> rhs) {
		return alg.eq(ctx, abxy(lhs), abxy(rhs));
	}
	
	@Override
	public boolean hasNFs() {
		return alg.hasNFs();
	}
	
	@Override
	public Term<Ty, En, Sym, Fk, Att, A, B> nf(Ctx<Var, Chc<Ty, En>> ctx, Term<Ty, En, Sym, Fk, Att, A, B> term) {
		return xyab(alg.nf(ctx, abxy(term)));
	}
	
	
	@Override
	public Schema<Ty, En, Sym, Fk, Att> schema() {
		return alg.schema();
	}
	
	@Override
	public Collection<A> en(En en) {
		return alg.en(en).stream().map(this::xa).collect(Collectors.toList());
	}
	
	@Override
	public A fk(Fk fk, A a) {
		return xa(alg.fk(fk, ax(a)));
	}
	
	@Override
	public Term<Ty, Void, Sym, Void, Void, Void, B> att(Att att, A a) {
		return yb(alg.att(att, ax(a)));
	}
	
	@Override
	public Term<Ty, Void, Sym, Void, Void, Void, B> sk(B sk) {
		return yb(alg.sk(by(sk)));
	}
	
	@Override
	public A nf(Term<Void, En, Void, Fk, Void, A, Void> term) {
		return xa(alg.nf(ax(term)));
	}
	
	@Override
	public Term<Void, En, Void, Fk, Void, A, Void> repr(A x) {
		return xa(alg.repr(ax(x)));
	}
	
	@Override
	protected Collage<Ty, Void, Sym, Void, Void, Void, B> talg() {
		Collage<Ty, Void, Sym, Void, Void, Void, B> ret = new Collage<>(schema().typeSide);
		
		Util.putAllSafely(ret.sks, Util.compose0(by, alg.talg().sks));
		for (Triple<Ctx<Var, Chc<Ty, Void>>, Term<Ty, Void, Sym, Void, Void, Void, Y>, Term<Ty, Void, Sym, Void, Void, Void, Y>> eq : alg.talg().eqs) {
			ret.eqs.add(new Triple<>(eq.first, yb(eq.second), yb(eq.third)));
		}
		
		return ret;
	}
		
	//TODO clean up
	public <P,Q> Term<Ty, Void, Sym, Void, Void, Void, P> transSk(Term<Ty, Void, Sym, Void, Void, Void, Q> term, Map<Q,P> m) {
		if (term.obj != null || term.var != null) {
			return term.convert();
		} else if (term.sk != null) {
			return Term.Sk(m.get(term.sk));
		} else if (term.sym != null) {
			return Term.Sym(term.sym, term.args().stream().map(x -> transSk(x, m)).collect(Collectors.toList()));
		}
		throw new RuntimeException("Anomaly: please report");
	}
	public <P,Q> Term<Void, En, Void, Fk, Void, P, Void> transGen(Term<Void, En, Void, Fk, Void, Q, Void> term, Map<Q,P> m) {
		if (term.obj != null || term.var != null) {
			return term.convert();
		} else if (term.gen != null) {
			return Term.Gen(m.get(term.gen));
		} else if (term.fk != null) {
			return Term.Fk(term.fk,transGen(term.arg, m));
		}
		throw new RuntimeException("Anomaly: please report");
	}
	public <P,Q,R,S> Term<Ty, En, Sym, Fk, Att, R, S> transAll(Term<Ty, En, Sym, Fk, Att, P, Q> term, Map<P,R> m, Map<Q,S> n) {
		if (term.obj != null || term.var != null) {
			return term.convert();
		} else if (term.gen != null) {
			return Term.Gen(m.get(term.gen));
		} else if (term.sk != null) {
			return Term.Sk(n.get(term.sk));
		} else if (term.fk != null) {
			return Term.Fk(term.fk,transAll(term.arg, m, n));
		} else if (term.sym != null) {
			return Term.Sym(term.sym, term.args().stream().map(x -> transAll(x, m, n)).collect(Collectors.toList()));
		} else if (term.att != null) {
			return Term.Att(term.att,transAll(term.arg, m, n));
		}
		throw new RuntimeException("Anomaly: please report");
	}
	
	public Term<Ty, Void, Sym, Void, Void, Void, B> yb(Term<Ty, Void, Sym, Void, Void, Void, Y> term) {
		return transSk(term, yb);
	}
	public Term<Ty, Void, Sym, Void, Void, Void, Y> by(Term<Ty, Void, Sym, Void, Void, Void, B> term) {
		return transSk(term, by);
	}
	public Term<Void, En, Void, Fk, Void, A, Void> xa(Term<Void, En, Void, Fk, Void, X, Void> term) {
		return transGen(term, xa);
	}
	public Term<Void, En, Void, Fk, Void, X, Void> ax(Term<Void, En, Void, Fk, Void, A, Void> term) {
		return transGen(term, ax);
	}
	public Term<Ty, En, Sym, Fk, Att, A, B> xyab(Term<Ty, En, Sym, Fk, Att, X, Y> term) {
		return transAll(term, xa, yb);
	}
	public Term<Ty, En, Sym, Fk, Att, X, Y> abxy(Term<Ty, En, Sym, Fk, Att, A, B> term) {
		return transAll(term, ax, by);
	}
	
	@Override
	public Term<Ty, En, Sym, Fk, Att, A, B> reprT(Term<Ty, Void, Sym, Void, Void, Void, B> y) {
		return xyab(alg.reprT(by(y)));
	}
	
	@Override
	protected String toStringProver() {
		return "AlgIso wrap of " + alg.toStringProver();
	} 
	
	*/
	
}
