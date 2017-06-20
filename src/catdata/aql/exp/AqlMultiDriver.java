package catdata.aql.exp;

import java.util.Collection;
import java.util.Collections;
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
import catdata.aql.AqlOptions;
import catdata.aql.AqlOptions.AqlOption;
import catdata.aql.Kind;
import catdata.aql.Pragma;
import catdata.graph.DAG;

//TODO aql does assume unique names
//TODO aql make sure transforms validate
public final class AqlMultiDriver implements Callable<Unit> {

	public void abort() {
		interruptAll();		
		exn.add(new RuntimeException("Execution interrupted while waiting.  If execution was not aborted manually, please report."));
	}
	
	private void interruptAll() {
		//synchronized(this) {
		//	stop = true;
		//}
		for (Thread t : threads) {
			t.interrupt();
		}
	}

	private static <X> Collection<Pair<String, Kind>> wrapDeps(String s, Exp<X> exp, Program<Exp<?>> prog) {
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

	private final List<String> todo = Collections.synchronizedList(new LinkedList<>());
	private final List<String> processing = Collections.synchronizedList(new LinkedList<>());
	private final List<String> completed =Collections.synchronizedList(new LinkedList<>());

	private final Program<Exp<?>> prog;
	private final String[] toUpdate;
	private final Program<Exp<?>> last_prog;
	private final AqlEnv last_env;

	private final List<RuntimeException> exn = Collections.synchronizedList(new LinkedList<>());

//	boolean stop = false;

	public AqlMultiDriver(Program<Exp<?>> prog, String[] toUpdate, Program<Exp<?>> last_prog, AqlEnv last_env) {
		this.prog = prog;
		this.toUpdate = toUpdate;
		this.last_prog = last_prog;
		this.last_env = last_env;
		//this.env.user_defaults = prog.options;
		this.env.defaults = new AqlOptions(prog.options, null, AqlOptions.initialOptions);
		//System.out.println("pre " + env.defaults.getOrDefault(AqlOption.gui_rows_to_display));
		this.numProcs = (int) this.env.defaults.getOrDefault(AqlOption.num_threads);
		if (numProcs < 1) {
			throw new RuntimeException("num_procs must be > 0");
		}
	}
	
	public void start() {
		checkAcyclic();
		//set the defaults here
		env.typing = new AqlTyping(prog, env.defaults); // TODO aql line exceptions in typing
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
			return ended.i == numProcs;
		}
	}
	
	//static int numProcs = 2; //Runtime.getRuntime().availableProcessors();

	private void barrier() {
		synchronized (ended) {
			while (!isEnded()) {
				try {
					ended.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
					abort();
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

	private final IntRef ended = new IntRef(0);
	private final List<Thread> threads = new LinkedList<>();
	private final int  numProcs;

	private void process() {
		for (int i = 0; i < numProcs; i++) {
			Thread thr = new Thread(this::call);
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

	private final Map<String, Boolean> changed = new HashMap<>();

	private void init() {
		if (last_prog == null || !last_prog.options.equals(prog.options)) {
			todo.addAll(prog.order);
			return;
		}
		for (String n : prog.order) {
			if (!last_env.defs.keySet().contains(n) || changed(n)) {
				todo.add(n);
			} else {
				Kind k = prog.exps.get(n).kind();
				env.defs.put(n, k, last_env.defs.get(n, k));
				env.performance.put(n, last_env.performance.get(n));
				completed.add(n);
			}
		}
	}

	private boolean changed(String n) {
		if (changed.containsKey(n)) {
			return changed.get(n);
		}
		Exp<?> prev = last_prog.exps.get(n);
		if (prev == null || (Boolean) prog.exps.get(n).getOrDefault(env, AqlOption.always_reload)) {
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
			while (true) {
				n = null;

				synchronized (this) {
					if (/*stop == true ||*/ todo.isEmpty() || Thread.currentThread().isInterrupted()) {
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
				long time1 = System.currentTimeMillis();
				Object val = Util.timeout(() -> exp.eval(env), (Long)exp.getOrDefault(env, AqlOption.timeout) * 1000);
				long time2 = System.currentTimeMillis();
				// Object val = exp.eval(env);
				if (val == null) {
					throw new RuntimeException("anomaly, please report: null result on " + exp);
				} else if (k.equals(Kind.PRAGMA)) {
					((Pragma) val).execute();
				}
				synchronized (this) {
					env.defs.put(n, k, val);
					env.performance.put(n, Float.toString((time2 - time1) / (1000f)));
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
		} catch (RuntimeException e) {
			e.printStackTrace();
			synchronized (this) {
				//stop = true;
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
