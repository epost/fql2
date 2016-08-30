package catdata.aql;

import java.util.List;
import java.util.Map;
import java.util.Set;

import catdata.Pair;

public final class Algebra<En,Ty,Sym,Fk,Att,Gen,Sk> {

	public  Map<En,Set<Term<Void,En,Void,Fk,Void,Gen,Void>>> ens;
	
	public  Map<Term<Void,En,Void,Fk,Att,Gen,Sk>, Ty> sks;
	
	//TODO this is wrong, instance is on type side so will have equations like [ ... ] = 3
	public  List<Pair<Term<Void,En,Sym,Void,Void,Void,Term<Void,En,Void,Fk,Att,Gen,Sk>>, 
	                       Term<Void,En,Sym,Void,Void,Void,Term<Void,En,Void,Fk,Att,Gen,Sk>>>> eqs;
	
	public  DP<Ty,En,Sym,Void,Void,Void,Term<Void,En,Void,Fk,Att,Gen,Sk>> talg_eq;

	public  NF<Ty,En,Sym,Void,Void,Void,Term<Void,En,Void,Fk,Att,Gen,Sk>> ens_nf; 
		
}
