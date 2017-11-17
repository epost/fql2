package catdata.aql.exp;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import catdata.Pair;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.ED;
import catdata.aql.ED.WHICH;
import catdata.aql.Instance;
import catdata.aql.Kind;
import catdata.aql.Pragma;
import catdata.aql.exp.SchExpRaw.Att;
import catdata.aql.exp.SchExpRaw.En;
import catdata.aql.exp.SchExpRaw.Fk;
import catdata.aql.exp.TyExpRaw.Sym;
import catdata.aql.exp.TyExpRaw.Ty;
import catdata.aql.fdm.EvalAlgebra.Row;
import catdata.aql.fdm.JdbcPragma;
import catdata.aql.fdm.JsPragma;
import catdata.aql.fdm.ProcPragma;
import catdata.aql.fdm.ToCsvPragmaInstance;
import catdata.aql.fdm.ToCsvPragmaTransform;
import catdata.aql.fdm.ToJdbcPragmaInstance;
import catdata.aql.fdm.ToJdbcPragmaQuery;
import catdata.aql.fdm.ToJdbcPragmaTransform;
import catdata.graph.DMG;
import catdata.graph.Matcher;
import catdata.graph.NaiveMatcher;
import catdata.graph.SimilarityFloodingMatcher;

public abstract class PragmaExp extends Exp<Pragma> {

	@Override
	public Kind kind() {
		return Kind.PRAGMA;
	}
	
		
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static final class PragmaExpConsistent<Ty,En,Sym,Fk,Att,Gen,Sk,X,Y> extends PragmaExp {
		public final InstExp<Ty,En,Sym,Fk,Att,Gen,Sk,X,Y> I;
		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
		}
		public PragmaExpConsistent(InstExp<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> i) {
			I = i;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((I == null) ? 0 : I.hashCode());
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
			PragmaExpConsistent<?, ?, ?, ?, ?, ?, ?, ?, ?> other = (PragmaExpConsistent<?, ?, ?, ?, ?, ?, ?, ?, ?>) obj;
			if (I == null) {
				if (other.I != null)
					return false;
			} else if (!I.equals(other.I))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "assert_consistent " + I;
		}

		
		@Override
		public Pragma eval(AqlEnv env) {
			return new Pragma() {

				@Override
				public void execute() {
					Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> J = I.eval(env);
					if (!J.algebra().hasFreeTypeAlgebra()) {
						throw new RuntimeException("Not necessarily consistent: type algebra is\n\n" + J.algebra().talg());
					}
				}

				@Override
				public String toString() {
					return "Consistent";
				}
				
			};
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return I.deps();
		}
		
		
		
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class PragmaExpCheck<Ty,En,Sym,Fk,Att,Gen,Sk,X,Y> extends PragmaExp {
		public InstExp<Ty,En,Sym,Fk,Att,Gen,Sk,X,Y> I;
		public EdsExp<Ty,En,Sym,Fk,Att> C;
		
		public PragmaExpCheck(InstExp<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> i, EdsExp<Ty, En, Sym, Fk, Att> c) {
			I = i;
			C = c;
		}
		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((C == null) ? 0 : C.hashCode());
			result = prime * result + ((I == null) ? 0 : I.hashCode());
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
			PragmaExpCheck<?, ?, ?, ?, ?, ?, ?, ?, ?> other = (PragmaExpCheck<?, ?, ?, ?, ?, ?, ?, ?, ?>) obj;
			if (C == null) {
				if (other.C != null)
					return false;
			} else if (!C.equals(other.C))
				return false;
			if (I == null) {
				if (other.I != null)
					return false;
			} else if (!I.equals(other.I))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "check " + C + " " + I;
		}

		

		@Override
		public Pragma eval(AqlEnv env) {
			return new Pragma() {

				@Override
				public void execute() {
					Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> J = I.eval(env);
					Collection<Pair<ED<Ty, En, Sym, Fk, Att>, Row<WHICH, X>>> t 
					= C.eval(env).triggers(J, env.defaults);
					if (!t.isEmpty()) {
						throw new RuntimeException("Not satisfied.\n\n" + printTriggers(t, J)); 
					}
				}

				private String printTriggers(Collection<Pair<ED<Ty, En, Sym, Fk, Att>, Row<WHICH, X>>> t, Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> J) {
					Map<ED<Ty, En, Sym, Fk, Att>, List<Row<WHICH, X>>> m = new HashMap<>();
					for (Pair<ED<Ty, En, Sym, Fk, Att>, Row<WHICH, X>> p : t) {
						if (!m.containsKey(p.first)) {
							m.put(p.first, new LinkedList<>());
						}
						List<Row<WHICH, X>> l = m.get(p.first);
						l.add(p.second);
					}
					String ret = "";
					for (ED<Ty, En, Sym, Fk, Att> ed : m.keySet()) {
						ret += "======================\n";
						ret += "On constraint\n\n" + ed.toString() + "\n\nthe failing triggers are:\n\n";
						ret += Util.sep(m.get(ed).iterator(), "\n", r->Util.sep(r.map(z->J.algebra().printX(z)).asMap(),"->",", "));
						ret += "\n";
					}
					return ret;
				}

				@Override
				public String toString() {
					return "Satisfies";
				}
				
			};
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.union(C.deps(), I.deps());
		}
		
		
		
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class PragmaExpLoadJars extends PragmaExp {

		public final List<String> files;@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
		}
		public PragmaExpLoadJars(List<String> files) {
			this.files = files;
			//this isn't side effect free, but it should be benign, or at least as benign as having direct access to the classpath from the command line
			

		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + ((files == null) ? 0 : files.hashCode());
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
			PragmaExpLoadJars other = (PragmaExpLoadJars) obj;
			if (files == null) {
				if (other.files != null)
					return false;
			} else if (!files.equals(other.files))
				return false;
			return true;
		}


		@Override
		public Pragma eval(AqlEnv env) {
			return new Pragma() {
				@Override
				public String toString() {
					//URL[] urls = ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs();
					//List<URL> urls0 = Arrays.asList(urls);
					return ""; //"Classpath:\n\n" + Util.sep(urls0, "\n");
				}
				@Override
				public void execute() {
					
					try {
						Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
						method.setAccessible(true);
						for (String f : files) {
							File file = new File(f);
							if (!file.exists()) {
								throw new RuntimeException("Not a file: " + f);
							}
							method.invoke(ClassLoader.getSystemClassLoader(), file.toURI().toURL());
						}
					} catch (IllegalAccessException | NoSuchMethodException | RuntimeException | InvocationTargetException | MalformedURLException thr) {
						thr.printStackTrace();
						throw new RuntimeException(thr);
					}
					
				}
			};
		}

		@Override
		public String toString() {
			return "load_jars {\n\t" + Util.sep(files.stream().map(Util::quote).collect(Collectors.toList()), "\n\t") + "\n}";
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Collections.emptySet();
		}

	}

	///////////////////////////////////////////////////////////////////////////////////////////////

	public static final class PragmaExpMatch<N1, E1, N2, E2> extends PragmaExp {
		public final String which;
		public final Map<String, String> options;

		public final GraphExp<N1, E1> src;
		public final GraphExp<N2, E2> dst;

		@Override
		public Map<String, String> options() {
			return options;
		}

		public PragmaExpMatch(String which, GraphExp<N1, E1> src, GraphExp<N2, E2> dst, List<Pair<String, String>> options) {
			this.which = which;
			this.options = Util.toMapSafely(options);
			this.src = src;
			this.dst = dst;
		}

		@Override
		public Pragma eval(AqlEnv env) {
			DMG<N1, E1> src0 = src.eval(env).dmg;
			DMG<N2, E2> dst0 = dst.eval(env).dmg;

			return new Pragma() {

				private String s;

				@Override
				public void execute() {
					toString();
				}

				@Override
				public String toString() {
					if (s != null) {
						return s;
					}
					// TODO aql eventually, this should not catch the exception
					Matcher<N1, E1, N2, E2, ?> ret0;
					try {
						switch (which) {
							case "naive":
								ret0 = new NaiveMatcher<>(src0, dst0, options);
								break;
							case "sf":
								ret0 = new SimilarityFloodingMatcher<>(src0, dst0, options);
								break;
							default:
								throw new RuntimeException("Please use naive or sf for which match desired, not " + which);
						}
						s = ret0.bestMatch.toString();
					} catch (Exception e) {
						//e.printStackTrace();
						s = e.getMessage();
						throw e;
					}
					return s;
				}
			};
		}

		@Override
		public String toString() {
			return "match " + which + " : " + src + " -> " + dst;
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Util.union(src.deps(), dst.deps());
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + ((dst == null) ? 0 : dst.hashCode());
			result = prime * result + ((options == null) ? 0 : options.hashCode());
			result = prime * result + ((src == null) ? 0 : src.hashCode());
			result = prime * result + ((which == null) ? 0 : which.hashCode());
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
			PragmaExpMatch<?, ?, ?, ?> other = (PragmaExpMatch<?, ?, ?, ?>) obj;
			if (dst == null) {
				if (other.dst != null)
					return false;
			} else if (!dst.equals(other.dst))
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
			if (which == null) {
				if (other.which != null)
					return false;
			} else if (!which.equals(other.which))
				return false;
			return true;
		}

	}

	/////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class PragmaExpSql extends PragmaExp {
		private final List<String> sqls;

		private final String jdbcString;

		private final String clazz;

		private final Map<String, String> options;

		@Override
		public Map<String, String> options() {
			return options;
		}

		public PragmaExpSql(String clazz, String jdbcString, List<String> sqls, List<Pair<String, String>> options) {
			this.clazz = clazz;
			this.jdbcString = jdbcString;
			this.options = Util.toMapSafely(options);
			this.sqls = new LinkedList<>(sqls);
			Util.checkClass(clazz);			
		}

		@Override
		public int hashCode() {
			int prime = 31;
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
			String toGet = jdbcString;
			String driver = clazz;
			AqlOptions op = new AqlOptions(options, null, env.defaults);
			if (clazz.trim().isEmpty()) {
				driver = (String) op.getOrDefault(AqlOption.jdbc_default_class);
				Util.checkClass(driver);
			}
			if (jdbcString.trim().isEmpty()) {
				toGet = (String) op.getOrDefault(AqlOption.jdbc_default_string);
			}
			return new JdbcPragma(toGet, sqls);
		}

		//TODO aql add options
		@Override
		public String toString() {
			String s = "";
			if (!options.isEmpty()) {
				s = "\n\toptions" + Util.sep(options, "\n\t\t", " = ");
			}
		
			return "sql " + Util.quote(clazz) + " " + Util.quote(jdbcString) + " {\n" + Util.sep(sqls.stream().map(Util::quote).collect(Collectors.toList()), "\n") + s + "\n}";
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Collections.emptyList();
		}

	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	public static final class PragmaExpToCsvInst<Ty, En, Sym, Att, Fk, Gen, Sk, X, Y> extends PragmaExp {

		public final String file;

		public final Map<String, String> options;

		public final InstExp<Ty, En, Sym, Att, Fk, Gen, Sk, X, Y> inst;

		@Override
		public Map<String, String> options() {
			return options;
		}

		public PragmaExpToCsvInst(InstExp<Ty, En, Sym, Att, Fk, Gen, Sk, X, Y> inst, String file, List<Pair<String, String>> options) {
			Util.assertNotNull(file, options, inst);
			this.file = file;
			this.options = Util.toMapSafely(options);
			this.inst = inst;
		}

		@Override
		public Pragma eval(AqlEnv env) {
			AqlOptions op = new AqlOptions(options, null, env.defaults);
			return new ToCsvPragmaInstance<>(inst.eval(env), file, op);
		}

		@Override
		public String toString() {
			String s = "";
			if (!options.isEmpty()) {
				s = " {\n\toptions" + Util.sep(options, "\n\t\t", " = ") + "\n}";
			}
			return "export_csv_instance " + inst + " " + Util.quote(file) + s;
		}

		@Override
		public int hashCode() {
			int prime = 31;
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

	public static final class PragmaExpToCsvTrans<Ty, En, Sym, Att, Fk, Gen1, Sk1, X1, Y1, Gen2, Sk2, X2, Y2> extends PragmaExp {

		public final String file;

		public final Map<String, String> options1;
		public final Map<String, String> options2;

		public final TransExp<Ty, En, Sym, Att, Fk, Gen1, Sk1, X1, Y1, Gen2, Sk2, X2, Y2> trans;

		@Override
		public Map<String, String> options() {
			return options1;
		}

		public PragmaExpToCsvTrans(TransExp<Ty, En, Sym, Att, Fk, Gen1, Sk1, X1, Y1, Gen2, Sk2, X2, Y2> trans, String file, List<Pair<String, String>> options1, List<Pair<String, String>> options2) {
			this.file = file;
			this.options1 = Util.toMapSafely(options1);
			this.options2 = Util.toMapSafely(options2);
			this.trans = trans;
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + ((file == null) ? 0 : file.hashCode());
			result = prime * result + ((options1 == null) ? 0 : options1.hashCode());
			result = prime * result + ((options2 == null) ? 0 : options2.hashCode());
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
			if (options1 == null) {
				if (other.options1 != null)
					return false;
			} else if (!options1.equals(other.options1))
				return false;
			if (options2 == null) {
				if (other.options2 != null)
					return false;
			} else if (!options2.equals(other.options2))
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
			String s = "";
			if (!options1.isEmpty()) {
				s = " {\n\toptions" + Util.sep(options1, "\n\t\t", " = ")  + "\n}";
			}
			if (!options2.isEmpty()) {
				s += "\n {\n\toptions" + Util.sep(options2, "\n\t\t", " = ")  + "\n}";
			}
		
			return "export_csv " + trans + " " + Util.quote(file) + s;
		}

		@Override
		public Pragma eval(AqlEnv env) {
			AqlOptions op1 = new AqlOptions(options1, null, env.defaults);
			AqlOptions op2 = new AqlOptions(options2, null, env.defaults);
			return new ToCsvPragmaTransform<>(trans.eval(env), file, op1, op2);
		}
	}

	//////////////////////////////////////////////////

	public static final class PragmaExpVar extends PragmaExp {
		public final String var;

		@Override
		public Map<String, String> options() {
			return Collections.emptyMap();
		}

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

	/////////////////////////////////////////////////////////////////////////////////////////

	public static final class PragmaExpJs extends PragmaExp {
		private final List<String> jss;

	
		private final Map<String, String> options;
		
		@Override
		public Map<String, String> options() {
			return options;
		}

		public PragmaExpJs(List<String> jss, List<Pair<String, String>> options) {
			this.options = Util.toMapSafely(options);
			this.jss = new LinkedList<>(jss);
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + ((options == null) ? 0 : options.hashCode());
			result = prime * result + ((jss == null) ? 0 : jss.hashCode());
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
			PragmaExpJs other = (PragmaExpJs) obj;

			if (options == null) {
				if (other.options != null)
					return false;
			} else if (!options.equals(other.options))
				return false;
			if (jss == null) {
				if (other.jss != null)
					return false;
			} else if (!jss.equals(other.jss))
				return false;
			return true;
		}

		@Override
		public Pragma eval(AqlEnv env) {
			return new JsPragma(jss, options, env);
		}

		@Override
		public String toString() {
			String s = "";
			if (!options.isEmpty()) {
				s = "\n\toptions" + Util.sep(options, "\n\t\t", " = ");
			}
		
			return "exec_js {\n" + Util.sep(jss.stream().map(Util::quote).collect(Collectors.toList()), "\n") + s + "\n}";
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Collections.emptyList();
		}

	}

	/////////////////////////////////////////////////////////////////////////////////////////

	public static final class PragmaExpProc extends PragmaExp {
		private final List<String> cmds;

		@Override
		public Map<String, String> options() {
			return options;
		}

		private final Map<String, String> options;

		public PragmaExpProc(List<String> cmds, List<Pair<String, String>> options) {
			this.options = Util.toMapSafely(options);
			this.cmds = new LinkedList<>(cmds);
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + ((options == null) ? 0 : options.hashCode());
			result = prime * result + ((cmds == null) ? 0 : cmds.hashCode());
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
			PragmaExpProc other = (PragmaExpProc) obj;

			if (options == null) {
				if (other.options != null)
					return false;
			} else if (!options.equals(other.options))
				return false;
			if (cmds == null) {
				if (other.cmds != null)
					return false;
			} else if (!cmds.equals(other.cmds))
				return false;
			return true;
		}

		@Override
		public Pragma eval(AqlEnv env) {
			return new ProcPragma(cmds, options);
		}

		//TODO aql doc
		@Override
		public String toString() {
			
			String s = "";
			if (!options.isEmpty()) {
				s = "\n\toptions" + Util.sep(options, "\n\t\t", " = ");
			}
		
			return "exec_js {\n" + Util.sep(cmds.stream().map(Util::quote).collect(Collectors.toList()), "\n") + s + "\n}";
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Collections.emptyList();
		}

	}

	/////////////////////////////////////////////////

	public static class PragmaExpToJdbcInst<Ty, En, Sym, Att, Fk, Gen, Sk, X, Y> extends PragmaExp {

		public final String jdbcString;
		public final String prefix;
		public final String clazz;

		public final Map<String, String> options;

		public final InstExp<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> I;

		@Override
		public Map<String, String> options() {
			return options;
		}

		public PragmaExpToJdbcInst(InstExp<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> i, String clazz, String jdbcString, String prefix, List<Pair<String, String>> options) {
			this.jdbcString = jdbcString;
			this.prefix = prefix;
			this.clazz = clazz;
			this.options = Util.toMapSafely(options);
			I = i;
			Util.checkClass(clazz);
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return I.deps();
		}

		@Override
		public Pragma eval(AqlEnv env) {
			String toGet = jdbcString;
			String driver = clazz;
			AqlOptions op = new AqlOptions(options, null, env.defaults);
			if (clazz.trim().isEmpty()) {
				driver = (String) op.getOrDefault(AqlOption.jdbc_default_class);
				Util.checkClass(driver);
			}
			if (jdbcString.trim().isEmpty()) {
				toGet = (String) op.getOrDefault(AqlOption.jdbc_default_string);
			}
			return new ToJdbcPragmaInstance<>(prefix, I.eval(env), driver, toGet, op);
		}

		@Override
		public String toString() {
			String s = "";
			if (!options.isEmpty()) {
				s = " {\n\toptions\n\t\t" + Util.sep(options, " = ", "\n\t\t")  + "\n}";
			}
		
			return "export_jdbc_instance " + I + " " + Util.quote(clazz) + " " + Util.quote(jdbcString) + " " + prefix + s;
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + ((I == null) ? 0 : I.hashCode());
			result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
			result = prime * result + ((jdbcString == null) ? 0 : jdbcString.hashCode());
			result = prime * result + ((options == null) ? 0 : options.hashCode());
			result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
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
			PragmaExpToJdbcInst<?, ?, ?, ?, ?, ?, ?, ?, ?> other = (PragmaExpToJdbcInst<?, ?, ?, ?, ?, ?, ?, ?, ?>) obj;

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
			if (prefix == null) {
				if (other.prefix != null)
					return false;
			} else if (!prefix.equals(other.prefix))
				return false;
			return true;
		}

	}

	////////////////////////////////////////////////////////////////////////////////////////////

	public static class PragmaExpToJdbcTrans<Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> extends PragmaExp {

		public final String jdbcString;
		public final String prefix;
		public final String clazz;

		public final Map<String, String> options1;
		public final Map<String, String> options2;

		public final TransExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> h;

		@Override
		public Map<String, String> options() {
			return options1;
		}

		public PragmaExpToJdbcTrans(TransExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> h, String clazz, String jdbcString, String prefix, List<Pair<String, String>> options1, List<Pair<String, String>> options2) {
			this.jdbcString = jdbcString;
			this.prefix = prefix;
			this.clazz = clazz;
			this.options1 = Util.toMapSafely(options1);
			this.options2 = Util.toMapSafely(options2);
			this.h = h;
			Util.checkClass(clazz);
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return h.deps();
		}

		@Override
		public Pragma eval(AqlEnv env) {
			String toGet = jdbcString;
			String driver = clazz;
			AqlOptions op1 = new AqlOptions(options1, null, env.defaults);
			AqlOptions op2 = new AqlOptions(options2, null, env.defaults);

			if (clazz.trim().isEmpty()) {
				driver = (String) op1.getOrDefault(AqlOption.jdbc_default_class);
				Util.checkClass(driver);
			}
			if (jdbcString.trim().isEmpty()) {
				toGet = (String) op1.getOrDefault(AqlOption.jdbc_default_string);
			}
			return new ToJdbcPragmaTransform<>(prefix, h.eval(env), driver, toGet, op1, op2);
		}

		//TODO aql maybe quote for RHS of options
		
		@Override
		public String toString() {
			String s = "";
			if (!options1.isEmpty()) {
				s += " {\n\toptions" + Util.sep(options1, "\n\t\t", " = ")  + "\n}\n";
			}
			if (!options1.isEmpty()) {
				s += " {\n\toptions" + Util.sep(options2, "\n\t\t", " = ")  + "\n}";
			}
		
			return "export_jdbc_transform " + h + " " + Util.quote(clazz) + " " + Util.quote(jdbcString) + " " + prefix + s;
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + ((h == null) ? 0 : h.hashCode());
			result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
			result = prime * result + ((jdbcString == null) ? 0 : jdbcString.hashCode());
			result = prime * result + ((options1 == null) ? 0 : options1.hashCode());
			result = prime * result + ((options2 == null) ? 0 : options2.hashCode());
			result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
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
			PragmaExpToJdbcTrans<?, ?, ?, ?, ?, ?, ?, ?> other = (PragmaExpToJdbcTrans<?, ?, ?, ?, ?, ?, ?, ?>) obj;

			if (h == null) {
				if (other.h != null)
					return false;
			} else if (!h.equals(other.h))
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
			if (options1 == null) {
				if (other.options1 != null)
					return false;
			} else if (!options1.equals(other.options1))
				return false;
			if (options2 == null) {
				if (other.options2 != null)
					return false;
			} else if (!options2.equals(other.options2))
				return false;
			if (prefix == null) {
				if (other.prefix != null)
					return false;
			} else if (!prefix.equals(other.prefix))
				return false;
			return true;
		}

	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static class PragmaExpToJdbcQuery<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> extends PragmaExp {

		public final String jdbcString;
		public final String prefixSrc;
		public final String prefixDst;
		public final String clazz;

		public final Map<String, String> options;

		public final QueryExp<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> Q;

		@Override
		public Map<String, String> options() {
			return options;
		}

		public PragmaExpToJdbcQuery(QueryExp<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> Q, String clazz, String jdbcString, String prefixSrc, String prefixDst, List<Pair<String, String>> options) {
			this.jdbcString = jdbcString;
			this.prefixSrc = prefixSrc;
			this.prefixDst = prefixDst;
			this.clazz = clazz;
			this.options = Util.toMapSafely(options);
			this.Q = Q;
			Util.checkClass(clazz);
		}

		@Override
		public Collection<Pair<String, Kind>> deps() {
			return Q.deps();
		}

		@Override
		public Pragma eval(AqlEnv env) {
			String toGet = jdbcString;
			String driver = clazz;
			AqlOptions op = new AqlOptions(options, null, env.defaults);
			if (clazz.trim().isEmpty()) {
				driver = (String) op.getOrDefault(AqlOption.jdbc_default_class);
				Util.checkClass(driver);
			}
			if (jdbcString.trim().isEmpty()) {
				toGet = (String) op.getOrDefault(AqlOption.jdbc_default_string);
			}
			return new ToJdbcPragmaQuery<>(prefixSrc, prefixDst, Q.eval(env), driver, toGet, op);
		}

		@Override
		public String toString() {
			String s = "";
			if (!options.isEmpty()) {
				s = " {\n\toptions" + Util.sep(options, "\n\t\t", " = ")  + "\n}";;
			}
		
			return "export_jdbc_query " + Q + " " + Util.quote(clazz) + " " + Util.quote(jdbcString) + " " + prefixSrc + " " + prefixDst + s;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((Q == null) ? 0 : Q.hashCode());
			result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
			result = prime * result + ((jdbcString == null) ? 0 : jdbcString.hashCode());
			result = prime * result + ((options == null) ? 0 : options.hashCode());
			result = prime * result + ((prefixDst == null) ? 0 : prefixDst.hashCode());
			result = prime * result + ((prefixSrc == null) ? 0 : prefixSrc.hashCode());
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
			PragmaExpToJdbcQuery<?, ?, ?, ?, ?, ?, ?, ?> other = (PragmaExpToJdbcQuery<?, ?, ?, ?, ?, ?, ?, ?>) obj;
			if (Q == null) {
				if (other.Q != null)
					return false;
			} else if (!Q.equals(other.Q))
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
			if (prefixDst == null) {
				if (other.prefixDst != null)
					return false;
			} else if (!prefixDst.equals(other.prefixDst))
				return false;
			if (prefixSrc == null) {
				if (other.prefixSrc != null)
					return false;
			} else if (!prefixSrc.equals(other.prefixSrc))
				return false;
			return true;
		}

	}
}