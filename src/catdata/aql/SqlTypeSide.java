package catdata.aql;

import java.sql.SQLType;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import catdata.Ctx;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;

public class SqlTypeSide extends TypeSide<String, String> {

//	private final AqlOptions ops;
	public SqlTypeSide(AqlOptions ops) {
		super(tys(), syms(), eqs(), jts(), jps(), jfs(), ops);
	//	this.ops = ops;
	}
	
	private static Set<String> tys() {
		return jts().keySet();
	}
	
	private static Map<String, Pair<List<String>, String>> syms() {
		return Collections.emptyMap();
	}
	
	private static Set<Triple<Ctx<Var, String>, Term<String, Void, String, Void, Void, Void, Void>, Term<String, Void, String, Void, Void, Void, Void>>> eqs() {
		return Collections.emptySet();
	}
	
	public static int getSqlType(String s) {
		switch (s.toLowerCase()) {
		case "bigint": return Types.BIGINT;
		case "boolean": return Types.BOOLEAN;
		case "char": return Types.CHAR;
		case "doubleprecision": return Types.DOUBLE;
		case "float": return Types.FLOAT;
		case "integer": return Types.INTEGER;
		case "smallint": return Types.SMALLINT;
		case "text": return Types.VARCHAR;
		case "varchar": return Types.VARCHAR;
		case "other": return Types.OTHER;
		}
		return Util.anomaly();
	}

	private static Map<String, String> jts() {
		Map<String, String> m = new HashMap<>();
		m.put("BigInt", "java.lang.Long");
		m.put("Boolean", "java.lang.Boolean");
		m.put("Char", "java.lang.Character");
		m.put("DoublePrecision", "java.lang.Double");
		m.put("Float", "java.lang.Float");
		m.put("Integer", "java.lang.Integer");
		m.put("SmallInt", "java.lang.Short");
		m.put("Text", "java.lang.String");
		m.put("Varchar", "java.lang.String");
		m.put("Custom", "java.lang.Object");
		return m;
	}

	private static Map<String, String> jps() {
		Map<String, String> m = new HashMap<>();
		m.put("BigInt", "return new java.lang.Long(input[0])");
		m.put("Boolean", "return new java.lang.Boolean(input[0])");
		m.put("Char", "return input[0].charAt(0)");
		m.put("DoublePrecision", "return new java.lang.Double(input[0])");
		m.put("Float", "return new java.lang.Float(input[0])");
		m.put("Integer", "return new java.lang.Integer(input[0])");
		m.put("SmallInt", "return new java.lang.Short(input[0])");
		m.put("Text", "return input[0]");
		m.put("Varchar", "return input[0]");
		m.put("Custom", "return input[0]");
		return m;
	}

	private static Map<String, String> jfs() {
		return Collections.emptyMap();
	}
	

}
