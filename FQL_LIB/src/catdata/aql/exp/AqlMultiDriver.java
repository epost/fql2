package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import catdata.DAG;
import catdata.IntRef;
import catdata.InvisibleException;
import catdata.Pair;
import catdata.Unit;
import catdata.Util;
import catdata.aql.Pragma;
import catdata.ide.LineException;
import catdata.ide.Program;

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
	public String toString() {
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

	private List<RuntimeException> exn = Collections.synchronizedList(new LinkedList<>()); 
	
	boolean stop = false;
	
	public AqlMultiDriver(Program<Exp<?>> prog, String[] toUpdate, Program<Exp<?>> last_prog, AqlEnv last_env) {
		this.prog = prog;
		this.toUpdate = toUpdate;
		this.last_prog = last_prog;
		this.last_env = last_env;

		checkAcyclic();
		env.typing = new AqlTyping(prog);  //TODO aql line exceptions in typing
		init();
		toUpdate[0] = toString();
		process();
	}

	private void checkAcyclic() {
		DAG<String> dag = new DAG<>();
		for (String n : prog.order) {
			for (Pair<String, Kind> d : wrapDeps(n, prog.exps.get(n), prog)) {
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

	List<Future<Unit>> threads = new LinkedList<>();

	private void cancel() {
		for (Future<Unit> thread : threads) {
			if (!thread.isDone() && !thread.isCancelled()) {
				try {
					thread.cancel(true);
				} catch (CancellationException ex) {
				}
			}
		}
	}

	private IntRef ended = new IntRef(0);
	
	private void process() {
		for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
			ExecutorService executor = Executors.newSingleThreadExecutor();
			Future<Unit> future = executor.submit(this);
			threads.add(future);
		}
		for (Future<Unit> thread : threads) {
				try {
					thread.get();
				} catch (Throwable ex) {
					stop = true;
					cancel();
				}
		}
		
		synchronized (ended) {
			while (ended.i < Runtime.getRuntime().availableProcessors()) {
				try {
					ended.wait();
				} catch (InterruptedException e) {
					throw new RuntimeException("Please report: driver interrupted while waiting");
				}
			}
		}
		
		//TODO: aql form a barrier
		//all thread must be dead here
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
			if (changed(n) || !last_env.defs.keySet().contains(n)) {
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

	@Override
	public Unit call() throws Exception {
		String k2 = "";
		String n = "";

		try {
			for (;;) {
				n = null;
				if (Thread.currentThread().isInterrupted() || stop == true) {
					return new Unit();
				}

				synchronized (this) {
					if (todo.isEmpty()) {
						break;
					}
					n = nextAvailable();
					if (n == null) {
						wait();
						continue;
					}
					processing.add(n);
					todo.remove(n);
					toUpdate[0] = toString();
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
					toUpdate[0] = toString();
					notifyAll();
				}
			}
		} catch (InterruptedException exp) {
			exn.add(new InvisibleException(exp));			 
		} catch (InvisibleException exp) {
			exn.add(exp);
		} catch (Throwable e) {
			e.printStackTrace();
			exn.add(new LineException(e.getMessage(), n, k2));
			stop = true;
			cancel();
		}
		
		synchronized (ended) {
			ended.i++;
			ended.notify();
		}
		return new Unit();
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

