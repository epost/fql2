package catdata.aql;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import catdata.DAG;
import catdata.Unit;
import catdata.Util;
import catdata.ide.LineException;
import catdata.ide.Program;


public final class AqlMultiDriver implements Callable<Unit> {

	// n is unchanged if it is equal to old(n) and for every dependency d, unchanged(d)
	// this means that expressions such as 'load from disk' will need to be careful about equality

	@Override 
	public String toString() {
		return "Completed: " + Util.sep(completed, " ") 
		   + "\nProcessing: " + Util.sep(processing, " ")
		   + "\nTodo: " + Util.sep(todo, " ");
	}
	
	public final AqlEnv env = new AqlEnv();
	
	public final List<String> todo = new LinkedList<>();
	public final List<String> processing = new LinkedList<>();
	public final List<String> completed = new LinkedList<>();
	
	public final Program<Exp<?>> prog;
	public final String[] toUpdate;
	public final Program<Exp<?>> last_prog;
	public final AqlEnv last_env;
	
	private LineException exn;
	boolean stop = false;
	
	public AqlMultiDriver(Program<Exp<?>> prog, String[] toUpdate, Program<Exp<?>> last_prog, AqlEnv last_env) {
		this.prog = prog;
		this.toUpdate = toUpdate;
		this.last_prog = last_prog;
		this.last_env = last_env;
		
		checkAcyclic();
		init();
		toUpdate[0] = toString();
		//System.out.println(this);
		process();
	}

	private void checkAcyclic() {
		DAG dag = new DAG();
		for (String n : prog.order) {
			for (String d : prog.exps.get(n).deps()) {
				if (!prog.order.contains(d)) {
					throw new LineException("Undefined dependency: " + d, n, prog.exps.get(n).kind().toString());
				}
				boolean ok = dag.addEdge(n, d);			
				if (!ok) {
					throw new LineException("Adding dependency on " + d + " causes circularity", n, prog.exps.get(n).kind().toString());
				}
			}
		}
		System.out.println(dag);
	}

	List<Future<Unit>> threads = new LinkedList<>();
	private void cancel() {
		for (Future<Unit> thread : threads) {
			if (!thread.isDone() && !thread.isCancelled()) {
				try {
					thread.cancel(true);
				} catch (CancellationException ex) { }
			}
		}
	}
	private void process() {
		
			for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
				ExecutorService executor = Executors.newSingleThreadExecutor();
			    Future<Unit> future = executor.submit(this);
			    threads.add(future);
			}
			for (Future<Unit> thread : threads) {
				if (!thread.isDone() && !thread.isCancelled()) {
					try {
						thread.get();
					} catch (Throwable ex) { 
						stop = true;
						cancel();
					}
				}
			}
		
		
		if (exn != null) {
			throw exn;
		}
		
		if (!todo.isEmpty()) {
			throw new RuntimeException("Anomaly, please report: " + todo + " " + this);
		}
	}

	private Map<String, Boolean> changed = new HashMap<>();

	private void init() {
		if (last_prog == null) {
			todo.addAll(prog.order);
			return;
		}
		for (String n : prog.order) {
			if (changed(n)) {
				todo.add(n);
			} else {
				Kind k = prog.exps.get(n).kind();
				env.put(n,k, last_env.get(n, k));
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
		for (String d : prev.deps()) {
			if (changed(d)) {
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

		//System.out.println(Thread.currentThread()+ " start "+ toString());
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
					throw new RuntimeException("null result on " + exp);
				}
				synchronized(this) {
					env.put(n, k, val);
					env.semantics(n, k);
					processing.remove(n);
					completed.add(n);
					toUpdate[0] = toString();
					notifyAll();
				}
			}
		} catch (Throwable thr) {
			thr.printStackTrace();
			exn = new LineException(thr.getMessage(), n, k2);
			throw exn;
		}
			
		return new Unit();
	}

	private String nextAvailable() {
		outer: for (String s : todo) {
			for (String d : prog.exps.get(s).deps()) {
				if (!completed.contains(d)) {
					continue outer;
				}
			}
			return s;
		}
		return null;
	}

	
	
	
	
}
