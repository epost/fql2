package catdata.aql;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JComponent;

import catdata.Pair;
import catdata.Unit;
import catdata.Util;
import catdata.aql.AqlProver.ProverName;
import catdata.aql.exp.AqlParser;
import catdata.ide.CodeTextPanel;
import catdata.ide.Language;
import catdata.ide.Options;

public final class AqlOptions {
	

	public static enum AqlOption {
	
		//TODO number of cores
		
		//TODO: each typeside/instance/etc should make sure only appropriate options are given to it
		
		completion_precedence,
		completion_sort,
		completion_compose,
		completion_filter_subsumed,
		completion_syntactic_ac,
		precomputed,
		allow_java_eqs_unsafe,
		require_consistency, //TODO: enforce
		timeout, 
		dont_verify_is_appropriate_for_prover_unsafe,
		prover;
		
			
		
		private String getString(Map<String, String> map) {
			String n = map.get(this.toString());
			if (n == null) {
				throw new RuntimeException("No option named " + this + " in options");
			}
			return n;
		}
		
		public Boolean getBoolean(Map<String, String> map) {
			return Boolean.parseBoolean(getString(map));
		}
		
		public Integer getInteger(Map<String, String> map) {
			return Integer.parseInt(getString(map));
		}
		
		public Integer getNat(Map<String, String> map) {
			Integer ret = getInteger(map);
			if (ret < 0) {
				throw new RuntimeException("Expected non-zero integer for " + this);
			}
			return ret;
		}
		
		public static <Ty, En, Sym, Fk, Att, Gen, Sk> List<Head<Ty, En, Sym, Fk, Att, Gen, Sk>> getPrec(String str, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col) {
			if (str == null) {
				throw new RuntimeException("Anomaly: please report");
			}
			return AqlParser.parseManyIdent(str).stream().map(x -> RawTerm.toHeadNoPrim(x, col)).collect(Collectors.toList());		
		}
		
		public ProverName getDPName(Map<String, String> map) {
			return ProverName.valueOf(getString(map));
		}
		
		
		
	}

	
	public final Map<AqlOption, Object> options; 

	public <Ty, En, Sym, Fk, Att, Gen, Sk> AqlOptions(ProverName name, Object ob) {
		options = new HashMap<>();
		options.put(AqlOption.prover, name);
		options.put(AqlOption.precomputed, ob);
	}
	
	public static String printDefault() {
		List<String> l = new LinkedList<>();
		for (AqlOption option : AqlOption.values()) {
			if (option.equals(AqlOption.precomputed)) {
				continue;
			}
			l.add(option + " = " + getDefault(option));
		}
		return Util.sep(l, "\n\t");
	}
	
	//anything 'unsafe' should default to false
	private static Object getDefault(AqlOption option) {
		switch (option) {
		case allow_java_eqs_unsafe:
			return false;
		case completion_precedence:
			return null;
		case precomputed:
			throw new RuntimeException("Anomaly: please report");
		case prover:
			return ProverName.auto;
		case require_consistency: 
			return false;
		case timeout:
			return 5;
		case dont_verify_is_appropriate_for_prover_unsafe:
			return false;
		case completion_compose:
			return true;
		case completion_filter_subsumed:
			return true;
		case completion_sort:
			return true;
		case completion_syntactic_ac:
			return false;
		}
		throw new RuntimeException("Anomaly: please report");
	}
	
	//TODO aql
	public <Ty, En, Sym, Fk, Att, Gen, Sk> AqlOptions(Map<String, String> map, Collage<Ty,En,Sym,Fk,Att,Gen,Sk> col) {
		options = new HashMap<>();
		for (String key : map.keySet()) {
			AqlOption op = AqlOption.valueOf(key);
			Object ob = null;
			switch (op) {
			case allow_java_eqs_unsafe:
				ob = op.getBoolean(map);
				break;
			case completion_precedence:
				ob = AqlOption.getPrec(map.get(op.toString()), col);
				break;
			case prover:
				ob = op.getDPName(map);
				break;
			case require_consistency:
				ob = op.getBoolean(map);
				break;
			case timeout:
				ob = op.getNat(map);
				break;
			case dont_verify_is_appropriate_for_prover_unsafe:
				ob = op.getBoolean(map);
				break;
			case precomputed:
				throw new RuntimeException(op + " option is reserved for AQL compiler");
			case completion_compose:
				ob = op.getBoolean(map);
				break;
			case completion_filter_subsumed:
				ob = op.getBoolean(map);
				break;
			case completion_sort:
				ob = op.getBoolean(map);
				break;
			case completion_syntactic_ac:
				ob = op.getBoolean(map);
				break;
			}
			options.put(op, ob);
		}
		
	}

	/*
	public AqlOptions(Map<AqlOption, Object> options) {
		if (options == null) {
			throw new RuntimeException("Attempt to create options with null options");
		}
		this.options = options;
	} */

	public Object getOrDefault(AqlOption op) {
		Object o = options.get(op);
		if (o == null) {
			return getDefault(op);
		}
		return o;
	}
	
	public Object get(AqlOption op) {
		Object o = options.get(op);
		if (o == null) {
			throw new RuntimeException("Missing required option " + op);
		}
		return o;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		AqlOptions other = (AqlOptions) obj;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return Util.sep(options, " = ", "\n");
	}


	public final static class AqlOptionsDefunct extends Options {

		private static final long serialVersionUID = 1L;
	
		@Override
		public String getName() {
			return Language.AQL.toString();
		}
		
		String msg = "completion_precedence = \"a b c\" means a < b < c";
	
		@Override
		public Pair<JComponent, Function<Unit, Unit>> display() {
			return new Pair<>(new CodeTextPanel("", "Aql options are specified as pragmas in each Aql file.\nHere are the available options and their defaults:\n\n\t" + AqlOptions.printDefault() + "\n\n" + msg), x -> x);
		}
	
		@Override
		public int size() {
			return 1;
		}

}
	
	
	
}
