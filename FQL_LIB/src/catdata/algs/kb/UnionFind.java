package catdata.algs.kb;

import java.util.Collection;
import java.util.Map;

import catdata.Util;

//WeightedQuickUnionPathCompressionUF a la Robert Sedgewick and Kevin Wayne
public class UnionFind<X>  {
	
    @Override
	public String toString() {
		return "UnionFind [parent=" + parent + ", size=" + size + "]";
	}


	private final Map<X,X> parent;  
    private final Map<X,Integer> size;    

    public UnionFind(Collection<X> xs) {
        parent = Util.id(xs);
        size = Util.constMap(xs, 1);
    }

    private void add(X x) {
    	if (parent.containsKey(x)) {
    		return;
    	}
    	parent.put(x, x);
    	size.put(x, 1);
    }
   
    public X find(X p) {
    	add(p);
        X root = p;
        while (!root.equals(parent.get(root))) {
            root = parent.get(root);
        }
        while (!(p.equals(root))) {
            X newp = parent.get(p);
            parent.put(p, root);
            p = newp;
        }
        return root;
    }

    public boolean connected(X p, X q) {
        return find(p).equals(find(q));
    }

  
     public void union(X p, X q) {
        X rootP = find(p);
        X rootQ = find(q);
        if (rootP.equals(rootQ)) return;

        // make smaller root point to larger one
        if (size.get(rootP) < size.get(rootQ)) {
            parent.put(rootP, rootQ);
            size.put(rootQ, size.get(rootQ) + size.get(rootP));
        } else {
            parent.put(rootQ, rootP);
            size.put(rootP, size.get(rootP) + size.get(rootQ));
        }
    }

	

}
