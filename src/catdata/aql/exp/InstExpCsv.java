package catdata.aql.exp;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import catdata.Ctx;
import catdata.Null;
import catdata.Pair;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Schema;
import catdata.aql.Term;

public class InstExpCsv<Ty, En, Sym, Fk, Att, Gen> extends InstExpImport<Ty, En, Sym, Fk, Att, Gen, Map<En, List<CSVRecord>>> {
	
	public InstExpCsv(SchExp<Ty, En, Sym, Fk, Att> schema, List<Pair<LocStr, String>> map,
			 List<Pair<String, String>> options) {
		super(schema, map, options);
	}

	public final static String helpStr = "Possible problem: AQL IDs be unique among all entities and types; it is not possible to have, for example,"
			+ "\n" + "\n	0:Employee" + "\n	0:Department" + "\n"
			+ "\nPossible solution: Distinguish the IDs prior to import";
	
	@Override
	protected String getHelpStr() {
		return helpStr;
	}
	
	public static CSVFormat getFormat(AqlOptions op) {
		String format0 = (String) op.getOrDefault(AqlOption.csv_format);
		CSVFormat format = CSVFormat.valueOf(format0);

		if (op.options.containsKey(AqlOption.csv_field_delim_char)) {
			format = format.withDelimiter((Character) op.get(AqlOption.csv_field_delim_char));
		}
		if (op.options.containsKey(AqlOption.csv_quote_char)) {
			format = format.withQuote((Character) op.get(AqlOption.csv_quote_char));
		}
		if (op.options.containsKey(AqlOption.csv_line_delim_string)) {
			format = format.withRecordSeparator((String) op.get(AqlOption.csv_line_delim_string));
		}
		// escape isn't enabled by default
		format = format.withEscape((Character) op.getOrDefault(AqlOption.csv_escape_char));

		if (op.options.containsKey(AqlOption.csv_null_string)) {
			format = format.withNullString((String) op.getOrDefault(AqlOption.csv_null_string));
		}
		return format;
	}

	@Override
	public String toString() {
		return "import_csv : " + schema + " {\n\t"
				+ Util.sep(map, " -> ", "\n\t") + "\n}";
	}

	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof InstExpCsv) && super.equals(obj);
	}

	
	public static <Ty, En, Sym, Fk, Att> Map<En, List<CSVRecord>> start(boolean firstRecHeader, Map<String, String> map, AqlOptions op, Schema<Ty, En, Sym, Fk, Att> sch) throws Exception {
		String charset0 = (String) op.getOrDefault(AqlOption.csv_charset);
		Charset charset = Charset.forName(charset0);
		CSVFormat format = getFormat(op);
		if (firstRecHeader) {
			format = format.withFirstRecordAsHeader();
		}
		
		Map<En, List<CSVRecord>> ret = new HashMap<>();
		for (String k : map.keySet()) {
			if (!sch.ens.contains(k)) {
				throw new RuntimeException("Not an entity: " + k);
			}
			File file = new File(map.get(k));
			CSVParser parser = CSVParser.parse(file, charset, format);
			List<CSVRecord> rows = parser.getRecords();
			parser.close();
			ret.put((En)k, rows);
		}
		for (En en : sch.ens) {
			if (!ret.containsKey(en)) {
				ret.put(en, new LinkedList<>());
			}
		}
		return ret;
	}
	
	@Override
	protected Map<En, List<CSVRecord>> start(Schema<Ty, En, Sym, Fk, Att> sch) throws Exception {
		return start(true, map, op, sch);
	}

	@Override
	protected void end(Map<En, List<CSVRecord>> h) throws Exception {
		//clear h?
	}

	@Override
	protected void joinedEn(Map<En, List<CSVRecord>> map, En en, String s, Schema<Ty, En, Sym, Fk, Att> sch) throws Exception {
		String idCol = (String) op.getOrDefault(AqlOption.id_column_name);
		for (CSVRecord row : map.get(en)) {
			Gen l0 = (Gen) row.get(idCol);

			ens0.get(en).add(l0);
			
			for (Fk fk : sch.fksFrom(en)) {
				if (!fks0.containsKey(l0)) {
					fks0.put(l0, new Ctx<>());
				}
				Gen g = (Gen) row.get((String) fk);				
				fks0.get(l0).put(fk, g);
			}
			
			for (Att att : sch.attsFrom(en)) {
				if (!atts0.containsKey(l0)) {
					atts0.put(l0, new Ctx<>());
				}
				Object o = row.get((String) att);
				Term<Ty, Void, Sym, Void, Void, Void, Null<?>> r 
				= objectToSk(sch, o, l0.toString(), att, tys0, extraRepr, true); 
				atts0.get(l0).put(att, r);
			}
		}
		
	}

	//TODO aql shredded input format for CSV
	@Override
	protected void shreddedAtt(Map<En, List<CSVRecord>> h, Att att, String s, Schema<Ty, En, Sym, Fk, Att> sch) throws Exception {
		throw new RuntimeException("Shredded input format not avaiable for CSV (if desired, please email info@catinf.com)");
	}

	@Override
	protected void shreddedFk(Map<En, List<CSVRecord>> h, Fk fk, String s, Schema<Ty, En, Sym, Fk, Att> sch) throws Exception {
		throw new RuntimeException("Shredded input format not avaiable for CSV (if desired, please email info@catinf.com)");
	}

	@Override
	protected void shreddedEn(Map<En, List<CSVRecord>> h, En en, String s, Schema<Ty, En, Sym, Fk, Att> sch) throws Exception {
		throw new RuntimeException("Shredded input format not avaiable for CSV (if desired, please email info@catinf.com)");
	}

	

}
