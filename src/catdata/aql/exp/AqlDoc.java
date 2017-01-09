package catdata.aql.exp;

import catdata.Program;
import catdata.Unit;
import catdata.aql.Comment;
import catdata.aql.Instance;
import catdata.aql.Kind;
import catdata.aql.Mapping;
import catdata.aql.Pragma;
import catdata.aql.Query;
import catdata.aql.Schema;
import catdata.aql.SemanticsVisitor;
import catdata.aql.Transform;
import catdata.aql.TypeSide;
import catdata.graph.DMG;

public final class AqlDoc implements SemanticsVisitor<String, Unit, RuntimeException> {
	
	public static String doc(AqlEnv env, Program<Exp<?>> prog, String name) {
		StringBuffer sb = new StringBuffer();
		//sb.append(header(name));
		AqlDoc doc = new AqlDoc();
		for (String k : prog.order) {
			Exp<?> e = prog.exps.get(k);
			if (e.kind() != Kind.COMMENT) {
				sb.append("<pre>\n");
				sb.append(e.kind() + " " + k + " = " + toHtml(e.toString()));
				sb.append("\n</pre>");
			}  
//			sb.append("\n<br />\n");
			sb.append(doc.visit(new Unit(), env.get(e.kind(), k)));
			sb.append("\n");
		}
//		sb.append(footer());
		sb.append("\n");
		return sb.toString();
	}
	
	private static String toHtml(String s) {
	//	s = s.replace("\\", "\\" + "\\"); //  \ --> \\
	//	s = s.replace("\"", "\\\""); // " --> \"
	//	s = s.replace("\n", "\n<br />"); // <LF> --> \n
	//	s = s.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;"); // <LF> --> \n
		return s;
	}


	@Override
	public <T, C> String visit(Unit arg, TypeSide<T, C> T)  {
		return "";
	}

	private static String black = "";
	private static String grey = "";
	
	private int fresh = 0;
	@Override
	public <Ty, En, Sym, Fk, Att> String visit(Unit arg, Schema<Ty, En, Sym, Fk, Att> S)  {
		String ret = "";
		ret += "\n<script>";
		ret += "\nvar graph" + fresh + " = new Springy.Graph();";
		
		for (En en : S.ens) {
			ret += "\ngraph" + fresh + ".addNodes('" + en + "');";
		}
		for (Ty ty : S.typeSide.tys) {
			ret += "\ngraph" + fresh + ".addNodes('" + ty + "');";
		}
		for (Fk fk : S.fks.keySet()) {
			ret += "\ngraph" + fresh + ".addEdges(['" + S.fks.get(fk).first + "', '" + S.fks.get(fk).second + "', {label: '" + fk + "'}]);";
		}
		for (Att att : S.atts.keySet()) {
			ret += "\ngraph" + fresh + ".addEdges(['" + S.atts.get(att).first + "', '" + S.atts.get(att).second + "', {label: '" + att + "'}]);";
		}
		/*graph.addNodes('Dennis', 'Michael', 'Jessica', 'Timothy', 'Barbara')
		graph.addNodes('Amphitryon', 'Alcmene', 'Iphicles', 'Heracles');

		graph.addEdges(
		  ['Dennis', 'Michael', {color: '#00A0B0', label: 'Foo bar'}],
		  ['Michael', 'Dennis', {color: '#6A4A3C'}],
		  ['Michael', 'Jessica', {color: '#CC333F'}],
		  ['Jessica', 'Barbara', {color: '#EB6841'}],
		  ['Michael', 'Timothy', {color: '#EDC951'}],
		  ['Amphitryon', 'Alcmene', {color: '#7DBE3C'}],
		  ['Alcmene', 'Amphitryon', {color: '#BE7D3C'}],
		  ['Amphitryon', 'Iphicles'],
		  ['Amphitryon', 'Heracles'],
		  ['Barbara', 'Timothy', {color: '#6A4A3C'}]
		); */
		ret += "\njQuery(function(){ var springy = jQuery('#canvas" + fresh + "').springy({graph: graph" + fresh + "});});";
		ret += "\n</script>";
		ret += "\n<div><canvas id=\"canvas" + fresh + "\" width=\"640\" height=\"320\" /></div>";
		fresh++;
		return ret;
	}

	@Override
	public <Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> String visit(Unit arg, Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> I)  {
		return "\n" + AqlInACan.toHtml(I);
	}

	@Override
	public <Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> String visit(Unit arg, Transform<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> h)  {
		return "";
	}

	@Override
	public String visit(Unit arg, Pragma P)  {
		return "";
	}

	@Override
	public String visit(Unit arg, Comment C)  {
		return C.toString();
	}

	@Override
	public <Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> String visit(Unit arg, Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> Q)  {
		return "";
	}

	@Override
	public <Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> String visit(Unit arg, Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> M)  {
		return "";
	}

	@Override
	public <N, e> String visit(Unit arg, DMG<N, e> G)  {
		return "";
	}


}