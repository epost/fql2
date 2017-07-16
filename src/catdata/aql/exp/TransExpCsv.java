package catdata.aql.exp;

import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;

import catdata.Pair;
import catdata.Util;
import catdata.aql.Schema;
import catdata.aql.Term;

public class TransExpCsv<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2> 
	extends TransExpImport<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2,Map<En, List<CSVRecord>>> {

	public TransExpCsv(InstExp<Ty,En,Sym,Fk,Att,Gen1,Sk1,X1,Y1> src, InstExp<Ty,En,Sym,Fk,Att,Gen2,Sk2,X2,Y2> dst, List<Pair<LocStr, String>> files, List<Pair<String, String>> options) {
		super(src, dst, files, options);
	}

	@Override //TODO aql
	public String toString() {
		String s = "";
		if (!options.isEmpty()) {
			s = "options" + Util.sep(options, "\n\t\t", " = ");
		}
		return "import_csv " + src + " -> " + dst + " {\n\t" + Util.sep(map, " -> ", "\n\t") + s + "\n}";

	}



	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof TransExpCsv) && super.equals(obj);
	}

	@Override
	protected String getHelpStr() {
		return InstExpCsv.helpStr;
	}

	@Override
	protected void stop(Map<En, List<CSVRecord>> h) throws Exception {
	}

	@Override
	protected void processEn(En en, Schema<Ty, En, Sym, Fk, Att> sch, Map<En, List<CSVRecord>> h, String q) throws Exception {
		for (CSVRecord row : h.get(en)) {
			String gen = row.get(0);
			String gen2 = row.get(1);
			if (gen == null) {
				throw new RuntimeException("Encountered a NULL generator in column 1 of " + en);
			}
			if (gen2 == null) {
				throw new RuntimeException("Encountered a NULL generator in column 2 of " + en);
			}
			gens.put((Gen1) gen, Term.Gen((Gen2) gen2));
		}
	}

	@Override
	protected Map<En, List<CSVRecord>> start(Schema<Ty, En, Sym, Fk, Att> sch) throws Exception {
		Map<En, List<CSVRecord>> ret = InstExpCsv.start(false, map, op, sch);
		return ret;
	}
	
}
