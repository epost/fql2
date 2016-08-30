package catdata.aql;

import java.util.List;

import catdata.Pair;
import catdata.Triple;

public abstract class TyExp<Ty,Sym> extends Exp<TypeSide<Ty,Sym>> {
	
	@Override
	public String meta() {
		return "";
	}
	
	public Kind kind() {
		return Kind.TYPESIDE;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class TyExpSch<Ty,Sym> extends TyExp<Ty,Sym> {
		
		public final SchExp<Ty,?,Sym,?,?> schema;

		public TyExpSch(SchExp<Ty, ?, Sym, ?, ?> schema) {
			if (schema == null) {
				throw new RuntimeException("Attempt to take the typeside of a null schema");
			}
			this.schema = schema;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((schema == null) ? 0 : schema.hashCode());
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
			TyExpSch<?,?> other = (TyExpSch<?,?>) obj;
			if (schema == null) {
				if (other.schema != null)
					return false;
			} else if (!schema.equals(other.schema))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TyExpSch [schema=" + schema + "]";
		}

		@Override
		public TypeSide<Ty, Sym> eval(Env env) {
			return schema.eval(env).typeSide;
		}
		
		
		
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class TyExpEmpty extends TyExp<Void,Void> {

		public TyExpEmpty() { 
		}
		
		@Override
		public TypeSide<Void, Void> eval(Env env) {
			return TypeSide.terminal();
		}
	
		@Override
		public String toString() {
			return "empty";
		}
	
		@Override
		public int hashCode() {
			return 0;
		}
	
		@Override
		public boolean equals(Object o) {
			return (o instanceof TyExpEmpty);
		}
		
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static final class TyExpRaw extends TyExp<String, String> {

		public final List<String> imports;
		public final List<String> types;
		public final List<Pair<String, String>> constants;
		public final List<Triple<String, List<String>, String>> functions;
		public final List<Triple<List<Pair<String, String>>, RawTerm, RawTerm>> eqs;

		public final List<Pair<String, String>> java_tys_string;
		public final List<Pair<String, String>> java_parser_string;
		public final List<Pair<String, String>> java_fns_string;
		
		public final List<Pair<String, String>> options;
		
		public TyExpRaw(List<String> imports, List<String> types, List<Pair<String, String>> constants, List<Triple<String, List<String>, String>> functions, List<Triple<List<Pair<String, String>>, RawTerm, RawTerm>> eqs, List<Pair<String, String>> java_tys_string, List<Pair<String, String>> java_parser_string, List<Pair<String, String>> java_fns_string, List<Pair<String, String>> options) {
			this.imports = imports;
			this.types = types;
			this.constants = constants;
			this.functions = functions;
			this.eqs = eqs;
			this.java_tys_string = java_tys_string;
			this.java_parser_string = java_parser_string;
			this.java_fns_string = java_fns_string;
			this.options = options;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((constants == null) ? 0 : constants.hashCode());
			result = prime * result + ((eqs == null) ? 0 : eqs.hashCode());
			result = prime * result + ((functions == null) ? 0 : functions.hashCode());
			result = prime * result + ((imports == null) ? 0 : imports.hashCode());
			result = prime * result + ((java_fns_string == null) ? 0 : java_fns_string.hashCode());
			result = prime * result + ((java_parser_string == null) ? 0 : java_parser_string.hashCode());
			result = prime * result + ((java_tys_string == null) ? 0 : java_tys_string.hashCode());
			result = prime * result + ((options == null) ? 0 : options.hashCode());
			result = prime * result + ((types == null) ? 0 : types.hashCode());
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
			TyExpRaw other = (TyExpRaw) obj;
			if (constants == null) {
				if (other.constants != null)
					return false;
			} else if (!constants.equals(other.constants))
				return false;
			if (eqs == null) {
				if (other.eqs != null)
					return false;
			} else if (!eqs.equals(other.eqs))
				return false;
			if (functions == null) {
				if (other.functions != null)
					return false;
			} else if (!functions.equals(other.functions))
				return false;
			if (imports == null) {
				if (other.imports != null)
					return false;
			} else if (!imports.equals(other.imports))
				return false;
			if (java_fns_string == null) {
				if (other.java_fns_string != null)
					return false;
			} else if (!java_fns_string.equals(other.java_fns_string))
				return false;
			if (java_parser_string == null) {
				if (other.java_parser_string != null)
					return false;
			} else if (!java_parser_string.equals(other.java_parser_string))
				return false;
			if (java_tys_string == null) {
				if (other.java_tys_string != null)
					return false;
			} else if (!java_tys_string.equals(other.java_tys_string))
				return false;
			if (options == null) {
				if (other.options != null)
					return false;
			} else if (!options.equals(other.options))
				return false;
			if (types == null) {
				if (other.types != null)
					return false;
			} else if (!types.equals(other.types))
				return false;
			return true;
		}
		@Override
		public String toString() {
			return "RawTypeSide [imports=" + imports + ", types=" + types + ", constants=" + constants + ", functions=" + functions + ", eqs=" + eqs + ", java_tys_string=" + java_tys_string + ", java_parser_string=" + java_parser_string + ", java_fns_string=" + java_fns_string + ", options=" + options + "]";
		}
		
		
		
		@Override
		public TypeSide<String, String> eval(Env env) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class TyExpLit<Ty,Sym> extends TyExp<Ty,Sym> {
		public final TypeSide<Ty,Sym> typeSide;
		
		public TyExpLit(TypeSide<Ty,Sym> typeSide) {
			if (typeSide == null) {
				throw new RuntimeException("Attempt to create TyExpLit with null type side");
			}
			this.typeSide = typeSide;
		}

		@Override
		public TypeSide<Ty,Sym> eval(Env env) {
			return typeSide;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((typeSide == null) ? 0 : typeSide.hashCode());
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
			TyExpLit<?,?> other = (TyExpLit<?,?>) obj;
			if (typeSide == null) {
				if (other.typeSide != null)
					return false;
			} else if (!typeSide.equals(other.typeSide))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TyExpLit [typeSide=" + typeSide + "]";
		} 
		
		
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static final class TyExpVar<Ty,Sym> extends TyExp<Ty,Sym> {
		public final String var;
		
		public TyExpVar(String var) {
			if (var == null) {
				throw new RuntimeException("Attempt to create TyExpVar will null var");
			}
			this.var = var;
		}

		@SuppressWarnings("unchecked")
		@Override
		public TypeSide<Ty,Sym> eval(Env env) {
			return (TypeSide<Ty, Sym>) env.getTypeSide(var);
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
			TyExpVar<?,?> other = (TyExpVar<?,?>) obj;
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
	
	public static final class TyExpTerminal extends TyExp<Void,Void> {

		@Override
		public TypeSide<Void,Void> eval(Env env) {
			return TypeSide.terminal();
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public boolean equals(Object o) {
			return (o instanceof TyExpTerminal);
		}

		@Override
		public String toString() {
			return "TyExpTerminal []"; 
		}
		
		
		
	}
	
}