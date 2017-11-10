package catdata.aql.exp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Collage;
import catdata.aql.Eq;
import catdata.aql.Instance;
import catdata.aql.It;
import catdata.aql.It.ID;
import catdata.aql.Kind;
import catdata.aql.Term;
import catdata.aql.exp.InstExpRaw.Gen;
import catdata.aql.exp.InstExpRaw.Sk;
import catdata.aql.exp.SchExpRaw.Att;
import catdata.aql.exp.SchExpRaw.En;
import catdata.aql.exp.SchExpRaw.Fk;
import catdata.aql.exp.TyExpRaw.Sym;
import catdata.aql.exp.TyExpRaw.Ty;
import catdata.aql.fdm.InitialAlgebra;
import catdata.aql.fdm.LiteralInstance;

//TODO AQL CSV version of this
//TODO merge this with coproduct sigma
public final class InstExpJdbcQuotient<X, Y>
extends InstExp<Ty,En,Sym,Fk,Att,Gen,Sk,ID,Chc<Sk, Pair<ID, Att>>>  implements Raw {
	
	public final InstExp<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> I;
	
	public final Map<String, String> options;
	
	public final Map<LocStr,String> queries;
	
	public final String clazz;
	
	public final String jdbcString;
	
	@Override
	public Map<String, String> options() {
		return options;
	}
	
	@Override
	public Collection<Pair<String, Kind>> deps() {
		return I.deps();
	}



	public InstExpJdbcQuotient(String clazz, String jdbcString, InstExp<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> i, 
			List<Pair<LocStr,String>> queries,
			List<Pair<String, String>> options) {
		I = i;
		this.options = Util.toMapSafely(options);
		this.queries = Util.toMapSafely(queries);
		this.clazz = clazz;
		this.jdbcString = jdbcString;
		Util.checkClass(clazz);
		
		List<InteriorLabel<Object>> f = new LinkedList<>();
		for (Pair<LocStr, String> p : queries) {
			f.add(new InteriorLabel<>("entities", new Pair<>(p.first.str, p.second), p.first.loc,
					x -> x.first + " -> " + x.second).conv());
		}
		raw.put("entities", f);
	}
	private Ctx<String, List<InteriorLabel<Object>>> raw = new Ctx<>();
	
	@Override 
	public Ctx<String, List<InteriorLabel<Object>>> raw() {
		return raw;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((I == null) ? 0 : I.hashCode());
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result + ((jdbcString == null) ? 0 : jdbcString.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		result = prime * result + ((queries == null) ? 0 : queries.hashCode());
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
		InstExpJdbcQuotient<?, ?> other = (InstExpJdbcQuotient<?, ?>) obj;
		if (I == null) {
			if (other.I != null)
				return false;
		} else if (!I.equals(other.I))
			return false;
		if (clazz == null) {
			if (other.clazz != null)
				return false;
		} else if (!clazz.equals(other.clazz))
			return false;
		if (jdbcString == null) {
			if (other.jdbcString != null)
				return false;
		} else if (!jdbcString.equals(other.jdbcString))
			return false;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		if (queries == null) {
			if (other.queries != null)
				return false;
		} else if (!queries.equals(other.queries))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "quotient_jdbc " + clazz + " " + jdbcString + " " + I + " {\n" + Util.sep(queries, " -> ", "\n") + "\n}"; 
	} 

	@Override
	public SchExp<Ty, En, Sym, Fk, Att> type(AqlTyping G) {
		return I.type(G);
	}

	@Override
	public Instance<Ty, En, Sym, Fk, Att, Gen, Sk, ID, Chc<Sk, Pair<ID, Att>>> eval(AqlEnv env) {
		Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> J = I.eval(env);
		Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col = new Collage<>(J.collage());
		AqlOptions strat = new AqlOptions(options, col, env.defaults);

		String toGet = jdbcString;
		String driver = clazz;
		if (clazz.trim().isEmpty()) {
			driver = (String) strat.getOrDefault(AqlOption.jdbc_default_class);
			Util.checkClass(driver);
		}
		if (jdbcString.trim().isEmpty()) {
			toGet = (String) strat.getOrDefault(AqlOption.jdbc_default_string);
		}
		
		
		Set<Pair<Term<Ty, En, Sym, Fk, Att, Gen, Sk>, Term<Ty, En, Sym, Fk, Att, Gen, Sk>>> 
		eqs0 = new HashSet<>(J.eqs());

//		Collection<Pair<Gen, Gen>> q = new HashSet<>(); //TODO aql
		
		try (Connection conn = DriverManager.getConnection(toGet)) {
		
			for (Entry<LocStr, String> q : queries.entrySet()) {
				if (!J.schema().ens.contains(new En(q.getKey().str))){
					throw new LocException(q.getKey().loc, "Not an entity: " + q.getKey().str + ", expected one of " + J.schema().ens);
				}
				Statement stmt = conn.createStatement();

				stmt.execute(q.getValue());
				ResultSet rs = stmt.getResultSet();
				ResultSetMetaData rsmd = rs.getMetaData();
				int columnsNumber = rsmd.getColumnCount();
				if (columnsNumber != 2) {
					stmt.close();
					rs.close();
					throw new LocException(q.getKey().loc, "Expected 2 columns but received " + columnsNumber);
				}
				while (rs.next()) {
					//input instance need not be jdbc, so dont call toGen from import here
					Gen gen1 = new Gen(rs.getObject(1).toString()); //(Gen) ;
					Gen gen2 = new Gen(rs.getObject(2).toString()); //(Gen) rs.getObject(2).toString();
					if (gen1 == null || gen2 == null) {
						stmt.close();
						rs.close();
						throw new LocException(q.getKey().loc, "Encountered a NULL generator");
					} else if (!J.gens().containsKey(gen1)) {
						throw new LocException(q.getKey().loc, "Cannot import record linkage: " + gen1 + " is not a generator in the input instance");
					} else if (!J.gens().containsKey(gen2)) {
						throw new LocException(q.getKey().loc, "Cannot import record linkage: " + gen2 + " is not a generator in the input instance");
					}
					Term<Ty, En, Sym, Fk, Att, Gen, Sk> l = Term.Gen(gen1);
					Term<Ty, En, Sym, Fk, Att, Gen, Sk> r = Term.Gen(gen2);
					eqs0.add(new Pair<>(l, r));
					col.eqs.add(new Eq<>(new Ctx<>(), l, r));	
				}
				stmt.close();
				rs.close();
			}
	//	} catch (SQLException exn) {
	//		exn.printStackTrace();
	//		throw new RuntimeException(/*"JDBC exception: " + */ exn /*.getMessage() */);
		} catch (Throwable thr) {
//			thr.printStackTrace();
			throw new RuntimeException(thr);
		}
		
		InitialAlgebra<Ty, En, Sym, Fk, Att, Gen, Sk, ID> initial0 = new InitialAlgebra<>(strat, J.schema(), col, new It(), Object::toString, Object::toString);			 
			
		return new LiteralInstance<>(J.schema(), col.gens.map, col.sks.map, eqs0, initial0.dp(), initial0, (Boolean) strat.getOrDefault(AqlOption.require_consistency), (Boolean) strat.getOrDefault(AqlOption.allow_java_eqs_unsafe)); 
	}

	
}