package catdata.ide;

import java.util.List;

import catdata.aql.examples.AqlExamples;
import catdata.aql.gui.AqlCodeEditor;
import catdata.fpql.XCodeEditor;
import catdata.fpql.XExamples;
import catdata.fql.FqlExamples;
import catdata.fql.gui.FqlCodeEditor;
import catdata.fqlpp.FQLPPCodeEditor;
import catdata.fqlpp.FqlppExamples;
import catdata.mpl.MplCodeEditor;
import catdata.mpl.MplExamples;
import catdata.opl.OplCodeEditor;
import catdata.opl.OplExamples;

public enum Language {

	FQL,
	FQLPP,
	FPQL,
	OPL,
	MPL,
	AQL;
	
	public static Language getDefault() {
		return FQL;
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
		default:
			break;
		}
		throw new RuntimeException();
	}
	
	public String prefix() {
		switch (this) {
		case FQL: return "-";
		case FQLPP: return "+";
		case FPQL: return "P";
		case OPL: return "O";
		case MPL: return "M";
		case AQL: return "A";
		default:
			break;
		}
		throw new RuntimeException();
	}
	
	public String fileExtension() {
		switch (this) {
		case FQL: return "fql";
		case FQLPP: return "fqlpp";
		case FPQL: return "fpql";
		case OPL: return "opl";
		case MPL: return "mpl";
		case AQL: return "aql";
		default:
			throw new RuntimeException(); 
		}
		
	}
	
	@SuppressWarnings("rawtypes")
	public CodeEditor createEditor(String title, int id, String content) {
		switch (this) {
		case FPQL: return new XCodeEditor(title, id, content);
		case FQLPP: return new FQLPPCodeEditor(title, id, content);
		case OPL: return new OplCodeEditor(title, id, content);
		case FQL: return new FqlCodeEditor(title,id, content);
		case MPL: return new MplCodeEditor(title, id, content);
		case AQL: return new AqlCodeEditor(title, id, content);
		default:
			throw new RuntimeException(this.toString());
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
		case FPQL: return Examples.getExamples(XExamples.class);
		case FQLPP: return Examples.getExamples(FqlppExamples.class);
		case OPL: return Examples.getExamples(OplExamples.class);
		case FQL: return Examples.getExamples(FqlExamples.class);
		case MPL: return Examples.getExamples(MplExamples.class);
		case AQL: return Examples.getExamples(AqlExamples.class);
		default:
			throw new RuntimeException(this.toString());
		}
		
	}
}
