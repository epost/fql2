package catdata.opl.examples;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import catdata.Chc;
import catdata.Pair;
import catdata.Triple;
import catdata.ide.Util;
import catdata.opl.OplCtx;
import catdata.opl.OplExp.OplMapping;
import catdata.opl.OplExp.OplPres;
import catdata.opl.OplExp.OplSat;
import catdata.opl.OplExp.OplSetInst;
import catdata.opl.OplExp.OplSig;
import catdata.opl.OplTerm;

/**
 * This standalone example illustrates direct use of the OPL API on this example:
 *
 * S = theory {
 *		sorts A;
 *		symbols f: A -> A;
 *		equations forall a. f(f(a)) = f(a);
 * }
 * 
 * T = theory {
 * 		sorts B;
 * 		symbols;
 * 		equations;
 * }
 * 
 * F = mapping {
 * 		sorts A -> B;
 * 		symbols f -> forall b:B. b;
 * } : S -> T
 * 
 * I = presentation {
 * 		generators g1, g2 : A;
 * 		equations;
 * } : S
 * 
 * J = sigma F I
 * 
 * I1 = saturate I
 * J1 = saturate J
 */
public class OplStandaloneExample {

	
	public static void main(String[] args) {
		
		//first we define S
		Set<String> sortsS = Util.singSet("A");
		Map<String, Pair<List<String>, String>> symbolsS = Util.singMap("f", new Pair<>(Util.singList("A"), "A"));

		OplCtx<String, String> ffafaCtx = new OplCtx<>(Util.singMap("a", "A"));
		OplTerm<String, String> ffafaLhs = new OplTerm<>("f", Util.singList(new OplTerm<>("f", Util.singList(new OplTerm<>("a")))));
		OplTerm<String, String> ffafaRhs = new OplTerm<>("f", Util.singList(new OplTerm<>("a")));
		List<Triple<OplCtx<String, String>, OplTerm<String, String>, OplTerm<String, String>>> 
		 equationsS = Util.singList(new Triple<>(ffafaCtx, ffafaLhs, ffafaRhs));
		
		//the type OplSig<X,Y,Z> is for algebraic theories with sorts of type X, symbols of type Y, and variables of type Z.
		//it requires a way to make fresh variables of type Z (here, the VarIterator), and it takes an optional
		//'predence' on the symbols (here, the new hashmap).  Then it takes a set of X for the sorts, a map Y -> List X * X
		//for the symbols and their types, and a set of equations, where each equation has a context, consisting of
		//a set of typed variables (a map Z -> X, aka a "context") and two terms of type OplTerm<Y,Z>.  An OplTerm<Y,Z> is an algebraic term
		//that is either a variable of type Z or a symbol of type Y applied to a list of arguments (themselves OplTerm<Y,Z>'s).
		//Here, X = Y = Z = String.
		OplSig<String, String, String> S = new OplSig<>(new VarIterator(), new HashMap<>(), sortsS, symbolsS, equationsS);
		
		///////
		
		//next, we define T

		OplSig<String, String, String> T = new OplSig<>(new VarIterator(), new HashMap<>(),  Util.singSet("B"), new HashMap<>(), new LinkedList<>());

		//////
		
		//next, we define F
		
		Map<String, Pair<OplCtx<String, String>, OplTerm<String, String>>> 
		 symbolsM = Util.singMap("f", new Pair<>(new OplCtx<>(Util.singMap("b", "B")), new OplTerm<>("b")));
		Map<String, String> sortsM = Util.singMap("A", "B");
		
		//the type OplMapping<S1,C1,V,S2,C2> is for mappings from OplSig<S1,C1,V> to OplSig<S2,C2,V>.
		//it requires a map on sorts S1 -> S2, and a map from symbols C1 to terms-in-context, that is,
		//a pair of a map V -> S2 and a term using those variables and symbols in C2
		OplMapping<String,String,String,String,String> F = new OplMapping<>(sortsM, symbolsM, "S", "T"); //the "S" and "T" are for display purposes only
		F.validate(S, T); // validate is where the source and target of F are set and it is checked that F is a functor
		
		//////

		//next, we define I
		
		Map<String, String> gensI = new HashMap<>();
		gensI.put("g1", "A");
		gensI.put("g2", "A");
		//the type OplPres<S,C,V,X> is for presentations on OplSig<S,C,V> with generators of type X.  The equations
		//will be between OplTerm<Chc<C, X>, V> (because terms can mention both symbols C from the theory and X from
		//the generators, but here we don't have any equations.
		OplPres<String, String, String, String> I = new OplPres<String, String, String, String>(new HashMap<>(), "I", S, gensI , new LinkedList<>());
		
		//////

		//finally, we define J
		
		OplPres<String, String, String, String> J = F.sigma(I);
		
		//////
		
		//An OplSetInst<S,C,X> is a set-valued model on OplSig<S,C,V> of an OplPres<S,C,V,X>.  
		//When we saturate an instance presentation, we get a set-valued model where 
		//the elements of the sets are OplTerm<S,Chc<C,X>,V>.  It is these sets that are usually
		//inspected in the gui.
		OplSetInst<String,String,OplTerm<Chc<String,String>,String>> I1 = OplSat.saturate(I);
		OplSetInst<String,String,OplTerm<Chc<String,String>,String>> J1 = OplSat.saturate(J);

		//display the result
		
		//each Opl artifact has a 'display' method for showing it graphically.
		JTabbedPane pane = new JTabbedPane();
		pane.add("S", S.display());
		pane.add("T", T.display());
		pane.add("F", F.display());
		pane.add("I", I.display());
		pane.add("I1", I1.display());	
		pane.add("J", J.display());
		pane.add("J1", J1.display());	
		JFrame frame = new JFrame("Demo");
		frame.setContentPane(pane);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.pack();
		frame.setVisible(true);
	}
	
	static class VarIterator implements Iterator<String> {
		static int n = 0;

		public boolean hasNext() {
			return true;
		}

		public String next() {
			return "v" + n++;
		}
	}

	
}
