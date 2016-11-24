package catdata.aql.exp;

import catdata.aql.Instance;
import catdata.aql.Mapping;
import catdata.aql.Pragma;
import catdata.aql.Query;
import catdata.aql.Schema;
import catdata.aql.Transform;
import catdata.aql.TypeSide;
import catdata.graph.DMG;

public final class AqlEnv {

	@SuppressWarnings("rawtypes")
	public final KindCtx<String, DMG, TypeSide, Schema, Instance, Transform, Mapping, Query, Pragma> defs = new KindCtx<>();
	
	public RuntimeException exn = null;
	
	public AqlTyping typing = null;
}
