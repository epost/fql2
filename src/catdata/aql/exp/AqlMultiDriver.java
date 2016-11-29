package catdata.aql.exp;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import catdata.IntRef;
import catdata.LineException;
import catdata.Pair;
import catdata.Program;
import catdata.RuntimeInterruptedException;
import catdata.Unit;
import catdata.Util;
import catdata.aql.Pragma;
import catdata.graph.DAG;

//TODO: aql does assume unique names
public final class AqlMultiDriver implements Callable<Unit> {

	public void abort() {
		interruptAll();		
		exn.add(new RuntimeException("Manual abort"));
	}
	
	private void interruptAll() {
		synchronized(this) {
			stop = true;
		}
		for (Thread t : threads) {
			t.interrupt();
		}
	}

	public static <X> Collection<Pair<String, Kind>> wrapDeps(String s, Exp<X> exp, Program<Exp<?>> prog) {
		Collection<Pair<String, Kind>> ret = new HashSet<>(exp.deps());
		for (String s0 : prog.order) {
			if (s.equals(s0)) {
				break;
			}
			// each expression depends on all the pragmas before it
			if (prog.exps.get(s0).kind().equals(Kind.PRAGMA)) {
				ret.add(new Pair<>(s0, Kind.PRAGMA));
			}
			// each pragma depends on all expressions before it
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
	}
	
	public void start() {
		checkAcyclic();
		env.typing = new AqlTyping(prog); // TODO aql line exceptions in typing
		init();
		update();
		process();
	}

	private void checkAcyclic() {
		DAG<String> dag = new DAG<>();
		for (String n : prog.order) {
			for (Pair<String, Kind> d : /* wrapDeps(n, */ prog.exps.get(n).deps() /* , prog) */) { // crushes
																									// performance
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
		synchronized (ended) {
			while (!isEnded()) {
				try {
					ended.wait();
				} catch (InterruptedException e) {
					abort();
					throw new RuntimeException("Driver interrupted while waiting.  If execution was not aborted manually, please report.");
				}
			}
		}
	}

	private void update() {
		String s = toString();
		synchronized (toUpdate) {
			toUpdate[0] = s;
		}
	}

	private IntRef ended = new IntRef(0);
	private List<Thread> threads = new LinkedList<>();

	private void process() {
		int numProcs = Runtime.getRuntime().availableProcessors();
		for (int i = 0; i < numProcs; i++) {
			Thread thr = new Thread(() -> {
			try {
				call();
			} catch (ThreadDeath d) { }
			});
			threads.add(thr);
			thr.start();
		}
		barrier();

		if (!exn.isEmpty()) {
			for (RuntimeException t : exn) {
				if (!(t instanceof RuntimeInterruptedException)) {
					env.exn = t;
				}
			}
			if (env.exn == null) {
				env.exn = new RuntimeException("Anomaly: please report");
			}
			// when uncommented, partial results will not appear
			// throw env.exn; //TODO aql configure behavior, stop on error or
			// not?
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
		synchronized (this) { 
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
						wait(5000); //just in case
						continue;
					}
					processing.add(n);
					todo.remove(n);
					update();
				}
				Exp<?> exp = prog.exps.get(n);
				Kind k = exp.kind();
				k2 = k.toString();
				Object val = Util.timeout(() -> exp.eval(env), exp.timeout() * 1000);
				// Object val = exp.eval(env);
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
			exn.add(new RuntimeInterruptedException(exp));
		} catch (RuntimeInterruptedException exp) {
			exn.add(exp);
		} catch (ThreadDeath d) { 
			exn.add(new RuntimeInterruptedException(d));
		} catch (Exception e) {
			e.printStackTrace();
			synchronized (this) {
				stop = true;
				exn.add(new LineException(e.getMessage(), n, k2));
				notifyAll();
				interruptAll();
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