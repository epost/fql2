package catdata.aql.fdm;

public class SigmaChaseAlgebra {
/*
<Ty, En1, Sym, Fk1, Att1, Gen, Sk, En2, Fk2, Att2, X, Y>
		extends Algebra<Ty, En2, Sym, Fk2, Att2, Chc<ID, X>, Chc<ID, Y>, Chc<ID, X>, Chc<ID, Y>>
	implements DP<Ty, En2, Sym, Fk2, Att2, Chc<ID, X>, Chc<ID, Y>> {

	private final Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> F;
	private final Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> I;

	private Content theContent;

	static <Z> boolean isEmpty0(Collection<Set<Z>> S) {
		for (Set<?> s : S) {
			if (!s.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private class Content {

		final Ctx<En1, Set<Pair<X, Chc<ID, X>>>> m = new Ctx<>(Util.newSetsFor(F.src.ens));

		final Ctx<En2, Set<Chc<ID, X>>> ens = new Ctx<>(Util.newSetsFor(F.dst.ens));

		
		final Ctx<Ty, Set<Chc<ID, Y>>> tys = new Ctx<>(Util.newSetsFor(F.src.typeSide.tys));

		final Ctx<Fk2, Set<Pair<Chc<ID, X>, Chc<ID, X>>>> fks = new Ctx<>(Util.newSetsFor(F.dst.fks.keySet()));

		final Ctx<Att2, Set<Pair<Chc<ID, X>, Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>>>>> atts = new Ctx<>(
				Util.newSetsFor(F.dst.atts.keySet()));

		//shoudl really be called "no tuples to add"
		public boolean isEmpty() {
			return isEmpty0(m.values()) && isEmpty0(ens.values()) && isEmpty0(tys.values()) && isEmpty0(fks.values())
					&& isEmpty0(atts.values());
		}

		public void addFrom(Content other) {
			for (Ty ty : schema().typeSide.tys) {
				tys.get(ty).addAll(other.tys.get(ty));
			} /*
			for (Ty ty : schema().typeSide.tys) {
				idsT.get(ty).addAll(other.idsT.get(ty));
			}
			for (En2 en2 : schema().ens) {
				ids.get(en2).addAll(other.ids.get(en2));
			} 
			for (En2 en2 : schema().ens) {
				ens.get(en2).addAll(other.ens.get(en2));
			}
			for (Fk2 fk2 : schema().fks.keySet()) {
				fks.get(fk2).addAll(other.fks.get(fk2));
			}
			for (Att2 att2 : schema().atts.keySet()) {
				atts.get(att2).addAll(other.atts.get(att2));
			}
			for (En1 en1 : F.src.ens) {
				m.get(en1).addAll(other.m.get(en1)); 
			}
			
		}

		@Override
		public String toString() {
			return "Content [\nm=" + m + "\nens=" + ens + "\ntys=" + tys + "\nfks="
					+ fks + "\natts=" + atts + "]";
		}

	}

	// todo - keep inverse of each edge
	// todo - make this into an actual map

	private <X> void merge(X x, X y, UnionFind<X> uf) {
		if (!x.equals(y)) {
			uf.union(x, y);
			egd_fired = true;
		}
	}
	
	boolean egd_fired;
	
	UnionFind<Chc<ID, X>> ids = null;

	UnionFind<Chc<Pair<Object, Ty>, Chc<ID, Y>>> idsT = null;
	
	public SigmaChaseAlgebra(Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> f,
			Instance<Ty, En1, Sym, Fk1, Att1, Gen, Sk, X, Y> i, AqlOptions strat, int max) {
		if (!f.src.equals(i.schema())) {
			throw new RuntimeException(
					"In sigma instance, source of mapping is " + f.src + ", but instance has type " + i.schema());
		}
		F = f;
		I = i;
		if (!I.algebra().hasFreeTypeAlgebra()) {
			throw new RuntimeException("Chase sigma cannot be used on instances with non free type algebras, given\n\n"
					+ I.algebra().talg());
		}

		theContent = new Content();
//		boolean success = false;
		for (int j = 0; j < max; j++) {
		//	System.out.println("theContent start: " + theContent);

			ids = new UnionFind<>(new HashSet<>());
			idsT= new UnionFind<>(new HashSet<>());
			egd_fired = false;
			
			Content toAdd = fireTgds();
			boolean changed = !toAdd.isEmpty() || egd_fired;
		//	System.out.println("toAdd: " + toAdd);
			theContent.addFrom(toAdd);

		//	System.out.println("eqs1: " + ids);
		//	System.out.println("eqs2: " + idsT);
			
			if (changed) {
				theContent = update();
		//		System.out.println("theContent after egds: " + theContent);
			} else {
				return;
				//succes = true
			}
			//if (!changed) {
			//	success = true;
			//	break;
			//}
		}
		//if (!success) {
			throw new RuntimeException("No convergence after " + max + " iterations.");
		//}
	}

	private Content update() {
		Content ret = new Content();
		
		for (En2 en2 : schema().ens) {
			for (Chc<ID, X> x : theContent.ens.get(en2)) {
				ret.ens.get(en2).add(ids.find(x));
			}
		}
		
/*		for (En2 en2 : schema().ens) {
			for (Pair<Chc<ID, X>, Chc<ID, X>> x : theContent.ids.get(en2)) {
				ret.ids.get(en2).add(new Pair<>(ufE.find(x.first), ufE.find(x.second)));
			}
		}
		
		for (Fk2 fk2 : schema().fks.keySet()) {
			for (Pair<Chc<ID, X>, Chc<ID, X>> x : theContent.fks.get(fk2)) {
				ret.fks.get(fk2).add(new Pair<>(ids.find(x.first), ids.find(x.second)));
			}
		}
		
		for (En1 en1 : F.src.ens) {
			for (Pair<X, Chc<ID, X>> x : theContent.m.get(en1)) {
				ret.m.get(en1).add(new Pair<>(x.first, ids.find(x.second)));
			}
		}
		
		Map<Chc<Pair<Object, Ty>, Chc<ID, Y>>, Set<Chc<Pair<Object, Ty>, Chc<ID, Y>>>> 
		eqcs = idsT.toMap();
		
		Ctx<Set<Chc<Pair<Object, Ty>, Chc<ID, Y>>>, Pair<Object, Ty>> m2 = new Ctx<>();
		for (Set<Chc<Pair<Object, Ty>, Chc<ID, Y>>> set : eqcs.values()) {
			List<Chc<Pair<Object,Ty>,Chc<It.ID,Y>>> y = set.stream().filter(x -> x.left).collect(Collectors.toList());
			if (y.size() > 1) {
				throw new RuntimeException("Chase failure (resultant type algebra possibly inconsistent): " + y + " being equated.");				
			} else if (y.size() == 1) {
				m2.put(set, Util.get0(y).l);
			}
		}
		
		Function<Chc<ID, Y>,Chc<Pair<Object, Ty>, Chc<ID, Y>>> fun = x -> {
			Set<Chc<Pair<Object, Ty>, Chc<ID, Y>>> s = eqcs.get(x);
			if (s != null) {
				Pair<Object, Ty> u = m2.map.get(s);
				if (u != null) {
					return Chc.inLeft(u);
				} else {
					return idsT.find(Chc.inRight(x));
				}
			} else {
				return Chc.inRight(x);
			}
		};
		
		for (Ty ty : schema().typeSide.tys) {
			for (Chc<ID, Y> x : theContent.tys.get(ty)) {
				Chc<Pair<Object, Ty>, Chc<ID, Y>> y = fun.apply(x);
				if (!y.left) {
					ret.tys.get(ty).add(y.r);
				}				
			}
		}
		
		for (Att2 att2 : schema().atts.keySet()) {
			for (Pair<Chc<ID, X>, Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>>> x : theContent.atts.get(att2)) {
				ret.atts.get(att2).add(new Pair<>(ids.find(x.first), repl(x.second, idsT, fun)));
			}
		}
		/*
		for (Ty ty : schema().typeSide.tys) {
			for (Pair<Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>>> x : ret.idsT.get(ty)) {
				ret.idsT.get(ty).add(new Pair<>(repl(x.first, ufT, fun), repl(x.second, ufT, fun)));
			}
		}

		
		//TODO: aql sanity check
		/*
		for (Ty ty : schema().typeSide.tys) {
			for (Pair<Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>>> x : ret.idsT.get(ty)) {
				if (!x.first.equals(x.second)) {
					Util.anomaly();
				}
			}
		} 
		/*
		for (En2 en : schema().ens) {
			for (Pair<Chc<ID, X>, Chc<ID, X>> x : ret.ids.get(en)) {
				if (!x.first.equals(x.second)) {
					Util.anomaly();
				}
			}
		}
		
		for (Fk2 fk2 : schema().fks.keySet()) {
			Util.toMapSafely(ret.fks.get(fk2));
		}
		for (Att2 att2 : schema().atts.keySet()) {
			Util.toMapSafely(ret.atts.get(att2));
		}
		for (En1 en1 : F.src.ens) {
			Util.toMapSafely(ret.m.get(en1));
		}
		
		return ret;
	}

	private Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>> repl(Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>> t,
			UnionFind<Chc<Pair<Object, Ty>, Chc<ID, Y>>> ufT, Function<Chc<ID, Y>,Chc<Pair<Object, Ty>, Chc<ID, Y>>> fun) {
		if (t.obj != null) {
			return t;
		} else if (t.sym != null) {
			List<Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>>> l = new LinkedList<>();
			for (int i = 0; i < t.args.size(); i++) {
				l.add(repl(t.args.get(i), ufT, fun));
			}
			return Term.Sym(t.sym, l);
		} else if (t.sk != null) {
			Chc<Pair<Object, Ty>, Chc<ID, Y>> r = fun.apply(t.sk);
			if (r.left) {
				return Term.Obj(r.l.first, r.l.second);
			}
			return Term.Sk(r.r);
		}
		return Util.anomaly();
		
	}
/*
	private Pair<UnionFind<Chc<ID, X>>, UnionFind<Chc<Pair<Object,Ty>,Chc<ID, Y>>>> fireEgds() { 
		UnionFind<Chc<ID, X>> ufE = new UnionFind<>(new HashSet<>());
		UnionFind<Chc<Pair<Object,Ty>,Chc<ID, Y>>> ufT = new UnionFind<>(new HashSet<>());
		
		boolean b = false;
		for (Set<Pair<Chc<ID, X>, Chc<ID, X>>> set : theContent.ids.values()) {
			for (Pair<Chc<ID, X>, Chc<ID, X>> p : set) {
				if (!p.first.equals(p.second)) {
					b = true;
					ufE.union(p.first, p.second);
				}
			}
		}
		for (Set<Pair<Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>>>> set : theContent.idsT.values()) {
			for (Pair<Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>>> p : set) {
				b = fireEgds(p.first, p.second, ufT) | b;
			}
		}
		
		if (b) {
			return new Pair<>(ufE, ufT);
		}
		return null;
	} 

	private boolean fireEgds(Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>> l,
			Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>> r) {
		if (l.obj != null && r.obj != null) {
			if (!l.obj.equals(r.obj)) {
				throw new RuntimeException("Chase failure (resultant type algebra possibly inconsistent): " + l.obj + " and" + r.obj + " and being equated.");
			}
			return false;
		} else if (l.sk != null && r.sk != null) {
			if (l.sk.equals(r.sk)) {
				return false;
			}
//			I.sk
			merge(Chc.inRight(l.sk), Chc.inRight(r.sk), idsT);
			return true;
		} else if (l.sym != null && r.sym != null) {
			if (!l.sym.equals(r.sym)) {
				throw new RuntimeException("Chase failure (resultant type algebra possibly inconsistent): " + l.sym + " and" + r.sym + " and being equated.");				
			}
			boolean b = false;
			for (int i = 0; i < l.args.size(); i++) {
				b = fireEgds(l.args.get(i), r.args.get(i)) | b;
			}
			return b;
		} else if (l.sk != null && r.obj != null) {
			merge(Chc.inRight(l.sk), Chc.inLeft(new Pair<>(r.obj, r.ty)), idsT);	
			return true;
		} else if (r.sk != null && l.obj != null) {
			merge(Chc.inRight(r.sk), Chc.inLeft(new Pair<>(l.obj, l.ty)), idsT);	
			return true;			
		}
		return Util.anomaly();
	}

	private It fr = new It();
	

	private Content fireTgds() {
		Content toAdd = new Content();

		//S(x) -> Ey. F(x,y) 
		ensToSyms(toAdd);
		
//		System.out.println("A");
//		System.out.println(toAdd);


		//F(x,y) -> S(x) /\ T(x)
		symsToEns(toAdd);

//		System.out.println("B");
//		System.out.println(toAdd);

		//S(x) -> E a..z. F(x,a) ... T1(a,z) /\ T2(b,z)
		pathEqs(toAdd);

//		System.out.println("C");
	//	System.out.println(toAdd);

		//F(x,y) /\ F(x,z) -> id(y,z)
		functional(toAdd);

//		System.out.println("D");
	//	System.out.println(toAdd);


		return toAdd;
	}

	public void functional(Content toAdd) {
		//fks, atts, m, id, idT
		for (Fk2 fk2 : F.dst.fks.keySet()) {
			addToId(theContent.fks.get(fk2), ids);
		}
		for (Att2 att2 : F.dst.atts.keySet()) {
			addToId2(theContent.atts.get(att2));
		}
		for (En1 en1 : F.src.ens) {
			addToId(theContent.m.get(en1), ids);
		}/*
		for (En2 en2 : F.dst.ens) {
			addToId(ids.get(en2), ids.get(en2));
		}
		for (Ty ty : F.dst.typeSide.tys) {
			addToId(idsT.get(ty), idsT.get(ty));
		}
	}
	
	private void addToId2(Set<Pair<Chc<ID, X>, Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>>>> set) {
		Map<Chc<ID, X>, Set<Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>>>> r = Util.convertMulti(set);
		for (Chc<ID, X> x : r.keySet()) {
			if (r.get(x).size() > 1) {
				List<Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>>> l = new LinkedList<>(r.get(x));
				Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>> y = l.get(0);
				for (int j = 1; j < l.size(); j++) {
					fireEgds(y, l.get(j));
				}
			}
		}
	}

	public static <X,Y> void addToId(Set<Pair<X,Y>> set, UnionFind<Y> unionFind) {
		Map<X, Set<Y>> r = Util.convertMulti(set);
		for (X x : r.keySet()) {
			if (r.get(x).size() > 1) {
				List<Y> l = new LinkedList<>(r.get(x));
				Y y = l.get(0);
				for (int j = 1; j < l.size(); j++) {
					unionFind.union(y, l.get(j));						
				}
			}
		}
	}


	//TODO: do as egds
	public void pathEqs(Content toAdd) {
		// path equations from T
		
		for (Triple<Pair<Var, En2>, Term<Ty, En2, Sym, Fk2, Att2, Void, Void>, Term<Ty, En2, Sym, Fk2, Att2, Void, Void>> eq : F.dst.eqs) {
			for (Chc<ID, X> x : theContent.ens.get(eq.first.second)) {
				if (F.dst.type(eq.first, eq.second).left) {
					Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>> l = evalT(x, eq.second);
					Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>> r = evalT(x, eq.third);
					//TODO aql equals ok here because free talg
					if (l == null || r == null || !l.equals(r)) { 
						Chc<ID, Y> y = Chc.inLeft(fr.next());
						addT(x, y, eq.second, toAdd, eq.first.second);
						addT(x, y, eq.third, toAdd, eq.first.second);
					}
				} else {
					Chc<ID, X> l = evalE(x, eq.second);
					Chc<ID, X> r = evalE(x, eq.third);
					if (l == null || r == null || !l.equals(r)) {
						Chc<ID, X> y = Chc.inLeft(fr.next());
						addE(x, y, eq.second, toAdd, eq.first.second);
						addE(x, y, eq.third, toAdd, eq.first.second);
					}
				}
			}
		}
		
		// path equations from M
		//fk1.m_t = m_s.F(fk1)
		for (Fk1 fk1 : F.src.fks.keySet()) {
			En1 s = F.src.fks.get(fk1).first;
			En1 t = F.src.fks.get(fk1).second;
			Pair<En2, List<Fk2>> f = F.fks.get(fk1);
			
			for (X xs : I.algebra().en(s)) {
				X xt = I.algebra().fk(fk1, xs);
				Chc<ID, X> l = Util.lookupNull(theContent.m.get(t), xt);
				
				Chc<ID, X> r = null;
				Chc<ID, X> start = Util.lookupNull(theContent.m.get(s), xs);
				Term<Ty, En2, Sym, Fk2, Att2, Void, Void> rhs = Term.Fks(f.second, Term.Var(new Var("v")));
				if (start != null) {
					r = evalE(start, rhs);
				}
				
				if (l == null || r == null || !l.equals(r)) { //TODO aql ok bc free
					Chc<ID, X> end = Chc.inLeft(fr.next());
					//toAdd.m.get(t).add(new Pair<>(xt, end));
					addE(Chc.inRight(xs), end, rhs, toAdd, f.first);
				}
			}
		}
	}
	
	
	//TODO aql the egds should just add to identity tables, then process only id tables

	private void addT(Chc<ID, X> start, Chc<ID, Y> end, Term<Ty, En2, Sym, Fk2, Att2, Void, Void> t, Content toAdd, En2 en2) {
		if (t.att != null) {
			Chc<ID, X> z = Chc.inLeft(fr.next());
			addE(start, z, t.arg, toAdd, en2);
			theContent.atts.get(t.att).add(new Pair<>(z, Term.Sk(end)));
			return;
		} else if (t.obj != null) {
			merge(Chc.inLeft(new Pair<>(t.obj, t.ty)), Chc.inRight(end),idsT);
			return;
		}
		Util.anomaly();
	}
	
	private void addE(Chc<ID, X> start, Chc<ID, X> end, Term<Ty, En2, Sym, Fk2, Att2, Void, Void> t, Content toAdd, En2 en2) {
		if (t.var != null) {
			merge(start, end, ids);
			return;
		} else if (t.fk != null) {
			Chc<ID, X> z = Chc.inLeft(fr.next());
			addE(z, end, t.arg, toAdd, F.dst.fks.get(t.fk).second);
			theContent.fks.get(t.fk).add(new Pair<>(start, z));
			return;
		}
		Util.anomaly();
	}

	private Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>> evalT(Chc<ID, X> x,
			Term<Ty, En2, Sym, Fk2, Att2, Void, Void> t) {
		if (t.obj != null) {
			return Term.Obj(t.obj, t.ty);
		} else if (t.att != null) {
			Chc<ID, X> v = evalE(x, t.arg);
			if (v == null) {
				return null;
			}
			for (Pair<Chc<ID, X>, Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>>> p : theContent.atts.get(t.att)) {
				if (p.first.equals(v)) {
					return p.second;
				}
			}
			return null;
		} else if (t.sym != null) {
			throw new RuntimeException("Cannot have equations between terms that have typeside functions in them");
		}
		return Util.anomaly();
	}

	private Chc<ID, X> evalE(Chc<ID, X> x, Term<Ty, En2, Sym, Fk2, Att2, Void, Void> t) {
		if (t.var != null) {
			return x;
		} else if (t.fk != null) {
			Chc<ID, X> v = evalE(x, t.arg);
			if (v == null) {
				return null;
			}
			for (Pair<Chc<ID, X>, Chc<ID, X>> p : theContent.fks.get(t.fk)) {
				if (p.first.equals(v)) {
					return p.second;
				}
			}
			return null;
		}
		return Util.anomaly();
	}

	public void symsToEns(Content toAdd) {
		// F(x,y) -> T(x) /\ T'(y)
				// m, fk, att, id, idT
		for (Fk2 fk2 : F.dst.fks.keySet()) {
			for (Pair<Chc<ID, X>, Chc<ID, X>> p : theContent.fks.get(fk2)) {
				if (!theContent.ens.get(F.dst.fks.get(fk2).first).contains(p.first)) {
					toAdd.ens.get(F.dst.fks.get(fk2).first).add(p.first);
				}
				if (!theContent.ens.get(F.dst.fks.get(fk2).second).contains(p.second)) {
					toAdd.ens.get(F.dst.fks.get(fk2).second).add(p.second);
				}
			}
		}
		for (Att2 att2 : F.dst.atts.keySet()) {
			for (Pair<Chc<ID, X>, Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>>> p : theContent.atts.get(att2)) {
				if (!theContent.ens.get(F.dst.atts.get(att2).first).contains(p.first)) {
					toAdd.ens.get(F.dst.atts.get(att2).first).add(p.first);
				}
				Set<Chc<ID, Y>> set = new HashSet<>();
				getYs(set, p.second);
				for (Chc<ID, Y> xs : set) {
					if (!theContent.tys.get(F.dst.atts.get(att2).second).contains(xs)) {
						toAdd.tys.get(F.dst.atts.get(att2).second).add(xs);
					}
				}
			}
		}
		for (En1 en1 : F.src.ens) {
			for (Pair<X, Chc<ID, X>> p : theContent.m.get(en1)) {
				if (!I.algebra().en(en1).contains(p.first)) {
					Util.anomaly();
				}
				if (!theContent.ens.get(F.ens.get(en1)).contains(p.second)) {
					toAdd.ens.get(F.ens.get(en1)).add(p.second);
				}
			}
		}/*
		for (En2 en2 : F.dst.ens) {
			for (Pair<Chc<ID, X>, Chc<ID, X>> p : theContent.ids.get(en2)) {
				if (!theContent.ens.get(en2).contains(p.first)) {
					toAdd.ens.get(en2).add(p.first);
				}
				if (!theContent.ens.get(en2).contains(p.second)) {
					toAdd.ens.get(en2).add(p.second);
				}
			}
		} /*
		for (Ty ty : F.dst.typeSide.tys) {
			for (Pair<Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>>, Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>>> p : theContent.idsT.get(ty)) {
				Set<Chc<ID, Y>> set = new HashSet<>();
				getYs(set, p.second);
				getYs(set, p.first);
				for (Chc<ID, Y> xs : set) {
					if (!theContent.tys.get(ty).contains(xs)) {
						toAdd.tys.get(ty).add(xs);
					}
				}
			}			
		} 
	}
		

	private void getYs(Set<Chc<ID, Y>> set, Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>> t) {
		if (t.sk != null) {
			set.add(t.sk);
		} else if (t.sym != null) {
			for (Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>> u : t.args) {
				getYs(set, u);
			}
		}
	}

	public void ensToSyms(Content toAdd) {
		// T(x) -> Ey. F(x,y)
		
		// fk, att, m, id, idT 
		for (En1 en1 : F.src.ens) {
			for (X x : I.algebra().en(en1)) {
				if (!Util.containsKey(theContent.m.get(en1), x)) {				
					toAdd.m.get(en1).add(new Pair<>(x, Chc.inLeft(fr.next())));
				}
			}
		}
	//	for (Y y : I.algebra().talg().sks.keySet()) {
		//	if (!Util.containsKey(theContent.idsT.get(I.algebra().talg().sks.get(y)), Term.Sk(Chc.inRight(y)))) {				
				//cheating a little, see comment below as well
			//	toAdd.idsT.get(I.algebra().talg().sks.get(y)).add(new Pair<>(Term.Sk(Chc.inRight(y)), Term.Sk(Chc.inRight(y))));
			//}
		// }
		
		for (En2 en2 : F.dst.ens) {
			for (Chc<ID, X> x : theContent.ens.get(en2)) {
			//	if (!Util.containsKey(theContent.ids.get(en2), x)) {
				//	toAdd.ids.get(en2).add(new Pair<>(x, x)); //this is technically cheating a little, not to create a new one
			//	}
			
				for (Fk2 fk2 : F.dst.fksFrom(en2)) {
					if (!Util.containsKey(theContent.fks.get(fk2), x)) {
						toAdd.fks.get(fk2).add(new Pair<>(x, Chc.inLeft(fr.next())));
					}
				}
				
				for (Att2 att2 : F.dst.attsFrom(en2)) {
					if (!Util.containsKey(theContent.atts.get(att2), x)) {
						toAdd.atts.get(att2).add(new Pair<>(x, Term.Sk(Chc.inLeft(fr.next()))));
					}
				}
			}
		}

	}



	@Override
	public Schema<Ty, En2, Sym, Fk2, Att2> schema() {
		return F.dst;
	}

	@Override
	public Collection<Chc<ID, X>> en(En2 en) {
		return theContent.ens.get(en);
	}

	@Override
	public Chc<ID, X> gen(Chc<ID, X> gen) {
		return gen;
	}

	@Override
	public Chc<ID, X> fk(Fk2 fk, Chc<ID, X> x) {
		return Util.toMapSafely(theContent.fks.get(fk)).get(x);
	}

	@Override
	public Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>> att(Att2 att, Chc<ID, X> x) {
		return Util.toMapSafely(theContent.atts.get(att)).get(x);
	}

	@Override
	public Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>> sk(Chc<ID, Y> sk) {
		return Term.Sk(sk);
	}

	@Override
	public Term<Void, En2, Void, Fk2, Void, Chc<ID, X>, Void> repr(Chc<ID, X> x) {
		return Term.Gen(x);
	}

	@Override
	public Term<Ty, En2, Sym, Fk2, Att2, Chc<ID, X>, Chc<ID, Y>> reprT_protected(
			Term<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>> y) {
		return y.map(Function.identity(), Function.identity(), Util.voidFn(), Util.voidFn(), Util.voidFn(),
				Function.identity());
	}

	@Override
	public String toStringProver() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String printX(Chc<ID, X> x) {
		// TODO Auto-generated method stub
		return x.toString();
	}

	@Override
	public String printY(Chc<ID, Y> y) {
		//I.algebra().re
		
		// TODO Auto-generated method stub
		return y.toString();
	}

	@Override
	public boolean eq(Ctx<Var, Chc<Ty, En2>> ctx, Term<Ty, En2, Sym, Fk2, Att2, Chc<ID, X>, Chc<ID, Y>> lhs,
			Term<Ty, En2, Sym, Fk2, Att2, Chc<ID, X>, Chc<ID, Y>> rhs) {
		if (!lhs.hasTypeType()) {
			return this.intoX(lhs).equals(intoX(rhs));
		} else {
			return this.intoY(lhs).equals(intoY(rhs));
		}
	}
	
	@Override
	public Collage<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>> talg() {
		Collage<Ty, Void, Sym, Void, Void, Void, Chc<ID, Y>> ret = new Collage<>();

		for (Ty ty1 : schema().typeSide.tys) {
			for (Chc<ID, Y> s : theContent.tys.get(ty1)) {
				ret.sks.put(s, ty1);
			}
		}
		
		return ret;
	}

	*/

}
