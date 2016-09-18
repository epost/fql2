package catdata.aql;

public abstract class TyExp<Ty, Sym> extends Exp<TypeSide<Ty, Sym>> {
	
	@Override
	public String meta() {
		return "";
	}
	
	public Kind kind() {
		return Kind.TYPESIDE;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class TyExpSch<Ty, Sym> extends TyExp<Ty, Sym> {
		
		public final SchExp<Ty, ?, Sym, ?, ?> schema;

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
		public TypeSide<Ty, Sym> eval(AqlEnv env) {
			return schema.eval(env).typeSide;
		}
		
		
		
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class TyExpEmpty extends TyExp<Void,Void> {

		public TyExpEmpty() { 
		}
		
		@Override
		public TypeSide<Void,Void> eval(AqlEnv env) {
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

	public static final class TyExpLit<Ty, Sym> extends TyExp<Ty, Sym> {
		public final TypeSide<Ty, Sym> typeSide;
		
		public TyExpLit(TypeSide<Ty, Sym> typeSide) {
			if (typeSide == null) {
				throw new RuntimeException("Attempt to create TyExpLit with null type side");
			}
			this.typeSide = typeSide;
		}

		@Override
		public TypeSide<Ty, Sym> eval(AqlEnv env) {
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
	
	public static final class TyExpVar extends TyExp<Object, Object> {
		public final String var;
		
		public TyExpVar(String var) {
			if (var == null) {
				throw new RuntimeException("Attempt to create TyExpVar will null var");
			}
			this.var = var;
		}

		@Override
		public TypeSide <Object, Object> eval(AqlEnv env) {
			return env.getTypeSide(var);
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
			TyExpVar other = (TyExpVar) obj;
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
		public TypeSide<Void,Void> eval(AqlEnv env) {
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