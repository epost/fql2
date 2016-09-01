package catdata.aql;

import java.util.List;

import catdata.Pair;

public abstract class MapExp<Ty,En1,Sym1,Fk1,Att1,En2,Sym2,Fk2,Att2> extends Exp<Mapping<Ty,En1,Sym1,Fk1,Att1,En2,Sym2,Fk2,Att2>> {
	
	public Kind kind() {
		return Kind.MAPPING;
	}
	
/////////////////////////////////////////////////////////////////////
	
	public static final class MapExpRaw extends MapExp<String,String,String,String,String,String,String,String,String> {
		
		public final SchExp<?,?,?,?,?> src, dst;
		
		public final List<String> imports;
		
		public final List<Pair<String, String>> ens;
		public final List<Pair<String, List<String>>> fks;
		public final List<Pair<String, Pair<String, RawTerm>>> atts;
		
		public final List<Pair<String, String>> options;
		
		public MapExpRaw(SchExp<?, ?, ?, ?, ?> src, SchExp<?, ?, ?, ?, ?> dst, List<String> imports, List<Pair<String, String>> ens, List<Pair<String, List<String>>> fks, List<Pair<String, Pair<String, RawTerm>>> atts, List<Pair<String, String>> options) {
			this.src = src;
			this.dst = dst;
			this.imports = imports;
			this.ens = ens;
			this.fks = fks;
			this.atts = atts;
			this.options = options;
		}

		@Override
		public String toString() {
			return "MapExpRaw [src=" + src + ", dst=" + dst + ", imports=" + imports + ", ens=" + ens + ", fks=" + fks + ", atts=" + atts + ", options=" + options + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((atts == null) ? 0 : atts.hashCode());
			result = prime * result + ((dst == null) ? 0 : dst.hashCode());
			result = prime * result + ((ens == null) ? 0 : ens.hashCode());
			result = prime * result + ((fks == null) ? 0 : fks.hashCode());
			result = prime * result + ((imports == null) ? 0 : imports.hashCode());
			result = prime * result + ((options == null) ? 0 : options.hashCode());
			result = prime * result + ((src == null) ? 0 : src.hashCode());
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
			MapExpRaw other = (MapExpRaw) obj;
			if (atts == null) {
				if (other.atts != null)
					return false;
			} else if (!atts.equals(other.atts))
				return false;
			if (dst == null) {
				if (other.dst != null)
					return false;
			} else if (!dst.equals(other.dst))
				return false;
			if (ens == null) {
				if (other.ens != null)
					return false;
			} else if (!ens.equals(other.ens))
				return false;
			if (fks == null) {
				if (other.fks != null)
					return false;
			} else if (!fks.equals(other.fks))
				return false;
			if (imports == null) {
				if (other.imports != null)
					return false;
			} else if (!imports.equals(other.imports))
				return false;
			if (options == null) {
				if (other.options != null)
					return false;
			} else if (!options.equals(other.options))
				return false;
			if (src == null) {
				if (other.src != null)
					return false;
			} else if (!src.equals(other.src))
				return false;
			return true;
		}

		public String meta() {
			return " : " + src + " -> " + dst;
		}

		@Override
		public Mapping<String, String, String, String, String, String, String, String, String> eval(Env env) {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
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
	
	public static final class MapExpVar<Ty,En1,Sym1,Fk1,Att1,En2,Sym2,Fk2,Att2> extends MapExp<Ty,En1,Sym1,Fk1,Att1,En2,Sym2,Fk2,Att2> {
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

		@SuppressWarnings("unchecked")
		@Override
		public Mapping<Ty,En1,Sym1,Fk1,Att1,En2,Sym2,Fk2,Att2> eval(Env env) {
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
			MapExpVar<?,?,?,?,?,?,?,?,?> other = (MapExpVar<?,?,?,?,?,?,?,?,?>) obj;
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