package catdata.aql.exp;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.text.MessageFormat;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;

import catdata.Ctx;
import catdata.Null;
import catdata.Pair;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Schema;
import catdata.aql.Term;

public class InstExpCsv<Ty, En, Sym, Fk, Att, Gen> extends InstExpImport<Ty, En, Sym, Fk, Att, Gen, Map<En, List<String[]>>> {
	
	public final File f;
	
	public InstExpCsv(SchExp<Ty, En, Sym, Fk, Att> schema, List<Pair<LocStr, String>> map,
			 List<Pair<String, String>> options, String f) {
		super(schema, map, options);
		this.f = new File(f);
	}

	public final static String helpStr = "--------\nPossible problem: AQL IDs be unique among all entities and types; it is not possible to have, for example,"
			+ "\n" + "\n	0:Employee" + "\n	0:Department" + "\n"
			+ "\nPossible solution: Distinguish the IDs prior to import";
	
	@Override
	protected String getHelpStr() {
		return helpStr;
	}
	
	

	@Override
	public String toString() {
		return "import_csv " + f + " : " + schema + " {\n\t"
				+ Util.sep(map, " -> ", "\n\t") + "\n}";
	}

	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof InstExpCsv) && super.equals(obj);
	}


	/**
	 * Expects filenames in the map
	 */
	public static <Ty, En, Sym, Fk, Att> Map<En, List<String[]>> start2(Map<String, String> map, AqlOptions op, Schema<Ty, En, Sym, Fk, Att> sch, boolean omitCheck) throws Exception {
		Character sepChar = (Character) op.getOrDefault(AqlOption.csv_field_delim_char);
		Character quoteChar = (Character) op.getOrDefault(AqlOption.csv_quote_char);
		Character escapeChar = (Character) op.getOrDefault(AqlOption.csv_escape_char);
		
		final CSVParser parser =
				new CSVParserBuilder()
				.withSeparator(sepChar)
				.withQuoteChar(quoteChar)
				.withEscapeChar(escapeChar)
				.withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
				.build();
	
		Map<En, List<String[]>> ret = new HashMap<>();
		for (String k : map.keySet()) {
			if (!omitCheck) {
				if (!sch.ens.contains(k)) {
					throw new RuntimeException("Not an entity: " + k);
				}
			}
			FileReader fileReader;
			try {
				final File file = new File(map.get(k));
				fileReader = new FileReader(file);
			} catch (Exception ex) {
				System.err.println(MessageFormat.format(
															"not a valid file: {0} : {1} : in {2}",
															k, map.get(k), System.getProperty("user.dir")));
				continue;
			}
			final CSVReader reader =
					new CSVReaderBuilder(fileReader)
					.withCSVParser(parser)
					.withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
					.build();
			
			 List<String[]> rows = reader.readAll();
			 
			 fileReader.close();
			
			 ret.put((En)k, rows);
		}
		if (!omitCheck) {
			for (En en : sch.ens) {
				if (!ret.containsKey(en)) {
					ret.put(en, new LinkedList<>(Util.singList(Util.union(sch.attsFrom(en),sch.fksFrom(en)).toArray(new String[0]))));
				}
			}
		}
		return ret;
	}
	
	@Override
	protected Map<En, List<String[]>> start(Schema<Ty, En, Sym, Fk, Att> sch) throws Exception {
		Map<String, String> m = new HashMap<>();
		for (En en : sch.ens) {
			m.put((String)en, new File(f, en.toString() + "." + op.getOrDefault(AqlOption.csv_file_extension)).getAbsolutePath());
		}
		return start2(m, op, sch, false);
	}

	@Override
	protected void end(Map<En, List<String[]>> h) throws Exception {
		//clear h?
	}
	
	@Override
	protected void joinedEn(Map<En, List<String[]>> rows, En en, String s, Schema<Ty, En, Sym, Fk, Att> sch) throws Exception {
		boolean autoGenIds = (Boolean) op.getOrDefault(AqlOption.csv_generate_ids);
		if (rows.size() == 0) {
			throw new RuntimeException("No header in CSV file for " + en);
		}
		
		//index of each column name
		Ctx<String, Integer> m = new Ctx<>();
		for (int i = 0; i < rows.get(en).get(0).length; i++) {
			m.put(rows.get(en).get(0)[i], i);
		}
		
		Function<String, String> mediate = x -> map.containsKey((String)x) ? map.get((String) x) : x;
		//System.out.println(rows);
		int startId = 0;
		for (String[] row : rows.get(en).subList(1, rows.get(en).size())) {
		//	System.out.println("xx");
			Gen l0;
			
			String idCol = s == null ? (String) op.getOrDefault(AqlOption.id_column_name) : s;
			
			if (autoGenIds && !m.containsKey(idCol)) {
				l0 = (Gen) ("" + startId++);
			} else if (!autoGenIds && !m.containsKey(idCol)) {
				throw new RuntimeException("ID column " + idCol + " not found in headers " + m.keySet() + ". \n\nPossible solution: set csv_generate_ids=true to auto-generate IDs.\n\nPossible solution: rename the headers in the CSV file.\n\nPossible solution: add an ID column to the CSV file.");
			} else {
				l0 = (Gen) row[m.get(idCol)];
			}
			
			ens0.get(en).add(l0);
			
			for (Fk fk : sch.fksFrom(en)) {
				if (!fks0.containsKey(l0)) {
					fks0.put(l0, new Ctx<>());
				}
				Gen g = (Gen) row[m.get(mediate.apply((String) fk))];				
				fks0.get(l0).put(fk, g);
			}
			
			for (Att att : sch.attsFrom(en)) {
				if (!atts0.containsKey(l0)) {
					atts0.put(l0, new Ctx<>());
				}
				String o = row[m.get(mediate.apply((String) att))];
				Term<Ty, Void, Sym, Void, Void, Void, Null<?>> r 
				= objectToSk(sch, o, l0.toString(), att, tys0, extraRepr, true); 
				atts0.get(l0).put(att, r);
			}
		}
		
	}

	
/*	protected void joinedEn(Map<En, List<CSVRecord>> map, En en, String s, Schema<Ty, En, Sym, Fk, Att> sch) throws Exception {
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
				System.out.println("is " + o);
				Term<Ty, Void, Sym, Void, Void, Void, Null<?>> r 
				= objectToSk(sch, o, l0.toString(), att, tys0, extraRepr, true); 
				atts0.get(l0).put(att, r);
			}
		}
		
	} */

	//TODO aql shredded input format for CSV
	@Override
	protected void shreddedAtt(Map<En, List<String[]>> h, Att att, String s, Schema<Ty, En, Sym, Fk, Att> sch) throws Exception {
		throw new RuntimeException("Shredded input format not avaiable for CSV (if desired, please email info@catinf.com)");
	}

	@Override
	protected void shreddedFk(Map<En, List<String[]>> h, Fk fk, String s, Schema<Ty, En, Sym, Fk, Att> sch) throws Exception {
		throw new RuntimeException("Shredded input format not avaiable for CSV (if desired, please email info@catinf.com)");
	}

	@Override
	protected void shreddedEn(Map<En, List<String[]>> h, En en, String s, Schema<Ty, En, Sym, Fk, Att> sch) throws Exception {
		throw new RuntimeException("Shredded input format not avaiable for CSV (if desired, please email info@catinf.com)");
	}

	

}
