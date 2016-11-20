package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import catdata.Pair;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Pragma;
import catdata.aql.fdm.JdbcPragma;
import catdata.aql.fdm.ToCsvPragmaInstance;
import catdata.aql.fdm.ToCsvPragmaTransform;

public abstract class PragmaExp extends Exp<Pragma> {
	
	public Kind kind() {
		return Kind.PRAGMA;
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static final class PragmaExpSql extends PragmaExp {
		private final List<String> sqls;
		
		private final String jdbcString;
	
		private final String clazz;
		
		private final Map<String, String> options; //TODO aql autoreload
		
		public PragmaExpSql(String clazz, String jdbcString, List<String> sqls, List<Pair<String, String>> options) {
			this.clazz = clazz;
			this.jdbcString = jdbcString;
			this.options = Util.toMapSafely(options);
			this.sqls = new LinkedList<>(sqls);
			try {
				Class.forName(clazz);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
			result = prime * result + ((jdbcString == null) ? 0 : jdbcString.hashCode());
			result = prime * result + ((options == null) ? 0 : options.hashCode());
			result = prime * result + ((sqls == null) ? 0 : sqls.hashCode());
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
			PragmaExpSql other = (PragmaExpSql) obj;
			
			AqlOptions op = new AqlOptions(options, null);
			Boolean reload = (Boolean) op.getOrDefault(AqlOption.always_reload);
			if (reload) {
				return false;
			}
			
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
			if (sqls == null) {
				if (other.sqls != null)
					return false;
			} else if (!sqls.equals(other.sqls))
				return false;
			return true;
		}

		@Override
		public Pragma eval(AqlEnv env) {
			return new JdbcPragma(clazz, jdbcString, sqls, options);
		}

		@Override
		public String toString() {
			return "sql " + Util.sep(sqls, "\n");
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Collections.emptyList();
		}	
		
		
		
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static final class PragmaExpToCsvInst<Ty,En,Sym,Att,Fk,Gen,Sk,X,Y> extends PragmaExp {
		
		public final String file;
				
		public final Map<String, String> options;

		public final InstExp<Ty,En,Sym,Att,Fk,Gen,Sk,X,Y> inst;
		
		public PragmaExpToCsvInst(InstExp<Ty, En, Sym, Att, Fk, Gen, Sk, X, Y> inst, String file, List<Pair<String, String>> options) {
			Util.assertNotNull(file, options, inst);
			this.file = file;
			this.options = Util.toMapSafely(options);
			this.inst = inst;
		}

		@Override
		public Pragma eval(AqlEnv env) {
			AqlOptions op = new AqlOptions(options, null);
			return new ToCsvPragmaInstance<>(inst.eval(env), file, InstExpCsv.getFormat(op), (String)op.getOrDefault(AqlOption.csv_id_column_name));
		}

		@Override
		public String toString() {
			return "export_csv " + inst + " " + file;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((file == null) ? 0 : file.hashCode());
			result = prime * result + ((inst == null) ? 0 : inst.hashCode());
			result = prime * result + ((options == null) ? 0 : options.hashCode());
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
			PragmaExpToCsvInst<?, ?, ?, ?, ?, ?, ?, ?, ?> other = (PragmaExpToCsvInst<?, ?, ?, ?, ?, ?, ?, ?, ?>) obj;
			if (file == null) {
				if (other.file != null)
					return false;
			} else if (!file.equals(other.file))
				return false;
			if (inst == null) {
				if (other.inst != null)
					return false;
			} else if (!inst.equals(other.inst))
				return false;
			if (options == null) {
				if (other.options != null)
					return false;
			} else if (!options.equals(other.options))
				return false;
			return true;
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return inst.deps();
		}		
		
	}

	///////////////////////////////////////////////////////////////////
	
	public static final class PragmaExpToCsvTrans<Ty,En,Sym,Att,Fk,Gen1,Sk1,X1,Y1,Gen2,Sk2,X2,Y2> extends PragmaExp {
		
		public final String file;
				
		public final Map<String, String> options;

		public final TransExp<Ty,En,Sym,Att,Fk,Gen1,Sk1,X1,Y1,Gen2,Sk2,X2,Y2> trans;

		public PragmaExpToCsvTrans(TransExp<Ty, En, Sym, Att, Fk, Gen1, Sk1, X1, Y1, Gen2, Sk2, X2, Y2> trans, String file, List<Pair<String, String>> options) {
			this.file = file;
			this.options = Util.toMapSafely(options);
			this.trans = trans;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((file == null) ? 0 : file.hashCode());
			result = prime * result + ((options == null) ? 0 : options.hashCode());
			result = prime * result + ((trans == null) ? 0 : trans.hashCode());
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
			PragmaExpToCsvTrans<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?> other = (PragmaExpToCsvTrans<?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) obj;
			if (file == null) {
				if (other.file != null)
					return false;
			} else if (!file.equals(other.file))
				return false;
			if (options == null) {
				if (other.options != null)
					return false;
			} else if (!options.equals(other.options))
				return false;
			if (trans == null) {
				if (other.trans != null)
					return false;
			} else if (!trans.equals(other.trans))
				return false;
			return true;
		}
		
		@Override
		public Collection<Pair<String, Kind>> deps() {
			return trans.deps();
		}

		@Override
		public String toString() {
			return "export_csv " + trans + " " + file;
		}

		@Override
		public Pragma eval(AqlEnv env) {
			return new ToCsvPragmaTransform<>(trans.eval(env), file, InstExpCsv.getFormat(new AqlOptions(options, null)));
		}
	
		//////////////////////////////////////////////////
		
		public static final class PragmaExpVar extends PragmaExp {
			public final String var;
			
			@Override
			public Collection<Pair<String, Kind>> deps() {
				return Util.singList(new Pair<>(var, Kind.PRAGMA));
			}
			
			public PragmaExpVar(String var) {
				Util.assertNotNull(var);
				this.var = var;
			}

			@Override
			public Pragma eval(AqlEnv env) {
				return env.defs.ps.get(var);
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
				PragmaExpVar other = (PragmaExpVar) obj;
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
}
	
	
	
}