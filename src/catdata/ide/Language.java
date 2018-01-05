package catdata.ide;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import catdata.aql.gui.AqlCodeEditor;
import catdata.fpql.XCodeEditor;
import catdata.fql.gui.FqlCodeEditor;
import catdata.fqlpp.FQLPPCodeEditor;
import catdata.mpl.MplCodeEditor;
import catdata.opl.OplCodeEditor;

public enum Language {

	EASIK,
	SKETCH,
	FQL,
	FQLPP,
	FPQL,
	OPL,
	MPL,
	AQL;
	
	public static Language getDefault() {
		return AQL;
	}
	
	//non easik ones
	public static Language[] values0() {
		List<Language> l = new LinkedList<>(Arrays.asList(values()));
		//l.remove(EASIK);
		l.remove(SKETCH);
		return l.toArray(new Language[0]);
	}
	
	@Override
	public String toString() {
		switch (this) {
		case FQL: return "FQL";
		case FQLPP: return "FQL++";
		case FPQL: return "FPQL";
		case OPL: return "OPL";
		case MPL: return "MPL";
		case AQL: return "AQL";
		case EASIK: return "EASIK";
		case SKETCH: return "Sketch";
		default:
			break;
		}
		throw new RuntimeException("Anomaly - please report");
	}
	
	public String prefix() {
		switch (this) {
		case FQL: return "-";
		case FQLPP: return "+";
		case FPQL: return "P";
		case OPL: return "O";
		case MPL: return "M";
		case AQL: return " ";
		case EASIK: return "E";
		case SKETCH: return "S";
		default:
			break;
		}
		throw new RuntimeException("Anomaly - please report");
	}
	
	public String fileExtension() {
		switch (this) {
		case FQL: return "fql";
		case FQLPP: return "fqlpp";
		case FPQL: return "fpql";
		case OPL: return "opl";
		case MPL: return "mpl";
		case AQL: return "aql";
		case EASIK: return "easik";
		case SKETCH: return "sketch";
		default:
			throw new RuntimeException("Anomaly - please report"); 
		}
		
	}
	
	@SuppressWarnings({ "rawtypes" })
	public CodeEditor createEditor(String title, int id, String content) {
		switch (this) {
		case FPQL: return new XCodeEditor(title, id, content);
		case FQLPP: return new FQLPPCodeEditor(title, id, content);
		case OPL: return new OplCodeEditor(title, id, content);
		case FQL: return new FqlCodeEditor(title,id, content);
		case MPL: return new MplCodeEditor(title, id, content);
		case AQL: return new AqlCodeEditor(title, id, content);
		case EASIK: 
		case SKETCH: 
		default:
			throw new RuntimeException("Anomaly - please report");
		}
		
	}

/*	public Options getOptions() {
		switch (this) {
			case FPQL: return new XOptions();
			case FQLPP: return new FqlppOptions();
			case OPL: return new OplOptions();
			case FQL: return new FqlOptions();
			case MPL: return null;
			case AQL: return new AqlOptions();
		}
		throw new RuntimeException(this.toString());
	} */
	
	public List<Example> getExamples() {
		switch (this) {
		case FPQL: return Examples.getExamples(Language.FPQL);
		case FQLPP: return Examples.getExamples(Language.FQLPP);
		case OPL: return Examples.getExamples(Language.OPL);
		case FQL: return Examples.getExamples(Language.FQL);
		case MPL: return Examples.getExamples(Language.MPL);
		case AQL: return Examples.getExamples(Language.AQL);
		case EASIK: return Examples.getExamples(Language.EASIK);
		case SKETCH: return Examples.getExamples(Language.SKETCH);
		default:
			throw new RuntimeException("Anomaly - please report");
		}
		
	}
}
