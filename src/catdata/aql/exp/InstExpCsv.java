package catdata.aql.exp;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import catdata.Chc;
import catdata.Ctx;
import catdata.Pair;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Collage;
import catdata.aql.Eq;
import catdata.aql.Instance;
import catdata.aql.It;
import catdata.aql.Kind;
import catdata.aql.It.ID;
import catdata.aql.Schema;
import catdata.aql.Term;
import catdata.aql.fdm.InitialAlgebra;
import catdata.aql.fdm.LiteralInstance;


public class InstExpCsv<Ty,En,Sym,Fk,Att,Gen,Sk> 
	extends InstExp<Ty,En,Sym,Fk,Att,Gen,Sk,ID,Chc<Sk,Pair<ID,Att>>> {

	private final SchExp<Ty,En,Sym,Fk,Att> schema;
	
	private final List<String> imports;

	private final Map<String, String> options;
	
	private final String fileStr;
	
	@Override
	public long timeout() {
		return (Long) AqlOptions.getOrDefault(options, AqlOption.timeout);
	}	

	public InstExpCsv(SchExp<Ty, En, Sym, Fk, Att> schema, String file, List<String> imports, List<Pair<String, String>> options) {
		Util.assertNotNull(schema, imports, options, file);
		this.schema = schema;
		this.imports = imports;
		this.options = Util.toMapSafely(options);
        fileStr = file;
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
		if (gen == null) {
			throw new RuntimeException("Error: generators cannot be null");
		}
		return (Gen) gen;
	}
	
	@SuppressWarnings("unchecked")
	private static <Sk> Sk stringToSk0(String s) {
		if (s == null) {
			throw new RuntimeException("Error: enountered a null labelled null (how ironic)");
		}
		return (Sk) s;
	}


	@SuppressWarnings("unchecked")
	private static <Sym> Sym stringToSym(String s) {
		return (Sym) s;
	}

	@SuppressWarnings({ "unchecked" })
	public static <Ty,En,Sym,Fk,Att,Gen,Sk> Term<Ty,En,Sym,Fk,Att,Gen,Sk> stringToSk(Collection<Sk> sks, Ty t, Schema<Ty,En,Sym,Fk,Att> sch, String s) {
		int i = 0;
		Sym sym = null;
		if (sch.typeSide.syms.containsKey(stringToSym(s)) && sch.typeSide.syms.get(stringToSym(s)).first.isEmpty() && sch.typeSide.syms.get(stringToSym(s)).second.equals(t)) {
			sym = (Sym) s;
			i++;
		}
		Sk sk = null;
		if (sks.contains(stringToSk0(s))) {
			sk = (Sk) s; 
			i++;
		}
		Object o = null;
		if (sch.typeSide.js.java_tys.containsKey(t)) {
			o = s;
			i++;
		}
		if (i < 1) {
			throw new RuntimeException("Not a labelled null, java object, or constant symbol: " + s);
		} else if (i > 1) {
			throw new RuntimeException("Ambiguously a labelled null, java object, or constant symbol: " + s);			
		}
		if (sym != null) {
			return Term.Sym(sym, Collections.emptyList());
		} else if (sk != null) {
			return Term.Sk(sk);
		} else if (o != null) {
			return Term.Obj(sch.typeSide.js.parse(t, s), t);
		}
		throw new RuntimeException("Anomaly: please report");
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
		String pre = fileStr;
		if (!pre.endsWith("/")) {
            pre += "/";
		}
		AqlOptions op = new AqlOptions(options, null);
		String charset0 = (String) op.getOrDefault(AqlOption.csv_charset);
		Charset charset = Charset.forName(charset0);
		String idCol = (String) op.getOrDefault(AqlOption.id_column_name);
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
				parser.close();
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
						col.sks.put(stringToSk0(row.get(idCol)), ty);
					}
					parser.close();
				}
			}
			
			for (En en : sch.ens) {
				for (CSVRecord row : map.get(en)) {
					Term<Ty,En,Sym,Fk,Att,Gen,Sk> l0 = Term.Gen(stringToGen(row.get(idCol)));
					for (Fk fk : sch.fksFrom(en)) {
						Term<Ty,En,Sym,Fk,Att,Gen,Sk> l = Term.Fk(fk,  l0);
						Term<Ty,En,Sym,Fk,Att,Gen,Sk> r = Term.Gen(stringToGen(row.get(fkToString(fk))));	
						eqs0.add(new Pair<>(l, r));
						col.eqs.add(new Eq<>(new Ctx<>(), l, r));
					}
					for (Att att : sch.attsFrom(en)) {
						Term<Ty,En,Sym,Fk,Att,Gen,Sk> l = Term.Att(att, l0);
						if (row.get(attToString(att)) == null) {
							continue;
						}
						Term<Ty,En,Sym,Fk,Att,Gen,Sk> r = stringToSk(col.sks.keySet(), sch.atts.get(att).second, sch, row.get(attToString(att)));	
						eqs0.add(new Pair<>(l,r));
						col.eqs.add(new Eq<>(new Ctx<>(), l, r));
					}
				}
			}
		} catch (IOException exn) {
			throw new RuntimeException(exn.getMessage());
		} catch (Throwable exn) {
			exn.printStackTrace();
			throw new RuntimeException("Error: [text positions are relative to the record]: " + exn.getMessage() + "\n\n" + helpStr);
		}
		
		//TODO aql validate for collage
		AqlOptions strat = new AqlOptions(options, col);
		
		InitialAlgebra<Ty,En,Sym,Fk,Att,Gen,Sk,ID> 
		initial = new InitialAlgebra<>(strat, sch, col, new It(), Object::toString, Object::toString);
				 
		return new LiteralInstance<>(sch, col.gens.map, col.sks.map, eqs0, initial.dp(), initial); 
		//TODO aql switch to saturated prover for csv
	}
	

	private static final String helpStr = "Possible problem: AQL IDs be unique among all entities and types; it is not possible to have, for example,"
			+ "\n"
			+ "\n	0:Employee"
			+ "\n	0:Department"
			+ "\n"
			+ "\nPossible solution: Distinguish the IDs prior to import";

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

		if (op.options.containsKey(AqlOption.csv_null_string)) {
			format = format.withNullString( (String) op.getOrDefault(AqlOption.csv_null_string) );
		}
		return format;
	}

	@Override
	public String toString() {
		return "import_csv " + fileStr;
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
		int prime = 31;
		int result = 1;
		result = prime * result + ((fileStr == null) ? 0 : fileStr.hashCode());
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
		Boolean reload = (Boolean) op.getOrDefault(AqlOption.always_reload);
		if (reload) {
			return false;
		}
		
		if (fileStr == null) {
			if (other.fileStr != null)
				return false;
		} else if (!fileStr.equals(other.fileStr))
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
