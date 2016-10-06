package catdata.aql;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import catdata.Util;

//TODO why showing viewer so long - change
public final class AqlOptions {
	
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
	
	//TODO anything 'unsafe' should default to false, but need empty sorts for now
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
		case require_consistency: //TODO: require consistency check
			return false;
		case timeout:
			return 5;
		case dont_verify_is_appropriate_for_prover_unsafe:
			return false;
		case allow_empty_sorts_unsafe:
			return true; //TODO set to false eventually
		case completion_compose:
			return true;
		case completion_filter_subsumed:
			return true;
		case completion_sort:
			return true;
		}
		throw new RuntimeException("Anomaly: please report");
	}
	
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
			case allow_empty_sorts_unsafe:
				ob = op.getBoolean(map);
				break;	
			case completion_compose:
				ob = op.getBoolean(map);
				break;
			case completion_filter_subsumed:
				ob = op.getBoolean(map);
				break;
			case completion_sort:
				ob = op.getBoolean(map);
				break;
			default:
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


	
	
	
}
