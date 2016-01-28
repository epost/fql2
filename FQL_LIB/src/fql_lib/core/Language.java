package fql_lib.core;

import fql_lib.A.ACodeEditor;
import fql_lib.X.XCodeEditor;
import fql_lib.opl.OplCodeEditor;
import fql_lib.pp.FQLPPCodeEditor;

public enum Language {

	FQL,
	FQLPP,
	FPQL,
	FPQLPP,
	OPL;
	
	public static Language getDefault() {
		return FPQL;
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
		case FQL: return "";
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
	
	public CodeEditor createEditor(int untitled_count, String content) {
		switch (this) {
		case FPQL: return new XCodeEditor(untitled_count, content);
		case FQLPP: return new FQLPPCodeEditor(untitled_count, content);
		case OPL: return new OplCodeEditor(untitled_count, content);
		case FPQLPP: return new ACodeEditor(untitled_count, content);
		}
		throw new RuntimeException(this.toString());
	}
}
