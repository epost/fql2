package catdata.aql;

public abstract class MapExp extends Exp<Mapping> {
	
	public Kind kind() {
		return Kind.MAPPING;
	}
	
	
	public static final class MapExpVar extends MapExp {
		String var;
		
		@Override
		public Mapping eval(Env env) {
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
			return "MapExpVar [var=" + var + "]";
		}
		
		
	}

	public static final class MapExpLit extends MapExp {

		public final Mapping map;
		
		public MapExpLit(Mapping map) {
			if (map == null) {
				throw new RuntimeException("Attempt to create MapExpLit with null schema");
			}
			this.map = map;
		}

		@Override
		public Mapping eval(Env env) {
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
			MapExpLit other = (MapExpLit) obj;
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
		
		
	}
}