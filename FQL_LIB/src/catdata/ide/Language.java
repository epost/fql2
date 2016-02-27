package catdata.ide;

import java.util.List;

import catdata.fpql.XCodeEditor;
import catdata.fpql.XExamples;
import catdata.fpql.XOptions;
import catdata.fql.FqlExamples;
import catdata.fql.FqlOptions;
import catdata.fql.gui.FqlCodeEditor;
import catdata.fqlpp.FQLPPCodeEditor;
import catdata.fqlpp.FqlppExamples;
import catdata.fqlpp.FqlppOptions;
import catdata.mpl.MplCodeEditor;
import catdata.mpl.MplExamples;
import catdata.opl.OplCodeEditor;
import catdata.opl.OplExamples;
import catdata.opl.OplOptions;

public enum Language {

	FQL,
	FQLPP,
	FPQL,
	OPL,
	MPL;
	
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
		}
		throw new RuntimeException();
	}
	
	@SuppressWarnings("rawtypes")
	public CodeEditor createEditor(int untitled_count, String content) {
		switch (this) {
		case FPQL: return new XCodeEditor(untitled_count, content);
		case FQLPP: return new FQLPPCodeEditor(untitled_count, content);
		case OPL: return new OplCodeEditor(untitled_count, content);
		case FQL: return new FqlCodeEditor(untitled_count, content);
		case MPL: return new MplCodeEditor(untitled_count, content);
		}
		throw new RuntimeException(this.toString());
	}

	public Options getOptions() {
		switch (this) {
			case FPQL: return new XOptions();
			case FQLPP: return new FqlppOptions();
			case OPL: return new OplOptions();
			case FQL: return new FqlOptions();
			case MPL: return null;
		}
		throw new RuntimeException(this.toString());
	}
	
	public List<Example> getExamples() {
		switch (this) {
		case FPQL: return Examples.getExamples(XExamples.class);
		case FQLPP: return Examples.getExamples(FqlppExamples.class);
		case OPL: return Examples.getExamples(OplExamples.class);
		case FQL: return Examples.getExamples(FqlExamples.class);
		case MPL: return Examples.getExamples(MplExamples.class);
		}
		throw new RuntimeException(this.toString());
	}
}