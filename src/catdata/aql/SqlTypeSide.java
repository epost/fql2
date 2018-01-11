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
import catdata.aql.exp.TyExpRaw.Sym;
import catdata.aql.exp.TyExpRaw.Ty;

//TODO AQL add dates
public class SqlTypeSide extends TypeSide<Ty, Sym> {

	//TODO AQL varchar length can go here, or can add annotations on types
	//TODO AQL get rid of String
	public SqlTypeSide(AqlOptions ops) {
		super(tys(), syms(), eqs(), jts(), jps(), jfs(), ops);
	}
	
	public static Set<Ty> tys() {
		return jts().keySet();
	}
	
	private static Map<Sym, Pair<List<Ty>, Ty>> syms() {
		return Collections.emptyMap();
	}
	
	private static Set<Triple<Ctx<Var, Ty>, Term<Ty, Void, Sym, Void, Void, Void, Void>, Term<Ty, Void, Sym, Void, Void, Void, Void>>> eqs() {
		return Collections.emptySet();
	}
	
	//bit tinyint real numeric date time timestamp
	//TODO aql do this generically
	public static int getSqlType(String s) {
		switch (s.toLowerCase()) {
		case "varbinary": return Types.VARBINARY;
		case "longvarbinary": return Types.LONGVARBINARY;
		case "binary": return Types.BINARY;
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
		case "nvarchar": return Types.NVARCHAR;
		case "longvarchar": return Types.LONGVARCHAR;
		case "text": return Types.VARCHAR;
		case "varchar": return Types.VARCHAR;
		case "string": return Types.VARCHAR;
		case "int": return Types.INTEGER;
		case "bool": return Types.BOOLEAN;
		case "dom": return Types.BLOB;
		case "blob": return Types.BLOB;
		case "other": return Types.OTHER;
		case "clob": return Types.CLOB;
		}
		return Util.anomaly();
	}

	private static Map<Ty, String> jts() {
		Map<Ty, String> m = new HashMap<>();
		m.put(new Ty("Longvarbinary"), "[B"); //TODO aql
		m.put(new Ty("Varbinary"), "[B"); //TODO aql
		m.put(new Ty("Binary"), "[B"); //TODO aql
		
		m.put(new Ty("Bigint"), "java.lang.Long");
		m.put(new Ty("Boolean"), "java.lang.Boolean");
		m.put(new Ty("Bit"), "java.lang.Boolean");
		m.put(new Ty("Bool"), "java.lang.Boolean");
		m.put(new Ty("Char"), "java.lang.String"); //TODO aql
		m.put(new Ty("Double"), "java.lang.Double");
		m.put(new Ty("Doubleprecision"), "java.lang.Double");
		m.put(new Ty("Decimal"), "java.math.BigDecimal");
		m.put(new Ty("Numeric"), "java.math.BigDecimal");
		m.put(new Ty("Float"), "java.lang.Float");
		m.put(new Ty("Real"), "java.lang.Float");
		m.put(new Ty("Integer"), "java.lang.Integer");
		m.put(new Ty("Int"), "java.lang.Integer");
		m.put(new Ty("Smallint"), "java.lang.Integer");
		m.put(new Ty("Tinyint"), "java.lang.Integer"); 

		m.put(new Ty("Text"), "java.lang.String");
		m.put(new Ty("Nvarchar"), "java.lang.String");
		m.put(new Ty("Varchar"), "java.lang.String");
		m.put(new Ty("Longvarchar"), "java.lang.String");
		m.put(new Ty("String"), "java.lang.String");
		m.put(new Ty("Custom"), "java.lang.Object");
		m.put(new Ty("Dom"), "java.lang.Object");
		m.put(new Ty("Blob"), "java.lang.Object");
		m.put(new Ty("Clob"), "java.lang.Object");
		m.put(new Ty("Other"), "java.lang.Object");
		
		
/*
		m.put("Date", "java.lang.Object"); //TODO aql
		m.put("Time", "java.lang.Object"); //TODO aql
		m.put("Timestamp", "java.lang.Object"); //TODO aql
		*/

		m.put(new Ty("Date"), "java.lang.Object");
		m.put(new Ty("Time"), "java.sql.Time");
		m.put(new Ty("Timestamp"), "java.sql.Timestamp");
		

		return m;
	}

	private static Map<Ty, String> jps() {
		Map<Ty, String> m = new HashMap<>();

		m.put(new Ty("Longvarbinary"), "return input[0]"); //TODO AQL
		m.put(new Ty("Varbinary"), "return input[0]"); //TODO AQL
		m.put(new Ty("Binary"), "return input[0]"); //TODO AQL

		/*
		m.put("Date", "return input[0]");
		m.put("Time", "return input[0]");
		m.put("Timestamp", "return input[0]");
		*/

		m.put(new Ty("Clob"), "return input[0]"); 
		m.put(new Ty("Date"), "return input[0]"); //java.sql.Date.valueOf(input[0])");
		m.put(new Ty("Time"), "return java.sql.Time.valueOf(input[0])");
		m.put(new Ty("Timestamp"), "return java.sql.Timestamp.valueOf(input[0])"); 
		
		m.put(new Ty("Bigint"), "return new java.lang.Long(input[0])");
		m.put(new Ty("Boolean"), "return new java.lang.Boolean(input[0])");
		m.put(new Ty("Bool"), "return new java.lang.Boolean(input[0])");
		m.put(new Ty("Char"), "return input[0]"); //TODO aql
		m.put(new Ty("Bit"), "return new java.lang.Boolean(input[0])");

		m.put(new Ty("Double"), "return new java.lang.Double(input[0])");
		m.put(new Ty("Doubleprecision"), "return new java.lang.Double(input[0])");
		m.put(new Ty("Numeric"), "return new java.math.BigDecimal(input[0])");

		m.put(new Ty("Decimal"), "return new java.math.BigDecimal(input[0])");
		m.put(new Ty("Real"), "return new java.lang.Float(input[0])");

		m.put(new Ty("Float"), "return new java.lang.Float(input[0])");
		m.put(new Ty("Integer"), "return new java.lang.Integer(input[0])");
		m.put(new Ty("Int"), "return new java.lang.Integer(input[0])");

		m.put(new Ty("Tinyint"), "return new java.lang.Integer(input[0])");
		m.put(new Ty("Smallint"), "return new java.lang.Integer(input[0])");
		m.put(new Ty("Text"), "return input[0]");
		m.put(new Ty("String"), "return input[0]");

		m.put(new Ty("Nvarchar"), "return input[0]");
		m.put(new Ty("Varchar"), "return input[0]");
		m.put(new Ty("Longvarchar"), "return input[0]");
		m.put(new Ty("Custom"), "return input[0]");
		m.put(new Ty("Dom"), "return input[0]");
		m.put(new Ty("Other"), "return input[0]");
		m.put(new Ty("Blob"), "return input[0]");
		return m;
	}

	private static Map<Sym, String> jfs() {
		return Collections.emptyMap();
	}

	public static String mediate(int len, String t) {
		switch (t.toLowerCase()) {
		case "varchar": return "varchar(" + len + ")";
		case "string": return "varchar(" + len + ")";
		case "int": return "integer";
		case "bool": return "boolean";
		case "dom": return "other";
		}
		return t;
	}
	

}
