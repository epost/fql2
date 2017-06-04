package catdata.aql.exp;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import catdata.Pair;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.ImportAlgebra;
import catdata.aql.Instance;
import catdata.aql.Kind;
import catdata.aql.Term;
import catdata.aql.Transform;
import catdata.aql.fdm.LiteralTransform;
import catdata.aql.fdm.SaturatedInstance;

public class TransExpCsv<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2> 
	extends TransExp<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2> {

	private final InstExp<Ty,En,Sym,Fk,Att,Gen1,Sk1,X1,Y1> src;
	private final InstExp<Ty,En,Sym,Fk,Att,Gen2,Sk2,X2,Y2> dst;
	
	//private final List<String> imports;

	private final Map<String, String> options;
	
	private final String file;
	
	@Override
	public Map<String, String> options() {
		return options;
	}

	public TransExpCsv(InstExp<Ty,En,Sym,Fk,Att,Gen1,Sk1,X1,Y1> src, InstExp<Ty,En,Sym,Fk,Att,Gen2,Sk2,X2,Y2> dst, String file, /* List<String> imports ,*/ List<Pair<String, String>> options) {
		Util.assertNotNull(src, dst, options, file);
		this.src = src;
		this.dst = dst;
		//this.imports = imports;
		this.options = Util.toMapSafely(options);
		this.file = file;
	}


	@Override
	public Pair<InstExp<Ty, En, Sym, Fk, Att, Gen1, Sk1, X1, Y1>, InstExp<Ty, En, Sym, Fk, Att, Gen2, Sk2, X2, Y2>> type(AqlTyping G) {
		SchExp<Ty, En, Sym, Fk, Att> s = src.type(G);
		SchExp<Ty, En, Sym, Fk, Att> t = dst.type(G);
		if (!s.equals(t)) { //TODO aql schema equality
			throw new RuntimeException("Source instance of transform has schema\n" + s + " \n\n but target instance has schema\n" + t);
		}
		return new Pair<>(src, dst);
	}

	@SuppressWarnings("unchecked")
	private Gen1 stringToGen1(String gen) {
		return (Gen1) gen;
	}
///	@SuppressWarnings("unchecked")
	//private Sk1 stringToSk1(String s) {
		//return (Sk1) s;
	//}
	@SuppressWarnings("unchecked")
	private Gen2 stringToGen2(String gen) {
		return (Gen2) gen;
	}
	
	private static boolean cameFromImport(Instance<?, ?, ?, ?, ?, ?, ?, ?, ?> I) {
		if (!(I instanceof SaturatedInstance)) {
			return false;
		}
		SaturatedInstance<?, ?, ?, ?, ?, ?, ?, ?, ?> J = (SaturatedInstance<?, ?, ?, ?, ?, ?, ?, ?, ?>) I;
		return J.alg instanceof ImportAlgebra;
	}
	
	@Override
	public Transform<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2> eval(AqlEnv env) {
		Instance<Ty, En, Sym, Fk, Att, Gen1, Sk1, X1, Y1> s = src.eval(env);
		Instance<Ty, En, Sym, Fk, Att, Gen2, Sk2, X2, Y2> t = dst.eval(env);

		if (!(cameFromImport(s) || !(cameFromImport(t)))) {
			throw new RuntimeException("Can only import CSV transforms between CSV instances");
		}
		
		Map<Gen1, Term<Void,En,Void,Fk,Void,Gen2,Void>> gens = new HashMap<>();
		Map<Sk1 , Term<Ty,En,Sym,Fk,Att,Gen2,Sk2>> sks = new HashMap<>();

		AqlOptions op = new AqlOptions(options, null, env.defaults);
		boolean dontValidateEqs = (Boolean) op.getOrDefault(AqlOption.dont_validate_unsafe);
		String charset0 = (String) op.getOrDefault(AqlOption.csv_charset);
		Charset charset = Charset.forName(charset0);		
		CSVFormat format = InstExpCsv.getFormat(op);
	
		try {
			File fil = new File(file);
			if (!fil.exists()) {
				throw new RuntimeException("File not found: " + fil.getAbsolutePath());
			}
			CSVParser parser = CSVParser.parse(fil, charset, format);
			for (Sk1 sk : s.sks().keySet()) {
				@SuppressWarnings("unchecked")
				Sk2 sk2 = (Sk2) sk;
				sks.put(sk, Term.Sk(sk2)); //map Null@Ty to Null@Ty
			}
			
			for (CSVRecord row : parser) {
				String gen = row.get(0);
				
				if (!s.gens().map.containsKey(gen)) {
					throw new RuntimeException("in transform for " + gen + ", " + gen + " is not a source generator");
				}
				
				String gen2 = row.get(1);
				
				if (!t.gens().map.containsKey(gen2)) {
					throw new RuntimeException("in transform for " + gen + ", " + gen2 + " is not a target generator");
				}
				Util.putSafely(gens, stringToGen1(gen), Term.Gen(stringToGen2(gen2)));
			}
		} catch (IOException exn) {
			throw new RuntimeException(exn.getMessage());
		} catch (RuntimeException exn) {
			exn.printStackTrace();
			throw new RuntimeException("Parser error in underlying CSV data [text positions are relative to the record]: " + exn.getMessage());
		}
				 
		return new LiteralTransform<>(gens, sks, s, t, dontValidateEqs); 
	
		//TODO aql CSV instances with skolems.
	}

	

	@Override
	public String toString() {
		String s = "";
		if (!options.isEmpty()) {
			s = " {\n\toptions" + Util.sep(options, "\n\t\t", " = ") + "\n}";
		}
		return "import_csv " + file + " : " + src + " -> " + dst + s;
	}


	@Override
	public Collection<Pair<String, Kind>> deps() {
		Set<Pair<String, Kind>> ret = new HashSet<>();
		ret.addAll(src.deps());
		ret.addAll(dst.deps());
//		ret.addAll(imports.stream().map(x -> new Pair<>(x, Kind.TRANSFORM)).collect(Collectors.toList()));
		return ret;
	}


	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((dst == null) ? 0 : dst.hashCode());
		result = prime * result + ((file == null) ? 0 : file.hashCode());
	//	result = prime * result + ((imports == null) ? 0 : imports.hashCode());
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		result = prime * result + ((src == null) ? 0 : src.hashCode());
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
		
		TransExpCsv<?,?,?,?,?,?,?, ?, ?, ?, ?, ?, ?> other = (TransExpCsv<?,?,?,?,?,?,?, ?, ?, ?, ?, ?, ?>) obj;
		
		if (dst == null) {
			if (other.dst != null)
				return false;
		} else if (!dst.equals(other.dst))
			return false;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		if (src == null) {
			if (other.src != null)
				return false;
		} else if (!src.equals(other.src))
			return false;
		return true;
	}
	
}
