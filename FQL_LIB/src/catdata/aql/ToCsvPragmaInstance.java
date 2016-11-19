package catdata.aql;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import catdata.Util;

public class ToCsvPragmaInstance<Ty,En,Sym,Fk,Att,Gen,Sk,X,Y> extends Pragma {
	
	public final String fil;
	
	//public final Instance<Ty,En,Sym,Fk,Att,Gen,Sk,X,Y> I;
	
	//public final CSVFormat format;
	//public final String idCol;
	
	public Map<En, String> ens = new HashMap<>();
	public Map<Ty, String> tys = new HashMap<>();

	public ToCsvPragmaInstance(Instance<Ty,En,Sym,Fk,Att,Gen,Sk,X,Y> I, String s, CSVFormat format, String idCol) {
		if (!s.endsWith("/")) {
			s = s + "/";
		}
		this.fil = s;
	//	this.format = format;
	//	this.I = I;
	//	this.idCol = idCol;
	
		try {
			for (En en : I.schema().ens) {
				StringBuffer sb = new StringBuffer();
				CSVPrinter printer = new CSVPrinter(sb, format);
				
				List<String> header = new LinkedList<>();
				header.add(idCol);
				for (Att att : I.schema().attsFrom(en)) {
					header.add(att.toString());
				}
				for (Fk fk : I.schema().fksFrom(en)) {
					header.add(fk.toString());
				}
				printer.printRecord(header);
				for (X x : I.algebra().en(en)) {
					List<String> row = new LinkedList<>();
					row.add(x.toString());
					for (Att att : I.schema().attsFrom(en)) {
						row.add(I.algebra().att(att, x).toString());
					}
					for (Fk fk : I.schema().fksFrom(en)) {
						row.add(I.algebra().fk(fk, x).toString());
					}
					printer.printRecord(row);
				}
				ens.put(en, sb.toString());
				printer.close();
			}
			for (Ty ty : I.schema().typeSide.tys) {
				StringBuffer sb = new StringBuffer();
				CSVPrinter printer = new CSVPrinter(sb, format);
				List<String> header = new LinkedList<>();
				header.add(idCol);
				printer.printRecord(header);
				for (Y y : I.algebra().talg().sks.keySet()) {
					List<String> row = new LinkedList<>();
					row.add(y.toString());
					printer.printRecord(row);
				}
				tys.put(ty, sb.toString());
				printer.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 			
	}
	
	private void delete() {
		File file = new File(fil);
		if (!file.exists()) {
			if (!file.mkdirs()) {
				throw new RuntimeException("Cannot create directory: " + file);
			} else {
				return;
			}
		}
		if (!file.isDirectory()) {
			if (!file.delete()) {
				throw new RuntimeException("Cannot delete file: " + file);
			}
			if (!file.mkdirs()) {
				throw new RuntimeException("Cannot create directory: " + file);
			} else {
				return;
			}
		}
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				if (!f.isDirectory()) {
					if (!f.delete()) {
						throw new RuntimeException("Cannot delete file: " + f);
					}
				} else {
					throw new RuntimeException("Cannot delete directory: " + f);
				}
			}
		}
		
		
	}

	
	
	@Override
	public void execute() {
		delete();
		try {
			for (En en : ens.keySet()) {
				FileWriter out = new FileWriter(fil + en + ".csv");
				out.write(ens.get(en));		
				out.close();
			}
			for (Ty ty : tys.keySet()) {
				FileWriter out = new FileWriter(fil + ty + ".csv");
				out.write(tys.get(ty));					
				out.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 			
	}
	
	@Override
	public String toString() {
		return Util.sep(ens, "\n\n", "\n----\n\n") + Util.sep(tys, "\n\n", "\n----\n\n");
	}
	
}
