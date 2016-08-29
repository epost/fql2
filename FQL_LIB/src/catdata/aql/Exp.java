package catdata.aql;

@SuppressWarnings("rawtypes")
public abstract class Exp {
	
	//TODO: dom, cod, id, comp, +, 0
	
	public abstract Kind kind();
	
	public abstract Object evaluate(Env env);
	
	public static abstract class TyExp extends Exp {
		
		public abstract TypeSide eval(Env env);

		public Kind kind() {
			return Kind.TYPESIDE;
		}
		
		@Override 
		public Object evaluate(Env env) {
			return eval(env);
		}

		//TODO: add imports to separate surface syntax class

		
		public static class TyExpLit extends TyExp {
			public final TypeSide typeSide;
			
			public TyExpLit(TypeSide typeSide) {
				if (typeSide == null) {
					throw new RuntimeException("Attempt to create TyExpLit with null type side");
				}
				this.typeSide = typeSide;
			}

			@Override
			public TypeSide eval(Env env) {
				return typeSide;
			}
			
		}
		
		public static class TyExpVar extends TyExp {
			String var;
			
			@Override
			public TypeSide eval(Env env) {
				return env.getTypeSide(var);
			}
			
		}
		
		public static class TyExpTerminal extends TyExp {

			@Override
			public TypeSide eval(Env env) {
				return TypeSide.terminal();
			}
			
		}
		
	}

	public static abstract class SchExp extends Exp {
		
		public abstract Schema eval(Env env);
		
		public Kind kind() {
			return Kind.SCHEMA;
		}

		@Override 
		public Object evaluate(Env env) {
			return eval(env);
		}

		public static class SchExpVar extends SchExp {
			
			String var;
			
			@Override
			public Schema eval(Env env) {
				return env.getSchema(var);
			}
		}
	
		//TODO: add imports

		public static class SchExpLit extends SchExp {
	
			public final Schema schema;
			
			public SchExpLit(Schema schema) {
				if (schema == null) {
					throw new RuntimeException("Attempt to create SchExpLit with null schema");
				}
				this.schema = schema;
			}

			@Override
			public Schema eval(Env env) {
				return schema;
			}
		}
		
	}
	
	public static abstract class InstExp extends Exp{
		
		public abstract Instance eval(Env env);
		
		public Kind kind() {
			return Kind.INSTANCE;
		}

		@Override 
		public Object evaluate(Env env) {
			return eval(env);
		}

		public static class InstExpVar extends InstExp {	
			String var;
			
			@Override
			public Instance eval(Env env) {
				return env.getInstance(var);
			}
		}
	
		//TODO: add imports

		public static class InstExpLit extends InstExp {
	
			public final Instance inst;
			
			public InstExpLit(Instance inst) {
				if (inst == null) {
					throw new RuntimeException("Attempt to create InstExpLit with null schema");
				}
				this.inst = inst;
			}

			@Override
			public Instance eval(Env env) {
				return inst;
			}
		}
		
		//public class SigmaExp extends InstExp {
			
		//}

	}
	
	//TODO: add imports

	public static abstract class TransExp extends Exp {
		
		public abstract Transform eval(Env env);
		
		public Kind kind() {
			return Kind.TRANSFORM;
		}
		
		@Override 
		public Object evaluate(Env env) {
			return eval(env);
		}

		public static class TransExpVar extends TransExp {
			String var;
			
			@Override
			public Transform eval(Env env) {
				return env.getTransform(var);
			}
		}
		
		public static class TransExpLit extends TransExp {
	
			public final Transform trans;
			
			public TransExpLit(Transform trans) {
				if (trans == null) {
					throw new RuntimeException("Attempt to create TransExpLit with null schema");
				}
				this.trans = trans;
			}

			@Override
			public Transform eval(Env env) {
				return trans;
			}
		}
	}
	
	public static abstract class MapExp extends Exp {
		
		public Kind kind() {
			return Kind.MAPPING;
		}
		
		public abstract Mapping eval(Env env);

		@Override 
		public Object evaluate(Env env) {
			return eval(env);
		}

		public static class MapExpVar extends MapExp {
			String var;
			
			@Override
			public Mapping eval(Env env) {
				return env.getMapping(var);
			}
		}
	
		//TODO: add imports

		public static class MapExpLit extends MapExp {
	
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
		}
	}
	
	public abstract static class QueryExp extends Exp {
		
		public Kind kind() {
			return Kind.QUERY;
		}
		
		public abstract Query eval(Env env);

		@Override 
		public Object evaluate(Env env) {
			return eval(env);
		}

		public static class QueryExpVar extends QueryExp {
			String var;
			
			@Override
			public Query eval(Env env) {
				return env.getQuery(var);
			}
		}
	
		//TODO: add imports

		public static class QueryExpLit extends QueryExp {
	
			public final Query q;
			
			public QueryExpLit(Query q) {
				if (q == null) {
					throw new RuntimeException("Attempt to create MapExpLit with null schema");
				}
				this.q = q;
			}

			@Override
			public Query eval(Env env) {
				throw new RuntimeException(); //TODO
			}
		}

	} 
	
	public abstract static class PragmaExp extends Exp {
		
		public Kind kind() {
			return Kind.PRAGMA;
		}
		
		public abstract Pragma eval(Env env);

		@Override 
		public Object evaluate(Env env) {
			return eval(env);
		}
		
	}
	
	
	
	
}
