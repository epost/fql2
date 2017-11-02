package catdata.aql.exp;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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

public class InstExpCsv<Ty, En, Sym, Fk, Att, Gen>
		extends InstExpImport<Ty, En, Sym, Fk, Att, Gen, Map<En, List<String[]>>, Pair<List<Pair<LocStr,String>>,List<Pair<String,String>>>> {

	public final File f;

	public InstExpCsv(SchExp<Ty, En, Sym, Fk, Att> schema, List<Pair<LocStr, Pair<List<Pair<LocStr, String>>, List<Pair<String, String>>>>> map,
			List<Pair<String, String>> options, String f) {
		super(schema, map, options);
		this.f = new File(f);
	}

	/*
	 * public final static String helpStr =
	 * "--------\nPossible problem: AQL IDs be unique among all entities and types; it is not possible to have, for example,"
	 * + "\n" + "\n	0:Employee" + "\n	0:Department" + "\n" +
	 * "\nPossible solution: Distinguish the IDs prior to import" +
	 * "\nPossible solution: set options prepend_entity_on_ids = true to obtain unique IDs"
	 * ;
	 */
	@Override
	protected String getHelpStr() {
		return "";
	}

	@Override
	public String toString() {
		return "import_csv " + f + " : " + schema + " {\n\t" + Util.sep(map, " -> ", "\n\t") + "\n}";
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof InstExpCsv) && super.equals(obj);
	}

	/**
	 * Expects filenames in the map
	 */
	public static <Ty, En, Sym, Fk, Att> Map<En, List<String[]>> start2(Map<String, String> map, AqlOptions op,
			Schema<Ty, En, Sym, Fk, Att> sch, boolean omitCheck) throws Exception {
		Character sepChar = (Character) op.getOrDefault(AqlOption.csv_field_delim_char);
		Character quoteChar = (Character) op.getOrDefault(AqlOption.csv_quote_char);
		Character escapeChar = (Character) op.getOrDefault(AqlOption.csv_escape_char);

		final CSVParser parser = new CSVParserBuilder().withSeparator(sepChar).withQuoteChar(quoteChar)
				.withEscapeChar(escapeChar).withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS).build();

		Map<En, List<String[]>> ret = new HashMap<>();
		for (String k : map.keySet()) {
			if (!omitCheck) {
				if (!sch.ens.contains(k)) {
					throw new RuntimeException("Not an entity: " + k);
				}
			}
			File file = new File(map.get(k));
			FileReader fileReader = new FileReader(file);

			final CSVReader reader = new CSVReaderBuilder(fileReader).withCSVParser(parser)
					.withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS).build();

			List<String[]> rows = reader.readAll();

			fileReader.close();

			ret.put((En) k, rows);
		}
		if (!omitCheck) {
			for (En en : sch.ens) {
				if (!ret.containsKey(en)) {
					ret.put(en, new LinkedList<>(
							Util.singList(Util.union(sch.attsFrom(en), sch.fksFrom(en)).toArray(new String[0]))));
				}
			}
		}
		return ret;
	}

	@Override
	protected Map<En, List<String[]>> start(Schema<Ty, En, Sym, Fk, Att> sch) throws Exception {
		Map<String, String> m = new HashMap<>();
		for (En en : sch.ens) {
			File file = new File(f, op.getOrDefault(AqlOption.csv_import_prefix) + en.toString() + "."
					+ op.getOrDefault(AqlOption.csv_file_extension));
			if (file.exists()) {
				m.put((String) en, file.getAbsolutePath());
			} else if (!(boolean) op.getOrDefault(AqlOption.csv_import_missing_is_empty)) {
				throw new RuntimeException("Missing file: " + file.getAbsolutePath()
						+ ". \n\nPossible options to consider: " + AqlOption.csv_import_missing_is_empty + " and "
						+ AqlOption.csv_import_prefix + " and " + AqlOption.csv_file_extension);
			}
		}
		return start2(m, op, sch, false);
	}

	@Override
	protected void end(Map<En, List<String[]>> h) throws Exception {
		// clear h?
	}

	@Override
	protected void joinedEn(Map<En, List<String[]>> rows, En en, Pair<List<Pair<LocStr,String>>,List<Pair<String,String>>> s, Schema<Ty, En, Sym, Fk, Att> sch)
			throws Exception {
		Map<String, String> inner;
		if (s == null) {
			inner = new HashMap<>(); 
		} else {
			inner = Util.toMapSafely(s.second);
		}
		boolean autoGenIds = (Boolean) op.getOrDefault(inner, AqlOption.csv_generate_ids);
		for (En en2 : rows.keySet()) {
			if (rows.get(en2).size() == 0) {
				throw new RuntimeException("No header in CSV file for " + en2);
			}
		}

		// index of each column name
		Ctx<String, Integer> m = new Ctx<>();
		for (int i = 0; i < rows.get(en).get(0).length; i++) {
			m.put(rows.get(en).get(0)[i], i);
		}
		boolean prepend = (boolean) op.getOrDefault(inner, AqlOption.csv_prepend_entity);
		String sep = (String) op.getOrDefault(inner, AqlOption.import_col_seperator);
		String pre = (String) op.getOrDefault(inner, AqlOption.csv_import_prefix);
		//System.out.println("prefix is " + pre);
		
		Map<String, String> map;
		if (s != null) {
			map = new Ctx<>(Util.toMapSafely(s.first)).map((x,y)->new Pair<>(x.str,y)).map;
		} else {
			map = new HashMap<>();
		}
		
		Function<String, String> mediate = x -> {
			if (map.containsKey(x)) {
				return map.get(x);
			}
			String z = map.containsKey(x) ? map.get(x) : x;
			if (prepend) {
				int i = x.indexOf(en + sep);
				if (i != 0) {
					return pre + z;
				}
				String temp = x.substring((en + sep).length());
				return pre + temp;
			}
			return pre + z;
		};
		int startId = 0;
		for (String[] row : rows.get(en).subList(1, rows.get(en).size())) {
			Gen l0;

			String idCol = map.containsKey(en) ? map.get(en) : 
				(String) op.getOrDefault(inner, AqlOption.id_column_name);

			if (autoGenIds && !m.containsKey(idCol)) {
				l0 = toGen(en, "" + startId++);
			} else if (!autoGenIds && !m.containsKey(idCol)) {
				throw new RuntimeException("On " + en + ", ID column " + idCol + " not found in headers " + m.keySet()
						+ ". \n\nPossible solution: set csv_generate_ids=true to auto-generate IDs.\n\nPossible solution: rename the headers in the CSV file.\n\nPossible solution: add an ID column to the CSV file.");
			} else {
				l0 = toGen(en, row[m.get(idCol)]);
			}

			ens0.get(en).add(l0);

			for (Fk fk : sch.fksFrom(en)) {
				if (!fks0.containsKey(l0)) {
					fks0.put(l0, new Ctx<>());
				}
				Gen g = toGen(sch.fks.get(fk).second, row[m.get(mediate.apply((String) fk))]);
				fks0.get(l0).put(fk, g);
			}

			for (Att att : sch.attsFrom(en)) {
				if (!atts0.containsKey(l0)) {
					atts0.put(l0, new Ctx<>());
				}
				String zz = mediate.apply((String) att);
				if (!m.containsKey(zz)) {
					throw new RuntimeException("No column " + att + " in file for " + en + " nor explicit mapping for "
							+ att + " given. Tried " + zz + " and options are " + m.keySet());
				}
				int z = m.get(zz);
				if (z >= row.length) {
					throw new RuntimeException("Cannot get index " + z + " from " + Arrays.toString(row));
				}
				String o = row[z];
				Term<Ty, Void, Sym, Void, Void, Void, Null<?>> r = objectToSk(sch, o, l0, att, tys0, extraRepr, true,
						nullOnErr);
				atts0.get(l0).put(att, r);
			}
		}

	}

	/*
	 * protected void joinedEn(Map<En, List<CSVRecord>> map, En en, String s,
	 * Schema<Ty, En, Sym, Fk, Att> sch) throws Exception { String idCol =
	 * (String) op.getOrDefault(AqlOption.id_column_name); for (CSVRecord row :
	 * map.get(en)) { Gen l0 = (Gen) row.get(idCol);
	 * 
	 * ens0.get(en).add(l0);
	 * 
	 * for (Fk fk : sch.fksFrom(en)) { if (!fks0.containsKey(l0)) { fks0.put(l0,
	 * new Ctx<>()); } Gen g = (Gen) row.get((String) fk); fks0.get(l0).put(fk,
	 * g); }
	 * 
	 * for (Att att : sch.attsFrom(en)) { if (!atts0.containsKey(l0)) {
	 * atts0.put(l0, new Ctx<>()); } Object o = row.get((String) att);
	 * //System.out.println("is " + o); Term<Ty, Void, Sym, Void, Void, Void,
	 * Null<?>> r = objectToSk(sch, o, l0.toString(), att, tys0, extraRepr,
	 * true); atts0.get(l0).put(att, r); } }
	 * 
	 * }
	 */

	// TODO aql shredded input format for CSV
	@Override
	protected void shreddedAtt(Map<En, List<String[]>> h, Att att, Pair<List<Pair<LocStr,String>>,List<Pair<String,String>>> s, Schema<Ty, En, Sym, Fk, Att> sch)
			throws Exception {
		throw new RuntimeException(
				"Shredded input format not avaiable for CSV (if desired, please email info@catinf.com)");
	}

	@Override
	protected void shreddedFk(Map<En, List<String[]>> h, Fk fk, Pair<List<Pair<LocStr,String>>,List<Pair<String,String>>> s, Schema<Ty, En, Sym, Fk, Att> sch)
			throws Exception {
		throw new RuntimeException(
				"Shredded input format not avaiable for CSV (if desired, please email info@catinf.com)");
	}

	@Override
	protected void shreddedEn(Map<En, List<String[]>> h, En en, Pair<List<Pair<LocStr,String>>,List<Pair<String,String>>> s, Schema<Ty, En, Sym, Fk, Att> sch)
			throws Exception {
		throw new RuntimeException(
				"Shredded input format not avaiable for CSV (if desired, please email info@catinf.com)");
	}

}
