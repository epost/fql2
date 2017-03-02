package catdata.aql;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import catdata.Pair;
import catdata.Util;
import catdata.aql.exp.EdsExp;
import catdata.aql.exp.EdsExp.EdExpRaw;
import catdata.aql.exp.EdsExp.EdsExpRaw;
import catdata.aql.exp.SchExp;
import catdata.aql.exp.SchExp.SchExpVar;
import catdata.aql.exp.SchExpRaw;
import catdata.aql.exp.TyExp.TyExpVar;
import catdata.aql.exp.TyExpRaw;

public class EasikAql {
	
	private static String safe(String s) {
		return s.replace(" ", "_").replace("-", "_");
	}

	private static Pair<SchExp<?, ?, ?, ?, ?>,List<Pair<String,EdsExpRaw>>> translate1(Node sketch, Set<String> used, Set<String> warnings, String sname) {
		List<String> ens = new LinkedList<>();
		List<Pair<String, Pair<String, String>>> atts = new LinkedList<>();
		List<Pair<String, Pair<String, String>>> fks = new LinkedList<>();
		List<Pair<List<String>, List<String>>> eqs = new LinkedList<>();
		// there shouldn't be observation equations in easik
		// List<Quad<String, String, RawTerm, RawTerm>> eqs2 = new
		// LinkedList<>();
		List<Pair<String,EdsExpRaw>> edsExps = new LinkedList<>();

		NodeList l = sketch.getChildNodes();
		for (int temp = 0; temp < l.getLength(); temp++) {
			Node n = l.item(temp);
			NodeList j = n.getChildNodes();
			for (int temp2 = 0; temp2 < j.getLength(); temp2++) {
				Node m = j.item(temp2);

				if (m.getNodeName().equals("entity")) {
					String nodeName = safe(m.getAttributes().getNamedItem("name").getTextContent());
					ens.add(nodeName);
					NodeList k = m.getChildNodes();
					for (int temp3 = 0; temp3 < k.getLength(); temp3++) {
						Node w = k.item(temp3);
						if (w.getNodeName().equals("attribute")) {
							String attName = safe(w.getAttributes().getNamedItem("name").getTextContent());
							String tyName = w.getAttributes().getNamedItem("attributeTypeClass").getTextContent();
							used.add(tyName);
							atts.add(new Pair<>(nodeName + "_" + attName.replace(" ", "_"), new Pair<>(nodeName, easikTypeToString(tyName))));
						}
					}
				} else if (m.getNodeName().equals("edge")) {
					String ename = safe(m.getAttributes().getNamedItem("id").getTextContent());
					String esrc = safe(m.getAttributes().getNamedItem("source").getTextContent());
					fks.add(new Pair<>(ename, new Pair<>(esrc, safe(m.getAttributes().getNamedItem("target").getTextContent()))));
					if (m.getAttributes().getNamedItem("type").getTextContent().equals("injective")) {
						List<EdExpRaw> eds = new LinkedList<>();
						List<Pair<String, String>> As = new LinkedList<>();
						As.add(new Pair<>("x", esrc));
						As.add(new Pair<>("y", esrc));
						List<Pair<RawTerm, RawTerm>> Aeqs = new LinkedList<>();
						Aeqs.add(new Pair<>(new RawTerm(ename, Util.singList(new RawTerm("x"))), new RawTerm(ename, Util.singList(new RawTerm("y")))));
						List<Pair<String, String>> Es = new LinkedList<>();
						List<Pair<RawTerm, RawTerm>> Eeqs = new LinkedList<>();
						Eeqs.add(new Pair<>(new RawTerm("x"), new RawTerm("y")));
						EdExpRaw ed = new EdExpRaw(As, Aeqs, Es, Eeqs, false);
						eds.add(ed);
						edsExps.add(new Pair<>("injective", new EdsExpRaw(new SchExpVar(sname), new LinkedList<>(), eds)));
					} if (m.getAttributes().getNamedItem("type").getTextContent().equals("partial")) {
						warnings.add("Not exported - partial edges.  Their AQL semantics is unclear");
					}
				} else if (m.getNodeName().equals("uniqueKey")) {
					String esrc = safe(m.getAttributes().getNamedItem("noderef").getTextContent());
					List<String> atts0 = new LinkedList<>();
					for (int w = 0; w < m.getChildNodes().getLength(); w++) {
						Node node = m.getChildNodes().item(w);
						if (!node.getNodeName().equals("attref")) {
							continue;
						}
						String att = safe(node.getAttributes().getNamedItem("name").getTextContent());
						atts0.add(att);
					}
					List<EdExpRaw> eds = new LinkedList<>();
					List<Pair<String, String>> As = new LinkedList<>();
					As.add(new Pair<>("x", esrc));
					As.add(new Pair<>("y", esrc));
					List<Pair<RawTerm, RawTerm>> Aeqs = new LinkedList<>();
					for (String att : atts0) {
						Aeqs.add(new Pair<>(new RawTerm(esrc + "_" + att, Util.singList(new RawTerm("x"))), new RawTerm(esrc + "_" + att, Util.singList(new RawTerm("y")))));
					}
					List<Pair<String, String>> Es = new LinkedList<>();
					List<Pair<RawTerm, RawTerm>> Eeqs = new LinkedList<>();
					Eeqs.add(new Pair<>(new RawTerm("x"), new RawTerm("y")));
					EdExpRaw ed = new EdExpRaw(As, Aeqs, Es, Eeqs, false);
					eds.add(ed);
					edsExps.add(new Pair<>("key", new EdsExpRaw(new SchExpVar(sname), new LinkedList<>(), eds)));
				}
				
				else if (m.getNodeName().equals("commutativediagram")) {
					NodeList k = m.getChildNodes();
					Node w1 = null;
					Node w2 = null;
					for (int temp4 = 0; temp4 < k.getLength(); temp4++) {
						Node wX = k.item(temp4);
						if (wX.getNodeName().equals("path") && w1 == null) {
							w1 = wX;
						} else if (wX.getNodeName().equals("path") && w2 == null) {
							w2 = wX;
						}
					}
					if (w1 == null || w2 == null) {
						throw new RuntimeException("Easik to AQL internal error");
					}
					String cod1 = safe(w1.getAttributes().getNamedItem("domain").getTextContent());
					String cod2 = safe(w2.getAttributes().getNamedItem("domain").getTextContent());
					List<String> lhs = new LinkedList<>();
					List<String> rhs = new LinkedList<>();
					lhs.add(cod1);
					rhs.add(cod2);

					NodeList lhsX = w1.getChildNodes();
					for (int temp3 = 0; temp3 < lhsX.getLength(); temp3++) {
						if (!lhsX.item(temp3).getNodeName().equals("edgeref")) {
							continue;
						}
						String toAdd = safe(lhsX.item(temp3).getAttributes().getNamedItem("id").getTextContent());
						lhs.add(toAdd);
					}
					NodeList rhsX = w2.getChildNodes();
					for (int temp3 = 0; temp3 < rhsX.getLength(); temp3++) {
						if (!rhsX.item(temp3).getNodeName().equals("edgeref")) {
							continue;
						}
						String toAdd = safe(rhsX.item(temp3).getAttributes().getNamedItem("id").getTextContent());
						rhs.add(toAdd);
					}
					eqs.add(new Pair<>(lhs, rhs));
				}
			}
		}
		SchExp<?, ?, ?, ?, ?> schExp = new SchExpRaw(new TyExpVar("SqlTypeSide"), new LinkedList<>(), ens, fks, eqs, atts, new LinkedList<>(), new LinkedList<>());
		
		return new Pair<>(schExp, edsExps);
	}
	
	private static Pair<List<String>, String> path(Node w1) {
		String dom = safe(w1.getAttributes().getNamedItem("domain").getTextContent());
		List<String> lhs = new LinkedList<>();
		lhs.add(dom);

		NodeList lhsX = w1.getChildNodes();
		for (int temp3 = 0; temp3 < lhsX.getLength(); temp3++) {
			if (!lhsX.item(temp3).getNodeName().equals("edgeref")) {
				continue;
			}
			String toAdd = safe(lhsX.item(temp3).getAttributes().getNamedItem("id").getTextContent());
			lhs.add(toAdd);
		}
		return new Pair<>(lhs, safe(w1.getAttributes().getNamedItem("codomain").getTextContent()));
	}

	private static RawTerm toTerm(List<String> l, String v) {
		RawTerm r = new RawTerm(v);
		for (String s : l) {
			r = new RawTerm(s, Util.singList(r));
		}
		return r;
	}
	
	private static List<Pair<String,EdsExpRaw>> translateC(Node sketch, Set<String> warnings, SchExp<?, ?, ?, ?, ?> schExp) {
		List<Pair<String,EdsExpRaw>> edsExps = new LinkedList<>();
		NodeList l = sketch.getChildNodes();
		for (int temp = 0; temp < l.getLength(); temp++) {
			Node n = l.item(temp);
			NodeList j = n.getChildNodes();
			for (int temp2 = 0; temp2 < j.getLength(); temp2++) {
				Node m = j.item(temp2);
				List<EdExpRaw> edExps = new LinkedList<>();
				String name = null;
				if (m.getNodeName().equals("sumconstraint")) {
					warnings.add("sum constraints not exported - AQL does not currently support sum constraints");
					continue;
				} else if (m.getNodeName().equals("limitconstraint")) {
					warnings.add("limit constraints not exported - if you see this, please report");
					continue;
				} else if (m.getNodeName().equals("pullbackconstraint")) {
					name = "pullback";
					Pair<List<String>, String> f1 = null, f2 = null, g1 = null, g2 = null;
					for (int i = 0; i < m.getChildNodes().getLength(); i++) {
						Node x = m.getChildNodes().item(i);
						if (x.getNodeName().equals("path")) {
							if (g1 == null) {
								g1 = path(x);
							} else if (g2 == null) {
								g2 = path(x);
							} else if (f1 == null) {
								f1 = path(x);
							} else if (f2 == null) {
								f2 = path(x);
							}
						}
					}
					if (f1 == null || g1 == null || f2 == null || g2 == null) {
						throw new RuntimeException("Anomaly - please report");
					}
					String A = f1.first.get(0);
					String B = f1.second;
					String C = g1.second;
					f1.first.remove(0);
					g1.first.remove(0);
					g2.first.remove(0);
					f2.first.remove(0);
					
					List<Pair<String, String>> as = new LinkedList<>();
					as.add(new Pair<>("b", B));
					as.add(new Pair<>("c", C));
					List<Pair<RawTerm, RawTerm>> a_eqs = new LinkedList<>();
					a_eqs.add(new Pair<>(toTerm(f2.first, "b"),toTerm(g2.first, "c")));
					
					List<Pair<String, String>> es = new LinkedList<>();
					es.add(new Pair<>("a", A));
					List<Pair<RawTerm, RawTerm>> e_eqs = new LinkedList<>();
					e_eqs.add(new Pair<>(toTerm(f1.first, "a"),new RawTerm("b")));
					e_eqs.add(new Pair<>(toTerm(g1.first, "a"),new RawTerm("c")));
					EdExpRaw ed1 = new EdExpRaw(as, a_eqs, es, e_eqs, true);
					edExps.add(ed1);
/*
					as = new LinkedList<>();
					as.add(new Pair<>("a1", A));
					as.add(new Pair<>("a2", A));
					a_eqs = new LinkedList<>();
					a_eqs.add(new Pair<>(toTerm(f1.first, "a1"),toTerm(f1.first, "a2")));					
					a_eqs.add(new Pair<>(toTerm(g1.first, "a1"),toTerm(g1.first, "a2")));					
					es = new LinkedList<>();
					e_eqs = new LinkedList<>();
					e_eqs.add(new Pair<>(new RawTerm("a1"),new RawTerm("a2")));
					EdExpRaw ed2 = new EdExpRaw(as, a_eqs, es, e_eqs);
					edExps.add(ed2);					
					*/
				} else if (m.getNodeName().equals("productconstraint")) {
				name = "product";
					Pair<List<String>, String> f = null, g = null;
					for (int i = 0; i < m.getChildNodes().getLength(); i++) {
						Node x = m.getChildNodes().item(i);
						if (x.getNodeName().equals("path")) {
							if (f == null) {
								f = path(x);
							} else if (g == null) {
								g = path(x);
							}
						}
					}
					if (f == null || g == null) {
						throw new RuntimeException("Anomaly - please report");
					}
					String A = f.first.get(0);
					String B = f.second;
					String C = g.second;
					f.first.remove(0);
					g.first.remove(0);
					
					List<Pair<String, String>> as = new LinkedList<>();
					as.add(new Pair<>("b", B));
					as.add(new Pair<>("c", C));
					List<Pair<RawTerm, RawTerm>> a_eqs = new LinkedList<>();
					List<Pair<String, String>> es = new LinkedList<>();
					es.add(new Pair<>("a", A));
					List<Pair<RawTerm, RawTerm>> e_eqs = new LinkedList<>();
					e_eqs.add(new Pair<>(toTerm(f.first, "a"),new RawTerm("b")));
					e_eqs.add(new Pair<>(toTerm(g.first, "a"),new RawTerm("c")));
					EdExpRaw ed1 = new EdExpRaw(as, a_eqs, es, e_eqs, true);
					edExps.add(ed1);
/*
					as = new LinkedList<>();
					as.add(new Pair<>("a1", A));
					as.add(new Pair<>("a2", A));
					a_eqs = new LinkedList<>();
					a_eqs.add(new Pair<>(toTerm(f.first, "a1"),toTerm(f.first, "a2")));					
					a_eqs.add(new Pair<>(toTerm(g.first, "a1"),toTerm(g.first, "a2")));					
					es = new LinkedList<>();
					e_eqs = new LinkedList<>();
					e_eqs.add(new Pair<>(new RawTerm("a1"),new RawTerm("a2")));
					EdExpRaw ed2 = new EdExpRaw(as, a_eqs, es, e_eqs);
					edExps.add(ed2);
					*/
				} else if (m.getNodeName().equals("equalizerconstraint")) {
					List<String> h = null, f = null, g = null; 
					for (int i = 0; i < m.getChildNodes().getLength(); i++) {
						Node x = m.getChildNodes().item(i);
						if (x.getNodeName().equals("path")) {
							if (h == null) {
								h = path(x).first;
							} else if (f == null) {
								f = path(x).first;
							} else if (g == null) {
								g = path(x).first;
							}
						}
					}
					if (h == null || f == null || g == null) {
						throw new RuntimeException("Anomaly - please report");
					}
					String B = f.get(0);
					String A = h.get(0);
					h.remove(0);
					f.remove(0);
					g.remove(0);
					
					List<Pair<String, String>> as = new LinkedList<>();
					as.add(new Pair<>("b", B));
					List<Pair<RawTerm, RawTerm>> a_eqs = new LinkedList<>();
					a_eqs.add(new Pair<>(toTerm(f, "b"),toTerm(g, "b")));					
					List<Pair<String, String>> es = new LinkedList<>();
					es.add(new Pair<>("a", A));
					List<Pair<RawTerm, RawTerm>> e_eqs = new LinkedList<>();
					e_eqs.add(new Pair<>(toTerm(h, "a"),new RawTerm("b")));
					EdExpRaw ed1 = new EdExpRaw(as, a_eqs, es, e_eqs, true);
					edExps.add(ed1);

/*					as = new LinkedList<>();
					as.add(new Pair<>("a1", A));
					as.add(new Pair<>("a2", A));
					a_eqs = new LinkedList<>();
					a_eqs.add(new Pair<>(toTerm(h, "a1"),toTerm(h, "a2")));					
					es = new LinkedList<>();
					e_eqs = new LinkedList<>();
					e_eqs.add(new Pair<>(new RawTerm("a1"),new RawTerm("a2")));
					EdExpRaw ed2 = new EdExpRaw(as, a_eqs, es, e_eqs);
					edExps.add(ed2);
					*/
					name = "equalizer";
					
//					warnings.add("equalizer constraints not exported - todo");
					//the EASIK code indicates these will be in order - the equalizer then the two parallel paths
				//	Assume your schema is just the graph A-->B==>C as you had it above. 
					//		To sketch that you want models in which h is the equalizer of f,g, you need two EDs:
					//	forall b : B, f(b)=g(b) -> exists a : A, h(a)=b.
					///	forall a1 a2 : A, h(a1)=h(a2) -> a1=a2.
					
					
					
				} else {
					continue;
				}
				
				EdsExpRaw edsExp = new EdsExp.EdsExpRaw(schExp, new LinkedList<>(), edExps);
				edsExps.add(new Pair<>(name, edsExp));
			}
		}
		return edsExps;
	}

	private static String easikTypeToString(String tyName) {
		switch (tyName) {
		case "easik.database.types.BigInt":
			return "BigInt"; // Long
		case "easik.database.types.Blob":
			return "Blob"; // byte[]
		case "easik.database.types.Boolean":
			return "Boolean"; // Boolean
		case "easik.database.types.Char":
			return "Char"; // Character
		case "easik.database.types.Date":
			return "Date"; // java.Sql.Date
		case "easik.database.types.Decimal":
			return "Decimal"; // java.math.BigDecimal
		case "easik.database.types.DoublePrecision":
			return "DoublePrecision"; // Double
		case "easik.database.types.Float":
			return "Float"; // Float
		case "easik.database.types.Int":
			return "Int"; // Integer
		case "easik.database.types.SmallInt":
			return "SmallInt"; // Short
		case "easik.database.types.Text":
			return "Text"; // String
		case "easik.database.types.Time":
			return "Time"; // java.sql.time
		case "easik.database.types.TimeStamp":
			return "TimeStamp"; // java.sql.timestamp
		case "easik.database.types.Varchar":
			return "Varchar";
		case "easik.database.types.Custom":
			return "Custom"; // Object
		default:
			throw new RuntimeException("Unknown type: " + tyName);
		}
	}

	public static String javaClassFor(String s) {
		switch (s) {
		case "BigInt":
			return "java.lang.Long";
		case "Boolean":
			return "java.lang.Boolean";
		case "Char":
			return "java.lang.Character";
		case "DoublePrecision":
			return "java.lang.Double";
		case "Float":
			return "java.lang.Float";
		case "Int":
			return "java.lang.Integer";
		case "SmallInt":
			return "java.lang.Short";
		case "Text":
			return "java.lang.String";
		case "Varchar":
			return "java.lang.String";
		case "Custom":
			return "java.lang.Object";
		// case "Blob" : return "Blob"; //byte[]
		// case "Date" : return "java.sql.Date";
		// case "Decimal" : return "java.math.BigDecimal";
		// case "Time" : return "java.sql.time";
		// case "TimeStamp" : return "java.sql.TimeStamp";
		default:
			return "java.lang.Object";
		}

	}

	public static String javaParserFor(String s) {
		switch (s) {
		case "BigInt":
			return "return new java.lang.Long(input[0])";
		case "Boolean":
			return "return new java.lang.Boolean(input[0])";
		case "Char":
			return "return input[0].charAt(0)";
		case "DoublePrecision":
			return "return new java.lang.Double(input[0])";
		case "Float":
			return "return new java.lang.Float(input[0])"; // Float
		case "Int":
			return "return new java.lang.Integer(input[0])"; // Integer
		case "SmallInt":
			return "return new java.lang.Short(input[0])"; // Short
		case "Text":
			return "return input[0]"; // String
		case "Varchar":
			return "return input[0]";
		case "Custom":
			return "return input[0]"; // Object
		// case "Blob" : return "Blob"; //byte[]
		// case "Date" : return "Date";
		// case "Decimal" : return "Decimal";
		// case "Time" : return "Time"; //java.sql.time
		// case "TimeStamp" : return "TimeStamp"; //java.sql.timestamp
		default:
			return "return input[0]";
		}
	}

	// TODO: AQL add operations here
	public static TyExpRaw sql(Set<String> used) {
		List<Pair<String, String>> java_tys = new LinkedList<>();
		List<Pair<String, String>> java_parsers = new LinkedList<>();

		for (String s0 : used) {
			String s = easikTypeToString(s0);
			java_tys.add(new Pair<>(s, javaClassFor(s)));
			java_parsers.add(new Pair<>(s, javaParserFor(s)));
		}

		return new TyExpRaw(new LinkedList<>(), new LinkedList<>(), new LinkedList<>(), new LinkedList<>(), java_tys, java_parsers, new LinkedList<>(), new LinkedList<>());
	}

	public static String easikToAql(String in) {
		String ret = "";
		Set<String> tys = new HashSet<>();
		Set<String> warnings = new HashSet<>();
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputStream stream = new ByteArrayInputStream(in.getBytes(StandardCharsets.UTF_8));
			Document doc = dBuilder.parse(stream);
			doc.getDocumentElement().normalize();
			NodeList sketchesNodes = doc.getElementsByTagName("sketches");
			if (sketchesNodes.getLength() != 1) {
				throw new RuntimeException("multiple sketches tags");
			}
			Node sketchesNode = sketchesNodes.item(0);
			NodeList nList = sketchesNode.getChildNodes();
			String ret2 = "";
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (!nNode.getNodeName().equals("easketch")) {
					continue;
				}
				String s0 = nNode.getAttributes().getNamedItem("name").getTextContent().replace(" ", "_") + "_schema";
				Pair<SchExp<?, ?, ?, ?, ?>, List<Pair<String, EdsExpRaw>>> schExp0 = translate1(nNode, tys, warnings, s0);
				SchExp<?,?,?,?,?> schExp = schExp0.first;
				String s1 = "schema " + s0 + " = " + schExp + "\n\n";
				
				List<Pair<String, EdsExpRaw>> edsExps = schExp0.second; 
				edsExps.addAll(translateC(nNode, warnings, new SchExpVar(s0)));
				ret2 += s1;
				int i = 0;
				List<String> imports = new LinkedList<>();
				for (Pair<String, EdsExpRaw> edsExp : edsExps) {
					String x = nNode.getAttributes().getNamedItem("name").getTextContent().replace(" ", "_") + "_" + edsExp.first + i;
					ret2 += "constraints " + x + " = " + edsExp.second + "\n\n";
					imports.add(x);
					i++;
				}
				if (!imports.isEmpty()) {
					ret2 += "constraints " + nNode.getAttributes().getNamedItem("name").getTextContent().replace(" ", "_") + "_constraints = literal : " + s0 + " {\n\timports\n\t\t" + Util.sep(imports, "\n\t\t") + "\n}\n\n";
				}
			}

			NodeList views = doc.getElementsByTagName("views");
			if (views != null && views.getLength() != 0 && views.item(0).hasChildNodes()) {
				warnings.add("Cannot export views - AQL does not currently support views ");
			}
			ret = "typeside SqlTypeSide = " + sql(tys) + "\n\n" + ret2;
			if (!warnings.isEmpty()) {
				ret += "/* Warnings:\n\n" + Util.sep(warnings, "\n") + "\n*/";
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}

	}

}
