package catdata.ide;

import java.util.LinkedList;
import java.util.List;

import catdata.fpql.XCodeEditor;
import catdata.fpql.XExamples;
import catdata.fpql.XOptions;
import catdata.fpqlpp.ACodeEditor;
import catdata.fql.FqlOptions;
import catdata.fql.FqlExamples;
import catdata.fql.gui.FqlCodeEditor;
import catdata.fqlpp.FQLPPCodeEditor;
import catdata.fqlpp.FqlppExamples;
import catdata.fqlpp.FqlppOptions;
import catdata.opl.OplCodeEditor;
import catdata.opl.OplExamples;
import catdata.opl.OplOptions;

public enum Language {

	FQL,
	FQLPP,
	FPQL,
	FPQLPP,
	OPL;
	
	public static Language getDefault() {
		return FQL;
	}
	
	@Override
	public String toString() {
		switch (this) {
		case FQL: return "FQL";
		case FQLPP: return "FQL++";
		case FPQL: return "FPQL";
		case FPQLPP: return "FPQL++";
		case OPL: return "OPL";
		}
		throw new RuntimeException();
	}
	
	public String prefix() {
		switch (this) {
		case FQL: return "-";
		case FQLPP: return "+";
		case FPQL: return "P";
		case FPQLPP: return "A";
		case OPL: return "O";
		}
		throw new RuntimeException();
	}
	
	public String fileExtension() {
		switch (this) {
		case FQL: return "fql";
		case FQLPP: return "fqlpp";
		case FPQL: return "fpql";
		case FPQLPP: return "fpqlpp";
		case OPL: return "opl";
		}
		throw new RuntimeException();
	}
	
	@SuppressWarnings("rawtypes")
	public CodeEditor createEditor(int untitled_count, String content) {
		switch (this) {
		case FPQL: return new XCodeEditor(untitled_count, content);
		case FQLPP: return new FQLPPCodeEditor(untitled_count, content);
		case OPL: return new OplCodeEditor(untitled_count, content);
		case FPQLPP: return new ACodeEditor(untitled_count, content);
		case FQL: return new FqlCodeEditor(untitled_count, content);
		}
		throw new RuntimeException(this.toString());
	}

	public Options getOptions() {
		switch (this) {
			case FPQL: return new XOptions();
			case FQLPP: return new FqlppOptions();
			case OPL: return new OplOptions();
			case FPQLPP: throw new RuntimeException();
			case FQL: return new FqlOptions();
		}
		throw new RuntimeException(this.toString());
	}
	
	public List<Example> getExamples() {
		switch (this) {
		case FPQL: return Examples.getExamples(XExamples.class);
		case FQLPP: return Examples.getExamples(FqlppExamples.class);
		case OPL: return Examples.getExamples(OplExamples.class);
		case FPQLPP: return new LinkedList<>();
		case FQL: return Examples.getExamples(FqlExamples.class);
		}
		throw new RuntimeException(this.toString());
	}
}
