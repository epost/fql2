package catdata.aql;

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

//TODO AQL add dates
public class SqlTypeSide extends TypeSide<String, String> {

	public SqlTypeSide(AqlOptions ops) {
		super(tys(), syms(), eqs(), jts(), jps(), jfs(), ops);
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
	
	//bit tinyint real numeric date time timestamp
	public static int getSqlType(String s) {
		switch (s.toLowerCase()) {
		case "varbinary": return Types.VARBINARY;
		case "longvarbinary": return Types.LONGVARBINARY;
		case "date" : return Types.DATE;
		case "time" : return Types.TIME;
		case "timestamp" : return Types.TIMESTAMP;
		case "bigint": return Types.BIGINT;
		case "boolean": return Types.BOOLEAN;
		case "char": return Types.CHAR;
		case "double": return Types.DOUBLE;
		case "doubleprecision": return Types.DOUBLE;
		case "numeric": return Types.NUMERIC;
		case "decimal": return Types.DECIMAL;
		case "real": return Types.REAL;
		case "float": return Types.FLOAT;
		case "integer": return Types.INTEGER;
		case "tinyint": return Types.TINYINT;
		case "bit": return Types.BIT;
		case "smallint": return Types.SMALLINT;
		case "text": return Types.VARCHAR;
		case "nvarchar": return Types.NVARCHAR;
		case "longvarchar": return Types.LONGVARCHAR;
		case "varchar": return Types.VARCHAR;
		case "string": return Types.VARCHAR;
		case "int": return Types.INTEGER;
		case "bool": return Types.BOOLEAN;
		case "dom": return Types.BLOB;
		case "other": return Types.OTHER;
		}
		return Util.anomaly();
	}

	private static Map<String, String> jts() {
		Map<String, String> m = new HashMap<>();
		m.put("Longvarbinary", "[B"); //TODO aql
		m.put("Varbinary", "[B"); //TODO aql
		
		m.put("Bigint", "java.lang.Long");
		m.put("Boolean", "java.lang.Boolean");
		m.put("Bit", "java.lang.Boolean");
		m.put("Bool", "java.lang.Boolean");
		m.put("Char", "java.lang.Character");
		m.put("Double", "java.lang.Double");
		m.put("Doubleprecision", "java.lang.Double");
		m.put("Decimal", "java.math.BigDecimal");
		m.put("Numeric", "java.math.BigDecimal");
		m.put("Float", "java.lang.Float");
		m.put("Real", "java.lang.Float");
		m.put("Integer", "java.lang.Integer");
		m.put("Int", "java.lang.Integer");
		m.put("Smallint", "java.lang.Short");
		m.put("Tinyint", "java.lang.Short"); 

		m.put("Text", "java.lang.String");
		m.put("Nvarchar", "java.lang.String");
		m.put("Varchar", "java.lang.String");
		m.put("Longvarchar", "java.lang.String");
		m.put("String", "java.lang.String");
		m.put("Custom", "java.lang.Object");
		m.put("Dom", "java.lang.Object");
		m.put("Other", "java.lang.Object");
		
		
/*
		m.put("Date", "java.lang.Object"); //TODO aql
		m.put("Time", "java.lang.Object"); //TODO aql
		m.put("Timestamp", "java.lang.Object"); //TODO aql
		*/

		m.put("Date", "java.sql.Date");
		m.put("Time", "java.sql.Time");
		m.put("Timestamp", "java.sql.Timestamp");
		

		return m;
	}

	private static Map<String, String> jps() {
		Map<String, String> m = new HashMap<>();

		m.put("Longvarbinary", "return input[0]"); //TODO AQL
		m.put("Varbinary", "return nput[0]"); //TODO AQL
/*
		m.put("Date", "return input[0]");
		m.put("Time", "return input[0]");
		m.put("Timestamp", "return input[0]");
		*/

		m.put("Date", "return java.sql.Date.valueOf(input[0])");
		m.put("Time", "return java.sql.Time.valueOf(input[0])");
		m.put("Timestamp", "return java.sql.Timestamp.valueOf(input[0])"); 
		
		m.put("Bigint", "return new java.lang.Long(input[0])");
		m.put("Boolean", "return new java.lang.Boolean(input[0])");
		m.put("Bool", "return new java.lang.Boolean(input[0])");
		m.put("Char", "return input[0]");
		m.put("Bit", "return new java.lang.Boolean(input[0])");

		m.put("Double", "return new java.lang.Double(input[0])");
		m.put("Doubleprecision", "return new java.lang.Double(input[0])");
		m.put("Numeric", "return new java.math.BigDecimal(input[0])");

		m.put("Decimal", "return new java.math.BigDecimal(input[0])");
		m.put("Real", "return new java.lang.Float(input[0])");

		m.put("Float", "return new java.lang.Float(input[0])");
		m.put("Integer", "return new java.lang.Integer(input[0])");
		m.put("Int", "return new java.lang.Integer(input[0])");

		m.put("Tinyint", "return new java.lang.Short(input[0])");
		m.put("Smallint", "return new java.lang.Short(input[0])");
		m.put("Text", "return input[0]");
		m.put("String", "return input[0]");

		m.put("Nvarchar", "return input[0]");
		m.put("Varchar", "return input[0]");
		m.put("Longvarchar", "return input[0]");
		m.put("Custom", "return input[0]");
		m.put("Dom", "return input[0]");
		m.put("Other", "return input[0]");
		return m;
	}

	private static Map<String, String> jfs() {
		return Collections.emptyMap();
	}

	public static String mediate(String t) {
		switch (t.toLowerCase()) {
		case "string": return "varchar";
		case "int": return "integer";
		case "bool": return "boolean";
		case "dom": return "other";
		}
		return t;
	}
	

}
