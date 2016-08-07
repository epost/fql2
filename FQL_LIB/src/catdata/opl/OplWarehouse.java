package catdata.opl;

import java.awt.GridLayout;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.ide.CodeTextPanel;
import catdata.ide.Program;
import catdata.ide.Util;
import catdata.ide.WizardModel;
import catdata.opl.OplExp.OplInst0;
import catdata.opl.OplExp.OplMapping;
import catdata.opl.OplExp.OplPres;
import catdata.opl.OplExp.OplPresTrans;
import catdata.opl.OplExp.OplSCHEMA0;
import catdata.opl.OplExp.OplSig;
import catdata.opl.OplExp.OplSigma;
import catdata.opl.OplExp.OplUnion;

public class OplWarehouse extends WizardModel<Program<OplExp>> {
	
	private static String INITIAL = "Initial";
	private static String THEORY = "Theory";
	private static String SHAPE = "Shape";
	private static String SCHEMA = "Schema";
	private static String MAPPING = "Mapping";
	private static String INSTANCE = "Instance";
	private static String TRANSFORM = "Transform";
	private static String FINISHED = "Finished";
		
	private static String[] states = new String[] {INITIAL, THEORY, SHAPE, SCHEMA, MAPPING, INSTANCE, TRANSFORM, FINISHED};
	
	private static Map<String, String> instructions = new HashMap<>();
	{
		instructions.put(INITIAL, "Welcome to the OPL Data Warehousing Wizard.");
		instructions.put(THEORY, "Enter the type side.");
		instructions.put(SHAPE, "Enter the shape of the colimit as a schema with no attributes or equations.");
		instructions.put(SCHEMA, "Enter the source schemas, one per entity in the shape.");
		instructions.put(MAPPING, "Enter the schema mappings, one per edge in the shape. " );
		instructions.put(INSTANCE, "Enter the database instances, one per source schema. " );
		instructions.put(TRANSFORM, "Enter the record linkages, one per schema mapping." );
		instructions.put(FINISHED, "Click 'Finish' to transfer the result to a new OPL file." );
	}
	
	private String state = INITIAL;

	private Map<String, OplExp> bindings = new LinkedHashMap<>();
	
	private List<String> orderedFor(String s) {
		List<String> l = new LinkedList<>(); 
		for (String b : bindings.keySet()) {
			if (b.startsWith(s)) {
				l.add(b);
			}
		}
		l.sort(String.CASE_INSENSITIVE_ORDER);
		return l;
	}
	
	private List<String> makeOrder() {
		//TODO
		return new LinkedList<>(bindings.keySet());
/*		List<String> ret = new LinkedList<>();
		for (String s : states) {
			ret.addAll(orderedFor(s));
		}
		return ret; */
	}
	
//	private List<String> order = new LinkedList<>();
	
	private Map<String, Page> pages = new HashMap<>();
	
	private Map<String, String> next = new HashMap<>(), prev = new HashMap<>();
	
	public OplWarehouse() {
		next.put(INITIAL, THEORY);
		next.put(THEORY, SHAPE);
		next.put(SHAPE, SCHEMA);
		next.put(SCHEMA, MAPPING);
		next.put(MAPPING, INSTANCE);
		next.put(INSTANCE, TRANSFORM);
		next.put(TRANSFORM, FINISHED);
		
		prev = Util.invMap(next);		
		
		for (String s : states) {
			Page p = new Page(s);
			pages.put(s, p);
			registerModelListener(p);
		}
	}
	
	private class Page extends JPanel implements ChangeListener {
		public String my_state;
		public Page(String string) {
			my_state = string;
			setLayout(new GridLayout(1,1));
			setBorder(BorderFactory.createTitledBorder(my_state));
			stateChanged(null);
		}
		public Map<String, CodeTextPanel> map = new HashMap<>();
		@Override
		public void stateChanged(ChangeEvent e) {
			//System.out.println("state change, bindings are " + bindings);
			JSplitPane p = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			p.add(new CodeTextPanel("", instructions.get(my_state)));
			JTabbedPane pane = new JTabbedPane();
			for (String name : orderedFor(my_state)) {
					String text = bindings.containsKey(name) ? bindings.get(name).toString() : "";
					CodeTextPanel ctp = new CodeTextPanel("", text);
					map.put(name, ctp);
					pane.add(name, ctp); 
			}
			removeAll();
			p.add(pane);
			add(p);
			this.revalidate();
		}
		
		//TODO
		public Map<String, String> transfer() {
//			System.out.println("called txfer for " + my_state + " keys " + map.keySet());
			Map<String, String> m = new HashMap<>();
			for (String name : map.keySet()) {
				String str = map.get(name).getText();
				try {
					OplExp exp = OplParser.exp(str);
					bindings.put(name, exp);					
				} catch (RuntimeException ex) {
					bindings.put(name, str.trim().length() == 0 ? null : new OplExp.OplString(str + "\n\n" + ex.getMessage())); //will not remain on page otherwise
					ex.printStackTrace();
					m.put(name, ex.getMessage());
				}
			}
		//	System.out.println("after trxfer, bindings are " + bindings);

			return m;
		}
	}
	
	@Override
	public boolean isComplete() {
		return state.equals(FINISHED);
	}

	@Override
	public Program<OplExp> complete() throws IllegalStateException {
		
		
		List<Triple<String, Integer, OplExp>> decls = new LinkedList<>();
		for (String name : makeOrder()) {
			if (name.equals(FINISHED + " all") ) {
				continue;
			}
			decls.add(new Triple<>(name, null, bindings.get(name)));
		}
		return new Program<>(decls);
	}
	
	//TODO: maintain compiling program?

	@Override
	public Map<String, Page> getAllPages() {
		return pages;
	}

	@Override
	public void forward0() {
		String proposedState = next.get(state);
		
		Page p = pages.get(state);
		Map<String, String> errs = p.transfer();
		if (errs.isEmpty()) {
			addBlanks();		
			state = proposedState;
		} else {

		}
		
	}
	
	//TODO: alphabetize tabs
	
	//TODO heuristic: if something already there, leave it.  otherwise, create new
	void addBlanks() {
		if (state.equals(INITIAL)) {
			if (!bindings.containsKey(THEORY)) {  
				bindings.put(THEORY, new OplSig<>(new OplParser.VIt(), new HashMap<>(), new HashSet<>(), new HashMap<>(), new LinkedList<>()));			
			}
		} else if (state.equals(THEORY)) {	
			if (!bindings.containsKey(SHAPE)) {
				bindings.put(SHAPE, new OplSCHEMA0<String, String, String>(new HashMap<>(), new HashSet<>(), new HashMap<>(), new HashMap<>(), new LinkedList<>(), new LinkedList<>(), THEORY));
			}
		} else if (state.equals(SHAPE)) {
			OplSCHEMA0<String, String, String> shape = (OplSCHEMA0<String, String, String>) bindings.get(SHAPE);
			for (String en : shape.entities) {
				if (bindings.containsKey(SCHEMA + "_" + en)) {  //TODO: false
					continue;
				}
				bindings.put(SCHEMA + "_" + en, new OplSCHEMA0<String, String, String>(new HashMap<>(), new HashSet<>(), new HashMap<>(), new HashMap<>(), new LinkedList<>(), new LinkedList<>(), THEORY));
			}
		} else if (state.equals(SCHEMA)) {
			OplSCHEMA0<String, String, String> shape = (OplSCHEMA0<String, String, String>) bindings.get(SHAPE);
			
			for (String ed : shape.edges.keySet()) {
				if (bindings.containsKey(MAPPING + "_" + ed)) {
					continue;
				}
				String src = SCHEMA + "_" + shape.edges.get(ed).first.get(0);
				//String dst = SCHEMA + "_" + shape.edges.get(ed).second;
				OplSCHEMA0<String,String,String> src0 = (OplSCHEMA0<String,String,String>) bindings.get(src);
				Map<String, String> sorts = new HashMap<>();
				for (String x : src0.entities) {
					sorts.put(x, "?");
				}
				Map<String, Pair<OplCtx<String, String>, OplTerm<String, String>>> symbols = new HashMap<>();
				for (String g : src0.attrs.keySet()) {
					OplCtx<String, String> ctx = new OplCtx<>(Util.singList(new Pair<>("x", "?")));
					OplTerm<String, String> term = new OplTerm<>("?");
					symbols.put(g, new Pair<>(ctx, term));					
				}
				for (String g : src0.edges.keySet()) {
					OplCtx<String, String> ctx = new OplCtx<>(Util.singList(new Pair<>("t", "?")));
					OplTerm<String, String> term = new OplTerm<>("?");
					symbols.put(g, new Pair<>(ctx, term));					
				}
				
				bindings.put(MAPPING + "_" + ed, new OplMapping<String,String,String,String,String>(sorts, symbols, src, SCHEMA + "_" + shape.edges.get(ed).second));
			}
		} else if (state.equals(MAPPING)) {
			OplSCHEMA0<String, String, String> shape = (OplSCHEMA0<String, String, String>) bindings.get(SHAPE);
			Set<String> en2 = shape.entities.stream().map(x -> { return SCHEMA + "_" + x; }).collect(Collectors.toSet());
			Map<String, Pair<List<String>, String>> ed2 = new HashMap<>();
			for (String e : shape.edges.keySet()) {
				Pair<List<String>, String> x = shape.edges.get(e);
				List<String> l = Util.singList(SCHEMA + "_" + x.first.get(0));
				ed2.put(MAPPING + "_" + e, new Pair<>(l, SCHEMA + "_" + x.second));
			}
			OplSCHEMA0<String, String, String> shape2 = new OplSCHEMA0<String,String,String>(new HashMap<>(), en2, ed2, new HashMap<>(), new LinkedList<>(), new LinkedList<>(), THEORY);
			//TODO: needs to compile to here
			computeColimit(bindings, shape2);
			
			for (String en : shape.entities) {
				if (bindings.containsKey(INSTANCE + "_" + en)) {  //TODO: false
					continue;
				}
				bindings.put(INSTANCE + "_" + en, new OplInst0<>(new OplPres<>(new HashMap<>(), SCHEMA + "_" + en, null, new HashMap<>(), new LinkedList<>())));
			}
			
		} else if (state.equals(INSTANCE)) {
			OplSCHEMA0<String, String, String> shape = (OplSCHEMA0<String, String, String>) bindings.get(SHAPE);
			
			for (String ed : shape.edges.keySet()) {
				if (bindings.containsKey(TRANSFORM + "_" + ed)) {
					continue;
				}
				String en = shape.edges.get(ed).first.get(0);
				String en2 = shape.edges.get(ed).second;
				
				String src = INSTANCE + "_" + ed + "_forward";
				bindings.put(src, new OplSigma(MAPPING + "_" + ed, INSTANCE + "_" + en));				
				
				OplMapping<String,String,String,String,String> mmm = (OplMapping<String,String,String,String,String>) bindings.get(MAPPING + "_" + ed);
				//TODO: factor getting into separate method, or use compiler
				OplInst0<String,String,String,String> src1 = (OplInst0<String,String,String,String>) bindings.get(INSTANCE + "_" + en);
				
				OplInst0<String,String,String,String> src0 = (OplInst0<String,String,String,String>) bindings.get(INSTANCE + "_" + en2);
				OplSCHEMA0<String,String,String> src0_sch = (OplSCHEMA0<String,String,String>) bindings.get(src0.P.S);
				OplSCHEMA0<String,String,String> src1_sch = (OplSCHEMA0<String,String,String>) bindings.get(src1.P.S);
				
				Map<String, Map<String, OplTerm<Object, String>>> sorts = new HashMap<>();
				for (String x : src0_sch.entities) {
					sorts.put(x, new HashMap<>());
				}
				for (String g : src1.P.gens.keySet()) {
					String gty = src1.P.gens.get(g);
					String s2 = mmm.sorts.get(gty);
					if (s2 == null) {
						continue;
					}
					Map<String, OplTerm<Object, String>> symbols = sorts.get(s2);
					if (symbols != null) {
						symbols.put(g, new OplTerm<>("?"));											
					} 
				}	
				//System.out.println(sorts);
				bindings.put(TRANSFORM + "_" + ed + "_forward", new OplPresTrans<String,String,String,String,String>(sorts, src, INSTANCE + "_" + shape.edges.get(ed).second));
			}
	
		} else if (state.equals(TRANSFORM)) {
			OplSCHEMA0<String, String, String> shape = (OplSCHEMA0<String, String, String>) bindings.get(SHAPE);
			for (String en : shape.entities) {
				bindings.put(INSTANCE + "_" + en + "_colimit", new OplSigma(MAPPING + "_" + SCHEMA + "_" + en + "_colimit", INSTANCE + "_" + en  ));
			}
			Map<String, Pair<List<String>, String>> ed2 = new HashMap<>();
			for (String e : shape.edges.keySet()) {
				Pair<List<String>, String> x = shape.edges.get(e);
				List<String> l = Util.singList(INSTANCE + "_" + x.first.get(0) + "_colimit");
				//String en = x.first.get(0);
				String en = x.second;
				bindings.put(TRANSFORM + "_" + e + "_colimit", new OplSigma(MAPPING + "_" + SCHEMA + "_" + en + "_colimit", TRANSFORM + "_" + e + "_forward"));
				ed2.put(TRANSFORM + "_" + e + "_colimit", new Pair<>(l, INSTANCE + "_" + x.second +"_colimit"));
			}
			Set<String> en2 = shape.entities.stream().map(x -> { return INSTANCE + "_" + x + "_colimit"; }).collect(Collectors.toSet());
			
			OplSCHEMA0<String, String, String> shape2 = new OplSCHEMA0<String,String,String>(new HashMap<>(), en2, ed2, new HashMap<>(), new LinkedList<>(), new LinkedList<>(), THEORY);
			bindings.put(SCHEMA + "_for_result", shape2);
			
			bindings.put(FINISHED, new OplExp.OplString("colimit " + SCHEMA + "_for_result"));
//			bindings.put(FINISHED + " all", new OplExp.OplString(complete().toString()));
		}
		
	}

	@Override
	public void back0() {
		state = prev.get(state);
	}

	@Override
	public boolean canGoForward() {
		return true; //!state.equals(FINISHED);
	}

	@Override
	public boolean canGoBack() {
		return !state.equals(INITIAL);
	}

	@Override
	protected String getState() {
		return state;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	public static void computeColimit(Map<String, OplExp> env, OplSCHEMA0<String,String,String> shape) {
		
		if (shape.attrs.size() > 0) {
			throw new RuntimeException("Cannot have attributes in colimit shape schemas");
		}
		if (shape.pathEqs.size() > 0 || shape.obsEqs.size() > 0) {
			throw new RuntimeException("Cannot have equations in colimit shape schemas");
		}
		

			OplUnion u0 = new OplUnion(new LinkedList<>(shape.entities), shape.typeSide);
			OplObject u1 = union(env, u0);
	
			OplSCHEMA0<String, String, String> u = (OplSCHEMA0<String, String, String>) u1;
		
			Map<String, Set<String>> equivs = new HashMap<>();
			Map<String, String> equivs0 = new HashMap<>();
			
			for (String schname : shape.entities) {
				if (!(env.get(schname) instanceof OplSCHEMA0)) {
					throw new RuntimeException("Not a SCHEMA: " + schname);
				}
				OplSCHEMA0<String, String, String> sch = (OplSCHEMA0<String, String, String>) env.get(schname);
				for (String ename : sch.entities) {
					HashSet<String> set = new HashSet<>();
					set.add(schname + "_" + ename);
					equivs.put(schname + "_" + ename, set);
				}
			}
			
			//TODO: type check colimit
			for (String mname : shape.edges.keySet()) {
				Pair<List<String>, String> mt = shape.edges.get(mname);
				String s = mt.first.get(0);
				String t = mt.second;
				
				OplSCHEMA0<String, String, String> s0 = (OplSCHEMA0<String, String, String>) env.get(s);
				OplMapping<String, String, String, String, String> m0 = (OplMapping<String, String, String, String, String>) env.get(mname);

				if (!m0.src0.equals(s)) {
					throw new RuntimeException("Source of " + m0 + " is " + m0.src + " and not " + s + "as expected");
				}
				if (!m0.dst0.equals(t)) {
					throw new RuntimeException("Target of " + m0 + " is " + m0.dst + " and not " + t + "as expected");
				}
				
				for (String ob : s0.entities) {
					String ob0 = m0.sorts.get(ob);
					Set<String> set1 = equivs.get(s + "_" + ob);
					Set<String> set2 = equivs.get(t + "_" + ob0);
					set1.addAll(set2);
					equivs.put(s + "_" + ob, set1);
					equivs.put(t + "_" + ob0, set1);
				}				
			}
			
			for (String k : equivs.keySet()) {
				List<String> v = new LinkedList<>(equivs.get(k));
				v.sort(String.CASE_INSENSITIVE_ORDER);
				equivs0.put(k, Util.sep(v, "__"));
			}

			Set<String> entities = new HashSet<>(equivs0.values());
			Map<String, Pair<List<String>, String>> edges = new HashMap<>();
			Map<String, Pair<List<String>, String>> attrs = new HashMap<>();
			List<Triple<OplCtx<String, String>, OplTerm<String, String>, OplTerm<String, String>>> pathEqs = new LinkedList<>();
			List<Triple<OplCtx<String, String>, OplTerm<String, String>, OplTerm<String, String>>> obsEqs = new LinkedList<>();

			Function<String, String> fun = x -> {
				if (equivs0.containsKey(x)) {
					return equivs0.get(x);
				}
				return x;
			};
									
			for (String edge : u.edges.keySet()) {
				Pair<List<String>, String> ty = u.edges.get(edge);				
				edges.put(edge, new Pair<>(ty.first.stream().map(fun).collect(Collectors.toList()), fun.apply(ty.second)));		
			}
			for (String attr : u.attrs.keySet()) {
				Pair<List<String>, String> ty = u.attrs.get(attr);				
				attrs.put(attr, new Pair<>(ty.first.stream().map(fun).collect(Collectors.toList()), fun.apply(ty.second)));
			}
			for (Triple<OplCtx<String, String>, OplTerm<String, String>, OplTerm<String, String>> eq : u.pathEqs) {
				OplCtx<String, String> ctx = new OplCtx<String, String>(eq.first.values2().stream().map(x -> { return new Pair<>(x.first, fun.apply(x.second)); }).collect(Collectors.toList()));
				pathEqs.add(new Triple<>(ctx, OplOps.fun2(equivs0,eq.second), OplOps.fun2(equivs0,eq.third)));
			}
			for (Triple<OplCtx<String, String>, OplTerm<String, String>, OplTerm<String, String>> eq : u.obsEqs) {
				OplCtx<String, String> ctx = new OplCtx<String, String>(eq.first.values2().stream().map(x -> { return new Pair<>(x.first, fun.apply(x.second)); }).collect(Collectors.toList()));
				obsEqs.add(new Triple<>(ctx, OplOps.fun2(equivs0,eq.second), OplOps.fun2(equivs0,eq.third)));
			}
			
			for (String mname : shape.edges.keySet()) {
				Pair<List<String>, String> mt = shape.edges.get(mname);
				String s = mt.first.get(0);
				String t = mt.second;
				
				OplSCHEMA0<String, String, String> s0 = (OplSCHEMA0<String, String, String>) env.get(s);
				//OplSchema<String, String, String> t0 = (OplSchema<String, String, String>) ENV.get(t);
				OplMapping<String, String, String, String, String> m0 = (OplMapping<String, String, String, String, String>) env.get(mname);

				for (String edge : s0.edges.keySet()) {
					Pair<OplCtx<String, String>, OplTerm<String, String>> edge2 = m0.symbols.get(edge);
					List<OplTerm<String, String>> args = edge2.first.vars0.keySet().stream().map(
							x -> {
								return new OplTerm(x);
							}).collect(Collectors.toList());
					OplTerm<String, String> lhs = OplOps.fun2(equivs0, new OplTerm<>(s + "_" + edge, args));
					OplCtx<String, String> ctx = new OplCtx<String, String>(edge2.first.values2().stream().map(x -> { return new Pair<>(x.first, fun.apply(s + "_" + x.second)); }).collect(Collectors.toList()));
					OplTerm<String, String> rhs = OplOps.fun2(equivs0, OplOps.prepend(t, edge2.second));		
					
					pathEqs.add(new Triple<>(ctx, lhs, rhs));
				}	
				for (String edge : s0.attrs.keySet()) {
					Pair<OplCtx<String, String>, OplTerm<String, String>> edge2 = m0.symbols.get(edge);
					List<OplTerm<String, String>> args = edge2.first.vars0.keySet().stream().map(
							x -> {
								return new OplTerm(x);
							}).collect(Collectors.toList());
					OplTerm<String, String> lhs = OplOps.fun2(equivs0, new OplTerm<>(s + "_" + edge, args));
					OplCtx<String, String> ctx = new OplCtx<String, String>(edge2.first.values2().stream().map(x -> { return new Pair<>(x.first, fun.apply(s + "_" + x.second)); }).collect(Collectors.toList()));
					OplTerm<String, String> rhs = OplOps.fun2(equivs0, OplOps.prepend(t, edge2.second));		
					
					obsEqs.add(new Triple<>(ctx, lhs, rhs));
				}	
			} 
		
			OplSCHEMA0<String, String, String> ret = new OplSCHEMA0<>(new HashMap<>(), entities, edges, attrs, pathEqs, obsEqs, shape.typeSide);
			if (env.get(SCHEMA + "_Colimit") == null) {
				env.put(SCHEMA + "_Colimit", ret);
			}
			
			for (String schname : shape.entities) {
				OplSCHEMA0<String, String, String> sch = (OplSCHEMA0<String, String, String>) env.get(schname);

				Map<String, String> inj_sorts = new HashMap<>();
				Map<String, Pair<OplCtx<String, String>, OplTerm<String, String>>> inj_symbols = new HashMap<>();

				for (String ename : sch.entities) {
					inj_sorts.put(ename, fun.apply(schname + "_" + ename));
				}
				for (String c1 : sch.attrs.keySet()) {
					Pair<List<String>, String> t = sch.attrs.get(c1);
					List<Pair<String, String>> l = new LinkedList<>();
					List<OplTerm<String, String>> vs = new LinkedList<>();
					for (String s1 : t.first) {
						String v = (String) new OplParser.VIt().next();
						vs.add(new OplTerm<>(v));
						l.add(new Pair<>(v, fun.apply(schname + "_" + s1)));
					}
					OplCtx<String, String> ctx = new OplCtx<>(l);
					OplTerm<String, String> value = OplOps.fun2(equivs0, new OplTerm<>(schname + "_" + c1, vs));
					inj_symbols.put(c1, new Pair<>(ctx, value));
				}
				for (String c1 : sch.edges.keySet()) {
					Pair<List<String>, String> t = sch.edges.get(c1);
					List<Pair<String, String>> l = new LinkedList<>();
					List<OplTerm<String, String>> vs = new LinkedList<>();
					for (String s1 : t.first) {
						String v = (String) new OplParser.VIt().next();
						vs.add(new OplTerm<>(v));
						l.add(new Pair<>(v, fun.apply(schname + "_" + s1)));
					}
					OplCtx<String, String> ctx = new OplCtx<>(l);
					OplTerm<String, String> value = OplOps.fun2(equivs0, new OplTerm<>(schname + "_" + c1, vs));
					inj_symbols.put(c1, new Pair<>(ctx, value));
				}
				
				OplMapping<String, String, String, String, String> mapping = new OplMapping<>(inj_sorts, inj_symbols, schname, SCHEMA + "_Colimit");
				
				if (env.get(MAPPING + "_" + schname + "_colimit") == null) {
					env.put(MAPPING + "_" + schname + "_colimit", mapping);
				}
			}
			
		
		
	}
	
	static int temp = 0;
	
	public static OplExp union(Map<String, OplExp> env, OplUnion e) {
		
		Map<String, Integer> prec = new HashMap<>();
		Set<String> entities = new HashSet<>();
		Map<String, Pair<List<String>, String>> edges = new HashMap<>();
		Map<String, Pair<List<String>, String>> attrs = new HashMap<>();
		List<Triple<OplCtx<String, String>, OplTerm<String, String>, OplTerm<String, String>>> pathEqs = new LinkedList<>();
		List<Triple<OplCtx<String, String>, OplTerm<String, String>, OplTerm<String, String>>> obsEqs = new LinkedList<>();
		
		Set<String> schs = new HashSet<>();
		for (String s : e.names) {
			OplExp exp = env.get(s);
			if (exp == null) {
				throw new RuntimeException("Missing expression: " + s);
			}
				if (!(exp instanceof OplSCHEMA0)) {
					throw new RuntimeException("Not a schema: " + s);
				}
				OplSCHEMA0<String, String, String> toAdd = (OplSCHEMA0<String, String, String>) exp;
				if (!toAdd.typeSide.equals(THEORY)) {
					throw new RuntimeException("not all equal typesides in " + e);
				}
				for (Object entity : toAdd.entities) {
					String proposed = s + "_" + entity;
					if (entities.contains(proposed)) {
						throw new RuntimeException("name clash: " + entity);
					}
					entities.add(proposed);
				}
				
				for (Object edge : toAdd.edges.keySet()) {
					String proposed = s + "_" + edge;
					if (edges.containsKey(proposed)) {
						throw new RuntimeException("name clash: " + edge);
					}
					edges.put(proposed, new Pair<>(Util.singList(s + "_" + toAdd.edges.get(edge).first.get(0)), s + "_" + toAdd.edges.get(edge).second));
					if (toAdd.prec.containsKey(edge)) {
						prec.put(proposed, toAdd.prec.get(edge));
					}
				}
				for (Object att : toAdd.attrs.keySet()) {
					String proposed = s + "_" + att;
					if (attrs.containsKey(proposed)) {
						throw new RuntimeException("name clash: " + att);
					}
					attrs.put(proposed, new Pair<>(Util.singList(s + "_" + toAdd.attrs.get(att).first.get(0)), toAdd.attrs.get(att).second));
					if (toAdd.prec.containsKey(att)) {
						prec.put(proposed, toAdd.prec.get(att));
					}
				}
				for (Triple<OplCtx<String, String>, OplTerm<String, String>, OplTerm<String, String>> tr : toAdd.pathEqs) {
					String v = tr.first.names().get(0);
					String t = s + "_" + tr.first.values().get(0);
					OplCtx<String, String> ctx = new OplCtx<>(Util.singList(new Pair<>(v, t)));
					OplTerm<String, String> lhs1 = OplOps.prepend(s, tr.second);
					OplTerm<String, String> rhs1 = OplOps.prepend(s, tr.third);
					pathEqs.add(new Triple<>(ctx, lhs1, rhs1));
				}
				for (Triple<OplCtx<String, String>, OplTerm<String, String>, OplTerm<String, String>> tr : toAdd.obsEqs) {
					String v = tr.first.names().get(0);
					String t = s + "_" + tr.first.values().get(0);
					OplCtx<String, String> ctx = new OplCtx<>(Util.singList(new Pair<>(v, t)));
					OplTerm<String, String> lhs1 = OplOps.prepend(s, tr.second);
					OplTerm<String, String> rhs1 = OplOps.prepend(s, tr.third);
					obsEqs.add(new Triple<>(ctx, lhs1, rhs1));
				}

			} 
			return new OplSCHEMA0<>(prec, entities, edges, attrs, pathEqs, obsEqs, THEORY);
	}
}
