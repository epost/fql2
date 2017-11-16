package catdata.aql.exp;

import com.github.rjeschke.txtmark.Processor;

import catdata.Program;
import catdata.Unit;
import catdata.aql.ColimitSchema;
import catdata.aql.Comment;
import catdata.aql.Constraints;
import catdata.aql.Graph;
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
	
	private final AqlEnv env ;
	public AqlDoc(AqlEnv env) {
		this.env =env;
	}

	public static String doc(AqlEnv env, Program<Exp<?>> prog) {
		if (prog == null || env == null) {
			throw new RuntimeException("Must compile before using HTML output");
		}
		StringBuffer sb = new StringBuffer();
		AqlDoc doc = new AqlDoc(env);
		for (String k : prog.order) {
			Exp<?> e = prog.exps.get(k);
			if (e.kind() != Kind.COMMENT) {
				sb.append("<pre>\n");
				sb.append(e.kind() + " " + k + " = " + e.toString());
				sb.append("\n</pre>");
			}  
			if (!env.defs.keySet().contains(k)) {
				continue;
			}
			sb.append(doc.visit(new Unit(), env.get(e.kind(), k)));
			sb.append("\n");
		
		}
		sb.append("\n");
		return sb.toString();
	}

	@Override
	public <T, C> String visit(Unit arg, TypeSide<T, C> T)  {
		return "";
	}

//	private static String black = "";
//	private static String grey = "";
	
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
	

	@Override
	public <Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> String visit(Unit arg, Instance<Ty, En, Sym, Fk, Att, Gen, Sk, X, Y> I)  {
		return "\n" + AqlInACan.toHtml(env, (Instance<catdata.aql.exp.TyExpRaw.Ty, catdata.aql.exp.SchExpRaw.En, catdata.aql.exp.TyExpRaw.Sym, catdata.aql.exp.SchExpRaw.Fk, catdata.aql.exp.SchExpRaw.Att, catdata.aql.exp.InstExpRaw.Gen, catdata.aql.exp.InstExpRaw.Sk, X, Y>) I);
	}

	@Override
	public <Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> String visit(Unit arg, Transform<Ty, En, Sym, Fk, Att, Gen1, Sk1, Gen2, Sk2, X1, Y1, X2, Y2> h)  {
		return "";
	}

	@Override
	public String visit(Unit arg, Pragma P)  {
		return "\n<pre>" + P.toString() + "</pre>";
	}

	@Override
	public String visit(Unit arg, Comment C)  {
		if (C.isMarkdown) {
			String result = Processor.process(C.comment);
			return result;	
		} else {
			return C.toString();
		}
	}

	@Override
	public <Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> String visit(Unit arg, Query<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> Q)  {
		return ""; //TODO aql print frozen instances?
	}

	@Override
	public <Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> String visit(Unit arg, Mapping<Ty, En1, Sym, Fk1, Att1, En2, Fk2, Att2> M)  {
		return "";
	}

	@Override
	public <N, e> String visit(Unit arg, Graph<N, e> G0)  {
		DMG<N,e> G = G0.dmg;
		String ret = "";
		ret += "\n<script>";
		ret += "\nvar graph" + fresh + " = new Springy.Graph();";
		
		for (N en : G.nodes) {
			ret += "\ngraph" + fresh + ".addNodes('" + en + "');";
		}
		for (e fk : G.edges.keySet()) {
			ret += "\ngraph" + fresh + ".addEdges(['" + G.edges.get(fk).first + "', '" + G.edges.get(fk).second + "', {label: '" + fk + "'}]);";
		}
		
		ret += "\njQuery(function(){ var springy = jQuery('#canvas" + fresh + "').springy({graph: graph" + fresh + "});});";
		ret += "\n</script>";
		ret += "\n<div><canvas id=\"canvas" + fresh + "\" width=\"640\" height=\"320\" /></div>";
		fresh++;
		return ret;
	}
	
	private int fresh = 0;
	@Override
	public <Ty, En, Sym, Fk, Att> String visit(Unit arg, Schema<Ty, En, Sym, Fk, Att> S)  {
		/* String ret = "";
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
		
		ret += "\njQuery(function(){ var springy = jQuery('#canvas" + fresh + "').springy({graph: graph" + fresh + "});});";
		ret += "\n</script>";
		ret += "\n<div><canvas id=\"canvas" + fresh + "\" width=\"640\" height=\"320\" /></div>";
		fresh++;
		return ret; */
		return "";
	}

	@Override
	public <N> String visit(Unit arg, ColimitSchema<N> S) throws RuntimeException {
		return "";
	}

	@Override
	public <Ty, En, Sym, Fk, Att> String visit(Unit arg, Constraints<Ty, En, Sym, Fk, Att> S) throws RuntimeException {
		return "";
	}



}