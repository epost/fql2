package catdata.aql.exp;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import catdata.DAG;
import catdata.IntRef;
import catdata.InvisibleException;
import catdata.LineException;
import catdata.Pair;
import catdata.Program;
import catdata.Unit;
import catdata.Util;
import catdata.aql.Pragma;

//TODO: aql does assume unique names
public final class AqlMultiDriver implements Callable<Unit> {

	public static <X> Collection<Pair<String, Kind>> wrapDeps(String s, Exp<X> exp, Program<Exp<?>> prog) {
		Collection<Pair<String, Kind>> ret = new HashSet<>(exp.deps());
		for (String s0 : prog.order) {
			if (s.equals(s0)) {
				break;
			}
			//each expression depends on all the pragmas before it
			if (prog.exps.get(s0).kind().equals(Kind.PRAGMA)) {
				ret.add(new Pair<>(s0, Kind.PRAGMA));
			}
			//each pragma depends on all expressions before it
			if (exp.kind().equals(Kind.PRAGMA)) {
				ret.add(new Pair<>(s0, Kind.PRAGMA));
			}
		}
		return ret;
	}
	
	// n is unchanged if it is equal to old(n) and for every dependency d,
	// unchanged(d)
	// this means that expressions such as 'load from disk' will need to be
	// careful about equality

	@Override
	public synchronized String toString() {
		return "Completed: " + Util.sep(completed, " ") + "\nProcessing: " + Util.sep(processing, " ") + "\nTodo: " + Util.sep(todo, " ");
	}

	public final AqlEnv env = new AqlEnv();

	public final List<String> todo = new LinkedList<>();
	public final List<String> processing = new LinkedList<>();
	public final List<String> completed = new LinkedList<>();

	public final Program<Exp<?>> prog;
	public final String[] toUpdate;
	public final Program<Exp<?>> last_prog;
	public final AqlEnv last_env;

	private List<RuntimeException> exn = new LinkedList<>(); 
	
	boolean stop = false;
	
	public AqlMultiDriver(Program<Exp<?>> prog, String[] toUpdate, Program<Exp<?>> last_prog, AqlEnv last_env) {
		this.prog = prog;
		this.toUpdate = toUpdate;
		this.last_prog = last_prog;
		this.last_env = last_env;

		checkAcyclic();
		env.typing = new AqlTyping(prog);  //TODO aql line exceptions in typing
		init();
		update();
		process();
	}

	private void checkAcyclic() {
		DAG<String> dag = new DAG<>();
		for (String n : prog.order) {
			for (Pair<String, Kind> d : /*wrapDeps(n,*/ prog.exps.get(n).deps() /*, prog) */) { //crushes performance
				if (!prog.order.contains(d.first)) {
					throw new LineException("Undefined dependency: " + d, n, prog.exps.get(n).kind().toString());
				}
				boolean ok = dag.addEdge(n, d.first);
				if (!ok) {
					throw new LineException("Adding dependency on " + d + " causes circularity", n, prog.exps.get(n).kind().toString());
				}
			}
		}
	}
	
	private boolean isEnded() {
		synchronized (ended) {
			return ended.i == Runtime.getRuntime().availableProcessors();
		}
	}
	private void barrier() {
		while (!isEnded()) {
			synchronized (ended) {
				try {
					ended.wait();
				} catch (InterruptedException e) {
					throw new RuntimeException("Please report: driver interrupted while waiting");
				}
			}
		}		
	}
	
	private void update() {
		String s = toString();
		synchronized(toUpdate) {
			toUpdate[0] = s;
		}		
	}

	private IntRef ended = new IntRef(0);
	
	private void process() {
		int numProcs = Runtime.getRuntime().availableProcessors();
		
		for (int i = 0; i < numProcs; i++) {
			new Thread(() -> call()).start();
		}
		barrier();
		
		if (!exn.isEmpty()) { 
			for (RuntimeException t : exn) {
				if (!(t instanceof InvisibleException)) {
					env.exn = t;
				}
			}
			if (env.exn == null) {
				env.exn = new RuntimeException("Anomaly: please report");
			}
			//when uncommented, partial results will not appear
			//throw env.exn; //TODO aql configure behavior, stop on error or not?
		}	
	}

	private Map<String, Boolean> changed = new HashMap<>();

	private void init() {		
		if (last_prog == null) {
			todo.addAll(prog.order);
			return;
		}
		for (String n : prog.order) {
			if (!last_env.defs.keySet().contains(n) || changed(n)) {
				todo.add(n);
			} else {
				Kind k = prog.exps.get(n).kind();
				env.defs.put(n, k, last_env.defs.get(n, k));
				completed.add(n);
			}
		}
	}

	private boolean changed(String n) {
		if (changed.containsKey(n)) {
			return changed.get(n);
		}
		Exp<?> prev = last_prog.exps.get(n);
		if (prev == null) {
			changed.put(n, true);
			return true;
		}
		for (Pair<String, Kind> d : wrapDeps(n, prev, last_prog)) {
			if (changed(d.first)) {
				changed.put(n, true);
				return true;
			}
		}
		boolean b = !prev.equals(prog.exps.get(n));
		changed.put(n, b);
		return b;
	}

	private Unit notifyOfDeath() {
		synchronized (ended) {
			ended.i++;
			ended.notifyAll();
		}
		synchronized (this) { //just in case
			notifyAll();
		}
		return new Unit();
	}
	
	@Override
	public Unit call() {
		String k2 = "";
		String n = "";

		try {
			for (;;) {
				n = null;
				
				synchronized (this) {
					if (stop == true || todo.isEmpty() || Thread.currentThread().isInterrupted()) {
						break;
					} 
					n = nextAvailable();
					if (n == null) {
						update();
						wait();
						continue;
					}
					processing.add(n);
					todo.remove(n);
					update();
				}
				Exp<?> exp = prog.exps.get(n);
				Kind k = exp.kind();
				k2 = k.toString();
				Object val = exp.eval(env);
				if (val == null) {
					throw new RuntimeException("anomaly, please report: null result on " + exp);
				} else if (k.equals(Kind.PRAGMA)) {
					((Pragma) val).execute();
				}
				synchronized (this) {
					env.defs.put(n, k, val);
					processing.remove(n);
					completed.add(n);
					update();
					notifyAll();
				}
			}
		} catch (InterruptedException exp) {
			exn.add(new InvisibleException(exp));			 
		} catch (InvisibleException exp) {
			exn.add(exp);
		} catch (Throwable e) {
			e.printStackTrace();
			synchronized (this)  {
				stop = true;
				exn.add(new LineException(e.getMessage(), n, k2));
				notifyAll();
			}
		}
		
		update();
		return notifyOfDeath();
	}

	private String nextAvailable() {
		outer: for (String s : todo) {
			for (Pair<String, Kind> d : wrapDeps(s, prog.exps.get(s), prog)) {
				if (!completed.contains(d.first)) {
					continue outer;
				}
			}
			return s;
		}
		return null;
	}

}

/* subsumed by multi driver

public static AqlEnv makeEnv(String str,
		Program<Exp<?>> init, String[] toUpdate, String last_str,
		Program<Exp<?>> last_prog, AqlEnv last_env) {
	AqlEnv env = new AqlEnv();
	
	env.type(init);

	Set<String> unchanged = new HashSet<>();
	int i = 0;
	if (last_str != null) { 
		for (String k : init.order) {
			if (i >= last_prog.order.size()) {
				break;
			}
			String v = last_prog.order.get(i);
			if (!v.equals(k)) {
				break;
			} 
			Exp<? extends Object> a = init.exps.get(k);
			Exp<? extends Object> b = last_prog.exps.get(k);
			if (!a.equals(b)) {
				break;
			}
			unchanged.add(k);
			i++;
		}
	}

	// 
	for (String n : init.order) {
		Exp<? extends Object> exp = init.exps.get(n);
		Kind k = exp.kind();

		if (unchanged.contains(n)) {
			env.put(n, k, last_env.get(n, k));
			continue;
		}
		try {
			Object val = exp.eval(env);
			if (val == null) {
				throw new RuntimeException("null result on " + exp);
			}
			env.put(n, k, val);
			env.semantics(n, k);
			if (toUpdate != null) {
				toUpdate[0] = "Last processed: " + k;
			}
		} catch (Throwable t) {
			t.printStackTrace();
			throw new LineException(t.getMessage(), n, "");
		}
	}

	return env;
}

*/

