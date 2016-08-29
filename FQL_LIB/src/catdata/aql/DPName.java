package catdata.aql;

public enum DPName {

	FINITE,
	UNARY,
	PROGRAM,
	COMPLETION,
	CONGRUENCE,
	ALLJAVA,
	FAIL,
	PRECOMPUTED;
	
	public boolean requiresParam() {
		return this == COMPLETION || this == PRECOMPUTED || this == PROGRAM;
	}
}
