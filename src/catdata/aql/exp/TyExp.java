package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import catdata.Pair;
import catdata.Program;
import catdata.Util;
import catdata.aql.Kind;
import catdata.aql.TypeSide;

public abstract class TyExp<Ty, Sym> extends Exp<TypeSide<Ty, Sym>> {
	
	public TyExp<Ty, Sym> resolve(@SuppressWarnings("unused") Program<Exp<?>> prog) {
		return this;
	}
	
	@Override
	public Kind kind() {
		return Kind.TYPESIDE;
	}
	
 
////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class TyExpSch<Ty, Sym> extends TyExp<Ty, Sym> {
		
		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
		}
		
		public final SchExp<Ty, ?, Sym, ?, ?> schema;

		public TyExpSch(SchExp<Ty, ?, Sym, ?, ?> schema) {
			this.schema = schema;
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + (schema.hashCode());
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
            return schema.equals(other.schema);
        }

		@Override
		public String toString() {
			return "TyExpSch [schema=" + schema + "]";
		}

		@Override
		public TypeSide<Ty, Sym> eval(AqlEnv env) {
			return schema.eval(env).typeSide;
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return schema.deps();
		}

			
		
	}
	 
////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class TyExpEmpty extends TyExp<Void,Void> {
		
		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Collections.emptyList();
		}
		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
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
			return (o != null && o instanceof TyExpEmpty);
		}
		
	}
	
	//////////////////////////////////////////////////////////
	

	
////////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class TyExpLit<Ty, Sym> extends TyExp<Ty, Sym> {
		public final TypeSide<Ty, Sym> typeSide;
		
		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
		}
		
		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Collections.emptyList();
		}
		
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
			int prime = 31;
			int result = 1;
			result = prime * result + (typeSide.hashCode());
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
            return typeSide.equals(other.typeSide);
        }

		@Override
		public String toString() {
			return "TyExpLit [typeSide=" + typeSide + "]";
		} 
		
		
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static final class TyExpVar<X,Y> extends TyExp<X, Y> {
		public final String var;
		
		@Override
		public TyExp<X, Y> resolve(Program<Exp<?>> prog) {
			if (!prog.exps.containsKey(var)) {
				throw new RuntimeException("Unbound typeside variable: " + var);
			}
			Exp<?> x = prog.exps.get(var);
			if (!(x instanceof TyExp)) {
				throw new RuntimeException("Variable " + var + " is bound to something that is not a typeside, namely\n\n" + x);
			}
			@SuppressWarnings("unchecked")
			TyExp<X,Y> texp = (TyExp<X,Y>) x;
			return texp.resolve(prog);
		}
		
		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
		}
		
		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.singList(new Pair<>(var, Kind.TYPESIDE));
		}
		
		public TyExpVar(String var) {
			if (var == null) {
				throw new RuntimeException("Attempt to create TyExpVar will null var");
			}
			this.var = var;
		}

		@SuppressWarnings("unchecked")
		@Override
		public TypeSide<X, Y> eval(AqlEnv env) {
			return env.defs.tys.get(var);
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + (var.hashCode());
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
            return var.equals(other.var);
        }

		@Override
		public String toString() {
			return var;
		}
		
	}
	
	
	
}