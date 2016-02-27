package catdata.mpl;

import java.util.LinkedList;
import java.util.List;

import catdata.Triple;
import catdata.Unit;
import catdata.ide.Util;
import catdata.mpl.Mpl.MplExp.MplSch;
import catdata.mpl.Mpl.MplTerm;
import catdata.mpl.Mpl.MplTerm.MplAlpha;
import catdata.mpl.Mpl.MplTerm.MplComp;
import catdata.mpl.Mpl.MplTerm.MplConst;
import catdata.mpl.Mpl.MplTerm.MplId;
import catdata.mpl.Mpl.MplTerm.MplLambda;
import catdata.mpl.Mpl.MplTerm.MplPair;
import catdata.mpl.Mpl.MplTerm.MplRho;
import catdata.mpl.Mpl.MplTerm.MplTr;
import catdata.mpl.Mpl.MplTermVisitor;
import catdata.mpl.Mpl.MplType.MplBase;
import catdata.mpl.Mpl.MplType.MplProd;
import catdata.mpl.Mpl.MplType.MplUnit;
import catdata.mpl.Mpl.MplTypeVisitor;
import catdata.mpl.MplStrict.Node;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;

public class MplStrict<O,A> implements MplTypeVisitor<O, List<O>, Unit>,
  MplTermVisitor<O, A, Triple<List<Node<O,A>>, List<Node<O,A>>, String>, Unit> {
	
	static class Node<O,A> {	
		
		MplTerm<O, A> term;
		boolean isInput;
		int which;
		O o;
		
		static int global_id=0;
		int id;
		
		public Node(MplTerm<O, A> term, boolean isInput, int which, O o) {
			this.term = term;
			this.isInput = isInput;
			this.which = which;
			this.o = o;
			id = global_id++;
		}
				
		
		@Override
		public String toString() {
			return "v" + id; // + " [label=\"" + term + " " + which + " \"]"; 
		}
		
		public String label() {
			String str = isInput ? "in" : "out";
			return toString() + " [label=\"" + /* term + " " */ + which + " " + str + " \"]; ";
		}
 	}
	
	int counter = 0;
	Integer fresh() {
		return counter++;
	}
	
	MplSch<O, A> ctx;
	public MplStrict(MplSch<O,A> ctx) {
		this.ctx = ctx;
	}
	
	Graph<Node<O,A>, Integer> g = new DirectedSparseMultigraph<>();
		
	@Override
	public List<O> visit(Unit env, MplBase<O> e) {
		return Util.singList(e.o);
	}

	@Override
	public List<O> visit(Unit env, MplProd<O> e) {
		List<O> ret = new LinkedList<>();
		ret.addAll(e.l.accept(env, this));
		ret.addAll(e.r.accept(env, this));
		return ret;
	}
	
	@Override
	public List<O> visit(Unit env, MplUnit<O> e) {
		return new LinkedList<>();
	}

	////////////////


	@Override
	public Triple<List<Node<O, A>>, List<Node<O, A>>, String> visit(Unit env, MplConst<O, A> e) {
		List<O> s = e.type(ctx).first.accept(env, this);
		List<O> t = e.type(ctx).second.accept(env, this);
		
		List<Node<O,A>> ret1 = new LinkedList<>();
		List<Node<O,A>> ret2 = new LinkedList<>();
		
		String ret = "subgraph cluster" + fresh() + "{ label=\"" + e + "\"; " ;

		int i = 0;
		for (O o : s) {
			Node<O,A> n = new Node<>(e, true, i, o);
			g.addVertex(n);
			ret1.add(n);
			i++;
			ret += n.label();
		}
		
		i = 0;
		for (O o : t) {
			Node<O,A> n = new Node<>(e, false, i, o);
			g.addVertex(n);
			ret2.add(n);
			i++;
			ret += n.label();
		}
		
		
		return new Triple<>(ret1, ret2, ret + " }");
	}

	@Override
	public Triple<List<Node<O, A>>, List<Node<O, A>>, String> visit(Unit env, MplId<O, A> e) {
		return makeId(e);
	}

	@Override
	public Triple<List<Node<O, A>>, List<Node<O, A>>, String>visit(Unit env, MplComp<O, A> e) {
		List<O> s = e.type(ctx).first.accept(env, this);
		List<O> t = e.type(ctx).second.accept(env, this);
		List<O> m = e.l.type(ctx).second.accept(env, this);
		
		List<Node<O,A>> ret1 = new LinkedList<>();
		List<Node<O,A>> ret2 = new LinkedList<>();
		
		Triple<List<Node<O, A>>, List<Node<O, A>>, String> l = e.l.accept(env, this);
		Triple<List<Node<O, A>>, List<Node<O, A>>, String> r = e.r.accept(env, this);
		
		String ret = "subgraph cluster" + fresh() + " { label=\"" + e + "\"; "  + l.third + " " + r.third;

		int i = 0;
		for (O o : s) {
			Node<O,A> n = new Node<>(e, true, i, o);
			g.addVertex(n);	
			ret += n.label();
			ret1.add(n);
			g.addEdge(fresh(), n, l.first.get(i));
			ret += n + " -> " + l.first.get(i) + ";";
			i++;
		}
		
		i = 0;
		for (O o : t) {
			Node<O,A> n = new Node<>(e, false, i, o);
			g.addVertex(n);
			ret += n.label();
			ret2.add(n);
			g.addEdge(fresh(), r.second.get(i), n);
			ret += r.second.get(i) + " -> " + n + ";";
			i++;
		}
		
		i = 0;
		for (O o : m) {
			g.addEdge(fresh(), l.second.get(i), r.first.get(i));
			ret += l.second.get(i) + " -> " + r.first.get(i) + ";";
			i++;
		}
		
		return new Triple<>(ret1, ret2, ret + " }");
	}

	@Override
	public Triple<List<Node<O, A>>, List<Node<O, A>>, String> visit(Unit env, MplPair<O, A> e) {
		List<Node<O,A>> ret1 = new LinkedList<>();
		List<Node<O,A>> ret2 = new LinkedList<>();
		
		Triple<List<Node<O, A>>, List<Node<O, A>>, String> l = e.l.accept(env, this);
		Triple<List<Node<O, A>>, List<Node<O, A>>, String> r = e.r.accept(env, this);
		
		String ret = "subgraph cluster" + fresh() + "{ label=\"" + e + "\"; "  + l.third + " " + r.third;

		int i = 0;
		for (Node<O,A> m : l.first) {
			Node<O,A> n = new Node<>(e, true, i, m.o);
			g.addVertex(n);			
			ret += n.label();
			g.addEdge(fresh(), n, m);
			ret += n + " -> " + m + " ;";
			ret1.add(n);
			i++;
		}
		for (Node<O,A> m : r.first) {
			Node<O,A> n = new Node<>(e, true, i, m.o);
			g.addVertex(n);			
			ret += n.label();
			g.addEdge(fresh(), n, m);
			ret += n + " -> " + m + " ;";
			ret1.add(n);
			i++;
		}
		
		i = 0;
		for (Node<O,A> m : l.second) {
			Node<O,A> n = new Node<>(e, false, i, m.o);
			g.addVertex(n);			
			ret += n.label();
			g.addEdge(fresh(), m, n);
			ret += m + " -> " + n + " ;";
			ret2.add(n);
			i++;
		}
		for (Node<O,A> m : r.second) {
			Node<O,A> n = new Node<>(e, false, i, m.o);
			g.addVertex(n);		
			ret += n.label();
			g.addEdge(fresh(), m, n);
			ret += m + " -> " + n + " ;";
			ret2.add(n);
			i++;
		}
		
		return new Triple<>(ret1, ret2, ret + " }");
	}

	public Triple<List<Node<O, A>>, List<Node<O, A>>, String> makeId(MplTerm<O, A> e) {
		List<O> x = e.type(ctx).first.accept(new Unit(), this);
		
		List<Node<O,A>> ret1 = new LinkedList<>();
		List<Node<O,A>> ret2 = new LinkedList<>();
		
		String ret = "subgraph cluster" + fresh() + "{ label=\"" + e + "\"; "  ;
		
		int i = 0;
		for (O o : x) {
			Node<O,A> n = new Node<>(e, true, i, o);
			ret += n.label();
			g.addVertex(n);
			ret1.add(n);
			Node<O,A> m = new Node<>(e, false, i, o);
			ret += m.label();
			g.addVertex(m);
			ret2.add(m);
			g.addEdge(fresh(), n, m);
			ret += n + " -> " + m + ";";
			i++;
		}
		
		return new Triple<>(ret1, ret2, ret + " }");	
	}
	
	@Override
	public Triple<List<Node<O, A>>, List<Node<O, A>>, String> visit(Unit env, MplAlpha<O, A> e) {
		return makeId(e);
	}

	@Override
	public Triple<List<Node<O, A>>, List<Node<O, A>>, String> visit(Unit env, MplRho<O, A> e) {
		return makeId(e);
	}

	@Override
	public Triple<List<Node<O, A>>, List<Node<O, A>>, String> visit(Unit env, MplLambda<O, A> e) {
		return makeId(e);
	}

	@Override
	public Triple<List<Node<O, A>>, List<Node<O, A>>, String> visit(Unit env, MplTr<O, A> e) {
		Triple<List<Node<O, A>>, List<Node<O, A>>, String> xs = e.t.accept(env, this);
		
//		List<O> ft = e.t.type(ctx).first.accept(env, this);
		List<O> et = e.type(ctx).first.accept(env, this);
		
		List<Node<O,A>> ret1 = new LinkedList<>();
		List<Node<O,A>> ret2 = new LinkedList<>();
		
		String ret = "subgraph cluster" + fresh() + "{ label=\"" + e + "\"; "   + xs.third ;

		int i = 0;
		for (O o : et) {
			Node<O,A> n = new Node<>(e, true, i, o);
			ret += n.label();
			g.addVertex(n);
			ret1.add(n);
			g.addEdge(fresh(), n, xs.first.get(i));			
			ret += n + " -> " + xs.first.get(i) + ";";

			Node<O,A> m = new Node<>(e, false, i, o);
			ret += m.label();
			g.addVertex(n);
			ret2.add(n);
			g.addEdge(fresh(), xs.second.get(i), m);	
			ret += xs.second.get(i) + " -> " + m + ";";

			i++;
		}
		
		for (; i < xs.first.size(); i++) {
			g.addEdge(fresh(), xs.second.get(i), xs.first.get(i));		
			ret += xs.second.get(i) + " -> " + xs.first.get(i) + ";";
		}
		
		return new Triple<>(ret1, ret2, ret + " }");
	}
	
	///////////////

}
