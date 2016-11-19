package catdata.aql.exp;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Collage;
import catdata.aql.Ctx;
import catdata.aql.Eq;
import catdata.aql.Instance;
import catdata.aql.It;
import catdata.aql.It.ID;
import catdata.aql.RawTerm;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.fdm.InitialAlgebra;
import catdata.aql.fdm.LiteralInstance;

//TODO aql arbitrary sql queries for jdbc import
public class InstExpCsv<Ty,En,Sym,Fk,Att,Gen,Sk> 
	extends InstExp<Ty,En,Sym,Fk,Att,Gen,Sk,ID,Chc<Sk,Pair<ID,Att>>> {

	public final SchExp<Ty,En,Sym,Fk,Att> schema;
	
	public final List<String> imports;

	public final Map<String, String> options;
	
	public final String file;

	public InstExpCsv(SchExp<Ty, En, Sym, Fk, Att> schema, String file, List<String> imports, List<Pair<String, String>> options) {
		Util.assertNotNull(schema, imports, options, file);
		this.schema = schema;
		this.imports = imports;
		this.options = Util.toMapSafely(options);
		this.file = file;
	}

	@Override
	public SchExp<Ty, En, Sym, Fk, Att> type(AqlTyping G) {
		return schema;
	}
	
	private String attToString(Att att) {
		return (String) att;
	}
	private String fkToString(Fk fk) {
		return (String) fk;
	}
	private String enToString(En en) {
		return (String) en;
	}
	private String tyToString(Ty ty) {
		return (String) ty;
	}
	@SuppressWarnings("unchecked")
	private Gen stringToGen(String gen) {
		return (Gen) gen;
	}
	@SuppressWarnings("unchecked")
	private Sk stringToSk(String s) {
		return (Sk) s;
	}
	
	
	@Override
	public Instance<Ty, En, Sym, Fk, Att, Gen, Sk, ID, Chc<Sk, Pair<ID, Att>>> eval(AqlEnv env) {
		Schema<Ty, En, Sym, Fk, Att> sch = schema.eval(env);
		Collage<Ty,En,Sym,Fk,Att,Gen,Sk> col = new Collage<>(sch.collage());
		
		Set<Pair<Term<Ty,En,Sym,Fk,Att,Gen,Sk>, Term<Ty,En,Sym,Fk,Att,Gen,Sk>>> eqs0 = new HashSet<>();

		for (String k : imports) {
			@SuppressWarnings("unchecked")
			Instance<Ty,En,Sym,Fk,Att,Gen,Sk,?,?> v = env.defs.insts.get(k);
			eqs0.addAll(v.eqs());
			col.gens.putAll(v.gens().map);
			col.sks.putAll(v.sks().map);
		}
		String pre = file;
		if (!pre.endsWith("/")) {
			pre = pre + "/";
		}
		AqlOptions op = new AqlOptions(options, null);
		String charset0 = (String) op.getOrDefault(AqlOption.csv_charset);
		Charset charset = Charset.forName(charset0);
		String idCol = (String) op.getOrDefault(AqlOption.csv_id_column_name);
		CSVFormat format = getFormat(op);
		format = format.withFirstRecordAsHeader();

		//TODO aql handling of empty fields
		for (En en : sch.ens) {
			File file = new File(pre + enToString(en) + ".csv");
			if (!file.exists()) {
				throw new RuntimeException("File does not exist: " + file.getAbsolutePath());
			}
		}
		try {
		
			Map<En, List<CSVRecord>> map = new HashMap<>();
			for (En en : sch.ens) {
				File file = new File(pre + enToString(en) + ".csv");
				CSVParser parser = CSVParser.parse(file, charset, format);
				List<CSVRecord> rows = parser.getRecords();
				for (CSVRecord row : rows) {
					col.gens.put(stringToGen(row.get(idCol)), en);
				}
				map.put(en, rows);
			}
			for (Ty ty : sch.typeSide.tys) {
				File file = new File(pre + tyToString(ty) + ".csv");
				if (file.exists()) {
					CSVParser parser = CSVParser.parse(file, charset, format);
					for (CSVRecord row : parser) {
						col.sks.put(stringToSk(row.get(idCol)), ty);
					}
				}
			}
			
			for (En en : sch.ens) {
				for (CSVRecord row : map.get(en)) {
					RawTerm l0 = new RawTerm(row.get(idCol), new LinkedList<>());
					for (Fk fk : sch.fksFrom(en)) {
						RawTerm l = new RawTerm(fkToString(fk), Util.singList(l0));
						RawTerm r = AqlParser.parseTermNoCtx(row.get(fkToString(fk)));
						Triple<Ctx<String,Chc<Ty,En>>,Term<Ty,En,Sym,Fk,Att,Gen,Sk>,Term<Ty,En,Sym,Fk,Att,Gen,Sk>> 
						eq0 = RawTerm.infer1(new HashMap<>(), l, r, col);
	
						eqs0.add(new Pair<>(eq0.second, eq0.third));
						col.eqs.add(new Eq<>(new Ctx<>(), eq0.second, eq0.third));
					}
					for (Att att : sch.attsFrom(en)) {
						RawTerm l = new RawTerm(attToString(att), Util.singList(l0));

						try {
							
						RawTerm r = AqlParser.parseTermNoCtx(row.get(attToString(att)));
						Triple<Ctx<String,Chc<Ty,En>>,Term<Ty,En,Sym,Fk,Att,Gen,Sk>,Term<Ty,En,Sym,Fk,Att,Gen,Sk>> 
						eq0 = RawTerm.infer1(new HashMap<>(), l, r, col);
	
						eqs0.add(new Pair<>(eq0.second, eq0.third));
						col.eqs.add(new Eq<>(new Ctx<>(), eq0.second, eq0.third));
						} catch (Throwable thr) {
							throw new RuntimeException("Error in attribute " + att + " of entity " + sch.atts.get(att).first + " of row " + row + ": " + thr.getMessage());
						}
					}
					
				}
			}
		} catch (IOException exn) {
			throw new RuntimeException(exn.getMessage());
		} catch (Throwable exn) {
			exn.printStackTrace();
			throw new RuntimeException("Parser error in underlying CSV data [text positions are relative to the record]: " + exn.getMessage());
		}
		
		//TODO aql validate for collage
		AqlOptions strat = new AqlOptions(options, col);
		
		InitialAlgebra<Ty,En,Sym,Fk,Att,Gen,Sk,ID> 
		initial = new InitialAlgebra<>(strat, sch, col, new It(), x -> x.toString(), x -> x.toString());
				 
		return new LiteralInstance<>(sch, col.gens.map, col.sks.map, eqs0, initial.dp(), initial); 
		//TODO aql switch to saturated prover for csv
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
		//escape isn't enabled by default
		format = format.withEscape((Character) op.getOrDefault(AqlOption.csv_escape_char));

		return format;
	}

	@Override
	public String toString() {
		return "import_csv " + file;
	}


	@Override
	public Collection<Pair<String, Kind>> deps() {
		Set<Pair<String, Kind>> ret = new HashSet<>();
		ret.addAll(schema.deps());
		ret.addAll(imports.stream().map(x -> new Pair<>(x, Kind.INSTANCE)).collect(Collectors.toList()));
		return ret;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + ((imports == null) ? 0 : imports.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		result = prime * result + ((schema == null) ? 0 : schema.hashCode());
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
		
		InstExpCsv<?, ?, ?, ?, ?, ?, ?> other = (InstExpCsv<?, ?, ?, ?, ?, ?, ?>) obj;
		AqlOptions op = new AqlOptions(options, null);
		Boolean reload = (Boolean) op.getOrDefault(AqlOption.csv_always_reload);
		if (reload) {
			return false;
		}
		
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (imports == null) {
			if (other.imports != null)
				return false;
		} else if (!imports.equals(other.imports))
			return false;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		if (schema == null) {
			if (other.schema != null)
				return false;
		} else if (!schema.equals(other.schema))
			return false;
		return true;
	}
	
	
	
}
