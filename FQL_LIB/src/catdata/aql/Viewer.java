package catdata.aql;

import javax.swing.JComponent;

import catdata.ide.CodeTextPanel;

public final class Viewer {

	public static String html(Object obj) {
		return obj.toString().replace("\n", "<br>").replace("\t", "&nbsp;");
	} 
	
	public static JComponent view(Kind kind, Object obj) {
		return new CodeTextPanel("", obj.toString());
		
		/* switch (kind) {
		case INSTANCE:
			return viewInstance((Instance<?,?,?,?,?,?,?>)obj);
		case MAPPING:
			return viewMapping((Mapping)obj);
		case PRAGMA:
			return viewPragma((Pragma)obj);
		case QUERY:
			return viewQuery((Query)obj);
		case SCHEMA:
			return viewSchema((Schema)obj);
		case TRANSFORM:
			return viewTransform((Transform)obj);
		case TYPESIDE:
			return viewTypeSide((TypeSide)obj);
		default:
			throw new RuntimeException();			
		} */
	}

	
	
}
