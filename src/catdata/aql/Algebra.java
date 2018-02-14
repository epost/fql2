package catdata.aql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;


public abstract class Algebra<Ty,En,Sym,Fk,Att,Gen,Sk,X,Y> /* implements DP<Ty,En,Sym,Fk,Att,Gen,Sk> */ {
	
	
	//TODO aql generic map method like printX
	
	//TODO aql add final eq method here
	
	public abstract Schema<Ty,En,Sym,Fk,Att> schema();
	
	//TODO aql cant validate algebras bc are not dps
	
	 public abstract boolean hasFreeTypeAlgebra();
	 
	 /*{
		return talg().eqs.isEmpty();
	} */
	
	public abstract boolean hasFreeTypeAlgebraOnJava(); /* {
		return talg().eqs.stream().filter(x -> talg().java_tys.containsKey(talg().type(x.ctx, x.lhs).l)).collect(Collectors.toList()).isEmpty();
	} */
	
	/**
	 * The Xs need be to be unique across ens, so that repr can invert.  Is it worth checking this? TODO aql
	 */
	public abstract Collection<X> en(En en);
	
	private final Map<Triple<En, List<Pair<Fk, X>>, List<Pair<Att, Object>>>, Collection<X>> en_index2 = Collections.synchronizedMap(new HashMap<>());

	/*
	public synchronized Collection<X> en_indexed(En en, List<Pair<Fk, X>> fks, List<Pair<Att, Object>> atts) {
		//TODO aql just pick smallest
		if (atts.size() > 0) {
			return en_indexedAtt(en, atts.get(0).first, atts.get(0).second);
		} else if (atts.size() > 0) {
			return en_indexedFk(en, fks.get(0).first, fks.get(0).second);						
		} 
		return en(en);
		
	} */
	
	
	public synchronized Collection<X> en_indexed(En en, List<Pair<Fk, X>> fks, List<Pair<Att, Object>> atts) {
		Triple<En, List<Pair<Fk, X>>, List<Pair<Att, Object>>> t = new Triple<>(en, fks, atts);
		if (en_index2.containsKey(t)) {
			return en_index2.get(t);
		} else if (atts.isEmpty() && fks.size() == 1) {
			return en_indexedFk(en, fks.get(0).first, fks.get(0).second);
		} else if (fks.isEmpty() && atts.size() == 1) {
			return en_indexedAtt(en, atts.get(0).first, atts.get(0).second);			
		} else if (atts.isEmpty() && fks.isEmpty()) {
			return en(en);
		} 
		List<X> l = new LinkedList<>(en(en));
		for (Pair<Fk, X> p : fks) {
			l.retainAll(en_indexedFk(en, p.first, p.second));
		}
		for (Pair<Att, Object> p : atts) {
			l.retainAll(en_indexedAtt(en, p.first, p.second));			
		}
		en_index2.put(t, l);
		return l;		
	}

	private final Map<Triple<En, Fk, X>, Collection<X>> fk_index = Collections.synchronizedMap(new HashMap<>());
	public synchronized Collection<X> en_indexedFk(En en, Fk fk, X x) {
		Triple<En, Fk, X> t = new Triple<>(en, fk, x);
		if (fk_index.containsKey(t)) {
			return fk_index.get(t);
		}
		List<X> ret = new LinkedList<>();
		for (X y : en(en)) {
			if (fk(fk, y).equals(x)) {
				ret.add(y);
			}
		}
		fk_index.put(t, ret);
		return ret;
	}
	public synchronized Collection<X> en_indexedAtt(En en, Att att, Object y) {
		Triple<En, Att, Object> t = new Triple<>(en, att, y);
		if (att_index.containsKey(t)) {
			return att_index.get(t);
		} else if (!hasFreeTypeAlgebra() || schema().typeSide.tys.size() != schema().typeSide.js.java_tys.size()) {
			return en(en);
		}
		List<X> ret = new LinkedList<>();
		for (X x : en(en)) {
			if (att(att, x).equals(Term.Obj(y, schema().atts.get(att).second))) { //TODO aql only works bc free
				ret.add(x);
			}
		}
		att_index.put(t, ret);
		return ret;
	}
	
	private final Map<Triple<En, Att, Object>, Collection<X>> att_index = Collections.synchronizedMap(new HashMap<>());
	

	public abstract X gen(Gen gen);
	
	public abstract X fk(Fk fk, X x);
	
	public abstract Term<Ty, Void, Sym, Void, Void, Void, Y> att(Att att, X x);
	
	public abstract Term<Ty, Void, Sym, Void, Void, Void, Y> sk(Sk sk);

	public final X nf(Term<Void, En, Void, Fk, Void, Gen, Void> term) {
		if (term.gen != null) {
			return gen(term.gen);
		} else if (term.fk != null) {
			return fk(term.fk, nf(term.arg)); 
		}
		throw new RuntimeException("Anomaly: please report");
	}

	public abstract Term<Void, En, Void, Fk, Void, Gen, Void> repr(X x);
	
	//rows
	public int size() {
		int i = 0;
		for (En en : schema().ens) {
			i += en(en).size();
		}
		return i;
	}

	private Collection<X> allXs;

	public synchronized Collection<X> allXs() {
		if (allXs != null) {
			return allXs;
		}
		allXs = new LinkedList<>();
		for (En en : schema().ens) {
			allXs.addAll(en(en));
		}
		return allXs;
	}
	
	
	
	/**
	 * @return only equations for instance part (no typeside, no schema)
	 */
	public abstract Collage<Ty, Void, Sym, Void, Void, Void, Y> talg();

	/**
	 * @param y the T of Y must be obtained from a call to att or sk only!
	 * @return not a true normal form, but a 'simplified' term for e.g., display purposes
	 */
	public synchronized final Term<Ty,En,Sym,Fk,Att,Gen,Sk> reprT(Term<Ty, Void, Sym, Void, Void, Void, Y> y) {
		Term<Ty,En,Sym,Fk,Att,Gen,Sk> ret = reprT_cache.get(y);
		if (ret != null) {
			return ret;
		}
		ret = reprT_protected(y);
		reprT_cache.put(y, ret);
		return ret;
	}
	
	private final Map<Term<Ty, Void, Sym, Void, Void, Void, Y>, Term<Ty,En,Sym,Fk,Att,Gen,Sk>> reprT_cache = Collections.synchronizedMap(new HashMap<>());
	public abstract Term<Ty,En,Sym,Fk,Att,Gen,Sk> reprT_protected(Term<Ty, Void, Sym, Void, Void, Void, Y> y);
	
	private final Map<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, Void, Sym, Void, Void, Void, Y>>
	intoY_cache = Collections.synchronizedMap(new HashMap<>());

	/**
	 * @param term of type sort
	 */
	public synchronized Term<Ty, Void, Sym, Void, Void, Void, Y> intoY(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		Term<Ty, Void, Sym, Void, Void, Void, Y> ret = intoY_cache.get(term);
		if (ret != null) {
			return ret;
		}
		ret = intoY0(reprT(intoY0(term)));
		intoY_cache.put(term, ret);
		return ret;
	}
		
	private Term<Ty, Void, Sym, Void, Void, Void, Y> intoY0(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
			if (term.obj != null) {
				return Term.Obj(term.obj, term.ty);
			} else if (term.sym != null) {
				return Term.Sym(term.sym, term.args().stream().map(this::intoY0).collect(Collectors.toList()));
			} else if (term.sk != null) {
				return sk(term.sk);
			} else if (term.att != null) {
				return att(term.att, intoX(term.arg));
			}
			throw new RuntimeException("Anomaly: please report: " + term);
		}

	/**
	 * @param term term of type entity
	 */
		public synchronized X intoX(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
			if (term.gen != null) {
				return nf(term.asGen());
			} else if (term.fk != null) {
				return fk(term.fk, nf(term.arg.asArgForFk()));
			}
			throw new RuntimeException("Anomaly: please report");
		}
/*
		public X eval(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term, Var var, X x) {
			if (term.var.equals(var)) {
				return x;
			} else if (term.gen != null) {
				return nf(term.asGen());
			} else if (term.fk != null) {
				return fk(term.fk, nf(term.arg.asArgForFk()));
			}
			throw new RuntimeException("Anomaly: please report");
		} */
	
	
	public abstract String toStringProver();
		/*
	public abstract String printSk(Sk y);
	public abstract String printGen(Gen x);
	*/
	public abstract String printX(X x);
	public abstract String printY(Y y);
	
	/*
	 * 	
	public String printSk(Sk y) { //TODO aql
		return y.toString();
	} 
	public String printGen(Gen x) {
		return x.toString();
	} 
	public String printX(X x) { 
		return x.toString();
	} 
	public String printY(Y y) {
		return y.toString(); 
	}
	 */
	


// --Commented out by Inspection START (12/24/16, 10:43 PM):
//	//TODO aql visitor cleanup
//    private Term<Ty, En, Sym, Fk, Att, X, Y> trans(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
//		if (term.var != null) {
//			return Term.Var(term.var);
//		} else if (term.obj != null) {
//			return Term.Obj(term.obj, term.ty);
//		} else if (term.sym != null) {
//			return Term.Sym(term.sym, term.args.stream().map(this::trans).collect(Collectors.toList()));
//		} else if (term.att != null) {
//			return Term.Att(term.att, trans(term.arg));
//		} else if (term.fk != null) {
//			return Term.Fk(term.fk, trans(term.arg));
//		} else if (term.gen != null) {
//			return Term.Gen(nf(Term.Gen(term.gen)));
//		} else if (term.sk != null) {
//			return sk(term.sk).map(Function.identity(), Function.identity(), Util.voidFn(), Util.voidFn(), Util.voidFn(), Function.identity());
//		}
//		throw new RuntimeException("Anomaly: please report");
//	}
// --Commented out by Inspection STOP (12/24/16, 10:43 PM)

	
	//TODO: aql have simplified collages also print out their definitions
	
	
	@Override
	public String toString() {
		String ret;

		//TODO aql stop printing eventually
		ret = "carriers\n\t";
		ret += Util.sep(schema().ens.stream().map(x -> x + " -> {" + Util.sep(en(x).stream().map(this::printX).collect(Collectors.toList()), ", ") + "}").collect(Collectors.toList()), "\n\t");
	
		ret += "\n\nforeign keys";
		for (Fk fk : schema().fks.keySet()) {
			ret += "\n\t" + fk + " -> {" + Util.sep(en(schema().fks.get(fk).first).stream().map(x -> "(" + x + ", " + printX(fk(fk, x)) + ")").collect(Collectors.toList()), ", ") + "}";
		}
		
		ret += "\n\nattributes";
		for (Att att : schema().atts.keySet()) {
			ret += "\n\t" + att + " -> {" + Util.sep(en(schema().atts.get(att).first).stream().map(x -> "(" + x + ", " + att(att, x).toString(this::printY, Util.voidFn()) + ")").collect(Collectors.toList()), ", ") + "}";
		}
		
		ret += "\n\n----- type algebra\n\n";
		ret += talg().toString();
		
//		ret += "\n\n----- prover\n\n"; 
	//	ret += toStringProver();
		
		return ret;
	}
	
//	private Object intify_idx_lock = new Object();
//	private static volatile int intify_idx = 0;
	
//	private Pair<Map<X,Integer>, Map<Integer, X>> intifyX;
	public Pair<Map<X,Integer>, Map<Integer, X>> intifyX(int i) {
		Pair<Map<X,Integer>, Map<Integer, X>> intifyX = new Pair<>(new LinkedHashMap<>(), new LinkedHashMap<>());
		for (En en : schema().ens) {
			for (X x : en(en)) {
				intifyX.first.put(x, i);
				intifyX.second.put(i, x);
				i++;
			}
		}
			
		return intifyX;
	}
	
/*	
	public Term<Ty, Void, Sym, Void, Void, Void, Y> trans(Term<Ty, En, Sym, Fk, Att, Gen, Sk> term) {
		if (term.obj != null) {
			return Term.Obj(term.obj, term.ty);
		} else if (term.sym != null) {
			return Term.Sym(term.sym, term.args().stream().map(x -> trans(x)).collect(Collectors.toList()));
		} else if (term.sk != null) {
			return sk(term.sk);
		} else if (term.att != null) {
			return att(term.att, trans1(term.arg.convert()));
		}
		throw new RuntimeException("Anomaly: please report");
	}

	private X trans1(Term<Void, En, Void, Fk, Void, Gen, Void> term) {
		if (term.gen != null) {
			return nf(term);
		} else if (term.fk != null) {
			return fk(term.fk, nf(term.arg));
		}
		throw new RuntimeException("Anomaly: please report");
	}
	*/
	private static int session_id = 0;
	private Connection conn;
	private Collection<String> indicesLoaded = new LinkedList<>();
	
	
	private final Map<Fk, Set<Pair<X,X>>> fkAsSet0 = new HashMap<>();
	public synchronized Set<Pair<X, X>> fkAsSet(Fk fk) {
		if (fkAsSet0.containsKey(fk)) {
			return fkAsSet0.get(fk);
		}
		Set<Pair<X,X>> set = new HashSet<>();
		for (X x : en(schema().fks.get(fk).first)) {
			set.add(new Pair<>(x,fk(fk, x)));
		}
		fkAsSet0.put(fk, set);
		return set;
	}
	
	private final Map<Att, Set<Pair<X, Term<Ty, Void, Sym, Void, Void, Void, Y>>>> attsAsSet0 = new HashMap<>();
	public synchronized Set<Pair<X, Term<Ty, Void, Sym, Void, Void, Void, Y>>> attAsSet(Att att) {
		if (attsAsSet0.containsKey(att)) {
			return attsAsSet0.get(att);
		}
		Set<Pair<X,Term<Ty, Void, Sym, Void, Void, Void, Y>>> set = new HashSet<>();
		for (X x : en(schema().atts.get(att).first)) {
			set.add(new Pair<>(x,att(att, x)));
		}
		attsAsSet0.put(att, set);
		return set;
	}
	
	/**
	 * MUST close this connection
	 */
	public Connection createAndLoad(Map<En, List<String>> indices, Pair<Map<X,Integer>, Map<Integer, X>> I, int vlen) {
		try {
			Map<En, Triple<List<Chc<Fk, Att>>, List<String>, List<String>>> xxx = schema().toSQL("", "integer", "id", -1, Object::toString, vlen);
			Connection conn = DriverManager.getConnection("jdbc:h2:mem:db_temp_" + session_id++ + ";DB_CLOSE_DELAY=-1");
			try (Statement stmt = conn.createStatement()) {
				for (En en1 : schema().ens) {
					Triple<List<Chc<Fk, Att>>, List<String>, List<String>> qqq = xxx.get(en1);
					for (String s : qqq.second) {
						stmt.execute(s);
					}
					for (String s : qqq.third) {
						//don't need fks for AQL's internal use
						if (!s.startsWith("alter table")) {
							stmt.execute(s);
						}
					}
					for (String s : indices.get(en1)) {
						stmt.execute(s);
					}
					for (X x : en(en1)) {
						storeMyRecord(I, conn, x, qqq.first, en1.toString(), "", -1);
					}
				}
				
				stmt.close();
				//this.conn = conn;
				return conn;
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new RuntimeException(ex);
			}				
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}
	
	
	/**
	 * DO NOT close this connection
	 */
	//client should not remove
	public synchronized Connection addIndices(Pair<Map<X,Integer>, Map<Integer, X>> I, Map<En, List<String>> indices, int vlen) {
		if (conn == null) {
			conn = createAndLoad(indices, I, vlen);
			this.indicesLoaded = new LinkedList<>();
			for (List<String> l : indices.values()) {
				this.indicesLoaded.addAll(l);
			}
			return conn;
		}
		try (Statement stmt = conn.createStatement()) {
			for (List<String> ss : indices.values()) {
				for (String s : ss) {
					if (!indicesLoaded.contains(s)) {
						stmt.execute(s);
						indicesLoaded.add(s);
					}
				}
			}
			stmt.close();
			return conn;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}			
	}
	
	//TODO aql refactor
		public synchronized void storeMyRecord(Pair<Map<X,Integer>, Map<Integer, X>> I, Connection conn2, X x, List<Chc<Fk, Att>> header, String table, String prefix, int truncate) throws Exception {
			
			
			List<String> hdrQ = new LinkedList<>();
			  List<String> hdr = new LinkedList<>();
			  hdr.add("id");
			  hdrQ.add("?");
	      for (Chc<Fk, Att> aHeader : header) {
	          hdrQ.add("?");
	          Chc<Fk, Att> chc = aHeader;
	          if (chc.left) {
	              hdr.add( Schema.truncate( chc.l.toString(), truncate) ); //TODO aql unsafe
	          } else {
	              hdr.add( Schema.truncate( chc.r.toString(), truncate ) ); //TODO aql unsafe
	          }
	      }
			  
			  String insertSQL = "INSERT INTO " + prefix + table + "(" + Util.sep(hdr,"," )+ ") values (" + Util.sep(hdrQ,",") + ")";
			  PreparedStatement ps = conn2.prepareStatement(insertSQL);
			
			  ps.setObject(1, I.first.get(x), Types.INTEGER);
			
			  for (int i = 0; i < header.size(); i++) {
				  Chc<Fk,Att> chc = header.get(i);
				  if (chc.left) {
					  ps.setObject(i+1+1, I.first.get(fk(chc.l, x)), Types.INTEGER);			   
				  } else {
					  Object o = fromTerm(att(chc.r, x));
					  ps.setObject(i+1+1, o, SqlTypeSide.getSqlType(schema().atts.get(chc.r).second.toString()));			   			  
				  }
			  }
			
			  ps.executeUpdate();
		} 


		private Object fromTerm(Term<Ty, Void, Sym, Void, Void, Void, Y> term) {
			if (term.obj != null) {
				return term.obj;
			}
			return null;
		}
	
		@Override
		protected void finalize() {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		

}
