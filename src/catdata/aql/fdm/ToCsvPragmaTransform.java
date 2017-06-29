package catdata.aql.fdm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import catdata.aql.Pragma;
import catdata.aql.Transform;

public class ToCsvPragmaTransform<Ty,En,Sym,Att,Fk> extends Pragma {
	
	private final File file;
	
	private final String str;
	
	public <Gen1,Sk1,X1,Y1,Gen2,Sk2,X2,Y2> ToCsvPragmaTransform(Transform<Ty,En,Sym,Att,Fk,Gen1,Sk1,Gen2,Sk2,X1,Y1,X2,Y2> h, String s, CSVFormat format) {
        file = new File(s);
		
		try {
			StringBuffer sb = new StringBuffer();
			CSVPrinter printer = new CSVPrinter(sb, format);
			
			for (En en : h.src().schema().ens) {
				for (X1 x1 : h.src().algebra().en(en)) {
					List<String> row = new LinkedList<>();
					row.add(x1.toString());
					row.add(h.repr(x1).toString());
					printer.printRecord(row);
				}
			}
			for (Y1 y1 : h.src().algebra().talg().sks.keySet()) {
				List<String> row = new LinkedList<>();
				row.add(y1.toString());
				row.add(ToCsvPragmaInstance.print(h.dst().algebra().intoY(h.reprT(y1)), format.getNullString() != null));
				printer.printRecord(row);		
			}
			str = sb.toString();
			printer.close();				
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 			
	}
	
	private void delete() {
		if (!file.exists()) {
			return;
		}
		if (file.isDirectory()) {
			throw new RuntimeException("Cannot delete directory: " + file);
		}
		if (!file.delete()) {
			throw new RuntimeException("Cannot delete file: " + file);			
		}
	}

	@Override
	public void execute() {
		delete();
		try {
			FileWriter out = new FileWriter(file);
			out.write(str);		
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 		
	}
	
	@Override
	public String toString() {
		return str;
	}
	
}
