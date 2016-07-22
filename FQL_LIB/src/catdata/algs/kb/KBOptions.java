package catdata.algs.kb;

public class KBOptions {

	public boolean filter_subsumed_by_self = true;
	public boolean unfailing = true;
	public boolean sort_cps = true;
	public boolean horn = false;
	public boolean semantic_ac = true;
	public int iterations = 200000;
	public int red_its = 32;
//	public boolean simplify = true;
	public boolean compose = true;
	
	public KBOptions() {	
	}
	public static KBOptions defaultOptions = new KBOptions();
	
	public KBOptions(boolean unfailing, boolean sort_cps, boolean horn, boolean semantic_ac,
			int iterations, int red_its, boolean filter_subsumed_by_self,/* boolean simplify, */boolean compose) {
		this.unfailing = unfailing;
		this.sort_cps = sort_cps;
		this.horn = horn;
		this.semantic_ac = semantic_ac;
		this.iterations = iterations;
		this.red_its = red_its;
		this.filter_subsumed_by_self = filter_subsumed_by_self;
	//	this.simplify = simplify;
		this.compose = compose;
	}
	
}
