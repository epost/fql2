package catdata.aql;

import java.util.Arrays;
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
	
	//TODO aql number of cores
	
	//TODO: aql each typeside/instance/etc should make sure only appropriate options are given to it

	public enum AqlOption {
		csv_charset,
		csv_line_delim_string,
		csv_field_delim_char,
		csv_escape_char,
		csv_quote_char,
		csv_format,
		csv_null_string,
		id_column_name,
		always_reload,
		varchar_length,
				
		program_allow_nontermination_unsafe,
		completion_precedence,
		completion_sort,
		completion_compose,
		completion_filter_subsumed,
		completion_syntactic_ac,
		allow_java_eqs_unsafe, //TODO aql enforce
		require_consistency, //TODO: aql enforce require_consistency
		timeout, 
		dont_verify_is_appropriate_for_prover_unsafe,
		dont_validate_unsafe,
		static_typing,
		prover;
		
			
		
		private String getString(Map<String, String> map) {
			String n = map.get(toString());
			if (n == null) {
				throw new RuntimeException("No option named " + this + " in options");
			}
			return n;
		}
		
		public Boolean getBoolean(Map<String, String> map) {
			return Boolean.parseBoolean(getString(map));
		}
		/*
		public String getMaybeString(Map<String, String> map) {
			if (map.containsKey(this.toString())) {
				return getString(map);
			}
			return null;
		}
		*/
		public Character getChar(Map<String, String> map) {
			String s = getString(map);
			if (s.length() != 1) {
				throw new RuntimeException("Expected a character, instead received "+ s);
			}
			return s.charAt(0);
		}
		
		public Integer getInteger(Map<String, String> map) {
			return Integer.parseInt(getString(map));
		}
		
		public Long getLong(Map<String, String> map) {
			return Long.parseLong(getString(map));
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

	public AqlOptions(ProverName name) {
		options = new HashMap<>();
		options.put(AqlOption.prover, name);
	}
	
	private static String printDefault() {
		List<String> l = new LinkedList<>();
		for (AqlOption option : AqlOption.values()) {
			Object o = getDefault(option);
			if (o == null) {
				l.add(option + " has a null default ");
			} else {
				l.add(option + " = " + o);				
			}
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
		case prover:
			return ProverName.auto;
		case dont_validate_unsafe:
			return false;
		case require_consistency: 
			return false;
		case timeout:
			return 10L;
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
		case static_typing:
			return false;
		case always_reload:
			return false;
		case csv_charset:
			return "UTF-8";
		case csv_escape_char:
			return '\\';
		case csv_field_delim_char:
			return ',';
		case csv_format:
			return "Default";
		case id_column_name:
			return "id";
		case csv_line_delim_string:
			return "\n";
		case csv_quote_char:
			return '\"';
		case varchar_length:
			return 64;
		case csv_null_string:
			return null;
		case program_allow_nontermination_unsafe:
			return false;
		default:
			throw new RuntimeException("Anomaly: please report: "+ option);	
		}
		
	}
	
	public static Object getOrDefault(Map<String, String> map, AqlOption op) {
		if (map.containsKey(op.toString())) {
			return getFromMap(map, null, op);
		}
			return getDefault(op);
	}
	
	/**
	 * @param map
	 * @param col possibly null
	 */ 
	public <Ty, En, Sym, Fk, Att, Gen, Sk> AqlOptions(Map<String, String> map, Collage<Ty,En,Sym,Fk,Att,Gen,Sk> col) {
		options = new HashMap<>();
		for (String key : map.keySet()) {
			AqlOption op = AqlOption.valueOf(key);
			Object ob = getFromMap(map, col, op);
			options.put(op, ob);
		}		
	} 

	private static <Ty, En, Sym, Fk, Att, Gen, Sk> Object getFromMap(Map<String, String> map, Collage<Ty, En, Sym, Fk, Att, Gen, Sk> col, AqlOption op) {
		switch (op) {
		case allow_java_eqs_unsafe:
			return op.getBoolean(map);
		case completion_precedence:
			return AqlOption.getPrec(map.get(op.toString()), col);
		case prover:
			return op.getDPName(map);
		case require_consistency:
			return op.getBoolean(map);
		case timeout:
			return op.getLong(map);
		case dont_verify_is_appropriate_for_prover_unsafe:
			return op.getBoolean(map);
		case completion_compose:
			return op.getBoolean(map);
		case completion_filter_subsumed:
			return op.getBoolean(map);
		case completion_sort:
			return op.getBoolean(map);
		case completion_syntactic_ac:
			return op.getBoolean(map);
		case dont_validate_unsafe:
			return op.getBoolean(map);
		case static_typing:
			return op.getBoolean(map);
		case always_reload:
			return op.getBoolean(map);
		case csv_charset:
			return op.getString(map);
		case csv_escape_char:
			return op.getChar(map);
		case csv_field_delim_char:
			return op.getString(map);
		case csv_format:
			return op.getString(map);
		case id_column_name:
			return op.getString(map);
		case csv_line_delim_string:
			return op.getChar(map);
		case csv_quote_char:
			return op.getChar(map);
		case varchar_length:
			return op.getInteger(map);
		case csv_null_string:
			return op.getString(map);
		case program_allow_nontermination_unsafe:
			return op.getBoolean(map);
		default:
			throw new RuntimeException("Anomaly: please report");
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
		int prime = 31;
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


	public static final class AqlOptionsDefunct extends Options {

		private static final long serialVersionUID = 1L;
	
		@Override
		public String getName() {
			return Language.AQL.toString();
		}
		
		private final String msg0 = "completion_precedence = \"a b c\" means a < b < c";
		private final String msg1 = msg0 + "\n\nAvailable provers: " + Arrays.toString(ProverName.values());
		private final String msg  = msg1 + "\n\nOption descriptions are available in the AQL manual, see categoricaldata.net/fql.html";
		@Override
		public Pair<JComponent, Function<Unit, Unit>> display() {
			return new Pair<>(new CodeTextPanel("", "Aql options are specified in each Aql expression.\nHere are the available options and their defaults:\n\n\t" + printDefault() + "\n\n" + msg), x -> x);
		}
	
		@Override
		public int size() {
			return 1;
		}

}
	
	
	
}
