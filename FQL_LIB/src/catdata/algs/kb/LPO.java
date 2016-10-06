package catdata.algs.kb;

import java.util.List;
import java.util.function.Function;

import catdata.Pair;
import catdata.algs.kb.KBExp.KBApp;

public class LPO<C,V> {

	private Function<Pair<C, C>, Boolean> gt;
	
	public LPO (Function<Pair<C, C>, Boolean> gt) {
		this.gt = gt;
	}
	
	public boolean gt(C s, C t) {
		return gt.apply(new Pair<>(s, t));
	}
	
	public boolean gt_lpo(KBExp<C, V> s, KBExp<C, V> t) {
		return gt_lpo1(s, t) || gt_lpo2(s,t);
	}

	public boolean gt_lpo1(KBExp<C, V> s, KBExp<C, V> t) {
		if (s.isVar) {
			return false;
		} else {
			for (KBExp<C, V> si : s.getApp().args) {
				if (si.equals(t) || gt_lpo(si, t)) {
					return true;
				}
			}
			return false;
		}
	}
	
	public boolean gt_lpo2(KBExp<C, V> s, KBExp<C, V> t) {
		if (s.isVar || t.isVar) {
			return false;
		} 
		KBApp<C,V> S = s.getApp();
		KBApp<C,V> T = t.getApp();
		for (KBExp<C, V> ti : T.args) {
			if (!gt_lpo(S, ti)) {
				return false;
			}
		}
		if (S.f.equals(T.f)) {
			return gt_lpo_lex(S.args, T.args);
		} else {
			return gt(S.f, T.f);
		}
	}
	
	public boolean gt_lpo_lex(List<KBExp<C, V>> ss, List<KBExp<C, V>> tt) {
		if (ss.size() != tt.size()) {
			throw new RuntimeException("Anomaly: please report");
		}
		if (ss.isEmpty()) {
			return false;
		}
		KBExp<C, V> s0 = ss.get(0), t0 = tt.get(0);
		if (gt_lpo(s0, t0)) {
			return true;
		} 
		if (!s0.equals(t0)) {
			return false;
		}
		return gt_lpo_lex(ss.subList(1, ss.size()), tt.subList(1, tt.size()));
	}
			
}
