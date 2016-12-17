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
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import catdata.Chc;
import catdata.Pair;
import catdata.Util;
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Instance;
import catdata.aql.Term;
import catdata.aql.Transform;
import catdata.aql.fdm.LiteralTransform;

public class TransExpCsv<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2> 
	extends TransExp<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2> {

	public final InstExp<Ty,En,Sym,Fk,Att,Gen1,Sk1,X1,Y1> src;
	public final InstExp<Ty,En,Sym,Fk,Att,Gen2,Sk2,X2,Y2> dst;
	
	public final List<String> imports;

	public final Map<String, String> options;
	
	public final String file;
	
	@Override
	public long timeout() {
		return (Long) AqlOptions.getOrDefault(options, AqlOption.timeout);
	}	

	public TransExpCsv(InstExp<Ty,En,Sym,Fk,Att,Gen1,Sk1,X1,Y1> src, InstExp<Ty,En,Sym,Fk,Att,Gen2,Sk2,X2,Y2> dst, String file, List<String> imports, List<Pair<String, String>> options) {
		Util.assertNotNull(src, dst, imports, options, file);
		this.src = src;
		this.dst = dst;
		this.imports = imports;
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
	@SuppressWarnings("unchecked")
	private Sk1 stringToSk1(String s) {
		return (Sk1) s;
	}
	@SuppressWarnings("unchecked")
	private Gen2 stringToGen2(String gen) {
		return (Gen2) gen;
	}
	
	
	
	@Override
	public Transform<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2> eval(AqlEnv env) {
		Instance<Ty, En, Sym, Fk, Att, Gen1, Sk1, X1, Y1> s = src.eval(env);
		Instance<Ty, En, Sym, Fk, Att, Gen2, Sk2, X2, Y2> t = dst.eval(env);
				
		Map<Gen1, Term<Void,En,Void,Fk,Void,Gen2,Void>> gens = new HashMap<>();
		Map<Sk1 , Term<Ty,En,Sym,Fk,Att,Gen2,Sk2>> sks = new HashMap<>();

		for (String k : imports) {
			@SuppressWarnings("unchecked")
			Transform<Ty,En,Sym,Fk,Att,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2> v = env.defs.trans.get(k);
			Util.putAllSafely(gens, v.gens().map);
			Util.putAllSafely(sks, v.sks().map);
		}
		AqlOptions op = new AqlOptions(options, null);
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
			
			for (CSVRecord row : parser) {
				String gen = row.get(0);
				
				Chc<Ty,En> required = null;
				
				if (s.gens().map.containsKey(gen) && s.sks().map.containsKey(gen)) {
					throw new RuntimeException("in transform for " + gen + ", " + gen + " is ambiguously an entity generator and labelled null");
				} else if (s.gens().map.containsKey(gen)) {
					required = Chc.inRight(s.gens().map.get(gen));
				} else if (s.sks().map.containsKey(gen)){ //mediates
					required = Chc.inLeft(s.sks().map.get(gen));				
				} else {
					throw new RuntimeException("in transform for " + gen + ", " + gen + " is not a source generator/labelled null");
				}
				
				String gen2 = row.get(1);
				
				if (required.left) {
					Util.putSafely(sks, stringToSk1(gen), InstExpCsv.stringToSk(t.sks().keySet(), required.l, s.schema(), gen2));				
				} else {
					Util.putSafely(gens, stringToGen1(gen), Term.Gen(stringToGen2(gen)));
				} 
			
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
		return "import_csv " + file + " : " + src + " -> " + dst;
	}


	@Override
	public Collection<Pair<String, Kind>> deps() {
		Set<Pair<String, Kind>> ret = new HashSet<>();
		ret.addAll(src.deps());
		ret.addAll(dst.deps());
		ret.addAll(imports.stream().map(x -> new Pair<>(x, Kind.TRANSFORM)).collect(Collectors.toList()));
		return ret;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dst == null) ? 0 : dst.hashCode());
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + ((imports == null) ? 0 : imports.hashCode());
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
		AqlOptions op = new AqlOptions(options, null);
		Boolean reload = (Boolean) op.getOrDefault(AqlOption.always_reload);
		if (reload) {
			return false;
		}
	
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
		if (src == null) {
			if (other.src != null)
				return false;
		} else if (!src.equals(other.src))
			return false;
		return true;
	}
	
}
