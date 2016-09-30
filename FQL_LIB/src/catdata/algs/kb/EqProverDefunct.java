package catdata.algs.kb;

import java.util.HashSet;
import java.util.Set;

public abstract class EqProverDefunct<C,V> {
	
	public abstract boolean eq(KBExp<C,V> e1, KBExp<C,V> e2);

	//TODO: arrange by types
	public KBExp<C,V> nf(KBExp<C,V> e) {
		for (KBExp<C,V> e0 : nfs) {
			if (eq(e, e0)) {
				return e0;
			}
		}
		nfs.add(e);
		return e;
	}
	private Set<KBExp<C,V>> nfs = new HashSet<>();
	
	public abstract String printKB(); //print nicely
		
}
