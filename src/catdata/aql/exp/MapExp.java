package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import catdata.Pair;
import catdata.Util;
import catdata.aql.Kind;
import catdata.aql.Mapping;
import catdata.aql.exp.SchExp.SchExpLit;

//TODO aql move back to presentation / tables distinction?
public abstract class MapExp<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> extends Exp<Mapping<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2>> {
	
	@Override
	public Kind kind() {
		return Kind.MAPPING;
	}
	
	public abstract Pair<SchExp<Ty,En1,Sym,Fk1,Att1>, SchExp<Ty,En2,Sym,Fk2,Att2>> type(AqlTyping G);
	
	
	//////////////////////////////////////////////////////////////////////
	
	public static final class MapExpComp<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2,En3,Fk3,Att3> 
	extends MapExp<Ty,En1,Sym,Fk1,Att1,En3,Fk3,Att3> {
		
		public final MapExp<Ty,En1,Sym,Fk1,Att1,En2,Fk2,Att2> m1;
		public final MapExp<Ty,En2,Sym,Fk2,Att2,En3,Fk3,Att3> m2;
		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
		}
		public MapExpComp(MapExp<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> m1, MapExp<Ty, En2, Sym, Fk2, Att2, En3, Fk3, Att3> m2) {
			this.m1 = m1;
			this.m2 = m2;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((m1 == null) ? 0 : m1.hashCode());
			result = prime * result + ((m2 == null) ? 0 : m2.hashCode());
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
			MapExpComp<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> other = (MapExpComp<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) obj;
			if (m1 == null) {
				if (other.m1 != null)
					return false;
			} else if (!m1.equals(other.m1))
				return false;
			if (m2 == null) {
				if (other.m2 != null)
					return false;
			} else if (!m2.equals(other.m2))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "(" + m1 + " ; " + m2 + ")";
		}

		@Override
		public Pair<SchExp<Ty, En1, Sym, Fk1, Att1>, SchExp<Ty, En3, Sym, Fk3, Att3>> type(AqlTyping G) {
			//TODO aql schema equality
			if (!G.eq(m1.type(G).second, m2.type(G).first)) {
				throw new RuntimeException("Cod of first arg, " + m1.type(G).second + " is not equal to dom of second arg, " + m2.type(G).first + " in " + this);
			}
			return new Pair<>(m1.type(G).first, m2.type(G).second);
		}

		@Override
		public Mapping<Ty, En1, Sym, Fk1, Att1, En3, Fk3, Att3> eval(AqlEnv env) {
			return Mapping.compose(m1.eval(env), m2.eval(env));
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.union(m1.deps(), m2.deps());
		}
		
	}
	
	//////////////////////////////////////////////////////////////////////
	
	
	
	
	/////////////////////////////////////////////////////////////////////

	public static final class MapExpId<Ty,En,Sym,Fk,Att> extends MapExp<Ty,En,Sym,Fk,Att,En,Fk,Att> {
		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
		}		
		@Override
		public Collection<Pair<String, Kind>> deps() { 
			return sch.deps();
		}
				
		public final SchExp<Ty,En,Sym,Fk,Att> sch;

		public MapExpId(SchExp<Ty, En, Sym, Fk, Att> sch) {
			if (sch == null) {
				throw new RuntimeException("Attempt to create MapExpId with null schema");
			}
			this.sch = sch;
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + (sch.hashCode());
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
			MapExpId<?,?,?,?,?> other = (MapExpId<?,?,?,?,?>) obj;
            return sch.equals(other.sch);
        }

		@Override
		public String toString() {
			return "identity " + sch;
		}

		@Override
		public Mapping<Ty, En, Sym, Fk, Att, En, Fk, Att> eval(AqlEnv env) {
			return Mapping.id(sch.eval(env));
		}

		@Override
		public Pair<SchExp<Ty, En, Sym, Fk, Att>, SchExp<Ty, En, Sym, Fk, Att>> type(AqlTyping G) {
			return new Pair<>(sch, sch);
		}

		
	}
	
//////////////////////////////////////////////////////////////////
	
	public static final class MapExpVar extends MapExp<Object, Object, Object, Object, Object, Object, Object, Object> {
		public final String var;
		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
		}
		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.singList(new Pair<>(var, Kind.MAPPING));
		}
	
		public MapExpVar(String var) {
			Util.assertNotNull(var);
			this.var = var;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Mapping<Object, Object, Object, Object, Object, Object, Object, Object> eval(AqlEnv env) {
			return env.defs.maps.get(var);
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
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
			MapExpVar other = (MapExpVar) obj;
			if (var == null) {
				if (other.var != null)
					return false;
			} else if (!var.equals(other.var))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return var;
		}
		@SuppressWarnings("unchecked")
		@Override
		public Pair<SchExp<Object, Object, Object, Object, Object>, SchExp<Object, Object, Object, Object, Object>> type(AqlTyping G) {		
			if (!G.defs.maps.containsKey(var)) {
				throw new RuntimeException("Not a mapping: " + var);
			}
			return (Pair<SchExp<Object, Object, Object, Object, Object>, SchExp<Object, Object, Object, Object, Object>>) ((Object)G.defs.maps.get(var));
		}

		
	}

/////////////////////////////////////////////////////////////////////
	
	public static final class MapExpLit<Ty,En1,Sym1,Fk1,Att1,En2,Fk2,Att2> extends MapExp<Ty,En1,Sym1,Fk1,Att1,En2,Fk2,Att2> {
		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
		}
		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Collections.emptyList();
		}
		
		public final Mapping<Ty,En1,Sym1,Fk1,Att1,En2,Fk2,Att2> map;
		
		public MapExpLit(Mapping<Ty,En1,Sym1,Fk1,Att1,En2,Fk2,Att2> map) {
			Util.assertNotNull(map);
			this.map = map;
		}

		@Override
		public Mapping<Ty,En1,Sym1,Fk1,Att1,En2,Fk2,Att2> eval(AqlEnv env) {
			return map;
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + (map.hashCode());
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
			MapExpLit<?,?,?,?,?,?,?,?> other = (MapExpLit<?,?,?,?,?,?,?,?>) obj;
            return map.equals(other.map);
        }

		@Override
		public String toString() {
			return "MapExpLit [map=" + map + "]";
		}

		@Override
		public Pair<SchExp<Ty, En1, Sym1, Fk1, Att1>, SchExp<Ty, En2, Sym1, Fk2, Att2>> type(AqlTyping G) {
			return new Pair<>(new SchExpLit<>(map.src), new SchExpLit<>(map.dst));
		}


		
	}
}