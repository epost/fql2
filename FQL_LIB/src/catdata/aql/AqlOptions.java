package catdata.aql;

import java.util.HashMap;
import java.util.Map;

import catdata.Util;

public final class AqlOptions {
	
	public final Map<AqlOption, Object> options; 

	public <Ty, En, Sym, Fk, Att, Gen, Sk> AqlOptions(ProverName name, Object ob) {
		options = new HashMap<>();
		options.put(AqlOption.prover, name);
		options.put(AqlOption.precomputed, ob);
	}
	
	//anything 'unsafe' should default to false
	private Object getDefault(AqlOption option) {
		switch (option) {
		case allow_java_eqs_unsafe:
			return false;
		case precedence:
			throw new RuntimeException("No default precedence");
		case precomputed:
			throw new RuntimeException("No default precomputed");
		case prover:
			throw new RuntimeException("No default prover");
		case require_consistency:
			return false;
		case timeout:
			return 5000;
		case dont_verify_is_appropriate_for_prover_unsafe:
			return false;
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
			case precedence:
				ob = op.getPrec(map, col);
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
			default:
				throw new RuntimeException("Anomaly: please report");
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
