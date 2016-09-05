package catdata.aql;

public abstract class MapExp<Ty,En1,Sym1,Fk1,Att1,En2,Sym2,Fk2,Att2> extends Exp<Mapping<Ty,En1,Sym1,Fk1,Att1,En2,Sym2,Fk2,Att2>> {
	
	public Kind kind() {
		return Kind.MAPPING;
	}
	
/////////////////////////////////////////////////////////////////////
	
	
	
/////////////////////////////////////////////////////////////////////

	public static final class MapExpId<Ty,En,Sym,Fk,Att> extends MapExp<Ty,En,Sym,Fk,Att,En,Sym,Fk,Att> {
		
		public String meta() {
			return " : " + sch + " -> " + sch;
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
			final int prime = 31;
			int result = 1;
			result = prime * result + ((sch == null) ? 0 : sch.hashCode());
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
			if (sch == null) {
				if (other.sch != null)
					return false;
			} else if (!sch.equals(other.sch))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "id " + sch;
		}

		@Override
		public Mapping<Ty, En, Sym, Fk, Att, En, Sym, Fk, Att> eval(Env env) {
			return Mapping.id(sch.eval(env));
		}
		
	}
	
//////////////////////////////////////////////////////////////////
	
	public static final class MapExpVar extends MapExp<Object, Object, Object, Object, Object, Object, Object, Object, Object> {
		public final String var;
		
		public String meta() {
			return "";
		}

		
		public MapExpVar(String var) {
			if (var == null) {
				throw new RuntimeException("Attempt to create MapExpVar with null var");
			}
			this.var = var;
		}

		@Override
		public Mapping<Object, Object, Object, Object, Object, Object, Object, Object, Object> eval(Env env) {
			return env.getMapping(var);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
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
	}

/////////////////////////////////////////////////////////////////////
	
	public static final class MapExpLit<Ty,En1,Sym1,Fk1,Att1,En2,Sym2,Fk2,Att2> extends MapExp<Ty,En1,Sym1,Fk1,Att1,En2,Sym2,Fk2,Att2> {

		public final Mapping<Ty,En1,Sym1,Fk1,Att1,En2,Sym2,Fk2,Att2> map;
		
		public MapExpLit(Mapping<Ty,En1,Sym1,Fk1,Att1,En2,Sym2,Fk2,Att2> map) {
			if (map == null) {
				throw new RuntimeException("Attempt to create MapExpLit with null schema");
			}
			this.map = map;
		}

		@Override
		public Mapping<Ty,En1,Sym1,Fk1,Att1,En2,Sym2,Fk2,Att2> eval(Env env) {
			return map;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((map == null) ? 0 : map.hashCode());
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
			MapExpLit<?,?,?,?,?,?,?,?,?> other = (MapExpLit<?,?,?,?,?,?,?,?,?>) obj;
			if (map == null) {
				if (other.map != null)
					return false;
			} else if (!map.equals(other.map))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "MapExpLit [map=" + map + "]";
		}

		@Override
		public String meta() {
			return "";
		}
		
		
	}
}